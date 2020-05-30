package com.example.musicalears;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Switch;

public class PitchSettingsActivity extends SettingsActivity {
    private static final String PARAM_NUM_NOTES = "numNotes";
    private static final String PARAM_DURATION = "duration";
    private static final String PARAM_SHOULD_SHOW_NOTE_NAME = "showName";

    private Switch targetNoteToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pitch_settings);

        shortNumButton = findViewById(R.id.button1);
        mediumNumButton = findViewById(R.id.button2);
        longNumButton = findViewById(R.id.button3);
        easyDiffButton = findViewById(R.id.button4);
        mediumDiffButton = findViewById(R.id.button5);
        hardDiffButton = findViewById(R.id.button6);
        targetNoteToggle = findViewById(R.id.targetNoteToggle);
        numNotesText = findViewById(R.id.numNotesText);
        difficultyText = findViewById(R.id.difficultyText);

        shortNumButton.setOnClickListener(this);
        mediumNumButton.setOnClickListener(this);
        longNumButton.setOnClickListener(this);
        easyDiffButton.setOnClickListener(this);
        mediumDiffButton.setOnClickListener(this);
        hardDiffButton.setOnClickListener(this);

        ImageButton startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startModule();
            }
        });
        setTexts();
    }

    @Override
    protected void toggleButtons(int buttonId) {
        super.toggleButtons(buttonId);
        setTexts();
    }

    private void setTexts() {
        setDifficultyText();
        String numNotesString = numberOfNotes + " notes will need to be matched.";
        numNotesText.setText(numNotesString);
    }

    private void startModule() {
        Intent intent = new Intent(this, PitchMatchingActivity.class);
        intent.putExtra(PARAM_NUM_NOTES, numberOfNotes);
        intent.putExtra(PARAM_DURATION, duration);
        intent.putExtra(PARAM_SHOULD_SHOW_NOTE_NAME, targetNoteToggle.isChecked());
        startActivity(intent);
    }
}
