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

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.font.TextAttribute;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;

import uk.org.okapibarcode.backend.Hexagon;
import uk.org.okapibarcode.backend.Symbol;
import uk.org.okapibarcode.backend.TextBox;

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

    /**
     * Creates a new Java 2D renderer.
     *
     * @param g2d the graphics to render to
     * @param magnification the magnification factor to apply
     * @param paper the paper (background) color
     * @param ink the ink (foreground) color
     */
    public Java2DRenderer(Graphics2D g2d, double magnification, Color paper, Color ink) {
        this.g2d = g2d;
        this.magnification = magnification;
        this.paper = paper;
        this.ink = ink;
    }

    /** {@inheritDoc} */
    @Override
    public void render(Symbol symbol) {

        g2d.setBackground(paper);
        g2d.clearRect(0, 0, symbol.getWidth(), symbol.getHeight());

        int marginX = (int) (symbol.getQuietZoneHorizontal() * magnification);
        int marginY = (int) (symbol.getQuietZoneVertical() * magnification);

        Map< TextAttribute, Object > attributes = new HashMap<>();
        attributes.put(TextAttribute.TRACKING, 0);
        Font f = new Font(symbol.getFontName(), Font.PLAIN, (int) (symbol.getFontSize() * magnification)).deriveFont(attributes);

        Font oldFont = g2d.getFont();
        Color oldColor = g2d.getColor();

        g2d.setFont(f);
        g2d.setColor(ink);

        for (Rectangle2D.Double rect : symbol.getRectangles()) {
            double x = (rect.x * magnification) + marginX;
            double y = (rect.y * magnification) + marginY;
            double w = rect.width * magnification;
            double h = rect.height * magnification;
            g2d.fill(new Rectangle((int) x, (int) y, (int) w, (int) h));
        }

        FontMetrics fm = g2d.getFontMetrics();
        for (TextBox text : symbol.getTexts()) {
            Rectangle2D bounds = fm.getStringBounds(text.text, g2d);
            float x;
            switch (symbol.getHumanReadableAlignment()) {
                case LEFT:
                    x = (float) ((magnification * text.x) + marginX);
                    break;
                case RIGHT:
                    x = (float) ((magnification * text.x) + (magnification * text.width) - bounds.getWidth() + marginX);
                    break;
                case CENTER:
                    x = (float) ((magnification * text.x) + (magnification * text.width / 2) - (bounds.getWidth() / 2) + marginX);
                    break;
                default:
                    throw new IllegalStateException("Unknown alignment: " + symbol.getHumanReadableAlignment());
            }
            float y = (float) (text.y * magnification) + marginY;
            g2d.drawString(text.text, x, y);
        }

        for (Hexagon hexagon : symbol.getHexagons()) {
            Polygon polygon = new Polygon();
            for (int j = 0; j < 6; j++) {
                polygon.addPoint((int) ((hexagon.pointX[j] * magnification) + marginX),
                        (int) ((hexagon.pointY[j] * magnification) + marginY));
            }
            g2d.fill(polygon);
        }

        for (int i = 0; i < symbol.getTarget().size(); i++) {
            Ellipse2D.Double ellipse = symbol.getTarget().get(i);
            double x = (ellipse.x * magnification) + marginX;
            double y = (ellipse.y * magnification) + marginY;
            double w = (ellipse.width * magnification) + marginX;
            double h = (ellipse.height * magnification) + marginY;
            if ((i & 1) == 0) {
                g2d.setColor(ink);
            } else {
                g2d.setColor(paper);
            }
            g2d.fill(new Ellipse2D.Double(x, y, w, h));
        }

        g2d.setFont(oldFont);
        g2d.setColor(oldColor);
    }
}
