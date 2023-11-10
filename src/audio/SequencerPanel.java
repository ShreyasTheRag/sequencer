package audio;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import java.util.*;

@SuppressWarnings("serial")
public class SequencerPanel extends JPanel implements ActionListener
{
    // button array
    JButton[] buttons;
    // `buttonPanel` keeps buttons left-aligned
    JPanel buttonPanel;
    // array `sequencePanels`, one for each sequence
    JPanel[] sequencePanels;
    // panel scrollBar
    JScrollPane scrollPane;
    // which sequencePanel is highlighted
    // sound blocks go into sequencePanels[panelEmphasis]
    static int panelEmphasis;
    
    // constructor: create the panel
    public SequencerPanel()
    {
    	// customize `SequencerPanel` properties
    	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    	setBackground(new Color(245, 235, 220));
    	setPreferredSize(new Dimension(4000, 500));
    	
    	// initialize scrollbar
        int v = ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER;
		int h = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS;
		scrollPane = new JScrollPane(this, v, h);
		scrollPane.setPreferredSize(new Dimension(680, 720));
		
		// initialize array of 5 `JPanel`, one for each sequence
    	sequencePanels = new JPanel[5];
    	
    	for ( int i=0; i<sequencePanels.length; ++i )
    	{
    		// FlowLayout params set margins to 0 and left-aligns components
    		sequencePanels[i] = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    		sequencePanels[i].setPreferredSize(new Dimension(1000, 100));
    		sequencePanels[i].setBackground(new Color(220, 200, 200));
    		
    		// `buttonPanel` houses sequence buttons
    		buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    		buttonPanel.setBackground(new Color(245, 235, 220));
    		
    		JButton button = new JButton("Sequence " + i);
    		button.addActionListener(this);
    		
    		buttonPanel.add(button);
    		add(buttonPanel);
    		add(sequencePanels[i]);
    	}
    	// first sequence emphasized
    	sequencePanels[0].setBackground(new Color(220, 200, 240));
    	panelEmphasis = 0;
    }
    
    // paint() function (for making boxes)
    // use Graphics2D
//    public void paint(Graphics gr)
//    {
//        super.paint(gr);
//        Graphics2D g = (Graphics2D) gr;
//        //block.render((Graphics2D) g);
//        //block2.render((Graphics2D) g);
//        for (Sequence sq : sequences) sq.render(g);
//        repaint();
//    }
    
    // add a box to the box list
    public void addSequence(String text)
    {
//        Sequence sq = new Sequence(150 + (seqNum * 100), "Sequence " + seqNum);
//        sequences.add(sq);
//        addMouseListener(sq);
//        ++seqNum;
    	System.out.println("adding block to sequence panel " + panelEmphasis);
    	sequencePanels[panelEmphasis].add(new BlockPanel(text));
//    	sequencePanels[0].setBackground(new Color(200, 200, 150));
//    	sequencePanels[0].add(new JLabel("hello"));
    	revalidate();
    	repaint();
    }
    
    
    @Override
    public void actionPerformed(ActionEvent e)
    {
    	// `Sequence i` - extract `i` as int
        String actionCommand = e.getActionCommand();
        panelEmphasis = Character.getNumericValue(actionCommand.charAt(9));

        // set background color appropriately
        // emphasized sequence is highlighted in RED
        for (int i=0; i<5; ++i)
        {
        	if (i != panelEmphasis)
        	{
        		sequencePanels[i].setBackground(new Color(220, 200, 200));
        	}
        	else
        	{
        		sequencePanels[i].setBackground(new Color(220, 200, 240));
        	}
        }
    }
    
    
}

// `SequencerPanel` is filled with `BlockPanel` objects
// extend JPanel and listen to mouse input
@SuppressWarnings("serial")
class BlockPanel extends JPanel implements MouseListener
{
	JLabel label;

    // constructor creates rectangle with mouse listener
    BlockPanel(String text)
    {
    	// first call the super constructor
    	super();
    	
    	setPreferredSize(new Dimension(100,100));
    	setBackground(new Color(240, 240, 240));
    	setBorder(BorderFactory.createMatteBorder(0, 0, 0, 2, new Color(50, 50, 80)));
    	addMouseListener(this);
    	label = new JLabel(text);
    	add(label);
    }
    
    // mouse listener
    @Override
    public void mouseClicked(MouseEvent e) {
//            if (e.getX() >= rectangle.x && e.getX() <= (rectangle.x + rectangle.getWidth())
//                    && e.getY() >= rectangle.y && e.getY() <= (rectangle.y + rectangle.getHeight())) {
//                System.out.println("Mouse clicked in " + command);
//            }
    	System.out.println("hi from " + e.getSource());
    }

    // implement functions from MouseListener interface?
    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
}
