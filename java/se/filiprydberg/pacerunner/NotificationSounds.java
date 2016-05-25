package se.filiprydberg.pacerunner;

import android.content.Context;
import android.media.MediaPlayer;

/**
 * Created by Filip on 5/9/2016.
 */
public class NotificationSounds {
    private String[] notification = new String[]{
            "Jaws",
            "Psycho",
            "Chased by Dogs",
            "Beep",
            "Fishmouth"};
    private MediaPlayer firstSound;
    private MediaPlayer secondSound;
    private int previousSoundType;
    public String[] getNotificationArray() {
        return notification;
    }

    public void setMediaPlayerSounds(String soundName, Context context){
        int resourceIdForFirstSound = context.getResources().getIdentifier(
                soundName.toLowerCase() + 1, "raw", context.getPackageName());

        firstSound = MediaPlayer.create(context, resourceIdForFirstSound);
        firstSound.setLooping(true);

        int resourceIdForSecondSound = context.getResources().getIdentifier(
                soundName.toLowerCase() + 2, "raw", context.getPackageName());

        secondSound = MediaPlayer.create(context, resourceIdForSecondSound);
        secondSound.setLooping(true);
    }

    public void playSoundByType(int soundType){

        if(previousSoundType == soundType){
            return;
        }

        if(soundType == 1){
            if(secondSound.isPlaying()){
                secondSound.stop();
            }
            firstSound.start();
        }

        if(soundType == 2){
            if(firstSound.isPlaying()){
                firstSound.stop();
            }
            secondSound.start();
        }
        if(soundType == 0){
            if(firstSound.isPlaying() || secondSound.isPlaying()){
                firstSound.stop();
                secondSound.stop();
            }
        }

        previousSoundType = soundType;

    }


}
