package mihailris.edteditorgui;

import mihailris.edteditorgui.utils.EditorSwingUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {
    public static void main(String[] args) {
        EditorSwingUtils.configTheme();
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(SpringConfig.class);
        MainFrame mainFrame = (MainFrame) context.getBean("mainFrame");
        mainFrame.launch();
    }
}
