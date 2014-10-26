package local.koltun.reportfw.gui.panel;

import local.koltun.reportfw.gui.resources.LocalizedResourceBundle;

import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;

public class AboutDialog extends JPanel {
    private ResourceBundle resourceBundle;

    public AboutDialog() {
        setLayout(new BorderLayout());
        JLabel authorLabel = new JLabel(
                LocalizedResourceBundle.getStringFromBundle("Author") + ": " +
                        LocalizedResourceBundle.getStringFromBundle("AuthorName"));
        add(authorLabel, BorderLayout.NORTH);
        JLabel versionLabel = new JLabel(LocalizedResourceBundle.getStringFromBundle("Version") + ": " +
                LocalizedResourceBundle.getStringFromBundle("VersionNumber"));
        add(versionLabel, BorderLayout.CENTER);
    }
}
