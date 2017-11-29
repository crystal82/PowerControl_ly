package uascent.com.powercontrol.dialog;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import uascent.com.percent.support.PercentLinearLayout;
import uascent.com.powercontrol.event.LightDataEvent;
import uascent.com.powercontrol.constant.MyConstant;
import uascent.com.powercontrol.R;
import uascent.com.powercontrol.constant.SpConstant;
import uascent.com.powercontrol.base.BaseActivity;
import uascent.com.powercontrol.bean.LightBean;
import uascent.com.powercontrol.ui.MainActivity;
import uascent.com.powercontrol.utils.Lg;
import uascent.com.powercontrol.utils.MyUtils;
import uascent.com.powercontrol.utils.SpHelper;

public class DialogPositionSelect extends BaseActivity implements View.OnClickListener {

    private static final float CIRCLE_SIZE_BIG   = (float) 0.11;
    private static final float CIRCLE_SIZE_SMALL = (float) 0.10;

    @BindView(R.id.iv_car_model)
    ImageView           mIvCarModel;
    @BindView(R.id.btn_light1)
    Button              mBtnLight1;
    @BindView(R.id.btn_light2)
    Button              mBtnLight2;
    @BindView(R.id.btn_light3)
    Button              mBtnLight3;
    @BindView(R.id.btn_light4)
    Button              mBtnLight4;
    @BindView(R.id.btn_light5)
    Button              mBtnLight5;
    @BindView(R.id.btn_light6)
    Button              mBtnLight6;
    @BindView(R.id.btn_light7)
    Button              mBtnLight7;
    @BindView(R.id.btn_light8)
    Button              mBtnLight8;
    @BindView(R.id.btn_light9)
    Button              mBtnLight9;
    @BindView(R.id.btn_light10)
    Button              mBtnLight10;
    @BindView(R.id.btn_light11)
    Button              mBtnLight11;
    @BindView(R.id.btn_light12)
    Button              mBtnLight12;
    @BindView(R.id.btn_wait_light1)
    Button              mBtnWaitLight1;
    @BindView(R.id.btn_wait_light2)
    Button              mBtnWaitLight2;
    @BindView(R.id.btn_wait_light3)
    Button              mBtnWaitLight3;
    @BindView(R.id.btn_wait_light4)
    Button              mBtnWaitLight4;
    @BindView(R.id.btn_wait_light5)
    Button              mBtnWaitLight5;
    @BindView(R.id.btn_wait_light6)
    Button              mBtnWaitLight6;
    @BindView(R.id.btn_wait_light7)
    Button              mBtnWaitLight7;
    @BindView(R.id.btn_wait_light8)
    Button              mBtnWaitLight8;
    @BindView(R.id.ll_wait_light)
    PercentLinearLayout mLlWaitLight;
    @BindView(R.id.tv_line2)
    TextView            mTvLine2;
    @BindView(R.id.tv_hint1)
    TextView            mTvHint1;
    @BindView(R.id.tv_hint2)
    TextView            mTvHint2;
    @BindView(R.id.tv_dialog_cancel)
    TextView            mTvDialogCancel;
    @BindView(R.id.tv_dialog_enter)

    TextView            mTvDialogEnter;
    private String                     mCurrentLightId;
    private String                     mCurrentLightNum;
    private View                       mCurrentLightView;
    private LightBean                  mCurrentLightBean;
    private HashMap<String, LightBean> mLightMap; //保存灯泡数据 Key=lightId
    private int                        mCurrentPortsType;
    private int                        mCurrentModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_position_select);
        ButterKnife.bind(this);

        initData();
        initListener();
    }

    private void initData() {

        mCurrentPortsType = (int) SpHelper.get(SpConstant.PORTS_NUM, MyConstant.PORTS_4);
        setWaitCircleState(mCurrentPortsType);
        setCarModel();
        setOldState();
    }

    //TODO:设置之前设置的状态
    private void setOldState() {
        mLightMap = loadLightData(mCurrentPortsType);
        if (mLightMap == null) {
            mLightMap = new HashMap<>();
            return;
        }
        Set<String> lightNumberSet = mLightMap.keySet();
        for (String lightNumber : lightNumberSet) {
            LightBean lightBean = mLightMap.get(lightNumber);
            setLightCircleState(lightBean);
            setPortState(lightBean);
        }
    }

    private HashMap<String, LightBean> loadLightData(int currentPortsType) {

        String lightDataStr;
        if (currentPortsType == MyConstant.PORTS_8) {
            lightDataStr = (String) SpHelper.get(SpConstant.DATA_PORTS8_LIGHT, "");
            mTvHint2.setText("*8 lamps position max.");
        } else {
            lightDataStr = (String) SpHelper.get(SpConstant.DATA_PORTS4_LIGHT, "");
            mTvHint2.setText("*4 lamps position max.");
        }
        return MyUtils.analysisLightData(lightDataStr, false); //key为id
    }

    private void setWaitCircleState(int portsNum) {
        if (portsNum == MyConstant.PORTS_4) {
            mBtnWaitLight1.setText("18A");//1
            mBtnWaitLight2.setText("2A");//6
            mBtnWaitLight3.setText("2B");//5
            mBtnWaitLight4.setText("18B");//7
            mBtnWaitLight5.setVisibility(View.GONE);
            mBtnWaitLight6.setVisibility(View.GONE);
            mBtnWaitLight7.setVisibility(View.GONE);
            mBtnWaitLight8.setVisibility(View.GONE);
        } else if (portsNum == MyConstant.PORTS_8) {
            mBtnWaitLight1.setText("20A");
            mBtnWaitLight2.setText("5A");
            mBtnWaitLight3.setText("3.5A");
            mBtnWaitLight4.setText("1.5A");
            mBtnWaitLight5.setText("20B");
            mBtnWaitLight6.setText("5B");
            mBtnWaitLight7.setText("3.5B");
            mBtnWaitLight8.setText("1.5B");
        }
    }

    //设置车model
    private void setCarModel() {
        Intent intent = getIntent();
        mCurrentModel = intent.getIntExtra(MyConstant.SELECTED_MODEL, MyConstant.MODEL_CAR);
        MyUtils.setCarModel(this, mIvCarModel, mCurrentModel, false);
    }

    private void initListener() {
        MyUtils.setOnClick(this, mBtnLight1, mBtnLight2, mBtnLight3, mBtnLight4
                , mBtnLight5, mBtnLight6, mBtnLight7, mBtnLight8
                , mBtnLight9, mBtnLight10, mBtnLight11, mBtnLight12
                , mBtnWaitLight1, mBtnWaitLight2, mBtnWaitLight3
                , mBtnWaitLight4, mBtnWaitLight5, mBtnWaitLight6
                , mBtnWaitLight7, mBtnWaitLight8, mTvDialogCancel
                , mTvDialogEnter);
    }

    @Override
    public void onClick(View clickView) {
        switch (clickView.getId()) {
            case R.id.tv_dialog_cancel:
                finish();
                break;
            case R.id.tv_dialog_enter:
                //TODO:发送命令到蓝牙，成功才关闭
                if (mLightMap.size() > 0) {
                    //组合数据
                    Set<String>   keySet           = mLightMap.keySet();
                    StringBuilder lightSaveBuilder = new StringBuilder();
                    StringBuilder lightSendBuilder = new StringBuilder();
                    for (String lightId : keySet) {
                        LightBean lightBean = mLightMap.get(lightId);
                        lightSaveBuilder.append(lightBean.toString()).append(MyConstant.LIGHT_SP_DIVISION2);
                        lightSendBuilder.append(lightBean.toSendString()).append(MyConstant.LIGHT_SP_DIVISION2);
                    }

                    String lightSaveStr = lightSaveBuilder.substring(0, lightSaveBuilder.length() - 1);
                    String lightSendStr = lightSendBuilder.substring(0, lightSendBuilder.length() - 1);
                    saveDatas(lightSaveStr, mCurrentModel);
                    Lg.d("保存数据串：" + lightSaveStr
                                 + "发送数据串：" + lightSendStr);
                } else {
                    saveDatas("", mCurrentModel);
                    Lg.d("保存数据串：");
                }
                EventBus.getDefault().post(new LightDataEvent());
                startActivity(new Intent(DialogPositionSelect.this, MainActivity.class));
                finish();
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
                onLightCircleClick(clickView);
                break;
            case R.id.btn_wait_light1:
            case R.id.btn_wait_light2:
            case R.id.btn_wait_light3:
            case R.id.btn_wait_light4:
            case R.id.btn_wait_light5:
            case R.id.btn_wait_light6:
            case R.id.btn_wait_light7:
            case R.id.btn_wait_light8:
                onPortCircleClick(clickView);
                break;
        }
    }

    private void saveDatas(String lightStr, int currentModel) {
        if (mCurrentPortsType == MyConstant.PORTS_4) {
            SpHelper.putCommit(SpConstant.DATA_PORTS4_LIGHT, lightStr);
            SpHelper.putCommit(SpConstant.IS_PORT4_INIT_LIGHT, true);
        } else {
            SpHelper.putCommit(SpConstant.DATA_PORTS8_LIGHT, lightStr);
            SpHelper.putCommit(SpConstant.IS_PORT8_INIT_LIGHT, true);
        }

        SpHelper.putCommit(SpConstant.CAR_MODEL, currentModel);
    }

    //上方点击对应Num值
    private void onLightCircleClick(View clickView) {
        if (TextUtils.isEmpty(mCurrentLightId)) {
            showShortToast(getString(R.string.tip_select_port_first));
            return;
        }
        clickView.setSelected(true);
        ((Button) clickView).setText(mCurrentLightBean.describe);
        setLightCircle((Button) clickView, false);

        mCurrentLightNum = (String) clickView.getTag();
        mCurrentLightBean.number = mCurrentLightNum;
        mCurrentLightBean.mView = clickView;
        MyUtils.setViewPercent(clickView, CIRCLE_SIZE_BIG);
        mLightMap.put(mCurrentLightId, mCurrentLightBean);
        mCurrentLightId = "";
        setLightCircle((Button) mCurrentLightView, false);
        mCurrentLightView = null;
        Lg.d("当前选择Light：" + mCurrentLightBean.toString());
        clickView.setClickable(false); //不可重复点击
    }

    //下方点击，对应id值
    private void onPortCircleClick(View clickView) {
        mCurrentLightBean = new LightBean();
        mCurrentLightId = (String) clickView.getTag();

        //点击相同，恢复上次选定的状态
        if (mCurrentLightView != null && mCurrentLightView != clickView) {
            mCurrentLightView.setSelected(false);
        }
        setLightCircle((Button) clickView, true);

        if (clickView.isSelected()) {
            //重复点击已选的！重置状态
            LightBean lightBean = mLightMap.get(mCurrentLightId);
            if (lightBean != null) {
                Lg.d("mCurrentLightId:" + mCurrentLightId + "   bean:" + lightBean.toString());
                //TODO:数据不为空，清除数据，重新选择，还原小圆！！！
                View view = lightBean.mView;
                ((Button) view).setText("");
                setLightCircle((Button) view, false);
                view.setSelected(false);
                view.setClickable(true);
                MyUtils.setViewPercent(view, CIRCLE_SIZE_SMALL);
                mLightMap.remove(mCurrentLightId);
            }
            //clickView.setSelected(false);
            //mCurrentLightId = "";
            //mCurrentLightView = null;
            setClickPortState(clickView);
        } else {
            setClickPortState(clickView);
        }
    }

    private void setClickPortState(View clickView) {
        clickView.setSelected(true);
        mCurrentLightView = clickView;
        mCurrentLightBean.id = mCurrentLightId;
        mCurrentLightBean.describe = (String) ((Button) clickView).getText();
    }

    /**
     * 初始化设置上方各个圆状态
     *
     * @param lightBean 数据
     */
    private void setLightCircleState(LightBean lightBean) {
        Lg.d("lightBean.number:" + lightBean.number);
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
        lightView.setSelected(true);
        lightBean.mView = lightView;
        ((Button) lightView).setText(lightBean.describe);
        MyUtils.setViewPercent(lightView, CIRCLE_SIZE_BIG);

        setLightCircle((Button) lightView, false);
    }

    //显示带灯图标，当前为2B(控制闪烁，亮度)
    private void setLightCircle(Button lightView, boolean isBlueCircle) {
        CharSequence text = lightView.getText();
        Lg.d("setLightCircle----:" + text);
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

    /**
     * 初始化设置下方
     *
     * @param lightBean 数据
     */
    private void setPortState(LightBean lightBean) {
        Lg.d("lightBean.id:" + lightBean.id);
        View lightView;
        switch (lightBean.id) {
            case "7":
                lightView = mBtnWaitLight1;
                break;
            case "1":
                lightView = mBtnWaitLight2;
                break;
            case "6":
                lightView = mBtnWaitLight3;
                break;
            case "5":
                lightView = mBtnWaitLight4;
                break;
            case "8":
                lightView = mBtnWaitLight5;
                break;
            case "4":
                lightView = mBtnWaitLight6;
                break;
            case "3":
                lightView = mBtnWaitLight7;
                break;
            case "2":
                lightView = mBtnWaitLight8;
                break;
            default:
                return;
        }
        lightView.setSelected(true);
        MyUtils.setViewPercent(lightView, CIRCLE_SIZE_BIG);

        setLightCircle((Button) lightView, false);
    }
}
