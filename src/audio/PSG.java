package audio;

import audio.PSG.DynamicWaveform;
import audio.PSG.Waveform;

/** PSG = Programmable sound generator
 * @author Shreyas Raghunath
 * The PSG interface defines the framework for the CachedPSG class that generates sounds in real time.
 * It can also be used for other classes that may play back pre-recorded samples or generate sounds in real time as well.
 * */
public interface PSG extends Runnable {
    /**
     * Constant used in sampling waveforms.
     * */
    double TWO_PI = 2 * Math.PI;
    /**
     * Pre-defined percussion waveforms used when the PSG is in percussion mode.
     * */
    Waveform[] PERCUSSION_WAVEFORMS = {Waveform.KICK, Waveform.SNARE};
    /**
     * Start the PSG.
     * */
    void start();
    /**
     * Stop the PSG.
     * */
    void stop();
    /**
     * Toggle percussion mode.
     * @param b Percussion mode (true = yes, false = no)
     * @return The PSG instance itself.
     * */
    PSG setPercussion(boolean b);
    /**
     * Set the master volume.
     * @param d The master volume from 0 to 1.
     * @return The PSG instance itself.
     * */
    PSG setLoudness(double d);
    /**
     * Get the master volume.
     * @return The master volume from 0 to 1.
     * */
    double getLoudness();
    /**
     * Get the state of the PSG.
     * @return If the PSG is still running, return true, false otherwise.
     * */
    boolean isRunning();
    /**
     * Determine if the PSG is in percussion mode.
     * @return If the PSG is in percussion mode, return true, false otherwise.
     * */
    boolean isPercussion();
    /**
     * Set the playback speed of the PSG.
     * @param d The new playback speed.
     * @return The PSG instance itself.
     * */
    PSG setPlaybackSpeed(double d);
    /**
     * Get the playback speed of this PSG.
     * @return The playback speed.
     * */
    double getPlaybackSpeed();
    /**
     * Get the name of this PSG.
     * @return The name.
     * */
    String getName();
    /**
     * Get the size of this PSG instance's command list.
     * @return The length of the command list.
     * */
    int getLength();
    /**
     * @author Shreyas Raghunath
     * The Waveform interface represents a waveform used to generate samples, such as square waves, triangle waves, etc.
     * */
    @FunctionalInterface
    interface Waveform {
        /**
         * Constant used to scale waveforms that have a range of [-PI/2, PI/2] to [-1, 1].
         * */
        double TWO_OVER_PI = 2 / Math.PI;
        /**
         * 50%-width pulse wave, also known as a square wave. _|-|_|-|_
         * */
        Waveform SQUARE = x -> Math.signum(Math.sin(x));
        /**
         * Triangle wave. ^v^v^
         * */
        Waveform TRIANGLE = x -> TWO_OVER_PI * Math.asin(Math.sin(x));
        /**
         * Sawtooth wave. /|/|/|
         * */
        Waveform SAWTOOTH = x -> TWO_OVER_PI * Math.atan(Math.tan(x / 2));
        /**
         * Kick drum. Uses exponential decay to only sound the thump at the beginning and remove noise that might come after.
         * */
        Waveform KICK = x -> Math.sin(25 * Math.log(x)) / Math.exp(x / 175);
        /**
         * Snare drum. Similar to the kick drum but mixes in white noise to make it sound more like a snare drum.
         * */
        Waveform SNARE = x -> {
            double base = Math.cos(3.5 * Math.pow(Math.log(x), 2));
            for (byte i = 0; i < 4; i++) base += Math.random();
            return base / Math.exp(x / 175);
        };
        /**
         * The output of the waveform at the given x.
         * @param x The input value.
         * @return The output of the waveform at the given x.
         * */
        double output(double x);
        /**
         * Combine different waveforms to make one waveform that contains all the sounds of the constituent waveforms.
         * @param waveforms The list of waveforms to be combined.
         * @return The output waveform.
         * */
        static Waveform combine(Waveform... waveforms) {
            return x -> {
                double sum = 0.0;
                for (Waveform w : waveforms) sum += w.output(x);
                return sum / waveforms.length;
            };
        }
        /**
         * Create a waveform that involves one waveform dissolving into another waveform.
         * @param from The first waveform, which the new waveform will transition out of.
         * @param to The second waveform, which the new waveform will transition into.
         * @return The output waveform.
         * */
        static Waveform dissolve(Waveform from, Waveform to) {
            return new DynamicWaveform() {
                public double output(double x) {
                    return (1 - n()) * from.output(x) + n() * to.output(x);
                }
            };
        }
    }
    /**
     * @author Shreyas Raghunath
     * A waveform that can change over time.
     * */
    abstract class DynamicWaveform implements Waveform {
        /**
         * The parameter that determines how far along the change goes.
         * */
        private double n;
        /**
         * Return n itself.
         * @return n.
         * */
        protected final double n() {
            return n;
        }
        /**
         * Set n within CachedPSG.
         * @param n The new value of n from 0 to 1.
         * */
        public final void setN(double n) {
            if (n >= 0.0 && n <= 1.0) this.n = n;
        }
    }
}