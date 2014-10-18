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

import java.io.UnsupportedEncodingException;
import java.awt.Rectangle;

/**
 * Implements Code 16K symbology
 * According to BS EN 12323:2005
 *
 * @author Robin Stuart <rstuart114@gmail.com>
 * @version 0.1
 */
public class Code16k extends Symbol {

    private enum Mode {
        NULL, SHIFTA, LATCHA, SHIFTB, LATCHB, SHIFTC, LATCHC, AORB, ABORC, CANDB, CANDBB
    }

    /* EN 12323 Table 1 - "Code 16K" character encodations */
    private String[] C16KTable = {
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
    private String[] C16KStartStop = {
        "3211", "2221", "2122", "1411", "1132", "1231", "1114", "3112"
    };

    /* EN 12323 Table 5 - Start and stop values defining row numbers */
    private int[] C16KStartValues = {
        0, 1, 2, 3, 4, 5, 6, 7, 0, 1, 2, 3, 4, 5, 6, 7
    };
    private int[] C16KStopValues = {
        0, 1, 2, 3, 4, 5, 6, 7, 4, 5, 6, 7, 0, 1, 2, 3
    };

    private Mode[] block_mode = new Mode[170]; /* RENAME block_mode */
    private int[] block_length = new int[170]; /* RENAME block_length */
    private int block_count;

    @Override
    public boolean encode() {
        String width_pattern;
        int current_row, rows_needed, flip_flop, looper, first_check, second_check;
        int indexchaine, pads_needed;
        char[] set, fset;
        Mode mode;
        char last_set, last_fset, current_set;
        int i, j, k, m, e_count, read, mx_reader, writer;
        int[] values;
        int bar_characters;
        double glyph_count;
        int errornum, first_sum, second_sum;
        int input_length;
        int c_count;
        boolean f_state;
        byte[] inputBytes;
        int[] inputData;

        try {
            inputBytes = content.getBytes("ISO8859_1");
        } catch (UnsupportedEncodingException e) {
            error_msg = "Invalid character in input data";
            return false;
        }

        input_length = content.length();
        inputData = new int[input_length];
        for (i = 0; i < input_length; i++) {
            inputData[i] = inputBytes[i] & 0xFF;
        }

        e_count = 0;
        bar_characters = 0;
        set = new char[160];
        fset = new char[160];
        values = new int[160];

        if (input_length > 157) {
            error_msg = "Input too long";
            return false;
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
                    for (k = 0; k <= j; k++) {
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
        if ((gs1) && (inputData[indexchaine] == '[')) {
            mode = Mode.ABORC;
        } /* FNC1 */

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
                    if ((gs1) && (inputData[indexchaine] == '[')) {
                        mode = Mode.ABORC;
                    } /* FNC1 */
                }
            }
            block_count++;
        } while (indexchaine < input_length);

        reduceSubsetChanges(block_count);


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
                if (inputData[i] == '[') {
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
            if ((set[i] == 'C') && ((set[i - 1] == 'B') && (set[i + 1] == 'B'))) {
                set[i] = 'B';
            }
        }

        /* Make sure the data will fit in the symbol */
        last_set = ' ';
        last_fset = ' ';
        glyph_count = 0.0;
        for (i = 0; i < input_length; i++) {
            if ((set[i] == 'a') || (set[i] == 'b')) {
                glyph_count = glyph_count + 1.0;
            }
            if ((fset[i] == 'f') || (fset[i] == 'n')) {
                glyph_count = glyph_count + 1.0;
            }
            if (((set[i] == 'A') || (set[i] == 'B')) || (set[i] == 'C')) {
                if (set[i] != last_set) {
                    last_set = set[i];
                    glyph_count = glyph_count + 1.0;
                }
            }
            if (i == 0) {
                if ((set[i] == 'B') && (set[1] == 'C')) {
                    glyph_count = glyph_count - 1.0;
                }
                if ((set[i] == 'B') && (set[1] == 'B')) {
                    if (set[2] == 'C') {
                        glyph_count = glyph_count - 1.0;
                    }
                }
                if (fset[i] == 'F') {
                    last_fset = 'F';
                    glyph_count = glyph_count + 2.0;
                }
            } else {
                if ((fset[i] == 'F') && (fset[i - 1] != 'F')) {
                    last_fset = 'F';
                    glyph_count = glyph_count + 2.0;
                }
                if ((fset[i] != 'F') && (fset[i - 1] == 'F')) {
                    last_fset = ' ';
                    glyph_count = glyph_count + 2.0;
                }
            }

            if ((set[i] == 'C') && (!((gs1) && (content.charAt(i) == '[')))) {
                glyph_count = glyph_count + 0.5;
            } else {
                glyph_count = glyph_count + 1.0;
            }
        }

        if ((gs1) && (set[0] != 'A')) {
            /* FNC1 can be integrated with mode character */
            glyph_count--;
        }

        if (glyph_count > 77.0) {
            error_msg = "Input too long";
            return false;
        }

        /* Calculate how tall the symbol will be */
        glyph_count = glyph_count + 2.0;
        i = (int) glyph_count;
        rows_needed = (i / 5);
        if (i % 5 > 0) {
            rows_needed++;
        }

        if (rows_needed == 1) {
            rows_needed = 2;
        }

        /* start with the mode character - Table 2 */
        m = 0;
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
        }

        if(readerInit) {
        	if(m == 2) { m = 5; }
        	if(gs1) {
        		error_msg = "Cannot use both GS1 mode and Reader Initialisation";
        		return false;
        	} else {
        		if((set[0] == 'B') && (set[1] == 'C')) { m = 6; }
        	}
        	values[bar_characters] = (7 * (rows_needed - 2)) + m; /* see 4.3.4.2 */
        	values[bar_characters + 1] = 96; /* FNC3 */
        	bar_characters += 2;
        } else {
            if (gs1) {
                /* Integrate FNC1 */
                switch (set[0]) {
                case 'B':
                    m = 3;
                    break;
                case 'C':
                    m = 4;
                    break;
                }
            } else {
                if ((set[0] == 'B') && (set[1] == 'C')) {
                    m = 5;
                }
                if (((set[0] == 'B') && (set[1] == 'B')) && (set[2] == 'C')) {
                    m = 6;
                }
            }
        }
        values[bar_characters] = (7 * (rows_needed - 2)) + m; /* see 4.3.4.2 */
        bar_characters++;
        //}
        current_set = set[0];
        f_state = false;
        /* f_state remembers if we are in Extended ASCII mode (value 1) or
	in ISO/IEC 646 mode (value 0) */
        if (fset[0] == 'F') {
            switch (current_set) {
            case 'A':
                values[bar_characters] = 101;
                values[bar_characters + 1] = 101;
                break;
            case 'B':
                values[bar_characters] = 100;
                values[bar_characters + 1] = 100;
                break;
            }
            bar_characters += 2;
            f_state = true;
        }

        read = 0;

        /* Encode the data */
        do {

            if ((read != 0) && (set[read] != set[read - 1])) { /* Latch different code set */
                switch (set[read]) {
                case 'A':
                    values[bar_characters] = 101;
                    bar_characters++;
                    current_set = 'A';
                    break;
                case 'B':
                    values[bar_characters] = 100;
                    bar_characters++;
                    current_set = 'B';
                    break;
                case 'C':
                    if (!((read == 1) && (set[0] == 'B'))) { /* Not Mode C/Shift B */
                        if (!((read == 2) && ((set[0] == 'B') && (set[1] == 'B')))) {
                            /* Not Mode C/Double Shift B */
                            values[bar_characters] = 99;
                            bar_characters++;
                        }
                    }
                    current_set = 'C';
                    break;
                }
            }
            if (read != 0) {
                if ((fset[read] == 'F') && (f_state == false)) {
                    /* Latch beginning of extended mode */
                    switch (current_set) {
                    case 'A':
                        values[bar_characters] = 101;
                        values[bar_characters + 1] = 101;
                        break;
                    case 'B':
                        values[bar_characters] = 100;
                        values[bar_characters + 1] = 100;
                        break;
                    }
                    bar_characters += 2;
                    f_state = true;
                }
                if ((fset[read] == ' ') && (f_state == true)) {
                    /* Latch end of extended mode */
                    switch (current_set) {
                    case 'A':
                        values[bar_characters] = 101;
                        values[bar_characters + 1] = 101;
                        break;
                    case 'B':
                        values[bar_characters] = 100;
                        values[bar_characters + 1] = 100;
                        break;
                    }
                    bar_characters += 2;
                    f_state = false;
                }
            }

            if ((fset[i] == 'f') || (fset[i] == 'n')) {
                /* Shift extended mode */
                switch (current_set) {
                case 'A':
                    values[bar_characters] = 101; /* FNC 4 */
                    break;
                case 'B':
                    values[bar_characters] = 100; /* FNC 4 */
                    break;
                }
                bar_characters++;
            }

            if ((set[i] == 'a') || (set[i] == 'b')) {
                /* Insert shift character */
                values[bar_characters] = 98;
                bar_characters++;
            }

            if (!((gs1) && (inputData[read] == '['))) {
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
                values[bar_characters] = 102;
                bar_characters++;
                read++;
            }

        } while (read < input_length);

        pads_needed = 5 - ((bar_characters + 2) % 5);
        if (pads_needed == 5) {
            pads_needed = 0;
        }
        if ((bar_characters + pads_needed) < 8) {
            pads_needed += 8 - (bar_characters + pads_needed);
        }
        for (i = 0; i < pads_needed; i++) {
            values[bar_characters] = 106;
            bar_characters++;
        }

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
        values[bar_characters] = first_check;
        values[bar_characters + 1] = second_check;
        bar_characters += 2;

        readable = "";
        pattern = new String[rows_needed];
        row_count = rows_needed;
        row_height = new int[rows_needed];

        encodeInfo += "Symbol Rows: " + rows_needed + "\n";
        encodeInfo += "First Check Digit: " + first_check + "\n";
        encodeInfo += "Second Check Digit: " + second_check + "\n";
        
        for (current_row = 0; current_row < rows_needed; current_row++) {

            width_pattern = "";
            width_pattern += C16KStartStop[C16KStartValues[current_row]];
            width_pattern += "1";
            for (i = 0; i < 5; i++) {
                width_pattern += C16KTable[values[(current_row * 5) + i]];
                if (debug) {
                    System.out.print("[" + values[(current_row * 5) + i] + "] ");
                }

            }
            width_pattern += C16KStartStop[C16KStopValues[current_row]];
            if (debug) {
                System.out.println();
            }

            pattern[current_row] = width_pattern;
            row_height[current_row] = 10;
        }
        plotSymbol();
        return true;

    }

    private void getValueSubsetA(int source, int[] values, int bar_chars) {
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

    private void getValueSubsetB(int source, int[] values, int bar_chars) {
        if (source > 127) {
            values[bar_chars] = source - 32 - 128;
        } else {
            values[bar_chars] = source - 32;
        }
    }

    private void getValueSubsetC(int source_a, int source_b, int values[], int bar_chars) {
        int weight;

        weight = (10 * Character.getNumericValue(source_a)) + Character.getNumericValue(source_b);
        values[bar_chars] = weight;
    }

    private Mode findSubset(int letter) {
        Mode mode;

        if (letter <= 31) {
            mode = Mode.SHIFTA;
        } else if ((letter >= 48) && (letter <= 57)) {
            mode = Mode.ABORC;
        } else if (letter <= 95) {
            mode = Mode.AORB;
        } else if (letter <= 127) {
            mode = Mode.SHIFTB;
        } else if (letter <= 159) {
            mode = Mode.SHIFTA;
        } else if (letter <= 223) {
            mode = Mode.AORB;
        } else {
            mode = Mode.SHIFTB;
        }

        return mode;
    }

    private void reduceSubsetChanges(int block_count) { /* Implements rules from ISO 15417 Annex E */
        int i, length;
        Mode current, last, next;

        for (i = 0; i < block_count; i++) {
            current = block_mode[i];
            length = block_length[i];
            if (i != 0) {
                last = block_mode[i - 1];
            } else {
                last = Mode.NULL;
            }
            if (i != block_count - 1) {
                next = block_mode[i + 1];
            } else {
                next = Mode.NULL;
            }

            if (i == 0) { /* first block */
                if ((block_count == 1) && ((length == 2) && (current == Mode.ABORC))) { /* Rule 1a */
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
                if ((current == Mode.AORB) && (next == Mode.SHIFTA)) { /* Rule 1c */
                    block_mode[i] = Mode.LATCHA;
                    current = Mode.LATCHA;
                }
                if (current == Mode.AORB) { /* Rule 1d */
                    block_mode[i] = Mode.LATCHB;
                }
            } else {
                if ((current == Mode.ABORC) && (length >= 4)) { /* Rule 3 */
                    block_mode[i] = Mode.LATCHC;
                    current = Mode.LATCHC;
                }
                if (current == Mode.ABORC) {
                    block_mode[i] = Mode.AORB;
                    current = Mode.AORB;
                }
                if ((current == Mode.AORB) && (last == Mode.LATCHA)) {
                    block_mode[i] = Mode.LATCHA;
                    current = Mode.LATCHA;
                }
                if ((current == Mode.AORB) && (last == Mode.LATCHB)) {
                    block_mode[i] = Mode.LATCHB;
                    current = Mode.LATCHB;
                }
                if ((current == Mode.AORB) && (next == Mode.SHIFTA)) {
                    block_mode[i] = Mode.LATCHA;
                    current = Mode.LATCHA;
                }
                if ((current == Mode.AORB) && (next == Mode.SHIFTB)) {
                    block_mode[i] = Mode.LATCHB;
                    current = Mode.LATCHB;
                }
                if (current == Mode.AORB) {
                    block_mode[i] = Mode.LATCHB;
                    current = Mode.LATCHB;
                }
                if ((current == Mode.SHIFTA) && (length > 1)) { /* Rule 4 */
                    block_mode[i] = Mode.LATCHA;
                    current = Mode.LATCHA;
                }
                if ((current == Mode.SHIFTB) && (length > 1)) { /* Rule 5 */
                    block_mode[i] = Mode.LATCHB;
                    current = Mode.LATCHB;
                }
                if ((current == Mode.SHIFTA) && (last == Mode.LATCHA)) {
                    block_mode[i] = Mode.LATCHA;
                    current = Mode.LATCHA;
                }
                if ((current == Mode.SHIFTB) && (last == Mode.LATCHB)) {
                    block_mode[i] = Mode.LATCHB;
                    current = Mode.LATCHB;
                }
                if ((current == Mode.SHIFTA) && (last == Mode.LATCHC)) {
                    block_mode[i] = Mode.LATCHA;
                    current = Mode.LATCHA;
                }
                if ((current == Mode.SHIFTB) && (last == Mode.LATCHC)) {
                    block_mode[i] = Mode.LATCHB;
                    current = Mode.LATCHB;
                }
            } /* Rule 2 is implimented elsewhere, Rule 6 is implied */
        }
        combineSubsetBlocks(block_count);

    }

    private void combineSubsetBlocks(int block_count) {
        int i, j;

        /* bring together same type blocks */
        if (block_count > 1) {
            i = 1;
            while (i < block_count) {
                if (block_mode[i - 1] == block_mode[i]) {
                    /* bring together */
                    block_length[i - 1] = block_length[i - 1] + block_length[i];
                    j = i + 1;

                    /* decreace the list */
                    while (j < block_count) {
                        block_length[j - 1] = block_length[j];
                        block_mode[j - 1] = block_mode[j];
                        j++;
                    }
                    block_count = block_count - 1;
                    i--;
                }
                i++;
            }
        }
    }

    @Override
    public void plotSymbol() {
        int xBlock, yBlock;
        int x, y, w, h;
        boolean black;

        rect.clear();
        y = 1;
        h = 1;
        for (yBlock = 0; yBlock < row_count; yBlock++) {
            black = true;
            x = 15;
            for (xBlock = 0; xBlock < pattern[yBlock].length(); xBlock++) {
                if (black == true) {
                    black = false;
                    w = pattern[yBlock].charAt(xBlock) - '0';
                    if (row_height[yBlock] == -1) {
                        h = default_height;
                    } else {
                        h = row_height[yBlock];
                    }
                    Rectangle thisrect = new Rectangle(x, y, w, h);
                    if ((w != 0.0) && (h != 0.0)) {
                        rect.add(thisrect);
                    }
                    if ((x + w) > symbol_width) {
                        symbol_width = x + w;
                    }
                } else {
                    black = true;
                }
                x += (double)(pattern[yBlock].charAt(xBlock) - '0');
            }
            y += h;
            if ((y + h) > symbol_height) {
                symbol_height = y + h;
            }
            /* Add bars between rows */
            if (yBlock != (row_count - 1)) {
                Rectangle thisrect = new Rectangle(15, y - 1, (symbol_width - 15), 2);
                rect.add(thisrect);
            }
        }
        /* Add top and bottom binding bars */
        Rectangle toprect = new Rectangle(0, 0, (symbol_width + 15), 2);
        rect.add(toprect);
        Rectangle botrect = new Rectangle(0, y - 1, (symbol_width + 15), 2);
        rect.add(botrect);
        symbol_width += 30.0;
        symbol_height += 2.0;
    }

}
