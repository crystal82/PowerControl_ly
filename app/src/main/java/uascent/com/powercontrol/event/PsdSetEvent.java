package uascent.com.powercontrol.event;

/**
 * 作者：HWQ on 2017/5/12 17:27
 * 描述：
 */
public class PsdSetEvent {

    public boolean setState = false;

    public PsdSetEvent(boolean setState) {
        this.setState = setState;
    }
}
