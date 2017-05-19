package com.example.jan10.pulsometer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class ConnectActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        this.setTitle("Pulsometer");
    }

    public void processToNextAcitvity(View view) {
        Intent intent = new Intent(this, ConnectingActivity.class);
        startActivity(intent);
    }
}
