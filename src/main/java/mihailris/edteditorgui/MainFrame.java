package mihailris.edteditorgui;

import mihailris.edteditorgui.actions.ActionOpenEDT;
import mihailris.edteditorgui.actions.Actions;
import mihailris.edteditorgui.uicomponents.EditorTree;
import mihailris.edteditorgui.uicomponents.TreePopUpMenu;
import mihailris.edteditorgui.utils.EditorSwingUtils;
import mihailris.edtfile.EDT;
import mihailris.edtfile.EDTGroup;
import mihailris.edtfile.EDTItem;
import mihailris.edtfile.EDTList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@Component
public class MainFrame extends JFrame {
    @Autowired
    public AppContext context;

    final EditorTree tree;
    private EDTItem selectionParent;

    public TreePath renaming;
    public MainFrame(){
        EditorSwingUtils.configTheme();

        setTitle(EDTEditorGUIApp.title+" "+EDTEditorGUIApp.versionString);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 600);

        JMenuBar mb = new JMenuBar();
        constructMenu(mb);

        tree = new EditorTree(this);

        JPanel panel = new JPanel();

        // Button for tree refreshing debug
        JButton rebuildTreeButton = new JButton("Rebuild Tree");
        rebuildTreeButton.addMouseListener(new MouseInputAdapter(){
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                super.mousePressed(mouseEvent);
                tree.buildTree();
            }
        });
        panel.add(rebuildTreeButton);

        JScrollPane treeScrollPane = new JScrollPane(tree);
        JPanel editorPanel = new JPanel();
        Color color = editorPanel.getBackground();
        editorPanel.setBackground(new Color(
                (int) (color.getRed()*0.8f),
                (int) (color.getGreen()*0.8f),
                (int) (color.getBlue()*0.8f)
        ));
        JSplitPane infoSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JPanel(), editorPanel);
        infoSplitPane.setContinuousLayout(true);
        infoSplitPane.setDividerLocation(250);
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScrollPane, infoSplitPane);
        splitPane.setDividerLocation(250);
        splitPane.setContinuousLayout(true);

        Container contentPane = getContentPane();
        contentPane.add(BorderLayout.SOUTH, panel);
        contentPane.add(BorderLayout.NORTH, mb);
        contentPane.add(BorderLayout.CENTER, splitPane);

        configureDrop();
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
                    EDTItem edtItem = EDT.read(Files.readAllBytes(file.toPath()));
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
        updateTitle();
        setVisible(true);
    }

    /**
     * Create menu items for the application
     * @param mb target MenuBar
     */
    private void constructMenu(JMenuBar mb){
        JMenu m1 = new JMenu("File");
        m1.setMnemonic('f');
        JMenu m2 = new JMenu("Edit");
        m2.setMnemonic('e');
        mb.add(m1);
        mb.add(m2);
        JMenuItem m21 = new JMenuItem("Undo");
        JMenuItem m22 = new JMenuItem("Redo");
        m21.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK));
        m22.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.ALT_DOWN_MASK));
        m21.addActionListener(actionEvent -> Actions.undo(context));
        m22.addActionListener(actionEvent -> Actions.redo(context));
        m2.add(m21);
        m2.add(m22);
        JMenuItem m11 = new JMenuItem("New");
        m11.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK));
        m11.addActionListener(actionEvent -> {
            EDTItem newItem = EDTGroup.create("root");
            Actions.act(new ActionOpenEDT(context.root, newItem), context);
            context.setLastFile(null);
        });
        JMenuItem m12 = new JMenuItem("Open");
        m12.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK));
        m12.addActionListener(actionEvent -> {
            FileDialog fileChooser = new FileDialog(this);
            fileChooser.setVisible(true);
            String directory = fileChooser.getDirectory();
            String filename = fileChooser.getFile();
            if (directory == null || filename == null)
                return;
            File file = new File(directory, filename);
            try {
                byte[] bytes = Files.readAllBytes(file.toPath());
                EDTItem edtItem = EDT.read(bytes);
                context.setLastFile(file);
                Actions.act(new ActionOpenEDT(context.root, edtItem), context);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        JMenuItem m13 = new JMenuItem("Save");
        m13.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));
        m13.addActionListener(actionEvent -> saveEDTToFile(context.lastFile));

        JMenuItem m14 = new JMenuItem("Save as");
        m14.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK));
        m14.addActionListener(actionEvent -> saveEDTToFile(null));
        m1.add(m11);
        m1.add(m12);
        m1.add(m13);
        m1.add(m14);
    }

    /**
     * Save current root to the file and remember the file
     * @param file file where to write
     */
    private void saveEDTToFile(File file) {
        if (file == null){
            FileDialog fileChooser = new FileDialog(this);
            fileChooser.setMode(FileDialog.SAVE);
            fileChooser.setVisible(true);
            String directory = fileChooser.getDirectory();
            String filename = fileChooser.getFile();
            if (directory == null || filename == null)
                return;
            file = new File(directory, filename);
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

    public Object getSelectedNode(Object root, Object[] path, int index){
        if (index == path.length)
            return root;
        if (root instanceof EDTGroup) {
            selectionParent = (EDTItem) root;
            return getSelectedNode(((EDTGroup) root).getObjects().get(path[index].toString()), path, index+1);
        }
        if (root instanceof EDTList) {
            selectionParent = (EDTItem) root;
            return getSelectedNode(((EDTList) root).getObjects().get(Integer.parseInt(path[index].toString())), path, index+1);
        }
        return root;
    }



    public void openNodeContextMenu(MouseEvent e, TreePath path) {
        int row = tree.getClosestRowForLocation(e.getX(), e.getY());
        tree.setSelectionRow(row);
        new TreePopUpMenu(this, path).show(e.getComponent(), e.getX(), e.getY());
    }

    public void selectByPath(TreePath path) {
        selectionParent = null;
        getSelectedNode(context.root, path.getPath(), 1);
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

    public void onSomethingChanged() {
        tree.refreshTree();
        updateTitle();
    }
}
