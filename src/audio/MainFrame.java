package audio;

import java.awt.*;
import java.awt.event.*;
import java.io.InputStream;
import java.io.*;		// read/write files, looking through directories
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import javax.swing.*;
import java.util.*;
import java.util.List;
import java.util.Scanner; // read files (loading tracks)
import java.util.concurrent.*; // Executor, ExecutorService

/**
 * @class MainFrame
 * 
 * extends JFrame, display interactive window
 * implements ActionListener interface for button functionality
 */
@SuppressWarnings("serial")
public class MainFrame extends JFrame implements ActionListener
{
	// declare window elements
	JPanel startPanel, searchPanel, contentPanel;
	SequencerPanel trackPanel;
	JScrollPane searchScrollPane;
	JButton startButton, playButton, tempButton, saveButton, loadButton;
	JTextField saveField, loadField;
	JLabel infoLabel, saveLabel, loadLabel;
	
	// hold sounds in `music`, a List of Lists of PSG
	// sounds play at `PB` 1.0 speed
	static List<List<PSG>> music;
	final double PB = 1.0;
	
	// sound, track directory
	File folder;
	String trackDir;
	
	// use ExecutorService to play sequences concurrently
	ExecutorService executor;
	
	/**
	 * @function constructor
	 * 
	 * initialize window, display `startPanel`
	 * window uses BorderLayout for ease of placing components
	 * dimensions 1280x720, centered to display, user cannot resize window
	 * program exits on close
	 */
	public MainFrame ()
	{
		setTitle("Sequencer Project");
		setLayout(new BorderLayout());
		setSize(1280,720);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		
		// initialize `music` List of List of PSG
		music = new ArrayList<>();
		for (int i=0; i<5; ++i)
		{
			music.add(new ArrayList<>());
		}
		
		// initialize `folder` sound directory
		folder = new File("./src/audio/sounds");
		
		// initialize `trackDir` for saving, loading tracks
		trackDir = "./src/audio/tracks/";

		// initialize `startPanel`
		// use FlowLayout.CENTER to center `startButton`
		startPanel = new JPanel();
		startPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 260));
		startPanel.setBackground(new Color(220,220,220));
		startPanel.setBorder(BorderFactory.createMatteBorder(10, 10, 10, 10, new Color(152, 163, 130)));
		
		// initialize `startButton`
		startButton = new JButton("Sequencer Project");
		startButton.setActionCommand("startButton");
		startButton.setPreferredSize(new Dimension(200, 75));
		startButton.addActionListener(this);
		
		// add `startButton` to `startPanel`
		startPanel.add(startButton);
		// add `startPanel` to window
		add(startPanel);
		
		// make window visible (with above changes)
		setVisible(true);
	}
	
	/**
	 * @function listFilesForFolder()
	 * @param folder we iterate through the files/subfolders
	 * 
	 * called by `showContent()`
	 * recursively list sound files in the (sub)directory
	 * create buttons and labels as needed
	 */
	public void listFilesForFolder (File folder)
	{
		// iterate through each file, subfolder in `folder`
		for(File fileEntry : folder.listFiles())
		{
			if (fileEntry.isDirectory())
			{
				// place subfolder label in `searchPanel`
				// recursively call this function on the subfolder
				searchPanel.add(new JLabel(fileEntry.getName()));
				listFilesForFolder(fileEntry);
			}
			else
			{
				// convert filepath from `folder` to String
				// ex: .\src\audio\sounds\dreamer
				String subfolder = folder.toString();
				// remove prefix
				// ex: dreamer
				subfolder = subfolder.replace(".\\src\\audio\\sounds\\", "");
				// concatenate `fileEntry` file name
				// ex: dreamer/bass.txt
				subfolder = subfolder + "/" + fileEntry.getName();
				// remove `.txt`
				// ex: dreamer/bass
				subfolder = subfolder.substring(0, subfolder.lastIndexOf("."));
				
				// initialize `tempButton`, assign ActionListener, add to `searchPanel`
				tempButton = new JButton(subfolder);
				tempButton.setActionCommand(subfolder);
				tempButton.addActionListener(this);
				searchPanel.add(tempButton);
			}
		}
	}
	
	/**
	 * @function showContent()
	 * 
	 * display `searchPanel`, `contentPanel`, `trackPanel`
	 * called by `startButton` -> `actionPerformed()`
	 */
	public void showContent ()
	{
		remove(startPanel);
		
		// initialize `searchPanel` (leftmost)
		// `searchPanel` uses `BoxLayout` to add components top to bottom
		searchPanel = new JPanel();
		searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.Y_AXIS));
		searchPanel.setBackground(new Color(180,180,210));
		
		// initialize `searchScrollPane` for vertical scrolling
		// `searchScrollPane` has large height, which is cut off by MainFrame, resulting in scrollbar
		int v = ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;
		int h = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;
		searchScrollPane = new JScrollPane(searchPanel, v, h);
		searchScrollPane.setPreferredSize(new Dimension(200, 2000));
		
		// add files, folders from sound directory to `searchPanel`
		// create buttons, labels for all sounds in the directory
		listFilesForFolder(folder);


		// initialize `contentPanel` (center)
		contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		
		// initialize `playButton`
		playButton = new JButton("play track");
		playButton.setActionCommand("playButton");
		playButton.addActionListener(this);
		
		// initialize `infoLabel`
		infoLabel = new JLabel("selected sounds are added to the highlighted sequence");
		
		// initialize `saveButton`
		saveButton = new JButton("save track");
		saveButton.setActionCommand("saveButton");
		saveButton.addActionListener(this);
		
		// initialize `saveField`
		saveField = new JTextField();
		saveField.setMaximumSize(new Dimension(400, 25));
		
		// initialize `saveLabel`
		saveLabel = new JLabel("");
		
		// initialize `loadButton`
		loadButton = new JButton("load track");
		loadButton.setActionCommand("loadButton");
		loadButton.addActionListener(this);
		
		// initialize `loadField`
		loadField = new JTextField();
		loadField.setMaximumSize(new Dimension(400, 25));
		
		// initialize `loadLabel`
		loadLabel = new JLabel("");
		
		// add components to `contentPanel` with proper spacing
		contentPanel.add(playButton);
		contentPanel.add(infoLabel);
		contentPanel.add(Box.createVerticalStrut(200));
		contentPanel.add(saveButton);
		contentPanel.add(saveField);
		contentPanel.add(saveLabel);
		contentPanel.add(Box.createVerticalStrut(100));
		contentPanel.add(loadButton);
		contentPanel.add(loadField);
		contentPanel.add(loadLabel);
		
		// add `searchScrollPane` and `contentPanel` to the window
		add(searchScrollPane, BorderLayout.WEST);
		add(contentPanel, BorderLayout.CENTER);
		
		// initialize `trackPanel`
		// SequencerPanel defined in `SequencerPanel.java`
		trackPanel = new SequencerPanel();
		
		// add `trackPanel` by its `scrollPane` so we can scroll
		add(trackPanel.scrollPane, BorderLayout.EAST);
		
		// display window changes
		revalidate();
		repaint();
	}
	
	/**
	 * @function istream()
	 * @param file, sound
	 * @return InputStream, of the sound we want to play
	 */
	private static InputStream istream(String file)
	{
		return PSGTester.class.getResourceAsStream("/audio/sounds/" + file + ".txt");
	}

	/**
	 * @function playSounds()
	 * 
	 * play each sound in `music.get(index)`
	 */
	public void playSounds(int index)
	{
        for (PSG psg : music.get(index))
        {
        	psg.setPlaybackSpeed(PB * psg.getPlaybackSpeed());
        	psg.start();
        	System.out.println("playing " + psg.getName() + " from track " + index);
        	
        	// sleep while sound plays
        	while (psg.isRunning())
            {
            	// try-catch block for `sleep()` command
				try
				{
					Thread.sleep(1);
				}
				catch (InterruptedException e1)
				{
					e1.printStackTrace();
				}
            }
        	// stop the sound
        	psg.stop();
        }
	}
		
	/**
	 * @function actionPerformed()
	 * @param e, event
	 * 
	 * implemented from ActionListener interface
	 * define JButton behavior on click
	 */
	public void actionPerformed (ActionEvent e)
	{
		String source = e.getActionCommand();
		String fileName;
		
		// switch statement O(1), performant
		switch (source)
		{
			// `startButton` removes `startPanel` and displays our main content
			case "startButton":
				System.out.printf("user pressed startButton\n");
				showContent();
				break;
				
			// `playButton` plays all sequences in parallel, and each sound in the sequence sequentially
			case "playButton":
				System.out.printf("user pressed playButton\n\n");
				
				// use ExecutorService to play sequences concurrently
				executor = Executors.newFixedThreadPool(5);
				for (int i=0; i<5; ++i)
				{
					// `j` cannot be changed
					final int j = i;
					executor.submit(() -> {
						playSounds(j);
					});
				}
				// shutdown service after use
				executor.shutdown();
				break;
			
			// `saveButton` gives us the functionality of saving our tracks
			case "saveButton":
				// get `fileName` from `saveField`
				fileName = saveField.getText();
				// file must have name
				if(fileName.equals(""))
				{
					saveLabel.setText("please give your track a name!");
					saveLabel.setForeground(Color.red);
					break;
				}
				
				String fileToWrite = trackDir + fileName;
				
				// write to file
				// try-with-resources block automatically closes `writer`
				try (FileWriter writer = new FileWriter(fileToWrite))
				{
					// write all track names to file
					for (int i=0; i<5; ++i)
					{
						int seqSize = music.get(i).size();
						for (int j=0; j<seqSize; ++j)
						{
							writer.write(music.get(i).get(j).getName());
							
							// if this is not the last sound, we separate by comma
							if (j != seqSize-1)
							{
								writer.write(",");
							}
						}
						
						// if this is not the last sequence, separate by new line
						if (i != 4)
						{
							writer.write(System.lineSeparator());
						}
					}
				}
				catch (IOException e2)
				{
					e2.printStackTrace();
				}
				
				// inform user of successful save
				saveLabel.setText("file saved as " + fileName);
				saveLabel.setForeground(new Color(42, 172, 42));
				break;

			case "loadButton":
				// get `fileName` from `loadField`
				fileName = loadField.getText();
				// file must have name
				if (fileName.equals(""))
				{
					loadLabel.setText("please type the track's name!");
					loadLabel.setForeground(Color.red);
					break;
				}
				
				System.out.println("loading " + fileName + " ...");
				String fileToRead = trackDir + fileName;
				
				// read from file
				// try-with-resources block automatically closes `scanner`
				try (Scanner scanner = new Scanner(new FileReader(fileToRead)))
				{
					// first, clear out all sounds currently in `music`, `blocks`
					clearSounds();
					
					// read the file, adding to `music` and creating `blocks` as needed
					int seqIndex = 0;
					while (scanner.hasNextLine())
					{
						String line = scanner.nextLine();
						if (!line.equals(""))
						{
							String[] soundSources = line.split(",");
							
							for (String soundSource : soundSources)
							{
								System.out.println(soundSource);
								addSound(soundSource, seqIndex);
							}
						}
						++seqIndex;
					}
				}
				catch (IOException e2)
				{
					// most likely FileNotFoundException, inform user
					loadLabel.setText("file not found! please type the track's name!");
					loadLabel.setForeground(Color.red);
					break;
				}
				
				// notify the user of successful load
				loadLabel.setText("loaded " + fileName);
				loadLabel.setForeground(new Color(42, 172, 42));
				break;
				
			default:
				// user pressed sound button
				// add to `music`, `sequence`, and create block
				// `sequence` define which sequence to add sound to
				addSound(source);
				
		}
	}
	
	/**
	 * @function addSound
	 * @param source, sound to be added
	 * 
	 * this signature is called by sound buttons
	 * gets `seqIndex` from the currently highlighted panel
	 * calls more specific signature below
	 * 
	 */
	public void addSound(String source)
	{
		// `seqIndex` define which sequence to add sound to
		int seqIndex = SequencerPanel.panelSelected;
		// call below signature
		addSound(source, seqIndex);
	}
	
	/**
	 * @function addSound
	 * @param source, sound to be added
	 * @param seqIndex, sequence to add to
	 * 
	 * this signature is directly called by `loadButton`,
	 * also called by sound buttons as shown above
	 * 
	 * add `source` to `music`, create block within `seqIndex`
	 */
	public void addSound(String source, int seqIndex)
	{
		CachedPSG sound = new CachedPSG(istream(source), source);
		int soundLength = sound.getLength();
		
		// `index` to sequentially add `source` to music.get(seqIndex)
		int index = music.get(seqIndex).size();
		
		// add to tail end of the sequence
		music.get(seqIndex).add(sound);
		System.out.printf("added %s, length %d to music.get(%d) at index %d\n", source, soundLength, seqIndex, index);
		
		// create corresponding block in sequence panel
		trackPanel.addBlock(source, soundLength, seqIndex, index);
	}
	
	/**
	 * @function removeSound()
	 * @param panel
	 * @param index, sound location in `music`
	 * 
	 * called by individual BlockPanel when they are clicked
	 * 
	 * remove sound from `music`
	 */
	public static void removeSound (int panel, int index)
	{
		music.get(panel).remove(index);
	}
	
	/**
	 * @function clearSounds()
	 * 
	 * called when loading track
	 * clears all sounds in sequences, removing them from `music` and `blocks`
	 * changes get validated when new sounds are added
	 */
	public void clearSounds()
	{
		for (int i=0; i<5; ++i)
		{
			int seqSize = music.get(i).size();
			for (int j=0; j<seqSize; ++j)
			{
				music.get(i).remove(0);
				SequencerPanel.blocks.get(i).get(0).setPreferredSize(new Dimension(0,0));
				SequencerPanel.blocks.get(i).remove(0);
			}
		}
	}
	
	
	
	/**
	 * @function main
	 * @param args, command line arguments, not used
	 * 
	 * main function executes our project, displaying window
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args)
	{
		// create an instance of `MainFrame` class -> displays window
		MainFrame window = new MainFrame();
	}
}
