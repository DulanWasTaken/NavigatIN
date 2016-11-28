package es.udc.tfg.navigatin.adapter;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Usuario on 10/11/2016.
 */

public class Location {
    LatLng position;
    double orientation;
    int level;
    float accurracy;

    public Location(LatLng position, double orientation, int level, float accurracy) {
        this.position = position;
        this.orientation = orientation;
        this.level = level;
        this.accurracy = accurracy;
    }

    public LatLng getPosition() {
        return position;
    }

    public double getOrientation() {
        return orientation;
    }

    public int getLevel() {
        return level;
    }

    public float getAccurracy() {
        return accurracy;
    }
}
