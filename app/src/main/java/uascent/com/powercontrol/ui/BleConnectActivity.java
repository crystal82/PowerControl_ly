package uascent.com.powercontrol.ui;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.clj.fastble.BleManager;
import com.clj.fastble.conn.BleCharacterCallback;
import com.clj.fastble.conn.BleGattCallback;
import com.clj.fastble.event.ConnectEvent;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.scan.PeriodScanCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import uascent.com.powercontrol.MyApplication;
import uascent.com.powercontrol.R;
import uascent.com.powercontrol.adapter.BleConnectAdapter;
import uascent.com.powercontrol.base.BaseActivity;
import uascent.com.powercontrol.bean.BleScanBean;
import uascent.com.powercontrol.constant.MyConstant;
import uascent.com.powercontrol.constant.SpConstant;
import uascent.com.powercontrol.dialog.DialogConnectPsd;
import uascent.com.powercontrol.utils.Lg;
import uascent.com.powercontrol.utils.MyUtils;
import uascent.com.powercontrol.utils.SpHelper;
import uascent.com.powercontrol.view.CustomWaitDialog2;
import uascent.com.powercontrol.view.OnRefreshListener;
import uascent.com.powercontrol.view.PullToRefreshLayout;

import static uascent.com.powercontrol.constant.MyConstant.CALL_BACK_KEY_CONNECT;
import static uascent.com.powercontrol.constant.MyConstant.UUID_NOTIFY;
import static uascent.com.powercontrol.constant.MyConstant.UUID_SERVICE;
import static uascent.com.powercontrol.constant.MyConstant.UUID_WRITE;

public class BleConnectActivity extends BaseActivity implements AdapterView.OnItemClickListener, OnRefreshListener {

    @BindView(R.id.iv_back_icon)
    ImageView           mIvBackIcon;
    @BindView(R.id.tv_back)
    TextView            mTvBack;
    @BindView(R.id.tv_title)
    TextView            mTvTitle;
    @BindView(R.id.lv_ble_device)
    ListView            mLvBleDevice;
    @BindView(R.id.pull_to_refresh)
    PullToRefreshLayout mPullToRefresh;
    @BindView(R.id.tv_connect_ble_name)
    TextView            mTvConnectBleName;
    @BindView(R.id.iv_connect_ble_icon)
    ImageView           mIvConnectBleIcon;
    @BindView(R.id.tv_connect_ble_state)
    TextView            mTvConnectBleState;
    @BindView(R.id.tv_connect_ble_mac)
    TextView            mTvConnectBleMac;
    @BindView(R.id.ll_connect_info)
    LinearLayout        mLlConnectInfo;

    private RotateAnimation   loadingAnimation;
    private BleConnectAdapter mAdapter;
    private String            mConnectedMac;
    private String            mConnectedName;
    private List<BleScanBean> mBleScanData;
    private List<String>      mBleScanMac;

    private BleManager        mBleManager;
    private CustomWaitDialog2 mProgressDialog;
    private BleScanBean       mBleClickBean;
    private String            mConnectPsd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_connect);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);//注册

        mBleManager = MyApplication.getBleManager();
        mProgressDialog = new CustomWaitDialog2(this);
        mBleManager.enableBluetooth();

        initData();
        initListener();
        doStartScan();
    }


    @Override
    protected void onDestroy() {
        Lg.d("-----BleConnectActivity onDestroy-----");
        EventBus.getDefault().unregister(this);//反注册
        mBleManager.stopScan(mPeriodScanCallback);
        //mBleManager.stopListenCharacterCallback(UUID_NOTIFY);
        mBleManager.removeCallback(CALL_BACK_KEY_CONNECT);
        mProgressDialog.dismiss();
        super.onDestroy();
    }

    private void doStartScan() {
        //showShortToast(getString(R.string.bleNotFound));

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mBleScanData.clear();
                mAdapter.notifyDataSetChanged();
            }
        });
        mBleScanMac = new ArrayList<>();

        if (mBleManager.isInScanning())
            return;

        mBleManager.scanDevice(mPeriodScanCallback);
    }

    PeriodScanCallback mPeriodScanCallback = new PeriodScanCallback(MyConstant.BLE_SCAN_TIME) {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            //Lg.d("BleConnectActivity------name:" + device.getName() + "   address:" + device.getAddress() + "   " + mBleScanMac.contains(device.getAddress()));
            if (device.getAddress() == null
                    || mBleScanMac.contains(device.getAddress())
                    || device.getName() == null
                    || device.getAddress().equals(mConnectedMac)) {
                return;
            }
            mBleScanMac.add(device.getAddress());
            BleScanBean bleScanBean = new BleScanBean(device.getAddress(), device.getName());
            if (device.getAddress().equals(mConnectedMac)) { //&& mBleManager.isServiceDiscovered()
                bleScanBean.state = true;
                mBleScanData.add(0, bleScanBean);
                updateOnStateChange(false, true);
            } else {
                mBleScanData.add(bleScanBean);
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAdapter.notifyDataSetChanged();
                }
            });
        }

        @Override
        public void onScanTimeout() {

        }
    };

    private void initData() {
        mConnectedMac = (String) SpHelper.get(SpConstant.BLE_CONNECTED_DEVICE_MAC, "");
        mConnectedName = (String) SpHelper.get(SpConstant.BLE_CONNECTED_DEVICE_NAME, "");
        mConnectPsd = (String) SpHelper.get(SpConstant.BLE_CONNECT_PSD, MyConstant.DEFAULT_BLE_PSD);

        if (!TextUtils.isEmpty(mConnectedMac)) {
            mBleClickBean = new BleScanBean(mConnectedMac, mConnectedName);
            updateOnStateChange(MyApplication.sBleConnectState, true);
        }
        mTvTitle.setText("Connect");

        mBleScanData = new ArrayList<>();
        mAdapter = new BleConnectAdapter(this, mBleScanData);
        mLvBleDevice.setAdapter(mAdapter);
    }

    private void initListener() {
        mTvBack.setOnClickListener(backOnClickListener);
        mIvBackIcon.setOnClickListener(backOnClickListener);
        mLvBleDevice.setOnItemClickListener(this);
        mPullToRefresh.setOnRefreshListener(this);

        //点击条目直接连接
        mLlConnectInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // doBleConnect();
                Intent intent = new Intent(BleConnectActivity.this, DialogConnectPsd.class);
                startActivityForResult(intent, MyConstant.REQUEST_CODE_CONNECT_PSD);
            }
        });
    }

    View.OnClickListener backOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
        }
    };


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //弹出密码框！
        if (mBleScanData != null) {
            mBleClickBean = mBleScanData.get(position);
        }
        Intent intent = new Intent(BleConnectActivity.this, DialogConnectPsd.class);
        startActivityForResult(intent, MyConstant.REQUEST_CODE_CONNECT_PSD);
    }

    //下拉刷新
    @Override
    public void onRefresh() {
        doStartScan();
        // 下拉刷新操作
        new Handler() {
            @Override
            public void handleMessage(Message msg) {
                mPullToRefresh.refreshFinish(PullToRefreshLayout.REFRESH_SUCCEED);
            }
        }.sendEmptyMessageDelayed(0, MyConstant.BLE_SCAN_TIME);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == MyConstant.RESULT_CODE_CONNECT_PSD) {
            mBleManager.stopScan(mPeriodScanCallback);

            mConnectPsd = data.getStringExtra(MyConstant.CONNECT_PSD);

            //断开连接
            if (MyApplication.sBleConnectState) {
                mBleManager.closeBluetoothGatt();
                try {
                    Thread.sleep(150);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            //蓝牙连接操作,连接成功后鉴权
            doBleConnect();
        }
    }

    boolean isCheckPsd = false;

    private void doBleConnect() {
        Lg.d("BleConnectActivity------doBleConnect---");
        mProgressDialog.show();
        isCheckPsd = false;
        if (mBleClickBean != null) {
            mBleManager.connectDevice(
                    mBleClickBean.mac,
                    CALL_BACK_KEY_CONNECT,
                    false,
                    mBleGattCallback);
        }
    }

    BleGattCallback mBleGattCallback = new BleGattCallback() {
        @Override
        public void onNotFoundDevice() {
            Lg.d("BleConnectActivity------onNotFoundDevice---");
            mProgressDialog.dismiss();
            showShortToast(getString(R.string.bleNotFound));
        }

        @Override
        public void onFoundDevice(BluetoothDevice device) {
            Lg.d("BleConnectActivity------onNotFoundDevice---");

        }

        @Override
        public void onConnectSuccess(BluetoothGatt gatt, int status) {
            Lg.d("BleConnectActivity------onConnectSuccess---");
            gatt.discoverServices();
        }

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, int status) {
            Lg.d("BleConnectActivity------onServicesDiscovered---");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    List<BluetoothGattService> services      = gatt.getServices();
                    boolean                    isServiceFind = false;
                    for (BluetoothGattService service : services) {
                        if (MyConstant.UUID_SERVICE.equals(service.getUuid().toString())) {
                            Lg.d("BleConnectActivity-----onServicesDiscovered,找到服务");
                            isServiceFind = true;

                            //设置通知
                            setListener();
                            //写入密码，鉴权
                            doWritePsd(mConnectPsd);
                            break;
                        }
                    }
                    if (!isServiceFind) {
                        Lg.d("BleConnectActivity----没到服务");
                        mProgressDialog.dismiss();
                        showShortToast(getString(R.string.ble_disconnect));
                        mBleManager.closeBluetoothGatt();
                    }
                }
            });
        }

        @Override
        public void onConnectDisconnected(BleException exception) {
            Lg.d("BleConnectActivity-----onConnectDisconnected---释放资源");
        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN) //第2步:注册一个在后台线程执行的方法,用于接收事件
    public void onConnectStateEvent(ConnectEvent event) {//参数必须是ClassEvent类型, 否则不会调用此方法
        Lg.d("BleConnectActivity---onPsdSetSuccessEvent---");
        //重置ui状态
        if (!event.connectState) {
            MyApplication.sBleConnectState = false;
            updateOnStateChange(MyApplication.sBleConnectState, false);
            if (!isCheckPsd) {
                showShortToast(getString(R.string.ble_disconnect));
            }
            mProgressDialog.dismiss();
            mBleManager.closeBluetoothGatt();
        }
    }


    //设置监听器
    private void setListener() {
        mBleManager.notify(
                UUID_SERVICE,
                UUID_NOTIFY,
                new BleCharacterCallback() {
                    @Override
                    public void onSuccess(final BluetoothGattCharacteristic characteristic) {
                        Lg.d("BleConnectActivity----setListener onSuccess---" + Arrays.toString(characteristic.getValue()));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                byte[] value = characteristic.getValue();
                                switch (value[0]) {
                                    case MyConstant.BLE_CMD_PSD_CHECK:
                                        mProgressDialog.dismiss();
                                        if (value[1] == 1) {
                                            showShortToast(getString(R.string.bleConnectSuccess));
                                            SpHelper.putCommit(SpConstant.BLE_CONNECT_PSD, mConnectPsd);
                                            updateOnStateChange(true, true);
                                        } else {
                                            showShortToast("Incorrect Password");
                                            mBleManager.closeBluetoothGatt();
                                        }
                                        break;
                                }
                            }
                        });
                    }

                    @Override
                    public void onFailure(BleException exception) {
                        mBleManager.handleException(exception);
                    }
                });
        try {
            Thread.sleep(600);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void doWritePsd(String connectPsd) {
        Lg.d("BleConnectActivity-----doWritePsd---:" + Arrays.toString(connectPsd.getBytes()));
        isCheckPsd = true;
        mBleManager.writeDevice(
                UUID_SERVICE,
                UUID_WRITE,
                MyUtils.getBleData(MyConstant.BLE_CMD_PSD_CHECK, connectPsd.getBytes()),
                new BleCharacterCallback() {
                    @Override
                    public void onSuccess(final BluetoothGattCharacteristic characteristic) {
                        //mProgressDialog.dismiss();
                        Lg.d("BleCharacterCallback-----onSuccess---" + Arrays.toString(characteristic.getValue()));
                    }

                    @Override
                    public void onFailure(BleException exception) {
                        //mProgressDialog.dismiss();
                        Lg.e("BleConnectActivity-----write: " + exception.toString());
                        mBleManager.handleException(exception);
                    }
                });
    }

    private void updateOnStateChange(final boolean state, final boolean isUp) {
        if (!isUp) {
            return;
        }
        mLlConnectInfo.setVisibility(View.VISIBLE);
        MyApplication.sBleConnectState = state;
        if (state) {
            mConnectedMac = mBleClickBean.mac;
            mConnectedName = mBleClickBean.name;
            SpHelper.putCommit(SpConstant.BLE_CONNECTED_DEVICE_MAC, mBleClickBean.mac);
            SpHelper.putCommit(SpConstant.BLE_CONNECTED_DEVICE_NAME, mBleClickBean.name);
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTvConnectBleName.setText(mBleClickBean.name);
                mTvConnectBleMac.setText(mBleClickBean.mac);
                mTvConnectBleState.setText(state ? "Connected" : "Not Connected");
                mTvConnectBleState.setTextColor(state ? 0xff00A650 : 0xFFFF0000);
                MyUtils.setDrawable(getApplicationContext(), mIvConnectBleIcon, state ?
                        R.mipmap.ble_connected : R.mipmap.ble_disconnect_icon);
            }
        });
    }

}
