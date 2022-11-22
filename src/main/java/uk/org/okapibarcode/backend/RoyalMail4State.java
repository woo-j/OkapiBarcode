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
 * <p>Encodes data according to the Royal Mail 4-State Country Code.
 *
 * <p>Data input can consist of numbers 0-9 and letters A-Z and usually includes
 * delivery postal code followed by house number. A check digit is calculated
 * and added.
 *
 * @author <a href="mailto:rstuart114@gmail.com">Robin Stuart</a>
 */
public class RoyalMail4State extends Symbol {

    private static final String[] ROYAL_TABLE = {
        "TTFF", "TDAF", "TDFA", "DTAF", "DTFA", "DDAA", "TADF", "TFTF", "TFDA", "DATF",
        "DADA", "DFTA", "TAFD", "TFAD", "TFFT", "DAAD", "DAFT", "DFAT", "ATDF", "ADTF",
        "ADDA", "FTTF", "FTDA", "FDTA", "ATFD", "ADAD", "ADFT", "FTAD", "FTFT", "FDAT",
        "AADD", "AFTD", "AFDT", "FATD", "FADT", "FFTT"
    };

    private static final int[][] ROYAL_VALUES = {
        { 1, 1 }, { 1, 2 }, { 1, 3 }, { 1, 4 }, { 1, 5 }, { 1, 0 }, { 2, 1 }, { 2, 2 }, { 2, 3 }, { 2, 4 },
        { 2, 5 }, { 2, 0 }, { 3, 1 }, { 3, 2 }, { 3, 3 }, { 3, 4 }, { 3, 5 }, { 3, 0 }, { 4, 1 }, { 4, 2 },
        { 4, 3 }, { 4, 4 }, { 4, 5 }, { 4, 0 }, { 5, 1 }, { 5, 2 }, { 5, 3 }, { 5, 4 }, { 5, 5 }, { 5, 0 },
        { 0, 1 }, { 0, 2 }, { 0, 3 }, { 0, 4 }, { 0, 5 }, { 0, 0 }
    };

    private static final char[] KR_SET = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
        'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
        'U', 'V', 'W', 'X', 'Y', 'Z'
    };

    @Override
    protected void encode() {

        content = content.toUpperCase(Locale.ENGLISH);
        if (!content.matches("[0-9A-Z]+")) {
            throw new OkapiException("Invalid characters in data");
        }

        int top = 0;
        int bottom = 0;
        StringBuilder dest = new StringBuilder();
        dest.append('A');
        for (int i = 0; i < content.length(); i++) {
            int index = positionOf(content.charAt(i), KR_SET);
            dest.append(ROYAL_TABLE[index]);
            top += ROYAL_VALUES[index][0];
            bottom += ROYAL_VALUES[index][1];
        }

        /* calculate check digit */
        int row = (top % 6) - 1;
        int column = (bottom % 6) - 1;
        if (row == -1) {
            row = 5;
        }
        if (column == -1) {
            column = 5;
        }
        int check = (6 * row) + column;
        dest.append(ROYAL_TABLE[check]);
        infoLine("Check Digit: " + check);

        /* stop character */
        dest.append('F');
        infoLine("Encoding: " + dest);

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
