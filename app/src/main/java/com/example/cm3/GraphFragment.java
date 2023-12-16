package com.example.cm3;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class GraphFragment extends Fragment {

    private LineChart lineChart;
    private FirebaseFirestore firestore;

    private Spinner spinnerDataType;

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

        spinnerDataType = view.findViewById(R.id.spinner_data_type);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(), R.array.data_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDataType.setAdapter(adapter);
        spinnerDataType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                // Handle item selection
                filterData(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Do nothing here
            }
        });

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

    private void filterData(int position) {
        switch (position) {
            case 0: // All
                setData(temperatureEntries, temperatureTimeStamps, humidityEntries, humidityTimeStamps, "Temperature", "Humidity");
                break;
            case 1: // Temperature
                setData(temperatureEntries, temperatureTimeStamps, null, null, "Temperature", null);
                break;
            case 2: // Humidity
                setData(humidityEntries, humidityTimeStamps, null, null, null, "Humidity");
                break;
        }
    }

    private void setData(
            ArrayList<Entry> dataSet1,
            ArrayList<Long> timeStamps1,
            ArrayList<Entry> dataSet2,
            ArrayList<Long> timeStamps2,
            String label1,
            String label2
    ) {
        LineDataSet dataSetTemperature = new LineDataSet(dataSet1, label1);
        dataSetTemperature.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSetTemperature.setColor(Color.RED);
        dataSetTemperature.setDrawCircles(true);
        dataSetTemperature.setCircleColor(Color.RED);

        LineDataSet dataSetHumidity = new LineDataSet(dataSet2, label2);
        dataSetHumidity.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSetHumidity.setColor(Color.BLUE);
        dataSetHumidity.setDrawCircles(true);
        dataSetHumidity.setCircleColor(Color.BLUE);

        LineData lineData = new LineData(dataSetTemperature);

        if (dataSet2 != null && timeStamps2 != null && label2 != null) {
            dataSetTemperature.setLabel(label1);
            dataSetHumidity.setLabel(label2);
            lineData = new LineData(dataSetTemperature, dataSetHumidity);
        }

        XAxis xAxis = lineChart.getXAxis();
        String[] timeStampsArray = getFormattedTimeStamps(timeStamps1); // You can modify this to handle combined timestamps if needed
        xAxis.setValueFormatter(new com.github.mikephil.charting.formatter.IndexAxisValueFormatter(timeStampsArray));

        lineChart.setData(lineData);
        lineChart.invalidate(); // Refresh the chart
    }

    private String[] getFormattedTimeStamps(ArrayList<Long> timeStamps) {
        String[] formattedTimeStamps = new String[timeStamps.size()];
        for (int i = 0; i < timeStamps.size(); i++) {
            formattedTimeStamps[i] = formatDate(timeStamps.get(i));
        }
        return formattedTimeStamps;
    }

    private String formatDate(long timeStamp) {
        // Format the timestamp as dd/MM/yy@HH:mm:ss
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy@HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(timeStamp));
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
                                // TODO guardar currenttimemillis no mainactivity e processar a partir daí
                                Long timeStamp = document.getTimestamp("t_stamp").getSeconds() * 1000; // Convert to milliseconds

                                // Check if temperature value is not null
                                if (temperature != null && timeStamp != null) {
                                    temperatureEntries.add(new Entry(temperatureEntries.size() + 1, temperature.floatValue()));
                                    temperatureTimeStamps.add(timeStamp);
                                } else {
                                    Log.e("GraphFragment", "Temperature is null");
                                }
                            }

                            if (document.contains("humidity")) {
                                Double humidity = document.getDouble("humidity");
                                // TODO guardar currenttimemillis no mainactivity e processar a partir daí
                                Long timeStamp = document.getTimestamp("t_stamp").getSeconds() * 1000; // Convert to milliseconds

                                // Check if humidity value is not null
                                if (humidity != null && timeStamp != null) {
                                    humidityEntries.add(new Entry(humidityEntries.size() + 1, humidity.floatValue()));
                                    humidityTimeStamps.add(timeStamp);
                                } else {
                                    Log.e("GraphFragment", "Humidity is null");
                                }
                            }
                        }
//                        setData();
                        filterData(spinnerDataType.getSelectedItemPosition());
                    }
                });
    }
}