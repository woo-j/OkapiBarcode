package uk.org.okapibarcode.backend;

import static java.lang.Integer.toHexString;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import org.junit.Assert;
import org.junit.ComparisonFailure;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.reflections.Reflections;

import uk.org.okapibarcode.output.Java2DRenderer;

/**
 * <p>
 * Scans the test resources for file-based bar code tests.
 *
 * <p>
 * Tests that verify successful behavior will contain the following sets of files:
 *
 * <pre>
 *   /src/test/resources/uk/org/okapibarcode/backend/[symbol-name]/[test-name].properties (bar code initialization attributes)
 *   /src/test/resources/uk/org/okapibarcode/backend/[symbol-name]/[test-name].codewords  (expected intermediate coding of the bar code)
 *   /src/test/resources/uk/org/okapibarcode/backend/[symbol-name]/[test-name].png        (expected final rendering of the bar code)
 * </pre>
 *
 * <p>
 * Tests that verify error conditions will contain the following sets of files:
 *
 * <pre>
 *   /src/test/resources/uk/org/okapibarcode/backend/[symbol-name]/[test-name].properties (bar code initialization attributes)
 *   /src/test/resources/uk/org/okapibarcode/backend/[symbol-name]/[test-name].error      (expected error message)
 * </pre>
 *
 * <p>
 * If a properties file is found with no matching expectation files, we assume that it was recently added to the test suite and
 * that we need to generate suitable expectation files for it.
 */
@RunWith(Parameterized.class)
public class SymbolTest {

    /** The type of symbology being tested. */
    private final Class< ? extends Symbol > symbolType;

    /** The properties file containing the test configuration. */
    private final File propertiesFile;

    /** The file containing the expected intermediate coding of the bar code, if this test verifies successful behavior. */
    private final File codewordsFile;

    /** The file containing the expected final rendering of the bar code, if this test verifies successful behavior. */
    private final File pngFile;

    /** The file containing the expected error message, if this test verifies a failure. */
    private final File errorFile;

    /**
     * Creates a new test.
     *
     * @param symbolType the type of symbol being tested
     * @param propertiesFile the properties file containing the test configuration
     * @param codewordsFile the file containing the expected intermediate coding of the bar code, if this test verifies successful behavior
     * @param pngFile the file containing the expected final rendering of the bar code, if this test verifies successful behavior
     * @param errorFile the file containing the expected error message, if this test verifies a failure
     * @param symbolName the name of the symbol type (used only for test naming)
     * @param fileBaseName the base name of the test file (used only for test naming)
     * @throws IOException if there is any I/O error
     */
    public SymbolTest(Class< ? extends Symbol > symbolType, File propertiesFile, File codewordsFile, File pngFile,
                    File errorFile, String symbolName, String fileBaseName) throws IOException {
        this.symbolType = symbolType;
        this.propertiesFile = propertiesFile;
        this.codewordsFile = codewordsFile;
        this.pngFile = pngFile;
        this.errorFile = errorFile;
    }

    /**
     * Runs the test. If there are no expectation files yet, we generate them instead of checking against them.
     *
     * @throws Exception if any error occurs during the test
     */
    @Test
    public void test() throws Exception {

        byte[] bytes = Files.readAllBytes(propertiesFile.toPath());
        String content = new String(bytes, UTF_8);
        String[] lines = content.split("\r\n"); // useful because sometimes we want to use \n or \r in data, but usually not both together

        Map< String, String > properties = new LinkedHashMap<>();
        for (String line : lines) {
            if (!line.startsWith("#") && !line.isEmpty()) {
                int index = line.indexOf('=');
                if (index != -1) {
                    String name = line.substring(0, index);
                    String value = line.substring(index + 1);
                    properties.put(name, value);
                }
            }
        }

        Symbol symbol = symbolType.newInstance();

        try {
            setProperties(symbol, properties);
        } catch (InvocationTargetException e) {
            symbol.error_msg = e.getCause().getMessage(); // TODO: migrate completely to exceptions?
        }

        if (codewordsFile.exists() && pngFile.exists()) {
            verifySuccess(symbol);
        } else if (errorFile.exists()) {
            verifyError(symbol);
        } else {
            generateExpectationFiles(symbol);
        }
    }

    /**
     * Verifies that the specified symbol was encoded and rendered in a way that matches expectations.
     *
     * @param symbol the symbol to check
     * @throws IOException if there is any I/O error
     */
    private void verifySuccess(Symbol symbol) throws IOException {

        assertEquals("error message", "", symbol.error_msg);

        List< String > expectedCodewords = Files.readAllLines(codewordsFile.toPath(), UTF_8);
        int[] actualCodewords = symbol.getCodewords();
        assertEquals(expectedCodewords.size(), actualCodewords.length);
        for (int i = 0; i < actualCodewords.length; i++) {
            int expected = getInt(expectedCodewords.get(i));
            int actual = actualCodewords[i];
            assertEquals("at codeword index " + i, expected, actual);
        }

        BufferedImage expected = ImageIO.read(pngFile);
        BufferedImage actual = draw(symbol);
        assertEqual(expected, actual);
    }

    /**
     * Verifies that the specified symbol encountered the expected error during encoding.
     *
     * @param symbol the symbol to check
     * @throws IOException if there is any I/O error
     */
    private void verifyError(Symbol symbol) throws IOException {
        String expectedError = Files.readAllLines(errorFile.toPath(), UTF_8).get(0);
        assertEquals(expectedError, symbol.error_msg);
    }

    /**
     * Generates the expectation files for the specified symbol.
     *
     * @param symbol the symbol to generate expectation files for
     * @throws IOException if there is any I/O error
     */
    private void generateExpectationFiles(Symbol symbol) throws IOException {
        if (symbol.error_msg != null && !symbol.error_msg.isEmpty()) {
            generateErrorExpectationFile(symbol);
        } else {
            generateCodewordsExpectationFile(symbol);
            generatePngExpectationFile(symbol);
        }
    }

    /**
     * Generates the error expectation file for the specified symbol.
     *
     * @param symbol the symbol to generate the error expectation file for
     * @throws IOException if there is any I/O error
     */
    private void generateErrorExpectationFile(Symbol symbol) throws IOException {
        PrintWriter writer = new PrintWriter(errorFile);
        writer.println(symbol.error_msg);
        writer.close();
    }

    /**
     * Generates the codewords expectation file for the specified symbol.
     *
     * @param symbol the symbol to generate codewords for
     * @throws IOException if there is any I/O error
     */
    private void generateCodewordsExpectationFile(Symbol symbol) throws IOException {
        int[] codewords = symbol.getCodewords();
        PrintWriter writer = new PrintWriter(codewordsFile);
        for (int codeword : codewords) {
            writer.println(codeword);
        }
        writer.close();
    }

    /**
     * Generates the image expectation file for the specified symbol.
     *
     * @param symbol the symbol to draw
     * @throws IOException if there is any I/O error
     */
    private void generatePngExpectationFile(Symbol symbol) throws IOException {
        BufferedImage img = draw(symbol);
        ImageIO.write(img, "png", pngFile);
    }

    /**
     * Returns the integer contained in the specified string. If the string contains a tab character, it and everything after it
     * is ignored.
     *
     * @param s the string to extract the integer from
     * @return the integer contained in the specified string
     */
    private static int getInt(String s) {
        int i = s.indexOf('\t');
        if (i != -1) {
            s = s.substring(0, i);
        }
        return Integer.parseInt(s);
    }

    /**
     * Draws the specified symbol and returns the resultant image.
     *
     * @param symbol the symbol to draw
     * @return the resultant image
     */
    private static BufferedImage draw(Symbol symbol) {

        int magnification = 10;
        int width = symbol.symbol_width * magnification;
        int height = symbol.symbol_height * magnification;

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g2d = img.createGraphics();
        g2d.setPaint(Color.WHITE);
        g2d.fillRect(0, 0, width, height);

        Java2DRenderer renderer = new Java2DRenderer(g2d, magnification, Color.WHITE, Color.BLACK);
        renderer.render(symbol);

        g2d.dispose();

        return img;
    }

    /**
     * Initializes the specified symbol using the specified properties, where keys are attribute names and values are attribute
     * values.
     *
     * @param symbol the symbol to initialize
     * @param properties the attribute names and values to set
     * @throws ReflectiveOperationException if there is any reflection error
     */
    private static void setProperties(Symbol symbol, Map< String, String > properties) throws ReflectiveOperationException {
        for (Map.Entry< String, String > entry : properties.entrySet()) {
            String name = entry.getKey();
            String value = entry.getValue();
            String setterName = "set" + name.substring(0, 1).toUpperCase() + name.substring(1);
            Method setter = getMethod(symbol.getClass(), setterName);
            invoke(symbol, setter, value);
        }
    }

    /**
     * Returns the method with the specified name in the specified class, or throws an exception if the specified method cannot be
     * found.
     *
     * @param clazz the class to search in
     * @param name the name of the method to search for
     * @return the method with the specified name in the specified class
     */
    private static Method getMethod(Class< ? > clazz, String name) {
        for (Method method : clazz.getMethods()) {
            if (method.getName().equals(name)) {
                return method;
            }
        }
        throw new RuntimeException("Unable to find method: " + name);
    }

    /**
     * Invokes the specified method on the specified object with the specified parameter.
     *
     * @param object the object to invoke the method on
     * @param setter the method to invoke
     * @param parameter the parameter to pass to the method
     * @throws ReflectiveOperationException if there is any reflection error
     * @throws IllegalArgumentException if the specified parameter is not valid
     */
    private static void invoke(Object object, Method setter, Object parameter) throws ReflectiveOperationException,
                    IllegalArgumentException {
        Class< ? > paramType = setter.getParameters()[0].getType();
        if (String.class.equals(paramType)) {
            setter.invoke(object, parameter.toString());
        } else if (int.class.equals(paramType)) {
            setter.invoke(object, Integer.parseInt(parameter.toString()));
        } else {
            throw new RuntimeException("Unknown setter type: " + paramType);
        }
    }

    /**
     * Returns all .properties files in the specified directory, or an empty array if none are found.
     *
     * @param dir the directory to search in
     * @return all .properties files in the specified directory, or an empty array if none are found
     */
    private static File[] getPropertiesFiles(String dir) {

        File[] files = new File(dir).listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".properties");
            }
        });

        if (files != null) {
            return files;
        } else {
            return new File[0];
        }
    }

    /**
     * Verifies that the specified images match.
     *
     * @param expected the expected image to check against
     * @param actual the actual image
     */
    private static void assertEqual(BufferedImage expected, BufferedImage actual) {

        int w = expected.getWidth();
        int h = expected.getHeight();

        Assert.assertEquals("width", w, actual.getWidth());
        Assert.assertEquals("height", h, actual.getHeight());

        int[] expectedPixels = new int[w * h];
        expected.getRGB(0, 0, w, h, expectedPixels, 0, w);

        int[] actualPixels = new int[w * h];
        actual.getRGB(0, 0, w, h, actualPixels, 0, w);

        for (int i = 0; i < expectedPixels.length; i++) {
            int expectedPixel = expectedPixels[i];
            int actualPixel = actualPixels[i];
            if (expectedPixel != actualPixel) {
                int x = i % w;
                int y = i / w;
                throw new ComparisonFailure("pixel at " + x + ", " + y, toHexString(expectedPixel), toHexString(actualPixel));
            }
        }
    }

    /**
     * Finds all test resources and returns the information that JUnit needs to dynamically create the corresponding test cases.
     *
     * @return the test data needed to dynamically create the test cases
     */
    @Parameters(name = "test {index}: {5}: {6}")
    public static List< Object[] > data() {

        String backend = "uk.org.okapibarcode.backend";
        Reflections reflections = new Reflections(backend);
        Set< Class< ? extends Symbol >> symbols = reflections.getSubTypesOf(Symbol.class);

        List< Object[] > data = new ArrayList<>();
        for (Class< ? extends Symbol > symbol : symbols) {
            String symbolName = symbol.getSimpleName().toLowerCase();
            String dir = "src/test/resources/" + backend.replace('.', '/') + "/" + symbolName;
            for (File file : getPropertiesFiles(dir)) {
                String fileBaseName = file.getName().replaceAll(".properties", "");
                File codewordsFile = new File(file.getParentFile(), fileBaseName + ".codewords");
                File pngFile = new File(file.getParentFile(), fileBaseName + ".png");
                File errorFile = new File(file.getParentFile(), fileBaseName + ".error");
                data.add(new Object[] { symbol, file, codewordsFile, pngFile, errorFile, symbolName, fileBaseName });
            }
        }

        return data;
    }
}
