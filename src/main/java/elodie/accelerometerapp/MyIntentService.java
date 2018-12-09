package elodie.accelerometerapp;

import android.app.IntentService;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileWriter;
import java.text.DateFormat;



public class MyIntentService  extends IntentService implements SensorEventListener{


    private Sensor accelerometer;
    private Sensor linear_accelerometer;
    private SensorManager SM_accelerometer;
    private SensorManager SM_linearaccelerometer;

    public float[][] data; //save data
    private int counter = 0; // counter in data
    private boolean record = true; // for record

    private File file;
    private FileOutputStream fileOutputStream;
    private FileWriter writer;

    long lts;
    float ax,ay,az,lax,lay,laz;

    public MyIntentService() {
        super("AccelDataIntentService");
    }

    public MyIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        // matrix to log results
        data = new float[10000][7];
        Bundle b = intent.getExtras();
        String FILE_NAME = (String) b.get("FILE_NAME");
        final Long longMillSec = Long.parseLong(b.get("longMillSec").toString());


        SM_accelerometer = (SensorManager) getSystemService(SENSOR_SERVICE);
        SM_linearaccelerometer = (SensorManager) getSystemService(SENSOR_SERVICE);

        accelerometer = SM_accelerometer.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        linear_accelerometer = SM_linearaccelerometer.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        SM_accelerometer.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        SM_linearaccelerometer.registerListener(this, linear_accelerometer, SensorManager.SENSOR_DELAY_FASTEST);

        File Root = Environment.getExternalStorageDirectory();
        File Dir = new File(Root.getAbsoluteFile(),"/AccelData");
        if(!Dir.exists()) {
            Dir.mkdir();
        }
        file = new File(Dir, FILE_NAME);

    }



    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onSensorChanged (SensorEvent event){
        Sensor sensor = event.sensor;

        if(sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            ax = event.values[0];
            ay = event.values[1];
            az = event.values[2];
        }

        if(sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            lax = event.values[0];
            lay = event.values[1];
            laz = event.values[2];
        }

        lts = event.timestamp;

        // ESB do the log part
        if (record)
        {
            data[counter][0] = ax;
            data[counter][1] = ay;
            data[counter][2] = az;
            data[counter][3] = lax;
            data[counter][4] = lay;
            data[counter][5] = laz;
            data[counter][6] = counter;
            data[counter][7] = lts;

            /*long timeInMillis = (new Date()).getTime()
                    + (sensorEvent.timestamp - System.nanoTime()) / 1000000L;
            long t = sensor.timestamp;*/

            //String it = intent.getStringExtra(TEXT_INPUT);

            float t =  System.currentTimeMillis();


            try {
                writeCsvData(ax,ay,az,lax,lay,laz,counter,t);
                writer.flush();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            counter++;


        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i){
    }

    private void writeCsvHeader(String h1, String h2, String h3, String h4, String h5, String h6, String h7) throws IOException {
        String line = String.format("%s,%s,%s\n", h1,h2,h3);
        writer.write(line);
    }
    private void writeCsvData(float ax, float ay, float az,float lax, float lay, float laz, int c, float t) throws IOException {
        String line = String.format("%f,%f,%f,%f,%f,%f,%d,%f\n", ax, ay, az, lax,lay,laz,c,t);
        writer.write(line);
    }

}
