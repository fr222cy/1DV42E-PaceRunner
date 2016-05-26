package se.filiprydberg.pacerunner;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

/**
 * Created by Filip on 5/9/2016.
 */
public class NotificationSounds {
    private String[] notification = new String[]{
            "Jaws",
            "Psycho",
            "Dogs",
            "Beep"};
    SoundPool sp;
    private int firstSound;
    private int secondSound;
    private int firstSoundID;
    private int secondSoundID;
    private int previousSoundType;
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

        sp = new SoundPool(1, AudioManager.STREAM_MUSIC,0);
        firstSound = sp.load(context, resourceIdForFirstSound, 1);
        secondSound = sp.load(context, resourceIdForSecondSound,1);
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
