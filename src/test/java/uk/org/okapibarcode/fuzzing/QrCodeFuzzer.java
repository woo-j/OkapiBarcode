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

package uk.org.okapibarcode.fuzzing;

import static uk.org.okapibarcode.backend.SymbolTest.assertEqual;
import static uk.org.okapibarcode.backend.SymbolTest.decode;
import static uk.org.okapibarcode.backend.SymbolTest.draw;
import static uk.org.okapibarcode.backend.SymbolTest.verifyMetadata;

import java.awt.image.BufferedImage;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;
import com.google.zxing.Result;
import com.google.zxing.qrcode.QRCodeReader;

import uk.org.okapibarcode.backend.OkapiInputException;
import uk.org.okapibarcode.backend.QrCode;
import uk.org.okapibarcode.backend.QrCode.EccLevel;
import uk.org.okapibarcode.backend.Symbol.DataType;

/**
 * Fuzz tester for {@link QrCode}.
 *
 * @see <a href="https://github.com/zxing/zxing/">ZXing</a>
 * @see <a href="https://github.com/CodeIntelligenceTesting/jazzer">Jazzer</a>
 * @see <a href="https://github.com/CodeIntelligenceTesting/jazzer/blob/main/docs/junit-integration.md">Jazzer + JUnit integration</a>
 * @see <a href="https://llvm.org/docs/LibFuzzer.html#options">LibFuzzer Options</a>
 * @see <a href="https://llvm.org/docs/LibFuzzer.html#corpus">LibFuzzer Corpus</a>
 */
public class QrCodeFuzzer {

    @FuzzTest(maxDuration = "5m")
    public void test(FuzzedDataProvider data) throws Exception {

        // it would be great if we could check all ~250 input combinations per content string, but
        // it would make the fuzzing extremely slow, and checking a wide variety of data strings is
        // more important than being 100% exhaustive with all of the other parameters; the compromise
        // here is to run two tests per data string: one using the most common defaults, and one using
        // a semi-random combination of secondary parameters derived from the data string

        String content = data.consumeString(50);
        check(content, 0, DataType.ECI, EccLevel.L);

        int version = deriveIntFrom(content, 1, 40);
        DataType type = deriveDataTypeFrom(content);
        EccLevel ecc = deriveEccLevelFrom(content);
        check(content, version, type, ecc);
    }

    private static void check(String content, int version, DataType type, EccLevel ecc) throws Exception {

        QrCode symbol = new QrCode();
        symbol.setDataType(type);
        symbol.setPreferredVersion(version);
        symbol.setPreferredEccLevel(ecc);
        try {
            symbol.setContent(content);
        } catch (OkapiInputException e) {
            // a validation error is not unexpected behavior
            return;
        }

        BufferedImage image = draw(symbol);
        Result result = decode(image, new QRCodeReader());
        String output = result.getText();

        String input = switch (type) {
            case ECI -> content;
            case GS1 -> symbol.getContent().replace("\\<FNC1>", "\u001d"); // ZXing output represents FNC1 as a GS char
            case HIBC -> symbol.getContent(); // HIBC automatically adds a prefix and a suffix which ZXing will include
        };

        assertEqual(input, output, content, version, type, ecc);
        verifyMetadata(symbol, result);
    }

    private static DataType deriveDataTypeFrom(String s) {
        int i = s.isEmpty() ? 0 : s.charAt(s.length() - 1);
        int j = i % 3;
        return switch (j) {
            case 0 -> DataType.ECI;
            case 1 -> DataType.GS1;
            default -> DataType.HIBC;
        };
    }

    private static EccLevel deriveEccLevelFrom(String s) {
        int i = s.isEmpty() ? 0 : s.charAt(s.length() - 1);
        int j = i % 4;
        return switch (j) {
            case 0 -> EccLevel.L;
            case 1 -> EccLevel.M;
            case 2 -> EccLevel.Q;
            default -> EccLevel.H;
        };
    }

    private static int deriveIntFrom(String s, int min, int max) {
        int i = s.isEmpty() ? 0 : s.charAt(s.length() - 1);
        int j = i % (max - min + 1);
        return min + j;
    }
}
