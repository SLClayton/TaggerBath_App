package uk.co.claytapp.taggerbath.Activities;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.facebook.login.widget.ProfilePictureView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import uk.co.claytapp.taggerbath.Game_Objects.User;
import uk.co.claytapp.taggerbath.Interfaces.ApiManager;
import uk.co.claytapp.taggerbath.R;

/**
 * Created by Sam on 02/03/2017.
 */

public class User_Leaderboard_Activity extends AppCompatActivity {

    private static final String logtag = "u_leaderboard_act";

    private Context context;

    private Toolbar toolbar;
    ActionBar actionBar;
    private Spinner score_spinner;
    private Spinner people_spinner;
    private ListView leaderboard;
    private TextView loading;
    private TextView none;

    private HashMap<String, String> friends;

    private static final String score_total = "All time";
    private static final String score_week = "Weekly";
    private static final String score_day = "Daily";
    private static final String score_hour = "Hourly";
    private static final String spm = "Score per minute";
    private static final String all_players = "All";
    private static final String friend_players = "Friends";

    private static final String first_selected = score_day;


    private String[] score_spinner_options = {score_total,
                                              score_week,
                                              score_day,
                                              score_hour,
                                              spm};

    private String[] people_spinner_options = {all_players,
                                               friend_players};




    private ApiManager api;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_leaderboard_activity);

        context = this;

        api = new ApiManager(this);

        friends = null;

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        score_spinner = (Spinner) findViewById(R.id.score_spinner);
        people_spinner =(Spinner) findViewById(R.id.people_spinner);
        leaderboard = (ListView) findViewById(R.id.leaderboard);
        loading = (TextView) findViewById(R.id.loading);
        none = (TextView) findViewById(R.id.none);

        none.setVisibility(View.GONE);

        setupToolbar();
        setupSpinners();

        if (Profile.getCurrentProfile() != null){
            new GraphRequest(
                    AccessToken.getCurrentAccessToken(),
                    "/" + Profile.getCurrentProfile().getId() + "/friends",
                    null,
                    HttpMethod.GET,
                    new GraphRequest.Callback() {

                        public void onCompleted(GraphResponse response) {
                            onFriendsCompleted(response);
                        }
                    }).executeAsync();
        }
        new update_leaderboard().execute();
    }





    @Override
    protected void onResume() {
        super.onResume();
    }

    public void setupToolbar() {
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();

        actionBar.setTitle("Leaderboard - " + first_selected);
    }

    public void setupSpinners(){

        score_spinner.setAdapter(new ArrayAdapter<String>(this,
                R.layout.spinner_dropdown_layout,
                score_spinner_options));

        people_spinner.setAdapter(new ArrayAdapter<String>(this,
                R.layout.spinner_dropdown_layout,
                people_spinner_options));


        score_spinner.setSelection(Arrays.asList(score_spinner_options).indexOf(first_selected));

        AdapterView.OnItemSelectedListener a = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                new update_leaderboard().execute();
                actionBar.setTitle("Leaderboard - " + score_spinner.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        };

        score_spinner.setOnItemSelectedListener(a);
        people_spinner.setOnItemSelectedListener(a);



    }

    private class update_leaderboard extends AsyncTask<Void, Void, Void> {

        JSONObject response;
        boolean error;
        ArrayList<User> users;

        String score_type;
        String players_type;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            people_spinner.setClickable(false);
            score_spinner.setClickable(false);
            people_spinner.setEnabled(false);
            score_spinner.setEnabled(false);


            leaderboard.setAdapter(null);
            loading.setVisibility(View.VISIBLE);
            none.setVisibility(View.GONE);

            score_type = score_spinner.getSelectedItem().toString();
            players_type = people_spinner.getSelectedItem().toString();
        }

        @Override
        protected Void doInBackground(Void... params) {

            ApiManager api = new ApiManager(context);
            response = null;


            ArrayList<String> wl = null;
            if (players_type.equals(friend_players)){
                wl = new ArrayList<>();

                if (Profile.getCurrentProfile() != null){

                    if (friends == null){
                        new GraphRequest(
                                AccessToken.getCurrentAccessToken(),
                                "/" + Profile.getCurrentProfile().getId() + "/friends",
                                null,
                                HttpMethod.GET,
                                new GraphRequest.Callback() {

                                    public void onCompleted(GraphResponse response) {
                                        onFriendsCompleted(response);
                                    }
                                }).executeAndWait();
                    }

                    wl = new ArrayList<String>(friends.keySet());
                    wl.add(Profile.getCurrentProfile().getId());
                }
            }


            switch (score_type){

                case spm:
                    response = api.get_leaderboard_spm(wl);
                    break;

                case score_hour:
                    response = api.get_leaderboard_score("HOUR", wl);
                    break;

                case score_day:
                    response = api.get_leaderboard_score("DAY", wl);
                    break;

                case score_week:
                    response = api.get_leaderboard_score("WEEK", wl);
                    break;

                default:
                    response = api.get_leaderboard_score(null, wl);
                    break;
            }



            try {

                if (response == null || !response.has("outcome")) {
                    Log.e(logtag, "Connection error, response null or no outcome");
                    error = true;

                }
                else if (!response.getString("outcome").equals("success")) {
                    Log.e(logtag, "Outcome not successful");
                    error = true;

                }
                else {

                    JSONArray a = response.getJSONArray("leaderboard");

                    users = new ArrayList<>();
                    for (int i = 0; i < a.length(); i++) {

                        JSONArray b = a.getJSONArray(i);
                        users.add(new User(context,
                                b.getString(0),
                                b.getString(1),
                                b.getString(2),
                                b.getInt(3)));
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            loading.setVisibility(View.GONE);
            if (users == null || users.size() <= 0){
                none.setVisibility(View.VISIBLE);
            }
            else if (error != true) {
                leaderboard.setAdapter(new leaderboardListAdapter(getApplicationContext(), users));
            }
            else{
                none.setVisibility(View.VISIBLE);
            }

            people_spinner.setClickable(true);
            score_spinner.setClickable(true);
            people_spinner.setEnabled(true);
            score_spinner.setEnabled(true);
        }
    }


    public class leaderboardListAdapter extends ArrayAdapter{

        String me;

        public leaderboardListAdapter(Context context, ArrayList<User> USERS) {
            super(context, new Integer(2762), USERS);

            me = null;
            if (Profile.getCurrentProfile() != null){
                me = Profile.getCurrentProfile().getId();
            }
        }

        public String getPlace(int place){
            String s = String.valueOf(place);
            int l = s.length();

            String last2 = null;
            if (l > 1){
                last2 =  s.substring(l - 2);
            }

            String last = s.substring(l - 1);

            if (last2 != null && (last2.equals("11") || last2.equals("12") || last2.equals("13"))){
                return s + "th";
            }
            else if (last.equals("1")){
                return s + "st";
            }
            else if (last.equals("2")){
                return s + "nd";
            }
            else if (last.equals("3")){
                return s + "rd";
            }
            else {
                return s + "th";
            }
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            //---------------------------------------------------------------
            // Creates views for each row of the leaderboard with user
            // profile picture and team colour
            //---------------------------------------------------------------


            LayoutInflater inflater = LayoutInflater.from(this.getContext());
            View row = inflater.inflate(R.layout.leaderboard_row, parent, false);

            User u = (User) getItem(position);

            TextView place = (TextView) row.findViewById(R.id.place);
            TextView name = (TextView) row.findViewById(R.id.player_name);
            ProfilePictureView pp = (ProfilePictureView) row.findViewById(R.id.pp);
            TextView score = (TextView) row.findViewById(R.id.score);
            LinearLayout box = (LinearLayout) row.findViewById(R.id.item_box);

            place.setText(getPlace(position + 1));

            name.setText(u.getName());
            pp.setProfileId(u.getFb_id());
            score.setText(NumberFormat.getIntegerInstance().format(u.getScore()));

            if (me != null &&  u.getFb_id().equals(me)){

                GradientDrawable my_row = (GradientDrawable) getResources()
                        .getDrawable(u.getListBackground()).getConstantState()
                        .newDrawable().mutate();

                my_row.setStroke(8, Color.BLACK);
                box.setBackground(my_row);
            }
            else{
                box.setBackground(getResources().getDrawable(u.getListBackground()));
            }



            return row;
        }
    }

    public void onFriendsCompleted(GraphResponse response){
        if (response.getError() != null){
            return;
        }

        try {

            JSONArray list = response.getJSONObject().getJSONArray("data");

            friends = new HashMap<>();

            for (int i = 0; i < list.length(); i++) {
                JSONObject friend = list.getJSONObject(i);
                friends.put(friend.getString("id"), friend.getString("name"));
            }


        } catch (JSONException e) {
            e.printStackTrace();
            Log.i(logtag, "Error getting friends from response");
        }
    }
}
