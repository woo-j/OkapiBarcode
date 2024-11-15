/*
 * Copyright 2015 Robin Stuart, Daniel Gredler
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

import static uk.org.okapibarcode.graphics.TextAlignment.CENTER;
import static uk.org.okapibarcode.graphics.TextAlignment.JUSTIFY;
import static uk.org.okapibarcode.util.Doubles.roughlyEqual;

import java.io.IOException;
import java.io.OutputStream;

import uk.org.okapibarcode.backend.OkapiInternalException;
import uk.org.okapibarcode.backend.Symbol;
import uk.org.okapibarcode.graphics.Circle;
import uk.org.okapibarcode.graphics.Color;
import uk.org.okapibarcode.graphics.Hexagon;
import uk.org.okapibarcode.graphics.Rectangle;
import uk.org.okapibarcode.graphics.TextAlignment;
import uk.org.okapibarcode.graphics.TextBox;

/**
 * Renders symbologies to EPS (Encapsulated PostScript).
 *
 * @author <a href="mailto:rstuart114@gmail.com">Robin Stuart</a>
 * @author Daniel Gredler
 */
public class PostScriptRenderer implements SymbolRenderer {

    /** The output stream to render to. */
    private final OutputStream out;

    /** The magnification factor to apply. */
    private final double magnification;

    /** The paper (background) color. */
    private final Color paper;

    /** The ink (foreground) color. */
    private final Color ink;

    /** The clockwise rotation of the symbol in degrees. */
    private final int rotation;

    /**
     * Creates a new PostScript renderer.
     *
     * @param out the output stream to render to
     * @param magnification the magnification factor to apply
     * @param paper the paper (background) color
     * @param ink the ink (foreground) color
     */
    public PostScriptRenderer(OutputStream out, double magnification, Color paper, Color ink) {
        this(out, magnification, paper, ink, 0);
    }

    /**
     * Creates a new PostScript renderer.
     *
     * @param out the output stream to render to
     * @param magnification the magnification factor to apply
     * @param paper the paper (background) color
     * @param ink the ink (foreground) color
     * @param rotation the clockwise rotation of the symbol in degrees (0, 90, 180, or 270)
     */
    public PostScriptRenderer(OutputStream out, double magnification, Color paper, Color ink, int rotation) {
        this.out = out;
        this.magnification = magnification;
        this.paper = paper;
        this.ink = ink;
        this.rotation = SymbolRenderer.normalizeRotation(rotation);
    }

    /** {@inheritDoc} */
    @Override
    public void render(Symbol symbol) throws IOException {

        // All y dimensions are reversed because EPS origin (0,0) is at the bottom left, not top left

        String content = symbol.getContent();
        int width = (int) Math.ceil(symbol.getWidth() * magnification);
        int height = (int) Math.ceil(symbol.getHeight() * magnification);
        int marginX = (int) (symbol.getQuietZoneHorizontal() * magnification);
        int marginY = (int) (symbol.getQuietZoneVertical() * magnification);

         // render rotation clockwise
         int rotateHeight = height;
         int rotateWidth = width;
         String transform;
         switch (rotation) {
             case 90:
             case 270:
                 rotateHeight = width;
                 rotateWidth = height;
                 break;
             default:
                 break;
         }

        String title;
        if (content.isEmpty()) {
            title = "OkapiBarcode Generated Symbol";
        } else {
            title = content;
        }

        try (ExtendedOutputStreamWriter writer = new ExtendedOutputStreamWriter(out, "%.2f")) {

            // Header
            writer.append("%!PS-Adobe-3.0 EPSF-3.0\n");
            writer.append("%%Creator: OkapiBarcode\n");
            writer.append("%%Title: ").append(title).append('\n');
            writer.append("%%Pages: 0\n");
            writer.append("%%BoundingBox: 0 0 ").appendInt(rotateWidth).append(" ").appendInt(rotateHeight).append("\n");
            writer.append("%%EndComments\n");

            // Definitions
            writer.append("/TL { setlinewidth moveto lineto stroke } bind def\n");
            writer.append("/TC { moveto 0 360 arc 360 0 arcn fill } bind def\n");
            writer.append("/TH { 0 setlinewidth moveto lineto lineto lineto lineto lineto closepath fill } bind def\n");
            writer.append("/TB { 2 copy } bind def\n");
            writer.append("/TR { newpath 4 1 roll exch moveto 1 index 0 rlineto 0 exch rlineto neg 0 rlineto closepath fill } bind def\n");
            writer.append("/TE { pop pop } bind def\n");

            // Set orientation
            switch (rotation) {
                case 90:
                    writer.append("gsave\n")
                          .append(-rotation).append(" rotate\n")
                          .append(-width).append(" 0.00 translate\n");
                    break;
                case 180:
                    writer.append("gsave\n")
                          .append(-rotation).append(" rotate\n")
                          .append(-width).append(" ").append(-height).append(" translate\n");
                    break;
                case 270:
                    writer.append("gsave\n")
                          .append(-rotation).append(" rotate\n")
                          .append("0.00 ").append(-height).append(" translate\n");
                    break;
                default:
                    break;
            }

            // Background
            writer.append("newpath\n");
            writer.append(ink.red / 255.0).append(" ")
                  .append(ink.green / 255.0).append(" ")
                  .append(ink.blue / 255.0).append(" setrgbcolor\n");
            writer.append(paper.red / 255.0).append(" ")
                  .append(paper.green / 255.0).append(" ")
                  .append(paper.blue / 255.0).append(" setrgbcolor\n");
            writer.append(height).append(" 0.00 TB 0.00 ").append(width).append(" TR\n");

            // Rectangles
            for (int i = 0; i < symbol.getRectangles().size(); i++) {
                Rectangle rect = symbol.getRectangles().get(i);
                if (i == 0) {
                    writer.append("TE\n");
                    writer.append(ink.red / 255.0).append(" ")
                          .append(ink.green / 255.0).append(" ")
                          .append(ink.blue / 255.0).append(" setrgbcolor\n");
                    writer.append(rect.height * magnification).append(" ")
                          .append(height - ((rect.y + rect.height) * magnification) - marginY).append(" TB ")
                          .append((rect.x * magnification) + marginX).append(" ")
                          .append(rect.width * magnification).append(" TR\n");
                } else {
                    Rectangle prev = symbol.getRectangles().get(i - 1);
                    if (!roughlyEqual(rect.height, prev.height) || !roughlyEqual(rect.y, prev.y)) {
                        writer.append("TE\n");
                        writer.append(ink.red / 255.0).append(" ")
                              .append(ink.green / 255.0).append(" ")
                              .append(ink.blue / 255.0).append(" setrgbcolor\n");
                        writer.append(rect.height * magnification).append(" ")
                              .append(height - ((rect.y + rect.height) * magnification) - marginY).append(" ");
                    }
                    writer.append("TB ").append((rect.x * magnification) + marginX).append(" ").append(rect.width * magnification).append(" TR\n");
                }
            }

            // Text
            for (int i = 0; i < symbol.getTexts().size(); i++) {
                TextBox text = symbol.getTexts().get(i);
                TextAlignment alignment = (text.alignment == JUSTIFY && text.text.length() == 1 ? CENTER : text.alignment);
                if (i == 0) {
                    writer.append("TE\n");
                    writer.append(ink.red / 255.0).append(" ")
                          .append(ink.green / 255.0).append(" ")
                          .append(ink.blue / 255.0).append(" setrgbcolor\n");
                }
                writer.append("matrix currentmatrix\n");
                writer.append("/").append(symbol.getFontName()).append(" findfont\n");
                writer.append(symbol.getFontSize() * magnification).append(" scalefont setfont\n");
                double y = height - (text.y * magnification) - marginY;
                switch (alignment) {
                    case LEFT:
                        double leftX = (magnification * text.x) + marginX;
                        writer.append(" 0 0 moveto ").append(leftX).append(" ").append(y)
                              .append(" translate 0.00 rotate 0 0 moveto\n");
                        writer.append(" (").append(text.text).append(") show\n");
                        break;
                    case JUSTIFY:
                        double textX = (magnification * text.x) + marginX;
                        double textW = (magnification * text.width);
                        writer.append(" 0 0 moveto ").append(textX).append(" ").append(y)
                              .append(" translate 0.00 rotate 0 0 moveto\n");
                        writer.append(" (").append(text.text).append(") dup stringwidth pop ")
                              .append(textW).append(" sub neg 1 index length 1 sub div 0")
                              .append(" 3 -1 roll ashow\n");
                        break;
                    case RIGHT:
                        double rightX = (magnification * text.x) + (magnification * text.width) + marginX;
                        writer.append(" 0 0 moveto ").append(rightX).append(" ").append(y)
                              .append(" translate 0.00 rotate 0 0 moveto\n");
                        writer.append(" (").append(text.text).append(") stringwidth\n");
                        writer.append("pop\n");
                        writer.append("-1 mul 0 rmoveto\n");
                        writer.append(" (").append(text.text).append(") show\n");
                        break;
                    case CENTER:
                        double centerX = (magnification * text.x) + (magnification * text.width / 2) + marginX;
                        writer.append(" 0 0 moveto ").append(centerX).append(" ").append(y)
                              .append(" translate 0.00 rotate 0 0 moveto\n");
                        writer.append(" (").append(text.text).append(") stringwidth\n");
                        writer.append("pop\n");
                        writer.append("-2 div 0 rmoveto\n");
                        writer.append(" (").append(text.text).append(") show\n");
                        break;
                    default:
                        throw new OkapiInternalException("Unknown alignment: " + alignment);
                }
                writer.append("setmatrix\n");
            }

            // Circles
            // Because MaxiCode size is fixed, this ignores magnification
            for (int i = 0; i < symbol.getTarget().size(); i += 2) {
                Circle circle1 = symbol.getTarget().get(i);
                Circle circle2 = symbol.getTarget().get(i + 1);
                if (i == 0) {
                    writer.append("TE\n");
                    writer.append(ink.red / 255.0).append(" ")
                          .append(ink.green / 255.0).append(" ")
                          .append(ink.blue / 255.0).append(" setrgbcolor\n");
                    writer.append(ink.red / 255.0).append(" ")
                          .append(ink.green / 255.0).append(" ")
                          .append(ink.blue / 255.0).append(" setrgbcolor\n");
                }
                double x1 = circle1.centreX;
                double x2 = circle2.centreX;
                double y1 = height - circle1.centreY;
                double y2 = height - circle2.centreY;
                double r1 = circle1.radius;
                double r2 = circle2.radius;
                writer.append(x1 + marginX)
                      .append(" ").append(y1 - marginY)
                      .append(" ").append(r1)
                      .append(" ").append(x2 + marginX)
                      .append(" ").append(y2 - marginY)
                      .append(" ").append(r2)
                      .append(" ").append(x2 + r2 + marginX)
                      .append(" ").append(y2 - marginY)
                      .append(" TC\n");
            }

            // Hexagons
            // Because MaxiCode size is fixed, this ignores magnification
            for (Hexagon hexagon : symbol.getHexagons()) {
                for (int j = 0; j < 6; j++) {
                    writer.append(hexagon.getX(j) + marginX).append(" ")
                          .append((height - hexagon.getY(j)) - marginY).append(" ");
                }
                writer.append(" TH\n");
            }

            // Restore original transformation if rotated
            if (rotation != 0) {
                writer.append("grestore\n");
            }

            // Footer
            writer.append("\nshowpage\n");
        }
    }
}
