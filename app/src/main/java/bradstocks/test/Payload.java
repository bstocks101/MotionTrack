package bradstocks.test;

import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * Created by bradstocks on 2016/06/14.
 */
public class Payload {
    float pressure, xMag, yMag, zMag, xAcc, yAcc, zAcc, xGyro, yGyro, zGyro, bearGPS, temperature;
    double lat, lon, alt;
    File toSDcard;
   // FileWriter fWrite;
    //FileOutputStream fOut;
    //PrintWriter pWrite;
    //OutputStreamWriter osw;
    OutputStream os;

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public Payload(String name){
        pressure = xMag = yMag = zMag = temperature = xAcc = yAcc = zAcc = xGyro = yGyro = zGyro = bearGPS = 0;
        lat = lon = alt = 0;
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
                    ", GPSlat, GPSlon, GPSalt, GPSBear, temperature\n";
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
                ""+lat+ ", "+lon+ ", "+alt+ ", "+bearGPS+ ", "+temperature +"\n";
        os.write(temp.getBytes());
    }

    public void endFile() throws IOException {
           os.close();
    }

    public void setxMag(float xMag) {
        this.xMag = xMag;
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

    public void setAlt(double alt) {
        this.alt = alt;
    }
}
