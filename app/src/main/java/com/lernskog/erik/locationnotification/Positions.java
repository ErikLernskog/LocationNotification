package com.lernskog.erik.locationnotification;

import android.location.Location;

import java.util.ArrayList;
import java.util.List;

class Positions {
    private List<Position> mPositions;
    private LocationNotificationActivity mLocationNotificationActivity;

    Positions(LocationNotificationActivity locationNotificationActivity) {
        mLocationNotificationActivity = locationNotificationActivity;
        mPositions = new ArrayList<Position>();
    }

    public void add(Position position) {
        mPositions.add(position);
        mPositions.
    }

    public void verifiyDistance(User user) {
        mLocationNotificationActivity.print("user Latitude " + user.mLatitude + " Longitude " + user.mLongitude);
        for (Position position : mPositions) {
            mLocationNotificationActivity.mLatitudeTextView.setText(String.valueOf(position.mLatitude));
            mLocationNotificationActivity.mLongitudeTextView.setText(String.valueOf(position.mLongitude));
            Location markerLocation = new Location("marker");
            markerLocation.setLongitude(position.mLongitude);
            markerLocation.setLatitude(position.mLatitude);
            Location userLocation = new Location("user");
            userLocation.setLongitude(user.mLongitude);
            userLocation.setLatitude(user.mLatitude);
            float distance = userLocation.distanceTo(markerLocation);
            mLocationNotificationActivity.print("marker id " + position.mMarker.getId() + " Latitude " + position.mLatitude + " Longitude " + position.mLongitude + " distance " + distance);
            if (distance < 1.0) {
                mLocationNotificationActivity.showNotification("Place", String.valueOf(distance));
                mLocationNotificationActivity.showToast("Place " + String.valueOf(distance));
            }
        }

    }
}
