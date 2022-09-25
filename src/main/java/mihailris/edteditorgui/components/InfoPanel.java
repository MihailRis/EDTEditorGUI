package mihailris.edteditorgui.components;

import mihailris.edteditorgui.EDTNodeUserData;
import mihailris.edtfile.EDTGroup;
import mihailris.edtfile.EDTList;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class InfoPanel {
    private static final String SPAN_FORMAT = "<span style='color:%s;'>%s</span>";

    private final JPanel infoPanel;
    private final JTextPane itemTitleLabel;
    private EDTNodeUserData userData;
    private final Pattern pattern = Pattern.compile("\r\n|\r|\n");

    public InfoPanel(){
        infoPanel = new JPanel(new BorderLayout());
        itemTitleLabel = new JTextPane();
        itemTitleLabel.setContentType("text/html");
        itemTitleLabel.setText("<html><b>Group 'root' with 0 items</b></html>");
        itemTitleLabel.setEditable(false);
        itemTitleLabel.setBorder(null);
        itemTitleLabel.setOpaque(false);
        itemTitleLabel.setMargin(new Insets(10, 4, 4, 4));
        itemTitleLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 15));
        infoPanel.add(itemTitleLabel, BorderLayout.CENTER);
        update();
    }
    public JComponent getRootComponent(){
        return infoPanel;
    }


    public void update(){
        int lines = 0;
        int length = 0;
        if (userData != null && userData.getValue() instanceof String){
            String string = (String) userData.getValue();
            Matcher matcher = pattern.matcher(string);
            while (matcher.find()){
                lines++;
            }
            length = string.length();
        }
        update(lines, length);
    }

    public void update(int editorLines, int editorLength){
        StringBuilder builder = new StringBuilder();
        builder.append("<html>");
        builder.append("<h3 style='color:gray;'>Selected node information</h3>");

        if (userData == null){
            empty(builder);
            builder.append("</html>");
            itemTitleLabel.setText(builder.toString());
            return;
        }

        Object value = userData.getValue();
        String tag = userData.getTag();
        int index = userData.getIndex();
        if (value instanceof EDTGroup){
            common(builder, "group", tag, index);
            groupInfo(builder, (EDTGroup)value);
        }
        else if (value instanceof EDTList){
            common(builder, "list", tag, index);
            listInfo(builder, (EDTList)value);
        }
        else if (value instanceof String){
            common(builder, "string", tag, index);
            stringInfo(builder, editorLines, editorLength);
        }
        else if (value instanceof byte[]){
            common(builder, "bytes", tag, index);
            bytesInfo(builder, (byte[])value);
        }
        else if (value instanceof Integer || value instanceof Long){
            common(builder, "int", tag, index);
            intInfo(builder, ((Number)value).longValue());
        }
        else if (value instanceof Float){
            common(builder, "float", tag, index);
            floatInfo(builder, (Float)value);
        }
        else if (value instanceof Double){
            common(builder, "double", tag, index);
            doubleInfo(builder, (Double)value);
        }
        else if (value instanceof Boolean){
            common(builder, "bool", tag, index);
            boolInfo(builder, (Boolean)value);
        }
        else {
            empty(builder);
        }

        builder.append("</html>");
        itemTitleLabel.setText(builder.toString());
    }

    private void common(StringBuilder builder, String type, String tag, int index){
        builder.append("<b>Type:</b> ").append(type).append("<br>");
        if (tag != null)
            builder.append("<b>Tag:</b> '").append(tag).append("'<br>");
        else
            builder.append("<b>Tag:</b> ").append(String.format(SPAN_FORMAT, "gray", "null")).append("<br>");
        if (index != -1) {
            builder.append("<b>Index:</b> ").append(index).append("<br>");
        }
    }

    private void intInfo(StringBuilder builder, long value) {
        builder.append("<b>Dec:</b> ").append(value).append("<br>");
        builder.append("<b>Hex:</b> ").append(Long.toHexString(value)).append("<br>");
        builder.append("<b>Oct:</b> ").append(Long.toOctalString(value)).append("<br>");
        builder.append("<b>Bin:</b> ").append(Long.toBinaryString(value)).append("<br>");
    }

    private void boolInfo(StringBuilder builder, boolean value) {
        builder.append("<b>Value:</b> ").append(value).append("<br>");
    }

    private void floatInfo(StringBuilder builder, float value) {
        builder.append("<b>Value:</b> ").append(value).append("<br>");
    }

    private void doubleInfo(StringBuilder builder, double value) {
        builder.append("<b>Value:</b> ").append(value).append("<br>");
    }

    private void empty(StringBuilder builder){
        builder.append("<i>").append(String.format(SPAN_FORMAT, "gray", "nothing to show")).append("</i>");
    }

    private void listInfo(StringBuilder builder, EDTList list) {
        builder.append("<b>Length:</b> ").append(list.size()).append(" items<br>");
    }

    private void groupInfo(StringBuilder builder, EDTGroup group) {
        builder.append("<b>Size:</b> ").append(group.size()).append(" items<br>");
    }

    private void stringInfo(StringBuilder builder, int lines, int length) {
        builder.append("<b>Lines:</b> ").append(lines).append("<br>");
        builder.append("<b>Length:</b> ").append(length).append("<br>");
    }

    private void bytesInfo(StringBuilder builder, byte[] bytes) {
        builder.append("<b>Length:</b> ").append(bytes.length).append("<br>");
    }

    public void set(EDTNodeUserData userData) {
        this.userData = userData;
        update();
    }
}
