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

/**
 * Implements Code 128 bar code symbology
 * According to ISO/IEC 15417:2007
 * <p>
 * Code 128 supports encoding of 8-bit ISO 8859-1 (Latin-1) characters.
 * Setting GS1 mode allows encoding in GS1-128 (also known as UPC/EAN-128).
 *
 * @author <a href="mailto:rstuart114@gmail.com">Robin Stuart</a>
 */

public class Code128 extends Symbol {
    private enum Mode {
        NULL, SHIFTA, LATCHA, SHIFTB, LATCHB, SHIFTC, LATCHC, AORB, ABORC
    }
    private enum FMode {
        SHIFTN, LATCHN, SHIFTF, LATCHF
    }

    private String[] code128Table = {
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

    private Mode[] mode_type = new Mode[200];
    private int[] mode_length = new int[200];
    private int index_point = 0, read = 0;
    private boolean modeCSupression;
    private enum Composite { OFF, CCA, CCB, CCC };
    private Composite compositeMode;

    public Code128() {
        modeCSupression = false;
        compositeMode = Composite.OFF;
    }

    /**
     * Allow the use of subset C (numeric compression) in encoding (default).
     */
    public void useModeC() {
        modeCSupression = false;
    }

    /**
     * Disallow the use of subset C (numeric compression) in encoding.
     * Numeric values will be encoded using subset B.
     */
    public void stopModeC() {
        modeCSupression = true;
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
    public boolean encode() {
        int sourcelen = content.length();
        int i, j, k;
        int input_point = 0;
        Mode mode, last_mode;
        Mode last_set, current_set;
        double glyph_count;
        int bar_characters = 0, total_sum = 0;
        FMode f_state = FMode.LATCHN;
        int[] values = new int[200];
        int c;
        String dest = "";
        int[] inputData;
        int c_count;
        int linkage_flag = 0;
        
        if (!content.matches("[\u0000-\u00FF]+")) {
            error_msg = "Invalid characters in input data";
            return false;
        }
        
        try {
            inputBytes = content.getBytes("ISO8859_1");
        } catch (UnsupportedEncodingException e) {
            error_msg = "Character encoding error";
            return false;
        }

        inputData = new int[sourcelen];
        for (i = 0; i < sourcelen; i++) {
            inputData[i] = inputBytes[i] & 0xFF;
        }

        FMode[] fset = new FMode[200];
        Mode[] set = new Mode[200]; /* set[] = Calculated mode for each character */

        if (sourcelen > 170) {
            error_msg = "Input data too long";
            return false;
        }

        /* Detect extended ASCII characters */
        for (i = 0; i < sourcelen; i++) {
            if (inputData[i] >= 128) {
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
        mode = findSubset(inputData[input_point]);
        mode_type[0] = mode;
        mode_length[0] = 1;

        if(inputDataType == DataType.GS1) {
            mode = Mode.ABORC;
        }

        if((modeCSupression) && (mode == Mode.ABORC)) {
            mode = Mode.AORB;
        }

        for (i = 1; i < sourcelen; i++) {
            last_mode = mode;
            mode = findSubset(inputData[i]);
            if((inputDataType == DataType.GS1) && inputData[i] == '[') {
                mode = Mode.ABORC;
            }
            if((modeCSupression) && (mode == Mode.ABORC)) {
                mode = Mode.AORB;
            }
            if (mode == last_mode) {
                mode_length[index_point]++;
            } else {
                index_point++;
                mode_type[index_point] = mode;
                mode_length[index_point] = 1;
            }
        }
        index_point++;

        reduceSubsetChanges();


        if (inputDataType == DataType.GS1) {
            /* Put set data into set[] */
            read = 0;
            for(i = 0; i < index_point; i++) {
                for(j = 0; j < mode_length[i]; j++) {
                        set[read] = mode_type[i];
                    read++;
                }
            }

            /* Resolve odd length LATCHC blocks */
            c_count = 0;
            for(i = 0; i < read; i++) {
		if(set[i] == Mode.LATCHC) {
                    if(inputData[i] == '[') {
                        if((c_count & 1) != 0) {
                            if((i - c_count) != 0) {
                                set[i - c_count] = Mode.LATCHB;
                            } else {
                                set[i - 1] = Mode.LATCHB;
                            }
                        }
                        c_count = 0;
                    } else {
                        c_count++;
                    }
		} else {
                    if((c_count & 1) != 0) {
                        if((i - c_count) != 0) {
                            set[i - c_count] = Mode.LATCHB;
                        } else {
                            set[i - 1] = Mode.LATCHB;
                        }
                    }
                    c_count = 0;
		}
            }
            if((c_count & 1) != 0) {
		if((i - c_count) != 0) {
                    set[i - c_count] = Mode.LATCHB;
		} else {
                    set[i - 1] = Mode.LATCHB;
		}
            }
            for(i = 1; i < read - 1; i++) {
                if((set[i] == Mode.LATCHC) && ((set[i - 1] == Mode.LATCHB)
                        && (set[i + 1] == Mode.LATCHB))) {
                    set[i] = Mode.LATCHB;
                }
            }
	} else {
            /* Resolve odd length LATCHC blocks */

            if ((mode_type[0] == Mode.LATCHC) && ((mode_length[0] & 1) != 0)) {
                /* Rule 2 */
                mode_length[1]++;
                mode_length[0]--;
                if (index_point == 1) {
                    mode_length[1] = 1;
                    mode_type[1] = Mode.LATCHB;
                    index_point = 2;
                }
            }
            if (index_point > 1) {
                for (i = 1; i < index_point; i++) {
                    if ((mode_type[i] == Mode.LATCHC) && ((mode_length[i] & 1) != 0)) {
                        /* Rule 3b */
                        mode_length[i - 1]++;
                        mode_length[i]--;
                    }
                }
            }

            /* Put set data into set[] */
            for (i = 0; i < index_point; i++) {
                for (j = 0; j < mode_length[i]; j++) {
                    set[read] = mode_type[i];
                    read++;
                }
            }
        }

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

        /* Now we can calculate how long the barcode is going to be - and stop it from
	   being too long */
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
                if ((inputDataType == DataType.GS1) && (inputData[i] == '[')) {
                    glyph_count += 1.0;
                } else {
                    glyph_count += 0.5;
                }
            } else {
                glyph_count += 1.0;
            }
        }
        if (glyph_count > 80.0) {
            error_msg = "Input data too long";
            return false;
        }

        encodeInfo += "Encoding: ";

        /* So now we know what start character to use - we can get on with it! */
        if(readerInit) {
        /* Reader Initialisation mode */
            switch(set[0]) {
                case LATCHA: /* Start A */
                    dest += code128Table[103];
                    values[0] = 103;
                    current_set = Mode.LATCHA;
                    dest += code128Table[96]; /* FNC3 */
                    values[1] = 96;
                    bar_characters++;
                    encodeInfo += "STARTA FNC3 ";
                    break;
                case LATCHB: /* Start B */
                    dest += code128Table[104];
                    values[0] = 104;
                    current_set = Mode.LATCHB;
                    dest += code128Table[96]; /* FNC3 */
                    values[1] = 96;
                    bar_characters++;
                    encodeInfo += "STARTB FNC3 ";
                    break;
                default: /* Start C */
                    dest += code128Table[104]; /* Start B */
                    values[0] = 105;
                    dest += code128Table[96]; /* FNC3 */
                    values[1] = 96;
                    dest += code128Table[99]; /* Code C */
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
                /* Start A */
                dest += code128Table[103];
                values[0] = 103;
                current_set = Mode.LATCHA;
                encodeInfo += "STARTA ";
                break;
            case LATCHB:
                /* Start B */
                dest += code128Table[104];
                values[0] = 104;
                current_set = Mode.LATCHB;
                encodeInfo += "STARTB ";
                break;
            default:
                /* Start C */
                dest += code128Table[105];
                values[0] = 105;
                current_set = Mode.LATCHC;
                encodeInfo += "STARTC ";
                break;
            }
        }
        bar_characters++;

        if (inputDataType == DataType.GS1) {
            dest += code128Table[102];
            values[1] = 102;
            bar_characters++;
            encodeInfo += "FNC1 ";
        }

        if (fset[0] == FMode.LATCHF) {
            switch (current_set) {
            case LATCHA:
                dest += code128Table[101];
                dest += code128Table[101];
                values[bar_characters] = 101;
                values[bar_characters + 1] = 101;
                encodeInfo += "FNC4 FNC4 ";
                break;
            case LATCHB:
                dest += code128Table[100];
                dest += code128Table[100];
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
                    dest += code128Table[101];
                    values[bar_characters] = 101;
                    bar_characters++;
                    current_set = Mode.LATCHA;
                    encodeInfo += "CODEA ";
                    break;
                case LATCHB:
                    dest += code128Table[100];
                    values[bar_characters] = 100;
                    bar_characters++;
                    current_set = Mode.LATCHB;
                    encodeInfo += "CODEB ";
                    break;
                case LATCHC:
                    dest += code128Table[99];
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
                        dest += code128Table[101];
                        dest += code128Table[101];
                        values[bar_characters] = 101;
                        values[bar_characters + 1] = 101;
                        encodeInfo += "FNC4 FNC4 ";
                        break;
                    case LATCHB:
                        dest += code128Table[100];
                        dest += code128Table[100];
                        values[bar_characters] = 100;
                        values[bar_characters + 1] = 100;
                        encodeInfo += "FNC4 FNC4 ";
                        break;
                    }
                    bar_characters += 2;
                    f_state = FMode.LATCHN;
                }
                if ((fset[read] == FMode.LATCHN) && (f_state == FMode.LATCHF)) {
                    /* Latch end of extended mode */
                    switch (current_set) {
                    case LATCHA:
                        dest += code128Table[101];
                        dest += code128Table[101];
                        values[bar_characters] = 101;
                        values[bar_characters + 1] = 101;
                        encodeInfo += "FNC4 FNC4 ";
                        break;
                    case LATCHB:
                        dest += code128Table[100];
                        dest += code128Table[100];
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
                    dest += code128Table[101]; /* FNC 4 */
                    values[bar_characters] = 101;
                    encodeInfo += "FNC4 ";
                    break;
                case LATCHB:
                    dest += code128Table[100]; /* FNC 4 */
                    values[bar_characters] = 100;
                    encodeInfo += "FNC4 ";
                    break;
                }
                bar_characters++;
            }

            if ((set[read] == Mode.SHIFTA) || (set[read] == Mode.SHIFTB)) {
                /* Insert shift character */
                dest += code128Table[98];
                values[bar_characters] = 98;
                encodeInfo += "SHFT ";
                bar_characters++;
            }

            if (!((inputDataType == DataType.GS1) && (inputData[read] == '['))) {
                /* Encode data characters */
                c = inputData[read];
                switch (set[read]) {
                case SHIFTA:
                case LATCHA:
                    if (c > 127) {
                        if (c < 160) {
                            dest += code128Table[(c - 128) + 64];
                            values[bar_characters] = (c - 128) + 64;
                        } else {
                            dest += code128Table[(c - 128) - 32];
                            values[bar_characters] = (c - 128) - 32;
                        }
                    } else {
                        if (c < 32) {
                            dest += code128Table[c + 64];
                            values[bar_characters] = c + 64;
                        } else {
                            dest += code128Table[c - 32];
                            values[bar_characters] = c - 32;
                        }
                    }
                    encodeInfo += Integer.toString(values[bar_characters]) + " ";
                    bar_characters++;
                    read++;
                    break;
                case SHIFTB:
                case LATCHB:
                    if (c > 127) {
                        dest += code128Table[c - 32 - 128];
                        values[bar_characters] = c - 32 - 128;
                    } else {
                        dest += code128Table[c - 32];
                        values[bar_characters] = c - 32;
                    }
                    encodeInfo += Integer.toString(values[bar_characters]) + " ";
                    bar_characters++;
                    read++;
                    break;
                case LATCHC:
                    int weight;
                    int d = inputData[read + 1];

                    weight = (10 * (c - '0')) + (d - '0');
                    dest += code128Table[weight];
                    values[bar_characters] = weight;
                    encodeInfo += Integer.toString(values[bar_characters]) + " ";
                    bar_characters++;
                    read += 2;
                    break;
                }
            } else {
                // FNC1
                dest += code128Table[102];
                values[bar_characters] = 102;
                bar_characters++;
                read++;
                encodeInfo += "FNC1 ";
            }

        } while (read < sourcelen);
        
        encodeInfo += "\n";

        /* "...note that the linkage flag is an extra code set character between
	the last data character and the Symbol Check Character" (GS1 Specification) */

	/* Linkage flags in GS1-128 are determined by ISO/IEC 24723 section 7.4 */

	switch(compositeMode) {
            case CCA:
            case CCB:
                /* CC-A or CC-B 2D component */
                switch(set[sourcelen - 1]) {
                    case LATCHA: linkage_flag = 100; break;
                    case LATCHB: linkage_flag = 99; break;
                    case LATCHC: linkage_flag = 101; break;
                }
                encodeInfo += "Linkage flag: " + linkage_flag + '\n';
                break;
            case CCC:
                /* CC-C 2D component */
                switch(set[sourcelen - 1]) {
                    case LATCHA: linkage_flag = 99; break;
                    case LATCHB: linkage_flag = 101; break;
                    case LATCHC: linkage_flag = 100; break;
                }
                encodeInfo += "Linkage flag: " + linkage_flag + '\n';
                break;
            default:
                break;
	}

	if(linkage_flag != 0) {
            dest += code128Table[linkage_flag];
            values[bar_characters] = linkage_flag;
            bar_characters++;
	}

        /* check digit calculation */
        for (i = 0; i < bar_characters; i++) {
            if (i > 0) {
                values[i] *= i;
            }
            total_sum += values[i];
        }
        dest += code128Table[total_sum % 103];
        encodeInfo += "Data Codewords: " + bar_characters + '\n';
        encodeInfo += "Check Digit: " + (total_sum % 103) + '\n';

        /* Stop character */
        dest += code128Table[106];

        if (!(inputDataType == DataType.GS1)) {
            readable = content;
        }

        if (inputDataType == DataType.HIBC) {
            readable = "*" + content + "*";
        }

        if (compositeMode == Composite.OFF) {
            pattern = new String[1];
            pattern[0] = dest;
            row_count = 1;
            row_height = new int[1];
            row_height[0] = -1;
        } else {
            /* Add the separator pattern for composite symbols */
            pattern = new String[2];
            pattern[0] = "0" + dest;
            pattern[1] = dest;
            row_count = 2;
            row_height = new int[2];
            row_height[0] = 1;
            row_height[1] = -1;
        }
        plotSymbol();
        return true;
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

    private void reduceSubsetChanges() { /* Implements rules from ISO 15417 Annex E */
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

            if (i == 0) { /* first block */
                if ((index_point == 1) && ((length == 2) && (current == Mode.ABORC))) { /* Rule 1a */
                    mode_type[i] = Mode.LATCHC;
                }
                if (current == Mode.ABORC) {
                    if (length >= 4) { /* Rule 1b */
                        mode_type[i] = Mode.LATCHC;
                    } else {
                        mode_type[i] = Mode.AORB;
                        current = Mode.AORB;
                    }
                }
                if (current == Mode.SHIFTA) { /* Rule 1c */
                    mode_type[i] = Mode.LATCHA;
                }
                if ((current == Mode.AORB) && (next == Mode.SHIFTA)) { /* Rule 1c */
                    mode_type[i] = Mode.LATCHA;
                    current = Mode.LATCHA;
                }
                if (current == Mode.AORB) { /* Rule 1d */
                    mode_type[i] = Mode.LATCHB;
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
                if ((current == Mode.SHIFTA) && (last == Mode.LATCHC)) {
                    mode_type[i] = Mode.LATCHA;
                    current = Mode.LATCHA;
                }
                if ((current == Mode.SHIFTB) && (last == Mode.LATCHC)) {
                    mode_type[i] = Mode.LATCHB;
                    current = Mode.LATCHB;
                }
            } /* Rule 2 is implemented elsewhere, Rule 6 is implied */
        }

        combineSubsetBlocks();

    }

    private void combineSubsetBlocks() {
        int i, j;

        /* bring together same type blocks */
        if (index_point > 1) {
            i = 1;
            while (i < index_point) {
                if (mode_type[i - 1] == mode_type[i]) {
                    /* bring together */
                    mode_length[i - 1] = mode_length[i - 1] + mode_length[i];
                    j = i + 1;

                    /* decreace the list */
                    while (j < index_point) {
                        mode_length[j - 1] = mode_length[j];
                        mode_type[j - 1] = mode_type[j];
                        j++;
                    }
                    index_point--;
                    i--;
                }
                i++;
            }
        }
    }
}
