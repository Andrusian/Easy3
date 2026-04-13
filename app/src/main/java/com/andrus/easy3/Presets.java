package com.andrus.easy3;

import static com.andrus.easy3.C.*;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.Buffer;


public class Presets {
    int number = 0;   // max possible number of storepoints
    PrintWriter out;
    BufferedReader in;
    FileReader inF;
    Context context;

    Presets(Context context, int number) {
        this.number = number;
        this.context = context;
    }

    public void save(int number, String name, String desc, int color) {

        File dir = new File(context.getFilesDir(), "Easy3");
        if (!dir.exists()) dir.mkdirs();

        File file = new File(dir, "preset" + number + ".ini");
        try (OutputStream outStream = new FileOutputStream(file)) {
            out = new PrintWriter(outStream);
            writeSection("identification");
            saveString("name", name);
            saveString("desc", desc);
            saveInt("color", color);

            // save the output module first

            writeSection("output");

            saveItem("outL", synth.outMod.volL);
            saveItem("outR", synth.outMod.volR);

            writeSection("carriers");

            // store carrier wave settings

            saveItem("leftMix", leftMix.getProgress());
            saveItem("leftFreq", synth.oscL1.freq);
            saveItem("leftPhase", synth.oscL2.getPhase());
            saveItem("leftFreq2", synth.oscL2.freq);

            saveItem("rightMix", rightMix.getProgress());
            saveItem("rightFreq", synth.oscR1.freq);
            saveItem("rightFreq2", synth.oscR2.freq);

            saveItem("waveformL1", waveformSpinnerL1.getSelectedItemPosition());
            saveItem("waveformL2", waveformSpinnerL2.getSelectedItemPosition());
            saveItem("waveformR1", waveformSpinnerR1.getSelectedItemPosition());
            saveItem("waveformR2", waveformSpinnerR2.getSelectedItemPosition());

            writeSection("modulators");

            // save the amplitude modulators

            saveItem("amodL1form", waveformSpinner1.getSelectedItemPosition());
            saveItem("amodL1freq", synth.amodL1.freq);
            saveItem("amodL1depth", synth.amodL1.depth);
            saveItem("amodL1duty", synth.amodL1.duty);
            saveItem("amodL1phase", synth.amodL1.getPhase());

            saveItem("amodL2form", waveformSpinner2.getSelectedItemPosition());
            saveItem("amodL2freq", synth.amodL2.freq);
            saveItem("amodL2depth", synth.amodL2.depth);
            saveItem("amodL2duty", synth.amodL2.duty);

            saveItem("amodR1form", waveformSpinner3.getSelectedItemPosition());
            saveItem("amodR1freq", synth.amodR1.freq);
            saveItem("amodR1depth", synth.amodR1.depth);
            saveItem("amodR1duty", synth.amodR1.duty);

            saveItem("amodR2form", waveformSpinner4.getSelectedItemPosition());
            saveItem("amodR2freq", synth.amodR2.freq);
            saveItem("amodR2depth", synth.amodR2.depth);
            saveItem("amodR2duty", synth.amodR2.duty);

            // extra oscillator settings

            writeSection("extras");

            saveItem("oscAfreq", synth.oscA.freq);
            saveInt2("oscAform", synth.oscA.form);
            saveItem("oscAduty", synth.oscA.duty);
            saveItem("oscAphase", synth.oscA.getPhase());
            saveInt2("oscAdestination", synth.oscA.destination);
            saveBoolean("oscAenabled", destinationFlags[synth.oscA.destination]);
            saveItem("oscAmax", synth.oscA.maxValue);
            saveItem("oscAmin", synth.oscA.minValue);

            saveItem("oscBfreq", synth.oscB.freq);
            saveInt2("oscBform", synth.oscB.form);
            saveItem("oscBduty", synth.oscB.duty);
            saveItem("oscBphase", synth.oscB.getPhase());
            saveInt2("oscBdestination", synth.oscB.destination);
            saveBoolean("oscBenabled", destinationFlags[synth.oscB.destination]);
            saveItem("oscBmax", synth.oscB.maxValue);
            saveItem("oscBmin", synth.oscB.minValue);

            saveItem("oscCfreq", synth.oscC.freq);
            saveInt2("oscCform", synth.oscC.form);
            saveItem("oscCduty", synth.oscC.duty);
            saveItem("oscCphase", synth.oscC.getPhase());
            saveInt2("oscCdestination", synth.oscC.destination);
            saveBoolean("oscCenabled", destinationFlags[synth.oscC.destination]);
            saveItem("oscCmax", synth.oscC.maxValue);
            saveItem("oscCmin", synth.oscC.minValue);

            // silences boosts

            saveInt2("silenceMode", synth.silenceMode);
            saveInt2("boostMode", synth.boostMode);
            saveItem("silenceDelay", synth.silenceDelay);
            saveItem("silenceLength", synth.silenceLength);
            saveInt2("silenceEvents", synth.silenceEvents);

            out.flush();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private void saveBoolean(String tag, boolean value) {
        out.write(tag);
        out.write(" = ");
        out.write(String.valueOf(value));
        out.write("\n");
    }

    private void writeSection(String section) {
        out.write("[ ");
        out.write(section);
        out.write(" ]");
        out.write("\n");
    }

    private void saveItem(String tag, double value) {
        out.write(tag);
        out.write(" = ");
        out.write(String.valueOf(value));
        out.write("\n");
    }

    private void saveInt(String tag, int color) {
        out.write(tag);
        out.write(" = ");
        String colorst = String.format("#%06X", (0xFFFFFF & color));
        out.write(String.valueOf(colorst));
        out.write("\n");
    }

    private void saveInt2(String tag, int value) {
        out.write(tag);
        out.write(" = ");
        out.write(String.valueOf(value));
        out.write("\n");
    }

    private void saveString(String tag, String value) {
        out.write(tag);
        out.write(" = ");
        out.write(value);
        out.write("\n");
    }

    //=================================================================================

    public PresetItem getInfo(int number) {
        PresetItem presetInfo = new PresetItem();
        String filename;
        try {

            File dir = new File(context.getFilesDir(), "Easy3");
            if (!dir.exists()) dir.mkdirs();
            File file = new File(dir, "preset" + number + ".ini");
            try (BufferedReader in = new BufferedReader(new FileReader(file))) {
                String input;

                while ((input = in.readLine()) != null) {
                    String[] items = input.split("\\s=\\s");
                    //Log.i("EASY3", "Preset "+number+" exists.");

                    if (items.length == 2) {           // did the regex match?
                        String tag = items[0];       // first item is a tag
                        String setting = items[1];   // second item is value
                        presetInfo.num = number;
                        presetInfo.exists = true;


                        if (tag.equals("name")) {
                            presetInfo.name = setting;
                            //Log.i("EASY3", "Preset " + number + " name " + presetInfo.name);
                        } else if (tag.equals("desc")) {
                            presetInfo.desc = setting;
                            //Log.i("EASY3", "Preset " + number + " desc " + presetInfo.desc);
                        } else if (tag.equals("color")) {
                            presetInfo.color = items[1];
                            //Log.i("EASY3", "Preset " + number + " color " + presetInfo.color);
                        }
                    }
                }
            }
        } catch (IOException e) {
            //    Log.i("EASY3", "Preset "+number+" does not exist");
            //    Log.i("EASY3", e.getMessage());
            presetInfo.num = number;
            presetInfo.name = new String("Empty");
            presetInfo.exists = false;
            presetInfo.color = "#cccccc";
            return presetInfo;
        }
        return presetInfo;
    }

    //====================================================================

    public void load(int number) {
        PresetItem presetInfo = new PresetItem();
        String filename;
        try {

            File dir = new File(context.getFilesDir(), "Easy3");
            if (!dir.exists()) dir.mkdirs();
            File file = new File(dir, "preset" + number + ".ini");
            try (BufferedReader in = new BufferedReader(new FileReader(file))) {
                String input;


                synth.silenceMode = 0;   // clear for backwards compatibility
                synth.boostMode = 0;

                while ((input = in.readLine()) != null) {
                    String[] items = input.split("\\s=\\s");
                    if (items.length == 2) {           // did the regex match?
                        String tag = items[0];       // first item is a tag
                        String setting = items[1];   // second item is value


                        // CARRIERS...

                        if (tag.equals("leftMix")) {
                            double settingNum = Double.parseDouble(items[1]);
                            leftMix.setProgress((int) (settingNum * 100));
                        } else if (tag.equals("rightMix")) {
                            double settingNum = Double.parseDouble(items[1]);
                            rightMix.setProgress((int) settingNum);
                        } else if (tag.equals("leftFreq")) {
                            double settingNum = Double.parseDouble(items[1]);

                            synth.oscL1.setFreq(settingNum);
                            //Log.i("PRESET","leftFreq "+settingNum+" position "+freqScaleY((float)settingNum));
                        } else if (tag.equals("leftFreq2")) {
                            double settingNum = Double.parseDouble(items[1]);

                            synth.oscL2.setFreq(settingNum);
                            //Log.i("PRESET","leftFreq2 "+settingNum+" position "+freqScaleY((float)settingNum));
                        } else if (tag.equals("rightFreq")) {
                            double settingNum = Double.parseDouble(items[1]);

                            synth.oscR1.setFreq(settingNum);
                            //Log.i("PRESET","rightFreq "+settingNum+" position "+freqScaleY((float)settingNum));
                        } else if (tag.equals("rightFreq2")) {
                            double settingNum = Double.parseDouble(items[1]);

                            synth.oscR2.setFreq(settingNum);
                            //Log.i("PRESET","rightFreq2 "+settingNum+" position "+freqScaleY((float)settingNum));
                        } else if (tag.equals("waveformL1")) {
                            double settingNum = Double.parseDouble(items[1]);
//                        waveformSpinnerL1.setSelection((int)settingNum);
                        } else if (tag.equals("waveformL2")) {
                            double settingNum = Double.parseDouble(items[1]);
//                        waveformSpinnerL2.setSelection((int)settingNum);
                        } else if (tag.equals("waveformR1")) {
                            double settingNum = Double.parseDouble(items[1]);
//                        waveformSpinnerR1.setSelection((int)settingNum);
                        } else if (tag.equals("waveformR2")) {
                            double settingNum = Double.parseDouble(items[1]);
//                        waveformSpinnerR2.setSelection((int)settingNum);
                        }

                        // AMODS...

                        else if (tag.equals("amodL1freq")) {
                            double settingNum = Double.parseDouble(items[1]);
                            synth.amodL1.setFreq(settingNum);
                        } else if (tag.equals("amodL1depth")) {
                            double settingNum = Double.parseDouble(items[1]);
                            synth.amodL1.setDepth(settingNum);
                        } else if (tag.equals("amodL1duty")) {
                            double settingNum = Double.parseDouble(items[1]);
                            synth.amodL1.setDuty(settingNum);
                            dutyL1.setProgress((int) settingNum);
                        } else if (tag.equals("amodL2freq")) {
                            double settingNum = Double.parseDouble(items[1]);
                            synth.amodL2.setFreq(settingNum);
                        } else if (tag.equals("amodL2depth")) {
                            double settingNum = Double.parseDouble(items[1]);
                            synth.amodL2.setDepth(settingNum);
                        } else if (tag.equals("amodL2duty")) {
                            double settingNum = Double.parseDouble(items[1]);
                            synth.amodL2.setDuty(settingNum);
                            dutyL2.setProgress((int) settingNum);
                        } else if (tag.equals("amodR1freq")) {
                            double settingNum = Double.parseDouble(items[1]);
                            synth.amodR1.setFreq(settingNum);
                        } else if (tag.equals("amodR1depth")) {
                            double settingNum = Double.parseDouble(items[1]);
                            synth.amodR1.setDepth(settingNum);
                        } else if (tag.equals("amodR1duty")) {
                            double settingNum = Double.parseDouble(items[1]);
                            synth.amodR1.setDuty(settingNum);
                            dutyR1.setProgress((int) settingNum);
                        } else if (tag.equals("amodR2freq")) {
                            double settingNum = Double.parseDouble(items[1]);
                            synth.amodR2.setFreq(settingNum);
                        } else if (tag.equals("amodR2depth")) {
                            double settingNum = Double.parseDouble(items[1]);
                            synth.amodR2.setDepth(settingNum);
                        } else if (tag.equals("amodR2duty")) {
                            double settingNum = Double.parseDouble(items[1]);
                            synth.amodR2.setDuty(settingNum);
                            dutyR2.setProgress((int) settingNum);
                        } else if (tag.equals("amodL1form")) {
                            double settingNum = Double.parseDouble(items[1]);
                            waveformSpinner1.setSelection((int)settingNum);
                        } else if (tag.equals("amodL2form")) {
                            double settingNum = Double.parseDouble(items[1]);
                          waveformSpinner2.setSelection((int)settingNum);
                        } else if (tag.equals("amodR1form")) {
                            double settingNum = Double.parseDouble(items[1]);
                          waveformSpinner3.setSelection((int)settingNum);
                        } else if (tag.equals("amodR2form")) {
                            double settingNum = Double.parseDouble(items[1]);
                          waveformSpinner4.setSelection((int)settingNum);
                        } else if (tag.equals("outL")) {
                            double settingNum = Double.parseDouble(items[1]);
                            synth.outMod.setVolL(settingNum);
                            //Log.i("LOAD","Left volume set to "+settingNum);
                        } else if (tag.equals("outR")) {
                            double settingNum = Double.parseDouble(items[1]);
                            synth.outMod.setVolR(settingNum);
                            //Log.i("LOAD","Right volume set to "+settingNum);
                        } else if (tag.equals("oscAfreq")) {
                            double settingNum = Double.parseDouble(items[1]);
                            synth.oscA.setFreq(settingNum);
                            //Log.i("LOAD","oscA freq set to "+settingNum);
                        } else if (tag.equals("oscAform")) {
                            Log.i("EASY3", "oscAform" + items[1]);
                            int settingInt = Integer.parseInt((items[1]));
                            synth.oscA.setForm(settingInt);
                            //Log.i("LOAD","oscA form set to "+settingInt);
                        } else if (tag.equals("oscAduty")) {
                            double settingNum = Double.parseDouble((items[1]));
                            synth.oscA.setDuty(settingNum);
                            //Log.i("LOAD","oscA duty set to "+settingNum);
                        } else if (tag.equals("oscAphase")) {
                            double settingNum = Double.parseDouble((items[1]));
                            synth.oscA.setDuty(settingNum);
                            //Log.i("LOAD","oscA phase set to "+settingNum);
                        } else if (tag.equals("oscAdestination")) {
                            int settingInt = Integer.parseInt((items[1]));
                            synth.oscA.destination = settingInt;
                            //Log.i("LOAD","oscA dest set to "+settingInt);
                        } else if (tag.equals("oscAenabled")) {
                            boolean settingBool = Boolean.parseBoolean((items[1]));
                            destinationFlags[synth.oscA.destination] = settingBool;
                            synth.oscA.setActive(settingBool);
                            //Log.i("LOAD","oscA destFlag and active set to "+settingBool);
                        } else if (tag.equals("oscAmax")) {
                            double settingNum = Double.parseDouble((items[1]));
                            synth.oscA.setMax(settingNum);
                            //Log.i("LOAD","oscA max set to "+settingNum);
                        } else if (tag.equals("oscAmin")) {
                            double settingNum = Double.parseDouble((items[1]));
                            synth.oscA.setMin(settingNum);
                            //Log.i("LOAD","oscA min set to "+settingNum);
                        } else if (tag.equals("oscCfreq")) {
                            double settingNum = Double.parseDouble(items[1]);
                            synth.oscC.setFreq(settingNum);
                            //Log.i("LOAD","oscC freq set to "+settingNum);
                        } else if (tag.equals("oscCform")) {
                            int settingInt = Integer.parseInt((items[1]));
                            synth.oscC.setForm(settingInt);
                            //Log.i("LOAD","oscC form set to "+settingInt);
                        } else if (tag.equals("oscCduty")) {
                            double settingNum = Double.parseDouble((items[1]));
                            synth.oscC.setDuty(settingNum);
                            //Log.i("LOAD","oscC duty set to "+settingNum);
                        } else if (tag.equals("oscCphase")) {
                            double settingNum = Double.parseDouble((items[1]));
                            synth.oscC.setDuty(settingNum);
                            //Log.i("LOAD","oscC phase set to "+settingNum);
                        } else if (tag.equals("oscCdestination")) {
                            int settingInt = Integer.parseInt((items[1]));
                            synth.oscC.destination = settingInt;
                            //Log.i("LOAD","oscC dest set to "+settingInt);
                        } else if (tag.equals("oscCenabled")) {
                            boolean settingBool = Boolean.parseBoolean((items[1]));
                            destinationFlags[synth.oscC.destination] = settingBool;
                            synth.oscC.setActive(settingBool);
                            //Log.i("LOAD","oscC destFlag and active set to "+settingBool);
                        } else if (tag.equals("oscCmax")) {
                            double settingNum = Double.parseDouble((items[1]));
                            synth.oscC.setMax(settingNum);
                            //Log.i("LOAD","oscC max set to "+settingNum);
                        } else if (tag.equals("oscCmin")) {
                            double settingNum = Double.parseDouble((items[1]));
                            synth.oscC.setMin(settingNum);
                            //Log.i("LOAD","oscC min set to "+settingNum);
                        } else if (tag.equals("oscBfreq")) {
                            double settingNum = Double.parseDouble(items[1]);
                            synth.oscB.setFreq(settingNum);
                            //Log.i("LOAD","oscB freq set to "+settingNum);
                        } else if (tag.equals("oscBform")) {
                            int settingInt = Integer.parseInt((items[1]));
                            synth.oscB.setForm(settingInt);
                            //Log.i("LOAD","oscB form set to "+settingInt);
                        } else if (tag.equals("oscBduty")) {
                            double settingNum = Double.parseDouble((items[1]));
                            synth.oscB.setDuty(settingNum);
                            //Log.i("LOAD","oscB duty set to "+settingNum);
                        } else if (tag.equals("oscBphase")) {
                            double settingNum = Double.parseDouble((items[1]));
                            synth.oscB.setDuty(settingNum);
                            //Log.i("LOAD","oscB phase set to "+settingNum);
                        } else if (tag.equals("oscBdestination")) {
                            int settingInt = Integer.parseInt((items[1]));
                            synth.oscB.destination = settingInt;
                            //Log.i("LOAD","oscB dest set to "+settingInt);
                        } else if (tag.equals("oscBenabled")) {
                            boolean settingBool = Boolean.parseBoolean((items[1]));
                            destinationFlags[synth.oscB.destination] = settingBool;
                            synth.oscB.setActive(settingBool);
                            //Log.i("LOAD","oscB destFlag and active set to "+settingBool);
                        } else if (tag.equals("oscBmax")) {
                            double settingNum = Double.parseDouble((items[1]));
                            synth.oscB.setMax(settingNum);
                            //Log.i("LOAD","oscB max set to "+settingNum);
                        } else if (tag.equals("oscBmin")) {
                            double settingNum = Double.parseDouble((items[1]));
                            synth.oscB.setMin(settingNum);
                            //Log.i("LOAD","oscB min set to "+settingNum);
                        }

                        // silences and boosts

                        else if (tag.equals("silenceMode")) {
                            int settingInt = Integer.parseInt((items[1]));
                            synth.silenceMode = settingInt;
                        } else if (tag.equals("boostMode")) {
                            int settingInt = Integer.parseInt((items[1]));
                            synth.boostMode = settingInt;
                        } else if (tag.equals("silenceDelay")) {
                            double settingNum = Double.parseDouble((items[1]));
                            synth.silenceDelay = settingNum;
                        } else if (tag.equals("silenceLength")) {
                            double settingNum = Double.parseDouble((items[1]));
                            synth.silenceLength = settingNum;
                        } else if (tag.equals("silenceEvents")) {
                            int settingInt = Integer.parseInt((items[1]));
                            synth.silenceEvents = settingInt;

                        }


                    }
                }

                // need to set amod touch pad positions from the current
                // settings. It doesn't have to be exact. This is a pain in the ass
                // but the scaling isn't internal to the touchpads.
                // GENERALLY ASSUME THIS STUFF BELOW IS BROKEN

                float padDepth;
                float padFreq;

                padDepth = (float) (1 - synth.amodL1.depth);
                padFreq = (float) (Math.log(10 * synth.amodL1.freq) / 2.878);
                padL1.gotoPosition(padFreq, padDepth, false);

                padDepth = (float) (1 - synth.amodL2.depth);
                padFreq = (float) ((synth.amodL2.freq - 1) / 19.);
                padL2.gotoPosition(padFreq, padDepth, false);

                padDepth = (float) (1 - synth.amodR1.depth);
                padFreq = (float) (Math.log(10 * synth.amodR1.freq) / 2.878);
                padR1.gotoPosition(padFreq, padDepth, false);

                padDepth = (float) (1 - synth.amodR2.depth);
                padFreq = (float) ((synth.amodR2.freq - 1) / 19.);
                padR2.gotoPosition(padFreq, padDepth, false);

                synth.outMod.doRampin(1.);  // fade in

            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}


