package com.itsd83.compassapp;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager compass_sensor_manager;
    private Sensor accelerometer;
    private Sensor magnetometer;

    private TextView tv_degrees;
    private ImageView iv_compass;

    private final float[] accelerometer_reading = new float[3];
    private final float[] magnetometer_reading = new float[3];

    private final float[] rotation_matrix = new float[9];
    private final float[] orientation_angles = new float[3];

    private float current_degree = 0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_degrees = findViewById(R.id.degrees);
        iv_compass = findViewById(R.id.compass_image);

        compass_sensor_manager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = compass_sensor_manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = compass_sensor_manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (accelerometer != null) {
            compass_sensor_manager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
        if (magnetometer != null) {
            compass_sensor_manager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        compass_sensor_manager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometer_reading, 0, accelerometer_reading.length);
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometer_reading, 0, magnetometer_reading.length);
        }

        update_orientation_angles();
    }

    public void update_orientation_angles() {
        boolean success = SensorManager.getRotationMatrix(rotation_matrix, null, accelerometer_reading, magnetometer_reading);

        if (success) {
            SensorManager.getOrientation(rotation_matrix, orientation_angles);

            float azimuth = (float) Math.toDegrees(orientation_angles[0]);

            if (azimuth < 0) {
                azimuth += 360;
            }

            int display_angle;
            String label;

            float tolerance = 1.5f;

            if (Math.abs(azimuth - 0) < tolerance || Math.abs(azimuth - 360) < tolerance) {
                display_angle = 0;
                label = "N";
            } else if (Math.abs(azimuth - 90) < tolerance) {
                display_angle = 0;
                label = "E";
            } else if (Math.abs(azimuth - 180) < tolerance) {
                display_angle = 0;
                label = "S";
            } else if (Math.abs(azimuth - 270) < tolerance) {
                display_angle = 0;
                label = "W";
            }

            else if (azimuth > 0 && azimuth < 90) {
                display_angle = (int) Math.floor(azimuth);
                label = "NE";
            } else if (azimuth > 90 && azimuth < 180) {
                display_angle = (int) Math.floor(180 - azimuth);
                label = "SE";
            } else if (azimuth > 180 && azimuth < 270) {
                display_angle = (int) Math.floor(azimuth - 180);
                label = "SW";
            } else if (azimuth > 270 && azimuth < 360) {
                display_angle = (int) Math.floor(360 - azimuth);
                label = "NW";
            } else {
                display_angle = 0;
                label = "N";
            }

            tv_degrees.setText(display_angle + "° " + label);

            RotateAnimation rotate = new RotateAnimation(
                    current_degree,
                    -azimuth,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f
            );
            rotate.setDuration(200);
            rotate.setInterpolator(new AccelerateInterpolator());
            rotate.setFillAfter(true);
            iv_compass.startAnimation(rotate);
            current_degree = -azimuth;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}