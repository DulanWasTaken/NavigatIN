package es.udc.tfg.navigatin.core;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;

import java.util.ArrayList;

import es.udc.tfg.navigatin.R;
import es.udc.tfg.navigatin.adapter.AdapterInterface;
import es.udc.tfg.navigatin.adapter.Building;
import es.udc.tfg.navigatin.adapter.Level;
import es.udc.tfg.navigatin.adapter.SitumAdapter;

public class MapActivity extends AppCompatActivity implements MapActivityFragmentInterface,AdapterInterface {

    FragmentManager fragmentManager;
    CoordinatorLayout coordinator;
    SitumAdapter adapter = null;
    String TAG = "MapActivity";
    String MapFragmentTag = "MapFragmentTag";
    Location l;
    int LOCATION_PERMISSION_RESULT;
    Snackbar errorMSG;
    boolean locationState = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        fragmentManager = getSupportFragmentManager();
        coordinator = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final FloatingActionButton startLocationButton = (FloatingActionButton) findViewById(R.id.fab);
        startLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locationState=!locationState;
                Snackbar.make(view, locationState ? R.string.snackBarLocationEnabled : R.string.snackBarLocationDisabled, Snackbar.LENGTH_SHORT).show();
                MapActivityFragment f = (MapActivityFragment) fragmentManager.findFragmentByTag(MapFragmentTag);
                if(f != null) {
                    if (locationState) {
                        f.onStartLocationButtonClicked();
                    } else {
                        f.onStopLocationButtonClicked();
                    }
                }
                startLocationButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(locationState ? R.color.colorButtonOn : R.color.colorButtonOff)));
                Interpolator interpolador = AnimationUtils.loadInterpolator(getBaseContext(),
                        android.R.interpolator.fast_out_slow_in);
                view.animate()
                        .rotation(locationState ? 45f : 0)
                        .setInterpolator(interpolador)
                        .start();
            }
        });

        adapter = new SitumAdapter(this);

        init();
    }

    private void init(){
        LocationManager locateManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_RESULT);
        }else {
            Log.d(TAG,locateManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)+"");
            if(!(locateManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))){

                Snackbar.make(coordinator,R.string.alertDialog_network_msg,Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.alertDialog_network_action, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                                startActivity(intent);
                            }
                        })
                        .show();

            }
            else {
                l = locateManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                MapActivityFragment mapFrag = new MapActivityFragment();
                Bundle args = new Bundle();
                args.putDouble("lat", l.getLatitude());
                args.putDouble("lng", l.getLongitude());
                mapFrag.setArguments(args);
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.fragment_content, mapFrag,MapFragmentTag).commit();
                //fragmentManager.executePendingTransactions();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        init();
    }

    /************************************ MAP FRAGMENT INTERFACE FUNCTIONS **************************************/

    @Override
    public SitumAdapter getAdapter(){
        return adapter;
    }

    /************************************************************************************************************/

    /************************************ ADAPTER INTERFACE FUNCTIONS ******************************************/

    @Override
    public void onBuildingsReceived(ArrayList<Building> buildings) {
        MapActivityFragment f = (MapActivityFragment) fragmentManager.findFragmentByTag(MapFragmentTag);
        if(f != null)
            f.onBuildingsReceived(buildings);
    }

    @Override
    public void onSensorErrorReceived(String error){
        errorMSG = Snackbar.make(coordinator,error,Snackbar.LENGTH_INDEFINITE);
        errorMSG.show();
    }

    @Override
    public void onGettingBuildingsErrorReceived() {

    }

    @Override
    public void onLevelsReceived(ArrayList<Level> levels) {
        MapActivityFragment f = (MapActivityFragment) fragmentManager.findFragmentByTag(MapFragmentTag);
        if(f != null)
            f.onLevelsReceived(levels);
    }

    @Override
    public void onGettingLevelsErrorReceived() {

    }

    @Override
    public void onGettingMapImageErrorReceived() {

    }

    @Override
    public void onMapImageReceived(Bitmap mapImage) {
        MapActivityFragment f = (MapActivityFragment) fragmentManager.findFragmentByTag(MapFragmentTag);
        if(f != null)
            f.onMapImageReceived(mapImage);
    }

    @Override
    public void onPosReceived(es.udc.tfg.navigatin.adapter.Location location) {
        errorMSG.dismiss();
        MapActivityFragment f = (MapActivityFragment) fragmentManager.findFragmentByTag(MapFragmentTag);
        if(f != null)
            f.onPositionReceived(location);
    }

    @Override
    public void onInvalidPosReceived() {

    }

    /************************************************************************************************************/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
