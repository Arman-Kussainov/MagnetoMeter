package com.arman.magnetometer;

import static com.google.android.material.internal.ContextUtils.getActivity;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class MyGPSService extends Service {
    // 04.03.2023 moving GPS services from fragment ...
//    LocationTrack locationTrack = new LocationTrack(this);
    private double current_longitude = 0, longitude = 0;
    private double current_latitude = 0, latitude = 0;

    // https://androidexperinz.wordpress.com/2012/02/14/communication-between-service-and-activity-part-1/
    private static final String ACTION_STRING_SERVICE = "ToService";
    private static final String ACTION_STRING_ACTIVITY = "ToActivity";
    private BroadcastReceiver serviceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v("GPS", "public void onReceive(Context context, Intent intent)");
            String meme = intent.getExtras().getString("EXTRA_FROM_MAIN");
            sendBroadcast();
            Log.v("GPS", meme);
        }
    };

    private MediaPlayer player;
    private Runnable busyLoop = new Runnable() {
        //LocationTrack locationTrack = new LocationTrack(MyGPSService.this);

        public void run() {
                try {
                    Thread.sleep(100);
                   // LocationTrack locationTrack = new LocationTrack(getApplicationContext());
//                    Log.v("CCP", "+"+String.valueOf(locationTrack));
//                    Log.v("CCP", "+"+String.valueOf(MyGPSService.this));
//
//                    longitude = locationTrack.getLocation().getLongitude();
//                    latitude = locationTrack.getLocation().getLatitude();

                    player = MediaPlayer.create( getApplicationContext(), Settings.System.DEFAULT_RINGTONE_URI );
                    player.setLooping( true );
                    player.start();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.v("METOO","METOO");
        }
    };

    Thread t = new Thread(busyLoop);

    @Override
    public void onCreate() {
        super.onCreate();
//STEP2: register the receiver
        if (serviceReceiver != null) {
            Log.v("GPS", "register the receiver in MyGPSService");
            IntentFilter intentFilter = new IntentFilter(ACTION_STRING_SERVICE);
            registerReceiver(serviceReceiver, intentFilter);

            //Log.v("GPS", String.valueOf(registerReceiver(serviceReceiver, intentFilter)));
        }
    }


    @Override
    // execution of service will start
    // on calling this method
    public int onStartCommand(Intent intent, int flags, int startId) {
         //Thread t = new Thread(busyLoop);
        if (!t.isAlive()) {
            t.start();
        }
        return START_STICKY;
    }

    @Override
    // execution of the service will
    // stop on calling this method
    public void onDestroy() {
        super.onDestroy();

        Log.v("GPS", "in Service's onDestroy");
//STEP3: Unregister the receiver
        unregisterReceiver(serviceReceiver);

        // Do I need to kill a thread to free resources?..
        t.interrupt();
        // stopping the process
        player.stop();
       // locationTrack.stopListener();
    }

    //send broadcast from activity to all receivers listening to the action "ACTION_STRING_ACTIVITY"
    private void sendBroadcast() {
        Intent new_intent = new Intent();
        //int phoneNo=123;
        //new_intent.putExtra("extra",123);
        new_intent.setAction(ACTION_STRING_ACTIVITY);
        Log.v("GPS","sendBroadcast in GPSService");

        for(int i=1;i<=12;i++){
        new_intent.putExtra("EXTRA_FROM_SRVC", i);
        sendBroadcast(new_intent);}
    }

    @Nullable
    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

 }