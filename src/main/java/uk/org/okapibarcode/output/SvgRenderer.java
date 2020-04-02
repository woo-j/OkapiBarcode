/*
 * Copyright 2014-2015 Robin Stuart, Daniel Gredler
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

import static uk.org.okapibarcode.backend.HumanReadableAlignment.CENTER;
import static uk.org.okapibarcode.backend.HumanReadableAlignment.JUSTIFY;

import java.awt.Color;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Text;

import uk.org.okapibarcode.backend.Hexagon;
import uk.org.okapibarcode.backend.HumanReadableAlignment;
import uk.org.okapibarcode.backend.Symbol;
import uk.org.okapibarcode.backend.TextBox;

/**
 * Renders symbologies to SVG (Scalable Vector Graphics).
 *
 * @author <a href="mailto:rstuart114@gmail.com">Robin Stuart</a>
 * @author Daniel Gredler
 */
public class SvgRenderer implements SymbolRenderer {

    public enum ShapeRendering {
        AUTO("auto"),
        /**
         * Indicates that the user agent shall emphasize rendering speed over
         * geometric precision and crisp edges.
         */
        OPTIMIZE_SPEED("optimizeSpeed"),
        /**
         * Indicates that the user agent shall attempt to emphasize the contrast
         * between clean edges of artwork over rendering speed and geometric precision.
         */
        CRISP_EDGES("crispEdges"),
        /**
         * Indicates that the user agent shall emphasize geometric precision over speed
         * and crisp edges.
         */
        GEOMETRIC_PRECISION("geometricPrecision")
        ;

        private final String attr;

        ShapeRendering(String shareRendering) {
            this.attr = shareRendering;
        }
    }

    /** The output stream to render to. */
    protected final OutputStream out;

    /** The magnification factor to apply. */
    protected final double magnification;

    /** The paper (background) color. */
    protected final Color paper;

    /** The ink (foreground) color. */
    protected final Color ink;

    /** SVG rendering (experimental) */
    protected final ShapeRendering shapeRendering;

    public SvgRenderer(OutputStream out, double magnification, Color paper, Color ink) {
        this(out, magnification, paper, ink, null);
    }

    /**
     * Creates a new SVG renderer.
     *
     * @param magnification the magnification factor to apply
     * @param paper the paper (background) color
     * @param ink the ink (foreground) color
     * @param shapeRendering experimental rendering method
     */
    public SvgRenderer(OutputStream out, double magnification, Color paper, Color ink, ShapeRendering shapeRendering) {
        this.out = out;
        this.magnification = magnification;
        this.paper = paper;
        this.ink = ink;
        this.shapeRendering = shapeRendering;
    }

    /**
     * XML header, override when using inline SVG (HTML5)
     */
    protected void appendHeader(ExtendedOutputStreamWriter writer) throws IOException {
        writer.appendLine("<?xml version=\"1.0\" standalone=\"no\"?>");
        writer.append("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\"");
        writer.appendLine(" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">");
    }

    /** {@inheritDoc} */
    @Override
    public void render(Symbol symbol) throws IOException {

        String content = symbol.getContent();
        int width = (int) (symbol.getWidth() * magnification);
        int height = (int) (symbol.getHeight() * magnification);
        int marginX = (int) (symbol.getQuietZoneHorizontal() * magnification);
        int marginY = (int) (symbol.getQuietZoneVertical() * magnification);

        String title;
        if (content == null || content.isEmpty()) {
            title = "OkapiBarcode Generated Symbol";
        } else {
            title = content;
        }

        String fgColour = String.format("%02X", ink.getRed())
                        + String.format("%02X", ink.getGreen())
                        + String.format("%02X", ink.getBlue());

        String bgColour = String.format("%02X", paper.getRed())
                        + String.format("%02X", paper.getGreen())
                        + String.format("%02X", paper.getBlue());

        try (ExtendedOutputStreamWriter writer = createWriter()) {

            // Header
            appendHeader(writer);
            writer.append("<svg width=\"").appendInt(width)
                  .append("\" height=\"").appendInt(height)
                  .append("\" version=\"1.1\"");
            if (shapeRendering != null) {
                writer.append(" shape-rendering=\"").append(shapeRendering.attr).append("\"");
            }
            writer.appendLine(" xmlns=\"http://www.w3.org/2000/svg\">");
            writer.append("<desc>").append(clean(title)).appendLine("</desc>");
            writer.append("<g id=\"barcode\" fill=\"#").append(fgColour).appendLine("\">");
            writer.append("<rect x=\"0\" y=\"0\" width=\"").appendInt(width)
                  .append("\" height=\"").appendInt(height)
                  .append("\" fill=\"#").append(bgColour).appendLine("\" />");

            // Rectangles
            for (int i = 0; i < symbol.getRectangles().size(); i++) {
                Rectangle2D.Double rect = symbol.getRectangles().get(i);
                writer.append("<rect x=\"").append((rect.x * magnification) + marginX)
                      .append("\" y=\"").append((rect.y * magnification) + marginY)
                      .append("\" width=\"").append(rect.width * magnification)
                      .append("\" height=\"").append(rect.height * magnification)
                      .appendLine("\" />");
            }

            // Text
            for (int i = 0; i < symbol.getTexts().size(); i++) {
                TextBox text = symbol.getTexts().get(i);
                HumanReadableAlignment alignment = (text.alignment == JUSTIFY && text.text.length() == 1 ? CENTER : text.alignment);
                double x;
                String anchor;
                switch (alignment) {
                    case LEFT:
                    case JUSTIFY:
                        x = (magnification * text.x) + marginX;
                        anchor = "start";
                        break;
                    case RIGHT:
                        x = (magnification * text.x) + (magnification * text.width) + marginX;
                        anchor = "end";
                        break;
                    case CENTER:
                        x = (magnification * text.x) + (magnification * text.width / 2) + marginX;
                        anchor = "middle";
                        break;
                    default:
                        throw new IllegalStateException("Unknown alignment: " + alignment);
                }
                writer.append("<text x=\"").append(x)
                      .append("\" y=\"").append((text.y * magnification) + marginY)
                      .append("\" text-anchor=\"").append(anchor).append("\"");
                if (alignment == JUSTIFY) {
                    writer.append(" textLength=\"")
                          .append(text.width * magnification)
                          .append("\" lengthAdjust=\"spacing\"");
                }
                writer.append(" font-family=\"").append(clean(symbol.getFontName()))
                      .append("\" font-size=\"").append(symbol.getFontSize() * magnification)
                      .append("\" fill=\"#").append(fgColour).append("\">");
                writer.append(clean(text.text));
                writer.appendLine("</text>");
            }

            // Circles
            for (int i = 0; i < symbol.getTarget().size(); i++) {
                Ellipse2D.Double ellipse = symbol.getTarget().get(i);
                String color;
                if ((i & 1) == 0) {
                    color = fgColour;
                } else {
                    color = bgColour;
                }
                writer.append("<circle cx=\"").append(((ellipse.x + (ellipse.width / 2)) * magnification) + marginX)
                      .append("\" cy=\"").append(((ellipse.y + (ellipse.width / 2)) * magnification) + marginY)
                      .append("\" r=\"").append((ellipse.width / 2) * magnification)
                      .append("\" fill=\"#").append(color).appendLine("\" />");
            }

            // Hexagons
            for (int i = 0; i < symbol.getHexagons().size(); i++) {
                Hexagon hexagon = symbol.getHexagons().get(i);
                writer.append("<path d=\"");
                for (int j = 0; j < 6; j++) {
                    if (j == 0) {
                        writer.append("M ");
                    } else {
                        writer.append("L ");
                    }
                    writer.append((hexagon.pointX[j] * magnification) + marginX).append(" ")
                          .append((hexagon.pointY[j] * magnification) + marginY).append(" ");
                }
                writer.appendLine("Z\" />");
            }

            // Footer
            writer.appendLine("</g>");
            writer.appendLine("</svg>");
        }
    }

    /**
     * Cleans / sanitizes the specified string for inclusion in XML. A bit convoluted, but we're
     * trying to do it without adding an external dependency just for this...
     *
     * @param s the string to be cleaned / sanitized
     * @return the cleaned / sanitized string
     */
    protected String clean(String s) {

        // remove control characters
        s = s.replaceAll("[\u0000-\u001f]", "");

        // escape XML characters
        try {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Text text = document.createTextNode(s);
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            DOMSource source = new DOMSource(text);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.transform(source, result);
            return writer.toString();
        } catch (ParserConfigurationException | TransformerException | TransformerFactoryConfigurationError e) {
            return s;
        }
    }

    /**
     * @return outputStream extended writer
     */
    protected ExtendedOutputStreamWriter createWriter() {
        return new ExtendedOutputStreamWriter(out, "%.2f");
    }
}
