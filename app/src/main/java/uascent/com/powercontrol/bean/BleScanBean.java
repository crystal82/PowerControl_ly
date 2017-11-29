package uascent.com.powercontrol.bean;

/**
 * 作者：HWQ on 2017/5/16 15:57
 * 描述：
 */

public class BleScanBean {

    public String mac;
    public String name;
    public boolean state = false;//true 连接

    public BleScanBean(String mac, String name) {
        this.mac = mac;
        this.name = name;
    }

    public BleScanBean(String mac, String name, boolean state) {
        this.mac = mac;
        this.name = name;
        this.state = state;
    }

    @Override
    public String toString() {
        return "BleScanBean{" +
                "mac='" + mac + '\'' +
                ", name='" + name + '\'' +
                ", state=" + state +
                '}';
    }
}
