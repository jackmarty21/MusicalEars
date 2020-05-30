package com.example.musicalears;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

class SettingsActivity extends AppCompatActivity implements View.OnClickListener {
    ImageButton shortNumButton;
    ImageButton mediumNumButton;
    ImageButton longNumButton;
    ImageButton easyDiffButton;
    ImageButton mediumDiffButton;
    ImageButton hardDiffButton;
    TextView numNotesText;
    TextView difficultyText;

    int numberOfNotes = 20;
    long duration = 3000;

    protected void setDifficultyText() {
        String difficultyString = "Notes will need to be held for " + duration / 1000;
        if (duration == 1000)
            difficultyString = String.format("%s second.", difficultyString);
        else
            difficultyString = String.format("%s seconds.", difficultyString);
        difficultyText.setText(difficultyString);
    }

    protected void toggleButtons(int buttonId) {
        switch (buttonId) {
            case R.id.button1:
                numberOfNotes = 10;
                shortNumButton.setBackgroundResource(R.drawable.short_on);
                mediumNumButton.setBackgroundResource(R.drawable.medium_off);
                longNumButton.setBackgroundResource(R.drawable.long_off);
                break;
            case R.id.button2:
                numberOfNotes = 20;
                shortNumButton.setBackgroundResource(R.drawable.short_off);
                mediumNumButton.setBackgroundResource(R.drawable.medium_on);
                longNumButton.setBackgroundResource(R.drawable.long_off);
                break;
            case R.id.button3:
                numberOfNotes = 50;
                shortNumButton.setBackgroundResource(R.drawable.short_off);
                mediumNumButton.setBackgroundResource(R.drawable.medium_off);
                longNumButton.setBackgroundResource(R.drawable.long_on);
                break;
            case R.id.button4:
                duration = 1000;
                easyDiffButton.setBackgroundResource(R.drawable.easy_on);
                mediumDiffButton.setBackgroundResource(R.drawable.medium_off);
                hardDiffButton.setBackgroundResource(R.drawable.hard_off);
                break;
            case R.id.button5:
                duration = 3000;
                easyDiffButton.setBackgroundResource(R.drawable.easy_off);
                mediumDiffButton.setBackgroundResource(R.drawable.medium_on);
                hardDiffButton.setBackgroundResource(R.drawable.hard_off);
                break;
            case R.id.button6:
                duration = 5000;
                easyDiffButton.setBackgroundResource(R.drawable.easy_off);
                mediumDiffButton.setBackgroundResource(R.drawable.medium_off);
                hardDiffButton.setBackgroundResource(R.drawable.hard_on);
                break;
        }
    }

    @Override
    public void onClick(View view) {
        toggleButtons(view.getId());
    }
}
