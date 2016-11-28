package es.udc.tfg.navigatin.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.loopj.android.http.FileAsyncHttpResponseHandler;

import org.apache.http.Header;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import es.situm.sdk.v1.Point2f;
import es.situm.sdk.v1.SitumBuilding;
import es.situm.sdk.v1.SitumDataManager;
import es.situm.sdk.v1.SitumError;
import es.situm.sdk.v1.SitumIPSManager;
import es.situm.sdk.v1.SitumLevel;
import es.situm.sdk.v1.SitumLocation;
import es.situm.sdk.v1.SitumLogin;
import es.situm.sdk.v1.SitumPoseReceiver;
import es.situm.sdk.v1.SitumResponseHandler;
import es.situm.sdk.v1.SitumSensorErrorListener;
import es.udc.tfg.navigatin.core.MapActivity;
import es.udc.tfg.navigatin.core.MapActivityFragmentInterface;

/**
 * Created by Usuario on 10/11/2016.
 */

public class SitumAdapter {
    String TAG = "Log Situm adapter";
    private Context context;
    private SitumDataManager situmDataManager;
    private SitumIPSManager ipsManager;
    private ArrayList<SitumBuilding> buildings;
    private ArrayList<SitumLevel> levels;
    private SitumBuilding selectedBuilding = null;
    private AdapterInterface mListener;

    public SitumAdapter(Context context){
        this.context = context;

        if (context instanceof AdapterInterface) {
            mListener = (MapActivity) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement AdapterInterface");
        }

        situmDataManager = SitumLogin.login(context, "udc@situm.es", "dWitVupDW/5HnkOWG36Z8+q0eTtmu9bO");
        ipsManager = new SitumIPSManager(context);

        ipsManager.setSensorErrorListener(new SitumSensorErrorListener() {
            @Override
            public void onError(SitumError situmError) {
                Log.e(TAG, situmError.name + " " + situmError.description);
                mListener.onSensorErrorReceived(situmError.name+" "+situmError.description);
            }
        });
    }

    public void getBuildings (){
        situmDataManager.fetchBuildings(new SitumResponseHandler<SitumBuilding>() {
            @Override
            public void onListReceived(List<SitumBuilding> list) {
                buildings = new ArrayList<>(list);
                ArrayList<Building> resultBuildings = new ArrayList<Building>();
                for(SitumBuilding b : buildings){
                            resultBuildings.add(new Building(b.getId(),b.getName(),b.getLatitud(),b.getLongitud(),(float)b.getWidth(),(float)b.getHeight(),b.getRotation()));
                }
                mListener.onBuildingsReceived(resultBuildings);
            }

            @Override
            public void onErrorReceived(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                Log.e(TAG, "Error receiving buildings");
                mListener.onGettingBuildingsErrorReceived();
             }
        });
    }

    public void getLevels(Building building){
        selectedBuilding = null;
        for (SitumBuilding b : buildings){
            if (b.getId()==building.getId()){
                selectedBuilding = b;
                break;
            }
        }
        if(selectedBuilding != null) {
            situmDataManager.fetchLevelsForBuilding(selectedBuilding, new SitumResponseHandler<SitumLevel>() {
                @Override
                public void onListReceived(List<SitumLevel> list) {
                    levels = new ArrayList<>(list);
                    ArrayList<Level> resultLevels = new ArrayList<Level>();
                    for(SitumLevel l : levels){

                        resultLevels.add(new Level(l.getLevelId(),l.getBuildingId(),l.getLevel()));
                    }
                    mListener.onLevelsReceived(resultLevels);
                }

                @Override
                public void onErrorReceived(int i, Header[] h, byte[] bytes, Throwable t) {
                    Log.e(TAG, "Error receiving levels");
                    mListener.onGettingLevelsErrorReceived();
                }
            });
        }
    }


    public void getMapLevel(Level level){
        SitumLevel selectedLevel = null;
        for (SitumLevel l : levels){
            if (l.getLevelId()==level.getLevelID()){
                selectedLevel = l;
                break;
            }
        }
        if (selectedLevel != null) {
            situmDataManager.fetchMapForLevel(selectedLevel,
                    new FileAsyncHttpResponseHandler(context) {
                        @Override
                        public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, Throwable throwable, File file) {
                            Log.e(TAG, "Error downloading image for level");
                            mListener.onGettingMapImageErrorReceived();
                        }

                        @Override
                        public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, File file) {
                            Bitmap mapImage = BitmapFactory.decodeFile(file.getAbsolutePath());
                            mListener.onMapImageReceived(mapImage);
                        }
                    });
        }
    }

    public void startLocation(Building building){
        selectedBuilding = null;
        for (SitumBuilding b : buildings){
            if (b.getId()==building.getId()){
                selectedBuilding = b;
                break;
            }
        }
        if (selectedBuilding != null){
            ipsManager.start(selectedBuilding);

            ipsManager.setPoseReceiver(new SitumPoseReceiver() {
                @Override
                public void onPoseReceived(SitumLocation situmLocation) {
                    Log.i(TAG, String.format("x %s y %s", situmLocation.x, situmLocation.y));
                    Point2f aux = selectedBuilding.toEarthCoordinates(situmLocation.x,situmLocation.y);
                    double orientation = selectedBuilding.toEarthAngle(situmLocation.yaw);
                    LatLng pos = new LatLng(aux.getY(),aux.getX());
                    Location resultLocation = new Location(pos,orientation,situmLocation.level,situmLocation.getAccuracy());
                    mListener.onPosReceived(resultLocation);
                }

                @Override
                public void onInvalidPoseReceived(SitumLocation situmLocation) {
                    Log.w(TAG, String.format("x %s y %s", situmLocation.x, situmLocation.y));
                    mListener.onInvalidPosReceived();
                }
            });
        }
    }

    public void stopLocation(){
        ipsManager.stop();
    }
}
