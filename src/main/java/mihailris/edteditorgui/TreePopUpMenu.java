package mihailris.edteditorgui;

import mihailris.edtfile.EDTGroup;
import mihailris.edtfile.EDTItem;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.event.MouseEvent;

public class TreePopUpMenu extends JPopupMenu {
    JMenu subnodeItem;
    JMenuItem renameItem;
    JMenuItem deleteItem;
    public TreePopUpMenu(MainFrame frame, TreePath path){
        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) path.getLastPathComponent();
        EDTNodeUserData userData = (EDTNodeUserData) treeNode.getUserObject();
        setFocusable(false);
        Object value = userData.getValue();
        if (value instanceof EDTItem) {
            subnodeItem = new JMenu((value instanceof EDTGroup) ? "Put" : "Add");
            JMenuItem subgroupItem = new JMenuItem("Group");
            subgroupItem.addMouseListener(new MouseInputAdapter() {
                @Override
                public void mousePressed(MouseEvent mouseEvent) {
                    super.mousePressed(mouseEvent);
                    SmallInputDialog dialog = new SmallInputDialog(frame);
                    int x = subnodeItem.getLocationOnScreen().x;
                    dialog.setLocation(x, mouseEvent.getYOnScreen());
                    dialog.setVisible(true);
                }
            });
            subnodeItem.add(subgroupItem);
            subnodeItem.add(new JMenuItem("List"));
            subnodeItem.add(new JMenuItem("Integer"));
            subnodeItem.add(new JMenuItem("Float"));
            subnodeItem.add(new JMenuItem("Double"));
            subnodeItem.add(new JMenuItem("Boolean"));
            subnodeItem.add(new JMenuItem("String"));
            subnodeItem.add(new JMenuItem("Bytes"));
            add(subnodeItem);
        }

        renameItem = new JMenuItem("Rename");
        renameItem.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                super.mousePressed(mouseEvent);
                frame.startRenaming(path);
            }
        });
        add(renameItem);

        deleteItem = new JMenuItem("Delete");
        add(deleteItem);
    }
}
