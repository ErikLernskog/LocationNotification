package com.lernskog.erik.locationnotification;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class LocationNotificationActivity extends FragmentActivity implements OnMapReadyCallback {

    boolean mRequestingLocationUpdates;
    private LocationRequest mLocationRequest;
    private GoogleMap mGoogleMap;
    private LocationCallback mLocationCallback;
    private FusedLocationProviderClient mFusedLocationClient;
    private final static String REQUESTING_LOCATION_UPDATES_KEY = "REQUESTING_LOCATION_UPDATES_KEY";
    private LatLng mLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        print("onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_notification);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        createLocationRequest();
        createLocationCallback();
        updateValuesFromBundle(savedInstanceState);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        print("onMapReady");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mGoogleMap = googleMap;
        mLatLng = new LatLng(-34, 151);
        mGoogleMap.addMarker(new MarkerOptions().position(mLatLng).title("Marker in Sydney").draggable(true));
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(mLatLng));
        mGoogleMap.setMyLocationEnabled(true);
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
        mGoogleMap.getUiSettings().setCompassEnabled(true);
        mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
    }

    @Override
    protected void onResume() {
        print("onResume");
        super.onResume();
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        print("onPause");
        super.onPause();
        stopLocationUpdates();
    }

    private void stopLocationUpdates() {
        print("stopLocationUpdates");
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    protected void createLocationCallback() {
        print("createLocationCallback");
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    LatLng newLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(newLatLng));
                }
            }
        };
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        print("onSaveInstanceState");
        outState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, mRequestingLocationUpdates);
        super.onSaveInstanceState(outState);
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        print("updateValuesFromBundel");
        if (savedInstanceState == null) {
            return;
        }
        if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
            mRequestingLocationUpdates = savedInstanceState.getBoolean(REQUESTING_LOCATION_UPDATES_KEY);
        }
    }

    protected void createLocationRequest() {
        print("createLocationRequest");
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mRequestingLocationUpdates = true;
    }

    private void startLocationUpdates() {
        print("startLocationUpdates");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
    }

    public void print(final String message) {
        Log.d("LocationNotificationActivity", message);
    }
}
