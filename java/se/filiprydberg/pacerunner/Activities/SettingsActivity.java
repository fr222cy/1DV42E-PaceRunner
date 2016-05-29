package se.filiprydberg.pacerunner.Activities;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;


import se.filiprydberg.pacerunner.R;

public class SettingsActivity extends AppCompatActivity {
    private EditText sensitivityPicker;
    private EditText meterPicker;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        if(ab != null){
            ab.setDisplayHomeAsUpEnabled(true);
        }
        createFields();
        saveButton = (Button) findViewById(R.id.saveButton);

        saveButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                try{
                    if(sensitivityPicker.getText() != null && meterPicker.getText() != null){
                        int sensitivityValue = Integer.parseInt(sensitivityPicker.getText().toString());
                        int metersValue = Integer.parseInt(meterPicker.getText().toString());

                        if(validateMeters(metersValue) && validateSensitivity(sensitivityValue)){
                            sensitivityValue = Math.abs(sensitivityValue) * -1;
                            saveSettings(sensitivityValue, metersValue);
                            finish();
                        } else {
                            saveButton.setBackgroundResource(R.color.standardOrange);
                            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Sensitivity:10-60  | Meters:200-5000.", Snackbar.LENGTH_LONG);
                            snackbar.show();
                        }
                    } else{
                        //No values entered, exit activity
                        finish();
                    }
                }catch(Exception e){
                    e.printStackTrace();
                    saveButton.setBackgroundResource(R.color.standardOrange);
                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Enter both fields.", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            }
        });

    }
    // Found solution @ http://stackoverflow.com/questions/14095656/is-it-possible-to-change-the-increment-of-numberpicker-when-long-pressing
    private boolean validateSensitivity(int sens) {
        return sens >= 10 && sens <=60;
    }

    private boolean validateMeters(int meters) {
        return meters >= 200 && meters <= 5000;
    }

    private void createFields(){
        meterPicker = (EditText) findViewById(R.id.meters);
        sensitivityPicker = (EditText) findViewById(R.id.sensitivity);
        SharedPreferences sp = getSharedPreferences("PACERUNNER_SETTINGS", Activity.MODE_PRIVATE);

        sensitivityPicker.setText(String.valueOf(Math.abs(sp.getInt("NOTIFICATION_SENSITIVITY", -15))));
        sensitivityPicker.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent motionEvent) {
                sensitivityPicker.setText("");
                return false;
            }
        });
        meterPicker.setText(String.valueOf(sp.getInt("NOTIFICATION_METERS_BEFORE_START", 500)));
        meterPicker.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent motionEvent) {
                meterPicker.setText("");
                return false;
            }
        });
    }




    private void saveSettings(int sensitivityValue, int meters){

        SharedPreferences prefs = getSharedPreferences("PACERUNNER_SETTINGS", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("NOTIFICATION_SENSITIVITY", sensitivityValue);
        editor.putInt("NOTIFICATION_METERS_BEFORE_START", meters);
        editor.commit();

    }

}
