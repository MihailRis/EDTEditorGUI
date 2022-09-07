package mihailris.edteditorgui.uicomponents;

import mihailris.edteditorgui.actions.ActionSetValueGroup;
import mihailris.edteditorgui.actions.Actions;
import mihailris.edteditorgui.EDTNodeUserData;
import mihailris.edteditorgui.MainFrame;
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
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.nio.charset.StandardCharsets;

public class TreePopUpMenu extends JPopupMenu {
    JMenu subnodeItem;
    JMenuItem renameItem;
    JMenuItem deleteItem;
    JMenuItem convertItem;

    public TreePopUpMenu(MainFrame frame, TreePath path){
        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) path.getLastPathComponent();
        EDTNodeUserData userData = (EDTNodeUserData) treeNode.getUserObject();
        setFocusable(false);
        Object value = userData.getValue();
        if (value instanceof EDTItem) {
            EDTItem edtItem = (EDTItem) value;
            subnodeItem = new JMenu((value instanceof EDTGroup) ? "Put" : "Add");
            add(subnodeItem);

            subnodeItem.add(createSubnodeMenuItem(frame, EDTType.GROUP, edtItem, "Group"));
            subnodeItem.add(createSubnodeMenuItem(frame, EDTType.LIST, edtItem, "List"));
            subnodeItem.add(createSubnodeMenuItem(frame, EDTType.INT64, edtItem, "Integer"));
            subnodeItem.add(createSubnodeMenuItem(frame, EDTType.FLOAT32, edtItem, "Float"));
            subnodeItem.add(createSubnodeMenuItem(frame, EDTType.FLOAT64, edtItem, "Double"));
            subnodeItem.add(createSubnodeMenuItem(frame, EDTType.BOOL, edtItem, "Boolean"));
            subnodeItem.add(createSubnodeMenuItem(frame, EDTType.STRING, edtItem, "String"));
            subnodeItem.add(createSubnodeMenuItem(frame, EDTType.BYTES, edtItem, "Bytes"));
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

        final EDTItem parent = userData.getParent();
        if (userData.getValue() instanceof byte[]){
            convertItem = new JMenuItem("Decode to String");
            convertItem.addMouseListener(new MouseInputAdapter() {
                @Override
                public void mousePressed(MouseEvent mouseEvent) {
                    String string;
                    try {
                        string = new String((byte[]) userData.getValue(), StandardCharsets.UTF_8);
                    } catch (Exception e){
                        e.printStackTrace();
                        return;
                    }
                    if (parent instanceof EDTGroup) {
                        Actions.act(new ActionSetValueGroup((EDTGroup) parent, userData.getTag(), userData.getValue(), string, userData), frame.context);
                    } else {
                        // todo: fixme
                    }
                    frame.selectByPath(path);
                }
            });
            add(convertItem);
        }

        if (parent != null) {
            deleteItem = new JMenuItem("Delete");
            deleteItem.addActionListener(actionEvent -> {
                if (parent instanceof EDTGroup){
                    EDTGroup group = (EDTGroup) parent;
                    Actions.act(new ActionCreateRemoveGroup(group, userData.getTag(), value, false), frame.context);
                }
                else if (parent instanceof EDTList){
                    EDTList list = (EDTList) parent;
                    Actions.act(new ActionCreateRemoveList(list, userData.getIndex(), value, false), frame.context);
                }
            });
            add(deleteItem);
        }
    }

    private ActionListener createAddNodeActionListener(MainFrame frame, EDTType edtType, EDTItem value){
        return mouseEvent -> {
            NameInputDialog dialog = new NameInputDialog(frame);
            Point point = MouseInfo.getPointerInfo().getLocation();
            int x = point.x;
            int y = point.y;
            dialog.setLocation(x-160, y-50);
            dialog.show(text -> {
                text = text.trim();
                if (text.isEmpty())
                    return;
                Object object = createDefaultObject(edtType, text);
                if (value instanceof EDTGroup){
                    EDTGroup group = (EDTGroup) value;
                    Actions.act(new ActionCreateRemoveGroup(group, text, object, true), frame.context);
                }
                else if (value instanceof EDTList){
                    EDTList list = (EDTList) value;
                    Actions.act(new ActionCreateRemoveList(list, list.size(), object, true), frame.context);
                }
            });
        };
    }

    private JMenuItem createSubnodeMenuItem(MainFrame frame, EDTType type, EDTItem value, String text){
        JMenuItem subgroupItem = new JMenuItem(text);
        subgroupItem.addActionListener(createAddNodeActionListener(frame, type, value));
        return subgroupItem;
    }

    /**
     * @param type type of value to create
     * @param tag tag for EDTItem if it is EDTItem
     * @return default value for required type
     */
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
            case BOOL:
                return false;
            case BYTES:
                return new byte[0];
        }
        throw new IllegalArgumentException(String.valueOf(type));
    }
}
