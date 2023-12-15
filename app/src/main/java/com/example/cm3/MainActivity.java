package com.example.cm3;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowInsetsController;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import androidx.lifecycle.ViewModelProvider;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private HomeFragment homeFragment;
    private GraphFragment graphFragment;
    private AlertsFragment alertsFragment;
    private MqttAndroidClient mqttAndroidClient;
    private final String humidityTopic = "randomhumiditytopic";
    private final String temperatureTopic = "randomtemperaturetopic";
    private DataRepository dataRepository;
    private final Map<Integer, Fragment> fragmentMap = new HashMap<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        hideSystemUI();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        homeFragment = new HomeFragment();
        graphFragment = new GraphFragment();
        alertsFragment = new AlertsFragment();

        fragmentMap.put(R.id.action_screen1, homeFragment);
        fragmentMap.put(R.id.action_screen2, graphFragment);
        fragmentMap.put(R.id.action_screen3, alertsFragment);

        // default fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, homeFragment)
                .commit();

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = fragmentMap.get(item.getItemId());

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }

            return true;
        });

        db = FirebaseFirestore.getInstance();
        dataRepository = DataRepository.getInstance();
        MqttViewModel mqttViewModel = new ViewModelProvider(this).get(MqttViewModel.class);
        mqttViewModel.setDataRepository(dataRepository);

        String serverUri = "tcp://broker.hivemq.com:1883";
        String clientId = "androidClient_" + UUID.randomUUID().toString();
        mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), serverUri, clientId);
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttAndroidClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                // Handle connection lost
                Log.e("MainActivity", "Connection lost");
                updateMqttData(humidityTopic, "Connection Lost");
                updateMqttData(temperatureTopic, "Connection Lost");

            }

            @Override
            public void messageArrived(String topic, org.eclipse.paho.client.mqttv3.MqttMessage message) {
                // Log.e("MainActivity", "Message arrived" + message.toString());
                String payload = new String(message.getPayload());
                updateMqttData(topic, payload);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                // Log.e("MainActivity", "Delivery complete");
            }
        });

        connectMQTT();
    }

    @Override
    protected void onStart() {
        super.onStart();
        hideSystemUI();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // unsubscribe from MQTT topics
        unsubscribeFromTopics();
        disconnectMQTT();
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
                    subscribeToTopics();
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

    private void subscribeToTopics() {
        try {
            int qos = 1;

            IMqttToken subToken1 = mqttAndroidClient.subscribe(temperatureTopic, qos);
            subToken1.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // Log.e("MainActivity", "Subscribed to temperature topic");
                    // Handle successful subscription to temperature topic
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Log.e("MainActivity", "Failed to subscribe to temperature topic");
                    // Handle failure
                }
            });

            IMqttToken subToken2 = mqttAndroidClient.subscribe(humidityTopic, qos);
            subToken2.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // Log.e("MainActivity", "Subscribed to humidity topic");
                    // Handle successful subscription to humidity topic
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Log.e("MainActivity", "Failed to subscribe to humidity topic");
                    // Handle failure
                }
            });
        } catch (MqttSecurityException e) {
            // Log.e("MainActivity", "security exception", e);
            throw new RuntimeException(e);
        } catch (MqttException e) {
            // Log.e("MainActivity", "mqtt exception", e);
            e.printStackTrace();
        }
    }

    private void updateMqttData(String topic, String payload) {
        // update DataRepository with received MQTT data
        // Log.e("MainActivity", "Received message on topic: " + topic + ", payload: " + payload);
        dataRepository.updateData(topic, payload);

        // convert payload to double
        double payloadDouble = Double.parseDouble(payload);

        if (topic.equals(temperatureTopic)) {
            saveDataToFirestore(topic, payloadDouble);
        } else if (topic.equals(humidityTopic)) {
            saveDataToFirestore(topic, payloadDouble);
        }
    }

    private void disconnectMQTT() {
        try {
            if (mqttAndroidClient != null && mqttAndroidClient.isConnected()) {
                mqttAndroidClient.disconnect();
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void unsubscribeFromTopics() {
        try {

            IMqttToken unsubToken1 = mqttAndroidClient.unsubscribe(temperatureTopic);
            unsubToken1.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // Log.e("MainActivity", "Unsubscribed from temperature topic");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Log.e("MainActivity", "Failed to unsubscribe from temperature topic");
                }
            });

            IMqttToken unsubToken2 = mqttAndroidClient.unsubscribe(humidityTopic);
            unsubToken2.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // Log.e("MainActivity", "Unsubscribed from humidity topic");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Log.e("MainActivity", "Failed to unsubscribe from humidity topic");
                }
            });
        } catch (MqttException e) {
            // Log.e("MainActivity", "Connection exception", e);
            e.printStackTrace();
        }

    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        WindowInsetsController insetsController = decorView.getWindowInsetsController();

        if (insetsController != null) {
            insetsController.hide(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);

        }
    }

    private void saveDataToFirestore(String topic, Double payload) {
        Map<String, Object> data = new HashMap<>();

        if (topic.equals(temperatureTopic)) {
            data.put("temperature", payload);
            data.put("t_stamp", FieldValue.serverTimestamp());
        } else if (topic.equals(humidityTopic)) {
            data.put("humidity", payload);
            data.put("t_stamp", FieldValue.serverTimestamp());
        }

        db.collection("data")
                .add(data)
                .addOnSuccessListener(documentReference -> {
                    Log.d("Firestore", "DocumentSnapshot added with ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.w("Firestore", "Error adding document", e);
                });
    }
}