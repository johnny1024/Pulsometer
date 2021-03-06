package com.example.jan10.pulsometer;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class ConnectingActivity extends Activity {

    // SETUP, STARTING BT, ETC

    private final static int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter mBluetoothAdapter;
    private ListView listView;
    private ArrayList<BluetoothDevice> devices;
    private ArrayList<String> deviceNames;
    private ArrayAdapter<String> adapter;
    private BluetoothSocket socket;
    private final UUID myUUID;

    public ConnectingActivity() {
        myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connecting);
        this.setTitle("Choose a bluetooth device");

        listView = (ListView)findViewById(R.id.list);
        deviceNames = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, R.layout.list_row, deviceNames);
        listView.setAdapter(adapter);

        listView.setClickable(true);

        // CONNECTING
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?>adapter, View view, int position, long id) {
                BluetoothDevice device = devices.get(position);

                try {
                    socket = device.createInsecureRfcommSocketToServiceRecord(myUUID);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    Toast.makeText(ConnectingActivity.this, "Trying to connect", Toast.LENGTH_LONG).show();
                    socket.connect();
                    BluetoothSocketHandler.setSocket(socket);
                    Intent goToPulseIntent = new Intent(ConnectingActivity.this, PulseActivity.class);
                    startActivity(goToPulseIntent);
                } catch (IOException e) {
                    Toast.makeText(ConnectingActivity.this, "Failed to connect", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        });
        // END OF CONNECTING

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        else {
            discover();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            discover();
        }
        else {
            Intent intent = new Intent(this, ConnectActivity.class);
            startActivity(intent);
        }
    }

    // SEARCHING FOR DEVICES

    private void discover() {
        devices = new ArrayList<>();
        devices.addAll(mBluetoothAdapter.getBondedDevices());

        if (devices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : devices) {
                String deviceName = device.getName();
                //String deviceHardwareAddress = device.getAddress(); // MAC address
                adapter.add(deviceName);
//                devices.add(device);
            }
        }
    }

    public void refresh(View view) {
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                //String deviceHardwareAddress = device.getAddress(); // MAC address
                adapter.add(deviceName);
                devices.add(device);
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(mReceiver);
        } catch (IllegalArgumentException e) {
            // nop
        }
    }
}
