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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import uk.org.okapibarcode.graphics.Rectangle;

/**
 * <p>Implements Codablock F according to AIM Europe "Uniform Symbology Specification - Codablock F", 1995.
 *
 * <p>Codablock-F is a multi-row symbology using Code 128 encoding. It can encode any 8-bit ISO 8859-1 (Latin-1)
 * data up to approximately 1000 alpha-numeric characters or 2000 numeric digits in length.
 *
 * @author <a href="mailto:rstuart114@gmail.com">Robin Stuart</a>
 * @author Daniel Gredler
 */
public class CodablockF extends Symbol {

    private enum Mode {
        SHIFTA, LATCHA, SHIFTB, LATCHB, SHIFTC, LATCHC, AORB, ABORC, CANDB, CANDBB
    }

    private enum CfMode {
        MODEA, MODEB, MODEC
    }

    /* Annex A Table A.1 */
    private static final String[] C_128_TABLE = {
        "212222", "222122", "222221", "121223", "121322", "131222", "122213",
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
    private int columns;
    private int rows;
    private CfMode finalMode;
    private CfMode[] subsets = new CfMode[44];

    /**
     * Creates a new instance.
     */
    public CodablockF() {
        this.humanReadableLocation = HumanReadableLocation.NONE;
        this.defaultHeight = 15;
    }

    /**
     * Codablock F is not currently listed in the GS1 specification as a GS1-supporting symbology. However,
     * it did support EAN/UCC application identifiers before EAN and UCC became GS1, so it did historically
     * support what eventually became the GS1 data format.
     */
    @Override
    public boolean supportsGs1() {
        return true;
    }

    @Override
    protected void encode() {

        int input_length, i, j, k;
        int min_module_height;
        Mode last_mode, this_mode;
        double estimate_codelength;
        String row_pattern;
        int[] row_indicator = new int[44];
        int[] row_check = new int[44];

        finalMode = CfMode.MODEA;

        if (!content.matches("[\u0000-\u00FF]*")) {
            throw OkapiInputException.invalidCharactersInInput();
        }

        inputData = toBytes(content, StandardCharsets.ISO_8859_1, 0x00);
        input_length = inputData.length - 1;

        if (input_length > 5450) {
            throw OkapiInputException.inputTooLong();
        }

        // guess how many characters will be needed to encode the data,
        // we need two characters right off the bat for the check digits
        estimate_codelength = 2.0;
        last_mode = Mode.AORB; // Codablock always starts with Code A
        for (i = 0; i < input_length; i++) {
            this_mode = findSubset(inputData[i]);
            if (this_mode != last_mode) {
                estimate_codelength += 1.0;
            }
            if (this_mode != Mode.ABORC) {
                estimate_codelength += 1.0;
            } else {
                estimate_codelength += 0.5;
            }
            if (inputData[i] > 127) {
                estimate_codelength += 1.0;
            }
            last_mode = this_mode;
        }

        /* Decide symbol size based on the above guess */
        rows = (int) (0.5 + Math.sqrt(estimate_codelength / 1.45));
        if (rows < 2) {
            rows = 2;
        }
        if (rows > 44) {
            rows = 44;
        }
        columns = (int) estimate_codelength / rows;
        if (columns < 4) {
            columns = 4;
        }
        if (columns > 62) {
            throw OkapiInputException.inputTooLong();
        }

        /* Encode the data */
        data_encode_blockf();

        /* Add check digits - Annex F */
        int k1 = 0;
        int k2 = 0;
        for (i = 0; i < input_length; i++) {
            if (inputData[i] == FNC1) {
                k1 += (i + 1) * 29; /* GS */
                k2 += i * 29;
            } else {
                k1 += (i + 1) * inputData[i];
                k2 += i * inputData[i];
            }
        }
        k1 = k1 % 86;
        k2 = k2 % 86;
        k1 = toSymbolValue(k1, finalMode);
        k2 = toSymbolValue(k2, finalMode);
        blockmatrix[rows - 1][columns - 2] = k1;
        blockmatrix[rows - 1][columns - 1] = k2;

        /* Calculate row height (4.6.1.a) */
        min_module_height = (int) (0.55 * (columns + 3)) + 3;
        if (min_module_height < 8) {
            min_module_height = 8;
        }

        /* Encode the Row Indicator in the First Row of the Symbol - Table D2 */
        if (subsets[0] == CfMode.MODEC) {
            /* Code C */
            row_indicator[0] = rows - 2;
        } else {
            /* Code A or B */
            row_indicator[0] = rows + 62;
            if (row_indicator[0] > 95) {
                row_indicator[0] -= 95;
            }
        }

        /* Encode the Row Indicator in the Second and Subsequent Rows of the Symbol - Table D3 */
        for (i = 1; i < rows; i++) {
            /* Note that the second row is row number 1 because counting starts from 0 */
            if (subsets[i] == CfMode.MODEC) {
                /* Code C */
                row_indicator[i] = i + 42;
            } else {
                /* Code A or B */
                if (i < 6) {
                    row_indicator[i] = i + 10;
                } else {
                    row_indicator[i] = i + 20;
                }
            }
        }

        /* Calculate row check digits - Annex E */
        for (i = 0; i < rows; i++) {
            k = 103;
            switch (subsets[i]) {
                case MODEA:
                    k += 98;
                    break;
                case MODEB:
                    k += 100;
                    break;
                case MODEC:
                    k += 99;
                    break;
            }
            k += 2 * row_indicator[i];
            for (j = 0; j < columns; j++) {
                k += (j + 3) * blockmatrix[i][j];
            }
            row_check[i] = k % 103;
        }

        readable = "";
        rowCount = rows;
        pattern = new String[rowCount];
        rowHeight = new int[rowCount];

        infoLine("Grid Size: " + columns + " X " + rows);
        infoLine("K1 Check Digit: ", k1);
        infoLine("K2 Check Digit: ", k2);

        /* Resolve the data into patterns and place in symbol structure */
        info("Encoding: ");
        for (i = 0; i < rows; i++) {

            row_pattern = "";
            /* Start character */
            row_pattern += C_128_TABLE[103]; /* Always Start A */

            switch (subsets[i]) {
                case MODEA:
                    row_pattern += C_128_TABLE[98];
                    info("MODEA ");
                    break;
                case MODEB:
                    row_pattern += C_128_TABLE[100];
                    info("MODEB ");
                    break;
                case MODEC:
                    row_pattern += C_128_TABLE[99];
                    info("MODEC ");
                    break;
            }
            row_pattern += C_128_TABLE[row_indicator[i]];
            infoSpace(row_indicator[i]);

            for (j = 0; j < columns; j++) {
                row_pattern += C_128_TABLE[blockmatrix[i][j]];
                infoSpace(blockmatrix[i][j]);
            }

            row_pattern += C_128_TABLE[row_check[i]];
            info("(" + row_check[i] + ") ");

            /* Stop character */
            row_pattern += C_128_TABLE[106];

            /* Write the information into the symbol */
            pattern[i] = row_pattern;
            rowHeight[i] = defaultHeight;
        }
        infoLine();
    }

    private static Mode findSubset(int letter) {
        Mode mode;
        if (letter == FNC1) {
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
        return mode;
    }

    /** Converts a data check character value to a symbol character value, per Table F.1. */
    private static int toSymbolValue(int k, CfMode mode) {
        if (mode == CfMode.MODEC) {
            return k;
        }
        if (k < 32) {
            return k + 64;
        }
        if (k < 48) {
            return k - 32;
        }
        return k - 22;
    }

    private void data_encode_blockf() {

        int i, j, input_position, current_row;
        int column_position, c;
        CfMode current_mode;
        boolean done, exit_status;
        int input_length = inputData.length - 1;

        exit_status = false;
        current_row = 0;
        current_mode = CfMode.MODEA;
        column_position = 0;
        input_position = 0;
        c = 0;

        do {
            done = false;
            /* 'done' ensures that the instructions are followed in the correct order for each input character */

            if (column_position == 0) {
                /* The Beginning of a row */
                c = columns;
                current_mode = character_subset_select(input_position);
                subsets[current_row] = current_mode;
                if ((current_row == 0) && (inputDataType == DataType.GS1)) {
                    /* Section 4.4.7.1 */
                    blockmatrix[current_row][column_position] = 102; /* FNC1 */
                    column_position++;
                    c--;
                }
            }

            if (inputData[input_position] == FNC1) {
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
                            if (findSubset(inputData[input_position]) == Mode.ABORC) {
                                blockmatrix[current_row][column_position] = a3_convert(inputData[input_position]);
                                column_position++;
                                c--;
                                input_position++;
                                done = true;
                            }
                            if ((findSubset(inputData[input_position]) == Mode.SHIFTB) && (c == 1)) {
                                /* Needs two symbols */
                                blockmatrix[current_row][column_position] = 100; /* Code B */
                                column_position++;
                                c--;
                                done = true;
                            }
                            if ((inputData[input_position] >= 244) && (!done)) {
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
                            if ((inputData[input_position] >= 128) && (!done) && c == 1) {
                                /* Needs two symbols */
                                blockmatrix[current_row][column_position] = 100; /* Code B */
                                column_position++;
                                c--;
                                done = true;
                            }
                            break;
                        case MODEB: /* Table B2 applies */
                            if (findSubset(inputData[input_position]) == Mode.ABORC) {
                                blockmatrix[current_row][column_position] = a3_convert(inputData[input_position]);
                                column_position++;
                                c--;
                                input_position++;
                                done = true;
                            }
                            if ((findSubset(inputData[input_position]) == Mode.SHIFTA) && (c == 1)) {
                                /* Needs two symbols */
                                blockmatrix[current_row][column_position] = 101; /* Code A */
                                column_position++;
                                c--;
                                done = true;
                            }
                            if (((inputData[input_position] >= 128)
                                    && (inputData[input_position] <= 159)) && (!done)) {
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
                            if ((inputData[input_position] >= 160) && (!done) && c == 1) {
                                /* Needs two symbols */
                                blockmatrix[current_row][column_position] = 101; /* Code A */
                                column_position++;
                                c--;
                                done = true;
                            }
                            break;
                        case MODEC: /* Table B3 applies */
                            if ((findSubset(inputData[input_position]) != Mode.ABORC) && (c == 1)) {
                                /* Needs two symbols */
                                blockmatrix[current_row][column_position] = 101; /* Code A */
                                column_position++;
                                c--;
                                done = true;
                            }
                            if (((findSubset(inputData[input_position]) == Mode.ABORC)
                                    && (findSubset(inputData[input_position + 1]) != Mode.ABORC))
                                    && (c == 1)) {
                                /* Needs two symbols */
                                blockmatrix[current_row][column_position] = 101; /* Code A */
                                column_position++;
                                c--;
                                done = true;
                            }
                            if (inputData[input_position] >= 128) {
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
                            break;
                    }
                }
            }

            if (!done) {
                if (((findSubset(inputData[input_position]) == Mode.AORB)
                        || (findSubset(inputData[input_position]) == Mode.SHIFTA))
                        && (current_mode == CfMode.MODEA)) {
                    /* Annex B section 1 rule 2 */
                    /* If in Code Subset A and the next data character can be encoded in Subset A encode the next
                     character. */
                    if (inputData[input_position] >= 128) {
                        /* Extended ASCII character */
                        blockmatrix[current_row][column_position] = 101; /* FNC4 */
                        column_position++;
                        c--;
                    }
                    blockmatrix[current_row][column_position] = a3_convert(inputData[input_position]);
                    column_position++;
                    c--;
                    input_position++;
                    done = true;
                }
            }

            if (!done) {
                if (((findSubset(inputData[input_position]) == Mode.AORB)
                        || (findSubset(inputData[input_position]) == Mode.SHIFTB))
                        && (current_mode == CfMode.MODEB)) {
                    /* Annex B section 1 rule 3 */
                    /* If in Code Subset B and the next data character can be encoded in subset B, encode the next
                     character. */
                    if (inputData[input_position] >= 128) {
                        /* Extended ASCII character */
                        blockmatrix[current_row][column_position] = 100; /* FNC4 */
                        column_position++;
                        c--;
                    }
                    blockmatrix[current_row][column_position] = a3_convert(inputData[input_position]);
                    column_position++;
                    c--;
                    input_position++;
                    done = true;
                }
            }

            if (!done) {
                if (((findSubset(inputData[input_position]) == Mode.ABORC)
                        && (findSubset(inputData[input_position + 1]) == Mode.ABORC))
                        && (current_mode == CfMode.MODEC)) {
                    /* Annex B section 1 rule 4 */
                    /* If in Code Subset C and the next data are 2 digits, encode them. */
                    blockmatrix[current_row][column_position]
                            = ((inputData[input_position] - '0') * 10)
                            + (inputData[input_position + 1] - '0');
                    column_position++;
                    c--;
                    input_position += 2;
                    done = true;
                }
            }

            if (!done) {
                if (((current_mode == CfMode.MODEA) || (current_mode == CfMode.MODEB))
                        && ((findSubset(inputData[input_position]) == Mode.ABORC)
                        || (inputData[input_position] == FNC1))) {
                    // Count the number of numeric digits
                    // If 4 or more numeric data characters occur together when in subsets A or B:
                    //   a. If there is an even number of numeric data characters, insert a Code C character before the
                    //      first numeric digit to change to subset C.
                    //   b. If there is an odd number of numeric data characters, insert a Code Set C character immediately
                    //      after the first numeric digit to change to subset C.
                    i = 0;
                    j = 0;
                    do {
                        i++;
                        if (inputData[input_position + j] == FNC1) {
                            i++;
                        }
                        j++;
                    } while ((findSubset(inputData[input_position + j]) == Mode.ABORC)
                            || ((inputData[input_position + j] == FNC1) && i % 2 == 0));
                    i--;

                    if (i >= 4) {
                        /* Annex B section 1 rule 5 */
                        if (i % 2 == 1) {
                            /* Annex B section 1 rule 5a */
                            blockmatrix[current_row][column_position] = 99; /* Code C */
                            column_position++;
                            c--;
                            blockmatrix[current_row][column_position] =
                                ((inputData[input_position] - '0') * 10) +
                                (inputData[input_position + 1] - '0');
                            column_position++;
                            c--;
                            input_position += 2;
                            current_mode = CfMode.MODEC;
                        } else {
                            /* Annex B section 1 rule 5b */
                            blockmatrix[current_row][column_position] = a3_convert(inputData[input_position]);
                            column_position++;
                            c--;
                            input_position++;
                        }
                        done = true;
                    } else {
                        blockmatrix[current_row][column_position] = a3_convert(inputData[input_position]);
                        column_position++;
                        c--;
                        input_position++;
                        done = true;
                    }
                }
            }

            if (!done) {
                if ((current_mode == CfMode.MODEB) && (findSubset(inputData[input_position]) == Mode.SHIFTA)) {
                    /* Annex B section 1 rule 6 */
                    /*  When in subset B and an ASCII control character occurs in the data:
                     a.   If there is a lower case character immediately following the control character, insert a Shift
                     character before the control character.
                     b.   Otherwise, insert a Code A character before the control character to change to subset A. */
                    if ((inputData[input_position + 1] >= 96) && (inputData[input_position + 1] <= 127)) {
                        /* Annex B section 1 rule 6a */
                        blockmatrix[current_row][column_position] = 98; /* Shift */
                        column_position++;
                        c--;
                        if (inputData[input_position] >= 128) {
                            /* Extended ASCII character */
                            blockmatrix[current_row][column_position] = 101; /* FNC4 */
                            column_position++;
                            c--;
                        }
                        blockmatrix[current_row][column_position] = a3_convert(inputData[input_position]);
                        column_position++;
                        c--;
                        input_position++;
                    } else {
                        /* Annex B section 1 rule 6b */
                        blockmatrix[current_row][column_position] = 101; /* Code A */
                        column_position++;
                        c--;
                        if (inputData[input_position] >= 128) {
                            /* Extended ASCII character */
                            blockmatrix[current_row][column_position] = 101; /* FNC4 */
                            column_position++;
                            c--;
                        }
                        blockmatrix[current_row][column_position] = a3_convert(inputData[input_position]);
                        column_position++;
                        c--;
                        input_position++;
                        current_mode = CfMode.MODEA;
                    }
                    done = true;
                }
            }

            if (!done) {
                if ((current_mode == CfMode.MODEA) && (findSubset(inputData[input_position]) == Mode.SHIFTB)) {
                    /* Annex B section 1 rule 7 */
                    /* When in subset A and a lower case character occurs in the data:
                     a.   If following that character, a control character occurs in the data before the occurrence of
                     another lower case character, insert a Shift character before the lower case character.
                     b.   Otherwise, insert a Code B character before the lower case character to change to subset B. */
                    if (input_position + 2 < inputData.length
                            && (findSubset(inputData[input_position + 1]) == Mode.SHIFTA)
                            && (findSubset(inputData[input_position + 2]) == Mode.SHIFTB)) {
                        /* Annex B section 1 rule 7a */
                        blockmatrix[current_row][column_position] = 98; /* Shift */
                        column_position++;
                        c--;
                        if (inputData[input_position] >= 128) {
                            /* Extended ASCII character */
                            blockmatrix[current_row][column_position] = 100; /* FNC4 */
                            column_position++;
                            c--;
                        }
                        blockmatrix[current_row][column_position] = a3_convert(inputData[input_position]);
                        column_position++;
                        c--;
                        input_position++;
                    } else {
                        /* Annex B section 1 rule 7b */
                        blockmatrix[current_row][column_position] = 100; /* Code B */
                        column_position++;
                        c--;
                        if (inputData[input_position] >= 128) {
                            /* Extended ASCII character */
                            blockmatrix[current_row][column_position] = 100; /* FNC4 */
                            column_position++;
                            c--;
                        }
                        blockmatrix[current_row][column_position] = a3_convert(inputData[input_position]);
                        column_position++;
                        c--;
                        input_position++;
                        current_mode = CfMode.MODEB;
                    }
                    done = true;
                }
            }

            if (!done) {
                if ((current_mode == CfMode.MODEC) && ((findSubset(inputData[input_position]) != Mode.ABORC)
                        || (findSubset(inputData[input_position + 1]) != Mode.ABORC))) {
                    /* Annex B section 1 rule 8 */
                    /*  When in subset C and a non-numeric character (or a single digit) occurs in the data, insert a Code
                     A or Code B character before that character, following rules 8a and 8b to determine between code
                     subsets A and B.
                     a.    If an ASCII control character (eg NUL) occurs in the data before any lower case character, use
                     Code A.
                     b.    Otherwise use Code B. */
                    if (findSubset(inputData[input_position]) == Mode.SHIFTA) {
                        /* Annex B section 1 rule 8a */
                        blockmatrix[current_row][column_position] = 101; /* Code A */
                        column_position++;
                        c--;
                        if (inputData[input_position] >= 128) {
                            /* Extended ASCII character */
                            blockmatrix[current_row][column_position] = 101; /* FNC4 */
                            column_position++;
                            c--;
                        }
                        blockmatrix[current_row][column_position] = a3_convert(inputData[input_position]);
                        column_position++;
                        c--;
                        input_position++;
                        current_mode = CfMode.MODEA;
                    } else {
                        /* Annex B section 1 rule 8b */
                        blockmatrix[current_row][column_position] = 100; /* Code B */
                        column_position++;
                        c--;
                        if (inputData[input_position] >= 128) {
                            /* Extended ASCII character */
                            blockmatrix[current_row][column_position] = 100; /* FNC4 */
                            column_position++;
                            c--;
                        }
                        blockmatrix[current_row][column_position] = a3_convert(inputData[input_position]);
                        column_position++;
                        c--;
                        input_position++;
                        current_mode = CfMode.MODEB;
                    }
                    done = true;
                }
            }

            if (input_position == input_length) {
                /* End of data - Annex B rule 5a */
                if (c == 1) {
                    if (current_mode == CfMode.MODEA) {
                        blockmatrix[current_row][column_position] = 100; /* Code B */
                        current_mode = CfMode.MODEB;
                    } else {
                        blockmatrix[current_row][column_position] = 101; /* Code A */
                        current_mode = CfMode.MODEA;
                    }
                    column_position++;
                    c--;
                }

                if (c == 0) {
                    /* Another row is needed */
                    column_position = 0;
                    c = columns;
                    current_row++;
                    subsets[current_row] = CfMode.MODEA;
                    current_mode = CfMode.MODEA;
                }

                if (c > 2) {
                    /* Fill up the last row */
                    do {
                        if (current_mode == CfMode.MODEA) {
                            blockmatrix[current_row][column_position] = 100; /* Code B */
                            current_mode = CfMode.MODEB;
                        } else {
                            blockmatrix[current_row][column_position] = 101; /* Code A */
                            current_mode = CfMode.MODEA;
                        }
                        column_position++;
                        c--;
                    } while (c > 2);
                }

                /* If (c == 2) { do nothing } */

                exit_status = true;
                finalMode = current_mode;
            } else {
                if (c <= 0) {
                    /* Start new row - Annex B rule 5b */
                    column_position = 0;
                    current_row++;
                    if (current_row > 43) {
                        throw new OkapiInputException("Too many rows.");
                    }
                }
            }

        } while (!exit_status);

        if (current_row == 0) {
            /* fill up the first row */
            for(c = column_position; c <= columns; c++) {
                if(current_mode == CfMode.MODEA) {
                    blockmatrix[current_row][c] = 100; /* Code B */
                    current_mode = CfMode.MODEB;
                } else {
                    blockmatrix[current_row][c] = 101; /* Code A */
                    current_mode = CfMode.MODEA;
                }
            }
            current_row++;
            /* add a second row */
            subsets[current_row] = CfMode.MODEA;
            current_mode = CfMode.MODEA;
            for(c = 0; c <= columns - 2; c++) {
                if(current_mode == CfMode.MODEA) {
                    blockmatrix[current_row][c] = 100; /* Code B */
                    current_mode = CfMode.MODEB;
                } else {
                    blockmatrix[current_row][c] = 101; /* Code A */
                    current_mode = CfMode.MODEA;
                }
            }
        }

        rows = current_row + 1;
    }

    private CfMode character_subset_select(int input_position) {

        /* Section 4.5.2 - Determining the Character Subset Selector in a Row */

        if((inputData[input_position] >= '0') && (inputData[input_position] <= '9')) {
            /* Rule 1 */
            return CfMode.MODEC;
        }

        if((inputData[input_position] >= 128) && (inputData[input_position] <= 160)) {
            /* Rule 2 (i) */
            return CfMode.MODEA;
        }

        if((inputData[input_position] >= 0) && (inputData[input_position] <= 31)) {
            /* Rule 3 */
            return CfMode.MODEA;
        }

        /* Rule 4 */
        return CfMode.MODEB;
    }

    private static int a3_convert(int source) {
        /* Annex A section 3 */
        if (source < 32) {
            return source + 64;
        } else if (source <= 127) {
            return source - 32;
        } else if (source <= 159) {
            return (source - 128) + 64;
        } else {
            return (source - 128) - 32; // source >= 160
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
            x = 0;
            h = rowHeight[yBlock];
            for (xBlock = 0; xBlock < pattern[yBlock].length(); xBlock++) {
                char c = pattern[yBlock].charAt(xBlock);
                w = (c - '0') * moduleWidth;
                if (black) {
                    if (w != 0 && h != 0) {
                        addRectangle(new Rectangle(x, y, w, h));
                    }
                    if ((x + w) > symbol_width) {
                        symbol_width = x + w;
                    }
                }
                black = !black;
                x += w;
            }
            y += h;
            if (y > symbol_height) {
                symbol_height = y;
            }
            if (yBlock != (rowCount - 1)) {
                dividers.add(new Rectangle(11 * moduleWidth, y - 1, symbol_width - (24 * moduleWidth), 2));
            }
        }

        /* Add bars between rows last, so they do not interfere with rectangle merging */
        for (Rectangle divider : dividers) {
            addRectangle(divider);
        }

        /* Add top and bottom binding bars */
        addRectangle(new Rectangle(0, 0, symbol_width, 2)); // top
        addRectangle(new Rectangle(0, y - 1, symbol_width, 2)); // bottom
        symbol_height = (rows * defaultHeight) + 2; // one extra line above, one below
    }
}
