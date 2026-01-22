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

import static java.awt.image.BufferedImage.TYPE_BYTE_BINARY;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.org.okapibarcode.graphics.Color.BLACK;
import static uk.org.okapibarcode.graphics.Color.WHITE;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.ResultMetadataType;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import uk.org.okapibarcode.backend.QrCode.EccLevel;
import uk.org.okapibarcode.output.Java2DRenderer;

/**
 * Tests for {@link QrCode} structured append.
 */
class QrCodeAutoStructuredAppendTest {

    @ParameterizedTest
    @CsvSource({
        "0,0",
        "1,1",
        "5,1",
        "6,2",
        "80,16"
    })
    public void testCreateStructuredAppendSymbols(int dataLength, int expectedSymbolsCount) throws Exception {

        QrCode template = new QrCode();
        template.setPreferredVersion(1);
        template.setPreferredEccLevel(EccLevel.H);
        template.setForceByteCompaction(true);

        byte[] bytes = bytes(dataLength);
        List< QrCode > symbols = QrCode.createStructuredAppendSymbols(new String(bytes, ISO_8859_1), template);
        assertions(symbols, bytes, expectedSymbolsCount, (qrCode, result) -> { /* no custom assertions */ });
    }

    @ParameterizedTest
    @CsvSource({
        "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ,1",
        "祈れ、フリースイスよ、祈れ！,72"
    })
    public void testCalculateStructuredAppendParity(String content, int expectedParity) throws Exception {
        List< QrCode > symbols = QrCode.createStructuredAppendSymbols(content, new QrCode());
        for (QrCode symbol : symbols) {
            assertEquals(expectedParity, symbol.getStructuredAppendParity());
        }
    }

    @Test
    public void testCreateStructuredAppendSymbolsWithTooSmallTemplate() throws Exception {

        QrCode template = new QrCode();
        template.setPreferredVersion(1);
        template.setPreferredEccLevel(EccLevel.H);
        template.setForceByteCompaction(true);

        // first validate that the template can handle the maximum size (16 symbols, 5 bytes of data in each)
        int totalByteCount = 16 * 5;
        byte[] bytes = bytes(totalByteCount);
        List< QrCode > symbols = QrCode.createStructuredAppendSymbols(new String(bytes, ISO_8859_1), template);
        assertions(symbols, bytes, 16, (qrCode, result) -> {
            assertEquals(9, result.getRawBytes().length);
            assertEquals(5, result.getText().length());
        });

        // now re-test with one extra byte, which would require 17 symbols (not possible)
        OkapiInputException exception = assertThrows(OkapiInputException.class, () -> {
            QrCode.createStructuredAppendSymbols(new String(bytes(totalByteCount + 1), ISO_8859_1), template);
        });
        assertEquals("The specified template is too small to hold both data and structured append metadata", exception.getMessage());
    }

    private byte[] bytes(int length) {
        return "A".repeat(length).getBytes(ISO_8859_1);
    }

    private static void assertions(List< QrCode > symbols, byte[] expectedBytes, int expectedSymbolCount, CustomSymbolAssertion customAssertion) throws ReaderException {
        assertEquals(expectedSymbolCount, symbols.size());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        for (int i = 0; i < symbols.size(); i++) {
            QrCode barcode = symbols.get(i);
            assertEquals(EccLevel.H, barcode.getPreferredEccLevel());
            BufferedImage img = drawToImage(barcode);
            Result result = decodeImage(img);
            if (expectedSymbolCount > 1) {
                assertStructuredAppendSequence(result, i + 1, expectedSymbolCount);
            } else {
                assertNoStructuredAppendMode(result);
            }
            customAssertion.assertSymbol(barcode, result);
            byte[] output = result.getText().getBytes(ISO_8859_1);
            outputStream.writeBytes(output);
        }
        assertArrayEquals(expectedBytes, outputStream.toByteArray());
    }

    private static void assertStructuredAppendSequence(Result result, int expectedPosition, int expectedTotal) {
        int sequence = (int) result.getResultMetadata().get(ResultMetadataType.STRUCTURED_APPEND_SEQUENCE);
        int position = ((sequence >> 4) & 0x0F) + 1;
        int total = (sequence & 0x0F) + 1;
        assertEquals(expectedPosition, position);
        assertEquals(expectedTotal, total);
    }

    private static void assertNoStructuredAppendMode(Result result) {
        assertNull(result.getResultMetadata().get(ResultMetadataType.STRUCTURED_APPEND_SEQUENCE));
        assertNull(result.getResultMetadata().get(ResultMetadataType.STRUCTURED_APPEND_PARITY));
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
        Result result = reader.decode(bitmap, Map.of(DecodeHintType.PURE_BARCODE, true));
        return result;
    }

    @FunctionalInterface
    private interface CustomSymbolAssertion {
        void assertSymbol(QrCode symbol, Result result);
    }
}
