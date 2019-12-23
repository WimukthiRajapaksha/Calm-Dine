package com.example.calmdine;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Printer;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.calmdine.models.Restaurant;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.PrivateKey;
import java.util.ArrayList;

public class AsyncTaskRunner extends AsyncTask<Void, Void, Void> implements SensorEventListener {
    private AudioRecord ar = null;
    private int minSize;
    private SensorManager sensorManager;
    private Sensor lightSensor;
    private SensorEventListener sensorEventListener;
    private int samplingRate;
    private int intervalTime;
    private ArrayList<Double> lightSensorValues;
    ArrayList<Restaurant> restaurantsList;
    ArrayList<Restaurant> restaurantsUpdatedList;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference restaurantRef;


    public AsyncTaskRunner(SensorManager sensorManager) {
        minSize = 64;
        this.sensorManager = sensorManager;
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        samplingRate = 1000000;
        intervalTime = 10;
        lightSensorValues = new ArrayList<>();
        restaurantsList = new ArrayList<>();
        restaurantsUpdatedList = new ArrayList<>();

        firebaseDatabase = FirebaseDatabase.getInstance();
        restaurantRef = firebaseDatabase.getReference().child("restaurants");

        Log.i("Constructor", "---------------");

        restaurantRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    Restaurant rest = new Restaurant(
                            postSnapshot.getKey(),
                            Double.parseDouble(postSnapshot.child("noise").getValue().toString()),
                            Double.parseDouble(postSnapshot.child("light").getValue().toString()),
                            Double.parseDouble(postSnapshot.child("rating").getValue().toString()),
                            Float.parseFloat(postSnapshot.child("longitude").getValue().toString()),
                            Float.parseFloat(postSnapshot.child("latitude").getValue().toString())
                    );
                    restaurantsList.add(rest);
                }
//                Log.i("Size++", String.valueOf(restaurantsList.size()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });
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
//        Log.i("Amplitude", String.valueOf(minSize));
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
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.i("background", "result");
            Log.i("Amplitude", String.valueOf(getAmplitude()));

            sensorManager.registerListener(this, lightSensor, samplingRate);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (intervalTime > 0) {
            synchronized (this) {
                lightSensorValues.add(Double.valueOf(sensorEvent.values[0]));
                Log.i("light", String.valueOf(sensorEvent.values[0]));
                Log.i("light--", String.valueOf(lightSensor.getMaximumRange()));
                intervalTime--;
            }
        }
        System.out.println(lightSensorValues);
        if (intervalTime == 0) {
            double sum = 0;
            for (Double lightValSum : lightSensorValues) {
                sum += lightValSum;
            }
            double avg = sum/lightSensorValues.size();
            for (Restaurant res: restaurantsList) {
                if (res.getLatitude() == 79.900873f && res.getLongitude() == 6.795937f) {
                    res.setLight(avg);
                    restaurantRef.child(res.getName()).setValue(res);
                }
            }



            intervalTime = 10;
            lightSensorValues.clear();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
