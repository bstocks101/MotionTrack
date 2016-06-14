package bradstocks.test;

/**
 * Created by bradstocks on 2016/06/13.
 */

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

import java.util.List;

public class Accelerometer extends AppCompatActivity implements SensorEventListener {
    TextView tvMag = null;
    private SensorManager mSensorManager;
    private Sensor mMag;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvMag = (TextView) findViewById(R.id.textView3);
        tvMag.setText("Hello world");
        tvMag.setVisibility(View.VISIBLE);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mMag = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);


    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float press = event.values[0];
       // tvMag.setText("Mag:\n"+press);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume(){
        super.onResume();
        mSensorManager.registerListener(this, mMag, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause(){
        super.onPause();
        mSensorManager.unregisterListener(this);
    }
}
