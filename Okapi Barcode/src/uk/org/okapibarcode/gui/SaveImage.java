package uk.org.okapibarcode.gui;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import uk.org.okapibarcode.output.ScalableVectorGraphics;
import uk.org.okapibarcode.output.PostScript;

/**
 * Save bar code image to image file
 *
 * @author Robert Elliott <jakel2006@me.com>
 */
public class SaveImage {
    ScalableVectorGraphics svg;
    PostScript eps;

    public void SaveImage(File file, JPanel panel) {
        try {
            String extension = "";
            int i = file.getName().lastIndexOf('.');
            if (i > 0) {
                extension = file.getName().substring(i+1);
            }
            BufferedImage img = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_RGB);
            panel.paint(img.getGraphics());
            try {
                switch (extension) {
                    case "png":
                    case "gif":
                    case "jpg":
                    case "bmp":
                        ImageIO.write(img, extension, file);
                        break;
                    case "svg":
                        svg = new ScalableVectorGraphics();
                        svg.setShapes(OkapiUI.bcs, OkapiUI.txt, OkapiUI.hex, OkapiUI.target);
                        svg.setValues(OkapiUI.dataInput, OkapiUI.width, OkapiUI.height);
                        if (!(svg.write(file))) {
                            OkapiUI.errorOutput = "Error writing to file";
                        };
                        break;
                    case "eps":
                        eps = new PostScript();
                        eps.setShapes(OkapiUI.bcs, OkapiUI.txt, OkapiUI.hex, OkapiUI.target);
                        eps.setValues(OkapiUI.dataInput, OkapiUI.width, OkapiUI.height);
                        if (!(eps.write(file))) {
                            OkapiUI.errorOutput = "Error writing to file";
                        }
                        break;
                    default:
                        System.out.println("Unsupported output format");
                        break;
                }
            } catch (Exception e) {
            }
        } catch (Exception e) {
        }
    }
}
