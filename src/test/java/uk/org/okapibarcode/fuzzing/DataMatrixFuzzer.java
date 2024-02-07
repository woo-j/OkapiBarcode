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

import java.awt.image.BufferedImage;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;
import com.google.zxing.Result;
import com.google.zxing.datamatrix.DataMatrixReader;

import uk.org.okapibarcode.backend.DataMatrix;
import uk.org.okapibarcode.backend.DataMatrix.ForceMode;
import uk.org.okapibarcode.backend.OkapiInputException;
import uk.org.okapibarcode.backend.Symbol.DataType;

/**
 * Fuzz tester for {@link DataMatrix}.
 *
 * @see <a href="https://github.com/zxing/zxing/">ZXing</a>
 * @see <a href="https://github.com/CodeIntelligenceTesting/jazzer">Jazzer</a>
 * @see <a href="https://github.com/CodeIntelligenceTesting/jazzer/blob/main/docs/junit-integration.md">Jazzer + JUnit integration</a>
 * @see <a href="https://llvm.org/docs/LibFuzzer.html#options">LibFuzzer Options</a>
 * @see <a href="https://llvm.org/docs/LibFuzzer.html#corpus">LibFuzzer Corpus</a>
 */
public class DataMatrixFuzzer {

    @FuzzTest(maxDuration = "5m")
    public void test(FuzzedDataProvider data) throws Exception {

        // it would be great if we could check all ~600 input combinations per content string, but
        // it would make the fuzzing extremely slow, and checking a wide variety of data strings is
        // more important than being 100% exhaustive with all of the other parameters; the compromise
        // here is to run two tests per data string: one using the most common defaults, and one using
        // a semi-random combination of secondary parameters derived from the data string

        String content = data.consumeString(50);
        check(content, DataType.ECI, false, 0, ForceMode.NONE);

        DataType type = deriveDataTypeFrom(content);
        boolean readerInit = deriveBooleanFrom(content);
        int size = deriveIntFrom(content, 1, 30);
        ForceMode mode = deriveForceModeFrom(content);
        check(content, type, readerInit, size, mode);
    }

    private static void check(String content, DataType type, boolean readerInit, int size, ForceMode mode) throws Exception {

        // ZXing does not support structured append in Data Matrix symbols, so we don't test structured append options here

        DataMatrix symbol = new DataMatrix();
        symbol.setDataType(type);
        symbol.setReaderInit(readerInit);
        symbol.setPreferredSize(size);
        symbol.setForceMode(mode);
        try {
            symbol.setContent(content);
        } catch (OkapiInputException e) {
            // a validation error is not unexpected behavior
            return;
        }

        if (symbol.getEciMode() != 3) {
            // ZXing does not currently support ECI in Data Matrix symbols
            return;
        }

        BufferedImage image = draw(symbol);
        Result result = decode(image, new DataMatrixReader());
        String output = result.getText();

        String input = switch (type) {
            case ECI -> content;
            case GS1 -> content.replace("[", "\u001d").replace("]", ""); // ZXing output represents FNC1 as a GS char
            case HIBC -> symbol.getContent(); // HIBC automatically adds a prefix and a suffix which ZXing will include
        };

        assertEqual(input, output, content, type, readerInit, size, mode);
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

    private static ForceMode deriveForceModeFrom(String s) {
        int i = s.isEmpty() ? 0 : s.charAt(s.length() - 1);
        int j = i % 3;
        return switch (j) {
            case 0 -> ForceMode.NONE;
            case 1 -> ForceMode.SQUARE;
            default -> ForceMode.RECTANGULAR;
        };
    }

    private static boolean deriveBooleanFrom(String s) {
        int i = s.isEmpty() ? 0 : s.charAt(s.length() - 1);
        int j = i % 2;
        return j == 0;
    }

    private static int deriveIntFrom(String s, int min, int max) {
        int i = s.isEmpty() ? 0 : s.charAt(s.length() - 1);
        int j = i % (max - min + 1);
        return min + j;
    }
}
