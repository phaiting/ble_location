package com.htpu.bletracker;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.htpu.bletracker.data.Ble;
import com.htpu.bletracker.databinding.BleControlFragmentBinding;


import org.parceler.Parcels;

import static com.htpu.bletracker.BleControlActivity.BLE_CONTROL_KEY;
import static com.htpu.bletracker.BleService.ACTION_GATT_CONNECTED;
import static com.htpu.bletracker.BleService.ACTION_GATT_DISCONNECTED;
import static com.htpu.bletracker.BleService.ACTION_GATT_SERVICES_DISCOVERED;

public class BleControlFragment extends Fragment {
    public static final String TAG = BleControlFragment.class.getSimpleName();
    private BleService mService;
    private boolean mBound;

    private Button mBtnConnect;
    private boolean mConnected;
    private BleControlFragmentBinding mBinding;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case ACTION_GATT_CONNECTED:
                    mConnected = true;
                    mBtnConnect.setText(getResources().getString(R.string.label_disconnect));
                    break;
                case ACTION_GATT_DISCONNECTED:
                    mConnected = false;
                    mBtnConnect.setText(getResources().getString(R.string.label_connect));
                    break;
                case ACTION_GATT_SERVICES_DISCOVERED:
                    break;
                default:
                    break;
            }
        }
    };

    public BleControlFragment() {

    }

    public static BleControlFragment newInstance() {
        return new BleControlFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.ble_control_fragment, container, false);
        Bundle bundle = getArguments();
        Ble item = Parcels.unwrap(bundle.getParcelable(BLE_CONTROL_KEY));
        mBinding.setData(item);
        Intent serviceIntent = new Intent(getActivity(), BleService.class);
        getActivity().bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBtnConnect  = view.findViewById(R.id.btn_connect);
        mBtnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mService != null) {
                    if (!mConnected) {
                        mService.connect(mBinding.getData().getDeviceId());
                    } else {
                        mService.disconnect();
                    }
                }
            }
        });
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            BleService.LocalBinder binder = (BleService.LocalBinder) iBinder;
            mService = binder.getService();
            mBound = true;
            if (!mService.init()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                getActivity().finish();
            }
            Log.i(TAG, " onServiceConnected");

            // Automatically connects to the device upon successful start-up initialization.
            mService.connect(mBinding.getData().getDeviceId());
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mService = null;
            mBound = false;
            Log.i(TAG, " onServiceDisconnected");
        }
    };

    @Override
    public void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, makeIntentFilter());
        if (mService != null) {
            String deviceId = mBinding.getData().getDeviceId();
            mService.connect(deviceId);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mBound) {
            getActivity().unbindService(mConnection);
            mBound = false;
        }
    }


    private static IntentFilter makeIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_GATT_CONNECTED);
        intentFilter.addAction(ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(ACTION_GATT_SERVICES_DISCOVERED);
        return intentFilter;
    }
}
