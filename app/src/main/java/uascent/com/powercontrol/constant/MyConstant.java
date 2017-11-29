package uascent.com.powercontrol.constant;

import uascent.com.powercontrol.R;

/**
 * 作者：HWQ on 2017/5/11 18:13
 * 描述：
 */

public interface MyConstant {
    int POWER_MAX = 16;
    int POWER_MIN = 9;
    //==========================权限RequestCode=============
    int REQUEST_CODE_PERMISSION = 201;

    //===============================回调函数关键字=================================
    String CALL_BACK_KEY_CONNECT = "call_back_key_connect";
    String CALL_BACK_KEY_MAIN    = "call_back_key_main";

    //===============================常量=================================
    String DEFAULT_BLE_PSD = "123456";  //蓝牙连接默认密码
    long   BLE_SCAN_TIME   = 8000;
    String UUID_SERVICE    = "0000fff0-0000-1000-8000-00805f9b34fb";
    String UUID_WRITE      = "0000fff1-0000-1000-8000-00805f9b34fb";
    String UUID_NOTIFY     = "0000fff2-0000-1000-8000-00805f9b34fb";

    byte BLE_SUCCESS = 1;
    byte BLE_ERROR   = 0;

    byte BLE_CMD_LED_CONTROL        = 1; //控制LED亮度、闪烁、开关
    byte BLE_CMD_GET_STATE          = 2; //读取设备状态（这个命令也会定时返回，APP可以获取电源电量）
    byte BLE_CMD_GET_LED_STATE      = 3; //读取指定灯泡状态  ID+bright+flash+switch
    byte BLE_CMD_GET_LED_ALL_SWITCH = 4; //读取所有LED的开关状态
    byte BLE_CMD_SET_VOLT           = 5; //设置低电报警
    byte BLE_CMD_SET_TIME_SHRESHOLD = 6; //设置自动关机时间
    byte BLE_CMD_PSD_CHANGE         = 7; //修改密码
    byte BLE_CMD_PSD_CHECK          = 8; //鉴权
    byte BLE_CMD_CHANGE_NAME        = 9; //修改蓝牙名称
    byte BLE_CMD_SWITCH_ALL         = 10; //总开关

    int NOTIFICATION_ALARM_ID = 996600;

    int PORTS_4 = 4;
    int PORTS_8 = 8;

    String LIGHT_SP_DIVISION   = "\\|";//灯泡sp分割
    String LIGHT_SP_DIVISION2  = "|";//灯泡sp分割,append!!不需要转意
    String LIGHT_DATA_DIVISION = "\\,";//灯泡数据分割

    //重置车灯
    int REQUEST_CODE_REST_LIGHT = 105;
    int RESULT_CODE_REST_LIGHT  = 106;
    int CANCEL                  = 10;
    int ENTER                   = 11;

    //连接返回
    int REQUEST_CODE_BLE_CONNECT = 103;
    int RESULT_CODE_BLE_CONNECT  = 104;


    //选择车辆模型
    String SELECTED_MODEL          = "selected_model";
    int    REQUEST_CODE_SELECT_CAR = 101;
    int    RESULT_CODE_SELECT_CAR  = 102;
    int    MODEL_CAR               = 100;
    int    MODEL_TRUCK             = 101;
    int    MODEL_JEEP              = 102;
    int    MODEL_ATV               = 103;

    //输入密码
    String CONNECT_PSD              = "connect_psd";
    int    REQUEST_CODE_CONNECT_PSD = 103;
    int    RESULT_CODE_CONNECT_PSD  = 104;

    //0表示震动
    int AUDIO_MP_3_RES[] = {0, R.raw.alarm1, R.raw.alarm2, R.raw.alarm3,
            R.raw.alarm4, R.raw.alarm5,
    };

    //设置界面
    int SET_VOLT_GROW = 15; //低电报警，增量--- 100~115（10~11.5）

}
