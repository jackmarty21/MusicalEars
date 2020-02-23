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
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

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
    private static PitchDetectionHandler pitchDetectionHandler;

    private TextView scoreText;
    private TextView statusText;
    private View disableFilter;
    private ImageButton playButton;

    PitchMatchFragment pitchMatchFragment;
    private TargetNote randomNote;

    private boolean shouldListen;

    private int randomNoteIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pitch_matching);

        scoreText = findViewById(R.id.scoreText);
        disableFilter = findViewById(R.id.disableFilter);
        statusText = findViewById(R.id.statusText);
        playButton = findViewById(R.id.playButton);

        pitchMatchFragment = PitchMatchFragment.newInstance("pitch", "false");
        getSupportFragmentManager().beginTransaction().replace(R.id.pitchMatchFrameLayout, pitchMatchFragment).commit();

        checkPermissions();
    }

    private void startApp() {
        shouldListen = true;
        statusText.setText(R.string.mic_on);

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playRandomTone();
                pitchMatchFragment.onPlayRandomTone(randomNote, false);
            }
        });
        AudioDispatcher dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050, 1024, 0);
        pitchDetectionHandler = new PitchDetectionHandler() {
            @Override
            public void handlePitch(PitchDetectionResult pitchDetectionResult, AudioEvent audioEvent) {
                final float pitchInHz = pitchDetectionResult.getPitch();
                final float probability = pitchDetectionResult.getProbability();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (shouldListen) {
                            float frequency = pitchMatchFragment.processPitch(pitchInHz, probability, false);

                            if (pitchMatchFragment.targetNote != null) {
                                if (frequency > 0 && pitchMatchFragment.checkAccuracy(frequency)) {
                                    pitchMatchFragment.startTimer(3000, true);
                                } else {
                                    pitchMatchFragment.stopTimer();
                                }
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

    private void playRandomTone() {

        shouldListen = false;
        statusText.setText("");
        if (randomNote != null) {
            randomNoteIndex = 5;
            randomNote = new TargetNote(
                    Note.adjustedNoteList.get(randomNoteIndex).getNoteName(),
                    Note.adjustedNoteList.get(randomNoteIndex).getNoteFrequency(),
                    Note.adjustedNoteList.get(randomNoteIndex).getNoteResource());
        } else {
            randomNoteIndex = (int) Math.floor(Math.random()*12);
            randomNote = new TargetNote(
                    noteList.get(randomNoteIndex).getNoteName(),
                    noteList.get(randomNoteIndex).getNoteFrequency(),
                    noteList.get(randomNoteIndex).getNoteResource());
        }
        Log.d("randomNoteName", String.valueOf(randomNote.getNoteName()));
        Log.d("randomNoteFreq", String.valueOf(randomNote.getNoteFrequency()));

        randomNote.adjustNotes(randomNoteIndex, false);



        final MediaPlayer mp = MediaPlayer.create(PitchMatchingActivity.this, randomNote.getNoteResource());
        mp.start();

        //prevents the app from 'listening' to itself
        new Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        shouldListen = true;
                        statusText.setText(R.string.now_listening);
                    }
                }, 2000);
    }

    public void scorePoint(int points) {
        scoreText.setText(points + " pts");
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(PitchMatchingActivity.this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            //permission not granted
            ActivityCompat.requestPermissions(PitchMatchingActivity.this, new String[] {Manifest.permission.RECORD_AUDIO},
                    PERMISSIONS_REQUEST_RECORD_AUDIO);
        } else {
            //permission granted
            Log.d("permission", "GRANTED");
//            isPermissionGranted = true;
            disableFilter.setVisibility(View.GONE);
            startApp();
        }
    }

    public void reset() {
        randomNote = null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("permission", "GRANTED");
//                isPermissionGranted = true;
                disableFilter.setVisibility(View.GONE);
                startApp();
            } else {
                Log.d("permission", "DENIED");
//                isPermissionGranted = false;
                disableFilter.setVisibility(View.VISIBLE);
            }
        }
    }
}
