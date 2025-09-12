package com.andrus.easy3;

import static com.andrus.easy3.C.presets;
import static com.andrus.easy3.C.updateSequencerGUI;

import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class Sequencer {
    public final int MAXSTEPS=80;
    public boolean active=false;
    public volatile int currentStep=0;
    public ArrayList<Step> steps;
    public double timePassed;

    public void clearSteps() {
        steps=new ArrayList<>();
    }

    public int stepCount() {
        return steps.size();
    }

    public void gotoStep(int i) {
        currentStep=i-1;
        timePassed=0.;
        if (active) {
            steps.get(currentStep).start();
            Log.i("SEQUENCER", "STARTING step ---> "+i);
        }
        else {
            Log.i("SEQUENCER", "Selected step "+i+" but not active.");
        }
    }

    //------------------------------------------------
    // update the sequencer

    public void update(double interval) {
        if (active) {              // only update if active

            // Log.i("SEQUENCER","update (step "+currentStep+" )");
            // if a step is available start it

            if ((currentStep == 0) && (steps.size() >= 0)) {
                // start at first step if nothing selected
                currentStep=1;
                steps.get(currentStep-1).start();
                Log.i("SEQUENCER","sequencer start first step");
                updateSequencerGUI=true;
            }

            // if a step is already running, update it

            else {
                if (currentStep > 0) {
                    // Log.i("SEQUENCER","update sequencer step "+currentStep+" "+steps.get(currentStep-1).percent);
                    if (steps.get(currentStep-1).update(interval)) {
                        Log.i("SEQUENCER", "Step currentStep done.");
                        currentStep++;
                        if (currentStep > steps.size()) {
                            // rewind to begining of list if at end
                            currentStep = 1;
                            Log.i("SEQUENCER", "Rewinding to first step");

                        }
                        Step thisStep=steps.get(currentStep-1);
                        thisStep.start();
                        Log.i("SEQUENCER", "Starting step "+currentStep+" length "+thisStep.length+" preset "+thisStep.preset);
                        updateSequencerGUI=true;

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
            // Log.i("SEQUENCER","Start load "+ preset);
            presets.load(preset);
            updateSequencerGUI=true;
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
