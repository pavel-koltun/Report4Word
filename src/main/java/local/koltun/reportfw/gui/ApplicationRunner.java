package local.koltun.reportfw.gui;

import javax.swing.*;
import java.awt.*;

public class ApplicationRunner {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Application application = new Application("Создание отчета");
                application.setMinimumSize(new Dimension(320, 240));
                application.setSize(640, 480);
                application.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                application.setVisible(true);
            }
        });
    }
}
