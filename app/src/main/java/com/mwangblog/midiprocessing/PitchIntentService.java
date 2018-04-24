package com.mwangblog.midiprocessing;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;

public class PitchIntentService extends IntentService {

    public static final String SELECT_FORMAT = "selectFromat";
    public static final String PITCH_IN_HZ = "pitchInHz";

    private static Boolean mIsRunning = false;

    private static final String TAG = "PitchIntentService";

    private Thread mAudioThread;
    private static float mPitchInHz;

    public PitchIntentService () {
        super("PitchIntentService");
        Log.i (TAG, "Start.");
        HzGetInit();
        mIsRunning = true;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
    }

    private void HzGetInit () {
        AudioDispatcher dispatcher =
                AudioDispatcherFactory.fromDefaultMicrophone(22050,1024,0);
        PitchDetectionHandler pdh = new PitchDetectionHandler() {
            @Override
            public void handlePitch(PitchDetectionResult res, AudioEvent e){
                final float pitchInHz = res.getPitch();
                Handler pitchHandler = new Handler(Looper.getMainLooper());
                pitchHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        processHz(pitchInHz);
                        // Log.i (TAG, "Pitch: " + pitchInHz);
                    }
                });
            }
        };
        AudioProcessor pitchProcessor = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, pdh);
        dispatcher.addAudioProcessor(pitchProcessor);
        mAudioThread = new Thread(dispatcher, "Audio Thread");
        Log.i (TAG, "NEW Audio Thread.");
        mAudioThread.start();
    }

    public void processHz (float pitchInHz) {
        mPitchInHz = pitchInHz;
    }

    public static float getPitchInHz() {
        return mPitchInHz;
    }

    public static Boolean isRunning() {
        return mIsRunning;
    }
}
