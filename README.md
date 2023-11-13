# Sequencer project

shreyas, riley


# javadoc
javadoc documents have been generated and added to /doc

`doc/audio/`
directory with javadoc documents

# src
contains our source code and directories

`src/audio/sounds`
we add sound files to this directory and they are detected when MainFrame initializes

`src/audio/tracks`
we save, load tracks with this directory

`src/audio/CachedPSG.java`
implements PSG interface defined in `PSG.java`
we create objects of CachedPSG and play them as audio

`src/audio/MainFrame.java`
has our `main` function, displays interactive sequencer window
extends JFrame, implements ActionListener

`src/audio/SequencerPanel.java`
separate file for the SequencerPanel to provide visual representatoin of our sequences
extends JFrame, implements ActionListener

`src/audio/PSG.java`
provide interface for dealing with sound files