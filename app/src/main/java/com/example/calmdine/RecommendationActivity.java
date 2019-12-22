package com.example.calmdine;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

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

        spinnerNoise = findViewById(R.id.spinnerNoise);
        spinnerLight = findViewById(R.id.spinnerLight);
//        restaurantAdapter = new RestaurantAdapter()

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
                            Double.parseDouble(postSnapshot.child("rating").getValue().toString())
                    );
                    restaurantsList.add(rest);
                }
                RecommendationActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Log.i("Size", String.valueOf(restaurantsList.size()));
                        restaurantAdapter = new RestaurantAdapter(restaurantsList);
                        recyclerView.setAdapter(restaurantAdapter);
                        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(RecommendationActivity.this);
                        recyclerView.setLayoutManager(layoutManager);
                        recyclerView.setHasFixedSize(true);
                    }
                });
//                Log.i("Size", String.valueOf(restaurantsList.size()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });

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
