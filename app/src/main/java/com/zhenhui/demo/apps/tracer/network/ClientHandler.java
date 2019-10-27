package com.zhenhui.demo.apps.tracer.network;

import android.util.Log;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ClientHandler extends SimpleChannelInboundHandler<String> {
    private static final String TAG = ClientHandler.class.getName();
    private ClientListener listener;

    public ClientHandler(ClientListener listener) {
        this.listener = listener;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        NettyClient.getInstance().setConnectStatus(true);
        listener.onConnectStatusChanged(ConnectStatus.STATUS_CONNECT_SUCCESS);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        NettyClient.getInstance().setConnectStatus(false);
        listener.onConnectStatusChanged(ConnectStatus.STATUS_CONNECT_CLOSED);
        NettyClient.getInstance().reconnect();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String byteBuf) throws Exception {
        Log.e(TAG, "thread == " + Thread.currentThread().getName());
        Log.e(TAG, "来自服务器的消息 ====》" + byteBuf);
        listener.onMessageReceived(byteBuf);

    }
}
