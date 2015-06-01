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
package uk.org.okapibarcode.gui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.TextAttribute;
import java.awt.geom.Ellipse2D;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

/**
 * Draw symbol in "invisible" panel for saving to file
 *
 * @author <a href="mailto:rstuart114@gmail.com">Robin Stuart</a>
 */
public class SaveSymbol extends JPanel{
    private int magnification = 1; // Magnification doesn't change depending on symbol size
    private int borderSize = 5 * magnification; // Add whitespace when saving to file
    Polygon polygon;

    public void setMagnification(int factor) {
        magnification = factor;
    }
    
    public void setBorderSize(int borderWidth) {
        borderSize = borderWidth;
    }
    
    @Override
    public Dimension getPreferredSize() {
        return new Dimension((OkapiUI.symbol.getWidth() * magnification) + (2 * borderSize),
                (OkapiUI.symbol.getHeight() * magnification) + (2 * borderSize));
    }

    @Override
    protected void paintComponent(Graphics g) {
        int i, j;
        double x, y, h, w;
        this.setBackground(OkapiUI.paperColour);
        super.paintComponent(g);
        Map<TextAttribute, Object> attributes = new HashMap<>();
        attributes.put(TextAttribute.TRACKING, 0);
        Font f = new Font("Arial", Font.PLAIN, 10 * (int) (magnification * 0.9)).deriveFont(attributes);

        Graphics2D g2 = (Graphics2D) g;

        // Rectangles
        for (i = 0; i != OkapiUI.symbol.rect.size(); i++) {
            x = (OkapiUI.symbol.rect.get(i).x * magnification) + borderSize;
            y = (OkapiUI.symbol.rect.get(i).y * magnification) + borderSize;
            w = OkapiUI.symbol.rect.get(i).width * magnification;
            h = OkapiUI.symbol.rect.get(i).height * magnification;
            g2.setColor(OkapiUI.inkColour);
            g2.fill(new Rectangle((int) x, (int) y, (int) w, (int) h));
        }

        // Text
        for (i = 0; i < OkapiUI.symbol.txt.size(); i++) {
            g2.setFont(f);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.drawString(OkapiUI.symbol.txt.get(i).text,
                    (float) (OkapiUI.symbol.txt.get(i).x * magnification) + borderSize,
                    (float) (OkapiUI.symbol.txt.get(i).y * magnification) + borderSize);
        }

        // Hexagons
        for (i = 0; i < OkapiUI.symbol.hex.size(); i++) {
            polygon = new Polygon();
            for (j = 0; j < 6; j++) {
                polygon.addPoint((int) (OkapiUI.symbol.hex.get(i).pointX[j] * magnification) + borderSize,
                        (int) (OkapiUI.symbol.hex.get(i).pointY[j] * magnification) + borderSize);
            }
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(OkapiUI.inkColour);
            g2.fill(polygon);
        }

        // Circles
        for (i = 0; i < OkapiUI.symbol.target.size(); i++) {
            x = (OkapiUI.symbol.target.get(i).x * magnification) + borderSize;
            y = (OkapiUI.symbol.target.get(i).y * magnification) + borderSize;
            w = OkapiUI.symbol.target.get(i).width * magnification;
            h = OkapiUI.symbol.target.get(i).height * magnification;
            if ((i & 1) == 0) {
                g2.setColor(OkapiUI.inkColour);
            } else {
                g2.setColor(OkapiUI.paperColour);
            }
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.fill(new Ellipse2D.Double(x, y, w, h));
        }
    }
}
