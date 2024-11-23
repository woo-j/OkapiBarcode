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

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;

import uk.org.okapibarcode.backend.CodablockF;
import uk.org.okapibarcode.backend.OkapiInputException;
import uk.org.okapibarcode.backend.Symbol.DataType;

/**
 * Fuzz tester for {@link CodablockF}.
 *
 * @see <a href="https://github.com/zxing/zxing/">ZXing</a>
 * @see <a href="https://github.com/CodeIntelligenceTesting/jazzer">Jazzer</a>
 * @see <a href="https://github.com/CodeIntelligenceTesting/jazzer/blob/main/docs/junit-integration.md">Jazzer + JUnit integration</a>
 * @see <a href="https://llvm.org/docs/LibFuzzer.html#options">LibFuzzer Options</a>
 * @see <a href="https://llvm.org/docs/LibFuzzer.html#corpus">LibFuzzer Corpus</a>
 */
public class CodablockFFuzzer {

    @FuzzTest(maxDuration = "5m")
    public void test(FuzzedDataProvider data) throws Exception {
        String content = data.consumeString(20);
        check(content, DataType.ECI,  false);
        check(content, DataType.ECI,  true);
        check(content, DataType.GS1,  false);
        check(content, DataType.GS1,  true);
        check(content, DataType.HIBC, false);
        check(content, DataType.HIBC, true);
    }

    private static void check(String content, DataType type, boolean readerInit) throws Exception {

        CodablockF symbol = new CodablockF();
        symbol.setDataType(type);
        symbol.setReaderInit(readerInit);
        try {
            symbol.setContent(content);
        } catch (OkapiInputException e) {
            // a validation error is not unexpected behavior
        }

        // ZXing cannot decode Codablock F, so we cannot check decoded values against encoded values,
        // meaning that this specific fuzzer can detect severe encoding errors, but not subtle issues
    }
}
