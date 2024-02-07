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
 * <p>Implements Code 39 bar code symbology according to ISO/IEC 16388:2007.
 *
 * <p>Input data can be of any length and supports the characters 0-9, A-Z, dash
 * (-), full stop (.), space, asterisk (*), dollar ($), slash (/), plus (+)
 * and percent (%). The standard does not require a check digit but a
 * modulo-43 check digit can be added if required.
 *
 * @author <a href="mailto:rstuart114@gmail.com">Robin Stuart</a>
 */
public class Code3Of9 extends Symbol {

    public enum CheckDigit {
        NONE, MOD43
    }

    private static final String[] CODE_39 = {
        "1112212111", "2112111121", "1122111121", "2122111111", "1112211121",
        "2112211111", "1122211111", "1112112121", "2112112111", "1122112111",
        "2111121121", "1121121121", "2121121111", "1111221121", "2111221111",
        "1121221111", "1111122121", "2111122111", "1121122111", "1111222111",
        "2111111221", "1121111221", "2121111211", "1111211221", "2111211211",
        "1121211211", "1111112221", "2111112211", "1121112211", "1111212211",
        "2211111121", "1221111121", "2221111111", "1211211121", "2211211111",
        "1221211111", "1211112121", "2211112111", "1221112111", "1212121111",
        "1212111211", "1211121211", "1112121211"
    };

    private static final char[] LOOKUP = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
        'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
        'U', 'V', 'W', 'X', 'Y', 'Z', '-', '.', ' ', '$',
        '/', '+', '%'
    };

    private CheckDigit checkOption = CheckDigit.NONE;
    private double moduleWidthRatio = 2;

    /**
     * Sets the ratio of wide bar width to narrow bar width. Valid values are usually between
     * {@code 2} and {@code 3}. The default value is {@code 2}.
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
     * Select addition of optional Modulo-43 check digit or encoding without check digit. By
     * default, no check digit is added.
     *
     * @param checkMode Check digit option.
     */
    public void setCheckDigit(CheckDigit checkMode) {
        checkOption = checkMode;
    }

    /**
     * Returns the check digit mode.
     *
     * @return the check digit mode
     */
    public CheckDigit getCheckDigit() {
        return checkOption;
    }

    @Override
    protected void encode() {

        if (!content.matches("[0-9A-Z\\. \\-$/+%]*")) {
            throw OkapiInputException.invalidCharactersInInput();
        }

        String start = "1211212111";
        String stop = "121121211";

        int patternLength = start.length() +
                            stop.length() +
                            (10 * content.length()) +
                            (checkOption == CheckDigit.MOD43 ? 10 : 0);

        StringBuilder dest = new StringBuilder(patternLength);
        dest.append(start);

        int counter = 0;
        char checkDigit = ' ';

        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            int index = positionOf(c, LOOKUP);
            dest.append(CODE_39[index]);
            counter += index;
        }

        if (checkOption == CheckDigit.MOD43) {
            counter = counter % 43;
            checkDigit = LOOKUP[counter];
            int index = positionOf(checkDigit, LOOKUP);
            dest.append(CODE_39[index]);
            if (checkDigit == ' ') {
                // display a space check digit as _, otherwise it looks like an error
                checkDigit = '_';
            }
            infoLine("Check Digit: " + checkDigit);
        }

        dest.append(stop);

        if (checkOption == CheckDigit.MOD43) {
            readable = "*" + content + checkDigit + "*";
        } else {
            readable = "*" + content + "*";
        }

        pattern = new String[] { dest.toString() };
        row_count = 1;
        row_height = new int[] { -1 };
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
