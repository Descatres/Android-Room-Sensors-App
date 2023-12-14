package com.example.cm3;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

public class AlertsFragment extends Fragment {

    private MqttViewModel mqttViewModel;

    private EditText editTextTempMin;
    private EditText editTextTempMax;
    private EditText editTextHumidityMin;
    private EditText editTextHumidityMax;

    private static final String CHANNEL_ID = "MyChannel";
    private static final int CHECK_INTERVAL = 5000; // Check every 5 seconds

    private float prevTemp = Float.NaN;
    private float prevHumidity = Float.NaN;

    private final Handler handler = new Handler(Looper.getMainLooper());

    public AlertsFragment() {
        //
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alerts, container, false);


        // Initialize ViewModel
        mqttViewModel = new ViewModelProvider(requireActivity()).get(MqttViewModel.class);

        // Initialize UI elements
        editTextTempMin = view.findViewById(R.id.editTextTempMin);
        editTextTempMax = view.findViewById(R.id.editTextTempMax);
        editTextHumidityMin = view.findViewById(R.id.editTextHumidityMin);
        editTextHumidityMax = view.findViewById(R.id.editTextHumidityMax);

        TextView textViewMinMaxMessage = view.findViewById(R.id.textViewMinMaxMessage);
        createNotificationChannel();

        // Observe changes in ViewModel data
        mqttViewModel.getTemperature().observe(getViewLifecycleOwner(), temperature -> checkThresholds());

        mqttViewModel.getHumidity().observe(getViewLifecycleOwner(), humidity -> checkThresholds());

        return view;
    }

    private void checkThresholds() {
        // Get current values from ViewModel
        String currentTemp = mqttViewModel.getTemperature().getValue();
        String currentHumidity = mqttViewModel.getHumidity().getValue();

        // Get user input values
        float tempMin = Float.parseFloat(editTextTempMin.getText().toString().isEmpty() ? "-1000" : editTextTempMin.getText().toString());
        float tempMax = Float.parseFloat(editTextTempMax.getText().toString().isEmpty() ? "1000" : editTextTempMax.getText().toString());
        float humidityMin = Float.parseFloat(editTextHumidityMin.getText().toString().isEmpty() ? "-1000" : editTextHumidityMin.getText().toString());
        float humidityMax = Float.parseFloat(editTextHumidityMax.getText().toString().isEmpty() ? "1000" : editTextHumidityMax.getText().toString());

        // Validate min and max values
        if (validateMinMaxValues(tempMin, tempMax) || validateMinMaxValues(humidityMin, humidityMax)) {
            showMinMaxMessage(true);
            return;
        } else {
            showMinMaxMessage(false);
        }


        // Check temperature thresholds
        checkThreshold("Temperature", currentTemp, tempMin, tempMax, prevTemp);
        // Check humidity thresholds
        checkThreshold("Humidity", currentHumidity, humidityMin, humidityMax, prevHumidity);

        // Schedule the next check after a delay
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkThresholds();
            }
        }, CHECK_INTERVAL);
    }

    private void checkThreshold(String parameter, String currentValue, float min, float max, float prevValue) {
        if (Float.isNaN(min) || Float.isNaN(max)) {
            return;
        }

        float current = parseFloatWithDefault(currentValue);
        if (Float.isNaN(current)) {
            return;
        }

        if ((current < min || current > max) && current != prevValue) {
            showNotification(parameter + " Alert", parameter + " value is (" + currentValue + ")");
            if (parameter.equals("Temperature")) {
                prevTemp = current;
            } else if (parameter.equals("Humidity")) {
                prevHumidity = current;
            }
        }
    }

    private float parseFloatWithDefault(String str) {
        try {
            return Float.parseFloat(str);
        } catch (NumberFormatException e) {
            return Float.NaN;
        }
    }

    private boolean validateMinMaxValues(float min, float max) {
        return !(min < max);
    }

    private void showMinMaxMessage(boolean show) {
        TextView textViewMinMaxMessage = requireView().findViewById(R.id.textViewMinMaxMessage);
        textViewMinMaxMessage.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showNotification(String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(requireContext());
        if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: call ActivityCompat#requestPermissions here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            return;
        }
        notificationManager.notify(1, builder.build());
    }

    private void createNotificationChannel() {
        CharSequence name = "MyChannel";
        String description = "Channel for my app";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);

        NotificationManager notificationManager = requireContext().getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Remove any remaining callbacks to avoid memory leaks
        handler.removeCallbacksAndMessages(null);
    }
}