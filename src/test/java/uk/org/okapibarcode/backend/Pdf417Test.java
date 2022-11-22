/*
 * Copyright 2022 Daniel Gredler
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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.pdf417.PDF417Reader;

import uk.org.okapibarcode.graphics.Color;
import uk.org.okapibarcode.output.Java2DRenderer;

/**
 * {@link Pdf417} tests that can't be run via the {@link SymbolTest}.
 */
public class Pdf417Test {

    @Test
    public void testSetContentBytes() throws Exception {

        byte[] bytes = new byte[256];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) i; // all possible byte values
        }

        Pdf417 barcode = new Pdf417();
        barcode.setContent(bytes);

        BufferedImage img = new BufferedImage(barcode.getWidth(), barcode.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D g2d = img.createGraphics();
        Java2DRenderer renderer = new Java2DRenderer(g2d, 1, Color.WHITE, Color.BLACK);
        renderer.render(barcode);
        g2d.dispose();

        LuminanceSource source = new BufferedImageLuminanceSource(img);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        PDF417Reader reader = new PDF417Reader();
        Result result = reader.decode(bitmap);

        // result.getRawBytes() doesn't work: https://github.com/zxing/zxing/issues/555
        byte[] output = result.getText().getBytes(StandardCharsets.ISO_8859_1);
        assertArrayEquals(bytes, output);
    }

}
