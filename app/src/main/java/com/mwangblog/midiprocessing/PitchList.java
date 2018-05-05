package com.mwangblog.midiprocessing;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class PitchList {
    private ArrayList<Integer> mPitchList;

    public PitchList() {
        mPitchList = new ArrayList<Integer>();
    }

    public ArrayList<Integer> getPitchList() {
        return mPitchList;
    }
}
