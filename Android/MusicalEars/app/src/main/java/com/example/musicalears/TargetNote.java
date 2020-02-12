package com.example.musicalears;

public class TargetNote extends Note {
    private float targetUpperTwenty = -1;
    private float targetLowerTwenty = -1;

    //constructor
    public TargetNote(int noteId, String noteName, float noteFrequency, int noteResource) {
        super(noteId, noteName, noteFrequency, noteResource);
    }

    public float getTargetLowerTwenty() {
        return targetLowerTwenty;
    }

    public float getTargetUpperTwenty() {
        return targetUpperTwenty;
    }

    public void setTargetLowerTwenty(float targetLowerTwenty) {
        this.targetLowerTwenty = targetLowerTwenty;
    }

    public void setTargetUpperTwenty(float targetUpperTwenty) {
        this.targetUpperTwenty = targetUpperTwenty;
    }
}
