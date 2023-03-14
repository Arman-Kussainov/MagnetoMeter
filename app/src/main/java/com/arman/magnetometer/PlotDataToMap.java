package com.arman.magnetometer;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Random;

public class PlotDataToMap extends Fragment {

    private float B;

    interface PlotDataCallbacks {
        void onProgressUpdate(Bitmap map_bitmap);

        void onCancelled();

        void onPostExecute();

    }

    int do_once = 0;

    private PlotDataCallbacks PDCallbacks;
    private PlotDataCallTask PDTask;
    private static final String TAG = "CCCP";

    private double current_longitude = 0, longitude = 0;
    private double current_latitude = 0, latitude = 0;

    public Bitmap map_bitmap;
    public float B_local;
    public int R_max = 0;

    int height = Resources.getSystem().getDisplayMetrics().heightPixels;
    int width = Resources.getSystem().getDisplayMetrics().widthPixels;

    int x_center = (int) Math.round((double)width / 2.0);
    int y_center = (int) Math.round((double)width / 2.0);

    int R_marker=10; // in pixels
    double travel_distance = 6000.0; //  width of the observation window in meters
    float B_max=0.0f;
    // pixels to meters
    double pixels_to_meters=travel_distance/(double)width;
    // degrees to pixels
    double degrees_to_pixels=111139.0/travel_distance*(double)width;

    // min distance to trigger GPS data display
    // first version
    //double min_distance = 10.0 / 111139.0; // 1 degree = 111139 meters;
                                            // x degree = 10 meters

    // second version calculated from a marker's double size and changed to degrees
    double min_distance=(double)R_marker*2.0*pixels_to_meters/111139.0;


    public void onActivityCancelled() {
        PDTask.cancel(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        PDCallbacks = (PlotDataCallbacks) activity;
        Log.v(TAG, "I'm in onAttach!");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retain this fragment across configuration changes.
        setRetainInstance(true);
        PDTask = new PlotDataCallTask();
        PDTask.execute();

        //stopService(new Intent( this, MyGPSService.class ) );
        //getActivity().startService(new Intent(getActivity(),MyGPSService.class));
    }

    @Override
    public void onDetach() {
        super.onDetach();
        PDCallbacks = null;
        Log.v(TAG, "I'm on onDetach");
    }

    private class PlotDataCallTask extends AsyncTask<Void, Bitmap, String> implements SensorEventListener {

        private static final String TAG = "CCCP";
        // GPS data
        LocationTrack locationTrack = new LocationTrack(getContext());

        double center_longitude = 0.0, center_latitude = 0.0;

        // Magnetic and Light Sensors
        private SensorManager mSensorManager =
                (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        private Sensor mSensorProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        private Sensor mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        String sensor_error = getResources().getString(R.string.error_no_sensor);


//        if (mMagnetometer == null) {
//            mTextSensorLight.setText(sensor_error);
//        }
//        if (mSensorProximity == null) {
//            mTextSensorMagnetometer.setText(sensor_error);
//        }

        @Override
        protected String doInBackground(Void... voids) {

            map_bitmap = Bitmap.createBitmap(width, width,
                    Bitmap.Config.ARGB_8888);
            // Fill with the single color
            map_bitmap.eraseColor(Color.BLACK);

            //Log.v("CCCP", "min_distance "+String.valueOf(min_distance*111139.0)+" meters");
            //Log.v(TAG, String.valueOf(R_marker/(double)width*2.0*1.000));

            if (do_once != 1 && locationTrack.canGetLocation()) {
                center_longitude = locationTrack.getLongitude();
                center_latitude = locationTrack.getLatitude();
                do_once = 1;
            }

            if (mSensorProximity != null) {
                mSensorManager.registerListener((SensorEventListener) this, mSensorProximity,
                        SensorManager.SENSOR_DELAY_NORMAL);
                Log.v(TAG, "TRYING TO REGISTER");
                // do not forget to unregister them

            }
            if (mMagnetometer != null) {
                mSensorManager.registerListener((SensorEventListener) this, mMagnetometer,
                        SensorManager.SENSOR_DELAY_NORMAL);
            }

            return "DoInBackground is completed!";
        }

        @Override
        protected void onProgressUpdate(Bitmap... slice_bitmap0) {

            if (PDCallbacks != null) {
                PDCallbacks.onProgressUpdate(slice_bitmap0[0]);
            }

            //Log.v(TAG, String.valueOf(longitude)+" and " + String.valueOf(latitude));
            //mImageView.get().setImageBitmap(slice_bitmap0[0]);
        }

        @Override
        protected void onCancelled() {

            if (mSensorManager != null) {
                mSensorManager.unregisterListener(this);
                Log.v(TAG, mSensorManager + "asynctask's cancelled is called");
            }

            // Put in "ifs"
            locationTrack.stopListener();

            if (PDCallbacks != null) {
                PDCallbacks.onCancelled();
                Log.v(TAG, "asynctask's cancelled is called");
                PDTask.cancel(true);
            }

        }

        @Override
        protected void onPostExecute(String result) {
            if (PDCallbacks != null) {
                Log.v(TAG, "fragment's callbacks not equal to zero");
                PDCallbacks.onPostExecute();
            }
        }

        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            //Log.v(TAG, "in on sensor changed");
            int sensorType = sensorEvent.sensor.getType();
            float[] MValues = sensorEvent.values;

            float l_ight = 0.0f;
            l_ight = MValues[0];

            TextView mTextSensorLight = ((MainActivity) getActivity()).getLightView();
            TextView mTextSensorMagnetometer = ((MainActivity) getActivity()).getMagnetometerView();

            // create instance of Random class
            Random rand = new Random();

            switch (sensorType) {
                // Event came from the light sensor.
                case Sensor.TYPE_LIGHT:
                    // Handle light sensor
                    mTextSensorLight.setText("Light sensor " + Float.toString(l_ight));

                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    B_local = (float) Math.sqrt(MValues[0] * MValues[0] + MValues[1] * MValues[1] +
                            MValues[2] * MValues[2]);
//                    B_local = rand.nextFloat()*Math.round(B_local * 10.0f) / 10.0f;
                    B_local = Math.round(B_local * 10.0f) / 10.0f;

                    for (int i = 0; i < MValues.length; i++) {
                        MValues[i] = Math.round(MValues[i] * 10.0f) / 10.0f;
                    }
                    String XYZ_field = Arrays.toString(MValues);
                    XYZ_field = "Mag.values [Bx,By,Bz]=" + XYZ_field + ", |B|=" + Float.toString(B_local);
                    mTextSensorMagnetometer.setText(XYZ_field);

                    break;
                default:
                    // do
                    // nothing
            }
            // To display GPS data in Main UI
            TextView mDisplayLocationData = ((MainActivity) getActivity()).getLocationView();

            //if (locationTrack.canGetLocation()) {

        Log.v("CCP", "-"+String.valueOf(locationTrack));
        Log.v("CCP", "-"+String.valueOf(getContext()));

                longitude = locationTrack.getLocation().getLongitude();
                latitude = locationTrack.getLocation().getLatitude();

                // Need to check the symmetry...
//                longitude = (1.0-(rand.nextDouble()-0.5)/10000.0)*locationTrack.getLocation().getLongitude();
//               Log.v("CCCP", "d(latitude) "+String.valueOf((1.0-(rand.nextDouble()-0.5)/10000.0)));
                // WHY it SHOULD by TIMES 2.0 !!? )))
//                latitude =  (1.0-2.0*(rand.nextDouble()-0.5)/10000.0)*locationTrack.getLocation().getLatitude();
//                Log.v("CCCP", "d(longitude) "+String.valueOf((1.0-2.0*(rand.nextDouble()-0.5)/10000.0)));

                if (Math.abs(longitude - current_longitude) >= min_distance
                        || Math.abs(latitude - current_latitude) >= min_distance) {

                    Log.v("CCCP", " ");
                    Log.v("CCCP", "||->"+String.valueOf(R_marker*pixels_to_meters));
                    Log.v("CCCP", "d(latitude) "+String.valueOf(degrees_to_pixels*(latitude - current_latitude)));
                    Log.v("CCCP", "d(longitude) "+String.valueOf(degrees_to_pixels*(longitude - current_longitude)));

                    current_latitude = latitude;
                    current_longitude = longitude;

                    int x = x_center + (int)((center_longitude - current_longitude)*degrees_to_pixels);
                    int y = y_center + (int)((center_latitude - current_latitude)*degrees_to_pixels);

                    // assuming B_min=0; B_max=90; and color range 0-359 degrees
                    // the multiplier should be *359/150
                    float B_rated=B_local*359.0f/90.0f;
                    if(B_rated>359){B_rated=359;}

                    if ((x > R_marker) && (y > R_marker) && (x < width - R_marker) && (y < width - R_marker)) {
                        map_bitmap = DrawCircle.Circle(map_bitmap,
                                x, y, R_marker, B_rated);
                    }

                }else{
                    if(B_local>=B_max){
                        int x = x_center + (int)((center_longitude - current_longitude)*degrees_to_pixels);
                        int y = y_center + (int)((center_latitude - current_latitude)*degrees_to_pixels);

                        // assuming B_min=0; B_max=90; and color range 0-359 degrees
                        // the multiplier should be *359/150
                        float B_rated=B_local*359.0f/90.0f;
                        if(B_rated>359){B_rated=359;}

                        if ((x > R_marker) && (y > R_marker) && (x < width - R_marker) && (y < width - R_marker)) {
                            map_bitmap = DrawCircle.Circle(map_bitmap,
                                    x, y, R_marker, B_rated);
                        }
                        B_max=B_local;
                    }
                }

                double[] location = {longitude,latitude};
                String location_field = Arrays.toString(location);
                mDisplayLocationData.setText("GPS data " + location_field);
                //  Log.v(TAG, "new "+String.valueOf(longitude));
            //}
            //else {
            //    locationTrack.showSettingsAlert();
            //}

            publishProgress(map_bitmap);

        }


        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

    }

//    @Override
//    protected void onDestroyView() {
//        super.onDestroyView();
//        //locationTrack.stopListener();
//        Log.v(TAG, "in onDestroy");
//    }


}