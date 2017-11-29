package uascent.com.powercontrol.base;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import java.util.HashSet;

import uascent.com.powercontrol.MyApplication;
import uascent.com.powercontrol.R;
import uascent.com.powercontrol.utils.DialogUtil;
import uascent.com.powercontrol.utils.Lg;
import uascent.com.powercontrol.utils.StatusBarUtil;
import uascent.com.powercontrol.utils.XPermissionUtils;

public class BaseActivity extends Activity {

    private HashSet<Dialog> mHashSet = new HashSet<>();
    private Toast  mToast;
    private Dialog mDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setStatusBar();
        MyApplication.getAppInstance().addActivity(this);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    //设置状态栏
    private void setStatusBar() {
        StatusBarUtil.setColor(this, getResources().getColor(R.color.colorPrimaryDark));
    }

    @Override
    protected void onDestroy() {
        MyApplication.getAppInstance().removeActivity(this);
        //MyApplication.getRefWatcher().watch(this);
        super.onDestroy();
    }

    public void showLongToast(String text) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(this, text, Toast.LENGTH_LONG);
        mToast.show();
    }

    public void showShortToast(String text) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        mToast.show();
    }

    public void showLoadingDialog() {
        closeLoadingDialog();
        mDialog = DialogUtil.createLoadingDialog(this, getResources().getString(R.string.loading));
        mDialog.setCancelable(true);
        mDialog.show();
        mHashSet.add(mDialog);

    }

    public void showLoadingDialog(String msg) {
        closeLoadingDialog();
        mDialog = DialogUtil.createLoadingDialog(this, msg);
        mDialog.setCancelable(true);
        mDialog.show();
        mHashSet.add(mDialog);

        mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                onDialogCancel();
            }
        });
    }

    public void showLoadingDialog(String msg, boolean cancelAble) {
        closeLoadingDialog();
        mDialog = DialogUtil.createLoadingDialog(this, msg);
        mDialog.setCancelable(cancelAble);
        mDialog.show();
        mHashSet.add(mDialog);

        mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                onDialogCancel();
            }
        });
    }

    protected void onDialogCancel() {
        Log.e("hjq", "onDialogCancel called");
    }

    public boolean closeLoadingDialog() {
        if (mHashSet == null) {
            return false;
        }
        for (Dialog dialog : mHashSet) {
            dialog.dismiss();
        }
        mHashSet.clear();
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Lg.d("PermissionsResult");
        XPermissionUtils.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
