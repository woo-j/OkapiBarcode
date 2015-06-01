/*
 * Copyright 2014 Robin Stuart and Robert Elliott
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.org.okapibarcode.gui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import uk.org.okapibarcode.output.PostScript;
import uk.org.okapibarcode.output.ScalableVectorGraphics;

/**
 * Save bar code image to image file
 *
 * @author <a href="mailto:jakel2006@me.com">Robert Elliott</a>
 */
public class SaveImage {

    public void saveImage(File file, JPanel panel) throws IOException {
        int magnification = 1;
        int borderSize = 5;

        String extension = "";
        int i = file.getName().lastIndexOf('.');
        if (i > 0) {
            extension = file.getName().substring(i + 1);
        }

        switch (extension) {
            case "png":
            case "gif":
            case "jpg":
            case "bmp":
                BufferedImage img = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_RGB);
                panel.paint(img.getGraphics());
                ImageIO.write(img, extension, file);
                break;
            case "svg":
                ScalableVectorGraphics svg = new ScalableVectorGraphics();
                svg.setMagnification(magnification);
                svg.setBorderSize(borderSize);
                svg.setShapes(OkapiUI.symbol.rect, OkapiUI.symbol.txt, OkapiUI.symbol.hex, OkapiUI.symbol.target);
                svg.setValues(OkapiUI.dataInput, OkapiUI.symbol.getWidth(), OkapiUI.symbol.getHeight());
                svg.write(file);
                break;
            case "eps":
                PostScript eps = new PostScript();
                eps.setMagnification(magnification);
                eps.setBorderSize(borderSize);
                eps.setShapes(OkapiUI.symbol.rect, OkapiUI.symbol.txt, OkapiUI.symbol.hex, OkapiUI.symbol.target);
                eps.setValues(OkapiUI.dataInput, OkapiUI.symbol.getWidth(), OkapiUI.symbol.getHeight());
                eps.write(file);
                break;
            default:
                System.out.println("Unsupported output format");
                break;
        }
    }

}
