package com.andrus.easy3;

//====================================================================
// Generates a signal from a single conventional mono oscillator with
// various possible waveforms. The waveform can either be bipolar
// or unipolar. Bipolar is suitable for final signals while unipolar is
// suitable for amplitude modulation.

import static com.andrus.easy3.C.destinationFlags;
import static com.andrus.easy3.C.destinations;
import static java.lang.Math.*;

import android.util.Log;

import java.util.Random;

public class Oscillator {

    // CONSTANTS------------------
    public final static int SINE=0;
    public final static int SQUARE=1;
    public final static int SAW=2;
    public final static int TRI=3;
    public final static int TENS=4;
    public final static int RANDOM=5;
    public final static int OFF =6;

    public final static int MODE_CARRIER=0;
    public final static int MODE_LFO=1;
    public final static int MODE_EXTRA=2;

    public final static int SR=48000;          // sample rate
    private static final double MAX_FREQ = 1500.;
    private static final double MIN_FREQ = 250.;

    Random RNG=new Random();

    // fields -----------------------------

    public double duty;            // duty cycle for square waves
    public volatile int form;
    private int lastForm;
    private int usage;
    public double ph;              // phase shift percent
    private int phX;                // phase shift samples
    public double vol;             // volume
    private int mode;  // default mode
    private double low;             // for an amod, low is the minimum value
    private final double high=1.0;  // high is not really used
    private double center;
    private boolean active=false;   // extra oscillators must be activated

    // TODO: implement volume controls for non-carrier signal oscillators
    // TODO: implement high and low volume limits for LFE inputs

    // volume each oscillator will be full by default
    // if we are mixing signals or shimming volume
    // that will be handled at a higher level.

    int pos=0;
    public double freq;
    double increment;
    private String name;
    private int dutyPos;
    int periodX;
    private double sawslope;
    private double trislope;
    private double sawval=0;
    private boolean sawreset=false;
    private int tridirection=0;
    private int tensPos;
    private boolean freqFlag;   // flag to reset zero position when frequency changes
    private double lastVal=0;
    private int posRepeat=0;
    private double outClipped;
    private double rndval;
    private double radPosition;
    private double radPhaseShift;
    private float oscval;
    private boolean goingUp=false;
    private boolean lastGoingUp=false;
    private int tensX;
    private double lastfreq=-2;
    int debugCount=0;
    public double depth=0.;

    public int destination=0;  // output of this oscillator drives another object
    public float maxValue;     // used for scaling in MODE_EXTRA
    public float minValue;     // used for scaling in MODE_EXTRA

    // constructor--------------------------------
    //
    // basic oscillator used for carrier frequency

    Oscillator(int m, int f, String label, double fre) {
        mode=m;
        form=f;       // default to sine
        lastForm=form;
        name=label;      // give this thing a name
        ph=0.0f;         // no phase shift by default
        duty=1.;         // default duty cycle
        setFreq(fre);
        calcDutyPos();
        calcPhase();
        vol=1.0;
        low=-1.;
        depth=0.0;

    }

//    Oscillator(int m, int f, String label, PatchCord ffpatch) {
//        this.ffpatch=ffpatch;
//        new Oscillator(m,f,label,ffpatch.val());
//    }

    //---------------------------------------------
    // getters and setters
    public void setForm(int a) {
        form = a;
        if (form==TRI) {
            sawval=-1.;   // doesn't start at zero
        }
        else if (form==SAW) {
            sawval=-1.;   // dpesn't start at zero
        }
    }
    public void setDuty(double d) {
        duty=d;
        calcDutyPos();
    }


    public void setFreq(double f) {

        // impose an upper bound on frequency for our purposes
        if (f>MAX_FREQ) {
            f=MAX_FREQ;
        }

        // if this is a carrier, set a lower bound on frequency

        if ((f<MIN_FREQ) && (mode==MODE_CARRIER)) {
            f=MIN_FREQ;
        }

        freq=f;

        if (freq!=lastfreq) {

            lastfreq=freq;
            if (freq != 0) {     // freq==0 turns off oscillator
                calcDutyPos();
                calcPhase();
                sawslope = 2.0 / dutyPos;
                trislope = 4.0 / dutyPos;
                periodX = (int) Math.floor(SR / freq);
                tensPos = (int) (0.2 * SR / freq);
                // radPosition = 0;      // needed? caused glitches
            }
        }
    }

    private void calcPhase() {
        // 1/freq is length of one wavelength in s
        // multiply by phase fraction and multi by 2PI
        // to get phaseshift in radians.

        radPhaseShift=ph*2*PI;
    }

    private void calcDutyPos() {
        double period;
        period = SR / freq;
        dutyPos = (int) (period * duty);  // it's a fraction of half the wave form
    }

    public int getForm() {
        return form;
    }

    public void setUsage(int u) {

        usage=u;
    }

    public void setPhase(double phase) {
        ph=phase;
        calcPhase();
    }

    public void setDepth(double depth) {
        this.depth=depth;
    }

    //===========================================================
    // Fetches one sample of the waveform.
    // The current position (e.g. current time)
    //  in the waveform is specified in
    // the calling function... this is normally the main loop
    // of the synthesizer layer.
    //
    // For some waveforms, a phase shift may apply.

    double val () {
        return val(1.0);
    }

    double val(double in) {
        double out;

        // if frequency is zero, the output becomes a
        // constant depending on the mode

        if (freq == 0.0) {
            if (mode == MODE_CARRIER) {
                out = 0.0;
            } else {
                out = in;
            }
            return out;
        }

        if (form == OFF) {
            out = in;
            return out;
        }

        //---------------------------------
        // CORE WAVE CALCULATION
        //---------------------------------
        // The SINE wave calculation drives
        // also drives the SAW and TRI
        // waveforms.
        //
        // The waveforms it is the base
        // for do not have duty cycles.
        //---------------------------------
        // To have accurate frequency, the variable radPosition
        // is used as the "MASTER" position and must be continuous
        // across subsequent calls to val();
        //
        // double radians = 2 * PI * freq * (double) ((pos + phX) / (double) SR);
        // --------------------------------
        // UNLESS NOTED, VALUES IN THIS SECTION ARE NORMALIZED: (-1 to 1) or (0 to 1)
        //---------------------------------

        oscval = (float) sin(radPosition + radPhaseShift);
        if (form != lastForm) {
            // Log.i("EASY3",this.name+ "'s form changed to "+form);
            lastForm = form;
        }

        switch (form) {
            case SINE:
            default:
                // duty cycle ignored

                out = oscval;
                break;

            case TRI:
                // a small drift will occur due
                // to accumulated errors
                // it's easiest to detect a max
                // to correct it there
                // (instead of a zero crossing)

                if (lastVal < oscval) {
                    goingUp = true;
                } else {
                    goingUp = false;
                }
                if ((lastGoingUp) && (!goingUp)) {
                    sawval = 1;     // reset position at peak
                }
                lastGoingUp = goingUp;

                // based on sine, duty cycle ignored
                if (goingUp) {
                    sawval += trislope;
                } else {
                    sawval -= trislope;
                }
                out = sawval;
                break;

            case SAW:
                // detect upwards zero crossing
                if ((lastVal < 0.) && (oscval > 0.)) {
                    sawval = -1;
                }
                sawval += sawslope;
                out = sawval;
                break;

            // SQUARE - version 2... based on SINE
            // Square has to be done two different ways.
            //
            // For CARRIER WAVES, the duty cycle is
            // always 100%
            //
            // for AMOD the range is still -1 to 1
            // but -1 corresponds to no signal

            case SQUARE:

                if (mode == MODE_CARRIER) {
                    if (oscval > 0) {
                        out = 1.f;
                    } else {
                        out = -1.f;
                    }
                }
                else {
                    // MODE is LFO or EXTRA

                    if ((posRepeat + phX) < dutyPos) {
                        out = 1;
                    } else {
                        out = -1;
                    }
                }
                break;

            case TENS:
                // TENS has constant volume
                // but instead makes wider pulses as volume
                // increases. Actual volume does not change.
                //
                //
                // rough calc:
                // 500Hz is 2000us period
                // aim for maybe 200us pulsewidth at 100% vol?
                // so conversion factor around 1% = 1usec?

                // System.out.printf("tens: x %d width %d%n", x, tensX);

                tensX = (int) Math.floor(0.2 * vol * SR / freq);

                if (posRepeat < tensX) {
                    out = 0.95;
                } else if (posRepeat < (tensX * 2)) {
                    out = -0.95;    // needs both polarities
                } else {
                    out = 0;
                }
                break;

            case RANDOM:
                // I was assuming LFO but it's
                // possible this could be used to
                // generate noise

                if ((posRepeat + phX) == 0) {
                    rndval = RNG.nextDouble() * 2. - 1;
                }
                out = rndval;
                break;

            // TODO: write delay mode
            // TODO: write ramp mode (LFO only)
            // TODO: write overdrive mode

        }

        // lastval is the last value of the sine() generator
        // oscval is the current value of the sine() generator
        // current output oscillator value is in 'out'

        lastVal = oscval;

        // move our current time forward
        // note that pos is preserved between calls and between sound packets

        pos++;                                  // this count is the continuous count for the whole buffer
        posRepeat++;                            // this count repeats every wavelength
        radPosition += 2.0 * Math.PI * freq / SR;        // advance time and position in wave

        if (radPosition > 2.0 * Math.PI) {
            radPosition -= 2.0 * Math.PI;
        }

        if (posRepeat >= periodX) {  // do the repeat
            posRepeat = 0;
        }


        if (mode == MODE_CARRIER) {                    // CARRIER ----------------------

            // carrier mode ignores input
            // it just generates pure signal in range -1 to 1
            // carrier mode outputs the full wave. In could optionally set volume

            return out * in;
        }
        else if (mode == MODE_LFO) {                 // AMOD LFO --------------------
            // LFO mode uses oscillator values above zero.
            // LFO mode can optionally modify an input
            //
            // 'out' is the 0 to 1 signal from the oscillator
            // 'in' is the input signal


            double modifier = Math.abs(out) * (1 - depth);
            outClipped = in * (1 - depth * (1 - (out + 1) / 2));

            // limit output within -1 to 1 and
            // implement clipping

            if (outClipped > 1.) {
                outClipped = 1.;
            } else if (outClipped < -1.) {
                outClipped = -1.;
            }
            return outClipped;
        }
        // this is MODE_EXTRA

        else if (mode==MODE_EXTRA) {                // EXTRA --------------------
            // EXTRA mode is for modifying parameters of other oscillators.
            // it doesn't take an input and allows any value range.
            //
            // Recommended limits are set by the ExtrasFragment.

            float outScaled=(float) out*((maxValue-minValue)+minValue);

            if (destination != 0) {
                destinations[destination] = outScaled;
            }
            return outScaled;
        }
        else {
            Log.i("EASY3", "Error - oscillator mode not identified");
            return 0;
        }
    }

    // TODO: this is used by the spare oscillators. These are not carriers
    // and are also not AMODs.

    public void setRange(float maxValue, float minValue) {

        this.maxValue=maxValue;
        this.minValue=minValue;

    }

    // record the destination for patch cord in this class
    // make sure the flag is set to turn if off

    public void setDestination(int purpose) {
        this.destination=purpose;
    }

    public int getDestination() {
        return this.destination;
    }

    public void setActive(boolean status) {
        active=status;
    }

    public boolean isActive() {
        return active;
    }

    public float getPhase() {
        return (float) ph;
    }

    public void setMax(double m) {
        this.maxValue=(float) m;
    }

    public void setMin(double m) {
        this.minValue=(float) m;
    }

    public void changeFreq(double ratio) {
        double newFreq=freq*ratio;
        setFreq(newFreq);

    }

    public void incFreq(double v) {
        double newFreq=freq+v;
        setFreq(newFreq);
    }
}
