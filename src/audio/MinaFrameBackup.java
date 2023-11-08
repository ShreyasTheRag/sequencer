package audio;

// `MainFrame` our sequencer's window
import java.awt.*;
import java.awt.event.*;
import java.io.InputStream;
import java.io.File;		// looking through directories
import javax.swing.*;
import java.util.*;
import java.util.List;

//class extends JFrame, making a window
// implements `ActionListener` interface, so we implement `actionPerformed()`
// we are not serializing right now, suppress warnings
@SuppressWarnings("serial")
public class MinaFrameBackup extends JFrame implements ActionListener
{
	// screen elements
	JPanel startPanel, searchPanel, searchInnerPanel, contentPanel;
	JButton startButton, playButton, tempButton;
	JLabel infoLabel;
	JScrollPane searchScrollPane;
	
	// hold sounds in `music` List
	List<PSG> music;
	final double PB = 1.0;
	
	// sound directory
	File folder;
	
	// constructor
	public MinaFrameBackup()
	{
		// create window frame by calling the super constructor
		// @argument frame title
		super("the first frame");
		
		// initialize `music` as ArrayList<>();
		music = new ArrayList<>();
		
		// choose `BorderLayout` for flexibility in placing elements
		setLayout(new BorderLayout());
		
		// customize `contentPane` properties
		setBackground(new Color(245, 235, 220));
		
		// initialize `startPanel`
		startPanel = new JPanel();
		startPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 260));
		startPanel.setBackground(new Color(220,220,220));
		startPanel.setBorder(BorderFactory.createMatteBorder(10, 10, 10, 10, new Color(152, 163, 130)));
		
		// initialize `startButton`
		startButton = new JButton("Sequencer Project");
		
		// customize `startButton` properties
		startButton.setPreferredSize(new Dimension(200, 75));
		// create listener for `startButton` clicks
		startButton.addActionListener(this);
		
		// add `startButton` to `startPanel`
		startPanel.add(startButton, BorderLayout.CENTER);
		
		// add `startPanel` to our frame
		add(startPanel, BorderLayout.CENTER);
		
		// set window dimensions
		setSize(1280,720);
		// center the window to the display
		setLocationRelativeTo(null);
		// tell the window to exit on close
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// prevent frame resizing
		setResizable(false);
		// fit window frame to element size
//		pack();
		// make window visible to the user
		setVisible(true);
	}
	
	
	// recursively list sound files
	// @param `folder` we iterate through the files/subfolders
	// create buttons and labels as needed
	public void listFilesForFolder(File folder)
	{
		// iterate through each file in the folder
		for(File fileEntry : folder.listFiles())
		{
			// if `fileEntry` is a subfolder, recursively call this function
			if (fileEntry.isDirectory())
			{
				// create a label within our search panel
				searchInnerPanel.add(new JLabel(fileEntry.getName()));
				listFilesForFolder(fileEntry);
			}
			// otherwise, `fileEntry` is a file
			// create a button whose text is the file name
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
				
				// initialize `tempButton`, assign its action listener, and add to `searchPanel`
				tempButton = new JButton(subfolder);
				tempButton.addActionListener(this);
				searchInnerPanel.add(tempButton);
			}
		}
	}
	
	// build `contentPanel` after `startButton` is clicked
	// called by `actionPerformed()` function below
	public void buildContentPanel()
	{
		// remove `startPanel` from window
		remove(startPanel);
		
		// initialize outer `searchPanel`, `contentPanel`
		searchPanel = new JPanel();
		searchInnerPanel = new JPanel();
		contentPanel = new JPanel();
		
		// customize `searchPanel`
		// we use `BoxLayout` to have elements fill from top to bottom
//		searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.Y_AXIS));
		searchPanel.setLayout(new FlowLayout());
		searchInnerPanel.setLayout(new BoxLayout(searchInnerPanel, BoxLayout.Y_AXIS));
		searchPanel.setBackground(new Color(180,180,210));
		searchInnerPanel.setPreferredSize(new Dimension(200, 1200));
		searchInnerPanel.setBackground(new Color(180,180,210));
		
		// add a scroll pane to `searchPanel` for vertical scrolling
		int v = ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;
		int h = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
		searchScrollPane = new JScrollPane(searchInnerPanel, v, h);
		searchScrollPane.setPreferredSize(new Dimension(200, 720));
		
		// add the scroll pane, which has the inner panel inside
		searchPanel.add(searchScrollPane);
		
		// grab sound directory
		folder = new File("./src/audio/sounds");
		// create buttons, labels for all sounds in the directory
		listFilesForFolder(folder);
		
		// initialize `playButton`, `searchBar`
		playButton = new JButton("play your sound");
		infoLabel = new JLabel("type in the audio you want to hear, ex: dreamer/harmony1");
		
		// add action listener to `playButton`
		playButton.addActionListener(this);
		
		// add elements to `contentPanel`
		contentPanel.add(playButton);
		contentPanel.add(infoLabel);
		
		// customize `contentPanel`
		contentPanel.setPreferredSize(new Dimension(400, 720));
		
		// add `searchScrollPane` and `contentPanel` to the window
//		add(searchPanel, BorderLayout.WEST);
		add(searchPanel, BorderLayout.WEST);
		add(contentPanel, BorderLayout.CENTER);
		
		// create the `trackPanel`
		// SequencerPanel defined in `SequencerPanel.java`
		SequencerPanel trackPanel = new SequencerPanel();
		// add `trackPanel` 's `scrollPane` to the window so we can scroll
		add(trackPanel, BorderLayout.EAST);
		
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
	
	// from the listener interface
	// define JButton behavior on click
	public void actionPerformed (ActionEvent e)
	{
		// user pressed `startButton`
		// we want to remove `startPanel` and display `contentPanel`
		if (e.getSource() == startButton)
		{
			System.out.printf("startButton clicked %s\n", e);
			buildContentPanel();
		}
		// user pressed `playButton`, wants to hear track
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
		// one of the sound buttons in `searchPanel` is clicked
		else
		{
			// extract the sound's name from the event's `toString()`
			String actionCommand = e.getActionCommand();
			System.out.println("user pressed sound button " + actionCommand);
			
			
			// add to `music` list
			music.add(new CachedPSG(istream(actionCommand)));
			
			// add a Block
		}
	}
	
	// main function executes our program
	public static void main(String[] args)
	{
		// create a new instance of the `project0001` class
		// which displays our window
		MinaFrameBackup window = new MinaFrameBackup();
	}
}
