package mihailris.edteditorgui.utils;

import java.util.HashMap;
import java.util.Map;

public class EditorSwingUtils {
    public static void configTheme(){
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

    public static void setTheme(String className){
        try {
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
