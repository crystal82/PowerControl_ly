package com.clj.fastble.scan;

import android.bluetooth.BluetoothAdapter;
import android.os.Handler;
import android.os.Looper;

import com.clj.fastble.bluetooth.BleBluetooth;

/**
 * 蓝牙扫描回调类，实现LeScanCallback
 *
 *
 */
public abstract class PeriodScanCallback implements BluetoothAdapter.LeScanCallback {

    private Handler handler = new Handler(Looper.getMainLooper());
    private long timeoutMillis = 10000;
    BleBluetooth bleBluetooth;

    public PeriodScanCallback(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }

    public abstract void onScanTimeout();

    public BleBluetooth getBleBluetooth() {
        return bleBluetooth;
    }

    //绑定当前蓝牙扫描方法！
    public PeriodScanCallback setBleBluetooth(BleBluetooth bluetooth) {
        this.bleBluetooth = bluetooth;
        return this;
    }

    //开始扫描，注册！
    public void notifyScanStarted() {
        if (timeoutMillis > 0) {
            removeHandlerMsg();//notifyScanStarted
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    bleBluetooth.stopScan(PeriodScanCallback.this);
                    onScanTimeout();
                }
            }, timeoutMillis);
        }
    }

    public void removeHandlerMsg() {
        handler.removeCallbacksAndMessages(null);
    }

    public long getTimeoutMillis() {
        return timeoutMillis;
    }

    public PeriodScanCallback setTimeoutMillis(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
        return this;
    }

}
