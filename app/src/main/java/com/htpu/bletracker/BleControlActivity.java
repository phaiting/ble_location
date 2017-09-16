package com.htpu.bletracker;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.htpu.bletracker.util.ActivityUtils;

public class BleControlActivity extends AppCompatActivity {

    public static final String BLE_CONTROL_KEY = "ble";
    public static final String EXTRA_BLE_CONTROL = "extra_ble";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_control);

        BleControlFragment fragment = (BleControlFragment) getSupportFragmentManager().findFragmentById(R.id.ble_control_frame);
        if (fragment == null) {
            fragment = BleControlFragment.newInstance();
            final Intent i = getIntent();
            Bundle bundle = i.getBundleExtra(EXTRA_BLE_CONTROL);
            fragment.setArguments(bundle);
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), fragment, R.id.ble_control_frame);
        }
    }
}
