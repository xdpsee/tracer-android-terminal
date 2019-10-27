package com.zhenhui.demo.apps.tracer.network;

import android.util.Log;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.GenericFutureListener;

public class NettyClient {

    public final static String TAG = NettyClient.class.getName();

    private static NettyClient nettyClient = new NettyClient();

    private Bootstrap bootstrap;

    private EventLoopGroup group;

    private ClientListener listener;

    private Channel channel;

    private volatile boolean isConnect = false;

    private int reconnectNum = Integer.MAX_VALUE;

    private long reconnectIntervalTime = 5000;

    public static NettyClient getInstance() {
        return nettyClient;
    }

    private NettyClient() {
    }

    public synchronized NettyClient connect() {
        if (!isConnect) {
            group = new NioEventLoopGroup();
            bootstrap = new Bootstrap().group(group)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .channel(NioSocketChannel.class)
                    .handler(new ClientInitializer(listener));
            try {
                ChannelFuture future = bootstrap.connect(Constant.SOCKET_HOST, Constant.SOCKET_PORT).sync();
                if (future != null && future.isSuccess()) {
                    channel = future.channel();
                    isConnect = true;
                } else {
                    isConnect = false;
                }


            } catch (Exception e) {
                e.printStackTrace();
                listener.onConnectStatusChanged(ConnectStatus.STATUS_CONNECT_ERROR);
                reconnect();
            }
        }
        return this;
    }

    public synchronized void disconnect() {
        group.shutdownGracefully();
    }

    public void reconnect() {
        if (reconnectNum > 0 && !isConnect) {
            reconnectNum--;
            try {
                Thread.sleep(reconnectIntervalTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            disconnect();
            connect();
        } else {
            disconnect();
        }
    }

    /**
     * 发送消息
     *
     * @param futureListener 发送成功与否的监听
     */
    public synchronized boolean sendMessage(final String message, final FutureListener futureListener) {
        boolean flag = channel != null && isConnect;
        if (!flag) {
            Log.e(TAG, "------尚未连接");
            return false;
        }

        if (futureListener == null) {
            channel.writeAndFlush(message).addListener(new GenericFutureListener<Future<? super Void>>() {
                @Override
                public void operationComplete(Future<? super Void> future) throws Exception {
                    if (future.isSuccess()) {
                        Log.d(TAG, "send message success, " + message);
                    } else {
                        Log.d(TAG, "send message failure, " + message);
                    }
                }
            });
        } else {
            channel.writeAndFlush(message).addListener(futureListener);
        }

        return true;
    }

    /**
     * 设置重连次数
     *
     * @param reconnectNum 重连次数
     */
    public void setReconnectNum(int reconnectNum) {
        this.reconnectNum = reconnectNum;
    }

    /**
     * 设置重连时间间隔
     *
     * @param reconnectIntervalTime 时间间隔
     */
    public void setReconnectIntervalTime(long reconnectIntervalTime) {
        this.reconnectIntervalTime = reconnectIntervalTime;
    }

    public boolean getConnectStatus() {
        return isConnect;
    }

    /**
     * 设置连接状态
     *
     * @param status
     */
    public void setConnectStatus(boolean status) {
        this.isConnect = status;
    }

    public void setListener(ClientListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener == null ");
        }
        this.listener = listener;
    }

}
