/*
 * Copyright 2025 Jonas Scheiwiller
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

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.org.okapibarcode.backend.QrCode.EccLevel;
import uk.org.okapibarcode.output.Java2DRenderer;

import java.awt.*;
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

class QrCodeAutoStructuredAppendTest {

    @ParameterizedTest
    @CsvSource({
            "0,0",
            "11,1",
            "12,1",
            "13,2",
            "14,2",
            "192,16"
    })
    public void testCreateStructuredAppendSymbols(int dataLength, int expectedSymbolsCount) throws Exception {

        QrCode template = new QrCode();
        template.setPreferredVersion(1);
        template.setPreferredEccLevel(EccLevel.M);
        template.setForceStructuredAppendMode(true);
        template.setForceByteCompaction(true);

        byte[] bytes = bytes(dataLength);
        List<QrCode> symbols = QrCode.createStructuredAppendSymbols(new String(bytes, ISO_8859_1), template);
        assertions(symbols, bytes, expectedSymbolsCount);
    }

    private byte[] bytes(int length) {
        return "A".repeat(Math.max(0, length)).getBytes(ISO_8859_1);
    }

    private static void assertions(List<QrCode> symbols, byte[] bytes, int expectedSymbolsCount) throws IOException, ReaderException {

        assertEquals(expectedSymbolsCount, symbols.size());

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            for (int i = 0; i < symbols.size(); i++) {
                QrCode barcode = symbols.get(i);
                assertEquals(EccLevel.M, barcode.getPreferredEccLevel());
                BufferedImage img = drawToImage(barcode);
                Result result = decodeImage(img);
                assertStructuredAppendSequence(result, i + 1, expectedSymbolsCount);
                byte[] output = result.getText().getBytes(ISO_8859_1);
                outputStream.write(output);
            }
            assertArrayEquals(bytes, outputStream.toByteArray());
        }
    }

    private static void assertStructuredAppendSequence(Result result, int expectedPosition, int expectedTotal) {
        int sequence = (int) result.getResultMetadata().get(ResultMetadataType.STRUCTURED_APPEND_SEQUENCE);
        int position = ((sequence >> 4) & 0x0F) + 1;
        int total = (sequence & 0x0F) + 1;
        assertEquals(expectedPosition, position);
        assertEquals(expectedTotal, total);
    }

    private static BufferedImage drawToImage(QrCode barcode) {
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
        QRCodeReader reader = new QRCodeReader();
        Result result = reader.decode(bitmap);
        return result;
    }
}
