
package com.example.cm3;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

import java.util.UUID;

public class HomeFragment extends Fragment {

    private TextView textTemperature;
    private TextView realtimeTemperature;
    private TextView textHumidity;
    private TextView realtimeHumidity;
    private ImageView imageLightBulb;

    private final String ledTopic = "randomon_offtopic";
    private MqttAndroidClient mqttAndroidClient;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Find the TextViews for temperature and humidity
        textTemperature = view.findViewById(R.id.textTemperature);
        realtimeTemperature = view.findViewById(R.id.realtimeTemperature);
        textHumidity = view.findViewById(R.id.textHumidity);
        realtimeHumidity = view.findViewById(R.id.realtimeHumidity);

        // Find the ImageView for the light bulb
        imageLightBulb = view.findViewById(R.id.imageLightBulb);

        // Find the "All" button and set a click listener
        Button btnOptionAll = view.findViewById(R.id.btnOption1);
        btnOptionAll.setOnClickListener(v -> {
            // Update the content to show both temperature and humidity
            updateContent(0);
        });

        // Find the "Temperature" button and set a click listener
        Button btnOptionTemperature = view.findViewById(R.id.btnOption2);
        btnOptionTemperature.setOnClickListener(v -> {
            // Update the content to show only temperature
            updateContent(1);
        });

        // Find the "Humidity" button and set a click listener
        Button btnOptionHumidity = view.findViewById(R.id.btnOption3);
        btnOptionHumidity.setOnClickListener(v -> {
            // Update the content to show only humidity
            updateContent(2);
        });

        String serverUri = "tcp://broker.hivemq.com:1883";
        String clientId = "androidClient_" + UUID.randomUUID().toString();
        mqttAndroidClient = new MqttAndroidClient(getContext(), serverUri, clientId);
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);

        connectMQTT();

        // Find the "on" button and set a click listener
        Button btnTurnOn = view.findViewById(R.id.btnTurnOn);
        btnTurnOn.setOnClickListener(v -> {
            // Change the image to the "on" state
            imageLightBulb.setImageResource(R.drawable.light_on);
            // Publish the "on" message to the LED topic
            publishToLedTopic("on");
        });

        // Find the "off" button and set a click listener
        Button btnTurnOff = view.findViewById(R.id.btnTurnOff);
        btnTurnOff.setOnClickListener(v -> {
            // Change the image to the "off" state
            imageLightBulb.setImageResource(R.drawable.light_off);
            // Publish the "off" message to the LED topic
            publishToLedTopic("off");
        });

        MqttViewModel mqttViewModel = new ViewModelProvider(requireActivity()).get(MqttViewModel.class);

        // update UI when new MQTT data is received
        mqttViewModel.getTemperature().observe(getViewLifecycleOwner(), temperature -> {
//            Log.e("HomeFragment", "Temperature data changed: " + temperature);
            realtimeTemperature.setText(temperature);
        });

        mqttViewModel.getHumidity().observe(getViewLifecycleOwner(), humidity -> {
//            Log.e("HomeFragment", "Humidity data changed: " + humidity);
            String humidityString = humidity + " %";
            realtimeHumidity.setText(humidityString);
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void connectMQTT() {
        try {
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);

            IMqttToken token = mqttAndroidClient.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // Subscribe to MQTT
                    // Log.e("MainActivity", "Connected to MQTT broker");
//                    subscribeToTopics();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Log.e("MainActivity", "Failed to connect to MQTT broker");
//                    unsubscribeFromTopics();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void updateContent(int selectedOption) {
        // Update the content based on the selected option
        switch (selectedOption) {
            case 0: // "All"
                textTemperature.setVisibility(View.VISIBLE);
                realtimeTemperature.setVisibility(View.VISIBLE);
                textHumidity.setVisibility(View.VISIBLE);
                realtimeHumidity.setVisibility(View.VISIBLE);
                break;
            case 1: // "Temperature"
                textTemperature.setVisibility(View.VISIBLE);
                realtimeTemperature.setVisibility(View.VISIBLE);
                textHumidity.setVisibility(View.GONE);
                realtimeHumidity.setVisibility(View.GONE);
                break;
            case 2: // "Humidity"
                textTemperature.setVisibility(View.GONE);
                realtimeTemperature.setVisibility(View.GONE);
                textHumidity.setVisibility(View.VISIBLE);
                realtimeHumidity.setVisibility(View.VISIBLE);
                break;
            default:
                // Handle unexpected case
                break;
        }
    }

    private void publishToLedTopic(String message) {

        Log.e("HomeFragment", "Publishing message to LED topic: " + message);
        try {
            // Check if the MQTT client is connected
            if (mqttAndroidClient != null && mqttAndroidClient.isConnected()) {
                // Publish the message to the LED topic
                mqttAndroidClient.publish(ledTopic, message.getBytes(), 0, false);
                // Check if it worked
                if (mqttAndroidClient.isConnected()) {
                    Log.e("HomeFragment", "Message published to LED topic");
                } else {
                    Log.e("HomeFragment", "MQTT client not connected");
                }
            } else {
                Log.e("HomeFragment", "MQTT client not connected");
            }
        } catch (MqttException e) {
            Log.e("HomeFragment", "Error publishing message to LED topic", e);
        }
    }


}