package com.andrus.easy3;

public class AudioVizData {
    public static final int WAVEFORM_SIZE = 4800; // 100ms at 48kHz
    public static final int SPECTRUM_BINS = 64;

    public static volatile float[] leftWaveform = new float[WAVEFORM_SIZE];
    public static volatile float[] rightWaveform = new float[WAVEFORM_SIZE];
    public static volatile float[] leftSpectrum = new float[SPECTRUM_BINS];
    public static volatile float[] rightSpectrum = new float[SPECTRUM_BINS];
    public static volatile int waveformSamples = 0;
    public static volatile boolean dataReady = false;
}
