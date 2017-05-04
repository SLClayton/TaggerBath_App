package uk.co.claytapp.taggerbath.Activities;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import uk.co.claytapp.taggerbath.Interfaces.ApiManager;
import uk.co.claytapp.taggerbath.R;

/**
 * Created by Sam on 17/02/2017.
 */

public class sign_up_dialog extends Activity {

    private EditText editText;
    private TextView error_message;
    private Button sign_up_button;

    private String email;
    private ApiManager api;

    private int min_length = 3;
    private static final String logtag ="Sign_up_dialog";

    private String allowed_chars = "0123456789" +
            "abcdefghijklmnopqrstuvwxyz" +
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
            "_-";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.sign_up_dialog);
        this.setFinishOnTouchOutside(false);

        email = null;
        get_email();

        api = new ApiManager(this);

        editText = (EditText) findViewById(R.id.editText);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                checkUsername(s);
            }
        });
        error_message = (TextView) findViewById(R.id.error_message);
        error_message.setText("");
        sign_up_button = (Button) findViewById(R.id.sign_up_button);
        sign_up_button.setClickable(false);




    }

    public void get_email(){
        //---------------------------------------------------------------
        // Retrieve user email from facebook server
        //---------------------------------------------------------------

        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        Profile profile = Profile.getCurrentProfile();

        // Facebook Email address
        GraphRequest r = GraphRequest.newMeRequest(accessToken,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {

                        Log.v("LoginActivity Response ", response.toString());

                        try {
                            email = object.getString("email");

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email,gender, birthday");
        r.setParameters(parameters);
        r.executeAsync();
    }

    public void checkUsername(CharSequence s){

        //---------------------------------------------------------------
        // Check username is okay and fits parameters
        //---------------------------------------------------------------

        sign_up_button.setClickable(false);

        ArrayList<Character> illegals = new ArrayList<>();

        for (int i=0; i<s.length(); i++){

            char this_char = s.charAt(i);

            if (!allowed_chars.contains(this_char + "") && !illegals.contains(this_char)){
                illegals.add(this_char);
                sign_up_button.setClickable(false);
            }
        }

        if (illegals.size() <= 0 && s.length() >= min_length) {
            sign_up_button.setClickable(true);
            error_message.setText("");
        }
        else{
            sign_up_button.setClickable(false);

            String m = "";

            if (illegals.size() > 0){
                m += "Illegal character/s\n";

                for (int i=0; i< illegals.size(); i++){
                    m += "  " + illegals.get(i);
                }

                m += "\n";
            }


            if (s.length() < min_length){
                m += "Min length " + String.valueOf(min_length);
            }

            error_message.setText(m);
        }




    }

    public void signUp(View view){
        if (email != null){
            error_message.setText("Requesting...");
            new signUp(editText.getText().toString(), email, this).execute();
        }
    }

    public class signUp extends AsyncTask<Void, Void, Void>{


        //---------------------------------------------------------------
        // Attempt to create account, connecting to game server
        //---------------------------------------------------------------

        String name;
        String email;
        Activity activity;

        JSONObject response;

        public signUp(String NAME, String EMAIL, Activity ACTIVITY){
            name = NAME;
            email = EMAIL;
            activity = ACTIVITY;
        }

        @Override
        protected Void doInBackground(Void... params) {
            response = api.create_user(name, email);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            error_message.setText("");

            try {

                if (response != null && response.has("outcome")){

                    if (response.getString("outcome").equals("success")){
                        activity.setResult(Activity.RESULT_OK);
                        activity.finish();
                    }
                    else if (response.has("error_code") && response.getInt("error_code") == 1){
                        error_message.setText("Name already taken\nTry another");
                    }
                    else if (response.has("error_code") && response.getInt("error_code") == 3){
                        error_message.setText("Invalid name, try again");
                    }
                }
                else {
                    Log.e(logtag, "Error with response, either null or no outcome field");
                    error_message.setText("Unknown error please try again.");
                }
            } catch (JSONException e) {
                Log.e(logtag, "Error with response, JSON Exception - " + response.toString());
                e.printStackTrace();
            }
        }
    }




}
