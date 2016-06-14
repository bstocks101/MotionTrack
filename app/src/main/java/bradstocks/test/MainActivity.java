package bradstocks.test;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    TextView tvPress = null;
    TextView tvMag = null;
    TextView tvAcc = null;
    TextView tvGyro = null;
    TextView tvGPS = null;
    private SensorManager mSensorManager;
    private SensorEventListener mSensorListener;
    LocationManager locationManager;
    private LocationListener locListener;

    float temp;
    float[] temp3;
    double alt, lat, lon;
    float bear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        temp3 = new float[3];
        locationManager  = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        tvPress = (TextView) findViewById(R.id.textView);

        // tv1.setVisibility(View.GONE);
        tvPress.setVisibility(View.VISIBLE);
        tvMag = (TextView) findViewById(R.id.textView2);
        tvMag.setVisibility(View.VISIBLE);
        tvAcc = (TextView) findViewById(R.id.textView3);
        tvAcc.setVisibility(View.VISIBLE);
        tvGyro = (TextView) findViewById(R.id.textView4);
        tvGyro.setVisibility(View.VISIBLE);
        tvGPS = (TextView) findViewById(R.id.textView5);
        tvGPS.setVisibility(View.VISIBLE);


        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensorListener = new SensorEventListener() {
            @Override
            public final void onAccuracyChanged(Sensor sensor, int accuracy) {
                //nothing here yet
            }

            @Override
            public final void onSensorChanged(SensorEvent event) {
                Sensor sensor = event.sensor;
                if (sensor.getType() == Sensor.TYPE_PRESSURE) {
                    temp = event.values[0];
                    tvPress.setText("Pressure:\n" + temp);
                } else if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                    temp = event.values[0];
                    tvMag.setText("Mag:\n" + temp);
                } else if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    temp3[0] = event.values[0];
                    temp3[1] = event.values[1];
                    temp3[2] = event.values[2];
                    tvAcc.setText("ACC:\nx: " + temp3[0] + "\ny: " + temp3[1] + "\nz: " + temp3[2]);
                } else if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                    temp3[0] = event.values[0];
                    temp3[1] = event.values[1];
                    temp3[2] = event.values[2];
                    tvGyro.setText("Gyro:\nx: " + temp3[0] + "\ny: " + temp3[1] + "\nz: " + temp3[2]);
                } else {
                    tvMag.setText("Sorry");
                }

            }
        };

        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                lat = location.getLatitude();
                lon = location.getLongitude();
                alt = location.getAltitude();
                bear = location.getBearing();
                tvGPS.setText("GPS:\nlat: " + lat + "\nlong: " + lon + "\nalt: " + alt + "\nbear: " + bear);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };


        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE), SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        //Sensor temp;
        //List<Sensor> mList = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        //mPress = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);

        /*
        for (int i = 0; i < mList.size(); i++) {
            tv1.setVisibility(View.VISIBLE);
            if(mList.get(i).getType()==Sensor.TYPE_GYROSCOPE) {
                tv1.append(mList.get(i));
            }
            tv1.append("\n" + mList.get(i).getName() + "\n" + mList.get(i).getVendor()
                    + "\n" + mList.get(i).getVersion());
        }*/
        //tv1.setMovementMethod(new ScrollingMovementMethod());

    }



/*    @Override
    protected void onResume(){
        super.onResume();
        mSensorManager.registerListener(this, mPress, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause(){
        super.onPause();
        mSensorManager.unregisterListener(this);
    }*/

}
