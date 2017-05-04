package uk.co.claytapp.taggerbath.Interfaces;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.facebook.Profile;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import uk.co.claytapp.taggerbath.Activities.MainActivity;
import uk.co.claytapp.taggerbath.BiHashMap;
import uk.co.claytapp.taggerbath.Bounds;
import uk.co.claytapp.taggerbath.Game_Objects.GridSquare;
import uk.co.claytapp.taggerbath.Game_Objects.User;
import uk.co.claytapp.taggerbath.Iterators;

import static android.content.Context.LOCATION_SERVICE;

/**
 * Created by Sam on 07/02/2017.
 */

public class MapController {

    double latScale = 0.000270;
    double lngScale = 0.000432;

    private int time_between_grid_updates = 20000;

    private int time_between_position_updates = 0;
    private float distance_between_position_updates = 1f;
    private float location_accuracy_required = 20f;
    private float max_speed = 8.9408f;

    private static final float min_width_of_grid_request = 0.003773f;
    private static final float default_zoom = 18f;

    private static final float invisible_zoom = 15f;

    private boolean location_setup;

    private static final String logtag = "MapController";

    ApiManager api;

    private Context context;
    private MainActivity mainActivity;
    private GoogleMap mMap;
    private BiHashMap<Double, Double, GridSquare> gridSquares;

    private LocationManager locationManager;
    private Timer updateGridTimer;

    private Toast tooFastToast;

    private boolean grid_updating;
    private boolean grid_update_pending;
    private int grid_updates_in_progress;

    private boolean location_updates;
    private boolean location_updating;

    private boolean changing_square_visibility;
    private boolean change_square_visibility_pending;

    private Switch camera_lock;

    private Marker marker;
    private LatLng current_position;
    private Location current_location;
    private User user;

    private BiHashMap<String, Integer, BitmapDescriptor> square_images;


    public MapController(Context c, GoogleMap googleMap) {
        context = c;
        mainActivity = (MainActivity) context;

        location_setup = false;
        current_location = null;

        mMap = googleMap;
        gridSquares = new BiHashMap<Double, Double, GridSquare>();
        api = new ApiManager(context);

        changing_square_visibility = false;
        change_square_visibility_pending = false;

        grid_updates_in_progress = 0;

        setupLocationMonitoring();
        setupMap();

        camera_lock = mainActivity.getCamera_lock();
        camera_lock.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean locked) {
                if (locked) {
                    mMap.getUiSettings().setScrollGesturesEnabled(false);
                    moveToPosition(true);
                } else {
                    mMap.getUiSettings().setScrollGesturesEnabled(true);
                }
            }
        });

        camera_lock.setChecked(true);


        updateGridTimer = new Timer();
        updateGridTimer.schedule(updateGridTimerTask, 0, time_between_grid_updates);
    }

    public void moveToPosition(boolean zoom) {
        if (current_position != null) {
            if (zoom) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(current_position, default_zoom), 500, null);
            } else {
                mMap.animateCamera(CameraUpdateFactory.newLatLng(current_position), 500, null);
            }
        }
    }


    private void setupMap() {

        mMap.setOnCameraIdleListener(cameraIdleListener);
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        //mMap.setMinZoomPreference(15);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(51.379802, -2.328756), default_zoom));
    }


    public void setupLocationMonitoring() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.i(logtag, "setupLocationMonitoring, location isnt on so requesting");
            mainActivity.requestLocationPermission();
            return;
        }

        if (location_setup){
            Log.i(logtag, "setupLocationMonitoring, already setup");
            return;
        }

        if (locationManager == null) {
            locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        }

        Log.i(logtag, "setting up location updates \n\n\n\n\n ######\n\n\n");

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                time_between_position_updates,
                distance_between_position_updates,
                locationListener);

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                time_between_position_updates,
                distance_between_position_updates,
                locationListener);

        location_setup = true;
    }

    public void cancelLocationMonitoring() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        if (location_setup){
            Log.i(logtag, "cancelLocationMonitoring, already not set uop");
            return;
        }

        locationManager.removeUpdates(locationListener);
        location_setup = false;
    }

    public boolean isLocation_setup(){
        return location_setup;
    }

    private LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            newLocation(location);
        }



        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            context.startActivity(intent);
        }

    };

    public void setLocationUpdates(boolean status){
        location_updates = status;
    }

    public class new_position_request extends AsyncTask<Void, Void, Void>{

        LatLng loc;
        JSONObject response;


        public new_position_request(LatLng location){
            location_updating = true;
            loc = location;
        }


        @Override
        protected Void doInBackground(Void... params) {

            response = api.new_position(loc);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            String new_team;
            double nw_lat;
            double nw_lng;
            int new_level;

            try {
                if (response == null){
                    Log.i(logtag, "New position failed - null response from server");
                    Toast.makeText(context, "Location Null Response", Toast.LENGTH_SHORT);
                }
                else if (!response.has("outcome")){
                    Log.i(logtag, "New position failed - no outcome field");
                    Toast.makeText(context, "Location Response error", Toast.LENGTH_SHORT);
                }
                else if (!response.getString("outcome").equals("success")){
                    Log.i(logtag, "New position failed - outcome 'fail'");
                    Toast.makeText(context, "Location response failure", Toast.LENGTH_SHORT);
                }
                else{
                    Log.i(logtag, "New position successful");

                    new_team = response.getString("team");
                    nw_lat = response.getDouble("nw_lat");
                    nw_lng = response.getDouble("nw_lng");
                    new_level = response.getInt("level");

                    LatLng square_loc = new LatLng(nw_lat, nw_lng);
                    changeSquareColour(square_loc, new_team, new_level);
                }


            } catch (JSONException e) {
                e.printStackTrace();
                Log.i(logtag, "New position failed - JSONException while reading response - " + e.toString());
                Toast.makeText(context, "Location JSON response failure", Toast.LENGTH_SHORT);
            }


            location_updating = false;
        }
    }

    public void refreshLocalLocation(LatLng loc){
        Log.i(logtag, "Updating local position on map");

        double old_lat = 0.0d;
        double old_lng = 0.0d;
        if (current_position != null){
            old_lat = current_position.latitude;
            old_lng = current_position.longitude;
        }

        current_position = loc;

        if (marker == null){
            marker = mMap.addMarker(new MarkerOptions().position(loc).title("You"));
        }
        else{
            marker.setPosition(loc);
        }


        User u = mainActivity.getUser();
        if (u != null){
            marker.setIcon(u.getMarkerIcon());
        }
        else{
            marker.setIcon(BitmapDescriptorFactory.fromResource(User.getMarkerIcon(null)));
        }


        if (camera_lock.isChecked() &&
                (current_position.latitude != old_lat ||
                current_position.longitude != old_lng)){
            mMap.animateCamera(CameraUpdateFactory.newLatLng(loc), 1000, null);
        }
    }

    public void newLocation(Location location){
        Log.i(logtag, "Loc from " + location.getProvider()
                + ". Accuracy: " + location.getAccuracy()
                + " Speed: " + String.valueOf(location.getSpeed()) + "m/s");

        current_location = location;

        LatLng loc = new LatLng(location.getLatitude(),
                location.getLongitude());

        refreshLocalLocation(loc);



        if (location_updating == true){
            Log.v(logtag, "location in the middle of an update");
        }
        else if (location_updates != true
                || Profile.getCurrentProfile() == null){
            Log.i(logtag, "Not signed in or updates disabled");
        }
        else if (location.getAccuracy() > location_accuracy_required){
            Log.v(logtag, "location accuracey not enough");
        }
        else if (location.getSpeed() > max_speed){
            Log.i(logtag, "Moving over max speed");

            if (mainActivity.isRunning() && (tooFastToast == null || tooFastToast.getView() == null || !tooFastToast.getView().isShown())){
                tooFastToast = Toast.makeText(context, "Moving too fast", Toast.LENGTH_SHORT);
                tooFastToast.show();
            }

        }
        else{
            new new_position_request(loc).execute();
        }
    }

    private TimerTask updateGridTimerTask = new TimerTask() {

        boolean this_is_so_it_code_folds;

        @Override
        public void run() {
            // Get a handler that can be used to post to the main thread
            Handler mainHandler = new Handler(Looper.getMainLooper());
            Runnable myRunnable = new Runnable() {
                @Override
                public void run() {
                    Log.i(logtag, "Updating grid because of timer");
                    refreshGridFromServer();
                    if (current_location != null){
                        Log.i(logtag, "Updating location because of timer");
                        newLocation(current_location);
                    }

                }
            };
            mainHandler.post(myRunnable);

        }
    };


    public GoogleMap.OnCameraIdleListener cameraIdleListener = new GoogleMap.OnCameraIdleListener() {
        boolean so_i_can_fully_fold_code;

        @Override
        public void onCameraIdle() {

            Log.i(logtag, "Camera moved, zoom: " + String.valueOf(mMap.getCameraPosition().zoom));

            changeSquareVisibility();
            refreshGridFromServer();
        }
    };



    public class refreshGridFromServer extends AsyncTask<Void, Void, Void> {

        Bounds bounds;
        JSONObject response;
        JSONArray grid;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            grid_updating = true;
            grid_updates_in_progress += 1;
            Log.i(logtag, "Grid starting update from server");

            bounds  = new Bounds(mMap.getProjection().getVisibleRegion().latLngBounds);
        }

        @Override
        protected Void doInBackground(Void... params) {

            //-------------------------------------------------------------------------------
            // Make a minimum area to load, will always request at least this area of grid.
            //-------------------------------------------------------------------------------
            if (bounds.getWidth() <= min_width_of_grid_request && bounds.getWidth() > 0.0d){

                double min_height = min_width_of_grid_request * (bounds.getHeight() / bounds.getWidth());

                bounds = new Bounds(bounds.getCentre(), min_height, min_width_of_grid_request);
            }

            //-------------------------------------------------------------------------------
            // Get response from server for grid map for bounded area given
            //-------------------------------------------------------------------------------
            response = api.get_grid(bounds.getNW(), bounds.getSE());


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            try {

                if (response == null) {
                    Log.i(logtag, "Grid response null");
                    Toast.makeText(context, "Grid response Null", Toast.LENGTH_SHORT).show();

                } else if (!response.has("outcome") ||
                           response.getString("outcome") == "fail" ||
                           !response.has("grid")) {

                    Log.i(logtag, "Grid response error - " + response.toString());
                    Toast.makeText(context, "Grid response error", Toast.LENGTH_SHORT).show();

                } else {

                    grid = response.getJSONArray("grid");

                    //-------------------------------------------------------------------------------
                    // For every grid square in response, update it if it already exists in the map,
                    // or add it to the map if not.
                    // I like how when I say 'map' here, I mean both the google map and the hashmap
                    // at the same time.
                    //-------------------------------------------------------------------------------

                    for (int i = 0; i < grid.length(); i++) {
                        JSONArray square = (JSONArray) grid.get(i);

                        double lat = square.getDouble(0);
                        double lng = square.getDouble(1);
                        String team = null;
                        if (!square.isNull(2)) {
                            team = square.getString(2);
                        }
                        String item = null;
                        if (square.length() >= 5){
                            item = square.getString(4);
                        }

                        Integer level = square.getInt(3);


                        if (gridSquares.containsKeys(lat, lng)) {

                            gridSquares.get(lat, lng).setValues(team, level, item);

                        } else {

                            LatLngBounds newSquareBounds = new LatLngBounds(
                                    new LatLng(lat - latScale, lng),
                                    new LatLng(lat, lng + lngScale));

                            gridSquares.put(lat, lng, new GridSquare(mMap,
                                                                    newSquareBounds,
                                                                    team,
                                                                    level,
                                                                    item));
                        }

                    }

                    Log.i(logtag, "Grid updated from server");
                }

            } catch (JSONException e) {
                e.printStackTrace();
                Log.i(logtag, "JSONException while updating grid - " + e.toString());
            }


            grid_updating = false;
            grid_updates_in_progress -= 1;

            if (grid_update_pending == true){
                grid_update_pending = false;
                Log.i(logtag, "End of update, moving onto pending update");
                refreshGridFromServer();
            }

            changeSquareVisibility();

        }
    }

    public void refreshGridFromServer(){

        Log.i(logtag, "Updates in progress - " + String.valueOf(grid_updates_in_progress));

        if (!mainActivity.isRunning()){
            Log.i(logtag, "Activity paused, no grid updates");
        }
        else if (mMap.getCameraPosition().zoom < invisible_zoom){
            Log.i(logtag, "Zoomed too far out for grid request");
        }
        else if (grid_updates_in_progress >= 2){
            grid_update_pending = true;
            Log.i(logtag, "set grid pending as true");
        }
        else{
            new refreshGridFromServer().execute();
        }
    }



    public void changeSquareVisibility(){
        if (changing_square_visibility){
            change_square_visibility_pending = true;
        }

        Log.i(logtag, "Changing square visibility");

        //---------------------------------------------------------------------
        // Find bounds of screen with square scale allowance
        //---------------------------------------------------------------------
        Bounds camera_bounds = new Bounds(mMap.getProjection().getVisibleRegion().latLngBounds);
        double scale_increase = getScaleIncrease(mMap.getCameraPosition().zoom);

        Bounds cached_bounds = camera_bounds.multiplyBound(scale_increase);
        Bounds visible_bounds = camera_bounds.multiplyBound((scale_increase + 1) / 2);


        Log.i(logtag, "camera(" + camera_bounds.getWidth() + ", " + camera_bounds.getHeight() + ") "
                + "visible(" + visible_bounds.getWidth() + ", " + visible_bounds.getHeight() + ") "
                + "cached(" + cached_bounds.getWidth() + ", " + cached_bounds.getHeight() + ") ");


        int total = 0;
        int cached = 0;
        int visible = 0;
        int on_camera = 0;

        for (Iterators<GridSquare> it = gridSquares.getIterators(); it.hasNext(); ){
            GridSquare gs = it.next();

            total++;

            if (cached_bounds.contains(gs.getPosition(), latScale, lngScale)) {

                cached++;

                if (mMap.getCameraPosition().zoom > invisible_zoom
                        && visible_bounds.contains(gs.getPosition(), latScale, lngScale)) {

                    visible++;
                    gs.setVisible(true);
                    if (camera_bounds.contains(gs.getPosition(), latScale, lngScale)){
                        on_camera++;
                    }

                }
                else{
                    gs.setVisible(false);
                }


            } else {
                gs.removeFromMap();
                it.remove();
            }
        }


        String s = String.valueOf(on_camera) + "cam / " +
                String.valueOf(visible) + "vis / " +
                String.valueOf(cached) + "cach / " +
                String.valueOf(total) + "tot";
        Log.i(logtag, s);

        if (mMap.getCameraPosition().zoom <= invisible_zoom){
            Toast.makeText(context, "Zoom in to see grid", Toast.LENGTH_SHORT).show();
        }


        changing_square_visibility = false;
        if (change_square_visibility_pending){
            change_square_visibility_pending = false;
            changeSquareVisibility();
        }
    }



    public void changeSquareColour(LatLng loc, String team, int level){
        GridSquare gs = gridSquares.get(loc.latitude, loc.longitude);

        if (gs != null){
            gs.setValues(team, level, gs.getItem());
            Log.i(logtag, "Changed local square Team successfully!");
        }
        else{
            Log.i(logtag, "Looked for " + loc.toString());
            Log.i(logtag, "in " + gridSquares.toString());
            Log.i(logtag, "Failed to change local square Team");
        }
    }

    public static double getScaleIncrease(double zoom){
        double min_zoom_value = 16.0;
        double max_zoom_value = 18.0;

        double min_scale = 1.0;
        double max_scale = 2.0;

        double scale = min_scale + (
                ((zoom - min_zoom_value) / (max_zoom_value - min_zoom_value)) *
                        (max_scale - min_scale));

        if (scale > max_scale){
            scale = max_scale;
        }
        else if (scale < min_scale){
            scale = min_scale;
        }

        return scale;
    }


}
