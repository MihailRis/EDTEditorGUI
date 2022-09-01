package mihailris.edteditorgui;

import mihailris.edtfile.EDTGroup;
import mihailris.edtfile.EDTItem;
import mihailris.edtfile.EDTList;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;

public class MainFrame extends JFrame {
    private EDTItem root;
    private JTree tree;
    private EDTItem selectionParent;
    private Object selection;
    public MainFrame(){
        configTheme();

        setTitle("EDT3 Editor GUI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);

        //Creating the MenuBar and adding components
        JMenuBar mb = new JMenuBar();
        JMenu m1 = new JMenu("File");
        JMenu m2 = new JMenu("Edit");
        mb.add(m1);
        mb.add(m2);
        JMenuItem m11 = new JMenuItem("Open");
        m11.addActionListener(actionEvent -> {
            FileDialog fileChooser = new FileDialog(this);
            fileChooser.show();
        });
        JMenuItem m22 = new JMenuItem("Save");
        JMenuItem m23 = new JMenuItem("Save as");
        m1.add(m11);
        m1.add(m22);
        m1.add(m23);

        //Creating the panel at bottom and adding components
        JPanel panel = new JPanel(); // the panel is not visible in output
        JLabel label = new JLabel("Enter Text");
        JTextField tf = new JTextField(10); // accepts up to 10 characters
        JButton send = new JButton("Send");
        JButton reset = new JButton("Reset");
        panel.add(label); // Components Added using Flow Layout
        panel.add(tf);
        panel.add(send);
        panel.add(reset);

        // Text Area at the Center
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("root");
        tree = new JTree(node);
        TreeCellEditor editor = new DefaultCellEditor(new JTextField());
        tree.setEditable(true);
        tree.setCellEditor(editor);
        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                int selRow = tree.getRowForLocation(e.getX(), e.getY());
                TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
                if(selRow != -1) {
                    if(e.getClickCount() == 1) {
                        int button = e.getButton();
                        switch (button) {
                            case MouseEvent.BUTTON1:
                                assert selPath != null;
                                selectByPath(selPath);
                                break;
                            case MouseEvent.BUTTON2:
                                openNodeContextMenu(selPath);
                                break;
                        }
                        System.out.println(e.getButton()+" "+selPath);
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
        setVisible(true);
    }

    public void load(EDTItem root){
        this.root = root;
        buildTree();
    }

    private void buildTree() {
        tree.setModel(new DefaultTreeModel(buildNode(root, root.getTag())));
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
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(key);
        if (root instanceof EDTGroup){
            EDTGroup group = (EDTGroup) root;
            Map<String, Object> objects = group.getObjects();
            objects.keySet().stream().sorted().forEach(k -> {
                node.add(buildNode(objects.get(k), k));
            });
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

    private void openNodeContextMenu(TreePath path) {
    }

    private void selectByPath(TreePath path) {
        selectionParent = null;
        selection = getSelectedNode(root, path.getPath(), 1);
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
}
