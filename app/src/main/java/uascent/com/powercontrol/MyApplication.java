package uascent.com.powercontrol;

import android.app.Activity;
import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import com.clj.fastble.BleManager;
import com.clj.fastble.bluetooth.BleBluetooth;

import java.util.LinkedList;

import uascent.com.powercontrol.bean.DeviceBean;
import uascent.com.powercontrol.constant.MyConstant;
import uascent.com.powercontrol.utils.Cockroach;
import uascent.com.powercontrol.utils.Lg;
import uascent.com.powercontrol.utils.SpHelper;

/**
 * 作者：HWQ on 2017/5/2 09:49
 * 描述：
 */

public class MyApplication extends Application {

    public LinkedList<Activity> activityList = new LinkedList<Activity>();
    private static MyApplication sAppInstance;
    private static BleManager    mBleManager;

    public static int        mCurrentPortsType = MyConstant.PORTS_4;//只需要4路
    public static boolean    sBleConnectState  = false;
    public static DeviceBean sDeviceBean       = new DeviceBean();

    //private       RefWatcher mRefWatcher;

    @Override
    public void onCreate() {
        super.onCreate();
        sAppInstance = this;
        mBleManager = new BleManager(this);

        BleBluetooth.initial(sAppInstance);
        SpHelper.initSP(this);
        installCockroach();
        //mRefWatcher = BuildConfig.DEBUG ? LeakCanary.install(this) : RefWatcher.DISABLED;
    }

    public static BleManager getBleManager() {
        return mBleManager;
    }

    public static MyApplication getAppInstance() {
        return sAppInstance;
    }

    public void addActivity(Activity activity) {
        activityList.add(activity);
    }

    public void removeActivity(Activity activity) {
        activityList.remove(activity);
    }

    //public static RefWatcher getRefWatcher() {
    //    return getAppInstance().mRefWatcher;
    //}

    public void installCockroach() {
        Cockroach.install(new Cockroach.ExceptionHandler() {

            // handlerException内部建议手动try{  你的异常处理逻辑  }catch(Throwable e){ } ，以防handlerException内部再次抛出异常，导致循环调用handlerException

            @Override
            public void handlerException(final Thread thread, final Throwable throwable) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            throwable.printStackTrace();
                            Lg.d("Exception Happend\n" + thread + "\n" + throwable.toString());
                            //                        throw new RuntimeException("..."+(i++));
                        } catch (Throwable e) {

                        }
                    }
                });
            }
        });
    }
}
