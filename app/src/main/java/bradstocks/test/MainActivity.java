package bradstocks.test;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
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
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
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
import com.mbientlab.metawear.data.CartesianShort;
import com.mbientlab.metawear.module.Bmi160Accelerometer;
import com.mbientlab.metawear.module.Led;

import android.widget.Switch;
import android.widget.CompoundButton;
import com.mbientlab.metawear.RouteManager;
import com.mbientlab.metawear.data.CartesianFloat;
import com.mbientlab.metawear.module.Bmi160Gyro;
import com.mbientlab.metawear.module.Bmi160Gyro.*;
import com.mbientlab.metawear.module.Bmm150Magnetometer;
import com.mbientlab.metawear.module.Bmm150Magnetometer.PowerPreset;
import com.mbientlab.metawear.module.Logging;
import com.mbientlab.metawear.module.MultiChannelTemperature;
import com.mbientlab.metawear.module.MultiChannelTemperature.*;
import com.mbientlab.metawear.module.Bmi160Accelerometer.AccRange;
import com.mbientlab.metawear.module.Bmi160Accelerometer.OutputDataRate;


import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements ServiceConnection{

    ToneGenerator tone;
    private MetaWearBleService.LocalBinder serviceBinder;
    private final String MW_MAC_ADDRESS= "D0:73:6E:F3:AA:12"; //tip of tail
    //private final String MW_MAC_ADDRESS2= "C2:5D:6E:47:85:C2"; faulty IMU sensor
    private final String MW_MAC_ADDRESS2= "FE:8B:EF:C3:49:E5"; //base of spine
    private final String MW_MAC_ADDRESS3= "C2:5D:6E:47:85:C2"; //complementary accel
    String log1 = "";
    String log2 = "";
    private MetaWearBoard mwBoard;
    private MetaWearBoard mwBoard2;
    private MetaWearBoard mwBoard3;
    private Led ledModule;
    private Led ledModule2;
    private static final String TAG = "Feedback";
    private static final String TAG2 = "Stream1";
    private static final String TAG3 = "Stream2";
    private static final String TAG4 = "GPS";
    private static final String TAG5 = "Stream3";
    private Button connect;
    private Button connect2;
    private Button connect3;
    private Button led1_tog;
    private Button led2_tog;
    private Switch accel_switch;
    private Switch accel_switch2;
    private Switch accel_switch3;
    private Bmi160Accelerometer accelModule;
    private Bmi160Accelerometer accelModule2;
    private Bmi160Accelerometer accelModule3;
    private Bmi160Gyro gyroModule;
    private Bmi160Gyro gyroModule2;
    private Logging loggingModule;
    private Logging loggingModule2;
    Bmm150Magnetometer magModule;
    Bmm150Magnetometer magModule2;
    MultiChannelTemperature mcTempModule;
    MultiChannelTemperature mcTempModule2;
    private static final String STREAM_KEY = "accel_stream";
    private static final String GYRO_STREAM_KEY = "gyro_stream";
    BluetoothAdapter mBluetoothAdapter;
    List<Source> tempSources;
    List<Source> tempSources2;
    int tempCount;
    int count;
    Timer serverTimer;
    String state = "";
    int sensor1Count = 0;
    int sensor2Count = 0;
    int sensor3Count = 0;
    List<String> Acc1Queue = new ArrayList<String>();
    List<String> Acc2Queue = new ArrayList<String>();
    List<String> Acc3Queue = new ArrayList<String>();
    List<String> Gyro1Queue = new ArrayList<String>();
    List<String> Gyro2Queue = new ArrayList<String>();
    List<String> Mag1Queue = new ArrayList<String>();
    List<String> Mag2Queue = new ArrayList<String>();
    String previousState = "";
    TextView onBoardStatus = null;
    TextView sensorStaus = null;
    TextView sampleAcc = null;
    TextView sampleAcc2 = null;
    TextView feedback = null;


    private SensorManager mSensorManager;
    private SensorEventListener mSensorListener;
    LocationManager locationManager;
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
    String accel3;
    float bear;
    boolean running;
    boolean connected;
    boolean connected2;
    boolean connected3;
    boolean attemptConnect1;
    boolean attemptConnect2;
    boolean attemptConnect3;
    boolean sampling;
    boolean sampling2;
    boolean sampling3;
    boolean LED1;
    boolean LED2;
    float tempArr[];
    int stage;
    protected PowerManager.WakeLock mWakeLock;
    

    HttpClient httpclient;
    HttpGet httpget;

    //Task to check webserver every second
    TimerTask getTask = new TimerTask() {
        @Override
        public void run() {
            try {
                HttpResponse response = httpclient.execute(httpget);
                if(response!=null) {
                    InputStream inputStream = response.getEntity().getContent();
                    state = convertStreamToString(inputStream);
                    if(!state.equalsIgnoreCase(previousState)){
                        Log.i(TAG, "State changed to: " + state);
                        if(state.isEmpty()){}

                        else if(state.charAt(0)=='#'){ //set count number for new file name
                            String temp = state.substring(1, state.length());
                            count = Integer.parseInt(temp);
                        }
                        if(state.equalsIgnoreCase("1")){ //connect sensor 1
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    connect.callOnClick();
                                }
                            });
                        }
                        else if(state.equalsIgnoreCase("2")){ //connect sensor 2
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    connect2.callOnClick();
                                }
                            });
                        }
                        else if(state.equalsIgnoreCase("3")){ //connect sensor 3
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    connect3.callOnClick();
                                }
                            });
                        }
                       else if(state.equalsIgnoreCase("4")){ //enable stream 1
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(connected)
                                    accel_switch.setChecked(true);
                                    else Log.i(TAG, "Attempted to open stream 1 without connection to sensor 1");
                                }
                            });
                        }
                        else if(state.equalsIgnoreCase("5")){ //etc
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(connected2)
                                    accel_switch2.setChecked(true);
                                    else Log.i(TAG, "Attempted to open stream 2 without connection to sensor 2");
                                }
                            });

                        }
                        else if(state.equalsIgnoreCase("6")){
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(connected3)
                                    accel_switch3.setChecked(true);
                                    else Log.i(TAG, "Attempted to open stream 3 without connection to sensor 3");
                                }
                            });

                        }
                       else if(state.equalsIgnoreCase("7")){
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(!running)
                                    start.callOnClick();
                                    else Log.i(TAG, "Attempted to start a log that was already running");
                                }
                            });

                        }
                        else if(state.equalsIgnoreCase("8")){
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //if(running)
                                    stop.callOnClick();
                                    //else Log.i(TAG, "Attempted to stop logging without starting");
                                }
                            });

                        }
                        else if(state.equalsIgnoreCase("9")){
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(sampling)
                                    accel_switch.setChecked(false);
                                    else Log.i(TAG, "Attempted to close stream 1 that wasn't streaming");
                                }
                            });
                        }
                       else  if(state.equalsIgnoreCase("10")){
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(sampling2)
                                    accel_switch2.setChecked(false);
                                    else Log.i(TAG, "Attempted to close stream 3 that wasn't streaming");
                                }
                            });
                        }
                        else  if(state.equalsIgnoreCase("11")){
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(sampling3)
                                    accel_switch3.setChecked(false);
                                    else Log.i(TAG, "Attempted to close stream 3 that wasn't streaming");
                                }
                            });
                        }
                        else if(state.equalsIgnoreCase("12")){
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    attemptConnect1 = false;
                                    if(connected) {

                                        mwBoard.disconnect();
                                    }
                                    else Log.i(TAG, "Attempted to disconnect sensor 1 that is not connected");

                                }
                            });
                        }
                        else if(state.equalsIgnoreCase("13")){
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    attemptConnect2 = false;
                                    if(connected2){

                                        mwBoard2.disconnect();
                                }
                                    else Log.i(TAG, "Attempted to disconnect sensor 2 that is not connected");

                                }
                            });
                        }
                        else if(state.equalsIgnoreCase("14")){
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    attemptConnect3 = false;

                                    if(connected3) {
                                        mwBoard3.disconnect();
                                    }
                                    else Log.i(TAG, "Attempted to disconnect sensor 3 that is not connected");

                                }
                            });
                        }

                        else if(state.equalsIgnoreCase("15")){
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(mBluetoothAdapter.isEnabled()) {
                                        mBluetoothAdapter.disable();
                                        Log.i(TAG, "Disabling Bluetooth");
                                    }
                                    else
                                        Log.i(TAG, "Bluetooth already disabled");
                                }
                            });
                        }
                        else if(state.equalsIgnoreCase("16")){
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.i(TAG, "Clear entries on sensor 1");
                                    loggingModule.clearEntries();

                                }
                            });
                        }
                        else if(state.equalsIgnoreCase("17")){
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.i(TAG, "Clear entries on sensor 2");
                                    loggingModule2.clearEntries();

                                }
                            });
                        }

                    }
                    previousState = state;
                }
                else{
                    Log.i(TAG, "Server not responding");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    private final ConnectionStateHandler stateHandler= new ConnectionStateHandler() {
        @Override
        public void connected() {
            Log.i(TAG, "Device 1 Connected");
            connected = true;
            try {
                //ledModule = mwBoard.getModule(Led.class);
                accelModule = mwBoard.getModule(Bmi160Accelerometer.class);
                gyroModule = mwBoard.getModule(Bmi160Gyro.class);
                //magModule= mwBoard.getModule(Bmm150Magnetometer.class);
                //mcTempModule= mwBoard.getModule(MultiChannelTemperature.class);
                //tempSources= mcTempModule.getSources();
                loggingModule = mwBoard.getModule(Logging.class);
                if(sampling) setListeners();

            } catch (UnsupportedModuleException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void disconnected() {
           // killListeners();
           // mwBoard.connect();
            connected = false;
            attemptConnect1 = false;
            Log.i(TAG, "Connection 1 lost");
        }

        @Override
        public void failure(int status, Throwable error) {
           // killListeners();
            if(attemptConnect1) {
                mwBoard.connect();
                Log.e(TAG, "Error connecting 1... retrying", error);
            }
        }
    };

    private final ConnectionStateHandler stateHandler2= new ConnectionStateHandler() {
        @Override
        public void connected() {
            Log.i(TAG, "Device 2 connected");
            connected2 = true;
            try {
                //ledModule2 = mwBoard2.getModule(Led.class);
                accelModule2 = mwBoard2.getModule(Bmi160Accelerometer.class);
                gyroModule2 = mwBoard2.getModule(Bmi160Gyro.class);
                //gpioModule = mwBoard.getModule(Gpio.class);
                //magModule2= mwBoard2.getModule(Bmm150Magnetometer.class);
                //mcTempModule2= mwBoard2.getModule(MultiChannelTemperature.class);
                //tempSources2= mcTempModule2.getSources();
                loggingModule2 = mwBoard2.getModule(Logging.class);
                if(sampling2) setListeners2();

            } catch (UnsupportedModuleException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void disconnected() {
            //killListeners2();
            //mwBoard2.connect();
            connected2 = false;
            Log.i(TAG, "Connection 2 lost");
        }

        @Override
        public void failure(int status, Throwable error) {
            //killListeners2();
            if(attemptConnect2) {
                mwBoard2.connect();
                Log.e(TAG, "Error connecting 2... retrying", error);
            }
        }
    };

    private final ConnectionStateHandler stateHandler3= new ConnectionStateHandler() {
        @Override
        public void connected() {
            Log.i(TAG, "Device 3 Connected");
            connected3 = true;
            try {
                accelModule3 = mwBoard3.getModule(Bmi160Accelerometer.class);

            } catch (UnsupportedModuleException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void disconnected() {
            // killListeners();
            // mwBoard.connect();
            connected3 = false;
            Log.i(TAG, "Connection 3 lost");
        }

        @Override
        public void failure(int status, Throwable error) {
            // killListeners();
            if(attemptConnect3) {
                mwBoard3.connect();
                Log.e(TAG, "Error connecting 3... retrying", error);
            }
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

    public void retrieveBoard3() {
        final BluetoothManager btManager=
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        final BluetoothDevice remoteDevice=
                btManager.getAdapter().getRemoteDevice(MW_MAC_ADDRESS3);

        // Create a MetaWear board object for the Bluetooth Device
        mwBoard3= serviceBinder.getMetaWearBoard(remoteDevice);
        mwBoard3.setConnectionStateHandler(stateHandler3);

    }


    //end current timer task, create another
    public void pause() {
        Log.i(TAG, "Logging stopped");
        this.t.cancel();
        this.t = new Timer();
    }

    //begin new timer task
    public void resume() {
        Log.i(TAG, "Logging started");
        this.t.schedule(new TimerTask(){
            public void run(){
                writeHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(running) {
                            tempCount++;
                            //check temperature every second
                            if(tempCount > 100) {
//                                if(sampling)mcTempModule.readTemperature(tempSources.get(MetaWearRChannel.NRF_DIE));
//                                if(sampling2)mcTempModule2.readTemperature(tempSources.get(MetaWearRChannel.NRF_DIE));
                                tempCount = 0;
                            }
                            try {
                                if(sampling && connected) { //check stream 1 if enabled
                                    if (Gyro1Queue.size() > 0) {
                                        payload.setIMU1Gyro(Gyro1Queue.remove(0));
                                        sensor1Count = 0;
                                    } else {
                                        payload.setIMU1Gyro(gyro);
                                        sensor1Count++;
                                        if (sensor1Count > 50) { //if no new data for half a second, feed is dead
                                            connected = false;
                                            mwBoard.connect();
                                            Log.i(TAG, "Sensor 1 idle, reconnecting");
                                        }
                                    }
                                    /*if (Gyro1Queue.size() > 0)
                                        payload.setIMU1Gyro(Gyro1Queue.remove(0));
                                    else payload.setIMU1Gyro(gyro);
                                    if (Mag1Queue.size() > 0)
                                        payload.setIMU1MAG(Mag1Queue.remove(0));
                                    else payload.setIMU1MAG(mag);*/
                                }
                                if(sampling2 & connected2) {
                                    if (Gyro2Queue.size() > 0) {
                                        payload.setIMU2Gyro(Gyro2Queue.remove(0));
                                        sensor2Count = 0;
                                    } else {
                                        payload.setIMU2Gyro(gyro2);
                                        sensor2Count++;
                                        if (sensor2Count > 50) {
                                            mwBoard2.connect();
                                            connected2 = false;
                                            Log.i(TAG, "Sensor 2 idle, reconnecting");
                                        }
                                    }
                                   /* if (Gyro2Queue.size() > 0)
                                        payload.setIMU2Gyro(Gyro2Queue.remove(0));
                                    else payload.setIMU2Gyro(gyro2);
                                    if (Mag2Queue.size() > 0)
                                        payload.setIMU2MAG(Mag2Queue.remove(0));
                                    else payload.setIMU2MAG(mag2);*/
                                }
                                if(sampling3 && connected3){
                                    if (Acc3Queue.size() > 0) {
                                        payload.setIMU3Acc(Acc3Queue.remove(0));
                                        sensor3Count = 0;
                                    } else {
                                        payload.setIMU3Acc(accel3);
                                        sensor3Count++;
                                        if (sensor3Count > 50) {
                                            mwBoard3.connect();
                                            connected3 = false;
                                            Log.i(TAG, "Sensor 3 idle, reconnecting");
                                        }

                                    }
                                }
                                //Log.i(TAG, "Here");
                                payload.writeToFile(); //write data to file
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        }, 10, 10);
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        count = 0; //file count, can be set
        stage = 0; //stage sent from server
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        t= new Timer();
        
        final BluetoothManager mBluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
            Log.i(TAG, "Bluetooth enabled");
        }
        Log.i(TAG, "Application started");
        httpclient = new DefaultHttpClient(); //depreciated, need explicit library reference in gradle build
        httpget = new HttpGet("http://192.168.43.102:8000/state.html"); //static IP of laptop on tethered hotspot
        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
        this.mWakeLock.acquire(); //disable screen off timer

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
        payload = new Payload(timeStamp + ".csv");
        //payload = new Payload("testData" + count + ".csv");
        tone = new ToneGenerator(AudioManager.STREAM_ALARM, 100); 
        tempArr = new float[3];
        running = false;
        connected = false;
        connected2 = connected3 = false;
        sampling = sampling2 = sampling3 = false;
        LED1 = false;
        LED2 = false;
        attemptConnect1 = attemptConnect2 = attemptConnect3 = false;

        Context context = getApplicationContext();
        context.bindService(new Intent(this, MetaWearBleService.class),
                this, Context.BIND_AUTO_CREATE);

        CharSequence text = "Write is complete!";
        int duration = Toast.LENGTH_SHORT;
        toast = Toast.makeText(context, text, duration);
        start = (Button) findViewById(R.id.button);
        stop = (Button) findViewById(R.id.button2);
        connect = (Button) findViewById(R.id.connect1);
        connect2 = (Button) findViewById(R.id.connect2);
        connect3 = (Button) findViewById(R.id.connect3);
        led2_tog = (Button) findViewById(R.id.led_off);
        led1_tog = (Button) findViewById(R.id.led_on);
        accel_switch = (Switch) findViewById(R.id.accel_switch);
        accel_switch2 = (Switch) findViewById(R.id.accel_switch2);
        accel_switch3 = (Switch) findViewById(R.id.accel_switch3);

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
       /* Log.i(TAG, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER).getVendor());
        Log.i(TAG, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER).getName());
        Log.i(TAG,""+ mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER).getMaximumRange());
        Log.i(TAG, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE).getVendor());
        Log.i(TAG, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE).getName());
        Log.i(TAG,""+ mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE).getMaximumRange());
    */

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
                    payload.setPressure(event.values[0]);
                } else if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                    payload.setxMag(event.values[0]);
                    payload.setyMag(event.values[1]);
                    payload.setzMag(event.values[2]);
                } else if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    payload.setxAcc(event.values[0]);
                    payload.setyAcc(event.values[1]);
                    payload.setzAcc(event.values[2]);
                } else if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                    payload.setxGyro(event.values[0]);
                    payload.setyGyro(event.values[1]);
                    payload.setzGyro(event.values[2]);
                }
                else if(sensor.getType() == Sensor.TYPE_GRAVITY){
                    payload.setGravX(event.values[0]);
                    payload.setGravY(event.values[1]);
                    payload.setGravZ(event.values[2]);
                }
                else if(sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE){
                    payload.setTemperature(event.values[0]);
                }
                else if(sensor.getType() == Sensor.TYPE_ROTATION_VECTOR){
                    payload.setRotX(event.values[0]);
                    payload.setRotY(event.values[1]);
                    payload.setRotZ(event.values[2]);
                    payload.setScalarComp(event.values[3]);
                }
                else {
                }

            }
        };

        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                lat = location.getLatitude();
                lon = location.getLongitude();
                alt = location.getAltitude();
                bear = location.getBearing();
                Log.i(TAG4, "GPS: (" + lat + ", " + lon + ", " + location.getSpeed() + ", " + bear + ")");

                payload.setLat(lat);
                payload.setLon(lon);
                payload.setGPSSpeed(location.getSpeed());
                payload.setBearGPS(bear);
                onBoardStatus.setText("All sensors ready");
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.i(TAG, "GPS connected");

            }

            public void onProviderEnabled(String provider) {
                Log.i(TAG, "GPS provider enabled");
            }

            public void onProviderDisabled(String provider) {
            }
        };


        stop.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                if (running) {
                    try {

                        payload.endFile(); //stop writing
                        toast.show();
                        tone.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 20); //20ms tone
                        String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
                        Log.i(TAG, "Write is complete to file " + timeStamp +".csv");
                        pause(); 
                        running = false;
                        if(sampling) {
                            loggingModule.stopLogging();
                            accelModule.disableAxisSampling();
                            accelModule.stop();
                           // accelModule.disableAxisSampling();
                           // accelModule.stop();
                        }
                        if(sampling2) {
                           // loggingModule2.stopLogging();
                            loggingModule2.stopLogging();
                            accelModule2.disableAxisSampling();
                            accelModule2.stop();
                        }
                        //payload.endLog();



                        payload = new Payload(timeStamp + ".csv");
                       // payload = new Payload("testData" + count + ".csv"); //make new file
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    Log.i(TAG, "Writing log data");
                    String temp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
                    File logFile = new File(Environment.getExternalStorageDirectory()+File.separator + temp + "LOG1.csv");
                    File logFile2 = new File(Environment.getExternalStorageDirectory()+File.separator + temp + "LOG2.csv");
                    try {
                        logFile.createNewFile();
                        logFile2.createNewFile();
                        if(logFile.exists()) {
                            OutputStream os1 = new FileOutputStream(logFile);
                            OutputStream os2 = new FileOutputStream(logFile2);
                            os1.write(log1.getBytes());
                            os2.write(log2.getBytes());
                            os1.close();
                            os2.close();
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        });

        start.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){

                    if (!running) {
                        tone.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 20);
                        resume();
                        running = true;

                        if(sampling) {
                            loggingModule.startLogging();
                            accelModule.enableAxisSampling();
                            accelModule.start();
                            log1 = "";
                        }
                         if(sampling2) {
                             loggingModule2.startLogging();
                             accelModule2.enableAxisSampling();
                             accelModule2.start();
                             log2 = "";
                         }
                        /*try {
                            payload.createLogFile();
                            Log.i(TAG, "Log file created");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }*/
                    }

            }
        });

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBluetoothAdapter.isEnabled()) {
                    mBluetoothAdapter.enable();
                    Log.i(TAG, "Bluetooth enabled");
                }
                Log.i(TAG, "Clicked connect 1");
                sensorStaus.setText("Connecting");
                attemptConnect1 = true;
                mwBoard.connect();

            }
        });

        connect2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBluetoothAdapter.isEnabled()) {
                    mBluetoothAdapter.enable();
                    Log.i(TAG, "Bluetooth enabled");
                }
                Log.i(TAG, "Clicked connect 2");
                sensorStaus.setText("Connecting");
                attemptConnect2 = true;
                mwBoard2.connect();

            }
        });

        connect3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBluetoothAdapter.isEnabled()) {
                    mBluetoothAdapter.enable();
                    Log.i(TAG, "Bluetooth enabled");
                }
                Log.i(TAG, "Clicked connect 3");
                sensorStaus.setText("Connecting");
                attemptConnect3 = true;
                mwBoard3.connect();
            }
        });
         /*
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
        });*/

        assert accel_switch != null;
        accel_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.i(TAG, "Stream 1 = " + isChecked);
               if(!connected) feedback.setText("Connection 1 lost");
               else {
                    sampling = isChecked;
                    if (isChecked) {
                        setListeners();
                        //loggingModule.startLogging(true);
                    } else {
                        killListeners();
                    }
               }
            }
        });

        assert accel_switch2 != null;
        accel_switch2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.i(TAG, "Stream 2 = " + isChecked);
                if(!connected2)feedback.setText("Connection 2 lost");
                else {
                    sampling2 = isChecked;
                    if (isChecked) {
                        setListeners2();
                        //loggingModule2.startLogging(true);


                    } else {
                        killListeners2();
                    }
                }
            }
        });

        assert accel_switch3 != null;
        accel_switch3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.i(TAG, "Stream 3 = " + isChecked);
                if(!connected3
                        ) feedback.setText("Connection 3 lost");
                else {
                    sampling3 = isChecked;
                    if (isChecked) {
                        accelModule3.configureAxisSampling()
                                .setFullScaleRange(AccRange.AR_16G)
                                .setOutputDataRate(OutputDataRate.ODR_100_HZ)
                                .commit();
                        AsyncOperation<RouteManager> routeManagerResultAccel3 = accelModule3.routeData().fromAxes().stream("Test_stream").commit();
                        routeManagerResultAccel3.onComplete(new AsyncOperation.CompletionHandler<RouteManager>() {
                            @Override
                            public void success(RouteManager result) {
                                result.subscribe("Test_stream", new RouteManager.MessageHandler() {
                                    @Override
                                    public void process(Message message) {
                                        CartesianFloat axes = message.getData(CartesianFloat.class);
                                        Log.i(TAG5, "Acc: " + axes.toString());
                                        accel3= axes.x() + ", " + axes.y() + ", " + axes.z();
                                        if(running) Acc3Queue.add(accel3);
                                    }
                                });
                            }
                            @Override
                            public void failure(Throwable error) {
                                Log.e(TAG, "Error committing route", error);
                            }
                        });

                        accelModule3.enableAxisSampling();
                        accelModule3.start();
                    } else {
                        accelModule3.disableAxisSampling();
                        accelModule3.stop();
                    }
                }
            }
        });

        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE), 5000);
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), 5000);
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), 5000);
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 5000);
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY), 5000);
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), 5000);
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE), 10000);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener); //or NETWORK_PROVIDER
        serverTimer = new Timer();
        this.serverTimer.schedule(getTask, 5000, 1000);

    }


    private String convertStreamToString(InputStream is) {
        String line = "";
        StringBuilder total = new StringBuilder();
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
        try {
            while ((line = rd.readLine()) != null) {
                total.append(line);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Stream Exception", Toast.LENGTH_SHORT).show();
        }
        return total.toString();
    }

    @Override
    public void onDestroy() {
       // mwBoard.disconnect();
       // mwBoard2.disconnect();
       // mBluetoothAdapter.disable();
       // if(running) stop.callOnClick();
        Log.i(TAG, "Destroying app");
        //this.mWakeLock.release();
        super.onDestroy();

        // Unbind the service when the activity is destroyed
//        getApplicationContext().unbindService(this);
    }

    @Override
    public void onStop(){
        //mwBoard.disconnect();
        //mwBoard2.disconnect();
        //mBluetoothAdapter.disable();
        //if(running) stop.callOnClick();
        Log.i(TAG, "Stopping app");
        //this.mWakeLock.release();
        super.onStop();
//        getApplicationContext().unbindService(this);

    }


    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        // Typecast the binder to the service's LocalBinder class
        serviceBinder = (MetaWearBleService.LocalBinder) service;
        retrieveBoard();
        retrieveBoard2();
        retrieveBoard3();

    }

    public void setListeners(){

       accelModule.configureAxisSampling()
                .setFullScaleRange(AccRange.AR_16G)
                .setOutputDataRate(OutputDataRate.ODR_100_HZ)
                .commit();

        accelModule.routeData()
                .fromAxes().log("Log1")
                .commit().onComplete(new AsyncOperation.CompletionHandler<RouteManager>() {
            @Override
            public void success(RouteManager result) {
                result.setLogMessageHandler("Log1", new RouteManager.MessageHandler() {
                    @Override
                    public void process(Message msg) {
                        final CartesianFloat axisData = msg.getData(CartesianFloat.class);
                        Log.i(TAG, String.format("Log1: %s", axisData.toString()));
                        String s = axisData.x() + ", " + axisData.y() + ", " + axisData.z();
                        log1 += s + "\n";
                    }
                });
            }

            @Override
            public void failure(Throwable error) {
                Log.e(TAG2, "Error committing route", error);
            }
        });

       gyroModule.configure()
                .setOutputDataRate(Bmi160Gyro.OutputDataRate.ODR_100_HZ)
                .setFullScaleRange(FullScaleRange.FSR_2000)
                .commit();
        /*magModule.setPowerPrsest(PowerPreset.LOW_POWER);
        magModule.enableBFieldSampling();
        mcTempModule.routeData()
                .fromSource(tempSources.get(MetaWearRChannel.NRF_DIE)).stream("temp_stream")
                .commit().onComplete(new AsyncOperation.CompletionHandler<RouteManager>() {

            @Override
            public void success(RouteManager result) {
                result.subscribe("temp_stream", new RouteManager.MessageHandler() {
                    @Override
                    public void process(Message msg) {
                       Log.i(TAG2, String.format("Temperature: %.3fC",
                                msg.getData(Float.class)));
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

                                Log.i(TAG2, "Mag1: " + bField.toString());
                                mag = bField.x() + ", " + bField.y() +", " + bField.z();
                                if(running)Mag1Queue.add(mag);
                                //payload.setIMU1MAG(mag);
                            }
                        });
                    }
                });
        AsyncOperation<RouteManager> routeManagerResultAccel = accelModule.routeData().fromAxes().stream(STREAM_KEY).commit();*/
        AsyncOperation<RouteManager> routeManagerResultGyro = gyroModule.routeData().fromAxes().stream(GYRO_STREAM_KEY).commit();
        /*routeManagerResultAccel.onComplete(new AsyncOperation.CompletionHandler<RouteManager>() {
            @Override

            public void success(final RouteManager result) {
                result.subscribe(STREAM_KEY, new RouteManager.MessageHandler() {
                    @Override
                    public void process(Message message) {
                        CartesianFloat axes = message.getData(CartesianFloat.class);
                        Log.i(TAG2, "Acc1: " + axes.toString());
                        accel= axes.x() + ", " + axes.y() + ", " + axes.z();
                        if(running)Acc1Queue.add(accel);
                        //     payload.setIMU1Acc(accel);
                    }
                });

            }
            @Override
            public void failure(Throwable error) {
                Log.e(TAG, "Error committing route", error);
            }
        });*/

        routeManagerResultGyro.onComplete(new AsyncOperation.CompletionHandler<RouteManager>() {
            @Override
            public void success(RouteManager result) {
                result.subscribe(GYRO_STREAM_KEY, new RouteManager.MessageHandler() {
                    @Override
                    public void process(Message msg) {
                        final CartesianFloat spinData = msg.getData(CartesianFloat.class);
                        Log.i(TAG2, "Gyro1: " + spinData.toString());
                        gyro = spinData.x() + ", " + spinData.y() + ", " + spinData.z();
                        if(running)Gyro1Queue.add(gyro);
                        //payload.setIMU1Gyro(gyro);
                    }
                });
            }
        });

        gyroModule.start();


        //magModule.start();

    }

    public void killListeners(){
        gyroModule.stop();
        //loggingModule.stopLogging();
        Log.i(TAG, "Downloading log1");
        loggingModule.downloadLog(0.05f, new Logging.DownloadHandler() {
            @Override
            public void onProgressUpdate(int nEntriesLeft, int totalEntries) {
                Log.i(TAG, String.format("Progress= %d / %d", nEntriesLeft,
                        totalEntries));
            }
        });
        //magModule.stop();
    }

    public void setListeners2(){
        accelModule2.configureAxisSampling()
                .setFullScaleRange(AccRange.AR_16G)
                .setOutputDataRate(OutputDataRate.ODR_100_HZ)
                .commit();

        accelModule2.routeData()
                .fromAxes().log("Log2")
                .commit().onComplete(new AsyncOperation.CompletionHandler<RouteManager>() {
            @Override
            public void success(RouteManager result) {
                result.setLogMessageHandler("Log2", new RouteManager.MessageHandler() {
                    @Override
                    public void process(Message msg) {
                        final CartesianFloat axisData = msg.getData(CartesianFloat.class);
                        Log.i(TAG, String.format("Log2: %s", axisData.toString()));
                        String s1 = axisData.x() + ", " + axisData.y() + ", " + axisData.z();
                        //Log.i(TAG, String.format("Log: %s", s));
                        log2 += s1 + "\n";
                        /*try {
                            payload.writeToLog(s);
                        } catch (IOException e) {
                            Log.i(TAG, "Didn't write");
                            e.printStackTrace();
                        }*/
                    }
                });
            }

            @Override
            public void failure(Throwable error) {
                Log.e(TAG2, "Error committing route", error);
            }
        });

        gyroModule2.configure()
                .setOutputDataRate(Bmi160Gyro.OutputDataRate.ODR_100_HZ)
                .setFullScaleRange(FullScaleRange.FSR_2000)
                .commit();

        /*magModule2.setPowerPrsest(PowerPreset.LOW_POWER);
        magModule2.enableBFieldSampling();
        mcTempModule2.routeData()
                .fromSource(tempSources2.get(MetaWearRChannel.NRF_DIE)).stream("temp_stream2")
                .commit().onComplete(new AsyncOperation.CompletionHandler<RouteManager>() {

            @Override
            public void success(RouteManager result) {
                result.subscribe("temp_stream2", new RouteManager.MessageHandler() {
                    @Override
                    public void process(Message msg) {
                        Log.i(TAG3, String.format("Temp2: %.3fC",
                               msg.getData(Float.class)));
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

                                Log.i(TAG3, "Mag2: " + bField.toString());
                                mag2 = bField.x() + ", " + bField.y() +", " + bField.z();
                                if(running)Mag2Queue.add(mag2);
                                //payload.setIMU2MAG(mag2);
                            }
                        });
                    }
                });
        AsyncOperation<RouteManager> routeManagerResultAccel = accelModule2.routeData().fromAxes().stream("accel_stream2").commit();*/
        AsyncOperation<RouteManager> routeManagerResultGyro = gyroModule2.routeData().fromAxes().stream("gyro_stream2").commit();
        /*routeManagerResultAccel.onComplete(new AsyncOperation.CompletionHandler<RouteManager>() {
            @Override
            public void success(RouteManager result) {
                result.subscribe("accel_stream2", new RouteManager.MessageHandler() {
                    @Override
                    public void process(Message message) {
                        CartesianFloat axes = message.getData(CartesianFloat.class);
                        Log.i(TAG3, "Accel2: " + axes.toString());
                        accel2= axes.x() + ", " + axes.y() + ", " + axes.z();
                        if(running)Acc2Queue.add(accel2);
                        //payload.setIMU2Acc(accel2);
                    }
                });
            }
            @Override
            public void failure(Throwable error) {
                Log.e(TAG, "Error committing route", error);
            }
        });
*/
        routeManagerResultGyro.onComplete(new AsyncOperation.CompletionHandler<RouteManager>() {
            @Override
            public void success(RouteManager result) {
                result.subscribe("gyro_stream2", new RouteManager.MessageHandler() {
                    @Override
                    public void process(Message msg) {
                        final CartesianFloat spinData = msg.getData(CartesianFloat.class);
                        Log.i(TAG3, "Gyro2: "+ spinData.toString());
                        gyro2 = spinData.x() + ", " + spinData.y() + ", " + spinData.z();
                        if(running)Gyro2Queue.add(gyro2);
                        //payload.setIMU2Gyro(gyro2);
                    }
                });
            }
        });
        //accelModule2.enableAxisSampling(); //You must enable axis sampling before you can start
        //accelModule2.start();
        gyroModule2.start();
       // magModule2.start();
    }

    public void killListeners2(){
        gyroModule2.stop();
        Log.i(TAG, "Downloading log2");
        loggingModule2.downloadLog(0.05f, new Logging.DownloadHandler() {
            @Override
            public void onProgressUpdate(int nEntriesLeft, int totalEntries) {
                Log.i(TAG, String.format("Progress2= %d / %d", nEntriesLeft,
                        totalEntries));
            }
        });

        //accelModule2.disableAxisSampling(); //Likewise, you must first disable axis sampling before stopping
        //accelModule2.stop();
        //magModule2.stop();
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) { }


}
