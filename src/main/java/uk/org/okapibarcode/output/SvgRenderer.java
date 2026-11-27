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

import static java.nio.charset.StandardCharsets.UTF_8;
import static uk.org.okapibarcode.graphics.TextAlignment.CENTER;
import static uk.org.okapibarcode.graphics.TextAlignment.JUSTIFY;
import static uk.org.okapibarcode.util.Integers.normalizeRotation;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Objects;

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
public final class SvgRenderer implements SymbolRenderer {

    /** Hex digits, for hex string conversion. */
    private static final char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();

    /** The writer to render to. */
    private final Writer writer;

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
     * @param writer the writer to render to
     * @param magnification the magnification factor to apply
     * @param paper the paper (background) color
     * @param ink the ink (foreground) color
     * @param xmlProlog whether or not to include the XML prolog in the output (usually {@code true} for
     *        standalone SVG documents, {@code false} for SVG content embedded directly in HTML documents)
     */
    public SvgRenderer(Writer writer, double magnification, Color paper, Color ink, boolean xmlProlog) {
        this(writer, magnification, paper, ink, xmlProlog, 0);
    }

    /**
     * Creates a new SVG renderer.
     *
     * @param writer the writer to render to
     * @param magnification the magnification factor to apply
     * @param paper the paper (background) color
     * @param ink the ink (foreground) color
     * @param xmlProlog whether or not to include the XML prolog in the output (usually {@code true} for
     *        standalone SVG documents, {@code false} for SVG content embedded directly in HTML documents)
     * @param rotation the clockwise rotation of the symbol in degrees (must be a multiple of 90)
     */
    public SvgRenderer(Writer writer, double magnification, Color paper, Color ink, boolean xmlProlog, int rotation) {
        this.writer = Objects.requireNonNull(writer);
        this.magnification = magnification;
        this.paper = Objects.requireNonNull(paper);
        this.ink = Objects.requireNonNull(ink);
        this.xmlProlog = xmlProlog;
        this.rotation = normalizeRotation(rotation);
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
        this(new BufferedWriter(new OutputStreamWriter(Objects.requireNonNull(out), UTF_8), 512),
             magnification, paper, ink, xmlProlog, rotation);
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

        String fgColour = hex(ink);
        String bgColour = hex(paper);

        try (CustomWriter writer = new CustomWriter(this.writer)) {

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
            writer.append("<svg width=\"").append(rotatedWidth)
                  .append("\" height=\"").append(rotatedHeight)
                  .append("\" version=\"1.1")
                  .append("\" xmlns=\"http://www.w3.org/2000/svg\">\n");
            writer.append("   <desc>").append(clean(title)).append("</desc>\n");
            writer.append("   <g id=\"barcode\" fill=\"#").append(fgColour).append("\"").append(transform).append(">\n");
            writer.append("      <rect x=\"0\" y=\"0\" width=\"").append(width)
                  .append("\" height=\"").append(height)
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
     * Cleans / sanitizes the specified string for inclusion in XML.
     *
     * @param s the string to be cleaned / sanitized
     * @return the cleaned / sanitized string
     */
    protected CharSequence clean(String s) {
        int len = s.length();
        StringBuilder output = new StringBuilder(len);
        for (int i = 0 ; i < len;) {
            char c = s.charAt(i++);
            if (c == '&') {
                output.append("&amp;");
            } else if (c == '<') {
                output.append("&lt;");
            } else if (c == '>') {
                output.append("&gt;");
            } else if (c == '"') {
                output.append("&quot;");
            } else if (Character.isHighSurrogate(c)) {
                if (i < len) { // exclude dangling high surrogates
                    char low = s.charAt(i++);
                    if (Character.isLowSurrogate(low)) { // exclude high surrogates without a low surrogate
                        output.append("&#");
                        output.append(Character.toCodePoint(c, low));
                        output.append(';');
                    } else {
                        i--; // go back so that "low" can be checked in isolation
                    }
                }
            } else if ((c > '\u001F') &&                 // exclude control chars U+0000 through U+001F
                       (c < '\u007F' || c > '\u009F') && // exclude control chars U+007F through U+009F
                       (c < '\uFFFE') &&                 // exclude non-characters U+FFFE and U+FFFF
                       (!Character.isLowSurrogate(c))) { // exclude dangling low surrogates
                output.append(c);
            }
        }
        return output;
    }

    private static String hex(Color color) {
        return new String(new char[] {
            HEX_DIGITS[color.red >>> 4], HEX_DIGITS[color.red & 0x0F],
            HEX_DIGITS[color.green >>> 4], HEX_DIGITS[color.green & 0x0F],
            HEX_DIGITS[color.blue >>> 4], HEX_DIGITS[color.blue & 0x0F]
        });
    }
}
