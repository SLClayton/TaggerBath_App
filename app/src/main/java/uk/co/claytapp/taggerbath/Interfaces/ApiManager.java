package uk.co.claytapp.taggerbath.Interfaces;

import android.content.Context;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.Profile;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;

import uk.co.claytapp.taggerbath.R;


/**
 * Created by Sam on 29/01/2017.
 */

public class ApiManager {

    private static final String logtag = "ApiManager";

    private Context context;
    private String url;

    public ApiManager(Context c){
        context = c;
        url = context.getString(R.string.api_url);
    };

    public JSONObject get_grid(LatLng tl, LatLng br) {

        JSONObject request = new JSONObject();

        try {
            request.put("request_id", new Integer(619));
            request.put("request_type", "get_grid");

            request.put("nw_lat", tl.latitude);
            request.put("nw_lng", tl.longitude);
            request.put("se_lat", br.latitude);
            request.put("se_lng", br.longitude);


        } catch (JSONException e) {
            e.printStackTrace();
        }

        return getResponse(url, request);
    }

    public JSONObject new_position(LatLng pos){
        JSONObject request = new JSONObject();

        try {
            request.put("request_id", new Integer(619));
            request.put("request_type", "new_position");

            request.put("fb_id", Profile.getCurrentProfile().getId());
            request.put("userAccessToken", AccessToken.getCurrentAccessToken().getToken());

            request.put("nw_lat", pos.latitude);
            request.put("nw_lng", pos.longitude);



        } catch (JSONException e) {
            e.printStackTrace();
        }

        return getResponse(url, request);
    }

    public JSONObject get_user_info(){
        JSONObject request = new JSONObject();

        try {
            request.put("request_id", new Integer(619));
            request.put("request_type", "get_user_info");

            request.put("fb_id", Profile.getCurrentProfile().getId());
            request.put("userAccessToken", AccessToken.getCurrentAccessToken().getToken());


        } catch (JSONException e) {
            e.printStackTrace();
        }

        return getResponse(url, request);
    }

    public JSONObject get_leaderboard_spm(ArrayList<String> whitelist){
        JSONObject request = new JSONObject();

        try {
            request.put("request_id", new Integer(619));
            request.put("request_type", "get_leaderboard_spm");

            if (whitelist != null){
                JSONArray ja = new JSONArray();
                for (int i=0; i<whitelist.size(); i++){
                    ja.put(whitelist.get(i));
                }

                request.put("whitelist", ja);
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

        return getResponse(url, request);
    }

    public JSONObject get_leaderboard_score(String timescale,  ArrayList<String> whitelist) {
        JSONObject request = new JSONObject();

        try {
            request.put("request_id", new Integer(619));
            request.put("request_type", "get_leaderboard_score");

            if (whitelist != null){
                JSONArray ja = new JSONArray();
                for (int i=0; i<whitelist.size(); i++){
                    ja.put(whitelist.get(i));
                }

                request.put("whitelist", ja);
            }

            if (timescale != null){
                request.put("timescale", timescale.toUpperCase());
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

        return getResponse(url, request);
    }

    public JSONObject get_leaderboard_team_spm(){
        JSONObject request = new JSONObject();

        try {
            request.put("request_id", new Integer(619));
            request.put("request_type", "get_leaderboard_team_spm");


        } catch (JSONException e) {
            e.printStackTrace();
        }

        return getResponse(url, request);
    }

    public JSONObject get_leaderboard_team_score(String timescale){
        JSONObject request = new JSONObject();

        try {
            request.put("request_id", new Integer(619));
            request.put("request_type", "get_leaderboard_team_score");

            if (timescale != null){
                request.put("timescale", timescale.toUpperCase());
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

        return getResponse(url, request);
    }

    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    public JSONObject getResponse(String url_string, JSONObject request){

        long t0 = System.currentTimeMillis();

        URL url = null;
        JSONObject JSON = null;
        try {
            String type = request.getString("request_type").toUpperCase();
            System.out.println(type + " REQUEST #### " + request.toString());


            url = new URL(url_string);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");

            OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
            out.write(request.toString());
            out.flush();
            out.close();

            int res = connection.getResponseCode();

            Log.i(logtag, type + " RESPONSE CODE: " + String.valueOf(res));

            if (res == 500){
                return null;
            }

            InputStream is = connection.getInputStream();

            String json_string = convertStreamToString(is);

            is.close();

            connection.disconnect();

            JSON = new JSONObject(json_string);

            long elapsed = System.currentTimeMillis() - t0;

            Log.i(logtag, type + " RESPONSE #### Time: " + String.valueOf(Double.valueOf(elapsed)/1000) + JSON.toString());



        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return JSON;
    }

    public JSONObject create_user(String name, String email){


        JSONObject request = new JSONObject();

        try {
            request.put("request_id", new Integer(619));
            request.put("request_type", "create_user");

            request.put("fb_id", Profile.getCurrentProfile().getId());
            request.put("userAccessToken", AccessToken.getCurrentAccessToken().getToken());

            request.put("name", name);
            request.put("email", email);


        } catch (JSONException e) {
            e.printStackTrace();
        }

        return getResponse(url, request);
    }


}
