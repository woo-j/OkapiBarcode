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
import static uk.org.okapibarcode.util.Doubles.roughlyEqual;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import uk.org.okapibarcode.util.EciMode;

/**
 * Generic barcode symbology class.
 *
 * TODO: Setting attributes like module width, font size, etc should probably throw
 * an exception if set *after* encoding has already been completed.
 *
 * @author <a href="mailto:rstuart114@gmail.com">Robin Stuart</a>
 */
public abstract class Symbol {

    public static enum DataType {
        UTF8, LATIN1, BINARY, GS1, HIBC, ECI
    }

    // user-specified values and settings

    protected DataType inputDataType = DataType.ECI;
    protected boolean readerInit;
    protected int default_height = 40;
    protected int quietZoneHorizontal = 0;
    protected int quietZoneVertical = 0;
    protected int moduleWidth = 1;
    protected String fontName = "Helvetica";
    protected int fontSize = 8;
    protected HumanReadableLocation humanReadableLocation = BOTTOM;
    protected HumanReadableAlignment humanReadableAlignment = CENTER;
    protected boolean debug = false;

    // internal state calculated when setContent() is called

    protected String content;
    protected int eciMode = 3;
    protected byte[] inputBytes;
    protected String readable = "";
    protected String[] pattern;
    protected int row_count = 0;
    protected int[] row_height;
    protected int symbol_height = 0;
    protected int symbol_width = 0;
    protected String encodeInfo = "";
    protected List< Rectangle2D.Double > rectangles = new ArrayList<>();
    protected List< TextBox > texts = new ArrayList<>();
    protected List< Hexagon > hexagons = new ArrayList<>();
    protected List< Ellipse2D.Double > target = new ArrayList<>();

    /**
     * Sets the type of input data. This setting influences what
     * pre-processing is done on data before encoding in the symbol.
     * For example: for <code>GS1</code> mode the AI data will be used to
     * calculate the position of 'FNC1' characters.
     * Valid values are:
     * <ul>
     * <li><code>UTF8</code> (default) Unicode encoding
     * <li><code>LATIN1</code> ISO 8859-1 (Latin-1) encoding
     * <li><code>BINARY</code> Byte encoding mode
     * <li><code>GS1</code> Application Identifier and data pairs in "[AI]DATA" format
     * <li><code>HIBC</code> Health Industry Bar Code number (without check digit)
     * <li><code>ECI</code> Extended Channel Interpretations
     * </ul>
     * @param dataType the type of input data
     */
    public void setDataType(DataType dataType) {
        inputDataType = dataType;
    }

    /**
     * Prefixes symbol data with a "Reader Initialisation" or "Reader
     * Programming" instruction.
     */
    public final void setReaderInit() {
        readerInit = true;
    }

    /**
     * Removes "Reader Initialisation" or "Reader Programming" instruction
     * from symbol data.
     */
    public final void unsetReaderInit() {
        readerInit = false;
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
     * Sets the name of the font to use to render the human-readable text (default value is <code>Helvetica</code>).
     *
     * @param fontName the name of the font to use to render the human-readable text
     */
    public void setFontName(String fontName) {
        this.fontName = fontName;
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
     * Sets the size of the font to use to render the human-readable text (default value is <code>8</code>).
     *
     * @param fontSize the size of the font to use to render the human-readable text
     */
    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
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
        return Collections.unmodifiableList(rectangles);
    }

    /**
     * Returns render information about the text elements in this symbol.
     *
     * @return render information about the text elements in this symbol
     */
    public List< TextBox > getTexts() {
        return Collections.unmodifiableList(texts);
    }

    /**
     * Returns render information about the hexagons in this symbol.
     *
     * @return render information about the hexagons in this symbol
     */
    public List< Hexagon > getHexagons() {
        return Collections.unmodifiableList(hexagons);
    }

    /**
     * Returns render information about the target circles in this symbol.
     *
     * @return render information about the target circles in this symbol
     */
    public List< Ellipse2D.Double > getTarget() {
        return Collections.unmodifiableList(target);
    }

    protected int positionOf(char c, char[] chars) {

        for (int i = 0; i < chars.length; i++) {
            if (c == chars[i]) {
                return i;
            }
        }

        throw new OkapiException("Unable to find character '" + c + "' in character array.");
    }

    protected String bin2pat(String bin) {

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
     * Sets the data to be encoded. Input data will be assumed to be of the type set
     * by {@link #setDataType(DataType)}.
     *
     * @param data the data to encode
     * @throws OkapiException if no data or data is invalid
     */
    public void setContent(String data) {

        switch (inputDataType) {
            case GS1:
                content = gs1SanityCheck(data);
                readable = data.replace('[', '(').replace(']', ')');
                break;
            case HIBC:
                content = hibcProcess(data);
                break;
            default:
                content = data;
                break;
        }

        if (content.isEmpty()) {
            throw new OkapiException("No input data");
        }

        encode();
        plotSymbol();
    }

    public String getContent() {
        return content;
    }

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
        inputBytes = content.getBytes(eci.charset);
        encodeInfo += "Encoding in " + eci.charset.name() + " character set\n";
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

        mergeVerticalBlocks();

        if (humanReadableLocation != NONE && !readable.isEmpty()) {
            double baseline;
            if (humanReadableLocation == TOP) {
                baseline = fontSize;
            } else {
                baseline = getHeight() + fontSize;
            }
            texts.add(new TextBox(0, baseline, symbol_width, readable));
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

    protected String gs1SanityCheck(String source) {
        // Enforce compliance with GS1 General Specification
        // http://www.gs1.org/docs/gsmp/barcodes/GS1_General_Specifications.pdf

        String reduced = "";

        int i, j, last_ai;
        boolean ai_latch;
        String ai_string;
        int bracket_level, max_bracket_level, ai_length, max_ai_length, min_ai_length;
        int[] ai_value = new int[100];
        int[] ai_location = new int[100];
        int ai_count;
        int[] data_location = new int[100];
        int[] data_length = new int[100];
        int src_len = source.length();
        int error_latch;

        /* Detect extended ASCII characters */
        for (i = 0; i < src_len; i++) {
            if (source.charAt(i) >= 128) {
                throw new OkapiException("Extended ASCII characters are not supported by GS1");
            }
            if (source.charAt(i) < 32) {
                throw new OkapiException("Control characters are not supported by GS1");
            }
        }

        if (source.charAt(0) != '[') {
            throw new OkapiException("Data does not start with an AI");
        }

        /* Check the position of the brackets */
        bracket_level = 0;
        max_bracket_level = 0;
        ai_length = 0;
        max_ai_length = 0;
        min_ai_length = 5;
        j = 0;
        ai_latch = false;
        for (i = 0; i < src_len; i++) {
            ai_length += j;
            if (((j == 1) && (source.charAt(i) != ']'))
                    && ((source.charAt(i) < '0') || (source.charAt(i) > '9'))) {
                ai_latch = true;
            }
            if (source.charAt(i) == '[') {
                bracket_level++;
                j = 1;
            }
            if (source.charAt(i) == ']') {
                bracket_level--;
                if (ai_length < min_ai_length) {
                    min_ai_length = ai_length;
                }
                j = 0;
                ai_length = 0;
            }
            if (bracket_level > max_bracket_level) {
                max_bracket_level = bracket_level;
            }
            if (ai_length > max_ai_length) {
                max_ai_length = ai_length;
            }
        }
        min_ai_length--;

        if (bracket_level != 0) {
            /* Not all brackets are closed */
            throw new OkapiException("Malformed AI in input data (brackets don't match)");
        }

        if (max_bracket_level > 1) {
            /* Nested brackets */
            throw new OkapiException("Found nested brackets in input data");
        }

        if (max_ai_length > 4) {
            /* AI is too long */
            throw new OkapiException("Invalid AI in input data (AI too long)");
        }

        if (min_ai_length <= 1) {
            /* AI is too short */
            throw new OkapiException("Invalid AI in input data (AI too short)");
        }

        if (ai_latch) {
            /* Non-numeric data in AI */
            throw new OkapiException("Invalid AI in input data (non-numeric characters in AI)");
        }

        ai_count = 0;
        for (i = 1; i < src_len; i++) {
            if (source.charAt(i - 1) == '[') {
                ai_location[ai_count] = i;
                ai_value[ai_count] = 0;
                for (j = 0; source.charAt(i + j) != ']'; j++) {
                    ai_value[ai_count] *= 10;
                    ai_value[ai_count] += Character.getNumericValue(source.charAt(i + j));
                }
                ai_count++;
            }
        }

        for (i = 0; i < ai_count; i++) {
            data_location[i] = ai_location[i] + 3;
            if (ai_value[i] >= 100) {
                data_location[i]++;
            }
            if (ai_value[i] >= 1000) {
                data_location[i]++;
            }
            data_length[i] = source.length() - data_location[i];
            for(j = source.length() - 1; j >= data_location[i]; j--) {
                if (source.charAt(j) == '[') {
                    data_length[i] = j - data_location[i];
                }
            }
        }

        for (i = 0; i < ai_count; i++) {
            if (data_length[i] == 0) {
                /* No data for given AI */
                throw new OkapiException("Empty data field in input data");
            }
        }

        error_latch = 0;
        ai_string = "";
        for (i = 0; i < ai_count; i++) {
            switch (ai_value[i]) {
            case 0:
                if (data_length[i] != 18) {
                    error_latch = 1;
                }
                break;
            case 1:
            case 2:
            case 3:
                if (data_length[i] != 14) {
                    error_latch = 1;
                }
                break;
            case 4:
                if (data_length[i] != 16) {
                    error_latch = 1;
                }
                break;
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
            case 19:
                if (data_length[i] != 6) {
                    error_latch = 1;
                }
                break;
            case 20:
                if (data_length[i] != 2) {
                    error_latch = 1;
                }
                break;
            case 23:
            case 24:
            case 25:
            case 39:
            case 40:
            case 41:
            case 42:
            case 70:
            case 80:
            case 81:
                error_latch = 2;
                break;
            }
            if (
            ((ai_value[i] >= 100) && (ai_value[i] <= 179))
                    || ((ai_value[i] >= 1000) && (ai_value[i] <= 1799))
                    || ((ai_value[i] >= 200) && (ai_value[i] <= 229))
                    || ((ai_value[i] >= 2000) && (ai_value[i] <= 2299))
                    || ((ai_value[i] >= 300) && (ai_value[i] <= 309))
                    || ((ai_value[i] >= 3000) && (ai_value[i] <= 3099))
                    || ((ai_value[i] >= 31) && (ai_value[i] <= 36))
                    || ((ai_value[i] >= 310) && (ai_value[i] <= 369))) {
                error_latch = 2;
            }
            if ((ai_value[i] >= 3100) && (ai_value[i] <= 3699) && data_length[i] != 6) {
                error_latch = 1;
            }
            if (
            ((ai_value[i] >= 370) && (ai_value[i] <= 379))
                    || ((ai_value[i] >= 3700) && (ai_value[i] <= 3799))) {
                error_latch = 2;
            }
            if ((ai_value[i] >= 410) && (ai_value[i] <= 415)) {
                if (data_length[i] != 13) {
                    error_latch = 1;
                }
            }
            if (
            ((ai_value[i] >= 4100) && (ai_value[i] <= 4199))
                    || ((ai_value[i] >= 700) && (ai_value[i] <= 703))
                    || ((ai_value[i] >= 800) && (ai_value[i] <= 810))
                    || ((ai_value[i] >= 900) && (ai_value[i] <= 999))
                    || ((ai_value[i] >= 9000) && (ai_value[i] <= 9999))) {
                error_latch = 2;
            }

            if (error_latch == 1) {
                throw new OkapiException("Invalid data length for AI");
            }

            if (error_latch == 2) {
                throw new OkapiException("Invalid AI value");
            }
        }

        /* Resolve AI data - put resulting string in 'reduced' */
        j = 0;
        last_ai = 0;
        ai_latch = false;
        for (i = 0; i < src_len; i++) {
            if ((source.charAt(i) != '[') && (source.charAt(i) != ']')) {
                reduced += source.charAt(i);
            }
            if (source.charAt(i) == '[') {
                /* Start of an AI string */
                if (ai_latch) {
                    reduced += '[';
                }
                last_ai = (10 * Character.getNumericValue(source.charAt(i + 1)))
                        + Character.getNumericValue(source.charAt(i + 2));
                if ( ((last_ai >= 0) && (last_ai <= 4))
                        || ((last_ai >= 11) && (last_ai <= 20))
                        || (last_ai == 23) /* legacy support - see 5.3.8.2.2 */
                        || ((last_ai >= 31) && (last_ai <= 36))
                        || (last_ai == 41)) {
                    // The end of the current data block doesn't need FNC1
                    ai_latch = false;
                } else {
                    // The end of the current data block does need FNC1
                    ai_latch = true;
                }
            }
            /* The ']' character is simply dropped from the input */
        }

        /* the character '[' in the reduced string refers to the FNC1 character */
        return reduced;
    }

    protected String hibcProcess(String source) {

        char[] hibcCharTable = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
            'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
            'U', 'V', 'W', 'X', 'Y', 'Z', '-', '.', ' ', '$',
            '/', '+', '%' };

        int counter, i;
        String to_process;
        char check_digit;

        if(source.length() > 36) {
            throw new OkapiException("Data too long for HIBC LIC");
        }

        source = source.toUpperCase();
        if (!source.matches("[A-Z0-9-\\. \\$/+\\%]+?")) {
            throw new OkapiException("Invalid characters in input");
        }

        counter = 41;
        for (i = 0; i < source.length(); i++) {
            counter += positionOf(source.charAt(i), hibcCharTable);
        }
        counter = counter % 43;

        if (counter < 10) {
            check_digit = (char) (counter + '0');
        } else {
            if (counter < 36) {
                check_digit = (char) ((counter - 10) + 'A');
            } else {
                switch (counter) {
                    case 36: check_digit = '-'; break;
                    case 37: check_digit = '.'; break;
                    case 38: check_digit = ' '; break;
                    case 39: check_digit = '$'; break;
                    case 40: check_digit = '/'; break;
                    case 41: check_digit = '+'; break;
                    case 42: check_digit = '%'; break;
                    default: check_digit = ' '; break; /* Keep compiler happy */
                }
            }
        }

        encodeInfo += "HIBC Check Digit: " + counter + " (" + check_digit + ")\n";

        to_process = "+" + source + check_digit;
        return to_process;
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

    /**
     * Inserts the specified array into the specified original array at the specified index.
     *
     * @param original the original array into which we want to insert another array
     * @param index the index at which we want to insert the array
     * @param inserted the array that we want to insert
     * @return the combined array
     */
    protected static int[] insert(int[] original, int index, int[] inserted) {
        int[] modified = new int[original.length + inserted.length];
        System.arraycopy(original, 0, modified, 0, index);
        System.arraycopy(inserted, 0, modified, index, inserted.length);
        System.arraycopy(original, index, modified, index + inserted.length, modified.length - index - inserted.length);
        return modified;
    }

    /**
     * Returns true if the specified array contains the specified value.
     *
     * @param values the array to check in
     * @param value the value to check for
     * @return true if the specified array contains the specified value
     */
    protected static boolean contains(int[] values, int value) {
        for (int i = 0; i < values.length; i++) {
            if (values[i] == value) {
                return true;
            }
        }
        return false;
    }
}
