package com.lernskog.erik.locationnotification;

import android.location.Location;

import java.util.HashMap;
import java.util.Map;

class Positions {
    public Map<String, Position> mPositions;
    private LocationNotificationActivity mLocationNotificationActivity;

    Positions(LocationNotificationActivity locationNotificationActivity) {
        mLocationNotificationActivity = locationNotificationActivity;
        mPositions = new HashMap<String, Position>();
    }

    public void add(Position position) {
        mPositions.put(position.mMarker.getId(), position);
    }

    public void del(String id) {
        Position position = mPositions.get(id);
        position.mCircle.remove();
        position.mMarker.remove();
        mPositions.remove(id);
    }

    public Position get(String id){
        return mPositions.get(id);
    }

    public void verifiyDistance(User user) {
        mLocationNotificationActivity.print("user Latitude " + user.mLatitude + " Longitude " + user.mLongitude);
        for (String id : mPositions.keySet()) {
            Position position = mPositions.get(id);
            position.mLatitude = position.mMarker.getPosition().latitude;
            position.mLongitude = position.mMarker.getPosition().longitude;
            position.mCircle.setCenter(position.mMarker.getPosition());
            Location markerLocation = new Location("marker");
            markerLocation.setLongitude(position.mLongitude);
            markerLocation.setLatitude(position.mLatitude);
            Location userLocation = new Location("user");
            userLocation.setLongitude(user.mLongitude);
            userLocation.setLatitude(user.mLatitude);
            float distance = userLocation.distanceTo(markerLocation);
            mLocationNotificationActivity.print("marker id " + position.mMarker.getId() + " Latitude " + position.mLatitude + " Longitude " + position.mLongitude + " distance " + distance);
            if (distance < position.mRadius) {
                mLocationNotificationActivity.showNotification("Place", String.valueOf(distance));
                mLocationNotificationActivity.showToast("Place " + String.valueOf(distance));
            }
        }
    }
}

