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

import java.math.BigInteger;

/**
 * <p>Implements GS1 DataBar Omnidirectional and GS1 DataBar Truncated according to ISO/IEC 24724:2011.
 *
 * <p>Input data should be a 13-digit Global Trade Identification Number (GTIN) without check digit or
 * Application Identifier [01].
 *
 * @author <a href="mailto:rstuart114@gmail.com">Robin Stuart</a>
 */
public class DataBar14 extends Symbol {

    public enum Mode {
        /** DataBar-14 */
        LINEAR,
        /** DataBar-14 Omnidirectional */
        OMNI,
        /** DataBar-14 Omnidirectional Stacked */
        STACKED
    }

    private static final int[] G_SUM_TABLE = {
        0, 161, 961, 2015, 2715, 0, 336, 1036, 1516
    };

    private static final int[] T_TABLE = {
        1, 10, 34, 70, 126, 4, 20, 48, 81
    };

    private static final int[] MODULES_ODD = {
        12, 10, 8, 6, 4, 5, 7, 9, 11
    };

    private static final int[] MODULES_EVEN = {
        4, 6, 8, 10, 12, 10, 8, 6, 4
    };

    private static final int[] WIDEST_ODD = {
        8, 6, 4, 3, 1, 2, 4, 6, 8
    };

    private static final int[] WIDEST_EVEN = {
        1, 3, 5, 6, 8, 7, 5, 3, 1
    };

    private static final int[] CHECKSUM_WEIGHT = { /* Table 5 */
        1, 3, 9, 27, 2, 6, 18, 54, 4, 12, 36, 29, 8, 24, 72, 58, 16, 48, 65,
        37, 32, 17, 51, 74, 64, 34, 23, 69, 49, 68, 46, 59
    };

    private static final int[] FINDER_PATTERN = {
        3, 8, 2, 1, 1, 3, 5, 5, 1, 1, 3, 3, 7, 1, 1, 3, 1, 9, 1, 1, 2, 7, 4,
        1, 1, 2, 5, 6, 1, 1, 2, 3, 8, 1, 1, 1, 5, 7, 1, 1, 1, 3, 9, 1, 1
    };

    private boolean linkageFlag;
    private int separatorHeight = 1;
    private Mode mode;

    /**
     * Creates a new instance, using mode {@link Mode#LINEAR}.
     */
    public DataBar14() {
        this(Mode.LINEAR);
    }

    /**
     * Creates a new instance, using the specified mode.
     *
     * @param mode the symbol mode
     */
    public DataBar14(Mode mode) {
        this.mode = mode;
    }

    /** {@inheritDoc} */
    @Override
    public void setDataType(DataType dummy) {
        // Do nothing!
    }

    /**
     * Although this is a GS1 symbology, input data is expected to omit the [01] Application Identifier,
     * as well as the check digit. Thus, the input data is not considered GS1-format data.
     */
    @Override
    protected boolean gs1Supported() {
        return false;
    }

    /**
     * Sets the symbol mode. The default is {@link Mode#LINEAR}.
     *
     * @param mode the symbol mode
     */
    public void setMode(Mode mode) {
        this.mode = mode;
    }

    /**
     * Returns the symbol mode.
     *
     * @return the symbol mode
     */
    public Mode getMode() {
        return mode;
    }

    /**
     * Sets the separator height for {@link Mode#STACKED} and {@link Mode#OMNI} symbols. The default value is {@code 1}.
     *
     * @param separatorHeight the separator height for {@link Mode#STACKED} and {@link Mode#OMNI} symbols
     */
    public void setSeparatorHeight(int separatorHeight) {
        if (separatorHeight < 1) {
            throw new IllegalArgumentException("Invalid DataBar-14 Stacked separator height: " + separatorHeight);
        }
        this.separatorHeight = separatorHeight;
    }

    /**
     * Returns the separator height for {@link Mode#STACKED} and {@link Mode#OMNI} symbols.
     *
     * @return the separator height for {@link Mode#STACKED} and {@link Mode#OMNI} symbols
     */
    public int getSeparatorHeight() {
        return separatorHeight;
    }

    protected void setLinkageFlag(boolean linkageFlag) {
        this.linkageFlag = linkageFlag;
    }

    protected boolean getLinkageFlag() {
        return linkageFlag;
    }

    @Override
    protected void encode() {

        boolean[][] grid = new boolean[5][100];
        BigInteger accum;
        BigInteger left_reg;
        BigInteger right_reg;
        int[] data_character = new int[4];
        int[] data_group = new int[4];
        int[] v_odd = new int[4];
        int[] v_even = new int[4];
        int i;
        int[][] data_widths = new int[8][4];
        int checksum;
        int c_left;
        int c_right;
        int[] total_widths = new int[46];
        int writer;
        char latch;
        int j;
        int count;
        int check_digit;
        StringBuilder bin = new StringBuilder();
        int compositeOffset = 0;

        if (content.length() > 13) {
            throw OkapiInputException.inputTooLong();
        }

        if (!content.matches("[0-9]+?")) {
            throw OkapiInputException.invalidCharactersInInput();
        }

        accum = new BigInteger(content);
        if (linkageFlag) {
            accum = accum.add(new BigInteger("10000000000000"));
            compositeOffset = 1;
        }

        /* Calculate left and right pair values */
        left_reg = accum.divide(new BigInteger("4537077"));
        right_reg = accum.mod(new BigInteger("4537077"));

        /* Calculate four data characters */
        accum = left_reg.divide(new BigInteger("1597"));
        data_character[0] = accum.intValue();
        accum = left_reg.mod(new BigInteger("1597"));
        data_character[1] = accum.intValue();
        accum = right_reg.divide(new BigInteger("1597"));
        data_character[2] = accum.intValue();
        accum = right_reg.mod(new BigInteger("1597"));
        data_character[3] = accum.intValue();

        info("Data Characters: ");
        for (i = 0; i < 4; i++) {
            infoSpace(data_character[i]);
        }
        infoLine();

        /* Calculate odd and even subset values */
        if (data_character[0] >= 0 && data_character[0] <= 160) {
            data_group[0] = 0;
        }
        if (data_character[0] >= 161 && data_character[0] <= 960) {
            data_group[0] = 1;
        }
        if (data_character[0] >= 961 && data_character[0] <= 2014) {
            data_group[0] = 2;
        }
        if (data_character[0] >= 2015 && data_character[0] <= 2714) {
            data_group[0] = 3;
        }
        if (data_character[0] >= 2715 && data_character[0] <= 2840) {
            data_group[0] = 4;
        }
        if (data_character[1] >= 0 && data_character[1] <= 335) {
            data_group[1] = 5;
        }
        if (data_character[1] >= 336 && data_character[1] <= 1035) {
            data_group[1] = 6;
        }
        if (data_character[1] >= 1036 && data_character[1] <= 1515) {
            data_group[1] = 7;
        }
        if (data_character[1] >= 1516 && data_character[1] <= 1596) {
            data_group[1] = 8;
        }
        if (data_character[3] >= 0 && data_character[3] <= 335) {
            data_group[3] = 5;
        }
        if (data_character[3] >= 336 && data_character[3] <= 1035) {
            data_group[3] = 6;
        }
        if (data_character[3] >= 1036 && data_character[3] <= 1515) {
            data_group[3] = 7;
        }
        if (data_character[3] >= 1516 && data_character[3] <= 1596) {
            data_group[3] = 8;
        }
        if (data_character[2] >= 0 && data_character[2] <= 160) {
            data_group[2] = 0;
        }
        if (data_character[2] >= 161 && data_character[2] <= 960) {
            data_group[2] = 1;
        }
        if (data_character[2] >= 961 && data_character[2] <= 2014) {
            data_group[2] = 2;
        }
        if (data_character[2] >= 2015 && data_character[2] <= 2714) {
            data_group[2] = 3;
        }
        if (data_character[2] >= 2715 && data_character[2] <= 2840) {
            data_group[2] = 4;
        }

        v_odd[0] = (data_character[0] - G_SUM_TABLE[data_group[0]]) / T_TABLE[data_group[0]];
        v_even[0] = (data_character[0] - G_SUM_TABLE[data_group[0]]) % T_TABLE[data_group[0]];
        v_odd[1] = (data_character[1] - G_SUM_TABLE[data_group[1]]) % T_TABLE[data_group[1]];
        v_even[1] = (data_character[1] - G_SUM_TABLE[data_group[1]]) / T_TABLE[data_group[1]];
        v_odd[3] = (data_character[3] - G_SUM_TABLE[data_group[3]]) % T_TABLE[data_group[3]];
        v_even[3] = (data_character[3] - G_SUM_TABLE[data_group[3]]) / T_TABLE[data_group[3]];
        v_odd[2] = (data_character[2] - G_SUM_TABLE[data_group[2]]) / T_TABLE[data_group[2]];
        v_even[2] = (data_character[2] - G_SUM_TABLE[data_group[2]]) % T_TABLE[data_group[2]];

        /* Use RSS subset width algorithm */
        for (i = 0; i < 4; i++) {
            if ((i == 0) || (i == 2)) {
                int[] widths = getWidths(v_odd[i], MODULES_ODD[data_group[i]], 4, WIDEST_ODD[data_group[i]], 1);
                data_widths[0][i] = widths[0];
                data_widths[2][i] = widths[1];
                data_widths[4][i] = widths[2];
                data_widths[6][i] = widths[3];
                widths = getWidths(v_even[i], MODULES_EVEN[data_group[i]], 4, WIDEST_EVEN[data_group[i]], 0);
                data_widths[1][i] = widths[0];
                data_widths[3][i] = widths[1];
                data_widths[5][i] = widths[2];
                data_widths[7][i] = widths[3];
            } else {
                int[] widths = getWidths(v_odd[i], MODULES_ODD[data_group[i]], 4, WIDEST_ODD[data_group[i]], 0);
                data_widths[0][i] = widths[0];
                data_widths[2][i] = widths[1];
                data_widths[4][i] = widths[2];
                data_widths[6][i] = widths[3];
                widths = getWidths(v_even[i], MODULES_EVEN[data_group[i]], 4, WIDEST_EVEN[data_group[i]], 1);
                data_widths[1][i] = widths[0];
                data_widths[3][i] = widths[1];
                data_widths[5][i] = widths[2];
                data_widths[7][i] = widths[3];
            }
        }

        /* Calculate the checksum */
        checksum = 0;
        for (i = 0; i < 8; i++) {
            checksum += CHECKSUM_WEIGHT[i] * data_widths[i][0];
            checksum += CHECKSUM_WEIGHT[i + 8] * data_widths[i][1];
            checksum += CHECKSUM_WEIGHT[i + 16] * data_widths[i][2];
            checksum += CHECKSUM_WEIGHT[i + 24] * data_widths[i][3];
        }
        checksum %= 79;

        /* Calculate the two check characters */
        if (checksum >= 8) {
            checksum++;
        }
        if (checksum >= 72) {
            checksum++;
        }
        c_left = checksum / 9;
        c_right = checksum % 9;

        infoLine("Checksum: " + checksum);

        /* Put element widths together */
        total_widths[0] = 1;
        total_widths[1] = 1;
        total_widths[44] = 1;
        total_widths[45] = 1;
        for (i = 0; i < 8; i++) {
            total_widths[i + 2] = data_widths[i][0];
            total_widths[i + 15] = data_widths[7 - i][1];
            total_widths[i + 23] = data_widths[i][3];
            total_widths[i + 36] = data_widths[7 - i][2];
        }
        for (i = 0; i < 5; i++) {
            total_widths[i + 10] = FINDER_PATTERN[i + (5 * c_left)];
            total_widths[i + 31] = FINDER_PATTERN[(4 - i) + (5 * c_right)];
        }

        row_count = 0;

        boolean[] separator = new boolean[100];
        for(i = 0; i < separator.length; i++) {
            separator[i] = false;
        }

        /* Put this data into the symbol */
        if (mode == Mode.LINEAR) {
            writer = 0;
            latch = '0';
            for (i = 0; i < 46; i++) {
                for (j = 0; j < total_widths[i]; j++) {
                    if (latch == '1') {
                        grid[row_count][writer] = true;
                    }
                    writer++;
                }
                if (latch == '1') {
                    latch = '0';
                } else {
                    latch = '1';
                }
            }
            if (symbol_width < writer) {
                symbol_width = writer;
            }

            if(linkageFlag) {
                /* separator pattern for composite symbol */
                for(i = 4; i < 92; i++) {
                    separator[i] = (!(grid[0][i]));
                }
                latch = '1';
                for(i = 16; i < 32; i++) {
                    if (!grid[0][i]) {
                        if (latch == '1') {
                            separator[i] = true;
                            latch = '0';
                        } else {
                            separator[i] = false;
                            latch = '1';
                        }
                    } else {
                        separator[i] = false;
                        latch = '1';
                    }
                }
                latch = '1';
                for(i = 63; i < 78; i++) {
                    if (!grid[0][i]) {
                        if (latch == '1') {
                            separator[i] = true;
                            latch = '0';
                        } else {
                            separator[i] = false;
                            latch = '1';
                        }
                    } else {
                        separator[i] = false;
                        latch = '1';
                    }
                }
            }
            row_count = row_count + 1;

            count = 0;
            check_digit = 0;

            /* Calculate check digit from Annex A and place human readable text */
            StringBuilder hrt = new StringBuilder(14);
            for (i = content.length(); i < 13; i++) {
                hrt.append('0');
            }
            hrt.append(content);
            for (i = 0; i < 13; i++) {
                count += hrt.charAt(i) - '0';
                if ((i & 1) == 0) {
                    count += 2 * (hrt.charAt(i) - '0');
                }
            }
            check_digit = 10 - (count % 10);
            if (check_digit == 10) {
                check_digit = 0;
            }
            infoLine("Check Digit: " + check_digit);
            hrt.append((char) (check_digit + '0'));
            readable = "(01)" + hrt;
        }

        if (mode == Mode.STACKED) {
            /* top row */
            writer = 0;
            latch = '0';
            for (i = 0; i < 23; i++) {
                for (j = 0; j < total_widths[i]; j++) {
                    grid[row_count][writer] = (latch == '1');
                    writer++;
                }
                if (latch == '1') {
                    latch = '0';
                } else {
                    latch = '1';
                }
            }
            grid[row_count][writer] = true;
            grid[row_count][writer + 1] = false;

            /* bottom row */
            row_count = row_count + 2;
            grid[row_count][0] = true;
            grid[row_count][1] = false;
            writer = 0;
            latch = '1';
            for (i = 23; i < 46; i++) {
                for (j = 0; j < total_widths[i]; j++) {
                    grid[row_count][writer + 2] = (latch == '1');
                    writer++;
                }
                if (latch == '1') {
                    latch = '0';
                } else {
                    latch = '1';
                }
            }

            /* separator pattern */
            for (i = 1; i < 46; i++) {
                if (grid[row_count - 2][i] == grid[row_count][i]) {
                    if (!grid[row_count - 2][i]) {
                        grid[row_count - 1][i] = true;
                    }
                } else {
                    if (!grid[row_count - 1][i - 1]) {
                        grid[row_count - 1][i] = true;
                    }
                }
            }
            for (i = 0; i < 4; i++) {
                grid[row_count - 1][i] = false;
            }

            if (linkageFlag) {
                /* separator pattern for composite symbol */
                for(i = 4; i < 46; i++) {
                    separator[i] = !grid[0][i];
                }
                latch = '1';
                for(i = 16; i < 32; i++) {
                    if (!grid[0][i]) {
                        if (latch == '1') {
                            separator[i] = true;
                            latch = '0';
                        } else {
                            separator[i] = false;
                            latch = '1';
                        }
                    } else {
                        separator[i] = false;
                        latch = '1';
                    }
                }
            }
            row_count = row_count + 1;
            if (symbol_width < 50) {
                symbol_width = 50;
            }
        }

        if (mode == Mode.OMNI) {
            /* top row */
            writer = 0;
            latch = '0';
            for (i = 0; i < 23; i++) {
                for (j = 0; j < total_widths[i]; j++) {
                    grid[row_count][writer] = (latch == '1');
                    writer++;
                }
                latch = (latch == '1' ? '0' : '1');
            }
            grid[row_count][writer] = true;
            grid[row_count][writer + 1] = false;

            /* bottom row */
            row_count = row_count + 4;
            grid[row_count][0] = true;
            grid[row_count][1] = false;
            writer = 0;
            latch = '1';
            for (i = 23; i < 46; i++) {
                for (j = 0; j < total_widths[i]; j++) {
                    grid[row_count][writer + 2] = (latch == '1');
                    writer++;
                }
                if (latch == '1') {
                    latch = '0';
                } else {
                    latch = '1';
                }
            }

            /* middle separator */
            for (i = 5; i < 46; i += 2) {
                grid[row_count - 2][i] = true;
            }

            /* top separator */
            for (i = 4; i < 46; i++) {
                if (!grid[row_count - 4][i]) {
                    grid[row_count - 3][i] = true;
                }
            }
            latch = '1';
            for (i = 17; i < 33; i++) {
                if (!grid[row_count - 4][i]) {
                    if (latch == '1') {
                        grid[row_count - 3][i] = true;
                        latch = '0';
                    } else {
                        grid[row_count - 3][i] = false;
                        latch = '1';
                    }
                } else {
                    grid[row_count - 3][i] = false;
                    latch = '1';
                }
            }

            /* bottom separator */
            for (i = 4; i < 46; i++) {
                if (!grid[row_count][i]) {
                    grid[row_count - 1][i] = true;
                }
            }
            latch = '1';
            for (i = 16; i < 32; i++) {
                if (!grid[row_count][i]) {
                    if (latch == '1') {
                        grid[row_count - 1][i] = true;
                        latch = '0';
                    } else {
                        grid[row_count - 1][i] = false;
                        latch = '1';
                    }
                } else {
                    grid[row_count - 1][i] = false;
                    latch = '1';
                }
            }

            if (symbol_width < 50) {
                symbol_width = 50;
            }
            if (linkageFlag) {
                /* separator pattern for composite symbol */
                for (i = 4; i < 46; i++) {
                    separator[i] = !grid[0][i];
                }
                latch = '1';
                for(i = 16; i < 32; i++) {
                    if (!grid[0][i]) {
                        if (latch == '1') {
                            separator[i] = true;
                            latch = '0';
                        } else {
                            separator[i] = false;
                            latch = '1';
                        }
                    } else {
                        separator[i] = false;
                        latch = '1';
                    }
                }
            }
            row_count = row_count + 1;
        }

        pattern = new String[row_count + compositeOffset];
        row_height = new int[row_count + compositeOffset];

        if (linkageFlag) {
            bin.setLength(0);
            for (j = 0; j < symbol_width; j++) {
                if (separator[j]) {
                    bin.append('1');
                } else {
                    bin.append('0');
                }
            }
            pattern[0] = bin2pat(bin);
            row_height[0] = 1;
        }

        for (i = 0; i < row_count; i++) {
            bin.setLength(0);
            for (j = 0; j < symbol_width; j++) {
                if (grid[i][j]) {
                    bin.append('1');
                } else {
                    bin.append('0');
                }
            }
            pattern[i + compositeOffset] = bin2pat(bin);
        }

        if (mode == Mode.LINEAR) {
            row_height[0 + compositeOffset] = -1;
        }
        if (mode == Mode.STACKED) {
            row_height[0 + compositeOffset] = 5;
            row_height[1 + compositeOffset] = separatorHeight;
            row_height[2 + compositeOffset] = 7;
        }
        if (mode == Mode.OMNI) {
            row_height[0 + compositeOffset] = -1;
            row_height[1 + compositeOffset] = separatorHeight;
            row_height[2 + compositeOffset] = separatorHeight;
            row_height[3 + compositeOffset] = separatorHeight;
            row_height[4 + compositeOffset] = -1;
        }

        if (linkageFlag) {
            row_count++;
        }
    }
}
