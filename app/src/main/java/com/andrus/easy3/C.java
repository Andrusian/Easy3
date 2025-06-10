package com.andrus.easy3;

//=============================================================
// Common parameter database static class
//
// Implements the patch board driven by modules.

import android.widget.SeekBar;
import android.widget.Spinner;

public class C {
    public static volatile double patch[]=new double[200];

    static public final int PATCH_PAD1X=0;
    static public final int PATCH_PAD1Y=1;

    static public final int PATCH_PAD2X=2;
    static public final int PATCH_PAD2Y=3;

    static public final int PATCH_PAD3X=4;
    static public final int PATCH_PAD3Y=5;

    static public final int PATCH_PAD4X=6;
    static public final int PATCH_PAD4Y=7;

    static public final int PATCH_PAD5X=8;
    static public final int PATCH_PAD6X=9;
    static public final int PATCH_PAD7X=10;
    static public final int PATCH_PAD8X=11;

    static public final int PATCH_LFREQ1=12;
    static public final int PATCH_LFREQ2=13;

    static public final int PATCH_RFREQ1=14;
    static public final int PATCH_RFREQ2=15;

    static public final int PATCH_LMIX=16;
    static public final int PATCH_RMIX=17;


    static public final int PATCH_SPARE1=50;
    static public final int PATCH_SPARE2=51;
    static public final int PATCH_SPARE3=52;
    static public final int PATCH_SPARE4=53;
    static public final int PATCH_SPARE5=54;

    static public final int PATCH_CONSTANT_VOL_DEFAULT=100;
    static public final int PATCH_CONSTANT_ONE=101;
    static public final int PATCH_CONSTANT_TWO=102;
    static public final int PATCH_CONSTANT_FIVE=103;
    static public final int PATCH_CONSTANT_TEN=104;
    static public final int PATCH_CONSTANT_TWENTY=105;
    static public final int PATCH_CONSTANT_THREEQUARTERS=106;
    static public final int PATCH_CONSTANT_HALF=107;
    static public final int PATCH_CONSTANT_THIRD=108;
    static public final int PATCH_CONSTANT_QUARTER=109;
    static public final int PATCH_CONSTANT_FIFTH=110;

    static public final double freqL1default=750.;
    static public final double freqL2default=900.;
    static public final double freqR1default=700.;
    static public final double freqR2default=840.;
    static public final double freqAmodL1default=.3;
    static public final double freqAmodL2default=10;
    static public final double freqAmodR1default=.3;
    static public final double freqAmodR2default=10;

    static volatile public SeekBar rightMix;
    static volatile public AudioTouchPad padL1;
    static volatile public AudioTouchPad padL2;
    static volatile public SeekBar dutyL1;
    static volatile public SeekBar dutyL2;
    static volatile public AudioTouchPad padR1;
    static volatile public AudioTouchPad padR2;
    static volatile public SeekBar dutyR1;
    static volatile public SeekBar dutyR2;
    static volatile public Spinner waveformSpinner1;
    static volatile public Spinner waveformSpinner2;
    static volatile public Spinner waveformSpinner3;
    static volatile public Spinner waveformSpinner4;

    static volatile public Spinner waveformSpinnerL1;
    static volatile public Spinner waveformSpinnerL2;
    static volatile public Spinner waveformSpinnerR1;
    static volatile public Spinner waveformSpinnerR2;

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
    public static SeekBar leftMix;

    public static float freqScaleY(float input) {
        return ((input-300.f) / (1200.f - 300.f));
    }

    public static SequencerLayoutFragment sequencerFragment;


}
