package mihailris.edteditorgui.utils;

import java.awt.*;
import java.io.File;

public class DialogsUtil {
    public static File chooseOpenFile(Frame frame){
        FileDialog fileChooser = new FileDialog(frame);
        fileChooser.setVisible(true);
        String directory = fileChooser.getDirectory();
        String filename = fileChooser.getFile();
        if (directory == null || filename == null)
            return null;
        return new File(directory, filename);
    }

    public static File chooseSaveFile(Frame frame){
        FileDialog fileChooser = new FileDialog(frame);
        fileChooser.setMode(FileDialog.SAVE);
        fileChooser.setVisible(true);
        String directory = fileChooser.getDirectory();
        String filename = fileChooser.getFile();
        if (directory == null || filename == null)
            return null;
        return new File(directory, filename);
    }
}
