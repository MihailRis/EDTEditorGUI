package mihailris.edteditorgui.uicomponents;

import mihailris.edteditorgui.AppContext;
import mihailris.edteditorgui.EDTNodeUserData;
import mihailris.edtfile.EDTGroup;
import mihailris.edtfile.EDTItem;
import mihailris.edtfile.EDTList;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
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
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        EDTNodeUserData userObject = (EDTNodeUserData) node.getUserObject();
        String key = userObject.getTag();
        {
            final Object edtValue = userObject.getValue();
            String valueString = String.valueOf(edtValue);
            EDTItem parentNode = userObject.getParent();
            boolean isListItem = false;
            if (parentNode != null) {
                if (parentNode instanceof EDTList)
                    isListItem = true;
            }
            if (isListItem){
                key = String.format(SPAN_FORMAT, "gray", "["+userObject.getIndex()+"]");
                if (userObject.getTag() != null)
                    key += " "+userObject.getTag();
            } else if (!(edtValue instanceof EDTItem)){
                key += ":";
            }
            String text;
            if (!(edtValue instanceof EDTItem)) {
                if (edtValue instanceof String) {
                    valueString = "\"" + valueString + "\"";
                }
                if (edtValue instanceof byte[]) {
                    valueString = "byte["+((byte[]) edtValue).length+"]";
                }
                text = key+" <b>"+String.format(SPAN_FORMAT, "#50A040", valueString)+"</b>";
            } else {
                if (edtValue instanceof EDTGroup && ((EDTGroup) edtValue).size() == 0){
                    text = key + ": " + String.format(SPAN_FORMAT, "gray", "empty");
                }
                else {
                    text = "" + key + "";
                }
            }
            this.setText("<html>" + text + "</html>");
        }
        return this;
    }
}
