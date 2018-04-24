package com.mwangblog.midiprocessing;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MidiLab {
    private static MidiLab sMidiLab;

    private static final String TAG = "MidiLab";
    private static final String SOUNDS_FOLDER = "midi_files";
    private static final int MAX_SOUNDS = 1;

    private AssetManager mAssets;
    private List<Midi> mMidis = new ArrayList<>();

    public List<Midi> getMidis() {
        return mMidis;
    }

    public static MidiLab get (Context context) {
        if (sMidiLab == null) {
            sMidiLab = new MidiLab(context);
        }
        return sMidiLab;
    }

    private MidiLab (Context context) {
        mAssets = context.getAssets();
        loadSounds();
    }

    private void load(Midi midi) throws IOException {
        AssetFileDescriptor afd = mAssets.openFd(midi.getAssetPath());
    }

    private void loadSounds () {
        String[] soundNames;
        try {
            soundNames = mAssets.list(SOUNDS_FOLDER);
            Log.i(TAG, "Found " + soundNames.length + " midi_sounds");
        } catch (IOException ioe) {
            Log.e(TAG, "Could not list assets", ioe);
            return;
        }

        for (String filename : soundNames) {
            try {
                String assetPath = SOUNDS_FOLDER + "/" + filename;
                Midi midi = new Midi(assetPath);
                load(midi);
                mMidis.add(midi);
            } catch (IOException ioe){
                Log.e (TAG, "Could not load sound " + filename, ioe);
            }
        }
    }

    public Midi getMidi (String name) {
        for (Midi midi : mMidis) {
            if (midi.getName().equals(name)) {
                return midi;
            }
        }
        return null;
    }
}
