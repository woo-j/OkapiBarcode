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
import static uk.org.okapibarcode.util.Strings.binaryAppend;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <p>Implements GS1 DataBar Expanded Omnidirectional and GS1 DataBar Expanded Stacked
 * Omnidirectional according to ISO/IEC 24724:2011.
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

    /** Table 14 */
    private static final int[] CHECKSUM_WEIGHT_EXP = {
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

    /** Table 15 */
    private static final int[] FINDER_PATTERN_EXP = {
        1, 8, 4, 1, 1, 1, 1, 4, 8, 1, 3, 6, 4, 1, 1, 1, 1, 4, 6, 3, 3, 4, 6, 1,
        1, 1, 1, 6, 4, 3, 3, 2, 8, 1, 1, 1, 1, 8, 2, 3, 2, 6, 5, 1, 1, 1, 1, 5,
        6, 2, 2, 2, 9, 1, 1, 1, 1, 9, 2, 2
    };

    /** Table 16 */
    private static final int[] FINDER_SEQUENCE = {
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

    protected enum EncodeMode {
        NUMERIC, ALPHA, ISOIEC, ANY_ENC, ALPHA_OR_ISO
    }

    private boolean linkageFlag;
    private int preferredColumns = 2;
    private boolean stacked = true;

    public DataBarExpanded() {
        inputDataType = DataType.GS1;
    }

    @Override
    protected boolean gs1Supported() {
        return true;
    }

    /**
     * Sets the preferred width of a stacked symbol by selecting the number of "columns" or symbol segments in each row of data.
     *
     * @param columns the number of segments in each row
     */
    public void setPreferredColumns(int columns) {
        if (columns < 1 || columns > 10) {
            throw new IllegalArgumentException("Invalid column count: " + columns);
        }
        preferredColumns = columns;
    }

    /**
     * Returns the preferred width of a stacked symbol by selecting the number of "columns" or symbol segments in each row of data.
     *
     * @return the number of segments in each row
     */
    public int getPreferredColumns() {
        return preferredColumns;
    }

    /**
     * Sets whether or not this symbology is stacked.
     *
     * @param stacked {@code true} for GS1 DataBar Expanded Stacked Omnidirectional, {@code false} for GS1 DataBar Expanded Omnidirectional
     */
    public void setStacked(boolean stacked) {
        this.stacked = stacked;
    }

    /**
     * Returns whether or not this symbology is stacked.
     *
     * @return {@code true} for GS1 DataBar Expanded Stacked Omnidirectional, {@code false} for GS1 DataBar Expanded Omnidirectional
     */
    public boolean isStacked() {
        return stacked;
    }

    protected void setLinkageFlag(boolean linkageFlag) {
        this.linkageFlag = linkageFlag;
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
        int num_columns;
        int elements_in_sub;
        int reader;
        int[] sub_elements = new int[235];
        int symbol_row;
        boolean black;
        boolean left_to_right;
        int compositeOffset;

        inputData = toBytes(content, StandardCharsets.US_ASCII);
        if (inputData == null) {
            throw OkapiInputException.invalidCharactersInInput();
        }

        StringBuilder binaryString = new StringBuilder(inputData.length * 8);

        if (linkageFlag) {
            binaryString.append('1');
            compositeOffset = 1;
        } else {
            binaryString.append('0');
            compositeOffset = 0;
        }

        int encodingMethod = calculateBinaryString(stacked, preferredColumns, inputData, binaryString); // updates binaryString
        infoLine("Encoding Method: " + encodingMethod);
        logBinaryStringInfo(binaryString);

        data_chars = binaryString.length() / 12;

        info("Data Characters: ");
        for (i = 0; i < data_chars; i++) {
            vs[i] = 0;
            for (j = 0; j < 12; j++) {
                if (binaryString.charAt((i * 12) + j) == '1') {
                    vs[i] += 2048 >> j;
                }
            }
            infoSpace(vs[i]);
        }
        infoLine();

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
        /* The checksum value is equal to the mod 211 residue of the weighted sum of the widths of the elements in the data characters. */
        checksum = 0;
        for (i = 0; i < data_chars; i++) {
            row = WEIGHT_ROWS[(((data_chars - 2) / 2) * 21) + i];
            for (j = 0; j < 8; j++) {
                checksum += (char_widths[i][j] * CHECKSUM_WEIGHT_EXP[(row * 8) + j]);

            }
        }

        check_char = (211 * ((data_chars + 1) - 4)) + (checksum % 211);

        infoLine("Check Character: " + check_char);

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

        if (!stacked) {

            /* Add left and right guards */
            elements[0] = 1;
            elements[1] = 1;
            elements[pattern_width - 2] = 1;
            elements[pattern_width - 1] = 1;

            /* Copy elements into symbol */
            row_count = 1 + compositeOffset;
            row_height = new int[1 + compositeOffset];
            row_height[0 + compositeOffset] = -1;
            pattern = new String[1 + compositeOffset];

            black = false;
            StringBuilder pat = new StringBuilder("0");
            for (i = 0; i < pattern_width; i++) {
                pat.append((char) (elements[i] + '0'));
                black = !black;
            }
            pattern[0 + compositeOffset] = pat.toString();

        } else {

            /* RSS Expanded Stacked */
            codeblocks = (data_chars + 1) / 2 + ((data_chars + 1) % 2);

            blocksPerRow = preferredColumns;
            if (linkageFlag && blocksPerRow == 1) {
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
            AtomicBoolean v2 = new AtomicBoolean(false);

            for (current_row = 1; current_row <= stack_rows; current_row++) {

                Arrays.fill(sub_elements, 0);
                special_case_row = false;
                num_columns = (current_row < stack_rows ? blocksPerRow : codeblocks - current_block);

                /* Row Start */
                sub_elements[0] = 1;
                sub_elements[1] = 1;
                elements_in_sub = 2;

                left_to_right = (current_row  % 2 == 1) || // odd row
                                (blocksPerRow % 2 == 1);   // odd number of segment pairs per row

                if (!left_to_right &&              // row should be mirrored
                    current_row == stack_rows &&   // last row
                    num_columns != blocksPerRow && // partial width row
                    num_columns % 2 == 1) {        // odd number of finder patterns / columns
                    special_case_row = true;
                    left_to_right = true;
                    sub_elements[0] = 2;
                } else {
                    special_case_row = false;
                }

                /* Row Data */
                reader = 0;
                do {
                    i = 2 + (current_block * 21);
                    for (j = 0; j < 21; j++) {
                        if (i + j < pattern_width) {
                            if (left_to_right) {
                                sub_elements[j + (reader * 21) + 2] = elements[i + j];
                            } else {
                                sub_elements[(20 - j) + (num_columns - 1 - reader) * 21 + 2] = elements[i + j];
                            }
                        }
                        elements_in_sub++;
                    }
                    reader++;
                    current_block++;
                } while (reader < blocksPerRow && current_block < codeblocks);

                /* Row Stop */
                sub_elements[elements_in_sub] = 1;
                sub_elements[elements_in_sub + 1] = 1;
                elements_in_sub += 2;

                black = true;
                row_height[symbol_row + compositeOffset] = -1;
                StringBuilder pat = new StringBuilder();
                if (current_row % 2 == 1 || special_case_row) {
                    pat.append('0');
                    black = false;
                }
                for (i = 0; i < elements_in_sub; i++) {
                    pat.append((char) (sub_elements[i] + '0'));
                    black = !black;
                }
                pattern[symbol_row + compositeOffset] = pat.toString();

                if (current_row != 1) {
                    /* middle separator pattern (above current row) */
                    StringBuilder sep = new StringBuilder("05");
                    for (j = 5; j < (49 * blocksPerRow); j += 2) {
                        sep.append("11");
                    }
                    pattern[symbol_row - 2 + compositeOffset] = sep.toString();
                    row_height[symbol_row - 2 + compositeOffset] = 1;
                    /* bottom separator pattern (above current row) */
                    boolean odd_last_row = (current_row == stack_rows) && (data_chars % 2 == 0);
                    row_height[symbol_row - 1 + compositeOffset] = 1;
                    pattern[symbol_row - 1 + compositeOffset] = separator(pat, reader, false, special_case_row, left_to_right, odd_last_row, v2);
                }

                if (current_row != stack_rows) {
                    /* top separator pattern (below current row) */
                    row_height[symbol_row + 1 + compositeOffset] = 1;
                    pattern[symbol_row + 1 + compositeOffset] = separator(pat, reader, true, false, left_to_right, false, v2);
                }

                symbol_row += 4;

            } // end current_row loop

            readable = "";
            row_count += compositeOffset;
        }

        if (linkageFlag) {
            // Add composite code separator
            pattern[0] = separator(pattern[1], 4, false, false, true, false, new AtomicBoolean(false));
            row_height[0] = 1;
        }
    }

    private static String separator(CharSequence pattern, int cols, boolean below, boolean specialCaseRow,
                    boolean leftToRight, boolean oddLastRow, AtomicBoolean v2mutable) {

        // start with the complement of the linear symbol
        StringBuilder linearBin = new StringBuilder();
        StringBuilder separator = new StringBuilder();
        boolean black = true;
        for (int i = 0; i < pattern.length(); i++) {
            int c = pattern.charAt(i) - '0';
            for (int j = 0; j < c; j++) {
                linearBin.append(black ? '1' : '0');
                separator.append(black ? '0' : '1');
            }
            black = !black;
        }

        // clear first 4 and last 4 modules
        for (int i = 0; i < 4; i++) {
            separator.setCharAt(i, '0');
            separator.setCharAt(separator.length() - 1 - i, '0');
        }

        // finder adjustments
        boolean space = false;
        boolean v2 = v2mutable.get();
        for (int j = 0; j < cols; j++) {
            // 49 == data (17) + finder (15) + data(17) triplet
            // 19 == 2 (guard) + 17 (initial check/data character)
            int k = (49 * j) + 19 + (specialCaseRow ? 1 : 0);
            if (leftToRight) {
                // version 1 finder: first 13 modules
                // version 2 finder: last 13 modules
                int start = v2 ? 2 : 0;
                int end = v2 ? 15 : 13;
                for (int i = start; i < end; i++) {
                    if (i + k < linearBin.length()) {
                        if (linearBin.charAt(i + k) == '1') {
                            separator.setCharAt(i + k, '0');
                            space = false;
                        } else {
                            separator.setCharAt(i + k, space ? '0' : '1');
                            space = !space;
                        }
                    }
                }
            } else {
                if (oddLastRow) {
                    // no data char at beginning of row (ends with finder)
                    k -= 17;
                }
                // version 1 finder: first 13 modules
                // version 2 finder: last 13 modules
                int start = v2 ? 14 : 12;
                int end = v2 ? 2 : 0;
                for (int i = start; i >= end; i--) {
                    if (i + k < linearBin.length()) {
                        if (linearBin.charAt(i + k) == '1') {
                            separator.setCharAt(i + k, '0');
                            space = false;
                        } else {
                            separator.setCharAt(i + k, space ? '0' : '1');
                            space = !space;
                        }
                    }
                }
            }
            v2 = !v2;
        }

        if (below) {
            v2mutable.set(v2);
        }

        return bin2pat(separator);
    }

    /** Handles all data encodation from section 7.2.5 of ISO/IEC 24724. */
    private static int calculateBinaryString(boolean stacked, int blocksPerRow, int[] inputData, StringBuilder binaryString) {

        EncodeMode lastMode = EncodeMode.NUMERIC;
        int remainder, d1, d2, value;
        String padstring;

        /* Decide whether a compressed data field is required and if so what method to use: method 2 = no compressed data field */

        int encodingMethod;
        if (inputData.length >= 16 && inputData[0] == '0' && inputData[1] == '1') {
            /* (01) and other AIs */
            encodingMethod = 1;
        } else {
            /* any AIs */
            encodingMethod = 2;
        }

        if (inputData.length >= 20 && encodingMethod == 1 && inputData[2] == '9' && inputData[16] == '3') {

            /* Possibly encoding method > 2 */

            if (inputData.length >= 26 && inputData[17] == '1') {
                /* Methods 3, 7, 9, 11 and 13 */
                if (inputData[18] == '0') {
                    /* (01) and (310x), weight in kilos */
                    double weight = 0;
                    for (int i = 0; i < 6; i++) {
                        weight *= 10;
                        weight += (inputData[20 + i] - '0');
                    }
                    if (weight < 99_999) { /* Maximum weight = 99999 */
                        if (inputData[19] == '3' && inputData.length == 26) {
                            /* (01) and (3103) */
                            weight /= 1000.0;
                            if (weight <= 32.767) {
                                encodingMethod = 3;
                            }
                        }
                        if (inputData.length == 34) {
                            if (inputData[26] == '1' && inputData[27] == '1') {
                                /* (01), (310x) and (11) - metric weight and production date */
                                encodingMethod = 7;
                            }
                            if (inputData[26] == '1' && inputData[27] == '3') {
                                /* (01), (310x) and (13) - metric weight and packaging date */
                                encodingMethod = 9;
                            }
                            if (inputData[26] == '1' && inputData[27] == '5') {
                                /* (01), (310x) and (15) - metric weight and "best before" date */
                                encodingMethod = 11;
                            }
                            if (inputData[26] == '1' && inputData[27] == '7') {
                                /* (01), (310x) and (17) - metric weight and expiration date */
                                encodingMethod = 13;
                            }
                        }
                    }
                }
            }

            if (inputData.length >= 26 && inputData[17] == '2') {
                /* Methods 4, 8, 10, 12 and 14 */
                if (inputData[18] == '0') {
                    /* (01) and (320x), weight in pounds */
                    double weight = 0;
                    for (int i = 0; i < 6; i++) {
                        weight *= 10;
                        weight += (inputData[20 + i] - '0');
                    }
                    if (weight < 99_999) { /* Maximum weight = 99999 */
                        if ((inputData[19] == '2' || inputData[19] == '3') && (inputData.length == 26)) {
                            /* (01) and (3202)/(3203) */
                            if (inputData[19] == '3') {
                                weight /= 1000.0;
                                if (weight <= 22.767) {
                                    encodingMethod = 4;
                                }
                            } else {
                                weight /= 100.0;
                                if (weight <= 99.99) {
                                    encodingMethod = 4;
                                }
                            }
                        }
                        if (inputData.length == 34) {
                            if (inputData[26] == '1' && inputData[27] == '1') {
                                /* (01), (320x) and (11) - English weight and production date */
                                encodingMethod = 8;
                            }
                            if (inputData[26] == '1' && inputData[27] == '3') {
                                /* (01), (320x) and (13) - English weight and packaging date */
                                encodingMethod = 10;
                            }
                            if (inputData[26] == '1' && inputData[27] == '5') {
                                /* (01), (320x) and (15) - English weight and "best before" date */
                                encodingMethod = 12;
                            }
                            if (inputData[26] == '1' && inputData[27] == '7') {
                                /* (01), (320x) and (17) - English weight and expiration date */
                                encodingMethod = 14;
                            }
                        }
                    }
                }
            }

            if (inputData[17] == '9') {
                /* Methods 5 and 6 */
                if (inputData[18] == '2' && inputData[19] >= '0' && inputData[19] <= '3') {
                    /* (01) and (392x) */
                    encodingMethod = 5;
                }
                if (inputData[18] == '3' && inputData[19] >= '0' && inputData[19] <= '3') {
                    /* (01) and (393x) */
                    encodingMethod = 6;
                }
            }
        }

        /* Encoding method - Table 10 */
        /* Variable length symbol bit field is just given a place holder (XX) for the time being */
        int read_posn;
        switch (encodingMethod) {
            case 1:
                binaryString.append("1XX");
                read_posn = 16;
                break;
            case 2:
                binaryString.append("00XX");
                read_posn = 0;
                break;
            case 3:
                binaryString.append("0100");
                read_posn = inputData.length;
                break;
            case 4:
                binaryString.append("0101");
                read_posn = inputData.length;
                break;
            case 5:
                binaryString.append("01100XX");
                read_posn = 20;
                break;
            case 6:
                binaryString.append("01101XX");
                read_posn = 23;
                break;
            default: /* modes 7 (0111000) to 14 (0111111) */
                binaryString.append("0" + Integer.toBinaryString(56 + encodingMethod - 7));
                read_posn = inputData.length;
                break;
        }

        /* Verify that the data to be placed in the compressed data field is all numeric data before carrying out compression */
        for (int i = 0; i < read_posn; i++) {
            if (inputData[i] < '0' || inputData[i] > '9') {
                /* Something is wrong */
                throw OkapiInputException.invalidCharactersInInput();
            }
        }

        /* Now encode the compressed data field */

        if (encodingMethod == 1) {
            /* Encoding method field "1" - general item identification data */
            binaryAppend(binaryString, inputData[2] - '0', 4);
            for (int i = 1; i < 5; i++) {
                int group = parseInt(inputData, i * 3, 3);
                binaryAppend(binaryString, group, 10);
            }
        }

        if (encodingMethod == 3 || encodingMethod == 4) {
            /* Encoding method field "0100" - variable weight item (0,001 kilogram increments) */
            /* Encoding method field "0101" - variable weight item (0,01 or 0,001 pound increment) */
            for (int i = 1; i < 5; i++) {
                int group = parseInt(inputData, i * 3, 3);
                binaryAppend(binaryString, group, 10);
            }
            int group = parseInt(inputData, 20, 6);
            if (encodingMethod == 4 && inputData[19] == '3') {
                group += 10_000;
            }
            binaryAppend(binaryString, group, 15);
        }

        if (encodingMethod == 5 || encodingMethod == 6) {
            /* Encoding method field "01100" - variable measure item and price */
            /* Encoding method "01101" - variable measure item and price with ISO 4217 currency code */
            for (int i = 1; i < 5; i++) {
                int group = parseInt(inputData, i * 3, 3);
                binaryAppend(binaryString, group, 10);
            }
            binaryAppend(binaryString, inputData[19] - '0', 2);
            if (encodingMethod == 6) {
                int currency = parseInt(inputData, 20, 3);
                binaryAppend(binaryString, currency, 10);
            }
        }

        if (encodingMethod >= 7 && encodingMethod <= 14) {
            /* Encoding method fields "0111000" through "0111111" - variable weight item plus date */
            for (int i = 1; i < 5; i++) {
                int group = parseInt(inputData, i * 3, 3);
                binaryAppend(binaryString, group, 10);
            }
            int weight = inputData[19] - '0';
            for (int i = 0; i < 5; i++) {
                weight *= 10;
                weight += inputData[21 + i] - '0';
            }
            binaryAppend(binaryString, weight, 20);
            int date;
            if (inputData.length == 34) {
                /* Date information is included */
                date = parseInt(inputData, 28, 2) * 384;
                date += (parseInt(inputData, 30, 2) - 1) * 32;
                date += parseInt(inputData, 32, 2);
            } else {
                date = 38_400;
            }
            binaryAppend(binaryString, date, 16);
        }

        /* The compressed data field has been processed if appropriate - the rest of the data (if any) goes into a general-purpose data compaction field */

        int[] generalField = Arrays.copyOfRange(inputData, read_posn, inputData.length);

        if (generalField.length != 0) {

            EncodeMode[] generalFieldType = getInitialEncodeModes(generalField);
            boolean trailingDigit = applyGeneralFieldRules(generalFieldType); // modifies generalFieldType
            lastMode = appendToBinaryString(generalField, generalFieldType, trailingDigit, false, binaryString); // modifies binaryString
            remainder = calculateRemainder(binaryString.length(), stacked, blocksPerRow);

            if (trailingDigit) {
                /* There is still one more numeric digit to encode */
                int i = generalField.length - 1;
                if (lastMode == EncodeMode.NUMERIC) {
                    if (remainder >= 4 && remainder <= 6) {
                        value = generalField[i] - '0';
                        value++;
                        binaryAppend(binaryString, value, 4);
                    } else {
                        d1 = generalField[i] - '0';
                        d2 = 10;
                        value = (11 * d1) + d2 + 8;
                        binaryAppend(binaryString, value, 7);
                    }
                } else {
                    value = generalField[i] - 43;
                    binaryAppend(binaryString, value, 5);
                }
            }
        }

        if (binaryString.length() > 252) {
            throw OkapiInputException.inputTooLong();
        }

        remainder = calculateRemainder(binaryString.length(), stacked, blocksPerRow);

        /* Now add padding to binary string (7.2.5.5.4) */
        int i = remainder;
        if (lastMode == EncodeMode.NUMERIC) {
            padstring = "0000";
            i -= 4;
        } else {
            padstring = "";
        }
        for (; i > 0; i -= 5) {
            padstring += "00100";
        }
        binaryString.append(padstring.substring(0, remainder));

        /* Patch variable length symbol bit field */
        char patchEvenOdd, patchSize;
        if ((((binaryString.length() / 12) + 1) & 1) == 0) {
            patchEvenOdd = '0';
        } else {
            patchEvenOdd = '1';
        }
        if (binaryString.length() <= 156) {
            patchSize = '0';
        } else {
            patchSize = '1';
        }

        if (encodingMethod == 1) {
            binaryString.setCharAt(2, patchEvenOdd);
            binaryString.setCharAt(3, patchSize);
        }
        if (encodingMethod == 2) {
            binaryString.setCharAt(3, patchEvenOdd);
            binaryString.setCharAt(4, patchSize);
        }
        if (encodingMethod == 5 || encodingMethod == 6) {
            binaryString.setCharAt(6, patchEvenOdd);
            binaryString.setCharAt(7, patchSize);
        }

        return encodingMethod;
    }

    private static int calculateRemainder(int binaryStringLength, boolean stacked, int blocksPerRow) {
        int remainder = 12 - (binaryStringLength % 12);
        if (remainder == 12) {
            remainder = 0;
        }
        if (binaryStringLength < 36) {
            remainder = 36 - binaryStringLength;
        }
        if (stacked) {
            int symbolChars = ((binaryStringLength + remainder) / 12) + 1; // +1 for check digit
            int symbolCharsInLastRow = symbolChars % (blocksPerRow * 2);
            if (symbolCharsInLastRow == 1) {
                // 7.2.8: The last row shall contain a minimum of two symbol characters with extra padding, if needed.
                remainder += 12;
            }
        }
        return remainder;
    }

    /** Logs binary string as hexadecimal */
    private void logBinaryStringInfo(StringBuilder binaryString) {

        infoLine("Binary Length: " + binaryString.length());
        info("Binary String: ");

        int nibble = 0;
        for (int i = 0; i < binaryString.length(); i++) {
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
                    info(Integer.toHexString(nibble));
                    nibble = 0;
                    break;
            }
        }

        if ((binaryString.length() % 4) != 0) {
            info(Integer.toHexString(nibble));
        }

        infoLine();
    }

    protected static EncodeMode[] getInitialEncodeModes(int[] generalField) {

        EncodeMode[] generalFieldType = new EncodeMode[generalField.length];

        for (int i = 0; i < generalField.length; i++) {
            /* Tables 11, 12, 13 - ISO/IEC 646 encodation */
            int c = generalField[i];
            EncodeMode mode;
            if (c == FNC1) {
                // FNC1 can be encoded in any system
                mode = EncodeMode.ANY_ENC;
            } else if (c >= '0' && c <= '9') {
                // numbers can be encoded in any system, but will usually narrow down to numeric encodation
                mode = EncodeMode.ANY_ENC;
            } else if ((c >= 'A' && c <= 'Z') || c == '*' || c == ',' || c == '-' || c == '.' || c == '/') {
                // alphanumeric encodation or ISO/IEC encodation
                mode = EncodeMode.ALPHA_OR_ISO;
            } else if ((c >= 'a' && c <= 'z') || c == '!' || c == '"' || c == '%' || c == '&' || c == '\'' ||
                        c == '(' || c == ')' || c == '+' || c == ':' || c == ';' || c == '<' || c == '=' ||
                        c == '>' || c == '?' || c == '_' || c == ' ') {
                // ISO/IEC encodation
                mode = EncodeMode.ISOIEC;
            } else {
                // unable to encode this character
                throw OkapiInputException.invalidCharactersInInput();
            }
            generalFieldType[i] = mode;
        }

        for (int i = 0; i < generalField.length - 1; i++) {
            if (generalFieldType[i] == EncodeMode.ISOIEC && generalField[i + 1] == FNC1) {
                generalFieldType[i + 1] = EncodeMode.ISOIEC;
            }
        }

        for (int i = 0; i < generalField.length - 1; i++) {
            if (generalFieldType[i] == EncodeMode.ALPHA_OR_ISO && generalField[i + 1] == FNC1) {
                generalFieldType[i + 1] = EncodeMode.ALPHA_OR_ISO;
            }
        }

        return generalFieldType;
    }

    /** Attempts to apply encoding rules from sections 7.2.5.5.1 to 7.2.5.5.3 of ISO/IEC 24724:2006 */
    protected static boolean applyGeneralFieldRules(EncodeMode[] generalFieldType) {

        int block_count, i, j, k;
        EncodeMode current, next, last;
        int[] blockLength = new int[200];
        EncodeMode[] blockType = new EncodeMode[200];

        block_count = 0;

        blockLength[block_count] = 1;
        blockType[block_count] = generalFieldType[0];

        for (i = 1; i < generalFieldType.length; i++) {
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

            if (current == EncodeMode.ISOIEC && i != (block_count - 1)) {
                if (next == EncodeMode.ANY_ENC && blockLength[i + 1] >= 4) {
                    blockType[i + 1] = EncodeMode.NUMERIC;
                }
                if (next == EncodeMode.ANY_ENC && blockLength[i + 1] < 4) {
                    blockType[i + 1] = EncodeMode.ISOIEC;
                }
                if (next == EncodeMode.ALPHA_OR_ISO && blockLength[i + 1] >= 5) {
                    blockType[i + 1] = EncodeMode.ALPHA;
                }
                if (next == EncodeMode.ALPHA_OR_ISO && blockLength[i + 1] < 5) {
                    blockType[i + 1] = EncodeMode.ISOIEC;
                }
            }

            if (current == EncodeMode.ALPHA_OR_ISO) {
                blockType[i] = EncodeMode.ALPHA;
                current = EncodeMode.ALPHA;
            }

            if (current == EncodeMode.ALPHA && i != (block_count - 1)) {
                if (next == EncodeMode.ANY_ENC && blockLength[i + 1] >= 6) {
                    blockType[i + 1] = EncodeMode.NUMERIC;
                }
                if (next == EncodeMode.ANY_ENC && blockLength[i + 1] < 6) {
                    if (i == block_count - 2 && blockLength[i + 1] >= 4) {
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

                    /* decrease the list */
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
            if (blockType[i] == EncodeMode.NUMERIC && (blockLength[i] & 1) != 0) {
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

        if (blockType[block_count - 1] == EncodeMode.NUMERIC && (blockLength[block_count - 1] & 1) != 0) {
            /* If the last block is numeric and an odd size, further processing needs to be done outside this procedure */
            return true;
        } else {
            return false;
        }
    }

    protected static EncodeMode appendToBinaryString(int[] generalField, EncodeMode[] generalFieldType, boolean trailingDigit, boolean treatFnc1AsNumericLatch, StringBuilder binaryString) {

        EncodeMode lastMode = EncodeMode.NUMERIC;
        int value, d1, d2;

        /* Set initial mode if not NUMERIC */
        if (generalFieldType[0] == EncodeMode.ALPHA) {
            binaryString.append("0000"); /* Alphanumeric latch */
            lastMode = EncodeMode.ALPHA;
        }
        if (generalFieldType[0] == EncodeMode.ISOIEC) {
            binaryString.append("0000"); /* Alphanumeric latch */
            binaryString.append("00100"); /* ISO/IEC 646 latch */
            lastMode = EncodeMode.ISOIEC;
        }

        int i = 0;

        int current_length = i;
        if (trailingDigit) {
            current_length++;
        }

        while (current_length < generalField.length) {

            switch (generalFieldType[i]) {
            case NUMERIC:
                if (lastMode != EncodeMode.NUMERIC) {
                    binaryString.append("000"); /* Numeric latch */
                }
                if (generalField[i] != FNC1) {
                    d1 = generalField[i] - '0';
                } else {
                    d1 = 10;
                }
                if (generalField[i + 1] != FNC1) {
                    d2 = generalField[i + 1] - '0';
                } else {
                    d2 = 10;
                }
                value = (11 * d1) + d2 + 8;
                binaryAppend(binaryString, value, 7);
                i += 2;
                lastMode = EncodeMode.NUMERIC;
                break;

            case ALPHA:
                if (i != 0) {
                    if (lastMode == EncodeMode.NUMERIC) {
                        binaryString.append("0000"); /* Alphanumeric latch */
                    }
                    if (lastMode == EncodeMode.ISOIEC) {
                        binaryString.append("00100"); /* Alphanumeric latch */
                    }
                }
                if (generalField[i] >= '0' && generalField[i] <= '9') {
                    value = generalField[i] - 43;
                    binaryAppend(binaryString, value, 5);
                }
                if (generalField[i] >= 'A' && generalField[i] <= 'Z') {
                    value = generalField[i] - 33;
                    binaryAppend(binaryString, value, 6);
                }
                lastMode = EncodeMode.ALPHA;
                if (generalField[i] == FNC1) {
                    binaryString.append("01111");
                    // TODO: FNC1 should act as an implicit numeric latch, so the commented out line below should be correct, but ZXing cannot
                    // read barcodes which use FNC1 as an implicit numeric latch... so for now, and in order to achieve widest compatibility,
                    // we sometimes waste 3 bits and don't perform the implicit mode change (see https://sourceforge.net/p/zint/tickets/145/)
                    if (treatFnc1AsNumericLatch) lastMode = EncodeMode.NUMERIC;
                } /* FNC1 / Numeric latch */
                if (generalField[i] == '*') binaryString.append("111010"); /* asterisk */
                if (generalField[i] == ',') binaryString.append("111011"); /* comma */
                if (generalField[i] == '-') binaryString.append("111100"); /* minus or hyphen */
                if (generalField[i] == '.') binaryString.append("111101"); /* period or full stop */
                if (generalField[i] == '/') binaryString.append("111110"); /* slash or solidus */
                i++;
                break;

            case ISOIEC:
                if (i != 0) {
                    if (lastMode == EncodeMode.NUMERIC) {
                        binaryString.append("0000"); /* Alphanumeric latch */
                        binaryString.append("00100"); /* ISO/IEC 646 latch */
                    }
                    if (lastMode == EncodeMode.ALPHA) {
                        binaryString.append("00100"); /* ISO/IEC 646 latch */
                    }
                }
                if (generalField[i] >= '0' && generalField[i] <= '9') {
                    value = generalField[i] - 43;
                    binaryAppend(binaryString, value, 5);
                }
                if (generalField[i] >= 'A' && generalField[i] <= 'Z') {
                    value = generalField[i] - 1;
                    binaryAppend(binaryString, value, 7);
                }
                if (generalField[i] >= 'a' && generalField[i] <= 'z') {
                    value = generalField[i] - 7;
                    binaryAppend(binaryString, value, 7);
                }
                lastMode = EncodeMode.ISOIEC;
                if (generalField[i] == FNC1) {
                    binaryString.append("01111");
                    // TODO: FNC1 should act as an implicit numeric latch, so the commented out line below should be correct, but ZXing cannot
                    // read barcodes which use FNC1 as an implicit numeric latch... so for now, and in order to achieve widest compatibility,
                    // we sometimes waste 3 bits and don't perform the implicit mode change (see https://sourceforge.net/p/zint/tickets/145/)
                    if (treatFnc1AsNumericLatch) lastMode = EncodeMode.NUMERIC;
                } /* FNC1 / Numeric latch */
                if (generalField[i] == '!') binaryString.append("11101000"); /* exclamation mark */
                if (generalField[i] == 34)  binaryString.append("11101001"); /* quotation mark */
                if (generalField[i] == 37)  binaryString.append("11101010"); /* percent sign */
                if (generalField[i] == '&') binaryString.append("11101011"); /* ampersand */
                if (generalField[i] == 39)  binaryString.append("11101100"); /* apostrophe */
                if (generalField[i] == '(') binaryString.append("11101101"); /* left parenthesis */
                if (generalField[i] == ')') binaryString.append("11101110"); /* right parenthesis */
                if (generalField[i] == '*') binaryString.append("11101111"); /* asterisk */
                if (generalField[i] == '+') binaryString.append("11110000"); /* plus sign */
                if (generalField[i] == ',') binaryString.append("11110001"); /* comma */
                if (generalField[i] == '-') binaryString.append("11110010"); /* minus or hyphen */
                if (generalField[i] == '.') binaryString.append("11110011"); /* period or full stop */
                if (generalField[i] == '/') binaryString.append("11110100"); /* slash or solidus */
                if (generalField[i] == ':') binaryString.append("11110101"); /* colon */
                if (generalField[i] == ';') binaryString.append("11110110"); /* semicolon */
                if (generalField[i] == '<') binaryString.append("11110111"); /* less-than sign */
                if (generalField[i] == '=') binaryString.append("11111000"); /* equals sign */
                if (generalField[i] == '>') binaryString.append("11111001"); /* greater-than sign */
                if (generalField[i] == '?') binaryString.append("11111010"); /* question mark */
                if (generalField[i] == '_') binaryString.append("11111011"); /* underline or low line */
                if (generalField[i] == ' ') binaryString.append("11111100"); /* space */
                i++;
                break;
            }

            current_length = i;
            if (trailingDigit) {
                current_length++;
            }
        }

        return lastMode;
    }

    private static int parseInt(int[] chars, int index, int length) {
        int val = 0;
        int pow = (int) Math.pow(10, length - 1);
        for (int i = 0; i < length; i++) {
            int c = chars[index + i];
            val += (c - '0') * pow;
            pow /= 10;
        }
        return val;
    }
}
