package com.example.musicalears;

import java.util.List;

class TargetNote extends Note {

    TargetNote(String noteName, float noteFrequency) {
        super(noteName, noteFrequency);
    }

    float getUpperTwenty(boolean isInterval) {
        List<Note> list;
        if (isInterval) {
            list = adjustedIntervalNoteList;
        } else {
            list = adjustedNoteList;
        }
        return list.get(5).getNoteFrequency() + (list.get(6).getNoteFrequency() - list.get(5).getNoteFrequency())/2;
    }

    float getLowerTwenty(boolean isInterval) {
        List<Note> list;
        if (isInterval) {
            list = adjustedIntervalNoteList;
        } else {
            list = adjustedNoteList;
        }
        return list.get(5).getNoteFrequency() + (list.get(4).getNoteFrequency() - list.get(5).getNoteFrequency())/2;
    }
}
