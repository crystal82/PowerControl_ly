package uascent.com.powercontrol.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.clj.fastble.BleManager;
import com.clj.fastble.conn.BleCharacterCallback;
import com.clj.fastble.conn.BleGattCallback;
import com.clj.fastble.event.ConnectEvent;
import com.clj.fastble.exception.BleException;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import uascent.com.percent.support.PercentLinearLayout;
import uascent.com.percent.support.PercentRelativeLayout;
import uascent.com.powercontrol.MyApplication;
import uascent.com.powercontrol.R;
import uascent.com.powercontrol.base.BaseActivity;
import uascent.com.powercontrol.bean.DeviceBean;
import uascent.com.powercontrol.bean.LightBean;
import uascent.com.powercontrol.constant.MyConstant;
import uascent.com.powercontrol.constant.SpConstant;
import uascent.com.powercontrol.dialog.DialogPositionSelect;
import uascent.com.powercontrol.dialog.HelpDialog;
import uascent.com.powercontrol.event.LightDataEvent;
import uascent.com.powercontrol.event.PsdSetEvent;
import uascent.com.powercontrol.event.WriteEvent;
import uascent.com.powercontrol.utils.DialogUtil;
import uascent.com.powercontrol.utils.Lg;
import uascent.com.powercontrol.utils.MyUtils;
import uascent.com.powercontrol.utils.SpHelper;
import uascent.com.powercontrol.utils.UiUtils;
import uascent.com.powercontrol.utils.XPermissionUtils;
import uascent.com.powercontrol.view.CustomWaitDialog1;
import uascent.com.powercontrol.view.ProgressSeekBar;

import static uascent.com.powercontrol.MyApplication.mCurrentPortsType;
import static uascent.com.powercontrol.MyApplication.sDeviceBean;
import static uascent.com.powercontrol.constant.MyConstant.UUID_NOTIFY;
import static uascent.com.powercontrol.constant.MyConstant.UUID_SERVICE;
import static uascent.com.powercontrol.constant.MyConstant.UUID_WRITE;

public class MainActivity extends BaseActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    private static final int GPS_REQUEST_CODE      = 1;
    private static final int ACTION_SET_BRIGHTNESS = 1;//设置brightness
    private static final int ACTION_SET_FLASH      = 2;//设置flash
    private static final int ACTION_SET_SWITCH     = 3;//设置switch
    @BindView(R.id.ll_seekbar)
    PercentLinearLayout llSeekbar;
    private int mCurrentAction = -1;
    private String mOldBrightness; //记录原先状态，失败时还原
    private String mOldFlash;
    private String mOldSwitch;
    @BindView(R.id.help)
    ImageView             help;
    @BindView(R.id.iv_car_model)
    ImageView             mIvCarModel;
    @BindView(R.id.iv_main_power)
    ImageView             mIvMainPower;
    @BindView(R.id.iv_main_setting)
    ImageView             mIvMainSetting;
    @BindView(R.id.iv_main_switch)
    ImageView             mIvMainSwitch;
    @BindView(R.id.iv_main_ble)
    ImageView             mIvMainBle;
    @BindView(R.id.iv_select_car)
    ImageView             mIvSelectCar;
    @BindView(R.id.btn_light1)
    Button                mBtnLight1;
    @BindView(R.id.btn_light2)
    Button                mBtnLight2;
    @BindView(R.id.btn_light3)
    Button                mBtnLight3;
    @BindView(R.id.btn_light4)
    Button                mBtnLight4;
    @BindView(R.id.btn_light5)
    Button                mBtnLight5;
    @BindView(R.id.btn_light6)
    Button                mBtnLight6;
    @BindView(R.id.btn_light7)
    Button                mBtnLight7;
    @BindView(R.id.btn_light8)
    Button                mBtnLight8;
    @BindView(R.id.btn_light9)
    Button                mBtnLight9;
    @BindView(R.id.btn_light10)
    Button                mBtnLight10;
    @BindView(R.id.btn_light11)
    Button                mBtnLight11;
    @BindView(R.id.btn_light12)
    Button                mBtnLight12;
    @BindView(R.id.rl_rest)
    PercentRelativeLayout mRlRest;
    @BindView(R.id.tb_led)
    ToggleButton          mTbLed;
    @BindView(R.id.seekbar_bright)
    SeekBar               mSeekbarBright;
    @BindView(R.id.seekbar_flash)
    SeekBar               mSeekbarFlash;
    @BindView(R.id.tv_brightness)
    TextView              mTvBrightness;
    @BindView(R.id.tv_flash)
    TextView              mTvFlash;

    private HashMap<String, LightBean> mLightMap; //key=number点击位置
    private ArrayList<String>          mLoadedLightList; //已经加载，key=number点击位置
    private String mCurrentSelectNum  = "-1";//当前选择的圈，从map找对应的数据
    private View   mCurrentSelectView = null;//当前选择的View
    private int       mCurrentCarModel;
    private LightBean mSelectLightBean;
    private String    mMac;

    private BleManager        mBleManager;
    private CustomWaitDialog1 mLoadDialog;

    private boolean clickLedInitState = false;//避免设置progress,switch是触发change
    private Intent      mNotifyIntent;
    private int         mRawId;
    private MediaPlayer mMediaPlayer;
    private Vibrator    mVibrator;
    private boolean     mIsCloseAll;
    private AlertDialog mErrorAlertDialog;
    private AlertDialog mDisconnectDialog;
    private int         mAlarmVoltagePercent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EventBus.getDefault().register(this);//注册
        ButterKnife.bind(this);
        doRequestPermission();

        mBleManager = MyApplication.getBleManager();
        mLoadDialog = CustomWaitDialog1.createDialog(this);
        mLoadedLightList = new ArrayList<>();

        //MyUtils.playAlarmRingtone(this, rawId, true, 1);
        mNotifyIntent = new Intent(MainActivity.this, MainActivity.class);
        mNotifyIntent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_NEW_TASK);

        initData();
        initListener();
    }

    @Override
    protected void onStart() {
        super.onStart();
        doCheckGpsState();
        isStop = false;
        //检查蓝牙连接状态,判断是否连接
        mMac = (String) SpHelper.get(SpConstant.BLE_CONNECTED_DEVICE_MAC, "");
        mBleManager.enableBluetooth();
        Lg.d("MAIN---doBleConnect---:  mMac:"
                     + mMac
                     + MyApplication.sBleConnectState);

        mIvMainBle.setSelected(MyApplication.sBleConnectState);
        if (MyApplication.sBleConnectState && mDisconnectDialog != null) {
            mDisconnectDialog.dismiss();
        }
        if (!TextUtils.isEmpty(mMac) && !MyApplication.sBleConnectState) {
            //TODO:1.执行连接操作
            doBleConnect();
        }
        if (!TextUtils.isEmpty(mMac) && MyApplication.sBleConnectState) {
            setListener();
        }
    }

    boolean isStop = false;

    @Override
    protected void onStop() {
        super.onStop();
        isStop = true;
    }

    @Override
    public void onBackPressed() {
        Lg.d("---onBackPressed---");
        finish();
    }

    @Override
    protected void onDestroy() {
        Lg.d("-----MainActivity onDestroy-----");
        mBleManager.closeBluetoothGatt();
        saveDataToSp();
        mLoadDialog.dismiss();
        EventBus.getDefault().unregister(this);//反注册
        MyApplication.sBleConnectState = false;
        super.onDestroy();
    }

    private void doBleConnect() {
        if (mDisconnectDialog != null) {
            mDisconnectDialog.dismiss();
        }
        Lg.d("MAIN---doBleConnect---:" + isPsdCheckError);
        isPsdCheckError = false;
        mLoadDialog.show();
        mLoadDialog.setMessage(getString(R.string.connecting));
        //mBleManager.connectDevice(mMac, MyConstant.CALL_BACK_KEY_MAIN, false, mBleGattCallback);

        mBleManager.scanMacAndConnect(
                mMac,
                MyConstant.CALL_BACK_KEY_MAIN,
                6 * 1000,
                false,
                mBleGattCallback);
    }

    BleGattCallback mBleGattCallback = new BleGattCallback() {
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
        }

        @Override
        public void onNotFoundDevice() {
            mLoadDialog.dismiss();
            showLongToast(getString(R.string.not_found_device));
            MyApplication.sBleConnectState = false;
            mIvMainBle.setSelected(MyApplication.sBleConnectState);

            mBleManager.closeBluetoothGatt();
            Lg.d("MAIN-----onNotFoundDevice");
        }

        @Override
        public void onFoundDevice(BluetoothDevice device) {
            Lg.d("MAIN-----onFoundDevice");
        }

        @Override
        public void onConnectSuccess(BluetoothGatt gatt, int status) {
            Lg.d("MAIN-----onConnectSuccess");
            gatt.discoverServices();
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Lg.d("MAIN-----onServicesDiscovered");
            if (isStop) {
                return;
            }
            List<BluetoothGattService> services = gatt.getServices();
            for (BluetoothGattService service : services) {
                if (MyConstant.UUID_SERVICE.equals(service.getUuid().toString())) {
                    Lg.d("MAIN-----onServicesDiscovered,找到服务");
                    //设置通知
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //TODO:2.1 连接成功，设置notify
                            setListener();
                            //TODO:2.2 连接成功，写入密码，鉴权
                            try {
                                Thread.sleep(400);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            String psd = (String) SpHelper.get(SpConstant.BLE_CONNECT_PSD, MyConstant.DEFAULT_BLE_PSD);
                            doWritePsdCheck(psd);
                        }
                    });
                    break;
                }
            }
        }

        @Override
        public void onConnectDisconnected(BleException exception) {
        }
    };

    boolean isPsdCheckError = false; //鉴权失败导致断开不回连

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConnectStateEvent(ConnectEvent event) {//参数必须是ClassEvent类型, 否则不会调用此方法
        Lg.d("MainActivity---onConnectStateEvent---" + event.connectState);
        //重置ui状态
        mIvMainBle.setSelected(event.connectState);
        if (!event.connectState) {
            mLoadDialog.dismiss();
            mBleManager.closeBluetoothGatt();
            MyApplication.sBleConnectState = false;
            //mBleManager.removeAllCallback();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mIvMainBle.setSelected(MyApplication.sBleConnectState);
                    if (mDisconnectDialog == null || !mDisconnectDialog.isShowing()) {
                        mDisconnectDialog = MyUtils.showDialog(MainActivity.this,
                                                               false,
                                                               R.layout.dialog_warn,
                                                               R.id.ll_dialog_judge,
                                                               new MyUtils.DialogAble() {
                                                                   @Override
                                                                   public void onDataSet(View layout, final AlertDialog dialog) {
                                                                       TextView tvTip = (TextView) layout.findViewById(R.id.tv_tip);
                                                                       layout.findViewById(R.id.tv_title).setVisibility(View.GONE);
                                                                       tvTip.setText(getString(R.string.ble_disconnect));
                                                                       layout.findViewById(R.id.tv_dialog_cancel).setOnClickListener(new View.OnClickListener() {
                                                                           @Override
                                                                           public void onClick(View v) {
                                                                               if (!TextUtils.isEmpty(mMac) && !isPsdCheckError) {
                                                                                   doBleConnect();
                                                                               }
                                                                               dialog.dismiss();
                                                                           }
                                                                       });
                                                                   }
                                                               });

                    }
                }
            });
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDoWriteEvent(WriteEvent event) {//参数必须是ClassEvent类型, 否则不会调用此方法
        Lg.d("MainActivity---onDoWriteEvent---" + event.writeState);
        if (WriteEvent.psdWrite.equals(event.writeState)) {
            mBleManager.writeDevice(
                    UUID_SERVICE,
                    UUID_WRITE,
                    MyUtils.getBleData(MyConstant.BLE_CMD_PSD_CHANGE, event.info),
                    new BleCharacterCallback() {
                        @Override
                        public void onSuccess(BluetoothGattCharacteristic characteristic) {
                            //mLoadDialog.dismiss();
                        }

                        @Override
                        public void onFailure(BleException exception) {
                            //mLoadDialog.dismiss();
                            Lg.e("MAIN----write: " + exception.toString());
                            mBleManager.handleException(exception);
                        }
                    });
        }
    }

    //设置notify提醒！！！
    private void setListener() {
        Lg.d("MAIN----setListener---isStop:" + isStop);
        if (isStop) {
            return;
        }
        mBleManager.notify(
                UUID_SERVICE,
                UUID_NOTIFY,
                mNotifyCallback);
        try {
            Thread.sleep(350);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    BleCharacterCallback mNotifyCallback = new BleCharacterCallback() {
        @Override
        public void onSuccess(final BluetoothGattCharacteristic characteristic) {
            Lg.d("MAIN----setListener onSuccess---" + Arrays.toString(characteristic.getValue()));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    byte[] value = characteristic.getValue();
                    switch (value[0]) {
                        case MyConstant.BLE_CMD_LED_CONTROL:
                            Lg.d("MAIN----设置LED");
                            onSetLedBack(value);
                            break;
                        case MyConstant.BLE_CMD_GET_STATE:
                            Lg.d("MAIN----读取设备状态: " + Arrays.toString(characteristic.getValue()));
                            sDeviceBean.energyPercentNum = value[3] > 100 ? 100 : value[3];
                            sDeviceBean.lowAlarm = value[4];
                            sDeviceBean.timingNum = value[5];
                            SpHelper.putCommit(SpConstant.TIME_SHUTDOWN_NUM, sDeviceBean.timingNum + "");
                            SpHelper.putCommit(SpConstant.ALARM_VOLTAGE, (sDeviceBean.lowAlarm / 10.0) + "");

                            //总开关状态
                            if (value.length >= 8) {
                                mIvMainSwitch.setSelected(value[7] == 1);
                            }
                            //低电电压提示
                            shoeLowPower(value);
                            //设备警告！！
                            onReadStateBack(value);
                            //TODO:4.设置所有灯泡开关状态
                            onReadAllLedState(value);

                            //判断是否显示电量
                            if (isReadPowerInfo) {
                                mLoadDialog.dismiss();
                                isReadPowerInfo = false;
                                showPowerDialog();
                            }
                            break;
                        case MyConstant.BLE_CMD_GET_LED_STATE:
                            //读取完，设置数据
                            onReadLedBack(characteristic);
                            break;
                        case MyConstant.BLE_CMD_GET_LED_ALL_SWITCH:
                            //TODO:4.获取所有灯泡开关状态（与0x02命令重复，可以去除）
                            onReadAllLedState(value);
                            break;
                        case MyConstant.BLE_CMD_PSD_CHANGE:
                            Lg.d("---修改密码---");
                            mLoadDialog.dismiss();
                            if (value[1] == 1) {
                                String newPsd = (String) SpHelper.get(SpConstant.BLE_CONNECT_CHANGE_PSD, MyConstant.DEFAULT_BLE_PSD);
                                SpHelper.putCommit(SpConstant.BLE_CONNECT_PSD, newPsd);
                                EventBus.getDefault().post(new PsdSetEvent(true));
                                showShortToast(getString(R.string.set_psd_success));
                            } else {
                                EventBus.getDefault().post(new PsdSetEvent(false));
                                showShortToast(getString(R.string.set_psd_error));
                            }
                            break;
                        case MyConstant.BLE_CMD_PSD_CHECK:
                            onPsdWriteBack(value[1], characteristic);
                            break;
                        case MyConstant.BLE_CMD_SWITCH_ALL:
                            Lg.d("MAIN----总开关:" + mIvMainSwitch.isSelected());
                            //Set<String> keySet = mLightMap.keySet();
                            //TODO:总开关不影响单个灯状态
                            //mTbLed.setChecked(mIsCloseAll);
                            //for (String lightNumber : keySet) {
                            //    LightBean lightBean = mLightMap.get(lightNumber);
                            //    lightBean.switchState = mIsCloseAll ? "1" : "0";
                            //}
                            //setUiStateAsData(mLightMap, false);
                            //mIvMainSwitch.setSelected(!mIvMainSwitch.isSelected());

                            break;
                    }
                }
            });
        }

        @Override
        public void onFailure(BleException exception) {
            mBleManager.handleException(exception);
        }
    };

    private void shoeLowPower(byte[] value) {
        double powerValue = Double.parseDouble(ProgressSeekBar.getValue(value[3], 100)) * 10;
        Lg.d("powerValue: " + powerValue);
        if (powerValue < sDeviceBean.lowAlarm) {//mAlarmVoltagePercent
            mIvMainPower.setSelected(true);
        } else {
            mIvMainPower.setSelected(false);
        }
    }

    private void onReadAllLedState(byte[] value) {
        Lg.d("获取所有灯泡开关状态:" + Arrays.toString(value));
        if (mLightMap != null) {
            Set<String> keySet = mLightMap.keySet();
            int         states = value[6];
            int         tag    = 0;
            for (String lightNumber : keySet) {
                LightBean lightBean = mLightMap.get(lightNumber);
                tag = Integer.parseInt(lightBean.id) - 1; //第一位不移
                lightBean.switchState = ((states & (0x01 << tag)) == (0x01 << tag)) ? "1" : "0";
                Lg.d("开关状态switchState:" + lightBean.switchState);
            }
            setUiStateAsData(mLightMap, false);
        }

        //mIvMainSwitch.setSelected(getAllSwitchState());//初始化，更新ICON
    }

    private void onPsdWriteBack(byte b, BluetoothGattCharacteristic characteristic) {
        if (b == MyConstant.BLE_SUCCESS) {
            Lg.d("MAIN----鉴权成功: " + Arrays.toString(characteristic.getValue()));
            MyApplication.sBleConnectState = true;
            mIvMainBle.setSelected(MyApplication.sBleConnectState);
            showShortToast(getString(R.string.bleConnectSuccess));
            //TODO:3.读取状态
            doWriteRequestState("初始化数据");
        } else {
            Lg.d("MAIN----鉴权失败");
            isPsdCheckError = true;
            MyApplication.sBleConnectState = false;
            mIvMainBle.setSelected(MyApplication.sBleConnectState);
            showShortToast(getString(R.string.ble_psd_error));
        }
    }

    private void onReadLedBack(BluetoothGattCharacteristic characteristic) {
        mLoadedLightList.add(mCurrentSelectNum);
        int[] intValues = MyUtils.bytesToInts(characteristic.getValue());
        mSelectLightBean.brightness = intValues[2] + "";
        mSelectLightBean.flash = intValues[3] + "";
        mSelectLightBean.switchState = intValues[4] + "";
        setUiAsLightBean(Integer.parseInt(intValues[2] + "", 16),
                         Integer.parseInt(intValues[3] + "", 16),
                         Integer.parseInt(intValues[4] + "", 16), false);
        //mIvMainSwitch.setSelected(getAllSwitchState());//读取单个状态成功
        Lg.d("MAIN----读取灯泡状态: " + Arrays.toString(characteristic.getValue())
                     + "   mSelectLightBean:" + mSelectLightBean.toString());
    }

    boolean isError = false;

    //TODO:警告
    private void onReadStateBack(byte[] value) {
        String errorInfo = showErrorNotification(value);
        if (isError && (mErrorAlertDialog == null || !mErrorAlertDialog.isShowing())) {
            mTbLed.setChecked(false);
            showErrorDialog(errorInfo);
        }
    }

    private void showErrorDialog(String errorInfo) {
        final String finalErrorInfo = errorInfo;
        mErrorAlertDialog = MyUtils.showDialog(MainActivity.this,
                                               false,
                                               R.layout.dialog_warn,
                                               R.id.ll_dialog_warn,
                                               new MyUtils.DialogAble() {
                                                   @Override
                                                   public void onDataSet(View layout, final AlertDialog dialog) {
                                                       TextView tvTitle = (TextView) layout.findViewById(R.id.tv_title);
                                                       tvTitle.setText(finalErrorInfo);
                                                       layout.findViewById(R.id.tv_dialog_cancel).setOnClickListener(new View.OnClickListener() {
                                                           @Override
                                                           public void onClick(View v) {
                                                               //调转GPS设置界面
                                                               isError = false;
                                                               //停止震动，报警
                                                               MyUtils.stopAlertRingtone(mMediaPlayer);
                                                               Lg.d("stopVibrator:" + (mVibrator == null));
                                                               MyUtils.stopVibrator(mVibrator);
                                                               dialog.dismiss();
                                                           }
                                                       });
                                                   }
                                               });
    }

    private String showErrorNotification(byte[] value) {
        String  errorInfo = null;
        boolean isShake   = (boolean) SpHelper.get(SpConstant.IS_SHAKE, true);
        if (isError) {
            return "";
        }
        if (DeviceBean.parseLedInfo(sDeviceBean, value[1])) {
            isError = true;
            //TODO:报警，Led出现过流现象！！！
            errorInfo = sDeviceBean.overflowingLedStr + getString(R.string.error_over_flowing);
            UiUtils.createNotification(MainActivity.this,
                                       R.mipmap.ic_launcher,
                                       getString(R.string.notification_ticker),
                                       getString(R.string.notification_title),
                                       errorInfo,
                                       mNotifyIntent,
                                       MyConstant.NOTIFICATION_ALARM_ID);
            if (isShake) {
                mVibrator = MyUtils.vibrateAction(getApplicationContext(), 1);
            }
            mRawId = (int) SpHelper.get(SpConstant.ALARM_VOLTAGE_RAW_ID, 0);
            mMediaPlayer = MyUtils.playAlarmRingtone(MainActivity.this, mRawId, true);
        } else if (DeviceBean.parseStateInfo(sDeviceBean, value[2])) {
            isError = true;
            errorInfo = sDeviceBean.deviceStateStr();
            UiUtils.createNotification(MainActivity.this,
                                       R.mipmap.ic_launcher,
                                       getString(R.string.notification_ticker),
                                       getString(R.string.error_device) + getString(R.string.notification_title),
                                       errorInfo,
                                       mNotifyIntent,
                                       MyConstant.NOTIFICATION_ALARM_ID);
            if (isShake) {
                mVibrator = MyUtils.vibrateAction(getApplicationContext(), 1);
            }
            //报警音乐
            mRawId = (int) SpHelper.get(SpConstant.ALARM_VOLTAGE_RAW_ID, 0);
            mMediaPlayer = MyUtils.playAlarmRingtone(MainActivity.this, mRawId, true);
        }
        return errorInfo;
    }

    private void onSetLedBack(byte[] value) {
        if (value[1] == 1) {
            //showShortToast(getString(R.string.setup_success));
        } else {
            showShortToast(getString(R.string.setup_failed));
            switch (mCurrentAction) {
                case ACTION_SET_BRIGHTNESS:
                    mSelectLightBean.brightness = mOldBrightness;
                    mSeekbarBright.setProgress(Integer.parseInt(mOldBrightness));
                    break;
                case ACTION_SET_FLASH:
                    mSelectLightBean.flash = mOldFlash;
                    mSeekbarFlash.setProgress(Integer.parseInt(mOldFlash));
                    break;
                case ACTION_SET_SWITCH:
                    mSelectLightBean.switchState = mOldSwitch;
                    mCurrentSelectView.setSelected(mOldBrightness.equals("1"));
                    mTbLed.setChecked(mOldBrightness.equals("1"));
                    break;
            }
        }
    }

    //读取所有灯泡开关状态
    private void doWriteGetAllSwitch() {
        Lg.d("MAIN----doWriteGetAllSwitch");
        mBleManager.writeDevice(
                UUID_SERVICE,
                UUID_WRITE,
                MyUtils.getBleData(MyConstant.BLE_CMD_GET_LED_ALL_SWITCH, new byte[]{}),
                new BleCharacterCallback() {
                    @Override
                    public void onSuccess(BluetoothGattCharacteristic characteristic) {
                        mLoadDialog.dismiss();
                    }

                    @Override
                    public void onFailure(BleException exception) {
                        mLoadDialog.dismiss();
                        Lg.e("MAIN----write: " + exception.toString());
                        mBleManager.handleException(exception);
                    }
                });
    }

    //读取指定Led数据
    private void doWriteGetLedState(String id) {
        Lg.d("MAIN----doWriteGetLedState" + id);
        //showShortToast("读取当前灯泡数据");
        mBleManager.writeDevice(
                UUID_SERVICE,
                UUID_WRITE,
                MyUtils.getBleData(MyConstant.BLE_CMD_GET_LED_STATE, id),
                new BleCharacterCallback() {
                    @Override
                    public void onSuccess(BluetoothGattCharacteristic characteristic) {
                        mLoadDialog.dismiss();
                    }

                    @Override
                    public void onFailure(BleException exception) {
                        mLoadDialog.dismiss();
                        setUiAsLightBean(Integer.parseInt(mSelectLightBean.brightness),
                                         Integer.parseInt(mSelectLightBean.flash),
                                         Integer.parseInt(mSelectLightBean.switchState), false);
                        //mIvMainSwitch.setSelected(getAllSwitchState());//读取单个状态失败
                        Lg.e("MAIN----write: " + exception.toString());
                        mBleManager.handleException(exception);
                    }
                });
    }

    //设置当前设备状态
    private void doWriteLedControl(byte brightness, byte flash, byte switchInfo) {
        Lg.d("MAIN----doWriteLedControl----brightness:" + brightness + "   flash:" + flash + "   switchInfo:" + switchInfo);
        mBleManager.writeDevice(
                UUID_SERVICE,
                UUID_WRITE,
                MyUtils.getBleData(MyConstant.BLE_CMD_LED_CONTROL,
                                   new byte[]{Byte.parseByte(mSelectLightBean.id),
                                           brightness, flash, switchInfo}),
                new BleCharacterCallback() {
                    @Override
                    public void onSuccess(BluetoothGattCharacteristic characteristic) {
                        mLoadDialog.dismiss();
                    }

                    @Override
                    public void onFailure(BleException exception) {
                        mLoadDialog.dismiss();
                        Lg.e("MAIN----write: " + exception.toString());
                        mBleManager.handleException(exception);
                    }
                });
    }

    //设置密码！
    private void doWritePsdCheck(String psd) {
        Lg.d("MAIN----doWritePsdCheck---:" + psd + "  isStop:" + isStop);
        if (isStop) {
            return;
        }
        mBleManager.writeDevice(
                UUID_SERVICE,
                UUID_WRITE,
                MyUtils.getBleData(MyConstant.BLE_CMD_PSD_CHECK, psd.getBytes()),
                new BleCharacterCallback() {
                    @Override
                    public void onSuccess(BluetoothGattCharacteristic characteristic) {
                       //MyApplication.sBleConnectState = true;
                       //runOnUiThread(new Runnable() {
                       //    @Override
                       //    public void run() {
                       //        mIvMainBle.setSelected(MyApplication.sBleConnectState);
                       //        if (mDisconnectDialog != null) {
                       //            mDisconnectDialog.dismiss();
                       //        }
                       //        mLoadDialog.dismiss();
                       //    }
                       //});
                    }

                    @Override
                    public void onFailure(BleException exception) {
                        mLoadDialog.dismiss();
                        MyApplication.sBleConnectState = false;
                        mIvMainBle.setSelected(MyApplication.sBleConnectState);
                        showShortToast(getString(R.string.ble_psd_error));
                        Lg.e("MAIN----write: " + exception.toString());
                        mBleManager.handleException(exception);
                    }
                });
    }

    //获取当前开关状态,跟当前开关状态相反
    private boolean getAllSwitchState() {
        mIsCloseAll = !mIvMainSwitch.isSelected();
        return mIsCloseAll;
        //if (mLightMap == null) {
        //    return false;
        //}
        //Set<String> keySet = mLightMap.keySet();
        //for (String lightNum : keySet) {
        //    LightBean lightBean = mLightMap.get(lightNum);
        //    //有开着，则关
        //    if ("1".equals(lightBean.switchState)) {
        //        mIsCloseAll = false;
        //        Lg.d("----当前总状态1:---:"+ mIsCloseAll);
        //        return false;
        //    }
        //}
        ////全关，则开
        //mIsCloseAll = true;
        //Lg.d("----当前总状态2:---:"+ mIsCloseAll);
        //return true;
    }

    private void doWriteSwitchAll() {
        //getAllSwitchState();//点击总开关！
        Lg.d("MAIN----doWriteSwitchAll---" + mIsCloseAll);
        mBleManager.writeDevice(
                UUID_SERVICE,
                UUID_WRITE,
                MyUtils.getBleData(MyConstant.BLE_CMD_SWITCH_ALL,
                                   new byte[]{(byte) (!mIvMainSwitch.isSelected() ? 1 : 0)}),  //mIsCloseAll
                new BleCharacterCallback() {
                    @Override
                    public void onSuccess(BluetoothGattCharacteristic characteristic) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showShortToast(getString(R.string.set_success));
                                Lg.d("------mIsCloseAll:-----:" + mIvMainSwitch.isSelected());
                                mIvMainSwitch.setSelected(!mIvMainSwitch.isSelected());
                                mLoadDialog.dismiss();
                            }
                        });
                    }

                    @Override
                    public void onFailure(BleException exception) {
                        mLoadDialog.dismiss();
                        showShortToast(getString(R.string.ble_write_error));
                        Lg.e("MAIN----write: " + exception.toString());
                        mBleManager.handleException(exception);
                    }
                });
    }

    //读取设备状态
    private void doWriteRequestState(String strs) {
        Lg.d("MAIN------doWriteRequestState");
        mLoadDialog.setMessage(strs);
        mLoadDialog.show();

        mBleManager.writeDevice(
                UUID_SERVICE,
                UUID_WRITE,
                MyUtils.getBleData(MyConstant.BLE_CMD_GET_STATE, new byte[]{0}),
                new BleCharacterCallback() {
                    @Override
                    public void onSuccess(BluetoothGattCharacteristic characteristic) {
                        mLoadDialog.dismiss();
                        Lg.e("MAIN-----doWriteRequestState onSuccess: " + Arrays.toString(characteristic.getValue()));
                    }

                    @Override
                    public void onFailure(BleException exception) {
                        mLoadDialog.dismiss();
                        Lg.e("MAIN-----doWriteRequestState onFailure: " + exception.toString());
                        mBleManager.handleException(exception);
                    }
                });
    }

    private void saveDataToSp() {
        StringBuilder builder = new StringBuilder();
        if (mLightMap != null) {
            Set<String> keySet = mLightMap.keySet();
            for (String lightNum : keySet) {
                LightBean lightBean = mLightMap.get(lightNum);
                builder.append(lightBean.toString()).append(MyConstant.LIGHT_SP_DIVISION2);
            }
            String lightSaveStr = builder.substring(0, builder.length() - 1);
            Lg.d("保存当前数据：" + lightSaveStr);
            if (mCurrentPortsType == MyConstant.PORTS_8) {
                SpHelper.putCommit(SpConstant.DATA_PORTS8_LIGHT, lightSaveStr);
            } else {
                SpHelper.putCommit(SpConstant.DATA_PORTS4_LIGHT, lightSaveStr);
            }
        }
    }

    //DialogSelect 选择完后回调！
    @Subscribe(threadMode = ThreadMode.MAIN) //第2步:注册一个在后台线程执行的方法,用于接收事件
    public void onUpLightDataEvent(LightDataEvent event) {//参数必须是ClassEvent类型, 否则不会调用此方法
        Lg.d("MAIN---onUpLightDataEvent---");
        //重置ui状态
        setUiStateAsData(mLightMap, true);

        //初始化显示
        initData();
    }

    private void initData() {
        //报警音乐
        //mRawId = (int) SpHelper.get(SpConstant.ALARM_VOLTAGE_RAW_ID, 0);
        SpHelper.putCommit(SpConstant.PORTS_NUM, MyConstant.PORTS_4);
        //设置车辆模型
        mCurrentCarModel = (int) SpHelper.get(SpConstant.CAR_MODEL, MyConstant.MODEL_CAR);
        MyUtils.setCarModel(this, mIvCarModel, mCurrentCarModel, true);

        //本地数据加载！
        mLightMap = loadLightData(mCurrentPortsType);
        if (mLightMap == null || mLightMap.size() == 0) {
            //showShortToast(getString(R.string.data_no_light));
            return;
        }

        setUiStateAsData(mLightMap, false);//初始化设置数据
    }

    private void initListener() {
        MyUtils.setOnClick(
                this,
                mBtnLight1, mBtnLight2, mBtnLight3, mBtnLight4,
                mBtnLight5, mBtnLight6, mBtnLight7, mBtnLight8,
                mBtnLight9, mBtnLight10, mBtnLight11, mBtnLight12,
                mIvMainPower, mIvMainSetting, mIvMainSwitch, mIvMainBle,
                mIvSelectCar, mRlRest,
                mTbLed,
                help);

        mSeekbarBright.setOnSeekBarChangeListener(this);
        mSeekbarFlash.setOnSeekBarChangeListener(this);
    }


    private HashMap<String, LightBean> loadLightData(int currentPortsType) {

        String lightDataStr;
        if (currentPortsType == MyConstant.PORTS_8) {
            lightDataStr = (String) SpHelper.get(SpConstant.DATA_PORTS8_LIGHT, "");
        } else {
            lightDataStr = (String) SpHelper.get(SpConstant.DATA_PORTS4_LIGHT, "");
        }

        return MyUtils.analysisLightData(lightDataStr, true);
    }

    public void setUiStateAsData(HashMap<String, LightBean> lightMap, boolean isReset) {
        if (isReset) {
            //初始化当前选灯
            setUiAsLightBean(0, 0, 0, true);
            mLoadedLightList.clear();
        }

        if (mLightMap == null) {
            return;
        }
        Set<String> lightNumberSet = lightMap.keySet();
        for (String lightNumber : lightNumberSet) {
            LightBean lightBean = lightMap.get(lightNumber);
            setLightCircleState(lightBean, isReset);
        }
    }

    //设置选择灯数据ui
    private void setUiAsLightBean(int seekbarBright, int seekbarFlash, int toggle, boolean isReset) {
        clickLedInitState = false;

        mSeekbarBright.setProgress(seekbarBright);
        mSeekbarFlash.setProgress(seekbarFlash);
        mTbLed.setChecked(toggle == 1);
        if (mCurrentSelectView != null && !isReset) {
            mCurrentSelectView.setSelected(toggle == 1);//开启，绿色！！
        }
    }

    //1、手机控制开关18A、2A、18B只控制开关信号，不用调节亮度与闪烁频率。
    //2、2B有调节亮度与闪烁频率功能
    //1.5B对应的控制脚功能改为只控制开关功能，无调节亮度、闪灯功能
    private void setProcessState(LightBean selectLightBean) {
        String describe = selectLightBean.describe;
        Lg.e("----setProcessState:---" + describe);
        if ("18A".equals(describe) ||
                "2A".equals(describe) ||
                "20A".equals(describe) ||
                "20B".equals(describe) ||
                "18B".equals(describe) ||
                "1.5B".equals(describe)) {
            setSeekBarState(false);
        } else {
            //mTvBrightness.setVisibility(View.VISIBLE);
            //mTvFlash.setVisibility(View.VISIBLE);
            //mSeekbarBright.setVisibility(View.VISIBLE);
            //mSeekbarFlash.setVisibility(View.VISIBLE);
            setSeekBarState(true);
        }
    }

    //seekbar不可使用显示红色
    private void setSeekBarState(boolean enable) {
       /* mTvBrightness.setTextColor(getResources().getColor(enable ? R.color.colorWhite : R.color.colorSeekUn));
        mTvFlash.setTextColor(getResources().getColor(enable ? R.color.colorWhite : R.color.colorSeekUn));
        mSeekbarBright.setEnabled(enable);
        mSeekbarFlash.setEnabled(enable);
        mSeekbarBright.setThumb(getResources().getDrawable(enable ? R.drawable.bright_thumb : R.drawable.red_thumb));
        mSeekbarFlash.setThumb(getResources().getDrawable(enable ? R.drawable.bright_thumb : R.drawable.red_thumb));*/
        if (enable)
            llSeekbar.setVisibility(View.VISIBLE);
        else
            llSeekbar.setVisibility(View.GONE);
    }


    /**
     * 设置各个圆状态
     *
     * @param lightBean 数据
     * @param isReset   是否重置状态
     */
    private void setLightCircleState(LightBean lightBean, boolean isReset) {
        View lightView;
        switch (lightBean.number) {
            case "1":
                lightView = mBtnLight1;
                break;
            case "2":
                lightView = mBtnLight2;
                break;
            case "3":
                lightView = mBtnLight3;
                break;
            case "4":
                lightView = mBtnLight4;
                break;
            case "5":
                lightView = mBtnLight5;
                break;
            case "6":
                lightView = mBtnLight6;
                break;
            case "7":
                lightView = mBtnLight7;
                break;
            case "8":
                lightView = mBtnLight8;
                break;
            case "9":
                lightView = mBtnLight9;
                break;
            case "10":
                lightView = mBtnLight10;
                break;
            case "11":
                lightView = mBtnLight11;
                break;
            case "12":
                lightView = mBtnLight12;
                break;
            default:
                return;
        }
        if (isReset) {
            //初始化
            lightView.setVisibility(View.INVISIBLE);
            lightView.setSelected(false);
        } else {
            lightView.setVisibility(View.VISIBLE);
            lightView.setSelected(lightBean.switchState.equals("1"));
            ((Button) lightView).setText(lightBean.describe);
        }
        setLightCircle((Button) lightView, false);
    }

    //TODO:显示带灯图标，当前为2B(控制闪烁，亮度)
    private void setLightCircle(Button lightView, boolean isBlueCircle) {
        CharSequence text = lightView.getText();
        Lg.d("setLightCircle----:" + text + "  isBlueCircle:" + isBlueCircle);
        if (isBlueCircle) {
            int blueCircleId = "2A".equals(text) ?
                    R.drawable.selector_light_blue_circle : R.drawable.selector_blue_circle;
            MyUtils.setDrawable(this, lightView, blueCircleId);
        } else {
            int blueCircleId = "2A".equals(text) ?
                    R.drawable.selector_light_circle : R.drawable.selector_green_circle;
            MyUtils.setDrawable(this, lightView, blueCircleId);
        }
    }

    @Override
    public void onClick(View viewClick) {
        //判断连接状态
        if (viewClick.getId() != R.id.iv_main_ble
                && viewClick.getId() != R.id.iv_select_car
                && viewClick.getId() != R.id.rl_rest) {
            if (!MyApplication.sBleConnectState) {
                startActivity(new Intent(this, BleConnectActivity.class));
                return;
            }
        }
        switch (viewClick.getId()) {
            case R.id.tb_led:
                if (mCurrentSelectView != null) {
                    //TODO:开关改变，蓝牙发送,成功才设置
                    mOldSwitch = mSelectLightBean.switchState;
                    mSelectLightBean.switchState = mTbLed.isChecked() ? "1" : "0";
                    mCurrentSelectView.setSelected(mTbLed.isChecked());//开启，绿色！！
                    mCurrentAction = ACTION_SET_SWITCH;
                    //特定按钮设置100
                    setInfoForDescribe();
                    doWriteLedControl(Byte.parseByte(mSelectLightBean.brightness),
                                      Byte.parseByte(mSelectLightBean.flash),
                                      Byte.parseByte(mSelectLightBean.switchState));
                }
                break;
            case R.id.iv_main_power:
                if (MyApplication.sBleConnectState) {
                    mLoadDialog.show();
                    Lg.d("MAIN------读取电量");
                    doWriteRequestState("读取电量");
                    isReadPowerInfo = true;
                } else {
                    startActivity(new Intent(this, BleConnectActivity.class));
                }
                break;
            case R.id.iv_main_setting:
                startActivity(new Intent(this, SettingActivity.class));
                break;
            case R.id.iv_main_switch:
                //TODO:总开关，遍历判断当前是否有开着的！！！无则开
                if (MyApplication.sBleConnectState)
                    doWriteSwitchAll();
                else
                    startActivity(new Intent(this, BleConnectActivity.class));
                break;
            case R.id.iv_main_ble:
                startActivity(new Intent(this, BleConnectActivity.class));
                break;
            case R.id.iv_select_car:
                startActivityForResult(new Intent(this, SelectCarActivity.class),
                                       MyConstant.REQUEST_CODE_SELECT_CAR);
                break;
            case R.id.rl_rest:
                Intent intent = new Intent(this, DialogPositionSelect.class);
                intent.putExtra(MyConstant.SELECTED_MODEL, mCurrentCarModel);
                startActivity(intent);
                break;
            case R.id.help:
                new HelpDialog(this);
                break;
            case R.id.btn_light1:
            case R.id.btn_light2:
            case R.id.btn_light3:
            case R.id.btn_light4:
            case R.id.btn_light5:
            case R.id.btn_light6:
            case R.id.btn_light7:
            case R.id.btn_light8:
            case R.id.btn_light9:
            case R.id.btn_light10:
            case R.id.btn_light11:
            case R.id.btn_light12:
                setCircleClickState(viewClick);

                mCurrentSelectNum = (String) viewClick.getTag();
                mSelectLightBean = mLightMap.get(mCurrentSelectNum);

                setProcessState(mSelectLightBean);//部分不需要控制亮度，闪频

                Lg.d("onClick mCurrentSelectNum:" + mLoadedLightList.contains(mCurrentSelectNum));
                //if (mLoadedLightList.contains(mCurrentSelectNum)) {
                //    setUiAsLightBean(Integer.parseInt(mSelectLightBean.brightness),
                //                     Integer.parseInt(mSelectLightBean.flash),
                //                     Integer.parseInt(mSelectLightBean.switchState), false);
                //} else {}

                //TODO:如果是第一次点击则读取数据,读取成功加上！
                doWriteGetLedState(mSelectLightBean.id);
                break;
        }
    }

    private void setInfoForDescribe() {
        String describe = mSelectLightBean.describe;
        if ("18A".equals(describe) ||
                "2A".equals(describe) ||
                "20A".equals(describe) ||
                "20B".equals(describe) ||
                "18B".equals(describe) ||
                "1.5B".equals(describe)) {
            //不显示发送100
            mSelectLightBean.brightness = "100";
            mSelectLightBean.flash = "0";
        }
    }

    boolean isReadPowerInfo = false;

    //查看电量信息
    private void showPowerDialog() {
        MyUtils.showDialog(MainActivity.this,
                           R.layout.dialog_power_info,
                           R.id.ll_dialog_power,
                           new MyUtils.DialogAble() {
                               @Override
                               public void onDataSet(View layout, final AlertDialog dialog) {
                                   layout.findViewById(R.id.iv_dialog_close).setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View v) {
                                           dialog.dismiss();
                                       }
                                   });
                                   TextView powerPercent = (TextView) layout.findViewById(R.id.tv_power_percent);
                                   TextView powerNum     = (TextView) layout.findViewById(R.id.tv_power_num);
                                   powerPercent.setText(sDeviceBean.energyPercentNum + "%");

                                   String i = ProgressSeekBar.getValue(sDeviceBean.energyPercentNum, 100);
                                   Lg.d("--sDeviceBean.energyPercentNum % 100--:" + i);
                                   powerNum.setText(i + "V");
                               }
                           });
    }

    //是指当前重置原先
    private void setCircleClickState(View viewClick) {
        //重置原先选择
        if (mCurrentSelectView != null && mCurrentSelectView != viewClick) {
            setLightCircle((Button) mCurrentSelectView, false);
        }

        //显示当前选择
        mCurrentSelectView = viewClick;
        if ("2A".equals(((Button) mCurrentSelectView).getText())) {
            MyUtils.setDrawable(this, mCurrentSelectView, R.mipmap.icon_light_blue);
        } else {
            MyUtils.setDrawable(this, mCurrentSelectView, R.drawable.shape_blue_circle);
        }
    }

    //进度条改变监听器！！！！
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        //TODO:蓝牙发送,控制设备
        Lg.d("MAIN--onStopTrackingTouch--");
        if (mCurrentSelectView != null && clickLedInitState) {
            if (seekBar.getId() == R.id.seekbar_bright) {
                mOldBrightness = mSelectLightBean.brightness;
                mSelectLightBean.brightness = seekBar.getProgress() + "";
                mCurrentAction = ACTION_SET_BRIGHTNESS;
            } else {
                mOldFlash = mSelectLightBean.flash;
                mSelectLightBean.flash = seekBar.getProgress() + "";
                mCurrentAction = ACTION_SET_FLASH;
            }
            doWriteLedControl(Byte.parseByte(mSelectLightBean.brightness),
                              Byte.parseByte(mSelectLightBean.flash),
                              Byte.parseByte(mSelectLightBean.switchState));
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        Lg.d("MAIN--onStartTrackingTouch--");
        clickLedInitState = true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Lg.d(resultCode + "  onActivityResult----" + requestCode + "  data:" + data.getIntExtra(SelectCarActivity.SELECTED_MODEL, SelectCarActivity.MODEL_CAR));
        if (resultCode == MyConstant.RESULT_CODE_SELECT_CAR) {
            mCurrentCarModel = data.getIntExtra(MyConstant.SELECTED_MODEL, MyConstant.MODEL_CAR);
            SpHelper.putCommit(SpConstant.CAR_MODEL, mCurrentCarModel);
            MyUtils.setCarModel(this, mIvCarModel, mCurrentCarModel, true);
        }
    }


    private void doRequestPermission() {

        XPermissionUtils.requestPermissions(this, MyConstant.REQUEST_CODE_PERMISSION,
                                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                                    Manifest.permission.BLUETOOTH},
                                            new XPermissionUtils.OnPermissionListener() {
                                                @Override
                                                public void onPermissionGranted() {
                                                    //Lg.d("onPermissionGranted");
                                                    //showShortToast("获取权限成功");
                                                }

                                                @Override
                                                public void onPermissionDenied(String[] deniedPermissions, boolean alwaysDenied) {
                                                    StringBuilder sBuilder = new StringBuilder();
                                                    for (String deniedPermission : deniedPermissions) {
                                                        if (deniedPermission.equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                                                            sBuilder.append("location");
                                                            sBuilder.append(",");
                                                        }
                                                        if (deniedPermission.equals(Manifest.permission.BLUETOOTH)) {
                                                            sBuilder.append("bluetooth");
                                                            sBuilder.append(",");
                                                        }
                                                    }
                                                    if (sBuilder.length() > 0) {
                                                        sBuilder.deleteCharAt(sBuilder.length() - 1);
                                                    }
                                                    showShortToast(getString(R.string.get_permission_error, sBuilder.toString()));
                                                    if (alwaysDenied) {
                                                        DialogUtil.showPermissionManagerDialog(MainActivity.this, sBuilder.toString());
                                                    }
                                                }
                                            });
    }

    //检查GPS是否打开！！！
    private void doCheckGpsState() {
        if (!XPermissionUtils.isGpsOpen(MainActivity.this)) {
            MyUtils.showDialog(MainActivity.this,
                               false,
                               R.layout.dialog_judge,
                               R.id.ll_dialog_judge,
                               new MyUtils.DialogAble() {
                                   @Override
                                   public void onDataSet(View layout, final AlertDialog dialog) {
                                       TextView tvTitle = (TextView) layout.findViewById(R.id.tv_title);
                                       tvTitle.setText(getString(R.string.open_gps));
                                       layout.findViewById(R.id.tv_dialog_cancel).setOnClickListener(new View.OnClickListener() {
                                           @Override
                                           public void onClick(View v) {
                                               finish();
                                               dialog.dismiss();
                                           }
                                       });
                                       layout.findViewById(R.id.tv_dialog_enter).setOnClickListener(new View.OnClickListener() {
                                           @Override
                                           public void onClick(View v) {
                                               //调转GPS设置界面
                                               Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                               //此为设置完成后返回到获取界面
                                               startActivityForResult(intent, GPS_REQUEST_CODE);
                                               dialog.dismiss();
                                           }
                                       });
                                   }
                               });
        }
    }
}
