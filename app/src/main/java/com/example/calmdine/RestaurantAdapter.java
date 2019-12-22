package com.example.calmdine;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.calmdine.models.Restaurant;

import java.security.PublicKey;
import java.util.ArrayList;

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.MyViewHolder> {
    private ArrayList<Restaurant> mRestaurants = new ArrayList<Restaurant>();
    private Context mContext;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
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
    }

    public RestaurantAdapter(ArrayList<Restaurant> restaurant) {
        mRestaurants = restaurant;
    }

    @Override
    public RestaurantAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LinearLayout recommendationListLayoutLinearLayout = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_recommendation_item, parent, false);
//        TextView restaurantName = (TextView) LayoutInflater.from(parent.getContext()).inflate(R.id.txtRestaurantName, parent, false);

//        System.out.println("-----------------------------" + v);
        MyViewHolder vh = new MyViewHolder(recommendationListLayoutLinearLayout);
//        System.out.println("---------++++++++++++--------" +vh);
        return vh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Restaurant currentRestaurant = mRestaurants.get(position);
        holder.txtRestaurantName.setText(currentRestaurant.getName());
        holder.txtLightValue.setText(String.valueOf(currentRestaurant.getLight()));
        holder.txtNoiseValue.setText(String.valueOf(currentRestaurant.getNoise()));
//        holder.ratingBar.setRating(0.3f);
        holder.ratingBar.setRating(Float.parseFloat(String.valueOf(currentRestaurant.getRating())));
        holder.txtRateValue.setText("("+currentRestaurant.getRating()+")");

        LayerDrawable stars = (LayerDrawable) holder.ratingBar.getProgressDrawable();
        stars.getDrawable(2).setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(0).setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(1).setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP);

//        holder.txtNoiseValue.setText(String.valueOf(mRestaurants.get(position).getLight()));
//        holder.txtLightValue.setText(String.valueOf(mRestaurants.get(position).getNoise()));
//        holder.ratingBar.setRating((float) mRestaurants.get(position).getRating());
    }

    @Override
    public int getItemCount() {
        return mRestaurants.size();
    }
}
