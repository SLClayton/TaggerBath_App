package uk.co.claytapp.taggerbath.Activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import uk.co.claytapp.taggerbath.R;

/**
 * Created by Sam on 11/03/2017.
 */

public class Help_Activity extends AppCompatActivity{

    private ActionBar actionBar;
    private Toolbar toolbar;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help_activity_layout);

        toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();

        actionBar.setTitle("How to play");
    }
}
