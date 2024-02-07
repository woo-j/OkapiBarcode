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

import static uk.org.okapibarcode.util.Arrays.positionOf;

/**
 * Implements the LOGMARS (Logistics Applications of Automated Marking
 * and Reading Symbols) standard used by the US Department of Defense.
 * Input data can be of any length and supports the characters 0-9, A-Z, dash
 * (-), full stop (.), space, dollar ($), slash (/), plus (+) and percent (%).
 * A Modulo-43 check digit is calculated and added, and should not form part
 * of the input data.
 *
 * @author <a href="mailto:rstuart114@gmail.com">Robin Stuart</a>
 */
public class Logmars extends Symbol {

    private static final String[] CODE39LM = {
        "1113313111", "3113111131", "1133111131", "3133111111", "1113311131",
        "3113311111", "1133311111", "1113113131", "3113113111", "1133113111",
        "3111131131", "1131131131", "3131131111", "1111331131", "3111331111",
        "1131331111", "1111133131", "3111133111", "1131133111", "1111333111",
        "3111111331", "1131111331", "3131111311", "1111311331", "3111311311",
        "1131311311", "1111113331", "3111113311", "1131113311", "1111313311",
        "3311111131", "1331111131", "3331111111", "1311311131", "3311311111",
        "1331311111", "1311113131", "3311113111", "1331113111", "1313131111",
        "1313111311", "1311131311", "1113131311"
    };

    private static final char[] LOOKUP = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D',
        'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R',
        'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '-', '.', ' ', '$', '/', '+',
        '%'
    };

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
    protected void encode() {

        if (!content.matches("[0-9A-Z\\. \\-$/+%]*")) {
            throw OkapiInputException.invalidCharactersInInput();
        }

        int counter = 0;
        int len = (content.length() * 10) + 29;
        StringBuilder p = new StringBuilder(len);
        p.append("1311313111");
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            int val = positionOf(c, LOOKUP);
            counter += val;
            p.append(CODE39LM[val]);
        }

        counter = counter % 43;
        char checkDigit = LOOKUP[counter];
        infoLine("Check Digit: " + checkDigit);
        p.append(CODE39LM[counter]);
        p.append("131131311");

        assert len == p.length();

        readable = content + checkDigit;
        pattern = new String[] { p.toString() };
        row_count = 1;
        row_height = new int[] { -1 };
    }
}
