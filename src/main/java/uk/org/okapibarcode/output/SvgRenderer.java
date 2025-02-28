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

import static uk.org.okapibarcode.graphics.TextAlignment.CENTER;
import static uk.org.okapibarcode.graphics.TextAlignment.JUSTIFY;
import static uk.org.okapibarcode.util.Integers.normalizeRotation;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.Objects;

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

import uk.org.okapibarcode.backend.OkapiInternalException;
import uk.org.okapibarcode.backend.Symbol;
import uk.org.okapibarcode.graphics.Circle;
import uk.org.okapibarcode.graphics.Color;
import uk.org.okapibarcode.graphics.Hexagon;
import uk.org.okapibarcode.graphics.Rectangle;
import uk.org.okapibarcode.graphics.TextAlignment;
import uk.org.okapibarcode.graphics.TextBox;

/**
 * Renders symbologies to SVG (Scalable Vector Graphics).
 *
 * @author <a href="mailto:rstuart114@gmail.com">Robin Stuart</a>
 * @author Daniel Gredler
 */
public class SvgRenderer implements SymbolRenderer {

    /** The output stream to render to. */
    private final OutputStream out;

    /** The magnification factor to apply. */
    private final double magnification;

    /** The paper (background) color. */
    private final Color paper;

    /** The ink (foreground) color. */
    private final Color ink;

    /** Whether or not to include the XML prolog in the output. */
    private final boolean xmlProlog;

    /** The clockwise rotation of the symbol in degrees. */
    private final int rotation;

    /**
     * Creates a new SVG renderer.
     *
     * @param out the output stream to render to
     * @param magnification the magnification factor to apply
     * @param paper the paper (background) color
     * @param ink the ink (foreground) color
     * @param xmlProlog whether or not to include the XML prolog in the output (usually {@code true} for
     *        standalone SVG documents, {@code false} for SVG content embedded directly in HTML documents)
     */
    public SvgRenderer(OutputStream out, double magnification, Color paper, Color ink, boolean xmlProlog) {
        this(out, magnification, paper, ink, xmlProlog, 0);
    }

    /**
     * Creates a new SVG renderer.
     *
     * @param out the output stream to render to
     * @param magnification the magnification factor to apply
     * @param paper the paper (background) color
     * @param ink the ink (foreground) color
     * @param xmlProlog whether or not to include the XML prolog in the output (usually {@code true} for
     *        standalone SVG documents, {@code false} for SVG content embedded directly in HTML documents)
     * @param rotation the clockwise rotation of the symbol in degrees (must be a multiple of 90)
     */
    public SvgRenderer(OutputStream out, double magnification, Color paper, Color ink, boolean xmlProlog, int rotation) {
        this.out = Objects.requireNonNull(out);
        this.magnification = magnification;
        this.paper = Objects.requireNonNull(paper);
        this.ink = Objects.requireNonNull(ink);
        this.xmlProlog = xmlProlog;
        this.rotation = normalizeRotation(rotation);
    }

    /** {@inheritDoc} */
    @Override
    public void render(Symbol symbol) throws IOException {

        String content = symbol.getContent();
        int width = (int) Math.ceil(symbol.getWidth() * magnification);
        int height = (int) Math.ceil(symbol.getHeight() * magnification);
        int marginX = (int) (symbol.getQuietZoneHorizontal() * magnification);
        int marginY = (int) (symbol.getQuietZoneVertical() * magnification);

        String title;
        if (content.isEmpty()) {
            title = "OkapiBarcode Generated Symbol";
        } else {
            title = content;
        }

        String fgColour = String.format("%02X", ink.red)
                        + String.format("%02X", ink.green)
                        + String.format("%02X", ink.blue);

        String bgColour = String.format("%02X", paper.red)
                        + String.format("%02X", paper.green)
                        + String.format("%02X", paper.blue);

        try (ExtendedOutputStreamWriter writer = new ExtendedOutputStreamWriter(out, "%.2f")) {

            // XML Prolog
            if(xmlProlog) {
                writer.append("<?xml version=\"1.0\" standalone=\"no\"?>\n");
                writer.append("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\"\n");
                writer.append("   \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n");
            }

            // Rotation
            int rotatedHeight = height;
            int rotatedWidth = width;
            String transform;
            switch (rotation) {
                case 90:
                    rotatedHeight = width;
                    rotatedWidth = height;
                    transform = " transform=\"rotate(" + rotation + ") translate(0,-" + rotatedWidth + ")\"";
                    break;
                case 180:
                    transform = " transform=\"rotate(" + rotation + "," + (width / 2) + "," + (height / 2) + ")\"";
                    break;
                case 270:
                    rotatedHeight = width;
                    rotatedWidth = height;
                    transform = " transform=\"rotate(" + rotation + ") translate(-" + rotatedHeight + ",0)\"";
                    break;
                default:
                    transform = "";
                    break;
            }

            // Header
            writer.append("<svg width=\"").appendInt(rotatedWidth)
                  .append("\" height=\"").appendInt(rotatedHeight)
                  .append("\" version=\"1.1")
                  .append("\" xmlns=\"http://www.w3.org/2000/svg\">\n");
            writer.append("   <desc>").append(clean(title)).append("</desc>\n");
            writer.append("   <g id=\"barcode\" fill=\"#").append(fgColour).append("\"").append(transform).append(">\n");
            writer.append("      <rect x=\"0\" y=\"0\" width=\"").appendInt(width)
                  .append("\" height=\"").appendInt(height)
                  .append("\" fill=\"#").append(bgColour).append("\" />\n");

            // Rectangles
            for (Rectangle rect : symbol.getRectangles()) {
                writer.append("      <rect x=\"").append((rect.x * magnification) + marginX)
                      .append("\" y=\"").append((rect.y * magnification) + marginY)
                      .append("\" width=\"").append(rect.width * magnification)
                      .append("\" height=\"").append(rect.height * magnification)
                      .append("\" />\n");
            }

            // Text
            for (TextBox text : symbol.getTexts()) {
                TextAlignment alignment = (text.alignment == JUSTIFY && text.text.length() == 1 ? CENTER : text.alignment);
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
                        throw new OkapiInternalException("Unknown alignment: " + alignment);
                }
                writer.append("      <text x=\"").append(x)
                      .append("\" y=\"").append((text.y * magnification) + marginY)
                      .append("\" text-anchor=\"").append(anchor).append("\"\n");
                if (alignment == JUSTIFY) {
                    writer.append("         textLength=\"")
                          .append(text.width * magnification)
                          .append("\" lengthAdjust=\"spacing\"\n");
                }
                writer.append("         font-family=\"").append(clean(symbol.getFontName()))
                      .append("\" font-size=\"").append(symbol.getFontSize() * magnification)
                      .append("\" fill=\"#").append(fgColour).append("\">\n");
                writer.append("         ").append(clean(text.text)).append("\n");
                writer.append("      </text>\n");
            }

            // Circles
            for (int i = 0; i < symbol.getTarget().size(); i++) {
                Circle circle = symbol.getTarget().get(i);
                String color;
                if ((i & 1) == 0) {
                    color = fgColour;
                } else {
                    color = bgColour;
                }
                writer.append("      <circle cx=\"").append((circle.centreX * magnification) + marginX)
                      .append("\" cy=\"").append((circle.centreY * magnification) + marginY)
                      .append("\" r=\"").append(circle.radius * magnification)
                      .append("\" fill=\"#").append(color).append("\" />\n");
            }

            // Hexagons
            for (Hexagon hexagon : symbol.getHexagons()) {
                writer.append("      <path d=\"");
                for (int j = 0; j < 6; j++) {
                    if (j == 0) {
                        writer.append("M ");
                    } else {
                        writer.append("L ");
                    }
                    writer.append((hexagon.getX(j) * magnification) + marginX).append(" ")
                          .append((hexagon.getY(j) * magnification) + marginY).append(" ");
                }
                writer.append("Z\" />\n");
            }

            // Footer
            writer.append("   </g>\n");
            writer.append("</svg>\n");
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
}
