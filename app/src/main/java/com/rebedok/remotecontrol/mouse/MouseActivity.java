package com.rebedok.remotecontrol.mouse;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.rebedok.remotecontrol.preference.MyPreferenceActivity;
import com.rebedok.remotecontrol.R;
import com.rebedok.remotecontrol.Server;

import java.util.concurrent.Semaphore;

public class MouseActivity extends AppCompatActivity implements SensorEventListener{
    private SensorManager sensorManager;
    private Server server;
    private Semaphore semaphore = null;
    private Sensor gyroscope;
    private int speedX = 0;
    private int speedY = 0;
    private int movementX = 0;
    private int movementY = 0;
    private int valueX = 0;
    private int valueY = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pointer);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        semaphore = new Semaphore(1);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
    }

    @Override
    protected void onStart() {
        super.onStart();
        String address = getIntent().getExtras().getString("ipAddress");
        connect(address);
        sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this, gyroscope);
        try {
            semaphore.acquire();
            server.closeConnection();
            semaphore.release();
        } catch (Exception e) {
            e.printStackTrace();
            setResult(RESULT_OK);
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.main_settings:
                Intent intent = new Intent(MouseActivity.this, MyPreferenceActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    public void sendString(final String code) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    semaphore.acquire();
                    server.Send(code);
                } catch (Exception e) {
                    e.printStackTrace();
                    setResult(RESULT_OK);
                    finish();
                }
                semaphore.release();
            }
        }).start();
    }

    public void connect(final String address) {
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
            setResult(RESULT_OK);
            finish();
        }
        if (!address.isEmpty()) {
            server = new Server();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        server.openConnection(address);
                    } catch (Exception e) {
                        e.printStackTrace();
                        server = null;
                        setResult(RESULT_OK);
                        finish();
                    }
                }
            }).start();
            semaphore.release();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int type = event.sensor.getType();
        if (type == Sensor.TYPE_LINEAR_ACCELERATION) {
            int axisX = (int)(event.values[0] * -10);
            int axisY = (int)(event.values[1] * 10);
            if(axisX != 0 && movementX > 3) {
                valueX = axisX;
            }
            if(axisY != 0 && movementY > 3) {
                valueY = axisY;
            }
//            axisX = (int) Math.signum(axisX) * 10;
//            axisY = (int) Math.signum(axisY) * 10;
            movementX = axisX == 0 ?  movementX + 1 : 0;
            movementY = axisY == 0 ?  movementY + 1 : 0;
            speedX = movementX < 3 ? valueX * 10 : 0;
            speedY = movementY < 3 ? valueY * 10 : 0;
//            speedX = movementX < 2 ? speedX + axisX : 0;
//            speedY = movementY < 2 ? speedY + axisY : 0;
//            speedX += axisX * (int)(event.timestamp/1000000000);


//            speedX += axisX;
//            speedY += axisY;
            sendString(speedY + "#" + speedX + "#");
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}
