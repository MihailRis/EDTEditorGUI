package mihailris.edteditorgui.uicomponents;

import mihailris.edteditorgui.AppContext;
import mihailris.edteditorgui.EDTNodeUserData;
import mihailris.edteditorgui.MainFrame;
import mihailris.edteditorgui.actions.ActionsUtil;
import mihailris.edteditorgui.utils.ImageUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Element;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.charset.StandardCharsets;

public class TextEditor {
    private final JPanel panel;
    private JScrollPane editorScrollPane;
    private final JTextArea editorTextArea;
    private final JTextArea linesArea;
    private final JToolBar toolBar;
    private final DefaultHighlighter highlighter;
    private final DefaultHighlighter.DefaultHighlightPainter painter;
    private Object lineSelectionTag;
    private EDTNodeUserData userData;
    private int prevLines;
    private boolean linesEnumeration = true;

    public TextEditor(MainFrame mainFrame) {
        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        editorTextArea = new JTextArea();
        editorTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 15));
        editorTextArea.setTabSize(4);
        editorTextArea.setMargin(new Insets(0, 2, 0, 2));

        Color color = editorTextArea.getBackground();
        editorTextArea.setBackground(new Color(
                (int) (color.getRed()*0.8f),
                (int) (color.getGreen()*0.8f),
                (int) (color.getBlue()*0.8f)
        ));

        linesArea = new JTextArea();
        linesArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));

        Color foregroundColor = linesArea.getForeground();
        linesArea.setForeground(new Color(foregroundColor.getRed(), foregroundColor.getGreen(), foregroundColor.getBlue(), 75));
        linesArea.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        linesArea.setMargin(new Insets(0, 2, 0, 2));
        linesArea.setEditable(false);
        linesArea.setText("1 ");
        editorTextArea.getDocument().addDocumentListener(new DocumentListener() {
            public String getText() {
                int actualLines = editorTextArea.getLineCount();
                mainFrame.infoPanel.update(actualLines, editorTextArea.getText().length());
                if (prevLines != actualLines) {
                    int caretPosition = editorTextArea.getDocument().getLength();
                    String separator = System.getProperty("line.separator");
                    Element root = editorTextArea.getDocument().getDefaultRootElement();
                    StringBuilder text = new StringBuilder("1 " + separator);
                    int length = root.getElementIndex(caretPosition);
                    for (int i = 2; i < length + 2 && i < 10; i++) {
                        text.append(i).append(' ').append(separator);
                    }
                    for (int i = 10; i < length + 2; i++) {
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
        editorTextArea.setText("");

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

        toolBar = new JToolBar();
        Color tint = toolBar.getForeground();

        ImageIcon icon = new ImageIcon(ImageUtils.loadColored("/images/line_nums.png", tint));

        Action linesEnumButton = new AbstractAction("123", icon) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                linesEnumeration = !linesEnumeration;
                if (linesEnumeration) {
                    editorScrollPane.setRowHeaderView(linesArea);
                } else {
                    editorScrollPane.setRowHeaderView(null);
                }
            }
        };
        toolBar.add(linesEnumButton);

        toolBar.addSeparator();

        JButton convertToBytesButton = new JButton("Convert to Bytes");
        convertToBytesButton.addActionListener(actionEvent -> {
            apply(mainFrame.context, getText().getBytes(StandardCharsets.UTF_8));
            close();
        });
        toolBar.add(convertToBytesButton);

        JButton button = new JButton("Update");
        button.addActionListener(actionEvent -> ActionsUtil.actionSetValue(userData, getText(), mainFrame.context));
        toolBar.add(button);

        panel.add(toolBar, BorderLayout.NORTH);

        editorScrollPane = new JScrollPane(editorTextArea);
        if (linesEnumeration)
            editorScrollPane.setRowHeaderView(linesArea);
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

    public JComponent getRootComponent(){
        return panel;
    }

    public void setText(String text) {
        editorTextArea.setText(text);
    }

    public String getText() {
        return editorTextArea.getText();
    }

    public void apply(AppContext context){
        apply(context, getText());
    }

    public void apply(AppContext context, Object value){
        if (userData == null)
            return;

        if (!userData.getValue().equals(value)) {
            ActionsUtil.actionSetValue(userData, value, context);
        }
    }

    public void applyAndClose(AppContext context) {
        if (userData == null)
            return;
        apply(context);
        close();
    }

    public void close() {
        editorTextArea.setText("");
        userData = null;
        setEnabled(false);
    }

    public int getLineCount() {
        return editorTextArea.getLineCount();
    }
}
