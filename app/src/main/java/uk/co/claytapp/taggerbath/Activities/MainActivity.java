package uk.co.claytapp.taggerbath.Activities;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import uk.co.claytapp.taggerbath.Game_Objects.User;
import uk.co.claytapp.taggerbath.Interfaces.ApiManager;
import uk.co.claytapp.taggerbath.Interfaces.MapController;
import uk.co.claytapp.taggerbath.R;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String logtag = "MainActivity";

    private static final int LOCATION_PERMISSION_REQUEST = 32;
    private static final int NEW_SIGNUP_ID = 43;

    private boolean isRunning;

    public Context context;
    private GoogleMap gmap;
    private Toolbar toolbar;
    private TextView playerName;
    private TextView playerTeam;
    private TextView spm;
    private ImageView playerPhoto;
    private LinearLayout signed_in_hud;
    private FrameLayout signed_out_hud;
    private Switch camera_lock;

    private boolean was_signed_in;


    private LoginButton loginButton;
    CallbackManager callbackManager;

    NotificationManager notificationManager;

    private User user;

    private Timer checkLoginStatusTimer;
    private int time_between_check_status = 10000;

    private boolean run_in_background;

    private ApiManager api;

    private MapController map;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        isRunning = true;
        run_in_background = true;

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        context = this;
        api = new ApiManager(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setupToolbar();

        playerName = (TextView) findViewById(R.id.PlayerName);
        playerTeam = (TextView) findViewById(R.id.PlayerTeam);
        playerPhoto = (ImageView) findViewById(R.id.pp);
        spm = (TextView) findViewById(R.id.spm);
        signed_out_hud = (FrameLayout) findViewById(R.id.signed_out_hud);
        signed_in_hud = (LinearLayout) findViewById(R.id.signed_in_hud);
        camera_lock = (Switch) findViewById(R.id.camera_lock);


        //---------------------------------------------------------------
        // FB login button stuff
        //---------------------------------------------------------------
        callbackManager = CallbackManager.Factory.create();
        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList("email", "user_friends"));
        loginButton.registerCallback(callbackManager, facebookCallback);
        accessTokenTracker.startTracking();


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.googlemap);
        mapFragment.getMapAsync(this);

        checkLoginStatusTimer = new Timer();
        checkLoginStatusTimer.schedule(checkLoginStatusTask, 0, time_between_check_status);


        setupNotification();



    }

    public void setupNotification(){

        //---------------------------------------------------------------
        // Sets up notifications to tell user to go out and play at 1:12pm
        //---------------------------------------------------------------

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 13);
        calendar.set(Calendar.MINUTE, 12);
        calendar.set(Calendar.SECOND, 00);



        Intent intent = new Intent(getApplicationContext(), Notification_receiver.class);

        PendingIntent pi = PendingIntent.getBroadcast(getApplicationContext(), 100, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);

    }


    @Override
    protected void onResume() {
        super.onResume();
        isRunning = true;

        checkLoginStatus();

        if (map != null){
            map.setupLocationMonitoring();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isRunning = false;

        //---------------------------------------------------------------
        // Either carry on tracking or not depending on settings
        //---------------------------------------------------------------
        if (map !=null && !run_in_background) {
            map.cancelLocationMonitoring();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        accessTokenTracker.stopTracking();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gmap = googleMap;
        map = new MapController(context, gmap);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == NEW_SIGNUP_ID){
            getUserInfo();
        }

        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void setupToolbar() {
        setSupportActionBar(toolbar);
        ActionBar a = getSupportActionBar();

        a.setTitle(R.string.app_name);
    }



    private FacebookCallback<LoginResult> facebookCallback = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {
            Toast.makeText(context, "Facebook login successful!", Toast.LENGTH_SHORT).show();
            checkLoginStatus();
        }

        @Override
        public void onCancel() {
            Toast.makeText(context, "Facebook login cancelled", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(FacebookException exception) {
            Toast.makeText(context, "Error during Facebook login", Toast.LENGTH_SHORT).show();
        }

    };


    private TimerTask checkLoginStatusTask = new TimerTask() {

        boolean this_is_so_it_code_folds;

        @Override
        public void run() {
            // Get a handler that can be used to post to the main thread
            Handler mainHandler = new Handler(Looper.getMainLooper());
            Runnable myRunnable = new Runnable() {
                @Override
                public void run() {
                    if (isRunning){
                        checkLoginStatus();
                    }

                }
            };
            mainHandler.post(myRunnable);

        }
    };


    public void checkLoginStatus(){
        if (!isRunning){
            return;
        }

        //---------------------------------------------------------------
        // Checks FB login status and changes hud accordingly
        //---------------------------------------------------------------

        if (Profile.getCurrentProfile() == null){
            user = null;
            playerPhoto.setImageBitmap(null);
            if (map != null){
                map.setLocationUpdates(false);
            }
            was_signed_in = false;
            signed_in_hud.setVisibility(View.GONE);
            signed_out_hud.setVisibility(View.VISIBLE);
        }
        else{
            Log.i(logtag, "User logged in as " + Profile.getCurrentProfile().getId());

            if (!was_signed_in){
                new getFBPhoto().execute();
            }
            getUserInfo();
            if (map != null){
                map.setLocationUpdates(true);
            }
            was_signed_in = true;
            signed_in_hud.setVisibility(View.VISIBLE);
            signed_out_hud.setVisibility(View.GONE);
        }

    }


    AccessTokenTracker accessTokenTracker = new AccessTokenTracker() {
        boolean so_i_can_fold;

        @Override
        protected void onCurrentAccessTokenChanged(AccessToken old, AccessToken current) {
            checkLoginStatus();
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_activity, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent i = null;

        //---------------------------------------------------------------
        // Toolbar menu options
        //---------------------------------------------------------------

        switch (id){

            case R.id.logout:
                LoginManager.getInstance().logOut();
                checkLoginStatus();
                break;

            case R.id.menu_leaderboard:
                Intent a = new Intent(getApplicationContext(), User_Leaderboard_Activity.class);
                startActivity(a);
                break;

            case R.id.menu_team_leaderboard:
                i = new Intent(getApplicationContext(), Team_Leaderboard_Activity.class);
                startActivity(i);
                break;

            case R.id.help:
                i = new Intent(getApplicationContext(), Help_Activity.class);
                startActivity(i);
                break;

            case R.id.menu_items:
                if (Profile.getCurrentProfile() != null) {
                    i = new Intent(getApplicationContext(), Item_Activity.class);
                    startActivity(i);
                }
                else{
                    Toast.makeText(this, "Please sign in", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.runinbackground:
                if (run_in_background){
                    item.setTitle("Run in background: Off");
                    run_in_background = false;
                }
                else{
                    item.setTitle("Run in background: On");
                    run_in_background = true;
                }
        }

        return super.onOptionsItemSelected(item);
    }

    public void requestLocationPermission(){
        Log.i(logtag, "PERMISSIONS REQUESTED \n\n\n\n\n ######\n\n\n");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION,
                            android.Manifest.permission.INTERNET},
                    LOCATION_PERMISSION_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        if (requestCode == LOCATION_PERMISSION_REQUEST){
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(context, "Permission Granted", Toast.LENGTH_SHORT).show();
                Log.i(logtag, "PERMISSIONS GRANTED \n\n\n\n\n ######\n\n\n");

                if (map != null){
                    map.setupLocationMonitoring();
                }

                startActivity(new Intent(context, Help_Activity.class));

                // permission was granted, yay! Do the
                // contacts-related task you need to do.

            } else {

                Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show();
                Log.i(logtag, "PERMISSIONS DENIED \n\n\n\n\n ######\n\n\n");
                if (map != null){
                    map.cancelLocationMonitoring();
                }
            }
            return;
        }

        return;
    }

    public boolean isRunning(){
        return isRunning;
    }

    public class getFBPhoto extends AsyncTask<Void, Void, Void> {

        Bitmap bm;

        @Override
        protected Void doInBackground(Void... params) {
            bm = getFacebookProfilePicture(Profile.getCurrentProfile().getId());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            playerPhoto.setImageBitmap(bm);
        }
    }

    public static Bitmap getFacebookProfilePicture(String userID){
        URL imageURL = null;
        Bitmap bitmap = null;
        try {
            imageURL = new URL("https://graph.facebook.com/" + userID + "/picture?type=large");
            bitmap = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return bitmap;
    }

    public class getUserInfo extends AsyncTask<Void, Void, Void> {

        //---------------------------------------------------------------
        // Retrieve user data from game server
        //---------------------------------------------------------------

        JSONObject response;

        private void serverError() {
            Toast.makeText(context, "Server error, try again later", Toast.LENGTH_LONG).show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            response = api.get_user_info();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            user = null;

            try {
                if (response == null) {
                    Log.e(logtag, "Null response");
                }
                else if (!response.has("outcome")) {
                    Log.e(logtag, "Error in response, no outcome - " + response.toString());
                }
                else if (!response.getString("outcome").equals("success")) {

                    if (response.has("error_code") && response.getInt("error_code") == 2) {
                        newSignUp();
                    } else {
                        Log.e(logtag, "Error in response, non success - " + response.toString());
                    }

                }
                else {
                    user = new User(context,
                            response.getString("name"),
                            response.getString("fb_id"),
                            response.getString("team"),
                            response.getInt("spm"));


                    playerName.setText(user.getName());
                    playerTeam.setText(user.getTeam().substring(0, 1).toUpperCase() +
                            user.getTeam().substring(1) + " Team");

                    spm.setText(String.valueOf(user.getScore()));

                    signed_in_hud.setBackground(getResources().getDrawable(user.getListBackground()));

                }

            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(logtag, "get_user_info JSON Exception - " + response.toString());
            }
        }

    }


    public void getUserInfo(){
        if (isRunning && Profile.getCurrentProfile() != null && AccessToken.getCurrentAccessToken() != null){
            new getUserInfo().execute();
        }
    }


    public void newSignUp(){
        if (this.isRunning) {
            Intent i = new Intent(this, sign_up_dialog.class);
            startActivityForResult(i, NEW_SIGNUP_ID);
        }
    }

    public User getUser(){
        return user;
    }

    public Switch getCamera_lock(){
        return camera_lock;
    }
}