package mihailris.edteditorgui;

import mihailris.edtfile.EDTItem;
import mihailris.edtfile.EDTList;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;
import java.awt.*;

public class EDTTreeCellRenderer extends DefaultTreeCellRenderer {
    private static final String SPAN_FORMAT = "<span style='color:%s;'>%s</span>";

    final AppContext context;

    public EDTTreeCellRenderer(AppContext context){
        this.context = context;
        setBorderSelectionColor(new Color(0, 0, 0, 0));
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
                                                  boolean leaf, int row, boolean hasFocus) {
        /*return*/ super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        Object userObject = node.getUserObject();
        {
            Object edtValue = context.getEdtNode(context.root, node.getPath(), 1);
            TreeNode parent = node.getParent();
            boolean isListItem = false;
            if (parent != null) {
                Object parentValue = context.getEdtNode(context.root, ((DefaultMutableTreeNode) parent).getPath(), 1);
                if (parentValue instanceof EDTList)
                    isListItem = true;
            }
            if (isListItem){
                userObject = String.format(SPAN_FORMAT, "gray", "["+userObject+"]");
            } else if (!(edtValue instanceof EDTItem)){
                userObject += ":";
            }
            String text;
            if (!(edtValue instanceof EDTItem)) {
                if (edtValue instanceof String) {
                    edtValue = "\"" + edtValue + "\"";
                }
                if (edtValue instanceof byte[]) {
                    edtValue = "byte["+((byte[]) edtValue).length+"]";
                }
                text = ""+String.format(SPAN_FORMAT, "white", userObject)+" <b>"+String.format(SPAN_FORMAT, "#50A040", edtValue)+"</b>";
            } else {
                text = ""+String.valueOf(userObject)+"";
            }
            this.setText("<html>" + text + "</html>");
        }
        return this;
    }
}
