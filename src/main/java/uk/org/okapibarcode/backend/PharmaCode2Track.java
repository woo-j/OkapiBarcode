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

import java.awt.Rectangle;

/**
 *
 * @author <a href="mailto:rstuart114@gmail.com">Robin Stuart</a>
 */
public class PharmaCode2Track extends Symbol {

    @Override
    public boolean encode() {
        int i, tester = 0;
        String inter, dest;

        if (content.length() > 8) {
            error_msg = "Input too long";
            return false;
        }

        if (!(content.matches("[0-9]+?"))) {
            error_msg = "Invalid characters in data";
            return false;
        }

        for (i = 0; i < content.length(); i++) {
            tester *= 10;
            tester += Character.getNumericValue(content.charAt(i));
        }

        if ((tester < 4) || (tester > 64570080)) {
            error_msg = "Data out of range";
            return false;
        }

        inter = "";
        do {
            switch (tester % 3) {
            case 0:
                inter += "F";
                tester = (tester - 3) / 3;
                break;
            case 1:
                inter += "D";
                tester = (tester - 1) / 3;
                break;
            case 2:
                inter += "A";
                tester = (tester - 2) / 3;
                break;
            }
        }
        while (tester != 0);

        dest = "";
        for (i = (inter.length() - 1); i >= 0; i--) {
            dest += inter.charAt(i);
        }

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
                h = default_height / 2;
                break;
            case 'D':
                y = default_height / 2;
                h = default_height / 2;
                break;
            case 'F':
                y = 0;
                h = default_height;
                break;
            }

            Rectangle thisrect = new Rectangle(x, y, w, h);
            rect.add(thisrect);

            x += 2.0;
        }
        symbol_width = pattern[0].length() * 2;
        symbol_height = default_height;
    }
}
