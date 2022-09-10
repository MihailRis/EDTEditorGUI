package mihailris.edteditorgui;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(SpringConfig.class);

        MainFrame mainFrame = (MainFrame) context.getBean("mainFrame");
        mainFrame.launch();
    }
}
