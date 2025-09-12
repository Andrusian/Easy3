package com.andrus.easy3;



// when called, debug will log information for a short period of time.

import static java.sql.Types.NULL;

import android.content.Context;
import android.util.Log;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Debug {
    private double TIME=1.0;  // in seconds
    private int INTERVAL=1000;  // in seconds
    double elapsedTime=TIME;
    private int countdown=0;
    private int iteration=0;
    PrintWriter outfile;
    final double SAMPLERATE = 1/48000;
    private boolean triggered;

    public void reset () {
        countdown=0;
        elapsedTime=0;
        iteration++;
    }

    // opens the output file

    public void init (Context context, double time, int interval) {
        TIME=time;
        INTERVAL=interval;

        try {
            outfile = new PrintWriter(new FileWriter(context.getFilesDir() + "/waveform.dat"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void trigger () {
      triggered=true;
      Log.i("EASY3", "Writing debug file.");
    }

    // logs a a value when interval runs out. Assumed to be called at 48kHz.

    public void log ( double a, double b, double c) {
        if (triggered) {
            elapsedTime += SAMPLERATE;
            if (countdown == 0) {   // sample every nnnn iterations
                outfile.println(String.format("%08d", iteration) + "\t" + String.format("%5.2f", a) + ",\t" + String.format("%5.2f", b)+ ",\t" + String.format("%5.2f", c));
                countdown = INTERVAL;
            }
            iteration++;
            countdown--;

            // if total time reached untrigger the logger and reset

            if (elapsedTime > TIME) {
                triggered = false;
                reset();
                outfile.flush();
            }
        }
    }

    public void close () {
        outfile.close();
    }
}
