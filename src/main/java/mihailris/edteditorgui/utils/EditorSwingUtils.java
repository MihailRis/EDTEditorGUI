package mihailris.edteditorgui.utils;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class EditorSwingUtils {
    public static void configLookAndFeel(){
        for (UIManager.LookAndFeelInfo lookAndFeel : UIManager.getInstalledLookAndFeels()) {
            System.out.println(lookAndFeel.getName()+" "+lookAndFeel.getClassName());
        }
        try {
            for (UIManager.LookAndFeelInfo lookAndFeel : UIManager.getInstalledLookAndFeels()) {
                if (lookAndFeel.getName().equals("GTK+")) {
                    setTheme(lookAndFeel.getClassName());
                    return;
                }
            }
            for (UIManager.LookAndFeelInfo lookAndFeel : UIManager.getInstalledLookAndFeels()) {
                if (lookAndFeel.getName().equals("Nimbus")) {
                    setTheme(lookAndFeel.getClassName());
                    return;
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void setTheme(String className){
        try {
            System.out.println("SET THEME "+className);
            javax.swing.UIManager.setLookAndFeel(className);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Map<String, String> getAvailableThemes(){
        Map<String, String> themes = new HashMap<>();
        for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
            themes.put(info.getName(), info.getClassName());
        }
        return themes;
    }
}
