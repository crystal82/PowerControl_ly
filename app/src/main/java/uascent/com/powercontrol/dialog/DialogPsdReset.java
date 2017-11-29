package uascent.com.powercontrol.dialog;

import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.clj.fastble.BleManager;
import com.clj.fastble.conn.BleCharacterCallback;
import com.clj.fastble.exception.BleException;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import uascent.com.powercontrol.MyApplication;
import uascent.com.powercontrol.R;
import uascent.com.powercontrol.base.BaseActivity;
import uascent.com.powercontrol.constant.SpConstant;
import uascent.com.powercontrol.event.PsdSetEvent;
import uascent.com.powercontrol.event.WriteEvent;
import uascent.com.powercontrol.utils.Lg;
import uascent.com.powercontrol.utils.SpHelper;
import uascent.com.powercontrol.view.CustomWaitDialog1;

public class DialogPsdReset extends BaseActivity implements CompoundButton.OnCheckedChangeListener {

    @BindView(R.id.et_old_psd)
    EditText mEtOldPsd;
    @BindView(R.id.et_new_psd)
    EditText mEtNewPsd;
    @BindView(R.id.et_confirm_psd)
    EditText mEtConfirmPsd;
    @BindView(R.id.tv_dialog_cancel)
    TextView mTvDialogCancel;
    @BindView(R.id.tv_dialog_enter)
    TextView mTvDialogEnter;
    @BindView(R.id.cbLaws1)
    CheckBox mCbLaws1;
    @BindView(R.id.cbLaws2)
    CheckBox mCbLaws2;
    @BindView(R.id.cbLaws3)
    CheckBox mCbLaws3;

    private BleManager        mBleManager;
    private CustomWaitDialog1 mLoadDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_psd_reset);
        EventBus.getDefault().register(this);//注册
        ButterKnife.bind(this);
        mBleManager = MyApplication.getBleManager();
        mLoadDialog = CustomWaitDialog1.createDialog(this);
        initListener();
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);//反注册
        super.onDestroy();
    }

    private void initListener() {
        mTvDialogCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mTvDialogEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldPsd     = mEtOldPsd.getText().toString();
                String newPsd     = mEtNewPsd.getText().toString();
                String confirmPsd = mEtConfirmPsd.getText().toString();
                if (checkInputInfo(oldPsd, newPsd, confirmPsd)) {
                    mLoadDialog.show();
                    //TODO:同时发送新旧密码到蓝牙设备上！！！
                    SpHelper.putCommit(SpConstant.BLE_CONNECT_CHANGE_PSD, confirmPsd);
                    Lg.d("Setting----doChangePsd");

                    //TODO:通过EventBus在MainActivity发送数据
                    EventBus.getDefault().post(new WriteEvent(WriteEvent.psdWrite, (oldPsd + newPsd).getBytes()));

                    //mBleManager.writeDevice(
                    //        UUID_SERVICE,
                    //        UUID_WRITE,
                    //        MyUtils.getBleData(MyConstant.BLE_CMD_PSD_CHANGE, (oldPsd + newPsd).getBytes()),
                    //        changePsdCallback.get());
                }
            }
        });

        mCbLaws1.setOnCheckedChangeListener(this);
        mCbLaws2.setOnCheckedChangeListener(this);
        mCbLaws3.setOnCheckedChangeListener(this);
        mCbLaws1.setChecked(false);
        mCbLaws2.setChecked(false);
        mCbLaws3.setChecked(false);
    }

    //检查当前输入的数据
    private boolean checkInputInfo(String oldPsd, String newPsd, String confirmPsd) {
        if (TextUtils.isEmpty(oldPsd) || oldPsd.length() != 6) {
            showShortToast(getString(R.string.tip_old_psd));
            return false;
        }

        if (TextUtils.isEmpty(newPsd) || newPsd.length() != 6) {
            showShortToast(getString(R.string.tip_new_psd));
            return false;
        }

        if (TextUtils.isEmpty(confirmPsd) || !newPsd.equals(confirmPsd)) {
            showShortToast(getString(R.string.tip_confirm_psd));
            return false;
        }
        return true;
    }

    //密码设置成功回调
    @Subscribe(threadMode = ThreadMode.MAIN) //第2步:注册一个在后台线程执行的方法,用于接收事件
    public void onPsdSetSuccessEvent(PsdSetEvent event) {//参数必须是ClassEvent类型, 否则不会调用此方法
        Lg.d("MAIN---onPsdSetSuccessEvent---");
        mLoadDialog.dismiss();
        //重置ui状态
        if (event.setState) {
            finish();
        }
    }

    WeakReference<BleCharacterCallback> changePsdCallback = new WeakReference<BleCharacterCallback>(new BleCharacterCallback() {
        @Override
        public void onSuccess(BluetoothGattCharacteristic characteristic) {
            Lg.d("Setting----doChangePsd---onSuccess：" + mEtConfirmPsd.getText().toString());
        }

        @Override
        public void onFailure(BleException exception) {
            Lg.e("Setting----write: " + exception.toString());
        }
    });

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        EditText editText = null;
        switch (buttonView.getId()) {
            case R.id.cbLaws1:
                editText = mEtOldPsd;
                break;
            case R.id.cbLaws2:
                editText = mEtNewPsd;
                break;
            case R.id.cbLaws3:
                editText = mEtConfirmPsd;
                break;
            default:
                return;
        }
        if (isChecked) {
            editText.setInputType(0x01 | 0x90);
        } else {
            editText.setInputType(0x81);
        }
        editText.setSelection(editText.getText().toString().length());
    }
}
