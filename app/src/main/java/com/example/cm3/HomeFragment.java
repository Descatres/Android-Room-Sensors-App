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

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.UUID;

public class HomeFragment extends Fragment {

    private TextView textTemperature;
    private TextView realtimeTemperature;
    private TextView textHumidity;
    private TextView realtimeHumidity;
    private ImageView imageLightBulb;

    private MqttAndroidClient mqttAndroidClient;
    private String serverUri = "tcp://broker.hivemq.com:1883";

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
            updateContent(0); // Assuming 0 corresponds to "All"
        });

        // Find the "Temperature" button and set a click listener
        Button btnOptionTemperature = view.findViewById(R.id.btnOption2);
        btnOptionTemperature.setOnClickListener(v -> {
            // Update the content to show only temperature
            updateContent(1); // Assuming 1 corresponds to "Temperature"
        });

        // Find the "Humidity" button and set a click listener
        Button btnOptionHumidity = view.findViewById(R.id.btnOption3);
        btnOptionHumidity.setOnClickListener(v -> {
            // Update the content to show only humidity
            updateContent(2); // Assuming 2 corresponds to "Humidity"
        });

        // Find the "on" button and set a click listener
        Button btnTurnOn = view.findViewById(R.id.btnTurnOn);
        btnTurnOn.setOnClickListener(v -> {
            // Change the image to the "on" state
            imageLightBulb.setImageResource(R.drawable.light_on);
        });

        // Find the "off" button and set a click listener
        Button btnTurnOff = view.findViewById(R.id.btnTurnOff);
        btnTurnOff.setOnClickListener(v -> {
            // Change the image to the "off" state
            imageLightBulb.setImageResource(R.drawable.light_off);
        });


        String clientId = MqttClient.generateClientId();
        mqttAndroidClient = new MqttAndroidClient(getActivity(), serverUri, clientId);
        Log.e("HomeFragment", "Connecting to " + serverUri + " with client ID " + clientId);

        // Connect to the MQTT broker and subscribe to a topic
        connectAndSubscribe();

        return view;
    }

    public void connectAndSubscribe() {
        // Initialize MQTT client
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);
        try {
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d("HomeFragment", "Conectado ao broker MQTT com sucesso!");

                    subscribeToTopic("temperature");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.e("HomeFragment", "Falha ao conectar ao broker MQTT: " + exception.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e("HomeFragment", "Exceção ao conectar ao broker MQTT: " + e.getMessage());
        }
    }

    private void subscribeToTopic(String topic) {
        try {
            int qos = 1;
            IMqttToken subToken = mqttAndroidClient.subscribe(topic, qos);
            subToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.e("HomeFragment", "Subscribed to topic: " + topic);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.e("HomeFragment", "Failed to subscribe to topic: " + topic, exception);
                }
            });
        } catch (MqttException e) {
            Log.e("HomeFragment", "Subscription exception", e);
        }
    }


    private void disconnect() {
        try {
            if (mqttAndroidClient != null && mqttAndroidClient.isConnected()) {
                mqttAndroidClient.disconnect();
                Log.e("HomeFragment", "Disconnected from the MQTT broker");
            }
        } catch (MqttException e) {
            Log.e("HomeFragment", "Disconnection exception", e);
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Disconnect from the MQTT broker
        disconnect();
    }
}

