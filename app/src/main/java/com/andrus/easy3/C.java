package com.andrus.easy3;

//=============================================================
// Common parameter database static class
//
// Implements the patch board driven by modules.

import android.widget.SeekBar;
import android.widget.Spinner;

public class C {
    public static volatile double patch[]=new double[200];


    static public final double freqL1default=750.;
    static public final double freqL2default=900.;
    static public final double freqR1default=750.;
    static public final double freqR2default=840.;
    static public final double freqAmodL1default=.3;
    static public final double freqAmodL2default=10;
    static public final double freqAmodR1default=.3;
    static public final double freqAmodR2default=10;

    public static LabeledSeekBar leftMix;
    static volatile public LabeledSeekBar rightMix;
    static volatile public AudioTouchPad padL1;
    static volatile public AudioTouchPad padL2;
    static volatile public LabeledSeekBar dutyL1;
    static volatile public LabeledSeekBar dutyL2;
    static volatile public AudioTouchPad padR1;
    static volatile public AudioTouchPad padR2;
    static volatile public LabeledSeekBar dutyR1;
    static volatile public LabeledSeekBar dutyR2;
    static volatile public Spinner waveformSpinner1;
    static volatile public Spinner waveformSpinner2;
    static volatile public Spinner waveformSpinner3;
    static volatile public Spinner waveformSpinner4;

    static volatile public Spinner waveformSpinnerL1;
    static volatile public Spinner waveformSpinnerL2;
    static volatile public Spinner waveformSpinnerR1;
    static volatile public Spinner waveformSpinnerR2;

    static volatile public boolean updateSequencerGUI=false;

    // globals for important objects

    static volatile public Presets presets;
    static volatile public Sequencer sequencer;
    public volatile static Synth synth;

    // common block items for inputs from spare oscillators
    // each item has a float value and a flag

    static final int NUM_DESITNATIONS=15;
    static volatile public float[] destinations = new float[NUM_DESITNATIONS];
    static volatile public boolean[] destinationFlags = new boolean[NUM_DESITNATIONS];

    static final int DEST_LEFTPHASE=1;
    static final int DEST_LEFTVOL=2;
    static final int DEST_RIGHTVOL=3;

    static final int DEST_L1PHASE=4;
    static final int DEST_L1FREQ=5;
    static final int DEST_L1DEPTH=6;
    static final int DEST_L1DUTY=7;

    static final int DEST_R1FREQ=8;
    static final int DEST_R1DEPTH=9;
    static final int DEST_R1DUTY=10;

    static final int DEST_L2FREQ=11;
    static final int DEST_R2FREQ=12;
    static final int DEST_R2DUTY=13;
    static final int SILENCE_OFF=0;
    static final int SILENCE_ON=1;
    static final int SILENCE_RIGHT=2;
    static final int SILENCE_LEFT=3;

    public static float freqScaleY(float input) {
        return ((input-300.f) / (1200.f - 300.f));
    }

    public static SequencerLayoutFragment sequencerFragment;



}
