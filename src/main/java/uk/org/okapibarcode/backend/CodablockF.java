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

import java.awt.geom.Rectangle2D;
import java.io.UnsupportedEncodingException;

/**
 * Implements Codablock-F according to AIM Europe "Uniform Symbology
 * Specification - Codablock F", 1995
 * <p>
 * Codablock F is a multi-row symbology using Code 128 encoding. It can
 * encode any 8-bit ISO 8859-1 (Latin-1) data up to approximately 1000
 * alpha-numeric characters or 2000 numeric digits in length.
 *
 * @author <a href="mailto:rstuart114@gmail.com">Robin Stuart</a>
 */
public class CodablockF extends Symbol {

    private enum Mode {
        SHIFTA, LATCHA, SHIFTB, LATCHB, SHIFTC, LATCHC, AORB, ABORC, CANDB, CANDBB
    }

    private enum cfMode {
        MODEA, MODEB, MODEC
    }

    /* Annex A Table A.1 */
    private String[] C128Table = {"212222", "222122", "222221", "121223", "121322", "131222", "122213",
        "122312", "132212", "221213", "221312", "231212", "112232", "122132", "122231", "113222",
        "123122", "123221", "223211", "221132", "221231", "213212", "223112", "312131", "311222",
        "321122", "321221", "312212", "322112", "322211", "212123", "212321", "232121", "111323",
        "131123", "131321", "112313", "132113", "132311", "211313", "231113", "231311", "112133",
        "112331", "132131", "113123", "113321", "133121", "313121", "211331", "231131", "213113",
        "213311", "213131", "311123", "311321", "331121", "312113", "312311", "332111", "314111",
        "221411", "431111", "111224", "111422", "121124", "121421", "141122", "141221", "112214",
        "112412", "122114", "122411", "142112", "142211", "241211", "221114", "413111", "241112",
        "134111", "111242", "121142", "121241", "114212", "124112", "124211", "411212", "421112",
        "421211", "212141", "214121", "412121", "111143", "111341", "131141", "114113", "114311",
        "411113", "411311", "113141", "114131", "311141", "411131", "211412", "211214", "211232",
        "2331112"};

    private int[][] blockmatrix = new int[44][62];
    private int columns_needed;
    private int[] source;
    private int rows_needed;
    private cfMode final_mode;
    private cfMode[] subset_selector = new cfMode[44];

    @Override
    public boolean encode() {
        int input_length, i, j, k, h;
        int min_module_height;
        Mode last_mode, this_mode;
        double estimate_codelength;
        String row_pattern;
        int[] row_indicator = new int[44];
        int[] row_check = new int[44];
        int k1_sum, k2_sum;
        int k1_check, k2_check;

        input_length = content.length();
        final_mode = cfMode.MODEA;

        if (input_length > 5450) {
            error_msg = "Input data too long";
            return false;
        }

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

        source = new int[input_length + 1];
        for (i = 0; i < input_length; i++) {
            source[i] = inputBytes[i] & 0xFF;
        }
        source[input_length] = 0x00;

        /* Make a guess at how many characters will be needed to encode the data */
        estimate_codelength = 0.0;
        last_mode = Mode.AORB; /* Codablock always starts with Code A */
        for (i = 0; i < input_length; i++) {
            this_mode = findSubset(source[i]);
            if (this_mode != last_mode) {
                estimate_codelength += 1.0;
            }
            if (this_mode != Mode.ABORC) {
                estimate_codelength += 1.0;
            } else {
                estimate_codelength += 0.5;
            }
            if (source[i] > 127) {
                estimate_codelength += 1.0;
            }
            last_mode = this_mode;
        }

        /* Decide symbol size based on the above guess */
        rows_needed = (int) (0.5 + Math.sqrt((estimate_codelength + 2) / 1.45));
        if (rows_needed < 2) {
            rows_needed = 2;
        }
        if (rows_needed > 44) {
            rows_needed = 44;
        }
        columns_needed = (int) (estimate_codelength + 2) / rows_needed;
        if (columns_needed < 4) {
            columns_needed = 4;
        }
        if (columns_needed > 62) {
            error_msg = "Input data too long";
            return false;
        }

        /* Encode the data */
        if (!(data_encode_blockf())) {
            return false;
        }

	/* Add check digits - Annex F */
	k1_sum = 0;
	k2_sum = 0;
	for(i = 0; i < input_length; i++) {
		if((inputDataType == DataType.GS1) && source[i] == '[') {
			k1_sum += (i + 1) * 29; /* GS */
			k2_sum += i * 29;
		} else {
			k1_sum += (i + 1) * source[i];
			k2_sum += i * source[i];
		}
	}
	k1_check = k1_sum % 86;
	k2_check = k2_sum % 86;
	if((final_mode == cfMode.MODEA) || (final_mode == cfMode.MODEB)) {
		k1_check = k1_check + 64;
		if(k1_check > 95) { k1_check -= 96; }
		k2_check = k2_check + 64;
		if(k2_check > 95) { k2_check -= 96; }
	}
	blockmatrix[rows_needed - 1][columns_needed - 2] = k1_check;
	blockmatrix[rows_needed - 1][columns_needed - 1] = k2_check;

	/* Calculate row height (4.6.1.a) */
	min_module_height = (int) (0.55 * (columns_needed + 3)) + 3;
	if(min_module_height < 8) { min_module_height = 8; }

	/* Encode the Row Indicator in the First Row of the Symbol - Table D2 */
	if(subset_selector[0] == cfMode.MODEC) {
		/* Code C */
		row_indicator[0] = rows_needed - 2;
	} else {
		/* Code A or B */
		row_indicator[0] = rows_needed + 62;

		if(row_indicator[0] > 95) {
			row_indicator[0] -= 95;
		}
	}

	/* Encode the Row Indicator in the Second and Subsequent Rows of the Symbol - Table D3 */
	for(i = 1; i < rows_needed; i++) {
		/* Note that the second row is row number 1 because counting starts from 0 */
		if(subset_selector[i] == cfMode.MODEC) {
			/* Code C */
			row_indicator[i] = i + 42;
		} else {
			/* Code A or B */
			if( i < 6 )
				row_indicator[i] = i + 10;
			else
				row_indicator[i] = i + 20;
		}
	}

	/* Calculate row check digits - Annex E */
	for(i = 0; i < rows_needed; i++) {
		k = 103;
                switch (subset_selector[i]) {
                    case MODEA: k += 98;
                        break;
                    case MODEB: k += 100;
                        break;
                    case MODEC: k += 99;
                        break;
                }
		k += 2 * row_indicator[i];
		for(j = 0; j < columns_needed; j++) {
			k+= (j + 3) * blockmatrix[i][j];
		}
		row_check[i] = k % 103;
	}

        readable = "";
        row_count = rows_needed;
        pattern = new String[row_count];
        row_height = new int[row_count];

        encodeInfo += "Grid size: " + columns_needed + " X " + rows_needed + '\n';
        encodeInfo += "K1 Check Digit: " + k1_check + "\n";
        encodeInfo += "K2 Check Digit: " + k2_check + "\n";

	/* Resolve the data into patterns and place in symbol structure */
        encodeInfo += "Encoding: ";
	for(i = 0; i < rows_needed; i++) {

            row_pattern = "";
            /* Start character */
            row_pattern += C128Table[103]; /* Always Start A */

            switch (subset_selector[i]) {
                case MODEA:
                    row_pattern += C128Table[98];
                    encodeInfo += "MODEA ";
                    break;
                case MODEB:
                    row_pattern += C128Table[100];
                    encodeInfo += "MODEB ";
                    break;
                case MODEC:
                    row_pattern += C128Table[99];
                    encodeInfo += "MODEC ";
                    break;
            }
            row_pattern += C128Table[row_indicator[i]];
            encodeInfo += Integer.toString(row_indicator[i]) + " ";

            for(j = 0; j < columns_needed; j++) {
                    row_pattern += C128Table[blockmatrix[i][j]];
                    encodeInfo += Integer.toString(blockmatrix[i][j]) + " ";
            }

            row_pattern += C128Table[row_check[i]];
            encodeInfo += "(" + Integer.toString(row_check[i]) + ") ";

            /* Stop character */
            row_pattern += C128Table[106];

            /* Write the information into the symbol */
            pattern[i] = row_pattern;
            row_height[i] = 15;
	}
        encodeInfo += "\n";

        symbol_height = rows_needed * 15;
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

    private boolean data_encode_blockf() {
        int i, j, input_position, current_row;
        int column_position, c;
        cfMode current_mode;
        boolean done, exit_status;

        exit_status = false;
        current_row = 0;
        current_mode = cfMode.MODEA;
        column_position = 0;
        input_position = 0;
        c = 0;

        do {
            done = false;
            /* 'done' ensures that the instructions are followed in the correct order for each input character */

            if (column_position == 0) {
                /* The Beginning of a row */
                c = columns_needed;
                current_mode = character_subset_select(input_position);
                subset_selector[current_row] = current_mode;
                if ((current_row == 0) && (inputDataType == DataType.GS1)) {
                    /* Section 4.4.7.1 */
                    blockmatrix[current_row][column_position] = 102; /* FNC1 */
                    column_position++;
                    c--;
                }
            }

            if ((inputDataType == DataType.GS1) && (source[input_position] == '[')) {
                blockmatrix[current_row][column_position] = 102; /* FNC1 */
                column_position++;
                c--;
                input_position++;
                done = true;
            }

            if (!done) {
                if (c <= 2) {
                    /* Annex B section 1 rule 1 */
                    /* Ensure that there is sufficient encodation capacity to continue (using the rules of Annex B.2). */
                    switch (current_mode) {
                        case MODEA: /* Table B1 applies */
                            if (findSubset(source[input_position]) == Mode.ABORC) {
                                blockmatrix[current_row][column_position] = a3_convert(source[input_position]);
                                column_position++;
                                c--;
                                input_position++;
                                done = true;
                            }

                            if ((findSubset(source[input_position]) == Mode.SHIFTB) && (c == 1)) {
                                /* Needs two symbols */
                                blockmatrix[current_row][column_position] = 100; /* Code B */
                                column_position++;
                                c--;
                                done = true;
                            }

                            if ((source[input_position] >= 244) && (!done)) {
                                /* Needs three symbols */
                                blockmatrix[current_row][column_position] = 100; /* Code B */
                                column_position++;
                                c--;
                                if (c == 1) {
                                    blockmatrix[current_row][column_position] = 101; /* Code A */
                                    column_position++;
                                    c--;
                                }
                                done = true;
                            }

                            if ((source[input_position] >= 128) && (!done) && c == 1) {
                                /* Needs two symbols */
                                blockmatrix[current_row][column_position] = 100; /* Code B */
                                column_position++;
                                c--;
                                done = true;
                            }
                            break;
                        case MODEB: /* Table B2 applies */
                            if (findSubset(source[input_position]) == Mode.ABORC) {
                                blockmatrix[current_row][column_position] = a3_convert(source[input_position]);
                                column_position++;
                                c--;
                                input_position++;
                                done = true;
                            }

                            if ((findSubset(source[input_position]) == Mode.SHIFTA) && (c == 1)) {
                                /* Needs two symbols */
                                blockmatrix[current_row][column_position] = 101; /* Code A */
                                column_position++;
                                c--;
                                done = true;
                            }

                            if (((source[input_position] >= 128)
                                    && (source[input_position] <= 159)) && (!done)) {
                                /* Needs three symbols */
                                blockmatrix[current_row][column_position] = 101; /* Code A */
                                column_position++;
                                c--;
                                if (c == 1) {
                                    blockmatrix[current_row][column_position] = 100; /* Code B */
                                    column_position++;
                                    c--;
                                }
                                done = true;
                            }

                            if ((source[input_position] >= 160) && (!done) && c == 1) {
                                /* Needs two symbols */
                                blockmatrix[current_row][column_position] = 101; /* Code A */
                                column_position++;
                                c--;
                                done = true;
                            }
                            break;
                        case MODEC: /* Table B3 applies */
                            if ((findSubset(source[input_position]) != Mode.ABORC) && (c == 1)) {
                                /* Needs two symbols */
                                blockmatrix[current_row][column_position] = 101; /* Code A */
                                column_position++;
                                c--;
                                done = true;
                            }

                            if (((findSubset(source[input_position]) == Mode.ABORC)
                                    && (findSubset(source[input_position + 1]) != Mode.ABORC))
                                    && (c == 1)) {
                                /* Needs two symbols */
                                blockmatrix[current_row][column_position] = 101; /* Code A */
                                column_position++;
                                c--;
                                done = true;
                            }

                            if (source[input_position] >= 128) {
                                /* Needs three symbols */
                                blockmatrix[current_row][column_position] = 101; /* Code A */
                                column_position++;
                                c--;
                                if (c == 1) {
                                    blockmatrix[current_row][column_position] = 100; /* Code B */
                                    column_position++;
                                    c--;
                                }
                            }
                            break;
                    }
                }
            }

            if (!done) {
                if (((findSubset(source[input_position]) == Mode.AORB)
                        || (findSubset(source[input_position]) == Mode.SHIFTA))
                        && (current_mode == cfMode.MODEA)) {
                    /* Annex B section 1 rule 2 */
                    /* If in Code Subset A and the next data character can be encoded in Subset A encode the next
                     character. */
                    if (source[input_position] >= 128) {
                        /* Extended ASCII character */
                        blockmatrix[current_row][column_position] = 101; /* FNC4 */
                        column_position++;
                        c--;
                    }
                    blockmatrix[current_row][column_position] = a3_convert(source[input_position]);
                    column_position++;
                    c--;
                    input_position++;
                    done = true;
                }
            }

            if (!done) {
                if (((findSubset(source[input_position]) == Mode.AORB)
                        || (findSubset(source[input_position]) == Mode.SHIFTB))
                        && (current_mode == cfMode.MODEB)) {
                    /* Annex B section 1 rule 3 */
                    /* If in Code Subset B and the next data character can be encoded in subset B, encode the next
                     character. */
                    if (source[input_position] >= 128) {
                        /* Extended ASCII character */
                        blockmatrix[current_row][column_position] = 100; /* FNC4 */
                        column_position++;
                        c--;
                    }
                    blockmatrix[current_row][column_position] = a3_convert(source[input_position]);
                    column_position++;
                    c--;
                    input_position++;
                    done = true;
                }
            }

            if (!done) {
                if (((findSubset(source[input_position]) == Mode.ABORC)
                        && (findSubset(source[input_position + 1]) == Mode.ABORC))
                        && (current_mode == cfMode.MODEC)) {
                    /* Annex B section 1 rule 4 */
                    /* If in Code Subset C and the next data are 2 digits, encode them. */
                    blockmatrix[current_row][column_position]
                            = ((source[input_position] - '0') * 10)
                            + (source[input_position + 1] - '0');
                    column_position++;
                    c--;
                    input_position += 2;
                    done = true;
                }
            }

            if (!done) {
                if (((current_mode == cfMode.MODEA) || (current_mode == cfMode.MODEB))
                        && ((findSubset(source[input_position]) == Mode.ABORC)
                        || ((inputDataType == DataType.GS1) && (source[input_position] == '[')))) {
                    /* Count the number of numeric digits */
                    /*  If 4 or more numeric data characters occur together when in subsets A or B:
                     a.      If there is an even number of numeric data characters, insert a Code C character before the
                     first numeric digit to change to subset C.
                     b.      If there is an odd number of numeric data characters, insert a Code Set C character immedi-
                     ately after the first numeric digit to change to subset C. */
                    i = 0;
                    j = 0;
                    do {
                        i++;
                        if ((inputDataType == DataType.GS1) && (source[input_position + j] == '[')) {
                            i++;
                        }
                        j++;
                    } while ((findSubset(source[input_position + j]) == Mode.ABORC)
                            || ((inputDataType == DataType.GS1) && (source[input_position + j] == '[')));
                    i--;

                    if (i >= 4) {
                        /* Annex B section 1 rule 5 */
                        if ((i % 2) == 1) {
                            /* Annex B section 1 rule 5a */
                            blockmatrix[current_row][column_position] = 99; /* Code C */
                            column_position++;
                            c--;
                            blockmatrix[current_row][column_position] = ((source[input_position] - '0') * 10)
                                    + (source[input_position + 1] - '0');
                            column_position++;
                            c--;
                            input_position += 2;
                            current_mode = cfMode.MODEC;
                        } else {
                            /* Annex B section 1 rule 5b */
                            blockmatrix[current_row][column_position] = a3_convert(source[input_position]);
                            column_position++;
                            c--;
                            input_position++;
                        }
                        done = true;
                    } else {
                        blockmatrix[current_row][column_position] = a3_convert(source[input_position]);
                        column_position++;
                        c--;
                        input_position++;
                        done = true;
                    }
                }
            }

            if (!done) {
                if ((current_mode == cfMode.MODEB) && (findSubset(source[input_position]) == Mode.SHIFTA)) {
                    /* Annex B section 1 rule 6 */
                    /*  When in subset B and an ASCII control character occurs in the data:
                     a.   If there is a lower case character immediately following the control character, insert a Shift
                     character before the control character.
                     b.   Otherwise, insert a Code A character before the control character to change to subset A. */
                    if ((source[input_position + 1] >= 96) && (source[input_position + 1] <= 127)) {
                        /* Annex B section 1 rule 6a */
                        blockmatrix[current_row][column_position] = 98; /* Shift */
                        column_position++;
                        c--;
                        if (source[input_position] >= 128) {
                            /* Extended ASCII character */
                            blockmatrix[current_row][column_position] = 100; /* FNC4 */
                            column_position++;
                            c--;
                        }
                        blockmatrix[current_row][column_position] = a3_convert(source[input_position]);
                        column_position++;
                        c--;
                        input_position++;
                    } else {
                        /* Annex B section 1 rule 6b */
                        blockmatrix[current_row][column_position] = 101; /* Code A */
                        column_position++;
                        c--;
                        if (source[input_position] >= 128) {
                            /* Extended ASCII character */
                            blockmatrix[current_row][column_position] = 100; /* FNC4 */
                            column_position++;
                            c--;
                        }
                        blockmatrix[current_row][column_position] = a3_convert(source[input_position]);
                        column_position++;
                        c--;
                        input_position++;
                        current_mode = cfMode.MODEA;
                    }
                    done = true;
                }
            }

            if (!done) {
                if ((current_mode == cfMode.MODEA) && (findSubset(source[input_position]) == Mode.SHIFTB)) {
                    /* Annex B section 1 rule 7 */
                    /* When in subset A and a lower case character occurs in the data:
                     a.   If following that character, a control character occurs in the data before the occurrence of
                     another lower case character, insert a Shift character before the lower case character.
                     b.   Otherwise, insert a Code B character before the lower case character to change to subset B. */
                    if ((findSubset(source[input_position + 1]) == Mode.SHIFTA)
                            && (findSubset(source[input_position + 2]) == Mode.SHIFTB)) {
                        /* Annex B section 1 rule 7a */
                        blockmatrix[current_row][column_position] = 98; /* Shift */
                        column_position++;
                        c--;
                        if (source[input_position] >= 128) {
                            /* Extended ASCII character */
                            blockmatrix[current_row][column_position] = 101; /* FNC4 */
                            column_position++;
                            c--;
                        }
                        blockmatrix[current_row][column_position] = a3_convert(source[input_position]);
                        column_position++;
                        c--;
                        input_position++;
                    } else {
                        /* Annex B section 1 rule 7b */
                        blockmatrix[current_row][column_position] = 100; /* Code B */
                        column_position++;
                        c--;
                        if (source[input_position] >= 128) {
                            /* Extended ASCII character */
                            blockmatrix[current_row][column_position] = 101; /* FNC4 */
                            column_position++;
                            c--;
                        }
                        blockmatrix[current_row][column_position] = a3_convert(source[input_position]);
                        column_position++;
                        c--;
                        input_position++;
                        current_mode = cfMode.MODEB;
                    }
                    done = true;
                }
            }

            if (!done) {
                if ((current_mode == cfMode.MODEC) && ((findSubset(source[input_position]) != Mode.ABORC)
                        || (findSubset(source[input_position + 1]) != Mode.ABORC))) {
                    /* Annex B section 1 rule 8 */
                    /*  When in subset C and a non-numeric character (or a single digit) occurs in the data, insert a Code
                     A or Code B character before that character, following rules 8a and 8b to determine between code
                     subsets A and B.
                     a.    If an ASCII control character (eg NUL) occurs in the data before any lower case character, use
                     Code A.
                     b.    Otherwise use Code B. */
                    if (findSubset(source[input_position]) == Mode.SHIFTA) {
                        /* Annex B section 1 rule 8a */
                        blockmatrix[current_row][column_position] = 101; /* Code A */
                        column_position++;
                        c--;
                        if (source[input_position] >= 128) {
                            /* Extended ASCII character */
                            blockmatrix[current_row][column_position] = 101; /* FNC4 */
                            column_position++;
                            c--;
                        }
                        blockmatrix[current_row][column_position] = a3_convert(source[input_position]);
                        column_position++;
                        c--;
                        input_position++;
                        current_mode = cfMode.MODEA;
                    } else {
                        /* Annex B section 1 rule 8b */
                        blockmatrix[current_row][column_position] = 100; /* Code B */
                        column_position++;
                        c--;
                        if (source[input_position] >= 128) {
                            /* Extended ASCII character */
                            blockmatrix[current_row][column_position] = 100; /* FNC4 */
                            column_position++;
                            c--;
                        }
                        blockmatrix[current_row][column_position] = a3_convert(source[input_position]);
                        column_position++;
                        c--;
                        input_position++;
                        current_mode = cfMode.MODEB;
                    }
                    done = true;
                }
            }

            if (input_position == content.length()) {
                /* End of data - Annex B rule 5a */
                if (c == 1) {
                    if (current_mode == cfMode.MODEA) {
                        blockmatrix[current_row][column_position] = 100; /* Code B */
                        current_mode = cfMode.MODEB;
                    } else {
                        blockmatrix[current_row][column_position] = 101; /* Code A */
                        current_mode = cfMode.MODEA;
                    }
                    column_position++;
                    c--;
                }

                if (c == 0) {
                    /* Another row is needed */
                    column_position = 0;
                    c = columns_needed;
                    current_row++;
                    subset_selector[current_row] = cfMode.MODEA;
                    current_mode = cfMode.MODEA;
                }

                if (c > 2) {
                    /* Fill up the last row */
                    do {
                        if (current_mode == cfMode.MODEA) {
                            blockmatrix[current_row][column_position] = 100; /* Code B */
                            current_mode = cfMode.MODEB;
                        } else {
                            blockmatrix[current_row][column_position] = 101; /* Code A */
                            current_mode = cfMode.MODEA;
                        }
                        column_position++;
                        c--;
                    } while (c > 2);
                }

                /* If (c == 2) { do nothing } */

                exit_status = true;
                 final_mode = current_mode;
            } else {
                if (c <= 0) {
                    /* Start new row - Annex B rule 5b */
                    column_position = 0;
                    current_row++;
                    if (current_row > 43) {
                        return false;
                    }
                }
            }

        } while (!exit_status);

	if(current_row == 0) {
		/* fill up the first row */
		for(c = column_position; c <= columns_needed; c++) {
			if(current_mode == cfMode.MODEA) {
				blockmatrix[current_row][c] = 100; /* Code B */
				current_mode = cfMode.MODEB;
			} else {
				blockmatrix[current_row][c] = 101; /* Code A */
				current_mode = cfMode.MODEA;
			}
		}
		current_row++;
		/* add a second row */
		subset_selector[current_row] = cfMode.MODEA;
		current_mode = cfMode.MODEA;
		for(c = 0; c <= columns_needed - 2; c++) {
			if(current_mode == cfMode.MODEA) {
				blockmatrix[current_row][c] = 100; /* Code B */
				current_mode = cfMode.MODEB;
			} else {
				blockmatrix[current_row][c] = 101; /* Code A */
				current_mode = cfMode.MODEA;
			}
		}
	}
        rows_needed = current_row + 1;

        return true;
    }

    private cfMode character_subset_select(int input_position) {
        /* Section 4.5.2 - Determining the Character Subset Selector in a Row */

	if((source[input_position] >= '0') && (source[input_position + 1] <= '9')) {
		/* Rule 1 */
		return cfMode.MODEC;
	}

	if((source[input_position] >= 128) && (source[input_position] <= 160)) {
		/* Rule 2 (i) */
		return cfMode.MODEA;
	}

	if((source[input_position] >= 0) && (source[input_position] <= 31)) {
		/* Rule 3 */
		return cfMode.MODEA;
	}

	/* Rule 4 */
	return cfMode.MODEB;
    }

    private int a3_convert(int source) {
        /* Annex A section 3 */
	if(source < 32) { return source + 64; }
	if((source >= 32) && (source <= 127)) { return source - 32; }
	if((source >= 128) && (source <= 159)) { return (source - 128) + 64; }
	/* if source >= 160 */
	return (source - 128) - 32;
    }

    @Override
    protected void plotSymbol() {
        int xBlock, yBlock;
        int x, y, w, h;
        boolean black;

        rectangles.clear();
        y = 1;
        h = 1;
        for (yBlock = 0; yBlock < row_count; yBlock++) {
            black = true;
            x = 0;
            for (xBlock = 0; xBlock < pattern[yBlock].length(); xBlock++) {
                if (black == true) {
                    black = false;
                    w = pattern[yBlock].charAt(xBlock) - '0';
                    if (row_height[yBlock] == -1) {
                        h = default_height;
                    } else {
                        h = row_height[yBlock];
                    }
                    if (w != 0 && h != 0) {
                        Rectangle2D.Double rect = new Rectangle2D.Double(x, y, w, h);
                        rectangles.add(rect);
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
                Rectangle2D.Double rect = new Rectangle2D.Double(11, y - 1, (symbol_width - 24), 2);
                rectangles.add(rect);
            }
        }

        /* Add top and bottom binding bars */
        Rectangle2D.Double top = new Rectangle2D.Double(0, 0, symbol_width, 2);
        rectangles.add(top);
        Rectangle2D.Double bottom = new Rectangle2D.Double(0, y - 1, symbol_width, 2);
        rectangles.add(bottom);
        symbol_height += 2;

        mergeVerticalBlocks();
    }
}
