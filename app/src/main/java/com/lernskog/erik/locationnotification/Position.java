package com.lernskog.erik.locationnotification;

import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.Marker;

class Position {
    public Marker mMarker;
    public Circle mCircle;
    public double mLatitude;
    public double mLongitude;
    public float mRadius = 100;
    public String mInfo;
}
