package uascent.com.powercontrol.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.clj.fastble.utils.HexUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import uascent.com.percent.support.PercentLayoutHelper;
import uascent.com.powercontrol.constant.MyConstant;
import uascent.com.powercontrol.R;
import uascent.com.powercontrol.bean.LightBean;

/**
 * 作者：HWQ on 2017/5/9 11:09
 * 描述：
 */

public class MyUtils {


    private MyUtils() {
    }

    //拼接Ble数据
    public static byte[] getBleData(byte cmd, String data) {
        int sum = 0;
        byte[] info = HexUtil.hexStringToBytes2(data);
        byte[] bleData = new byte[info.length + 2];
        bleData[0] = cmd;
        for (int i = 0; i < info.length; i++) {
            bleData[i + 1] = info[i];
            sum += info[i];
        }
        bleData[bleData.length - 1] = (byte) (sum % 0xff);

        Lg.d("发送数据---" + Arrays.toString(bleData));
        return bleData;
    }

    public static byte[] getBleData(byte cmd, byte[] info) {
        int sum = 0;
        byte[] bleData = new byte[info.length + 2];
        bleData[0] = cmd;
        for (int i = 0; i < info.length; i++) {
            bleData[i + 1] = info[i];
            sum += info[i];
        }
        bleData[bleData.length - 1] = (byte) (sum % 0xff);

        return bleData;
    }

    //播放报警音乐
    public static MediaPlayer playAlarmRingtone(Context context, int rawId, boolean isLooping) {
        MediaPlayer sMediaPlayer = null;

        if (rawId != 0) {     // 免打扰模式没有打开，播放声音
            sMediaPlayer = MediaPlayer.create(context, rawId);
            sMediaPlayer.setLooping(isLooping);
            sMediaPlayer.start();
        }

        return sMediaPlayer;
    }

    //停止报警音乐
    public static void stopAlertRingtone(MediaPlayer mediaPlayer) {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
            } catch (IllegalStateException e) {
                // TODO 如果当前java状态和jni里面的状态不一致，
                //e.printStackTrace();
                mediaPlayer = new MediaPlayer();
                mediaPlayer.stop();
            }
            mediaPlayer.release();
        }
    }

    //sVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    public static Vibrator vibrateAction(Context context, int vibratorTime) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        //震动
        long[] pattern = {400, 400, 400, 400, 400, 400}; // 停止 开启 停止 开启
        vibrator.vibrate(pattern, vibratorTime); //重复两次上面的pattern 如果只想震动一次，index设为-1

        return vibrator;
    }

    public static void stopVibrator(Vibrator vibrator) {
        if (vibrator != null) {
            vibrator.cancel();
        }
    }

    //解析SP中加载的String，
    //格式  number, id, brightness, flash, switchState |
    public static HashMap<String, LightBean> analysisLightData(String lightDataStr, boolean keyIsNum) {
        HashMap<String, LightBean> lightMap = new HashMap<>();
        if (TextUtils.isEmpty(lightDataStr)) {
            return null;
        }
        Lg.d("当前灯泡数据:" + lightDataStr);
        String[] splitDataArr = lightDataStr.split(MyConstant.LIGHT_SP_DIVISION);
        for (String lightStr : splitDataArr) {
            Lg.d("lightData:" + lightStr);
            String[] lightData = lightStr.split(MyConstant.LIGHT_DATA_DIVISION);
            LightBean light = new LightBean(lightData[0], lightData[1],
                    lightData[2], lightData[3],
                    lightData[4], lightData[5]);
            lightMap.put(keyIsNum ? lightData[0] : lightData[1], light); //[0]位为number,[1]位为id
        }
        return lightMap;
    }

    public interface DialogAble {
        void onDataSet(View layout, AlertDialog dialog);
    }

    //显示dialog
    public static AlertDialog showDialog(Activity activity, int layoutId, int viewGroupId, DialogAble dialogAble) {
        LayoutInflater inflater = activity.getLayoutInflater();
        View layout = inflater.inflate(layoutId,
                (ViewGroup) activity.findViewById(viewGroupId));
        AlertDialog dialog = new AlertDialog.Builder(activity, AlertDialog.THEME_HOLO_LIGHT).show();
        dialogAble.onDataSet(layout, dialog);//设置数据
        dialog.getWindow().setContentView(layout);
        return dialog;
    }

    public static AlertDialog showDialog(Activity activity, boolean cancelAbl, int layoutId, int viewGroupId, DialogAble dialogAble) {
        LayoutInflater inflater = activity.getLayoutInflater();
        View layout = inflater.inflate(layoutId,
                (ViewGroup) activity.findViewById(viewGroupId));
        AlertDialog dialog = new AlertDialog.Builder(activity, AlertDialog.THEME_HOLO_LIGHT).show();
        dialogAble.onDataSet(layout, dialog);//设置数据
        dialog.setCancelable(cancelAbl);
        dialog.getWindow().setContentView(layout);

        return dialog;
    }

    //显示dialog
    public static AlertDialog showEditDialog(Activity activity, int layoutId, int viewGroupId, DialogAble dialogAble) {
        LayoutInflater inflater = activity.getLayoutInflater();
        View layout = inflater.inflate(layoutId,
                (ViewGroup) activity.findViewById(viewGroupId));
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, AlertDialog.THEME_HOLO_LIGHT);
        builder.setView(new EditText(activity));
        AlertDialog dialog = builder.show();
        dialogAble.onDataSet(layout, dialog);//设置数据
        dialog.getWindow().setContentView(layout);

        return dialog;
    }

    public static void setDrawable(Context context, View view, int drawableId) {
        Resources resources = context.getResources();
        view.setBackgroundDrawable(resources.getDrawable(drawableId));
    }

    //根据传入编号，返回对应车model图片
    public static void setCarModel(Context context, ImageView imageView, int selectMode, boolean isWhite) {
        Resources resources = context.getResources();
        switch (selectMode) {
            case MyConstant.MODEL_CAR:
                imageView.setImageDrawable(resources.getDrawable(isWhite ? R.mipmap.select_car_white : R.mipmap.select_car));
                break;
            case MyConstant.MODEL_TRUCK:
                imageView.setImageDrawable(resources.getDrawable(isWhite ? R.mipmap.select_truck_white : R.mipmap.select_truck));
                break;
            case MyConstant.MODEL_JEEP:
                imageView.setImageDrawable(resources.getDrawable(isWhite ? R.mipmap.select_jeep_white : R.mipmap.select_jeep));
                break;
            case MyConstant.MODEL_ATV:
                imageView.setImageDrawable(resources.getDrawable(isWhite ? R.mipmap.select_atv_white : R.mipmap.select_atv));
                break;
        }
    }

    /**
     * 改变控件百分比
     *
     * @param view    指定控件
     * @param percent 百分比
     */
    public static void setViewPercent(View view, float percent) {
        PercentLayoutHelper.PercentLayoutParams layoutParams = (PercentLayoutHelper.PercentLayoutParams) view.getLayoutParams();
        PercentLayoutHelper.PercentLayoutInfo percentLayoutInfo = layoutParams.getPercentLayoutInfo();
        PercentLayoutHelper.PercentLayoutInfo.PercentVal percentVal = new PercentLayoutHelper.PercentLayoutInfo.PercentVal();
        percentVal.basemode = PercentLayoutHelper.PercentLayoutInfo.BASEMODE.BASE_WIDTH;
        percentVal.percent = percent;
        percentLayoutInfo.widthPercent = percentVal;
        percentVal.basemode = PercentLayoutHelper.PercentLayoutInfo.BASEMODE.BASE_WIDTH;
        percentVal.percent = percent;
        percentLayoutInfo.heightPercent = percentVal;

        view.setLayoutParams((ViewGroup.LayoutParams) layoutParams);
    }

    /**
     * 批量设置OnClickListener
     *
     * @param listener 监听器
     * @param views    可变参数view
     */
    public static void setOnClick(View.OnClickListener listener, View... views) {
        for (View view : views) {
            view.setOnClickListener(listener);
        }
    }

    public static void showGattService(BluetoothGatt bluetoothGatt) {
        List<BluetoothGattService> services = bluetoothGatt.getServices();
        for (BluetoothGattService service : services) {
            Lg.d("service:" + service.getUuid());
            List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
            for (BluetoothGattCharacteristic characteristic : characteristics) {
                Lg.d("characteristic:" + characteristic.getUuid());
            }
        }
    }

    public static int[] bytesToInts(byte[] bytes) {
        int[] ints = new int[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            ints[i] = bytes[i] & 0xFF;
        }
        return ints;
    }
}
