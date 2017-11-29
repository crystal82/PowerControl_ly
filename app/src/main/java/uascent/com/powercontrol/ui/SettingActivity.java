package uascent.com.powercontrol.ui;

import android.app.AlertDialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.clj.fastble.BleManager;
import com.clj.fastble.conn.BleCharacterCallback;
import com.clj.fastble.event.ConnectEvent;
import com.clj.fastble.exception.BleException;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import uascent.com.percent.support.PercentRelativeLayout;
import uascent.com.powercontrol.MyApplication;
import uascent.com.powercontrol.constant.MyConstant;
import uascent.com.powercontrol.R;
import uascent.com.powercontrol.constant.SpConstant;
import uascent.com.powercontrol.base.BaseActivity;
import uascent.com.powercontrol.dialog.DialogPsdReset;
import uascent.com.powercontrol.dialog.TimingDialog;
import uascent.com.powercontrol.utils.Lg;
import uascent.com.powercontrol.utils.MyUtils;
import uascent.com.powercontrol.utils.SpHelper;
import uascent.com.powercontrol.view.ProgressSeekBar;
import uascent.com.powercontrol.view.WheelView;

import static uascent.com.powercontrol.constant.MyConstant.SET_VOLT_GROW;
import static uascent.com.powercontrol.constant.MyConstant.UUID_SERVICE;
import static uascent.com.powercontrol.constant.MyConstant.UUID_WRITE;

public class SettingActivity extends BaseActivity implements View.OnClickListener {
    @BindView(R.id.iv_back_icon)
    ImageView             mIvBackIcon;
    @BindView(R.id.tv_back)
    TextView              mTvBack;
    @BindView(R.id.tv_title)
    TextView              mTvTitle;
    @BindView(R.id.rl_low_voltage_alarm)
    PercentRelativeLayout mRlLowVoltageAlarm;
    @BindView(R.id.rl_timing_shutdown)
    PercentRelativeLayout mRlTimingShutdown;
    @BindView(R.id.rl_revise_name)
    PercentRelativeLayout mRlReviseName;
    @BindView(R.id.rl_password_reset)
    PercentRelativeLayout mRlPasswordReset;
    @BindView(R.id.tv_low_alarm_num)
    TextView              mTvLowAlarmNum;
    @BindView(R.id.tv_timing_num)
    TextView              mTvTimingNum;

    private Context mContext;
    private String timeItem = "";
    private View mAlarmView;//当前被选择的Alarm，alarm弹出时初始化！！！
    private boolean isShake = true;//当前被选择的Alarm，alarm弹出时初始化！！！
    private String          mAlarmTypeTag;
    private ProgressSeekBar mPs_voltage;
    private double          mAlarmVoltage;
    private String          mTimeShutdownNum;
    private BleManager      mBleManager;
    private MediaPlayer     mMediaPlayer;
    private Vibrator        mVibrator;
    private AlertDialog     mAlertDialog;
    private AlertDialog     mVoltageDialog;
    private TimingDialog    mTimingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        EventBus.getDefault().register(this);//注册
        ButterKnife.bind(this);
        mContext = this;
        mBleManager = MyApplication.getBleManager();

        initData();
        initListener();
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);//反注册
        if (mAlertDialog != null) {
            mAlertDialog.dismiss();
        }
        if (mVoltageDialog != null) {
            mVoltageDialog.dismiss();
        }
        if (mTimingDialog != null) {
            mTimingDialog.dismiss();
        }
        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.MAIN) //第2步:注册一个在后台线程执行的方法,用于接收事件
    public void onConnectStateEvent(ConnectEvent event) {//参数必须是ClassEvent类型, 否则不会调用此方法
        Lg.d("MAIN---onPsdSetSuccessEvent---");
        //重置ui状态
        if (!event.connectState) {
            showShortToast(getString(R.string.ble_disconnect));
            startActivity(new Intent(SettingActivity.this, MainActivity.class));
            finish();
        }
    }

    private void initData() {
        mTvTitle.setText(getString(R.string.title_settings));
        mAlarmVoltage = Double.parseDouble((String) SpHelper.get(SpConstant.ALARM_VOLTAGE, 10.5 + "")); //默认10.5
        mTimeShutdownNum = (String) SpHelper.get(SpConstant.TIME_SHUTDOWN_NUM, "48"); //默认48

        mTvLowAlarmNum.setText("(" + mAlarmVoltage + "V)");
        mTvTimingNum.setText("(" + mTimeShutdownNum + "h)");
        isShake = (boolean) SpHelper.get(SpConstant.IS_SHAKE, true);
    }

    private void initListener() {
        mIvBackIcon.setOnClickListener(this);
        mTvBack.setOnClickListener(this);
        mRlTimingShutdown.setOnClickListener(this);
        mRlPasswordReset.setOnClickListener(this);
        mRlLowVoltageAlarm.setOnClickListener(this);
        mRlReviseName.setOnClickListener(this);
    }

    private List<String> getScopeList(int start, int end) {
        ArrayList<String> arr = new ArrayList<>();
        for (int i = start; i <= end; i++) {
            arr.add(i + "");
        }
        return arr;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_revise_name:
                onReviseNameClick();
                break;
            case R.id.rl_low_voltage_alarm:
                onLowVoltageAlarmClick();
                break;
            case R.id.rl_timing_shutdown:
                onTimingShutdownClick();
                break;
            case R.id.rl_password_reset:
                startActivity(new Intent(this, DialogPsdReset.class));
                break;
            case R.id.iv_back_icon:
            case R.id.tv_back:
                finish();
                break;
            case R.id.iv_shake:
                if (isShake) {
                    MyUtils.stopAlertRingtone(mMediaPlayer);
                    MyUtils.stopVibrator(mVibrator);
                    v.setSelected(false);
                    isShake = false;
                } else {
                    mVibrator = MyUtils.vibrateAction(this, -1);
                    v.setSelected(true);
                    isShake = true;
                }
                break;
            case R.id.iv_music1:
            case R.id.iv_music2:
            case R.id.iv_music3:
            case R.id.iv_music4:
            case R.id.iv_music5:
                if (v != mAlarmView) {
                    mAlarmTypeTag = (String) v.getTag();
                    Lg.e("mAlarmTypeTag=" + mAlarmTypeTag);
                    MyUtils.stopAlertRingtone(mMediaPlayer);
                    mMediaPlayer = MyUtils.playAlarmRingtone(this, MyConstant.AUDIO_MP_3_RES[Integer.parseInt(mAlarmTypeTag)], false);
                    if (isShake) {
                        MyUtils.stopVibrator(mVibrator);
                        mVibrator = MyUtils.vibrateAction(this, -1);
                    }

                    if (mAlarmView != null) {
                        mAlarmView.setSelected(false);
                    }
                    v.setSelected(true);
                    mAlarmView = v;
                } else {
                    mAlarmTypeTag = "0";
                    MyUtils.stopAlertRingtone(mMediaPlayer);
                    if (isShake) {
                        MyUtils.stopVibrator(mVibrator);
                        mVibrator = MyUtils.vibrateAction(this, -1);
                    }
                    v.setSelected(false);
                    mAlarmView = null;
                }
                break;
        }
    }

    //修改名
    private void onReviseNameClick() {
        //showShortToast("当前名称：" + et_new_name.getText().toString());
        //TODO:发送修改名称命令
        if ((mAlertDialog != null && !mAlertDialog.isShowing()) || mAlertDialog == null) {
            mAlertDialog = MyUtils.showEditDialog(this,
                                                  R.layout.dialog_revise_name,
                                                  R.id.ll_revise_name,
                                                  new MyUtils.DialogAble() {
                                                      @Override
                                                      public void onDataSet(View layout, final AlertDialog dialog) {
                                                          final EditText et_new_name         = (EditText) layout.findViewById(R.id.et_new_name);
                                                          TextView       tv_ble_connect_name = (TextView) layout.findViewById(R.id.tv_ble_connect_name);
                                                          tv_ble_connect_name.setText((String) SpHelper.get(SpConstant.BLE_CONNECTED_DEVICE_NAME, ""));

                                                          layout.findViewById(R.id.tv_dialog_cancel).setOnClickListener(new View.OnClickListener() {
                                                              @Override
                                                              public void onClick(View v) {
                                                                  dialog.dismiss();
                                                              }
                                                          });
                                                          layout.findViewById(R.id.tv_dialog_enter).setOnClickListener(new View.OnClickListener() {
                                                              @Override
                                                              public void onClick(View v) {
                                                                  //showShortToast("当前名称：" + et_new_name.getText().toString());
                                                                  //TODO:发送修改名称命令
                                                                  String newName = et_new_name.getText().toString();
                                                                  if (!TextUtils.isEmpty(newName)) {
                                                                      doSetNewName(newName);
                                                                      dialog.dismiss();
                                                                  } else {
                                                                      showShortToast("Please enter the correct format name");
                                                                  }
                                                              }
                                                          });
                                                      }
                                                  });
        }
    }


    //设置警报电压，音乐
    private void onLowVoltageAlarmClick() {
        //初始化设置dialog
        //mAlarmVoltage = mPs_voltage.getProgress();
        if ((mVoltageDialog != null && !mVoltageDialog.isShowing()) || mVoltageDialog == null) {
            mVoltageDialog = MyUtils.showDialog(this,
                                                R.layout.dialog_voltage_alarm,
                                                R.id.ll_voltage_alarm,
                                                new MyUtils.DialogAble() {
                                                    @Override
                                                    public void onDataSet(View layout, final AlertDialog dialog) {
                                                        layout.findViewById(R.id.iv_shake).setOnClickListener(SettingActivity.this);
                                                        layout.findViewById(R.id.iv_music1).setOnClickListener(SettingActivity.this);
                                                        layout.findViewById(R.id.iv_music2).setOnClickListener(SettingActivity.this);
                                                        layout.findViewById(R.id.iv_music3).setOnClickListener(SettingActivity.this);
                                                        layout.findViewById(R.id.iv_music4).setOnClickListener(SettingActivity.this);
                                                        layout.findViewById(R.id.iv_music5).setOnClickListener(SettingActivity.this);
                                                        mPs_voltage = (ProgressSeekBar) layout.findViewById(R.id.ps_voltage);
                                                        mPs_voltage.setOnSeekBarChangeListener(new onSeekBarChangeListener());
                                                        mPs_voltage.setProgress(ProgressSeekBar.getLowAlarmPercent(mAlarmVoltage));

                                                        mAlarmTypeTag = (String) SpHelper.get(SpConstant.ALARM_VOLTAGE_TYPE, "0");
                                                        setAlarmTypeByTag(layout, mAlarmTypeTag);//初始化设置dialog

                                                        layout.findViewById(R.id.tv_dialog_cancel).setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                MyUtils.stopAlertRingtone(mMediaPlayer);
                                                                MyUtils.stopVibrator(mVibrator);
                                                                dialog.dismiss();
                                                            }
                                                        });
                                                        layout.findViewById(R.id.tv_dialog_enter).setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                Lg.e("当前选择的alarm:" + mAlarmTypeTag
                                                                             + "  电量:" + mPs_voltage.getAlarmValue()
                                                                             + "  百分比:" + mPs_voltage.getProgress());
                                                                MyUtils.stopAlertRingtone(mMediaPlayer);
                                                                MyUtils.stopVibrator(mVibrator);

                                                                //mAlarmVoltage = mPs_voltage.getProgress();
                                                                mAlarmVoltage = Double.parseDouble(mPs_voltage.getAlarmValue());
                                                                SpHelper.putCommit(SpConstant.ALARM_VOLTAGE, mAlarmVoltage + "");
                                                                SpHelper.putCommit(SpConstant.IS_SHAKE, isShake);
                                                                SpHelper.putCommit(SpConstant.ALARM_VOLTAGE_TYPE, mAlarmTypeTag);
                                                                int alarmType = Integer.parseInt(mAlarmTypeTag);
                                                                SpHelper.putCommit(SpConstant.ALARM_VOLTAGE_RAW_ID, MyConstant.AUDIO_MP_3_RES[alarmType]);
                                                                mTvLowAlarmNum.setText("(" + mPs_voltage.getAlarmValue() + "V)");

                                                                doSetLowVoltageAlarm(mPs_voltage.getProgress());
                                                                dialog.dismiss();
                                                            }
                                                        });
                                                    }
                                                });
        }
    }

    //设置当前提醒模式icon,音乐模式！！！
    private void setAlarmTypeByTag(View layout, String tag) {
        if (isShake) {
            layout.findViewById(R.id.iv_shake).setSelected(true);
        }
        switch (tag) {
            case "1":
                mAlarmView = layout.findViewById(R.id.iv_music1);
                break;
            case "2":
                mAlarmView = layout.findViewById(R.id.iv_music2);
                break;
            case "3":
                mAlarmView = layout.findViewById(R.id.iv_music3);
                break;
            case "4":
                mAlarmView = layout.findViewById(R.id.iv_music4);
                break;
            case "5":
                mAlarmView = layout.findViewById(R.id.iv_music5);
                break;
        }
        if (mAlarmView != null) {
            mAlarmView.setSelected(true);
        }
    }

    //自动关机时间
    private void onTimingShutdownClick() {
        if ((mTimingDialog != null && !mTimingDialog.isShowing()) || mTimingDialog == null) {
            mTimingDialog = new TimingDialog(this, Integer.parseInt(mTimeShutdownNum), new TimingDialog.EventListener() {
                @Override
                public void onTimeChanged(int hour) {
                    Lg.e("onTimeChanged hour=" + hour);
                    mTimeShutdownNum = "" + hour;
                    doSetTimingShutdown(mTimeShutdownNum);
                    SpHelper.putCommit(SpConstant.TIME_SHUTDOWN_NUM, mTimeShutdownNum);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTvTimingNum.setText("(" + mTimeShutdownNum + "h)");
                        }
                    });
                }
            });
        }
    }


    private void initWheelView(WheelView wv_time) {
        wv_time.setOffset(1);
        wv_time.setSeletion(Integer.parseInt(mTimeShutdownNum) - 1);
        wv_time.setItems(getScopeList(1, 48));
        wv_time.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
            @Override
            public void onSelected(int selectedIndex, String item) {
                Log.d("onSelected", "selectedIndex: " + selectedIndex + ", item: " + item);
            }
        });
    }

    private class onSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            mPs_voltage.setIshide(true);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    }


    //设置报警电压
    private void doSetLowVoltageAlarm(int progress) {
        Lg.d(progress + "  Setting----doSetLowVoltageAlarm:" + Math.round(SET_VOLT_GROW * (progress * 1.0 / 100)));
        byte volt = (byte) (100 + Math.round(SET_VOLT_GROW * (progress * 1.0 / 100)));
        mBleManager.writeDevice(
                UUID_SERVICE,
                UUID_WRITE,
                MyUtils.getBleData(MyConstant.BLE_CMD_SET_VOLT, new byte[]{volt}),
                new BleCharacterCallback() {
                    @Override
                    public void onSuccess(BluetoothGattCharacteristic characteristic) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showShortToast(getString(R.string.set_success));
                                Lg.d("Setting----doSetLowVoltageAlarm---onSuccess");
                            }
                        });
                    }

                    @Override
                    public void onFailure(BleException exception) {
                        Lg.e("Setting----write: " + exception.toString());
                        mBleManager.handleException(exception);
                    }
                });
    }

    //设置自动关机
    private void doSetTimingShutdown(String time) {
        Lg.d("Setting----doSetTimingShutdown：" + time);
        mBleManager.writeDevice(
                UUID_SERVICE,
                UUID_WRITE,
                MyUtils.getBleData(MyConstant.BLE_CMD_SET_TIME_SHRESHOLD, new byte[]{Byte.parseByte(time)}),
                new BleCharacterCallback() {
                    @Override
                    public void onSuccess(BluetoothGattCharacteristic characteristic) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showShortToast(getString(R.string.set_success));
                            }
                        });
                        Lg.d("Setting----doSetTimingShutdown---onSuccess");
                    }

                    @Override
                    public void onFailure(BleException exception) {
                        Lg.e("Setting----write: " + exception.toString());
                        mBleManager.handleException(exception);
                    }
                });
    }


    //发送修改名称命令
    private void doSetNewName(final String name) {
        Lg.d("Setting----doSetNewName：" + name);
        byte[] bytes  = name.getBytes();
        byte[] bytes1 = new byte[bytes.length + 1];
        bytes1[bytes.length] = 0;
        System.arraycopy(bytes, 0, bytes1, 0, bytes.length);
        Lg.d("Setting----doSetNewName：" + Arrays.toString(bytes) + "     " + Arrays.toString(bytes1));
        mBleManager.writeDevice(
                UUID_SERVICE,
                UUID_WRITE,
                MyUtils.getBleData(MyConstant.BLE_CMD_CHANGE_NAME, bytes1),
                new BleCharacterCallback() {
                    @Override
                    public void onSuccess(BluetoothGattCharacteristic characteristic) {
                        Lg.d("Setting----doSetTimingShutdown---onSuccess");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showShortToast(getString(R.string.set_success));
                            }
                        });
                        SpHelper.putCommit(SpConstant.BLE_CONNECTED_DEVICE_NAME, name);
                    }

                    @Override
                    public void onFailure(BleException exception) {
                        Lg.e("Setting----write: " + exception.toString());
                        mBleManager.handleException(exception);
                    }
                });
    }
}
