/*
 * Copyright 2014-2018 Robin Stuart, Daniel Gredler
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

import static java.nio.charset.StandardCharsets.ISO_8859_1;

/**
 * <p>Implements Code 128 bar code symbology according to ISO/IEC 15417:2007.
 *
 * <p>Code 128 supports encoding of 8-bit ISO 8859-1 (Latin-1) characters.
 *
 * <p>Setting GS1 mode allows encoding in GS1-128 (also known as UCC/EAN-128).
 *
 * @author <a href="mailto:rstuart114@gmail.com">Robin Stuart</a>
 * @author Daniel Gredler
 */
public class Code128 extends Symbol {

    private enum Mode {
        NULL, SHIFTA, LATCHA, SHIFTB, LATCHB, SHIFTC, LATCHC, AORB, ABORC
    }

    private enum FMode {
        SHIFTN, LATCHN, SHIFTF, LATCHF
    }

    private enum Composite {
        OFF, CCA, CCB, CCC
    };

    protected static final String[] CODE128_TABLE = {
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
        "211232", "2331112"
    };

    private boolean suppressModeC = false;
    private Composite compositeMode = Composite.OFF;

    /**
     * Optionally prevents this symbol from using subset mode C for numeric data compression.
     *
     * @param suppressModeC whether or not to prevent this symbol from using subset mode C
     */
    public void setSuppressModeC(boolean suppressModeC) {
        this.suppressModeC = suppressModeC;
    }

    /**
     * Returns whether or not this symbol is prevented from using subset mode C for numeric data compression.
     *
     * @return whether or not this symbol is prevented from using subset mode C for numeric data compression
     */
    public boolean getSuppressModeC() {
        return suppressModeC;
    }

    protected void setCca() {
        compositeMode = Composite.CCA;
    }

    protected void setCcb() {
        compositeMode = Composite.CCB;
    }

    protected void setCcc() {
        compositeMode = Composite.CCC;
    }

    public void unsetCc() {
        compositeMode = Composite.OFF;
    }

    @Override
    protected boolean gs1Supported() {
        return true;
    }

    @Override
    protected void encode() {
        int i, j, k;
        int input_point = 0;
        Mode mode, last_mode;
        Mode last_set, current_set;
        double glyph_count;
        int bar_characters = 0, total_sum = 0;
        FMode f_state = FMode.LATCHN;
        Mode[] mode_type = new Mode[200];
        int[] mode_length = new int[200];
        int[] values = new int[200];
        int c;
        int linkage_flag = 0;
        int index_point = 0;
        int read = 0;

        inputData = toBytes(content, ISO_8859_1);
        if (inputData == null) {
            throw new OkapiException("Invalid characters in input data");
        }

        int sourcelen = inputData.length;

        FMode[] fset = new FMode[200];
        Mode[] set = new Mode[200]; /* set[] = Calculated mode for each character */

        if (sourcelen > 170) {
            throw new OkapiException("Input data too long");
        }

        /* Detect extended ASCII characters */
        for (i = 0; i < sourcelen; i++) {
            int ch = inputData[i];
            if (ch >= 128 && ch != FNC1 && ch != FNC2 && ch != FNC3 && ch != FNC4) {
                fset[i] = FMode.SHIFTF;
            } else {
                fset[i] = FMode.LATCHN;
            }
        }

        /* Decide when to latch to extended mode - Annex E note 3 */
        j = 0;
        for (i = 0; i < sourcelen; i++) {
            if (fset[i] == FMode.SHIFTF) {
                j++;
            } else {
                j = 0;
            }
            if (j >= 5) {
                for (k = i; k > (i - 5); k--) {
                    fset[k] = FMode.LATCHF;
                }
            }
            if ((j >= 3) && (i == (sourcelen - 1))) {
                for (k = i; k > (i - 3); k--) {
                    fset[k] = FMode.LATCHF;
                }
            }
        }

        /* Decide if it is worth reverting to 646 encodation for a few characters as described in 4.3.4.2 (d) */
        for (i = 1; i < sourcelen; i++) {
            if ((fset[i - 1] == FMode.LATCHF) && (fset[i] == FMode.LATCHN)) {
                /* Detected a change from 8859-1 to 646 - count how long for */
                for (j = 0; (fset[i + j] == FMode.LATCHN) && ((i + j) < sourcelen); j++);
                if ((j < 5) || ((j < 3) && ((i + j) == (sourcelen - 1)))) {
                    /* Uses the same figures recommended by Annex E note 3 */
                    /* Change to shifting back rather than latching back */
                    for (k = 0; k < j; k++) {
                        fset[i + k] = FMode.SHIFTN;
                    }
                }
            }
        }

        /* Decide on mode using same system as PDF417 and rules of ISO 15417 Annex E */
        int letter = inputData[input_point];
        int numbers = (letter >= '0' && letter <= '9' ? 1 : 0);
        mode = findSubset(letter, numbers);
        mode_type[0] = mode;
        mode_length[0] += length(letter, mode);
        for (i = 1; i < sourcelen; i++) {
            letter = inputData[i];
            last_mode = mode;
            mode = findSubset(letter, numbers);
            if (mode == last_mode) {
                mode_length[index_point] += length(letter, mode);
            } else {
                index_point++;
                mode_type[index_point] = mode;
                mode_length[index_point] = length(letter, mode);
            }
            if (letter >= '0' && letter <= '9') {
                numbers++;
            } else {
                numbers = 0;
            }
        }
        index_point++;
        index_point = reduceSubsetChanges(mode_type, mode_length, index_point);

        /* Put set data into set[] */
        read = 0;
        for (i = 0; i < index_point; i++) {
            for (j = 0; j < mode_length[i]; j++) {
                set[read] = mode_type[i];
                read++;
            }
        }

        /* Resolve odd length LATCHC blocks */
        int cs = 0, nums = 0, fncs = 0;
        for (i = 0; i < read; i++) {
            if (set[i] == Mode.LATCHC) {
                cs++;
                if (inputData[i] >= '0' && inputData[i] <= '9') {
                    nums++;
                } else if (inputData[i] == FNC1) {
                    fncs++;
                }
            } else {
                resolveOddCs(set, i, cs, nums, fncs);
                cs = 0;
                nums = 0;
                fncs = 0;
            }
        }
        resolveOddCs(set, i, cs, nums, fncs);

        /* Adjust for strings which start with shift characters - make them latch instead */
        if (set[0] == Mode.SHIFTA) {
            i = 0;
            do {
                set[i] = Mode.LATCHA;
                i++;
            } while (set[i] == Mode.SHIFTA);
        }
        if (set[0] == Mode.SHIFTB) {
            i = 0;
            do {
                set[i] = Mode.LATCHB;
                i++;
            } while (set[i] == Mode.SHIFTB);
        }

        /* Now we can calculate how long the barcode is going to be - and stop it from being too long */
        last_set = Mode.NULL;
        glyph_count = 0.0;
        for (i = 0; i < sourcelen; i++) {
            if ((set[i] == Mode.SHIFTA) || (set[i] == Mode.SHIFTB)) {
                glyph_count += 1.0;
            }
            if ((fset[i] == FMode.SHIFTF) || (fset[i] == FMode.SHIFTN)) {
                glyph_count += 1.0;
            }
            if (((set[i] == Mode.LATCHA) || (set[i] == Mode.LATCHB)) || (set[i] == Mode.LATCHC)) {
                if (set[i] != last_set) {
                    last_set = set[i];
                    glyph_count += 1.0;
                }
            }
            if (i == 0) {
                if (fset[i] == FMode.LATCHF) {
                    glyph_count += 2.0;
                }
            } else {
                if ((fset[i] == FMode.LATCHF) && (fset[i - 1] != FMode.LATCHF)) {
                    glyph_count += 2.0;
                }
                if ((fset[i] != FMode.LATCHF) && (fset[i - 1] == FMode.LATCHF)) {
                    glyph_count += 2.0;
                }
            }
            if (set[i] == Mode.LATCHC) {
                if (inputData[i] == FNC1) {
                    glyph_count += 1.0;
                } else {
                    glyph_count += 0.5;
                }
            } else {
                glyph_count += 1.0;
            }
        }
        if (glyph_count > 80.0) {
            throw new OkapiException("Input data too long");
        }

        encodeInfo += "Encoding: ";

        /* So now we know what start character to use - we can get on with it! */
        if (readerInit) {
            /* Reader Initialisation mode */
            switch (set[0]) {
                case LATCHA:
                    values[0] = 103;
                    current_set = Mode.LATCHA;
                    values[1] = 96;
                    bar_characters++;
                    encodeInfo += "STARTA FNC3 ";
                    break;
                case LATCHB:
                    values[0] = 104;
                    current_set = Mode.LATCHB;
                    values[1] = 96;
                    bar_characters++;
                    encodeInfo += "STARTB FNC3 ";
                    break;
                default: /* Start C */
                    values[0] = 104;
                    values[1] = 96;
                    values[2] = 99;
                    bar_characters += 2;
                    current_set = Mode.LATCHC;
                    encodeInfo += "STARTB FNC3 CODEC ";
                    break;
            }
        } else {
            /* Normal mode */
            switch (set[0]) {
                case LATCHA:
                    values[0] = 103;
                    current_set = Mode.LATCHA;
                    encodeInfo += "STARTA ";
                    break;
                case LATCHB:
                    values[0] = 104;
                    current_set = Mode.LATCHB;
                    encodeInfo += "STARTB ";
                    break;
                default:
                    values[0] = 105;
                    current_set = Mode.LATCHC;
                    encodeInfo += "STARTC ";
                    break;
            }
        }
        bar_characters++;

        if (inputDataType == DataType.GS1) {
            values[1] = 102;
            bar_characters++;
            encodeInfo += "FNC1 ";
        }

        if (fset[0] == FMode.LATCHF) {
            switch (current_set) {
                case LATCHA:
                    values[bar_characters] = 101;
                    values[bar_characters + 1] = 101;
                    encodeInfo += "FNC4 FNC4 ";
                    break;
                case LATCHB:
                    values[bar_characters] = 100;
                    values[bar_characters + 1] = 100;
                    encodeInfo += "FNC4 FNC4 ";
                    break;
            }
            bar_characters += 2;
            f_state = FMode.LATCHF;
        }

        /* Encode the data */
        read = 0;
        do {

            if ((read != 0) && (set[read] != current_set)) { /* Latch different code set */
                switch (set[read]) {
                    case LATCHA:
                        values[bar_characters] = 101;
                        bar_characters++;
                        current_set = Mode.LATCHA;
                        encodeInfo += "CODEA ";
                        break;
                    case LATCHB:
                        values[bar_characters] = 100;
                        bar_characters++;
                        current_set = Mode.LATCHB;
                        encodeInfo += "CODEB ";
                        break;
                    case LATCHC:
                        values[bar_characters] = 99;
                        bar_characters++;
                        current_set = Mode.LATCHC;
                        encodeInfo += "CODEC ";
                        break;
                }
            }

            if (read != 0) {
                if ((fset[read] == FMode.LATCHF) && (f_state == FMode.LATCHN)) {
                    /* Latch beginning of extended mode */
                    switch (current_set) {
                        case LATCHA:
                            values[bar_characters] = 101;
                            values[bar_characters + 1] = 101;
                            encodeInfo += "FNC4 FNC4 ";
                            break;
                        case LATCHB:
                            values[bar_characters] = 100;
                            values[bar_characters + 1] = 100;
                            encodeInfo += "FNC4 FNC4 ";
                            break;
                    }
                    bar_characters += 2;
                    f_state = FMode.LATCHF;
                }
                if ((fset[read] == FMode.LATCHN) && (f_state == FMode.LATCHF)) {
                    /* Latch end of extended mode */
                    switch (current_set) {
                        case LATCHA:
                            values[bar_characters] = 101;
                            values[bar_characters + 1] = 101;
                            encodeInfo += "FNC4 FNC4 ";
                            break;
                        case LATCHB:
                            values[bar_characters] = 100;
                            values[bar_characters + 1] = 100;
                            encodeInfo += "FNC4 FNC4 ";
                            break;
                    }
                    bar_characters += 2;
                    f_state = FMode.LATCHN;
                }
            }

            if ((fset[read] == FMode.SHIFTF) || (fset[read] == FMode.SHIFTN)) {
                /* Shift to or from extended mode */
                switch (current_set) {
                    case LATCHA:
                        values[bar_characters] = 101;
                        encodeInfo += "FNC4 ";
                        break;
                    case LATCHB:
                        values[bar_characters] = 100;
                        encodeInfo += "FNC4 ";
                        break;
                }
                bar_characters++;
            }

            if ((set[read] == Mode.SHIFTA) || (set[read] == Mode.SHIFTB)) {
                /* Insert shift character */
                values[bar_characters] = 98;
                encodeInfo += "SHFT ";
                bar_characters++;
            }

            /* Encode data characters */
            c = inputData[read];
            switch (set[read]) {
                case SHIFTA:
                case LATCHA:
                    if (c == FNC1) {
                        values[bar_characters] = 102;
                        encodeInfo += "FNC1 ";
                    } else if (c == FNC2) {
                        values[bar_characters] = 97;
                        encodeInfo += "FNC2 ";
                    } else if (c == FNC3) {
                        values[bar_characters] = 96;
                        encodeInfo += "FNC3 ";
                    } else if (c == FNC4) {
                        values[bar_characters] = 101;
                        encodeInfo += "FNC4 ";
                    } else if (c > 127) {
                        if (c < 160) {
                            values[bar_characters] = (c - 128) + 64;
                        } else {
                            values[bar_characters] = (c - 128) - 32;
                        }
                        encodeInfo += Integer.toString(values[bar_characters]) + " ";
                    } else {
                        if (c < 32) {
                            values[bar_characters] = c + 64;
                        } else {
                            values[bar_characters] = c - 32;
                        }
                        encodeInfo += Integer.toString(values[bar_characters]) + " ";
                    }
                    bar_characters++;
                    read++;
                    break;
                case SHIFTB:
                case LATCHB:
                    if (c == FNC1) {
                        values[bar_characters] = 102;
                        encodeInfo += "FNC1 ";
                    } else if (c == FNC2) {
                        values[bar_characters] = 97;
                        encodeInfo += "FNC2 ";
                    } else if (c == FNC3) {
                        values[bar_characters] = 96;
                        encodeInfo += "FNC3 ";
                    } else if (c == FNC4) {
                        values[bar_characters] = 100;
                        encodeInfo += "FNC4 ";
                    } else if (c > 127) {
                        values[bar_characters] = c - 32 - 128;
                        encodeInfo += Integer.toString(values[bar_characters]) + " ";
                    } else {
                        values[bar_characters] = c - 32;
                        encodeInfo += Integer.toString(values[bar_characters]) + " ";
                    }
                    bar_characters++;
                    read++;
                    break;
                case LATCHC:
                    if (c == FNC1) {
                        values[bar_characters] = 102;
                        encodeInfo += "FNC1 ";
                        bar_characters++;
                        read++;
                    } else {
                        int d = inputData[read + 1];
                        int weight = (10 * (c - '0')) + (d - '0');
                        values[bar_characters] = weight;
                        encodeInfo += Integer.toString(values[bar_characters]) + " ";
                        bar_characters++;
                        read += 2;
                    }
                    break;
            }

        } while (read < sourcelen);

        encodeInfo += "\n";

        /* "...note that the linkage flag is an extra code set character between
        the last data character and the Symbol Check Character" (GS1 Specification) */

        /* Linkage flags in GS1-128 are determined by ISO/IEC 24723 section 7.4 */

        switch (compositeMode) {
            case CCA:
            case CCB:
                /* CC-A or CC-B 2D component */
                switch(set[sourcelen - 1]) {
                    case LATCHA: linkage_flag = 100; break;
                    case LATCHB: linkage_flag = 99; break;
                    case LATCHC: linkage_flag = 101; break;
                }
                encodeInfo += "Linkage Flag: " + linkage_flag + '\n';
                break;
            case CCC:
                /* CC-C 2D component */
                switch(set[sourcelen - 1]) {
                    case LATCHA: linkage_flag = 99; break;
                    case LATCHB: linkage_flag = 101; break;
                    case LATCHC: linkage_flag = 100; break;
                }
                encodeInfo += "Linkage Flag: " + linkage_flag + '\n';
                break;
            default:
                break;
        }

        if (linkage_flag != 0) {
            values[bar_characters] = linkage_flag;
            bar_characters++;
        }

        encodeInfo += "Data Codewords: " + bar_characters + '\n';

        /* Check digit calculation */
        for (i = 0; i < bar_characters; i++) {
            total_sum += (i == 0 ? values[i] : values[i] * i);
        }
        int checkDigit = total_sum % 103;
        encodeInfo += "Check Digit: " + checkDigit + '\n';

        /* Build pattern string */
        StringBuilder dest = new StringBuilder(bar_characters + 2);
        for (i = 0; i < bar_characters; i++) {
            dest.append(CODE128_TABLE[values[i]]);
        }
        dest.append(CODE128_TABLE[checkDigit]);
        dest.append(CODE128_TABLE[106]); // stop character

        /* Readable text */
        if (inputDataType != DataType.GS1) {
            readable = removeFncEscapeSequences(content);
            if (inputDataType == DataType.HIBC) {
                readable = "*" + readable + "*";
            }
        }

        if (compositeMode == Composite.OFF) {
            pattern = new String[] { dest.toString() };
            row_height = new int[] { -1 };
            row_count = 1;
        } else {
            /* Add the separator pattern for composite symbols */
            pattern = new String[] { "0" + dest, dest.toString() };
            row_height = new int[] { 1, -1 };
            row_count = 2;
        }
    }

    private static String removeFncEscapeSequences(String s) {
        return s.replace(FNC1_STRING, "")
                .replace(FNC2_STRING, "")
                .replace(FNC3_STRING, "")
                .replace(FNC4_STRING, "");
    }

    private void resolveOddCs(Mode[] set, int i, int cs, int nums, int fncs) {
        if ((nums & 1) != 0) {
            int index;
            Mode m;
            if (i - cs == 0 || fncs > 0) {
                // Rule 2: first block -> swap last digit to A or B
                index = i - 1;
                if (index + 1 < set.length && set[index + 1] != null && set[index + 1] != Mode.LATCHC) {
                    // next block is either A or B -- match it
                    m = set[index + 1];
                } else {
                    // next block is C, or there is no next block -- just latch to B
                    m = Mode.LATCHB;
                }
            } else {
                // Rule 3b: subsequent block -> swap first digit to A or B
                // Note that we make an exception for C blocks which contain one (or more) FNC1 characters,
                // since swapping the first digit would place the FNC1 in an invalid position in the block
                index = i - nums;
                if (index - 1 >= 0 && set[index - 1] != null && set[index - 1] != Mode.LATCHC) {
                    // previous block is either A or B -- match it
                    m = set[index - 1];
                } else {
                    // previous block is C, or there is no previous block -- just latch to B
                    m = Mode.LATCHB;
                }
            }
            set[index] = m;
        }
    }

    private Mode findSubset(int letter, int numbers) {

        Mode mode;

        if (letter == FNC1) {
            if (numbers % 2 == 0) {
                /* ISO 15417 Annex E Note 2 */
                /* FNC1 may use subset C, so long as it doesn't break data into an odd number of digits */
                mode = Mode.ABORC;
            } else {
                mode = Mode.AORB;
            }
        } else if (letter == FNC2 || letter == FNC3 || letter == FNC4) {
            mode = Mode.AORB;
        } else if (letter <= 31) {
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

        if (suppressModeC && mode == Mode.ABORC) {
            mode = Mode.AORB;
        }

        return mode;
    }

    private int length(int letter, Mode mode) {
        if (letter == FNC1 && mode == Mode.ABORC) {
            /* ISO 15417 Annex E Note 2 */
            /* Logical length used for making subset switching decisions, not actual length */
            return 2;
        } else {
            return 1;
        }
    }

    /** Implements rules from ISO 15417 Annex E. Returns the updated index point. */
    private int reduceSubsetChanges(Mode[] mode_type, int[] mode_length, int index_point) {

        int totalLength = 0;
        int i, length;
        Mode current, last, next;

        for (i = 0; i < index_point; i++) {
            current = mode_type[i];
            length = mode_length[i];
            if (i != 0) {
                last = mode_type[i - 1];
            } else {
                last = Mode.NULL;
            }
            if (i != index_point - 1) {
                next = mode_type[i + 1];
            } else {
                next = Mode.NULL;
            }

            /* ISO 15417 Annex E Note 2 */
            /* Calculate difference between logical length and actual length in this block */
            int extraLength = 0;
            for (int j = 0; j < length - extraLength; j++) {
                if (length(inputData[totalLength + j], current) == 2) {
                    extraLength++;
                }
            }

            if (i == 0) { /* first block */
                if ((index_point == 1) && ((length == 2) && (current == Mode.ABORC))) { /* Rule 1a */
                    mode_type[i] = Mode.LATCHC;
                    current = Mode.LATCHC;
                }
                if (current == Mode.ABORC) {
                    if (length >= 4) { /* Rule 1b */
                        mode_type[i] = Mode.LATCHC;
                        current = Mode.LATCHC;
                    } else {
                        mode_type[i] = Mode.AORB;
                        current = Mode.AORB;
                    }
                }
                if (current == Mode.SHIFTA) { /* Rule 1c */
                    mode_type[i] = Mode.LATCHA;
                    current = Mode.LATCHA;
                }
                if ((current == Mode.AORB) && (next == Mode.SHIFTA)) { /* Rule 1c */
                    mode_type[i] = Mode.LATCHA;
                    current = Mode.LATCHA;
                }
                if (current == Mode.AORB) { /* Rule 1d */
                    mode_type[i] = Mode.LATCHB;
                    current = Mode.LATCHB;
                }
            } else {
                if ((current == Mode.ABORC) && (length >= 4)) { /* Rule 3 */
                    mode_type[i] = Mode.LATCHC;
                    current = Mode.LATCHC;
                }
                if (current == Mode.ABORC) {
                    mode_type[i] = Mode.AORB;
                    current = Mode.AORB;
                }
                if ((current == Mode.AORB) && (last == Mode.LATCHA)) {
                    mode_type[i] = Mode.LATCHA;
                    current = Mode.LATCHA;
                }
                if ((current == Mode.AORB) && (last == Mode.LATCHB)) {
                    mode_type[i] = Mode.LATCHB;
                    current = Mode.LATCHB;
                }
                if ((current == Mode.AORB) && (next == Mode.SHIFTA)) {
                    mode_type[i] = Mode.LATCHA;
                    current = Mode.LATCHA;
                }
                if ((current == Mode.AORB) && (next == Mode.SHIFTB)) {
                    mode_type[i] = Mode.LATCHB;
                    current = Mode.LATCHB;
                }
                if (current == Mode.AORB) {
                    mode_type[i] = Mode.LATCHB;
                    current = Mode.LATCHB;
                }
                if ((current == Mode.SHIFTA) && (length > 1)) { /* Rule 4 */
                    mode_type[i] = Mode.LATCHA;
                    current = Mode.LATCHA;
                }
                if ((current == Mode.SHIFTB) && (length > 1)) { /* Rule 5 */
                    mode_type[i] = Mode.LATCHB;
                    current = Mode.LATCHB;
                }
                if ((current == Mode.SHIFTA) && (last == Mode.LATCHA)) {
                    mode_type[i] = Mode.LATCHA;
                    current = Mode.LATCHA;
                }
                if ((current == Mode.SHIFTB) && (last == Mode.LATCHB)) {
                    mode_type[i] = Mode.LATCHB;
                    current = Mode.LATCHB;
                }
                if ((current == Mode.SHIFTA) && (next == Mode.AORB)) {
                    mode_type[i] = Mode.LATCHA;
                    current = Mode.LATCHA;
                }
                if ((current == Mode.SHIFTB) && (next == Mode.AORB)) {
                    mode_type[i] = Mode.LATCHB;
                    current = Mode.LATCHB;
                }
                if ((current == Mode.SHIFTA) && (last == Mode.LATCHC)) {
                    mode_type[i] = Mode.LATCHA;
                    current = Mode.LATCHA;
                }
                if ((current == Mode.SHIFTB) && (last == Mode.LATCHC)) {
                    mode_type[i] = Mode.LATCHB;
                    current = Mode.LATCHB;
                }
            } /* Rule 2 is implemented elsewhere, Rule 6 is implied */

            /* ISO 15417 Annex E Note 2 */
            /* Convert logical length back to actual length for this block, now that we've decided on a subset */
            mode_length[i] -= extraLength;
            totalLength += mode_length[i];
        }

        return combineSubsetBlocks(mode_type, mode_length, index_point);
    }

    /** Modifies the specified mode and length arrays to combine adjacent modes of the same type, returning the updated index point. */
    private int combineSubsetBlocks(Mode[] mode_type, int[] mode_length, int index_point) {
        /* bring together same type blocks */
        if (index_point > 1) {
            for (int i = 1; i < index_point; i++) {
                if (mode_type[i - 1] == mode_type[i]) {
                    /* bring together */
                    mode_length[i - 1] = mode_length[i - 1] + mode_length[i];
                    /* decrease the list */
                    for (int j = i + 1; j < index_point; j++) {
                        mode_length[j - 1] = mode_length[j];
                        mode_type[j - 1] = mode_type[j];
                    }
                    index_point--;
                    i--;
                }
            }
        }
        return index_point;
    }

    /** {@inheritDoc} */
    @Override
    protected int[] getCodewords() {
        return getPatternAsCodewords(6);
    }
}
