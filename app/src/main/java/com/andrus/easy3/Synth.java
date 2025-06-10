package com.andrus.easy3;

import static com.andrus.easy3.Oscillator.*;
import static com.andrus.easy3.C.*;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.util.Log;

public class Synth {

    private static final int SAMPLE_RATE = 48000;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_OUT_STEREO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_FLOAT;
    private static final int PATCH_BOARD_SIZE = 200;
    private static final double DEFAULTVOL = 0.7;

    private AudioTrack audioTrack;
    private boolean isPlaying = false;
    private Thread audioThread;

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

    OutputModule outMod;
    Context context;

    public Synth(Context c) {
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


        // SIGNAL CHAIN
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

        oscL1.setDuty(1.);
        oscL2.setDuty(1.);
        oscR1.setDuty(1.);
        oscR2.setDuty(1.);

        mixL=new Mixer(0.5);
        mixR=new Mixer(0.5);

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
                boolean debug=true;
                boolean first_time=true;


                // Use a smaller buffer for more frequent updates (0.1 seconds)
                int bufferSize = SAMPLE_RATE / 5;
                float[] stereoBuffer = new float[bufferSize * 2]; // Stereo, so 2 samples per frame

                // Log.i("EASY3","Vol L: "+outMod.volL+" Vol R: "+outMod.volR);

                while (isPlaying) {

                    // Fill the buffer with audio data
                    for (int i = 0; i < bufferSize; i++) {

                        // SIGNAL CHAIN
                        //      |
                        //      |
                        //    \ | /
                        //     \|/

                        // The Extra osccilators affect various
                        // other things so do them first.
                        // We don't need the values from these
                        // they are written to the destinations array.

                        if (oscA.isActive()) {
                            double ex1=oscA.val(1.);
                        }
                        if (oscB.isActive()) {
                            double ex2=oscB.val(1.);
                        }
                        if (oscC.isActive()) {
                            double ex3=oscC.val(1.);
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

                        mixL.setRatio(100-leftMix.getProgress());
       // TODO                 mixR.setRatio(100-rightMix.getProgress());

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
                        }
                        else {
                            C.padL1.setOverride(false);
                        }
                        if (destinationFlags[DEST_L1DEPTH]) {
                            amodL1.setDepth(destinations[DEST_L1DEPTH]);
                            C.padL1.setOverride(true);
                        }
                        else {
                            C.padL1.setOverride(false);
                        }

                        if (destinationFlags[DEST_L1DUTY]) {
                            amodL1.setDuty(destinations[DEST_L1DUTY]);
                        }
                        else {
                              }

                        if (destinationFlags[DEST_R1FREQ]) {
                            amodR1.setFreq(destinations[DEST_R1FREQ]);
                            C.padR1.setOverride(true);
                        }
                        else {
                            C.padR1.setOverride(false);
                        }

                        if (destinationFlags[DEST_R1DEPTH]) {
                            amodR1.setDuty(destinations[DEST_R1DEPTH]);
                            C.padR1.setOverride(true);
                        }
                        else {
                            C.padR1.setOverride(false);
                        }


                        if (destinationFlags[DEST_R1DUTY]) {
                            amodR1.setDuty(destinations[DEST_R1DUTY]);
                        }
                        else {
                          }

                        //-----------------------
                        double l4=amodL1.val(l3);
                        double r4=amodR1.val(r3);
                        //-----------------------

                        // second layer amplitude modulation units

                        if (destinationFlags[DEST_L2FREQ]) {
                            amodL2.setFreq(destinations[DEST_L2FREQ]);
                            C.padL2.setOverride(true);
                        }
                        else {
                            C.padL2.setOverride(false);
                        }

                        if (destinationFlags[DEST_R2FREQ]) {
                            amodR2.setFreq(destinations[DEST_R2FREQ]);
                            C.padR2.setOverride(true);
                        }
                        else {
                            C.padR2.setOverride(false);
                        }

                        if (destinationFlags[DEST_R2DUTY]) {
                            amodR2.setDuty(destinations[DEST_R2DUTY]);
                        }
                        else {
                            }

                        //-----------------------
                        double l5=amodL2.val(l4);
                        double r5=amodR2.val(r4);
                        //-----------------------

                        // output module

                        if (destinationFlags[DEST_LEFTVOL]) {
                            outMod.setVolL(destinations[DEST_LEFTVOL]);
                        }
                        if (destinationFlags[DEST_RIGHTVOL]) {
                            outMod.setVolR(destinations[DEST_RIGHTVOL]);
                        }

                        //-----------------------
                        double l6=outMod.getL(l5);
                        double r6=outMod.getR(r5);
                        //-----------------------

                        // store output sample
                        // note: left and right seem to be in swapped order
                        // so offset corrects this.

                        stereoBuffer[i * 2] = (float) l6;
                        stereoBuffer[i * 2+1] = (float) r6;
                    }

                    // Write buffer to audio track (blocking call if buffer is full)
                    audioTrack.write(stereoBuffer, 0, stereoBuffer.length, AudioTrack.WRITE_BLOCKING);

//                    if (debug && first_time) {
//                        first_time = false;
//                        try {
//                            PrintWriter outfile = new PrintWriter(new FileWriter(context.getFilesDir() + "/waveform.dat"));
//                            for (int i = 0; i < bufferSize; i++) {
//                                outfile.println("" + i + "\t" + stereoBuffer[i * 2] + ",\t" + stereoBuffer[i * 2 + 1]);
//                            }
//                            outfile.close();
//
//                        } catch (IOException e) {
//                            Log.i("EASY3", "Could not write debug file.");
//                        }
//                    }
                }

                // update the sequencer step

                sequencer.update(.1);

            }

        });
        audioThread.setPriority(Thread.MAX_PRIORITY);
        audioThread.start();
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

    public void silence() {
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

    //----------------------------------------------------
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
