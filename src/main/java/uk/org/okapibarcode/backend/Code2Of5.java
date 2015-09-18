/*
 * Copyright 2014 Robin Stuart
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

import static uk.org.okapibarcode.backend.HumanReadableLocation.NONE;
import static uk.org.okapibarcode.backend.HumanReadableLocation.TOP;

import java.awt.geom.Rectangle2D;

/**
 * Implements the Code 2 of 5 family of barcode standards.
 *
 * @author <a href="mailto:jakel2006@me.com">Robert Elliott</a>
 */
public class Code2Of5 extends Symbol {

    private static enum ToFMode {
        MATRIX, INDUSTRIAL, IATA, DATA_LOGIC, INTERLEAVED, ITF14, DPLEIT, DPIDENT
    }

    private static final String[] C25_MATRIX_TABLE = {
        "113311", "311131", "131131", "331111", "113131", "313111", "133111", "111331", "311311", "131311"
    };

    private static final String[] C25_INDUSTRIAL_TABLE = {
        "1111313111", "3111111131", "1131111131", "3131111111", "1111311131", "3111311111", "1131311111", "1111113131", "3111113111", "1131113111"
    };

    private static final String[] C25_INTERLEAVED_TABLE = {
        "11331", "31113", "13113", "33111", "11313", "31311", "13311", "11133", "31131", "13131"
    };

    /** The 2-of-5 mode. */
    private ToFMode mode = ToFMode.MATRIX;

    /** Ratio of wide bar width to narrow bar width. */
    private double moduleWidthRatio = 3;

    /**
     * Sets the ratio of wide bar width to narrow bar width. Valid values are usually
     * between {@code 2} and {@code 3}. The default value is {@code 3}.
     *
     * @param moduleWidthRatio the ratio of wide bar width to narrow bar width
     */
    public void setModuleWidthRatio(double moduleWidthRatio) {
        this.moduleWidthRatio = moduleWidthRatio;
    }

    /**
     * Returns the ratio of wide bar width to narrow bar width.
     *
     * @return the ratio of wide bar width to narrow bar width
     */
    public double getModuleWidthRatio() {
        return moduleWidthRatio;
    }

    /**
     * Select Standard Code 2 of 5 mode, also known as Code 2 of 5 Matrix. (default)
     * Encodes any length numeric input (digits 0-9).
     */
    public void setMatrixMode() {
        mode = ToFMode.MATRIX;
    }

    /**
     * Select Industrial Code 2 of 5 which can encode any length numeric input
     * (digits 0-9) and does not include a check digit.
     */
    public void setIndustrialMode() {
        mode = ToFMode.INDUSTRIAL;
    }

    /**
     * Select International Air Transport Agency variation of Code 2 of 5.
     * Encodes any length numeric input (digits 0-9) and does not include
     * a check digit.
     */
    public void setIATAMode() {
        mode = ToFMode.IATA;
    }

    /**
     * Select Code 2 of 5 Data Logic. Encodes any length numeric input
     * (digits 0-9) and does not include a check digit.
     */
    public void setDataLogicMode() {
        mode = ToFMode.DATA_LOGIC;
    }

    /**
     * Select Interleaved Code 2 of 5. encodes pairs of numbers, and so can
     * only encode an even number of digits (0-9). If an odd number of digits
     * is entered a leading zero is added. No check digit is calculated.
     */
    public void setInterleavedMode() {
        mode = ToFMode.INTERLEAVED;
    }

    /**
     * Select ITF-14, also known as UPC Shipping Container Symbol or Case Code.
     * Requires a 13 digit numeric input (digits 0-9). One modulo-10 check
     * digit is calculated.
     */
    public void setITF14Mode() {
        mode = ToFMode.ITF14;
    }

    /**
     * Select Deutsche Post Leitcode. Requires a 13-digit numerical input.
     * Check digit is calculated.
     */
    public void setDPLeitMode() {
        mode = ToFMode.DPLEIT;
    }

    /**
     * Select Deutsche Post Identcode. Requires an 11-digit numerical input.
     * Check digit is calculated.
     */
    public void setDPIdentMode() {
        mode = ToFMode.DPIDENT;
    }

    @Override
    public boolean encode() {
        boolean retval = false;

        switch (mode) {
        case MATRIX:
            retval = dataMatrixTof();
            break;
        case INDUSTRIAL:
            retval = industrialTof();
            break;
        case IATA:
            retval = iataTof();
            break;
        case INTERLEAVED:
            retval = interleavedTof();
            break;
        case DATA_LOGIC:
            retval = dataLogic();
            break;
        case ITF14:
            retval = itf14();
            break;
        case DPLEIT:
            retval = deutschePostLeitcode();
            break;
        case DPIDENT:
            retval = deutschePostIdentcode();
            break;
        }

        if (retval) {
            plotSymbol();
        }

        return retval;
    }

    private boolean dataMatrixTof() {

        if (!(content.matches("[0-9]+"))) {
            error_msg = "Invalid characters in input";
            return false;
        }

        String dest = "311111";

        for (int i = 0; i < content.length(); i++) {
            dest += C25_MATRIX_TABLE[Character.getNumericValue(content.charAt(i))];
        }
        dest += "31111";

        readable = content;
        pattern = new String[] { dest };
        row_count = 1;
        row_height = new int[] { -1 };
        return true;
    }

    private boolean industrialTof() {
        if (!(content.matches("[0-9]+"))) {
            error_msg = "Invalid characters in input";
            return false;
        }

        String dest = "313111";
        readable = content;

        for (int i = 0; i < readable.length(); i++) {
            dest += C25_INDUSTRIAL_TABLE[Character.getNumericValue(readable.charAt(i))];
        }

        dest += "31113";

        pattern = new String[] { dest };
        row_count = 1;
        row_height = new int[] { -1 };
        return true;
    }

    private boolean iataTof() {
        if (!(content.matches("[0-9]+"))) {
            error_msg = "Invalid characters in input";
            return false;
        }

        String dest = "1111";
        readable = content;

        for (int i = 0; i < readable.length(); i++) {
            dest += C25_INDUSTRIAL_TABLE[Character.getNumericValue(readable.charAt(i))];
        }

        dest += "311";

        pattern = new String[] { dest };
        row_count = 1;
        row_height = new int[] { -1 };
        return true;
    }

    private boolean dataLogic() {
        if (!(content.matches("[0-9]+"))) {
            error_msg = "Invalid characters in input";
            return false;
        }

        String dest = "1111";
        readable = content;

        for (int i = 0; i < readable.length(); i++) {
            dest += C25_MATRIX_TABLE[Character.getNumericValue(readable.charAt(i))];
        }

        dest += "311";

        pattern = new String[] { dest };
        row_count = 1;
        row_height = new int[] { -1 };
        return true;
    }

    private boolean interleavedTof() {
        int i;
        String dest;

        if ((content.length() & 1) == 0) {
            readable = content;
        } else {
            readable = "0" + content;
        }
        if (!(readable.matches("[0-9]+"))) {
            error_msg = "Invalid characters in input";
            return false;
        }

        dest = "1111";

        for (i = 0; i < readable.length(); i += 2) {
            dest += interlace(i, i + 1);
        }

        dest += "311";

        pattern = new String[] { dest };
        row_count = 1;
        row_height = new int[] { -1 };
        return true;
    }

    private String interlace(int x, int y) {
        char a = readable.charAt(x);
        char b = readable.charAt(y);

        String one = C25_INTERLEAVED_TABLE[Character.getNumericValue(a)];
        String two = C25_INTERLEAVED_TABLE[Character.getNumericValue(b)];
        String f = "";

        for (int i = 0; i < 5; i++) {
            f += one.charAt(i);
            f += two.charAt(i);
        }

        return f;
    }

    private boolean itf14() {
        int i, count = 0;
        int input_length = content.length();
        String dest;

        if (!(content.matches("[0-9]+"))) {
            error_msg = "Invalid characters in input";
            return false;
        }

        if (input_length > 13) {
            error_msg = "Input data too long";
            return false;
        }

        readable = "";
        for (i = input_length; i < 13; i++) {
            readable += "0";
        }

        readable += content;
        for (i = 12; i >= 0; i--) {
            count += readable.charAt(i) - '0';

            if ((i & 1) == 0) {
                count += 2 * (readable.charAt(i) - '0');
            }
        }

        readable += (char)(((10 - (count % 10)) % 10) + '0');
        encodeInfo += "Check Digit: " + (char)(((10 - (count % 10)) % 10) + '0');
        encodeInfo += '\n';

        dest = "1111";

        for (i = 0; i < readable.length(); i += 2) {
            dest += interlace(i, i + 1);
        }

        dest += "311";

        pattern = new String[] { dest };
        row_count = 1;
        row_height = new int[] { -1 };
        return true;
    }

    private boolean deutschePostLeitcode() {
        int i, count = 0;
        int input_length = content.length();
        String dest;

        if (!(content.matches("[0-9]+"))) {
            error_msg = "Invalid characters in input";
            return false;
        }

        if (input_length > 13) {
            error_msg = "Input data too long";
            return false;
        }

        readable = "";
        for (i = input_length; i < 13; i++) {
            readable += "0";
        }

        readable += content;

        for (i = 12; i >= 0; i--) {
            count += 4 * (readable.charAt(i) - '0');

            if ((i & 1) != 0) {
                count += 5 * (readable.charAt(i) - '0');
            }
        }

        readable += (char)(((10 - (count % 10)) % 10) + '0');
        encodeInfo += "Check digit: " + (char)(((10 - (count % 10)) % 10) + '0');
        encodeInfo += '\n';

        dest = "1111";

        for (i = 0; i < readable.length(); i += 2) {
            dest += interlace(i, i + 1);
        }

        dest += "311";

        pattern = new String[] { dest };
        row_count = 1;
        row_height = new int[] { -1 };
        return true;
    }

    private boolean deutschePostIdentcode() {
        int i, count = 0;
        int input_length = content.length();
        String dest;

        if (!(content.matches("[0-9]+"))) {
            error_msg = "Invalid characters in input";
            return false;
        }

        if (input_length > 11) {
            error_msg = "Input data too long";
            return false;
        }

        readable = "";
        for (i = input_length; i < 11; i++) {
            readable += "0";
        }

        readable += content;
        for (i = 10; i >= 0; i--) {
            count += 4 * (readable.charAt(i) - '0');

            if ((i & 1) != 0) {
                count += 5 * (readable.charAt(i) - '0');
            }
        }

        readable += (char)(((10 - (count % 10)) % 10) + '0');
        encodeInfo += "Check Digit: " + (char)(((10 - (count % 10)) % 10) + '0');
        encodeInfo += '\n';

        dest = "1111";

        for (i = 0; i < readable.length(); i += 2) {
            dest += interlace(i, i + 1);
        }

        dest += "311";

        pattern = new String[] { dest };
        row_count = 1;
        row_height = new int[] { -1 };
        return true;
    }

    @Override
    protected void plotSymbol() {

        int xBlock;

        rectangles.clear();
        texts.clear();

        int baseY;
        if (humanReadableLocation == TOP) {
            baseY = getTheoreticalHumanReadableHeight();
        } else {
            baseY = 0;
        }

        double x = 0;
        int y = baseY;
        int h = 0;
        boolean black = true;

        int offset = 0;
        if (mode == ToFMode.ITF14) {
            offset = 20;
        }

        for (xBlock = 0; xBlock < pattern[0].length(); xBlock++) {
            char c = pattern[0].charAt(xBlock);
            double w = getModuleWidth(c - '0') * moduleWidth;
            if (black) {
                if (row_height[0] == -1) {
                    h = default_height;
                } else {
                    h = row_height[0];
                }
                if (w != 0 && h != 0) {
                    Rectangle2D.Double rect = new Rectangle2D.Double(x + offset, y, w, h);
                    rectangles.add(rect);
                }
                symbol_width = (int) Math.ceil(x + w + (2 * offset));
            }
            black = !black;
            x += w;
        }

        symbol_height = h;

        if (mode == ToFMode.ITF14) {
            // Add bounding box
            Rectangle2D.Double topBar = new Rectangle2D.Double(0, baseY, symbol_width, 4);
            Rectangle2D.Double bottomBar = new Rectangle2D.Double(0, baseY + symbol_height - 4, symbol_width, 4);
            Rectangle2D.Double leftBar = new Rectangle2D.Double(0, baseY, 4, symbol_height);
            Rectangle2D.Double rightBar = new Rectangle2D.Double(symbol_width - 4, baseY, 4, symbol_height);
            rectangles.add(topBar);
            rectangles.add(bottomBar);
            rectangles.add(leftBar);
            rectangles.add(rightBar);
        }

        if (humanReadableLocation != NONE && !readable.isEmpty()) {
            double baseline;
            if (humanReadableLocation == TOP) {
                baseline = fontSize;
            } else {
                baseline = getHeight() + fontSize;
            }
            double centerX = getWidth() / 2;
            texts.add(new TextBox(centerX, baseline, readable));
        }
    }

    /** {@inheritDoc} */
    @Override
    protected double getModuleWidth(int originalWidth) {
        if (originalWidth == 1) {
            return 1;
        } else {
            return moduleWidthRatio;
        }
    }
}
