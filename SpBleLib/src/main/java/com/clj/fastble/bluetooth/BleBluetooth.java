package com.clj.fastble.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.clj.fastble.conn.BleConnector;
import com.clj.fastble.conn.BleGattCallback;
import com.clj.fastble.event.ConnectEvent;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.exception.ConnectException;
import com.clj.fastble.scan.MacScanCallback;
import com.clj.fastble.scan.NameScanCallback;
import com.clj.fastble.scan.PeriodScanCallback;
import com.clj.fastble.utils.BleLog;

import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * 蓝牙操作类
 * <p>
 * 通过initial单例创建控制器
 * <p>
 * (1.0只适用控制单个设备)多设备则需同步问题，
 * private HashMap<String, Integer>       mConnectionState = new HashMap<>();
 * private HashMap<String, BluetoothGatt> mConnectionGatt  = new HashMap<>();
 * private ArrayList<String> mBluetoothDeviceAddresss;
 */
public class BleBluetooth {

    private static final String CONNECT_CALLBACK_KEY = "connect_key";
    public static final  String READ_RSSI_KEY        = "rssi_key";

    public static final int STATE_DISCONNECTED        = 0;
    public static final int STATE_SCANNING            = 1;
    public static final int STATE_CONNECTING          = 2;
    public static final int STATE_CONNECTED           = 3;
    public static final int STATE_SERVICES_DISCOVERED = 4;

    private int connectionState = STATE_DISCONNECTED;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt    bluetoothGatt;
    private Handler handler = new Handler(Looper.getMainLooper());

    private HashMap<String, BluetoothGattCallback> mBleCallbackMap   = new HashMap<>();//Key = 用户自定义！删除回调使用
    private HashMap<String, BluetoothGatt>         mBluetoothGattMap = new HashMap(); // Key = address

    private Context context;

    private static BleBluetooth sInstance; //单例

    public static void initial(Context context) {
        sInstance = new BleBluetooth();
        sInstance.context = context;

        sInstance.mBleCallbackMap = new HashMap<>();
        sInstance.mBluetoothGattMap = new HashMap<>();
        sInstance.bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        sInstance.bluetoothAdapter = sInstance.bluetoothManager.getAdapter();
    }

    private BleBluetooth() {
    }

    public BleBluetooth(Context context) {
        this.context = context = context.getApplicationContext();
        bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
    }

    public static BleBluetooth getInstance(Context context) {
        if (sInstance == null) {
            initial(context);
        }
        return sInstance;
    }

    public BluetoothManager getBluetoothManager(Context context) {
        if (sInstance == null) {
            initial(context);
        }
        return bluetoothManager;
    }

    public BluetoothAdapter getBluetoothAdapter(Context context) {
        if (sInstance == null) {
            initial(context);
        }
        return bluetoothAdapter;
    }

    public BleConnector newBleConnector() {
        return new BleConnector(this);
    }

    public boolean isInScanning() {
        return connectionState == STATE_SCANNING;
    }

    public boolean isConnectingOrConnected() {
        return connectionState >= STATE_CONNECTING;
    }

    public boolean isConnected() {
        return connectionState >= STATE_CONNECTED;
    }

    public boolean isServiceDiscovered() {
        return connectionState == STATE_SERVICES_DISCOVERED;
    }


    private void addConnectGattCallback(String key, BleGattCallback callback) {
        mBleCallbackMap.put(key, callback);
    }

    public void addGattCallback(String uuid, BluetoothGattCallback callback) {
        mBleCallbackMap.put(uuid, callback);
    }

    public synchronized void removeConnectGattCallback() {
        BleLog.i("-----removeConnectGattCallback-------");
        mBleCallbackMap.remove(CONNECT_CALLBACK_KEY);
    }

    public synchronized void removeGattCallback(String callbackKey) {
        BleLog.i("-----removeGattCallback-------");
        mBleCallbackMap.remove(callbackKey);
    }

    public void removeAllCallback() {
        mBleCallbackMap.clear();
    }

    public boolean removeGattCallback(BluetoothGattCallback callback) {
        Iterator<String> iterator = mBleCallbackMap.keySet().iterator();
        while (iterator.hasNext()) {
            if (callback.equals(mBleCallbackMap.get(iterator.next()))) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    public void clearCallback() {
        mBleCallbackMap.clear();
    }

    public BluetoothGattCallback getGattCallback(String uuid) {
        if (TextUtils.isEmpty(uuid))
            return null;
        return mBleCallbackMap.get(uuid);
    }

    public boolean startLeScan(BluetoothAdapter.LeScanCallback callback) {
        boolean success = bluetoothAdapter.startLeScan(callback);
        if (success) {
            connectionState = STATE_SCANNING;
        }
        return success;
    }

    public boolean startLeScan(PeriodScanCallback callback) {
        callback.setBleBluetooth(this).notifyScanStarted();
        boolean success = bluetoothAdapter.startLeScan(callback);
        if (success) {
            connectionState = STATE_SCANNING;
        } else {
            callback.removeHandlerMsg();//开始扫描失败
        }
        return success;
    }

    public void stopScan(BluetoothAdapter.LeScanCallback callback) {
        if (callback instanceof PeriodScanCallback) {
            ((PeriodScanCallback) callback).removeHandlerMsg();//stopScan
        }
        bluetoothAdapter.stopLeScan(callback);
        if (connectionState == STATE_SCANNING) {
            connectionState = STATE_DISCONNECTED;
        }
    }


    public synchronized BluetoothGatt connect(final BluetoothDevice device,
                                              final String callbackKey,
                                              final boolean autoConnect,
                                              final BleGattCallback callback) {
        BleLog.i("connect name: " + device.getName()
                         + "\nmac: " + device.getAddress()
                         + "\nautoConnect: " + autoConnect);
        addConnectGattCallback(callbackKey, callback);
        BluetoothGatt bluetoothGatt = device.connectGatt(context, autoConnect, coreGattCallback);
        mBluetoothGattMap.put(device.getAddress(), bluetoothGatt);
        return bluetoothGatt;
    }

    public synchronized BluetoothGatt connect(final String address,
                                              final String key,
                                              final boolean autoConnect,
                                              final BleGattCallback callback) {
        BleLog.i("connect address: " + address
                         + "\nautoConnect: " + autoConnect);
        addConnectGattCallback(key, callback);
        BluetoothDevice device        = bluetoothAdapter.getRemoteDevice(address);
        BluetoothGatt   bluetoothGatt = device.connectGatt(context, autoConnect, coreGattCallback);
        mBluetoothGattMap.put(address, bluetoothGatt);
        return bluetoothGatt;
    }

    public boolean scanNameAndConnect(String name, final String callbackKey, long time_out, final boolean autoConnect, final BleGattCallback callback) {
        if (TextUtils.isEmpty(name)) {
            if (callback != null) {
                callback.onNotFoundDevice();
            }
            return false;
        }
        return startLeScan(new NameScanCallback(name, time_out, false) {

            @Override
            public void onDeviceFound(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        connect(device, callbackKey, autoConnect, callback);
                    }
                });
            }

            @Override
            public void onDeviceNotFound() {
                if (callback != null) {
                    callback.onNotFoundDevice();
                }
            }
        });
    }

    public boolean scanMacAndConnect(String mac, final String callbackKey, long time_out, final boolean autoConnect, final BleGattCallback callback) {
        if (TextUtils.isEmpty(mac)) {
            if (callback != null) {
                callback.onNotFoundDevice();
            }
            return false;
        }
        return startLeScan(new MacScanCallback(mac, time_out) {

            @Override
            public void onDeviceFound(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        connect(device, callbackKey, autoConnect, callback);
                    }
                });
            }

            @Override
            public void onDeviceNotFound() {
                if (callback != null) {
                    callback.onNotFoundDevice();
                }
            }
        });
    }

    public boolean fuzzySearchNameAndConnect(String name, final String callbackKey, long time_out, final boolean autoConnect, final BleGattCallback callback) {
        if (TextUtils.isEmpty(name)) {
            if (callback != null) {
                callback.onNotFoundDevice();
            }
            return false;
        }
        return startLeScan(new NameScanCallback(name, time_out, true) {

            @Override
            public void onDeviceFound(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                if (callback != null) {
                    callback.onFoundDevice(device);
                }
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        connect(device, callbackKey, autoConnect, callback);
                    }
                });
            }

            @Override
            public void onDeviceNotFound() {
                if (callback != null) {
                    callback.onNotFoundDevice();
                }
            }
        });
    }

    public boolean refreshDeviceCache() {
        try {
            final Method refresh = BluetoothGatt.class.getMethod("refresh");
            if (refresh != null) {
                final boolean success = (Boolean) refresh.invoke(getBluetoothGatt());
                BleLog.i("Refreshing result: " + success);
                return success;
            }
        } catch (Exception e) {
            BleLog.i("An exception occured while refreshing device", e);
        }
        return false;
    }

    public void disConnect() {
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
        }
    }

    public void closeBluetoothGatt() {
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
        }

        if (bluetoothGatt != null) {
            refreshDeviceCache();
        }

        if (bluetoothGatt != null) {
            bluetoothGatt.close();
        }
    }

    public void enableBluetoothIfDisabled() {
        if (!isBlueEnable()) {
            enableBluetooth();
        }
    }

    public boolean isBlueEnable() {
        return bluetoothAdapter.isEnabled();
    }

    public void enableBluetooth() {
        bluetoothAdapter.enable();
    }

    public void disableBluetooth() {
        bluetoothAdapter.disable();
    }

    private void runOnMainThread(Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            handler.post(runnable);
        }
    }

    public Context getContext() {
        return context;
    }

    public BluetoothManager getBluetoothManager() {
        return bluetoothManager;
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return bluetoothAdapter;
    }

    public BluetoothGatt getBluetoothGatt() {
        return bluetoothGatt;
    }

    public int getConnectionState() {
        return connectionState;
    }

    private BleGattCallback coreGattCallback = new BleGattCallback() {

        @Override
        public void onNotFoundDevice() {
            BleLog.i("coreGattCallback：onNotFoundDevice ");
        }

        @Override
        public void onFoundDevice(BluetoothDevice device) {
            BleLog.i("coreGattCallback：onFoundDevice ");
        }

        @Override
        public void onConnectSuccess(BluetoothGatt gatt, int status) {
            BleLog.i("coreGattCallback：onConnectSuccess ");

            bluetoothGatt = gatt;
            Iterator iterator = mBleCallbackMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                Object    call  = entry.getValue();
                if (call instanceof BleGattCallback) {
                    ((BleGattCallback) call).onConnectSuccess(gatt, status);
                }
            }
        }

        @Override
        public synchronized void onConnectDisconnected(BleException exception) {
            BleLog.i("coreGattCallback：onConnectDisconnected ");
            EventBus.getDefault().post(new ConnectEvent(false));

            bluetoothGatt = null;
            //Iterator iterator = mBleCallbackMap.entrySet().iterator();
            //while (iterator.hasNext()) {
            //    Map.Entry entry = (Map.Entry) iterator.next();
            //    Object    call  = entry.getValue();
            //    if (call instanceof BleGattCallback) {
            //        ((BleGattCallback) call).onConnectDisconnected(exception);
            //    }
            //}
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            BleLog.i("coreGattCallback：onConnectionStateChange "
                             + '\n' + "status: " + status
                             + '\n' + "newState: " + newState
                             + '\n' + "thread: " + Thread.currentThread().getId());

            if (newState == BluetoothGatt.STATE_CONNECTED) {
                connectionState = STATE_CONNECTED;
                onConnectSuccess(gatt, status);

            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                connectionState = STATE_DISCONNECTED;
                onConnectDisconnected(new ConnectException(gatt, status));

            } else if (newState == BluetoothGatt.STATE_CONNECTING) {
                connectionState = STATE_CONNECTING;
            }

            Iterator iterator = mBleCallbackMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                Object    call  = entry.getValue();
                if (call instanceof BluetoothGattCallback) {
                    ((BluetoothGattCallback) call).onConnectionStateChange(gatt, status, newState);
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            BleLog.i("coreGattCallback：onServicesDiscovered ");

            connectionState = STATE_SERVICES_DISCOVERED;
            Iterator iterator = mBleCallbackMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                Object    call  = entry.getValue();
                if (call instanceof BluetoothGattCallback) {
                    ((BluetoothGattCallback) call).onServicesDiscovered(gatt, status);
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            BleLog.i("coreGattCallback：onCharacteristicRead ");

            Iterator iterator = mBleCallbackMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                Object    call  = entry.getValue();
                if (call instanceof BluetoothGattCallback) {
                    ((BluetoothGattCallback) call).onCharacteristicRead(gatt, characteristic, status);
                }
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            BleLog.i("coreGattCallback：onCharacteristicWrite ");

            Iterator iterator = mBleCallbackMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                Object    call  = entry.getValue();
                if (call instanceof BluetoothGattCallback) {
                    ((BluetoothGattCallback) call).onCharacteristicWrite(gatt, characteristic, status);
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            BleLog.i("coreGattCallback：onCharacteristicChanged ");

            Iterator iterator = mBleCallbackMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                Object    call  = entry.getValue();
                if (call instanceof BluetoothGattCallback) {
                    ((BluetoothGattCallback) call).onCharacteristicChanged(gatt, characteristic);
                }
            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            BleLog.i("coreGattCallback：onDescriptorRead ");

            Iterator iterator = mBleCallbackMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                Object    call  = entry.getValue();
                if (call instanceof BluetoothGattCallback) {
                    ((BluetoothGattCallback) call).onDescriptorRead(gatt, descriptor, status);
                }
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            BleLog.i("coreGattCallback：onDescriptorWrite ");

            Iterator iterator = mBleCallbackMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                Object    call  = entry.getValue();
                if (call instanceof BluetoothGattCallback) {
                    ((BluetoothGattCallback) call).onDescriptorWrite(gatt, descriptor, status);
                }
            }
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            BleLog.i("coreGattCallback：onReliableWriteCompleted ");

            Iterator iterator = mBleCallbackMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                Object    call  = entry.getValue();
                if (call instanceof BluetoothGattCallback) {
                    ((BluetoothGattCallback) call).onReliableWriteCompleted(gatt, status);
                }
            }
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            BleLog.i("coreGattCallback：onReadRemoteRssi ");

            Iterator iterator = mBleCallbackMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                Object    call  = entry.getValue();
                if (call instanceof BluetoothGattCallback) {
                    ((BluetoothGattCallback) call).onReadRemoteRssi(gatt, rssi, status);
                }
            }
        }
    };

}
