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

import static java.lang.Character.toUpperCase;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import uk.org.okapibarcode.graphics.Rectangle;

/**
 * <p>Implements Code 16K symbology according to BS EN 12323:2005.
 *
 * <p>Encodes using a stacked symbology based on Code 128. Supports encoding
 * of any 8-bit ISO 8859-1 (Latin-1) data with a maximum data capacity of 77
 * alpha-numeric characters or 154 numerical digits.
 *
 * @author <a href="mailto:rstuart114@gmail.com">Robin Stuart</a>
 */
public class Code16k extends Symbol {

    private enum Mode {
        NULL, SHIFTA, LATCHA, SHIFTB, LATCHB, SHIFTC, LATCHC, AORB, ABORC, CANDB, CANDBB
    }

    /* EN 12323 Table 1 - "Code 16K" character encodations */
    private static final String[] C16K_TABLE = {
        "212222", "222122", "222221", "121223", "121322", "131222", "122213",
        "122312", "132212", "221213", "221312", "231212", "112232", "122132",
        "122231", "113222", "123122", "123221", "223211", "221132", "221231",
        "213212", "223112", "312131", "311222", "321122", "321221", "312212",
        "322112", "322211", "212123", "212321", "232121", "111323", "131123",
        "131321", "112313", "132113", "132311", "211313", "231113", "231311",
        "112133", "112331", "132131", "113123", "113321", "133121", "313121",
        "211331", "231131", "213113", "213311", "213131", "311123", "311321",
        "331121", "312113", "312311", "332111", "314111", "221411", "431111",
        "111224", "111422", "121124", "121421", "141122", "141221", "112214",
        "112412", "122114", "122411", "142112", "142211", "241211", "221114",
        "413111", "241112", "134111", "111242", "121142", "121241", "114212",
        "124112", "124211", "411212", "421112", "421211", "212141", "214121",
        "412121", "111143", "111341", "131141", "114113", "114311", "411113",
        "411311", "113141", "114131", "311141", "411131", "211412", "211214",
        "211232", "211133"
    };

    /* EN 12323 Table 3 and Table 4 - Start patterns and stop patterns */
    private static final String[] C16K_START_STOP = {
        "3211", "2221", "2122", "1411", "1132", "1231", "1114", "3112"
    };

    /* EN 12323 Table 5 - Start and stop values defining row numbers */
    private static final int[] C16K_START_VALUES = {
        0, 1, 2, 3, 4, 5, 6, 7, 0, 1, 2, 3, 4, 5, 6, 7
    };
    private static final int[] C16K_STOP_VALUES = {
        0, 1, 2, 3, 4, 5, 6, 7, 4, 5, 6, 7, 0, 1, 2, 3
    };

    private Mode[] block_mode = new Mode[170]; /* RENAME block_mode */
    private int[] block_length = new int[170]; /* RENAME block_length */
    private int block_count;

    /**
     * Creates a new instance.
     */
    public Code16k() {
        this.humanReadableLocation = HumanReadableLocation.NONE;
    }

    @Override
    public boolean supportsGs1() {
        return true;
    }

    @Override
    protected void encode() {

        // TODO: is it possible to share any of this code with Code128, which is more up to date?

        String width_pattern;
        int current_row, rows_needed, first_check, second_check;
        int indexchaine, pads_needed;
        char[] set, fset;
        Mode mode;
        char current_set;
        int i, j, k, read;
        int[] values;
        int bar_characters;
        int first_sum, second_sum;
        int input_length;
        int c_count;

        if (!content.matches("[\u0000-\u00FF]+")) {
            throw OkapiInputException.invalidCharactersInInput();
        }

        inputData = toBytes(content, StandardCharsets.ISO_8859_1);
        input_length = inputData.length;

        bar_characters = 0;
        set = new char[160]; // a = shift a, A = latch a, b = shift b, B = latch b, C = latch c
        fset = new char[160]; // f = shift to extended, n = shift from extended, F = latch to extended, space = latch from extended
        values = new int[160];

        if (input_length > 154) {
            throw OkapiInputException.inputTooLong();
        }

        /* Detect extended ASCII characters */
        for (i = 0; i < input_length; i++) {
            if (inputData[i] >= 128) {
                fset[i] = 'f';
            } else {
                fset[i] = ' ';
            }
        }

        /* Decide when to latch to extended mode */
        for (i = 0; i < input_length; i++) {
            j = 0;
            if (fset[i] == 'f') {
                do {
                    j++;
                } while (fset[i + j] == 'f');
                if ((j >= 5) || ((j >= 3) && ((i + j) == (input_length - 1)))) {
                    for (k = 0; k < j; k++) {
                        fset[i + k] = 'F';
                    }
                }
            }
        }

        /* Decide if it is worth reverting to 646 encodation for a few characters */
        if (input_length > 1) {
            for (i = 1; i < input_length; i++) {
                if ((fset[i - 1] == 'F') && (fset[i] == ' ')) {
                    /* Detected a change from 8859-1 to 646 - count how long for */
                    for (j = 0;
                    (fset[i + j] == ' ') && ((i + j) < input_length); j++);
                    if ((j < 5) || ((j < 3) && ((i + j) == (input_length - 1)))) {
                        /* Change to shifting back rather than latching back */
                        for (k = 0; k < j; k++) {
                            fset[i + k] = 'n';
                        }
                    }
                }
            }
        }

        /* Detect mode A, B and C characters */
        block_count = 0;
        indexchaine = 0;

        mode = findSubset(inputData[indexchaine]);
        if (inputData[indexchaine] == FNC1) {
            mode = Mode.ABORC;
        }

        for (i = 0; i < 160; i++) {
            block_length[i] = 0;
        }

        do {
            block_mode[block_count] = mode;
            while ((block_mode[block_count] == mode) && (indexchaine < input_length)) {
                block_length[block_count]++;
                indexchaine++;
                if (indexchaine < input_length) {
                    mode = findSubset(inputData[indexchaine]);
                    if (inputData[indexchaine] == FNC1) {
                        mode = Mode.ABORC;
                    } /* FNC1 */
                }
            }
            block_count++;
        } while (indexchaine < input_length);

        reduceSubsetChanges();

        /* Put set data into set[] */
        read = 0;
        for (i = 0; i < block_count; i++) {
            for (j = 0; j < block_length[i]; j++) {
                switch (block_mode[i]) {
                case SHIFTA:
                    set[read] = 'a';
                    break;
                case LATCHA:
                    set[read] = 'A';
                    break;
                case SHIFTB:
                    set[read] = 'b';
                    break;
                case LATCHB:
                    set[read] = 'B';
                    break;
                case LATCHC:
                    set[read] = 'C';
                    break;
                }
                read++;
            }
        }

        /* Adjust for strings which start with shift characters - make them latch instead */
        if (set[0] == 'a') {
            i = 0;
            do {
                set[i] = 'A';
                i++;
            } while (set[i] == 'a');
        }
        if (set[0] == 'b') {
            i = 0;
            do {
                set[i] = 'B';
                i++;
            } while (set[i] == 'b');
        }

        /* Watch out for odd-length Mode C blocks */
        c_count = 0;
        for (i = 0; i < read; i++) {
            if (set[i] == 'C') {
                if (inputData[i] == FNC1) {
                    if ((c_count & 1) != 0) {
                        if ((i - c_count) != 0) {
                            set[i - c_count] = 'B';
                        } else {
                            set[i - 1] = 'B';
                        }
                    }
                    c_count = 0;
                } else {
                    c_count++;
                }
            } else {
                if ((c_count & 1) != 0) {
                    if ((i - c_count) != 0) {
                        set[i - c_count] = 'B';
                    } else {
                        set[i - 1] = 'B';
                    }
                }
                c_count = 0;
            }
        }
        if ((c_count & 1) != 0) {
            if ((i - c_count) != 0) {
                set[i - c_count] = 'B';
            } else {
                set[i - 1] = 'B';
            }
        }
        for (i = 1; i < read - 1; i++) {
            if (set[i] == 'C' && set[i - 1] == 'B' && set[i + 1] == 'B') {
                set[i] = 'B';
            }
        }

        /* Start with the mode character - Table 2 */
        int implicitLatchCAt = -1;
        int m;
        switch (set[0]) {
            case 'A':
                m = 0;
                break;
            case 'B':
                m = 1;
                break;
            case 'C':
                m = 2;
                break;
            default:
                throw new OkapiInternalException("Unrecognized code set: " + set[0]);
        }

        if (readerInit) {
            if (m == 2) {
                m = 5; // start C, but with an initial Shift B (for the reader init FNC3)
            }
            if (set[0] == 'B' && set[1] == 'C' && fset[0] == ' ') {
                m = 6; // start C, but with an initial Double Shift B (one of them for the reader init FNC3)
                implicitLatchCAt = 1;
            }
        } else if (inputDataType == DataType.GS1) {
            if (set[0] == 'B') {
                m = 3; // implicit FNC1
            } else if (set[0] == 'C') {
                m = 4; // implicit FNC1
            }
        } else {
            if (set[0] == 'B' && set[1] == 'C') {
                if (fset[0] == ' ') {
                    m = 5; // start C, but with an initial Shift B
                    implicitLatchCAt = 1;
                } else if (fset[0] == 'f') {
                    m = 6; // start C, but with an initial Double Shift B (FNC4 + data, since data is extended ASCII)
                    implicitLatchCAt = 1;
                }
            } else if (set[0] == 'B' && set[1] == 'B' && set[2] == 'C' && fset[0] == ' ' && fset[1] == ' ') {
                m = 6; // start C, but with an initial Double Shift B
                implicitLatchCAt = 2;
            }
        }

        values[bar_characters++] = 0; // save space for mode codeword, value added later
        if (readerInit) {
            values[bar_characters++] = 96; /* FNC3 */
        }

        read = 0;
        current_set = set[read];
        boolean extended = false; // extended ASCII mode or ISO/IEC 646 mode
        if (fset[0] == 'F') {
            values[bar_characters++] = fnc4(current_set); /* FNC4 */
            values[bar_characters++] = fnc4(current_set); /* FNC4 */
            extended = true;
        }

        /* Encode the data */
        do {

            if (set[read] != current_set) { /* Latch different code set */
                switch (set[read]) {
                case 'A':
                    values[bar_characters++] = 101; /* LATCH A */
                    current_set = 'A';
                    break;
                case 'B':
                    values[bar_characters++] = 100; /* LATCH B */
                    current_set = 'B';
                    break;
                case 'C':
                    if (extended) {
                        /* Latch end of extended mode */
                        /* This means slightly larger symbols, but is safest for decoder compatibility */
                        values[bar_characters++] = fnc4(current_set); /* FNC4 */
                        values[bar_characters++] = fnc4(current_set); /* FNC4 */
                        extended = false;
                    }
                    if (read != implicitLatchCAt) {
                        values[bar_characters++] = 99; /* LATCH C */
                    }
                    current_set = 'C';
                    break;
                }
            }

            if (read != 0) {
                if (fset[read] == 'F' && !extended) {
                    /* Latch beginning of extended mode */
                    values[bar_characters++] = fnc4(current_set); /* FNC4 */
                    values[bar_characters++] = fnc4(current_set); /* FNC4 */
                    extended = true;
                }
                if (fset[read] == ' ' && extended) {
                    /* Latch end of extended mode */
                    values[bar_characters++] = fnc4(current_set); /* FNC4 */
                    values[bar_characters++] = fnc4(current_set); /* FNC4 */
                    extended = false;
                }
            }

            if (set[read] != 'C' && inputData[read] != FNC1 && ((inputData[read] >= 128) != extended)) {
                /* Shift to / from extended mode */
                values[bar_characters++] = fnc4(current_set); /* FNC4 */
            }

            if ((set[read] == 'a' || set[read] == 'b') && toUpperCase(set[read]) != current_set) {
                values[bar_characters++] = 98; /* SHIFT */
            }

            if (inputData[read] != FNC1) {
                switch (set[read]) { /* Encode data characters */
                case 'A':
                case 'a':
                    getValueSubsetA(inputData[read], values, bar_characters);
                    bar_characters++;
                    read++;
                    break;
                case 'B':
                case 'b':
                    getValueSubsetB(inputData[read], values, bar_characters);
                    bar_characters++;
                    read++;
                    break;
                case 'C':
                    getValueSubsetC(inputData[read], inputData[read + 1], values, bar_characters);
                    bar_characters++;
                    read += 2;
                    break;
                }
            } else {
                values[bar_characters++] = 102; /* FNC1 */
                read++;
            }

            if (bar_characters > 78) { // includes data and initial mode codeword, but not 2 EC codewords
                throw OkapiInputException.inputTooLong();
            }

        } while (read < input_length);

        /* Pad the data */
        pads_needed = 5 - ((bar_characters + 2) % 5); /* 5 codewords per row */
        if (pads_needed == 5) {
            pads_needed = 0;
        }
        if (bar_characters + pads_needed < 8) { /* 2 rows minimum */
            pads_needed += 8 - (bar_characters + pads_needed);
        }
        for (i = 0; i < pads_needed; i++) {
            values[bar_characters++] = 103; /* PAD */
        }

        /* Set the initial mode codeword value - Table 2 */
        rows_needed = (bar_characters + 2) / 5;
        values[0] = (7 * (rows_needed - 2)) + m; /* see 4.3.4.2 */

        /* Calculate check digits */
        first_sum = 0;
        second_sum = 0;
        for (i = 0; i < bar_characters; i++) {
            first_sum += (i + 2) * values[i];
            second_sum += (i + 1) * values[i];
        }
        first_check = first_sum % 107;
        second_sum += first_check * (bar_characters + 1);
        second_check = second_sum % 107;
        values[bar_characters++] = first_check;
        values[bar_characters++] = second_check;

        readable = "";
        pattern = new String[rows_needed];
        rowHeight = new int[rows_needed];
        rowCount = rows_needed;

        infoLine("Initial Mode: ", m);
        infoLine("Symbol Rows: ", rows_needed);
        infoLine("First Check Digit: ", first_check);
        infoLine("Second Check Digit: ", second_check);

        info("Codewords: ");
        for (current_row = 0; current_row < rows_needed; current_row++) {
            width_pattern = "";
            width_pattern += C16K_START_STOP[C16K_START_VALUES[current_row]];
            width_pattern += "1";
            for (i = 0; i < 5; i++) {
                width_pattern += C16K_TABLE[values[(current_row * 5) + i]];
                infoSpace(values[(current_row * 5) + i]);
            }
            width_pattern += C16K_START_STOP[C16K_STOP_VALUES[current_row]];
            pattern[current_row] = width_pattern;
            rowHeight[current_row] = 10;
        }
        infoLine();
    }

    private static int fnc4(char codeSet) {
        switch (codeSet) {
            case 'A':
                return 101;
            case 'B':
                return 100;
            default:
                throw new OkapiInternalException("FNC4 is not available in code set " + codeSet);
        }
    }

    private static void getValueSubsetA(int source, int[] values, int bar_chars) {
        if (source > 127) {
            if (source < 160) {
                values[bar_chars] = source + 64 - 128;
            } else {
                values[bar_chars] = source - 32 - 128;
            }
        } else {
            if (source < 32) {
                values[bar_chars] = source + 64;
            } else {
                values[bar_chars] = source - 32;
            }
        }
    }

    private static void getValueSubsetB(int source, int[] values, int bar_chars) {
        if (source > 127) {
            values[bar_chars] = source - 32 - 128;
        } else {
            values[bar_chars] = source - 32;
        }
    }

    private static void getValueSubsetC(int source_a, int source_b, int[] values, int bar_chars) {
        int weight = (10 * Character.getNumericValue(source_a)) + Character.getNumericValue(source_b);
        values[bar_chars] = weight;
    }

    private static Mode findSubset(int letter) {
        if (letter <= 31) {
            return Mode.SHIFTA;
        } else if ((letter >= 48) && (letter <= 57)) {
            return Mode.ABORC;
        } else if (letter <= 95) {
            return Mode.AORB;
        } else if (letter <= 127) {
            return Mode.SHIFTB;
        } else if (letter <= 159) {
            return Mode.SHIFTA;
        } else if (letter <= 223) {
            return Mode.AORB;
        } else {
            return Mode.SHIFTB;
        }
    }

    /* Implements rules from ISO 15417 Annex E */
    private void reduceSubsetChanges() {
        for (int i = 0; i < block_count; i++) {

            int length = block_length[i];
            Mode current = block_mode[i];

            Mode last;
            if (i != 0) {
                last = block_mode[i - 1];
            } else {
                last = Mode.NULL;
            }

            Mode next;
            if (i != block_count - 1) {
                next = block_mode[i + 1];
            } else {
                next = Mode.NULL;
            }

            if (i == 0) { /* first block */
                if (block_count == 1 && length == 2 && current == Mode.ABORC) { /* Rule 1a */
                    block_mode[i] = Mode.LATCHC;
                }
                if (current == Mode.ABORC) {
                    if (length >= 4) { /* Rule 1b */
                        block_mode[i] = Mode.LATCHC;
                    } else {
                        block_mode[i] = Mode.AORB;
                        current = Mode.AORB;
                    }
                }
                if (current == Mode.SHIFTA) { /* Rule 1c */
                    block_mode[i] = Mode.LATCHA;
                }
                if (current == Mode.AORB && next == Mode.SHIFTA) { /* Rule 1c */
                    block_mode[i] = Mode.LATCHA;
                    current = Mode.LATCHA;
                }
                if (current == Mode.AORB) { /* Rule 1d */
                    block_mode[i] = Mode.LATCHB;
                }
            } else {
                if (current == Mode.ABORC && length >= 4) { /* Rule 3 */
                    block_mode[i] = Mode.LATCHC;
                    current = Mode.LATCHC;
                }
                if (current == Mode.ABORC) {
                    block_mode[i] = Mode.AORB;
                    current = Mode.AORB;
                }
                if (current == Mode.AORB && last == Mode.LATCHA) {
                    block_mode[i] = Mode.LATCHA;
                    current = Mode.LATCHA;
                }
                if (current == Mode.AORB && last == Mode.LATCHB) {
                    block_mode[i] = Mode.LATCHB;
                    current = Mode.LATCHB;
                }
                if (current == Mode.AORB && next == Mode.SHIFTA) {
                    block_mode[i] = Mode.LATCHA;
                    current = Mode.LATCHA;
                }
                if (current == Mode.AORB && next == Mode.SHIFTB) {
                    block_mode[i] = Mode.LATCHB;
                    current = Mode.LATCHB;
                }
                if (current == Mode.AORB) {
                    block_mode[i] = Mode.LATCHB;
                    current = Mode.LATCHB;
                }
                if (current == Mode.SHIFTA && length > 1) { /* Rule 4 */
                    block_mode[i] = Mode.LATCHA;
                    current = Mode.LATCHA;
                }
                if (current == Mode.SHIFTB && length > 1) { /* Rule 5 */
                    block_mode[i] = Mode.LATCHB;
                    current = Mode.LATCHB;
                }
                if (current == Mode.SHIFTA && last == Mode.LATCHA) {
                    block_mode[i] = Mode.LATCHA;
                    current = Mode.LATCHA;
                }
                if (current == Mode.SHIFTB && last == Mode.LATCHB) {
                    block_mode[i] = Mode.LATCHB;
                    current = Mode.LATCHB;
                }
                if (current == Mode.SHIFTA && last == Mode.LATCHC) {
                    block_mode[i] = Mode.LATCHA;
                    current = Mode.LATCHA;
                }
                if (current == Mode.SHIFTB && last == Mode.LATCHC) {
                    block_mode[i] = Mode.LATCHB;
                    current = Mode.LATCHB;
                }
            } /* Rule 2 is implemented elsewhere, Rule 6 is implied */
        }

        combineSubsetBlocks();
    }

    private void combineSubsetBlocks() {
        /* bring together same type blocks */
        if (block_count > 1) {
            int i = 1;
            while (i < block_count) {
                if (block_mode[i - 1] == block_mode[i]) {
                    /* bring together */
                    block_length[i - 1] = block_length[i - 1] + block_length[i];
                    int j = i + 1;
                    /* decrease the list */
                    while (j < block_count) {
                        block_length[j - 1] = block_length[j];
                        block_mode[j - 1] = block_mode[j];
                        j++;
                    }
                    block_count--;
                    i--;
                }
                i++;
            }
        }
    }

    @Override
    protected void plotSymbol() {

        List< Rectangle > dividers = new ArrayList<>();
        int xBlock, yBlock;
        int x, y, w, h;
        boolean black;

        resetPlotElements();

        y = 1;
        h = 1;
        for (yBlock = 0; yBlock < rowCount; yBlock++) {
            black = true;
            x = 15;
            h = rowHeight[yBlock];
            for (xBlock = 0; xBlock < pattern[yBlock].length(); xBlock++) {
                if (black) {
                    black = false;
                    w = pattern[yBlock].charAt(xBlock) - '0';
                    if (w != 0 && h != 0) {
                        addRectangle(new Rectangle(x, y, w, h));
                    }
                    if ((x + w) > symbolWidth) {
                        symbolWidth = x + w;
                    }
                } else {
                    black = true;
                }
                x += pattern[yBlock].charAt(xBlock) - '0';
            }
            y += h;
            if (y > symbolHeight) {
                symbolHeight = y;
            }
            if (yBlock != (rowCount - 1)) {
                dividers.add(new Rectangle(15, y - 1, (symbolWidth - 15), 2));
            }
        }

        /* Add bars between rows last, so they do not interfere with rectangle merging */
        for (Rectangle divider : dividers) {
            addRectangle(divider);
        }

        /* Add top and bottom binding bars */
        Rectangle top = new Rectangle(0, 0, (symbolWidth + 15), 2);
        addRectangle(top);
        Rectangle bottom = new Rectangle(0, y - 1, (symbolWidth + 15), 2);
        addRectangle(bottom);
        symbolWidth += 15;
        symbolHeight += 1;
    }
}
