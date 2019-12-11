package com.example.musicalearsandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    //Variable Declarations
    Button play;
    Button replay;
    Button minor2nd;
    Button major2nd;
    Button minor3rd;
    Button major3rd;
    Button perfect4th;
    Button tritone;
    Button perfect5th;
    Button minor6th;
    Button major6th;
    Button minor7th;
    Button major7th;
    Button octave;
    TextView score;

    int random = 0; //Shows randomly selected audio
    int totalScore = 0;
    int correctScore = 0;
    boolean playEnabled;
    boolean replayEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialize buttons
        play = findViewById(R.id.play);
        replay = findViewById(R.id.replay);
        minor2nd = findViewById(R.id.m2);
        major2nd = findViewById(R.id.M2);
        minor3rd = findViewById(R.id.m3);
        major3rd = findViewById(R.id.M3);
        perfect4th = findViewById(R.id.p4);
        tritone = findViewById(R.id.tri);
        perfect5th = findViewById(R.id.p5);
        minor6th = findViewById(R.id.m6);
        major6th = findViewById(R.id.M6);
        minor7th = findViewById(R.id.m7);
        major7th = findViewById(R.id.M7);
        octave = findViewById(R.id.p8);

        replay.setEnabled(false);

        // check for recovering the instance state
        if (savedInstanceState !=null){
            random = savedInstanceState.getInt("random");
            correctScore = savedInstanceState.getInt("correctScore");
            totalScore = savedInstanceState.getInt("totalScore");
            playEnabled = savedInstanceState.getBoolean("playEnabled");
            replayEnabled = savedInstanceState.getBoolean("replayEnabled");

            if (playEnabled == false) {
                play.setEnabled(false);
            }
            if (replayEnabled == true) {
                replay.setEnabled(true);
            }
        }

        //Initialize Score
        score = findViewById(R.id.score);
        score.setText(correctScore + "/" + totalScore);


        //Initialize mediaplayers
        //From Developer Website https://developer.android.com/guide/topics/media/mediaplayer
        final MediaPlayer play1 = MediaPlayer.create(this, R.raw.minor2);
        final MediaPlayer play2 = MediaPlayer.create(this, R.raw.major2);
        final MediaPlayer play3 = MediaPlayer.create(this, R.raw.minor3);
        final MediaPlayer play4 = MediaPlayer.create(this, R.raw.major3);
        final MediaPlayer play5 = MediaPlayer.create(this, R.raw.perfect4);
        final MediaPlayer play6 = MediaPlayer.create(this, R.raw.tritone);
        final MediaPlayer play7 = MediaPlayer.create(this, R.raw.perfect5);
        final MediaPlayer play8 = MediaPlayer.create(this, R.raw.minor6);
        final MediaPlayer play9 = MediaPlayer.create(this, R.raw.major6);
        final MediaPlayer play10 = MediaPlayer.create(this, R.raw.minor7);
        final MediaPlayer play11 = MediaPlayer.create(this, R.raw.major7);
        final MediaPlayer play12 = MediaPlayer.create(this, R.raw.octave);

        replay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Stop, release, and replay mediaplayer
                switch (random) {
                    case 1:
                        Log.i("debug", "play1");
                        try {
                            play1.stop();
                            play1.prepare();
                            play1.start();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 2:
                        Log.i("debug", "play2");
                        try {
                            play2.stop();
                            play2.prepare();
                            play2.start();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 3:
                        Log.i("debug", "play3");
                        try {
                            play3.stop();
                            play3.prepare();
                            play3.start();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 4:
                        Log.i("debug", "play4");
                        try {
                            play4.stop();
                            play4.prepare();
                            play4.start();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 5:
                        Log.i("debug", "play5");
                        try {
                            play5.stop();
                            play5.prepare();
                            play5.start();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 6:
                        Log.i("debug", "play6");
                        try {
                            play6.stop();
                            play6.prepare();
                            play6.start();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 7:
                        Log.i("debug", "play7");
                        try {
                            play7.stop();
                            play7.prepare();
                            play7.start();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 8:
                        Log.i("debug", "play8");
                        try {
                            play8.stop();
                            play8.prepare();
                            play8.start();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 9:
                        Log.i("debug", "play9");
                        try {
                            play9.stop();
                            play9.prepare();
                            play9.start();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 10:
                        Log.i("debug", "play10");
                        try {
                            play10.stop();
                            play10.prepare();
                            play10.start();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 11:
                        Log.i("debug", "play11");
                        try {
                            play11.stop();
                            play11.prepare();
                            play11.start();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 12:
                        Log.i("debug", "play12");
                        try {
                            play12.stop();
                            play12.prepare();
                            play12.start();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                }
            }
        });

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //1=minor2nd, 2=major2nd, 3=minor3rd, etc...
                // Random number from https://stackoverflow.com/questions/21049747/how-can-i-generate-a-random-number-in-a-certain-range/21049922
                random = new Random().nextInt(12) + 1;
                Log.i("randomWav", String.valueOf(random));
                play.setEnabled(false);
                replay.setEnabled(true);

                switch (random) {
                    case 1:
                        Log.i("debug", "play1");
                        play1.start();
                        break;
                    case 2:
                        Log.i("debug", "play2");
                        play2.start();
                        break;
                    case 3:
                        Log.i("debug", "play3");
                        play3.start();
                        break;
                    case 4:
                        Log.i("debug", "play4");
                        play4.start();
                        break;
                    case 5:
                        Log.i("debug", "play5");
                        play5.start();
                        break;
                    case 6:
                        Log.i("debug", "play6");
                        play6.start();
                        break;
                    case 7:
                        Log.i("debug", "play7");
                        play7.start();
                        break;
                    case 8:
                        Log.i("debug", "play8");
                        play8.start();
                        break;
                    case 9:
                        Log.i("debug", "play9");
                        play9.start();
                        break;
                    case 10:
                        Log.i("debug", "play10");
                        play10.start();
                        break;
                    case 11:
                        Log.i("debug", "play11");
                        play11.start();
                        break;
                    case 12:
                        Log.i("debug", "play12");
                        play12.start();
                        break;
                }
            }
        });
    }

    public void clickedIntervalButton(View view) {

        int buttonTag = Integer.parseInt((String)view.getTag());

        Log.i("Tag", String.valueOf(buttonTag));

        if (random == buttonTag) {
            correctClick();
        } else {
            wrongClick();
        }

    }

    private void wrongClick() {
        Log.i("answer", "wrong");

        //StackOverFlow on how to change Toast BGcolor https://stackoverflow.com/questions/31175601/how-can-i-change-default-toast-message-color-and-background-color-in-android
        Toast toast = Toast.makeText(this, "Wrong!", Toast.LENGTH_SHORT);
        View view = toast.getView();

        //Gets the actual oval background of the Toast then sets the colour filter
        view.setBackgroundResource(R.color.toastWrongBG);
        TextView text = (TextView) view.findViewById(android.R.id.message);
        text.setTextColor(Color.parseColor("#ffffff"));

        toast.show();
        totalScore++;
        score.setText(correctScore + "/" + totalScore);
    }

    private void correctClick() {
        Log.i("answer", "right");
        //StackOverFlow on how to change Toast BG/text color https://stackoverflow.com/questions/31175601/how-can-i-change-default-toast-message-color-and-background-color-in-android
        Toast toast = Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT);
        View view = toast.getView();

        //Gets the actual oval background of the Toast then sets the colour filter
        view.setBackgroundResource(R.color.toastRightBG);
        TextView text = (TextView) view.findViewById(android.R.id.message);
        text.setTextColor(Color.parseColor("#ffffff"));

        toast.show();
        correctScore++;
        totalScore++;
        score.setText(correctScore + "/" + totalScore);
        play.setEnabled(true);
    }

    // invoked when the activity may be temporarily destroyed, save the instance state here
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("random", random);
        outState.putInt("correctScore", correctScore);
        outState.putInt("totalScore", totalScore);
        outState.putBoolean("playEnabled", play.isEnabled());
        outState.putBoolean("replayEnabled", replay.isEnabled());
    }
}
