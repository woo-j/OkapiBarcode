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

import static uk.org.okapibarcode.util.Integers.normalizeRotation;

import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collections;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

import uk.org.okapibarcode.backend.Code128;
import uk.org.okapibarcode.backend.Code93;
import uk.org.okapibarcode.backend.DataMatrix;
import uk.org.okapibarcode.backend.MaxiCode;
import uk.org.okapibarcode.backend.Symbol;
import uk.org.okapibarcode.backend.SymbolTest;
import uk.org.okapibarcode.graphics.Color;
import uk.org.okapibarcode.graphics.TextAlignment;

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
        g2d.setPaint(new GradientPaint(0, 0, java.awt.Color.ORANGE, width, height, java.awt.Color.GREEN));
        g2d.fillRect(0, 0, width, height);

        Java2DRenderer renderer = new Java2DRenderer(g2d, 4, null, Color.BLACK);
        g2d.translate(25, 25);
        renderer.render(code128);
        g2d.translate(300, 0);
        renderer.render(datamatrix);
        g2d.translate(100, 0);
        renderer.render(maxicode);

        Java2DRenderer renderer2 = new Java2DRenderer(g2d, 4, Color.WHITE, Color.BLACK);
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

        Code128 code128 = new Code128();
        code128.setHumanReadableAlignment(TextAlignment.CENTER);
        code128.setFont(font);
        code128.setContent("123456");

        test(code128, "java-2d-custom-font-strikethrough.png", 0);
    }

    @Test
    public void testCustomFontJustifyAndTransform() throws Exception {

        Font font = SymbolTest.DEJA_VU_SANS.deriveFont((float) 3);
        font = font.deriveFont(AffineTransform.getScaleInstance(5, 1));

        Code128 code128 = new Code128();
        code128.setHumanReadableAlignment(TextAlignment.JUSTIFY);
        code128.setFont(font);
        code128.setContent("123456");

        test(code128, "java-2d-custom-font-justify-transform.png", 0);
    }

    @Test
    public void testCode93AlignmentJustify() throws IOException {
        Code93 code93 = new Code93();
        code93.setQuietZoneHorizontal(5);
        code93.setQuietZoneVertical(5);
        code93.setHumanReadableAlignment(TextAlignment.JUSTIFY);
        code93.setFontName(SymbolTest.DEJA_VU_SANS.getFontName());
        code93.setContent("123456789");
        test(code93, "code93-alignment-justify.png", 0);
    }

    @Test
    public void testCode93AlignmentJustifyOneChar() throws IOException {
        Code93 code93 = new Code93();
        code93.setQuietZoneHorizontal(5);
        code93.setQuietZoneVertical(5);
        code93.setShowCheckDigits(false);
        code93.setHumanReadableAlignment(TextAlignment.JUSTIFY);
        code93.setFontName(SymbolTest.DEJA_VU_SANS.getFontName());
        code93.setContent("1");
        test(code93, "code93-alignment-justify-one-char.png", 0);
    }

    @Test
    public void testCode93Rotation90() throws IOException {
        Code93 code93 = new Code93();
        code93.setQuietZoneHorizontal(20);
        code93.setQuietZoneVertical(5);
        code93.setHumanReadableAlignment(TextAlignment.JUSTIFY);
        code93.setFontName(SymbolTest.DEJA_VU_SANS.getFontName());
        code93.setContent("123456789");
        test(code93, "code93-with-rotation-90.png", 90);
    }

    @Test
    public void testCode93Rotation180() throws IOException {
        Code93 code93 = new Code93();
        code93.setQuietZoneHorizontal(20);
        code93.setQuietZoneVertical(5);
        code93.setHumanReadableAlignment(TextAlignment.JUSTIFY);
        code93.setFontName(SymbolTest.DEJA_VU_SANS.getFontName());
        code93.setContent("123456789");
        test(code93, "code93-with-rotation-180.png", 180);
    }

    @Test
    public void testCode93Rotation270() throws IOException {
        Code93 code93 = new Code93();
        code93.setQuietZoneHorizontal(20);
        code93.setQuietZoneVertical(5);
        code93.setHumanReadableAlignment(TextAlignment.JUSTIFY);
        code93.setFontName(SymbolTest.DEJA_VU_SANS.getFontName());
        code93.setContent("123456789");
        test(code93, "code93-with-rotation-270.png", 270);
    }

    @Test
    public void testCode93RotationMinus90() throws IOException {
        Code93 code93 = new Code93();
        code93.setQuietZoneHorizontal(20);
        code93.setQuietZoneVertical(5);
        code93.setHumanReadableAlignment(TextAlignment.JUSTIFY);
        code93.setFontName(SymbolTest.DEJA_VU_SANS.getFontName());
        code93.setContent("123456789");
        test(code93, "code93-with-rotation-270.png", -90);
    }

    @Test
    public void testCode93RotationMinus180() throws IOException {
        Code93 code93 = new Code93();
        code93.setQuietZoneHorizontal(20);
        code93.setQuietZoneVertical(5);
        code93.setHumanReadableAlignment(TextAlignment.JUSTIFY);
        code93.setFontName(SymbolTest.DEJA_VU_SANS.getFontName());
        code93.setContent("123456789");
        test(code93, "code93-with-rotation-180.png", -180);
    }

    @Test
    public void testCode93RotationMinus270() throws IOException {
        Code93 code93 = new Code93();
        code93.setQuietZoneHorizontal(20);
        code93.setQuietZoneVertical(5);
        code93.setHumanReadableAlignment(TextAlignment.JUSTIFY);
        code93.setFontName(SymbolTest.DEJA_VU_SANS.getFontName());
        code93.setContent("123456789");
        test(code93, "code93-with-rotation-90.png", -270);
    }

    private static void test(Symbol symbol, String expectationFile, int rotation) throws IOException {

        int magnification = 4;
        int rot = normalizeRotation(rotation);
        int w = magnification * (rot != 90 && rot != 270 ? symbol.getWidth() : symbol.getHeight());
        int h = magnification * (rot != 90 && rot != 270 ? symbol.getHeight() : symbol.getWidth());

        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g2d = image.createGraphics();

        Java2DRenderer renderer = new Java2DRenderer(g2d, magnification, Color.WHITE, Color.BLACK, rotation);
        renderer.render(symbol);

        BufferedImage expected = ImageIO.read(Java2DRendererTest.class.getResourceAsStream(expectationFile));
        SymbolTest.assertEqual(expected, image, "java-2d");
    }
}
