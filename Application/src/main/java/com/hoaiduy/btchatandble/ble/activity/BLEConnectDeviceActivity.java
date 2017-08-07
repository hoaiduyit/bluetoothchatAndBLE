package com.hoaiduy.btchatandble.ble.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

import com.example.android.bluetoothchat.R;
import com.hoaiduy.btchatandble.DialogUtils;
import com.hoaiduy.btchatandble.ble.BLEService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by hoaiduy2503 on 8/1/2017.
 */

public class BLEConnectDeviceActivity extends Activity {
    private final static String TAG = BLEConnectDeviceActivity.class.getSimpleName();

    TextView txtName, txtAddress, txtState, txtData;
    Button btnConnect, btnDisconnect;
    private String mDeviceName, mDeviceAddress;
    ExpandableListView listService;
    private BLEService mBLE;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    final String LIST_NAME = "NAME";
    final String LIST_UUID = "UUID";
    private ProgressDialog dialog;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBLE = ((BLEService.LocalBinder) service).getService();
            if (!mBLE.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            //auto connect to a chosen device
            mBLE.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBLE = null;
        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BLEService.ACTION_GATT_CONNECTED.equals(action)) {
                dialog.dismiss();
                mConnected = true;
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            } else if (BLEService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                clearUI();
            } else if (BLEService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                displayGattServices(mBLE.getSupportedGattServices());
            } else if (BLEService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(BLEService.EXTRA_DATA));
            }
        }
    };

    private final ExpandableListView.OnChildClickListener servicesListClickListener = new ExpandableListView.OnChildClickListener() {
        @Override
        public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                    int childPosition, long id) {
            if (mGattCharacteristics != null) {
                final BluetoothGattCharacteristic characteristic = mGattCharacteristics.get(groupPosition).get(childPosition);
                final int charaProp = characteristic.getProperties();
                if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                    if (mNotifyCharacteristic != null) {
                        mBLE.setCharacteristicNotification(mNotifyCharacteristic, false);
                        mNotifyCharacteristic = null;
                    }
                    mBLE.readCharacteristic(characteristic);
                }
//                if ((charaProp | BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
//                    if (mNotifyCharacteristic != null) {
//                        mBLE.setCharacteristicNotification(mNotifyCharacteristic, false);
//                        mNotifyCharacteristic = null;
//                    }
//                    characteristic.setValue(0x01, android.bluetooth.BluetoothGattCharacteristic.FORMAT_UINT8, 0);
//                    mBLE.writeCharacteristic(characteristic);
//                }
                if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                    mNotifyCharacteristic = characteristic;
                    mBLE.setCharacteristicNotification(characteristic, true);
                }
                return true;
            }
            return false;
        }

    };

    private void clearUI() {
        listService.setAdapter((SimpleExpandableListAdapter) null);
        txtData.setText("no data");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_connect_other_ble);
        dialog = DialogUtils.getLoadingProgressDialog(this);

        Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        setupUI();

        Intent gattServiceIntent = new Intent(this, BLEService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    private void setupUI() {
        txtName = (TextView) findViewById(R.id.txtName);
        txtAddress = (TextView) findViewById(R.id.txtAddress);
        txtState = (TextView) findViewById(R.id.txtState);
        txtData = (TextView) findViewById(R.id.txtData);
        btnConnect = (Button) findViewById(R.id.btnConnect);
        btnDisconnect = (Button) findViewById(R.id.btnDisconnect);
        listService = (ExpandableListView) findViewById(R.id.listService);

        listService.setOnChildClickListener(servicesListClickListener);

        txtName.setText(mDeviceName);
        txtAddress.setText(mDeviceAddress);

        if (mConnected){
            btnConnect.setVisibility(View.GONE);
            btnDisconnect.setVisibility(View.VISIBLE);
        }else {
            btnConnect.setVisibility(View.VISIBLE);
            btnDisconnect.setVisibility(View.GONE);
        }

        btnConnect.setOnClickListener(v -> {
            mBLE.connect(mDeviceAddress);
            dialog.show();
        });
        btnDisconnect.setOnClickListener(v -> mBLE.disconnect());
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBLE != null) {
            final boolean result = mBLE.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBLE = null;
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(() -> txtState.setText(resourceId));
    }

    private void displayData(String data) {
        if (data != null) {
            txtData.setText(data);
        }
    }

    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData = new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> characteristics = new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                characteristics.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(characteristics);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }

        SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
                this,
                gattServiceData,
                android.R.layout.simple_expandable_list_item_2, new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 },
                gattCharacteristicData, android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 }
        );
        listService.setAdapter(gattServiceAdapter);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BLEService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BLEService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BLEService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BLEService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}