package audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.SoftReference;
import java.util.*;

/** PSG = Programmable sound generator
 * @author Shreyas Raghunath
 * The CachedPSG class is an instance of the PSG interface that
 * caches generated samples to avoid unnecessarily spending CPU
 * power on generating new samples for commands that have already
 * been seen before.
 * */
public class CachedPSG implements PSG {
    /**
     * The sample rate in kHz. Set to 44.1 kHz for CD-quality audio.
     * */
    private static final float SAMPLE_RATE_KHZ = 44.1f;
    /**
     * The sample rate in Hz. Simply multiply the sample rate in kHz
     * by 1000.
     * */
    private static final float SAMPLE_RATE_HZ = SAMPLE_RATE_KHZ * 1e3f;
    /**
     * The format of the generated samples is 8-bit signed PCM with mono speaker support.
     * */
    private static final AudioFormat FORMAT = new AudioFormat(SAMPLE_RATE_HZ, 8, 1, true, false);
    // NOTE: Must make into a Map<Waveform, Map<String[], byte[]>> to get rid of bugs
    /**
     * The cache where the samples are stored. They are accessible using the
     * waveform they are played with and the command they correspond to.
     * */
    private static final Map<PSG.Waveform, Map<Command, SoftReference<byte[]>>> cache = new HashMap<>();
    /**
     * Boolean flags indicating the status of the CachedPSG instance
     * and whether it is in percussion mode.
     * */
    private boolean running, percussion;
    /**
     * Double variables indicating playback speed and master volume.
     * */
    private double playbackSpeed, loudness;
    /**
     * A pointer to the waveform that is currently in use.
     * */
    private byte wfPtr;
    /**
     * The sound channel to which the sample data is written to.
     * */
    private SourceDataLine channel;
    /**
     * The list of commands to play.
     * */
    private List<Command> commands;
    /**
     * The list of waveforms used by this instance.
     * */
    private PSG.Waveform[] waveforms;
    
    // printing
    /**
     * Used for debugging purposes only.
     * */
    public String name;
    /**
     * Creates a sub-cache to store each sample for future playback.
     * @return The new sub-cache that stores samples corresponding to a particular waveform.
     * */
    private static Map<Command, SoftReference<byte[]>> createMap() {
        return new HashMap<>();
    }
    static {
        cache.put(null, createMap());
        for (PSG.Waveform pwf : PSG.PERCUSSION_WAVEFORMS) cache.put(pwf, createMap());
    }
    /**
     * @param file The input stream from which the command data is read.
     * @param name The name of this instance. Used for debugging purposes.
     * @param waveforms The list of waveforms to be used with this instance.
     * */
    public CachedPSG(InputStream file, String name, PSG.Waveform... waveforms) {
        this();
        this.name = name;
        this.waveforms = waveforms;
        for (PSG.Waveform wf : waveforms) if (!cache.containsKey(wf)) cache.put(wf, createMap()); // Set up cache for waveforms
        try (BufferedReader r = new BufferedReader(new InputStreamReader(Objects.requireNonNull(file)))) { // Read the commands from the input stream
            commands = new LinkedList<>(); // Linked list because removals are O(1)
            for (String l = r.readLine(); l != null; l = r.readLine()) {
                if (!(l.isEmpty() || l.startsWith("//"))) { // Exclude empty spaces and comments
                    l = l.toLowerCase().trim();
                    byte commentIndex = (byte) l.indexOf("//");
                    if (commentIndex != -1) l = l.substring(0, commentIndex);
                    commands.add(new Command(l.split(" ")));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * @param file The input stream from which the command data is read.
     * @param name The name of this instance. Used for debugging purposes.
     * */
    public CachedPSG(InputStream file, String name) {
        this(file, name, PSG.Waveform.SQUARE);
    }
    /**
     * Default constructor
     * */
    private CachedPSG() {
        running = false;
        wfPtr = 0;
        playbackSpeed = loudness = 1;
        try {
            channel = AudioSystem.getSourceDataLine(FORMAT); // Set up underlying audio channel
            channel.open();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }
    /**
     * Start the thread for this PSG.
     * */
    public synchronized void start() {
        new Thread(this, toString()).start();
        running = true;
    }
    /**
     * The run() method is where the magic happens. The PSG runs through each command.
     * */
    public void run() {
        try {
            channel.start();
            boolean hasOpening = false;
            for (Iterator<Command> it = commands.iterator(); it.hasNext() && !hasOpening; hasOpening = it.next().strings[0].equals("end"));
            if (hasOpening) { // Remove commands that are part of the opening so that they are never played again ion future loops
                for (Command l = commands.remove(0); !l.strings[0].equals("end"); l = commands.remove(0)) {
                    process(l);
                }
            }
            for (Command l : commands) process(l);
            running = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Parse a single character using radix 16 (hexadecimal).
     * @param c the character to be parsed.
     * @return the hexadecimal value represented by the character.
     * */
    private static byte parseSingleCharHex(char c) {
        if (c >= '0' && c <= '9') {
            return (byte) (c - '0');
        } else if (c >= 'A' && c <= 'F') {
            return (byte) (c - ('A' - 10));
        } else if (c >= 'a' && c <= 'f') {
            return (byte) (c - ('a' - 10));
        }
        return 0;
    }
    /**
     * For each command:
     * 1. Decode it.
     * 2. Set the appropriate waveform (if required).
     * 3. Create the sample if it is not already created or retrieve it from the cache.
     * 4. Play it back.
     * @param l The command to parse and run.
     * */
    private void process(Command l) { // l is the split line that contains an instruction to play a tone/white noise
        if (l.strings[0].charAt(0) == 'c') { // A = 10, B = 11, C = 12, D = 13, E = 14, F = 15
            wfPtr = parseSingleCharHex(l.strings[1].charAt(0));
        } else {
            boolean noise = l.strings[0].charAt(0) == 'w';
            PSG.Waveform currentWF = null; // null used to represent white noise waveform
            if (percussion) {
                currentWF = PERCUSSION_WAVEFORMS[parseSingleCharHex(l.strings[0].charAt(0)) % PERCUSSION_WAVEFORMS.length];
            } else if (!noise) {
                currentWF = waveforms[wfPtr];
            }
            Map<Command, SoftReference<byte[]>> wfCache = cache.get(currentWF); // remember this is a POINTER to a map
            SoftReference<byte[]> sample = wfCache.getOrDefault(l, null); // Get the sample corresponding to l
            if (sample == null) { // If the sample is not found, create it. Decode the command here.
                double amp = Double.parseDouble(l.strings[1]);
                double timeMS = Double.parseDouble(l.strings[2]);
                byte[] s;
                if (noise) {
                    s = genWhiteNoise(l.strings[0].length() > 1 ? Integer.parseInt(l.strings[0].substring(1)) : 1, amp, timeMS, l.strings.length == 4);
                } else if (l.strings.length == 4) {
                    s = genTone(currentWF, Double.parseDouble(l.strings[0]), amp, timeMS, l.strings[3].contains("a"), l.strings[3].contains("v"));
                } else {
                    s = genTone(currentWF, percussion ? 440.0 : Double.parseDouble(l.strings[0]), amp, timeMS, false, false);
                }
                sample = new SoftReference<>(s);
                wfCache.put(l, sample);
            }
            channel.write(sample.get(), 0, Objects.requireNonNull(sample.get()).length); // Here, the sample is finally played back by the sound channel
        }
    }
    /**
     * Stop the PSG.
     * */
    public void stop() {
        channel.drain();
        channel.stop();
        running = false;
    }
    /**
     * Toggle percussion mode.
     * @param percussion Percussion mode (true = yes, false = no)
     * @return The PSG instance itself.
     * */
    public CachedPSG setPercussion(boolean percussion) {
        this.percussion = percussion;
        return this;
    }
    /**
     * Set the master volume.
     * @param loudness The master volume from 0 to 1.
     * @return The PSG instance itself.
     * */
    public CachedPSG setLoudness(double loudness) {
        this.loudness = Math.abs(loudness);
        return this;
    }
    /**
     * Get the master volume.
     * @return The master volume from 0 to 1.
     * */
    public double getLoudness() {
        return loudness;
    }
    /**
     * Get the state of the PSG.
     * @return If the PSG is still running, return true, false otherwise.
     * */
    public boolean isRunning() {
        return running;
    }
    /**
     * Determine if the PSG is in percussion mode.
     * @return If the PSG is in percussion mode, return true, false otherwise.
     * */
    public boolean isPercussion() {
        return percussion;
    }
    /**
     * Set the playback speed of the PSG.
     * @param playbackSpeed The new playback speed.
     * @return The PSG instance itself.
     * */
    public CachedPSG setPlaybackSpeed(double playbackSpeed) {
        if (playbackSpeed != 0)
            this.playbackSpeed = playbackSpeed;
        return this;
    }
    /**
     * Get the playback speed of this PSG.
     * @return The playback speed.
     * */
    public double getPlaybackSpeed() {
        return playbackSpeed;
    }
    /**
     * Get a waveform at the desired index.
     * @param index The index.
     * @return The waveform at that index.
     * */
    public PSG.Waveform getWaveform(int index) {
        return waveforms[index];
    }
    /**
     * Get the name of this PSG.
     * @return The name.
     * */
    public String getName()
    {
    	return name;
    }
    /**
     * Get the size of this PSG instance's command list.
     * @return The length of the command list.
     * */
    public int getLength()
    {
    	return commands.size();
    }
    /**
     * Set a waveform at the desired index.
     * @param index The index.
     * @param waveform The waveform at that index.
     * @return The PSG instance itself.
     * */
    public CachedPSG setWaveform(int index, PSG.Waveform waveform) {
        if (index >= waveforms.length) waveforms = Arrays.copyOf(waveforms, index + 1);
        waveforms[index] = waveform;
        return this;
    }
    /**
     * Generate a sample representing a tone with the given waveform, frequency, amplitude, and duration and whether to attenuate the tone over time and add vibrato.
     * @param wf The waveform to use.
     * @param freq The frequency of the tone in Hz.
     * @param amp The amplitude from 0 to 1.
     * @param ms The duration in milliseconds.
     * @param attenuate Attenuate the tone over time.
     * @param vibrato Add vibrato.
     * @return A sample (as a byte[]) representing the tone.
     * */
    private byte[] genTone(PSG.Waveform wf, double freq, double amp, double ms, boolean attenuate, boolean vibrato) {
        amp = Math.min(1, Math.abs(amp * loudness)); // clamp amplitude
        ms /= playbackSpeed; // scale duration by playback speed
        byte[] sample = emptySample(ms);
        double period = SAMPLE_RATE_HZ / freq;
        double b = TWO_PI / period;
        for (int i = 0; i < sample.length; i++) {
            double x = b * i, n = i / (double) sample.length;
            if (wf instanceof PSG.DynamicWaveform) ((PSG.DynamicWaveform) wf).setN(n); // Set dynamic waveform n
            if (vibrato) x += Math.sin(n * ms * 3e-2) * 1.75; // Apply vibrato algorithm using sine wave
            double f = wf.output(x); // Get output at that x of that waveform
            if (attenuate) f /= Math.exp(x / (1.5 * ms > 1000 ? ms : 1000)); // Use exponential decay for attenuation
            sample[i] = (byte) (Byte.MAX_VALUE * Math.max(Math.min(f * amp, 1), -1)); // Store output value in sample
        }
        return sample;
    } // -0.92375 for 12.5%, -0.5 for 33.3%, -Math.sqrt(0.5) for 25%
    /**
     * Generate a sample representing white noise with the given stepdown, amplitude, and duration and whether to attenuate the white noise.
     * @param stepDown The number of times to skip generating a new random byte to make the white noise sound lower-pitched. Minimum is 1.
     * @param amp The amplitude from 0 to 1.
     * @param ms The duration in milliseconds.
     * @param attenuate Attenuate the tone over time.
     * @return A sample (as a byte[]) representing the white noise.
     * */
    private byte[] genWhiteNoise(int stepDown, double amp, double ms, boolean attenuate) {
        amp = Math.min(1, Math.abs(amp * loudness)); // clamp amplitude
        stepDown = Math.max(1, Math.abs(stepDown)); // clamp stepdown
        ms /= playbackSpeed; // scale duration by playback speed
        byte[] sample = emptySample(ms);
        double f = 0;
        for (int i = 0; i < sample.length; i++) {
            double n = i / (double) sample.length;
            if (i % stepDown == 0) f = Math.random(); // The stepdown is the number of times to skip generating a new random byte to make the white noise sound lower-pitched. Minimum is 1.
            if (attenuate) f /= Math.exp(2 * n); // Use exponential decay for attenuation
            sample[i] = (byte) (Byte.MAX_VALUE * Math.max(Math.min(f * amp, 1), -1)); // Store output value in sample
        }
        return sample;
    }
    /**
     * Generate an empty sample based on the desired duration.
     * @param ms The duration of the tone in milliseconds.
     * @return An empty sample (as a byte[]).
     * */
    private static byte[] emptySample(double ms) {
        return new byte[(int) (ms * SAMPLE_RATE_KHZ)];
    }
    /**
     * A helper class that represents commands. Has its own hashcode method.
     * @author Shreyas Raghunath
     * */
    private static class Command {
        String[] strings; // The strings used to represent the command
        private int hashCode = 0; // The stored hash code

        Command(String[] strings) {
            this.strings = strings;
            for (String s : strings) hashCode += s.hashCode();
        }
        public int hashCode() {
            return hashCode;
        }
        public boolean equals(Object o) {
            return this == o || (o != null && getClass() == o.getClass() && Arrays.equals(strings, ((Command) o).strings));
        }
    }
}