package local.koltun.reportfw.model;

import org.apache.commons.io.FilenameUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class PageContentModel {
    private String description;
    private final File file;
    private Image image;

    public PageContentModel(File file) {
        this.file = file;
        this.description = "";
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public synchronized Image getImage() {
        if (image == null) {
            try {
                image = ImageIO.read(this.file);
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }

        return image;
    }

    public File getFile() {
        return file;
    }

    public String getImageFormatName() {
        return FilenameUtils.getExtension(file.getName());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (this.getClass() != obj.getClass())
            return false;

        PageContentModel model = (PageContentModel) obj;

        if (getFile() == null) {
            if (model.getFile() != null) {
                return false;
            }
        } else {
            try {
                if (!getFile().getCanonicalPath().equals(model.getFile().getCanonicalPath())) {
                    return false;
                }
            } catch (IOException e) {
                System.err.println("File equals error: " + e.getMessage());
            }
        }

        return true;
    }

    @Override
    public String toString() {
        return getFile().getName();
    }
}
