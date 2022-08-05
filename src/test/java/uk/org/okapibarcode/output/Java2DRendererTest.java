/*
 * Copyright 2018 Daniel Gredler
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
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collections;

import javax.imageio.ImageIO;

import org.junit.Test;

import uk.org.okapibarcode.backend.Code128;
import uk.org.okapibarcode.backend.DataMatrix;
import uk.org.okapibarcode.backend.HumanReadableAlignment;
import uk.org.okapibarcode.backend.MaxiCode;
import uk.org.okapibarcode.backend.SymbolTest;

/**
 * Tests for {@link Java2DRenderer}.
 */
public class Java2DRendererTest {

    @Test
    public void testPaperColor() throws Exception {

        Code128 code128 = new Code128();
        code128.setFontName(SymbolTest.DEJA_VU_SANS.getFontName());
        code128.setContent("123456");

        DataMatrix datamatrix = new DataMatrix();
        datamatrix.setContent("ABCDEFG");

        MaxiCode maxicode = new MaxiCode();
        maxicode.setMode(4);
        maxicode.setContent("ABCDEFG");

        int width = 750;
        int height = 650;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setPaint(new GradientPaint(0, 0, Color.ORANGE, width, height, Color.GREEN));
        g2d.fillRect(0, 0, width, height);

        Java2DRenderer renderer = new Java2DRenderer(g2d, 4, null, Color.BLACK, null);
        g2d.translate(25, 25);
        renderer.render(code128);
        g2d.translate(300, 0);
        renderer.render(datamatrix);
        g2d.translate(100, 0);
        renderer.render(maxicode);

        Java2DRenderer renderer2 = new Java2DRenderer(g2d, 4, Color.WHITE, Color.BLACK, null);
        g2d.translate(-400, 300);
        renderer2.render(code128);
        g2d.translate(300, 0);
        renderer2.render(datamatrix);
        g2d.translate(100, 0);
        renderer2.render(maxicode);

        String filename = "java-2d-paper-color.png";
        BufferedImage expected = ImageIO.read(getClass().getResourceAsStream(filename));
        String dirName = filename.substring(0, filename.lastIndexOf('.'));
        SymbolTest.assertEqual(expected, image, dirName);
    }

    @Test
    public void testCustomFontStrikethrough() throws Exception {

        Font font = SymbolTest.DEJA_VU_SANS.deriveFont((float) 18);
        font = font.deriveFont(Collections.singletonMap(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON));

        testFont(font, HumanReadableAlignment.CENTER, "java-2d-custom-font-strikethrough.png");
    }

    @Test
    public void testCustomFontJustifyAndTransform() throws Exception {

        Font font = SymbolTest.DEJA_VU_SANS.deriveFont((float) 3);
        font = font.deriveFont(AffineTransform.getScaleInstance(5, 1));

        testFont(font, HumanReadableAlignment.JUSTIFY, "java-2d-custom-font-justify-transform.png");
    }

    private static void testFont(Font font, HumanReadableAlignment alignment, String filename) throws IOException {

        Code128 code128 = new Code128();
        code128.setHumanReadableAlignment(alignment);
//        code128.setFont(font);
        code128.setFontName(font.getFontName());
        code128.setFontSize(font.getSize());
        code128.setContent("123456");

        int magnification = 4;
        int w = code128.getWidth() * magnification;
        int h = code128.getHeight() * magnification;

        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g2d = image.createGraphics();

        Java2DRenderer renderer = new Java2DRenderer(g2d, magnification, Color.WHITE, Color.BLACK, font);
        renderer.render(code128);

        BufferedImage expected = ImageIO.read(Java2DRendererTest.class.getResourceAsStream(filename));
        String dirName = filename.substring(0, filename.lastIndexOf('.'));
        SymbolTest.assertEqual(expected, image, dirName);
    }
}
