package com.andrus.easy3;

import static com.andrus.easy3.C.presets;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class Sequencer {
    public final int MAXSTEPS=24;
    public boolean active=false;
    int stepCount=0;
    int currentStep=0;
    public ArrayList<Step> steps;
    public double timePassed;

    public void clearSteps() {
        steps=new ArrayList<>();
    }

    public void gotoStep(int i) {
        currentStep=i;
        timePassed=0.;
    }

    //------------------------------------------------
    // update the sequencer

    public void update(double interval) {
        if (active) {              // only update if active

            // if a step is available start it

            if ((currentStep == 0) && (steps.size() > 0)) {
                currentStep++;
                steps.get(currentStep).start();
            }

            // if a step is already running, update it

            else {
                if (currentStep > 0) {
                    if (steps.get(currentStep).update(interval)) {
                        currentStep++;
                        if (currentStep > steps.size()) {
                            steps.get(currentStep).start();
                        }
                    }
                }
            }
        }
    }

    // ====================================================
    // a small class for each step.

    class Step {
        public double length;   // in seconds
        public int preset;
        public String color;
        public double percent;

        Step(double length, int preset, String color) {
            if (length==0) {
                length=60;
            }
            this.length=length;
            this.preset=preset;
            this.color=color;
        }

        void start () {
            timePassed=0;
            presets.load(preset);
        }

        // update for delta time

        boolean update(double interval) {
            timePassed+=interval;
            percent=timePassed/length*100.;
            if (timePassed>length) {
                timePassed=0;   // reset if done
                return true;
            }
            else {
                return false;
            }
        }
    }

    Sequencer () {
        steps = new ArrayList<>();
        stepCount=0;
    }

    // add a step and return true if successful

    public boolean addStep (double length, int preset, String color) {
        if (steps.size() <= MAXSTEPS) {
            steps.add(new Step(length, preset, color));
            return true;
        }
        else {
            return false;
        }
    }
}
