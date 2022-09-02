package mihailris.edteditorgui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class SmallInputDialog extends JDialog {
    JTextField field;
    public SmallInputDialog(JFrame frame){
        super(frame);
        setUndecorated(true);
        JPanel panel = new JPanel();
        panel.add(new JLabel("Node name"));
        field = new JTextField(14);
        field.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent keyEvent) {
                super.keyPressed(keyEvent);
                if (keyEvent.getKeyCode() == KeyEvent.VK_ENTER)
                    setVisible(false);
            }
        });
        panel.add(field);
        add(panel);
        pack();
        setSize(204, 66);
        setAutoRequestFocus(true);
        getRootPane().setBorder(BorderFactory.createLineBorder(new Color(0,0,0,100)));
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setLocationRelativeTo(frame);
        this.setAlwaysOnTop(true);
        this.addWindowFocusListener(new WindowFocusListener() {
            public void windowGainedFocus(WindowEvent e) {
            }
            public void windowLostFocus(WindowEvent e) {
                if (e.getOppositeWindow() == null)
                    return;
                if (SwingUtilities.isDescendingFrom(e.getOppositeWindow(), SmallInputDialog.this)) {
                    return;
                }
                SmallInputDialog.this.setVisible(false);
            }
        });
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        EventQueue.invokeLater(() -> field.requestFocus());
    }
}
