package audio;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.util.List;

/**
 * @class SequencerPanel
 * 
 * SequencerPanel provides visual view of `music` using an array of `sequencePanels`
 * each panel corresponds to one List<PSG> in music, and are filled with `BlockPanel` to represent the individual sounds
 * implemented as `trackPanel` in `MainFrame`
 * 
 * also implements ActionListeners to define behavior for JButtons
 */
@SuppressWarnings("serial")
public class SequencerPanel extends JPanel implements ActionListener
{
    // `buttonPanel` consists of buttons, left-aligned to the panel
    JPanel buttonPanel;
    // array of panels, one for each `sequence` in `music`
    JPanel[] sequencePanels;
    // overarching sequencerPanel scrollbar
    JScrollPane scrollPane;
    // `blocks` is maintained in parallel to `music`
    static List<List<BlockPanel>> blocks;
    // `panelSelected` defines which panel is highlighted, to add sounds to
    static int panelSelected;
    
    /**
     * @function constructor
     * 
     * set panel properties
     */
    public SequencerPanel ()
    {
    	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    	setBackground(new Color(245, 235, 220));
    	setPreferredSize(new Dimension(4000, 500));
    	
    	// initialize scrollbar
        int v = ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER;
		int h = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS;
		scrollPane = new JScrollPane(this, v, h);
		scrollPane.setPreferredSize(new Dimension(680, 720));
		
		// initialize panel array, one for each sequence
    	sequencePanels = new JPanel[5];
    	
    	for ( int i=0; i<sequencePanels.length; ++i )
    	{
    		// initialize `buttonPanel` to display sequence buttons
    		buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    		buttonPanel.setBackground(new Color(245, 235, 220));
    		// initialize `button` (one for each sequence)
    		// the `ActionCommand` is "Sequence `i`"
    		JButton button = new JButton("Sequence " + i);
    		button.addActionListener(this);
    		buttonPanel.add(button);
    		add(buttonPanel);
    		
    		// sequencePanels are initialized with FlowLayout to accommodate BlockPanel objects with variable sizes
    		// params set margins to 0 and left-align blocks
    		sequencePanels[i] = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    		sequencePanels[i].setPreferredSize(new Dimension(1000, 100));
    		sequencePanels[i].setBackground(new Color(220, 200, 200));
    		add(sequencePanels[i]);
    	}
    	// initially, we highlight the first sequence
    	sequencePanels[0].setBackground(new Color(220, 200, 240));
    	panelSelected = 0;
    	
    	// initialize `blocks` List of List of BlockPanel
		blocks = new ArrayList<>();
		for (int i=0; i<5; ++i)
		{
			blocks.add(new ArrayList<>());
		}
    }
    
    /**
     * @function addBlock
     * @param text, text within the block
     * @param length, length of block, dependent on temporal length of the sound
     * @param sequence, which sequence to add to (typically == `panelSelected`)
     * @param index, sequential index of the sound to be added
     * 
     * called by sound buttons located in `MainFrame`
     * adds block to `sequence`
     */
    public void addBlock (String text, int length, int sequence, int index)
    {
    	System.out.println("added " + text + " block to sequence " + sequence);
    	
    	// initialize `tempBlock` with given parameters
    	// BlockPanel is defined below
    	BlockPanel tempBlock = new BlockPanel(sequence, index, text, length);
    	
    	// keep `blocks` up to date with current state of `music`
    	blocks.get(sequence).add(tempBlock);
    	sequencePanels[sequence].add(tempBlock);
    	
    	// redraw panel to display changes
    	revalidate();
    	repaint();
    }
    
    /**
     * @function actionPerformed
     * @param e
     * 
     * implemented from ActionListener interface
     * called by button clicks, highlights selected sequence
     */
    @Override
    public void actionPerformed (ActionEvent e)
    {
    	// button ActionCommand is `Sequence 'i'`
    	// we extract `i` as int
        String actionCommand = e.getActionCommand();
        panelSelected = Character.getNumericValue(actionCommand.charAt(9));

        // selected sequence is highlighted in blue
        // other sequences are red
        for ( int i=0; i<5; ++i )
        {
    		sequencePanels[i].setBackground(new Color(220, 200, 200));
        }
        sequencePanels[panelSelected].setBackground(new Color(220, 200, 240));
    }
}

/**
 * @class BlockPanel
 * 
 * fill each sequence panel with block objects
 * block keeps track of which sequence its in, as well as its `index` within the sequence
 * each block contains label of `text`, the sound name
 * block size corresponds to sound length
 * block listens to mouse input, deletes itself from `music`, `blocks`, and hides from display when clicked
 */
@SuppressWarnings("serial")
class BlockPanel extends JPanel implements MouseListener
{
	int sequence, index;
	JLabel label;
	String text;

    /**
     * @function constructor
     * @param sequence, which sequence in `music` the sound resides
     * @param index, sound's sequential index within `sequence`
     * @param text, displayed within label inside block
     * @param length, temporal length of sound, corresponds to block width
     * 
     * construct the block with label containing `text`
     * block has right border to differentiate between them
     */
    BlockPanel (int sequence, int index, String text, int length)
    {
    	this.sequence = sequence;
    	this.index = index;
    	setPreferredSize(new Dimension(length,100));
    	setBackground(new Color(240, 240, 240));
    	setBorder(BorderFactory.createMatteBorder(0, 0, 0, 2, new Color(50, 50, 80)));
    	addMouseListener(this);
    	
    	this.text = text;
    	label = new JLabel(text);
    	add(label);
    }
    
    /**
     * @function mouseClicked
     * @param e
     * 
     * block visually hides itself on mouseclick
     * also is removed from `blocks` and `music` lists
     */
    @Override
    public void mouseClicked (MouseEvent e) {
    	System.out.printf("deleted %s block from sequence %d, index %d\n", text, sequence, index);
    	
    	// 0x0 visually hides this panel
    	setPreferredSize(new Dimension(0, 0));
    	
    	// call MainFrame function `removeSound()` 
    	// which deletes the corresponding sound from `music` (based on sequence, index)
    	MainFrame.removeSound(sequence, index);
    	SequencerPanel.blocks.get(sequence).remove(index);
    	
    	// keep `blocks` up to date with `music`
    	// update indices within the sequence s.t. we avoid OutOfBounds errors
    	int nums = SequencerPanel.blocks.get(sequence).size();
    	for (int i=0; i<nums; ++i)
    	{
    		SequencerPanel.blocks.get(sequence).get(i).index = i;
    	}
    	
    	// display changes
    	revalidate();
    	repaint();
    }

    // functions from MouseListener interface
    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
}
