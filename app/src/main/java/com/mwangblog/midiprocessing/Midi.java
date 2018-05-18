package com.mwangblog.midiprocessing;

import android.app.Application;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.util.Log;

import com.mwangblog.midiprocessing.event.MidiEvent;
import com.mwangblog.midiprocessing.util.MidiEventListener;
import com.mwangblog.midiprocessing.util.MidiProcessor;
import com.mwangblog.midiprocessing.util.MidiUtil;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class Midi {
    private String mAssetPath;
    private String mName;

    private AssetManager mAssets;
    private SoundPool mSoundPool;
    private int mSoundId;
    private int mStreamId;
    private ArrayList<String> mMidiInfo;
    private MyNotes mMyNotes = null;
    private PitchList mMidiPitchList;

    private static final int MAX_SOUNDS = 1;
    private static final String TAG = "Midi";

    public Midi (String assetPath) {
        mAssetPath = assetPath;
        String[] components = assetPath.split("/");
        String filename = components[components.length-1];
        mName = filename.replace(".mid","");

        mAssets = AssetsApp.getmContext().getAssets();

        midiPlayInit();
        // getMidiInfo();
    }

    private void midiPlayInit () {
        mSoundPool = new SoundPool.Builder().setMaxStreams(MAX_SOUNDS)
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()).build();
        try {
            AssetFileDescriptor afd = mAssets.openFd(this.getAssetPath());
            int soundId = mSoundPool.load (afd, 1);
            this.setSoundId(soundId);
            // Log.i (TAG, this.getName() + " load successfully.");
        } catch (IOException ioe) {
            Log.e (TAG, "Could not load " + this.getName());
        }
    }

    public void setMidiInfo () {
        InputStream is = null;
        if (mMidiInfo == null) {
            try {
                is = mAssets.open(this.getAssetPath());
                MidiFile midiFile = new MidiFile(is);
                is.close();
                MidiProcessor processor = new MidiProcessor(midiFile);
                MidiEventPrinter ep = new MidiEventPrinter(mName);
                processor.registerEventListener(ep, MidiEvent.class);
                processor.start();
                while (processor.isRunning()) {
//                    Log.i (TAG, "Test whether processor finished.");
                    Thread.sleep(100);
                }
                mMidiInfo = ep.getInfo();
                mMyNotes = new MyNotes(midiFile.getResolution(),ep.getTempo(), ep.getNoteOns(), ep.getNoteOffs());
                Log.i(TAG, "setMidiInfo() successfully.");
            } catch (IOException ioe) {
                Log.e(TAG, "setMidiInfo() wrong : init InputStream.");
            } catch (InterruptedException ie) {
                Log.e(TAG, "setMidiInfo() wrong : Thread sleep ie.");
            }
            // Log.i(TAG, "init InputStream successfully.");
        } else {

        }
    }

    public PitchList getMidiPitchList() {
        return mMidiPitchList;
    }

    public void setMidiPitchList(int delayTime) {
        if (mMyNotes == null || mMidiPitchList != null) {
            return;
        }
        mMidiPitchList = new PitchList();
        long endMs = mMyNotes.getEndMs();
        Log.i (TAG, "setMidiPitchList, endMs = " + endMs);
        for (int i = 0; i*delayTime < endMs; i++) {
            boolean isSet = false;
            for (MyNote myNote : mMyNotes.getMyNotes()) {
                if (myNote.getOnMs() <=  (i*delayTime) && myNote.getOffMs() >= (i*delayTime)) {
                    mMidiPitchList.getPitchList().add(myNote.getPitch());
                    isSet = true;
                    break;
                }
            }
            if (!isSet) {
                mMidiPitchList.getPitchList().add(0);
            }
        }
    }

    public ArrayList<String> getMidiInfo () {
        return mMidiInfo;
    }

    public void play () {
        Integer soundId = this.mSoundId;
        if (soundId == null) {
            return;
        }
        mStreamId = mSoundPool.play (soundId, 1.0f, 1.0f, 1, 0, 1.0f);
    }

    public void stop () {
        mSoundPool.stop(mStreamId);
    }

    public void release () {
        mSoundPool.release();
    }

    public String getAssetPath() {
        return mAssetPath;
    }

    public String getName() {
        return mName;
    }

    public int getSoundId() {
        return mSoundId;
    }

    public void setSoundId(int soundId) {
        this.mSoundId = soundId;
    }

    public MyNotes getMyNotes() {
        return mMyNotes;
    }
}
