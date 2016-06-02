package se.filiprydberg.pacerunner;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.speech.tts.TextToSpeech;

import org.w3c.dom.Text;

import java.util.Locale;

/**
 * Created by Filip on 5/9/2016.
 */
public class NotificationSounds  {
    private String[] notification = new String[]{
            "Jaws",
            "Zombie",
            "Violin",
            "Dogs",
            "Beep"};
    SoundPool sp;
    private int firstSound;
    private int secondSound;
    private int firstSoundID;
    private int secondSoundID;
    private int previousSoundType;
    private String[] SpeechNotifications = {"You are under your minimum average pace. Run faster!",
                                            "You are now over your minimum average pace. Good job! "};
    private TextToSpeech tts;

    public String[] getNotificationArray() {
        return notification;
    }

    //Retrieve the sound files from the resource.
    //Creates a Soundpool and load the files. That are used in playSoundByType()
    public void setMediaPlayerSounds(String soundName, Context context){

        int resourceIdForFirstSound = context.getResources().getIdentifier(
                soundName.toLowerCase() + "1", "raw", context.getPackageName());
        int resourceIdForSecondSound = context.getResources().getIdentifier(
                soundName.toLowerCase() + "2", "raw", context.getPackageName());

        sp = new SoundPool(1, AudioManager.STREAM_ALARM,0);
        firstSound = sp.load(context, resourceIdForFirstSound, 1);
        secondSound = sp.load(context, resourceIdForSecondSound,1);

        tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR){
                    tts.setLanguage(Locale.US);
                }
            }
        });
    }
    //Plays a sound, based on the gap to the users average pace.
    public void playSoundByType(int soundType){

        if(previousSoundType == soundType){
            return;
        }
        try {
            switch (soundType){
                case 1: {
                    if(firstSound != 0)
                        firstSoundID = sp.play(firstSound,1,1,1,-1,1);
                        break;
                }
                case 2: {
                    if(secondSound != 0)
                        secondSoundID = sp.play(secondSound,1,1,1,-1,1);
                        break;
                }
                case 3: {
                    tts.speak(SpeechNotifications[0], TextToSpeech.QUEUE_FLUSH, null);
                }
                case 0: {
                    if(firstSoundID != 0){
                        sp.stop(firstSoundID);
                    }
                    if(secondSoundID != 0){
                        sp.stop(secondSoundID);
                    }
                    break;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        previousSoundType = soundType;
    }


}
