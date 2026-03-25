//管理蓝牙连接
package com.example.printerfeature;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Set;

public class DeviceListActivity extends Activity {

    private ProgressDialog loadingDialog;

    // 1.定义需要的权限
    private static final String[] RUNTIME_PERMISSIONS = {
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private BluetoothAdapter bluetoothAdapter;
    private DeviceAdapter adapter;
    private ArrayList<BluetoothDevice> deviceList = new ArrayList<>();
    private ArrayList<String> statusList = new ArrayList<>();
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 适配沉浸式状态栏
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        window.setStatusBarColor(Color.WHITE);

        setContentView(R.layout.activity_device_list);

        listView = findViewById(R.id.listView);
        ImageButton btnBack = findViewById(R.id.btnBack);
        MaterialButton btnRefresh = findViewById(R.id.btnRefresh);

        adapter = new DeviceAdapter();
        listView.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());
        btnRefresh.setOnClickListener(v -> {
            if (hasPermissions()) {
                deviceList.clear();
                statusList.clear();
                adapter.notifyDataSetChanged();
                initBluetoothLogic();
            } else {
                ActivityCompat.requestPermissions(this, RUNTIME_PERMISSIONS, 1001);
            }
        });

        // 2. 检查权限
        if (hasPermissions()) {
            initBluetoothLogic();
        } else {
            // 弹出系统权限请求对话框
            ActivityCompat.requestPermissions(this, RUNTIME_PERMISSIONS, 1001);
        }

        // 3. 点击连接逻辑
        listView.setOnItemClickListener((parent, view, position, id) -> {
            BluetoothDevice selectedDevice = deviceList.get(position);

            // 连接前先把设备名字存起来
            printerSDK.printerName = selectedDevice.getName();

            if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
            }
            connectToPrinter(selectedDevice.getAddress());
        });
    }

    // 检查权限的方法
    private boolean hasPermissions() {
        for (String permission : RUNTIME_PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    // 处理权限申请结果
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 用户同意了权限，开始初始化
                initBluetoothLogic();
            } else {
                Toast.makeText(this, "未获得蓝牙权限，无法扫描", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 蓝牙初始化逻辑
    private void initBluetoothLogic() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "设备不支持蓝牙", Toast.LENGTH_SHORT).show();
            return;
        }

        // 注册扫描广播
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);

        // A. 读取已配对设备
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices != null) {
            for (BluetoothDevice device : pairedDevices) {
                addDeviceIfMatch(device, "[已配对]");
            }
        }

        // B. 开始搜索附近新设备
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        bluetoothAdapter.startDiscovery();
        Toast.makeText(this, "正在搜索附近打印机...", Toast.LENGTH_SHORT).show();
    }

    private void addDeviceIfMatch(BluetoothDevice device, String status) {
        try {
            String name = device.getName();
            String address = device.getAddress();

            // 过滤：精臣打印机是 字母+数字 开头
            if (name != null && name.matches("^[A-Z][0-9].*")) {
                // 检查是否已经存在相同名称的设备
                for (BluetoothDevice existingDevice : deviceList) {
                    if (existingDevice.getAddress().equals(address)) {
                        return;
                    }
                }

                deviceList.add(device);
                statusList.add(status);
                adapter.notifyDataSetChanged();
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null) {
                    addDeviceIfMatch(device, "[发现新设备]");
                }
            }
        }
    };

    private void showLoading(String message) {
        if (loadingDialog == null) {
            loadingDialog = new ProgressDialog(this);
            loadingDialog.setCancelable(false);
            loadingDialog.setCanceledOnTouchOutside(false);
        }
        loadingDialog.setMessage(message);
        loadingDialog.show();
    }

    private void hideLoading() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    private void connectToPrinter(String address) {
        showLoading("正在连接打印机...");
        new Thread(() -> {
            int result = printerSDK.api.connectBluetoothPrinter(address);
            runOnUiThread(() -> {
                hideLoading();
                if (result == 0) {
                    Toast.makeText(this, "连接成功", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "连接失败：" + result, Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(receiver);
        } catch (Exception e) {
        }
        if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
    }

    private class DeviceAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return deviceList.size();
        }

        @Override
        public Object getItem(int position) {
            return deviceList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(DeviceListActivity.this).inflate(R.layout.item_device, parent, false);
            }
            BluetoothDevice device = deviceList.get(position);
            TextView tvName = convertView.findViewById(R.id.tvDeviceName);
            TextView tvAddress = convertView.findViewById(R.id.tvDeviceAddress);
            TextView tvStatus = convertView.findViewById(R.id.tvDeviceStatus);

            tvName.setText(device.getName());
            tvAddress.setText(device.getAddress());
            tvStatus.setText(statusList.get(position));

            return convertView;
        }
    }
}
