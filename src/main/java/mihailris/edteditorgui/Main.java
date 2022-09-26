package mihailris.edteditorgui;

import com.formdev.flatlaf.FlatDarkLaf;
import mihailris.edtfile.EDT;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;


public class Main {
    public static void main(String[] args) throws IOException {
        FlatDarkLaf.setup();

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(SpringConfig.class);
        MainFrame mainFrame = (MainFrame) context.getBean("mainFrame");
        if (args.length > 0){
            File file = new File(args[0]);
            mainFrame.context.setRoot(EDT.read(Files.readAllBytes(file.toPath())));
        }
        mainFrame.launch();
    }
}
