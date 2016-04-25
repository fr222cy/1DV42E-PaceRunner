package se.filiprydberg.pacerunner;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.graphics.Color;
import android.widget.RelativeLayout;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SetPace extends AppCompatActivity {
    private static final String TAG = "MyActivity";
    private Button startButton;
    private EditText averagePace;
    private String submittedPace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_pace);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        averagePace = (EditText) findViewById(R.id.averagePace);

        startButton = (Button) findViewById(R.id.startButton);

            startButton.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    submittedPace = averagePace.getText().toString();

                    if(isValidPace(submittedPace)) {
                        Intent intent = new Intent(getApplicationContext(), navigation.class);
                        intent.putExtra("pace", submittedPace);
                        startActivity(intent);
                    }
                    else {
                        startButton.setBackgroundColor(Color.RED);
                        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),"You need to enter a valid pace. eg: 5:00, 6:59, 14:23", Snackbar.LENGTH_LONG );
                        snackbar.show();
                    }

                }
            });


    }

    /* Valdidating the pace that has been set.
    * Valid Ones: 3:25, 09:55 14:33
    * Not Valid Ones: 001:43, 40:40, 9:61.*/
    private boolean isValidPace(String submittedPace) {
        Matcher matcher;
        Pattern pattern;

        pattern = Pattern.compile("([01]?[0-9]):[0-5][0-9]");
        matcher = pattern.matcher(submittedPace);
        if(submittedPace == null) {
            return true;
        }
        else {
            return matcher.matches();
        }
    }

}
