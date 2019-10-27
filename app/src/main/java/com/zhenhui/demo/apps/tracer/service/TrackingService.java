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

import com.amap.api.location.AMapLocation;
import com.zhenhui.demo.apps.tracer.R;
import com.zhenhui.demo.apps.tracer.SettingsActivity;
import com.zhenhui.demo.apps.tracer.location.LocationListener;
import com.zhenhui.demo.apps.tracer.location.LocationManager;
import com.zhenhui.demo.apps.tracer.network.ClientListener;
import com.zhenhui.demo.apps.tracer.network.ConnectStatus;
import com.zhenhui.demo.apps.tracer.network.NettyClient;
import com.zhenhui.demo.apps.tracer.storage.entities.DaoMaster;
import com.zhenhui.demo.apps.tracer.storage.entities.DaoSession;
import com.zhenhui.demo.apps.tracer.storage.entities.Location;

import java.util.Date;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;

public class TrackingService extends Service implements ClientListener, LocationListener {

    private static final String TAG = TrackingService.class.getName();

    private static final String CHANNEL_ID = "com.zhenhui.demo.tracer.service.channel";

    private LocationManager locationManager;

    private NetworkReceiver receiver;

    private DaoSession daoSession;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        this.locationManager = new LocationManager(this, this);
        receiver = new NetworkReceiver();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);

        DaoMaster.OpenHelper openHelper = new DaoMaster.DevOpenHelper(this, "tracer.db");
        DaoMaster master = new DaoMaster(openHelper.getWritableDatabase());
        daoSession = master.newSession();
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

        daoSession.getDatabase().close();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        NettyClient.getInstance().setReconnectNum(0);
        NettyClient.getInstance().disconnect();

        super.onDestroy();
    }

    private void connectServer() {
        if (!NettyClient.getInstance().getConnectStatus()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    NettyClient.getInstance().connect();
                }
            }).start();
        }
    }

    @Override
    public void onMessageReceived(String message) {

        Log.d(TAG, "onMessageReceived:" + message);

    }

    @Override
    public void onConnectStatusChanged(ConnectStatus status) {
        if (status == ConnectStatus.STATUS_CONNECT_SUCCESS) {
            Log.e(TAG, "connectServer successful");
            sendRegistryMessage();
        } else {
            Log.e(TAG, "connectServer failed, status = " + status);
        }
    }

    @Override
    public void locationChanged(final AMapLocation location) {

        Log.d(TAG, "location changed, " + location.toStr());

        if (!NettyClient.getInstance().sendMessage("", new FutureListener() {
            @Override
            public void operationComplete(Future future) throws Exception {
                if (!future.isSuccess()) {
                    saveLocation(location);
                } else {
                    Log.i(TAG, "send message success, ");
                }
            }
        })) {
            saveLocation(location);
        }
    }

    private void saveLocation(AMapLocation location) {
        try {
            Location loc = new Location();
            loc.setLatitude(location.getLatitude());
            loc.setLongitude(location.getLongitude());
            loc.setAccuracy((double) location.getAccuracy());
            loc.setSpeed((double) location.getSpeed());
            loc.setTimestamp(new Date(location.getTime()));
            loc.setStatus(0);

            daoSession.getLocationDao().save(loc);
        } catch (Exception e) {
            Log.e(TAG, "save location exception", e);
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
