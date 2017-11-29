package uascent.com.powercontrol.constant;

/**
 * 作者：HWQ on 2017/5/8 19:01
 * 描述：
 */

public interface SpConstant {
    String IS_PORT4_INIT_LIGHT       = "is_port4_init_light"; //初始化标记,下次选择model则不进入
    String IS_PORT8_INIT_LIGHT       = "is_port8_init_light";
    String PORTS_NUM                 = "ports_num";
    String DATA_PORTS4_LIGHT         = "data_ports4_light";//数据格式:id,lightness,flash,switch|id,lightness,flash,switch
    String DATA_PORTS8_LIGHT         = "data_ports8_light";
    String CAR_MODEL                 = "car_model";
    String ALARM_VOLTAGE             = "alarm_voltage"; //报警电量！！计算百分比
    String TIME_SHUTDOWN_NUM         = "time_shutdown_num";
    String BLE_CONNECTED_DEVICE_MAC  = "ble_connected_device_mac";//当前连接设别mac,name
    String BLE_CONNECTED_DEVICE_NAME = "ble_connected_device_name";
    String IS_SHAKE                  = "is_shake";  //报警模式选择
    String ALARM_VOLTAGE_TYPE        = "alarm_voltage_type";  //报警模式选择
    String ALARM_VOLTAGE_RAW_ID      = "alarm_voltage_raw_id"; //报警音音乐资源文件ID
    String BLE_CONNECT_PSD           = "ble_connect_psd";//蓝牙连接，鉴权密码
    String BLE_CONNECT_CHANGE_PSD    = "ble_connect_change_psd";//修改的密码(在主界面设置回调，进行过度！)
}
