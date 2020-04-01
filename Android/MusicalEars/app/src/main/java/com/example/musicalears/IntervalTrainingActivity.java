package com.example.musicalears;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;

import static com.example.musicalears.Note.noteList;

public class IntervalTrainingActivity extends AppCompatActivity {
    private static final String PARAM_NUM_INTERVALS = "numIntervals";
    private static final String PARAM_DURATION = "duration";
    private static final String PARAM_SHOULD_SHOW_BASE_NOTE = "showBaseNote";
    private static final String PARAM_SHOULD_SHOW_INTERVAL_NOTE = "showIntervalNote";

    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private final Handler timerHandler = new Handler();

    private TextView scoreText;
    private TextView timerText;
    private TextView intervalText;
    private ImageView micView;
    private ImageView skipView;

    private TargetNote targetNote = null;
    private TargetNote intervalNote = null;

    private int randomNoteIndex;
    private int intervalNoteIndex;

    private PitchMatchFragment intervalPitchMatchFragment;
    private PitchMatchFragment basePitchMatchFragment;

    private boolean didGetBaseNote = false;

    private int score = 0;
    private long startTime;

    private int numIntervals;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interval_training);

        numIntervals = Objects.requireNonNull(getIntent().getExtras()).getInt(PARAM_NUM_INTERVALS);
        long paramDuration = getIntent().getExtras().getLong(PARAM_DURATION);
        boolean paramShouldShowBaseNote = getIntent().getExtras().getBoolean(PARAM_SHOULD_SHOW_BASE_NOTE);
        boolean paramShouldShowIntervalNote = getIntent().getExtras().getBoolean(PARAM_SHOULD_SHOW_INTERVAL_NOTE);

        basePitchMatchFragment = PitchMatchFragment.newInstance(paramDuration, paramShouldShowBaseNote, false, "interval");
        getSupportFragmentManager().beginTransaction().replace(R.id.basePitchMatchFrameLayout, basePitchMatchFragment).commit();
        intervalPitchMatchFragment = PitchMatchFragment.newInstance(paramDuration, paramShouldShowIntervalNote, true, "interval");
        getSupportFragmentManager().beginTransaction().replace(R.id.intervalPitchMatchFrameLayout, intervalPitchMatchFragment).commit();

        scoreText = findViewById(R.id.scoreText);
        timerText = findViewById(R.id.timerText);
        intervalText = findViewById(R.id.intervalText);

        ImageButton playButton = findViewById(R.id.playButton2);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playRandomTone();
            }
        });

        micView = findViewById(R.id.micView);
        micView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (basePitchMatchFragment.getShouldListen() ||
                        intervalPitchMatchFragment.getShouldListen()) {
                    basePitchMatchFragment.setShouldListen(false);
                    intervalPitchMatchFragment.setShouldListen(false);
                    micView.setImageResource(R.drawable.mic_off);
                    micView.setAlpha(0.5f);
                } else if (targetNote != null) {
                    if (didGetBaseNote) {
                        basePitchMatchFragment.setShouldListen(false);
                        intervalPitchMatchFragment.setShouldListen(true);
                    } else {
                        basePitchMatchFragment.setShouldListen(true);
                        intervalPitchMatchFragment.setShouldListen(false);
                    }
                    micView.setImageResource(R.drawable.mic_on);
                    micView.setAlpha(1f);
                }
            }
        });
        skipView = findViewById(R.id.skipView);
        skipView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (targetNote != null) {
                    targetNote = null;
                    intervalNote = null;
                    micView.setAlpha(.5f);
                    playRandomTone();
                }
            }
        });

        checkPermissionsAndStart();
    }

    private void startApp() {
        startTimer(new Date().getTime());

        AudioDispatcher dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050, 1024, 0);
        PitchDetectionHandler pitchDetectionHandler = new PitchDetectionHandler() {
            @Override
            public void handlePitch(final PitchDetectionResult pitchDetectionResult, AudioEvent audioEvent) {
                final float pitchInHz = pitchDetectionResult.getPitch();
                final float probability = pitchDetectionResult.getProbability();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (basePitchMatchFragment.getShouldListen() &&
                                targetNote != null) {
                            if (pitchInHz > 0 && probability > .9) {
                                basePitchMatchFragment.processPitch(pitchInHz, false);
                            } else {
                                basePitchMatchFragment.stopTimer();
                            }
                        } else if (intervalPitchMatchFragment.getShouldListen() && intervalNote != null){
                            if (pitchInHz > 0 && probability > .9) {
                                intervalPitchMatchFragment.processPitch(pitchInHz, true);
                            } else {
                                intervalPitchMatchFragment.stopTimer();
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
    }

    private void startTimer(long time) {
        startTime = time;
        timerHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateTimer();
                timerHandler.postDelayed(this, 1000);
            }
        }, 0);
    }

    private void setTargetNote() {
        if (targetNote == null) {
            int randomInterval = (int) Math.floor(Math.random() * 11) + 1;
            boolean isIntervalAbove = Math.random() < 0.5;

            if (isIntervalAbove) {
                randomNoteIndex = (int) Math.floor(Math.random() * (22 - randomInterval));
                intervalNoteIndex = randomNoteIndex + randomInterval;
            } else {
                randomNoteIndex = (int) Math.floor(Math.random() * (22 - randomInterval)) + randomInterval;
                intervalNoteIndex = randomNoteIndex - randomInterval;
            }

            int adjustedRandomIndex = (randomNoteIndex + 8)%12;
            int adjustedIntervalIndex = (intervalNoteIndex + 8)%12;

            targetNote = new TargetNote(
                    noteList.get(adjustedRandomIndex).getNoteName(),
                    noteList.get(adjustedRandomIndex).getNoteFrequency());
            targetNote.adjustNotes(adjustedRandomIndex, false);
            basePitchMatchFragment.setTargetNote(targetNote);

            intervalNote = new TargetNote(
                    noteList.get(adjustedIntervalIndex).getNoteName(),
                    noteList.get(adjustedIntervalIndex).getNoteFrequency());
            intervalNote.adjustNotes(adjustedIntervalIndex, true);
            intervalPitchMatchFragment.setTargetNote(intervalNote);

            intervalText.setText(getIntervalName(randomInterval, isIntervalAbove));
            skipView.setAlpha(1f);
        }
    }

    private void playRandomTone() {
        basePitchMatchFragment.setShouldListen(false);
        intervalPitchMatchFragment.setShouldListen(false);
        micView.setImageResource(R.drawable.mic_off);
        micView.setAlpha(.5f);
        setTargetNote();

        final int intervalNoteResource = getResources().getIdentifier("note" + intervalNoteIndex, "raw", getPackageName());
        final int baseNoteResource = getResources().getIdentifier("note" + randomNoteIndex, "raw", getPackageName());

        final MediaPlayer mp = MediaPlayer.create(IntervalTrainingActivity.this, baseNoteResource);
        mp.start();

        new Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        final MediaPlayer mp2 = MediaPlayer.create(IntervalTrainingActivity.this, intervalNoteResource);
                        mp2.start();
                    }
                }, 1000);

//        prevents the app from 'listening' to itself
        new Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        if (didGetBaseNote) {
                            basePitchMatchFragment.setShouldListen(false);
                            intervalPitchMatchFragment.setShouldListen(true);
                        } else {
                            basePitchMatchFragment.setShouldListen(true);
                            intervalPitchMatchFragment.setShouldListen(false);
                        }
                        micView.setImageResource(R.drawable.mic_on);
                        micView.setAlpha(1f);
                    }
                }, 3000);
    }

    public void scorePointAndReset() {
        final MediaPlayer mp = MediaPlayer.create(IntervalTrainingActivity.this, R.raw.success);
        mp.start();
        targetNote = null;
        intervalNote = null;
        micView.setImageResource(R.drawable.mic_off);
        micView.setAlpha(0.5f);
        score++;
        String text = score + " pts";
        scoreText.setText(text);

        intervalPitchMatchFragment.resetSelf(true);
        basePitchMatchFragment.resetSelf(false);

        if (score == numIntervals) {
            new AlertDialog.Builder(this)
                    .setTitle("Congrats! You scored " + numIntervals + " points in " + timerText.getText() + "!")
                    .setMessage("You will now be taken back to the main screen.")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            startActivity(new Intent(IntervalTrainingActivity.this, MainActivity.class));
                        }
                    })
                    .show();
        }

    }

    private void checkPermissionsAndStart() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            //permission not granted
            ActivityCompat.requestPermissions(IntervalTrainingActivity.this, new String[] {Manifest.permission.RECORD_AUDIO},
                    PERMISSIONS_REQUEST_RECORD_AUDIO);
        } else {
            //permission granted
            startApp();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //permission granted
                startApp();
            } else {
                //permission denied
                basePitchMatchFragment.disableSelf();
                intervalPitchMatchFragment.disableSelf();
            }
        }
    }

    private void updateTimer() {
        Date currentDate = new Date();
        long currentTime = currentDate.getTime();
        long elapsedTime = currentTime - startTime;
        DateFormat dateFormatter = new SimpleDateFormat("mm:ss", Locale.US);
        dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        timerText.setText(dateFormatter.format(elapsedTime));
    }

    public void switchFragments() {
        if (!didGetBaseNote) {
            didGetBaseNote = true;
            intervalPitchMatchFragment.didGetBaseNote = true;
            intervalPitchMatchFragment.setShouldListen(true);
            intervalPitchMatchFragment.enableSelf();
        } else {
            didGetBaseNote = false;
            basePitchMatchFragment.didGetBaseNote = false;
            basePitchMatchFragment.setShouldListen(true);
            basePitchMatchFragment.enableSelf();
        }
    }

    private String getIntervalName(int index, boolean isAbove) {
        String[] intervalArray = {
                "Minor 2nd",
                "Major 2nd",
                "Minor 3rd",
                "Major 3rd",
                "Perfect 4th",
                "Tritone",
                "Perfect 5th",
                "Minor 6th",
                "Major 6th",
                "Minor 7th",
                "Major 7th"};
        if (isAbove) return intervalArray[index - 1] + " Above";
        else return intervalArray[index - 1] + " Below";
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(IntervalTrainingActivity.this, MainActivity.class));
    }
}
