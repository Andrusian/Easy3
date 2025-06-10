package com.andrus.easy3;

// implements a simple mixer for two signals

public class Mixer {
    public double ratio = .5;

    Mixer(double ratio) {
        this.ratio=ratio;
    }

    public void setRatio(double r) {
        if (r > 1.0) r = 1.0;
        if (r < 0) r = 0.0;
        ratio = r;
    }

    public double mix(double a, double b) {
        double out;
        out = a * ratio + b * (1. - ratio);

        return out;
    }
}
