package com.andrus.easy3;

import static com.andrus.easy3.Oscillator.SR;

import android.util.Log;

public class OutputModule {
    volatile public double volL;
    volatile public double volR;
    double bal;
    private double rampinSamples;
    private double rampinLength;
    private double rampinVal=1.0;

    OutputModule () {
        volL=0;
        volR=0;
    }

    public void setVolL(double v) {
        volL=v;
    }

    public void setVolR(double v) {
        volR=v;
    }

    // we'll calculate the rampin on Left Only because we
    // always call both

    public double getL(double in) {
        double out;
        if (rampinSamples>0.) {

            rampinSamples-=1;
            rampinVal=(1.-rampinSamples/rampinLength);
        }
        out=in*volL*rampinVal;
        return out;
    }

    public double getR(double in) {
        double out;
        // factor in rampinVal

        out=in*volR*rampinVal;
        return out;
    }

    public void setBal(double b) {
        bal=b;
    }

    public void doRampin(double length) {
        rampinSamples=length*SR;
        rampinLength=rampinSamples;
        rampinVal=0;
    }

    public double getVolR() {
        return volR;
    }
    public double getVolL() {
        return volL;
    }

    public void changeVolL(double amount) {
        double vol=volL+amount;
        volL=boundsCheck(vol,.05,1.);
    }
    public void changeVolR(double amount) {
        double vol=volR+amount;
        volR=boundsCheck(vol,.05,1.);
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

}
