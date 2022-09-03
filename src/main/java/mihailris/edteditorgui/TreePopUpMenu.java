package mihailris.edteditorgui;

import mihailris.edteditorgui.actions.ActionCreateRemoveGroup;
import mihailris.edteditorgui.actions.ActionCreateRemoveList;
import mihailris.edtfile.EDTGroup;
import mihailris.edtfile.EDTItem;
import mihailris.edtfile.EDTList;
import mihailris.edtfile.EDTType;

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
                    NameInputDialog dialog = new NameInputDialog(frame);
                    int x = subnodeItem.getLocationOnScreen().x;
                    dialog.setLocation(x, mouseEvent.getYOnScreen()-50);
                    dialog.show(text -> {
                        text = text.trim();
                        if (text.isEmpty())
                            return;
                        Object object = createDefaultObject(EDTType.GROUP, text);
                        if (value instanceof EDTGroup){
                            EDTGroup group = (EDTGroup) value;
                            Actions.act(new ActionCreateRemoveGroup(group, text, object, true), frame.context);
                        }
                        else if (value instanceof EDTList){
                            EDTList list = (EDTList) value;
                            Actions.act(new ActionCreateRemoveList(list, list.size(), object, true), frame.context);
                        }
                    });
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

    public Object createDefaultObject(EDTType type, String tag){
        switch (type){
            case GROUP:
                return EDTGroup.create(tag);
            case LIST:
                return EDTList.create(tag);
            case INT64:
                return 0;
            case FLOAT32:
                return 0.0f;
            case FLOAT64:
                return 0.0;
            case STRING:
                return "";
        }
        throw new IllegalArgumentException(String.valueOf(type));
    }
}
