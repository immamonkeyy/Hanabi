package clientboard;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import client.InvisiblePanel;

@SuppressWarnings("serial")
public class GameLog extends JScrollPane {

    private JTextArea textArea;
    private JScrollBar verticalScroll;

    public GameLog() {
        super();

        JPanel panel = InvisiblePanel.create(new BorderLayout());
        panel.add(new JButton("Other Discards"), BorderLayout.CENTER);

        textArea = new JTextArea();
        textArea.setOpaque(false);
        textArea.setEditable(false);
        textArea.setBorder(null);
        textArea.setLineWrap(true);

        panel.add(textArea, BorderLayout.SOUTH);

        verticalScroll = this.getVerticalScrollBar();
        verticalScroll.setPreferredSize(new Dimension(0, 0));

        this.setViewportView(panel);
        this.setOpaque(false);
        this.getViewport().setOpaque(false);
        this.setBorder(null);
        this.setPreferredSize(new Dimension(200, 180));
    }

    public void append(String s) {
        textArea.append("> " + s + "\n");
        verticalScroll.setValue(verticalScroll.getMaximum()); // make it scroll to bottom better
    }

}
