package audio;

// `MainFrame` our sequencer's window
import java.awt.*;
import java.awt.event.*;
import java.io.InputStream;
import java.io.File;		// looking through directories
import javax.swing.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*; // Executor, ExecutorService

/**
 * @class MainFrame
 * 
 * extends JFrame, making a window
 * implements ActionListener interface
 * suppress warnings for serializing the class
 */
@SuppressWarnings("serial")
public class MainFrame extends JFrame implements ActionListener
{
	// screen elements
	JPanel startPanel, searchPanel, contentPanel;
	SequencerPanel trackPanel;
	JScrollPane searchScrollPane;
	JButton startButton, playButton, tempButton;
	JLabel infoLabel;
	
	// hold sounds in a List of Lists of PSG
	// play at `PB` 1.0 speed
	static List<List<PSG>> music;
	final double PB = 1.0;
	
	// sound directory
	File folder;
	
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

		// initialize `startPanel`
		// use FlowLayout.CENTER to center components
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
				// create label in `searchPanel`
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
				// concatenate `fileEntry` the file name
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
	 * called by clicking `startButton`, `actionPerformed()`
	 */
	public void showContent ()
	{
		remove(startPanel);
		
		// initialize `searchPanel` (leftmost)
		// `searchPanel` uses `BoxLayout` to fill elements from top to bottom
		searchPanel = new JPanel();
		searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.Y_AXIS));
		searchPanel.setBackground(new Color(180,180,210));
		
		// add a scroll pane `searchScrollPane` for vertical scrolling
		// `searchScrollPane` has dimensions
		int v = ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;
		int h = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;
		searchScrollPane = new JScrollPane(searchPanel, v, h);
		searchScrollPane.setPreferredSize(new Dimension(200, 2000));
		
		// add files, folders from sound directory to `searchPanel`
		// create buttons, labels for all sounds in the directory
		listFilesForFolder(folder);
		
		
		// initialize `contentPanel`
		contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
//		contentPanel.setPreferredSize(new Dimension(400, 720));
		
		// initialize `playButton`
		playButton = new JButton("play track");
		playButton.setActionCommand("playButton");
		playButton.addActionListener(this);
		// add playButton to contentPanel
		contentPanel.add(playButton);
		
		// initialize `infoLabel`
		infoLabel = new JLabel("selected sounds are added to the highlighted sequence");
		contentPanel.add(infoLabel);
		
		// add `searchScrollPane` and `contentPanel` to the window
		add(searchScrollPane, BorderLayout.WEST);
		add(contentPanel, BorderLayout.CENTER);
		
		// initialize `trackPanel`
		// pass `this` as `parentFrame`
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
		
	// from `ActionListener` interface
	// define JButton behavior on click
	public void actionPerformed (ActionEvent e)
	{
		String source = e.getActionCommand();
		
		// switch statement O(1), performant
		switch (source)
		{
			case "startButton":
				System.out.printf("user pressed startButton\n");
				showContent();
				break;
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
			default:
				// user pressed sound button
				// add sound to `music` list
				int sequence = SequencerPanel.panelSelected;
				
				CachedPSG sound = new CachedPSG(istream(source), source);
				int soundLength = sound.getLength();
				
				// track index that this was added
				int index = music.get(sequence).size();
				
				music.get(sequence).add(sound);
				System.out.printf("added %s, length %d to music.get(%d) at index %d\n", source, soundLength, sequence, index);
				
				// add block to appropriate sequence panel
				trackPanel.addBlock(source, soundLength, index);
				
		}
	}
	
	public int getDummy ()
	{
		return 1;
	}
	
	// main function executes our program
	public static void main(String[] args)
	{
		// create an instance of `MainFrame` class -> displays window
		MainFrame window = new MainFrame();
	}
}
