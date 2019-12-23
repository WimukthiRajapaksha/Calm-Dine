package com.example.calmdine;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.calmdine.models.Restaurant;
import com.google.android.gms.maps.model.PolylineOptions;

import java.security.PublicKey;
import java.util.ArrayList;

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.MyViewHolder> {
    private ArrayList<Restaurant> mRestaurants = new ArrayList<Restaurant>();
    private Context mContext;

    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView txtNoiseValue;
        public TextView txtLightValue;
        public TextView txtRestaurantName;
        public TextView txtRateValue;
        public RatingBar ratingBar;

        public MyViewHolder(View view) {
            super(view);
            this.txtLightValue = view.findViewById(R.id.txtLightValue);
            this.txtNoiseValue = view.findViewById(R.id.txtNoiseValue);
            this.ratingBar = view.findViewById(R.id.ratingBar);
            this.txtRestaurantName = view.findViewById(R.id.txtRestaurantName);
            this.txtRateValue = view.findViewById(R.id.txtRateValue);
        }

        @Override
        public void onClick(View view) {
//            Log.i("Click", "event triggered");
//            Log.i("values", txtRestaurantName.toString());
        }
    }

    public RestaurantAdapter(ArrayList<Restaurant> restaurant) {
        mRestaurants = restaurant;
    }

    @Override
    public RestaurantAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        LinearLayout recommendationListLayoutLinearLayout = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_recommendation_item, parent, false);
        MyViewHolder vh = new MyViewHolder(recommendationListLayoutLinearLayout);
        return vh;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final Restaurant currentRestaurant = mRestaurants.get(position);
        holder.txtRestaurantName.setText(currentRestaurant.getName());
        holder.txtLightValue.setText(String.valueOf(currentRestaurant.getLight()));
        holder.txtNoiseValue.setText(String.valueOf(currentRestaurant.getNoise()));
        holder.ratingBar.setRating(Float.parseFloat(String.valueOf(currentRestaurant.getRating())));
        holder.txtRateValue.setText("("+currentRestaurant.getRating()+")");

        LayerDrawable stars = (LayerDrawable) holder.ratingBar.getProgressDrawable();
        stars.getDrawable(2).setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(0).setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(1).setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                PolylineOptions rectOptions = new PolylineOptions()
                Uri uri = Uri.parse("geo:"+currentRestaurant.getLongitude()+","+currentRestaurant.getLatitude() + "?q=" + Uri.encode(currentRestaurant.getName()));
//                Uri uri = Uri.parse("google.navigation:q=" + Uri.encode(currentRestaurant.getName()));
                Log.i("uri", String.valueOf(uri));
                Uri gmmIntentUri = uri;
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                mContext.startActivity(mapIntent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mRestaurants.size();
    }
}
