package uk.org.okapibarcode.fuzzing;

import static uk.org.okapibarcode.backend.SymbolTest.assertEqual;
import static uk.org.okapibarcode.backend.SymbolTest.decode;
import static uk.org.okapibarcode.backend.SymbolTest.draw;

import java.awt.image.BufferedImage;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;
import com.google.zxing.Result;
import com.google.zxing.pdf417.PDF417Reader;

import uk.org.okapibarcode.backend.OkapiException;
import uk.org.okapibarcode.backend.Pdf417;
import uk.org.okapibarcode.backend.Pdf417.Mode;
import uk.org.okapibarcode.backend.Symbol.DataType;

/**
 * Fuzz tester for {@link Pdf417}.
 *
 * @see <a href="https://github.com/zxing/zxing/">ZXing</a>
 * @see <a href="https://github.com/CodeIntelligenceTesting/jazzer">Jazzer</a>
 * @see <a href="https://github.com/CodeIntelligenceTesting/jazzer/blob/main/docs/junit-integration.md">Jazzer + JUnit integration</a>
 * @see <a href="https://llvm.org/docs/LibFuzzer.html#options">LibFuzzer Options</a>
 * @see <a href="https://llvm.org/docs/LibFuzzer.html#corpus">LibFuzzer Corpus</a>
 */
public class Pdf417Fuzzer {

    @FuzzTest(maxDuration = "5m")
    public void test(FuzzedDataProvider data) throws Exception {

        // it would be great if we could check all ~200k input combinations per content string, but
        // it would make the fuzzing extremely slow, and checking a wide variety of data strings is
        // more important than being 100% exhaustive with all of the other parameters; the compromise
        // here is to run two tests per data string: one using the most common defaults, and one using
        // a semi-random combination of secondary parameters derived from the data string

        String content = data.consumeString(50);
        check(content, DataType.ECI, null, null, null, Mode.NORMAL, false);

        DataType type = deriveDataTypeFrom(content);
        int cols = deriveIntFrom(content, 1, 30);
        int rows = deriveIntFrom(content, 3, 90);
        int ecc = deriveIntFrom(content, 0, 8);
        Mode mode = deriveModeFrom(content);
        boolean readerInit = deriveBooleanFrom(content);
        check(content, type, cols, rows, ecc, mode, readerInit);
    }

    private static void check(String content, DataType type, Integer cols, Integer rows, Integer ecc, Mode mode, boolean readerInit) throws Exception {

        // we omit testing structured append options, although technically it is possible to check via the ZXing result metadata

        Pdf417 symbol = new Pdf417();
        symbol.setDataType(type);
        if (cols != null) symbol.setDataColumns(cols);
        if (rows != null) symbol.setRows(rows);
        if (ecc != null) symbol.setPreferredEccLevel(ecc);
        symbol.setMode(mode);
        symbol.setReaderInit(readerInit);
        try {
            symbol.setContent(content);
        } catch (OkapiException e) {
            // a validation error is not unexpected behavior
            return;
        }

        BufferedImage image = draw(symbol);
        Result result = decode(image, new PDF417Reader());
        String output = result.getText();

        String input = switch (type) {
            case ECI -> content;
            case HIBC -> symbol.getContent(); // HIBC automatically adds a prefix and a suffix which ZXing will include
            case GS1 -> throw new IllegalStateException(); // this symbol does not support GS1
        };

        assertEqual(input, output, content, type, cols, rows, ecc, mode, readerInit);
    }

    private static DataType deriveDataTypeFrom(String s) {
        int i = s.isEmpty() ? 0 : s.charAt(s.length() - 1);
        int j = i % 2;
        return j == 0 ? DataType.ECI : DataType.HIBC; // this symbol does not support GS1
    }

    private static Mode deriveModeFrom(String s) {
        int i = s.isEmpty() ? 0 : s.charAt(s.length() - 1);
        int j = i % 2;
        return j == 0 ? Mode.NORMAL : Mode.TRUNCATED;
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
