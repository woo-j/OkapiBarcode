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

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.pdf417.PDF417Reader;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.org.okapibarcode.output.Java2DRenderer;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static java.awt.image.BufferedImage.TYPE_BYTE_BINARY;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.org.okapibarcode.graphics.Color.BLACK;
import static uk.org.okapibarcode.graphics.Color.WHITE;

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
        byte[] bytes = new byte[dataLength];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) i;
        }

        Pdf417 symbolTemplate = new Pdf417();
        symbolTemplate.setPreferredEccLevel(4);
        symbolTemplate.setBarHeight(1);
        symbolTemplate.setRows(12);
        symbolTemplate.setDataColumns(13);
        symbolTemplate.setStructuredAppendIncludeSegmentCount(true);

        List<Pdf417> symbols = Pdf417.createStructuredAppendSymbols(new String(bytes, ISO_8859_1), symbolTemplate);
        assertions(symbols, bytes, expectedSymbolsCount);

        symbolTemplate.setForceByteCompaction(true);
        List<Pdf417> symbolsByteEncoded = Pdf417.createStructuredAppendSymbols(bytes, symbolTemplate);
        assertions(symbolsByteEncoded, bytes, expectedSymbolsCountByteCompaction);
    }

    private static void assertions(List<Pdf417> symbols, byte[] bytes, int expectedSymbolsCount) throws IOException, NotFoundException, FormatException, ChecksumException {
        assertEquals(expectedSymbolsCount, symbols.size());

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            for (Pdf417 barcode : symbols) {
                assertEquals(4, barcode.getPreferredEccLevel());
                assertEquals(1, barcode.getBarHeight());
                assertEquals(12, barcode.getRows());
                assertEquals(13, barcode.getDataColumns());

                BufferedImage img = new BufferedImage(barcode.getWidth(), barcode.getHeight(), TYPE_BYTE_BINARY);
                Graphics2D g2d = img.createGraphics();
                Java2DRenderer renderer = new Java2DRenderer(g2d, 1, WHITE, BLACK);
                renderer.render(barcode);
                g2d.dispose();

                LuminanceSource source = new BufferedImageLuminanceSource(img);
                BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                PDF417Reader reader = new PDF417Reader();
                Result result = reader.decode(bitmap);

                byte[] output = result.getText().getBytes(ISO_8859_1);
                outputStream.write(output);

            }
            assertArrayEquals(bytes, outputStream.toByteArray());
        }
    }
}
