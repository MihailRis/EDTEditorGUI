package mihailris.edteditorgui;

import mihailris.edteditorgui.actions.ActionOpenEDT;
import mihailris.edteditorgui.actions.ActionRenameGroupSubItem;
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
import java.util.EventObject;
import java.util.List;
import java.util.Map;

@Component
public class MainFrame extends JFrame {
    @Autowired
    AppContext context;

    final JTree tree;
    private EDTItem selectionParent;
    private Object selection;
    TreeCellEditor treeCellEditor;
    private boolean renaming = false;
    public MainFrame(){
        configTheme();

        setTitle("EDT3 Editor GUI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);

        JMenuBar mb = new JMenuBar();
        JMenu m1 = new JMenu("File");
        JMenu m2 = new JMenu("Edit");
        mb.add(m1);
        mb.add(m2);
        JMenuItem m11 = new JMenuItem("Open");
        m11.addActionListener(actionEvent -> {
            FileDialog fileChooser = new FileDialog(this);
            fileChooser.setVisible(true);
            File file = new File(fileChooser.getDirectory(), fileChooser.getFile());
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

        JPanel panel = new JPanel();
        JLabel label = new JLabel("Enter Text");
        JTextField tf = new JTextField(10);
        JButton send = new JButton("Send");
        JButton reset = new JButton("Refresh");
        reset.addMouseListener(new MouseInputAdapter(){
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                super.mousePressed(mouseEvent);
                buildTree();
            }
        });
        panel.add(label);
        panel.add(tf);
        panel.add(send);
        panel.add(reset);

        DefaultMutableTreeNode node = new DefaultMutableTreeNode("root");
        tree = new JTree(node);
        tree.setFocusCycleRoot(true);

        treeCellEditor = new DefaultCellEditor(new JTextField()) {
            @Override
            public boolean isCellEditable(EventObject event) {
                MouseEvent e = (MouseEvent) event;
                if (e != null) {
                    int selRow = tree.getRowForLocation(e.getX(), e.getY());
                    TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
                    if (selRow != -1) {
                        System.out.println("MainFrame.isCellEditable " + selPath);
                        assert selPath != null;
                        Object object = context.getEdtNode(context.root, selPath.getPath(), 1);
                        if (object instanceof EDTItem)
                            return false;
                    }
                }
                return super.isCellEditable(e);
            }
        };

        treeCellEditor.addCellEditorListener(new CellEditorListener() {
            @Override
            public void editingStopped(ChangeEvent changeEvent) {
                renaming = false;
                System.out.println("MainFrame.editingStopped unrenaming");
            }

            @Override
            public void editingCanceled(ChangeEvent changeEvent) {
                renaming = false;
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

        JScrollPane treeScrollPane = new JScrollPane(tree);
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScrollPane, new JPanel());
        splitPane.setDividerLocation(200);
        splitPane.setContinuousLayout(true);

        //Adding Components to the frame.
        Container contentPane = getContentPane();
        contentPane.add(BorderLayout.SOUTH, panel);
        contentPane.add(BorderLayout.NORTH, mb);
        contentPane.add(BorderLayout.CENTER, splitPane);
    }

    public void buildTree() {
        tree.setModel(new DefaultTreeModel(buildNode(context.root, context.root.getTag())));
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

    private DefaultMutableTreeNode buildNode(Object root, String key) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(new EDTNodeUserData(key, root)) {
            @Override
            public void setUserObject(Object userObject) {
                if (selectionParent instanceof EDTGroup) {
                    EDTNodeUserData userData = (EDTNodeUserData) this.userObject;
                    if (renaming) {
                        Actions.act(new ActionRenameGroupSubItem(
                                (EDTGroup) selectionParent,
                                selection,
                                userData.getTag(),
                                (String) userObject), context);
                        userData.setTag((String) userObject);
                    }
                    else {
                        super.setUserObject(userObject);
                    }
                }
                System.out.println("MainFrame.setUserObject unrenaming");
                renaming = false;
            }
        };
        if (root instanceof EDTGroup){
            EDTGroup group = (EDTGroup) root;
            Map<String, Object> objects = group.getObjects();
            objects.keySet().stream().sorted().forEach(k -> node.add(buildNode(objects.get(k), k)));
        }
        if (root instanceof EDTList){
            EDTList list = (EDTList) root;
            List<Object> objects = list.getObjects();
            for (int i = 0; i < objects.size(); i++) {
                node.add(buildNode(objects.get(i), String.valueOf(i)));
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

    private static void configTheme(){
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("GTK+".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startRenaming(TreePath path) {
        renaming = true;
        tree.startEditingAtPath(path);
    }
}
