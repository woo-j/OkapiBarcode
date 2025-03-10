/*
 * Copyright 2015 Daniel Gredler
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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import uk.org.okapibarcode.backend.Code93;
import uk.org.okapibarcode.backend.Ean;
import uk.org.okapibarcode.backend.Ean.Mode;
import uk.org.okapibarcode.backend.MaxiCode;
import uk.org.okapibarcode.backend.QrCode;
import uk.org.okapibarcode.backend.Symbol;
import uk.org.okapibarcode.graphics.Color;
import uk.org.okapibarcode.graphics.TextAlignment;

/**
 * Tests for {@link PostScriptRenderer}.
 */
public class PostScriptRendererTest {

    private Locale originalDefaultLocale;

    @BeforeEach
    public void before() {
        // ensure use of correct decimal separator (period), regardless of default locale
        originalDefaultLocale = Locale.getDefault();
        Locale.setDefault(Locale.GERMANY);
    }

    @AfterEach
    public void after() {
        Locale.setDefault(originalDefaultLocale);
    }

    @Test
    public void testCode93Basic() throws IOException {
        Code93 code93 = new Code93();
        code93.setQuietZoneHorizontal(5);
        code93.setQuietZoneVertical(5);
        code93.setContent("123456789");
        test(code93, 1, Color.WHITE, Color.BLACK, "code93-basic.eps", 0);
    }

    @Test
    public void testCode93AlignmentLeft() throws IOException {
        Code93 code93 = new Code93();
        code93.setQuietZoneHorizontal(5);
        code93.setQuietZoneVertical(5);
        code93.setHumanReadableAlignment(TextAlignment.LEFT);
        code93.setContent("123456789");
        test(code93, 1, Color.WHITE, Color.BLACK, "code93-alignment-left.eps", 0);
    }

    @Test
    public void testCode93AlignmentRight() throws IOException {
        Code93 code93 = new Code93();
        code93.setQuietZoneHorizontal(5);
        code93.setQuietZoneVertical(5);
        code93.setHumanReadableAlignment(TextAlignment.RIGHT);
        code93.setContent("123456789");
        test(code93, 1, Color.WHITE, Color.BLACK, "code93-alignment-right.eps", 0);
    }

    @Test
    public void testCode93AlignmentJustify() throws IOException {
        Code93 code93 = new Code93();
        code93.setQuietZoneHorizontal(5);
        code93.setQuietZoneVertical(5);
        code93.setHumanReadableAlignment(TextAlignment.JUSTIFY);
        code93.setContent("123456789");
        test(code93, 1, Color.WHITE, Color.BLACK, "code93-alignment-justify.eps", 0);
    }

    @Test
    public void testCode93AlignmentJustifyOneChar() throws IOException {
        Code93 code93 = new Code93();
        code93.setQuietZoneHorizontal(5);
        code93.setQuietZoneVertical(5);
        code93.setShowCheckDigits(false);
        code93.setHumanReadableAlignment(TextAlignment.JUSTIFY);
        code93.setContent("1");
        test(code93, 1, Color.WHITE, Color.BLACK, "code93-alignment-justify-one-char.eps", 0);
    }

    @Test
    public void testCode93Margin() throws IOException {
        Code93 code93 = new Code93();
        code93.setQuietZoneHorizontal(20);
        code93.setQuietZoneVertical(20);
        code93.setContent("123456789");
        test(code93, 1, Color.WHITE, Color.BLACK, "code93-margin-size-20.eps", 0);
    }

    @Test
    public void testCode93Magnification() throws IOException {
        Code93 code93 = new Code93();
        code93.setQuietZoneHorizontal(5);
        code93.setQuietZoneVertical(5);
        code93.setContent("123456789");
        test(code93, 2, Color.WHITE, Color.BLACK, "code93-magnification-2.eps", 0);
    }

    @Test
    public void testCode93Colors() throws IOException {
        Code93 code93 = new Code93();
        code93.setQuietZoneHorizontal(5);
        code93.setQuietZoneVertical(5);
        code93.setContent("123456789");
        test(code93, 1, Color.GREEN, Color.RED, "code93-colors.eps", 0);
    }

    @Test
    public void testCode93CustomFont() throws IOException {
        Code93 code93 = new Code93();
        code93.setQuietZoneHorizontal(5);
        code93.setQuietZoneVertical(5);
        code93.setFontName("Arial");
        code93.setFontSize(26);
        code93.setContent("123456789");
        test(code93, 1, Color.WHITE, Color.BLACK, "code93-custom-font.eps", 0);
    }

    @Test
    public void testCode93Empty() throws IOException {
        Code93 code93 = new Code93();
        code93.setQuietZoneHorizontal(5);
        code93.setQuietZoneVertical(5);
        code93.setEmptyContentAllowed(true);
        code93.setContent("");
        test(code93, 1, Color.WHITE, Color.BLACK, "code93-empty.eps", 0);
    }

    @Test
    public void testCode93Null() throws IOException {
        Code93 code93 = new Code93();
        code93.setQuietZoneHorizontal(5);
        code93.setQuietZoneVertical(5);
        code93.setEmptyContentAllowed(true);
        code93.setContent(null);
        test(code93, 1, Color.WHITE, Color.BLACK, "code93-empty.eps", 0);
    }

    @Test
    public void testMaxiCodeBasic() throws IOException {
        MaxiCode maxicode = new MaxiCode();
        maxicode.setQuietZoneHorizontal(5);
        maxicode.setQuietZoneVertical(5);
        maxicode.setMode(4);
        maxicode.setContent("123456789");
        test(maxicode, 5, Color.WHITE, Color.BLACK, "maxicode-basic.eps", 0);
    }

    @Test
    public void testQrCodeBasic() throws IOException {
        QrCode qr = new QrCode();
        qr.setQuietZoneHorizontal(5);
        qr.setQuietZoneVertical(5);
        qr.setContent("123456789");
        test(qr, 5, Color.WHITE, Color.BLACK, "qr-basic.eps", 0);
    }

    @Test
    public void testEan13WithAddOn() throws IOException {
        Ean ean = new Ean();
        ean.setMode(Mode.EAN13);
        ean.setQuietZoneHorizontal(5);
        ean.setQuietZoneVertical(5);
        ean.setContent("123456789012+12345");
        test(ean, 2, Color.WHITE, Color.BLACK, "ean-13-with-add-on.eps", 0);
    }

    @Test
    public void testCode93With1dot2Magnification() throws IOException {
        Code93 code93 = new Code93();
        code93.setContent("123456789");
        test(code93, 1.2, Color.WHITE, Color.BLACK, "code93-with-magnification-1.2.eps", 0);
    }

    @Test
    public void testCode93Rotation90() throws IOException {
        Code93 code93 = new Code93();
        code93.setQuietZoneHorizontal(20);
        code93.setQuietZoneVertical(5);
        code93.setContent("123456789");
        test(code93, 1.2, Color.WHITE, Color.BLACK, "code93-with-rotation-90.eps", 90);
    }

    @Test
    public void testCode93Rotation180() throws IOException {
        Code93 code93 = new Code93();
        code93.setQuietZoneHorizontal(20);
        code93.setQuietZoneVertical(5);
        code93.setContent("123456789");
        test(code93, 1.2, Color.WHITE, Color.BLACK, "code93-with-rotation-180.eps", 180);
    }

    @Test
    public void testCode93Rotation270() throws IOException {
        Code93 code93 = new Code93();
        code93.setQuietZoneHorizontal(20);
        code93.setQuietZoneVertical(5);
        code93.setContent("123456789");
        test(code93, 1.2, Color.WHITE, Color.BLACK, "code93-with-rotation-270.eps", 270);
    }

    @Test
    public void testCode93RotationMinus90() throws IOException {
        Code93 code93 = new Code93();
        code93.setQuietZoneHorizontal(20);
        code93.setQuietZoneVertical(5);
        code93.setContent("123456789");
        test(code93, 1.2, Color.WHITE, Color.BLACK, "code93-with-rotation-270.eps", -90);
    }

    @Test
    public void testCode93RotationMinus180() throws IOException {
        Code93 code93 = new Code93();
        code93.setQuietZoneHorizontal(20);
        code93.setQuietZoneVertical(5);
        code93.setContent("123456789");
        test(code93, 1.2, Color.WHITE, Color.BLACK, "code93-with-rotation-180.eps", -180);
    }

    @Test
    public void testCode93RotationMinus270() throws IOException {
        Code93 code93 = new Code93();
        code93.setQuietZoneHorizontal(20);
        code93.setQuietZoneVertical(5);
        code93.setContent("123456789");
        test(code93, 1.2, Color.WHITE, Color.BLACK, "code93-with-rotation-90.eps", -270);
    }

    @Test
    public void testCode93RotationMinus360() throws IOException {
        Code93 code93 = new Code93();
        code93.setQuietZoneHorizontal(20);
        code93.setQuietZoneVertical(5);
        code93.setContent("123456789");
        test(code93, 1.2, Color.WHITE, Color.BLACK, "code93-with-rotation-360.eps", -360);
    }

    private void test(Symbol symbol, double magnification, Color paper, Color ink, String expectationFile, int rotation) throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PostScriptRenderer renderer = new PostScriptRenderer(baos, magnification, paper, ink, rotation);
        renderer.render(symbol);
        String actual = new String(baos.toByteArray(), StandardCharsets.UTF_8);
        BufferedReader actualReader = new BufferedReader(new StringReader(actual));

        InputStream is = getClass().getResourceAsStream(expectationFile);
        byte[] expectedBytes = new byte[is.available()];
        is.read(expectedBytes);
        String expected = new String(expectedBytes, StandardCharsets.UTF_8);
        BufferedReader expectedReader = new BufferedReader(new StringReader(expected));

        int line = 1;
        String actualLine = actualReader.readLine();
        String expectedLine = expectedReader.readLine();
        while (actualLine != null && expectedLine != null) {
            assertEquals(expectedLine, actualLine, "Line " + line);
            actualLine = actualReader.readLine();
            expectedLine = expectedReader.readLine();
            line++;
        }
    }
}
