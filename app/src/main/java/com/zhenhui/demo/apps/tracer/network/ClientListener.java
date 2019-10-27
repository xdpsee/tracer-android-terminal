package com.zhenhui.demo.apps.tracer.network;

public interface ClientListener {

    /**
     * 对消息的处理
     */
    void onMessageReceived(String message);

    /**
     * 当服务状态发生变化时触发
     */
    void onConnectStatusChanged(ConnectStatus status);

}
