package audio;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

/**
 * @class SequencerPanel
 * 
 * define `trackPanel` behavior
 * connected to `MainFrame` class through `parentFrame`
 */
@SuppressWarnings("serial")
public class SequencerPanel extends JPanel implements ActionListener
{
	JFrame parentFrame;
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
     * 
     * @param parent: parent JFrame `mainFrame`
     */
    public SequencerPanel (JFrame parent)
    {
    	parentFrame = parent;
    	
    	// customize `SequencerPanel` properties
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
     * block has a JLabel containing `text` passed in
     * called by sound buttons located in `MainFrame`
     */
    public void addBlock (String text, int length)
    {
    	System.out.println("adding block to sequence panel " + panelSelected);
    	sequencePanels[panelSelected].add(new BlockPanel(text, length));
    	
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
    
    
    /**
     * @function getPanelSelected()
     * 
     * `panelSelected` getter function, for calling from `MainFrame`
     */
    public int getPanelSelected ()
    {
    	return panelSelected;
    }
}

/**
 * @class BlockPanel
 * 
 * fill each sequence panel with block objects
 * each `block` has label containing `text`, the sound name
 * blocks listen to mouse input
 */
@SuppressWarnings("serial")
class BlockPanel extends JPanel implements MouseListener
{
	JLabel label;
	String text;

    /**
     * @function constructor
     * @param text
     * 
     * 
     * construct the block with label containing `text`
     */
    BlockPanel (String text, int length)
    {
    	// first call the super constructor
    	super();
    	
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
    	System.out.println("hi from " + text/*e.getSource()*/);
    	delete this;
    }

    // implement functions from MouseListener interface
    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
}
