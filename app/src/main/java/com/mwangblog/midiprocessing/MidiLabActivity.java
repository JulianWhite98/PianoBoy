package com.mwangblog.midiprocessing;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

public class MidiLabActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return MidiLabFragment.newInstance();
    }
}
