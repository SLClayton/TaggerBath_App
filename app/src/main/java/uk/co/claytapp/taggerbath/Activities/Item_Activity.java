package uk.co.claytapp.taggerbath.Activities;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.Profile;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import uk.co.claytapp.taggerbath.Game_Objects.Item;
import uk.co.claytapp.taggerbath.Game_Objects.User;
import uk.co.claytapp.taggerbath.Interfaces.ApiManager;
import uk.co.claytapp.taggerbath.R;

/**
 * Created by Sam on 08/03/2017.
 */

public class Item_Activity extends AppCompatActivity {

    private final static String logtag = "Item_Activity";

    private ActionBar actionBar;
    private Toolbar toolbar;

    private ApiManager api;
    private User user;

    private GridView grid;

    private ArrayList<Item> items;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_activity_layout);

        api = new ApiManager(this);

        grid = (GridView) findViewById(R.id.grid);
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        setupToolbar();

        //---------------------------------------------------------------
        // Load user items and setup
        //---------------------------------------------------------------
        setItems();


    }

    @Override
    protected void onResume() {
        super.onResume();
        new getUserInfo().execute();
    }

    public void setupToolbar() {
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();

        actionBar.setTitle("Your Items");
    }

    public void setItems(){
        items = new ArrayList<>();

        //---------------------------------------------------------------
        // get all items into a arraylist
        //---------------------------------------------------------------

        items.add(new Item(this, "duck", 0));
        items.add(new Item(this, "uni", 0));
        items.add(new Item(this, "rugby", 0));
        items.add(new Item(this, "bath", 0));
        items.add(new Item(this, "lollipop", 0));
        items.add(new Item(this, "star", 0));
        items.add(new Item(this, "trophy", 0));
        items.add(new Item(this, "beer", 0));

        grid.setAdapter(new item_grid_adapter(getApplicationContext(), items));

    }


    public class getUserInfo extends AsyncTask<Void, Void, Void> {

        JSONObject response;


        @Override
        protected Void doInBackground(Void... voids) {

            //---------------------------------------------------------------
            // Retrieve user items from game server
            //---------------------------------------------------------------

            response = api.get_user_info();

            try {
                if (response != null && response.has("outcome")){
                    if (response.getString("outcome").equals("success")){


                        //---------------------------------------------------------------
                        // Fill user object with data
                        //---------------------------------------------------------------

                        user = new User();
                        user.setName(response.getString("name"));
                        user.setTeam(response.getString("team"));
                        user.setFb_id(Profile.getCurrentProfile().getId());
                        user.setEmail(response.getString("email"));

                        JSONObject itemsObject = response.getJSONObject("items");

                        for (int i=0; i<items.size(); i++){
                            Item item = items.get(i);

                            if (itemsObject.has(item.getItem())){
                                item.setAmount(itemsObject.getInt(item.getItem()));
                            }
                            else{
                                item.setAmount(0);
                            }
                        }


                    }
                }
                else{
                    Log.e(logtag, "get_user_info repsonse NULL or no outcome field");
                    if (response != null){
                        Log.e(logtag, response.toString());
                    }
                }
            }
            catch (JSONException e) {
                e.printStackTrace();
                Log.e(logtag, "get_user_info JSON Exception - " + response.toString());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            Parcelable listState =  grid.onSaveInstanceState();


            grid.setAdapter(new item_grid_adapter(getApplicationContext(), items));

            if (listState != null) {
                grid.onRestoreInstanceState(listState);
            }
        }
    }




    public class item_grid_adapter extends ArrayAdapter {

        //---------------------------------------------------------------
        // Fills the grid with images of the items and the values
        // (the amounts) the user has
        //---------------------------------------------------------------


        public item_grid_adapter(Context context, ArrayList<Item> ITEMS) {
            super(context, new Integer(2762), ITEMS);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(this.getContext());
            View row = inflater.inflate(R.layout.grid_item, parent, false);

            TextView item = (TextView) row.findViewById(R.id.itemName);
            TextView amount = (TextView) row.findViewById(R.id.amount);
            ImageView image = (ImageView) row.findViewById(R.id.imageView);

            Item i = (Item) getItem(position);

            item.setText(i.getItem());
            amount.setText(String.valueOf(i.getAmount()));


            //---------------------------------------------------------------
            // Dim if amount is 0
            //---------------------------------------------------------------
            image.setImageDrawable(i.getItemImage());
            if (i.getAmount() <= 0){
                image.setAlpha(0.1f);
            }
            else{
                image.setAlpha(1f);
            }

            return row;
        }
    }

}
