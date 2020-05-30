package com.example.musicalears.Note;

import androidx.annotation.NonNull;

import java.util.List;

public class TargetNote extends Note {
    private NoteList noteList;

    public TargetNote(String noteName, float noteFrequency, int noteIndex) {
        super(noteName, noteFrequency);
        this.noteList = new NoteList(noteIndex);
    }

    public List<Note> getNoteList() {
        return noteList.getAdjustedNoteList();
    }

    @NonNull
    @Override
    public String toString() {
        return getNoteName() + " - " + getNoteFrequency() + " - " + getNoteList();
    }
}
