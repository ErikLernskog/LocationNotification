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
import android.widget.EditText;
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
    public TextView mLatitudeTextView;
    public TextView mLongitudeTextView;
    public EditText mInfoEditText;
    private User mUser;
    private Positions mPositions;
    private String mId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        print("onCreate");xt
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_notification);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mAddButton = findViewById(R.id.button_add);
        mAddButton.setOnClickListener(this);
        mDelButton = findViewById(R.id.button_del);
        mDelButton.setOnClickListener(this);
        mLatitudeTextView = findViewById(R.id.textview_latitude);
        mLongitudeTextView = findViewById(R.id.textview_longitude);
        mInfoEditText = findViewById(R.id.edittext_info);
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
                print("onMarkerDragEnd");
                showMarker(marker);
            }
        });
        mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                print("onMarkerClick");
                showMarker(marker);
                return false;
            }
        });
        restore();
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

    private void showMarker(Marker marker) {
        print("onMarkerClick id:" + marker.getId());
        mId = marker.getId();
        mLatitudeTextView.setText(String.valueOf(marker.getPosition().latitude));
        mLongitudeTextView.setText(String.valueOf(marker.getPosition().longitude));
        mInfoEditText.setText()
    }
    @Override
    protected void onResume() {
        print("onResume");
        super.onResume();
        startLocationUpdates();
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
                print("onLocationResult");
                if (locationResult == null) {
                    return;
                }
                if (mUser == null) {
                    mUser = new User();
                }
                for (Location location : locationResult.getLocations()) {
                    mUser.mLatitude = location.getLatitude();
                    mUser.mLongitude = location.getLongitude();
                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
                    verifyDistance();
                }
            }
        };
    }

    private void verifyDistance() {
        print("verifyDistance");
        if (mPositions != null) {
            mPositions.verifiyDistance(mUser);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        print("onSaveInstanceState");
        if (mPositions != null) {
            store();
        }
        super.onSaveInstanceState(outState);
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        print("updateValuesFromBundel");
        if (savedInstanceState == null) {
            return;
        }
    }

    protected void createLocationRequest() {
        print("createLocationRequest");
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        //mRequestingLocationUpdates = true;
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
            if (mUser != null) {
                addMarker(mUser.mLatitude, mUser.mLongitude);
            }
        } else if (v == mDelButton) {
            delMarker();
        }
    }

    private void delMarker() {
        if (mId != null) {
            mLatitudeTextView.setText("");
            mLongitudeTextView.setText("");
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
        if (mPositions == null) {
            mPositions = new Positions(this);
        }
        mPositions.add(position);

        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            print(addresses.get(0).getAddressLine(0).toString().replace(",",""));
            position.mInfo = addresses.get(0).getAddressLine(0).toString().replace(",","");
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public void store() {
        print("store");
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
}
