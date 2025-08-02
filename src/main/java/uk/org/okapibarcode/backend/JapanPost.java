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

import java.util.Locale;

import uk.org.okapibarcode.graphics.Rectangle;

/**
 * <p>Implements the Japanese Postal Code symbology as used to encode address
 * data for mail items in Japan. Valid input characters are digits 0-9,
 * characters A-Z and the dash (-) character. A modulo-19 check digit is
 * added and should not be included in the input data.
 *
 * @author <a href="mailto:rstuart114@gmail.com">Robin Stuart</a>
 */
public class JapanPost extends Symbol {

    private static final String[] JAPAN_TABLE = {
        "FFT", "FDA", "DFA", "FAD", "FTF", "DAF", "AFD", "ADF", "TFF", "FTT",
        "TFT", "DAT", "DTA", "ADT", "TDA", "ATD", "TAD", "TTF", "FFF"
    };

    private static final char[] KASUT_SET = {
        '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '-', 'a', 'b', 'c',
        'd', 'e', 'f', 'g', 'h'
    };

    private static final char[] CH_KASUT_SET = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', 'a', 'b', 'c',
        'd', 'e', 'f', 'g', 'h'
    };

    /**
     * Creates a new instance.
     */
    public JapanPost() {
        this.humanReadableLocation = HumanReadableLocation.NONE;
    }

    @Override
    protected void encode() {

        content = content.toUpperCase(Locale.ENGLISH);
        if (!content.matches("[0-9A-Z\\-]*")) {
            throw OkapiInputException.invalidCharactersInInput();
        }
        if (content.length() > 20) {
            throw OkapiInputException.inputTooLong();
        }

        StringBuilder inter = new StringBuilder(40);
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if ((c >= '0' && c <= '9') || c == '-') {
                inter.append(c);
            } else if (c <= 'J') { // A-J
                inter.append('a');
                inter.append(CH_KASUT_SET[(c - 'A')]);
            } else if (c <= 'T') { // K-T
                inter.append('b');
                inter.append(CH_KASUT_SET[(c - 'K')]);
            } else { // U-Z
                inter.append('c');
                inter.append(CH_KASUT_SET[(c - 'U')]);
            }
        }
        for (int i = inter.length(); i < 20; i++) {
            inter.append('d');
        }
        if (inter.length() > 20) {
            throw OkapiInputException.inputTooLong();
        }

        int sum = 0;
        int destLen = 7 + (20 * 3);
        StringBuilder dest = new StringBuilder(destLen);
        dest.append("FD");
        for (int i = 0; i < 20; i++) {
            dest.append(JAPAN_TABLE[positionOf(inter.charAt(i), KASUT_SET)]);
            sum += positionOf(inter.charAt(i), CH_KASUT_SET);
        }

        /* Calculate check digit */
        int check = 19 - (sum % 19);
        if (check == 19) {
            check = 0;
        }
        dest.append(JAPAN_TABLE[positionOf(CH_KASUT_SET[check], KASUT_SET)]);
        dest.append("DF");

        assert dest.length() == destLen;

        infoLine("Encoding: ", dest);
        infoLine("Check Digit: ", check);

        readable = "";
        pattern = new String[] { dest.toString() };
        row_count = 1;
        row_height = new int[] { -1 };
    }

    @Override
    protected void plotSymbol() {

        int x = 0;
        int w = 1;
        int y = 0;
        int h = 0;

        resetPlotElements();

        for (int xBlock = 0; xBlock < pattern[0].length(); xBlock++) {
            switch (pattern[0].charAt(xBlock)) {
                case 'A':
                    y = 0;
                    h = 5;
                    break;
                case 'D':
                    y = 3;
                    h = 5;
                    break;
                case 'F':
                    y = 0;
                    h = 8;
                    break;
                case 'T':
                    y = 3;
                    h = 2;
                    break;
            }
            Rectangle rect = new Rectangle(x, y, w, h);
            rectangles.add(rect);
            x += 2;
        }

        symbol_width = pattern[0].length() * 2;
        symbol_height = 8;
    }
}
