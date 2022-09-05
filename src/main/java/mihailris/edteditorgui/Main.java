package mihailris.edteditorgui;

import mihailris.edtfile.EDTGroup;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(SpringConfig.class);

        EDTGroup root = EDTGroup.create("root");
        /* root.put("version", "1.0.0");
        root.put("number", 5342);
        root.child("data")
                .put("name", "TEST-11")
                .put("value", 3.141592f)
                .put("flag", true);
        root.childList("liost").add(5).add("dyr").add(0.5f);*/

        AppContext appContext = (AppContext) context.getBean("appContext");
        MainFrame mainFrame = (MainFrame) context.getBean("mainFrame");
        appContext.setRoot(root);
        mainFrame.launch();
    }
}
