package com.example.calmdine;

import androidx.activity.OnBackPressedCallback;
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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

    double locationDataLong;
    double locationDataLat;

    ArrayList<String> nearbyRestaurantsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommendation);

        nearbyRestaurantsList = new ArrayList<>();

        Intent intent = getIntent();
        int noise = intent.getIntExtra("noise", 0);
        int light = intent.getIntExtra("light", 0);

        locationDataLong = intent.getDoubleExtra("locationDataLong", 100);
        locationDataLat = intent.getDoubleExtra("locationDataLat", 100);

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

        backendServices = new BackendServices(this);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        restaurantRef = databaseReference.child("restaurants");

        loadRestaurantData();

        final String urlRestaurants = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + locationDataLat + "," + locationDataLong + "&radius=1000&type=restaurant&key=" + getResources().getString(R.string.google_maps_key);
        RequestQueue queueRestaurants = Volley.newRequestQueue(RecommendationActivity.this);

        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, urlRestaurants, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {
                        // display response
                        Log.d("Response_01", urlRestaurants);
                        Log.d("Response_01", response.toString());

                        try {
                            JSONArray resultsArray = ((JSONArray) response.getJSONArray("results"));
                            for (int i=0; i< resultsArray.length(); i++) {
                                String name = resultsArray.getJSONObject(i).getString("name");
//                                Log.d("Response_01-name", name);
                                nearbyRestaurantsList.add(name);
//                                Log.d("Response_01-", String.valueOf(nearbyRestaurantsList.size()));
                            }
                            Log.d("Response_01-03", String.valueOf(nearbyRestaurantsList.size()));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error.Response", String.valueOf(error));
                    }
                }
        );

        Log.d("Response_01-02", String.valueOf(nearbyRestaurantsList.size()));

// add it to the RequestQueue
        queueRestaurants.add(getRequest);

        spinnerLight.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
//                Log.i("last", String.valueOf(spinnerNoise.getCount()));
//                Log.i("last", String.valueOf(position));
                int noiseSpinVal;
                int lightSpinVal;
//                if(spinnerNoise.getSelectedItemPosition()+1 != spinnerNoise.getCount()) {
//                    noiseSpinVal = spinnerNoise.getSelectedItemPosition()+1;
//                } else {
//                    noiseSpinVal = Double.valueOf(1000000000);
//                }
//                if(spinnerLight.getSelectedItemPosition()+1 != spinnerLight.getCount()) {
//                    lightSpinVal = spinnerLight.getSelectedItemPosition()+1;
//                } else {
//                    lightSpinVal = Double.valueOf(1000000000);
//                }
                lightSpinVal = spinnerLight.getSelectedItemPosition();
                noiseSpinVal = spinnerNoise.getSelectedItemPosition();
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
                int noiseSpinVal;
                int lightSpinVal;
//                if(spinnerNoise.getSelectedItemPosition()+1 != spinnerNoise.getCount()) {
//                    noiseSpinVal = Double.parseDouble(spinnerNoise.getSelectedItem().toString());
//                } else {
//                    noiseSpinVal = Double.valueOf(1000000000);
//                }
//                if(spinnerLight.getSelectedItemPosition()+1 != spinnerLight.getCount()) {
//                    lightSpinVal = Double.parseDouble(spinnerLight.getSelectedItem().toString());
//                } else {
//                    lightSpinVal = Double.valueOf(1000000000);
//                }
                lightSpinVal = spinnerLight.getSelectedItemPosition();
                noiseSpinVal = spinnerNoise.getSelectedItemPosition();
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

                restaurantWithTimestampList.clear();
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
                    try {
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
                        Log.i("time---------", String.valueOf(restTime.getRating()));
                        restaurantWithTimestampList.add(restTime);
                        Log.i("time--++", "1");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

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
        Log.i("time--000", String.valueOf(adapterModels.size()));
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

    public void onFilterChanged(int noise, int light) {
        restaurantsForUi.clear();
        Log.i("Size--", noise + " " + light);
        Log.i("Size--", String.valueOf(restaurantWithTimestampList.size()));





        Log.d("Response_01-05", String.valueOf(nearbyRestaurantsList.size()));





        for (RestaurantWithTimestamp restaurant: restaurantWithTimestampList) {


            if (nearbyRestaurantsList.indexOf(restaurant.getName()) != -1) {
                Log.i("size-----", restaurant.getName());
                Log.i("size-----", String.valueOf(restaurant.getRating()));
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



//            if (noise>=noiseSum && light >= lightSum) {
//                AdapterModel adapterModel = new AdapterModel(
//                        restaurant.getName(),
//                        lightSum/restaurant.getLightList().size(),
//                        noiseSum/restaurant.getNoiseList().size(),
//                        restaurant.getRating(),
//                        null,
//                        restaurant.getLongitude(),
//                        restaurant.getLatitude()
//                );
//                restaurantsForUi.add(adapterModel);
//            }

                float lightSumAvg = lightSum/restaurant.getLightList().size();
                float noiseSumAvg = noiseSum/restaurant.getNoiseList().size();
//-----------------------
                if (noise == 0 && light == 0) {
                    if (noiseSumAvg < 33 && lightSumAvg < 80) {
                        AdapterModel adapterModel = new AdapterModel(
                                restaurant.getName(),
                                lightSumAvg,
                                noiseSumAvg,
                                restaurant.getRating(),
                                null,
                                restaurant.getLongitude(),
                                restaurant.getLatitude()
                        );
                        restaurantsForUi.add(adapterModel);
                    }
                } else if (noise == 0 && light == 1) {
                    if (noiseSumAvg < 33 && lightSumAvg > 80 && lightSumAvg < 500) {
                        AdapterModel adapterModel = new AdapterModel(
                                restaurant.getName(),
                                lightSumAvg,
                                noiseSumAvg,
                                restaurant.getRating(),
                                null,
                                restaurant.getLongitude(),
                                restaurant.getLatitude()
                        );
                        restaurantsForUi.add(adapterModel);
                    }
                } else if (noise == 0 && light == 2) {
                    if (noiseSumAvg < 33 && lightSumAvg > 500) {
                        AdapterModel adapterModel = new AdapterModel(
                                restaurant.getName(),
                                lightSumAvg,
                                noiseSumAvg,
                                restaurant.getRating(),
                                null,
                                restaurant.getLongitude(),
                                restaurant.getLatitude()
                        );
                        restaurantsForUi.add(adapterModel);
                    }
                }
//            --------------------------
                else if (noise == 1 && light == 0) {
                    if (noiseSumAvg > 30 && noiseSumAvg < 75 && lightSumAvg < 80 ) {
                        AdapterModel adapterModel = new AdapterModel(
                                restaurant.getName(),
                                lightSumAvg,
                                noiseSumAvg,
                                restaurant.getRating(),
                                null,
                                restaurant.getLongitude(),
                                restaurant.getLatitude()
                        );
                        restaurantsForUi.add(adapterModel);
                    }
                } else if (noise == 1 && light == 1) {
                    if (noiseSumAvg > 30 && noiseSumAvg < 75 && lightSumAvg > 80 && lightSumAvg < 500 ) {
                        AdapterModel adapterModel = new AdapterModel(
                                restaurant.getName(),
                                lightSumAvg,
                                noiseSumAvg,
                                restaurant.getRating(),
                                null,
                                restaurant.getLongitude(),
                                restaurant.getLatitude()
                        );
                        restaurantsForUi.add(adapterModel);
                    }
                } else if (noise == 1 && light == 2) {
                    if (noiseSumAvg > 30 && noiseSumAvg < 75 && lightSumAvg > 500) {
                        AdapterModel adapterModel = new AdapterModel(
                                restaurant.getName(),
                                lightSumAvg,
                                noiseSumAvg,
                                restaurant.getRating(),
                                null,
                                restaurant.getLongitude(),
                                restaurant.getLatitude()
                        );
                        restaurantsForUi.add(adapterModel);
                    }
                }
//            ---------------------------
                else if (noise == 2 && light == 0) {
                    if (noiseSumAvg > 75 && lightSumAvg < 80) {
                        AdapterModel adapterModel = new AdapterModel(
                                restaurant.getName(),
                                lightSumAvg,
                                noiseSumAvg,
                                restaurant.getRating(),
                                null,
                                restaurant.getLongitude(),
                                restaurant.getLatitude()
                        );
                        restaurantsForUi.add(adapterModel);
                    }
                } else if (noise == 2 && light == 1) {
                    if (noiseSumAvg > 75 && lightSumAvg > 80 && lightSumAvg < 500) {
                        AdapterModel adapterModel = new AdapterModel(
                                restaurant.getName(),
                                lightSumAvg,
                                noiseSumAvg,
                                restaurant.getRating(),
                                null,
                                restaurant.getLongitude(),
                                restaurant.getLatitude()
                        );
                        restaurantsForUi.add(adapterModel);
                    }
                } else if (noise == 2 && light == 2) {
                    if (noiseSumAvg > 75 && lightSumAvg > 500) {
                        AdapterModel adapterModel = new AdapterModel(
                                restaurant.getName(),
                                lightSumAvg,
                                noiseSumAvg,
                                restaurant.getRating(),
                                null,
                                restaurant.getLongitude(),
                                restaurant.getLatitude()
                        );
                        restaurantsForUi.add(adapterModel);
                    }
                }
            }


            Log.d("Response_01-01", String.valueOf(nearbyRestaurantsList.size()));

//            Todo - complete

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

    @Override
    public void onBackPressed() {
        Intent setIntent = new Intent(this, HomeActivity.class);
        Log.i("Intent---", "Here");
        startActivity(setIntent);
        finish();
    }
}
