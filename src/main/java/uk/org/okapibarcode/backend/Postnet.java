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
 * Implements <a href="http://en.wikipedia.org/wiki/POSTNET">POSTNET</a> and
 * <a href="http://en.wikipedia.org/wiki/Postal_Alpha_Numeric_Encoding_Technique">PLANET</a>
 * bar code symbologies.
 * <br>
 * PostNet and PLANET both use numerical input data and include a modulo-10 
 * check digit. 
 *
 * @author <a href="mailto:rstuart114@gmail.com">Robin Stuart</a>
 */
public class Postnet extends Symbol {

    private static final String[] PN_TABLE = {
        "LLSSS", "SSSLL", "SSLSL", "SSLLS", "SLSSL", "SLSLS", "SLLSS", "LSSSL", "LSSLS", "LSLSS"
    };

    private static final String[] PL_TABLE = {
        "SSLLL", "LLLSS", "LLSLS", "LLSSL", "LSLLS", "LSLSL", "LSSLL", "SLLLS", "SLLSL", "SLSLL"
    };

    private enum Mode {
        PLANET, POSTNET
    };

    private Mode mode;

    public Postnet() {
        mode = Mode.POSTNET;
    }

    public void setPlanet() {
        mode = Mode.PLANET;
    }

    public void setPostnet() {
        mode = Mode.POSTNET;
    }

    @Override
    public boolean encode() {
        boolean retval;

        if (mode == Mode.POSTNET) {
            retval = makePostnet();
        } else {
            retval = makePlanet();
        }

        if (retval == true) {
            plotSymbol();
        }

        return retval;
    }

    private boolean makePostnet() {
        int i, sum, check_digit;
        String dest;

        if (content.length() > 38) {
            error_msg = "Input too long";
            return false;
        }

        if (!(content.matches("[0-9]+?"))) {
            error_msg = "Invalid characters in data";
            return false;
        }

        sum = 0;
        dest = "L";

        for (i = 0; i < content.length(); i++) {
            dest += PN_TABLE[content.charAt(i) - '0'];
            sum += content.charAt(i) - '0';
        }

        check_digit = (10 - (sum % 10)) % 10;
        encodeInfo += "Check Digit: " + check_digit + "\n";

        dest += PN_TABLE[check_digit];

        dest += "L";
        readable = "";
        pattern = new String[1];
        pattern[0] = dest;
        row_count = 1;
        row_height = new int[1];
        row_height[0] = -1;
        return true;
    }

    private boolean makePlanet() {
        int i, sum, check_digit;
        String dest;

        if (content.length() > 38) {
            error_msg = "Input too long";
            return false;
        }

        if (!(content.matches("[0-9]+?"))) {
            error_msg = "Invalid characters in data";
            return false;
        }

        sum = 0;
        dest = "L";

        for (i = 0; i < content.length(); i++) {
            dest += PL_TABLE[content.charAt(i) - '0'];
            sum += content.charAt(i) - '0';
        }

        check_digit = (10 - (sum % 10)) % 10;
        encodeInfo += "Check Digit: " + check_digit + "\n";

        dest += PL_TABLE[check_digit];

        dest += "L";
        readable = "";
        pattern = new String[1];
        pattern[0] = dest;
        row_count = 1;
        row_height = new int[1];
        row_height[0] = -1;
        return true;
    }

    @Override
    public void plotSymbol() {
        int xBlock;
        int x, y, w, h;

        rect.clear();
        x = 0;
        w = 1;
        for (xBlock = 0; xBlock < pattern[0].length(); xBlock++) {
            if (pattern[0].charAt(xBlock) == 'L') {
                y = 0;
                h = 12;
            } else {
                y = 6;
                h = 6;
            }

            Rectangle thisrect = new Rectangle(x, y, w, h);
            rect.add(thisrect);

            x += 3.0;
        }
        symbol_width = pattern[0].length() * 4;
        symbol_height = 12;
    }
}