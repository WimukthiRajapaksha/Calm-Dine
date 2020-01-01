package com.example.calmdine;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.calmdine.Interface.OnCheckingTaskCompleted;
import com.example.calmdine.Interface.OnRetrievingTaskCompleted;
import com.example.calmdine.Restaurant.CheckNearbyPlaceExistence;
import com.example.calmdine.Restaurant.GetNearbyPlacesData;
import com.example.calmdine.Restaurant.GetNearestPlaceData;
import com.example.calmdine.ServicesFire.BackendServices;
import com.example.calmdine.models.Place;
import com.example.calmdine.models.Restaurant;
import com.example.calmdine.models.SensorModel;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.exp;

public class AsyncTaskRunner extends AsyncTask<Void, Void, Void> implements SensorEventListener, OnMapReadyCallback, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener, OnRetrievingTaskCompleted, OnCheckingTaskCompleted {
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


    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private double currentLatitude, currentLongitude;
    private Location deviceLocation;
    private Place nearestPlace;
    private boolean isNearRestaurant;

    private final static String TAG = "AsyncTaskActivity";
    private final static int REQUEST_CHECK_SETTINGS_GPS = 0x1;
    private final static int REQUEST_ID_MULTIPLE_PERMISSIONS = 0x2;

    private MediaRecorder mRecorder = null;
    private static double mEMA = 0.0;
    static final private double EMA_FILTER = 0.6;


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
//  ---------------------------------------------------noise
//    public void start() {
////        minSize= AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
////        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, 8000,AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,minSize);
////        ar.startRecording();
//
//        if (mRecorder == null) {
//            mRecorder = new MediaRecorder();
//            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
//            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
//            mRecorder.setOutputFile("/dev/null");
//            try {
//                mRecorder.prepare();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            mRecorder.start();
//            mRecorder.getMaxAmplitude();
//        }
//    }
//
//    public void stop() {
//        if (ar != null) {
//            ar.stop();
//        }
//    }
//
//    public double getNoiseLevel() {
//        if (mRecorder != null) {
//            int amplitude = mRecorder.getMaxAmplitude();
//            return (20 * Math.log10(amplitude / 0.1));
//        } else {
//            return 0;
//        }
//    }


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
//    ----------------noise end

//    public void start() throws IOException {
//        if (mRecorder == null) {
//            mRecorder = new MediaRecorder();
//            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
//            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
//            mRecorder.setOutputFile("/dev/null");
//            mRecorder.prepare();
//            mRecorder.start();
//            mRecorder.getMaxAmplitude();
//        }
//    }
//
//    public void stop() {
//        if (mRecorder != null) {
//            mRecorder.stop();
//            mRecorder.release();
//            mRecorder = null;
//        }
//    }
//




    public void start() {
        if (mRecorder == null) {
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setOutputFile("/dev/null");
            try {
                mRecorder.prepare();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }catch (SecurityException e) {
                e.printStackTrace();
            }
            try {
                mRecorder.start();
            }catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }
//
    public void stop() {
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.reset();
            mRecorder.release();
            mRecorder = null;
        }
    }

    public double getAmplitude() {
        if (mRecorder != null)
            return  (mRecorder.getMaxAmplitude());
        else
            return 0;
    }

    public double soundDb() {
        double amp =  getAmplitude();
        mEMA = EMA_FILTER * amp + (1.0 - EMA_FILTER) * mEMA;
        return  20 * Math.log10(mEMA / (10 * exp(-7)) );
    }

//    -------------------------------------------------


    @Override
    protected Void doInBackground(Void... voids) {
        getDeviceLocation();
        while (true) {
            try {
                Thread.sleep(5000);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                Log.i("background", "result");
                try {
                    start();
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
//                    stop();
                    double noiseValue = soundDb();
                    Log.i("Amplitude", String.valueOf(noiseValue));
                }
//                Log.i("Amplitude", String.valueOf(getAmplitude()));
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
                            Log.i("eeee", String.valueOf(res.getLatitude()));
                            Log.i("eeee-", String.valueOf((mBackgroundLocationListener.getLocation())));
                            if (res != null && mBackgroundLocationListener.getLocation() != null) {
                                Log.i("eeeE", String.valueOf(round(res.getLatitude())));
                                Log.i("eeEe", String.valueOf(round(mBackgroundLocationListener.getLocation().getLatitude())));
                                Log.i("eEee", String.valueOf(round(res.getLongitude())));
                                Log.i("Eeee", String.valueOf(round(mBackgroundLocationListener.getLocation().getLongitude())));
                                if ((round(res.getLatitude()) == round(mBackgroundLocationListener.getLocation().getLatitude())) && (round(res.getLongitude()) == round(mBackgroundLocationListener.getLocation().getLongitude()))) {
                                    Log.i("Eeeeeeeeeeeeeeeeee", String.valueOf(avgLight) + "  " + new Timestamp(System.currentTimeMillis()));
                                    SensorModel sensorModel = new SensorModel(res.getName(), avgLight, soundDb());
                                    backendServices.addSensorData(sensorModel, res);
                                }
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

//    ------------------------------------------------------------------------------------------------------------

    @Override
    public void onLocationChanged(Location location) {
        Log.i("onLocationChanged", "Fired");
        deviceLocation = location;
        if(deviceLocation != null) {
            currentLatitude = deviceLocation.getLatitude();
            currentLongitude = deviceLocation.getLongitude();

            BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.navigation);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLatitude, currentLongitude), 15.0f));
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(new LatLng(currentLatitude, currentLongitude));
            markerOptions.title("You");
            markerOptions.icon(icon);
//            mMap.addMarker(markerOptions);
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(currentLatitude, currentLongitude))
                    .icon(bitmapDescriptorFromVector(mContext, R.drawable.navigation))
                    .title("You"));

            getNearbyRestaurants();
//            isDeviceNearbyRestaurant();

        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void getNearbyRestaurants() {
        Log.i("getNearbyRestaurants", "Fired");
        StringBuilder stringBuilder = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        stringBuilder.append("location=" + currentLatitude + "," + currentLongitude);
        stringBuilder.append("&radius=10000");
        stringBuilder.append("&type=restaurant");
        stringBuilder.append("&key=" + mContext.getResources().getString(R.string.google_maps_key));

        String url = stringBuilder.toString();

        Object dataTransfer[] = new Object[2];
        dataTransfer[0] = mMap;
        dataTransfer[1] = url;

        GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();
        getNearbyPlacesData.execute(dataTransfer);

        isDeviceNearbyRestaurant();
    }

    private void getNearestRestaurant() {
        StringBuilder stringBuilder = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        stringBuilder.append("location=" + currentLatitude + "," + currentLongitude);
        stringBuilder.append("&rankby=distance");
        stringBuilder.append("&type=restaurant");
        stringBuilder.append("&key=" + mContext.getResources().getString(R.string.google_maps_key));

        String url = stringBuilder.toString();

        Object dataTransfer[] = new Object[2];
        dataTransfer[0] = mMap;
        dataTransfer[1] = url;

        GetNearestPlaceData getNearestPlaceData = new GetNearestPlaceData(AsyncTaskRunner.this);
        getNearestPlaceData.execute(dataTransfer);
    }

    // To get the returning nearest restaurant value from the GetNearestPlaceData (AsyncTasks)
    @Override
    public void onRetrievingTaskCompleted(Place place) {
        nearestPlace = place;
        Log.d(TAG, "onRetrievingTaskCompleted: Nearest Restaurant Name: " + nearestPlace.getName());
    }

    private void isDeviceNearbyRestaurant(){
        StringBuilder stringBuilder = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        stringBuilder.append("location=" + currentLatitude + "," + currentLongitude);
        stringBuilder.append("&radius=15");
        stringBuilder.append("&type=restaurant");
        stringBuilder.append("&key=" + mContext.getResources().getString(R.string.google_maps_key));

        String url = stringBuilder.toString();

        Object dataTransfer[] = new Object[2];
        dataTransfer[0] = mMap;
        dataTransfer[1] = url;

        CheckNearbyPlaceExistence checkNearbyPlaceExistence = new CheckNearbyPlaceExistence(this);
        checkNearbyPlaceExistence.execute(dataTransfer);
    }

    // To get the boolean value to check the existence of a restaurant within the device area
    @Override
    public void onCheckingTaskCompleted(boolean isNearRestaurant) {
        this.isNearRestaurant = isNearRestaurant;
        Log.d(TAG, "onCheckingTaskCompleted: Is Device near a Restaurant: " + this.isNearRestaurant);

        //To get the nearest restaurant if the there's a restaurant in device area
        if(isNearRestaurant) {
            getNearestRestaurant();

        } else {
            Log.d(TAG, "onCheckingTaskCompleted: Device is not in a Restaurant");
        }
    }

    private void getDeviceLocation() {
        if(mGoogleApiClient != null) {
            if (mGoogleApiClient.isConnected()) {
                int permissionLocation = ContextCompat.checkSelfPermission(mContext,
                        Manifest.permission.ACCESS_FINE_LOCATION);
                if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
                    deviceLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    LocationRequest locationRequest = new LocationRequest();

                    //Refreshing location in every 3 seconds
//                    locationRequest.setInterval(3000);
//                    locationRequest.setFastestInterval(3000);

                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
                    builder.setAlwaysShow(true);
                    LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, AsyncTaskRunner.this);
                    PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
                    result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                        @Override
                        public void onResult(LocationSettingsResult result) {
                            final com.google.android.gms.common.api.Status status = result.getStatus();
                            switch (status.getStatusCode()) {
                                case LocationSettingsStatusCodes.SUCCESS:
                                    // All location settings are satisfied.
                                    // You can initialize location requests here.
                                    int permissionLocation = ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION);
                                    if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
                                        deviceLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                                    }
                                    break;
                                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                    // Location settings are not satisfied.
                                    // But could be fixed by showing the user a dialog.
                                    try {
                                        // Show the dialog by calling startResolutionForResult(),
                                        // and check the result in onActivityResult().
                                        // Ask to turn on GPS automatically
                                        status.startResolutionForResult((Activity) mContext, REQUEST_CHECK_SETTINGS_GPS);
                                    } catch (IntentSender.SendIntentException e) {
                                        // Ignore the error.
                                    }
                                    break;
                                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                    // Location settings are not satisfied.
                                    // However, we have no way
                                    // to fix the
                                    // settings so we won't show the dialog.
                                    // finish();
                                    break;
                            }
                        }
                    });
                }
            }
        }
    }

    private void moveCamera(LatLng latLng, float zoom) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    private void addMarker(LatLng latLng, String title) {
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title(title);
        mMap.addMarker(markerOptions);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
//        checkPermission();
    }

//    private void checkPermission() {
//        int permissionLocation = ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION);
//        List<String> listPermission = new ArrayList<>();
//        if(permissionLocation != PackageManager.PERMISSION_GRANTED) {
//            listPermission.add(Manifest.permission.ACCESS_FINE_LOCATION);
//
//            if(!listPermission.isEmpty()) {
//                ActivityCompat.requestPermissions(AsyncTaskRunner.this,
//                        listPermission.toArray(new String[listPermission.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
//            }
//        } else {
//            getDeviceLocation();
//        }
//    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(mContext, "Map is ready...", Toast.LENGTH_SHORT).show();
        mMap = googleMap;
        if(true) {
            getDeviceLocation();
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        }
    }



}
