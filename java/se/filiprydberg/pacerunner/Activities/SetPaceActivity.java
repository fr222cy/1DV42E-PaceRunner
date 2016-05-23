package se.filiprydberg.pacerunner.Activities;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.graphics.Color;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.NumberPicker;

import se.filiprydberg.pacerunner.Activities.SessionActivity;
import se.filiprydberg.pacerunner.NotificationSounds;
import se.filiprydberg.pacerunner.R;


public class SetPaceActivity extends AppCompatActivity  {
    private static final String TAG = "MyActivity";
    private Button startButton;
    private EditText averagePace;
    private int submittedMinute;
    private int submittedSecond;
    private NumberPicker minutePicker;
    private NumberPicker secondPicker;
    private ListView notificationList;
    private MediaPlayer soundPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_pace);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        NotificationSounds notificationSounds = new NotificationSounds();

        String[] notifications = notificationSounds.getNotificationArray();

        notificationList = getListView(notifications);

        choiceCallback();
        setTimePickers();

        startButton = (Button) findViewById(R.id.startButton);



            startButton.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    submittedMinute = minutePicker.getValue();
                    submittedSecond = secondPicker.getValue();


                    if (isValidTime(submittedMinute, submittedSecond)) {
                        Intent intent = new Intent(getApplicationContext(), SessionActivity.class);
                        intent.putExtra("MINUTE", submittedMinute);
                        intent.putExtra("SECOND", submittedSecond);

                        Intent previousIntent= getIntent();

                        //Pass the latest known LATLNG to the Runner class.
                        intent.putExtra("LATITUDE", previousIntent.getDoubleExtra("LATITUDE", 0));
                        intent.putExtra("LONGITUDE", previousIntent.getDoubleExtra("LONGITUDE", 0));

                        startActivity(intent);
                    } else {
                        startButton.setBackgroundColor(Color.RED);
                        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "You need to enter a valid pace, try again.", Snackbar.LENGTH_LONG);
                        snackbar.show();
                    }
                }
            });
    }

    public void setTimePickers() {
        minutePicker = (NumberPicker) findViewById(R.id.minutePicker);
        minutePicker.setMinValue(1);
        minutePicker.setMaxValue(20);

        secondPicker = (NumberPicker) findViewById(R.id.secondPicker);
        secondPicker.setMinValue(0);
        secondPicker.setMaxValue(59);
        secondPicker.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int i) {
                return String.format("%02d", i);
            }
        });
    }

    /* Validating the pace that has been set.
    * Valid minute 1-20
    * Valid second 0-59.*/
    private boolean isValidTime(int minute, int second) {
        return minute > 0 && minute <=20 && second >= 0 && second < 60;
    }

    private ListView getListView(String[] notifications){
        ListAdapter la = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, notifications);

        notificationList = (ListView) findViewById(R.id.notificationList);
        notificationList.setAdapter(la);
        return notificationList;
    }

    private void choiceCallback(){

        notificationList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String notificationChoice = String.valueOf(parent.getItemAtPosition(position));
                previewChoice(notificationChoice);
            }
        });
    }

    private void previewChoice(String notificationChoice){
            if(soundPreview != null){
                soundPreview.stop();
            }
        switch (notificationChoice){
            case "Jaws": {
                soundPreview = MediaPlayer.create(this, R.raw.jaws2);
                soundPreview.start();
            }
            default: {

                break;
            }
        }
    }

}
