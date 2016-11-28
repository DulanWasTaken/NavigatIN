package es.udc.tfg.navigatin.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import es.udc.tfg.navigatin.R;
import es.udc.tfg.navigatin.adapter.Building;
import es.udc.tfg.navigatin.adapter.Level;
import es.udc.tfg.navigatin.adapter.Location;
import es.udc.tfg.navigatin.adapter.SitumAdapter;
import es.udc.tfg.navigatin.utils.Utils;

/**
 * A placeholder fragment containing a simple view.
 */
public class MapActivityFragment extends Fragment implements OnMapReadyCallback {

    MapActivityFragmentInterface mListener;
    Context context;
    private double myLat,myLng;
    private SitumAdapter adapter;
    private Building currentBuilding;
    private Bitmap mapImage;
    private String GoogleMapFragmentTag = "GoogleMapFragmentTag";
    private Marker locationMarker;
    private Circle accuracyCircle;
    private GoogleMap map;
    private boolean mapReady = false;

    public MapActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MapActivityFragmentInterface) {
            mListener = (MapActivityFragmentInterface) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }

        this.context = context;
    }

    @Override
    public void onResume() {
        super.onResume();
        myLat = getArguments().getDouble("lat");
        myLng = getArguments().getDouble("lng");

        adapter = mListener.getAdapter();

        adapter.getBuildings();

    }

    public void onBuildingsReceived(ArrayList<Building> buildings){
        currentBuilding = buildings.get(0);
        double dist = Math.sqrt(Math.pow(Math.abs(myLat)-Math.abs(currentBuilding.getLat()),2)+Math.pow(Math.abs(myLng)-Math.abs(currentBuilding.getLng()),2));;
        for(Building b:buildings){
            double aux = Math.sqrt(Math.pow(Math.abs(myLat)-Math.abs(b.getLat()),2)+Math.pow(Math.abs(myLng)-Math.abs(b.getLng()),2));
            if(aux<dist){
                currentBuilding = b;
                dist=aux;
            }
        }
        adapter.getLevels(currentBuilding);
    }

    public void onLevelsReceived(ArrayList<Level> levels){
        adapter.getMapLevel(levels.get(0));
    }

    public void onMapImageReceived(Bitmap mapImage){
        this.mapImage = mapImage;

        GoogleMapOptions initOptions = new GoogleMapOptions()
                .camera(new CameraPosition(new LatLng(currentBuilding.getLat(),currentBuilding.getLng()),18,0,0));

        FragmentManager fm = getFragmentManager();
        SupportMapFragment mMapFragment = SupportMapFragment.newInstance(initOptions);
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.content_map, mMapFragment,GoogleMapFragmentTag);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        fm.executePendingTransactions();

        SupportMapFragment mapFragment = (SupportMapFragment) fm.findFragmentByTag(GoogleMapFragmentTag);
        mapFragment.getMapAsync(this);
    }

    public void onPositionReceived(Location location){
        locationMarker.setPosition(location.getPosition());
        locationMarker.setVisible(true);

        accuracyCircle.setCenter(location.getPosition());
        accuracyCircle.setRadius(location.getAccurracy());
        accuracyCircle.setVisible(false);

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(location.getPosition())
                .zoom(23)
                .bearing((float)location.getOrientation())
                .tilt(0)
                .build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    public void onStartLocationButtonClicked(){
        if(mapReady)
            adapter.startLocation(currentBuilding);
    }
    public void onStopLocationButtonClicked(){
        adapter.stopLocation();
        locationMarker.setVisible(false);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(currentBuilding.getLat(),currentBuilding.getLng()))
                .zoom(18)
                .bearing(0)
                .tilt(0)
                .build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.setIndoorEnabled(false);

        GroundOverlayOptions overlayOptions = new GroundOverlayOptions()
                .position(new LatLng(currentBuilding.getLat(),currentBuilding.getLng()),currentBuilding.getWidth(),currentBuilding.getHeight())
                .image(BitmapDescriptorFactory.fromBitmap(mapImage))
                .bearing(Utils.radToDegrees(currentBuilding.getRotation()))
                .zIndex(1);
        googleMap.addGroundOverlay(overlayOptions);

        MarkerOptions markerOptions = new MarkerOptions()
                .position(new LatLng(currentBuilding.getLat(),currentBuilding.getLng()))
                .flat(true)
                .anchor((float)0.5,(float)0.5)
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.location))
                .visible(false);
        locationMarker = googleMap.addMarker(markerOptions);

        final CircleOptions circleOptions = new CircleOptions()
                .center(new LatLng(currentBuilding.getLat(), currentBuilding.getLng()))
                .radius(50)
                .fillColor(Color.argb(50,0,0,100))
                .strokeColor(Color.BLUE)
                .visible(false)
                .zIndex(2);
        accuracyCircle = googleMap.addCircle(circleOptions);

        mapReady = true;
        map = googleMap;
    }
}
