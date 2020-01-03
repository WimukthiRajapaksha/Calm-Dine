package com.example.calmdine;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.calmdine.Restaurant.DownloadUrl;
import com.example.calmdine.Restaurant.GetNearestPlaceData;
import com.example.calmdine.ServicesFire.BackendServices;
import com.example.calmdine.models.AdapterModel;
import com.example.calmdine.models.Restaurant;
import com.example.calmdine.models.RestaurantWithTimestamp;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResponse;
import com.google.android.gms.location.places.PlacePhotoResponse;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.MyViewHolder> {
    private List<AdapterModel> mRestaurants = new ArrayList<AdapterModel>();
    private Context mContext;

    public GeoDataClient mGeoDataClient;
    public BackendServices backendServices;
    public FirebaseStorage storage;

    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView txtNoiseValue;
        public TextView txtLightValue;
        public TextView txtRestaurantName;
        public TextView txtRateValue;
        public RatingBar ratingBar;
        public ImageView imageView;

        public MyViewHolder(View view) {
            super(view);
            this.txtLightValue = view.findViewById(R.id.txtLightValue);
            this.txtNoiseValue = view.findViewById(R.id.txtNoiseValue);
            this.ratingBar = view.findViewById(R.id.ratingBar);
            this.txtRestaurantName = view.findViewById(R.id.txtRestaurantName);
            this.txtRateValue = view.findViewById(R.id.txtRateValue);
            this.imageView = view.findViewById(R.id.imgRestaurant);
        }

        @Override
        public void onClick(View view) {
//            Log.i("Click", "event triggered");
//            Log.i("values", txtRestaurantName.toString());
        }
    }

    public RestaurantAdapter(List<AdapterModel> restaurant) {
        mRestaurants = restaurant;
        backendServices = new BackendServices(mContext);
        storage = FirebaseStorage.getInstance();
        for (AdapterModel rest: restaurant) {
            Log.i("RestaurantNameIsAdapter", rest.getName());
            Log.i("RestaurantNameIsAdapter", String.valueOf(rest.getRating()));
        }
//        Places.initialize(mContext, mContext.getResources().getString(R.string.google_maps_key));
//        PlacesClient placesClient = Places.createClient(mContext);
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
        final AdapterModel currentRestaurant = mRestaurants.get(position);
        holder.txtRestaurantName.setText(currentRestaurant.getName());
        holder.txtLightValue.setText(String.format("%.3f",(currentRestaurant.getLight())));
        holder.txtNoiseValue.setText(String.format("%.3f",(currentRestaurant.getNoise())));
        holder.ratingBar.setRating(Float.parseFloat(String.valueOf(currentRestaurant.getRating())));
        holder.txtRateValue.setText("("+currentRestaurant.getRating()+")");


        Log.i("RestaurantNameIs", String.valueOf(mRestaurants.get(position).getRating()));
        StorageReference storageReference = storage.getReferenceFromUrl("gs://calmdine.appspot.com/images/" + currentRestaurant.getName());
//        Log.i("RestaurantNameIs", storageReference.getPath());
////
//        Glide.with(mContext).load(storageReference).into(holder.imageView);
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {


                Transformation transformation = new RoundedTransformationBuilder()
                        .borderColor(Color.BLACK)
                        .borderWidthDp(1)
                        .cornerRadiusDp(10)
                        .oval(false)
                        .build();


//                Glide.with(mContext).load(uri).into(holder.imageView);

                Picasso.get().load(uri).fit().transform(transformation).into(holder.imageView);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        });

//        -------------------------------------------------

//        Picasso.get().load("gs://calmdine.appspot.com/images/SEVO CATERERS.jpeg").into(holder.imageView);

//        storage.getReference().child(currentRestaurant.getName()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//            @Override
//            public void onSuccess(Uri uri) {
//                holder.imageView.setImageBitmap(uri.);
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Log.i("StringValue=-=-", e.toString());
//            }
//        });
//        holder.imageView.setImageBitmap();


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
//
//    private String getPlaceImage() {
//
//
////        String url = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=CnRtAAAATLZNl354RwP_9UKbQ_5Psy40texXePv4oAlgP4qNEkdIrkyse7rPXYGd9D_Uj1rVsQdWT4oRz4QrYAJNpFX7rzqqMlZw2h2E2y5IKMUZ7ouD_SlcHxYq1yL4KbKUv3qtWgTK0A6QbGh87GB3sscrHRIQiG2RrmU_jF4tENr9wGS_YxoUSSDrYjWmrNfeEHSGSc3FyhNLlBU&key=" + (mContext.getResources().getString(R.string.google_maps_key));
//        URL url = null;
//        try {
//            url = new URL("https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=CnRtAAAATLZNl354RwP_9UKbQ_5Psy40texXePv4oAlgP4qNEkdIrkyse7rPXYGd9D_Uj1rVsQdWT4oRz4QrYAJNpFX7rzqqMlZw2h2E2y5IKMUZ7ouD_SlcHxYq1yL4KbKUv3qtWgTK0A6QbGh87GB3sscrHRIQiG2RrmU_jF4tENr9wGS_YxoUSSDrYjWmrNfeEHSGSc3FyhNLlBU&key=" + (mContext.getResources().getString(R.string.google_maps_key)));
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        }
//        HttpURLConnection urlConnection = null;
//        try {
//            urlConnection = (HttpURLConnection) url.openConnection();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        try {
//            InputStream in = null;
//            try {
//                in = new BufferedInputStream(urlConnection.getInputStream());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            readStream(in);
//        } finally {
//            urlConnection.disconnect();
//        }
//    }

    @Override
    public int getItemCount() {
        return mRestaurants.size();
    }
}
