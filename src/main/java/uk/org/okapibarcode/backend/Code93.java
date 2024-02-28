/*
 * Copyright 2014-2015 Robin Stuart, Daniel Gredler
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
 * <p>Implements <a href="http://en.wikipedia.org/wiki/Code_93">Code 93</a>.
 *
 * <p>Supports encoding of 7-bit ASCII text. Two check digits are added.
 *
 * @author <a href="mailto:rstuart114@gmail.com">Robin Stuart</a>
 * @author Daniel Gredler
 */
public class Code93 extends Symbol {

    /**
     * Code 93 control characters, indexed by ASCII codes (NOTE: a = Ctrl $,
     * b = Ctrl %, c = Ctrl /, d = Ctrl + for sequences of two characters).
     */
    private static final String[] CODE_93_CTRL = {
        "bU", "aA", "aB", "aC", "aD", "aE", "aF", "aG", "aH", "aI",
        "aJ", "aK", "aL", "aM", "aN", "aO", "aP", "aQ", "aR", "aS",
        "aT", "aU", "aV", "aW", "aX", "aY", "aZ", "bA", "bB", "bC",
        "bD", "bE", " ",  "cA", "cB", "cC", "$",  "%",  "cF", "cG",
        "cH", "cI", "cJ", "+",  "cL", "-",  ".",  "/",  "0",  "1",
        "2",  "3",  "4",  "5",  "6",  "7",  "8",  "9",  "cZ", "bF",
        "bG", "bH", "bI", "bJ", "bV", "A",  "B",  "C",  "D",  "E",
        "F",  "G",  "H",  "I",  "J",  "K",  "L",  "M",  "N",  "O",
        "P",  "Q",  "R",  "S",  "T",  "U",  "V",  "W",  "X",  "Y",
        "Z",  "bK", "bL", "bM", "bN", "bO", "bW", "dA", "dB", "dC",
        "dD", "dE", "dF", "dG", "dH", "dI", "dJ", "dK", "dL", "dM",
        "dN", "dO", "dP", "dQ", "dR", "dS", "dT", "dU", "dV", "dW",
        "dX", "dY", "dZ", "bP", "bQ", "bR", "bS", "bT" };

    /**
     * Mapping of control characters to pattern table index (NOTE: a = Ctrl $,
     * b = Ctrl %, c = Ctrl /, d = Ctrl + for sequences of two characters).
     */
    private static final char[] CODE_93_LOOKUP = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
        'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
        'U', 'V', 'W', 'X', 'Y', 'Z', '-', '.', ' ', '$',
        '/', '+', '%', 'a', 'b', 'c', 'd' };

    /** Code 93 pattern table. */
    private static final String[] CODE_93_TABLE = {
        "131112", "111213", "111312", "111411", "121113",
        "121212", "121311", "111114", "131211", "141111",
        "211113", "211212", "211311", "221112", "221211",
        "231111", "112113", "112212", "112311", "122112",
        "132111", "111123", "111222", "111321", "121122",
        "131121", "212112", "212211", "211122", "211221",
        "221121", "222111", "112122", "112221", "122121",
        "123111", "121131", "311112", "311211", "321111",
        "112131", "113121", "211131", "121221", "312111",
        "311121", "122211" };

    /** Whether or not to show check digits in the human-readable text. */
    private boolean showCheckDigits = true;

    /** Optional start/stop delimiter to be shown in the human-readable text. */
    private Character startStopDelimiter;

    /**
     * Sets whether or not to show check digits in the human-readable text (defaults to <code>true</code>).
     *
     * @param showCheckDigits whether or not to show check digits in the human-readable text
     */
    public void setShowCheckDigits(boolean showCheckDigits) {
        this.showCheckDigits = showCheckDigits;
    }

    /**
     * Returns whether or not this symbol shows check digits in the human-readable text.
     *
     * @return whether or not this symbol shows check digits in the human-readable text
     */
    public boolean getShowCheckDigits() {
        return showCheckDigits;
    }

    /**
     * Sets an optional start/stop delimiter to be shown in the human-readable text (defaults to <code>null</code>).
     *
     * @param startStopDelimiter an optional start/stop delimiter to be shown in the human-readable text
     */
    public void setStartStopDelimiter(Character startStopDelimiter) {
        this.startStopDelimiter = startStopDelimiter;
    }

    /**
     * Returns the optional start/stop delimiter to be shown in the human-readable text.
     *
     * @return the optional start/stop delimiter to be shown in the human-readable text
     */
    public Character getStartStopDelimiter() {
        return startStopDelimiter;
    }

    /** {@inheritDoc} */
    @Override
    protected void encode() {

        if (!content.matches("[\u0000-\u007F]*")) {
            throw OkapiInputException.invalidCharactersInInput();
        }

        char[] controlChars = toControlChars(content);
        int l = controlChars.length;

        int[] values = new int[controlChars.length + 2];
        for (int i = 0; i < l; i++) {
            values[i] = positionOf(controlChars[i], CODE_93_LOOKUP);
        }

        int c = calculateCheckDigitC(values, l);
        values[l] = c;
        l++;

        int k = calculateCheckDigitK(values, l);
        values[l] = k;
        l++;

        readable = content;
        if (showCheckDigits) {
            readable = readable + CODE_93_LOOKUP[c] + CODE_93_LOOKUP[k];
        }
        if (startStopDelimiter != null) {
            readable = startStopDelimiter + readable + startStopDelimiter;
        }

        infoLine("Check Digit C: " + c);
        infoLine("Check Digit K: " + k);
        pattern = new String[] { toPattern(values) };
        row_count = 1;
        row_height = new int[] { -1 };
    }

    private static char[] toControlChars(String s) {
        StringBuilder buffer = new StringBuilder();
        char[] chars = s.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            int asciiCode = chars[i];
            buffer.append(CODE_93_CTRL[asciiCode]);
        }
        return buffer.toString().toCharArray();
    }

    private static int calculateCheckDigitC(int[] values, int length) {
        int c = 0;
        int weight = 1;
        for (int i = length - 1; i >= 0; i--) {
            c += values[i] * weight;
            weight++;
            if (weight == 21) {
                weight = 1;
            }
        }
        c = c % 47;
        return c;
    }

    private static int calculateCheckDigitK(int[] values, int length) {
        int k = 0;
        int weight = 1;
        for (int i = length - 1; i >= 0; i--) {
            k += values[i] * weight;
            weight++;
            if (weight == 16) {
                weight = 1;
            }
        }
        k = k % 47;
        return k;
    }

    private static String toPattern(int[] values) {
        StringBuilder buffer = new StringBuilder("111141");
        for (int i = 0; i < values.length; i++) {
            buffer.append(CODE_93_TABLE[values[i]]);
        }
        buffer.append("1111411");
        return buffer.toString();
    }

    /** {@inheritDoc} */
    @Override
    protected int[] getCodewords() {
        return getPatternAsCodewords(6);
    }
}
