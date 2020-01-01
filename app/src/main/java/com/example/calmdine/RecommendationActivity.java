package com.example.calmdine;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.calmdine.ServicesFire.BackendServices;
import com.example.calmdine.models.AdapterModel;
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

public class RecommendationActivity extends AppCompatActivity {

    Spinner spinnerNoise;
    Spinner spinnerLight;

    private RecyclerView recyclerView;
    private RestaurantAdapter restaurantAdapter;
    ArrayList<Restaurant> restaurantsList;
    List<AdapterModel> restaurantsForUi;
    boolean initializedUiList = false;

    BackendServices backendServices;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    DatabaseReference restaurantRef;

    private boolean listCompleted = false;
    List<RestaurantWithTimestamp> restaurantWithTimestampList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommendation);

        Intent intent = getIntent();
        int noise = intent.getIntExtra("noise", 0);
        int light = intent.getIntExtra("light", 0);

        firebaseDatabase = FirebaseDatabase.getInstance();
        restaurantsList = new ArrayList<>();
        restaurantsForUi = new ArrayList<>();

        spinnerNoise = findViewById(R.id.spinnerNoise);
        spinnerLight = findViewById(R.id.spinnerLight);

        ArrayAdapter<CharSequence> adapterNoise = ArrayAdapter.createFromResource(this, R.array.noise_levels, android.R.layout.simple_spinner_item);
        ArrayAdapter<CharSequence> adapterLight = ArrayAdapter.createFromResource(this, R.array.light_levels, android.R.layout.simple_spinner_item);

        adapterNoise.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapterLight.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerNoise.setAdapter(adapterNoise);
        spinnerLight.setAdapter(adapterLight);

        spinnerNoise.setSelection(noise);
        spinnerLight.setSelection(light);

        recyclerView = findViewById(R.id.recyclerViewRecommendations);
        recyclerView.setHasFixedSize(true);

        backendServices = new BackendServices();

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        restaurantRef = databaseReference.child("restaurants");

        loadRestaurantData();

        spinnerLight.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
//                Log.i("last", String.valueOf(spinnerNoise.getCount()));
//                Log.i("last", String.valueOf(position));
                double noiseSpinVal;
                double lightSpinVal;
                if(spinnerNoise.getSelectedItemPosition()+1 != spinnerNoise.getCount()) {
                    noiseSpinVal = Double.parseDouble(spinnerNoise.getSelectedItem().toString());
                } else {
                    noiseSpinVal = Double.valueOf(1000000000);
                }
                if(spinnerLight.getSelectedItemPosition()+1 != spinnerLight.getCount()) {
                    lightSpinVal = Double.parseDouble(spinnerLight.getSelectedItem().toString());
                } else {
                    lightSpinVal = Double.valueOf(1000000000);
                }
                onFilterChanged(noiseSpinVal, lightSpinVal);
//                Toast.makeText(getBaseContext(), "You select " + spinnerLight.getSelectedItem(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        spinnerNoise.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
//                Log.i("last", String.valueOf(spinnerNoise.getSelectedItemPosition()));
//                Log.i("last", String.valueOf(spinnerNoise.getCount()));
                double noiseSpinVal;
                double lightSpinVal;
                if(spinnerNoise.getSelectedItemPosition()+1 != spinnerNoise.getCount()) {
                    noiseSpinVal = Double.parseDouble(spinnerNoise.getSelectedItem().toString());
                } else {
                    noiseSpinVal = Double.valueOf(1000000000);
                }
                if(spinnerLight.getSelectedItemPosition()+1 != spinnerLight.getCount()) {
                    lightSpinVal = Double.parseDouble(spinnerLight.getSelectedItem().toString());
                } else {
                    lightSpinVal = Double.valueOf(1000000000);
                }
                onFilterChanged(noiseSpinVal, lightSpinVal);
//                Toast.makeText(getBaseContext(), "You select " + spinnerLight.getSelectedItem(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }

        });
//        addRestaurant("Wimu", 4.5, 3.1, 9.4);
//        addRestaurant("wimukthi", 5.0, 7.3, 8.8);
//        addRestaurant("rajapaksha", 5.4, 8.5, 7.3);
//        addRestaurant("raj", 8.5, 1.3, 4.9);
    }

    public void loadRestaurantData() {

        restaurantRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Iterable<DataSnapshot> arrLightWithTimeStamp = null;
                Iterable<DataSnapshot> arrNoiseWithTimeStamp = null;
                List<String> arrNames = new ArrayList<>();
                SensorModel sensorModel;

                Log.i("time--11", "0");
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    List<SensorModel> sensorModelsForLight = new ArrayList<>();
                    List<SensorModel> sensorModelsForNoise = new ArrayList<>();

                    Log.i("time--00", "0");
//                    Log.i("Object--", String.valueOf(postSnapshot.child("light")));
                    String nameSensor = postSnapshot.child("name").getValue().toString();
                    arrLightWithTimeStamp = postSnapshot.child("light").getChildren();
                    arrNoiseWithTimeStamp = postSnapshot.child("noise").getChildren();
                    arrNames.add(postSnapshot.child("name").getValue().toString());



                    while (arrLightWithTimeStamp.iterator().hasNext()) {
                        Log.i("time--==", "0");
                        DataSnapshot snapshotLight = arrLightWithTimeStamp.iterator().next();
                        if (!(snapshotLight.child("light").getValue().equals("0.0"))) {
                            SensorModel lightTemp = new SensorModel(
                                    nameSensor,
                                    Double.parseDouble(String.valueOf(snapshotLight.child("light").getValue())),
                                    Double.valueOf(0),
                                    Timestamp.valueOf(String.valueOf(snapshotLight.child("timeStamp").getValue()))
                            );
                            sensorModelsForLight.add(lightTemp);
                        }
                    }
                    while (arrNoiseWithTimeStamp.iterator().hasNext()) {
                        Log.i("time----", "0");
                        DataSnapshot snapshotNoise = arrNoiseWithTimeStamp.iterator().next();
                        if ((!(snapshotNoise.child("noise").getValue().equals("-Infinity"))) && (!(snapshotNoise.child("noise").getValue().equals("0.0")))) {
                            Log.i("timestamp--", "Here");
                            SensorModel noiseTemp = new SensorModel(
                                    nameSensor,
                                    Double.valueOf(0),
                                    Double.parseDouble(String.valueOf(snapshotNoise.child("noise").getValue())),
                                    Timestamp.valueOf(String.valueOf(snapshotNoise.child("timeStamp").getValue()))
                            );
                            sensorModelsForNoise.add(noiseTemp);
                        }
                        Log.i("timestamp--", String.valueOf((snapshotNoise.child("noise").getValue())));
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
                    Log.i("time---------", String.valueOf(restTime.getLightList().size()));
                    restaurantWithTimestampList.add(restTime);
                    Log.i("time--++", "1");
                }
                listCompleted = true;
                Log.i("time--++--", "1");
                Log.i("time--++--", String.valueOf(restaurantWithTimestampList.size()));
//                restaurantsForUi = restaurantWithTimestampList;
                initializedUiList = true;
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.i("time--1---1", "0");
            }
        });
//        DatabaseReference restaurantRef = firebaseDatabase.getReference().child("restaurants");
//
//        -----------------------------
//        restaurantRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
//                    Restaurant rest = new Restaurant(
//                            postSnapshot.getKey(),
//                            Double.parseDouble(postSnapshot.child("noise").getValue().toString()),
//                            Double.parseDouble(postSnapshot.child("light").getValue().toString()),
//                            Double.parseDouble(postSnapshot.child("rating").getValue().toString()),
//                            Float.parseFloat(postSnapshot.child("longitude").getValue().toString()),
//                            Float.parseFloat(postSnapshot.child("latitude").getValue().toString())
//                    );
//                    restaurantsList.add(rest);
//                }
//                Log.i("Size++", String.valueOf(restaurantsList.size()));
//                updateRecyclerView();
////                Log.i("Size", String.valueOf(restaurantsList.size()));
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//                System.out.println("The read failed: " + databaseError.getCode());
//            }
//        });
//
//        TODO - done
//        backendServices.getAllRestaurantDetailsForRecommendation();
//        Log.i("returnList--", String.valueOf(returnList.size()));
//        for (RestaurantWithTimestamp restaurantWithTimestamp: returnList) {
//            Log.i("returnList--", restaurantWithTimestamp.getName());
//        }
//        ----------------------------------

    }

//    public void updateRecyclerView() {
////        while (true) {
//        Log.i("time--000", String.valueOf(restaurantWithTimestampList.size()));
//        if (initializedUiList) {
//            RecommendationActivity.this.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    Log.i("Size", String.valueOf(restaurantsForUi.size()));
//                    restaurantAdapter = new RestaurantAdapter(restaurantsForUi);
//                    recyclerView.setAdapter(restaurantAdapter);
//                    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(RecommendationActivity.this);
//                    recyclerView.setLayoutManager(layoutManager);
//                    recyclerView.setHasFixedSize(true);
//                }
//            });
////                break;
//        }
////        }
//
//    }

    public void updateRecyclerView(final List<AdapterModel> adapterModels) {
//        Log.i("time--000", String.valueOf(restaurantWithTimestamps.size()));
        if (initializedUiList) {
            RecommendationActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i("Size", String.valueOf(adapterModels.size()));
                    restaurantAdapter = new RestaurantAdapter(adapterModels);
                    recyclerView.setAdapter(restaurantAdapter);
                    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(RecommendationActivity.this);
                    recyclerView.setLayoutManager(layoutManager);
                    recyclerView.setHasFixedSize(true);
                }
            });
        }
    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
//        Toast.makeText(parent.getContext(), "OnItemSelectedListener : " + parent.getItemAtPosition(pos).toString(), Toast.LENGTH_SHORT).show();
    }

    public void onFilterChanged(double noise, double light) {
        restaurantsForUi.clear();
        Log.i("Size--", String.valueOf(restaurantWithTimestampList.size()));
        restaurantsForUi.clear();
        for (RestaurantWithTimestamp restaurant: restaurantWithTimestampList) {
            Log.i("size-----", restaurant.getName());
            float lightSum = 0f;
            for (SensorModel lightVal: restaurant.getLightList()) {
                lightSum += lightVal.getLight();
            }
            Log.i("size-----", String.valueOf(lightSum));
            float noiseSum = 0f;
            for (SensorModel noiseVal: restaurant.getNoiseList()) {
                noiseSum += noiseVal.getNoise();
            }
            Log.i("size-----", String.valueOf(noiseSum));
            if (noise>=noiseSum && light >= lightSum) {
                AdapterModel adapterModel = new AdapterModel(
                        restaurant.getName(),
                        lightSum/restaurant.getLightList().size(),
                        noiseSum/restaurant.getNoiseList().size(),
                        1.5,
                        null,
                        restaurant.getLongitude(),
                        restaurant.getLatitude()
                );
                restaurantsForUi.add(adapterModel);
            }

//            Log.i("dataaaaaaaaa", String.valueOf(restaurantsList.size()));
//            Log.i("dataaaaaaaaa", restaurant.getName());
//            Log.i("dataaaaaaaaa-----", String.valueOf(restaurant.getLight()));

//            ------------------------------
//
//            if(restaurant.getNoise() <= noise && restaurant.getLight() <= light) {
//                restaurantsForUi.add(restaurant);
////                Log.i("dataaaaaaaaa", restaurant.getName());
//            }
//
//            ------------------------------
        }
        updateRecyclerView(restaurantsForUi);
//        Log.i(restaurantsForUi);
    }



    public void addRestaurant(String name, double noise, double light, double rating) {
        DatabaseReference restaurantRef = firebaseDatabase.getReference().child("restaurants");
        DatabaseReference newRestaurantRef = restaurantRef.child(name);
        Map<String, Double> addRestaurantData = new HashMap<>();
        addRestaurantData.put("noise", noise);
        addRestaurantData.put("light", light);
        addRestaurantData.put("rating", rating);

        newRestaurantRef.setValue(addRestaurantData);
    }
}
