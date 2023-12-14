package com.example.cm3;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class DataRepository {
    private static DataRepository instance;
    private final MutableLiveData<String> temperatureData = new MutableLiveData<>();
    private final MutableLiveData<String> humidityData = new MutableLiveData<>();

    private DataRepository() {
        // Private constructor to enforce Singleton pattern
    }

    public static DataRepository getInstance() {
        if (instance == null) {
            instance = new DataRepository();
        }
        return instance;
    }

    public LiveData<String> getTemperatureData() {
        return temperatureData;
    }

    public LiveData<String> getHumidityData() {
        return humidityData;
    }

    public void updateData(String topic, String payload) {
        // Update LiveData based on the received MQTT data
        String temperatureTopic = "randomtemperaturetopic";
        String humidityTopic = "randomhumiditytopic";
//        Log.e("DataRepository", "Received data on topic: " + topic + ", payload: " + payload);
        if (temperatureTopic.equals(topic)) {
            temperatureData.postValue(payload);
        } else if (humidityTopic.equals(topic)) {
            humidityData.postValue(payload);
        }
    }
}
