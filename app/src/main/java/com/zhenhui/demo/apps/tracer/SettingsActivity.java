package com.zhenhui.demo.apps.tracer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.zhenhui.demo.apps.tracer.permission.PermissionCheckActivity;
import com.zhenhui.demo.apps.tracer.service.TrackingService;

import androidx.core.content.ContextCompat;

public class SettingsActivity extends PermissionCheckActivity {

    private Button buttonStart;

    private Button buttonStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonStart = findViewById(R.id.button_start);
        buttonStop = findViewById(R.id.button_stop);

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService();
            }
        });

        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService();
            }
        });

    }

    public void startService() {
        Intent intent = new Intent(this, TrackingService.class);
        ContextCompat.startForegroundService(this, intent);
    }

    public void stopService() {
        Intent intent = new Intent(this, TrackingService.class);
        stopService(intent);
    }
}
