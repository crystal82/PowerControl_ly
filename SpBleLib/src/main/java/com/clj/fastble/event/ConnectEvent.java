package com.clj.fastble.event;


/**
 * 作者：HWQ on 2017/5/12 17:27
 * 描述：
 */
public class ConnectEvent {

    public boolean connectState = false; //断开连接

    public ConnectEvent(boolean connectState) {
        this.connectState = connectState;
    }
}
