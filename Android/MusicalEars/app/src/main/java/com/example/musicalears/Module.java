package com.example.musicalears;

import java.util.ArrayList;
import java.util.List;

public class Module {
    private String name;
    private String description;

    //constructor
    private Module(String newName, String newDesc) {
        this.name = newName;
        this.description = newDesc;
    }

    public static List<Module> modules = new ArrayList<Module>(){{
        add(new Module("Pitch Matching", "Match the given note, in any octave, using your voice or an instrument. Hold the note for three seconds to score a point."));
        add(new Module("Interval Training", "After matching the given note, find the note a certain interval above (or below) the initial note. Hold the second note for three seconds to score a point."));

    }};

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String toString() {
        return this.name;
    }
}
