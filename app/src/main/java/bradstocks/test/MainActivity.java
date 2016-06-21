package bradstocks.test;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Handler;
import android.os.IBinder;
import android.renderscript.Float3;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.mbientlab.metawear.AsyncOperation;
import com.mbientlab.metawear.Message;
import com.mbientlab.metawear.MetaWearBleService;
import com.mbientlab.metawear.MetaWearBoard;
import android.util.Log;
import static com.mbientlab.metawear.MetaWearBoard.ConnectionStateHandler;
import com.mbientlab.metawear.UnsupportedModuleException;
import com.mbientlab.metawear.module.Bma255Accelerometer;
import com.mbientlab.metawear.module.Bmi160Accelerometer;
import com.mbientlab.metawear.module.Led;
import com.mbientlab.metawear.module.Accelerometer;
import android.widget.Switch;
import android.widget.CompoundButton;
import com.mbientlab.metawear.RouteManager;
import com.mbientlab.metawear.data.CartesianFloat;
import com.mbientlab.metawear.module.Bmi160Gyro;
import com.mbientlab.metawear.module.Bmi160Gyro.*;
import com.mbientlab.metawear.module.Gpio;
import com.mbientlab.metawear.module.Bmm150Magnetometer;
import com.mbientlab.metawear.module.Bmm150Magnetometer.PowerPreset;
import com.mbientlab.metawear.module.MultiChannelTemperature;
import com.mbientlab.metawear.module.MultiChannelTemperature.*;
import com.mbientlab.metawear.module.Bmi160Accelerometer.AccRange;
import com.mbientlab.metawear.module.Bmi160Accelerometer.OutputDataRate;


import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements ServiceConnection{

    ToneGenerator tone;
    private MetaWearBleService.LocalBinder serviceBinder;
    private final String MW_MAC_ADDRESS= "D0:73:6E:F3:AA:12";
    //private final String MW_MAC_ADDRESS2= "C2:5D:6E:47:85:C2";
    private final String MW_MAC_ADDRESS2= "FE:8B:EF:C3:49:E5";

    private MetaWearBoard mwBoard;
    private MetaWearBoard mwBoard2;
    private Led ledModule;
    private Led ledModule2;
    private static final String TAG = "MetaWear";
    private Button connect;
    private Button led1_tog;
    private Button led2_tog;
    private Switch accel_switch;
    private Switch accel_switch2;
    private Bmi160Accelerometer accelModule;
    private Bmi160Accelerometer accelModule2;
    private Bmi160Gyro gyroModule;
    private Bmi160Gyro gyroModule2;
    private Gpio gpioModule;
    Bmm150Magnetometer magModule;
    Bmm150Magnetometer magModule2;
    MultiChannelTemperature mcTempModule;
    MultiChannelTemperature mcTempModule2;
    private static final float ACC_RANGE = 8.f, ACC_FREQ = 50.f;
    private static final String STREAM_KEY = "accel_stream";
    private static final String GYRO_STREAM_KEY = "gyro_stream";
    final byte GPIO_PIN = 2;
    int counter;
    List<Source> tempSources;
    List<Source> tempSources2;
    String GPIO;
    int disconDetect;
    int count;


    private final ConnectionStateHandler stateHandler= new ConnectionStateHandler() {
        @Override
        public void connected() {
            Log.i(TAG, "Connected");
            connected = true;
            try {
                ledModule = mwBoard.getModule(Led.class);
                accelModule = mwBoard.getModule(Bmi160Accelerometer.class);
                gyroModule = mwBoard.getModule(Bmi160Gyro.class);
                gpioModule = mwBoard.getModule(Gpio.class);
                magModule= mwBoard.getModule(Bmm150Magnetometer.class);
                mcTempModule= mwBoard.getModule(MultiChannelTemperature.class);
                tempSources= mcTempModule.getSources();
                if(sampling) setListeners();

            } catch (UnsupportedModuleException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void disconnected() {
           // killListeners();
            mwBoard.connect();
            Log.i(TAG, "Connected Lost");
        }

        @Override
        public void failure(int status, Throwable error) {
           // killListeners();
            mwBoard.connect();
            Log.e(TAG, "Error connecting", error);
        }
    };

    private final ConnectionStateHandler stateHandler2= new ConnectionStateHandler() {
        @Override
        public void connected() {
            Log.i(TAG, "Connected 2");
            connected2 = true;
            try {
                ledModule2 = mwBoard2.getModule(Led.class);
                accelModule2 = mwBoard2.getModule(Bmi160Accelerometer.class);
                gyroModule2 = mwBoard2.getModule(Bmi160Gyro.class);
                //gpioModule = mwBoard.getModule(Gpio.class);
                magModule2= mwBoard2.getModule(Bmm150Magnetometer.class);
                mcTempModule2= mwBoard2.getModule(MultiChannelTemperature.class);
                tempSources2= mcTempModule2.getSources();
                if(sampling2) setListeners2();

            } catch (UnsupportedModuleException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void disconnected() {
            //killListeners2();
            mwBoard2.connect();
            Log.i(TAG, "Connected Lost 2");
        }

        @Override
        public void failure(int status, Throwable error) {
            //killListeners2();
            mwBoard2.connect();
            Log.e(TAG, "Error connecting 2", error);
        }
    };

    public void retrieveBoard() {
        final BluetoothManager btManager=
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        final BluetoothDevice remoteDevice=
                btManager.getAdapter().getRemoteDevice(MW_MAC_ADDRESS);

        // Create a MetaWear board object for the Bluetooth Device
        mwBoard= serviceBinder.getMetaWearBoard(remoteDevice);
        mwBoard.setConnectionStateHandler(stateHandler);


    }

    public void retrieveBoard2() {
        final BluetoothManager btManager=
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        final BluetoothDevice remoteDevice=
                btManager.getAdapter().getRemoteDevice(MW_MAC_ADDRESS2);

        // Create a MetaWear board object for the Bluetooth Device
        mwBoard2= serviceBinder.getMetaWearBoard(remoteDevice);
        mwBoard2.setConnectionStateHandler(stateHandler2);

    }


    public void pause() {
        this.t.cancel();
        this.t = new Timer();
    }

    public void resume() {
        //this.t = new Timer();
        this.t.schedule(new TimerTask(){
            public void run(){
                writeHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(running) {
                            try {
                                payload.writeToFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        }, 10, 10);
    }



    /*TextView tvPress = null;
    TextView tvMag = null;
    TextView tvAcc = null;
    TextView tvGyro = null;
    TextView tvGPS = null;
    TextView tvTemp = null;
    */
    TextView onBoardStatus = null;
    TextView sensorStaus = null;
    TextView sampleAcc = null;
    TextView sampleAcc2 = null;
    TextView feedback = null;


    private SensorManager mSensorManager;
    private SensorEventListener mSensorListener;
    LocationManager locationManager;
    TimerTask writeTask;
    final Handler writeHandler = new Handler();
    Timer t;
    Payload payload;
    Button start;
    Button stop;
    Toast toast;

    float temp;
    float[] temp3;
    double alt, lat, lon;
    String accel;
    String gyro;
    String mag;
    String temperature;
    String accel2;
    String gyro2;
    String mag2;
    String temperature2;
    float bear;
    boolean running;
    boolean connected;
    boolean connected2;
    boolean sampling;
    boolean sampling2;
    boolean LED1;
    boolean LED2;
    float tempArr[];
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        count = 0;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        t= new Timer();

        payload = new Payload("testData" + count + ".csv");

        tone = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
        tempArr = new float[3];
        running = false;
        connected = false;
        sampling = false;
        counter = 0;
        LED1 = false;
        LED2 = false;

        Context context = getApplicationContext();
        context.bindService(new Intent(this, MetaWearBleService.class),
                this, Context.BIND_AUTO_CREATE);

        CharSequence text = "Write is complete!";
        int duration = Toast.LENGTH_SHORT;
        toast = Toast.makeText(context, text, duration);
        start = (Button) findViewById(R.id.button);
        stop = (Button) findViewById(R.id.button2);
        connect = (Button) findViewById(R.id.connect);
        led2_tog = (Button) findViewById(R.id.led_off);
        led1_tog = (Button) findViewById(R.id.led_on);
        accel_switch = (Switch) findViewById(R.id.accel_switch);
        accel_switch2 = (Switch) findViewById(R.id.accel_switch2);

        temp3 = new float[3];
        locationManager  = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        onBoardStatus = (TextView) findViewById(R.id.textView);
        onBoardStatus.setVisibility(View.VISIBLE);
        onBoardStatus.setText("Waiting for GPS");
        sensorStaus = (TextView) findViewById(R.id.textView2);
        sensorStaus.setVisibility(View.VISIBLE);
        sensorStaus.setText("Press connect");
        sampleAcc = (TextView) findViewById(R.id.tv_accel);
        sampleAcc.setVisibility(View.VISIBLE);
        sampleAcc.setText("Waiting for stream1");
        sampleAcc2 = (TextView) findViewById(R.id.tv_accel2);
        sampleAcc2.setVisibility(View.VISIBLE);
        sampleAcc2.setText("Waiting for stream2");
        feedback = (TextView) findViewById(R.id.feedback);
        feedback.setVisibility(View.VISIBLE);


        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensorListener = new SensorEventListener() {
            @Override
            public final void onAccuracyChanged(Sensor sensor, int accuracy) {
                //nothing here yet
            }

            @Override
            public final void onSensorChanged(SensorEvent event) {
                Sensor sensor = event.sensor;
                if(connected && !connected2) sensorStaus.setText("Device1 connected");
                if(connected2 && !connected) sensorStaus.setText("Device2 connected");
                if(connected2 && connected) sensorStaus.setText("Both devices connected");
                if(sampling) sampleAcc.setText("Feed 1:\n" + accel+"\n"+gyro+"\n"+mag+"\n" +temperature);
                if(!sampling) sampleAcc.setText("Feed 1 disabled");
                if(sampling2) sampleAcc2.setText("Feed 2:\n" + accel2+"\n"+gyro2+"\n"+mag2+"\n" +temperature2);
                if(!sampling2) sampleAcc2.setText("Feed 2 disabled");
                if (sensor.getType() == Sensor.TYPE_PRESSURE) {
                    temp = event.values[0];
                    payload.setPressure(temp);
                    //tvPress.setText("Pressure:\n" + temp);
                } else if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                    temp = event.values[0];
                    payload.setxMag(temp);
                    payload.setyMag(event.values[1]);
                    payload.setzMag(event.values[2]);
                    //tvMag.setText("Mag:\nx: " + temp +"\ny: " + event.values[1] + "\nz: " + event.values[2]);
                } else if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    temp3[0] = event.values[0];
                    temp3[1] = event.values[1];
                    temp3[2] = event.values[2];
                    payload.setxAcc(temp3[0]);
                    payload.setyAcc(temp3[1]);
                    payload.setzAcc(temp3[2]);
                    //tvAcc.setText("ACC:\nx: " + temp3[0] + "\ny: " + temp3[1] + "\nz: " + temp3[2]);
                } else if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                    temp3[0] = event.values[0];
                    temp3[1] = event.values[1];
                    temp3[2] = event.values[2];
                    payload.setxGyro(temp3[0]);
                    payload.setyGyro(temp3[1]);
                    payload.setzGyro(temp3[2]);
                    //tvGyro.setText("Gyro:\nx: " + temp3[0] + "\ny: " + temp3[1] + "\nz: " + temp3[2]);
                }
                /*else if (sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE) {
                    temp = event.values[0];
                    payload.setTemperature(temp);
                    tvTemp.setText("Temp:\n" + temp);
                } */else {
                    //tvMag.setText("Sorry");
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
                payload.setGPSSpeed(location.getSpeed());
                payload.setBearGPS(bear);
                onBoardStatus.setText("All sensors ready");
                //tvGPS.setText("GPS:\nlat: " + lat + "\nlong: " + lon + "\nalt: " + alt + "\nbear: " + bear);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

       /* writeTask = new TimerTask(){
            public void run(){
                writeHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(running) {
                            try {
                                payload.writeToFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        };*/


        stop.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                if (running) {
                    try {
                        payload.endFile();
                        toast.show();
                        tone.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 20);
                        pause();
                        running = false;
                        count++;
                        payload = new Payload("testData" + count + ".csv");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        start.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){

                //if(sampling && sampling2) {
                    if (!running) {
                        //t = new Timer();
                        tone.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 20);
                        resume();
                        running = true;
                    }
               // }
               // else{
               //     feedback.setText("Open both streams before starting");
               // }
            }
        });

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Clicked connect");
                sensorStaus.setText("Connecting");
                mwBoard.connect();
                mwBoard2.connect();
            }
        });
        led1_tog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!LED1){
                    Log.i(TAG, "Turn on LED1");
                    ledModule.configureColorChannel(Led.ColorChannel.BLUE)
                            .setRiseTime((short) 0).setPulseDuration((short) 1000)
                            .setRepeatCount((byte) -1).setHighTime((short) 500)
                            .setHighIntensity((byte) 16).setLowIntensity((byte) 16)
                            .commit();
                    ledModule.play(true);
                    LED1 = true;
                }
                else{
                    Log.i(TAG, "Turn off LED1");
                    ledModule.stop(true);
                    LED1 = false;
                }

            }
        });

        led2_tog =(Button)findViewById(R.id.led_off);
        led2_tog.setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View v) {
                                           if(!LED2){
                                               Log.i(TAG, "Turn on LED2");
                                               ledModule2.configureColorChannel(Led.ColorChannel.RED)
                                                       .setRiseTime((short) 0).setPulseDuration((short) 1000)
                                                       .setRepeatCount((byte) -1).setHighTime((short) 500)
                                                       .setHighIntensity((byte) 16).setLowIntensity((byte) 16)
                                                       .commit();
                                               ledModule2.play(true);
                                               LED2 = true;
                                           }
                                           else{
                                               Log.i(TAG, "Turn off LED2");
                                               ledModule2.stop(true);
                                               LED2 = false;
                                           }
                                       }
                                   });
            /* @Override
            public void onClick(View v) {
                Log.i(TAG, "Turn off LED");
                ledModule.stop(true);
            }
        });*/

        assert accel_switch != null;
        accel_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.i("Switch State=", "" + isChecked);
                if(!mwBoard.isConnected()) feedback.setText("Connection lost");
                sampling = isChecked;
                if (isChecked) {
                    setListeners();
                } else {
                    killListeners();
                }
            }
        });

        assert accel_switch2 != null;
        accel_switch2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.i("Switch State 2=", "" + isChecked);
                sampling2 = isChecked;
                if (isChecked) {
                    setListeners2();

                } else {
                    killListeners2();
                }
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
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener); //or NETWORK_PROVIDER
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

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Unbind the service when the activity is destroyed
        getApplicationContext().unbindService(this);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        // Typecast the binder to the service's LocalBinder class
        serviceBinder = (MetaWearBleService.LocalBinder) service;
        retrieveBoard();
        retrieveBoard2();

    }

    public void setListeners(){
        accelModule.configureAxisSampling()
                .setFullScaleRange(AccRange.AR_16G)
                .setOutputDataRate(OutputDataRate.ODR_25_HZ)
                .commit();
        gyroModule.configure()
                .setOutputDataRate(Bmi160Gyro.OutputDataRate.ODR_25_HZ)
                .setFullScaleRange(FullScaleRange.FSR_500)
                .commit();
        magModule.setPowerPrsest(PowerPreset.LOW_POWER);
        magModule.enableBFieldSampling();
        mcTempModule.routeData()
                .fromSource(tempSources.get(MetaWearRChannel.NRF_DIE)).stream("temp_stream")
                .commit().onComplete(new AsyncOperation.CompletionHandler<RouteManager>() {

            @Override
            public void success(RouteManager result) {
                result.subscribe("temp_stream", new RouteManager.MessageHandler() {
                    @Override
                    public void process(Message msg) {
                       // Log.i("MainActivity", String.format("Ext thermistor: %.3fC",
                        //        msg.getData(Float.class)));
                        temperature = "" + msg.getData(Float.class);
                        payload.setIMU1Temp(temperature);
                    }
                });

                // Read temperature from the NRF soc chip
                mcTempModule.readTemperature(tempSources.get(MetaWearRChannel.NRF_DIE));
            }
        });

        magModule.routeData().fromBField().stream("mag_stream").commit()
                .onComplete(new AsyncOperation.CompletionHandler<RouteManager>() {
                    @Override
                    public void success(RouteManager result) {
                        result.subscribe("mag_stream", new RouteManager.MessageHandler() {
                            @Override
                            public void process(Message msg) {
                                final CartesianFloat bField = msg.getData(CartesianFloat.class);

                                //Log.i("MainActivity", bField.toString());
                                mag = bField.x() + ", " + bField.y() +", " + bField.z();
                                payload.setIMU1MAG(mag);
                            }
                        });
                        magModule.start();
                    }
                });
        AsyncOperation<RouteManager> routeManagerResultAccel = accelModule.routeData().fromAxes().stream(STREAM_KEY).commit();
        AsyncOperation<RouteManager> routeManagerResultGyro = gyroModule.routeData().fromAxes().stream(GYRO_STREAM_KEY).commit();
        routeManagerResultAccel.onComplete(new AsyncOperation.CompletionHandler<RouteManager>() {
            @Override
            public void success(RouteManager result) {
                result.subscribe(STREAM_KEY, new RouteManager.MessageHandler() {
                    @Override
                    public void process(Message message) {
                        CartesianFloat axes = message.getData(CartesianFloat.class);
                       // Log.i(TAG, axes.toString());
                        accel= axes.x() + ", " + axes.y() + ", " + axes.z();
                        payload.setIMU1Acc(accel);
                        disconDetect = 0;
                    }
                });
            }
            @Override
            public void failure(Throwable error) {
                Log.e(TAG, "Error committing route", error);
            }
        });

        routeManagerResultGyro.onComplete(new AsyncOperation.CompletionHandler<RouteManager>() {
            @Override
            public void success(RouteManager result) {
                result.subscribe(GYRO_STREAM_KEY, new RouteManager.MessageHandler() {
                    @Override
                    public void process(Message msg) {
                        final CartesianFloat spinData = msg.getData(CartesianFloat.class);
                        //Log.i(TAG, String.format("Gyroscope: %s", spinData.toString()));
                        gyro = spinData.x() + ", " + spinData.y() + ", " + spinData.z();
                        payload.setIMU1Gyro(gyro);
                    }
                });
            }
        });
        accelModule.enableAxisSampling();
        accelModule.start();
        gyroModule.start();
    }

    public void killListeners(){
        gyroModule.stop();
        accelModule.disableAxisSampling();
        accelModule.stop();
        magModule.stop();
    }

    public void setListeners2(){
        accelModule2.configureAxisSampling()
                .setFullScaleRange(AccRange.AR_16G)
                .setOutputDataRate(OutputDataRate.ODR_25_HZ)
                .commit();
        gyroModule2.configure()
                .setOutputDataRate(Bmi160Gyro.OutputDataRate.ODR_25_HZ)
                .setFullScaleRange(FullScaleRange.FSR_500)
                .commit();
        magModule2.setPowerPrsest(PowerPreset.LOW_POWER);
        magModule2.enableBFieldSampling();
        mcTempModule2.routeData()
                .fromSource(tempSources2.get(MetaWearRChannel.NRF_DIE)).stream("temp_stream2")
                .commit().onComplete(new AsyncOperation.CompletionHandler<RouteManager>() {

            @Override
            public void success(RouteManager result) {
                result.subscribe("temp_stream2", new RouteManager.MessageHandler() {
                    @Override
                    public void process(Message msg) {
                       // Log.i(TAG, String.format("Ext thermistor: %.3fC",
                        //        msg.getData(Float.class)));
                        temperature2 = msg.getData(Float.class).toString();
                        payload.setIMU2Temp(temperature2);
                    }
                });

                // Read temperature from the NRF soc chip
                mcTempModule2.readTemperature(tempSources2.get(MetaWearRChannel.NRF_DIE));
            }
        });

        magModule2.routeData().fromBField().stream("mag_stream2").commit()
                .onComplete(new AsyncOperation.CompletionHandler<RouteManager>() {
                    @Override
                    public void success(RouteManager result) {
                        result.subscribe("mag_stream2", new RouteManager.MessageHandler() {
                            @Override
                            public void process(Message msg) {
                                final CartesianFloat bField = msg.getData(CartesianFloat.class);

                                //Log.i(TAG, bField.toString());
                                mag2 = bField.x() + ", " + bField.y() +", " + bField.z();
                                payload.setIMU2MAG(mag2);
                            }
                        });
                        magModule2.start();
                    }
                });
        AsyncOperation<RouteManager> routeManagerResultAccel = accelModule2.routeData().fromAxes().stream("accel_stream2").commit();
        AsyncOperation<RouteManager> routeManagerResultGyro = gyroModule2.routeData().fromAxes().stream("gyro_stream2").commit();
        routeManagerResultAccel.onComplete(new AsyncOperation.CompletionHandler<RouteManager>() {
            @Override
            public void success(RouteManager result) {
                result.subscribe("accel_stream2", new RouteManager.MessageHandler() {
                    @Override
                    public void process(Message message) {
                        CartesianFloat axes = message.getData(CartesianFloat.class);
                        //Log.i(TAG, axes.toString());
                        accel2= axes.x() + ", " + axes.y() + ", " + axes.z();
                        payload.setIMU2Acc(accel2);
                    }
                });
            }
            @Override
            public void failure(Throwable error) {
                Log.e(TAG, "Error committing route", error);
            }
        });

        routeManagerResultGyro.onComplete(new AsyncOperation.CompletionHandler<RouteManager>() {
            @Override
            public void success(RouteManager result) {
                result.subscribe("gyro_stream2", new RouteManager.MessageHandler() {
                    @Override
                    public void process(Message msg) {
                        final CartesianFloat spinData = msg.getData(CartesianFloat.class);
                        //Log.i(TAG, String.format("Gyroscope: %s", spinData.toString()));
                        gyro2 = spinData.x() + ", " + spinData.y() + ", " + spinData.z();
                        payload.setIMU2Gyro(gyro2);
                    }
                });
            }
        });
        accelModule2.enableAxisSampling(); //You must enable axis sampling before you can start
        accelModule2.start();
        gyroModule2.start();
    }

    public void killListeners2(){
        gyroModule2.stop();
        accelModule2.disableAxisSampling(); //Likewise, you must first disable axis sampling before stopping
        accelModule2.stop();
        magModule2.stop();
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) { }



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
