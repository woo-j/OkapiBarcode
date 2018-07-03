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

    private enum Mode {
        LINEAR, OMNI, STACKED
    };

    private boolean linkageFlag;
    private Mode symbolType = Mode.LINEAR;
    private boolean[][] grid = new boolean[5][100];

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

    protected void setLinkageFlag() {
        linkageFlag = true;
    }

    protected void unsetLinkageFlag() {
        linkageFlag = false;
    }

    /**
     * Set symbol type to DataBar-14
     */
    public void setLinearMode() {
        symbolType = Mode.LINEAR;
    }

    /**
     * Set symbol type to DataBar-14 Omnidirectional
     */
    public void setOmnidirectionalMode() {
        symbolType = Mode.OMNI;
    }

    /**
     * Set symbol type to DataBar-14 Omnidirectional Stacked
     */
    public void setStackedMode() {
        symbolType = Mode.STACKED;
    }

    @Override
    protected void encode() {
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
        String hrt;
        String bin;
        int compositeOffset = 0;

        if (content.length() > 13) {
            throw new OkapiException("Input too long");
        }

        if (!content.matches("[0-9]+?")) {
            throw new OkapiException("Invalid characters in input");
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

        encodeInfo += "Data Characters: ";
        for (i = 0; i < 4; i++) {
            encodeInfo += Integer.toString(data_character[i]) + " ";
        }
        encodeInfo += "\n";

//        if (debug) {
//            System.out.println("left " + left_reg.toString());
//            System.out.println("right " + right_reg.toString());
//            System.out.println("data1 " + data_character[0]);
//            System.out.println("data2 " + data_character[1]);
//            System.out.println("data3 " + data_character[2]);
//            System.out.println("data4 " + data_character[3]);
//        }

        /* Calculate odd and even subset values */
        if ((data_character[0] >= 0) && (data_character[0] <= 160)) {
            data_group[0] = 0;
        }
        if ((data_character[0] >= 161) && (data_character[0] <= 960)) {
            data_group[0] = 1;
        }
        if ((data_character[0] >= 961) && (data_character[0] <= 2014)) {
            data_group[0] = 2;
        }
        if ((data_character[0] >= 2015) && (data_character[0] <= 2714)) {
            data_group[0] = 3;
        }
        if ((data_character[0] >= 2715) && (data_character[0] <= 2840)) {
            data_group[0] = 4;
        }
        if ((data_character[1] >= 0) && (data_character[1] <= 335)) {
            data_group[1] = 5;
        }
        if ((data_character[1] >= 336) && (data_character[1] <= 1035)) {
            data_group[1] = 6;
        }
        if ((data_character[1] >= 1036) && (data_character[1] <= 1515)) {
            data_group[1] = 7;
        }
        if ((data_character[1] >= 1516) && (data_character[1] <= 1596)) {
            data_group[1] = 8;
        }
        if ((data_character[3] >= 0) && (data_character[3] <= 335)) {
            data_group[3] = 5;
        }
        if ((data_character[3] >= 336) && (data_character[3] <= 1035)) {
            data_group[3] = 6;
        }
        if ((data_character[3] >= 1036) && (data_character[3] <= 1515)) {
            data_group[3] = 7;
        }
        if ((data_character[3] >= 1516) && (data_character[3] <= 1596)) {
            data_group[3] = 8;
        }
        if ((data_character[2] >= 0) && (data_character[2] <= 160)) {
            data_group[2] = 0;
        }
        if ((data_character[2] >= 161) && (data_character[2] <= 960)) {
            data_group[2] = 1;
        }
        if ((data_character[2] >= 961) && (data_character[2] <= 2014)) {
            data_group[2] = 2;
        }
        if ((data_character[2] >= 2015) && (data_character[2] <= 2714)) {
            data_group[2] = 3;
        }
        if ((data_character[2] >= 2715) && (data_character[2] <= 2840)) {
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

//        if (debug) {
//            for (i = 0; i < 4; i++) {
//                System.out.println("Vodd[" + i + "] = " + v_odd[i] + "  Veven[" + i + "] = " + v_even[i]);
//            }
//        }

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

//        if (debug) {
//            for (i = 0; i < 4; i++) {
//                System.out.print("Data " + i + " widths ");
//                for(j = 0; j < 8; j++) {
//                    System.out.print(data_widths[j][i]);
//                }
//                System.out.println();
//            }
//        }

        checksum = 0;
        /* Calculate the checksum */
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

        encodeInfo += "Checksum: " + Integer.toString(checksum) + "\n";

//        if (debug) {
//            System.out.println("checksum " + checksum);
//            System.out.println("left check " + c_left);
//            System.out.println("right check " + c_right);
//        }

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
        if (symbolType == Mode.LINEAR) {
            writer = 0;
            latch = '0';
            for (i = 0; i < 46; i++) {
                for (j = 0; j < total_widths[i]; j++) {
                    if (latch == '1') {
                        setGridModule(row_count, writer);
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
                    if (!(grid[0][i])) {
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
                    if (!(grid[0][i])) {
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
            readable = "(01)";
            hrt = "";
            for (i = content.length(); i < 13; i++) {
                hrt += "0";
            }
            hrt += content;

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
            hrt += (char)(check_digit + '0');

            readable += hrt;
        }

        if (symbolType == Mode.STACKED) {
            /* top row */
            writer = 0;
            latch = '0';
            for (i = 0; i < 23; i++) {
                for (j = 0; j < total_widths[i]; j++) {
                    if (latch == '1') {
                        setGridModule(row_count, writer);
                    } else {
                        unsetGridModule(row_count, writer);
                    }
                    writer++;
                }
                if (latch == '1') {
                    latch = '0';
                } else {
                    latch = '1';
                }
            }
            setGridModule(row_count, writer);
            unsetGridModule(row_count, writer + 1);

            /* bottom row */
            row_count = row_count + 2;
            setGridModule(row_count, 0);
            unsetGridModule(row_count, 1);
            writer = 0;
            latch = '1';
            for (i = 23; i < 46; i++) {
                for (j = 0; j < total_widths[i]; j++) {
                    if (latch == '1') {
                        setGridModule(row_count, writer + 2);
                    } else {
                        unsetGridModule(row_count, writer + 2);
                    }
                    writer++;
                }
                if (latch == '1') {
                    latch = '0';
                } else {
                    latch = '1';
                }
            }

            /* separator pattern */
            for (i = 4; i < 46; i++) {
                if (gridModuleIsSet(row_count - 2, i) == gridModuleIsSet(row_count, i)) {
                    if (!(gridModuleIsSet(row_count - 2, i))) {
                        setGridModule(row_count - 1, i);
                    }
                } else {
                    if (!(gridModuleIsSet(row_count - 1, i - 1))) {
                        setGridModule(row_count - 1, i);
                    }
                }
            }

            if(linkageFlag) {
                /* separator pattern for composite symbol */
                for(i = 4; i < 46; i++) {
                    separator[i] = (!(grid[0][i]));
                }
                latch = '1';
                for(i = 16; i < 32; i++) {
                    if (!(grid[0][i])) {
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

        if (symbolType == Mode.OMNI) {
            /* top row */
            writer = 0;
            latch = '0';
            for (i = 0; i < 23; i++) {
                for (j = 0; j < total_widths[i]; j++) {
                    if (latch == '1') {
                        setGridModule(row_count, writer);
                    } else {
                        unsetGridModule(row_count, writer);
                    }
                    writer++;
                }
                latch = (latch == '1' ? '0' : '1');
            }
            setGridModule(row_count, writer);
            unsetGridModule(row_count, writer + 1);

            /* bottom row */
            row_count = row_count + 4;
            setGridModule(row_count, 0);
            unsetGridModule(row_count, 1);
            writer = 0;
            latch = '1';
            for (i = 23; i < 46; i++) {
                for (j = 0; j < total_widths[i]; j++) {
                    if (latch == '1') {
                        setGridModule(row_count, writer + 2);
                    } else {
                        unsetGridModule(row_count, writer + 2);
                    }
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
                setGridModule(row_count - 2, i);
            }

            /* top separator */
            for (i = 4; i < 46; i++) {
                if (!(gridModuleIsSet(row_count - 4, i))) {
                    setGridModule(row_count - 3, i);
                }
            }
            latch = '1';
            for (i = 17; i < 33; i++) {
                if (!(gridModuleIsSet(row_count - 4, i))) {
                    if (latch == '1') {
                        setGridModule(row_count - 3, i);
                        latch = '0';
                    } else {
                        unsetGridModule(row_count - 3, i);
                        latch = '1';
                    }
                } else {
                    unsetGridModule(row_count - 3, i);
                    latch = '1';
                }
            }

            /* bottom separator */
            for (i = 4; i < 46; i++) {
                if (!(gridModuleIsSet(row_count, i))) {
                    setGridModule(row_count - 1, i);
                }
            }
            latch = '1';
            for (i = 16; i < 32; i++) {
                if (!(gridModuleIsSet(row_count, i))) {
                    if (latch == '1') {
                        setGridModule(row_count - 1, i);
                        latch = '0';
                    } else {
                        unsetGridModule(row_count - 1, i);
                        latch = '1';
                    }
                } else {
                    unsetGridModule(row_count - 1, i);
                    latch = '1';
                }
            }

            if (symbol_width < 50) {
                symbol_width = 50;
            }
            if(linkageFlag) {
                /* separator pattern for composite symbol */
                for(i = 4; i < 46; i++) {
                    separator[i] = (!(grid[0][i]));
                }
                latch = '1';
                for(i = 16; i < 32; i++) {
                    if (!(grid[0][i])) {
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
            bin = "";
            for (j = 0; j < symbol_width; j++) {
                if (separator[j]) {
                    bin += "1";
                } else {
                    bin += "0";
                }
            }
            pattern[0] = bin2pat(bin);
            row_height[0] = 1;
        }

        for (i = 0; i < row_count; i++) {
            bin = "";
            for (j = 0; j < symbol_width; j++) {
                if (grid[i][j]) {
                    bin += "1";
                } else {
                    bin += "0";
                }
            }
            pattern[i + compositeOffset] = bin2pat(bin);
        }

        if (symbolType == Mode.LINEAR) {
            row_height[0 + compositeOffset] = -1;
        }
        if (symbolType == Mode.STACKED) {
            row_height[0 + compositeOffset] = 5;
            row_height[1 + compositeOffset] = 1;
            row_height[2 + compositeOffset] = 7;
        }
        if (symbolType == Mode.OMNI) {
            row_height[0 + compositeOffset] = -1;
            row_height[1 + compositeOffset] = 1;
            row_height[2 + compositeOffset] = 1;
            row_height[3 + compositeOffset] = 1;
            row_height[4 + compositeOffset] = -1;
        }

        if (linkageFlag) {
            row_count++;
        }
    }

    private void setGridModule(int row, int column) {
        grid[row][column] = true;
    }

    private void unsetGridModule(int row, int column) {
        grid[row][column] = false;
    }

    private boolean gridModuleIsSet(int row, int column) {
        return grid[row][column];
    }
}
