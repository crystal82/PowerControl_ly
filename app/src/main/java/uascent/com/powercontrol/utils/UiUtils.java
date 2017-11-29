package uascent.com.powercontrol.utils;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;
import android.view.Gravity;

/**
 * 作者：HWQ on 2017/2/23 19:29
 * 描述：
 */

public class UiUtils {

    private static NotificationManager mNotificationManager;
    private static Notification        notification;

    /**
     * 创建通知，  * 请在调用此方法时开启子线程
     *
     * @param context    上下文
     * @param icon       通知图片
     * @param tickerText 通知未拉开的内容
     * @param title      通知标题
     * @param content    通知主内容
     * @param intent     意图
     * @param id
     */
    public static void createNotification(
            Context context, int icon, String tickerText, String title,
            String content, Intent intent, int id) {
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        PendingIntent              pendingIntent = PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder mBuilder      = new NotificationCompat.Builder(context);
        mBuilder.setContentTitle(title)
                .setContentText(content)
                .setContentIntent(pendingIntent)
                .setTicker(tickerText)
                .setWhen(System.currentTimeMillis())
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setOngoing(false)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setSmallIcon(icon);

        notification = mBuilder.build();

        notification.flags = Notification.FLAG_ONGOING_EVENT;
        notification.flags = Notification.FLAG_AUTO_CANCEL;

        mNotificationManager.notify(id, notification);
    }

    public static void cancelNotification(Context context, int id) {
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        mNotificationManager.cancel(id);
    }

    public static ProgressDialog showProgress(Activity activity, String hintText, boolean cancelAble) {
        Activity mActivity = null;
        if (activity.getParent() != null) {
            mActivity = activity.getParent();
            if (mActivity.getParent() != null) {
                mActivity = mActivity.getParent();
            }
        } else {
            mActivity = activity;
        }
        final Activity finalActivity = mActivity;
        ProgressDialog window        = ProgressDialog.show(finalActivity, "", hintText);
        window.getWindow().setGravity(Gravity.CENTER);

        window.setCancelable(cancelAble);
        window.setCanceledOnTouchOutside(false);
        return window;
    }
}
