package uk.co.claytapp.taggerbath.Activities;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;

import uk.co.claytapp.taggerbath.R;

/**
 * Created by Sam on 15/04/2017.
 */
public class Repeating_Activity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);

        setContentView(R.layout.repeating_layout);
    }
}
