package com.example.calmdine;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import com.example.calmdine.Interface.OnCheckingTaskCompleted;
import com.example.calmdine.Interface.OnRetrievingTaskCompleted;
import com.example.calmdine.Restaurant.CheckNearbyPlaceExistence;
import com.example.calmdine.Restaurant.GetNearbyPlacesData;
import com.example.calmdine.Restaurant.GetNearestPlaceData;
import com.example.calmdine.ServicesFire.BackendServices;
import com.example.calmdine.models.Place;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.LocationCallback;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.api.internal.ConnectionCallbacks;
import com.google.android.gms.common.api.internal.OnConnectionFailedListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.os.Looper;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class HomeActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, com.google.android.gms.location.LocationListener, OnRetrievingTaskCompleted, OnCheckingTaskCompleted, ConnectionCallbacks, OnConnectionFailedListener {

    FirebaseDatabase firebaseDatabase;
    FirebaseAuth mAuth;
    DatabaseReference dbRef;

    Spinner spinnerNoise;
    Spinner spinnerLight;

    MapFragment mapFragment;
    private GoogleMap mMap;
    private LocationRequest mLocationRequest;
    int PERMISSION_ID = 204;

    String latTextView;
    String lonTextView;
    Button btnRecommendation;
    Context mContext;

    private SensorManager sensorManager;

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private final int MY_PERMISSIONS_REQUEST_AUDIO = 123;
    private final int FINE_LOCATION_PERMISSION_REQUEST_CODE = 1221;

//    FusedLocationProviderClient mFusedLocationClient;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private boolean mLocationPermissionGranted;
    private boolean mFineLocationPermissionGranted;
    private boolean mMicrophonePermissionGranted;
    protected LocationManager locationManager;
    private BackendServices backendServices;

    private Location currentLocation;
    private static final float DEFAULT_ZOOM = 10f;

    private GoogleApiClient mGoogleApiClient;


    private double currentLatitude, currentLongitude;
    private Location deviceLocation;
    private Place nearestPlace;
    private boolean isNearRestaurant;

    private final static String TAG = "HomeActivity";
    private final static int REQUEST_CHECK_SETTINGS_GPS = 0x1;
    private final static int REQUEST_ID_MULTIPLE_PERMISSIONS = 0x2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        btnRecommendation = findViewById(R.id.btnRecommendation);

        spinnerNoise = findViewById(R.id.spinnerNoise);
        spinnerLight = findViewById(R.id.spinnerLight);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mContext = getApplicationContext();

        firebaseDatabase = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        dbRef = firebaseDatabase.getReference("");

        backendServices = new BackendServices();

        ArrayAdapter<CharSequence> adapterNoise = ArrayAdapter.createFromResource(this, R.array.noise_levels, android.R.layout.simple_spinner_item);
        ArrayAdapter<CharSequence> adapterLight = ArrayAdapter.createFromResource(this, R.array.light_levels, android.R.layout.simple_spinner_item);

        adapterNoise.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapterLight.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerNoise.setAdapter(adapterNoise);
        spinnerLight.setAdapter(adapterLight);
        spinnerNoise.setSelection(adapterNoise.getCount()-1);
        spinnerLight.setSelection(adapterLight.getCount()-1);

        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapView);
        mapFragment.getMapAsync(this);

        mLocationRequest = LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY).setInterval(10 * 1000).setFastestInterval(1 * 1000);

//        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationPermissionGranted = false;
        mFineLocationPermissionGranted = false;

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    Activity#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for Activity#requestPermissions for more details.
//            return;
//        }
//        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        setUPGClient();
        permissionCheckAndRequest();
    }

    public void permissionCheckAndRequest() {
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},MY_PERMISSIONS_REQUEST_AUDIO);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},LOCATION_PERMISSION_REQUEST_CODE);
        } else if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},LOCATION_PERMISSION_REQUEST_CODE);
        } else if ((ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},MY_PERMISSIONS_REQUEST_AUDIO);
        } else {
            mMicrophonePermissionGranted = true;
            mLocationPermissionGranted = true;
            mFineLocationPermissionGranted = true;
            Log.i("permission", "inside");
            startBackgroundProcess();
//            setUPGClient();
            Log.d("call", "call----------");
//            getDeviceLocation();
        }
        Log.i("call", "call-" + mGoogleApiClient.isConnected());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        Log.i("permissions", String.valueOf(grantResults));
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_AUDIO: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0) {
                    for (int permission: grantResults) {
                        if (permission == PackageManager.PERMISSION_GRANTED) {
                            mMicrophonePermissionGranted = true;
                            startBackgroundProcess();
//                            setUPGClient();
                            getDeviceLocation();
//                            AsyncTaskRunner asyncTaskRunner = new AsyncTaskRunner(sensorManager);
//                            asyncTaskRunner.execute();
                        } else {
                            Toast.makeText(this, "permission denied by user", Toast.LENGTH_LONG).show();
                        }
                    }
//                    initializePlayerAndStartRecording();
                } else {
                    Toast.makeText(this, "permission denied by user", Toast.LENGTH_LONG).show();
                }
                return;
            }
            case LOCATION_PERMISSION_REQUEST_CODE : {
                if(grantResults.length > 0) {
                    for(int permission: grantResults) {
                        if(permission == PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionGranted = true;
                            mFineLocationPermissionGranted = true;
                            startBackgroundProcess();
//                            setUPGClient();
                            getDeviceLocation();
                            Log.d("permission", "onRequestPermissionsResult: Permission Granted");
                            return;
                        }
                    }
                }
            }
//            case FINE_LOCATION_PERMISSION_REQUEST_CODE: {
//                if(grantResults.length > 0) {
//                    for(int permission: grantResults) {
//                        if(permission == PackageManager.PERMISSION_GRANTED) {
//                            mFineLocationPermissionGranted = true;
//                            startBackgroundProcess();
//                            Log.d("permission", "onRequestPermissionsResult: Permission Granted");
//                            return;
//                        }
//                    }
//                }
//            }
        }
    }

    private void setUPGClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, 0, this)
                .addConnectionCallbacks(HomeActivity.this)
                .addOnConnectionFailedListener(HomeActivity.this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
        Log.i("calling", "call-02----02 - Connected" + mGoogleApiClient.isConnected());
    }


    public void startBackgroundProcess() {
        Log.i("permission", "background");
        Log.i("permission", String.valueOf(mLocationPermissionGranted));
        Log.i("permission", String.valueOf(mMicrophonePermissionGranted));
        if(mLocationPermissionGranted && mMicrophonePermissionGranted && mFineLocationPermissionGranted) {
            AsyncTaskRunner asyncTaskRunner = new AsyncTaskRunner(sensorManager, mContext);
            asyncTaskRunner.execute();
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

    public void onRecommendationList(View view) {
        Intent intent = new Intent(HomeActivity.this, RecommendationActivity.class);
        intent.putExtra("noise", spinnerNoise.getSelectedItemPosition());
        intent.putExtra("light", spinnerLight.getSelectedItemPosition());
        startActivity(intent);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Map is ready...", Toast.LENGTH_SHORT).show();
        mMap = googleMap;
        if(mLocationPermissionGranted) {
            getDeviceLocation();
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i("calling", "call-0001");

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
            mMap.addMarker(markerOptions);

            getNearbyRestaurants();
            isDeviceNearbyRestaurant();

        }
    }

    private void getNearbyRestaurants() {
        Log.i("calling", "call-01");
        StringBuilder stringBuilder = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        stringBuilder.append("location=" + currentLatitude + "," + currentLongitude);
        stringBuilder.append("&radius=1000");
        stringBuilder.append("&type=restaurant");
        stringBuilder.append("&key=" + getResources().getString(R.string.google_maps_key));

        String url = stringBuilder.toString();

        Object dataTransfer[] = new Object[2];
        dataTransfer[0] = mMap;
        dataTransfer[1] = url;

        GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();
        getNearbyPlacesData.execute(dataTransfer);
    }

    private void getNearestRestaurant() {
        Log.i("calling", "call-02");
        StringBuilder stringBuilder = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        stringBuilder.append("location=" + currentLatitude + "," + currentLongitude);
        stringBuilder.append("&rankby=distance");
        stringBuilder.append("&type=restaurant");
        stringBuilder.append("&key=" + getResources().getString(R.string.google_maps_key));

        String url = stringBuilder.toString();

        Object dataTransfer[] = new Object[2];
        dataTransfer[0] = mMap;
        dataTransfer[1] = url;

        GetNearestPlaceData getNearestPlaceData = new GetNearestPlaceData(this);
        getNearestPlaceData.execute(dataTransfer);
    }

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
        stringBuilder.append("&key=" + getResources().getString(R.string.google_maps_key));

        String url = stringBuilder.toString();

        Object dataTransfer[] = new Object[2];
        dataTransfer[0] = mMap;
        dataTransfer[1] = url;

        CheckNearbyPlaceExistence checkNearbyPlaceExistence = new CheckNearbyPlaceExistence(this);
        checkNearbyPlaceExistence.execute(dataTransfer);
    }

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
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onPause() {
        Log.i("calling", "call-02----01-disconnected");
        super.onPause();
        mGoogleApiClient.stopAutoManage(HomeActivity.this);
        mGoogleApiClient.disconnect();
    }

    private void getDeviceLocation() {
        Log.i("calling", "call-02----");
//        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
//        try {
//            if (mLocationPermissionGranted) {
//                Log.i("calling", "call-02----01");
//                Task location = mFusedLocationProviderClient.getLastLocation();
//                location.addOnCompleteListener(new OnCompleteListener() {
//                    @Override
//                    public void onComplete(@NonNull Task task) {
//                        if (task.isSuccessful()) {
//                            Location currentLocation = (Location) task.getResult();
//                            if (currentLocation != null) {
//                                moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM);
//                            }
//                        }
//                    }
//                });
//            }
//        } catch (SecurityException e) {
//        }
        if(mGoogleApiClient != null) {
            Log.i("calling", "call-02----02 - connection-checker");
            Log.i("calling", String.valueOf("call-02----02 - connection-checker ----- " + mGoogleApiClient.isConnecting()));
            Log.i("calling", String.valueOf("call-02----02 - connection-checker ----- " + mGoogleApiClient.isConnected()));
            if (mGoogleApiClient.isConnected()) {
                Log.i("calling", "call-02----02-01");
                int permissionLocation = ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
                if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
                    deviceLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    LocationRequest locationRequest = new LocationRequest();

                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
                    builder.setAlwaysShow(true);
                    LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, (com.google.android.gms.location.LocationListener) HomeActivity.this);
                    PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
                    result.setResultCallback(new ResultCallback<LocationSettingsResult>() {

                        @Override
                        public void onResult(LocationSettingsResult result) {
                            final Status status = result.getStatus();
                            switch (status.getStatusCode()) {
                                case LocationSettingsStatusCodes.SUCCESS:
                                    // All location settings are satisfied.
                                    // You can initialize location requests here.
                                    int permissionLocation = ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
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
                                        status.startResolutionForResult(HomeActivity.this, REQUEST_CHECK_SETTINGS_GPS);
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
            Log.i("calling", String.valueOf("call-02----02 - connection-checker == " + mGoogleApiClient.isConnected()));
//            if (mGoogleApiClient.isConnected()) {
//            }
        }
    }

    public void innerMethod() {
    }

}
