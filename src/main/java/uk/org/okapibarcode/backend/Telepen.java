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
 * Telepen (also known as Telepen Alpha) can encode ASCII text input and
 * includes a modulo-127 check digit. Telepen Numeric allows compression of 
 * numeric data into a Telepen symbol. Data can consist of pairs of numbers 
 * or pairs consisting of a numerical digit followed an X character.
 * Telepen Numeric also includes a modulo-127 check digit.
 *
 * @author <a href="mailto:rstuart114@gmail.com">Robin Stuart</a>
 */
public class Telepen extends Symbol {

    public enum tp_mode {
        NORMAL, NUMERIC
    }
    public tp_mode mode;

    private String[] TeleTable = {
        "1111111111111111", "1131313111", "33313111", "1111313131",
        "3111313111", "11333131", "13133131", "111111313111", "31333111",
        "1131113131", "33113131", "1111333111", "3111113131", "1113133111",
        "1311133111", "111111113131", "3131113111", "11313331", "333331",
        "111131113111", "31113331", "1133113111", "1313113111", "1111113331",
        "31131331", "113111113111", "3311113111", "1111131331", "311111113111",
        "1113111331", "1311111331", "11111111113111", "31313311", "1131311131",
        "33311131", "1111313311", "3111311131", "11333311", "13133311",
        "111111311131", "31331131", "1131113311", "33113311", "1111331131",
        "3111113311", "1113131131", "1311131131", "111111113311", "3131111131",
        "1131131311", "33131311", "111131111131", "3111131311", "1133111131",
        "1313111131", "111111131311", "3113111311", "113111111131",
        "3311111131", "111113111311", "311111111131", "111311111311",
        "131111111311", "11111111111131", "3131311111", "11313133", "333133",
        "111131311111", "31113133", "1133311111", "1313311111", "1111113133",
        "313333", "113111311111", "3311311111", "11113333", "311111311111",
        "11131333", "13111333", "11111111311111", "31311133", "1131331111",
        "33331111", "1111311133", "3111331111", "11331133", "13131133",
        "111111331111", "3113131111", "1131111133", "33111133", "111113131111",
        "3111111133", "111311131111", "131111131111", "111111111133",
        "31311313", "113131111111", "3331111111", "1111311313", "311131111111",
        "11331313", "13131313", "11111131111111", "3133111111", "1131111313",
        "33111313", "111133111111", "3111111313", "111313111111",
        "131113111111", "111111111313", "313111111111", "1131131113",
        "33131113", "11113111111111", "3111131113", "113311111111",
        "131311111111", "111111131113", "3113111113", "11311111111111",
        "331111111111", "111113111113", "31111111111111", "111311111113",
        "131111111113"
    };

    public Telepen() {
        mode = tp_mode.NORMAL;
    }

    public void setNormalMode() {
        mode = tp_mode.NORMAL;
    }

    public void setNumericMode() {
        mode = tp_mode.NUMERIC;
    }

    @Override
    public boolean encode() {
        if (mode == tp_mode.NORMAL) {
            return normal_mode();
        } else {
            return numeric_mode();
        }
    }

    private boolean normal_mode() {
        int count = 0, asciicode, check_digit;
        String p = "";
        String dest;

        int l = content.length();

        //FIXME: Ensure no extended ASCII or Unicode charcters are entered
        dest = TeleTable['_']; // Start
        for (int i = 0; i < l; i++) {
            asciicode = content.charAt(i);
            p += TeleTable[asciicode];
            count += asciicode;
        }

        check_digit = 127 - (count % 127);
        if (check_digit == 127) {
            check_digit = 0;
        }

        p += TeleTable[check_digit];

        encodeInfo += "Check Digit: " + check_digit + "\n";

        dest += p;
        dest += TeleTable['z']; // Stop

        readable = content;
        pattern = new String[1];
        pattern[0] = dest;
        row_count = 1;
        row_height = new int[1];
        row_height[0] = -1;
        plotSymbol();
        return true;
    }

    public boolean numeric_mode() {
        int count = 0, check_digit;
        String p = "";
        String t;
        String dest;
        int l = content.length();
        int tl, glyph;
        char c1, c2;

        //FIXME: Ensure no extended ASCII or Unicode charcters are entered
        if (!(content.matches("[0-9X]+?"))) {
            error_msg = "Invalid characters in input";
            return false;
        }

        /* If input is an odd length, add a leading zero */
        if ((l & 1) == 1) {
            t = "0" + content;
            tl = l + 1;
        } else {
            t = content;
            tl = l;
        }

        dest = TeleTable['_']; // Start
        for (int i = 0; i < tl; i += 2) {

            c1 = t.charAt(i);
            c2 = t.charAt(i + 1);

            /* Input nX is allowed, but Xn is not */
            if (c1 == 'X') {
                error_msg = "Invalid position of X in data";
                return false;
            }

            if (c2 == 'X') {
                glyph = (c1 - '0') + 17;
                count += glyph;
            } else {
                glyph = ((10 * (c1 - '0')) + (c2 - '0')) + 27;
                count += glyph;
            }

            p += TeleTable[glyph];
        }

        check_digit = 127 - (count % 127);
        if (check_digit == 127) {
            check_digit = 0;
        }

        p += TeleTable[check_digit];

        encodeInfo += "Check Digit: " + check_digit + "\n";

        dest += p;
        dest += TeleTable['z']; // Stop
        readable = content;
        pattern = new String[1];
        pattern[0] = dest;
        row_count = 1;
        row_height = new int[1];
        row_height[0] = -1;
        plotSymbol();
        return true;
    }
}
