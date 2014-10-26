package local.koltun.reportfw.gui.panel;

import local.koltun.reportfw.gui.Application;
import local.koltun.reportfw.images.ImageProcessor;
import org.imgscalr.Scalr;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class ImagePanel extends JPanel {
    private Image image;
    private int imageHeight;
    private int imageWidth;

    public ImagePanel() {
        super();
    }

    public void loadImage(String filename) throws IOException {
        setImage(new ImageIcon(Application.class.getClassLoader().getResource(filename)).getImage());
    }

    public void setImage(Image image) {
        this.image = Scalr.resize(
                ImageProcessor.toBufferedImage(image, "png"),
                Scalr.Method.BALANCED,
                this.getWidth() == 0 ? 512 : image.getWidth(null) < this.getWidth() ? image.getWidth(null) : ((int) (this.getWidth()*0.8)),
                Scalr.OP_ANTIALIAS);
        setDimensions(image);
    }

    private void setDimensions(Image image) {
        imageWidth = image.getWidth(this);
        imageHeight = image.getHeight(this);
        revalidate();
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image != null) {
            int x = (this.getWidth() - image.getWidth(null))/2;
            int y = (this.getHeight() - image.getHeight(null))/2;
            g.drawImage(image, x, y, this);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(image.getWidth(this), image.getHeight(this));
    }
}
