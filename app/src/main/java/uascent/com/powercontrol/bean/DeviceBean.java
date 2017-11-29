package uascent.com.powercontrol.bean;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import uascent.com.powercontrol.utils.Lg;

/**
 * 作者：HWQ on 2017/5/25 16:02
 * 描述：设备状态解析
 */

public class DeviceBean {

    public int    energyPercentNum; //电量百分比
    public int    overflowingLedInfo;//LED过流保护信息
    public String overflowingLedStr;

    public int deviceState;//设备状态   00000xxx Bit0 低电 Bit1 故障 Bit2 过温(0正常)
    public boolean lowEnergy         = false;
    public boolean breakdown         = false;
    public boolean heightTemperature = false;

    public int     lowAlarm;//低电电压
    public int     timingNum;//运行时间
    public boolean switchState;//开关状态

    /**
     * 设备状态信息
     *
     * @param bean
     * @param stateInfo
     * @return true表示出现异常
     */
    public static boolean parseStateInfo(DeviceBean bean, int stateInfo) {
        bean.deviceState = stateInfo;

        bean.lowEnergy = (stateInfo & 0x01) == 1;
        bean.breakdown = (stateInfo & 0x02) == 2;
        bean.heightTemperature = (stateInfo & 0x04) == 4;

        return bean.lowEnergy || bean.breakdown || bean.heightTemperature;//true表示出现异常
    }


    private static String[] lightDesc = {"0", "18A", "2", "3", "4", "2B", "2A", "18B", ""};//每个端口对应的描述

    /**
     * Led灯泡状态
     *
     * @param bean
     * @param ledInfo
     * @return true表示出现异常
     */
    public static boolean parseLedInfo(DeviceBean bean, int ledInfo) {
        bean.overflowingLedInfo = ledInfo;

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            if ((ledInfo & (0x01 << i)) == (0x01 << i)) {
                builder.append(lightDesc[i + 1]).append(",");
            }
        }
        if (builder.length() > 0) {
            Lg.d("parseLedInfo-----builder:" + builder.toString());
            bean.overflowingLedStr = builder.substring(0, builder.length() - 1);
        }
        return builder.toString().length() > 0;
    }

    @Override
    public String toString() {
        return "DeviceBean{" +
                "energyPercentNum=" + energyPercentNum +
                ", overflowingLedInfo=" + overflowingLedInfo +
                ", overflowingLedStr='" + overflowingLedStr + '\'' +
                ", deviceState=" + deviceState +
                ", lowEnergy=" + lowEnergy +
                ", breakdown=" + breakdown +
                ", heightTemperature=" + heightTemperature +
                '}';
    }

    public String deviceStateStr() {
        StringBuilder sb = new StringBuilder();
        if (lowEnergy) {
            sb.append("\nBattery Voltage Is Low");
        }
        //if (breakdown) {
        //    sb.append("\nBreakdown=" + (breakdown ? "error" : "normal"));
        //}
        if (heightTemperature) {
            sb.append("\nDevice Overheat");
        }

        if (sb.length() > 1) {
            return sb.substring(1, sb.length());
        }
        return sb.toString();
    }
}
