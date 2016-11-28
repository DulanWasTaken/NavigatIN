package es.udc.tfg.navigatin.adapter;

import android.graphics.Bitmap;

import java.util.ArrayList;

/**
 * Created by Usuario on 14/11/2016.
 */

public interface AdapterInterface {
    public void onBuildingsReceived(ArrayList<Building> buildings);
    public void onSensorErrorReceived(String error);
    public void onGettingBuildingsErrorReceived();
    public void onLevelsReceived(ArrayList<Level> levels);
    public void onGettingLevelsErrorReceived();
    public void onGettingMapImageErrorReceived();
    public void onMapImageReceived(Bitmap mapImage);
    public void onPosReceived(Location location);
    public void onInvalidPosReceived();
}
