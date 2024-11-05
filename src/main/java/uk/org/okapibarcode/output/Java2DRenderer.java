/*
 * Copyright 2014-2015 Robin Stuart, Robert Elliott, Daniel Gredler
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

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.List;

import uk.org.okapibarcode.backend.OkapiInternalException;
import uk.org.okapibarcode.backend.Symbol;
import uk.org.okapibarcode.graphics.Circle;
import uk.org.okapibarcode.graphics.Color;
import uk.org.okapibarcode.graphics.Hexagon;
import uk.org.okapibarcode.graphics.Rectangle;
import uk.org.okapibarcode.graphics.TextAlignment;
import uk.org.okapibarcode.graphics.TextBox;

/**
 * Renders symbologies using the Java 2D API.
 */
public class Java2DRenderer implements SymbolRenderer {

    /** The graphics to render to. */
    private final Graphics2D g2d;

    /** The magnification factor to apply. */
    private final double magnification;

    /** The paper (background) color. */
    private final Color paper;

    /** The ink (foreground) color. */
    private final Color ink;

    /** The clockwise rotation of the symbol in degrees (0, 90, 180, or 270). */
    private final int rotation;

    /**
     * Creates a new Java 2D renderer. If the specified paper color is {@code null}, the symbol is drawn without clearing the
     * existing {@code g2d} background.
     *
     * @param g2d the graphics to render to
     * @param magnification the magnification factor to apply
     * @param paper the paper (background) color (may be {@code null})
     * @param ink the ink (foreground) color
     */
    public Java2DRenderer(Graphics2D g2d, double magnification, Color paper, Color ink) {
        this(g2d, magnification, paper, ink, 0);
    }

    /**
     * Creates a new Java 2D renderer. If the specified paper color is {@code null}, the symbol is drawn without clearing the
     * existing {@code g2d} background.
     *
     * @param g2d the graphics to render to
     * @param magnification the magnification factor to apply
     * @param paper the paper (background) color (may be {@code null})
     * @param ink the ink (foreground) color
     * @param rotation the clockwise rotation of the symbol in degrees (0, 90, 180, or 270)
     */
    public Java2DRenderer(Graphics2D g2d, double magnification, Color paper, Color ink, int rotation) {
        this.g2d = g2d;
        this.magnification = magnification;
        this.paper = paper;
        this.ink = ink;
        this.rotation = SymbolRenderer.normalizeRotation(rotation);
    }

    /** {@inheritDoc} */
    @Override
    public void render(Symbol symbol) {

        int width = (int) (symbol.getWidth() * magnification);
        int height = (int) (symbol.getHeight() * magnification);
        int marginX = (int) (symbol.getQuietZoneHorizontal() * magnification);
        int marginY = (int) (symbol.getQuietZoneVertical() * magnification);
        int rotateHeight = height;
        int rotateWidth = width;

        // render rotation clockwise
        AffineTransform oldTransform = null;
        if (rotation != 0) {
            oldTransform = g2d.getTransform();
            switch (rotation) {
                case 90:
                    rotateHeight = width;
                    rotateWidth = height;
                    g2d.rotate(Math.PI / 2d);
                    g2d.translate(0, -rotateWidth);
                    break;
                case 180:
                    g2d.rotate(Math.PI);
                    g2d.translate(-width, -height);
                    break;
                case 270:
                    rotateHeight = width;
                    rotateWidth = height;
                    g2d.rotate(Math.PI * 1.5d);
                    g2d.translate(-rotateHeight, 0);
                    break;
            }
        }

        Font f = symbol.getFont();
        if (f != null) {
            f = f.deriveFont((float) (f.getSize2D() * magnification));
        } else {
            f = new Font(symbol.getFontName(), Font.PLAIN, (int) (symbol.getFontSize() * magnification));
            f = f.deriveFont(Collections.singletonMap(TextAttribute.TRACKING, 0));
        }

        Font oldFont = g2d.getFont();
        java.awt.Color oldColor = g2d.getColor();

        if (paper != null) {
            g2d.setColor(new java.awt.Color(paper.red, paper.green, paper.blue));
            g2d.fillRect(0, 0, width, height);
        }

        g2d.setColor(new java.awt.Color(ink.red, ink.green, ink.blue));

        for (Rectangle rect : symbol.getRectangles()) {
            double x = (rect.x * magnification) + marginX;
            double y = (rect.y * magnification) + marginY;
            double w = rect.width * magnification;
            double h = rect.height * magnification;
            g2d.fillRect((int) x, (int) y, (int) w, (int) h);
        }

        for (TextBox text : symbol.getTexts()) {
            TextAlignment alignment = (text.alignment == JUSTIFY && text.text.length() == 1 ? CENTER : text.alignment);
            Font font = (alignment != JUSTIFY ? f : addTracking(f, text.width * magnification, text.text, g2d));
            g2d.setFont(font);
            float y = (float) (text.y * magnification) + marginY;
            float x;
            switch (alignment) {
                case LEFT:
                case JUSTIFY:
                    x = (float) ((magnification * text.x) + marginX);
                    break;
                case RIGHT:
                    x = (float) ((magnification * text.x) + (magnification * text.width) - getBounds(text, g2d).getWidth() + marginX);
                    break;
                case CENTER:
                    x = (float) ((magnification * text.x) + (magnification * text.width / 2) - (getBounds(text, g2d).getWidth() / 2) + marginX);
                    break;
                default:
                    throw new OkapiInternalException("Unknown alignment: " + alignment);
            }
            g2d.drawString(text.text, x, y);
        }

        if (!symbol.getHexagons().isEmpty()) {
            Polygon polygon = new Polygon();
            for (Hexagon hexagon : symbol.getHexagons()) {
                for (int j = 0; j < 6; j++) {
                    polygon.addPoint((int) ((hexagon.getX(j) * magnification) + marginX),
                                     (int) ((hexagon.getY(j) * magnification) + marginY));
                }
                g2d.fill(polygon);
                polygon.reset();
            }
        }

        List< Circle > target = symbol.getTarget();
        for (int i = 0; i + 1 < target.size(); i += 2) {
            Ellipse2D.Double outer = adjust(target.get(i), magnification, marginX, marginY);
            Ellipse2D.Double inner = adjust(target.get(i + 1), magnification, marginX, marginY);
            Area area = new Area(outer);
            area.subtract(new Area(inner));
            g2d.fill(area);
        }

        g2d.setFont(oldFont);
        g2d.setColor(oldColor);
        if (oldTransform != null) {
            g2d.setTransform(oldTransform);
        }
    }

    private static Rectangle2D getBounds(TextBox text, Graphics2D g2d) {
        FontMetrics fm = g2d.getFontMetrics();
        return fm.getStringBounds(text.text, g2d);
    }

    private static Ellipse2D.Double adjust(Circle circle, double magnification, int marginX, int marginY) {
        double x = marginX + ((circle.centreX - circle.radius) * magnification);
        double y = marginY + ((circle.centreY - circle.radius) * magnification);
        double w = 2d * circle.radius * magnification;
        double h = 2d * circle.radius * magnification;
        return new Ellipse2D.Double(x, y, w, h);
    }

    private static Font addTracking(Font baseFont, double maxTextWidth, String text, Graphics2D g2d) {
        FontRenderContext frc = g2d.getFontRenderContext();
        double originalWidth = baseFont.getStringBounds(text, frc).getWidth();
        double extraSpace = maxTextWidth - originalWidth;
        double extraSpacePerGap = extraSpace / (text.length() - 1);
        double scaleX = (baseFont.isTransformed() ? baseFont.getTransform().getScaleX() : 1);
        double tracking = extraSpacePerGap / (baseFont.getSize2D() * scaleX);
        return baseFont.deriveFont(Collections.singletonMap(TextAttribute.TRACKING, tracking));
    }
}
