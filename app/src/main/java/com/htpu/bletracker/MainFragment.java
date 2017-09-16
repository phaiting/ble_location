package com.htpu.bletracker;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.htpu.bletracker.data.Ble;

import org.parceler.Parcels;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.htpu.bletracker.BleControlActivity.BLE_CONTROL_KEY;
import static com.htpu.bletracker.BleControlActivity.EXTRA_BLE_CONTROL;

public class MainFragment extends Fragment implements BleItemListener {
    private final String TAG = MainFragment.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private RecyclerListAdapter mAdapter;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private boolean mScanning;
    private Handler mHandler;

    // TODO request permission
    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    public MainFragment() {

    }

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new RecyclerListAdapter(getActivity(), this);
        mHandler = new Handler();

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(getActivity(), R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            getActivity().finish();
            return;
        }

        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
    }

    private void scanLeDevice(final boolean enable) {

        if (enable) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    setLoadingIndicator(false);
                    mScanning = false;
                    mBluetoothLeScanner.stopScan(mLeScanCallback);
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothLeScanner.startScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothLeScanner.stopScan(mLeScanCallback);
        }
    }

    // Device scan callback.
    private ScanCallback mLeScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, final ScanResult result) {
            super.onScanResult(callbackType, result);
            Log.i(TAG, "onScanResult: " + result);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (result != null) {
                        String rssi = String.valueOf(result.getRssi());
                        String name = result.getDevice().getName();
                        if (name == null || name.isEmpty()) {
                            name = getResources().getString(R.string.ble_unknown_device);
                        }
                        String addr = result.getDevice().getAddress();
                        Ble item = new Ble(rssi, name,addr);
                        mAdapter.addItem(item);
                    }
                }
            });
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.main_fragment, container, false   );

        mRecyclerView =  root.findViewById(R.id.ble_list);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        final SwipeRefreshLayout swipeRefreshLayout = root.findViewById(R.id.refresh_layout);
        swipeRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(getActivity(), R.color.colorPrimary),
                ContextCompat.getColor(getActivity(),R.color.colorAccent),
                ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark)
        );

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mAdapter.clear();
                scanLeDevice(true);
            }
        });

        return root;
    }

    private void setLoadingIndicator(final boolean active) {
        if (getView() == null) {
            return;
        }

        final SwipeRefreshLayout swipeRefreshLayout = getView().findViewById(R.id.refresh_layout);
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(active);
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        scanLeDevice(false);
    }

    @Override
    public void onBleItemClick(Ble clickedItem) {
        checkNotNull(clickedItem);

        final Intent i = new Intent(getActivity(), BleControlActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable(BLE_CONTROL_KEY, Parcels.wrap(clickedItem));
        i.putExtra(EXTRA_BLE_CONTROL, bundle);

        if (mScanning) {
            mScanning = false;
            mBluetoothLeScanner.stopScan(mLeScanCallback);
        }
        startActivity(i);
    }
}
