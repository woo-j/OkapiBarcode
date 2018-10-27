/*
 * Copyright 2014-2018 Robin Stuart, Daniel Gredler
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
package uk.org.okapibarcode.backend;

import static uk.org.okapibarcode.backend.HumanReadableAlignment.CENTER;
import static uk.org.okapibarcode.backend.HumanReadableLocation.BOTTOM;
import static uk.org.okapibarcode.backend.HumanReadableLocation.NONE;
import static uk.org.okapibarcode.backend.HumanReadableLocation.TOP;
import static uk.org.okapibarcode.util.Arrays.containsAt;
import static uk.org.okapibarcode.util.Arrays.positionOf;
import static uk.org.okapibarcode.util.Doubles.roughlyEqual;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import uk.org.okapibarcode.output.Java2DRenderer;
import uk.org.okapibarcode.util.EciMode;
import uk.org.okapibarcode.util.Gs1;

/**
 * Generic barcode symbology class.
 *
 * TODO: Setting attributes like module width, font size, etc should probably throw
 * an exception if set *after* encoding has already been completed.
 *
 * TODO: GS1 data is encoded slightly differently depending on whether [AI]data content
 * is used, or if FNC1 escape sequences are used. We may want to make sure that they
 * encode to the same output.
 *
 * @author <a href="mailto:rstuart114@gmail.com">Robin Stuart</a>
 */
public abstract class Symbol {

    public static enum DataType {
        ECI, GS1, HIBC
    }

    protected static final int FNC1 = -1;
    protected static final int FNC2 = -2;
    protected static final int FNC3 = -3;
    protected static final int FNC4 = -4;

    protected static final String FNC1_STRING = "\\<FNC1>";
    protected static final String FNC2_STRING = "\\<FNC2>";
    protected static final String FNC3_STRING = "\\<FNC3>";
    protected static final String FNC4_STRING = "\\<FNC4>";

    private static char[] HIBC_CHAR_TABLE = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
        'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
        'U', 'V', 'W', 'X', 'Y', 'Z', '-', '.', ' ', '$',
        '/', '+', '%' };

    // user-specified values and settings

    protected DataType inputDataType = DataType.ECI;
    protected boolean readerInit;
    protected int default_height = 40;
    protected int quietZoneHorizontal = 0;
    protected int quietZoneVertical = 0;
    protected int moduleWidth = 1;
    protected Font font;
    protected String fontName = "Helvetica";
    protected int fontSize = 8;
    protected HumanReadableLocation humanReadableLocation = BOTTOM;
    protected HumanReadableAlignment humanReadableAlignment = CENTER;
    protected boolean debug = false;
    protected boolean emptyContentAllowed = false;

    // internal state calculated when setContent() is called

    protected String content;
    protected int eciMode = -1;
    protected int[] inputData; // usually bytes (values 0-255), but may also contain FNC flags
    protected String readable = "";
    protected String[] pattern;
    protected int row_count = 0;
    protected int[] row_height;
    protected int symbol_height = 0;
    protected int symbol_width = 0;
    protected String encodeInfo = "";
    protected List< Rectangle2D.Double > rectangles = new ArrayList<>(); // note positions do not account for quiet zones (handled in renderers)
    protected List< TextBox > texts = new ArrayList<>();                 // note positions do not account for quiet zones (handled in renderers)
    protected List< Hexagon > hexagons = new ArrayList<>();              // note positions do not account for quiet zones (handled in renderers)
    protected List< Ellipse2D.Double > target = new ArrayList<>();       // note positions do not account for quiet zones (handled in renderers)

    /**
     * <p>Sets the type of input data. This setting influences what pre-processing is done on
     * data before encoding in the symbol. For example: for <code>GS1</code> mode the AI
     * data will be used to calculate the position of 'FNC1' characters.
     *
     * <p>Valid values are:
     *
     * <ul>
     * <li><code>UTF8</code> (default) Unicode encoding
     * <li><code>LATIN1</code> ISO 8859-1 (Latin-1) encoding
     * <li><code>BINARY</code> Byte encoding mode
     * <li><code>GS1</code> Application Identifier and data pairs in "[AI]DATA" format
     * <li><code>HIBC</code> Health Industry Bar Code number (without check digit)
     * <li><code>ECI</code> Extended Channel Interpretations
     * </ul>
     *
     * @param dataType the type of input data
     */
    public void setDataType(DataType dataType) {
        if (dataType == DataType.GS1 && !gs1Supported()) {
            throw new IllegalArgumentException("This symbology type does not support GS1 data.");
        }
        inputDataType = dataType;
    }

    /**
     * Returns the type of input data in this symbol.
     *
     * @return the type of input data in this symbol
     */
    public DataType getDataType() {
        return inputDataType;
    }

    /**
     * Returns <code>true</code> if this type of symbology supports GS1 data.
     *
     * @return <code>true</code> if this type of symbology supports GS1 data
     */
    protected boolean gs1Supported() {
        return false;
    }

    /**
     * If set to <code>true</code>, the symbol is prefixed with a "Reader Initialization"
     * or "Reader Programming" instruction.
     *
     * @param readerInit whether or not to enable reader initialization
     */
    public void setReaderInit(boolean readerInit) {
        this.readerInit = readerInit;
    }

    /**
     * Returns whether or not reader initialization is enabled.
     *
     * @return whether or not reader initialization is enabled
     */
    public boolean getReaderInit() {
        return readerInit;
    }

    /**
     * Sets the default bar height for this symbol (default value is <code>40</code>).
     *
     * @param barHeight the default bar height for this symbol
     */
    public void setBarHeight(int barHeight) {
        this.default_height = barHeight;
    }

    /**
     * Returns the default bar height for this symbol.
     *
     * @return the default bar height for this symbol
     */
    public int getBarHeight() {
        return default_height;
    }

    /**
     * Sets the module width for this symbol (default value is <code>1</code>).
     *
     * @param moduleWidth the module width for this symbol
     */
    public void setModuleWidth(int moduleWidth) {
        this.moduleWidth = moduleWidth;
    }

    /**
     * Returns the module width for this symbol.
     *
     * @return the module width for this symbol
     */
    public int getModuleWidth() {
        return moduleWidth;
    }

    /**
     * Sets the horizontal quiet zone (white space) added to the left and to the right of this symbol.
     *
     * @param quietZoneHorizontal the horizontal quiet zone (white space) added to the left and to the right of this symbol
     */
    public void setQuietZoneHorizontal(int quietZoneHorizontal) {
        this.quietZoneHorizontal = quietZoneHorizontal;
    }

    /**
     * Returns the horizontal quiet zone (white space) added to the left and to the right of this symbol.
     *
     * @return the horizontal quiet zone (white space) added to the left and to the right of this symbol
     */
    public int getQuietZoneHorizontal() {
        return quietZoneHorizontal;
    }

    /**
     * Sets the vertical quiet zone (white space) added above and below this symbol.
     *
     * @param quietZoneVertical the vertical quiet zone (white space) added above and below this symbol
     */
    public void setQuietZoneVertical(int quietZoneVertical) {
        this.quietZoneVertical = quietZoneVertical;
    }

    /**
     * Returns the vertical quiet zone (white space) added above and below this symbol.
     *
     * @return the vertical quiet zone (white space) added above and below this symbol
     */
    public int getQuietZoneVertical() {
        return quietZoneVertical;
    }

    /**
     * <p>Sets the font to use to render the human-readable text. This is an alternative to setting the
     * {@link #setFontName(String) font name} and {@link #setFontSize(int) font size} separately. May
     * allow some applications to avoid the use of {@link GraphicsEnvironment#registerFont(Font)}
     * when using the {@link Java2DRenderer}.
     *
     * <p>Do not use this method in combination with {@link #setFontName(String)} or {@link #setFontSize(int)}.
     *
     * @param font the font to use to render the human-readable text
     */
    public void setFont(Font font) {
        this.font = font;
        this.fontName = font.getFontName();
        this.fontSize = font.getSize();
    }

    /**
     * Returns the font to use to render the human-readable text.
     *
     * @return the font to use to render the human-readable text
     */
    public Font getFont() {
        return font;
    }

    /**
     * <p>Sets the name of the font to use to render the human-readable text (default value is <code>Helvetica</code>).
     * The specified font name needs to be registered via {@link GraphicsEnvironment#registerFont(Font)} if you are
     * using the {@link Java2DRenderer}. In order to set the font without registering the font with the graphics
     * environment when using the {@link Java2DRenderer}, you may need to use {@link #setFont(Font)} instead.
     *
     * <p>Use this method in combination with {@link #setFontSize(int)}.
     *
     * <p>Do not use this method in combination with {@link #setFont(Font)}.
     *
     * @param fontName the name of the font to use to render the human-readable text
     */
    public void setFontName(String fontName) {
        this.fontName = Objects.requireNonNull(fontName, "font name may not be null");
        this.font = null;
    }

    /**
     * Returns the name of the font to use to render the human-readable text.
     *
     * @return the name of the font to use to render the human-readable text
     */
    public String getFontName() {
        return fontName;
    }

    /**
     * <p>Sets the size of the font to use to render the human-readable text (default value is <code>8</code>).
     *
     * <p>Use this method in combination with {@link #setFontName(String)}.
     *
     * <p>Do not use this method in combination with {@link #setFont(Font)}.
     *
     * @param fontSize the size of the font to use to render the human-readable text
     */
    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
        this.font = null;
    }

    /**
     * Returns the size of the font to use to render the human-readable text.
     *
     * @return the size of the font to use to render the human-readable text
     */
    public int getFontSize() {
        return fontSize;
    }

    /**
     * Gets the width of the encoded symbol, including the horizontal quiet zone.
     *
     * @return the width of the encoded symbol
     */
    public int getWidth() {
        return symbol_width + (2 * quietZoneHorizontal);
    }

    /**
     * Returns the height of the symbol, including the human-readable text, if any, as well as the vertical
     * quiet zone. This height is an approximation, since it is calculated without access to a font engine.
     *
     * @return the height of the symbol, including the human-readable text, if any, as well as the vertical
     *         quiet zone
     */
    public int getHeight() {
        return symbol_height + getHumanReadableHeight() + (2 * quietZoneVertical);
    }

    /**
     * Returns the height of the human-readable text, including the space between the text and other symbols.
     * This height is an approximation, since it is calculated without access to a font engine.
     *
     * @return the height of the human-readable text
     */
    public int getHumanReadableHeight() {
        if (texts.isEmpty()) {
            return 0;
        } else {
            return getTheoreticalHumanReadableHeight();
        }
    }

    /**
     * Returns the height of the human-readable text, assuming this symbol had human-readable text.
     *
     * @return the height of the human-readable text, assuming this symbol had human-readable text
     */
    protected int getTheoreticalHumanReadableHeight() {
        return (int) Math.ceil(fontSize * 1.2); // 0.2 space between bars and text
    }

    /**
     * Returns a human readable summary of the decisions made by the encoder when creating a symbol.
     *
     * @return a human readable summary of the decisions made by the encoder when creating a symbol
     */
    public String getEncodeInfo() {
        return encodeInfo;
    }

    /**
     * Returns the ECI mode used by this symbol. The ECI mode is chosen automatically during encoding
     * if the symbol data type has been set to {@link DataType#ECI}. If this symbol does not use ECI,
     * this method will return <code>-1</code>.
     *
     * @return the ECI mode used by this symbol
     * @see #eciProcess()
     */
    public int getEciMode() {
        return eciMode;
    }

    /**
     * Sets the location of the human-readable text (default value is {@link HumanReadableLocation#BOTTOM}).
     *
     * @param humanReadableLocation the location of the human-readable text
     */
    public void setHumanReadableLocation(HumanReadableLocation humanReadableLocation) {
        this.humanReadableLocation = humanReadableLocation;
    }

    /**
     * Returns the location of the human-readable text.
     *
     * @return the location of the human-readable text
     */
    public HumanReadableLocation getHumanReadableLocation() {
        return humanReadableLocation;
    }

    /**
     * Sets the text alignment of the human-readable text (default value is {@link HumanReadableAlignment#CENTER}).
     *
     * @param humanReadableAlignment the text alignment of the human-readable text
     */
    public void setHumanReadableAlignment(HumanReadableAlignment humanReadableAlignment) {
        this.humanReadableAlignment = humanReadableAlignment;
    }

    /**
     * Returns the text alignment of the human-readable text.
     *
     * @return the text alignment of the human-readable text
     */
    public HumanReadableAlignment getHumanReadableAlignment() {
        return humanReadableAlignment;
    }

    /**
     * Returns render information about the rectangles in this symbol.
     *
     * @return render information about the rectangles in this symbol
     */
    public List< Rectangle2D.Double > getRectangles() {
        return rectangles;
    }

    /**
     * Returns render information about the text elements in this symbol.
     *
     * @return render information about the text elements in this symbol
     */
    public List< TextBox > getTexts() {
        return texts;
    }

    /**
     * Returns render information about the hexagons in this symbol.
     *
     * @return render information about the hexagons in this symbol
     */
    public List< Hexagon > getHexagons() {
        return hexagons;
    }

    /**
     * Returns render information about the target circles in this symbol.
     *
     * @return render information about the target circles in this symbol
     */
    public List< Ellipse2D.Double > getTarget() {
        return target;
    }

    protected static String bin2pat(String bin) {

        int len = 0;
        boolean black = true;
        StringBuilder pattern = new StringBuilder(bin.length());

        for (int i = 0; i < bin.length(); i++) {
            if (black) {
                if (bin.charAt(i) == '1') {
                    len++;
                } else {
                    black = false;
                    pattern.append((char) (len + '0'));
                    len = 1;
                }
            } else {
                if (bin.charAt(i) == '0') {
                    len++;
                } else {
                    black = true;
                    pattern.append((char) (len + '0'));
                    len = 1;
                }
            }
        }

        pattern.append((char) (len + '0'));
        return pattern.toString();
    }

    /**
     * Sets whether or not empty content is allowed. Some symbologies may be able to generate empty symbols when no data is
     * present, though this is not usually desired behavior. The default value is <code>false</code> (no empty content allowed).
     *
     * @param emptyContentAllowed whether or not empty content is allowed
     */
    public void setEmptyContentAllowed(boolean emptyContentAllowed) {
        this.emptyContentAllowed = emptyContentAllowed;
    }

    /**
     * Returns whether or not empty content is allowed.
     *
     * @return whether or not empty content is allowed
     */
    public boolean getEmptyContentAllowed() {
        return emptyContentAllowed;
    }

    /**
     * Sets the data to be encoded and triggers encoding. Input data will be assumed
     * to be of the type set by {@link #setDataType(DataType)}.
     *
     * @param data the data to encode
     * @throws OkapiException if no data or data is invalid
     */
    public void setContent(String data) {

        if (data == null) {
            data = "";
        }

        encodeInfo = "";

        switch (inputDataType) {
            case GS1:
                content = Gs1.verify(data, FNC1_STRING);
                readable = data.replace('[', '(').replace(']', ')');
                break;
            case HIBC:
                content = hibcProcess(data);
                break;
            default:
                content = data;
                break;
        }

        if (content.isEmpty() && !emptyContentAllowed) {
            throw new OkapiException("No input data");
        }

        encode();
        plotSymbol();
        mergeVerticalBlocks();
    }

    /**
     * Returns the content encoded by this symbol.
     *
     * @return the content encoded by this symbol
     */
    public String getContent() {
        return content;
    }

    /**
     * Returns the human-readable text for this symbol.
     *
     * @return the human-readable text for this symbol
     */
    public String getHumanReadableText() {
        return readable;
    }

    /**
     * Chooses the ECI mode most suitable for the content of this symbol.
     */
    protected void eciProcess() {

        EciMode eci = EciMode.of(content, "ISO8859_1",    3)
                             .or(content, "ISO8859_2",    4)
                             .or(content, "ISO8859_3",    5)
                             .or(content, "ISO8859_4",    6)
                             .or(content, "ISO8859_5",    7)
                             .or(content, "ISO8859_6",    8)
                             .or(content, "ISO8859_7",    9)
                             .or(content, "ISO8859_8",    10)
                             .or(content, "ISO8859_9",    11)
                             .or(content, "ISO8859_10",   12)
                             .or(content, "ISO8859_11",   13)
                             .or(content, "ISO8859_13",   15)
                             .or(content, "ISO8859_14",   16)
                             .or(content, "ISO8859_15",   17)
                             .or(content, "ISO8859_16",   18)
                             .or(content, "Windows_1250", 21)
                             .or(content, "Windows_1251", 22)
                             .or(content, "Windows_1252", 23)
                             .or(content, "Windows_1256", 24)
                             .or(content, "SJIS",         20)
                             .or(content, "UTF8",         26);

        if (EciMode.NONE.equals(eci)) {
            throw new OkapiException("Unable to determine ECI mode.");
        }

        eciMode = eci.mode;
        inputData = toBytes(content, eci.charset);

        encodeInfo += "ECI Mode: " + eci.mode + "\n";
        encodeInfo += "ECI Charset: " + eci.charset.name() + "\n";
    }

    protected static int[] toBytes(String s, Charset charset, int... suffix) {

        if (!charset.newEncoder().canEncode(s)) {
            return null;
        }

        byte[] fnc1 = FNC1_STRING.getBytes(charset);
        byte[] fnc2 = FNC2_STRING.getBytes(charset);
        byte[] fnc3 = FNC3_STRING.getBytes(charset);
        byte[] fnc4 = FNC4_STRING.getBytes(charset);

        byte[] bytes = s.getBytes(charset);
        int[] data = new int[bytes.length + suffix.length];

        int i = 0, j = 0;
        for (; i < bytes.length; i++, j++) {
            if (containsAt(bytes, fnc1, i)) {
                data[j] = FNC1;
                i += fnc1.length - 1;
            } else if (containsAt(bytes, fnc2, i)) {
                data[j] = FNC2;
                i += fnc1.length - 1;
            } else if (containsAt(bytes, fnc3, i)) {
                data[j] = FNC3;
                i += fnc1.length - 1;
            } else if (containsAt(bytes, fnc4, i)) {
                data[j] = FNC4;
                i += fnc1.length - 1;
            } else {
                data[j] = bytes[i] & 0xff;
            }
        }

        int k = 0;
        for (; k < suffix.length; k++) {
            data[j + k] = suffix[k];
        }

        if (j + k < i) {
            data = Arrays.copyOf(data, j + k);
        }

        return data;
    }

    protected abstract void encode();

    protected void plotSymbol() {
        int xBlock, yBlock;
        double x, y, w, h;
        boolean black;

        rectangles.clear();
        texts.clear();

        int baseY;
        if (humanReadableLocation == TOP) {
            baseY = getTheoreticalHumanReadableHeight();
        } else {
            baseY = 0;
        }

        h = 0;
        y = baseY;

        for (yBlock = 0; yBlock < row_count; yBlock++) {
            black = true;
            x = 0;
            for (xBlock = 0; xBlock < pattern[yBlock].length(); xBlock++) {
                char c = pattern[yBlock].charAt(xBlock);
                w = getModuleWidth(c - '0') * moduleWidth;
                if (black) {
                    if (row_height[yBlock] == -1) {
                        h = default_height;
                    } else {
                        h = row_height[yBlock];
                    }
                    if (w != 0 && h != 0) {
                        Rectangle2D.Double rect = new Rectangle2D.Double(x, y, w, h);
                        rectangles.add(rect);
                    }
                    if (x + w > symbol_width) {
                        symbol_width = (int) Math.ceil(x + w);
                    }
                }
                black = !black;
                x += w;
            }
            if ((y - baseY + h) > symbol_height) {
                symbol_height = (int) Math.ceil(y - baseY + h);
            }
            y += h;
        }

        if (humanReadableLocation != NONE && !readable.isEmpty()) {
            double baseline;
            if (humanReadableLocation == TOP) {
                baseline = fontSize;
            } else {
                baseline = symbol_height + fontSize;
            }
            texts.add(new TextBox(0, baseline, symbol_width, readable, humanReadableAlignment));
        }
    }

    /**
     * Returns the module width to use for the specified original module width, taking into account any module width ratio
     * customizations. Intended to be overridden by subclasses that support such module width ratio customization.
     *
     * @param originalWidth the original module width
     * @return the module width to use for the specified original module width
     */
    protected double getModuleWidth(int originalWidth) {
        return originalWidth;
    }

    /**
     * Search for rectangles which have the same width and x position, and
     * which join together vertically and merge them together to reduce the
     * number of rectangles needed to describe a symbol.
     */
    protected void mergeVerticalBlocks() {
        for(int i = 0; i < rectangles.size() - 1; i++) {
            for(int j = i + 1; j < rectangles.size(); j++) {
                Rectangle2D.Double firstRect = rectangles.get(i);
                Rectangle2D.Double secondRect = rectangles.get(j);
                if (roughlyEqual(firstRect.x, secondRect.x) && roughlyEqual(firstRect.width, secondRect.width)) {
                    if (roughlyEqual(firstRect.y + firstRect.height, secondRect.y)) {
                        firstRect.height += secondRect.height;
                        rectangles.set(i, firstRect);
                        rectangles.remove(j);
                    }
                }
            }
        }
    }

    /**
     * Adds the HIBC prefix and check digit to the specified data, returning the resultant data string.
     *
     * @see <a href="https://sourceforge.net/p/zint/code/ci/master/tree/backend/library.c">Corresponding Zint code</a>
     */
    private String hibcProcess(String source) {

        // HIBC 2.6 allows up to 110 characters, not including the "+" prefix or the check digit
        if (source.length() > 110) {
            throw new OkapiException("Data too long for HIBC LIC");
        }

        source = source.toUpperCase();
        if (!source.matches("[A-Z0-9-\\. \\$/+\\%]+?")) {
            throw new OkapiException("Invalid characters in input");
        }

        int counter = 41;
        for (int i = 0; i < source.length(); i++) {
            counter += positionOf(source.charAt(i), HIBC_CHAR_TABLE);
        }
        counter = counter % 43;

        char checkDigit = HIBC_CHAR_TABLE[counter];

        encodeInfo += "HIBC Check Digit Counter: " + counter + "\n";
        encodeInfo += "HIBC Check Digit: " + checkDigit + "\n";

        return "+" + source + checkDigit;
    }

    /**
     * Returns the intermediate coding of this bar code. Symbol types that use the test
     * infrastructure should override this method.
     *
     * @return the intermediate coding of this bar code
     */
    protected int[] getCodewords() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns this bar code's pattern, converted into a set of corresponding codewords.
     * Useful for bar codes that encode their content as a pattern.
     *
     * @param size the number of digits in each codeword
     * @return this bar code's pattern, converted into a set of corresponding codewords
     */
    protected int[] getPatternAsCodewords(int size) {
        if (size >= 10) {
            throw new IllegalArgumentException("Pattern groups of 10 or more digits are likely to be too large to parse as integers.");
        }
        if (pattern == null || pattern.length == 0) {
            return new int[0];
        } else {
            int count = (int) Math.ceil(pattern[0].length() / (double) size);
            int[] codewords = new int[pattern.length * count];
            for (int i = 0; i < pattern.length; i++) {
                String row = pattern[i];
                for (int j = 0; j < count; j++) {
                    int substringStart = j * size;
                    int substringEnd = Math.min((j + 1) * size, row.length());
                    codewords[(i * count) + j] = Integer.parseInt(row.substring(substringStart, substringEnd));
                }
            }
            return codewords;
        }
    }
}
