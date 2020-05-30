package com.example.musicalears;

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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.musicalears.Fragment.IntervalMainNoteFragment;
import com.example.musicalears.Fragment.IntervalBaseNoteFragment;

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

public class IntervalTrainingActivity extends AppCompatActivity {
    private static final String PARAM_NUM_INTERVALS = "numIntervals";
    private static final String PARAM_DURATION = "duration";
    private static final String PARAM_SHOULD_SHOW_BASE_NOTE = "showBaseNote";
    private static final String PARAM_SHOULD_SHOW_INTERVAL_NOTE = "showIntervalNote";

    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private final Handler timerHandler = new Handler();

    private TextView scoreText;
    private TextView timerText;
    private ImageView micView;
    private ImageView skipView;

    private TextView intervalText;

    private IntervalBaseNoteFragment intervalPitchMatchFragment;
    private IntervalMainNoteFragment basePitchMatchFragment;

    private AudioDispatcher dispatcher = null;
    private PitchDetectionHandler pitchDetectionHandler = null;

    private long startTime;
    private int paramNumIntervals;

    private int score = 0;
    private int baseNoteIndex = -1;
    private int intervalNoteIndex = -1;

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

    public void switchFragments() {
        basePitchMatchFragment.setShouldListen(false);
        intervalPitchMatchFragment.setShouldListen(true);
        basePitchMatchFragment.setTargetNote(-1);
        intervalPitchMatchFragment.enableSelf();
        basePitchMatchFragment.disableSelf();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interval_training);

        paramNumIntervals = Objects.requireNonNull(getIntent().getExtras()).getInt(PARAM_NUM_INTERVALS);
        long paramDuration = getIntent().getExtras().getLong(PARAM_DURATION);
        boolean paramShouldShowBaseNote = getIntent().getExtras().getBoolean(PARAM_SHOULD_SHOW_BASE_NOTE);
        boolean paramShouldShowIntervalNote = getIntent().getExtras().getBoolean(PARAM_SHOULD_SHOW_INTERVAL_NOTE);

        basePitchMatchFragment = IntervalMainNoteFragment.newInstance(paramDuration, paramShouldShowBaseNote);
        getSupportFragmentManager().beginTransaction().replace(R.id.basePitchMatchFrameLayout, basePitchMatchFragment).commit();

        intervalPitchMatchFragment = IntervalBaseNoteFragment.newInstance(paramDuration, paramShouldShowIntervalNote);
        getSupportFragmentManager().beginTransaction().replace(R.id.intervalPitchMatchFrameLayout, intervalPitchMatchFragment).commit();

        timerText = findViewById(R.id.timerText);
        intervalText = findViewById(R.id.intervalText);
        scoreText = findViewById(R.id.scoreText);

        ImageButton playButton = findViewById(R.id.playButton);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playTones();
            }
        });

        micView = findViewById(R.id.micView);
        micView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (intervalPitchMatchFragment.getTargetNote() == null) {
                    return;
                }
                if (basePitchMatchFragment.getShouldListen() ||
                        intervalPitchMatchFragment.getShouldListen()) {
                    micView.setImageResource(R.drawable.mic_off);
                    micView.setAlpha(0.5f);
                    basePitchMatchFragment.setShouldListen(false);
                    intervalPitchMatchFragment.setShouldListen(false);
                } else {
                    micView.setImageResource(R.drawable.mic_on);
                    micView.setAlpha(1f);
                    if (basePitchMatchFragment.getTargetNote() == null) {
                        basePitchMatchFragment.setShouldListen(false);
                        intervalPitchMatchFragment.setShouldListen(true);
                    } else {
                        basePitchMatchFragment.setShouldListen(true);
                        intervalPitchMatchFragment.setShouldListen(false);
                    }
                }
            }
        });
        skipView = findViewById(R.id.skipView);
        skipView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (intervalPitchMatchFragment.getTargetNote() != null) {
                    basePitchMatchFragment.setTargetNote(-1);
                    intervalPitchMatchFragment.setTargetNote(-1);
                    micView.setAlpha(.5f);
                    playTones();
                }
            }
        });

        checkPermissionsAndStart();
    }

    private void checkPermissionsAndStart() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(IntervalTrainingActivity.this, new String[]{Manifest.permission.RECORD_AUDIO},
                    PERMISSIONS_REQUEST_RECORD_AUDIO);
        } else {
            startApp();
        }
    }

    private void startApp() {
        startTimer(new Date().getTime());

        dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050, 2048, 1);
        pitchDetectionHandler = new PitchDetectionHandler() {
            @Override
            public void handlePitch(final PitchDetectionResult pitchDetectionResult, AudioEvent audioEvent) {
                final float pitchInHz = pitchDetectionResult.getPitch();
                final float probability = pitchDetectionResult.getProbability();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (pitchInHz < 100) {
                            if (basePitchMatchFragment.getTargetNote() != null) {
                                basePitchMatchFragment.stopTimer();
                            } else {
                                intervalPitchMatchFragment.stopTimer();
                            }
                        } else if (probability > .9) {
                            if (basePitchMatchFragment.getShouldListen() &&
                                    basePitchMatchFragment.getTargetNote() != null) {
                                basePitchMatchFragment.processPitch(pitchInHz);
                            } else if (intervalPitchMatchFragment.getShouldListen() &&
                                    intervalPitchMatchFragment.getTargetNote() != null) {
                                intervalPitchMatchFragment.processPitch(pitchInHz);
                            }
                        }
                    }
                });
            }
        };

        AudioProcessor pitchProcessor = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050, 2048, pitchDetectionHandler);
        dispatcher.addAudioProcessor(pitchProcessor);

        Thread audioThread = new Thread(dispatcher, "Audio Thread");
        audioThread.start();
    }

    private void stopApp() {
        dispatcher = null;
        pitchDetectionHandler = null;
    }

    private void playTones() {
        basePitchMatchFragment.setShouldListen(false);
        intervalPitchMatchFragment.setShouldListen(false);
        micView.setImageResource(R.drawable.mic_off);
        micView.setAlpha(.5f);

        if (intervalPitchMatchFragment.getTargetNote() == null) {
            boolean isIntervalAbove = Math.random() < 0.5;
            int interval = (int) Math.floor(Math.random() * 11) + 1;

            if (isIntervalAbove) {
                baseNoteIndex = (int) Math.floor(Math.random() * (22 - interval));
                intervalNoteIndex = baseNoteIndex + interval;
            } else {
                baseNoteIndex = (int) Math.floor(Math.random() * (22 - interval)) + interval;
                intervalNoteIndex = baseNoteIndex - interval;
            }

            int adjustedBaseIndex = (baseNoteIndex + 8) % 12;
            int adjustedIntervalIndex = (intervalNoteIndex + 8) % 12;

            basePitchMatchFragment.setTargetNote(adjustedBaseIndex);
            intervalPitchMatchFragment.setTargetNote(adjustedIntervalIndex);
            skipView.setAlpha(1f);
            intervalText.setText(getIntervalName(interval, isIntervalAbove));
        }

        final int intervalNoteResource = getResources().getIdentifier("note" + intervalNoteIndex, "raw", getPackageName());
        final int baseNoteResource = getResources().getIdentifier("note" + baseNoteIndex, "raw", getPackageName());

        final MediaPlayer mp = MediaPlayer.create(IntervalTrainingActivity.this, baseNoteResource);
        final MediaPlayer mp2 = MediaPlayer.create(IntervalTrainingActivity.this, intervalNoteResource);
        mp.start();

        new Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        if (mp.isPlaying()) mp.stop();
                        mp.reset();
                        mp.release();
                        mp2.start();
                    }
                }, 1000);

        //prevents the app from 'listening' to itself
        new Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        if (mp2.isPlaying()) mp2.stop();
                        mp2.reset();
                        mp2.release();
                        if (basePitchMatchFragment.getTargetNote() == null) {
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

    private void updateTimer() {
        Date currentDate = new Date();
        long currentTime = currentDate.getTime();
        long elapsedTime = currentTime - startTime;
        DateFormat dateFormatter = new SimpleDateFormat("mm:ss", Locale.US);
        dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        timerText.setText(dateFormatter.format(elapsedTime));
    }

    public void scorePointAndReset() {
        final MediaPlayer mp = MediaPlayer.create(this, R.raw.success);
        mp.start();
        score++;
        String text = score + " pts";
        scoreText.setText(text);
        intervalText.setText("");
        micView.setImageResource(R.drawable.mic_off);
        micView.setAlpha(0.5f);
        skipView.setAlpha(0.5f);

        basePitchMatchFragment.resetSelf();
        basePitchMatchFragment.enableSelf();
        basePitchMatchFragment.setShouldListen(false);
        intervalPitchMatchFragment.resetSelf();
        intervalPitchMatchFragment.disableSelf();
        intervalPitchMatchFragment.setShouldListen(false);

        new Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        if (mp.isPlaying()) mp.stop();
                        mp.reset();
                        mp.release();
                    }
                }, 3000);
        if (score == paramNumIntervals) {
            new AlertDialog.Builder(this)
                    .setTitle("Congrats! You scored " + score + " points in " + timerText.getText() + "!")
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startApp();
            } else {
                basePitchMatchFragment.disableSelf();
                intervalPitchMatchFragment.disableSelf();
            }
        }
    }

    @Override
    public void onBackPressed() {
        basePitchMatchFragment.setTargetNote(-1);
        intervalPitchMatchFragment.setTargetNote(-1);
        stopApp();
        super.onBackPressed();
    }
}
