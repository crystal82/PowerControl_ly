package uascent.com.powercontrol.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import uascent.com.powercontrol.constant.MyConstant;
import uascent.com.powercontrol.R;
import uascent.com.powercontrol.constant.SpConstant;
import uascent.com.powercontrol.base.BaseActivity;
import uascent.com.powercontrol.dialog.DialogPositionSelect;
import uascent.com.powercontrol.utils.SpHelper;

public class SelectCarActivity extends BaseActivity implements View.OnClickListener {


    @BindView(R.id.iv_car_model)
    ImageView mIvCarModel;
    @BindView(R.id.iv_truck_model)
    ImageView mIvTruckModel;
    @BindView(R.id.iv_jeep_model)
    ImageView mIvJeepModel;
    @BindView(R.id.iv_atv_model)
    ImageView mIvAtvModel;

    private int currentModel = -1;
    private boolean mIsInitedLight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_car);
        ButterKnife.bind(this);

        initData();
        initListener();
    }

    private void initData() {
        int portNum = (int) SpHelper.get(SpConstant.PORTS_NUM, MyConstant.PORTS_4);
        if (portNum == MyConstant.PORTS_4) {
            mIsInitedLight = (boolean) SpHelper.get(SpConstant.IS_PORT4_INIT_LIGHT, false);
        } else {
            mIsInitedLight = (boolean) SpHelper.get(SpConstant.IS_PORT8_INIT_LIGHT, false);
        }
    }

    private void initListener() {
        mIvCarModel.setOnClickListener(this);
        mIvTruckModel.setOnClickListener(this);
        mIvJeepModel.setOnClickListener(this);
        mIvAtvModel.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_car_model:
                currentModel = MyConstant.MODEL_CAR;
                break;
            case R.id.iv_truck_model:
                currentModel = MyConstant.MODEL_TRUCK;
                break;
            case R.id.iv_jeep_model:
                currentModel = MyConstant.MODEL_JEEP;
                break;
            case R.id.iv_atv_model:
                currentModel = MyConstant.MODEL_ATV;
                break;
        }

        if (mIsInitedLight) {
            //已初始化，返回选择编号
            Intent intent = new Intent();
            intent.putExtra(MyConstant.SELECTED_MODEL, currentModel);
            setResult(MyConstant.RESULT_CODE_SELECT_CAR, intent);
            finish();
        } else {
            //未初始化，则进入设置位置界面
            Intent intent = new Intent(SelectCarActivity.this, DialogPositionSelect.class);
            intent.putExtra(MyConstant.SELECTED_MODEL, currentModel);
            startActivity(intent);
        }
    }
}
