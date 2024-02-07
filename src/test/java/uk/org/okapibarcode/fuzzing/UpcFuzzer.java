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
import com.google.zxing.oned.UPCAReader;
import com.google.zxing.oned.UPCEReader;

import uk.org.okapibarcode.backend.OkapiInputException;
import uk.org.okapibarcode.backend.Upc;
import uk.org.okapibarcode.backend.Upc.Mode;

/**
 * Fuzz tester for {@link Upc}.
 *
 * @see <a href="https://github.com/zxing/zxing/">ZXing</a>
 * @see <a href="https://github.com/CodeIntelligenceTesting/jazzer">Jazzer</a>
 * @see <a href="https://github.com/CodeIntelligenceTesting/jazzer/blob/main/docs/junit-integration.md">Jazzer + JUnit integration</a>
 * @see <a href="https://llvm.org/docs/LibFuzzer.html#options">LibFuzzer Options</a>
 * @see <a href="https://llvm.org/docs/LibFuzzer.html#corpus">LibFuzzer Corpus</a>
 */
public class UpcFuzzer {

    @FuzzTest(maxDuration = "5m")
    public void test(FuzzedDataProvider data) throws Exception {
        String content = data.consumeString(20);
        check(content, Mode.UPCA);
        check(content, Mode.UPCE);
    }

    private static void check(String content, Mode mode) throws Exception {

        Upc symbol = new Upc();
        symbol.setMode(mode);
        symbol.setQuietZoneHorizontal(12);
        try {
            symbol.setContent(content);
        } catch (OkapiInputException e) {
            // a validation error is not unexpected behavior
            return;
        }

        BufferedImage image = draw(symbol);
        Reader reader = (mode == Mode.UPCA ? new UPCAReader() : new UPCEReader());
        Result result = decode(image, reader);
        String output = result.getText();
        output = output.substring(0, output.length() - 1); // remove check digit (wasn't part of the input but ZXing includes it)

        String input = symbol.getContent(); // compare against corrected input
        assertEqual(input, output, content, mode);
        verifyMetadata(symbol, result);
    }
}
