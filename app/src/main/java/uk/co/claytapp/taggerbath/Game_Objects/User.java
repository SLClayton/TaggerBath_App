package uk.co.claytapp.taggerbath.Game_Objects;

import android.content.Context;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import uk.co.claytapp.taggerbath.R;

/**
 * Created by Sam on 04/02/2017.
 */

public class User {

    private static final String logtag = "UserClass";

    private Context context;

    private String name;
    private String team;
    private String email;
    private String fb_id;
    private int score;

    public User(){
    }

    public User(Context c, String NAME, String FB_ID, String TEAM, int SCORE){
        context = c;
        name= NAME;
        fb_id = FB_ID;
        team = TEAM;
        score = SCORE;
    }


    public String getEmail(){
        return this.email;
    }

    public int getScore(){
        return this.score;
    }

    public String getTeam(){
        return this.team;
    }

    public String getFb_id(){
        return this.fb_id;

    }

    public String getName(){
        return this.name;
    }


    public void setTeam(String TEAM){
        this.team = TEAM;
    }

    public void setName(String NAME){
        this.name = NAME;
    }

    public void setEmail(String EMAIL){
        this.email = EMAIL;
    }

    public void setFb_id(String FB_ID){
        this.fb_id = FB_ID;
    }

    public int getColor(){
        return getColor(context, team);
    }

    public static int getColor(Context c, String TEAM){
        if (TEAM == null){
            TEAM = "null";
        }
        switch (TEAM){
            case ("red"):
                return ContextCompat.getColor(c, R.color.list_item_red);
            case ("green"):
                return ContextCompat.getColor(c, R.color.list_item_green);
            case ("blue"):
                return ContextCompat.getColor(c, R.color.list_item_blue);
            default:
                return ContextCompat.getColor(c, R.color.list_item_grey);
        }
    }

    public int getListBackground(){
        return getListBackground(team);
    }

    public static int getListBackground(String TEAM){
        switch (TEAM){
            case ("red"):
                return R.drawable.list_item_red;
            case ("green"):
                return R.drawable.list_item_green;
            case ("blue"):
                return R.drawable.list_item_blue;
            default:
                return R.drawable.list_item_grey;
        }
    }

    public BitmapDescriptor getMarkerIcon(){
        return BitmapDescriptorFactory.fromResource(getMarkerIcon(team));
    }

    public static int getMarkerIcon(String TEAM){
        if (TEAM == null){
            return R.drawable.marker_grey;
        }
        switch (TEAM){
            case ("red"):
                return R.drawable.marker_red;
            case ("green"):
                return R.drawable.marker_green;
            case ("blue"):
                return R.drawable.marker_blue;
            default:
                return R.drawable.marker_grey;
        }
    }



}
