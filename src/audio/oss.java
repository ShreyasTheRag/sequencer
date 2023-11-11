package audio;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class oss extends JFrame {
	public oss() {
		setTitle("Centered BoxLayout Example");
		setSize(720, 480);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        JButton button1 = new JButton("Button 1");
        JButton button2 = new JButton("Button 2");
        button1.setAlignmentX(CENTER_ALIGNMENT);
        button2.setAlignmentX(CENTER_ALIGNMENT);

        mainPanel.setAlignmentX(CENTER_ALIGNMENT);
        mainPanel.add(button1);
        mainPanel.add(Box.createVerticalStrut(10)); // Add some vertical space between components
        mainPanel.add(button2);

        add(mainPanel);

//        pack();
        setLocationRelativeTo(null); // Center the frame on the screen
        setVisible(true);
    }

    public static void main(String[] args) {
       oss ossu = new oss();
    }
}
