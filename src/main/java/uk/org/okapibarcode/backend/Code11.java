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

/**
 * Implements Code 11 bar code symbology.
 * <p>
 * Code 11 can encode any length string consisting of the digits 0-9 and the
 * dash character (-). One modulo-11 check digit is calculated.
 *
 * @author <a href="mailto:rstuart114@gmail.com">Robin Stuart</a>
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

    /** {@inheritDoc} */
    @Override
    public boolean encode() {
        if (!(content.matches("[0-9-]+"))) {
            error_msg = "Invalid characters in input";
            return false;
        }

        String horizontalSpacing;
        int i;
        int length = content.length();
        int checkDigitC, weightC = 1, countC = 0, checkDigitK, weightK = 1, countK = 0;
        int[] weight = new int[128];
        char thisCharacter;

        horizontalSpacing = "112211";
        for (i = 0; i < length; i++) {
            thisCharacter = content.charAt(i);
            weight[i] = positionOf(thisCharacter, CHARACTER_SET);
            horizontalSpacing += CODE_11_TABLE[weight[i]];
        }

        /* Calculate C checksum */
        for (i = length - 1; i >= 0; i--) {
            countC += (weightC * weight[i]);
            weightC++;

            if (weightC > 10) {
                weightC = 1;
            }
        }
        checkDigitC = countC % 11;

        encodeInfo += "Check Digit C: " + checkDigitC + "\n";

        weight[length] = checkDigitC;

        /* Calculate K checksum */
        for (i = length; i >= 0; i--) {
            countK += (weightK * weight[i]);
            weightK++;

            if (weightK > 9) {
                weightK = 1;
            }
        }
        checkDigitK = countK % 11;

        encodeInfo += "Check Digit K: " + checkDigitK + "\n";

        horizontalSpacing += CODE_11_TABLE[checkDigitC];
        horizontalSpacing += CODE_11_TABLE[checkDigitK];
        horizontalSpacing += "112211";

        readable = content + CHARACTER_SET[checkDigitC] + CHARACTER_SET[checkDigitK];
        pattern = new String[1];
        pattern[0] = horizontalSpacing;
        row_count = 1;
        row_height = new int[1];
        row_height[0] = -1;
        plotSymbol();
        return true;
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
