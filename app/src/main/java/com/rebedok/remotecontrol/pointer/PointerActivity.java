package com.rebedok.remotecontrol.pointer;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import androidx.core.view.GestureDetectorCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

import com.rebedok.remotecontrol.preference.MyPreferenceActivity;
import com.rebedok.remotecontrol.R;
import com.rebedok.remotecontrol.Server;

import java.util.concurrent.Semaphore;

import static android.view.KeyEvent.KEYCODE_VOLUME_DOWN;
import static android.view.KeyEvent.KEYCODE_VOLUME_UP;

public class PointerActivity extends AppCompatActivity implements SensorEventListener,
        GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener {
    private SensorManager sensorManager;
    private Server server;
    private Semaphore semaphore = null;
    private boolean keyPress = false;
    private boolean UpKeyPress = false;
    private boolean DownKeyPress = false;
    private Sensor gyroscope;
    private PointerProperties properties;
    private GestureDetectorCompat detector;
    private boolean isClose = true;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.detector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pointer);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        semaphore = new Semaphore(1);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        detector = new GestureDetectorCompat(this, this);
        detector.setOnDoubleTapListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        properties =
                new PointerProperties(this);
        String address = getIntent().getExtras().getString("ipAddress");
        connect(address);
        sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        isClose = true;
        sensorManager.unregisterListener(this, gyroscope);
        try {
            server.closeConnection();
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
                Intent intent = new Intent(PointerActivity.this, MyPreferenceActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KEYCODE_VOLUME_DOWN) {
            keyPress = true;
            sendKeyCode(properties.PressKey(), 1);
            return true;
        }
        if (keyCode == KEYCODE_VOLUME_UP) {
            keyPress = true;
            sendKeyCode(properties.UpPressKey(), 1);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KEYCODE_VOLUME_DOWN) {
            sendKeyCode(properties.ReleaseKey(), 0);
            DownKeyPress = false;
            return true;
        }
        if (keyCode == KEYCODE_VOLUME_UP) {
            sendKeyCode(properties.UpReleaseKey(), 0);
            UpKeyPress = false;
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    public void sendKeyCode(final String code, final int keyEvent) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (keyPress && keyEvent == 0) ;
                    semaphore.acquire();
                    server.Send(code);
                    if (keyEvent == 1) {
                        keyPress = false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    setResult(RESULT_OK);
                    finish();
                }
                semaphore.release();
            }
        }).start();
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
            isClose = false;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!isClose) {
            int type = event.sensor.getType();
            if (type == Sensor.TYPE_GYROSCOPE) {
                float axisX = properties.getMultiplierX() * event.values[0];
                float axisY = properties.getMultiplierY() * event.values[2];
                if (properties.isChangeAxis()) {
                    sendString(axisX + "#" + axisY + "#");
                } else {
                    sendString((-axisY) + "#" + axisX + "#");
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        sendKeyCode("LKMPress@", 1);
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        if (e.getAction() == MotionEvent.ACTION_UP) {
            sendKeyCode("LKMRelease@", 0);
        }
        return false;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        sendString("LKMPress@LKMRelease@");
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        sendString(String.valueOf(Math.signum(distanceY)));
        return false;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }
}
