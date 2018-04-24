package com.mwangblog.midiprocessing;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

public class MidiActivity extends SingleFragmentActivity {


    private static final String EXTRA_MIDI_NAME = "com.mwangblog.midiporcessing";
    private static final String TAG = "MidiActivity";

    @Override
    protected Fragment createFragment() {
        String name = (String) getIntent().getSerializableExtra(EXTRA_MIDI_NAME);
        return MidiFragment.newInstance(name);
    }

    public static Intent newIntent (Context packageContext, String name) {
        Intent intent = new Intent (packageContext, MidiActivity.class);
        intent.putExtra(EXTRA_MIDI_NAME, name);
        return intent;
    }
}
