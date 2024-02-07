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
import com.google.zxing.oned.Code128Reader;

import uk.org.okapibarcode.backend.Code128;
import uk.org.okapibarcode.backend.OkapiInputException;
import uk.org.okapibarcode.backend.Symbol.DataType;

/**
 * Fuzz tester for {@link Code128}.
 *
 * @see <a href="https://github.com/zxing/zxing/">ZXing</a>
 * @see <a href="https://github.com/CodeIntelligenceTesting/jazzer">Jazzer</a>
 * @see <a href="https://github.com/CodeIntelligenceTesting/jazzer/blob/main/docs/junit-integration.md">Jazzer + JUnit integration</a>
 * @see <a href="https://llvm.org/docs/LibFuzzer.html#options">LibFuzzer Options</a>
 * @see <a href="https://llvm.org/docs/LibFuzzer.html#corpus">LibFuzzer Corpus</a>
 */
public class Code128Fuzzer {

    @FuzzTest(maxDuration = "5m")
    public void test(FuzzedDataProvider data) throws Exception {
        String content = data.consumeString(20);
        check(content, DataType.ECI,  false, false);
        check(content, DataType.ECI,  false, true);
        check(content, DataType.ECI,  true,  false);
        check(content, DataType.ECI,  true,  true);
        check(content, DataType.GS1,  false, false);
        check(content, DataType.GS1,  false, true);
        check(content, DataType.GS1,  true,  false);
        check(content, DataType.GS1,  true,  true);
        check(content, DataType.HIBC, false, false);
        check(content, DataType.HIBC, false, true);
        check(content, DataType.HIBC, true,  false);
        check(content, DataType.HIBC, true,  true);
    }

    private static void check(String content, DataType type, boolean suppressModeC, boolean readerInit) throws Exception {

        Code128 symbol = new Code128();
        symbol.setDataType(type);
        symbol.setSuppressModeC(suppressModeC);
        symbol.setReaderInit(readerInit);
        try {
            symbol.setContent(content);
        } catch (OkapiInputException e) {
            // a validation error is not unexpected behavior
            return;
        }

        if (content.contains("\\<FNC1>") ||
            content.contains("\\<FNC2>") ||
            content.contains("\\<FNC3>") ||
            content.contains("\\<FNC4>")) {
            // modifies the meaning of the input data, so we can't just compare Okapi input vs ZXing output
            return;
        }

        BufferedImage image = draw(symbol);
        Result result = decode(image, new Code128Reader());
        String output = result.getText();

        String input = switch (type) {
            case ECI -> content;
            case GS1 -> content.replace("[", "").replace("]", ""); // ZXing won't include the brackets
            case HIBC -> symbol.getContent(); // HIBC automatically adds a prefix and a suffix which ZXing will include
        };

        assertEqual(input, output, content, type, suppressModeC, readerInit);
    }
}
