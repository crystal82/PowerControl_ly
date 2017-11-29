package uascent.com.powercontrol.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import uascent.com.powercontrol.R;
import uascent.com.powercontrol.constant.MyConstant;
import uascent.com.powercontrol.constant.SpConstant;
import uascent.com.powercontrol.base.BaseActivity;
import uascent.com.powercontrol.utils.DialogUtil;
import uascent.com.powercontrol.utils.Lg;
import uascent.com.powercontrol.utils.MyUtils;
import uascent.com.powercontrol.utils.SpHelper;
import uascent.com.powercontrol.utils.UiUtils;
import uascent.com.powercontrol.utils.XPermissionUtils;

import static uascent.com.powercontrol.MyApplication.mCurrentPortsType;
import static uascent.com.powercontrol.MyApplication.sDeviceBean;

/**
 * 选择电流控制器型号，4 ports 或 8 ports
 */
public class SelectPortsActivity extends BaseActivity {


    private static final int GPS_REQUEST_CODE = 1;
    @BindView(R.id.iv_ports4)
    ImageView mIvPorts4;
    @BindView(R.id.iv_ports8)
    ImageView mIvPorts8;
    boolean isper=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_ports);
        ButterKnife.bind(this);

        doRequestPermission();

        mIvPorts4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPortsSelect(MyConstant.PORTS_4);
            }
        });
        mIvPorts8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPortsSelect(MyConstant.PORTS_8);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Lg.e("isper="+isper);
        doCheckGpsState();
    }

    //检查GPS是否打开！！！
    private void doCheckGpsState() {
        if (!XPermissionUtils.isGpsOpen(SelectPortsActivity.this)) {
            MyUtils.showDialog(SelectPortsActivity.this,
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

    //保存当前选择，并跳转
    private void onPortsSelect(int ports) {
        if (SpHelper.putCommit(SpConstant.PORTS_NUM, ports)) {
            mCurrentPortsType = ports;//设置全局ports
            //showShortToast(String.format(getString(R.string.port_select_success), ports));
            startActivity(new Intent(SelectPortsActivity.this, MainActivity.class));
            finish();
        } else {
            showShortToast(getString(R.string.port_select_error));
        }
    }


    private void doRequestPermission() {

        XPermissionUtils.requestPermissions(this, MyConstant.REQUEST_CODE_PERMISSION,
                                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                                    Manifest.permission.BLUETOOTH},
                                            new XPermissionUtils.OnPermissionListener() {
                                                @Override
                                                public void onPermissionGranted() {
                                                    Lg.e("onPermissionGranted");
                                                    isper=true;
                                                    showShortToast("获取权限成功");
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
                                                        DialogUtil.showPermissionManagerDialog(SelectPortsActivity.this, sBuilder.toString());
                                                    }
                                                }
                                            });
    }
}
