/*
 * Copyright 2014 Robin Stuart
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
package uk.org.okapibarcode.output;

import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Outputs barcode to .SVG files
 *
 * @author <a href="mailto:rstuart114@gmail.com">Robin Stuart</a>
 */
public class ScalableVectorGraphics {
    public ArrayList<Rectangle> rectangle = new ArrayList<>();
    public ArrayList<uk.org.okapibarcode.backend.TextBox> textbox = new ArrayList<>();
    public ArrayList<uk.org.okapibarcode.backend.Hexagon> hexagon = new ArrayList<>();
    public ArrayList<Ellipse2D.Double> ellipse = new ArrayList<>();
    private int symbol_width;
    private int symbol_height;
    private String symbol_text = "";
    private String fgColour = "000000";
    private String bgColour = "FFFFFF";

    public void setShapes(ArrayList<Rectangle> bcs, ArrayList<uk.org.okapibarcode.backend.TextBox> txt,
            ArrayList<uk.org.okapibarcode.backend.Hexagon> hex, ArrayList<Ellipse2D.Double> target) {
        rectangle = bcs;
        textbox = txt;
        hexagon = hex;
        ellipse = target;
    }

    public void setValues (String readable, int width, int height) {
        symbol_width = width;
        symbol_height = height;
        symbol_text = readable;
    }

    public void write(File file) throws IOException {
        String outStream;
        int i, j;
        String nowColour;

        try (FileOutputStream fos = new FileOutputStream(file)) {
            // Header
            outStream = "<?xml version=\"1.0\" standalone=\"no\"?>\n";
            outStream += "<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\"\n";
            outStream += "   \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n";
            outStream += "<svg width=\"" + symbol_width + "\" height=\""
                    + symbol_height + "\" version=\"1.1\"\n";
            outStream += "   xmlns=\"http://www.w3.org/2000/svg\">\n";
            if (symbol_text.length() == 0) {
                outStream += "   <desc>OkapiBarcode Generated Symbol\n";
            } else {
                outStream += "   <desc>" + symbol_text + "\n";
            }
            outStream += "   </desc>\n";
            outStream += "\n   <g id=\"barcode\" fill=\"#" + fgColour + "\">\n";
            outStream += "      <rect x=\"0\" y=\"0\" width=\"" + symbol_width
                    + "\" height=\"" + symbol_height + "\" fill=\"#"
                    + bgColour + "\" />\n";

            // Rectangles
            for (i = 0; i < rectangle.size(); i++) {
                outStream += "      <rect x=\"" + rectangle.get(i).x
                        + ".00\" y=\"" + rectangle.get(i).y + ".00\" width=\""
                        + rectangle.get(i).width + ".00\" height=\""
                        + rectangle.get(i).height + ".00\" />\n";
            }

            // Text
            for(i = 0; i < textbox.size(); i++) {
                outStream += "      <text x=\"" + textbox.get(i).x + "\" y=\""
                        + textbox.get(i).y + "\" text-anchor=\"middle\"\n";
                outStream += "         font-family=\"Helvetica\" font-size=\"8\" fill=\""
                        + fgColour + "\" >\n";
                outStream += "         " + textbox.get(i).text + "\n";
                outStream += "      </text>\n";
            }

            // Circles
            for (i = 0; i < ellipse.size(); i++) {
                if ((i & 1) == 0) {
                    nowColour = fgColour;
                } else {
                    nowColour = bgColour;
                }
                outStream += "      <circle cx=\""
                        + String.format("%.2f", ellipse.get(i).x + (ellipse.get(i).width / 2))
                        + "\" cy=\""
                        + String.format("%.2f", ellipse.get(i).y + (ellipse.get(i).width / 2))
                        + "\" r=\"" + String.format("%.2f", ellipse.get(i).width / 2)
                        + "\" fill=\"#" + nowColour + "\" />\n";
            }

            // Hexagons
            for(i = 0; i < hexagon.size(); i++) {
                outStream += "      <path d=\"";
                for(j = 0; j < 6; j++) {
                    if (j == 0) {
                        outStream += "M ";
                    } else {
                        outStream += "L ";
                    }
                    outStream += String.format("%.2f", hexagon.get(i).pointX[j]) + " "
                            + String.format("%.2f", hexagon.get(i).pointY[j]) + " ";
                }
                outStream += "Z\" />\n";
            }

            // Footer
            outStream += "   </g>\n";
            outStream += "</svg>\n";

            // Output data to file
            for (i = 0; i < outStream.length(); i++) {
                fos.write(outStream.charAt(i));
            }
        }
    }
}
