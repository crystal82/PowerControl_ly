package uascent.com.powercontrol.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import uascent.com.powercontrol.R;

public class DialogUtil {

    public static void showPermissionManagerDialog(final Context context, String str) {
        new android.support.v7.app.AlertDialog.Builder(context).setTitle("获取" + str + "权限被禁用")
                .setMessage("请在 设置-应用管理-" + context.getString(R.string.app_name) + "-权限管理 (将" + str + "权限打开)")
                .setNegativeButton("取消", null)
                .setPositiveButton("去设置", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.parse("package:" + context.getPackageName()));
                        context.startActivity(intent);
                    }
                })
                .show();
    }

    public static void showDialog(Context context, int titleid, int msgid,
                                  int leftbtnid, int rightbtnid, OnClickListener LeftOnClickListener,
                                  OnClickListener RightOnClickListener, boolean cancelable) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setCancelable(false);
        builder.setTitle(titleid);
        builder.setMessage(msgid)
                .setNegativeButton(leftbtnid, LeftOnClickListener)
                .setPositiveButton(rightbtnid, RightOnClickListener).create()
                .show();
    }

    public static void showDialog(Context context, String title, String msg,
                                  String leftbtn, String rightbtn,
                                  OnClickListener LeftOnClickListener,
                                  OnClickListener RightOnClickListener, boolean cancelable) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setCancelable(cancelable);
        builder.setTitle(title).setMessage(msg)
                .setNegativeButton(leftbtn, LeftOnClickListener)
                .setPositiveButton(rightbtn, RightOnClickListener).create()
                .show();
    }

    public static void showNoTitleDialog(Context context, int msgid,
                                         int leftbtnid, int rightbtnid, OnClickListener LeftOnClickListener,
                                         OnClickListener RightOnClickListener, boolean cancelable) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setCancelable(cancelable);
        builder.setMessage(msgid)
                .setNegativeButton(leftbtnid, LeftOnClickListener)
                .setPositiveButton(rightbtnid, RightOnClickListener).create()
                .show();
    }

    public static void showNoTitleDialog(Context context, String msg,
                                         String leftbtn, String rightbtn,
                                         OnClickListener LeftOnClickListener,
                                         OnClickListener RightOnClickListener, boolean cancelable) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setCancelable(cancelable);
        builder.setMessage(msg).setNegativeButton(leftbtn, LeftOnClickListener)
                .setPositiveButton(rightbtn, RightOnClickListener).create()
                .show();
    }

    /**
     * 得到自定义的progressDialog
     *
     * @param context
     * @param msg
     * @return
     */
    public static Dialog createLoadingDialog(Context context, String msg) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View           v        = inflater.inflate(R.layout.dialog_loading, null);// 得到加载view
        LinearLayout   layout   = (LinearLayout) v.findViewById(R.id.dialog_view);// 加载布局
        // main.xml中的ImageView
        ProgressBar spaceshipImage = (ProgressBar) v.findViewById(R.id.loading);
        TextView    tipTextView    = (TextView) v.findViewById(R.id.tipTextView);// 提示文字
        tipTextView.setText(msg);// 设置加载信息
        Dialog loadingDialog = new Dialog(context, R.style.dialog_loading);// 创建自定义样式dialog
        loadingDialog.setCancelable(false);// 不可以用“返回键”取消
        loadingDialog.setContentView(layout, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));// 设置布局
        return loadingDialog;
    }

    public static AlertDialog getSelectDialog(Context context, boolean cancelable) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setCancelable(cancelable);


        return builder.create();

    }

    /**
     * 网络断开提示对话框
     */
    /*
    public static void showComReminderDialog(final Context context) {
        final ComReminderDialog comReminderDialog = new ComReminderDialog(context,
                context.getResources().getString(R.string.net_has_breaked)
                , context.getResources().getString(R.string.cancel), context.getResources().getString(R.string.ensure));
        comReminderDialog.setCanceledOnTouchOutside(false);
        comReminderDialog.show();
        comReminderDialog.dialog_cancel.setOnClickListener(new View.OnClickListener() {
                                                               @Override
                                                               public void onClick(View v) {
                                                                   comReminderDialog.cancel();
                                                               }
                                                           }
        );
        comReminderDialog.dialog_submit.setOnClickListener(new View.OnClickListener() {
                                                               @Override
                                                               public void onClick(View v) {
                                                                   comReminderDialog.cancel();
                                                                   if (android.os.Build.VERSION.SDK_INT > 13) {
                                                                       context.startActivity(new Intent(
                                                                               android.provider.Settings.ACTION_SETTINGS));
                                                                   } else {
                                                                       context.startActivity(new Intent(
                                                                               android.provider.Settings.ACTION_WIRELESS_SETTINGS));
                                                                   }
                                                               }
                                                           }
        );
    }*/


}
