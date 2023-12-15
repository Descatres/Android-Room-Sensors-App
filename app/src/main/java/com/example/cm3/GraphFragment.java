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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_graph, container, false);

        lineChart = view.findViewById(R.id.chart);
        setupLineChart();

        firestore = FirebaseFirestore.getInstance();

        // Retrieve real-time data from Firestore
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

    private void setData(ArrayList<Entry> temperatureEntries, ArrayList<Entry> humidityEntries) {
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

        String[] timestamps = new String[temperatureEntries.size()];
        for (int i = 0; i < temperatureEntries.size(); i++) {
            timestamps[i] = String.valueOf(temperatureEntries.get(i).getX()); // Adjust this line based on your timestamp format
        }

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new com.github.mikephil.charting.formatter.IndexAxisValueFormatter(timestamps));
        xAxis.setLabelRotationAngle(45f); // Rotate the x-axis labels if needed

        lineChart.setData(lineData);

        lineChart.setData(lineData);
        lineChart.invalidate(); // Refresh the chart
    }

    private void retrieveFirestoreData() {
        firestore.collection("data")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        return;
                    }

                    if (value != null) {
                        ArrayList<Entry> temperatureEntries = new ArrayList<>();
                        ArrayList<Entry> humidityEntries = new ArrayList<>();

                        for (DocumentChange dc : value.getDocumentChanges()) {
                            QueryDocumentSnapshot document = dc.getDocument();
                            if (document.contains("temperature") && document.contains("humidity")) {
                                Double temperature = document.getDouble("temperature");
                                Double humidity = document.getDouble("humidity");

                                // Check if temperature and humidity values are not null
                                if (temperature != null && humidity != null) {
                                    // Add the data points to the respective arrays
                                    temperatureEntries.add(new Entry(temperatureEntries.size() + 1, temperature.floatValue()));
                                    humidityEntries.add(new Entry(humidityEntries.size() + 1, humidity.floatValue()));
                                } else {
                                    // Handle error
                                    Log.e("GraphFragment", "Temperature or humidity is null");
                                }
                            }
                        }
                        // log 3 first entries
//                        Log.d("GraphFragment", "Temperature: " + temperatureEntries.get(0).getY() + ", " + temperatureEntries.get(1).getY() + ", " + temperatureEntries.get(2).getY());
                        setData(temperatureEntries, humidityEntries);
                    }
                });
    }
}