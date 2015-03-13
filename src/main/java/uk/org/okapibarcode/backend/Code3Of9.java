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
 * Implements Code 39 bar code symbology
 * According to ISO/IEC 16388:2007
 *
 * @author Robin Stuart <rstuart114@gmail.com>
 */
public class Code3Of9 extends Symbol {

    private final String Code39[] = {
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

    private final char LookUp[] = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D',
        'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R',
        'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '-', '.', ' ', '$', '/', '+',
        '%'
    };

    @Override
    public boolean encode() {

        if (!(content.matches("[0-9A-Z\\. \\-$/+%]+?"))) {
            error_msg = "Invalid characters in input";
            return false;
        }

        String p = "";
        String dest;
        int l = content.length();
        int charval;
        char thischar;
        int counter = 0;
        char check_digit = ' ';

        dest = "1211212111"; // Start
        for (int i = 0; i < l; i++) {
            thischar = content.charAt(i);
            charval = positionOf(thischar, LookUp);
            counter += charval;
            p += Code39[charval];
        }
        dest += p;

        if (option2 == 1) {
            //User has requested Mod-43 check digit

            counter = counter % 43;
            if(counter < 10) {
                check_digit = (char)(counter + '0');
            } else {
                if(counter < 36) {
                    check_digit = (char)((counter - 10) + 'A');
                } else {
                    switch(counter) {
                        case 36: check_digit = '-'; break;
                        case 37: check_digit = '.'; break;
                        case 38: check_digit = ' '; break;
                        case 39: check_digit = '$'; break;
                        case 40: check_digit = '/'; break;
                        case 41: check_digit = '+'; break;
                        default: check_digit = 37; break;
                    }
                }
            }

            charval = positionOf(check_digit, LookUp);
            p += Code39[charval];

            /* Display a space check digit as _, otherwise it looks like an error */
            if(check_digit == ' ') {
                check_digit = '_';
            }
        }

        dest += "121121211"; // Stop

        if (option2 == 1) {
            readable = "*" + content + check_digit + "*";
        } else {
            readable = "*" + content + "*";
        }
        pattern = new String[1];
        pattern[0] = dest;
        row_count = 1;
        row_height = new int[1];
        row_height[0] = -1;
        plotSymbol();
        return true;
    }
}
