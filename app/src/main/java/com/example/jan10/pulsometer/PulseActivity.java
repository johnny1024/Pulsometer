package com.example.jan10.pulsometer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class PulseActivity extends AppCompatActivity {

    public TextView textView = (TextView)findViewById(R.id.textView);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        textView.setText("Raz dwa trzy");
    }
}
