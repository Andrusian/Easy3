package com.andrus.easy3;

import static com.andrus.easy3.Oscillator.*;
import static com.andrus.easy3.C.*;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.util.Log;

import java.util.Random;

public class Synth {

    public Debug mydebug;

    private static final int SAMPLE_RATE = 48000;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_OUT_STEREO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_FLOAT;
    private static final int PATCH_BOARD_SIZE = 200;
    private static final double DEFAULTVOL = 0.85;

    private AudioTrack audioTrack;
    private boolean isPlaying = false;
    private Thread audioThread;

    // random silence function

    public int silenceMode=SILENCE_OFF;
    public int boostMode=SILENCE_OFF;   // boost and silence use same stuff

    public double silenceDelay=20;
    public double silenceLength=3;
    public int silenceEvents=1;
    public double silenceTrigger=0;
    public double silenceTimer=0;
    public double silenceEventCount=0;
    public boolean silenceTriggered;
    public boolean silenceStatus;
    private double silenceDuration=1;

    // Oscillators

    volatile public Oscillator oscL1;    // Left carrier primary
    volatile public Oscillator oscL2;    // Left carrier secondary
    public Oscillator oscR1;    // Right carrier primary
    public Oscillator oscR2;    // Right carrier secondary

    // Mixers

    public volatile Mixer mixL;          // Left carrier mixer
    public volatile Mixer mixR;          // Right carrier mixer

    public Oscillator amodL1;   // Left carrier primary
    public Oscillator amodL2;   // Left carrier secondary
    public Oscillator amodR1;   // Right carrier primary
    public Oscillator amodR2;   // Right carrier secondary

    Oscillator oscA;   // spare oscillator
    Oscillator oscB;   // spare oscillator
    Oscillator oscC;   // spare oscillator

    double ex1;        // extra osc outputs - merely for debugging
    double ex2;
    double ex3;

    OutputModule outMod;
    Context context;
    Random rnd=new Random();
    private double silenceDuration2;
    public double countdown;
    public boolean dosilence;


    public Synth(Context c) {

        // enable the debug class to capture samples
        mydebug=new Debug();
        mydebug.init(c,1.0,1000);

        context=c;
        int minBufferSize = AudioTrack.getMinBufferSize(
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT);

        // make sure all patch cables are turned off
        for (int i=0; i<NUM_DESITNATIONS;i++) {
            destinationFlags[i]=false;
        }

       // Create a buffer larger than minimum to ensure smooth playback
        int bufferSize = Math.max(minBufferSize, SAMPLE_RATE); // 1 second buffer

        audioTrack = new AudioTrack.Builder()
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build())
                .setAudioFormat(new AudioFormat.Builder()
                        .setSampleRate(SAMPLE_RATE)
                        .setEncoding(AUDIO_FORMAT)
                        .setChannelMask(CHANNEL_CONFIG)
                        .build())
                .setBufferSizeInBytes(bufferSize * 4) // 4 bytes per float
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build();

        // set up default constant positions on the patchboard


        // INITIALIZE SIGNAL CHAIN
        //      |
        //      |
        //    \ | /
        //     \|/

        oscL1= new Oscillator(MODE_CARRIER,SINE,"L Carrier1", C.freqL1default);
        oscL2= new Oscillator(MODE_CARRIER, SINE, "L Carrier2",C.freqL2default);
        oscR1= new Oscillator(MODE_CARRIER, SINE, "R Carrier1",C.freqR1default);
        oscR2= new Oscillator(MODE_CARRIER, SINE, "R Carrier2",C.freqR2default);

        // duty cycles for square waves on carriers are always 100%
        // so set that here as a default

        oscL1.setDuty(.5);
        oscL2.setDuty(.5);
        oscR1.setDuty(.5);
        oscR2.setDuty(.5);

        mixL=new Mixer(1);  // signal 1 100%
        mixR=new Mixer(1);  // signal 1 100%

        amodL1= new Oscillator(MODE_LFO,OFF,"L1 AMOD",C.freqAmodL1default);
        amodL2= new Oscillator(MODE_LFO,OFF,"L2 AMOD",C.freqAmodL2default);
        amodR1= new Oscillator(MODE_LFO,OFF,"R1 AMOD",C.freqAmodR1default);
        amodR2= new Oscillator(MODE_LFO,OFF,"R2 AMOD",C.freqAmodR2default);

        outMod=new OutputModule();
        outMod.doRampin(3);    // do a long fade in on initial start

        oscA = new Oscillator(MODE_EXTRA,SINE,"OSCA",1.);
        oscB = new Oscillator(MODE_EXTRA,SINE,"OSCB",1.);
        oscC = new Oscillator(MODE_EXTRA,SINE,"OSCC",1.);
    }

    public void start() {

        if (isPlaying) return;

        isPlaying = true;
        audioTrack.play();

        audioThread = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean debug = true;
                boolean first_time = true;

                // Use a smaller buffer for more frequent updates (0.1 seconds)
                int bufferSize = SAMPLE_RATE / 5;   // not 1/10 because it is stereo
                float[] stereoBuffer = new float[bufferSize * 2]; // Stereo, so 2 samples per frame

                // Log.i("EASY3","Vol L: "+outMod.volL+" Vol R: "+outMod.volR);

                while (isPlaying) {

                    // Fill the buffer with audio data. Calculate 1/10 of a second
                    // on each outer loop.

                    for (int i = 0; i < bufferSize; i++) {

                        // CALCULATE SIGNAL CHAIN (one sample)
                        //      |
                        //      |
                        //    \ | /
                        //     \|/

                        // The Extra oscilators affect various
                        // other things so do them first.
                        // We don't need the values from these
                        // they are written to the destinations array.

                        if (oscA.isActive()) {
                            double ex1 = oscA.val(1.);
                        }
                        if (oscB.isActive()) {
                            double ex2 = oscB.val(1.);
                        }
                        if (oscC.isActive()) {
                            double ex3 = oscC.val(1.);
                        }

                        // carrier sound oscillators
                        // these are hard coded and have normalized values

                        // phase1 affects both L1 and L2 carriers
                        // no need to shift right oscillators

                        if (destinationFlags[DEST_LEFTPHASE]) {
                            oscL1.setPhase(destinations[DEST_LEFTPHASE]);
                        }

                        //---------------------------
                        double l1 = oscL1.val(1.);
                        double l2 = oscL2.val(1.);
                        double r1 = oscR1.val(1.);
                        double r2 = oscR2.val(1.);
                        //----------------------------

                        // mix the signals out of the carrier oscillators

                        //--------------------------
                        double l3 = mixL.mix(l1, l2);
                        double r3 = mixR.mix(r1, r2);
                        //--------------------------

                        // first layer amplitude modulation units

                        // only amodL1 does phase. This is done
                        // to allow alternating-style signals

                        if (destinationFlags[DEST_L1PHASE]) {
                            amodL1.setPhase(destinations[DEST_L1PHASE]);
                        }
                        if (destinationFlags[DEST_L1FREQ]) {
                            amodL1.setFreq(destinations[DEST_L1FREQ]);
                            C.padL1.setOverride(true);
                        } else {
                            C.padL1.setOverride(false);
                        }
                        if (destinationFlags[DEST_L1DEPTH]) {
                            amodL1.setDepth(destinations[DEST_L1DEPTH]);
                            C.padL1.setOverride(true);
                        } else {
                            C.padL1.setOverride(false);
                        }

                        if (destinationFlags[DEST_L1DUTY]) {
                            amodL1.setDuty(destinations[DEST_L1DUTY]);
                        } else {
                        }

                        if (destinationFlags[DEST_R1FREQ]) {
                            amodR1.setFreq(destinations[DEST_R1FREQ]);
                            C.padR1.setOverride(true);
                        } else {
                            C.padR1.setOverride(false);
                        }

                        if (destinationFlags[DEST_R1DEPTH]) {
                            amodR1.setDuty(destinations[DEST_R1DEPTH]);
                            C.padR1.setOverride(true);
                        } else {
                            C.padR1.setOverride(false);
                        }


                        if (destinationFlags[DEST_R1DUTY]) {
                            amodR1.setDuty(destinations[DEST_R1DUTY]);
                        } else {
                        }

                        //-----------------------
                        double l4 = amodL1.val(l3);
                        double r4 = amodR1.val(r3);
                        //-----------------------

                        // second layer amplitude modulation units

                        if (destinationFlags[DEST_L2FREQ]) {
                            amodL2.setFreq(destinations[DEST_L2FREQ]);
                            C.padL2.setOverride(true);
                        } else {
                            C.padL2.setOverride(false);
                        }

                        if (destinationFlags[DEST_R2FREQ]) {
                            amodR2.setFreq(destinations[DEST_R2FREQ]);
                            C.padR2.setOverride(true);
                        } else {
                            C.padR2.setOverride(false);
                        }

                        if (destinationFlags[DEST_R2DUTY]) {
                            amodR2.setDuty(destinations[DEST_R2DUTY]);
                        } else {
                        }

                        //-----------------------
                        double l5 = amodL2.val(l4);
                        double r5 = amodR2.val(r4);
                        //-----------------------

                        // output module

                        if (destinationFlags[DEST_LEFTVOL]) {
                            outMod.setVolL(destinations[DEST_LEFTVOL]);
                        }
                        if (destinationFlags[DEST_RIGHTVOL]) {
                            outMod.setVolR(destinations[DEST_RIGHTVOL]);
                        }

                        //-----------------------
                        double l6 = outMod.getL(l5);
                        double r6 = outMod.getR(r5);
                        //-----------------------

                        //-------------------------------------------
                        // handle the random silence function

                        if ((silenceMode != 0) || (boostMode != 0)) {
                            dosilence = false;

                            if ((silenceTrigger < 0) && (!silenceTriggered)) {
                                silenceTriggered = true;

                                setSilenceTimes();

                                silenceEventCount = 0;  // haven't done an event yet
                                silenceStatus = true;   // start with silence
                                countdown = silenceDuration;
                            } else if (silenceTriggered) {
                                if (silenceStatus) {
                                    // silence is active and we are happening!
                                    dosilence = true;  // mute signal
                                    countdown -= 1. / SAMPLE_RATE;
                                    if (countdown <= 0.) {
                                        silenceStatus = false;
                                        countdown = silenceDuration2;  // prepare to do the space between
                                    }
                                } else { // silenceStatus=false;
                                    dosilence = false;  // no muting
                                    countdown -= 1. / SAMPLE_RATE;
                                    if (countdown <= 0.) {
                                        silenceStatus = true;         //go back to silence
                                        silenceEventCount++;
                                        countdown = silenceDuration;  // prepare to do the silence
                                        if (silenceEventCount >= silenceEvents) {
                                            silenceTriggered = false;  // end silence trigger
                                        }
                                    }
                                }
                            } else {
                                // decrement the counter
                                silenceTrigger -= 1. / SAMPLE_RATE;
                                // might trigger next pass
                            }

                            // now, if silence flag is set apply it to the appropriate signal

                            if (silenceTriggered && dosilence && (silenceMode == SILENCE_ON)) {
                                l6 = 0.0;   // silence both signals
                                r6 = 0.0;
                            } else if (silenceTriggered && dosilence && (boostMode == SILENCE_ON)) {
                                l6 = l6 * 1.08;   // boost both signals
                                r6 = r6 * 1.08;
                            } else if (silenceTriggered && dosilence && (silenceMode == SILENCE_LEFT)) {
                                l6 = 0.0;    // just left
                            } else if (silenceTriggered && dosilence && (boostMode == SILENCE_LEFT)) {
                                l6 = l6 * 1.08; // just left boost
                            } else if (silenceTriggered && dosilence && (silenceMode == SILENCE_RIGHT)) {
                                r6 = 0.0;    // just right silence
                            } else if (silenceTriggered && dosilence && (boostMode == SILENCE_LEFT)) {
                                r6 = r6 * 1.08; // just right boost
                            }
                        } else {
                            setSilenceTimes();
                        }

                        // do debug logging if it is enabled

                        // mydebug.log(l5,destinations[DEST_LEFTVOL],l6);
                        mydebug.log(l3, l4, l6);

                        // store output sample
                        // note: left and right seem to be in swapped order
                        // so offset corrects this.

                        stereoBuffer[i * 2] = (float) l6;
                        stereoBuffer[i * 2 + 1] = (float) r6;
                    }

                    // Write buffer to audio track (blocking call if buffer is full)
                    audioTrack.write(stereoBuffer, 0, stereoBuffer.length, AudioTrack.WRITE_BLOCKING);

                    // calculate the visualization data

                    calculateVisualizationData(stereoBuffer, 0, bufferSize);

                    // update the sequencer step

                    sequencer.update(.2);
                }
            }

        });
        audioThread.setPriority(Thread.MAX_PRIORITY);
        audioThread.start();
    }

    //=====================================================================
    // Visualization calculations
    //---------------------------------------------------------------------

    public void calculateVisualizationData(float[] stereoBuffer, int startIndex, int numFrames) {
        // Copy waveform data
        int samples = Math.min(numFrames, AudioVizData.WAVEFORM_SIZE);
        for (int i = 0; i < samples; i++) {
            int idx = (startIndex + i) * 2;
            AudioVizData.leftWaveform[i] = stereoBuffer[idx];
            AudioVizData.rightWaveform[i] = stereoBuffer[idx + 1];
        }
        AudioVizData.waveformSamples = samples;

        // Compute FFT spectrum (use first 32k samples from buffer)
        computeSpectrum(stereoBuffer, startIndex);

        AudioVizData.dataReady = true;
    }

    private void computeSpectrum(float[] stereoBuffer, int startIndex) {
        final int FFT_SIZE = 32768; // 2^15

        // Extract channels for FFT
        float[] leftChannel = new float[FFT_SIZE];
        float[] rightChannel = new float[FFT_SIZE];

        int available = Math.min(FFT_SIZE, stereoBuffer.length / 2 - startIndex);
        for (int i = 0; i < available; i++) {
            int idx = (startIndex + i) * 2;
            leftChannel[i] = stereoBuffer[idx];
            rightChannel[i] = stereoBuffer[idx + 1];
        }

        // Compute FFT magnitude
        float[] leftMag = computeFFTMagnitude(leftChannel);
        float[] rightMag = computeFFTMagnitude(rightChannel);

        // Map to logarithmic frequency bins (30Hz - 4kHz)
        mapToFrequencyBins(leftMag, AudioVizData.leftSpectrum, FFT_SIZE);
        mapToFrequencyBins(rightMag, AudioVizData.rightSpectrum, FFT_SIZE);
    }

    private float[] computeFFTMagnitude(float[] signal) {
        int n = signal.length;
        float[] real = new float[n];
        float[] imag = new float[n];

        // Apply Hamming window
        for (int i = 0; i < n; i++) {
            float window = (float) (0.54 - 0.46 * Math.cos(2 * Math.PI * i / (n - 1)));
            real[i] = signal[i] * window;
            imag[i] = 0;
        }

        // Perform FFT
        fft(real, imag);

        // Compute magnitude (only need first half)
        int halfN = n / 2;
        float[] magnitude = new float[halfN];
        for (int i = 0; i < halfN; i++) {
            magnitude[i] = (float) Math.sqrt(real[i] * real[i] + imag[i] * imag[i]);
        }

        return magnitude;
    }

    private void fft(float[] real, float[] imag) {
        int n = real.length;
        if (n <= 1) return;

        // Bit reversal
        int j = 0;
        for (int i = 0; i < n - 1; i++) {
            if (i < j) {
                float tempR = real[i];
                real[i] = real[j];
                real[j] = tempR;
                float tempI = imag[i];
                imag[i] = imag[j];
                imag[j] = tempI;
            }
            int k = n / 2;
            while (k <= j) {
                j -= k;
                k /= 2;
            }
            j += k;
        }

        // Cooley-Tukey FFT
        for (int len = 2; len <= n; len *= 2) {
            float angle = (float) (-2 * Math.PI / len);
            float wlenR = (float) Math.cos(angle);
            float wlenI = (float) Math.sin(angle);

            for (int i = 0; i < n; i += len) {
                float wR = 1;
                float wI = 0;

                for (int k = 0; k < len / 2; k++) {
                    int idx1 = i + k;
                    int idx2 = i + k + len / 2;

                    float tR = wR * real[idx2] - wI * imag[idx2];
                    float tI = wR * imag[idx2] + wI * real[idx2];

                    real[idx2] = real[idx1] - tR;
                    imag[idx2] = imag[idx1] - tI;
                    real[idx1] += tR;
                    imag[idx1] += tI;

                    float tempW = wR;
                    wR = wR * wlenR - wI * wlenI;
                    wI = tempW * wlenI + wI * wlenR;
                }
            }
        }
    }

    private void mapToFrequencyBins(float[] fftMag, float[] bins, int fftSize) {
        final int SAMPLE_RATE = 48000;
        final float MIN_FREQ = 125f;
        final float MAX_FREQ = 4000f;

        int numBins = bins.length;
        float freqPerBin = (float) SAMPLE_RATE / fftSize;

        // Logarithmic frequency mapping
        for (int i = 0; i < numBins; i++) {
            float t = (float) i / (numBins - 1);
            float freq = MIN_FREQ * (float) Math.pow(MAX_FREQ / MIN_FREQ, t);
            int fftBin = (int) (freq / freqPerBin);

            if (fftBin < fftMag.length) {
                bins[i] = fftMag[fftBin];
            }
        }
    }

    //=====================================================================
    // routines for the random silence / boost effects
    //---------------------------------------------------------------------

    private void setSilenceTimes() {
        // reset silence Trigger time
        // range is 75% to 125% of delay time

        silenceTrigger= rnd.nextDouble()*silenceDelay*.5+silenceDelay*.75;

        // silence duration is 66% to 133% of specification
        // every event in this trigger will be the same
        // silence length is a percentage of unmodified silenceDelay

        silenceDuration=rnd.nextDouble()*silenceLength*silenceDelay*.66+silenceLength*.66;

        // so this is something like 5% of 30 seconds with some random variance
        // and then the space between events is simply half whatever this actual
        // silence turns out to be

        silenceDuration2=silenceDuration/2.;
    }

    //-----------------------------------------------------------------------------------
    // stop output

    public void stop() {
        isPlaying = false;
        if (audioThread != null) {
            try {
                audioThread.join(1000); // Wait for thread to finish
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (audioTrack != null) {
            audioTrack.stop();
            audioTrack.flush();
        }
    }

    public void release() {
        stop();
        if (audioTrack != null) {
            audioTrack.release();
            audioTrack = null;
        }
    }

    public void boostL(double amount){
        outMod.changeVolL(amount);
    }
    public void boostR(double amount){
        outMod.changeVolR(amount);
    }
    public void duckL(double amount){
        outMod.changeVolL(-amount);
    }
    public void duckR(double amount){
        outMod.changeVolR(-amount);
    }
    public void setVolL(double v) {
        outMod.setVolL(v);
    }
    public void setVolR(double v) {
        outMod.setVolR(v);
    }

    public void silence() {  // not to be confused with random silence
        outMod.setVolL(0.);
        outMod.setVolR(0.);
    }

    private double boundsCheck(double value, double min, double max) {
        if (value<min) {
            value=min;
        }
        if (value>max) {
            value=max;
        }
        return value;
    }

    //=============================================================================
    // This sets every component to hard-coded defaults.
    // This is done through touchpads where possible.

    public void initialize() {

        Log.i("EASY3","Synth initialization");

        // set the carrier frequency mixes

        leftMix.setProgress((int) outMod.volL*100);
        rightMix.setProgress((int) outMod.volR*100);   // all the way to freq1 (Y doesn't matter)

        // default to sine on all carriers

        waveformSpinnerL1.setSelection(0);
        waveformSpinnerL2.setSelection(0);
        waveformSpinnerR1.setSelection(0);
        waveformSpinnerR2.setSelection(0);

        // set the amplitude modulators

        padL1.gotoPosition(1.f,1.f,true);   // higher frequency, smoother
        padL2.gotoPosition(1.f,1.f,true);
        padR1.gotoPosition(1.f,1.f,true);
        padR2.gotoPosition(1.f,1.f,true);

        dutyL1.setProgress(50);   // 50% duty cycle
        dutyR1.setProgress(50);   // 50% duty cycle
        dutyL2.setProgress(50);   // 50% duty cycle
        dutyR2.setProgress(50);   // 50% duty cycle

        dutyL1.setEnabled(false);
        dutyL2.setEnabled(false);
        dutyR1.setEnabled(false);
        dutyR2.setEnabled(false);

        // set default amod forms to OFF

        waveformSpinner1.setSelection(0);
        waveformSpinner2.setSelection(0);
        waveformSpinner3.setSelection(0);
        waveformSpinner4.setSelection(0);

        // set the output module

        outMod.setVolL(DEFAULTVOL);
        outMod.setVolR(DEFAULTVOL);
    }

    public double getVolR() {
        return outMod.getVolR();
    }
    public double getVolL() {
        return outMod.getVolL();
    }
}
