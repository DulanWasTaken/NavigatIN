package es.udc.tfg.navigatin.adapter;

/**
 * Created by Usuario on 10/11/2016.
 */

public class Building {
    private String name;
    private double lat,lng,rotation;
    private float width, height;
    private int id;


    public Building(int id, String name, double lat, double lng, float width, float height, double rotation) {
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.id = id;
        this.width = width;
        this.height = height;
        this.rotation = rotation;
    }

    public String getName() {
        return name;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public int getId() {
        return id;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public double getRotation() {
        return rotation;
    }
}
