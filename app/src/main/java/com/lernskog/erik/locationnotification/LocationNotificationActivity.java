package com.lernskog.erik.locationnotification;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class LocationNotificationActivity extends FragmentActivity implements View.OnClickListener, OnMapReadyCallback {

    private static final String TAG = LocationNotificationActivity.class.getSimpleName();
    private final static String REQUESTING_LOCATION_UPDATES_KEY = "REQUESTING_LOCATION_UPDATES_KEY";
    private boolean mRequestingLocationUpdates;
    private LocationRequest mLocationRequest;
    private GoogleMap mGoogleMap;
    private LocationCallback mLocationCallback;
    private FusedLocationProviderClient mFusedLocationClient;
    private Button mAddButton;
    public TextView mLatitudeTextView;
    public TextView mLongitudeTextView;
    private User mUser;
    private Positions mPositions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        print("onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_notification);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mAddButton = findViewById(R.id.button_add);
        mAddButton.setOnClickListener(this);
        mLatitudeTextView = findViewById(R.id.textview_latitude);
        mLongitudeTextView = findViewById(R.id.textview_longitude);
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
        mGoogleMap.setMyLocationEnabled(true);
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
        mGoogleMap.getUiSettings().setCompassEnabled(true);
        mGoogleMap.getUiSettings().setZoomControlsEnabled(true);

        mGoogleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                print("onMarkerDragStart");
            }

            @Override
            public void onMarkerDrag(Marker marker) {
                print("onMarkerDrag");
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                print("onMarkerDragEnd id:" + marker.getId());
                //mPosition.mMarker = marker;
            }
        });

        //mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(mLatLng));
        //if (mPosition != null) {
        //    addMarker(mUser.mLatitude, mUser.mLongitude);
        //}
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
                if (mUser == null) {
                    mUser = new User();
                }
                for (Location location : locationResult.getLocations()) {
                    mUser.mLatitude = location.getLatitude();
                    mUser.mLongitude = location.getLongitude();
                    //mLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    //mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(mLatLng));
                    print("onLocationResult " + location.getProvider());
                    verifyDistance();
                }
            }
        };
    }

    private void verifyDistance() {
        print("verifyDistance");
        mPositions.verifiyDistance(mUser);
        //if (mPosition != null) {
        //    print("marker exist");
        //    mPosition.mMarker.setTitle("Latitude " + String.valueOf(mPosition.mLatitude) + " Longitude " + String.valueOf(mPosition.mLongitude));
        //    mPosition.mMarker.setSnippet("Latitude " + String.valueOf(mUser.mLatitude) + " Longitude " + String.valueOf(mUser.mLongitude));
        //} else {
        //    print("marker does not exist");
        //    return;
        //}
//        print("user Latitude " + mUser.mLatitude + " Longitude " + mUser.mLongitude);
//        for (Position position : mPositions) {
//            mLatitudeTextView.setText(String.valueOf(position.mLatitude));
//            mLongitudeTextView.setText(String.valueOf(position.mLongitude));
//            Location markerLocation = new Location("marker");
//            markerLocation.setLongitude(position.mLongitude);
//            markerLocation.setLatitude(position.mLatitude);
//            Location userLocation = new Location("user");
//            userLocation.setLongitude(mUser.mLongitude);
//            userLocation.setLatitude(mUser.mLatitude);
//            float distance = userLocation.distanceTo(markerLocation);
//            print("marker id " + position.mMarker.getId() + " Latitude " + position.mLatitude + " Longitude " + position.mLongitude + " distance " + distance);
//            if (distance < 1.0) {
//                showNotification("Place", String.valueOf(distance));
//                showToast("Place " + String.valueOf(distance));
//            }
//        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        print("onSaveInstanceState");
        outState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, mRequestingLocationUpdates);
        //outState.putDouble("latitude", mPosition.mLatitude);
        //print("store latitude " + mPosition.mLatitude);
        //outState.putDouble("longitude", mPosition.mLongitude);
        //print("store longitude " + mPosition.mLongitude);
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
//        if (savedInstanceState.keySet().contains("latitude")) {
//            if (mPosition == null) {
//                mPosition = new Position();
//            }
//            mPosition.mLongitude = savedInstanceState.getDouble("longitude");
//            print("found longitude " + mPosition.mLongitude);
//        }
//        if (savedInstanceState.keySet().contains("longitude")) {
//            if (mPosition == null) {
//                mPosition = new Position();
//            }
//            mPosition.mLatitude = savedInstanceState.getDouble("latitude");
//            print("found latitude " + mPosition.mLatitude);
//        }
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
        Log.d(TAG, message);
    }

    @Override
    public void onClick(View v) {
        print("onClick");
        if (v == mAddButton) {
            addMarker(mUser.mLatitude, mUser.mLongitude);
        }
    }

    private void addMarker(double latitude, double longitude) {
        print("addMarker");
        Position position = new Position();
        position.mLongitude = longitude;
        position.mLatitude = latitude;
        position.mMarker = mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).draggable(true));
        mPositions.add(position);
    }

    public void showToast(String text) {
        print("showToast");
        Toast toast = Toast.makeText(this, text, Toast.LENGTH_LONG);
        toast.show();
    }

    public void showNotification(String title, String text) {
        print("showNotification");
        String NOTIFICATION_CHANNEL_ID = "1001";
        String NOTIFICATION_CHANNEL_NAME = "1234";
        int NOTIFICATION_ID = 1001;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
            mBuilder.setSmallIcon(R.drawable.common_google_signin_btn_icon_light_normal);
            mBuilder.setContentTitle(title);
            mBuilder.setContentText(text);
            mBuilder.setAutoCancel(true);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);
            notificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        } else {
            Intent intent = new Intent(this, NotificationReceiverActivity.class);
            PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);
            Notification noti = new Notification.Builder(this)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setSmallIcon(R.drawable.common_google_signin_btn_icon_light_normal)
                    .setContentIntent(pIntent).build();
            noti.flags |= Notification.FLAG_AUTO_CANCEL;
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(0, noti);
        }
    }
}
