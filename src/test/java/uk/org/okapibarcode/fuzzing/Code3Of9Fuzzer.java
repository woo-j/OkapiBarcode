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
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.oned.Code39Reader;

import uk.org.okapibarcode.backend.Code3Of9;
import uk.org.okapibarcode.backend.Code3Of9.CheckDigit;
import uk.org.okapibarcode.backend.OkapiInputException;
import uk.org.okapibarcode.backend.Symbol.DataType;

/**
 * Fuzz tester for {@link Code3Of9}.
 *
 * @see <a href="https://github.com/zxing/zxing/">ZXing</a>
 * @see <a href="https://github.com/CodeIntelligenceTesting/jazzer">Jazzer</a>
 * @see <a href="https://github.com/CodeIntelligenceTesting/jazzer/blob/main/docs/junit-integration.md">Jazzer + JUnit integration</a>
 * @see <a href="https://llvm.org/docs/LibFuzzer.html#options">LibFuzzer Options</a>
 * @see <a href="https://llvm.org/docs/LibFuzzer.html#corpus">LibFuzzer Corpus</a>
 */
public class Code3Of9Fuzzer {

    @FuzzTest(maxDuration = "5m")
    public void test(FuzzedDataProvider data) throws Exception {
        String content = data.consumeString(120);
        check(content, DataType.ECI, CheckDigit.NONE);
        check(content, DataType.ECI, CheckDigit.MOD43);
        check(content, DataType.HIBC, CheckDigit.NONE);
        check(content, DataType.HIBC, CheckDigit.MOD43);
    }

    private static void check(String content, DataType type, CheckDigit checkDigit) throws Exception {

        Code3Of9 symbol = new Code3Of9();
        symbol.setDataType(type);
        symbol.setCheckDigit(checkDigit);
        symbol.setQuietZoneHorizontal(12);
        try {
            symbol.setContent(content);
        } catch (OkapiInputException e) {
            // a validation error is not unexpected behavior
            return;
        }

        BufferedImage image = draw(symbol);
        Reader reader = new Code39Reader(checkDigit == CheckDigit.MOD43, false);
        Result result = decode(image, reader);
        String output = result.getText();

        String input = symbol.getContent();
        assertEqual(input, output, content, type, checkDigit);
        verifyMetadata(symbol, result);
    }
}
