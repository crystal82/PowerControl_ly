package com.clj.fastble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCallback;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import com.clj.fastble.bluetooth.BleBluetooth;
import com.clj.fastble.conn.BleCharacterCallback;
import com.clj.fastble.conn.BleGattCallback;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.exception.hanlder.DefaultBleExceptionHandler;
import com.clj.fastble.scan.ListScanCallback;
import com.clj.fastble.scan.PeriodScanCallback;
import com.clj.fastble.utils.BleLog;

import java.util.ArrayList;

/**
 * BLE Manager
 * <p>
 * 蓝牙管理器，作用仅仅是调用全局application单例创建
 * <p>
 * scanDevice(ListScanCallback callback)
 * -----ListScanCallback：蓝牙扫描回调，
 */
public class BleManager {

    private Context                    mContext;
    private BleBluetooth               bleBluetooth;
    private DefaultBleExceptionHandler bleExceptionHandler;

    public BleManager(Context context) {
        this.mContext = context;
        if (bleBluetooth == null) {
            bleBluetooth = new BleBluetooth(context);//BleBluetooth.getInstance(context);
        }
        bleExceptionHandler = new DefaultBleExceptionHandler(context);
    }

    public BleBluetooth getBleBluetooth() {
        return bleBluetooth;
    }

    public void removeCallback(String callbackKey) {
        bleBluetooth.removeGattCallback(callbackKey);
    }

    public boolean removeCallback(BluetoothGattCallback callback) {
        return bleBluetooth.removeGattCallback(callback);
    }

    public void removeAllCallback() {
        bleBluetooth.removeAllCallback();
    }

    /**
     * handle Exception Information
     */
    public void handleException(BleException exception) {
        bleExceptionHandler.handleException(exception);
    }

    /**
     * scan device around
     *
     * @param callback
     */
    public boolean scanDevice(PeriodScanCallback callback) {
        return bleBluetooth.startLeScan(callback);
    }

    /**
     * stop scan device around
     *
     * @param callback
     */
    public void stopScan(PeriodScanCallback callback) {
        bleBluetooth.stopScan(callback);
    }

    /**
     * connect a searched device
     *
     * @param device      searched device
     * @param autoConnect
     * @param callback
     */
    public void connectDevice(BluetoothDevice device,
                              String callbackKey,
                              boolean autoConnect,
                              BleGattCallback callback) {
        if (device == null) {
            if (callback != null) {
                callback.onNotFoundDevice();
            }
        } else {
            bleBluetooth.connect(device, callbackKey, autoConnect, callback);
        }
    }

    /**
     * connect a searched device
     *
     * @param address
     * @param autoConnect
     * @param callback
     */
    public void connectDevice(String address,
                              String callbackKey,
                              boolean autoConnect,
                              BleGattCallback callback) {
        if (address == null) {
            if (callback != null) {
                callback.onNotFoundDevice();
            }
        } else {
            bleBluetooth.connect(address, callbackKey, autoConnect, callback);
        }
    }

    /**
     * scan a known name device, then connect
     *
     * @param deviceName  known name
     * @param time_out    timeout
     * @param autoConnect
     * @param callback
     * @return
     */
    public boolean scanNameAndConnect(String deviceName,
                                      String callbackKey,
                                      long time_out,
                                      boolean autoConnect,
                                      BleGattCallback callback) {
        return bleBluetooth.scanNameAndConnect(deviceName, callbackKey, time_out, autoConnect, callback);
    }

    /**
     * scan a known mca device, then connect
     *
     * @param deviceMac   known mac
     * @param time_out    timeout
     * @param autoConnect
     * @param callback
     * @return
     */
    public boolean scanMacAndConnect(String deviceMac,
                                     String callbackKey,
                                     long time_out,
                                     boolean autoConnect,
                                     BleGattCallback callback) {
        return bleBluetooth.scanMacAndConnect(deviceMac, callbackKey, time_out, autoConnect, callback);
    }

    /**
     * fuzzy search name
     *
     * @param fuzzyName
     * @param time_out
     * @param autoConnect
     * @param callback
     * @return
     */
    public boolean fuzzySearchNameAndConnect(String fuzzyName,
                                             String callbackKey,
                                             long time_out,
                                             boolean autoConnect,
                                             BleGattCallback callback) {
        return bleBluetooth.fuzzySearchNameAndConnect(fuzzyName, callbackKey, time_out, autoConnect, callback);
    }

    /**
     * notify
     *
     * @param uuid_service
     * @param uuid_notify
     * @param callback
     * @return
     */
    public boolean notify(String uuid_service,
                          String uuid_notify,
                          BleCharacterCallback callback) {
        return bleBluetooth.newBleConnector()
                .withUUIDString(uuid_service, uuid_notify, null)
                .enableCharacteristicNotify(callback, uuid_notify);
    }

    /**
     * indicate
     *
     * @param uuid_service
     * @param uuid_indicate
     * @param callback
     * @return
     */
    public boolean indicate(String uuid_service,
                            String uuid_indicate,
                            BleCharacterCallback callback) {
        return bleBluetooth.newBleConnector()
                .withUUIDString(uuid_service, uuid_indicate, null)
                .enableCharacteristicIndicate(callback, uuid_indicate);
    }

    /**
     * write
     *
     * @param uuid_service
     * @param uuid_write
     * @param data
     * @param callback
     * @return
     */
    public boolean writeDevice(String uuid_service,
                               String uuid_write,
                               byte[] data,
                               BleCharacterCallback callback) {
        return bleBluetooth.newBleConnector()
                .withUUIDString(uuid_service, uuid_write, null)
                .writeCharacteristic(data, callback, uuid_write);
    }

    /**
     * read
     *
     * @param uuid_service
     * @param uuid_read
     * @param callback
     * @return
     */
    public boolean readDevice(String uuid_service,
                              String uuid_read,
                              BleCharacterCallback callback) {
        return bleBluetooth.newBleConnector()
                .withUUIDString(uuid_service, uuid_read, null)
                .readCharacteristic(callback, uuid_read);
    }

    /**
     * get state
     */
    public void getBluetoothState() {
        BleLog.i("ConnectionState:  " + bleBluetooth.getConnectionState()
                         + "\nisInScanning: " + bleBluetooth.isInScanning()
                         + "\nisConnected: " + bleBluetooth.isConnected()
                         + "\nisServiceDiscovered: " + bleBluetooth.isServiceDiscovered());
    }

    /**
     * refresh Device Cache
     */
    public void refreshDeviceCache() {
        bleBluetooth.refreshDeviceCache();
    }

    /**
     * close gatt
     */
    public void closeBluetoothGatt() {
        if (bleBluetooth != null) {
            //bleBluetooth.clearCallback();
            try {
                bleBluetooth.closeBluetoothGatt();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * is support ble?
     */
    public boolean isSupportBle() {
        return mContext.getApplicationContext()
                .getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    /**
     * open bluetooth
     */
    public void enableBluetooth() {
        if (bleBluetooth != null) {
            bleBluetooth.enableBluetoothIfDisabled();
        }
    }

    /**
     * close bluetooth
     */
    public void disableBluetooth() {
        if (bleBluetooth != null) {
            bleBluetooth.disableBluetooth();
        }
    }

    /**
     * is bluetooth enable?
     */
    public boolean isBlueEnable() {
        return bleBluetooth != null && bleBluetooth.isBlueEnable();
    }

    public boolean isInScanning() {
        return bleBluetooth.isInScanning();
    }

    public boolean isConnectingOrConnected() {
        return bleBluetooth.isConnectingOrConnected();
    }

    public boolean isConnected() {
        return bleBluetooth.isConnected();
    }

    public boolean isServiceDiscovered() {
        return bleBluetooth.isServiceDiscovered();
    }

    /**
     * remove callback form a character
     */
    public void stopListenCharacterCallback(String uuid) {
        bleBluetooth.removeGattCallback(uuid);
    }

    /**
     * remove callback for gatt connect
     */
    public void stopListenConnectCallback() {
        bleBluetooth.removeConnectGattCallback();
    }

    /**
     * stop notify, remove callback
     */
    public boolean stopNotify(String uuid_service, String uuid_notify) {
        boolean success = bleBluetooth.newBleConnector()
                .withUUIDString(uuid_service, uuid_notify, null)
                .disableCharacteristicNotify();
        if (success) {
            bleBluetooth.removeGattCallback(uuid_notify);
        }
        return success;
    }

    /**
     * stop indicate, remove callback
     */
    public boolean stopIndicate(String uuid_service, String uuid_indicate) {
        boolean success = bleBluetooth.newBleConnector()
                .withUUIDString(uuid_service, uuid_indicate, null)
                .disableCharacteristicIndicate();
        if (success) {
            bleBluetooth.removeGattCallback(uuid_indicate);
        }
        return success;
    }

}
