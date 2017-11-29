package uascent.com.powercontrol.event;

/**
 * 作者：HWQ on 2017/5/12 17:27
 * 描述：
 */
public class WriteEvent {

    public static String psdWrite = "psdWrite";
    public String writeState;
    public byte[] info;

    public WriteEvent(String writeState, byte[] info) {
        this.writeState = writeState;
        this.info = info;
    }
}
