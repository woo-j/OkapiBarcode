/*
 * Copyright 2014 Robin Stuart, Daniel Gredler
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

/**
 * Implements Code 11 bar code symbology.
 * <p>
 * Code 11 can encode any length string consisting of the digits 0-9 and the
 * dash character (-). One or two modulo-11 check digits are calculated.
 *
 * @author <a href="mailto:rstuart114@gmail.com">Robin Stuart</a>
 * @author Daniel Gredler
 */
public class Code11 extends Symbol {

    private static final String[] CODE_11_TABLE = {
        "111121", "211121", "121121", "221111", "112121", "212111",
        "122111", "111221", "211211", "211111", "112111"
    };

    private static final char[] CHARACTER_SET = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-'
    };

    /** Ratio of wide bar width to narrow bar width. */
    private double moduleWidthRatio = 2;

    /** The number of check digits to calculate ({@code 1} or {@code 2}). */
    private int checkDigitCount = 2;

    /** Optional start delimiter to be shown in the human-readable text. */
    private Character startDelimiter;

    /** Optional stop delimiter to be shown in the human-readable text. */
    private Character stopDelimiter;

    /**
     * Sets the ratio of wide bar width to narrow bar width. Valid values are usually
     * between {@code 2} and {@code 3}. The default value is {@code 2}.
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
     * Sets the number of check digits to calculate ({@code 1} or {@code 2}). The default value is {@code 2}.
     *
     * @param checkDigitCount the number of check digits to calculate
     */
    public void setCheckDigitCount(int checkDigitCount) {
        if (checkDigitCount < 1 || checkDigitCount > 2) {
            throw new IllegalArgumentException("Check digit count must be 1 or 2.");
        }
        this.checkDigitCount = checkDigitCount;
    }

    /**
     * Returns the number of check digits to calculate (1 or 2).
     *
     * @return the number of check digits to calculate
     */
    public int getCheckDigitCount() {
        return checkDigitCount;
    }

    /**
     * Sets an optional start delimiter to be shown in the human-readable text (defaults to <code>null</code>).
     *
     * @param startDelimiter an optional start delimiter to be shown in the human-readable text
     */
    public void setStartDelimiter(Character startDelimiter) {
        this.startDelimiter = startDelimiter;
    }

    /**
     * Returns the optional start delimiter to be shown in the human-readable text.
     *
     * @return the optional start delimiter to be shown in the human-readable text
     */
    public Character getStartDelimiter() {
        return startDelimiter;
    }

    /**
     * Sets an optional stop delimiter to be shown in the human-readable text (defaults to <code>null</code>).
     *
     * @param stopDelimiter an optional stop delimiter to be shown in the human-readable text
     */
    public void setStopDelimiter(Character stopDelimiter) {
        this.stopDelimiter = stopDelimiter;
    }

    /**
     * Returns the optional stop delimiter to be shown in the human-readable text.
     *
     * @return the optional stop delimiter to be shown in the human-readable text
     */
    public Character getStopDelimiter() {
        return stopDelimiter;
    }

    /** {@inheritDoc} */
    @Override
    public boolean encode() {

        if (!(content.matches("[0-9-]+"))) {
            error_msg = "Invalid characters in input";
            return false;
        }

        String horizontalSpacing = "112211";
        String humanReadable = content;
        int length = content.length();
        int[] weight = new int[length + 1];

        for (int i = 0; i < length; i++) {
            char c = content.charAt(i);
            weight[i] = positionOf(c, CHARACTER_SET);
            horizontalSpacing += CODE_11_TABLE[weight[i]];
        }

        int checkDigitC = getCheckDigitC(weight, length);
        horizontalSpacing += CODE_11_TABLE[checkDigitC];
        humanReadable += CHARACTER_SET[checkDigitC];
        encodeInfo += "Check Digit C: " + checkDigitC + "\n";

        if (checkDigitCount == 2) {
            weight[length] = checkDigitC;
            int checkDigitK = getCheckDigitK(weight, length + 1);
            horizontalSpacing += CODE_11_TABLE[checkDigitK];
            humanReadable += CHARACTER_SET[checkDigitK];
            encodeInfo += "Check Digit K: " + checkDigitK + "\n";
        }

        horizontalSpacing += "112211";

        readable = humanReadable;
        if (startDelimiter != null) {
            readable = startDelimiter + readable;
        }
        if (stopDelimiter != null) {
            readable = readable + stopDelimiter;
        }

        pattern = new String[] { horizontalSpacing };
        row_count = 1;
        row_height = new int[] { -1 };

        return true;
    }

    private static int getCheckDigitC(int[] weight, int length) {
        int countC = 0;
        int weightC = 1;
        for (int i = length - 1; i >= 0; i--) {
            countC += (weightC * weight[i]);
            weightC++;
            if (weightC > 10) {
                weightC = 1;
            }
        }
        return countC % 11;
    }

    private static int getCheckDigitK(int[] weight, int length) {
        int countK = 0;
        int weightK = 1;
        for (int i = length - 1; i >= 0; i--) {
            countK += (weightK * weight[i]);
            weightK++;
            if (weightK > 9) {
                weightK = 1;
            }
        }
        return countK % 11;
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

    /** {@inheritDoc} */
    @Override
    protected int[] getCodewords() {
        return getPatternAsCodewords(6);
    }
}
