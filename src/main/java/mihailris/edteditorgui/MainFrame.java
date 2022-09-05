package mihailris.edteditorgui;

import mihailris.edteditorgui.actions.*;
import mihailris.edteditorgui.utils.InputChecker;
import mihailris.edtfile.EDT;
import mihailris.edtfile.EDTGroup;
import mihailris.edtfile.EDTItem;
import mihailris.edtfile.EDTList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Queue;
import java.util.*;

@Component
public class MainFrame extends JFrame {
    @Autowired
    AppContext context;

    final JTree tree;
    DefaultTreeModel treeModel;
    final Map<EDTItem, Boolean> expansions = new HashMap<>();
    final List<EDTNodeUserData> userDataList = new ArrayList<>();
    private EDTItem selectionParent;
    TreeCellEditor treeCellEditor;

    private TreePath renaming;
    public MainFrame(){
        EditorSwingUtils.configTheme();

        setTitle("EDT3 Editor GUI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 600);

        JMenuBar mb = new JMenuBar();
        constructMenu(mb);

        tree = createTree();

        JPanel panel = new JPanel();

        // Button for tree refreshing debug
        JButton rebuildTreeButton = new JButton("Rebuild Tree");
        rebuildTreeButton.addMouseListener(new MouseInputAdapter(){
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                super.mousePressed(mouseEvent);
                buildTree();
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
                    Actions.act(new ActionOpenEDT(context.root, edtItem), context);
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    public JTextField editorField;

    /**
     * @return empty configured JTree
     */
    private JTree createTree(){
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("root");
        JTree tree = new JTree(node);
        tree.setFocusCycleRoot(true);
        editorField = new JTextField(10);
        treeCellEditor = new DefaultCellEditor(editorField);
        treeCellEditor.addCellEditorListener(new CellEditorListener() {
            @Override
            public void editingStopped(ChangeEvent changeEvent) {
                cancelEdit();
            }

            @Override
            public void editingCanceled(ChangeEvent changeEvent) {
                cancelEdit();
            }

            private void cancelEdit(){
                if (renaming != null) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) renaming.getLastPathComponent();
                    EDTNodeUserData userData = (EDTNodeUserData) node.getUserObject();
                    userData.setEditing(true);
                    renaming = null;
                }
            }
        });
        tree.setEditable(true);
        tree.setCellEditor(treeCellEditor);
        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                int selRow = tree.getRowForLocation(e.getX(), e.getY());
                TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
                if(selRow != -1) {
                    if(e.getClickCount() == 1) {
                        assert selPath != null;
                        selectByPath(selPath);
                        int button = e.getButton();
                        if (button == MouseEvent.BUTTON3) {
                            openNodeContextMenu(e, selPath);
                        }
                    }
                }
            }
        });
        tree.addTreeExpansionListener(new TreeExpansionListener() {
            @Override
            public void treeExpanded(TreeExpansionEvent treeExpansionEvent) {
                TreePath path = treeExpansionEvent.getPath();
                EDTNodeUserData data = getUserData(path);
                System.out.println("expanded "+data.getValue());
                expansions.put((EDTItem) data.getValue(), true);
            }

            @Override
            public void treeCollapsed(TreeExpansionEvent treeExpansionEvent) {
                TreePath path = treeExpansionEvent.getPath();
                EDTNodeUserData data = getUserData(path);
                System.out.println("collapsed "+data.getValue());
                expansions.remove((EDTItem) data.getValue());
            }
        });
        tree.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent keyEvent) {
                super.keyPressed(keyEvent);
                if (keyEvent.getKeyCode() == KeyEvent.VK_DELETE){
                    TreePath selPath = tree.getSelectionPath();
                    if(selPath != null) {
                        EDTNodeUserData userData = getUserData(selPath);
                        EDTItem parent = userData.getParent();
                        if (parent instanceof EDTGroup) {
                            Actions.act(new ActionCreateRemoveGroup(
                                    (EDTGroup) parent,
                                    userData.getTag(),
                                    userData.getValue(),
                                    false
                            ), context);
                        }
                        else if (parent instanceof EDTList){
                            Actions.act(new ActionCreateRemoveList(
                                    (EDTList) parent,
                                    userData.getIndex(),
                                    userData.getValue(),
                                    false
                            ), context);
                        }
                    }
                }
                System.out.println(keyEvent.getKeyCode()+" "+keyEvent.getKeyChar());
            }
        });
        return tree;
    }

    public void launch(){
        setVisible(true);
    }

    /**
     * Create menu items for the application
     * @param mb target MenuBar
     */
    private void constructMenu(JMenuBar mb){
        JMenu m1 = new JMenu("File");
        JMenu m2 = new JMenu("Edit");
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
                Actions.act(new ActionOpenEDT(context.root, EDT.read(bytes)), context);
                context.setLastFile(file);
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
        } catch (IOException e){
            JOptionPane.showMessageDialog(this, e);
        }
    }

    public void buildTree() {
        userDataList.clear();
        DefaultMutableTreeNode rootNode = buildNode(null, context.root, context.root.getTag(), -1);
        treeModel = new DefaultTreeModel(rootNode);
        tree.setModel(treeModel);
        tree.setCellRenderer(new EDTTreeCellRenderer(context));
        refresh(rootNode, context.root);
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

    private DefaultMutableTreeNode buildNode(EDTItem parentEDT, Object root, String key, int index) {
        EDTNodeUserData edtNodeUserData = new EDTNodeUserData(parentEDT, key, root, index);
        userDataList.add(edtNodeUserData);
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(edtNodeUserData) {
            @Override
            public void setUserObject(Object userObject) {
                EDTNodeUserData userData = (EDTNodeUserData) this.userObject;
                if (parentEDT instanceof EDTGroup) {
                    if (renaming != null) {
                        String from = userData.getTag();
                        String into = String.valueOf(userObject);
                        if (!into.equals(from))
                            Actions.act(new ActionRenameGroupSubItem(
                                    (EDTGroup) parentEDT,
                                    from, into
                            ), context);
                    }
                    else {
                        Object performed = InputChecker.checkAndParse(String.valueOf(userObject), userData.getValue().getClass());
                        if (performed != null){
                            EDTGroup group = (EDTGroup) parentEDT;
                            Actions.act(new ActionSetValueGroup(group, userData.getTag(), userData.getValue(), performed, userData), context);
                        }
                        else {
                            System.err.println("invalid input");
                        }
                    }
                }
                userData.setEditing(true);
                System.out.println("MainFrame.setUserObject unrenaming");
                renaming = null;
            }
        };
        if (root instanceof EDTGroup){
            EDTGroup group = (EDTGroup) root;
            Map<String, Object> objects = group.getObjects();
            objects.keySet().stream().sorted().forEach(k -> node.add(buildNode(group, objects.get(k), k, -1)));
        }
        if (root instanceof EDTList){
            EDTList list = (EDTList) root;
            List<Object> objects = list.getObjects();
            for (int i = 0; i < objects.size(); i++) {
                String k = null;
                Object object = objects.get(i);
                if (object instanceof EDTItem){
                    k = ((EDTItem) object).getTag();
                }
                node.add(buildNode(list, object, k, i));
            }
        }
        return node;
    }

    private void openNodeContextMenu(MouseEvent e, TreePath path) {
        int row = tree.getClosestRowForLocation(e.getX(), e.getY());
        tree.setSelectionRow(row);
        new TreePopUpMenu(this, path).show(e.getComponent(), e.getX(), e.getY());
    }

    private void selectByPath(TreePath path) {
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

    private void findNotPresentedTags(EDTGroup group, Queue<String> notPresented, DefaultMutableTreeNode rootNode) {
        for (Map.Entry<String, Object> entry : group.getObjects().entrySet()){
            String tag = entry.getKey();
            boolean used = false;
            for (int i = 0; i < rootNode.getChildCount(); i++) {
                EDTNodeUserData subUserData = getUserData(rootNode.getChildAt(i));
                if (tag.equals(subUserData.getTag())) {
                    used = true;
                    break;
                }
            }
            if (!used)
                notPresented.add(tag);
        }
    }

    private void refresh(DefaultMutableTreeNode rootNode, EDTItem root) {
        EDTNodeUserData userData = (EDTNodeUserData) rootNode.getUserObject();
        userData.setTag(root.getTag());
        if (expansions.get(root) != null){
            tree.expandPath(new TreePath(rootNode.getPath()));
        }
        if (root instanceof EDTGroup) {
            EDTGroup group = (EDTGroup) root;
            Queue<String> notPresented = new ArrayDeque<>();

            findNotPresentedTags(group, notPresented, rootNode);

            for (int i = 0; i < rootNode.getChildCount(); i++) {
                DefaultMutableTreeNode subnode = (DefaultMutableTreeNode) rootNode.getChildAt(i);
                EDTNodeUserData subUserData = (EDTNodeUserData) subnode.getUserObject();
                Object subEDT = group.getObjects().get(subUserData.getTag());
                if (subEDT == null){
                    if (!notPresented.isEmpty()){
                        String tag = notPresented.remove();
                        subUserData.setTag(tag);
                        subEDT = group.getObjects().get(tag);
                    }
                    else {
                        if (tree.isPathSelected(new TreePath(subnode.getPath()))){
                            if (i > 0) {
                                tree.setSelectionPath(
                                        new TreePath(((DefaultMutableTreeNode) rootNode.getChildAt(i - 1)).getPath()));
                            }
                            else {
                                tree.setSelectionPath(new TreePath(rootNode.getPath()));
                            }
                        }
                        subnode.removeFromParent();
                        System.out.println("MainFrame.refresh remoove " + i);
                        i--;
                        continue;
                    }
                }
                if (subEDT instanceof EDTItem) {
                    refresh(subnode, (EDTItem) subEDT);
                }
                else {
                    subUserData.setValue(subEDT);
                }
            }
            while (!notPresented.isEmpty()){
                String tag = notPresented.remove();
                DefaultMutableTreeNode node = buildNode(group, group.getObjects().get(tag), tag, -1);
                rootNode.add(node);
            }
        }
        else if (root instanceof EDTList) {
            EDTList list = (EDTList) root;
            List<Object> objects = list.getObjects();
            int selectedIndex = -1;
            TreePath selected = tree.getSelectionPath();
            if (selected != null) {
                DefaultMutableTreeNode mutableTreeNode = (DefaultMutableTreeNode) selected.getLastPathComponent();
                if (mutableTreeNode.getParent() != null) {
                    selectedIndex = mutableTreeNode.getParent().getIndex(mutableTreeNode);
                }
            }
            if (rootNode.getChildCount() != list.size()){
                rootNode.removeAllChildren();
                for (int i = 0; i < list.size(); i++) {
                    String k = null;
                    Object object = objects.get(i);
                    if (object instanceof EDTItem){
                        k = ((EDTItem) object).getTag();
                    }
                    DefaultMutableTreeNode subnode = buildNode(list, object, k, i);
                    rootNode.add(subnode);
                    if (object instanceof EDTItem)
                        refresh(subnode, (EDTItem) object);
                }
                if (selectedIndex > 0){
                    tree.setSelectionPath(new TreePath(((DefaultMutableTreeNode)rootNode.getChildAt(selectedIndex-1)).getPath()));
                }
                else if (selectedIndex == 0){
                    tree.setSelectionPath(new TreePath(rootNode.getPath()));
                }
            }
            else {
                for (int i = 0; i < rootNode.getChildCount(); i++) {
                    DefaultMutableTreeNode subnode = (DefaultMutableTreeNode) rootNode.getChildAt(i);
                    EDTNodeUserData subUserData = (EDTNodeUserData) subnode.getUserObject();
                    Object subEDT = list.getObjects().get(i);
                    if (subEDT instanceof EDTItem) {
                        refresh(subnode, (EDTItem) subEDT);
                    } else {
                        subUserData.setValue(subEDT);
                    }
                }
            }
        }
    }

    public void refreshTree() {
        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) tree.getModel().getRoot();
        EDTItem root = context.root;

        refresh(rootNode, root);

        tree.updateUI();
        tree.repaint();
    }

    private static EDTNodeUserData getUserData(TreeNode node){
        DefaultMutableTreeNode mutableTreeNode = (DefaultMutableTreeNode) node;
        return (EDTNodeUserData) mutableTreeNode.getUserObject();
    }

    private static EDTNodeUserData getUserData(TreePath path){
        return getUserData((TreeNode) path.getLastPathComponent());
    }

    public void onRootChanged() {
        expansions.clear();
        buildTree();
    }
}
