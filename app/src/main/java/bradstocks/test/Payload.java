package bradstocks.test;

import android.os.Environment;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


/**
 * Created by bradstocks on 2016/06/14.
 */
public class Payload {
    float pressure, xMag, yMag, zMag, xAcc, yAcc, zAcc, xGyro, yGyro, zGyro, bearGPS, temperature, GPSSpeed;
    String IMU1Acc, IMU1Gyro, IMU1MAG, IMU1Temp, IMU2Acc, IMU2Gyro, IMU2MAG, IMU2Temp, GPIO;
    double lat, lon;
    File toSDcard;
    OutputStream os;

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public Payload(String name){
        pressure = xMag = yMag = zMag = temperature = xAcc = yAcc = zAcc = xGyro = yGyro = zGyro = bearGPS = GPSSpeed= 0;
        lat = lon = 0;
        try {
            toSDcard = new File(Environment.getExternalStorageDirectory()+File.separator + name);
            toSDcard.createNewFile();
            //toSDcard = new File(/*Environment.getExternalStorageDirectory().getAbsolutePath()+ */"/" + name);
            //toSDcard.createNewFile();
            //fWrite = new FileWriter(toSDcard);
            //fOut = new FileOutputStream(toSDcard);
            //pWrite = new PrintWriter(fWrite);
            //outWriter = new OutputStreamWriter(fOut);

            String temp = "Pressure, xMag, yMag, zMag, xAcc, yAcc, zAcc, xGyro, yGyro, zGyro" +
                    ", GPSlat, GPSlon, GPSalt, GPSBear, temperature, IMU1Accx, IMU1Accy, IMU1Accz, IMU1Gyrox, IMU1Gyroy, IMU1Gyroz, " +
                    "IMU1Magx, IMU1Magy, IMU1Magz, IMU1Temp, " +
                    "IMU2Accx, IMU2Accy, IMU2Accz, IMU2Gyrox, IMU2Gyroy, IMU2Gyroz, IMU2Magx, IMU2Magy, IMU2Magz, IMU2Temp, GPIO\n";
            //fOut.write(temp.getBytes());
            if(toSDcard.exists()){
                os = new FileOutputStream(toSDcard);
                os.write(temp.getBytes());
            }
            //pWrite.print(temp);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeToFile() throws IOException {
        String temp = ""+pressure+ ", "+xMag+ ", "+yMag+ ", "+zMag+ ", "+xAcc+ ", "+yAcc+ ", "+zAcc+ ", "+xGyro+ ", "+yGyro+ ", "+zGyro+ ", " +
                ""+lat+ ", "+lon+ ", "+GPSSpeed+ ", "+bearGPS+ ", "+temperature + ", " + IMU1Acc +", "+
        IMU1Gyro + ", " + IMU1MAG + ", " + IMU1Temp + ", " + IMU2Acc + ", " + IMU2Gyro + ", " + IMU2MAG + ", " + IMU2Temp + ", " + GPIO +"\n";
        os.write(temp.getBytes());
    }

    public void setGPIO(String GPIO) {
        this.GPIO = GPIO;
    }

    public void endFile() throws IOException {
           os.close();
    }

    public void setxMag(float xMag) {
        this.xMag = xMag;
    }

    public void setIMU1Acc(String IMU1Acc) {
        this.IMU1Acc = IMU1Acc;
    }

    public void setIMU1Gyro(String IMU1Gyro) {
        this.IMU1Gyro = IMU1Gyro;
    }

    public void setIMU1MAG(String IMU1MAG) {
        this.IMU1MAG = IMU1MAG;
    }

    public void setIMU1Temp(String IMU1Temp) {
        this.IMU1Temp = IMU1Temp;
    }

    public void setIMU2Acc(String IMU2Acc) {
        this.IMU2Acc = IMU2Acc;
    }

    public void setIMU2Gyro(String IMU2Gyro) {
        this.IMU2Gyro = IMU2Gyro;
    }

    public void setIMU2MAG(String IMU2MAG) {
        this.IMU2MAG = IMU2MAG;
    }

    public void setIMU2Temp(String IMU2Temp) {
        this.IMU2Temp = IMU2Temp;
    }

    public void setyMag(float yMag) {
        this.yMag = yMag;
    }

    public void setzMag(float zMag) {
        this.zMag = zMag;
    }

    public void setPressure(float pressure) {
        this.pressure = pressure;
    }


    public void setxAcc(float xAcc) {
        this.xAcc = xAcc;
    }

    public void setyAcc(float yAcc) {
        this.yAcc = yAcc;
    }

    public void setzAcc(float zAcc) {
        this.zAcc = zAcc;
    }

    public void setxGyro(float xGyro) {
        this.xGyro = xGyro;
    }

    public void setyGyro(float yGyro) {
        this.yGyro = yGyro;
    }

    public void setzGyro(float zGyro) {
        this.zGyro = zGyro;
    }

    public void setBearGPS(float bearGPS) {
        this.bearGPS = bearGPS;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }


    public void setGPSSpeed(float GPSSpeed) {
        this.GPSSpeed = GPSSpeed;
    }
}
