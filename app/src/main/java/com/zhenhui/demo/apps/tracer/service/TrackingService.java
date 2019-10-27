package com.zhenhui.demo.apps.tracer.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.zhenhui.demo.apps.tracer.R;
import com.zhenhui.demo.apps.tracer.SettingsActivity;
import com.zhenhui.demo.apps.tracer.location.LocationManager;
import com.zhenhui.demo.apps.tracer.network.ClientListener;
import com.zhenhui.demo.apps.tracer.network.ConnectStatus;
import com.zhenhui.demo.apps.tracer.network.NettyClient;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class TrackingService extends Service implements ClientListener {

    private static final String TAG = TrackingService.class.getName();

    private static final String CHANNEL_ID = "com.zhenhui.demo.tracer.service.channel";

    private LocationManager locationManager;

    private NetworkReceiver receiver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        this.locationManager = new LocationManager(this);
        receiver = new NetworkReceiver();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        createNotificationChannel();
        Intent notificationIntent = new Intent(this, SettingsActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("定位服务已开启")
                .setContentText("")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);

        if (!locationManager.isStarted()) {
            locationManager.startLocation();
        }

        NettyClient.getInstance().setListener(this);
        connectServer();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        locationManager.shutdown();

        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        NettyClient.getInstance().setReconnectNum(0);
        NettyClient.getInstance().disconnect();
    }

    private void connectServer() {
        if (!NettyClient.getInstance().getConnectStatus()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    NettyClient.getInstance().connect();//连接服务器
                }
            }).start();
        }
    }

    @Override
    public void onMessageReceived(String message) {

        Log.d(TAG, message);

    }

    @Override
    public void onConnectStatusChanged(ConnectStatus statusCode) {
        if (statusCode == ConnectStatus.STATUS_CONNECT_SUCCESS) {
            Log.e(TAG, "connectServer successful");
            sendRegistryMessage();
        } else {
            Log.e(TAG, "connectServer fail status = " + statusCode);
        }
    }

    /**
     * 发送认证信息
     */
    private void sendRegistryMessage() {
        NettyClient.getInstance().sendMessage("##1,888888888888888#", null);
    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Tacking Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    public class NetworkReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            if (activeNetwork != null) { // connected to the internet
                if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI
                        || activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                    connectServer();
                    Log.e(TAG, "connecting ...");
                }
            }
        }
    }

}
