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

import com.example.musicalears.Fragment.PitchNoteFragment;

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

public class PitchMatchingActivity extends AppCompatActivity {
    private static final double PROBABILITY_THRESHOLD = 0.85;
    private static final double PITCH_THRESHOLD = 100;

    private static final String PARAM_NUM_NOTES = "numNotes";
    private static final String PARAM_DURATION = "duration";
    private static final String PARAM_SHOULD_SHOW_NOTE_NAME = "showName";

    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private final Handler timerHandler = new Handler();

    private TextView scoreText;
    private TextView timerText;
    private ImageView micView;
    private ImageView skipView;

    private PitchNoteFragment pitchMatchFragment;

    AudioDispatcher dispatcher = null;
    PitchDetectionHandler pitchDetectionHandler = null;

    private long startTime;
    private int paramNumNotes;

    private int score = 0;
    private int noteIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pitch_matching);

        paramNumNotes = Objects.requireNonNull(getIntent().getExtras()).getInt(PARAM_NUM_NOTES);
        long paramDuration = getIntent().getExtras().getLong(PARAM_DURATION);
        boolean paramShouldShowNoteName = getIntent().getExtras().getBoolean(PARAM_SHOULD_SHOW_NOTE_NAME);

        pitchMatchFragment = PitchNoteFragment.newInstance(paramDuration, paramShouldShowNoteName);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentFrameLayout, pitchMatchFragment).commit();

        timerText = findViewById(R.id.timerText);
        scoreText = findViewById(R.id.scoreText);

        ImageButton playButton = findViewById(R.id.playButton);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playTone();
            }
        });

        micView = findViewById(R.id.micView);
        micView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pitchMatchFragment.getTargetNote() == null) {
                    return;
                }
                if (pitchMatchFragment.getShouldListen()) {
                    micView.setImageResource(R.drawable.mic_off);
                    micView.setAlpha(0.5f);
                    pitchMatchFragment.setShouldListen(false);
                } else {
                    micView.setImageResource(R.drawable.mic_on);
                    micView.setAlpha(1f);
                    pitchMatchFragment.setShouldListen(true);
                }
            }
        });
        skipView = findViewById(R.id.skipView);
        skipView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pitchMatchFragment.getTargetNote() != null) {
                    pitchMatchFragment.setTargetNote(-1);
                    micView.setAlpha(.5f);
                    playTone();
                }
            }
        });

        checkPermissionsAndStart();
    }

    private void checkPermissionsAndStart() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(PitchMatchingActivity.this, new String[]{Manifest.permission.RECORD_AUDIO},
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
                        if (pitchInHz < PITCH_THRESHOLD) {
                            pitchMatchFragment.stopTimer();
                        } else if (probability > PROBABILITY_THRESHOLD) {
                            if (pitchMatchFragment.getShouldListen() &&
                                    pitchMatchFragment.getTargetNote() != null) {
                                pitchMatchFragment.processPitch(pitchInHz);
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

    private void playTone() {
        pitchMatchFragment.setShouldListen(false);
        micView.setImageResource(R.drawable.mic_off);
        micView.setAlpha(.5f);

        if (pitchMatchFragment.getTargetNote() == null) {
            int randomNoteIndex = (int) Math.floor(Math.random() * 22);
            if (randomNoteIndex == noteIndex) {
                playTone();
            }
            noteIndex = randomNoteIndex;
            int adjustedIndex = (noteIndex + 8) % 12;

            pitchMatchFragment.setTargetNote(adjustedIndex);
            skipView.setAlpha(1f);
        }

        final int noteResource = getResources().getIdentifier("note" + noteIndex, "raw", getPackageName());

        final MediaPlayer mp = MediaPlayer.create(PitchMatchingActivity.this, noteResource);
        mp.start();

        //prevents the app from 'listening' to itself
        new Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        if (mp.isPlaying()) mp.stop();
                        mp.reset();
                        mp.release();
                        pitchMatchFragment.setShouldListen(true);
                        micView.setImageResource(R.drawable.mic_on);
                        micView.setAlpha(1f);
                    }
                }, 2000);
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
        micView.setImageResource(R.drawable.mic_off);
        micView.setAlpha(0.5f);

        pitchMatchFragment.resetSelf();
        pitchMatchFragment.setShouldListen(false);

        new Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        if (mp.isPlaying()) mp.stop();
                        mp.reset();
                        mp.release();
                    }
                }, 3000);
        if (score == paramNumNotes) {
            new AlertDialog.Builder(this)
                    .setTitle("Congrats! You scored " + score + " points in " + timerText.getText() + "!")
                    .setMessage("You will now be taken back to the main screen.")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            startActivity(new Intent(PitchMatchingActivity.this, MainActivity.class));
                        }
                    })
                    .show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startApp();
            } else {
                pitchMatchFragment.disableSelf();
            }
        }
    }

    @Override
    public void onBackPressed() {
        pitchMatchFragment.setTargetNote(-1);
        stopApp();
        super.onBackPressed();
    }
}
