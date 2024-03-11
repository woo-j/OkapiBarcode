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
 * <p>Implements Telepen (also known as Telepen Alpha).
 *
 * <p>Telepen can encode ASCII text input and includes a modulo-127 check digit.
 * Telepen Numeric allows compression of numeric data into a Telepen symbol. Data
 * can consist of pairs of numbers or pairs consisting of a numerical digit followed
 * by an X character. Telepen Numeric also includes a modulo-127 check digit.
 *
 * @author <a href="mailto:rstuart114@gmail.com">Robin Stuart</a>
 */
public class Telepen extends Symbol {

    public static enum Mode {
        /** A normal Telepen symbol. */
        NORMAL,
        /** A numeric Telepen symbol. */
        NUMERIC
    }

    private static final String[] TELE_TABLE = {
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

    private Mode mode;

    /**
     * Creates a new instance, using mode {@link Mode#NORMAL}.
     */
    public Telepen() {
        this(Mode.NORMAL);
    }

    /**
     * Creates a new instance, using the specified mode.
     *
     * @param mode the Telepen mode
     */
    public Telepen(Mode mode) {
        this.mode = mode;
    }

    /**
     * Sets the Telepen mode.
     *
     * @param mode the Telepen mode
     */
    public void setMode(Mode mode) {
        this.mode = mode;
    }

    /**
     * Returns the Telepen mode.
     *
     * @return the Telepen mode
     */
    public Mode getMode() {
        return mode;
    }

    @Override
    protected void encode() {
        if (mode == Mode.NORMAL) {
            normalMode();
        } else {
            numericMode();
        }
    }

    private void normalMode() {

        if (!content.matches("[\u0000-\u007F]+")) {
            throw OkapiInputException.invalidCharactersInInput();
        }

        int count = 0;
        StringBuilder dest = new StringBuilder();
        dest.append(TELE_TABLE['_']); // Start
        for (int i = 0; i < content.length(); i++) {
            int asciicode = content.charAt(i);
            dest.append(TELE_TABLE[asciicode]);
            count += asciicode;
        }

        int check_digit = 127 - (count % 127);
        if (check_digit == 127) {
            check_digit = 0;
        }
        infoLine("Check Digit: " + check_digit);

        dest.append(TELE_TABLE[check_digit]);
        dest.append(TELE_TABLE['z']); // Stop

        readable = content;
        pattern = new String[] { dest.toString() };
        row_count = 1;
        row_height = new int[] { -1 };
    }

    private void numericMode() {

        if (!content.matches("[0-9X]+")) {
            throw OkapiInputException.invalidCharactersInInput();
        }

        /* If input is an odd length, add a leading zero */
        String t;
        if ((content.length() & 1) == 1) {
            t = "0" + content;
        } else {
            t = content;
        }

        int count = 0;
        StringBuilder dest = new StringBuilder();
        dest.append(TELE_TABLE['_']); // Start
        for (int i = 0; i < t.length(); i += 2) {
            char c1 = t.charAt(i);
            char c2 = t.charAt(i + 1);
            /* Input nX is allowed, but Xn is not */
            if (c1 == 'X') {
                throw new OkapiInputException("Invalid position of X in data");
            }
            int glyph;
            if (c2 == 'X') {
                glyph = (c1 - '0') + 17;
            } else {
                glyph = ((10 * (c1 - '0')) + (c2 - '0')) + 27;
            }
            count += glyph;
            dest.append(TELE_TABLE[glyph]);
        }

        int check_digit = 127 - (count % 127);
        if (check_digit == 127) {
            check_digit = 0;
        }
        infoLine("Check Digit: " + check_digit);

        dest.append(TELE_TABLE[check_digit]);
        dest.append(TELE_TABLE['z']); // Stop

        readable = content;
        pattern = new String[] { dest.toString() };
        row_count = 1;
        row_height = new int[] { -1 };
    }
}
