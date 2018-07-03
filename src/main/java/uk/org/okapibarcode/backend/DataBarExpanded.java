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

import static uk.org.okapibarcode.backend.DataBarLimited.getWidths;

/**
 * <p>Implements GS1 DataBar Expanded Omnidirectional and GS1 Expanded Stacked Omnidirectional
 * according to ISO/IEC 24724:2011.
 *
 * <p>DataBar expanded encodes GS1 data in either a linear or stacked format.
 *
 * @author <a href="mailto:rstuart114@gmail.com">Robin Stuart</a>
 */
public class DataBarExpanded extends Symbol {

    private static final int[] G_SUM_EXP = {
        0, 348, 1388, 2948, 3988
    };

    private static final int[] T_EVEN_EXP = {
        4, 20, 52, 104, 204
    };

    private static final int[] MODULES_ODD_EXP = {
        12, 10, 8, 6, 4
    };

    private static final int[] MODULES_EVEN_EXP = {
        5, 7, 9, 11, 13
    };

    private static final int[] WIDEST_ODD_EXP = {
        7, 5, 4, 3, 1
    };

    private static final int[] WIDEST_EVEN_EXP = {
        2, 4, 5, 6, 8
    };

    private static final int[] CHECKSUM_WEIGHT_EXP = { /* Table 14 */
        1, 3, 9, 27, 81, 32, 96, 77, 20, 60, 180, 118, 143, 7, 21, 63, 189,
        145, 13, 39, 117, 140, 209, 205, 193, 157, 49, 147, 19, 57, 171, 91,
        62, 186, 136, 197, 169, 85, 44, 132, 185, 133, 188, 142, 4, 12, 36,
        108, 113, 128, 173, 97, 80, 29, 87, 50, 150, 28, 84, 41, 123, 158, 52,
        156, 46, 138, 203, 187, 139, 206, 196, 166, 76, 17, 51, 153, 37, 111,
        122, 155, 43, 129, 176, 106, 107, 110, 119, 146, 16, 48, 144, 10, 30,
        90, 59, 177, 109, 116, 137, 200, 178, 112, 125, 164, 70, 210, 208, 202,
        184, 130, 179, 115, 134, 191, 151, 31, 93, 68, 204, 190, 148, 22, 66,
        198, 172, 94, 71, 2, 6, 18, 54, 162, 64, 192, 154, 40, 120, 149, 25,
        75, 14, 42, 126, 167, 79, 26, 78, 23, 69, 207, 199, 175, 103, 98, 83,
        38, 114, 131, 182, 124, 161, 61, 183, 127, 170, 88, 53, 159, 55, 165,
        73, 8, 24, 72, 5, 15, 45, 135, 194, 160, 58, 174, 100, 89
    };

    private static final int[] FINDER_PATTERN_EXP = { /* Table 15 */
        1, 8, 4, 1, 1, 1, 1, 4, 8, 1, 3, 6, 4, 1, 1, 1, 1, 4, 6, 3, 3, 4, 6, 1,
        1, 1, 1, 6, 4, 3, 3, 2, 8, 1, 1, 1, 1, 8, 2, 3, 2, 6, 5, 1, 1, 1, 1, 5,
        6, 2, 2, 2, 9, 1, 1, 1, 1, 9, 2, 2
    };

    private static final int[] FINDER_SEQUENCE = { /* Table 16 */
        1, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 4, 3, 0, 0, 0, 0, 0, 0, 0, 0, 1, 6,
        3, 8, 0, 0, 0, 0, 0, 0, 0, 1, 10, 3, 8, 5, 0, 0, 0, 0, 0, 0, 1, 10, 3,
        8, 7, 12, 0, 0, 0, 0, 0, 1, 10, 3, 8, 9, 12, 11, 0, 0, 0, 0, 1, 2, 3,
        4, 5, 6, 7, 8, 0, 0, 0, 1, 2, 3, 4, 5, 6, 7, 10, 9, 0, 0, 1, 2, 3, 4,
        5, 6, 7, 10, 11, 12, 0, 1, 2, 3, 4, 5, 8, 7, 10, 9, 12, 11
    };

    private static final int[] WEIGHT_ROWS = {
        0, 1, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 6,
        3, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 10, 3, 4,
        13, 14, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 17, 18, 3, 4, 13,
        14, 7, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 17, 18, 3, 4, 13, 14,
        11, 12, 21, 22, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 17, 18, 3, 4, 13, 14,
        15, 16, 21, 22, 19, 20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 3, 4, 5, 6, 7,
        8, 9, 10, 11, 12, 13, 14, 0, 0, 0, 0, 0, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8,
        9, 10, 11, 12, 17, 18, 15, 16, 0, 0, 0, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8,
        9, 10, 11, 12, 17, 18, 19, 20, 21, 22, 0, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8,
        13, 14, 11, 12, 17, 18, 15, 16, 21, 22, 19, 20
    };

    private enum Mode {
        UNSTACKED, STACKED
    };

    private enum EncodeMode {
        NUMERIC, ALPHA, ISOIEC, INVALID_CHAR, ANY_ENC, ALPHA_OR_ISO
    };

    private String source;
    private String binaryString;
    private String generalField;
    private EncodeMode[] generalFieldType;
    private boolean linkageFlag;
    private int preferredNoOfColumns;
    private Mode symbolType = Mode.STACKED;

    public DataBarExpanded() {
        inputDataType = DataType.GS1;
    }

    @Override
    public void setDataType(DataType dataType) {
        if (dataType != Symbol.DataType.GS1) {
            throw new IllegalArgumentException("Only GS1 data type is supported for DataBar Expanded symbology.");
        }
    }

    @Override
    protected boolean gs1Supported() {
        return true;
    }

    /**
     * Set the width of a stacked symbol by selecting the number
     * of "columns" or symbol segments in each row of data.
     * @param columns Number of segments in each row
     */
    public void setNoOfColumns(int columns) {
        preferredNoOfColumns = columns;
    }

    /**
     * Set symbology to DataBar Expanded Stacked
     */
    public void setStacked() {
        symbolType = Mode.STACKED;
    }

    /**
     * Set symbology to DataBar Expanded
     */
    public void setNotStacked() {
        symbolType = Mode.UNSTACKED;
    }

    protected void setLinkageFlag() {
        linkageFlag = true;
    }

    protected void unsetLinkageFlag() {
        linkageFlag = false;
    }

    @Override
    protected void encode() {
        int i;
        int j;
        int k;
        int data_chars;
        int[] vs = new int[21];
        int[] group = new int[21];
        int[] v_odd = new int[21];
        int[] v_even = new int[21];
        int[][] char_widths = new int[21][8];
        int checksum;
        int row;
        int check_char;
        int c_group;
        int c_odd;
        int c_even;
        int[] check_widths = new int[8];
        int pattern_width;
        int[] elements = new int[235];
        int codeblocks;
        int stack_rows;
        int blocksPerRow;
        int current_block;
        int current_row;
        boolean special_case_row;
        int elements_in_sub;
        int reader;
        int writer;
        int[] sub_elements = new int[235];
        int l;
        int symbol_row;
        String seperator_binary;
        String seperator_pattern;
        boolean black;
        boolean left_to_right;
        int compositeOffset;

        source = content;

        if (linkageFlag) {
            binaryString = "1";
            compositeOffset = 1;
        } else {
            binaryString = "0";
            compositeOffset = 0;
        }

        calculateBinaryString();

        data_chars = binaryString.length() / 12;

        encodeInfo += "Data Characters: ";
        for (i = 0; i < data_chars; i++) {
            vs[i] = 0;
            for (j = 0; j < 12; j++) {
                if (binaryString.charAt((i * 12) + j) == '1') {
                    vs[i] += 2048 >> j;
                }
            }
//            if (debug) {
//                System.out.println("Data character (vs[" + i + "]) is " + vs[i]);
//            }
            encodeInfo += Integer.toString(vs[i]) + " ";
        }
        encodeInfo += "\n";

        for (i = 0; i < data_chars; i++) {
            if (vs[i] <= 347) {
                group[i] = 1;
            }
            if ((vs[i] >= 348) && (vs[i] <= 1387)) {
                group[i] = 2;
            }
            if ((vs[i] >= 1388) && (vs[i] <= 2947)) {
                group[i] = 3;
            }
            if ((vs[i] >= 2948) && (vs[i] <= 3987)) {
                group[i] = 4;
            }
            if (vs[i] >= 3988) {
                group[i] = 5;
            }
            v_odd[i] = (vs[i] - G_SUM_EXP[group[i] - 1]) / T_EVEN_EXP[group[i] - 1];
            v_even[i] = (vs[i] - G_SUM_EXP[group[i] - 1]) % T_EVEN_EXP[group[i] - 1];

            int[] widths = getWidths(v_odd[i], MODULES_ODD_EXP[group[i] - 1], 4, WIDEST_ODD_EXP[group[i] - 1], 0);
            char_widths[i][0] = widths[0];
            char_widths[i][2] = widths[1];
            char_widths[i][4] = widths[2];
            char_widths[i][6] = widths[3];

            widths = getWidths(v_even[i], MODULES_EVEN_EXP[group[i] - 1], 4, WIDEST_EVEN_EXP[group[i] - 1], 1);
            char_widths[i][1] = widths[0];
            char_widths[i][3] = widths[1];
            char_widths[i][5] = widths[2];
            char_widths[i][7] = widths[3];
        }

        /* 7.2.6 Check character */
        /* The checksum value is equal to the mod 211 residue of the weighted sum of the widths of the
	   elements in the data characters. */
        checksum = 0;
        for (i = 0; i < data_chars; i++) {
            row = WEIGHT_ROWS[(((data_chars - 2) / 2) * 21) + i];
            for (j = 0; j < 8; j++) {
                checksum += (char_widths[i][j] * CHECKSUM_WEIGHT_EXP[(row * 8) + j]);

            }
        }

        check_char = (211 * ((data_chars + 1) - 4)) + (checksum % 211);

        encodeInfo += "Check Character: " + Integer.toString(check_char) + "\n";

        c_group = 1;
        if ((check_char >= 348) && (check_char <= 1387)) {
            c_group = 2;
        }
        if ((check_char >= 1388) && (check_char <= 2947)) {
            c_group = 3;
        }
        if ((check_char >= 2948) && (check_char <= 3987)) {
            c_group = 4;
        }
        if (check_char >= 3988) {
            c_group = 5;
        }

        c_odd = (check_char - G_SUM_EXP[c_group - 1]) / T_EVEN_EXP[c_group - 1];
        c_even = (check_char - G_SUM_EXP[c_group - 1]) % T_EVEN_EXP[c_group - 1];

        int[] widths = getWidths(c_odd, MODULES_ODD_EXP[c_group - 1], 4, WIDEST_ODD_EXP[c_group - 1], 0);
        check_widths[0] = widths[0];
        check_widths[2] = widths[1];
        check_widths[4] = widths[2];
        check_widths[6] = widths[3];

        widths = getWidths(c_even, MODULES_EVEN_EXP[c_group - 1], 4, WIDEST_EVEN_EXP[c_group - 1], 1);
        check_widths[1] = widths[0];
        check_widths[3] = widths[1];
        check_widths[5] = widths[2];
        check_widths[7] = widths[3];

        /* Initialise element array */
        pattern_width = ((((data_chars + 1) / 2) + ((data_chars + 1) & 1)) * 5) + ((data_chars + 1) * 8) + 4;
        for (i = 0; i < pattern_width; i++) {
            elements[i] = 0;
        }

        elements[0] = 1;
        elements[1] = 1;
        elements[pattern_width - 2] = 1;
        elements[pattern_width - 1] = 1;

        /* Put finder patterns in element array */
        for (i = 0; i < (((data_chars + 1) / 2) + ((data_chars + 1) & 1)); i++) {
            k = ((((((data_chars + 1) - 2) / 2) + ((data_chars + 1) & 1)) - 1) * 11) + i;
            for (j = 0; j < 5; j++) {
                elements[(21 * i) + j + 10] = FINDER_PATTERN_EXP[((FINDER_SEQUENCE[k] - 1) * 5) + j];
            }
        }

        /* Put check character in element array */
        for (i = 0; i < 8; i++) {
            elements[i + 2] = check_widths[i];
        }

        /* Put forward reading data characters in element array */
        for (i = 1; i < data_chars; i += 2) {
            for (j = 0; j < 8; j++) {
                elements[(((i - 1) / 2) * 21) + 23 + j] = char_widths[i][j];
            }
        }

        /* Put reversed data characters in element array */
        for (i = 0; i < data_chars; i += 2) {
            for (j = 0; j < 8; j++) {
                elements[((i / 2) * 21) + 15 + j] = char_widths[i][7 - j];
            }
        }


        if (symbolType == Mode.UNSTACKED) {
            /* Copy elements into symbol */
            row_count = 1 + compositeOffset;
            row_height = new int[1 + compositeOffset];
            row_height[0 + compositeOffset] = -1;
            pattern = new String[1 + compositeOffset];

            pattern[0 + compositeOffset] = "0";
            writer = 0;
            black = false;
            seperator_binary = "";
            for (i = 0; i < pattern_width; i++) {
                pattern[0 + compositeOffset] += (char)(elements[i] + '0');
                for (j = 0; j < elements[i]; j++) {
                    if (black) {
                        seperator_binary += "0";
                    } else {
                        seperator_binary += "1";
                    }
                }

                black = !(black);
                writer += elements[i];
            }
            seperator_binary = "0000" + seperator_binary.substring(4, writer - 4);
            for (j = 0; j < (writer / 49); j++) {
                k = (49 * j) + 18;
                for (i = 0; i < 15; i++) {
                    if ((seperator_binary.charAt(i + k - 1) == '1')
                            && (seperator_binary.charAt(i + k) == '1')) {
                        seperator_binary = seperator_binary.substring(0, (i + k))
                                + "0" + seperator_binary.substring(i + k + 1);
                    }
                }
            }
            if (linkageFlag) {
                // Add composite code seperator
                pattern[0] = bin2pat(seperator_binary);
                row_height[0] = 1;
            }

        } else {
            /* RSS Expanded Stacked */
            codeblocks = (data_chars + 1) / 2 + ((data_chars + 1) % 2);

            blocksPerRow = preferredNoOfColumns;
            if ((blocksPerRow < 1) || (blocksPerRow > 10)) {
                blocksPerRow = 2;
            }

            if (linkageFlag && (blocksPerRow == 1)) {
                /* "There shall be a minimum of four symbol characters in the
                first row of an RSS Expanded Stacked symbol when it is the linear
                component of an EAN.UCC Composite symbol." */
                blocksPerRow = 2;
            }

            stack_rows = codeblocks / blocksPerRow;
            if (codeblocks % blocksPerRow > 0) {
                stack_rows++;
            }

            row_count = (stack_rows * 4) - 3;
            row_height = new int[row_count + compositeOffset];
            pattern = new String[row_count + compositeOffset];
            symbol_row = 0;

            current_block = 0;
            for (current_row = 1; current_row <= stack_rows; current_row++) {
                for (i = 0; i < 235; i++) {
                    sub_elements[i] = 0;
                }
                special_case_row = false;

                /* Row Start */
                sub_elements[0] = 1;
                sub_elements[1] = 1;
                elements_in_sub = 2;

                /* Row Data */
                reader = 0;
                do {
                    if ((((blocksPerRow & 1) != 0) || ((current_row & 1) != 0))
                            || ((current_row == stack_rows)
                            && (codeblocks != (current_row * blocksPerRow))
                            && ((((current_row * blocksPerRow) - codeblocks) & 1)) != 0)) {
                        /* left to right */
                        left_to_right = true;
                        i = 2 + (current_block * 21);
                        for (j = 0; j < 21; j++) {
                            if ((i + j) < pattern_width) {
                                sub_elements[j + (reader * 21) + 2] = elements[i + j];
                                elements_in_sub++;
                            }
                        }
                    } else {
                        /* right to left */
                        left_to_right = false;
                        if ((current_row * blocksPerRow) < codeblocks) {
                            /* a full row */
                            i = 2 + (((current_row * blocksPerRow) - reader - 1) * 21);
                            for (j = 0; j < 21; j++) {
                                if ((i + j) < pattern_width) {
                                    sub_elements[(20 - j) + (reader * 21) + 2] = elements[i + j];
                                    elements_in_sub++;
                                }
                            }
                        } else {
                            /* a partial row */
                            k = ((current_row * blocksPerRow) - codeblocks);
                            l = (current_row * blocksPerRow) - reader - 1;
                            i = 2 + ((l - k) * 21);
                            for (j = 0; j < 21; j++) {
                                if ((i + j) < pattern_width) {
                                    sub_elements[(20 - j) + (reader * 21) + 2] = elements[i + j];
                                    elements_in_sub++;
                                }
                            }
                        }
                    }
                    reader++;
                    current_block++;
                } while ((reader < blocksPerRow) && (current_block < codeblocks));

                /* Row Stop */
                sub_elements[elements_in_sub] = 1;
                sub_elements[elements_in_sub + 1] = 1;
                elements_in_sub += 2;

                pattern[symbol_row + compositeOffset] = "";
                black = true;
                row_height[symbol_row + compositeOffset] = -1;

                if ((current_row & 1) != 0) {
                    pattern[symbol_row + compositeOffset] = "0";
                    black = false;
                } else {
                    if ((current_row == stack_rows)
                            && (codeblocks != (current_row * blocksPerRow))
                            && ((((current_row * blocksPerRow) - codeblocks) & 1) != 0)) {
                        /* Special case bottom row */
                        special_case_row = true;
                        sub_elements[0] = 2;
                        pattern[symbol_row + compositeOffset] = "0";
                        black = false;
                    }
                }

                writer = 0;

                seperator_binary = "";
                for (i = 0; i < elements_in_sub; i++) {
                    pattern[symbol_row + compositeOffset] += (char)(sub_elements[i] + '0');
                    for (j = 0; j < sub_elements[i]; j++) {
                        if (black) {
                            seperator_binary += "0";
                        } else {
                            seperator_binary += "1";
                        }
                    }

                    black = !(black);
                    writer += sub_elements[i];
                }
                seperator_binary = "0000" + seperator_binary.substring(4, writer - 4);
                for (j = 0; j < reader; j++) {
                    k = (49 * j) + (special_case_row ? 19 : 18);
                    if (left_to_right) {
                        for (i = 0; i < 15; i++) {
                            if ((seperator_binary.charAt(i + k - 1) == '1')
                                    && (seperator_binary.charAt(i + k) == '1')) {
                                seperator_binary = seperator_binary.substring(0, (i + k))
                                        + "0" + seperator_binary.substring(i + k + 1);
                            }
                        }
                    } else {
                        for (i = 14; i >= 0; i--) {
                            if ((seperator_binary.charAt(i + k + 1) == '1')
                                    && (seperator_binary.charAt(i + k) == '1')) {
                                seperator_binary = seperator_binary.substring(0, (i + k))
                                        + "0" + seperator_binary.substring(i + k + 1);
                            }
                        }
                    }
                }
                seperator_pattern = bin2pat(seperator_binary);

                if ((current_row == 1) && linkageFlag) {
                    // Add composite code seperator
                    row_height[0] = 1;
                    pattern[0] = seperator_pattern;
                }

                if (current_row != 1) {
                    /* middle separator pattern (above current row) */
                    pattern[symbol_row - 2 + compositeOffset] = "05";
                    for (j = 5; j < (49 * blocksPerRow); j += 2) {
                        pattern[symbol_row - 2 + compositeOffset] += "11";
                    }
                    row_height[symbol_row - 2 + compositeOffset] = 1;
                    /* bottom separator pattern (above current row) */
                    row_height[symbol_row - 1 + compositeOffset] = 1;
                    pattern[symbol_row - 1 + compositeOffset] = seperator_pattern;
                }

                if (current_row != stack_rows) {
                    row_height[symbol_row + 1 + compositeOffset] = 1;
                    pattern[symbol_row + 1 + compositeOffset] = seperator_pattern;
                }

                symbol_row += 4;
            }
            readable = "";
            row_count += compositeOffset;
        }
    }

    private void calculateBinaryString() {
        /* Handles all data encodation from section 7.2.5 of ISO/IEC 24724 */
        EncodeMode last_mode = EncodeMode.NUMERIC;
        int encoding_method, i, j, read_posn;
        boolean latch;
        int remainder, d1, d2, value;
        String padstring;
        double weight;
        int group_val;
        int current_length;
        String patch;

        read_posn = 0;

        /* Decide whether a compressed data field is required and if so what
	method to use - method 2 = no compressed data field */

        if ((source.length() >= 16) && ((source.charAt(0) == '0')
                && (source.charAt(1) == '1'))) {
            /* (01) and other AIs */
            encoding_method = 1;
//            if (debug) System.out.printf("Choosing Method 1\n");
        } else {
            /* any AIs */
            encoding_method = 2;
//            if (debug) System.out.printf("Choosing Mehod 2\n");
        }

        if (((source.length() >= 20) && (encoding_method == 1))
                && ((source.charAt(2) == '9') && (source.charAt(16) == '3'))) {
            /* Possibly encoding method > 2 */
//            if (debug) System.out.printf("Checking for other methods\n");

            if ((source.length() >= 26) && (source.charAt(17) == '1')) {
                /* Methods 3, 7, 9, 11 and 13 */

                if (source.charAt(18) == '0') {
                    /* (01) and (310x) */
                    /* In kilos */

                    weight = 0.0;
                    for (i = 0; i < 6; i++) {
                        weight *= 10;
                        weight += (source.charAt(20 + i) - '0');
                    }

                    if (weight < 99999.0) { /* Maximum weight = 99999 */

                        if ((source.charAt(19) == '3') && (source.length() == 26)) {
                            /* (01) and (3103) */
                            weight /= 1000.0;

                            if (weight <= 32.767) {
                                encoding_method = 3;
                            }
                        }

                        if (source.length() == 34) {
                            if ((source.charAt(26) == '1') && (source.charAt(27) == '1')) {
                                /* (01), (310x) and (11) - metric weight and production date */
                                encoding_method = 7;
                            }

                            if ((source.charAt(26) == '1') && (source.charAt(27) == '3')) {
                                /* (01), (310x) and (13) - metric weight and packaging date */
                                encoding_method = 9;
                            }

                            if ((source.charAt(26) == '1') && (source.charAt(27) == '5')) {
                                /* (01), (310x) and (15) - metric weight and "best before" date */
                                encoding_method = 11;
                            }

                            if ((source.charAt(26) == '1') && (source.charAt(27) == '7')) {
                                /* (01), (310x) and (17) - metric weight and expiration date */
                                encoding_method = 13;
                            }
                        }
                    }
                }
//                if (debug) System.out.printf("Now using method %d\n", encoding_method);
            }

            if ((source.length() >= 26) && (source.charAt(17) == '2')) {
                /* Methods 4, 8, 10, 12 and 14 */

                if (source.charAt(18) == '0') {
                    /* (01) and (320x) */
                    /* In pounds */

                    weight = 0.0;
                    for (i = 0; i < 6; i++) {
                        weight *= 10;
                        weight += (source.charAt(20 + i) - '0');
                    }

                    if (weight < 99999.0) { /* Maximum weight = 99999 */

                        if (((source.charAt(19) == '2') || (source.charAt(19) == '3'))
                                && (source.length() == 26)) {
                            /* (01) and (3202)/(3203) */

                            if (source.charAt(19) == '3') {
                                weight /= 1000.0;
                                if (weight <= 22.767) {
                                    encoding_method = 4;
                                }
                            } else {
                                weight /= 100.0;
                                if (weight <= 99.99) {
                                    encoding_method = 4;
                                }
                            }

                        }

                        if (source.length() == 34) {
                            if ((source.charAt(26) == '1') && (source.charAt(27) == '1')) {
                                /* (01), (320x) and (11) - English weight and production date */
                                encoding_method = 8;
                            }

                            if ((source.charAt(26) == '1') && (source.charAt(27) == '3')) {
                                /* (01), (320x) and (13) - English weight and packaging date */
                                encoding_method = 10;
                            }

                            if ((source.charAt(26) == '1') && (source.charAt(27) == '5')) {
                                /* (01), (320x) and (15) - English weight and "best before" date */
                                encoding_method = 12;
                            }

                            if ((source.charAt(26) == '1') && (source.charAt(27) == '7')) {
                                /* (01), (320x) and (17) - English weight and expiration date */
                                encoding_method = 14;
                            }
                        }
                    }
                }
//                if (debug) System.out.printf("Now using method %d\n", encoding_method);

            }

            if (source.charAt(17) == '9') {
                /* Methods 5 and 6 */
                if ((source.charAt(18) == '2') && ((source.charAt(19) >= '0')
                        && (source.charAt(19) <= '3'))) {
                    /* (01) and (392x) */
                    encoding_method = 5;
                }
                if ((source.charAt(18) == '3') && ((source.charAt(19) >= '0')
                        && (source.charAt(19) <= '3'))) {
                    /* (01) and (393x) */
                    encoding_method = 6;
                }
//                if (debug) System.out.printf("Now using method %d\n", encoding_method);
            }
        }

        encodeInfo += "Encoding Method: " + Integer.toString(encoding_method) + "\n";
        switch (encoding_method) { /* Encoding method - Table 10 */
        case 1:
            binaryString += "1XX";
            read_posn = 16;
            break;
        case 2:
            binaryString += "00XX";
            read_posn = 0;
            break;
        case 3:
            binaryString += "0100";
            read_posn = source.length();
            break;
        case 4:
            binaryString += "0101";
            read_posn = source.length();
            break;
        case 5:
            binaryString += "01100XX";
            read_posn = 20;
            break;
        case 6:
            binaryString += "01101XX";
            read_posn = 23;
            break;
        case 7:
            binaryString += "0111000";
            read_posn = source.length();
            break;
        case 8:
            binaryString += "0111001";
            read_posn = source.length();
            break;
        case 9:
            binaryString += "0111010";
            read_posn = source.length();
            break;
        case 10:
            binaryString += "0111011";
            read_posn = source.length();
            break;
        case 11:
            binaryString += "0111100";
            read_posn = source.length();
            break;
        case 12:
            binaryString += "0111101";
            read_posn = source.length();
            break;
        case 13:
            binaryString += "0111110";
            read_posn = source.length();
            break;
        case 14:
            binaryString += "0111111";
            read_posn = source.length();
            break;
        }
//        if (debug) System.out.printf("Setting binary = %s\n", binary_string);


        /* Variable length symbol bit field is just given a place holder (XX)
	for the time being */

        /* Verify that the data to be placed in the compressed data field is all
	numeric data before carrying out compression */
        for (i = 0; i < read_posn; i++) {
            if ((source.charAt(i) < '0') || (source.charAt(i) > '9')) {
                if ((source.charAt(i) != '[') && (source.charAt(i) != ']')) {
                    /* Something is wrong */
                    throw new OkapiException("Invalid characters in input data");
                }
            }
        }

        /* Now encode the compressed data field */

//        if (debug) System.out.printf("Proceeding to encode data\n");
        if (encoding_method == 1) {
            /* Encoding method field "1" - general item identification data */
            group_val = source.charAt(2) - '0';

            for (j = 0; j < 4; j++) {
                if ((group_val & (0x08 >> j)) == 0) {
                    binaryString += "0";
                } else {
                    binaryString += "1";
                }
            }

            for (i = 1; i < 5; i++) {
                group_val = 100 * (source.charAt(i * 3) - '0');
                group_val += 10 * (source.charAt((i * 3) + 1) - '0');
                group_val += source.charAt((i * 3) + 2) - '0';

                for (j = 0; j < 10; j++) {
                    if ((group_val & (0x200 >> j)) == 0) {
                        binaryString += "0";
                    } else {
                        binaryString += "1";
                    }
                }
            }
        }

        if (encoding_method == 3) {
            /* Encoding method field "0100" - variable weight item
		(0,001 kilogram icrements) */

            for (i = 1; i < 5; i++) {
                group_val = 100 * (source.charAt(i * 3) - '0');
                group_val += 10 * (source.charAt((i * 3) + 1) - '0');
                group_val += (source.charAt((i * 3) + 2) - '0');

                for (j = 0; j < 10; j++) {
                    if ((group_val & (0x200 >> j)) == 0) {
                        binaryString += "0";
                    } else {
                        binaryString += "1";
                    }
                }
            }

            group_val = 0;
            for (i = 0; i < 6; i++) {
                group_val *= 10;
                group_val += source.charAt(20 + i) - '0';
            }

            for (j = 0; j < 15; j++) {
                if ((group_val & (0x4000 >> j)) == 0) {
                    binaryString += "0";
                } else {
                    binaryString += "1";
                }
            }
        }

        if (encoding_method == 4) {
            /* Encoding method field "0101" - variable weight item (0,01 or
		0,001 pound increment) */

            for (i = 1; i < 5; i++) {
                group_val = 100 * (source.charAt(i * 3) - '0');
                group_val += 10 * (source.charAt((i * 3) + 1) - '0');
                group_val += (source.charAt((i * 3) + 2) - '0');

                for (j = 0; j < 10; j++) {
                    if ((group_val & (0x200 >> j)) == 0) {
                        binaryString += "0";
                    } else {
                        binaryString += "1";
                    }
                }
            }


            group_val = 0;
            for (i = 0; i < 6; i++) {
                group_val *= 10;
                group_val += source.charAt(20 + i) - '0';
            }

            if (source.charAt(19) == '3') {
                group_val = group_val + 10000;
            }

            for (j = 0; j < 15; j++) {
                if ((group_val & (0x4000 >> j)) == 0) {
                    binaryString += "0";
                } else {
                    binaryString += "1";
                }
            }
        }

        if ((encoding_method >= 7) && (encoding_method <= 14)) {
            /* Encoding method fields "0111000" through "0111111" - variable
		weight item plus date */

            for (i = 1; i < 5; i++) {
                group_val = 100 * (source.charAt(i * 3) - '0');
                group_val += 10 * (source.charAt((i * 3) + 1) - '0');
                group_val += (source.charAt((i * 3) + 2) - '0');

                for (j = 0; j < 10; j++) {
                    if ((group_val & (0x200 >> j)) == 0) {
                        binaryString += "0";
                    } else {
                        binaryString += "1";
                    }
                }
            }

            group_val = source.charAt(19) - '0';

            for (i = 0; i < 5; i++) {
                group_val *= 10;
                group_val += source.charAt(21 + i) - '0';
            }

            for (j = 0; j < 20; j++) {
                if ((group_val & (0x80000 >> j)) == 0) {
                    binaryString += "0";
                } else {
                    binaryString += "1";
                }
            }

            if (source.length() == 34) {
                /* Date information is included */
                group_val = ((10 * (source.charAt(28) - '0'))
                        + (source.charAt(29) - '0')) * 384;
                group_val += (((10 * (source.charAt(30) - '0'))
                        + (source.charAt(31) - '0')) - 1) * 32;
                group_val += (10 * (source.charAt(32) - '0'))
                        + (source.charAt(33) - '0');
            } else {
                group_val = 38400;
            }

            for (j = 0; j < 16; j++) {
                if ((group_val & (0x8000 >> j)) == 0) {
                    binaryString += "0";
                } else {
                    binaryString += "1";
                }
            }
        }

        if (encoding_method == 5) {
            /* Encoding method field "01100" - variable measure item and price */

            for (i = 1; i < 5; i++) {
                group_val = 100 * (source.charAt(i * 3) - '0');
                group_val += 10 * (source.charAt((i * 3) + 1) - '0');
                group_val += (source.charAt((i * 3) + 2) - '0');

                for (j = 0; j < 10; j++) {
                    if ((group_val & (0x200 >> j)) == 0) {
                        binaryString += "0";
                    } else {
                        binaryString += "1";
                    }
                }
            }

            switch (source.charAt(19)) {
            case '0':
                binaryString += "00";
                break;
            case '1':
                binaryString += "01";
                break;
            case '2':
                binaryString += "10";
                break;
            case '3':
                binaryString += "11";
                break;
            }
        }

        if (encoding_method == 6) {
            /* Encoding method "01101" - variable measure item and price with ISO 4217
		Currency Code */

            for (i = 1; i < 5; i++) {
                group_val = 100 * (source.charAt(i * 3) - '0');
                group_val += 10 * (source.charAt((i * 3) + 1) - '0');
                group_val += (source.charAt((i * 3) + 2) - '0');

                for (j = 0; j < 10; j++) {
                    if ((group_val & (0x200 >> j)) == 0) {
                        binaryString += "0";
                    } else {
                        binaryString += "1";
                    }
                }
            }

            switch (source.charAt(19)) {
            case '0':
                binaryString += "00";
                break;
            case '1':
                binaryString += "01";
                break;
            case '2':
                binaryString += "10";
                break;
            case '3':
                binaryString += "11";
                break;
            }

            group_val = 0;
            for (i = 0; i < 3; i++) {
                group_val *= 10;
                group_val += source.charAt(20 + i) - '0';
            }

            for (j = 0; j < 10; j++) {
                if ((group_val & (0x200 >> j)) == 0) {
                    binaryString += "0";
                } else {
                    binaryString += "1";
                }
            }
        }

        /* The compressed data field has been processed if appropriate - the
	rest of the data (if any) goes into a general-purpose data compaction field */

        generalField = source.substring(read_posn);
        generalFieldType = new EncodeMode[generalField.length()];
//        if (debug) System.out.printf("General field data = %s\n", general_field);

        if (generalField.length() != 0) {
            latch = false;
            for (i = 0; i < generalField.length(); i++) {
                /* Table 13 - ISO/IEC 646 encodation */
                if ((generalField.charAt(i) < ' ') || (generalField.charAt(i) > 'z')) {
                    generalFieldType[i] = EncodeMode.INVALID_CHAR;
                    latch = true;
                } else {
                    generalFieldType[i] = EncodeMode.ISOIEC;
                }

                if (generalField.charAt(i) == '#') {
                    generalFieldType[i] = EncodeMode.INVALID_CHAR;
                    latch = true;
                }
                if (generalField.charAt(i) == '$') {
                    generalFieldType[i] = EncodeMode.INVALID_CHAR;
                    latch = true;
                }
                if (generalField.charAt(i) == '@') {
                    generalFieldType[i] = EncodeMode.INVALID_CHAR;
                    latch = true;
                }
                if (generalField.charAt(i) == 92) {
                    generalFieldType[i] = EncodeMode.INVALID_CHAR;
                    latch = true;
                }
                if (generalField.charAt(i) == '^') {
                    generalFieldType[i] = EncodeMode.INVALID_CHAR;
                    latch = true;
                }
                if (generalField.charAt(i) == 96) {
                    generalFieldType[i] = EncodeMode.INVALID_CHAR;
                    latch = true;
                }

                /* Table 12 - Alphanumeric encodation */
                if ((generalField.charAt(i) >= 'A') && (generalField.charAt(i) <= 'Z')) {
                    generalFieldType[i] = EncodeMode.ALPHA_OR_ISO;
                }
                if (generalField.charAt(i) == '*') {
                    generalFieldType[i] = EncodeMode.ALPHA_OR_ISO;
                }
                if (generalField.charAt(i) == ',') {
                    generalFieldType[i] = EncodeMode.ALPHA_OR_ISO;
                }
                if (generalField.charAt(i) == '-') {
                    generalFieldType[i] = EncodeMode.ALPHA_OR_ISO;
                }
                if (generalField.charAt(i) == '.') {
                    generalFieldType[i] = EncodeMode.ALPHA_OR_ISO;
                }
                if (generalField.charAt(i) == '/') {
                    generalFieldType[i] = EncodeMode.ALPHA_OR_ISO;
                }

                /* Numeric encodation */
                if ((generalField.charAt(i) >= '0') && (generalField.charAt(i) <= '9')) {
                    generalFieldType[i] = EncodeMode.ANY_ENC;
                }
                if (generalField.charAt(i) == '[') {
                    /* FNC1 can be encoded in any system */
                    generalFieldType[i] = EncodeMode.ANY_ENC;
                }
            }

            if (latch) {
                throw new OkapiException("Invalid characters in input data");
            }

            for (i = 0; i < generalField.length() - 1; i++) {
                if ((generalFieldType[i] == EncodeMode.ISOIEC)
                        && (generalField.charAt(i + 1) == '[')) {
                    generalFieldType[i + 1] = EncodeMode.ISOIEC;
                }
            }

            for (i = 0; i < generalField.length() - 1; i++) {
                if ((generalFieldType[i] == EncodeMode.ALPHA_OR_ISO)
                        && (generalField.charAt(i + 1) == '[')) {
                    generalFieldType[i + 1] = EncodeMode.ALPHA_OR_ISO;
                }
            }

//            if (debug) {
//                System.out.println("General field length = " + general_field.length());
//            }
            latch = applyGeneralFieldRules();

            /* Set initial mode if not NUMERIC */
            if (generalFieldType[0] == EncodeMode.ALPHA) {
                binaryString += "0000"; /* Alphanumeric latch */
                last_mode = EncodeMode.ALPHA;
            }
            if (generalFieldType[0] == EncodeMode.ISOIEC) {
                binaryString += "0000"; /* Alphanumeric latch */
                binaryString += "00100"; /* ISO/IEC 646 latch */
                last_mode = EncodeMode.ISOIEC;
            }

            i = 0;
            do {
//                if (debug) System.out.printf("Processing character %d ", i);
                switch (generalFieldType[i]) {
                case NUMERIC:
//                    if (debug) System.out.printf("as NUMERIC:");

                    if (last_mode != EncodeMode.NUMERIC) {
                        binaryString += "000"; /* Numeric latch */
//                        if (debug) System.out.printf("<NUMERIC LATCH>\n");
                    }

//                    if (debug) System.out.printf("  %c%c > ", general_field.charAt(i),
//                            general_field.charAt(i + 1));
                    if (generalField.charAt(i) != '[') {
                        d1 = generalField.charAt(i) - '0';
                    } else {
                        d1 = 10;
                    }

                    if (generalField.charAt(i + 1) != '[') {
                        d2 = generalField.charAt(i + 1) - '0';
                    } else {
                        d2 = 10;
                    }

                    value = (11 * d1) + d2 + 8;

                    for (j = 0; j < 7; j++) {
                        if ((value & (0x40 >> j)) != 0) {
                            binaryString += "1";
//                            if (debug) System.out.print("1");
                        } else {
                            binaryString += "0";
//                            if (debug) System.out.print("0");
                        }
                    }

                    i += 2;
//                    if (debug) System.out.printf("\n");
                    last_mode = EncodeMode.NUMERIC;
                    break;

                case ALPHA:
//                    if (debug) System.out.printf("as ALPHA\n");
                    if (i != 0) {
                        if (last_mode == EncodeMode.NUMERIC) {
                            binaryString += "0000"; /* Alphanumeric latch */
                        }
                        if (last_mode == EncodeMode.ISOIEC) {
                            binaryString += "00100"; /* Alphanumeric latch */
                        }
                    }

                    if ((generalField.charAt(i) >= '0') && (generalField.charAt(i) <= '9')) {

                        value = generalField.charAt(i) - 43;

                        for (j = 0; j < 5; j++) {
                            if ((value & (0x10 >> j)) != 0) {
                                binaryString += "1";
                            } else {
                                binaryString += "0";
                            }
                        }
                    }

                    if ((generalField.charAt(i) >= 'A') && (generalField.charAt(i) <= 'Z')) {

                        value = generalField.charAt(i) - 33;

                        for (j = 0; j < 6; j++) {
                            if ((value & (0x20 >> j)) != 0) {
                                binaryString += "1";
                            } else {
                                binaryString += "0";
                            }
                        }
                    }

                    last_mode = EncodeMode.ALPHA;
                    if (generalField.charAt(i) == '[') {
                        binaryString += "01111";
                        last_mode = EncodeMode.NUMERIC;
                    } /* FNC1/Numeric latch */
                    if (generalField.charAt(i) == '*') binaryString += "111010"; /* asterisk */
                    if (generalField.charAt(i) == ',') binaryString += "111011"; /* comma */
                    if (generalField.charAt(i) == '-') binaryString += "111100"; /* minus or hyphen */
                    if (generalField.charAt(i) == '.') binaryString += "111101"; /* period or full stop */
                    if (generalField.charAt(i) == '/') binaryString += "111110"; /* slash or solidus */

                    i++;
                    break;

                case ISOIEC:
//                    if (debug) System.out.printf("as ISOIEC\n");
                    if (i != 0) {
                        if (last_mode == EncodeMode.NUMERIC) {
                            binaryString += "0000"; /* Alphanumeric latch */
                            binaryString += "00100"; /* ISO/IEC 646 latch */
                        }
                        if (last_mode == EncodeMode.ALPHA) {
                            binaryString += "00100"; /* ISO/IEC 646 latch */
                        }
                    }

                    if ((generalField.charAt(i) >= '0')
                            && (generalField.charAt(i) <= '9')) {

                        value = generalField.charAt(i) - 43;

                        for (j = 0; j < 5; j++) {
                            if ((value & (0x10 >> j)) != 0) {
                                binaryString += "1";
                            } else {
                                binaryString += "0";
                            }
                        }
                    }

                    if ((generalField.charAt(i) >= 'A')
                            && (generalField.charAt(i) <= 'Z')) {

                        value = generalField.charAt(i) - 1;

                        for (j = 0; j < 7; j++) {
                            if ((value & (0x40 >> j)) != 0) {
                                binaryString += "1";
                            } else {
                                binaryString += "0";
                            }
                        }
                    }

                    if ((generalField.charAt(i) >= 'a')
                            && (generalField.charAt(i) <= 'z')) {

                        value = generalField.charAt(i) - 7;

                        for (j = 0; j < 7; j++) {
                            if ((value & (0x40 >> j)) != 0) {
                                binaryString += "1";
                            } else {
                                binaryString += "0";
                            }
                        }
                    }

                    last_mode = EncodeMode.ISOIEC;
                    if (generalField.charAt(i) == '[') {
                        binaryString += "01111";
                        last_mode = EncodeMode.NUMERIC;
                    } /* FNC1/Numeric latch */
                    if (generalField.charAt(i) == '!') binaryString += "11101000"; /* exclamation mark */
                    if (generalField.charAt(i) == 34) binaryString += "11101001"; /* quotation mark */
                    if (generalField.charAt(i) == 37) binaryString += "11101010"; /* percent sign */
                    if (generalField.charAt(i) == '&') binaryString += "11101011"; /* ampersand */
                    if (generalField.charAt(i) == 39) binaryString += "11101100"; /* apostrophe */
                    if (generalField.charAt(i) == '(') binaryString += "11101101"; /* left parenthesis */
                    if (generalField.charAt(i) == ')') binaryString += "11101110"; /* right parenthesis */
                    if (generalField.charAt(i) == '*') binaryString += "11101111"; /* asterisk */
                    if (generalField.charAt(i) == '+') binaryString += "11110000"; /* plus sign */
                    if (generalField.charAt(i) == ',') binaryString += "11110001"; /* comma */
                    if (generalField.charAt(i) == '-') binaryString += "11110010"; /* minus or hyphen */
                    if (generalField.charAt(i) == '.') binaryString += "11110011"; /* period or full stop */
                    if (generalField.charAt(i) == '/') binaryString += "11110100"; /* slash or solidus */
                    if (generalField.charAt(i) == ':') binaryString += "11110101"; /* colon */
                    if (generalField.charAt(i) == ';') binaryString += "11110110"; /* semicolon */
                    if (generalField.charAt(i) == '<') binaryString += "11110111"; /* less-than sign */
                    if (generalField.charAt(i) == '=') binaryString += "11111000"; /* equals sign */
                    if (generalField.charAt(i) == '>') binaryString += "11111001"; /* greater-than sign */
                    if (generalField.charAt(i) == '?') binaryString += "11111010"; /* question mark */
                    if (generalField.charAt(i) == '_') binaryString += "11111011"; /* underline or low line */
                    if (generalField.charAt(i) == ' ') binaryString += "11111100"; /* space */

                    i++;
                    break;
                }
                current_length = i;
                if (latch) {
                    current_length++;
                }
            } while (current_length < generalField.length());
//            if (debug) System.out.printf("Resultant binary = %s\n", binary_string);
//            if (debug) System.out.printf("\tLength: %d\n", binary_string.length());

            remainder = calculateRemainder (binaryString.length());

            if (latch) {
                /* There is still one more numeric digit to encode */
//                if (debug) System.out.printf("Adding extra (odd) numeric digit\n");

                if (last_mode == EncodeMode.NUMERIC) {
                    if ((remainder >= 4) && (remainder <= 6)) {
                        value = generalField.charAt(i) - '0';
                        value++;

                        for (j = 0; j < 4; j++) {
                            if ((value & (0x08 >> j)) != 0) {
                                binaryString += "1";
                            } else {
                                binaryString += "0";
                            }
                        }
                    } else {
                        d1 = generalField.charAt(i) - '0';
                        d2 = 10;

                        value = (11 * d1) + d2 + 8;

                        for (j = 0; j < 7; j++) {
                            if ((value & (0x40 >> j)) != 0) {
                                binaryString += "1";
                            } else {
                                binaryString += "0";
                            }
                        }
                    }
                } else {
                    value = generalField.charAt(i) - 43;

                    for (j = 0; j < 5; j++) {
                        if ((value & (0x10 >> j)) != 0) {
                            binaryString += "1";
                        } else {
                            binaryString += "0";
                        }
                    }
                }

//                if (debug) System.out.printf("Resultant binary = %s\n", binary_string);
//                if (debug) System.out.printf("\tLength: %d\n", binary_string.length());
            }
        }

        if (binaryString.length() > 252) {
            throw new OkapiException("Input too long");
        }

        remainder = calculateRemainder (binaryString.length());

        /* Now add padding to binary string (7.2.5.5.4) */
        i = remainder;
        if ((generalField.length() != 0) && (last_mode == EncodeMode.NUMERIC)) {
            padstring = "0000";
            i -= 4;
        } else {
            padstring = "";
        }
        for (; i > 0; i -= 5) {
            padstring += "00100";
        }

        binaryString += padstring.substring(0, remainder);

        /* Patch variable length symbol bit field */
        patch = "";
        if ((((binaryString.length() / 12) + 1) & 1) == 0) {
            patch += "0";
        } else {
            patch += "1";
        }
        if (binaryString.length() <= 156) {
            patch += "0";
        } else {
            patch += "1";
        }

        if (encoding_method == 1) {
            binaryString = binaryString.substring(0, 2) + patch
                    + binaryString.substring(4);
        }
        if (encoding_method == 2) {
            binaryString = binaryString.substring(0, 3) + patch
                    + binaryString.substring(5);
        }
        if ((encoding_method == 5) || (encoding_method == 6)) {
            binaryString = binaryString.substring(0, 6) + patch
                    + binaryString.substring(8);
        }

        encodeInfo += "Binary Length: " + binaryString.length() + "\n";
        displayBinaryString();
//        if (debug) System.out.printf("Resultant binary = %s\n", binary_string);
//        if (debug) System.out.printf("\tLength: %d\n", binary_string.length());
    }

    private static int calculateRemainder ( int binaryStringLength ) {
        int remainder = 12 - (binaryStringLength % 12);
        if (remainder == 12) {
            remainder = 0;
        }
        if (binaryStringLength < 36) {
            remainder = 36 - binaryStringLength;
        }
        return remainder;
    }

    private void displayBinaryString() {
        int i, nibble;
        /* Display binary string as hexadecimal */

        encodeInfo += "Binary String: ";
        nibble = 0;
        for(i = 0; i < binaryString.length(); i++) {
            switch (i % 4) {
                case 0:
                    if (binaryString.charAt(i) == '1') {
                        nibble += 8;
                    }
                    break;
                case 1:
                    if (binaryString.charAt(i) == '1') {
                        nibble += 4;
                    }
                    break;
                case 2:
                    if (binaryString.charAt(i) == '1') {
                        nibble += 2;
                    }
                    break;
                case 3:
                    if (binaryString.charAt(i) == '1') {
                        nibble += 1;
                    }
                    encodeInfo += Integer.toHexString(nibble);
                    nibble = 0;
                    break;
            }
        }

        if ((binaryString.length() % 4) != 0) {
            encodeInfo += Integer.toHexString(nibble);
        }
        encodeInfo += "\n";
    }

    private boolean applyGeneralFieldRules() {
        /* Attempts to apply encoding rules from secions 7.2.5.5.1 to 7.2.5.5.3
	of ISO/IEC 24724:2006 */

        int block_count, i, j, k;
        EncodeMode current, next, last;
        int[] blockLength = new int[200];
        EncodeMode[] blockType = new EncodeMode[200];

        block_count = 0;

        blockLength[block_count] = 1;
        blockType[block_count] = generalFieldType[0];

        for (i = 1; i < generalField.length(); i++) {
            current = generalFieldType[i];
            last = generalFieldType[i - 1];

            if (current == last) {
                blockLength[block_count] = blockLength[block_count] + 1;
            } else {
                block_count++;
                blockLength[block_count] = 1;
                blockType[block_count] = generalFieldType[i];
            }
        }

        block_count++;

        for (i = 0; i < block_count; i++) {
            current = blockType[i];
            next = blockType[i + 1];

            if ((current == EncodeMode.ISOIEC) && (i != (block_count - 1))) {
                if ((next == EncodeMode.ANY_ENC) && (blockLength[i + 1] >= 4)) {
                    blockType[i + 1] = EncodeMode.NUMERIC;
                }
                if ((next == EncodeMode.ANY_ENC) && (blockLength[i + 1] < 4)) {
                    blockType[i + 1] = EncodeMode.ISOIEC;
                }
                if ((next == EncodeMode.ALPHA_OR_ISO) && (blockLength[i + 1] >= 5)) {
                    blockType[i + 1] = EncodeMode.ALPHA;
                }
                if ((next == EncodeMode.ALPHA_OR_ISO) && (blockLength[i + 1] < 5)) {
                    blockType[i + 1] = EncodeMode.ISOIEC;
                }
            }

            if (current == EncodeMode.ALPHA_OR_ISO) {
                blockType[i] = EncodeMode.ALPHA;
                current = EncodeMode.ALPHA;
            }

            if ((current == EncodeMode.ALPHA) && (i != (block_count - 1))) {
                if ((next == EncodeMode.ANY_ENC) && (blockLength[i + 1] >= 6)) {
                    blockType[i + 1] = EncodeMode.NUMERIC;
                }
                if ((next == EncodeMode.ANY_ENC) && (blockLength[i + 1] < 6)) {
                    if ((i == block_count - 2) && (blockLength[i + 1] >= 4)) {
                        blockType[i + 1] = EncodeMode.NUMERIC;
                    } else {
                        blockType[i + 1] = EncodeMode.ALPHA;
                    }
                }
            }

            if (current == EncodeMode.ANY_ENC) {
                blockType[i] = EncodeMode.NUMERIC;
            }
        }

        if (block_count > 1) {
            i = 1;
            while (i < block_count) {
                if (blockType[i - 1] == blockType[i]) {
                    /* bring together */
                    blockLength[i - 1] = blockLength[i - 1] + blockLength[i];
                    j = i + 1;

                    /* decreace the list */
                    while (j < block_count) {
                        blockLength[j - 1] = blockLength[j];
                        blockType[j - 1] = blockType[j];
                        j++;
                    }
                    block_count--;
                    i--;
                }
                i++;
            }
        }

        for (i = 0; i < block_count - 1; i++) {
            if ((blockType[i] == EncodeMode.NUMERIC) && ((blockLength[i] & 1) != 0)) {
                /* Odd size numeric block */
                blockLength[i] = blockLength[i] - 1;
                blockLength[i + 1] = blockLength[i + 1] + 1;
            }
        }

        j = 0;
        for (i = 0; i < block_count; i++) {
            for (k = 0; k < blockLength[i]; k++) {
                generalFieldType[j] = blockType[i];
                j++;
            }
        }

        if ((blockType[block_count - 1] == EncodeMode.NUMERIC)
                && ((blockLength[block_count - 1] & 1) != 0)) {
            /* If the last block is numeric and an odd size, further
		processing needs to be done outside this procedure */
            return true;
        } else {
            return false;
        }
    }
}
