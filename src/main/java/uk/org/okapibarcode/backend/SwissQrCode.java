/*
 * Copyright 2024 Daniel Gredler
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

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Swiss QR Code is a specialized type of QR Code symbol used for QR-bill in Switzerland. It is mostly a
 * spec-compliant QR Code, but it must use error correction level M, it cannot hold more than 997 characters,
 * it must always measure 46x46 mm when printed, data must be encoded as UTF-8 without the use of ECI, and it
 * features a Swiss cross logo in the center of the symbol.
 *
 * @author Daniel Gredler
 * @see <a href="https://www.six-group.com/dam/download/banking-services/standardization/qr-bill/ig-qr-bill-v2.2-en.pdf">Swiss QR Bill Specification, Section 5</a>
 */
public class SwissQrCode extends QrCode {

    public SwissQrCode() {
        minVersion = 6; // min size to fit logo
        preferredEccLevel = EccLevel.M; // mandated by spec
    }

    @Override
    public void setPreferredEccLevel(EccLevel preferredEccLevel) {
        if (preferredEccLevel != EccLevel.M) {
            throw new OkapiInputException("Swiss QR Code requires ECC level M");
        }
        super.setPreferredEccLevel(preferredEccLevel);
    }

    @Override
    public void setPreferredVersion(int version) {
        if (version < minVersion) {
            throw new OkapiInputException("Swiss QR Code cannot fit logo at sizes less than 6");
        }
        super.setPreferredVersion(version);
    }

    @Override
    public boolean supportsGs1() {
        return false;
    }

    @Override
    public void setContent(String data) {
        // Swiss QR Code requires coding data as "UTF-8 restricted to the Latin character set"
        byte[] bytes = data.getBytes(UTF_8);
        String data2 = new String(bytes, ISO_8859_1);
        if (data2.length() > 997) {
            throw OkapiInputException.inputTooLong();
        }
        super.setContent(data2);
    }

    @Override
    protected void customize(int[] grid, int size) {
        int w = (int) (size * (7d / 46d)); // 7x7 mm logo on a 46x46 mm symbol
        if (w % 2 == 0) {
            w++; // use odd logo sizes for better centering
        }
        int border = 1;
        int field = w - (2 * border);
        int pad = (int) Math.max(field * 6 / 32d, 1);
        int thick = (int) Math.max(field * 6 / 32d, 1);
        int len = (field - pad - pad - thick) / 2;
        int start = (size + 1) * ((size - w) / 2);
        for (int y = 0; y < w; y++) {
            for (int x = 0; x < w; x++) {
                int i = start + (y * size) + x;
                if (x == 0 || x == w - 1 || y == 0 || y == w - 1 ||
                   (x >= border + pad + len && x < w - border - pad - len && y >= border + pad && y < w - border - pad) ||
                   (y >= border + pad + len && y < w - border - pad - len && x >= border + pad && x < w - border - pad)) {
                    // borders and cross are white
                    grid[i] = 0;
                } else {
                    // the rest is black
                    grid[i] = 1;
                }
            }
        }
    }
}
