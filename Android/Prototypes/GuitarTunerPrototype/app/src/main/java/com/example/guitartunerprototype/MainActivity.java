package com.example.guitartunerprototype;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;


public class MainActivity extends AppCompatActivity {
    private int STORAGE_PERMISSION_CODE = 1;
    private static final int MY_PERMISSION = 0;

    private static float[] noteFrequencies = new float[] {(float) 16.35, (float) 17.32, (float) 18.35, (float) 19.45, (float) 20.6, (float) 21.83, (float) 23.12, (float) 24.5, (float) 25.96, (float) 27.5, (float) 29.14, (float) 30.87};
    private static final String[] noteNames = new String[] {"C", "C♯", "D", "D♯", "E", "F", "F♯", "G", "G♯", "A", "A♯", "B"};
    private static int[] noteResources = new int[] {R.raw.c4, R.raw.csh4, R.raw.d4, R.raw.dsh4, R.raw.e4, R.raw.f4, R.raw.fsh4, R.raw.g3, R.raw.gsh3, R.raw.a3, R.raw.bb3, R.raw.b3};

    private static float[] adjustedNoteFrequencies;
    private static String[] adjustedNoteNames;
    private static int[] adjustedNoteResources;

    private static float baseFrequency;
    private static float targetLowerFrequency;
    private static float targetUpperFrequency;
    private static int score = 0;
    private static boolean timerExists = false;
    private static boolean shouldListen = true;
    private static CountDownTimer accuracyTimer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //This app uses TarsosDSP to achieve its real-time audio processing capabilities.
        //The TarsosDSP Github repo can be found here: https://github.com/JorenSix/TarsosDSP,
        //and documentation can be found at: https://0110.be/releases/TarsosDSP/TarsosDSP-latest/TarsosDSP-latest-Documentation/

        AudioDispatcher dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050, 1024, 0);

        //The following implementation is based on an online tutorial titled:
        //"Pitch Detection in Android using TarsosDSP". It can be found at:
        //https://medium.com/@juniorbump/pitch-detection-in-android-using-tarsosdsp-a2dd4a3f04e9
        PitchDetectionHandler pdh = new PitchDetectionHandler() {
            @Override
            public void handlePitch(PitchDetectionResult pitchDetectionResult, AudioEvent audioEvent) {
                final float pitchInHz = pitchDetectionResult.getPitch();
                final float probability = pitchDetectionResult.getProbability();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (shouldListen) {
                            processPitch(pitchInHz, probability);

                            if (adjustedNoteNames != null && pitchInHz > 0 && probability > 0.8 ) {
                                if (checkAccuracy()) {
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

        AudioProcessor pitchProcessor = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, pdh);
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

        //At some point, permissions for microphone usage have to be handled.
        //This button initiates the permission check, for further implementation later.
        Button permButton = findViewById(R.id.permButton);
        permButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "You have already granted this permission!", Toast.LENGTH_SHORT).show();

                } else {
                    requestPermission();
                }
            }
        });
    }

    private void startTimer() {
        if (!timerExists) {
            timerExists = true;
            accuracyTimer = new CountDownTimer(3000, 300) {
                @Override
                public void onTick(long l) {
                    Log.d("tick", String.valueOf(l));
                }

                @Override
                public void onFinish() {
                    final TextView upperText = findViewById(R.id.octaveUpperText);
                    final TextView lowerText = findViewById(R.id.octaveLowerText);
                    final View upperLimit = findViewById(R.id.targetUpperLimit);
                    final View lowerLimit = findViewById(R.id.targetLowerLimit);
                    score++;
                    TextView scoreText = findViewById(R.id.scoreText);
                    scoreText.setText(score + " pts");
                    Toast.makeText(MainActivity.this, "Yay! You scored a point!", Toast.LENGTH_SHORT).show();

                    //nullify values and constraints, reset for new note
                    adjustedNoteNames = null;
                    adjustedNoteFrequencies = null;
                    adjustedNoteResources = null;
                    upperText.setText(noteNames[noteNames.length - 1]);
                    lowerText.setText(noteNames[0]);
                    ConstraintLayout.LayoutParams upperLimitLayoutParams = (ConstraintLayout.LayoutParams) upperLimit.getLayoutParams();
                    ConstraintLayout.LayoutParams lowerLimitLayoutParams = (ConstraintLayout.LayoutParams) lowerLimit.getLayoutParams();
                    upperLimitLayoutParams.verticalBias = 0;
                    lowerLimitLayoutParams.verticalBias = 1;
                    upperLimit.setLayoutParams(upperLimitLayoutParams);
                    lowerLimit.setLayoutParams(lowerLimitLayoutParams);
                }
            }.start();

            //while timer is ticking, turn bubble from blue to green
            //The following transition implementation is based on an online tutorial, found at:
            //https://riptutorial.com/android/example/21224/add-transition-or-cross-fade-between-two-images-
            ImageView image = findViewById(R.id.imageView);
            TransitionDrawable transition = new TransitionDrawable(new Drawable[] {
                    getResources().getDrawable(R.drawable.circle_blue),
                    getResources().getDrawable(R.drawable.circle_green)
            });
            image.setImageDrawable(transition);
            transition.startTransition(3000);
            Log.d("timer", "start");
        }
    }

    private void stopTimer() {
        if (accuracyTimer != null) {
            accuracyTimer.cancel();
            accuracyTimer = null;

            //The following transition implementation is based on an online tutorial, found at:
            //https://riptutorial.com/android/example/21224/add-transition-or-cross-fade-between-two-images-
            ImageView image = findViewById(R.id.imageView);
            TransitionDrawable transition = new TransitionDrawable(new Drawable[] {
                    getResources().getDrawable(R.drawable.circle_green),
                    getResources().getDrawable(R.drawable.circle_blue)

            });
            image.setImageDrawable(transition);
            transition.startTransition(100);
            Log.d("timer", "stop");
            timerExists = false;
        }
    }

    private boolean checkAccuracy() {
//        Log.d("base frequency", String.valueOf(baseFrequency));
//        Log.d("upper bound frequency", String.valueOf(targetUpperFrequency));
//        Log.d("lower bound frequency", String.valueOf(targetLowerFrequency));
        if (baseFrequency != -1 && baseFrequency < targetUpperFrequency && baseFrequency > targetLowerFrequency) {
            return true;
        } else {
            return false;
        }
    }

    private void playRandomTone() {
        shouldListen = false;
        int randomNote;
        int resource;
        final TextView noteText = findViewById(R.id.noteText);

        //until user correctly matches the pitch, the same note will play,
        //and not a new 'random' note.
        if (adjustedNoteNames != null) {
            randomNote = 5;
            resource = adjustedNoteResources[randomNote];
        } else {
            randomNote = (int) Math.floor(Math.random()*12);
            resource = noteResources[randomNote];
            adjustNotes(randomNote);
            addTargetBounds(randomNote, adjustedNoteNames, adjustedNoteFrequencies);
        }
        noteText.setText(adjustedNoteNames[5]);

        final MediaPlayer mp = MediaPlayer.create(this, resource);
        mp.start();

        //prevents the app from pitch matching itself. After 1000ms,
        //the random tone will be done playing, so the app should
        //resume listening for inputs through the microphone.
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        shouldListen = true;
                    }
                }, 1000);
    }

    private void adjustNotes(int randomNote) {
        //shifts arrays so that randomNote is in the middle ([5[)
        Log.d("randomNoteIndex", String.valueOf(randomNote));
        Log.d("randomNote", String.valueOf(noteNames[randomNote]));
        Log.d("noteNames", Arrays.toString(noteNames));
        Log.d("noteFreq", Arrays.toString(noteFrequencies));

        //indexDiff measures how many times the array shift should occur
        //for the target note to be in the middle
        int indexDiff = randomNote - 5;
        adjustedNoteNames = noteNames.clone();
        adjustedNoteFrequencies = noteFrequencies.clone();
        adjustedNoteResources = noteResources.clone();

        //if positive, shift left
        while (indexDiff > 0) {
            String firstName = adjustedNoteNames[0];
            float firstFreq = adjustedNoteFrequencies[0];
            for (int i=0; i<adjustedNoteNames.length - 1; i++) {
                adjustedNoteNames[i] = adjustedNoteNames[i+1];
            }
            for (int i=0; i<adjustedNoteFrequencies.length - 1; i++) {
                adjustedNoteFrequencies[i] = adjustedNoteFrequencies[i+1];
            }
            for (int i=0; i<adjustedNoteResources.length - 1; i++) {
                adjustedNoteResources[i] = adjustedNoteResources[i+1];
            }
            adjustedNoteNames[adjustedNoteNames.length - 1] = firstName;
            adjustedNoteFrequencies[adjustedNoteFrequencies.length - 1] = firstFreq;
            adjustedNoteFrequencies[adjustedNoteFrequencies.length - 1] *= 2;
            indexDiff--;
        }
        while (indexDiff < 0) {
            String lastName = adjustedNoteNames[adjustedNoteNames.length - 1];
            float lastFreq = adjustedNoteFrequencies[adjustedNoteFrequencies.length - 1];
            for (int i=adjustedNoteNames.length-2; i>=0; i--) {
                adjustedNoteNames[i+1] = adjustedNoteNames[i];
            }
            for (int i=adjustedNoteFrequencies.length-2; i>=0; i--) {
                adjustedNoteFrequencies[i+1] = adjustedNoteFrequencies[i];
            }
            for (int i=adjustedNoteResources.length-2; i>=0; i--) {
                adjustedNoteResources[i+1] = adjustedNoteResources[i];
            }
            adjustedNoteNames[0] = lastName;
            adjustedNoteFrequencies[0] = lastFreq;
            adjustedNoteFrequencies[0] /= 2;
            indexDiff++;
        }
        Log.d("adjustedNoteNames", Arrays.toString(adjustedNoteNames));
        Log.d("adjustedNoteFreq", Arrays.toString(adjustedNoteFrequencies));
        Log.d("adjustedNoteRes", Arrays.toString(adjustedNoteResources));
    }

    private void addTargetBounds(int targetNote, String[] adjustedNoteNames, float[] adjustedNoteFrequencies) {
        //adjusts thresholds for accuracy to be +-50c from true target pitch.
        //also adjusts threshold for entire scale to be +-200c from the target pitch.
        //ideally, we're hoping for a threshold of +-20c

        final View upperLimit = findViewById(R.id.targetUpperLimit);
        final View lowerLimit = findViewById(R.id.targetLowerLimit);
        final TextView upperText = findViewById(R.id.octaveUpperText);
        final TextView lowerText = findViewById(R.id.octaveLowerText);

        upperText.setText(adjustedNoteNames[7]);
        lowerText.setText(adjustedNoteNames[3]);

        float targetFrequency = adjustedNoteFrequencies[5];
        float nextFrequency;
        float prevFrequency;
        nextFrequency = adjustedNoteFrequencies[6];
        prevFrequency = adjustedNoteFrequencies[4];

        Log.d("starting frequency: ", String.valueOf(adjustedNoteFrequencies[5]));
        Log.d("next frequency: ", String.valueOf(nextFrequency));
        Log.d("prev frequency: ", String.valueOf(prevFrequency));

//        find value 20 cents above randomNote
        targetUpperFrequency = targetFrequency + (nextFrequency - targetFrequency)/2;//50c
        Log.d("upperTwenty", String.valueOf(targetUpperFrequency));

//        find value 20 cents below random Note
        targetLowerFrequency = targetFrequency + (prevFrequency - targetFrequency)/2;//50c
        Log.d("lowerTwenty", String.valueOf(targetLowerFrequency));


        //set constraints on targetUpperLimit and targetLowerLimit
        final float adjustedUpperFrequency = adjustedNoteFrequencies[7];
        Log.d("upperMax", String.valueOf(adjustedNoteFrequencies[7]));
        final float adjustedLowerFrequency = adjustedNoteFrequencies[3];
        Log.d("lowerMax", String.valueOf(adjustedNoteFrequencies[3]));

        final float upperBias = getBias(targetUpperFrequency, adjustedLowerFrequency, adjustedUpperFrequency);
        Log.d("upperTwentyBias", String.valueOf(upperBias));
        final float lowerBias = getBias(targetLowerFrequency, adjustedLowerFrequency, adjustedUpperFrequency);
        Log.d("lowerTwentyBias", String.valueOf(lowerBias));

        ConstraintLayout.LayoutParams upperLimitLayoutParams = (ConstraintLayout.LayoutParams) upperLimit.getLayoutParams();
        ConstraintLayout.LayoutParams lowerLimitLayoutParams = (ConstraintLayout.LayoutParams) lowerLimit.getLayoutParams();
        upperLimitLayoutParams.verticalBias = upperBias;
        lowerLimitLayoutParams.verticalBias = lowerBias;

        upperLimit.setLayoutParams(upperLimitLayoutParams);
        lowerLimit.setLayoutParams(lowerLimitLayoutParams);
    }

    private void processPitch(float pitchInHz, float probability) {
        TextView noteText = findViewById(R.id.noteText);

        //filters out background noise, smoothing out UI
        if (probability > .9 && pitchInHz > 0) {
            //reduces the incoming frequency to base values
            //this allows the user to input the correct tone in any octave
            float frequency = pitchInHz;
            while (frequency > noteFrequencies[noteFrequencies.length - 1]) {
                frequency /= (float) 2.0;
            }
            while (frequency < noteFrequencies[0]) {
                frequency *= (float) 2.0;
            }
            baseFrequency = frequency;

            float minDistance = (float) 10000.0;
            int index = 0;

            for (int i=0; i<noteFrequencies.length; i++) {
                float distance = Math.abs(noteFrequencies[i] - frequency);
                if (distance < minDistance) {
                    index = i;
                    minDistance = distance;
                }
            }

            int octave = (int) (Math.log(pitchInHz / frequency)/Math.log(2));

            //if the app is expecting a certain tone, it should show that
            //tone's note name in the bubble. otherwise, show incoming tone's note name.
            if (adjustedNoteNames != null) {
                noteText.setText("" + adjustedNoteNames[5] + "" + octave);

            } else {
                noteText.setText("" + noteNames[index] + "" + octave);
            }
            moveImage(frequency);
        }
    }

    private void moveImage(final float frequency) {
        final float upperFrequency;
        final float lowerFrequency;
        //if the app is expecting a certain tone, the scale should be +-200c
        //otherwise, reset the scale to span the entire octave.
        if (adjustedNoteNames != null) {
            upperFrequency = adjustedNoteFrequencies[7];
            lowerFrequency = adjustedNoteFrequencies[3];
        } else {
            upperFrequency = noteFrequencies[noteFrequencies.length - 1];
            lowerFrequency = noteFrequencies[0];
        }

        final float bias = getBias(frequency, lowerFrequency, upperFrequency);
        Log.d("imageBias: ", String.valueOf(bias));

        final ImageView image = findViewById(R.id.imageView);

        ConstraintLayout.LayoutParams imageParams = (ConstraintLayout.LayoutParams) image.getLayoutParams();
        imageParams.verticalBias = bias;
        image.setLayoutParams(imageParams);
    }

    private float getBias(float frequency, float lowerFrequency, float upperFrequency) {
        //this function simply converts the frequencies to a scale of 0-1,
        //for easy positioning using the bias layout parameter
        float bias = (float) (1 - (frequency - lowerFrequency)/(upperFrequency - lowerFrequency));
        if (bias < 0) {
            bias = 0;
        } else if (bias > 1) {
            bias = 1;
        }
        return bias;
    }

    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("This permission is needed for this and that")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, MY_PERMISSION);
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.RECORD_AUDIO},
                    STORAGE_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission GRANTED", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }
}


