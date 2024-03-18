/*
 * Copyright 2024 Urs Wolfer
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

package uk.org.okapibarcode.backend;

import static java.awt.image.BufferedImage.TYPE_BYTE_BINARY;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.org.okapibarcode.graphics.Color.BLACK;
import static uk.org.okapibarcode.graphics.Color.WHITE;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.pdf417.PDF417Reader;

import uk.org.okapibarcode.output.Java2DRenderer;

/**
 * @author Urs Wolfer
 */
class Pdf417AutoStructuredAppendTest {

    @ParameterizedTest
    @CsvSource({
        "0,0,0",
        "1,1,1",
        "2,1,1",
        "3,1,1",
        "270,2,2",
        "271,2,2",
        "282,2,3",
        "283,2,3",
        "288,2,3",
        "289,2,3",
        "290,2,3",
        "291,3,3",
        "292,3,3",
        "293,3,3",
        "294,3,3"
    })
    public void testCreateStructuredAppendSymbols(int dataLength, int expectedSymbolsCount, int expectedSymbolsCountByteCompaction) throws Exception {

        Pdf417 template = new Pdf417();
        template.setPreferredEccLevel(4);
        template.setBarHeight(1);
        template.setRows(12);
        template.setDataColumns(13);
        template.setStructuredAppendIncludeSegmentCount(true);

        byte[] bytes = bytes(dataLength);
        List< Pdf417 > symbols = Pdf417.createStructuredAppendSymbols(new String(bytes, ISO_8859_1), template);
        assertions(symbols, bytes, expectedSymbolsCount);

        template.setForceByteCompaction(true);
        List< Pdf417 > symbolsByteEncoded = Pdf417.createStructuredAppendSymbols(bytes, template);
        assertions(symbolsByteEncoded, bytes, expectedSymbolsCountByteCompaction);
    }

    @Test
    @Disabled // very slow: keep around for manual testing only
    public void bruteForce() throws Exception {
        ExecutorService executor = Executors.newWorkStealingPool();
        for (int length = 0; length <= 2048; length++) {
            byte[] bytes = bytes(length);
            for (int rows = 3; rows < 30; rows++) {
                for (int cols = 1; cols < 30; cols++) {
                    int r = rows;
                    int c = cols;
                    executor.submit(() -> testCombo(bytes, r, c));
                }
            }
        }
        executor.shutdown();
        executor.awaitTermination(999, TimeUnit.HOURS);
    }

    private static void testCombo(byte[] bytes, int rows, int cols) {
        String msg = "length: " + bytes.length + " rows: " + rows + " cols: " + cols;
        System.out.println(msg);
        Pdf417 template = new Pdf417();
        template.setRows(rows);
        template.setDataColumns(cols);
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            List< Pdf417 > symbols = Pdf417.createStructuredAppendSymbols(bytes, template);
            for (Pdf417 barcode : symbols) {
                BufferedImage img = drawToImage(barcode);
                Result result = decodeImage(img);
                byte[] output = result.getText().getBytes(ISO_8859_1);
                stream.write(output);
            }
            byte[] decoded = stream.toByteArray();
            assertArrayEquals(bytes, decoded, msg);
        } catch (OkapiInputException oie) {
            assertEquals("The specified template is too small to hold both data and structured append metadata", oie.getMessage(), msg);
            assertTrue(template.getRows() * template.getDataColumns() < 20, msg);
        } catch (ReaderException re) {
            throw new RuntimeException(msg, re);
        } catch (IOException ioe) {
            throw new RuntimeException(msg, ioe);
        }
    }

    private byte[] bytes(int length) {
        byte[] bytes = new byte[length];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) i;
        }
        return bytes;
    }

    private static void assertions(List< Pdf417 > symbols, byte[] bytes, int expectedSymbolsCount) throws IOException, ReaderException {

        assertEquals(expectedSymbolsCount, symbols.size());

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            for (Pdf417 barcode : symbols) {
                assertEquals(4, barcode.getPreferredEccLevel());
                assertEquals(1, barcode.getBarHeight());
                assertEquals(12, barcode.getRows());
                assertEquals(13, barcode.getDataColumns());
                BufferedImage img = drawToImage(barcode);
                Result result = decodeImage(img);
                byte[] output = result.getText().getBytes(ISO_8859_1);
                outputStream.write(output);
            }
            assertArrayEquals(bytes, outputStream.toByteArray());
        }
    }

    private static BufferedImage drawToImage(Pdf417 barcode) {
        int mag = 5;
        BufferedImage img = new BufferedImage(barcode.getWidth() * mag, barcode.getHeight() * mag, TYPE_BYTE_BINARY);
        Graphics2D g2d = img.createGraphics();
        Java2DRenderer renderer = new Java2DRenderer(g2d, mag, WHITE, BLACK);
        renderer.render(barcode);
        g2d.dispose();
        return img;
    }

    private static Result decodeImage(BufferedImage img) throws ReaderException {
        LuminanceSource source = new BufferedImageLuminanceSource(img);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        PDF417Reader reader = new PDF417Reader();
        Result result = reader.decode(bitmap);
        return result;
    }
}
