package uk.co.claytapp.taggerbath.Activities;

import android.content.Context;
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

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;

import uk.co.claytapp.taggerbath.Game_Objects.Team;
import uk.co.claytapp.taggerbath.Game_Objects.User;
import uk.co.claytapp.taggerbath.Interfaces.ApiManager;
import uk.co.claytapp.taggerbath.R;

import static android.view.View.GONE;

/**
 * Created by Sam on 02/03/2017.
 */

public class Team_Leaderboard_Activity extends AppCompatActivity {

    private static final String logtag = "team_leaderboard_act";

    private ApiManager api;

    private Context context;

    private Toolbar toolbar;
    private ActionBar actionBar;
    private Spinner scoreSpinner;
    private ListView leaderboard;
    private PieChart piechart;
    private TextView loading;
    private TextView none;


    private static final String score_total = "All time";
    private static final String score_week = "Weekly";
    private static final String score_day = "Daily";
    private static final String score_hour = "Hourly";
    private static final String spm = "Score per minute";

    private static final String first_selected = score_day;

    private String[] score_spinner_options = {score_total,
            score_week,
            score_day,
            score_hour,
            spm};

    private ArrayList<Team> teams;

    private ArrayList<PieEntry> entries;
    private ArrayList<String> pie_entry_labels;
    private ArrayList<Integer> pie_entry_colors;

    PieDataSet pieDataSet;
    PieData pieData;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.team_leaderboard_activity);

        context = this;

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        scoreSpinner = (Spinner) findViewById(R.id.score_spinner);
        leaderboard = (ListView) findViewById(R.id.leaderboard);
        piechart = (PieChart) findViewById(R.id.piechart);
        loading = (TextView) findViewById(R.id.loading);
        none = (TextView) findViewById(R.id.none);

        loading.setVisibility(GONE);
        none.setVisibility(GONE);
        piechart.setVisibility(GONE);
        scoreSpinner.setEnabled(false);
        api = new ApiManager(this);
        setupToolbar();
        setupSpinners();

        setupPieChart();


        new update_leaderboard().execute();
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    public void setupToolbar() {
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();

        actionBar.setTitle("Leaderboard");
    }

    public void setupSpinners(){

        //---------------------------------------------------------------
        // Create spinners with leaderboard options
        //---------------------------------------------------------------

        scoreSpinner.setAdapter(new ArrayAdapter<String>(this,
                R.layout.spinner_dropdown_layout,
                score_spinner_options));

        scoreSpinner.setSelection(Arrays.asList(score_spinner_options).indexOf(first_selected));

        AdapterView.OnItemSelectedListener a = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                new update_leaderboard().execute();
                actionBar.setTitle("Leaderboard - " + scoreSpinner.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        };

        scoreSpinner.setOnItemSelectedListener(a);
    }

    public void setupPieChart(){



        piechart.setUsePercentValues(false);
        piechart.setDrawHoleEnabled(true);
        piechart.setRotationAngle(0);
        piechart.setRotationEnabled(true);
        piechart.getLegend().setEnabled(false);
        piechart.getDescription().setEnabled(false);
    }

    private class update_leaderboard extends AsyncTask<Void, Void, Void> {

        JSONObject response;
        String score_type;
        boolean error;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            leaderboard.setAdapter(null);
            none.setVisibility(GONE);
            loading.setVisibility(View.VISIBLE);
            piechart.setVisibility(GONE);
            scoreSpinner.setEnabled(false);
            score_type = scoreSpinner.getSelectedItem().toString();

        }

        @Override
        protected Void doInBackground(Void... params) {

            //---------------------------------------------------------------
            // Different request is made depending on options
            //---------------------------------------------------------------

            response = null;

            switch (score_type){
                case spm:
                    response = api.get_leaderboard_team_spm();
                    break;

                case score_hour:
                    response = api.get_leaderboard_team_score("HOUR");
                    break;

                case score_day:
                    response = api.get_leaderboard_team_score("DAY");
                    break;

                case score_week:
                    response = api.get_leaderboard_team_score("WEEK");
                    break;

                default:
                    response = api.get_leaderboard_team_score(null);
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

                    teams = new ArrayList<>();
                    entries = new ArrayList<PieEntry>();
                    pie_entry_labels = new ArrayList<>();
                    pie_entry_colors = new ArrayList<>();

                    for (int i = 0; i < a.length(); i++) {

                        JSONArray b = a.getJSONArray(i);
                        String team = b.getString(0);
                        int score = b.getInt(1);

                        if (!team.equals("null")){
                            teams.add(new Team(team, score));

                            entries.add(new PieEntry(score, i));
                            pie_entry_labels.add(team);
                            pie_entry_colors.add(User.getColor(context, team));
                        }
                    }

                }

            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(logtag, "JSONException - " + e.toString());
            }

            pieDataSet = new PieDataSet(entries, "Piechart");
            pieDataSet.setColors(pie_entry_colors);
            pieData = new PieData(pieDataSet);
            pieData.setValueTextSize(14f);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            loading.setVisibility(GONE);

            if (teams != null && teams.size() > 0 && error != true){
                leaderboard.setAdapter(new leaderboardListAdapter(getApplicationContext(), teams));

                piechart.setVisibility(View.VISIBLE);

                piechart.setData(pieData);
                piechart.invalidate();

            }
            else {
                none.setVisibility(View.VISIBLE);
            }

            scoreSpinner.setEnabled(true);
        }
    }


    public class leaderboardListAdapter extends ArrayAdapter{


        public leaderboardListAdapter(Context context, ArrayList<Team> TEAMS) {
            super(context, new Integer(2762), TEAMS);
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
            // Creates and fills the rows of the leaderboard depending on
            //  score and team colour
            //---------------------------------------------------------------


            LayoutInflater inflater = LayoutInflater.from(this.getContext());
            View row = inflater.inflate(R.layout.leaderboard_team_row, parent, false);

            TextView place = (TextView) row.findViewById(R.id.place);
            TextView teamName = (TextView) row.findViewById(R.id.team_name);
            TextView score = (TextView) row.findViewById(R.id.score);
            LinearLayout box = (LinearLayout) row.findViewById(R.id.item_box);

            Team t = (Team) getItem(position);

            place.setText(getPlace(position + 1));

            String team = t.getTeam();
            teamName.setText(team.substring(0, 1).toUpperCase() + team.substring(1) + " team");

            score.setText(NumberFormat.getIntegerInstance().format(t.getScore()));

            box.setBackground(getResources().getDrawable(User.getListBackground(t.getTeam())));

            return row;
        }
    }



}
