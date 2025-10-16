package com.itsd83.compassapp;

import android.annotation.SuppressLint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager compassSensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;

    private TextView tv_degrees;
    private ImageView iv_compass;

    private final float[] accelerometerReading = new float[3];
    private final float[] magnetometerReading = new float[3];

    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];

    private float current_degree = 0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_degrees = findViewById(R.id.degrees);
        iv_compass = findViewById(R.id.compass_image);

        compassSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = compassSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = compassSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (accelerometer != null) {
            compassSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
        if (magnetometer != null) {
            compassSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        compassSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.length);
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.length);
        }

        updateOrientationAngles();
    }

    @SuppressLint("SetTextI18n")
    public void updateOrientationAngles() {
        boolean success = SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading);

        if (success) {
            SensorManager.getOrientation(rotationMatrix, orientationAngles);

            float azimuth = (float) Math.toDegrees(orientationAngles[0]);

            if (azimuth < 0) {
                azimuth += 360;
            }

            int displayAngle;
            String label;

            if (Math.abs(azimuth - 0) < 0.5 || Math.abs(azimuth - 360) < 0.5) {
                displayAngle = 0;
                label = "N";
            } else if (Math.abs(azimuth - 90) < 0.5) {
                displayAngle = 90;
                label = "E";
            } else if (Math.abs(azimuth - 180) < 0.5) {
                displayAngle = 0;
                label = "S";
            } else if (Math.abs(azimuth - 270) < 0.5) {
                displayAngle = 90;
                label = "W";
            }
            else if (azimuth > 0 && azimuth < 90) {
                displayAngle = Math.round(azimuth);
                label = "NE";
            } else if (azimuth > 90 && azimuth < 180) {
                displayAngle = Math.round(180 - azimuth);
                label = "SE";
            } else if (azimuth > 180 && azimuth < 270) {
                displayAngle = Math.round(azimuth - 180);
                label = "SW";
            } else if (azimuth > 270 && azimuth < 360) {
                displayAngle = Math.round(360 - azimuth);
                label = "NW";
            } else {
                displayAngle = 0;
                label = "N";
            }

            tv_degrees.setText(displayAngle + "° " + label);

            RotateAnimation rotate = new RotateAnimation(
                    current_degree,
                    -azimuth,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f
            );
            rotate.setDuration(200);
            rotate.setFillAfter(true);
            iv_compass.startAnimation(rotate);
            current_degree = -azimuth;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}