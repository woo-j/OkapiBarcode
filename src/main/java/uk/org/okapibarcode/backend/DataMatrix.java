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

/**
 * Implements Data Matrix ECC 200 bar code symbology According to ISO/IEC
 * 16022:2006
 * <p>
 * Data Matrix is a 2D matrix symbology capable of encoding characters in the
 * ISO/IEC 8859-1 (Latin-1) character set.
 *
 * @author <a href="mailto:rstuart114@gmail.com">Robin Stuart</a>
 */
public class DataMatrix extends Symbol {

    private static final int[] C40_SHIFT = {
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 2, 2, 2, 2, 2, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 2, 2, 2,
        3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
        3, 3, 3, 3, 3, 3, 3, 3
    };

    private static final int[] C40_VALUE = {
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
        20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 3, 0, 1, 2, 3, 4, 5, 6,
        7, 8, 9, 10, 11, 12, 13, 14, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 15, 16,
        17, 18, 19, 20, 21, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26,
        27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 22, 23, 24, 25, 26,
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
        20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31
    };

    private static final int[] TEXT_SHIFT = {
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3,
        3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 2, 2, 2, 2, 2,
        3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 3, 3, 3, 3, 3
    };

    private static final int[] TEXT_VALUE = {
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
        20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 3, 0, 1, 2, 3, 4, 5, 6,
        7, 8, 9, 10, 11, 12, 13, 14, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 15, 16,
        17, 18, 19, 20, 21, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
        16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 22, 23, 24, 25, 26, 0, 14,
        15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32,
        33, 34, 35, 36, 37, 38, 39, 27, 28, 29, 30, 31
    };

    private static final int[] INT_SYMBOL = {
        0, 1, 3, 5, 7, 8, 10, 12, 13, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24,
        25, 26, 27, 28, 29, 2, 4, 6, 9, 11, 14
    };

    private static final int[] MATRIX_H = {
        10, 12, 8, 14, 8, 16, 12, 18, 20, 12, 22, 16, 24, 26, 16, 32, 36, 40,
        44, 48, 52, 64, 72, 80, 88, 96, 104, 120, 132, 144
    };

    private static final int[] MATRIX_W = {
        10, 12, 18, 14, 32, 16, 26, 18, 20, 36, 22, 36, 24, 26, 48, 32, 36, 40,
        44, 48, 52, 64, 72, 80, 88, 96, 104, 120, 132, 144
    };

    private static final int[] MATRIX_FH = {
        10, 12, 8, 14, 8, 16, 12, 18, 20, 12, 22, 16, 24, 26, 16, 16, 18, 20,
        22, 24, 26, 16, 18, 20, 22, 24, 26, 20, 22, 24
    };

    private static final int[] MATRIX_FW = {
        10, 12, 18, 14, 16, 16, 26, 18, 20, 18, 22, 18, 24, 26, 24, 16, 18, 20,
        22, 24, 26, 16, 18, 20, 22, 24, 26, 20, 22, 24
    };

    private static final int[] MATRIX_BYTES = {
        3, 5, 5, 8, 10, 12, 16, 18, 22, 22, 30, 32, 36, 44, 49, 62, 86, 114,
        144, 174, 204, 280, 368, 456, 576, 696, 816, 1050, 1304, 1558
    };

    private static final int[] MATRIX_DATA_BLOCK = {
        3, 5, 5, 8, 10, 12, 16, 18, 22, 22, 30, 32, 36, 44, 49, 62, 86, 114,
        144, 174, 102, 140, 92, 114, 144, 174, 136, 175, 163, 156
    };

    private static final int[] MATRIX_RS_BLOCK = {
        5, 7, 7, 10, 11, 12, 14, 14, 18, 18, 20, 24, 24, 28, 28, 36, 42, 48, 56,
        68, 42, 56, 36, 48, 56, 68, 56, 68, 62, 62
    };

    private enum Mode {
        NULL, DM_ASCII, DM_C40, DM_TEXT, DM_X12, DM_EDIFACT, DM_BASE256
    }

    private int[] target = new int[2200];
    private int[] binary = new int[2200];
    private int binary_length;
    private Mode last_mode;
    private int[] places;
    private boolean isSquare = true;
    private int[] inputData;
    private int preferredSize = 0;
    private int process_p;
    private int[] process_buffer = new int[8];
    private int codewordCount;

    /**
     * Override selection of symbol size. When set as <code>false</code> the
     * symbol will be the smallest available for the amount of data given. When
     * set as <code>true</code> the encoding will not use rectangular symbols.
     *
     * @param input Forces a square symbol when set to <code>true</code>
     */
    public void forceSquare(boolean input) {
        isSquare = input;
    }

    /**
     * Set the prefereed symbol size according to the values in the following
     * table. Values may be ignored if the data is too big to fit in the
     * specified symbol, or if <code>forceSquare</code> mode has been invoked.
     * <table summary="Available Data Matrix symbol sizes">
     * <tbody>
     * <tr>
     * <th><p>
     * Input</p></th>
     * <th><p>
     * Symbol Size</p></th>
     * <th><p>
     * Input</p></th>
     * <th><p>
     * Symbol Size</p></th>
     * </tr>
     * <tr>
     * <td><p>
     * 1</p></td>
     * <td><p>
     * 10 x 10</p></td>
     * <td><p>
     * 16</p></td>
     * <td><p>
     * 64 x 64</p></td>
     * </tr>
     * <tr>
     * <td><p>
     * 2</p></td>
     * <td><p>
     * 12 x 12</p></td>
     * <td><p>
     * 17</p></td>
     * <td><p>
     * 72 x 72</p></td>
     * </tr>
     * <tr>
     * <td><p>
     * 3</p></td>
     * <td><p>
     * 14 x 14</p></td>
     * <td><p>
     * 18</p></td>
     * <td><p>
     * 80 x 80</p></td>
     * </tr>
     * <tr>
     * <td><p>
     * 4</p></td>
     * <td><p>
     * 16 x 16</p></td>
     * <td><p>
     * 19</p></td>
     * <td><p>
     * 88 x 88</p></td>
     * </tr>
     * <tr>
     * <td><p>
     * 5</p></td>
     * <td><p>
     * 18 x 18</p></td>
     * <td><p>
     * 20</p></td>
     * <td><p>
     * 96 x 96</p></td>
     * </tr>
     * <tr>
     * <td><p>
     * 6</p></td>
     * <td><p>
     * 20 x 20</p></td>
     * <td><p>
     * 21</p></td>
     * <td><p>
     * 104 x 104</p></td>
     * </tr>
     * <tr>
     * <td><p>
     * 7</p></td>
     * <td><p>
     * 22 x 22</p></td>
     * <td><p>
     * 22</p></td>
     * <td><p>
     * 120 x 120</p></td>
     * </tr>
     * <tr>
     * <td><p>
     * 8</p></td>
     * <td><p>
     * 24 x 24</p></td>
     * <td><p>
     * 23</p></td>
     * <td><p>
     * 132 x 132</p></td>
     * </tr>
     * <tr>
     * <td><p>
     * 9</p></td>
     * <td><p>
     * 26 x 26</p></td>
     * <td><p>
     * 24</p></td>
     * <td><p>
     * 144 x 144</p></td>
     * </tr>
     * <tr>
     * <td><p>
     * 10</p></td>
     * <td><p>
     * 32 x 32</p></td>
     * <td><p>
     * 25</p></td>
     * <td><p>
     * 8 x 18</p></td>
     * </tr>
     * <tr>
     * <td><p>
     * 11</p></td>
     * <td><p>
     * 36 x 36</p></td>
     * <td><p>
     * 26</p></td>
     * <td><p>
     * 8 x 32</p></td>
     * </tr>
     * <tr>
     * <td><p>
     * 12</p></td>
     * <td><p>
     * 40 x 40</p></td>
     * <td><p>
     * 27</p></td>
     * <td><p>
     * 12 x 26</p></td>
     * </tr>
     * <tr>
     * <td><p>
     * 13</p></td>
     * <td><p>
     * 44 x 44</p></td>
     * <td><p>
     * 28</p></td>
     * <td><p>
     * 12 x 36</p></td>
     * </tr>
     * <tr>
     * <td><p>
     * 14</p></td>
     * <td><p>
     * 48 x 48</p></td>
     * <td><p>
     * 29</p></td>
     * <td><p>
     * 16 x 36</p></td>
     * </tr>
     * <tr>
     * <td><p>
     * 15</p></td>
     * <td><p>
     * 52 x 52</p></td>
     * <td><p>
     * 30</p></td>
     * <td><p>
     * 16 x 48</p></td>
     * </tr>
     * </tbody>
     * </table>
     *
     * @param size Symbol size
     */
    public void setPreferredSize(int size) {
        preferredSize = size;
    }

    @Override
    protected void encode() {

        int i, binlen, skew = 0;
        int symbolsize, optionsize, calcsize;
        int taillength;
        int H, W, FH, FW, datablock, bytes, rsblock;
        int x, y, NC, NR, v;
        int[] grid;
        String bin;

        eciProcess(); // Get ECI mode

        inputData = new int[content.length()];
        for (i = 0; i < content.length(); i++) {
            inputData[i] = inputBytes[i] & 0xFF;
        }

        binlen = generateCodewords();

        if (preferredSize >= 1 && preferredSize <= 30) {
            optionsize = INT_SYMBOL[preferredSize - 1];
        } else {
            optionsize = -1;
        }

        calcsize = 29;
        for (i = 29; i > -1; i--) {
            if (MATRIX_BYTES[i] >= (binlen + process_p)) {
                calcsize = i;
            }
        }

        if (isSquare) {
            // Force to use square symbol
            switch (calcsize) {
                case 2:
                case 4:
                case 6:
                case 9:
                case 11:
                case 14:
                    calcsize++;
                    break;
                default:
                    break;
            }
        }

        symbolsize = optionsize;
        if (calcsize > optionsize) {
            symbolsize = calcsize;
            if (optionsize != -1) {
                /* flag an error */
                throw new OkapiException("Data does not fit in selected symbol size");
            }
        }

        // Now we know the symbol size we can handle the remaining data in the process buffer.
        if (process_p != 0) {
            binlen = encodeRemainder(MATRIX_BYTES[symbolsize] - binlen, binlen);
        }

        H = MATRIX_H[symbolsize];
        W = MATRIX_W[symbolsize];
        FH = MATRIX_FH[symbolsize];
        FW = MATRIX_FW[symbolsize];
        bytes = MATRIX_BYTES[symbolsize];
        datablock = MATRIX_DATA_BLOCK[symbolsize];
        rsblock = MATRIX_RS_BLOCK[symbolsize];

        codewordCount = datablock + rsblock; // data codewords + error correction codewords

        taillength = bytes - binlen;

        if (taillength != 0) {
            addPadBits(binlen, taillength);
        }

        // ecc code
        if (symbolsize == 29) {
            skew = 1;
        }
        calculateErrorCorrection(bytes, datablock, rsblock, skew);
        NC = W - 2 * (W / FW);
        NR = H - 2 * (H / FH);
        places = new int[NC * NR];
        placeData(NR, NC);
        grid = new int[W * H];
        for (i = 0; i < (W * H); i++) {
            grid[i] = 0;
        }
        for (y = 0; y < H; y += FH) {
            for (x = 0; x < W; x++) {
                grid[y * W + x] = 1;
            }
            for (x = 0; x < W; x += 2) {
                grid[(y + FH - 1) * W + x] = 1;
            }
        }
        for (x = 0; x < W; x += FW) {
            for (y = 0; y < H; y++) {
                grid[y * W + x] = 1;
            }
            for (y = 0; y < H; y += 2) {
                grid[y * W + x + FW - 1] = 1;
            }
        }
        for (y = 0; y < NR; y++) {
            for (x = 0; x < NC; x++) {
                v = places[(NR - y - 1) * NC + x];
                if (v == 1 || (v > 7 && (target[(v >> 3) - 1] & (1 << (v & 7))) != 0)) {
                    grid[(1 + y + 2 * (y / (FH - 2))) * W + 1 + x + 2 * (x / (FW - 2))] = 1;
                }
            }
        }

        readable = "";
        pattern = new String[H];
        row_count = H;
        row_height = new int[H];
        for (y = H - 1; y >= 0; y--) {
            bin = "";
            for (x = 0; x < W; x++) {
                if (grid[W * y + x] == 1) {
                    bin += "1";
                } else {
                    bin += "0";
                }
            }
            pattern[(H - y) - 1] = bin2pat(bin);
            row_height[(H - y) - 1] = 1;
        }

        encodeInfo += "Grid Size: " + W + " X " + H + "\n";
        encodeInfo += "Data Codewords: " + datablock + "\n";
        encodeInfo += "ECC Codewords: " + rsblock + "\n";
    }

    @Override
    protected int[] getCodewords() {
        return Arrays.copyOf(target, codewordCount);
    }

    private int generateCodewords() {
        /* Encodes data using ASCII, C40, Text, X12, EDIFACT or Base 256 modes as appropriate */
        /* Supports encoding FNC1 in supporting systems */
        /* Supports ECI encoding for whole message only, not inline switching */

        encodeInfo += "Encoding: ";
        int sp, tp, i;
        Mode current_mode, next_mode;
        int inputlen = content.length();

        sp = 0;
        tp = 0;
        process_p = 0;

        for (i = 0; i < 8; i++) {
            process_buffer[i] = 0;
        }
        binary_length = 0;

        /* step (a) */
        current_mode = Mode.DM_ASCII;
        next_mode = Mode.DM_ASCII;

        if (inputDataType == DataType.GS1) {
            target[tp] = 232;
            tp++;
            binary[binary_length] = ' ';
            binary_length++;
            encodeInfo += "FNC1 ";
        } /* FNC1 */

        if (eciMode != 3) {
            target[tp] = 241; // ECI
            tp++;

            if (eciMode <= 126) {
                target[tp] = eciMode + 1;
                tp++;
            }

            if ((eciMode >= 127) && (eciMode <= 16382)) {
                target[tp] = ((eciMode - 127) / 254) + 128;
                tp++;
                target[tp] = ((eciMode - 127) % 254) + 1;
                tp++;
            }

            if (eciMode >= 16383) {
                target[tp] = ((eciMode - 16383) / 64516) + 192;
                tp++;
                target[tp] = (((eciMode - 16383) / 254) % 254) + 1;
                tp++;
                target[tp] = ((eciMode - 16383) & 254) + 1;
                tp++;
            }

            encodeInfo += "ECI ";
        }

        if (readerInit) {
            if (inputDataType == DataType.GS1) {
                throw new OkapiException("Cannot encode in GS1 mode and Reader Initialisation at the same time");
            } else {
                target[tp] = 234;
                tp++; /* Reader Programming */

                binary[binary_length] = ' ';
                binary_length++;
                encodeInfo += "RP ";
            }
        }

        /* Check for Macro05/Macro06 */
        /* "[)>[RS]05[GS]...[RS][EOT]" -> CW 236 */
        /* "[)>[RS]06[GS]...[RS][EOT]" -> CW 237 */
        if (tp == 0 & sp == 0 && inputlen >= 9) {
            if (inputData[0] == '[' && inputData[1] == ')' && inputData[2] == '>'
                    && inputData[3] == '\u001e' && inputData[4] == '0'
                    && (inputData[5] == '5' || inputData[5] == '6')
                    && inputData[6] == '\u001d'
                    && inputData[inputlen - 2] == '\u001e'
                    && inputData[inputlen - 1] == '\u0004') {
                /* Output macro Codeword */
                if (inputData[5] == '5') {
                    target[tp] = 236;
                    encodeInfo += "Micro05 ";
                } else {
                    target[tp] = 237;
                    encodeInfo += "Macro06 ";
                }
                tp++;
                binary[binary_length] = ' ';
                binary_length++;
                /* Remove macro characters from input string */
                sp = 7;
                inputlen -= 2;
            }
        }

        while (sp < inputlen) {

            current_mode = next_mode;

            /* step (b) - ASCII encodation */
            if (current_mode == Mode.DM_ASCII) {
                next_mode = Mode.DM_ASCII;

                for (i = 0; i < 8; i++) {
                    process_buffer[i] = 0;
                }

                if (isTwoDigits(sp)) {
                    target[tp] = (10 * Character.getNumericValue(inputData[sp]))
                            + Character.getNumericValue(inputData[sp + 1]) + 130;
                    encodeInfo += Integer.toString(target[tp] - 130) + " ";
                    tp++;
                    binary[binary_length] = ' ';
                    binary_length++;
                    sp += 2;
                } else {
                    next_mode = lookAheadTest(sp, current_mode);

                    if (next_mode != Mode.DM_ASCII) {
                        switch (next_mode) {
                            case DM_C40:
                                target[tp] = 230;
                                tp++;
                                binary[binary_length] = ' ';
                                binary_length++;
                                encodeInfo += "C40 ";
                                break;
                            case DM_TEXT:
                                target[tp] = 239;
                                tp++;
                                binary[binary_length] = ' ';
                                binary_length++;
                                encodeInfo += "TEX ";
                                break;
                            case DM_X12:
                                target[tp] = 238;
                                tp++;
                                binary[binary_length] = ' ';
                                binary_length++;
                                encodeInfo += "X12 ";
                                break;
                            case DM_EDIFACT:
                                target[tp] = 240;
                                tp++;
                                binary[binary_length] = ' ';
                                binary_length++;
                                encodeInfo += "EDI ";
                                break;
                            case DM_BASE256:
                                target[tp] = 231;
                                tp++;
                                binary[binary_length] = ' ';
                                binary_length++;
                                encodeInfo += "BAS ";
                                break;
                        }
                    } else {
                        if (inputData[sp] > 127) {
                            target[tp] = 235; /* FNC4 */

                            encodeInfo += "FNC4 ";
                            tp++;
                            target[tp] = (inputData[sp] - 128) + 1;
                            encodeInfo += Integer.toString(target[tp] - 1) + " ";
                            tp++;
                            binary[binary_length] = ' ';
                            binary_length++;
                            binary[binary_length] = ' ';
                            binary_length++;
                        } else {
                            if ((inputDataType == DataType.GS1) && (inputData[sp] == '[')) {
                                target[tp] = 232; /* FNC1 */
                                encodeInfo += "FNC1 ";
                            } else {
                                target[tp] = inputData[sp] + 1;
                                encodeInfo += Integer.toString(target[tp] - 1) + " ";
                            }
                            tp++;
                            binary[binary_length] = ' ';
                            binary_length++;
                        }
                        sp++;
                    }
                }

            }

            /* step (c) C40 encodation */
            if (current_mode == Mode.DM_C40) {
                int shift_set, value;

                next_mode = Mode.DM_C40;
                if (process_p == 0) {
                    next_mode = lookAheadTest(sp, current_mode);
                }

                if (next_mode != Mode.DM_C40) {
                    target[tp] = 254;
                    tp++;
                    binary[binary_length] = ' ';
                    binary_length++; /* Unlatch */

                    next_mode = Mode.DM_ASCII;
                    encodeInfo += "ASC ";
                } else {
                    if (inputData[sp] > 127) {
                        process_buffer[process_p] = 1;
                        process_p++;
                        process_buffer[process_p] = 30;
                        process_p++; /* Upper Shift */

                        shift_set = C40_SHIFT[inputData[sp] - 128];
                        value = C40_VALUE[inputData[sp] - 128];
                    } else {
                        shift_set = C40_SHIFT[inputData[sp]];
                        value = C40_VALUE[inputData[sp]];
                    }

                    if ((inputDataType == DataType.GS1) && (inputData[sp] == '[')) {
                        shift_set = 2;
                        value = 27; /* FNC1 */

                    }

                    if (shift_set != 0) {
                        process_buffer[process_p] = shift_set - 1;
                        process_p++;
                    }
                    process_buffer[process_p] = value;
                    process_p++;

                    if (process_p >= 3) {
                        int iv;

                        iv = (1600 * process_buffer[0]) + (40 * process_buffer[1])
                                + (process_buffer[2]) + 1;
                        target[tp] = iv / 256;
                        tp++;
                        target[tp] = iv % 256;
                        tp++;
                        binary[binary_length] = ' ';
                        binary_length++;
                        binary[binary_length] = ' ';
                        binary_length++;
                        encodeInfo += "(" + Integer.toString(process_buffer[0]) +
                                " " + Integer.toString(process_buffer[1]) + " " +
                                Integer.toString(process_buffer[2]) + ") ";

                        process_buffer[0] = process_buffer[3];
                        process_buffer[1] = process_buffer[4];
                        process_buffer[2] = process_buffer[5];
                        process_buffer[3] = 0;
                        process_buffer[4] = 0;
                        process_buffer[5] = 0;
                        process_p -= 3;
                    }
                    sp++;
                }
            }

            /* step (d) Text encodation */
            if (current_mode == Mode.DM_TEXT) {
                int shift_set, value;

                next_mode = Mode.DM_TEXT;
                if (process_p == 0) {
                    next_mode = lookAheadTest(sp, current_mode);
                }

                if (next_mode != Mode.DM_TEXT) {
                    target[tp] = 254;
                    tp++;
                    binary[binary_length] = ' ';
                    binary_length++; /* Unlatch */

                    next_mode = Mode.DM_ASCII;
                    encodeInfo += "ASC ";
                } else {
                    if (inputData[sp] > 127) {
                        process_buffer[process_p] = 1;
                        process_p++;
                        process_buffer[process_p] = 30;
                        process_p++; /* Upper Shift */

                        shift_set = TEXT_SHIFT[inputData[sp] - 128];
                        value = TEXT_VALUE[inputData[sp] - 128];
                    } else {
                        shift_set = TEXT_SHIFT[inputData[sp]];
                        value = TEXT_VALUE[inputData[sp]];
                    }

                    if ((inputDataType == DataType.GS1) && (inputData[sp] == '[')) {
                        shift_set = 2;
                        value = 27; /* FNC1 */

                    }

                    if (shift_set != 0) {
                        process_buffer[process_p] = shift_set - 1;
                        process_p++;
                    }
                    process_buffer[process_p] = value;
                    process_p++;

                    if (process_p >= 3) {
                        int iv;

                        iv = (1600 * process_buffer[0]) + (40 * process_buffer[1])
                                + (process_buffer[2]) + 1;
                        target[tp] = iv / 256;
                        tp++;
                        target[tp] = iv % 256;
                        tp++;
                        binary[binary_length] = ' ';
                        binary_length++;
                        binary[binary_length] = ' ';
                        binary_length++;
                        encodeInfo += "(" + Integer.toString(process_buffer[0]) +
                                " " + Integer.toString(process_buffer[1]) + " " +
                                Integer.toString(process_buffer[2]) + ") ";

                        process_buffer[0] = process_buffer[3];
                        process_buffer[1] = process_buffer[4];
                        process_buffer[2] = process_buffer[5];
                        process_buffer[3] = 0;
                        process_buffer[4] = 0;
                        process_buffer[5] = 0;
                        process_p -= 3;
                    }
                    sp++;
                }
            }

            /* step (e) X12 encodation */
            if (current_mode == Mode.DM_X12) {
                int value = 0;

                next_mode = Mode.DM_X12;
                if (process_p == 0) {
                    next_mode = lookAheadTest(sp, current_mode);
                }

                if (next_mode != Mode.DM_X12) {
                    target[tp] = 254;
                    tp++;
                    binary[binary_length] = ' ';
                    binary_length++; /* Unlatch */

                    next_mode = Mode.DM_ASCII;
                    encodeInfo += "ASC ";
                } else {
                    if (inputData[sp] == 13) {
                        value = 0;
                    }
                    if (inputData[sp] == '*') {
                        value = 1;
                    }
                    if (inputData[sp] == '>') {
                        value = 2;
                    }
                    if (inputData[sp] == ' ') {
                        value = 3;
                    }
                    if ((inputData[sp] >= '0') && (inputData[sp] <= '9')) {
                        value = (inputData[sp] - '0') + 4;
                    }
                    if ((inputData[sp] >= 'A') && (inputData[sp] <= 'Z')) {
                        value = (inputData[sp] - 'A') + 14;
                    }

                    process_buffer[process_p] = value;
                    process_p++;

                    if (process_p >= 3) {
                        int iv;

                        iv = (1600 * process_buffer[0]) + (40 * process_buffer[1])
                                + (process_buffer[2]) + 1;
                        target[tp] = iv / 256;
                        tp++;
                        target[tp] = iv % 256;
                        tp++;
                        binary[binary_length] = ' ';
                        binary_length++;
                        binary[binary_length] = ' ';
                        binary_length++;
                        encodeInfo += "(" + Integer.toString(process_buffer[0]) +
                                " " + Integer.toString(process_buffer[1]) + " " +
                                Integer.toString(process_buffer[2]) + ") ";

                        process_buffer[0] = process_buffer[3];
                        process_buffer[1] = process_buffer[4];
                        process_buffer[2] = process_buffer[5];
                        process_buffer[3] = 0;
                        process_buffer[4] = 0;
                        process_buffer[5] = 0;
                        process_p -= 3;
                    }
                    sp++;
                }
            }

            /* step (f) EDIFACT encodation */
            if (current_mode == Mode.DM_EDIFACT) {
                int value = 0;

                next_mode = Mode.DM_EDIFACT;
                if (process_p == 3) {
                    next_mode = lookAheadTest(sp, current_mode);
                }

                if (next_mode != Mode.DM_EDIFACT) {
                    process_buffer[process_p] = 31;
                    process_p++;
                    next_mode = Mode.DM_ASCII;
                } else {
                    if ((inputData[sp] >= '@') && (inputData[sp] <= '^')) {
                        value = inputData[sp] - '@';
                    }
                    if ((inputData[sp] >= ' ') && (inputData[sp] <= '?')) {
                        value = inputData[sp];
                    }

                    process_buffer[process_p] = value;
                    process_p++;
                    sp++;
                }

                if (process_p >= 4) {
                    target[tp] = (process_buffer[0] << 2)
                            + ((process_buffer[1] & 0x30) >> 4);
                    tp++;
                    target[tp] = ((process_buffer[1] & 0x0f) << 4)
                            + ((process_buffer[2] & 0x3c) >> 2);
                    tp++;
                    target[tp] = ((process_buffer[2] & 0x03) << 6)
                            + process_buffer[3];
                    tp++;
                    binary[binary_length] = ' ';
                    binary_length++;
                    binary[binary_length] = ' ';
                    binary_length++;
                    binary[binary_length] = ' ';
                    binary_length++;
                    encodeInfo += "(" + Integer.toString(process_buffer[0]) +
                            " " + Integer.toString(process_buffer[1]) + " " +
                            Integer.toString(process_buffer[2]) + ") ";

                    process_buffer[0] = process_buffer[4];
                    process_buffer[1] = process_buffer[5];
                    process_buffer[2] = process_buffer[6];
                    process_buffer[3] = process_buffer[7];
                    process_buffer[4] = 0;
                    process_buffer[5] = 0;
                    process_buffer[6] = 0;
                    process_buffer[7] = 0;
                    process_p -= 4;
                }
            }

            /* step (g) Base 256 encodation */
            if (current_mode == Mode.DM_BASE256) {
                next_mode = lookAheadTest(sp, current_mode);

                if (next_mode == Mode.DM_BASE256) {
                    target[tp] = inputData[sp];
                    encodeInfo += Integer.toString(target[tp]) + " ";
                    tp++;
                    sp++;
                    binary[binary_length] = 'b';
                    binary_length++;
                } else {
                    next_mode = Mode.DM_ASCII;
                    encodeInfo += "ASC ";
                }
            }

            if (tp > 1558) {
                throw new OkapiException("Data too long to fit in symbol");
            }

        } /* while */

        /* Add length and randomising algorithm to b256 */
        i = 0;
        while (i < tp) {
            if (binary[i] == 'b') {
                if ((i == 0) || ((i != 0) && (binary[i - 1] != 'b'))) {
                    /* start of binary data */
                    int binary_count; /* length of b256 data */

                    for (binary_count = 0; binary[binary_count + i] == 'b';
                            binary_count++);

                    if (binary_count <= 249) {
                        insertAt(i, 'b');
                        insertValueAt(i, tp, (char) binary_count);
                        tp++;
                    } else {
                        insertAt(i, 'b');
                        insertAt(i + 1, 'b');
                        insertValueAt(i, tp, (char) ((binary_count / 250) + 249));
                        tp++;
                        insertValueAt(i + 1, tp, (char) (binary_count % 250));
                        tp++;
                    }
                }
            }
            i++;
        }

        for (i = 0; i < tp; i++) {
            if (binary[i] == 'b') {
                int prn, temp;

                prn = ((149 * (i + 1)) % 255) + 1;
                temp = target[i] + prn;
                if (temp <= 255) {
                    target[i] = temp;
                } else {
                    target[i] = temp - 256;
                }
            }
        }

        encodeInfo += "\n";

        encodeInfo += "Codewords: ";
        for (i = 0; i < tp; i++) {
            encodeInfo += Integer.toString(target[i]) + " ";
        }
        encodeInfo += "\n";

//        if (debug) {
//            System.out.printf("\nHex Data: ");
//            for (i = 0; i < tp; i++) {
//                System.out.printf("%02X ", target[i]);
//            }
//            System.out.printf("\n");
//        }

        last_mode = current_mode;
        return tp;
    }

    private int encodeRemainder(int symbols_left, int target_length) {

        int inputlen = content.length();

        switch (last_mode) {
            case DM_C40:
            case DM_TEXT:
                if (symbols_left == process_p) { // No unlatch required!

                    if (process_p == 1) {   // 1 data character left to encode.
                        target[target_length] = inputData[inputlen - 1] + 1;
                        target_length++;
                    }

                    if (process_p == 2) {   // 2 data characters left to encode.

                        // Pad with shift 1 value (0) and encode as double.
                        int intValue = (1600 * process_buffer[0]) + (40 * process_buffer[1]) + 1;	// ie (0 + 1).
                        target[target_length] = intValue / 256;
                        target_length++;
                        target[target_length] = intValue % 256;
                        target_length++;
                    }
                }

                if (symbols_left > process_p) {
                    target[target_length] = (254);
                    target_length++;    // Unlatch and encode remaining data in ascii.
                    if (process_p == 1 || (process_p == 2 && process_buffer[0] < 3)) {	// Check for a shift value.

                        target[target_length] = inputData[inputlen - 1] + 1;
                        target_length++;
                    } else if (process_p == 2) {
                        target[target_length] = inputData[inputlen - 2] + 1;
                        target_length++;
                        target[target_length] = inputData[inputlen - 1] + 1;
                        target_length++;
                    }
                }
                break;

            case DM_X12:
                if (symbols_left == process_p) {	// Unlatch not required!

                    if (process_p == 1) {   // 1 data character left to encode.
                        target[target_length] = inputData[inputlen - 1] + 1;
                        target_length++;
                    }

                    if (process_p == 2) {
                        // Encode last 2 bytes as ascii.
                        target[target_length] = inputData[inputlen - 2] + 1;
                        target_length++;
                        target[target_length] = inputData[inputlen - 1] + 1;
                        target_length++;
                    }
                }

                if (symbols_left > process_p) {	// Unlatch and encode remaining data in ascii.

                    target[target_length] = (254);
                    target_length++;   // Unlatch.

                    if (process_p == 1) {
                        target[target_length] = inputData[inputlen - 1] + 1;
                        target_length++;
                    }

                    if (process_p == 2) {
                        target[target_length] = inputData[inputlen - 2] + 1;
                        target_length++;
                        target[target_length] = inputData[inputlen - 1] + 1;
                        target_length++;
                    }
                }
                break;

            case DM_EDIFACT:
                if (symbols_left == process_p) // Unlatch not required!
                {
                    if (process_p == 1) {
                        target[target_length] = inputData[inputlen - 1] + 1;
                        target_length++;
                    }

                    if (process_p == 2) {
                        target[target_length] = inputData[inputlen - 2] + 1;
                        target_length++;
                        target[target_length] = inputData[inputlen - 1] + 1;
                        target_length++;
                    }

                    if (process_p == 3) { // Append edifact unlatch value (31) and encode as triple.
                        target[target_length] = (process_buffer[0] << 2)
                                + ((process_buffer[1] & 0x30) >> 4);
                        target_length++;
                        target[target_length] = ((process_buffer[1] & 0x0f) << 4)
                                + ((process_buffer[2] & 0x3c) >> 2);
                        target_length++;
                        target[target_length] = ((process_buffer[2] & 0x03) << 6)
                                + 31;
                        target_length++;
                    }
                }

                if (symbols_left > process_p) // Unlatch and encode remaining data in ascii.
                {
                    // Edifact unlatch.
                    if (symbols_left < 3) {
                        target[target_length] = 31;
                        target_length++;
                    } else {
                        target[target_length] = (31 << 2);
                        target_length++;
                    }

                    if (process_p == 1) {
                        target[target_length] = inputData[inputlen - 1] + 1;
                        target_length++;
                    }

                    if (process_p == 2) {
                        target[target_length] = inputData[inputlen - 2] + 1;
                        target_length++;
                        target[target_length] = inputData[inputlen - 1] + 1;
                        target_length++;
                    }

                    if (process_p == 3) {
                        target[target_length] = inputData[inputlen - 3] + 1;
                        target_length++;
                        target[target_length] = inputData[inputlen - 2] + 1;
                        target_length++;
                        target[target_length] = inputData[inputlen - 1] + 1;
                        target_length++;
                    }
                }
                break;
        }

//        if (debug) {
//            System.out.printf("+Buffer=: ");
//            for (int i = 0; i < target_length; i++) {
//                System.out.printf("%02X ", target[i]);
//            }
//
//            System.out.printf("\n\n");
//        }

        return target_length;
    }

    private boolean isTwoDigits(int pos) {
        if (Character.isDigit((char) inputData[pos])) {
            if (pos + 1 >= content.length()) {
                return false;
            }
            if (Character.isDigit((char) inputData[pos + 1])) {
                return true;
            }
            return false;
        }
        return false;
    }

    private Mode lookAheadTest(int position, Mode current_mode) {
        /* 'look ahead test' from Annex P */

        double ascii_count, c40_count, text_count, x12_count, edf_count, b256_count, best_count;
        int sp;
        int sourcelen = content.length();
        Mode best_scheme = Mode.NULL;

        /* step (j) */
        if (current_mode == Mode.DM_ASCII) {
            ascii_count = 0.0;
            c40_count = 1.0;
            text_count = 1.0;
            x12_count = 1.0;
            edf_count = 1.0;
            b256_count = 1.25;
        } else {
            ascii_count = 1.0;
            c40_count = 2.0;
            text_count = 2.0;
            x12_count = 2.0;
            edf_count = 2.0;
            b256_count = 2.25;
        }

        switch (current_mode) {
            case DM_C40: // (j)(2)
                c40_count = 0.0;
                break;
            case DM_TEXT: // (j)(3)
                text_count = 0.0;
                break;
            case DM_X12: // (j)(4)
                x12_count = 0.0;
                break;
            case DM_EDIFACT: // (j)(5)
                edf_count = 0.0;
                break;
            case DM_BASE256: // (j)(6)
                b256_count = 0.0;
                break;
        }

        sp = position;

        do {
            if(sp == (sourcelen - 1)) {
                /* At the end of data ... step (k) */
                ascii_count = Math.ceil(ascii_count);
                b256_count = Math.ceil(b256_count);
                edf_count = Math.ceil(edf_count);
                text_count = Math.ceil(text_count);
                x12_count = Math.ceil(x12_count);
                c40_count = Math.ceil(c40_count);

                best_count = c40_count;
                best_scheme = Mode.DM_C40; // (k)(7)

                if (x12_count < best_count) {
                    best_count = x12_count;
                    best_scheme = Mode.DM_X12; // (k)(6)
                }

                if (text_count < best_count) {
                    best_count = text_count;
                    best_scheme = Mode.DM_TEXT; // (k)(5)
                }

                if (edf_count < best_count) {
                    best_count = edf_count;
                    best_scheme = Mode.DM_EDIFACT; // (k)(4)
                }

                if (b256_count < best_count) {
                    best_count = b256_count;
                    best_scheme = Mode.DM_BASE256; // (k)(3)
                }

                if (ascii_count <= best_count) {
                    best_scheme = Mode.DM_ASCII; // (k)(2)
                }
            } else {

                /* ascii ... step (l) */
                if ((inputData[sp] >= '0') && (inputData[sp] <= '9')) {
                    ascii_count += 0.5; // (l)(1)
                } else {
                    if (inputData[sp] > 127) {
                        ascii_count = Math.ceil(ascii_count) + 2.0; // (l)(2)
                    } else {
                        ascii_count = Math.ceil(ascii_count) + 1.0; // (l)(3)
                    }
                }

                /* c40 ... step (m) */
                if ((inputData[sp] == ' ') ||
                       (((inputData[sp] >= '0') && (inputData[sp] <= '9')) ||
                       ((inputData[sp] >= 'A') && (inputData[sp] <= 'Z')))) {
                    c40_count += (2.0 / 3.0); // (m)(1)
                } else {
                    if (inputData[sp] > 127) {
                        c40_count += (8.0 / 3.0); // (m)(2)
                    } else {
                        c40_count += (4.0 / 3.0); // (m)(3)
                    }
                }

                /* text ... step (n) */
                if ((inputData[sp] == ' ') ||
                       (((inputData[sp] >= '0') && (inputData[sp] <= '9')) ||
                       ((inputData[sp] >= 'a') && (inputData[sp] <= 'z')))) {
                    text_count += (2.0 / 3.0); // (n)(1)
                } else {
                    if (inputData[sp] > 127) {
                        text_count += (8.0 / 3.0); // (n)(2)
                    } else {
                        text_count += (4.0 / 3.0); // (n)(3)
                    }
                }

                /* x12 ... step (o) */
                if (isX12(inputData[sp])) {
                    x12_count += (2.0 / 3.0); // (o)(1)
                } else {
                    if (inputData[sp] > 127) {
                        x12_count += (13.0 / 3.0); // (o)(2)
                    } else {
                        x12_count += (10.0 / 3.0); // (o)(3)
                    }
                }

                /* edifact ... step (p) */
                if ((inputData[sp] >= ' ') && (inputData[sp] <= '^')) {
                    edf_count += (3.0 / 4.0); // (p)(1)
                } else {
                    if (inputData[sp] > 127) {
                        edf_count += (17.0 / 4.0); // (p)(2)
                    } else {
                        edf_count += (13.0 / 4.0); // (p)(3)
                    }
                }
                if ((inputDataType == DataType.GS1) && (inputData[sp] == '[')) {
                    edf_count += 6.0;
                }

                /* base 256 ... step (q) */
                if ((inputDataType == DataType.GS1) && (inputData[sp] == '[')) {
                    b256_count += 4.0; // (q)(1)
                } else {
                    b256_count += 1.0; // (q)(2)
                }
            }


            if (sp > (position + 3)) {
                /* 4 data characters processed ... step (r) */

                /* step (r)(6) */
                if (((c40_count + 1.0) < ascii_count) &&
                        ((c40_count + 1.0) < b256_count) &&
                        ((c40_count + 1.0) < edf_count) &&
                        ((c40_count + 1.0) < text_count)) {

                    if (c40_count < x12_count) {
                        best_scheme = Mode.DM_C40;
                    }

                    if (c40_count == x12_count) {
                        if (p_r_6_2_1(sp, sourcelen)) {
                            // Test (r)(6)(ii)(i)
                            best_scheme = Mode.DM_X12;
                        } else {
                            best_scheme = Mode.DM_C40;
                        }
                    }
                }

                /* step (r)(5) */
                if (((x12_count + 1.0) < ascii_count) &&
                        ((x12_count + 1.0) < b256_count) &&
                        ((x12_count + 1.0) < edf_count) &&
                        ((x12_count + 1.0) < text_count) &&
                        ((x12_count + 1.0) < c40_count)) {
                    best_scheme = Mode.DM_X12;
                }

                /* step (r)(4) */
                if (((text_count + 1.0) < ascii_count) &&
                        ((text_count + 1.0) < b256_count) &&
                        ((text_count + 1.0) < edf_count) &&
                        ((text_count + 1.0) < x12_count) &&
                        ((text_count + 1.0) < c40_count)) {
                    best_scheme = Mode.DM_TEXT;
                }

                /* step (r)(3) */
                if (((edf_count + 1.0) < ascii_count) &&
                        ((edf_count + 1.0) < b256_count) &&
                        ((edf_count + 1.0) < text_count) &&
                        ((edf_count + 1.0) < x12_count) &&
                        ((edf_count + 1.0) < c40_count)) {
                    best_scheme = Mode.DM_EDIFACT;
                }

                /* step (r)(2) */
                if (((b256_count + 1.0) <= ascii_count) ||
                        (((b256_count + 1.0) < edf_count) &&
                        ((b256_count + 1.0) < text_count) &&
                        ((b256_count + 1.0) < x12_count) &&
                        ((b256_count + 1.0) < c40_count))) {
                    best_scheme = Mode.DM_BASE256;
                }

                /* step (r)(1) */
                if (((ascii_count + 1.0) <= b256_count) &&
                        ((ascii_count + 1.0) <= edf_count) &&
                        ((ascii_count + 1.0) <= text_count) &&
                        ((ascii_count + 1.0) <= x12_count) &&
                        ((ascii_count + 1.0) <= c40_count)) {
                   best_scheme = Mode.DM_ASCII;
               }
            }

            sp++;
        } while (best_scheme == Mode.NULL); // step (s)

        return best_scheme;
    }

    private boolean p_r_6_2_1(int position, int sourcelen) {
        /* Annex P section (r)(6)(ii)(I)
           "If one of the three X12 terminator/separator characters first
            occurs in the yet to be processed data before a non-X12 character..."
        */

        int i;
        int nonX12Position = 0;
        int specialX12Position = 0;
        boolean retval = false;

        for (i = position; i < sourcelen; i++) {
            if (nonX12Position == 0 && !isX12(i)) {
                nonX12Position = i;
            }

            if (specialX12Position == 0) {
                if ((inputData[i] == (char) 13) ||
                    (inputData[i] == '*') ||
                    (inputData[i] == '>')) {
                    specialX12Position = i;
                }
            }
        }

        if ((nonX12Position != 0) && (specialX12Position != 0)) {
            if (specialX12Position < nonX12Position) {
                retval = true;
            }
        }

        return retval;
    }

    private boolean isX12(int source) {
        if (source == 13) {
            return true;
        }
        if (source == 42) {
            return true;
        }
        if (source == 62) {
            return true;
        }
        if (source == 32) {
            return true;
        }
        if ((source >= '0') && (source <= '9')) {
            return true;
        }
        if ((source >= 'A') && (source <= 'Z')) {
            return true;
        }

        return false;
    }

    private void calculateErrorCorrection(int bytes, int datablock, int rsblock, int skew) {
        // calculate and append ecc code, and if necessary interleave
        int blocks = (bytes + 2) / datablock, b;
        int n, p;
        ReedSolomon rs = new ReedSolomon();

        rs.init_gf(0x12d);
        rs.init_code(rsblock, 1);

        for (b = 0; b < blocks; b++) {
            int[] buf = new int[256];
            int[] ecc = new int[256];

            p = 0;
            for (n = b; n < bytes; n += blocks) {
                buf[p++] = target[n];
            }
            rs.encode(p, buf);
            for (n = 0; n < rsblock; n++) {
                ecc[n] = rs.getResult(n);
            }
            p = rsblock - 1; // comes back reversed
            for (n = b; n < rsblock * blocks; n += blocks) {
                if (skew == 1) {
                    /* Rotate ecc data to make 144x144 size symbols acceptable */
                    /* See http://groups.google.com/group/postscriptbarcode/msg/5ae8fda7757477da */
                    if (b < 8) {
                        target[bytes + n + 2] = ecc[p--];
                    } else {
                        target[bytes + n - 8] = ecc[p--];
                    }
                } else {
                    target[bytes + n] = ecc[p--];
                }
            }
        }
    }

    private void insertAt(int pos, char newbit) {
        /* Insert a character into the middle of a string at position posn */
        int i;

        for (i = binary_length; i > pos; i--) {
            binary[i] = binary[i - 1];
        }
        binary[pos] = newbit;
    }

    private void insertValueAt(int posn, int streamlen, char newbit) {
        int i;

        for (i = streamlen; i > posn; i--) {
            target[i] = target[i - 1];
        }
        target[posn] = newbit;
    }

    private void addPadBits(int tp, int tail_length) {
        /* adds unlatch and pad bits */
        int i, prn, temp;

        switch (last_mode) {
            case DM_C40:
            case DM_TEXT:
            case DM_X12:
                target[tp] = 254;
                tp++; /* Unlatch */

                tail_length--;
        }

        for (i = tail_length; i > 0; i--) {
            if (i == tail_length) {
                target[tp] = 129;
                tp++; /* Pad */

            } else {
                prn = ((149 * (tp + 1)) % 253) + 1;
                temp = 129 + prn;
                if (temp <= 254) {
                    target[tp] = temp;
                    tp++;
                } else {
                    target[tp] = temp - 254;
                    tp++;
                }
            }
        }
    }

    private void placeData(int NR, int NC) {
        int r, c, p;
        // invalidate
        for (r = 0; r < NR; r++) {
            for (c = 0; c < NC; c++) {
                places[r * NC + c] = 0;
            }
        }
        // start
        p = 1;
        r = 4;
        c = 0;
        do {
            // check corner
            if (r == NR && (c == 0)) {
                placeCornerA(NR, NC, p++);
            }
            if (r == NR - 2 && (c == 0) && ((NC % 4) != 0)) {
                placeCornerB(NR, NC, p++);
            }
            if (r == NR - 2 && (c == 0) && (NC % 8) == 4) {
                placeCornerC(NR, NC, p++);
            }
            if (r == NR + 4 && c == 2 && ((NC % 8) == 0)) {
                placeCornerD(NR, NC, p++);
            }
            // up/right
            do {
                if (r < NR && c >= 0 && (places[r * NC + c] == 0)) {
                    placeBlock(NR, NC, r, c, p++);
                }
                r -= 2;
                c += 2;
            } while (r >= 0 && c < NC);
            r++;
            c += 3;
            // down/left
            do {
                if (r >= 0 && c < NC && (places[r * NC + c] == 0)) {
                    placeBlock(NR, NC, r, c, p++);
                }
                r += 2;
                c -= 2;
            } while (r < NR && c >= 0);
            r += 3;
            c++;
        } while (r < NR || c < NC);
        // unfilled corner
        if (places[NR * NC - 1] == 0) {
            places[NR * NC - 1] = places[NR * NC - NC - 2] = 1;
        }
    }

    private void placeCornerA(int NR, int NC, int p) {
        placeBit(NR, NC, NR - 1, 0, p, 7);
        placeBit(NR, NC, NR - 1, 1, p, 6);
        placeBit(NR, NC, NR - 1, 2, p, 5);
        placeBit(NR, NC, 0, NC - 2, p, 4);
        placeBit(NR, NC, 0, NC - 1, p, 3);
        placeBit(NR, NC, 1, NC - 1, p, 2);
        placeBit(NR, NC, 2, NC - 1, p, 1);
        placeBit(NR, NC, 3, NC - 1, p, 0);
    }

    private void placeCornerB(int NR, int NC, int p) {
        placeBit(NR, NC, NR - 3, 0, p, 7);
        placeBit(NR, NC, NR - 2, 0, p, 6);
        placeBit(NR, NC, NR - 1, 0, p, 5);
        placeBit(NR, NC, 0, NC - 4, p, 4);
        placeBit(NR, NC, 0, NC - 3, p, 3);
        placeBit(NR, NC, 0, NC - 2, p, 2);
        placeBit(NR, NC, 0, NC - 1, p, 1);
        placeBit(NR, NC, 1, NC - 1, p, 0);
    }

    private void placeCornerC(int NR, int NC, int p) {
        placeBit(NR, NC, NR - 3, 0, p, 7);
        placeBit(NR, NC, NR - 2, 0, p, 6);
        placeBit(NR, NC, NR - 1, 0, p, 5);
        placeBit(NR, NC, 0, NC - 2, p, 4);
        placeBit(NR, NC, 0, NC - 1, p, 3);
        placeBit(NR, NC, 1, NC - 1, p, 2);
        placeBit(NR, NC, 2, NC - 1, p, 1);
        placeBit(NR, NC, 3, NC - 1, p, 0);
    }

    private void placeCornerD(int NR, int NC, int p) {
        placeBit(NR, NC, NR - 1, 0, p, 7);
        placeBit(NR, NC, NR - 1, NC - 1, p, 6);
        placeBit(NR, NC, 0, NC - 3, p, 5);
        placeBit(NR, NC, 0, NC - 2, p, 4);
        placeBit(NR, NC, 0, NC - 1, p, 3);
        placeBit(NR, NC, 1, NC - 3, p, 2);
        placeBit(NR, NC, 1, NC - 2, p, 1);
        placeBit(NR, NC, 1, NC - 1, p, 0);
    }

    private void placeBlock(int NR, int NC, int r, int c, int p) {
        placeBit(NR, NC, r - 2, c - 2, p, 7);
        placeBit(NR, NC, r - 2, c - 1, p, 6);
        placeBit(NR, NC, r - 1, c - 2, p, 5);
        placeBit(NR, NC, r - 1, c - 1, p, 4);
        placeBit(NR, NC, r - 1, c - 0, p, 3);
        placeBit(NR, NC, r - 0, c - 2, p, 2);
        placeBit(NR, NC, r - 0, c - 1, p, 1);
        placeBit(NR, NC, r - 0, c - 0, p, 0);
    }

    private void placeBit(int NR, int NC, int r, int c, int p, int b) {
        if (r < 0) {
            r += NR;
            c += 4 - ((NR + 4) % 8);
        }
        if (c < 0) {
            c += NC;
            r += 4 - ((NC + 4) % 8);
        }
        places[r * NC + c] = (p << 3) + b;
    }
}
