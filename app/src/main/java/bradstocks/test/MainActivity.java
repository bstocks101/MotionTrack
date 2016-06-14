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
import android.location.LocationProvider;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    TextView tvPress = null;
    TextView tvMag = null;
    TextView tvAcc = null;
    TextView tvGyro = null;
    TextView tvGPS = null;
    TextView tvTemp = null;
    private SensorManager mSensorManager;
    private SensorEventListener mSensorListener;
    LocationManager locationManager;
    TimerTask writeTask;
    final Handler writeHandler = new Handler();
    Timer t = new Timer();
    Payload payload;
    Button button;
    Button button2;
    Toast toast;

    float temp;
    float[] temp3;
    double alt, lat, lon;
    float bear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Context context = getApplicationContext();
        CharSequence text = "Write is complete!";
        int duration = Toast.LENGTH_SHORT;
        toast = Toast.makeText(context, text, duration);
        button = (Button) findViewById(R.id.button);
        button = (Button) findViewById(R.id.button2);
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
        tvGPS.setText("GPS: awaiting connection");
        //tvTemp = (TextView) findViewById(R.id.textView6);
        //tvTemp.setVisibility(View.VISIBLE);

        payload = new Payload("testData.csv");

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
                    payload.setPressure(temp);
                    tvPress.setText("Pressure:\n" + temp);
                } else if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                    temp = event.values[0];
                    payload.setxMag(temp);
                    payload.setyMag(event.values[1]);
                    payload.setzMag(event.values[2]);
                    tvMag.setText("Mag:\nx: " + temp +"\ny: " + event.values[1] + "\nz: " + event.values[2]);
                } else if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    temp3[0] = event.values[0];
                    temp3[1] = event.values[1];
                    temp3[2] = event.values[2];
                    payload.setxAcc(temp3[0]);
                    payload.setyAcc(temp3[1]);
                    payload.setzAcc(temp3[2]);
                    tvAcc.setText("ACC:\nx: " + temp3[0] + "\ny: " + temp3[1] + "\nz: " + temp3[2]);
                } else if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                    temp3[0] = event.values[0];
                    temp3[1] = event.values[1];
                    temp3[2] = event.values[2];
                    payload.setxGyro(temp3[0]);
                    payload.setyGyro(temp3[1]);
                    payload.setzGyro(temp3[2]);
                    tvGyro.setText("Gyro:\nx: " + temp3[0] + "\ny: " + temp3[1] + "\nz: " + temp3[2]);
                }
                /*else if (sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE) {
                    temp = event.values[0];
                    payload.setTemperature(temp);
                    tvTemp.setText("Temp:\n" + temp);
                } */else {
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
                payload.setLat(lat);
                payload.setLon(lon);
                payload.setAlt(alt);
                payload.setBearGPS(bear);
                tvGPS.setText("GPS:\nlat: " + lat + "\nlong: " + lon + "\nalt: " + alt + "\nbear: " + bear);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

        writeTask = new TimerTask(){
            public void run(){
                writeHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            payload.writeToFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        };


        button.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                try {
                    payload.endFile();
                    toast.show();
                    t.cancel();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        button.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                    t.scheduleAtFixedRate(writeTask, 10, 10);
            }
        });

        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE), 10000);
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), 10000);
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), 10000);
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 10000);
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
