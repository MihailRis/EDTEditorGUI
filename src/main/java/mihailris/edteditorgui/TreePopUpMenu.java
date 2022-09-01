package mihailris.edteditorgui;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.tree.TreePath;
import java.awt.event.MouseEvent;

public class TreePopUpMenu extends JPopupMenu {
    JMenuItem subnodeItem;
    JMenuItem renameItem;
    JMenuItem deleteItem;
    public TreePopUpMenu(MainFrame frame, TreePath path){
        subnodeItem = new JMenuItem("Add");
        add(subnodeItem);

        renameItem = new JMenuItem("Rename");
        renameItem.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                super.mousePressed(mouseEvent);
                frame.tree.startEditingAtPath(path);
            }
        });
        add(renameItem);

        deleteItem = new JMenuItem("Delete");
        add(deleteItem);
    }
}
