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

import com.example.calmdine.models.Restaurant;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RecommendationActivity extends AppCompatActivity {

    Spinner spinnerNoise;
    Spinner spinnerLight;

    private RecyclerView recyclerView;
    private RestaurantAdapter restaurantAdapter;
    ArrayList<Restaurant> restaurantsList;
    ArrayList<Restaurant> restaurantsForUi;

    FirebaseDatabase firebaseDatabase;

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
                Toast.makeText(getBaseContext(), "You select " + spinnerLight.getSelectedItem(), Toast.LENGTH_LONG).show();
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
                Toast.makeText(getBaseContext(), "You select " + spinnerLight.getSelectedItem(), Toast.LENGTH_LONG).show();
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
        DatabaseReference restaurantRef = firebaseDatabase.getReference().child("restaurants");
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
                Log.i("Size++", String.valueOf(restaurantsList.size()));
                updateRecyclerView();
//                Log.i("Size", String.valueOf(restaurantsList.size()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });

    }

    public void updateRecyclerView() {
        RecommendationActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i("Size", String.valueOf(restaurantsForUi.size()));
                restaurantAdapter = new RestaurantAdapter(restaurantsForUi);
                recyclerView.setAdapter(restaurantAdapter);
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(RecommendationActivity.this);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setHasFixedSize(true);
            }
        });
    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        Toast.makeText(parent.getContext(), "OnItemSelectedListener : " + parent.getItemAtPosition(pos).toString(), Toast.LENGTH_SHORT).show();
    }

    public void onFilterChanged(double noise, double light) {
        restaurantsForUi.clear();
        Log.i("Size--", String.valueOf(restaurantsList.size()));
        for (Restaurant restaurant: restaurantsList) {
//            Log.i("dataaaaaaaaa", String.valueOf(restaurantsList.size()));
//            Log.i("dataaaaaaaaa", restaurant.getName());
//            Log.i("dataaaaaaaaa-----", String.valueOf(restaurant.getLight()));
            if(restaurant.getNoise() <= noise && restaurant.getLight() <= light) {
                restaurantsForUi.add(restaurant);
//                Log.i("dataaaaaaaaa", restaurant.getName());
            }
        }
        updateRecyclerView();
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
