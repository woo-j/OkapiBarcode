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
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import uk.org.okapibarcode.output.PostScriptRenderer;
import uk.org.okapibarcode.output.SvgRenderer;

/**
 * Save bar code image to image file
 *
 * @author <a href="mailto:jakel2006@me.com">Robert Elliott</a>
 */
public class SaveImage {

    public void save(File file, JPanel panel) throws IOException {
        String extension;
        int i = file.getName().lastIndexOf('.');
        if (i > 0) {
            extension = file.getName().substring(i + 1).toLowerCase();
        } else {
            extension = "png";
        }

        switch (extension) {
            case "png":
            case "gif":
            case "jpg":
            case "bmp":
                BufferedImage bi = new BufferedImage(OkapiUI.symbol.getWidth(), OkapiUI.symbol.getHeight(), BufferedImage.TYPE_INT_ARGB);
                panel.paint(bi.getGraphics());
                ImageIO.write(bi, extension, file);
                break;
            case "svg":
                SvgRenderer svg = new SvgRenderer(new FileOutputStream(file), OkapiUI.factor, OkapiUI.paperColour, OkapiUI.inkColour);
                svg.render(OkapiUI.symbol);
                break;
            case "eps":
                PostScriptRenderer eps = new PostScriptRenderer(new FileOutputStream(file), OkapiUI.factor, OkapiUI.paperColour, OkapiUI.inkColour);
                eps.render(OkapiUI.symbol);
                break;
            default:
                System.out.println("Unsupported output format");
                break;
        }
    }

}
