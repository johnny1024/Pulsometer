package com.example.jan10.pulsometer;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
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

    private final Handler mHandler = new Handler();
    private Runnable mTimer2;
    private LineGraphSeries<DataPoint> mSeries2;
    private double graph2LastXValue = 5d;

    private TextView textView;

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
                            textView.setText(String.valueOf(bpm));
                            graph2LastXValue += 1d;
                            mSeries2.appendData(new DataPoint(graph2LastXValue, (double) bpm), true, 40);
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pulse);

        textView = (TextView)findViewById(R.id.textView);

        GraphView graph2 = (GraphView) findViewById(R.id.graph2);
        mSeries2 = new LineGraphSeries<>();
        graph2.addSeries(mSeries2);
        graph2.getViewport().setXAxisBoundsManual(true);
        graph2.getViewport().setMinX(0);
        graph2.getViewport().setMaxX(40);
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

        mTimer2 = new Runnable() {
            @Override
            public void run() {
//                graph2LastXValue += 1d;
//                mSeries2.appendData(new DataPoint(graph2LastXValue, getRandom()), true, 40);
//                mHandler.postDelayed(this, 200);
            }
        };
        mHandler.postDelayed(mTimer2, 1000);
    }

    @Override
    protected void onPause() {
//        mHandler.removeCallbacks(mTimer1);
//        bluetoothThread.interrupt();
        mHandler.removeCallbacks(mTimer2);
        super.onPause();
    }
}
