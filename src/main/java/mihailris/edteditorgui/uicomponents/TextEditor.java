package mihailris.edteditorgui.uicomponents;

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
    private final JScrollPane editorScrollPane;
    private final JTextArea editorTextArea;
    private final JTextArea linesArea;
    private final DefaultHighlighter highlighter;
    private final DefaultHighlighter.DefaultHighlightPainter painter;
    private Object lineSelectionTag;

    public TextEditor() {
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
        linesArea = new JTextArea("1");
        linesArea.setEditable(false);
        editorTextArea.getDocument().addDocumentListener(new DocumentListener() {
            public String getText() {
                int caretPosition = editorTextArea.getDocument().getLength();
                Element root = editorTextArea.getDocument().getDefaultRootElement();
                StringBuilder text = new StringBuilder("1" + System.getProperty("line.separator"));
                for(int i = 2; i < root.getElementIndex(caretPosition) + 2; i++) {
                    text.append(i).append(System.getProperty("line.separator"));
                }
                return text.toString();
            }
            @Override
            public void changedUpdate(DocumentEvent de) {
                linesArea.setText(getText());
            }
            @Override
            public void insertUpdate(DocumentEvent de) {
                linesArea.setText(getText());
            }
            @Override
            public void removeUpdate(DocumentEvent de) {
                linesArea.setText(getText());
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

    public JScrollPane getContainer(){
        return editorScrollPane;
    }

    public void setText(String text) {
        editorTextArea.setText(text);
    }

    public String getText() {
        return editorTextArea.getText();
    }
}
