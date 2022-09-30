package mihailris.edteditorgui.utils;

import java.awt.*;
import java.io.File;

public class DialogsUtil {
    /**
     * Show file open dialog
     * @return file or null if cancelled
     */
    public static File chooseOpenFile(Frame frame){
        FileDialog fileChooser = new FileDialog(frame);
        fileChooser.setVisible(true);
        String directory = fileChooser.getDirectory();
        String filename = fileChooser.getFile();
        if (directory == null || filename == null)
            return null;
        return new File(directory, filename);
    }

    /**
     * Show file save dialog
     * @return file or null if cancelled
     */
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
