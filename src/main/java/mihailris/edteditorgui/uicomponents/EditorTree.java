package mihailris.edteditorgui.uicomponents;

import mihailris.edteditorgui.AppContext;
import mihailris.edteditorgui.EDTNodeUserData;
import mihailris.edteditorgui.MainFrame;
import mihailris.edteditorgui.actions.*;
import mihailris.edteditorgui.utils.InputChecker;
import mihailris.edtfile.EDTGroup;
import mihailris.edtfile.EDTItem;
import mihailris.edtfile.EDTList;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import java.awt.event.*;
import java.util.*;

public class EditorTree extends JTree {
    public JTextField editorField;
    TreeCellEditor treeCellEditor;
    private final MainFrame mainFrame;
    final List<EDTNodeUserData> userDataList = new ArrayList<>();
    public final Map<EDTItem, Boolean> expansions = new HashMap<>();

    public EditorTree(MainFrame mainFrame){
        super();
        this.mainFrame = mainFrame;
        setFocusCycleRoot(true);
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
                if (mainFrame.renaming != null) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) mainFrame.renaming.getLastPathComponent();
                    EDTNodeUserData userData = (EDTNodeUserData) node.getUserObject();
                    userData.setEditing(true);
                    mainFrame.renaming = null;
                }
            }
        });
        setEditable(true);
        setCellEditor(treeCellEditor);
        addMouseListener(createMouseListener());
        addTreeExpansionListener(createTreeExpansionListener());
        addKeyListener(createTreeKeyListener());
        addTreeSelectionListener(treeSelectionEvent -> {
            TreePath path = treeSelectionEvent.getPath();
            if (path == mainFrame.getLastSelectedPath())
                return;
            mainFrame.onSelected(path);
        });
    }

    private MouseListener createMouseListener(){
        return new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                int selRow = getRowForLocation(e.getX(), e.getY());
                TreePath selPath = getPathForLocation(e.getX(), e.getY());
                if(selRow != -1) {
                    if(e.getClickCount() == 1) {
                        assert selPath != null;
                        mainFrame.onSelected(selPath);
                        int button = e.getButton();
                        if (button == MouseEvent.BUTTON3) {
                            mainFrame.openNodeContextMenu(e, selPath);
                        }
                    }
                }
            }
        };
    }

    private TreeExpansionListener createTreeExpansionListener(){
        return new TreeExpansionListener() {
            @Override
            public void treeExpanded(TreeExpansionEvent treeExpansionEvent) {
                TreePath path = treeExpansionEvent.getPath();
                EDTNodeUserData data = MainFrame.getUserData(path);
                expansions.put((EDTItem) data.getValue(), true);
            }

            @Override
            public void treeCollapsed(TreeExpansionEvent treeExpansionEvent) {
                TreePath path = treeExpansionEvent.getPath();
                EDTNodeUserData data = MainFrame.getUserData(path);
                expansions.remove((EDTItem) data.getValue());
            }
        };
    }

    private KeyListener createTreeKeyListener(){
        return new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent keyEvent) {
                super.keyPressed(keyEvent);
                if (keyEvent.getKeyCode() == KeyEvent.VK_DELETE){
                    TreePath selPath = getSelectionPath();
                    if(selPath != null) {
                        EDTNodeUserData userData = MainFrame.getUserData(selPath);
                        EDTItem parent = userData.getParent();
                        if (parent instanceof EDTGroup) {
                            Actions.act(new ActionCreateRemoveGroup(
                                    (EDTGroup) parent,
                                    userData.getTag(),
                                    userData.getValue(),
                                    false
                            ), mainFrame.context);
                        }
                        else if (parent instanceof EDTList){
                            Actions.act(new ActionCreateRemoveList(
                                    (EDTList) parent,
                                    userData.getIndex(),
                                    userData.getValue(),
                                    false
                            ), mainFrame.context);
                        }
                    }
                }
            }
        };
    }

    private void refresh(DefaultMutableTreeNode rootNode, EDTItem root) {
        EDTNodeUserData userData = (EDTNodeUserData) rootNode.getUserObject();
        userData.setTag(root.getTag());
        if (expansions.get(root) != null){
            expandPath(new TreePath(rootNode.getPath()));
        }
        if (root instanceof EDTGroup) {
            EDTGroup group = (EDTGroup) root;
            refresh(rootNode, group);
        }
        else if (root instanceof EDTList) {
            EDTList list = (EDTList) root;
            refresh(rootNode, list);
        }
    }

    private DefaultMutableTreeNode buildNode(EDTItem parentEDT, Object root, String key, int index) {
        EDTNodeUserData edtNodeUserData = new EDTNodeUserData(parentEDT, key, root, index);
        userDataList.add(edtNodeUserData);
        DefaultMutableTreeNode node = new EditorMutableTreeNode(edtNodeUserData);
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

    private void findNotPresentedTags(EDTGroup group, Queue<String> notPresented, DefaultMutableTreeNode rootNode) {
        for (Map.Entry<String, Object> entry : group.getObjects().entrySet()){
            String tag = entry.getKey();
            boolean used = false;
            for (int i = 0; i < rootNode.getChildCount(); i++) {
                EDTNodeUserData subUserData = MainFrame.getUserData(rootNode.getChildAt(i));
                if (tag.equals(subUserData.getTag())) {
                    used = true;
                    break;
                }
            }
            if (!used)
                notPresented.add(tag);
        }
    }

    @Override
    public void setSelectionPath(TreePath treePath) {
        super.setSelectionPath(treePath);
        mainFrame.onSelected(treePath);
    }

    private void refresh(DefaultMutableTreeNode rootNode, EDTGroup group){
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
                    if (isPathSelected(new TreePath(subnode.getPath()))){
                        if (i > 0) {
                            setSelectionPath(
                                    new TreePath(((DefaultMutableTreeNode) rootNode.getChildAt(i - 1)).getPath()));
                        }
                        else {
                            setSelectionPath(new TreePath(rootNode.getPath()));
                        }
                    }
                    subnode.removeFromParent();
                    i--;
                    continue;
                }
            }
            if (subEDT instanceof EDTItem) {
                refresh(subnode, (EDTItem) subEDT);
            }
            else {
                if (subnode.getChildCount() > 0) {
                    subnode.removeAllChildren();
                }
                subUserData.setValue(subEDT);
            }
        }
        while (!notPresented.isEmpty()){
            String tag = notPresented.remove();
            DefaultMutableTreeNode node = buildNode(group, group.getObjects().get(tag), tag, -1);
            rootNode.add(node);

            setSelectionPath(new TreePath(node.getPath()));
        }
    }

    private void refresh(DefaultMutableTreeNode rootNode, EDTList list){
        List<Object> objects = list.getObjects();
        int selectedIndex = -1;
        TreePath selected = getSelectionPath();
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
                setSelectionPath(new TreePath(((DefaultMutableTreeNode)rootNode.getChildAt(selectedIndex-1)).getPath()));
            }
            else if (selectedIndex == 0){
                setSelectionPath(new TreePath(rootNode.getPath()));
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
                    if (subnode.getChildCount() > 0) {
                        subnode.removeAllChildren();
                    }
                    subUserData.setValue(subEDT);
                }
            }
        }
    }

    /**
     * Build whole JTree content from scratch
     */
    public void buildTree() {
        AppContext context = mainFrame.context;
        userDataList.clear();
        DefaultMutableTreeNode rootNode = buildNode(null, context.root, context.root.getTag(), -1);
        TreeModel treeModel = new DefaultTreeModel(rootNode);
        setModel(treeModel);
        setCellRenderer(new EDTTreeCellRenderer(context));
        refresh(rootNode, context.root);
    }

    public void onRootChanged() {
        expansions.clear();
        buildTree();
    }

    /**
     * Update JTree content without rebuilding whole tree
     */
    public void refreshTree() {
        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) getModel().getRoot();
        EDTItem root = mainFrame.context.root;

        refresh(rootNode, root);

        updateUI();
        repaint();
    }

    public class EditorMutableTreeNode extends DefaultMutableTreeNode {
        public EditorMutableTreeNode(Object userData) {
            super(userData);
        }

        @Override
        public void setUserObject(Object userObject) {
            EDTNodeUserData userData = (EDTNodeUserData) this.userObject;
            EDTItem parentEDT = userData.getParent();
            Object root = userData.getValue();
            boolean renaming = mainFrame.renaming != null;
            if (parentEDT instanceof EDTGroup) {
                if (renaming) {
                    String from = userData.getTag();
                    String into = String.valueOf(userObject);
                    if (!into.equals(from))
                        Actions.act(new ActionRenameGroupSubItem(
                                (EDTGroup) parentEDT,
                                from, into
                        ), mainFrame.context);
                }
            } else if (parentEDT == null) {
                if (renaming){
                    String from = userData.getTag();
                    String into = String.valueOf(userObject);
                    if (!into.equals(from))
                        Actions.act(new ActionRenameRoot(
                                (EDTItem) root,
                                from, into
                        ), mainFrame.context);
                }
            }
            if (!renaming){
                Object performed = InputChecker.checkAndParse(String.valueOf(userObject), userData.getValue().getClass());
                if (performed != null){
                    ActionsUtil.actionSetValue(userData, performed, mainFrame.context);
                } else {
                    System.err.println("invalid input");
                }
            }
            userData.setEditing(true);
            mainFrame.renaming = null;
        }
    }
}
