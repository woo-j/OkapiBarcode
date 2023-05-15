package uk.org.okapibarcode.fuzzing;

import static uk.org.okapibarcode.backend.SymbolTest.assertEqual;
import static uk.org.okapibarcode.backend.SymbolTest.decode;
import static uk.org.okapibarcode.backend.SymbolTest.draw;

import java.awt.image.BufferedImage;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;
import com.google.zxing.NotFoundException;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.oned.UPCAReader;
import com.google.zxing.oned.UPCEReader;

import uk.org.okapibarcode.backend.OkapiException;
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
        try {
            symbol.setContent(content);
        } catch (OkapiException e) {
            // a validation error is not unexpected behavior
            return;
        }

        BufferedImage image = draw(symbol);

        try {
            Reader reader = (mode == Mode.UPCA ? new UPCAReader() : new UPCEReader());
            Result result = decode(image, reader);
            String output = result.getText();
            assertEqual(content, output, content, mode);
        } catch (NotFoundException e) {
            // expected sometimes, because we're testing UPC-A and UPC-E at the same time, and also
            // because Okapi is more lenient about what it encodes than ZXing is about what it decodes
        }
    }
}
