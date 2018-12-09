package elodie.accelerometerapp;

import android.hardware.SensorEventListener;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import java.io.BufferedWriter;;
import android.widget.EditText;
import android.widget.TextView;
import android.view.View;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEvent;

import java.io.File;
import java.io.FileWriter;
import android.content.ServiceConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.media.MediaScannerConnection;
import android.content.Context;
import android.net.Uri;
//import android.media.Media

import android.os.Environment;
import android.Manifest;
import android.content.DialogInterface;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;
import android.support.v7.app.AlertDialog;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.os.Build;
import java.util.Locale;
import java.io.Writer;

import android.media.MediaScannerConnection;

import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.Button;

import 	android.util.Log;


import android.os.CountDownTimer;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    File OUTFile;

    TextView x,y,z;
    float sx,sy,sz;
    TextView time_ev;
    View v;
    String act = "";
    boolean record = false;
    private SensorManager sm_sensor;
    private Sensor sensor;
    private String fullName;
    File Dir;

    public static boolean writeStringAsFile(final String fileContents, final File file) {
        boolean result = false;
        try {
            if (file != null) {
                file.createNewFile(); // ok if returns false, overwrite
                Writer out = new BufferedWriter(new FileWriter(file), 1024);
                out.write(fileContents);
                out.close();
                result = true;
            }
        } catch (IOException e) {
        }
        return result;
    }

    public static boolean appendStringToFile(final String appendContents, final File file) {
        boolean result = false;
        try {
            if (file != null && file.canWrite()) {
                file.createNewFile(); // ok if returns false, overwrite
                Writer out = new BufferedWriter(new FileWriter(file, true), 1024);
                out.write(appendContents);
                out.close();
                result = true;
            }
        } catch (IOException e) {
        }
        return result;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        boolean write = isWriteStoragePermissionGranted();
        if (!write)
        {
            Toast.makeText(this, "Application can't run without storage access", Toast.LENGTH_LONG).show();
            Toast.makeText(this, "Application will close", Toast.LENGTH_LONG).show();
            System.exit(1);
        }


        //setup sensor
        // sensor list
        // TYPE_ACCELEROMETER               YES
        // TYPE_ACCELEROMETER_UNCALIBRATED  YES
        // TYPE_LINEAR_ACCELERATION         YES
        // TYPE_GRAVITY                     YES
        // TYPE_AMBIENT_TEMPERATURE         NA
        // TYPE_GYROSCOPE                   YES
        // TYPE_GYROSCOPE_UNCALIBRATED      YES
        // TYPE_LIGHT                       YES but only 1 output
        // TYPE_MAGNETIC_FIELD              YES
        // TYPE_MAGNETIC_FIELD_UNCALIBRATED YES
        // TYPE_RELATIVE_HUMIDITY           NA
        // TYPE_GEOMAGNETIC_ROTATION_VECTOR YES
        // TYPE_PROXIMITY                   YES but only 1 output
        // TYPE_PRESSURE                    YES but only 1 output



        sm_sensor = (SensorManager)  getSystemService(SENSOR_SERVICE);
        sensor = sm_sensor.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION); // ESB sensor
        sm_sensor.registerListener(MainActivity.this, sensor, SensorManager.SENSOR_DELAY_UI);

        findViewById(R.id.stop).setEnabled(false);

        // display sensor
        x = findViewById(R.id.x);
        y = findViewById(R.id.y);
        z = findViewById(R.id.z);
        // Display time
        time_ev = findViewById(R.id.time_ev);


        final Button buttonStart = (Button) findViewById(R.id.start);
        buttonStart.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!record) {
                    record = true;
                    v.findViewById(R.id.start).setEnabled(false);
                    findViewById(R.id.stop).setEnabled(true);
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        if (isExternalStorageReadable() && isExternalStorageWritable()) {
                            //create directory if non existant
                            Dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+ "/DataSensor");
                            if (!Dir.exists()) {
                                Log.e("111", "Directory not created");
                                Dir.mkdir();
                            }
                            EditText temp = (EditText) findViewById(R.id.newactivity);
                            act = temp.getText().toString();
                            findViewById(R.id.newactivity).setEnabled(false);

                            SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US);
                            Date now = new Date();
                            fullName = formatter.format(now) + "_" +  act + ".txt";
                            OUTFile = new File(Dir, fullName);
                            String header = "TimeStamp,X,Y,Z\n";
                            writeStringAsFile(header, OUTFile);
                            record = true;
                        }
                    }
                }

            }
        });


        final Button buttonStop = (Button) findViewById(R.id.stop);
        buttonStop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                v.findViewById(R.id.stop).setEnabled(false);
                //v.findViewById(R.id.newactivity).setEnabled(true);
                if (record) {
                    record = false;
                    buttonStart.setEnabled(true);
                    buttonStop.setEnabled(false);
                }
            }
        });

    }

    @Override
    public void onSensorChanged(final SensorEvent sensorEvent) {
        long longtimestamp = 0;
        sx = 0; sy = 0; sz = 0;
        // to account for sensors with only one output
        if (sensorEvent.sensor.getType() == Sensor.TYPE_LIGHT || sensorEvent.sensor.getType() == Sensor.TYPE_PRESSURE || sensorEvent.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            x.setText("s_x: " + sensorEvent.values[0]);
        }else {
            x.setText("s_x: " + sensorEvent.values[0]);
            y.setText("s_y: " + sensorEvent.values[1]);
            z.setText("s_z: " + sensorEvent.values[2]);
        }

        if (record) {
            if (sensorEvent.sensor.getType() == Sensor.TYPE_LIGHT || sensorEvent.sensor.getType() == Sensor.TYPE_PRESSURE || sensorEvent.sensor.getType() == Sensor.TYPE_PROXIMITY) {
                sx = sensorEvent.values[0];
                longtimestamp = sensorEvent.timestamp; // only when sensors are changing (ns)
                time_ev.setText("time event: " + longtimestamp);
            }else {
                sx = sensorEvent.values[0];
                sy = sensorEvent.values[1];
                sz = sensorEvent.values[2];
                longtimestamp = sensorEvent.timestamp; // only when sensors are changing (ns)
                time_ev.setText("time event: " + longtimestamp);
                }

           // }
            if (longtimestamp != 0) {
                // format string
                String x = String.format("%.20f", sx);
                String y = String.format("%.20f", sy);
                String z = String.format("%.20f", sz);
                String data = String.valueOf(longtimestamp) + "," + x + "," + y + "," + z + "\n";
            appendStringToFile(data, OUTFile);
            }
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
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public  boolean isWriteStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("444","Permission is granted2");
                return true;
            } else {

                Log.v("555","Permission is revoked2");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v("444","Permission is granted2");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 2:
                Log.d("100", "External storage2");
                if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    Log.v("110","Permission: "+permissions[0]+ "was "+grantResults[0]);
                }else{
                }
                break;

            case 3:
                Log.d("200", "External storage1");
                if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    Log.v("210","Permission: "+permissions[0]+ "was "+grantResults[0]);
                }else{
                }
                break;
        }
    }



    protected void onPause() {
        super.onPause();
        sm_sensor.unregisterListener(this);
    }

    protected void onResume() {
        super.onResume();
        sm_sensor.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    final class MyMediaScannerConnectionClient
            implements MediaScannerConnectionClient {

        private String mFilename;
        private String mMimetype;
        private MediaScannerConnection mConn;

        public MyMediaScannerConnectionClient
                (Context ctx, File file, String mimetype) {
            this.mFilename = file.getAbsolutePath();
            mConn = new MediaScannerConnection(ctx, this);
            mConn.connect();
        }
        @Override
        public void onMediaScannerConnected() {
            mConn.scanFile(mFilename, mMimetype);
        }

        @Override
        public void onScanCompleted(String path, Uri uri) {
            mConn.disconnect();
        }
    }


}

