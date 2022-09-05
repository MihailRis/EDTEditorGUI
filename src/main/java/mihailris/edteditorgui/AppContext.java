package mihailris.edteditorgui;

import mihailris.edtfile.EDTGroup;
import mihailris.edtfile.EDTItem;
import mihailris.edtfile.EDTList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class AppContext {
    public EDTItem root;

    @Autowired
    public
    MainFrame mainFrame;

    File lastFile;

    AppContext(){
        root = EDTGroup.create("root");
    }

    public void setRoot(EDTItem root){
        this.root = root;
        mainFrame.onRootChanged();
    }

    public File getLastFile() {
        return lastFile;
    }

    public void setLastFile(File lastFile) {
        this.lastFile = lastFile;
    }

    public Object getEdtNode(Object root, Object[] path, int index){
        if (index == path.length)
            return root;
        if (root instanceof EDTGroup) {
            return getEdtNode(((EDTGroup) root).getObjects().get(path[index].toString()), path, index+1);
        }
        if (root instanceof EDTList) {
            return getEdtNode(((EDTList) root).getObjects().get(Integer.parseInt(path[index].toString())), path, index+1);
        }
        return root;
    }
}
