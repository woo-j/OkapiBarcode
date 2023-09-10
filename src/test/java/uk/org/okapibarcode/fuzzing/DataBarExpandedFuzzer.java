package uk.org.okapibarcode.fuzzing;

import static uk.org.okapibarcode.backend.SymbolTest.assertEqual;
import static uk.org.okapibarcode.backend.SymbolTest.decode;
import static uk.org.okapibarcode.backend.SymbolTest.draw;

import java.awt.image.BufferedImage;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;
import com.google.zxing.Result;
import com.google.zxing.oned.rss.expanded.RSSExpandedReader;

import uk.org.okapibarcode.backend.DataBarExpanded;
import uk.org.okapibarcode.backend.OkapiException;
import uk.org.okapibarcode.backend.Symbol.DataType;

/**
 * Fuzz tester for {@link DataBarExpanded}.
 *
 * @see <a href="https://github.com/zxing/zxing/">ZXing</a>
 * @see <a href="https://github.com/CodeIntelligenceTesting/jazzer">Jazzer</a>
 * @see <a href="https://github.com/CodeIntelligenceTesting/jazzer/blob/main/docs/junit-integration.md">Jazzer + JUnit integration</a>
 * @see <a href="https://llvm.org/docs/LibFuzzer.html#options">LibFuzzer Options</a>
 * @see <a href="https://llvm.org/docs/LibFuzzer.html#corpus">LibFuzzer Corpus</a>
 */
public class DataBarExpandedFuzzer {

    @FuzzTest(maxDuration = "5m")
    public void test(FuzzedDataProvider data) throws Exception {
        String content = data.consumeString(20);
        int columns = deriveIntFrom(content, 1, 10);
        check(content, true, columns);
        check(content, false, columns);
    }

    private static void check(String content, boolean stacked, int columns) throws Exception {

        DataBarExpanded symbol = new DataBarExpanded();
        symbol.setDataType(DataType.GS1);
        symbol.setStacked(stacked);
        symbol.setPreferredColumns(columns);
        try {
            symbol.setContent(content);
        } catch (OkapiException e) {
            // a validation error is not unexpected behavior
            return;
        }

        BufferedImage image = draw(symbol);
        Result result = decode(image, new RSSExpandedReader());
        String output = result.getText();
        String input = content.replace('[', '(').replace(']', ')'); // ZXing uses parenthesis for AIs instead of brackets

        assertEqual(input, output, content, stacked, columns);
    }

    private static int deriveIntFrom(String s, int min, int max) {
        int i = s.isEmpty() ? 0 : s.charAt(s.length() - 1);
        int j = i % (max - min + 1);
        return min + j;
    }
}
