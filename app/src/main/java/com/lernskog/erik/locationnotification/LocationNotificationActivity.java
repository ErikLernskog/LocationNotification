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

public class LocationNotificationActivity extends FragmentActivity implements View.OnClickListener, OnMapReadyCallback {

    private static final String TAG = LocationNotificationActivity.class.getSimpleName();
    private final static String REQUESTING_LOCATION_UPDATES_KEY = "REQUESTING_LOCATION_UPDATES_KEY";
    private boolean mRequestingLocationUpdates;
    private LocationRequest mLocationRequest;
    private GoogleMap mGoogleMap;
    private LocationCallback mLocationCallback;
    private FusedLocationProviderClient mFusedLocationClient;
    private Button mAddButton;
    private TextView mLatitudeTextView;
    private TextView mLongitudeTextView;

    private User mUser;
    private Position mPosition;

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
        mUser = new User();
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
                print("onMarkerDragEnd");
                mMarker = marker;
            }
        });

        //mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(mLatLng));
        if ((mLatitudeMarker != 0) && (mLongitudeMarker != 0)) {
            addMarker(mUser.mLatitude, mUser.mLongitude);
        }
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
        //stopLocationUpdates();
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
                    mUser.mLatitude = location.getLatitude();
                    mUser.mLongitude = location.getLongitude();
                    //mLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    //mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(mLatLng));
                    print("locationResult " + location.getProvider());
                    verifyDistance();
                }
            }
        };
    }

    private void verifyDistance() {
        print("verifyDistance");
        if (mMarker != null) {
            print("marker exist");
            mLongitudeMarker = mMarker.getPosition().longitude;
            mLatitudeMarker = mMarker.getPosition().latitude;
            mMarker.setTitle("Latitude " + String.valueOf(mLatitudeMarker) + " Longitude " + String.valueOf(mLongitudeMarker));
            mMarker.setSnippet("Latitude " + String.valueOf(mUser.mLatitude) + " Longitude " + String.valueOf(mUser.mLongitude));
        } else {
            print("marker does not exist");
            return;
        }
        mLatitudeTextView.setText(String.valueOf(mLatitudeMarker));
        mLongitudeTextView.setText(String.valueOf(mLongitudeMarker));

        Location markerLocation = new Location("marker");
        markerLocation.setLongitude(mLongitudeMarker);
        markerLocation.setLatitude(mLatitudeMarker);
        Location userLocation = new Location("user");
        userLocation.setLongitude(mUser.mLongitude);
        userLocation.setLatitude(mUser.mLatitude);
        float distance = userLocation.distanceTo(markerLocation);
        print("distance " + distance);
        if (distance < 1.0) {
            showNotification("Place", String.valueOf(distance));
            showToast("Place " + String.valueOf(distance));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        print("onSaveInstanceState");
        outState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, mRequestingLocationUpdates);
        outState.putDouble("latitude", mLatitudeMarker);
        print("store latitude " + mLatitudeMarker);
        outState.putDouble("longitude", mLongitudeMarker);
        print("store longitude " + mLongitudeMarker);
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
        if (savedInstanceState.keySet().contains("latitude")) {
            mLongitudeMarker = savedInstanceState.getDouble("longitude");
            print("found longitude " + mLongitudeMarker);
        }
        if (savedInstanceState.keySet().contains("longitude")) {
            mLatitudeMarker = savedInstanceState.getDouble("latitude");
            print("found latitude " + mLatitudeMarker);
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
        mLongitudeMarker = longitude;
        mLatitudeMarker = latitude;
        mMarker = mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).draggable(true));
    }

    private void showToast(String text) {
        print("showToast");
        Toast toast = Toast.makeText(this, text, Toast.LENGTH_LONG);
        toast.show();
    }

    private void showNotification(String title, String text) {
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
