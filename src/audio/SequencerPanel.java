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
    // private Block block, block2;
    // static makes it so each object of the class shares `seqNum`
    static int seqNum = 0;
    
    // top panel
    JPanel topPanel;
    JPanel[] sequencePanels;
    // scroll bars
    JScrollPane[] scrollPanes;

    // constructor: create the panel
    public SequencerPanel()
    {
    	// call the superclass constructor (JPanel)
    	super();
    	setBackground(new Color(245, 235, 220));
        setPreferredSize(new Dimension(680, 720));
        
    	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    	
    	// `topPanel` is added first
    	topPanel = new JPanel();
    	topPanel.setBackground(new Color(245, 235, 220));
    	
    	// add `topPanel` to this SequencerPanel instance
        add(topPanel);
        
        
    	// initialize array of 5 JPanel's, one for each sequence
    	sequencePanels = new JPanel[5];
    	scrollPanes = new JScrollPane[5];
    	
    	for ( int i=0; i<sequencePanels.length; ++i )
    	{
    		sequencePanels[i] = new JPanel();
    		
//    		sequencePanels[i].setBackground(new Color(12, 12, 12));
    		
    		int v = ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER;
    		int h = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS;
    		scrollPanes[i] = new JScrollPane(sequencePanels[i], v, h);
    		// restrict x space
    		scrollPanes[i].setPreferredSize(new Dimension(1000, 200));
    		
    		add(scrollPanes[i]);
    	}

        ButtonAction[] buttonActionValues = ButtonAction.values();
        buttons = new JButton[buttonActionValues.length];
        
        for (byte i = 0; i < buttons.length; ++i) {
            String buttonName = String.join(" ", buttonActionValues[i].name().split("_")).toLowerCase();
            buttons[i] = new JButton((char) (buttonName.charAt(0) - ('a' - 'A')) + buttonName.substring(1));
            buttons[i].addActionListener(this);
            topPanel.add(buttons[i]);
        }
        
        
        
        
        
        // initialize scrollbar
        int v = ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER;
		int h = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS;
//		scrollPane = new JScrollPane(this, v, h);
//		scrollPane.setPreferredSize(new Dimension(680, 720));
        
        //block = new Block(300, 300, Color.red, "block 1");
        //block2 = new Block(400, 300, Color.red, "block 2");
    }
    
    // paint() function (for making boxes)
    // use Graphics2D
    public void paint(Graphics gr)
    {
        super.paint(gr);
        Graphics2D g = (Graphics2D) gr;
        //block.render((Graphics2D) g);
        //block2.render((Graphics2D) g);
        repaint();
    }
    
    // add a box to the box list
    private void addSequence()
    {
    }
    
    
    @Override
    public void actionPerformed(ActionEvent e)
    {
        String actionCommand = e.getActionCommand();
        if (actionCommand.equals("Add sequence"))
        {
            addSequence();
        }
        else if (actionCommand.equals("Add tone"))
        {

        }
        else if (actionCommand.equals("Add white noise"))
        {

        }
        else if (actionCommand.equals("Add percussion"))
        {

        }
        else if (actionCommand.equals("Save"))
        {

        }
        else if (actionCommand.equals("Play"))
        {

        }
    }

    // block class, listens to mouse input
    class Block implements MouseListener
    {
        private Rectangle2D.Double rectangle;
        private String command;
        private Color color;

        // constructor creates rectangle with mouse listener
        Block(double xOffset, double yOffset, Color color, String command)
        {
            rectangle = new Rectangle2D.Double(xOffset, yOffset, 100, 100);
            this.color = color;
            this.command = command;
            SequencerPanel.this.addMouseListener(this);
        }
        
        // render the rectangle
        void render(Graphics2D g) {
            g.setColor(color);
            g.fill(rectangle);
        }
        
        // mouse listener
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getX() >= rectangle.x && e.getX() <= (rectangle.x + rectangle.getWidth())
                    && e.getY() >= rectangle.y && e.getY() <= (rectangle.y + rectangle.getHeight())) {
                System.out.println("Mouse clicked in " + command);
            }
        }
        // Below methods shouldn't do anything
        public void mousePressed(MouseEvent e) {
        }
        public void mouseReleased(MouseEvent e) {}
        public void mouseEntered(MouseEvent e) {}
        public void mouseExited(MouseEvent e) {}
    }
    
    // `Button Action` enumeration
    private enum ButtonAction
    {
        ADD_TONE(new Color(194, 13, 6)),
        ADD_WHITE_NOISE(Color.gray),
        ADD_PERCUSSION(new Color(15, 48, 189)),
        SAVE(null),
        PLAY(null);

        private final Color color;
        ButtonAction(Color color) {
            this.color = color;
        }
    }
}
