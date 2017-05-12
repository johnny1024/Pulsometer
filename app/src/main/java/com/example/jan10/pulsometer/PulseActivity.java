package com.example.jan10.pulsometer;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.IOException;

public class PulseActivity extends Activity {

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
                        }
                    });
                    System.out.println("Rev " + counter + "\t" + bpm);
                    ++counter;
                }
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
    }

    @Override
    protected void onStart() {
        super.onStart();
        bluetoothThread.start();
    }

    //TODO onDestroy remove thread
    @Override
    protected void onDestroy() {
        super.onDestroy();
        bluetoothThread.interrupt();
    }
}
