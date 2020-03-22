package com.example.musicalears;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
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
import java.util.TimeZone;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;

import static com.example.musicalears.Note.noteList;

public class PitchMatchingActivity extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private final Handler timerHandler = new Handler();

    private TextView scoreText;
    private TextView timerText;
    private ImageView micView;
    private ImageView skipView;

    private TargetNote targetNote = null;

    private int randomNoteIndex;

    private PitchMatchFragment pitchMatchFragment;

    private int score = 0;
    private long startTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pitch_matching);

        pitchMatchFragment = PitchMatchFragment.newInstance("pitch", "false");
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentFrameLayout, pitchMatchFragment).commit();

        scoreText = findViewById(R.id.scoreText);
        timerText = findViewById(R.id.timerText);

        ImageButton playButton = findViewById(R.id.playButton);
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
                if (pitchMatchFragment.getShouldListen()) {
                    pitchMatchFragment.setShouldListen(false);
                    micView.setImageResource(R.drawable.mic_off);
                    micView.setAlpha(0.5f);
                } else if (targetNote != null) {
                    pitchMatchFragment.setShouldListen(true);
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
                        if (pitchMatchFragment.getShouldListen() &&
                                targetNote != null) {
                            if (probability > .8) {
                                pitchMatchFragment.processPitch(pitchInHz, true);
                            } else {
                                pitchMatchFragment.stopTimer();
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
            randomNoteIndex = (int) Math.floor(Math.random()*22);

            int adjustedRandomIndex = (randomNoteIndex + 8)%12;

            targetNote = new TargetNote(
                    noteList.get(adjustedRandomIndex).getNoteName(),
                    noteList.get(adjustedRandomIndex).getNoteFrequency());
            targetNote.adjustNotes(adjustedRandomIndex, false);
            pitchMatchFragment.setTargetNote(targetNote);

            skipView.setAlpha(1f);
        }
    }

    private void playRandomTone() {
        pitchMatchFragment.setShouldListen(false);
        micView.setImageResource(R.drawable.mic_off);
        micView.setAlpha(.5f);
        setTargetNote();

        final int noteResource = getResources().getIdentifier("note" + randomNoteIndex, "raw", getPackageName());

        final MediaPlayer mp = MediaPlayer.create(PitchMatchingActivity.this, noteResource);
        mp.start();

        //prevents the app from 'listening' to itself
        new Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        pitchMatchFragment.setShouldListen(true);
                        micView.setImageResource(R.drawable.mic_on);
                        micView.setAlpha(1f);
                    }
                }, 2000);
    }

    public void scorePointAndReset() {
        targetNote = null;
        micView.setImageResource(R.drawable.mic_off);
        micView.setAlpha(0.5f);
        score++;
        String text = score + " pts";
        scoreText.setText(text);

        pitchMatchFragment.resetSelf(false);
    }

    private void checkPermissionsAndStart() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            //permission not granted
            ActivityCompat.requestPermissions(PitchMatchingActivity.this, new String[] {Manifest.permission.RECORD_AUDIO},
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
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //permission granted
                startApp();
            } else {
                //permission denied
                pitchMatchFragment.disableSelf();
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
}
