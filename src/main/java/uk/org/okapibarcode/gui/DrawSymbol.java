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
 * Draw barcode symbol in top panel
 *
 * @author Robert Elliott <jakel2006@me.com>
 * @author Robin Stuart <rstuart114@gmail.com>
 */
public class DrawSymbol extends JPanel{

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(OkapiUI.width * OkapiUI.factor,
                OkapiUI.height * OkapiUI.factor);
    }

    @Override
    protected void paintComponent(Graphics g) {
        int i, j;
        double x, y, h, w;
        this.setBackground(OkapiUI.paperColour);
        super.paintComponent(g);
        Map<TextAttribute, Object> attributes = new HashMap<>();
        attributes.put(TextAttribute.TRACKING, 0);
        Font f = new Font("Arial", Font.PLAIN, 10 * (int) (OkapiUI.factor * 0.9)).deriveFont(attributes);
        Polygon polygon;

        Graphics2D g2 = (Graphics2D) g;

        for (i = 0; i != OkapiUI.bcs.size(); i++) {
            x = OkapiUI.bcs.get(i).x * OkapiUI.factor;
            y = OkapiUI.bcs.get(i).y * OkapiUI.factor;
            w = OkapiUI.bcs.get(i).width * OkapiUI.factor;
            h = OkapiUI.bcs.get(i).height * OkapiUI.factor;
            g2.setColor(OkapiUI.inkColour);
            g2.fill(new Rectangle((int) x, (int) y, (int) w, (int) h));
        }

        for (i = 0; i < OkapiUI.txt.size(); i++) {
            g2.setFont(f);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.drawString(OkapiUI.txt.get(i).arg,
                    (float) OkapiUI.txt.get(i).xPos * OkapiUI.factor,
                    (float) OkapiUI.txt.get(i).yPos * OkapiUI.factor);
        }

        for (i = 0; i < OkapiUI.hex.size(); i++) {
            polygon = new Polygon();
            for (j = 0; j < 6; j++) {
                polygon.addPoint((int) (OkapiUI.hex.get(i).pointX[j] * OkapiUI.factor),
                        (int) (OkapiUI.hex.get(i).pointY[j] * OkapiUI.factor));
            }
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(OkapiUI.inkColour);
            g2.fill(polygon);
        }

        for (i = 0; i < OkapiUI.target.size(); i++) {
            x = OkapiUI.target.get(i).x * OkapiUI.factor;
            y = OkapiUI.target.get(i).y * OkapiUI.factor;
            w = OkapiUI.target.get(i).width * OkapiUI.factor;
            h = OkapiUI.target.get(i).height * OkapiUI.factor;
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
