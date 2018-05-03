package com.lernskog.erik.locationnotification;

import com.google.android.gms.maps.model.Marker;

class Position {
    public Marker mMarker;
    public double mLatitude;
    public double mLongitude;

    @Override
    public String toString() {
        return "marker id " + mMarker.getId() + " Latitude " + mLatitude + " Longitude " + mLongitude;
    }
}
