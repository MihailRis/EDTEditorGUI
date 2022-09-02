package mihailris.edteditorgui;

import mihailris.edteditorgui.actions.ActionOpenEDT;
import mihailris.edteditorgui.actions.ActionRenameGroupSubItem;
import mihailris.edteditorgui.actions.ActionSetValueGroup;
import mihailris.edtfile.EDT;
import mihailris.edtfile.EDTGroup;
import mihailris.edtfile.EDTItem;
import mihailris.edtfile.EDTList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.MouseInputAdapter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreePath;
import java.awt.*;
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
    final List<EDTNodeUserData> userDataList = new ArrayList<>();
    private EDTItem selectionParent;
    private Object selection;
    TreeCellEditor treeCellEditor;

    private TreePath renaming;
    public MainFrame(){
        EditorSwingUtils.configTheme();

        setTitle("EDT3 Editor GUI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);

        JMenuBar mb = new JMenuBar();
        constructMenu(mb);

        tree = createTree();

        JPanel panel = new JPanel();
        JButton reset = new JButton("Refresh");
        reset.addMouseListener(new MouseInputAdapter(){
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                super.mousePressed(mouseEvent);
                buildTree();
            }
        });
        panel.add(reset);

        JScrollPane treeScrollPane = new JScrollPane(tree);
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScrollPane, new JPanel());
        splitPane.setDividerLocation(200);
        splitPane.setContinuousLayout(true);

        Container contentPane = getContentPane();
        contentPane.add(BorderLayout.SOUTH, panel);
        contentPane.add(BorderLayout.NORTH, mb);
        contentPane.add(BorderLayout.CENTER, splitPane);
    }

    /**
     * @return empty configured JTree
     */
    private JTree createTree(){
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("root");
        JTree tree = new JTree(node);
        tree.setFocusCycleRoot(true);
        treeCellEditor = new DefaultCellEditor(new JTextField());
        treeCellEditor.addCellEditorListener(new CellEditorListener() {
            @Override
            public void editingStopped(ChangeEvent changeEvent) {
                renaming = null;
                System.out.println("MainFrame.editingStopped unrenaming");
            }

            @Override
            public void editingCanceled(ChangeEvent changeEvent) {
                renaming = null;
                System.out.println("MainFrame.editingCanceled unrenaming");
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
                        switch (button) {
                            case MouseEvent.BUTTON1:
                                break;
                            case MouseEvent.BUTTON3:
                                openNodeContextMenu(e, selPath);
                                break;
                        }
                    }
                }
            }
        });
        return tree;
    }

    public void launch(){
        AppShortcuts.createShortcuts(context);
        setVisible(true);
    }

    private void constructMenu(JMenuBar mb){
        JMenu m1 = new JMenu("File");
        JMenu m2 = new JMenu("Edit");
        mb.add(m1);
        mb.add(m2);
        JMenuItem m11 = new JMenuItem("Open");
        m11.addActionListener(actionEvent -> {
            FileDialog fileChooser = new FileDialog(this);
            fileChooser.setVisible(true);
            String directory = fileChooser.getDirectory();
            String filename = fileChooser.getFile();
            if (directory == null || filename == null)
                return;
            File file = new File(directory, filename);
            System.out.println(file);
            try {
                byte[] bytes = Files.readAllBytes(file.toPath());
                Actions.act(new ActionOpenEDT(context.root, EDT.read(bytes)), context);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        JMenuItem m22 = new JMenuItem("Save");
        JMenuItem m23 = new JMenuItem("Save as");
        m1.add(m11);
        m1.add(m22);
        m1.add(m23);
    }

    public void buildTree() {
        userDataList.clear();
        treeModel = new DefaultTreeModel(buildNode(null, context.root, context.root.getTag()));
        tree.setModel(treeModel);
        tree.setCellRenderer(new EDTTreeCellRenderer(context));
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

    private DefaultMutableTreeNode buildNode(EDTItem parent, Object root, String key) {
        EDTNodeUserData edtNodeUserData = new EDTNodeUserData(parent, key, root);
        userDataList.add(edtNodeUserData);
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(edtNodeUserData) {
            @Override
            public void setUserObject(Object userObject) {
                EDTNodeUserData userData = (EDTNodeUserData) this.userObject;
                if (selectionParent instanceof EDTGroup) {
                    if (renaming != null) {
                        String from = userData.getTag();
                        String into = String.valueOf(userObject);
                        if (!into.equals(from))
                            Actions.act(new ActionRenameGroupSubItem(
                                    (EDTGroup) selectionParent,
                                    from, into,
                                    userData), context);
                    }
                    else {
                        Object performed = InputChecker.checkAndParse(String.valueOf(userObject), userData.getValue().getClass());
                        if (performed != null){
                            if (selectionParent instanceof EDTGroup) {
                                EDTGroup group = (EDTGroup) selectionParent;
                                Actions.act(new ActionSetValueGroup(group, userData.getTag(), userData.getValue(), performed, userData), context);
                            }
                            else {
                                throw new IllegalStateException();
                            }
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
            objects.keySet().stream().sorted().forEach(k -> node.add(buildNode(group, objects.get(k), k)));
        }
        if (root instanceof EDTList){
            EDTList list = (EDTList) root;
            List<Object> objects = list.getObjects();
            for (int i = 0; i < objects.size(); i++) {
                node.add(buildNode(list, objects.get(i), String.valueOf(i)));
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
        selection = getSelectedNode(context.root, path.getPath(), 1);
    }

    public void startRenaming(TreePath path) {
        renaming = path;
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        EDTNodeUserData userData = (EDTNodeUserData) node.getUserObject();
        userData.setEditing(false);
        System.out.println("MainFrame.startRenaming "+path.getLastPathComponent().getClass().getSuperclass());
        tree.startEditingAtPath(path);
    }

    private void refresh(DefaultMutableTreeNode rootNode, EDTItem root){
        EDTNodeUserData userData = (EDTNodeUserData) rootNode.getUserObject();
        userData.setTag(root.getTag());
        if (root instanceof EDTGroup) {
            EDTGroup group = (EDTGroup) root;
            Queue<String> notPresented = new ArrayDeque<>();
            for (Map.Entry<String, Object> entry : group.getObjects().entrySet()){
                String tag = entry.getKey();
                boolean used = false;
                for (int i = 0; i < rootNode.getChildCount(); i++) {
                    DefaultMutableTreeNode subnode = (DefaultMutableTreeNode) rootNode.getChildAt(i);
                    EDTNodeUserData subUserData = (EDTNodeUserData) subnode.getUserObject();
                    if (tag.equals(subUserData.getTag())) {
                        used = true;
                        break;
                    }
                }
                if (!used)
                    notPresented.add(tag);
            }

            System.out.println("MainFrame.refresh "+notPresented);

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
        }
        else if (root instanceof EDTList) {
            EDTList list = (EDTList) root;
            int childCount = rootNode.getChildCount();
            for (int i = 0; i < childCount; i++) {
                DefaultMutableTreeNode subnode = (DefaultMutableTreeNode) rootNode.getChildAt(i);
                EDTNodeUserData subUserData = (EDTNodeUserData) subnode.getUserObject();
                Object subEDT = list.getObjects().get(Integer.parseInt(subUserData.getTag()));
                if (subEDT instanceof EDTItem) {
                    refresh(subnode, (EDTItem) subEDT);
                }
                else {
                    subUserData.setValue(subEDT);
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
}
