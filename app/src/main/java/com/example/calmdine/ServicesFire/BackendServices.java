package com.example.calmdine.ServicesFire;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.calmdine.models.Restaurant;
import com.example.calmdine.models.RestaurantWithTimestamp;
import com.example.calmdine.models.SensorModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BackendServices extends Activity {
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    DatabaseReference restaurantRef;
    ArrayList<Restaurant> restaurantsList;
    private boolean listCompleted = false;
    List<RestaurantWithTimestamp> restaurantWithTimestampList = new ArrayList<>();

    public BackendServices() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        restaurantsList = new ArrayList<>();
        restaurantRef = databaseReference.child("restaurants");
        getAllRestaurants();
    }

    public void getAllRestaurants() {
        restaurantRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    Log.i("Object", String.valueOf(postSnapshot.child("light")));
                    String nameSensor = postSnapshot.child("name").getValue().toString();
                    Iterable<DataSnapshot> arrLight = postSnapshot.child("light").getChildren();
                    Iterable<DataSnapshot> arrNoise = postSnapshot.child("noise").getChildren();
                    SensorModel sensorModel;
                    List<SensorModel> sensorModels = new ArrayList<>();
                    List<Double> lightList = new ArrayList<>();
                    List<Double> noiseList = new ArrayList<>();
                    while (arrLight.iterator().hasNext()) {
                        DataSnapshot currentPosition = arrLight.iterator().next();
                        Double lightTemp = Double.valueOf(currentPosition.child("light").getValue().toString());
                        lightList.add(lightTemp);
                    }
                    while (arrNoise.iterator().hasNext()) {
                        DataSnapshot currentPosition = arrNoise.iterator().next();
                        Double noiseTemp = Double.valueOf(currentPosition.child("noise").getValue().toString());
                        noiseList.add(noiseTemp);
                    }
                    Restaurant rest = new Restaurant(
                            postSnapshot.getKey(),
                            noiseList,
                            lightList,
                            Double.parseDouble(postSnapshot.child("rating").getValue().toString()),
                            Float.parseFloat(postSnapshot.child("longitude").getValue().toString()),
                            Float.parseFloat(postSnapshot.child("latitude").getValue().toString())
                    );
                    restaurantsList.add(rest);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
    public void addSensorData(SensorModel sensorModel, Restaurant restaurant) {
        Restaurant selectedRestaurant = null;
        for (Restaurant rest: restaurantsList) {
            if (round(rest.getLongitude()) == round(restaurant.getLongitude()) && round(rest.getLatitude()) == round(restaurant.getLatitude())) {
                selectedRestaurant = rest;
            }
        }
        if (!selectedRestaurant.equals(null)) {
            String keyLight = restaurantRef.child(selectedRestaurant.getName()).child("light").push().getKey();
            Map<String, String> lightHashMap = new HashMap<>();
            lightHashMap.put("light", String.valueOf(sensorModel.getLight()));
            lightHashMap.put("timeStamp", String.valueOf(sensorModel.getTimestamp()));
            restaurantRef.child(selectedRestaurant.getName()).child("light").child(keyLight).setValue(lightHashMap);

            String keyNoise = restaurantRef.child(selectedRestaurant.getName()).child("noise").push().getKey();
            Map<String, String> noiseHashMap = new HashMap<>();
            noiseHashMap.put("noise", String.valueOf(sensorModel.getNoise()));
            noiseHashMap.put("timeStamp", String.valueOf(sensorModel.getTimestamp()));
            restaurantRef.child(selectedRestaurant.getName()).child("noise").child(keyNoise).setValue(noiseHashMap);
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

    public void addSensorDataDummy(Restaurant restaurant) {
        restaurantRef.child(restaurant.getName()).setValue(restaurant);
    }

    public ArrayList<Restaurant> returnAllRestaurants() {
        return restaurantsList;
    }

    public List<RestaurantWithTimestamp> getAllRestaurantDetailsForRecommendation() {
        Log.i("all-details", "called");
        listCompleted = false;
        restaurantRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                List<SensorModel> sensorModelsForLight = new ArrayList<>();
                List<SensorModel> sensorModelsForNoise = new ArrayList<>();

                Iterable<DataSnapshot> arrLightWithTimeStamp = null;
                Iterable<DataSnapshot> arrNoiseWithTimeStamp = null;
                List<String> arrNames = new ArrayList<>();
                SensorModel sensorModel;

                Log.i("time--11", "0");
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    Log.i("time--00", "0");
//                    Log.i("Object--", String.valueOf(postSnapshot.child("light")));
                    String nameSensor = postSnapshot.child("name").getValue().toString();
                    arrLightWithTimeStamp = postSnapshot.child("light").getChildren();
                    arrNoiseWithTimeStamp = postSnapshot.child("noise").getChildren();
                    arrNames.add(postSnapshot.child("name").getValue().toString());



                    while (arrLightWithTimeStamp.iterator().hasNext()) {
                        Log.i("time--==", "0");
                        DataSnapshot snapshotLight = arrLightWithTimeStamp.iterator().next();
//                        Log.i("timestamp--", String.valueOf(snapshotLight.child("timeStamp").getValue()));
                        SensorModel lightTemp = new SensorModel(
                                nameSensor,
                                Double.parseDouble(String.valueOf(snapshotLight.child("light").getValue())),
                                Double.valueOf(0),
                                Timestamp.valueOf(String.valueOf(snapshotLight.child("timeStamp").getValue()))
                        );
                        sensorModelsForLight.add(lightTemp);
                    }
                    while (arrNoiseWithTimeStamp.iterator().hasNext()) {
                        Log.i("time----", "0");
                        DataSnapshot snapshotLight = arrNoiseWithTimeStamp.iterator().next();
                        SensorModel noiseTemp = new SensorModel(
                                nameSensor,
                                Double.valueOf(0),
                                Double.parseDouble(String.valueOf(snapshotLight.child("noise").getValue())),
                                Timestamp.valueOf(String.valueOf(snapshotLight.child("timeStamp").getValue()))
                        );
                        sensorModelsForNoise.add(noiseTemp);
                    }
//                    while (restaurantWithTimestampList.iterator().hasNext()) {
//                        RestaurantWithTimestamp currentRestTimestamp = restaurantWithTimestampList.iterator().next();
////                        Log.i("time", String.valueOf(currentRestTimestamp.getLight()));
//                        for (SensorModel senModel: currentRestTimestamp.getLight()) {
//                            Log.i("time----", String.valueOf(senModel.getTimestamp()));
//                        }
//
//                    }
                    RestaurantWithTimestamp restTime = new RestaurantWithTimestamp(
                        postSnapshot.getKey(),
                        sensorModelsForNoise,
                        sensorModelsForLight,
                        Double.parseDouble(postSnapshot.child("rating").getValue().toString()),
                        Float.parseFloat(postSnapshot.child("longitude").getValue().toString()),
                        Float.parseFloat(postSnapshot.child("latitude").getValue().toString())
                    );
                    restaurantWithTimestampList.add(restTime);
                    Log.i("time--++", "1");
                }
                listCompleted = true;
                Log.i("time--++--", "1");
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.i("time--1---1", "0");
            }
        });
        while (!listCompleted) {
        }
        Log.i("time--++returning", "1");
        return restaurantWithTimestampList;

    }

}
