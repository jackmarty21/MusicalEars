package com.example.musicalears;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;

import static com.example.musicalears.Note.adjustedNoteList;
import static com.example.musicalears.Note.noteList;

public class PitchMatchingActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private boolean shouldListen = false;

    private static TargetNote randomNote = null;
    private CountDownTimer accuracyTimer = null;
    private int score;
    private TextView statusText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pitch_matching);
        statusText = findViewById(R.id.statusText);

        checkPermissions();
    }

    private void startApp() {
        Log.d("pitch matching", "started");
        Log.d("randomNote", String.valueOf(randomNote));
        shouldListen = true;
        statusText.setText(R.string.mic_on);

        AudioDispatcher dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050, 1024, 0);
        PitchDetectionHandler pitchDetectionHandler = new PitchDetectionHandler() {
            @Override
            public void handlePitch(PitchDetectionResult pitchDetectionResult, AudioEvent audioEvent) {
                final float pitchInHz = pitchDetectionResult.getPitch();
                final float probability = pitchDetectionResult.getProbability();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (shouldListen) {
                            float frequency = processPitch(pitchInHz, probability);

                            if (randomNote != null) {
                                if (frequency > 0 && checkAccuracy(frequency)) {
                                    startTimer();
                                } else {
                                    stopTimer();
                                }
                            } else {
                                stopTimer();
                            }
                        }
                    }
                });
            }
        };

        AudioProcessor pitchProcessor = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, pitchDetectionHandler);
        dispatcher.addAudioProcessor(pitchProcessor);

        Thread audioThread = new Thread(dispatcher, "Audio Thread");
        audioThread.start();

        ImageButton playButton = findViewById(R.id.playButton);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playRandomTone();
            }
        });
    }

    private void stopTimer() {
        if (accuracyTimer != null) {
            ImageView imageView = findViewById(R.id.imageView);
            //The following transition implementation is based on an online tutorial, found at:
            //https://riptutorial.com/android/example/21224/add-transition-or-cross-fade-between-two-images-
            TransitionDrawable transition = new TransitionDrawable(new Drawable[] {
                    getResources().getDrawable(R.drawable.circle_green),
                    getResources().getDrawable(R.drawable.circle_blue)

            });
            imageView.setImageDrawable(transition);
            transition.startTransition(100);
            Log.d("timer", "stop");

            accuracyTimer.cancel();
            accuracyTimer = null;
        }
    }

    private void startTimer() {
        if (accuracyTimer == null) {
            accuracyTimer = new CountDownTimer(3000, 300) {

                @Override
                public void onTick(long l) {
                    Log.d("tick", String.valueOf(l));
                }

                @Override
                public void onFinish() {
                    scorePointAndReset();
                }
            }.start();

            //while timer is ticking, turn bubble from blue to green
            //The following transition implementation is based on an online tutorial, found at:
            //https://riptutorial.com/android/example/21224/add-transition-or-cross-fade-between-two-images-
            ImageView imageView = findViewById(R.id.imageView);
            TransitionDrawable transition = new TransitionDrawable(new Drawable[] {
                    getResources().getDrawable(R.drawable.circle_blue),
                    getResources().getDrawable(R.drawable.circle_green)
            });
            imageView.setImageDrawable(transition);
            transition.startTransition(3000);
            Log.d("timer", "start");
        }
    }

    private void scorePointAndReset() {
        final TextView upperText = findViewById(R.id.octaveUpperText);
        final TextView lowerText = findViewById(R.id.octaveLowerText);
        final View upperLimit = findViewById(R.id.targetUpperLimit);
        final View lowerLimit = findViewById(R.id.targetLowerLimit);
        score++;
        TextView scoreText = findViewById(R.id.scoreText);
        scoreText.setText(score + " pts");

        //nullify values and constraints, reset for new note
        randomNote = null;
        upperText.setText(noteList.get(noteList.size() - 1).getNoteName());
        lowerText.setText(noteList.get(0).getNoteName());
        ConstraintLayout.LayoutParams upperLimitLayoutParams = (ConstraintLayout.LayoutParams) upperLimit.getLayoutParams();
        ConstraintLayout.LayoutParams lowerLimitLayoutParams = (ConstraintLayout.LayoutParams) lowerLimit.getLayoutParams();
        upperLimitLayoutParams.verticalBias = 0;
        lowerLimitLayoutParams.verticalBias = 1;
        upperLimit.setLayoutParams(upperLimitLayoutParams);
        lowerLimit.setLayoutParams(lowerLimitLayoutParams);
    }

    private boolean checkAccuracy(float frequency) {
        if (frequency < randomNote.getTargetUpperTwenty() &&
                frequency > randomNote.getTargetLowerTwenty()) {
            return true;
        } else {
            return false;
        }
    }

    private void playRandomTone() {
        final TextView noteText = findViewById(R.id.noteText);
        final TextView upperText = findViewById(R.id.octaveUpperText);
        final TextView lowerText = findViewById(R.id.octaveLowerText);
        shouldListen = false;
        statusText.setText("");

        int randomNoteIndex;

        if (randomNote != null) {
            randomNoteIndex = 5;
            randomNote = new TargetNote(randomNoteIndex,
                    adjustedNoteList.get(randomNoteIndex).getNoteName(),
                    adjustedNoteList.get(randomNoteIndex).getNoteFrequency(),
                    adjustedNoteList.get(randomNoteIndex).getNoteResource());
        } else {
            randomNoteIndex = (int) Math.floor(Math.random()*12);
            randomNote = new TargetNote(randomNoteIndex,
                    noteList.get(randomNoteIndex).getNoteName(),
                    noteList.get(randomNoteIndex).getNoteFrequency(),
                    noteList.get(randomNoteIndex).getNoteResource());
        }

        Log.d("randomNoteId", String.valueOf(randomNote.getNoteId()));
        Log.d("randomNoteName", String.valueOf(randomNote.getNoteName()));
        Log.d("randomNoteFreq", String.valueOf(randomNote.getNoteFrequency()));

        randomNote.adjustNotes(randomNoteIndex);

        noteText.setText(randomNote.getNoteName());
        upperText.setText(adjustedNoteList.get(7).getNoteName());
        lowerText.setText(adjustedNoteList.get(3).getNoteName());


        addTargetBounds();

        final MediaPlayer mp = MediaPlayer.create(this, randomNote.getNoteResource());
        mp.start();

        //prevents the app from 'listening' to itself
        new Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        shouldListen = true;
                        statusText.setText("Now listening through your microphone.");
                    }
                }, 1500);

    }

    private void addTargetBounds() {

        final View upperLimit = findViewById(R.id.targetUpperLimit);
        final View lowerLimit = findViewById(R.id.targetLowerLimit);

        float targetFrequency = adjustedNoteList.get(5).getNoteFrequency();
        float nextFrequency = adjustedNoteList.get(6).getNoteFrequency();
        float prevFrequency = adjustedNoteList.get(4).getNoteFrequency();
//        find value 20 cents above
        float targetUpperFrequency = targetFrequency + (nextFrequency - targetFrequency)/2;
        Log.d("upperTwenty", String.valueOf(targetUpperFrequency));
        randomNote.setTargetUpperTwenty(targetUpperFrequency);

//        find value 20 cents below
        float targetLowerFrequency = targetFrequency + (prevFrequency - targetFrequency)/2;
        Log.d("lowerTwenty", String.valueOf(targetLowerFrequency));
        randomNote.setTargetLowerTwenty(targetLowerFrequency);


        float targetUpperBias = getBias(targetUpperFrequency,
                adjustedNoteList.get(3).getNoteFrequency(),
                adjustedNoteList.get(7).getNoteFrequency());
        Log.d("upperTwentyBias", String.valueOf(targetUpperBias));

        float targetLowerBias = getBias(targetLowerFrequency,
                adjustedNoteList.get(3).getNoteFrequency(),
                adjustedNoteList.get(7).getNoteFrequency());
        Log.d("lowerTwentyBias", String.valueOf(targetLowerBias));

        ConstraintLayout.LayoutParams upperLimitLayoutParams = (ConstraintLayout.LayoutParams) upperLimit.getLayoutParams();
        ConstraintLayout.LayoutParams lowerLimitLayoutParams = (ConstraintLayout.LayoutParams) lowerLimit.getLayoutParams();
        upperLimitLayoutParams.verticalBias = targetUpperBias;
        lowerLimitLayoutParams.verticalBias = targetLowerBias;

        upperLimit.setLayoutParams(upperLimitLayoutParams);
        lowerLimit.setLayoutParams(lowerLimitLayoutParams);


    }

    private float processPitch(float pitchInHz, float probability) {
        TextView noteText = findViewById(R.id.noteText);
        float frequency = -1;

        //filter out background noise, smooth out UI
        if (probability > .85) {
            frequency = pitchInHz;
            int octave = 0;

            while (frequency > noteList.get(noteList.size() - 1).getNoteFrequency()) {
                if (frequency < 32) {
                     frequency = noteList.get(noteList.size() - 1).getNoteFrequency();
                } else {
                    frequency /= 2.0;
                    octave++;
                    if (frequency < noteList.get(0).getNoteFrequency()) {
                        frequency = noteList.get(0).getNoteFrequency();
                    }
                }
            }

            float minDistance = (float) 10000.0;
            int index = 0;

            for (int i=0; i<noteList.size(); i++) {
                float distance = Math.abs(noteList.get(i).getNoteFrequency() - frequency);
                if (distance < minDistance) {
                    index = i;
                    minDistance = distance;
                }
            }
            moveImage(frequency, index);
        }
        return frequency;
    }

    private void moveImage(float frequency, int index) {
        final TextView noteText = findViewById(R.id.noteText);
        final ImageView image = findViewById(R.id.imageView);
        final float upperBoundsFrequency;
        final float lowerBoundsFrequency;
        //if the app is expecting a certain tone, the scale should be +-200c
        //otherwise, reset the scale to span the entire octave.
        if (randomNote != null) {
            upperBoundsFrequency = adjustedNoteList.get(7).getNoteFrequency();
            lowerBoundsFrequency = adjustedNoteList.get(3).getNoteFrequency();
            noteText.setText(adjustedNoteList.get(5).getNoteName());
        } else {
            upperBoundsFrequency = noteList.get(noteList.size() - 1).getNoteFrequency();
            lowerBoundsFrequency = noteList.get(0).getNoteFrequency();
            noteText.setText(noteList.get(index).getNoteName()) ;
        }

        final float bias = getBias(frequency, lowerBoundsFrequency, upperBoundsFrequency);
        ConstraintLayout.LayoutParams imageParams = (ConstraintLayout.LayoutParams) image.getLayoutParams();
        imageParams.verticalBias = bias;
        image.setLayoutParams(imageParams);
    }

    private float getBias(float frequency, float lowerBoundsFrequency, float upperBoundsFrequency) {
        float bias = (float) (1 - (frequency - lowerBoundsFrequency)/(upperBoundsFrequency - lowerBoundsFrequency));
        if (bias < 0) {
            bias = 0;
        } else if (bias > 1) {
            bias = 1;
        }
        return bias;
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(PitchMatchingActivity.this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            //permission not granted
            ActivityCompat.requestPermissions(PitchMatchingActivity.this,
                    new String[] {Manifest.permission.RECORD_AUDIO},
                    PERMISSIONS_REQUEST_RECORD_AUDIO);
        } else {
            //permission granted
            Log.d("permission", "GRANTED");
            startApp();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("permission", "GRANTED");
                startApp();
            } else {
                Log.d("permission", "DENIED");
            }
        }
    }
}
