package client;

import java.awt.FlowLayout;
import java.awt.LayoutManager;

import javax.swing.JPanel;

public class InvisiblePanel {

    public static JPanel create() {
        return create(new FlowLayout());
    }

    public static JPanel create(LayoutManager layout) {
        JPanel p = new JPanel(layout);
        p.setOpaque(false);
        return p;
    }
}
