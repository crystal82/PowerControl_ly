package uascent.com.powercontrol.bean;

import android.view.View;

/**
 * 作者：HWQ on 2017/5/9 10:50
 * 描述：
 */

public class LightBean {
    public String number      = "0";//灯泡对应界面位置
    public String id          = "0";//当前灯泡对应端口
    public String brightness  = "0";//亮度值0~100
    public String flash       = "0";//闪烁值0~100
    public String switchState = "0";//1开,0关
    public String describe    = ""; //描述信息
    public View   mView       = null; //当前选择的View，方便PositionSelect使用

    public LightBean() {
    }

    public LightBean(String number, String id, String brightness, String flash, String switchState, String describe) {
        this.number = number;
        this.id = id;
        this.brightness = brightness;
        this.flash = flash;
        this.switchState = switchState;
        this.describe = describe;
    }

    //灯泡数据解析,设置灯泡
    @Override
    public String toString() {
        return number + ","
                + id + ","
                + brightness + ","
                + flash + ","
                + switchState + ","
                + describe;
    }

    //蓝牙数据发送，不需要Num
    public String toSendString() {
        return id + ","
                + brightness + ","
                + flash + ","
                + switchState;
    }
}
