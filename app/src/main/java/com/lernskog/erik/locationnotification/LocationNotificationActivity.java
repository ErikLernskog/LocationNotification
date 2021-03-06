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
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class LocationNotificationActivity extends FragmentActivity implements View.OnClickListener, OnMapReadyCallback {

    private static final String TAG = LocationNotificationActivity.class.getSimpleName();
    private LocationRequest mLocationRequest;
    private GoogleMap mGoogleMap;
    private LocationCallback mLocationCallback;
    private FusedLocationProviderClient mFusedLocationClient;
    private Button mAddButton;
    private Button mDelButton;
    private User mUser;
    private Positions mPositions;
    private String mId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        print("onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_notification);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mAddButton = findViewById(R.id.button_add);
        mAddButton.setOnClickListener(this);
        mDelButton = findViewById(R.id.button_del);
        mDelButton.setOnClickListener(this);
        checkAndRequestPermission();
        createLocationRequest();
        createLocationCallback();
    }

    @Override
    protected void onResume() {
        print("onResume");
        super.onResume();
        startLocationUpdates();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        print("onSaveInstanceState");
        store();
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onClick(View v) {
        print("onClick");
        if (v == mAddButton) {
            if (mUser != null) {
                addMarker(mUser.mLatitude, mUser.mLongitude);
            }
        } else if (v == mDelButton) {
            delMarker();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        print("onMapReady");
        mGoogleMap = googleMap;
        if (checkAndRequestPermission()) {
            return;
        }
        setupMap();
        restore();
    }

    private void setupMap() {
        if (checkAndRequestPermission()) {
            return;
        }
        mGoogleMap.setMyLocationEnabled(true);
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
        mGoogleMap.getUiSettings().setCompassEnabled(true);
        mGoogleMap.getUiSettings().setZoomControlsEnabled(true);

        mGoogleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                print("onMarkerDragStart");
                showMarker(marker, false);
            }

            @Override
            public void onMarkerDrag(Marker marker) {
                print("onMarkerDrag");
                showMarker(marker, false);
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                print("onMarkerDragEnd");
                showMarker(marker,true);
            }
        });

        mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                print("onMarkerClick");
                showMarker(marker, true);
                return false;
            }
        });

        mGoogleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                print("onMyLocationButtonClick");
                if (mId != null) {
                    Position position = mPositions.get(mId);
                    position.mMarker.hideInfoWindow();
                }
                mId = null;
                return false;
            }
        });
//        addMarker(59.858300, 17.647447); //uppsala
//        addMarker(59.725714, 17.786876); //knivsta
//        addMarker(59.646835, 17.924055); //arlanda
//        addMarker(59.521715, 17.899513); //upplandsvasby
//        addMarker(59.476302, 17.914430); //rotebro
//        addMarker(59.458144, 17.924387); //norrviken
//        addMarker(59.444316, 17.932369); //haggvik
//        addMarker(59.428758, 17.948076); //sollentuna
//        addMarker(59.409615, 17.961873); //helenelund
    }

    private boolean checkAndRequestPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return true;
        }
        return false;
    }

    private void showMarker(Marker marker, boolean updateStreet) {
        print("showMarker id:" + marker.getId());
        mId = marker.getId();
        Position position = mPositions.get(marker.getId());
        double latitude = marker.getPosition().latitude;
        double longitude = marker.getPosition().longitude;
        position.mCircle.setCenter(new LatLng(latitude, longitude));
        if (updateStreet) {
            position.mStreet = getStreet(latitude, longitude);
        }
        String title = position.mStreet;
        position.mMarker.setTitle(title);
        String snippet = String.valueOf(latitude) + ", " + String.valueOf(longitude);
        position.mMarker.setSnippet(snippet);
        position.mMarker.showInfoWindow();
    }

    protected void createLocationCallback() {
        print("createLocationCallback");
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                print("onLocationResult");
//                if (locationResult == null) {
//                    return;
//                }
                for (Location location : locationResult.getLocations()) {
                    updateUser(location.getLatitude(), location.getLongitude());
                    verifyDistance();
                }
            }
        };
    }

    protected void createLocationRequest() {
        print("createLocationRequest");
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    private void startLocationUpdates() {
        print("startLocationUpdates");
        if (checkAndRequestPermission()) {
            return;
        }
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
    }

    private void updateUser(double latitude, double longitude) {
        print("updateUser");
        if (mUser == null) {
            mUser = new User();
        }
        mUser.mLatitude = latitude;
        mUser.mLongitude = longitude;
        if (mGoogleMap == null) {
            return;
        }
        if (mId == null) {
            if (mGoogleMap.isMyLocationEnabled() == false) {
                setupMap();
            }
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(latitude, longitude)));
        }
    }

    private void verifyDistance() {
        print("verifyDistance");
        if (mPositions != null) {
            mPositions.verifiyDistance(mUser);
        }
    }

    private void delMarker() {
        print("delMarker");
        if (mId != null) {
            mPositions.del(mId);
            mId = null;
        }
    }

    public void addMarker(double latitude, double longitude) {
        print("addMarker");
        Position position = new Position();
        position.mLongitude = longitude;
        position.mLatitude = latitude;
        position.mMarker = mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).draggable(true));
        position.mCircle = mGoogleMap.addCircle(new CircleOptions().center(new LatLng(latitude, longitude)).radius(position.mRadius));
        position.mStreet = getStreet(latitude, longitude);
        if (mPositions == null) {
            mPositions = new Positions(this);
        }
        mPositions.add(position);
    }

    private String getStreet(double latitude, double longitude) {
        print("getStreet");
        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            return addresses.get(0).getAddressLine(0).toString().replace(",","");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public void showToast(String title, String text) {
        print("showToast");
        Toast toast = Toast.makeText(this, title + text, Toast.LENGTH_LONG);
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

    public void store() {
        print("store");
        if (mPositions == null) {
            return;
        }
        File path = getExternalFilesDir(null);
        File file = new File(path,"positions.txt");
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            for (String id : mPositions.mPositions.keySet()) {
                Position position = mPositions.mPositions.get(id);
                print("id " + id + " latitude " + String.valueOf(position.mLatitude) + " longitude " + String.valueOf(position.mLongitude));
                fileOutputStream.write(String.valueOf(position.mLatitude).getBytes());
                fileOutputStream.write(",".getBytes());
                fileOutputStream.write(String.valueOf(position.mLongitude).getBytes());
                fileOutputStream.write("\n".getBytes());
            }
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void restore() {
        print("restore");
        File path = getExternalFilesDir(null);
        File file = new File(path,"positions.txt");
        int length = (int) file.length();
        byte[] bytes = new byte[length];
        FileInputStream fileInputStream;
        String line;
        try {
            fileInputStream = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            while ((line = bufferedReader.readLine()) != null) {
                print("position " + line);
                String[] position = line.split("[,]");
                double latitude = Double.parseDouble(position[0]);
                double longitude = Double.parseDouble(position[1]);
                addMarker(latitude, longitude);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void print(final String message) {
        Log.d(TAG, message);
    }
}
