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

import java.util.Locale;
import java.awt.Rectangle;
/**
 *
 * @author Robin Stuart <rstuart114@gmail.com>
 */
public class JapanPost extends Symbol {
    private String[] JapanTable = {
        "FFT", "FDA", "DFA", "FAD", "FTF", "DAF", "AFD", "ADF", "TFF", "FTT", 
        "TFT", "DAT", "DTA", "ADT", "TDA", "ATD", "TAD", "TTF", "FFF"
    };

    private char[] kasutSet = {
        '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '-', 'a', 'b', 'c', 
        'd', 'e', 'f', 'g', 'h'
    };

    private char[] chKasutSet = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', 'a', 'b', 'c', 
        'd', 'e', 'f', 'g', 'h'
    };

    @Override
    public boolean encode() {
        String dest;
        String inter;
        int i, sum, check;
        char c;

        content = content.toUpperCase(Locale.ENGLISH);
        //if(!(content.matches("[0-9][A-Z]+?"))) {
        //    error_msg = "Invalid characters in data";
        //    return false;
        //}
        inter = "";

        for (i = 0;
        (i < content.length()) && (inter.length() < 20); i++) {
            c = content.charAt(i);

            if ((c >= '0') && (c <= '9')) {
                inter += c;
            }
            if (c == '-') {
                inter += c;
            }
            if ((c >= 'A') && (c <= 'J')) {
                inter += 'a';
                inter += chKasutSet[(c - 'A')];
            }

            if ((c >= 'K') && (c <= 'O')) {
                inter += 'b';
                inter += chKasutSet[(c - 'K')];
            }

            if ((c >= 'U') && (c <= 'Z')) {
                inter += 'c';
                inter += chKasutSet[(c - 'U')];
            }
        }

        for (i = inter.length(); i < 20; i++) {
            inter += "d";
        }

        if (debug) {
            System.out.println(inter);
        }

        dest = "FD";

        sum = 0;
        for (i = 0; i < 20; i++) {
            dest += JapanTable[positionOf(inter.charAt(i), kasutSet)];
            sum += positionOf(inter.charAt(i), chKasutSet);
        }

        /* Calculate check digit */
        check = 19 - (sum % 19);
        dest += JapanTable[positionOf(chKasutSet[check], kasutSet)];
        dest += "DF";
        
        encodeInfo += "Check Digit: " + check + "\n";

        readable = "";
        pattern = new String[1];
        pattern[0] = dest;
        row_count = 1;
        row_height = new int[1];
        row_height[0] = -1;
        plotSymbol();
        return true;
    }


    @Override
    public void plotSymbol() {
        int xBlock;
        int x, y, w, h;

        rect.clear();
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

            Rectangle thisrect = new Rectangle(x, y, w, h);
            rect.add(thisrect);

            x += 2.0;
        }
        symbol_width = pattern[0].length() * 3;
        symbol_height = 8;
    }
}
