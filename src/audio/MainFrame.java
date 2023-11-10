package audio;

// `MainFrame` our sequencer's window
import java.awt.*;
import java.awt.event.*;
import java.io.InputStream;
import java.io.File;		// looking through directories
import javax.swing.*;
import java.util.*;
import java.util.List;

// class extends JFrame, making a window
// implements `ActionListener` interface, so we implement `actionPerformed()`
// we are not serializing right now, suppress warnings
@SuppressWarnings("serial")
public class MainFrame extends JFrame implements ActionListener
{
	// screen elements
	JPanel startPanel, searchPanel, contentPanel;
	JButton startButton, playButton, tempButton;
	JLabel infoLabel;
	JScrollPane searchScrollPane;
	SequencerPanel trackPanel;
	
	// hold sounds in `music` List
	List<PSG> music;
	final double PB = 1.0;
	
	// sound directory
	File folder;
	
	// constructor
	public MainFrame ()
	{
		// window uses BorderLayout for ease of placing components
		// window dimensions 1280x720, centered to the display
		// exits on close, and user cannot resize window
		setTitle("Sequencer Project");
		setLayout(new BorderLayout());
		setBackground(new Color(245, 235, 220));
		setSize(1280,720);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		
		
		// initialize `music` as ArrayList<>();
		music = new ArrayList<>();

		// initialize `startPanel`
		// use FlowLayout.CENTER to center each row in the window
		startPanel = new JPanel();
		startPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 260));
		startPanel.setBackground(new Color(220,220,220));
		startPanel.setBorder(BorderFactory.createMatteBorder(10, 10, 10, 10, new Color(152, 163, 130)));
		
		// `startButton` has ActionListener
		startButton = new JButton("Sequencer Project");
		startButton.setPreferredSize(new Dimension(200, 75));
		startButton.addActionListener(this);
		
		// add `startButton` to `startPanel`
		startPanel.add(startButton);
		// add `startPanel` to window
		add(startPanel);
		
		// make window visible (with our above changes)
		setVisible(true);
	}
	
	/**
	 * @function listFilesForFolder()
	 * @param folder we iterate through the files/subfolders
	 * 
	 * called by `buildContentPanel()` below
	 * recursively list sound files in the (sub)directory
	 * create buttons and labels as needed
	 */
	public void listFilesForFolder (File folder)
	{
		// iterate through each file in `folder`
		for(File fileEntry : folder.listFiles())
		{
			// `fileEntry` is a subfolder
			if (fileEntry.isDirectory())
			{
				// create label in `searchPanel`
				// recursively call this function on the subfolder
				searchPanel.add(new JLabel(fileEntry.getName()));
				listFilesForFolder(fileEntry);
			}
			// `fileEntry` is a file
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
				tempButton.addActionListener(this);
				searchPanel.add(tempButton);
			}
		}
	}
	
	/**
	 * @function buildContentPanel()
	 * 
	 * build `contentPanel` after `startButton` is clicked
	 * called by `actionPerformed()` below
	 */
	public void buildContentPanel ()
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
		folder = new File("./src/audio/sounds");
		listFilesForFolder(folder);
		
		
		// initialize `contentPanel`
		contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
//		contentPanel.setPreferredSize(new Dimension(400, 720));
		
		// initialize `playButton` into `contentPanel`
		playButton = new JButton("play your sound");
		playButton.addActionListener(this);
		contentPanel.add(playButton);
		
		infoLabel = new JLabel("selected sounds are added to the highlighted sequence");
		contentPanel.add(infoLabel);
		
		// add `searchScrollPane` and `contentPanel` to the window
		add(searchScrollPane, BorderLayout.WEST);
		add(contentPanel, BorderLayout.CENTER);
		
		// initialize `trackPanel`
		// SequencerPanel defined in `SequencerPanel.java`
		trackPanel = new SequencerPanel();
		// add `trackPanel` 's `scrollPane` to the window so we can scroll
		add(trackPanel.scrollPane, BorderLayout.EAST);
		
		// display changes to the window
		revalidate();
		repaint();
	}
	
	// look through `sounds` directory
	private static InputStream istream(String file)
	{
		return PSGTester.class.getResourceAsStream("/audio/sounds/" + file + ".txt");
	}

	// play each sound in `music` List
	public void playSounds()
	{
		// cant we put this in the other for loop
        for (PSG psg : music) psg.setPlaybackSpeed(PB * psg.getPlaybackSpeed());
        
        // iterate through each sound in `music`, play
        for (byte i = 0, len = (byte) music.size(); i < len; i++) {
            for (PSG psg : music) psg.start();
            while (music.get(0).isRunning())
            {
            	// try-catch block for `sleep()` command
				try {
					Thread.sleep(1);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
            }
        }
        // tell the sound to stop playing
        for (PSG c : music) c.stop();		
	}
	
	// from `ActionListener` interface
	// define JButton behavior on click
	public void actionPerformed (ActionEvent e)
	{
		// user pressed `startButton` -> display `contentPanel`
		if (e.getSource() == startButton)
		{
			System.out.printf("startButton clicked %s\n", e);
			buildContentPanel();
		}
		// user pressed `playButton` -> play track
		else if (e.getSource() == playButton)
		{
			// get the search query from the text bar
//			String query = searchTextField.getText();

			// add sounds to `music` ArrayList
//			music.add(new CachedPSG(istream(query), Math::sin, (PSG.Waveform.SQUARE), PSG.Waveform.TRIANGLE, (PSG.Waveform.SAWTOOTH)));
//			music.add(new CachedPSG(istream(query)));
			
			// play all the Sounds
	        playSounds();
		}
		// user pressed sound button in `searchPanel` -> add to active sequence
		else
		{
			// TODO add functionality
			// extract button's `text` from event `e`
			String actionCommand = e.getActionCommand();
			System.out.println("user pressed sound button " + actionCommand);
			
			// TODO make it add to the sequence
			// add sound to `music` list
			music.add(new CachedPSG(istream(actionCommand)));
			
			// TODO call a function in `SequencerPanel`
			trackPanel.addSequence(actionCommand);
		}
	}
	
	// main function executes our program
	public static void main(String[] args)
	{
		// create an instance of `MainFrame` class -> displays window
		MainFrame window = new MainFrame();
	}
}
