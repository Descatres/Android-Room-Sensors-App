package com.example.cm3;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class GraphFragment extends Fragment {

    private LineChart lineChart;
    private FirebaseFirestore firestore;

    private ArrayList<Entry> temperatureEntries = new ArrayList<>();
    private ArrayList<Entry> humidityEntries = new ArrayList<>();
    private ArrayList<Long> temperatureTimeStamps = new ArrayList<>();
    private ArrayList<Long> humidityTimeStamps = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_graph, container, false);

        lineChart = view.findViewById(R.id.chart);
        setupLineChart();

        firestore = FirebaseFirestore.getInstance();
        retrieveFirestoreData();

        return view;
    }

    private void setupLineChart() {
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setPinchZoom(true);
        lineChart.setDoubleTapToZoomEnabled(true);
        lineChart.getXAxis().setDrawGridLines(false);

        Description description = new Description();
        description.setText("Temperature (ºC) and Humidity (%) Over Time (s)");
        lineChart.setDescription(description);


        Legend legend = lineChart.getLegend();
        legend.setForm(Legend.LegendForm.LINE);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f); // Minimum value on the Y-axis
        leftAxis.setAxisMaximum(100f); // Maximum value on the Y-axis

        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false); // Disable the right Y-axis

        lineChart.invalidate(); // Refresh the chart
    }

    private void setData() {
        LineDataSet temperatureDataSet = new LineDataSet(temperatureEntries, "Temperature");
        temperatureDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        temperatureDataSet.setColor(Color.RED);
        temperatureDataSet.setDrawCircles(true);
        temperatureDataSet.setCircleColor(Color.RED);

        LineDataSet humidityDataSet = new LineDataSet(humidityEntries, "Humidity");
        humidityDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        humidityDataSet.setColor(Color.BLUE);
        humidityDataSet.setDrawCircles(true);
        humidityDataSet.setCircleColor(Color.BLUE);

        LineData lineData = new LineData(temperatureDataSet, humidityDataSet);

        XAxis xAxis = lineChart.getXAxis();
        String[] temperatureTimeStampsArray = getFormattedTimeStamps(temperatureTimeStamps);
        String[] humidityTimeStampsArray = getFormattedTimeStamps(humidityTimeStamps);

        xAxis.setValueFormatter(new com.github.mikephil.charting.formatter.IndexAxisValueFormatter(temperatureTimeStampsArray));

        lineChart.setData(lineData);
        lineChart.invalidate(); // Refresh the chart
    }

    private String[] getFormattedTimeStamps(ArrayList<Long> timeStamps) {
        String[] formattedTimeStamps = new String[timeStamps.size()];
        for (int i = 0; i < timeStamps.size(); i++) {
            // Format the timestamp as needed
            // For example, you can use SimpleDateFormat or convert to a readable format
            formattedTimeStamps[i] = String.valueOf(timeStamps.get(i));
        }
        return formattedTimeStamps;
    }

    private void retrieveFirestoreData() {
        firestore.collection("data")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        return;
                    }

                    if (value != null) {
                        temperatureEntries.clear();         // Clear previous temperature entries
                        humidityEntries.clear();            // Clear previous humidity entries
                        temperatureTimeStamps.clear();      // Clear previous temperature timestamps
                        humidityTimeStamps.clear();         // Clear previous humidity timestamps

                        for (DocumentChange dc : value.getDocumentChanges()) {
                            QueryDocumentSnapshot document = dc.getDocument();
                            if (document.contains("temperature")) {
                                Double temperature = document.getDouble("temperature");
                                Long timeStamp = document.getTimestamp("t_stamp").getSeconds() * 1000; // Convert to milliseconds

                                // Check if temperature value is not null
                                if (temperature != null) {
                                    temperatureEntries.add(new Entry(temperatureEntries.size() + 1, temperature.floatValue()));
                                    temperatureTimeStamps.add(timeStamp);
                                } else {
                                    // Handle the case where temperature is null
                                    Log.e("GraphFragment", "Temperature is null");
                                }
                            }

                            if (document.contains("humidity")) {
                                Double humidity = document.getDouble("humidity");
                                Long timeStamp = document.getTimestamp("t_stamp").getSeconds() * 1000; // Convert to milliseconds

                                // Check if humidity value is not null
                                if (humidity != null) {
                                    humidityEntries.add(new Entry(humidityEntries.size() + 1, humidity.floatValue()));
                                    humidityTimeStamps.add(timeStamp);
                                } else {
                                    // Handle the case where humidity is null
                                    Log.e("GraphFragment", "Humidity is null");
                                }
                            }
                        }
                        setData();
                    }
                });
    }
}