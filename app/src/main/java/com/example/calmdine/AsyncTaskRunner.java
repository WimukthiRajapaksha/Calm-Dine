package com.example.calmdine;

import android.app.ProgressDialog;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.calmdine.ServicesFire.BackendServices;
import com.example.calmdine.models.Restaurant;
import com.example.calmdine.models.SensorModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;

public class AsyncTaskRunner extends AsyncTask<Void, Void, Void> implements SensorEventListener  {
    private AudioRecord ar = null;
    private int minSize;
    private SensorManager sensorManager;
    private Sensor lightSensor;
    private int samplingRate;
    private int intervalTime;
    private double avgLight;
    private ArrayList<Double> lightSensorValues;
    private ArrayList<Double> noiseSensorValues;
    ArrayList<Restaurant> restaurantsList;
    ArrayList<Restaurant> restaurantsUpdatedList;
    private Context mContext;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference restaurantRef;

    public BackgroundLocationListener mBackgroundLocationListener;
    ProgressDialog progDailog = null;
    BackendServices backendServices;


    public AsyncTaskRunner(SensorManager sensorManager, Context context) {
        minSize = 64;
        this.sensorManager = sensorManager;
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        samplingRate = 1000000;
        intervalTime = 10;
        lightSensorValues = new ArrayList<>();
        restaurantsList = new ArrayList<>();
        restaurantsUpdatedList = new ArrayList<>();
        mContext = context;
        firebaseDatabase = FirebaseDatabase.getInstance();
        restaurantRef = firebaseDatabase.getReference().child("restaurants");
        mBackgroundLocationListener = new BackgroundLocationListener(mContext);
        backendServices = new BackendServices();
        restaurantsList = backendServices.returnAllRestaurants();
    }

    public void start() {
        minSize= AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        ar = new AudioRecord(MediaRecorder.AudioSource.MIC, 8000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,minSize);
        ar.startRecording();
    }

    public void stop() {
        if (ar != null) {
            ar.stop();
        }
    }

    public double getAmplitude() {
        short[] buffer = new short[minSize];
        ar = new AudioRecord(MediaRecorder.AudioSource.MIC, 8000,AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT,minSize);
        ar.read(buffer, 0, minSize);
        int max = 0;
        for (short s : buffer)
        {
            if (Math.abs(s) > max)
            {
                max = Math.abs(s);
            }
        }
        return max;
    }

//    public boolean isBlowing()
//    {
//        boolean recorder=true;
//
//        int minSize = AudioRecord.getMinBufferSize(8000,AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);
//        AudioRecord ar = new AudioRecord(MediaRecorder.AudioSource.MIC, 8000,AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT,minSize);
//
//        short[] buffer = new short[minSize];
//
//        ar.startRecording();
//        while(recorder)
//        {
//
//            ar.read(buffer, 0, minSize);
//            for (short s : buffer)
//            {
//                if (Math.abs(s) > 27000)   //DETECT VOLUME (IF I BLOW IN THE MIC)
//                {
//                    int blow_value = Math.abs(s);
//                    Log.i("Blow Value=", String.valueOf(blow_value));
//                    ar.stop();
//                    recorder=false;
//
//                    return true;
//
//                }
//
//            }
//        }
//        return false;
//
//    }

    @Override
    protected Void doInBackground(Void... voids) {
        while (true) {
            try {
                Thread.sleep(5000);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                Log.i("background", "result");
                Log.i("Amplitude", String.valueOf(getAmplitude()));
                sensorManager.registerListener(this, lightSensor, samplingRate);
            }
            try {
                Thread.sleep(1);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                    while (true) {
                        if (restaurantsList.iterator().hasNext()) {
                            Restaurant res = restaurantsList.iterator().next();
                            Log.i("eeeE", String.valueOf(round(res.getLatitude())));
                            Log.i("eeEe", String.valueOf(round(mBackgroundLocationListener.getLocation().getLatitude())));
                            Log.i("eEee", String.valueOf(round(res.getLongitude())));
                            Log.i("Eeee", String.valueOf(round(mBackgroundLocationListener.getLocation().getLongitude())));
                            if ((round(res.getLatitude()) == round(mBackgroundLocationListener.getLocation().getLatitude())) && (round(res.getLongitude()) == round(mBackgroundLocationListener.getLocation().getLongitude()))) {
                                Log.i("Eeeeeeeeeeeeeeeeee", String.valueOf(avgLight) + "  " + new Timestamp(System.currentTimeMillis()));
                                SensorModel sensorModel = new SensorModel(res.getName(), avgLight, 0);
                                backendServices.addSensorData(sensorModel, res);
                            }
                        }
                        break;
                    }
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (intervalTime > 0) {
            synchronized (this) {
                lightSensorValues.add(Double.valueOf(sensorEvent.values[0]));
                Log.i("light", String.valueOf(sensorEvent.values[0]));
                intervalTime--;
            }
        }
        System.out.println(lightSensorValues);
        if (intervalTime == 0) {
            double sum = 0;
            for (Double lightValSum : lightSensorValues) {
                sum += lightValSum;
            }
            avgLight = sum/lightSensorValues.size();
            Log.i("list", String.valueOf(restaurantsList.size()));

//            TODO - noise sensor value calculator
//            for (Double noiseValSum : noiseSensorValues) {
//                sum += lightValSum;
//            }
//            double avgLight = sum/lightSensorValues.size();
            intervalTime = 10;
            lightSensorValues.clear();
        }
    }

    public static double round(double value) {
        int places = 3;
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
