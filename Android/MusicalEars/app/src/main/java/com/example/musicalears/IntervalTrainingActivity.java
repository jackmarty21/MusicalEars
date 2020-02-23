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
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;

import static com.example.musicalears.Note.noteList;

public class IntervalTrainingActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private static PitchDetectionHandler pitchDetectionHandler;
    private static MediaPlayer mp;

    private TextView scoreText;
    private TextView statusText;
    private View disableFilter;
    private ImageButton playButton;
    private Spinner intervalSpinner;

    PitchMatchFragment intervalPitchMatchFragment;
    PitchMatchFragment basePitchMatchFragment;
    private TargetNote randomNote;
    private TargetNote intervalNote;

    private boolean shouldBaseListen;
    private boolean shouldIntervalListen;
    private boolean didGetBaseNote = false;

    private int numSemitones;
    private int randomNoteIndex;
    private int intervalNoteIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interval_training);

        scoreText = findViewById(R.id.scoreText);
        disableFilter = findViewById(R.id.disableFilter);
        statusText = findViewById(R.id.statusText);
        playButton = findViewById(R.id.playButton);
        intervalSpinner = findViewById(R.id.spinner);

        intervalSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                numSemitones = (int) (adapterView.getItemIdAtPosition(i) + 1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        intervalPitchMatchFragment = PitchMatchFragment.newInstance("interval", "true");
        basePitchMatchFragment = PitchMatchFragment.newInstance("interval", "false");
        getSupportFragmentManager().beginTransaction().replace(R.id.intervalPitchMatchFrameLayout, intervalPitchMatchFragment).commit();
        getSupportFragmentManager().beginTransaction().replace(R.id.basePitchMatchFrameLayout, basePitchMatchFragment).commit();
        
        checkPermissions();
    }

    private void startApp() {
        shouldBaseListen = true;
        shouldIntervalListen = false;
        statusText.setText(R.string.mic_on);

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playRandomTone();
                basePitchMatchFragment.onPlayRandomTone(randomNote, false);
                intervalPitchMatchFragment.onPlayRandomTone(intervalNote, true);
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
                        if (shouldBaseListen) {
                            float frequency = basePitchMatchFragment.processPitch(pitchInHz, probability, didGetBaseNote);

                            if (basePitchMatchFragment.targetNote != null) {
                                if (frequency > 0 && basePitchMatchFragment.checkAccuracy(frequency)) {
                                    basePitchMatchFragment.startTimer(1000, false);
                                } else {
                                    basePitchMatchFragment.stopTimer();
                                }
                            } else {
                                basePitchMatchFragment.stopTimer();
                            }
                        } else if (shouldIntervalListen) {
                            float frequency = intervalPitchMatchFragment.processPitch(pitchInHz, probability, didGetBaseNote);

                            if (intervalPitchMatchFragment.targetNote != null) {
                                if (frequency > 0 && intervalPitchMatchFragment.checkAccuracy(frequency)) {
                                    intervalPitchMatchFragment.startTimer(2000, true);
                                } else {
                                    intervalPitchMatchFragment.stopTimer();
                                }
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

    private void playRandomTone() {
        if (didGetBaseNote) {
            switchFragments();
        }

        shouldBaseListen = false;
        shouldIntervalListen = false;
        statusText.setText("");
        if (randomNote != null) {
            randomNoteIndex = 5;
            intervalNoteIndex = randomNoteIndex + numSemitones;
            if (intervalNoteIndex > 11) {
                intervalNoteIndex-=12;
            }
        } else {
            randomNoteIndex = (int) Math.floor(Math.random()*12);
//            randomNoteIndex = 0;
            intervalNoteIndex = randomNoteIndex + numSemitones;
            if (intervalNoteIndex > 11) {
                intervalNoteIndex-=12;
            }
            randomNote = new TargetNote(
                    noteList.get(randomNoteIndex).getNoteName(),
                    noteList.get(randomNoteIndex).getNoteFrequency(),
                    noteList.get(randomNoteIndex).getNoteResource());
            intervalNote = new TargetNote(
                    noteList.get(intervalNoteIndex).getNoteName(),
                    noteList.get(intervalNoteIndex).getNoteFrequency(),
                    noteList.get(intervalNoteIndex).getNoteResource());

            randomNote.adjustNotes(randomNoteIndex, false);
            randomNote.adjustNotes(randomNoteIndex, false);
            intervalNote.adjustNotes(intervalNoteIndex, true);
        }

        Log.d("randomNoteName", String.valueOf(randomNote.getNoteName()));
        Log.d("randomNoteFreq", String.valueOf(randomNote.getNoteFrequency()));
        Log.d("intervalNoteName", String.valueOf(intervalNote.getNoteName()));
        Log.d("intervalNoteFreq", String.valueOf(intervalNote.getNoteFrequency()));

        mp = MediaPlayer.create(IntervalTrainingActivity.this, randomNote.getNoteResource());
        mp.start();
        new Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        mp = MediaPlayer.create(IntervalTrainingActivity.this, intervalNote.getNoteResource());
                        mp.start();
                    }
                }, 1000);


        //prevents the app from 'listening' to itself
        new Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        statusText.setText(R.string.now_listening);
                        if (didGetBaseNote) {
                            shouldBaseListen = false;
                            shouldIntervalListen = true;
                        } else {
                            shouldBaseListen = true;
                            shouldIntervalListen = false;
                        }
                    }
                }, 2500);
    }

    public void scorePoint(int points) {
        scoreText.setText(points + " pts");
    }

    public void reset() {
        randomNote = null;
        intervalNote = null;
        switchFragments();
    }

    public void switchFragments() {
        if (didGetBaseNote) {
            didGetBaseNote = false;
            shouldBaseListen = true;
            shouldIntervalListen = false;

            basePitchMatchFragment.getView().findViewById(R.id.disableFilter).setVisibility(View.GONE);
            intervalPitchMatchFragment.getView().findViewById(R.id.disableFilter).setVisibility(View.VISIBLE);

            ((TextView)intervalPitchMatchFragment.getView().findViewById(R.id.noteText)).setText("");
            ((ImageView)intervalPitchMatchFragment.getView().findViewById(R.id.imageView)).setImageResource(R.drawable.circle_blue);
        } else {
            didGetBaseNote = true;
            shouldBaseListen = false;
            shouldIntervalListen = true;
            basePitchMatchFragment.getView().findViewById(R.id.disableFilter).setVisibility(View.VISIBLE);
            intervalPitchMatchFragment.getView().findViewById(R.id.disableFilter).setVisibility(View.GONE);
        }
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(IntervalTrainingActivity.this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            //permission not granted
            ActivityCompat.requestPermissions(IntervalTrainingActivity.this, new String[] {Manifest.permission.RECORD_AUDIO},
                    PERMISSIONS_REQUEST_RECORD_AUDIO);
        } else {
            //permission granted
            Log.d("permission", "GRANTED");
//            isPermissionGranted = true;
            disableFilter.setVisibility(View.GONE);
            startApp();
        }
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
