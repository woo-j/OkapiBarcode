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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

import uk.org.okapibarcode.graphics.Color;
import uk.org.okapibarcode.output.Java2DRenderer;

/**
 * Draw symbol in "invisible" panel for saving to file.
 *
 * @author <a href="mailto:rstuart114@gmail.com">Robin Stuart</a>
 */
public class SaveSymbol extends JPanel {

    /** Serial version UID. */
    private static final long serialVersionUID = 8948804628452979514L;

    /** {@inheritDoc} */
    @Override
    public Dimension getPreferredSize() {
        int w = OkapiUI.symbol.getWidth() * OkapiUI.factor;
        int h = OkapiUI.symbol.getHeight() * OkapiUI.factor;
        return new Dimension(w, h);
    }

    /** {@inheritDoc} */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color paper = new Color(OkapiUI.paperColour.getRGB());
        Color ink = new Color(OkapiUI.inkColour.getRGB());
        Java2DRenderer renderer = new Java2DRenderer(g2d, OkapiUI.factor, paper, ink);
        renderer.render(OkapiUI.symbol);
    }
}
