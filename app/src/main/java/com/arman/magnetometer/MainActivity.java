package com.arman.magnetometer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

import java.util.Arrays;

public class MainActivity
        extends AppCompatActivity implements SensorEventListener {
    private SensorManager mSensorManager;
    // Individual light and proximity sensors.
    private Sensor mSensorProximity;
    private Sensor mMagnetometer;

    // TextViews to display current sensor values
    private TextView mTextSensorLight;
    private TextView mTextSensorMagnetometer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSensorManager =
                (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        mTextSensorLight = (TextView) findViewById(R.id.label_light);
        mTextSensorMagnetometer = (TextView) findViewById(R.id.label_magnetometer);

        mSensorProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        String sensor_error = getResources().getString(R.string.error_no_sensor);

        if (mMagnetometer == null) {
            mTextSensorLight.setText(sensor_error);
        }

        if (mSensorProximity == null) {
            mTextSensorMagnetometer.setText(sensor_error);
        }


    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        int sensorType = sensorEvent.sensor.getType();
        //float currentValue = sensorEvent.values[0];
        float [] MValues = sensorEvent.values;

        String XYZ_field=Arrays.toString(MValues);


        switch (sensorType) {
            // Event came from the light sensor.
            case Sensor.TYPE_LIGHT:
                // Handle light sensor
                mTextSensorLight.setText(getResources().getString(
                        R.string.label_light, MValues[0]));
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                mTextSensorMagnetometer.setText(XYZ_field);
                break;
            default:
                // do
                // nothing
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mSensorProximity != null) {
            mSensorManager.registerListener(this, mSensorProximity,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (mMagnetometer != null) {
            mSensorManager.registerListener(this, mMagnetometer,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mSensorManager.unregisterListener(this);
    }

}