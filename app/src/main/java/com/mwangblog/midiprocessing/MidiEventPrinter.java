package com.mwangblog.midiprocessing;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.mwangblog.midiprocessing.MidiFile;
import com.mwangblog.midiprocessing.event.MidiEvent;
import com.mwangblog.midiprocessing.event.NoteOff;
import com.mwangblog.midiprocessing.event.NoteOn;
import com.mwangblog.midiprocessing.event.meta.Tempo;
import com.mwangblog.midiprocessing.util.MidiEventListener;
import com.mwangblog.midiprocessing.util.MidiProcessor;

public class MidiEventPrinter implements MidiEventListener
{
    private static final String TAG = "MidiEventPrinter";
    private String mLabel;
    private ArrayList<String> mInfo;
    private Tempo mTempo = null;
    private ArrayList<NoteOn> mNoteOns;
    private ArrayList<NoteOff> mNoteOffs;

    public MidiEventPrinter(String label)
    {
        mLabel = label;
        mInfo = new ArrayList<String>();
        mNoteOns = new ArrayList<NoteOn>();
        mNoteOffs = new ArrayList<NoteOff>();
    }

    public ArrayList<String> getInfo() {
        return mInfo;
    }

    public Tempo getTempo() {
        return mTempo;
    }

    public ArrayList<NoteOn> getNoteOns() {
        return mNoteOns;
    }

    public ArrayList<NoteOff> getNoteOffs() {
        return mNoteOffs;
    }

    // 0. Implement the listener functions that will be called by the
    // MidiProcessor
    @Override
    public void onStart(boolean fromBeginning)
    {
        if(fromBeginning)
        {
            Log.i(TAG, mLabel + " Started!");
            mInfo.add (mLabel + " Started!");
        }
        else
        {
            // Log.i(TAG,mLabel + " resumed");
            mInfo.add (mInfo + mLabel + " resumed!");
        }
    }

    @Override
    public void onEvent(MidiEvent event, long ms)
    {
//        Log.i(TAG,mLabel + " received event: " + event);
        if (event instanceof Tempo) {
            mTempo = (Tempo) event;
        } else if (event instanceof NoteOn) {
            mNoteOns.add((NoteOn)event);
            mInfo.add(mLabel + " NoteOn: " + "NoteValue:" + ((NoteOn) event).getNoteValue()
                    + "-StartTick:" + event.getTick());
        } else if (event instanceof NoteOff) {
            mNoteOffs.add((NoteOff) event);
            mInfo.add(mLabel + " NoteOff: " + "NoteValue:" + ((NoteOff) event).getNoteValue()
                    + "-EndTick:" + event.getTick());
        } else {
            mInfo.add(mLabel + " received event " + event);
        }
        // mInfo.add(mLabel + " received event " + event);
    }

    @Override
    public void onStop(boolean finished)
    {
        if(finished)
        {
            Log.i(TAG,mLabel + " Finished!");
            mInfo.add(mLabel + " Finished!");
        }
        else
        {
            // Log.i(TAG,mLabel + " paused");
            mInfo.add(mInfo + mLabel + " paused!");
        }
    }
}
