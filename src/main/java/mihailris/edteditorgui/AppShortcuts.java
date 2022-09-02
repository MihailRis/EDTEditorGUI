package mihailris.edteditorgui;

import javax.swing.*;
import javax.swing.Action;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;

public class AppShortcuts {
    private static final HashMap<KeyStroke, Action> actionMap = new HashMap<>();

    public static void createShortcuts(AppContext context){
        KeyStroke key1 = KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK);
        actionMap.put(key1, new AbstractAction("action1") {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Ctrl-Z pressed");
                Actions.undo(context);
            }
        });

        KeyStroke key2 = KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.ALT_DOWN_MASK);
        actionMap.put(key2, new AbstractAction("action2") {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Ctrl-Shift-Z pressed");
                Actions.redo(context);
            }
        });
        KeyboardFocusManager kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        kfm.addKeyEventDispatcher(e -> {
            KeyStroke keyStroke = KeyStroke.getKeyStrokeForEvent(e);
            if ( actionMap.containsKey(keyStroke) ) {
                final javax.swing.Action a = actionMap.get(keyStroke);
                final ActionEvent ae = new ActionEvent(e.getSource(), e.getID(), null);
                SwingUtilities.invokeLater(() -> a.actionPerformed(ae));
                return true;
            }
            return false;
        });
    }
}
