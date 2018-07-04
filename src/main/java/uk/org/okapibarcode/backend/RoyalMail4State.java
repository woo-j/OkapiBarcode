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

import java.awt.geom.Rectangle2D;
import java.util.Locale;

/**
 * <p>Encodes data according to the Royal Mail 4-State Country Code.
 *
 * <p>Data input can consist of numbers 0-9 and letters A-Z and usually includes
 * delivery postcode followed by house number. A check digit is calculated
 * and added.
 *
 * @author <a href="mailto:rstuart114@gmail.com">Robin Stuart</a>
 */
public class RoyalMail4State extends Symbol {

    private static final String[] ROYAL_TABLE = {
        "TTFF", "TDAF", "TDFA", "DTAF", "DTFA", "DDAA", "TADF", "TFTF", "TFDA",
        "DATF", "DADA", "DFTA", "TAFD", "TFAD", "TFFT", "DAAD", "DAFT", "DFAT",
        "ATDF", "ADTF", "ADDA", "FTTF", "FTDA", "FDTA", "ATFD", "ADAD", "ADFT",
        "FTAD", "FTFT", "FDAT", "AADD", "AFTD", "AFDT", "FATD", "FADT", "FFTT"
    };

    private static final char[] KR_SET = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D',
        'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R',
        'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
    };

    @Override
    protected void encode() {
        String dest;
        int i, top = 0, bottom = 0;
        int row, column;
        int index;

        content = content.toUpperCase(Locale.ENGLISH);
        if(!content.matches("[0-9A-Z]+")) {
            throw new OkapiException("Invalid characters in data");
        }
        dest = "A";

        for (i = 0; i < content.length(); i++) {
            index = positionOf(content.charAt(i), KR_SET);
            dest += ROYAL_TABLE[index];
            top += (index + 1) % 6;
            bottom += ((index / 6) + 1) % 6;
        }

        /* calculate check digit */
        row = (top % 6) - 1;
        column = (bottom % 6) - 1;
        if (row == -1) {
            row = 5;
        }
        if (column == -1) {
            column = 5;
        }

        dest += ROYAL_TABLE[(6 * row) + column];

        encodeInfo += "Check Digit: " + ((6 * row) + column) + "\n";

        /* Stop character */
        dest += "F";

        encodeInfo += "Encoding: " + dest + "\n";
        readable = "";
        pattern = new String[1];
        pattern[0] = dest;
        row_count = 1;
        row_height = new int[1];
        row_height[0] = -1;
    }

    @Override
    protected void plotSymbol() {
        int xBlock;
        int x, y, w, h;

        rectangles.clear();
        x = 0;
        w = 1;
        y = 0;
        h = 0;
        for (xBlock = 0; xBlock < pattern[0].length(); xBlock++) {
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

            Rectangle2D.Double rect = new Rectangle2D.Double(x, y, w, h);
            rectangles.add(rect);

            x += 2;
        }
        symbol_width = pattern[0].length() * 3;
        symbol_height = 8;
    }
}
