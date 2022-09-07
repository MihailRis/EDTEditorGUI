package mihailris.edteditorgui.uicomponents;

import mihailris.edteditorgui.AppContext;
import mihailris.edteditorgui.EDTNodeUserData;
import mihailris.edteditorgui.actions.ActionSetValueGroup;
import mihailris.edteditorgui.actions.Actions;
import mihailris.edtfile.EDTGroup;
import mihailris.edtfile.EDTItem;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Element;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TextEditor {
    private final JPanel panel;
    private final JScrollPane editorScrollPane;
    private final JTextArea editorTextArea;
    private final JTextArea linesArea;
    private final JToolBar toolBar;
    private final DefaultHighlighter highlighter;
    private final DefaultHighlighter.DefaultHighlightPainter painter;
    private Object lineSelectionTag;
    private EDTNodeUserData userData;
    private int prevLines;

    public TextEditor() {
        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        editorTextArea = new JTextArea();
        editorTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 15));
        editorTextArea.setTabSize(4);

        editorScrollPane = new JScrollPane(editorTextArea);
        Color color = editorTextArea.getBackground();
        editorTextArea.setBackground(new Color(
                (int) (color.getRed()*0.8f),
                (int) (color.getGreen()*0.8f),
                (int) (color.getBlue()*0.8f)
        ));

        String text = "";
        linesArea = new JTextArea("1   ");
        linesArea.setEditable(false);
        editorTextArea.getDocument().addDocumentListener(new DocumentListener() {
            public String getText() {
                int actualLines = editorTextArea.getLineCount();
                if (prevLines != actualLines) {
                    int caretPosition = editorTextArea.getDocument().getLength();
                    String separator = System.getProperty("line.separator");
                    Element root = editorTextArea.getDocument().getDefaultRootElement();
                    StringBuilder text = new StringBuilder("1" + separator);
                    for (int i = 2; i < root.getElementIndex(caretPosition) + 2; i++) {
                        text.append(i).append(separator);
                    }
                    prevLines = actualLines;
                    return text.toString();
                }
                return null;
            }
            @Override
            public void changedUpdate(DocumentEvent de) {
                String text = getText();
                if (text != null) {
                    linesArea.setText(text);
                }
            }
            @Override
            public void insertUpdate(DocumentEvent de) {
                String text = getText();
                if (text != null) {
                    linesArea.setText(text);
                }
            }
            @Override
            public void removeUpdate(DocumentEvent de) {
                String text = getText();
                if (text != null) {
                    linesArea.setText(text);
                }
            }
        });
        editorTextArea.addCaretListener(caretEvent -> updateSelection());
        editorTextArea.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                updateSelection();
            }
        });
        editorTextArea.setSelectionColor(new Color(70, 90, 180));
        editorTextArea.setText(text);
        editorScrollPane.setRowHeaderView(linesArea);

        highlighter = (DefaultHighlighter)editorTextArea.getHighlighter();
        painter = new DefaultHighlighter.DefaultHighlightPainter(new Color(
                (int) (color.getRed()*0.9f),
                (int) (color.getGreen()*0.9f),
                (int) (color.getBlue()*0.9f)
        ));
        highlighter.setDrawsLayeredHighlights(false);
        try {
            int start = editorTextArea.getLineStartOffset(0);
            int end = editorTextArea.getLineEndOffset(0);

            lineSelectionTag = highlighter.addHighlight(start, end, painter);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        panel.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0, 50)));

        JButton convertToBytesButton = new JButton("Convert to bytes");
        convertToBytesButton.addActionListener(actionEvent -> {
            // vvv
        });

        toolBar = new JToolBar();
        toolBar.add(convertToBytesButton);
        toolBar.addSeparator();
        panel.add(toolBar, BorderLayout.NORTH);
        panel.add(editorScrollPane, BorderLayout.CENTER);
        setEnabled(false);
    }

    private void setEnabled(boolean enabled){
        editorTextArea.setEnabled(enabled);
        for (Component component : toolBar.getComponents()){
            component.setEnabled(enabled);
        }
    }

    private void updateSelection(){
        try {
            int line = editorTextArea.getLineOfOffset(editorTextArea.getSelectionStart());

            if (lineSelectionTag != null) {
                highlighter.removeHighlight(lineSelectionTag);
                lineSelectionTag = null;
            }
            if (editorTextArea.getSelectionEnd() - editorTextArea.getSelectionStart() == 0) {
                int start = editorTextArea.getLineStartOffset(line);
                int end = editorTextArea.getLineEndOffset(line);
                lineSelectionTag = highlighter.addHighlight(start, end, painter);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void open(EDTNodeUserData userData){
        this.userData = userData;
        setText((String) userData.getValue());
        setEnabled(true);
    }

    public JComponent getContainer(){
        return panel;
    }

    public void setText(String text) {
        editorTextArea.setText(text);
    }

    public String getText() {
        return editorTextArea.getText();
    }

    public void apply(AppContext context){
        if (userData == null)
            return;

        EDTItem parent = userData.getParent();
        if (userData.getParent() instanceof EDTGroup) {
            EDTGroup group = (EDTGroup) parent;
            Actions.act(new ActionSetValueGroup(group, userData.getTag(), userData.getValue(), getText(), userData), context);
        }
    }

    public void applyAndClose(AppContext context) {
        if (userData == null)
            return;
        apply(context);
        editorTextArea.setText("");
        userData = null;
        setEnabled(false);
    }
}
