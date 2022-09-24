package mihailris.edteditorgui;

import mihailris.edteditorgui.actions.ActionOpenEDT;
import mihailris.edteditorgui.actions.Actions;
import mihailris.edteditorgui.components.EditorTree;
import mihailris.edteditorgui.components.InfoPanel;
import mihailris.edteditorgui.components.TextEditor;
import mihailris.edteditorgui.uicomponents.TreePopUpMenu;
import mihailris.edteditorgui.utils.DialogsUtil;
import mihailris.edtfile.EDT;
import mihailris.edtfile.EDTConvert;
import mihailris.edtfile.EDTGroup;
import mihailris.edtfile.EDTItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

@Component
public class MainFrame extends JFrame {
    @Autowired
    public AppContext context;
    @Autowired
    EditorTree tree;
    @Autowired
    TextEditor textEditor;
    @Autowired
    InfoPanel infoPanel;

    private TreePath lastSelectedPath;
    public TreePath renaming;

    public MainFrame(){
        setTitle(EDTEditorGUIApp.title+" "+EDTEditorGUIApp.versionString);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                super.windowClosing(windowEvent);
                if (!Actions.isAllSaved()) {
                    try {
                        File file = File.createTempFile("unsaved", ".edt");
                        Files.write(file.toPath(), EDT.write(context.root));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @PostConstruct
    private void buildUI(){
        JMenuBar menuBar = new JMenuBar();
        constructMenu(menuBar);

        JPanel footerPanel = new JPanel();

        // Button for tree refreshing debug
        JButton rebuildTreeButton = new JButton("Rebuild Tree");
        rebuildTreeButton.addMouseListener(new MouseInputAdapter(){
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                super.mousePressed(mouseEvent);
                tree.buildTree();
            }
        });
        footerPanel.add(rebuildTreeButton);

        JScrollPane treeScrollPane = new JScrollPane(tree);

        JSplitPane infoSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                infoPanel.getRootComponent(), textEditor.getRootComponent());
        infoSplitPane.setContinuousLayout(true);
        infoSplitPane.setDividerLocation(150);
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScrollPane, infoSplitPane);
        splitPane.setDividerLocation(250);
        splitPane.setContinuousLayout(true);

        Container contentPane = getContentPane();
        contentPane.add(BorderLayout.SOUTH, footerPanel);
        contentPane.add(BorderLayout.NORTH, menuBar);
        contentPane.add(BorderLayout.CENTER, splitPane);

        configureDrop();
    }

    public TreePath getLastSelectedPath() {
        return lastSelectedPath;
    }

    /**
     * EDT files drop feature configuration
     */
    private void configureDrop(){
        setDropTarget(new DropTarget() {
            @SuppressWarnings("unchecked")
            @Override
            public synchronized void drop(DropTargetDropEvent event) {
                try {
                    event.acceptDrop(DnDConstants.ACTION_COPY);
                    List<File> droppedFiles = (List<File>)
                            event.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    if (droppedFiles.size() != 1)
                        return;
                    File file = droppedFiles.get(0);
                    EDTItem edtItem = EDT.read(Files.readAllBytes(file.toPath()), 0);
                    context.setLastFile(file);
                    Actions.act(new ActionOpenEDT(context.root, edtItem), context);
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * The main start up method that launches the application
     */
    public void launch(){
        onRootChanged();
        updateTitle();
        setVisible(true);
        tree.grabFocus();
        tree.setSelectionPath(tree.getPathForRow(0));
    }

    /**
     * Create menu items for the application
     * @param menuBar target MenuBar
     */
    private void constructMenu(JMenuBar menuBar){
        constructFileMenu(menuBar);
        constructEditMenu(menuBar);
        constructHelpMenu(menuBar);
    }

    private void constructFileMenu(JMenuBar menuBar) {
        JMenu m1 = new JMenu("File");
        m1.setMnemonic('f');
        menuBar.add(m1);

        JMenuItem m11 = new JMenuItem("New");
        m11.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK));
        m11.addActionListener(actionEvent -> {
            EDTItem newItem = EDTGroup.create("root");
            Actions.act(new ActionOpenEDT(context.root, newItem), context);
            context.setLastFile(null);
        });
        m1.add(m11);

        JMenuItem m12 = new JMenuItem("Open");
        m12.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK));
        m12.addActionListener(actionEvent -> {
            File file = DialogsUtil.chooseOpenFile(this);
            if (file == null)
                return;
            try {
                byte[] bytes = Files.readAllBytes(file.toPath());
                EDTItem edtItem = EDT.read(bytes, 0);
                context.setLastFile(file);
                Actions.act(new ActionOpenEDT(context.root, edtItem), context);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        m1.add(m12);

        JMenuItem m13 = new JMenuItem("Save");
        m13.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));
        m13.addActionListener(actionEvent -> saveEDTToFile(context.lastFile));
        m1.add(m13);

        JMenuItem m14 = new JMenuItem("Save as");
        m14.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK));
        m14.addActionListener(actionEvent -> saveEDTToFile(null));
        m1.add(m14);

        JMenuItem m15 = new JMenuItem("Import EDT2");
        m15.addActionListener(actionEvent -> {
            File file = DialogsUtil.chooseOpenFile(this);
            if (file == null)
                return;
            try {
                byte[] bytes = Files.readAllBytes(file.toPath());
                EDTItem edtItem = EDT.readEDT2(bytes);
                // don't remember file to prevent random overwriting
                context.setLastFile(null);
                Actions.act(new ActionOpenEDT(context.root, edtItem), context);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        m1.add(m15);

        JMenuItem m16 = new JMenuItem("Export JSON");
        m16.addActionListener(actionEvent -> exportJson());
        m1.add(m16);

        JMenuItem m17 = new JMenuItem("Export YAML");
        m17.addActionListener(actionEvent -> exportYaml());
        m1.add(m17);
    }

    private void constructEditMenu(JMenuBar menuBar) {
        JMenu m2 = new JMenu("Edit");
        m2.setMnemonic('e');
        menuBar.add(m2);

        JMenuItem m21 = new JMenuItem("Undo");
        JMenuItem m22 = new JMenuItem("Redo");
        m21.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK));
        m22.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.ALT_DOWN_MASK));
        m21.addActionListener(actionEvent -> Actions.undo(context));
        m22.addActionListener(actionEvent -> Actions.redo(context));
        m2.add(m21);
        m2.add(m22);
    }

    private void constructHelpMenu(JMenuBar menuBar) {
        JMenu m3 = new JMenu("Help");
        m3.setMnemonic('h');
        menuBar.add(m3);

        JMenuItem m31 = new JMenuItem("EDT3 specification");
        m31.addActionListener(actionEvent -> {
            try {
                Desktop.getDesktop().browse(new URI("https://github.com/MihailRis/EDT3/#table-of-contents"));
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        });
        m3.add(m31);

        JMenuItem m32 = new JMenuItem("GitHub repository");
        m32.addActionListener(actionEvent -> {
            try {
                Desktop.getDesktop().browse(new URI("https://github.com/MihailRis/EDT3/"));
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        });
        m3.add(m32);

        JMenuItem m33 = new JMenuItem("About");
        m33.addActionListener(actionEvent -> {
            String builder = "<html>Editor developed for EDT3 format<br>" +
                    "Version: " + EDTEditorGUIApp.versionString +
                    "<br>EDT3EditorGUI © MihailRis 2022</html>";
            JOptionPane.showMessageDialog(this, builder, "About EDTEditorGUI", JOptionPane.INFORMATION_MESSAGE);
        });
        m3.add(m33);
    }

    /**
     * Save current root to the file and remember the file
     * @param file file where to write
     */
    private void saveEDTToFile(File file) {
        prepareForSave();
        if (file == null) {
            file = DialogsUtil.chooseSaveFile(this);
            if (file == null)
                return;
        }
        try {
            Files.write(file.toPath(), EDT.write(context.root));
            context.setLastFile(file);
            Actions.save();
            updateTitle();
        } catch (IOException e){
            JOptionPane.showMessageDialog(this, e);
        }
    }

    /**
     * Export root item to JSON format and save to the file
     */
    private void exportJson() {
        prepareForSave();
        File file = DialogsUtil.chooseSaveFile(this);
        if (file == null)
            return;
        try {
            Files.write(file.toPath(), EDTConvert.toJson(context.root).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e){
            JOptionPane.showMessageDialog(this, e);
        }
    }

    /**
     * Export root item to YAML format and save to the file
     */
    private void exportYaml() {
        prepareForSave();
        File file = DialogsUtil.chooseSaveFile(this);
        if (file == null)
            return;
        try {
            Files.write(file.toPath(), EDTConvert.toYaml(context.root).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e){
            JOptionPane.showMessageDialog(this, e);
        }
    }

    public void prepareForSave(){
        textEditor.apply(context);
    }

    public void openNodeContextMenu(java.awt.Component component, int x, int y, TreePath path) {
        int row = tree.getClosestRowForLocation(x, y);
        tree.setSelectionRow(row);
        TreePopUpMenu popUpMenu = new TreePopUpMenu(this, path);
        popUpMenu.show(component, x, y);
        popUpMenu.grabFocus();
    }

    public void onSelected(TreePath path) {
        lastSelectedPath = path;
        EDTNodeUserData userData = getUserData(path);
        infoPanel.set(userData);
        if (userData.getValue() instanceof String){
            textEditor.open(userData);
        } else {
            textEditor.applyAndClose(context);
            textEditor.setText("");
        }
    }

    public void startRenaming(TreePath path) {
        renaming = path;
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        EDTNodeUserData userData = (EDTNodeUserData) node.getUserObject();
        userData.setEditing(false);
        tree.startEditingAtPath(path);
    }

    public static EDTNodeUserData getUserData(TreeNode node){
        DefaultMutableTreeNode mutableTreeNode = (DefaultMutableTreeNode) node;
        return (EDTNodeUserData) mutableTreeNode.getUserObject();
    }

    public static EDTNodeUserData getUserData(TreePath path){
        return getUserData((TreeNode) path.getLastPathComponent());
    }

    /**
     * Called on context.root replaced
     */
    public void onRootChanged() {
        tree.onRootChanged();
        textEditor.close();

        TreePath selected = tree.getSelectionPath();
        if (selected != null){
            onSelected(selected);
        } else {
            infoPanel.set(null);
            infoPanel.update();
        }
    }

    /**
     * Updates title following pattern: <pre>filename[* - if has changes] - appTitle</pre>
     */
    public void updateTitle() {
        String title = "";
        if (context.lastFile == null) {
            title += "untitled";
        }
        else {
            title += context.lastFile.getName();
        }
        if (!Actions.isAllSaved()){
            title += "*";
        }

        title += " - " + EDTEditorGUIApp.title + " " + EDTEditorGUIApp.versionString;
        setTitle(title);
    }

    /**
     * Refresh application content after action perfomed
     */
    public void onSomethingChanged() {
        tree.refreshTree();
        updateTitle();
        infoPanel.update();
    }
}
