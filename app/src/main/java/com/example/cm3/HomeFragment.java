
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
        btnOptionAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Update the content to show both temperature and humidity
                updateContent(0); // Assuming 0 corresponds to "All"
            }
        });

        // Find the "Temperature" button and set a click listener
        Button btnOptionTemperature = view.findViewById(R.id.btnOption2);
        btnOptionTemperature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Update the content to show only temperature
                updateContent(1); // Assuming 1 corresponds to "Temperature"
            }
        });

        // Find the "Humidity" button and set a click listener
        Button btnOptionHumidity = view.findViewById(R.id.btnOption3);
        btnOptionHumidity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Update the content to show only humidity
                updateContent(2); // Assuming 2 corresponds to "Humidity"
            }
        });

        // Find the "on" button and set a click listener
        Button btnTurnOn = view.findViewById(R.id.btnTurnOn);
        btnTurnOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Change the image to the "on" state
                imageLightBulb.setImageResource(R.drawable.light_on);
            }
        });

        // Find the "off" button and set a click listener
        Button btnTurnOff = view.findViewById(R.id.btnTurnOff);
        btnTurnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Change the image to the "off" state
                imageLightBulb.setImageResource(R.drawable.light_off);
            }
        });

        // Initialize MQTT client
        String serverUri = "tcp://broker.hivemq.com:1883";
        String clientId = "androidClient_" + UUID.randomUUID().toString();
        mqttAndroidClient = new MqttAndroidClient(requireContext(), serverUri, clientId);
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);

        Log.e("HomeFragment", "Connecting to " + serverUri + " with client ID " + clientId);
        // Set callback for handling incoming messages
        mqttAndroidClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                // Handle connection lost
                Log.e("HomeFragment", "Connection lost");
            }

            @Override
            public void messageArrived(String topic, org.eclipse.paho.client.mqttv3.MqttMessage message) {
                Log.e("HomeFragment", "Message arrived");
                // Handle incoming message
                String payload = new String(message.getPayload());
                // Update UI based on the received message
                updateUI(topic, payload);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Log.e("HomeFragment", "Delivery complete");
                // Handle delivery complete
            }
        });

        // Connect to the MQTT broker
        try {
            IMqttToken token = mqttAndroidClient.connect(mqttConnectOptions);
            Log.e("HomeFragment", "Connecting to MQTT broker");
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.e("HomeFragment", "Connection success");
                    // Subscribe to your MQTT topics here
                    subscribeToTopics();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Handle failure
                    Log.e("HomeFragment", "Connection failure");
                }
            });
        } catch (MqttException e) {
            Log.e("HomeFragment", "Connection exception");
            e.printStackTrace();
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Unsubscribe from MQTT topics
        unsubscribeFromTopics();
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

    private void subscribeToTopics() {
        try {
            String temperatureTopic = "randomtemperaturetopic";
            String humidityTopic = "randomhumiditytopic";
            int qos = 1;

            IMqttToken subToken1 = mqttAndroidClient.subscribe(temperatureTopic, qos);
            subToken1.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.e("HomeFragment", "Subscribed to temperature topic");
                    // Handle successful subscription to temperature topic
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.e("HomeFragment", "Failed to subscribe to temperature topic");
                    // Handle failure
                }
            });

            IMqttToken subToken2 = mqttAndroidClient.subscribe(humidityTopic, qos);
            subToken2.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.e("HomeFragment", "Subscribed to humidity topic");
                    // Handle successful subscription to humidity topic
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.e("HomeFragment", "Failed to subscribe to humidity topic");
                    // Handle failure
                }
            });
        } catch (MqttSecurityException e) {
            Log.e("HomeFragment", "security exception", e);
            throw new RuntimeException(e);
        } catch (MqttException e) {
            Log.e("HomeFragment", "mqtt exception", e);
            e.printStackTrace();
        }
    }

    // Update UI based on the received MQTT message
    private void updateUI(String topic, String payload) {
        String temperatureTopic = "randomtemperaturetopic";
        String humidityTopic = "randomhumiditytopic";
        Log.e("HomeFragment", "Topic: " + topic + ", Payload: " + payload);
        // Check the topic and update the corresponding UI element
        if (temperatureTopic.equals(topic)) {
            Log.e("HomeFragment", "Temperature: " + payload);
            // Update temperature UI
            realtimeTemperature.setText(payload);
        } else if (humidityTopic.equals(topic)) {
            // Update humidity UI
            realtimeHumidity.setText(payload);
        }
    }

    private void unsubscribeFromTopics() {
        try {
            String temperatureTopic = "randomtemperaturetopic";
            String humidityTopic = "randomhumiditytopic";

            IMqttToken unsubToken1 = mqttAndroidClient.unsubscribe(temperatureTopic);
            unsubToken1.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // Handle successful unsubscription from temperature topic
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Handle failure
                }
            });

            IMqttToken unsubToken2 = mqttAndroidClient.unsubscribe(humidityTopic);
            unsubToken2.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // Handle successful unsubscription from humidity topic
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Handle failure
                }
            });
        } catch (MqttException e) {
            Log.e("HomeFragment", "Connection exception", e);
            e.printStackTrace();
        }

    }
}