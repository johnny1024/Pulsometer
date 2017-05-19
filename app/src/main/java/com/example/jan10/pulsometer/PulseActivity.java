package com.example.jan10.pulsometer;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.DataInputStream;
import java.io.IOException;

public class PulseActivity extends Activity {

    private final Handler handler = new Handler();
    private Runnable graphUpdater;
    private LineGraphSeries<DataPoint> graphData;
    private double graphLastX = 5d;
    private IncrementalMean buffer;
    private TextView pulseValue;
    private TextView pulseInfo;

    private Thread bluetoothThread = new Thread() {
        @Override
        public void run() {
            String threadName = Thread.currentThread().getName();
            System.out.println("Hello " + threadName);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(PulseActivity.this, "Bluetooth thread started", Toast.LENGTH_LONG).show();
                }
            });

            BluetoothSocket socket = BluetoothSocketHandler.getSocket();
            DataInputStream inputStream;
            try {
                inputStream = new DataInputStream(socket.getInputStream());
                long counter = 0;
                byte[] bytes = new byte[2];
                while (!interrupted()) {
                    inputStream.readFully(bytes);
                    final short bpm = java.nio.ByteBuffer.wrap(bytes).order(java.nio.ByteOrder.LITTLE_ENDIAN).getShort();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pulseValue.setText("Aktualny odczyt czujnika: " + bpm);
                            buffer.addValue((double) bpm);
                        }
                    });
                    System.out.println(threadName + " [" + counter + "]\t" + bpm);
                    ++counter;
                }
                System.out.println("Bluetooth thread interrupted");
                socket.close();
            } catch (IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(PulseActivity.this, "Lost bluetooth connection", Toast.LENGTH_LONG).show();
                    }
                });
                finish();
            }
        }
    };

    private String getPulseInfo(double bpm) {
        String pulseMessage;
        if (bpm < 45) {
            pulseMessage = "Stanowczo zbyt niski puls!";
            pulseInfo.setTextColor(Color.RED);
        } else if (bpm < 60) {
            pulseMessage = "Niski puls";
            pulseInfo.setTextColor(Color.YELLOW);
        } else if (bpm < 85) {
            pulseMessage = "Normalny puls";
            pulseInfo.setTextColor(Color.GREEN);
        } else if (bpm < 100) {
            pulseMessage = "Wysoki puls";
            pulseInfo.setTextColor(Color.YELLOW);
        } else {
            pulseMessage = "Stanowczo zbyt wysoki puls!";
            pulseInfo.setTextColor(Color.RED);
        }
        return pulseMessage;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pulse);

        pulseValue = (TextView)findViewById(R.id.textView);
        pulseInfo = (TextView)findViewById(R.id.textViewPulseInfo);
        buffer = new IncrementalMean();

        GraphView graph = (GraphView) findViewById(R.id.graph);
        graphData = new LineGraphSeries<>();
        graph.addSeries(graphData);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(40);
        bluetoothThread.start();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bluetoothThread.interrupt();
    }

    @Override
    protected void onResume() {
        super.onResume();

        graphUpdater = new Runnable() {
            @Override
            public void run() {
                graphLastX += 1d;
                final double avgBpm = buffer.getMean();
                graphData.appendData(new DataPoint(graphLastX, avgBpm), true, 40);
                buffer.clear();
                pulseInfo.setText(getPulseInfo(avgBpm));
                handler.postDelayed(this, 1000);
            }
        };
        handler.postDelayed(graphUpdater, 1000);
    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(graphUpdater);
        super.onPause();
    }
}
