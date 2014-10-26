package local.koltun.reportfw.docx;

import local.koltun.reportfw.images.ImageProcessor;
import local.koltun.reportfw.model.PageContentModel;
import org.apache.log4j.Logger;
import org.docx4j.Docx4J;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.wml.*;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import static org.imgscalr.Scalr.*;

public class FileProcessor extends SwingWorker<File, String> {
    private static Logger logger = Logger.getLogger(FileProcessor.class);

    private WordprocessingMLPackage wordprocessingMLPackage;
    private final List pageContentModels;
    private final File file;

    public FileProcessor(List pageContentModels, File file) {
        this.pageContentModels = pageContentModels;
        this.file = file;
    }

    @Override
    protected File doInBackground() {
        try {
            logger.info("Started.");
            long time = System.currentTimeMillis();

            wordprocessingMLPackage = WordprocessingMLPackageSingleton.getInstance();
            wordprocessingMLPackage.getMainDocumentPart().getContent().removeAll(
                    wordprocessingMLPackage.getMainDocumentPart().getContent()
            );

            for (int i = 0, size = pageContentModels.size(); i < size; i++) {
                long startLocalTime = System.currentTimeMillis();
                PageContentModel currentPageContent = (PageContentModel) pageContentModels.get(i);

                addImage(wordprocessingMLPackage, currentPageContent.getImage(), currentPageContent.getImageFormatName());
                addDescription(wordprocessingMLPackage, currentPageContent.getDescription());
                if (i != size - 1) {
                    addPageBreak(wordprocessingMLPackage);
                }
                long endLocalTime = System.currentTimeMillis();
                logger.info("Page created for " + (endLocalTime-startLocalTime) + "ms");
                setProgress((i + 1) * 100 / size);
            }

            Docx4J.save(wordprocessingMLPackage, file, Docx4J.FLAG_NONE);

            long endTime = System.currentTimeMillis();
            logger.info("Finished: " + (endTime - time) + " ms");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return file;
    }

    private void addImage(WordprocessingMLPackage wordprocessingMLPackage, Image contentImage, String imageFormatName) {
        BufferedImage image = resize(ImageProcessor.toBufferedImage(contentImage, imageFormatName), Method.BALANCED, 768, OP_ANTIALIAS);
        try {
            byte[] bytes = ImageProcessor.getBytesFromImage(image, imageFormatName);
            P pictureParagraph = newImage(wordprocessingMLPackage, bytes, null, null, 0, 1);
            wordprocessingMLPackage.getMainDocumentPart().addObject(pictureParagraph);
        } catch (Exception e) {
            logger.error("Error during generating image.");
        }
    }

    private void addDescription(WordprocessingMLPackage wordprocessingMLPackage, String description) {
        for (String text : description.split("\n")) {
            wordprocessingMLPackage.getMainDocumentPart().addParagraphOfText(text);
        }
    }

    private void addPageBreak(WordprocessingMLPackage wordprocessingMLPackage) {
        Br newPageBr = new Br();
        newPageBr.setType(STBrType.PAGE);
        wordprocessingMLPackage.getMainDocumentPart().addObject(newPageBr);
    }

    public P newImage(WordprocessingMLPackage wordMLPackage,
                      byte[] bytes,
                      String filenameHint, String altText,
                      int id1, int id2) throws Exception {
        BinaryPartAbstractImage imagePart = BinaryPartAbstractImage.createImagePart(wordMLPackage, bytes);
        Inline inline = imagePart.createImageInline(filenameHint, altText, id1, id2, false);
        ObjectFactory factory = Context.getWmlObjectFactory();
        P p = factory.createP();
        R run = factory.createR();
        p.getContent().add(run);
        org.docx4j.wml.Drawing drawing = factory.createDrawing();
        run.getContent().add(drawing);
        drawing.getAnchorOrInline().add(inline);
        return p;
    }
}
