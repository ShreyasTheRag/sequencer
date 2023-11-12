package audio;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

/**
 * @class SequencerPanel
 * 
 * define `trackPanel` behavior
 */
@SuppressWarnings("serial")
public class SequencerPanel extends JPanel implements ActionListener
{
    // button array
    JButton[] buttons;
    // `buttonPanel` left-aligns buttons
    JPanel buttonPanel;
    // array, one for each sequence
    JPanel[] sequencePanels;
    // panel scrollbar
    JScrollPane scrollPane;
    // defines which panel is highlighted
    // sound blocks are added to sequencePanels[panelSelected]
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
    		// initialize `buttonPanel` to house sequence buttons
    		buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    		buttonPanel.setBackground(new Color(245, 235, 220));
    		// initialize `button` (one for each sequence)
    		JButton button = new JButton("Sequence " + i);
    		button.addActionListener(this);
    		buttonPanel.add(button);
    		add(buttonPanel);
    		
    		// FlowLayout params set margins to 0 and left-align components
    		sequencePanels[i] = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    		sequencePanels[i].setPreferredSize(new Dimension(1000, 100));
    		sequencePanels[i].setBackground(new Color(220, 200, 200));
    		add(sequencePanels[i]);
    	}
    	// initially, we highlight the first sequence
    	sequencePanels[0].setBackground(new Color(220, 200, 240));
    	panelSelected = 0;
    }
    
    /**
     * @function addBlock
     * @param text
     * 
     * adds block to current highlighted sequence
     * called by sound buttons located in `MainFrame`
     * 
     * `text` added to JLabel within block
     * `length` block horizontal length
     * `index` index within music.get(panelSelected)
     */
    public void addBlock (String text, int length, int index)
    {
    	System.out.println("added " + text + " block to sequence " + panelSelected);
    	sequencePanels[panelSelected].add(new BlockPanel(panelSelected, index, text, length));
    	
    	// redraw panel to display changes
    	revalidate();
    	repaint();
    }
    
    /**
     * @function actionPerformed
     * @param e
     * 
     * implemented from ActionListener interface
     * listens to button clicks
     */
    @Override
    public void actionPerformed (ActionEvent e)
    {
    	// `Sequence i` - extract `i` as int
        String actionCommand = e.getActionCommand();
        panelSelected = Character.getNumericValue(actionCommand.charAt(9));

        // set sequence background color appropriately
        // selected sequence is highlighted in blue
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
 * block keeps track of its sequence
 * each block has label containing `text`, the sound name
 * blocks listen to mouse input
 * block size corresponds to sound length
 */
@SuppressWarnings("serial")
class BlockPanel extends JPanel implements MouseListener
{
	int panel, index;
	JLabel label;
	String text;

    /**
     * @function constructor
     * @param text
     * 
     * 
     * construct the block with label containing `text`
     */
    BlockPanel (int panel, int index, String text, int length)
    {
    	this.panel = panel;
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
     * define behavior of block on mouseclick
     */
    @Override
    public void mouseClicked (MouseEvent e) {
    	System.out.printf("deleted %s block from sequence %d, index %d\n", text, panel, index);
    	
    	// 0x0 visually `deletes` this panel
    	setPreferredSize(new Dimension(0, 0));
    	// delete sound from `music` (based on this block's saved sequence, index)
    	MainFrame.music.get(panel).remove(index);
    	
    	revalidate();
    	repaint();
    }

    // implement functions from MouseListener interface
    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
}
