package com.arman.magnetometer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;


//package com.mobilemerit.usbhost;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;

///////////////////////////////////////////
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentManager;

import java.util.ArrayList;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
///////////////////////////////////////////

//public class MainActivity
//        extends AppCompatActivity implements SensorEventListener, PlotDataToMap.PlotDataCallbacks {

public class MainActivity
        extends AppCompatActivity implements PlotDataToMap.PlotDataCallbacks, View.OnClickListener {

    //Strings to register to create intent filter for registering the recivers
    private static final String ACTION_STRING_SERVICE = "ToService";
    private static final String ACTION_STRING_ACTIVITY = "ToActivity";

    //STEP1: Create a broadcast receiver
    private BroadcastReceiver activityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v("GPS", "In main_activities onReceive");
            int state = intent.getExtras().getInt("EXTRA_FROM_SRVC");
            //state="stg";
            Log.v("GPS", String.valueOf(state));
        }
    };

    private Button start, stop;

    //public float B = 111;
    //public static float B;
    //**
    private static final String TAG_TASK_FRAGMENT = "task_fragment";
    private PlotDataToMap mTaskFragment;
    FragmentManager fm = getSupportFragmentManager();
    //**
    public static final String TAG = "CCCP";


    // https://www.digitalocean.com/community/tutorials/android-location-api-tracking-gps
    private ArrayList permissionsToRequest;
    private ArrayList permissionsRejected = new ArrayList();
    private ArrayList permissions = new ArrayList();

    private final static int ALL_PERMISSIONS_RESULT = 101;

    private ImageView imageView;
    public static Bitmap map_bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (activityReceiver != null) {
            IntentFilter intentFilter = new IntentFilter(ACTION_STRING_ACTIVITY);
            //Map the intent filter to the receiver
            registerReceiver(activityReceiver, intentFilter);
        }

  //      Start the service on launching the application
        startService(new Intent(this,MyGPSService.class));
        findViewById(R.id.startButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("SampleActivity", "Sending broadcast to service");
                sendBroadcast();
            }
        });

        // Inject the service from example
        start = (Button) findViewById( R.id.startButton );
        stop = (Button) findViewById( R.id.stopButton );

        start.setOnClickListener(this);
        stop.setOnClickListener(this);

        imageView = findViewById(R.id.Pic);

        permissions.add(ACCESS_FINE_LOCATION);
        permissions.add(ACCESS_COARSE_LOCATION);

        permissionsToRequest = findUnAskedPermissions(permissions);
        //get the permissions we have asked for before but are not granted..
        //we will store this in a global list to access later.

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissionsToRequest.size() > 0)
                requestPermissions((String[]) permissionsToRequest.toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
        }

//        https://stackoverflow.com/questions/48859451/let-app-run-in-background-in-android
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String packageName = getPackageName();
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                Intent intent = new Intent();
                intent.setAction(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                intent.setData(Uri.parse("package:" + packageName));
                startActivity(intent);
            }
        }

        mTaskFragment = (PlotDataToMap) fm.findFragmentByTag(TAG_TASK_FRAGMENT);

        if (mTaskFragment == null) {
            mTaskFragment = new PlotDataToMap();
            fm.beginTransaction().add(mTaskFragment, TAG_TASK_FRAGMENT).commit();
            Log.v(TAG, "Commiting the fragment");
        }

    }

    public void onClick(View view) {
        if(view == start){
        //    startService(new Intent( this, MyGPSService.class ) );
        //    sendBroadcast();
        }
        else if (view == stop){
            Log.v("f", String.valueOf(BIND_AUTO_CREATE));
            stopService(new Intent( this, MyGPSService.class ) );
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save the state of the TextView
//        outState.putString(TEXT_STATE,
//                mTextView.getText().toString());
    }

    // GPS related stuff. Added 23.11.2022
    // by AK from https://www.digitalocean.com/community/tutorials/android-location-api-tracking-gps

    private ArrayList findUnAskedPermissions(ArrayList wanted) {
        ArrayList result = new ArrayList();

        for (Object perm : wanted) {
            if (!hasPermission((String) perm)) {
                result.add(perm);
            }
        }

        return result;
    }

    private boolean hasPermission(String permission) {
        if (canMakeSmores()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
            }
        }
        return true;
    }

    private boolean canMakeSmores() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }


    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {

            case ALL_PERMISSIONS_RESULT:
                for (Object perms : permissionsToRequest) {
                    if (!hasPermission((String) perms)) {
                        permissionsRejected.add(perms);
                    }
                }

                if (permissionsRejected.size() > 0) {


                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale((String) permissionsRejected.get(0))) {
                            showMessageOKCancel("These permissions are mandatory for the application. Please allow access.",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermissions((String[]) permissionsRejected.toArray(new String[permissionsRejected.size()]), ALL_PERMISSIONS_RESULT);
                                            }
                                        }
                                    });
                            return;
                        }
                    }

                }

                break;
        }

    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //locationTrack.stopListener();
        Log.v(TAG, "in onDestroy");

        Log.v("GPS", "onDestroy");
//STEP3: Unregister the receiver
        unregisterReceiver(activityReceiver);
    }

    //send broadcast from activity to all receivers listening to the action "ACTION_STRING_SERVICE"
    private void sendBroadcast() {
        Intent new_intent = new Intent();
        new_intent.putExtra("EXTRA_FROM_MAIN", "666");
        new_intent.setAction(ACTION_STRING_SERVICE);

        sendBroadcast(new_intent);
        Log.v("GPS", "send broadcast from activity to all receivers");
    }

    @Override
    public void onProgressUpdate(Bitmap map_bitmap) {
        //Log.v(TAG, "in Progress Update");
        imageView.setImageBitmap(map_bitmap);

    }

    @Override
    public void onCancelled() {
        Log.v(TAG, "in onCancelled");
    }

    @Override
    public void onPostExecute() {
        Log.v(TAG, "in onPostExecute");
        mTaskFragment=null;
    }

    public TextView getLocationView() {
        TextView mDisplayLocationData=(TextView) findViewById(R.id.label_location);
        return mDisplayLocationData;
    }
    public TextView getMagnetometerView() {
        TextView mTextSensorMagnetometer = (TextView) findViewById(R.id.label_magnetometer);
        return mTextSensorMagnetometer;
    }
    public TextView getLightView() {
        TextView mTextSensorLight = (TextView) findViewById(R.id.label_light);
        return mTextSensorLight;
    }
}