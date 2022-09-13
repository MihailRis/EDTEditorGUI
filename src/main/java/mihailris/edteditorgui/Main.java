package mihailris.edteditorgui;

import mihailris.edteditorgui.utils.EditorSwingUtils;
import mihailris.edtfile.EDT;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;


public class Main {
    private static void configureLookAndFeel(Preferences preferences) throws BackingStoreException {
        if (preferences.get("look-and-feel", null) == null){
            EditorSwingUtils.configTheme();
            preferences.put("look-and-feel", javax.swing.UIManager.getLookAndFeel().getClass().getName());
            preferences.flush();
        }
        EditorSwingUtils.setTheme(preferences.get("look-and-feel", null));
    }

    public static void main(String[] args) throws IOException, BackingStoreException {
        Preferences preferences = Preferences.userRoot().node("edteditorgui");
        configureLookAndFeel(preferences);

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(SpringConfig.class);
        MainFrame mainFrame = (MainFrame) context.getBean("mainFrame");
        if (args.length > 0){
            File file = new File(args[0]);
            mainFrame.context.setRoot(EDT.read(Files.readAllBytes(file.toPath())));
        }
        mainFrame.launch();
    }
}
