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
 * <p>Implements Aztec Code bar code symbology According to ISO/IEC 24778:2008.
 *
 * <p>Aztec Code can encode 8-bit ISO 8859-1 (Latin-1) data (except 0x00 Null
 * characters) up to a maximum length of approximately 3800 numeric characters,
 * 3000 alphabetic characters or 1900 bytes of data in a two-dimensional matrix
 * symbol.
 *
 * @author <a href="mailto:rstuart114@gmail.com">Robin Stuart</a>
 */
public class AztecCode extends Symbol {

    private static final int[] COMPACT_AZTEC_MAP = { //27 x 27 data grid
        609, 608, 411, 413, 415, 417, 419, 421, 423, 425, 427, 429, 431, 433, 435, 437, 439, 441, 443, 445, 447, 449, 451, 453, 455, 457, 459,
        607, 606, 410, 412, 414, 416, 418, 420, 422, 424, 426, 428, 430, 432, 434, 436, 438, 440, 442, 444, 446, 448, 450, 452, 454, 456, 458,
        605, 604, 409, 408, 243, 245, 247, 249, 251, 253, 255, 257, 259, 261, 263, 265, 267, 269, 271, 273, 275, 277, 279, 281, 283, 460, 461,
        603, 602, 407, 406, 242, 244, 246, 248, 250, 252, 254, 256, 258, 260, 262, 264, 266, 268, 270, 272, 274, 276, 278, 280, 282, 462, 463,
        601, 600, 405, 404, 241, 240, 107, 109, 111, 113, 115, 117, 119, 121, 123, 125, 127, 129, 131, 133, 135, 137, 139, 284, 285, 464, 465,
        599, 598, 403, 402, 239, 238, 106, 108, 110, 112, 114, 116, 118, 120, 122, 124, 126, 128, 130, 132, 134, 136, 138, 286, 287, 466, 467,
        597, 596, 401, 400, 237, 236, 105, 104, 3, 5, 7, 9, 11, 13, 15, 17, 19, 21, 23, 25, 27, 140, 141, 288, 289, 468, 469,
        595, 594, 399, 398, 235, 234, 103, 102, 2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22, 24, 26, 142, 143, 290, 291, 470, 471,
        593, 592, 397, 396, 233, 232, 101, 100, 1, 1, 2000, 2001, 2002, 2003, 2004, 2005, 2006, 0, 1, 28, 29, 144, 145, 292, 293, 472, 473,
        591, 590, 395, 394, 231, 230, 99, 98, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 30, 31, 146, 147, 294, 295, 474, 475,
        589, 588, 393, 392, 229, 228, 97, 96, 2027, 1, 0, 0, 0, 0, 0, 0, 0, 1, 2007, 32, 33, 148, 149, 296, 297, 476, 477,
        587, 586, 391, 390, 227, 226, 95, 94, 2026, 1, 0, 1, 1, 1, 1, 1, 0, 1, 2008, 34, 35, 150, 151, 298, 299, 478, 479,
        585, 584, 389, 388, 225, 224, 93, 92, 2025, 1, 0, 1, 0, 0, 0, 1, 0, 1, 2009, 36, 37, 152, 153, 300, 301, 480, 481,
        583, 582, 387, 386, 223, 222, 91, 90, 2024, 1, 0, 1, 0, 1, 0, 1, 0, 1, 2010, 38, 39, 154, 155, 302, 303, 482, 483,
        581, 580, 385, 384, 221, 220, 89, 88, 2023, 1, 0, 1, 0, 0, 0, 1, 0, 1, 2011, 40, 41, 156, 157, 304, 305, 484, 485,
        579, 578, 383, 382, 219, 218, 87, 86, 2022, 1, 0, 1, 1, 1, 1, 1, 0, 1, 2012, 42, 43, 158, 159, 306, 307, 486, 487,
        577, 576, 381, 380, 217, 216, 85, 84, 2021, 1, 0, 0, 0, 0, 0, 0, 0, 1, 2013, 44, 45, 160, 161, 308, 309, 488, 489,
        575, 574, 379, 378, 215, 214, 83, 82, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 46, 47, 162, 163, 310, 311, 490, 491,
        573, 572, 377, 376, 213, 212, 81, 80, 0, 0, 2020, 2019, 2018, 2017, 2016, 2015, 2014, 0, 0, 48, 49, 164, 165, 312, 313, 492, 493,
        571, 570, 375, 374, 211, 210, 78, 76, 74, 72, 70, 68, 66, 64, 62, 60, 58, 56, 54, 50, 51, 166, 167, 314, 315, 494, 495,
        569, 568, 373, 372, 209, 208, 79, 77, 75, 73, 71, 69, 67, 65, 63, 61, 59, 57, 55, 52, 53, 168, 169, 316, 317, 496, 497,
        567, 566, 371, 370, 206, 204, 202, 200, 198, 196, 194, 192, 190, 188, 186, 184, 182, 180, 178, 176, 174, 170, 171, 318, 319, 498, 499,
        565, 564, 369, 368, 207, 205, 203, 201, 199, 197, 195, 193, 191, 189, 187, 185, 183, 181, 179, 177, 175, 172, 173, 320, 321, 500, 501,
        563, 562, 366, 364, 362, 360, 358, 356, 354, 352, 350, 348, 346, 344, 342, 340, 338, 336, 334, 332, 330, 328, 326, 322, 323, 502, 503,
        561, 560, 367, 365, 363, 361, 359, 357, 355, 353, 351, 349, 347, 345, 343, 341, 339, 337, 335, 333, 331, 329, 327, 324, 325, 504, 505,
        558, 556, 554, 552, 550, 548, 546, 544, 542, 540, 538, 536, 534, 532, 530, 528, 526, 524, 522, 520, 518, 516, 514, 512, 510, 506, 507,
        559, 557, 555, 553, 551, 549, 547, 545, 543, 541, 539, 537, 535, 533, 531, 529, 527, 525, 523, 521, 519, 517, 515, 513, 511, 508, 509
    };

    private static final int[][] AZTEC_MAP = new int[151][151];

    private static final int[] AZTEC_CODE_SET = { /* From Table 2 */
        32, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 12, 32, 32, 32, 32, 32, 32,
        32, 32, 32, 32, 32, 32, 32, 4, 4, 4, 4, 4, 23, 8, 8, 8, 8, 8, 8, 8,
        8, 8, 8, 8, 24, 8, 24, 8, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 8, 8,
        8, 8, 8, 8, 4, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 8, 4, 8, 4, 4, 4, 2, 2, 2,
        2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
        2, 2, 2, 8, 4, 8, 4, 4
    };

    private static final int[] AZTEC_SYMBOL_CHAR = { /* From Table 2 */
        0, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 300, 14, 15, 16, 17, 18, 19,
        20, 21, 22, 23, 24, 25, 26, 15, 16, 17, 18, 19, 1, 6, 7, 8, 9, 10, 11, 12,
        13, 14, 15, 16, 301, 18, 302, 20, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 21, 22,
        23, 24, 25, 26, 20, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16,
        17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 27, 21, 28, 22, 23, 24, 2, 3, 4,
        5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24,
        25, 26, 27, 29, 25, 30, 26, 27
    };

    /* Problem characters are:
     300: Carriage Return (ASCII 13)
     301: Comma (ASCII 44)
     302: Full Stop (ASCII 46)
     */
    private static final String[] PENTBIT = {
        "00000", "00001", "00010", "00011", "00100", "00101", "00110", "00111", "01000", "01001",
        "01010", "01011", "01100", "01101", "01110", "01111", "10000", "10001", "10010", "10011", "10100", "10101",
        "10110", "10111", "11000", "11001", "11010", "11011", "11100", "11101", "11110", "11111"
    };

    private static final String[] QUADBIT = {
        "0000", "0001", "0010", "0011", "0100", "0101", "0110", "0111", "1000", "1001",
        "1010", "1011", "1100", "1101", "1110", "1111"
    };

    private static final String[] TRIBIT = {
        "000", "001", "010", "011", "100", "101", "110", "111"
    };

    private static final int[] AZTEC_SIZES = { /* Codewords per symbol */
        21, 48, 60, 88, 120, 156, 196, 240, 230, 272, 316, 364, 416, 470, 528, 588, 652, 720, 790,
        864, 940, 1020, 920, 992, 1066, 1144, 1224, 1306, 1392, 1480, 1570, 1664
    };

    private static final int[] AZTEC_COMPACT_SIZES = {
        17, 40, 51, 76
    };

    private static final int[] AZTEC_10_DATA_SIZES = { /* Data bits per symbol maximum with 10% error correction */
        96, 246, 408, 616, 840, 1104, 1392, 1704, 2040, 2420, 2820, 3250, 3720, 4200, 4730,
        5270, 5840, 6450, 7080, 7750, 8430, 9150, 9900, 10680, 11484, 12324, 13188, 14076,
        15000, 15948, 16920, 17940
    };

    private static final int[] AZTEC_23_DATA_SIZES = { /* Data bits per symbol maximum with 23% error correction */
        84, 204, 352, 520, 720, 944, 1184, 1456, 1750, 2070, 2410, 2780, 3180, 3590, 4040,
        4500, 5000, 5520, 6060, 6630, 7210, 7830, 8472, 9132, 9816, 10536, 11280, 12036,
        12828, 13644, 14472, 15348
    };

    private static final int[] AZTEC_36_DATA_SIZES = { /* Data bits per symbol maximum with 36% error correction */
        66, 168, 288, 432, 592, 776, 984, 1208, 1450, 1720, 2000, 2300, 2640, 2980, 3350,
        3740, 4150, 4580, 5030, 5500, 5990, 6500, 7032, 7584, 8160, 8760, 9372, 9996, 10656,
        11340, 12024, 12744
    };

    private static final int[] AZTEC_50_DATA_SIZES = { /* Data bits per symbol maximum with 50% error correction */
        48, 126, 216, 328, 456, 600, 760, 936, 1120, 1330, 1550, 1790, 2050, 2320, 2610,
        2910, 3230, 3570, 3920, 4290, 4670, 5070, 5484, 5916, 6360, 6828, 7308, 7800, 8316,
        8844, 9384, 9948
    };

    private static final int[] AZTEC_COMPACT_10_DATA_SIZES = {
        78, 198, 336, 520
    };
    private static final int[] AZTEC_COMPACT_23_DATA_SIZES = {
        66, 168, 288, 440
    };
    private static final int[] AZTEC_COMPACT_36_DATA_SIZES = {
        48, 138, 232, 360
    };
    private static final int[] AZTEC_COMPACT_50_DATA_SIZES = {
        36, 102, 176, 280
    };

    private static final int[] AZTEC_OFFSET = {
        66, 64, 62, 60, 57, 55, 53, 51, 49, 47, 45, 42, 40, 38, 36, 34, 32, 30, 28, 25, 23, 21,
        19, 17, 15, 13, 10, 8, 6, 4, 2, 0
    };

    private static final int[] AZTEC_COMPACT_OFFSET = {
        6, 4, 2, 0
    };

    /* Initialize AZTEC_MAP */
    static {

        int layer, start, length, n, i;
        int x, y;

        for (x = 0; x < 151; x++) {
            for (y = 0; y < 151; y++) {
                AZTEC_MAP[x][y] = 0;
            }
        }

        for (layer = 1; layer < 33; layer++) {
            start = (112 * (layer - 1)) + (16 * (layer - 1) * (layer - 1)) + 2;
            length = 28 + ((layer - 1) * 4) + (layer * 4);
            /* Top */
            i = 0;
            x = 64 - ((layer - 1) * 2);
            y = 63 - ((layer - 1) * 2);
            for (n = start; n < (start + length); n += 2) {
                AZTEC_MAP[avoidReferenceGrid(x + i)][avoidReferenceGrid(y)] = n;
                AZTEC_MAP[avoidReferenceGrid(x + i)][avoidReferenceGrid(y - 1)] = n + 1;
                i++;
            }
            /* Right */
            i = 0;
            x = 78 + ((layer - 1) * 2);
            y = 64 - ((layer - 1) * 2);
            for (n = start + length; n < (start + (length * 2)); n += 2) {
                AZTEC_MAP[avoidReferenceGrid(x)][avoidReferenceGrid(y + i)] = n;
                AZTEC_MAP[avoidReferenceGrid(x + 1)][avoidReferenceGrid(y + i)] = n + 1;
                i++;
            }
            /* Bottom */
            i = 0;
            x = 77 + ((layer - 1) * 2);
            y = 78 + ((layer - 1) * 2);
            for (n = start + (length * 2); n < (start + (length * 3)); n += 2) {
                AZTEC_MAP[avoidReferenceGrid(x - i)][avoidReferenceGrid(y)] = n;
                AZTEC_MAP[avoidReferenceGrid(x - i)][avoidReferenceGrid(y + 1)] = n + 1;
                i++;
            }
            /* Left */
            i = 0;
            x = 63 - ((layer - 1) * 2);
            y = 77 + ((layer - 1) * 2);
            for (n = start + (length * 3); n < (start + (length * 4)); n += 2) {
                AZTEC_MAP[avoidReferenceGrid(x)][avoidReferenceGrid(y - i)] = n;
                AZTEC_MAP[avoidReferenceGrid(x - 1)][avoidReferenceGrid(y - i)] = n + 1;
                i++;
            }
        }

        /* Central finder pattern */
        for (y = 69; y <= 81; y++) {
            for (x = 69; x <= 81; x++) {
                AZTEC_MAP[x][y] = 1;
            }
        }
        for (y = 70; y <= 80; y++) {
            for (x = 70; x <= 80; x++) {
                AZTEC_MAP[x][y] = 0;
            }
        }
        for (y = 71; y <= 79; y++) {
            for (x = 71; x <= 79; x++) {
                AZTEC_MAP[x][y] = 1;
            }
        }
        for (y = 72; y <= 78; y++) {
            for (x = 72; x <= 78; x++) {
                AZTEC_MAP[x][y] = 0;
            }
        }
        for (y = 73; y <= 77; y++) {
            for (x = 73; x <= 77; x++) {
                AZTEC_MAP[x][y] = 1;
            }
        }
        for (y = 74; y <= 76; y++) {
            for (x = 74; x <= 76; x++) {
                AZTEC_MAP[x][y] = 0;
            }
        }

        /* Guide bars */
        for (y = 11; y < 151; y += 16) {
            for (x = 1; x < 151; x += 2) {
                AZTEC_MAP[x][y] = 1;
                AZTEC_MAP[y][x] = 1;
            }
        }

        /* Descriptor */
        for (i = 0; i < 10; i++) { /* Top */
            AZTEC_MAP[avoidReferenceGrid(66 + i)][avoidReferenceGrid(64)] = 20000 + i;
        }
        for (i = 0; i < 10; i++) { /* Right */
            AZTEC_MAP[avoidReferenceGrid(77)][avoidReferenceGrid(66 + i)] = 20010 + i;
        }
        for (i = 0; i < 10; i++) { /* Bottom */
            AZTEC_MAP[avoidReferenceGrid(75 - i)][avoidReferenceGrid(77)] = 20020 + i;
        }
        for (i = 0; i < 10; i++) { /* Left */
            AZTEC_MAP[avoidReferenceGrid(64)][avoidReferenceGrid(75 - i)] = 20030 + i;
        }

        /* Orientation */
        AZTEC_MAP[avoidReferenceGrid(64)][avoidReferenceGrid(64)] = 1;
        AZTEC_MAP[avoidReferenceGrid(65)][avoidReferenceGrid(64)] = 1;
        AZTEC_MAP[avoidReferenceGrid(64)][avoidReferenceGrid(65)] = 1;
        AZTEC_MAP[avoidReferenceGrid(77)][avoidReferenceGrid(64)] = 1;
        AZTEC_MAP[avoidReferenceGrid(77)][avoidReferenceGrid(65)] = 1;
        AZTEC_MAP[avoidReferenceGrid(77)][avoidReferenceGrid(76)] = 1;
    }

    private static int avoidReferenceGrid(int input) {
        int output = input;
        if (output > 10) {
            output++;
        }
        if (output > 26) {
            output++;
        }
        if (output > 42) {
            output++;
        }
        if (output > 58) {
            output++;
        }
        if (output > 74) {
            output++;
        }
        if (output > 90) {
            output++;
        }
        if (output > 106) {
            output++;
        }
        if (output > 122) {
            output++;
        }
        if (output > 138) {
            output++;
        }
        return output;
    }

    private int preferredSize = 0;
    private int preferredEccLevel = 2;

    /**
     * Sets a preferred symbol size. This value may be ignored if data string is
     * too large to fit in the specified symbol size. Values correspond to symbol
     * sizes as shown in the following table:
     *
     * <table summary="Available Aztec Code symbol sizes">
     * <tbody>
     * <tr><th>Input</th><th>Symbol Size</th><th>Input</th><th>Symbol Size</th></tr>
     * <tr><td>1    </td><td>15 x 15    </td><td>19   </td><td>79 x 79    </td></tr>
     * <tr><td>2    </td><td>19 x 19    </td><td>20   </td><td>83 x 83    </td></tr>
     * <tr><td>3    </td><td>23 x 23    </td><td>21   </td><td>87 x 87    </td></tr>
     * <tr><td>4    </td><td>27 x 27    </td><td>22   </td><td>91 x 91    </td></tr>
     * <tr><td>5    </td><td>19 x 19    </td><td>23   </td><td>95 x 95    </td></tr>
     * <tr><td>6    </td><td>23 x 23    </td><td>24   </td><td>101 x 101  </td></tr>
     * <tr><td>7    </td><td>27 x 27    </td><td>25   </td><td>105 x 105  </td></tr>
     * <tr><td>8    </td><td>31 x 31    </td><td>26   </td><td>109 x 109  </td></tr>
     * <tr><td>9    </td><td>37 x 37    </td><td>27   </td><td>113 x 113  </td></tr>
     * <tr><td>10   </td><td>41 x 41    </td><td>28   </td><td>117 x 117  </td></tr>
     * <tr><td>11   </td><td>45 x 45    </td><td>29   </td><td>121 x 121  </td></tr>
     * <tr><td>12   </td><td>49 x 49    </td><td>30   </td><td>125 x 125  </td></tr>
     * <tr><td>13   </td><td>53 x 53    </td><td>31   </td><td>131 x 131  </td></tr>
     * <tr><td>14   </td><td>57 x 57    </td><td>32   </td><td>135 x 135  </td></tr>
     * <tr><td>15   </td><td>61 x 61    </td><td>33   </td><td>139 x 139  </td></tr>
     * <tr><td>16   </td><td>67 x 67    </td><td>34   </td><td>143 x 143  </td></tr>
     * <tr><td>17   </td><td>71 x 71    </td><td>35   </td><td>147 x 147  </td></tr>
     * <tr><td>18   </td><td>75 x 75    </td><td>36   </td><td>151 x 151  </td></tr>
     * </tbody>
     * </table>
     *
     * @param size an integer in the range 1 - 36
     */
    public void setPreferredSize(int size) {
        if (size < 1 || size > 36) {
            throw new IllegalArgumentException("Invalid size: " + size);
        }
        preferredSize = size;
    }

    /**
     * Sets the preferred minimum amount of symbol space dedicated to error
     * correction. This value will be ignored if a symbol size has been set by
     * <code>setPreferredSize</code>. Valid options are:
     *
     * <table summary="Error correction options">
     * <tbody>
     * <tr><th>Mode</th><th>Error Correction Capacity</th></tr>
     * <tr><td>1   </td><td>&gt; 10% + 3 codewords    </td></tr>
     * <tr><td>2   </td><td>&gt; 23% + 3 codewords    </td></tr>
     * <tr><td>3   </td><td>&gt; 36% + 3 codewords    </td></tr>
     * <tr><td>4   </td><td>&gt; 50% + 3 codewords    </td></tr>
     * </tbody>
     * </table>
     *
     * @param eccLevel an integer in the range 1 - 4
     */
    public void setPreferredEccLevel(int eccLevel) {
        if (eccLevel < 1 || eccLevel > 4) {
            throw new IllegalArgumentException("Invalid ECC level: " + eccLevel);
        }
        preferredEccLevel = eccLevel;
    }

    @Override
    protected boolean gs1Supported() {
        return true;
    }

    @Override
    protected void encode() {

        int i, ecc_level, data_length, layers, data_maxsize;
        int adjustment_size, codeword_size;
        int j, count, adjusted_length, padbits, remainder;
        String adjusted_string = "", bit_pattern = "";
        int comp_loop = 4;
        int data_blocks, ecc_blocks, total_bits;
        boolean compact;
        ReedSolomon rs = new ReedSolomon();
        ReedSolomon rs2 = new ReedSolomon();
        String descriptor = "";
        int[] desc_data = new int[4];
        int[] desc_ecc = new int[6];
        int y, x, weight;
        String bin;
        int t;
        boolean done;

        if (readerInit) {
            comp_loop = 1;
        }

        eciProcess(); // Get ECI mode

        if (inputDataType == DataType.GS1 && readerInit) {
            throw new OkapiException("Cannot encode in GS1 and Reader Initialisation mode at the same time");
        }

        String binaryString = generateAztecBinary();
        if (binaryString == null) {
            throw new OkapiException("Input too long or too many extended ASCII characters");
        }

        // Set the error correction level
        ecc_level = preferredEccLevel;

        data_length = binaryString.length();

        layers = 0; /* Keep compiler happy! */

        data_maxsize = 0; /* Keep compiler happy! */

        adjustment_size = 0;

        if (preferredSize == 0) { /* The size of the symbol can be determined by Okapi */

            do {
                /* Decide what size symbol to use - the smallest that fits the data */
                compact = false; /* 1 = Aztec Compact, 0 = Normal Aztec */

                layers = 0;

                switch (ecc_level) {
                    /* For each level of error correction work out the smallest symbol which
                     the data will fit in */
                    case 1:
                        for (i = 32; i > 0; i--) {
                            if ((data_length + adjustment_size) < AZTEC_10_DATA_SIZES[i - 1]) {
                                layers = i;
                                compact = false;
                                data_maxsize = AZTEC_10_DATA_SIZES[i - 1];
                            }
                        }
                        for (i = comp_loop; i > 0; i--) {
                            if ((data_length + adjustment_size) < AZTEC_COMPACT_10_DATA_SIZES[i - 1]) {
                                layers = i;
                                compact = true;
                                data_maxsize = AZTEC_COMPACT_10_DATA_SIZES[i - 1];
                            }
                        }
                        break;
                    case 2:
                        for (i = 32; i > 0; i--) {
                            if ((data_length + adjustment_size) < AZTEC_23_DATA_SIZES[i - 1]) {
                                layers = i;
                                compact = false;
                                data_maxsize = AZTEC_23_DATA_SIZES[i - 1];
                            }
                        }
                        for (i = comp_loop; i > 0; i--) {
                            if ((data_length + adjustment_size) < AZTEC_COMPACT_23_DATA_SIZES[i - 1]) {
                                layers = i;
                                compact = true;
                                data_maxsize = AZTEC_COMPACT_23_DATA_SIZES[i - 1];
                            }
                        }
                        break;
                    case 3:
                        for (i = 32; i > 0; i--) {
                            if ((data_length + adjustment_size) < AZTEC_36_DATA_SIZES[i - 1]) {
                                layers = i;
                                compact = false;
                                data_maxsize = AZTEC_36_DATA_SIZES[i - 1];
                            }
                        }
                        for (i = comp_loop; i > 0; i--) {
                            if ((data_length + adjustment_size) < AZTEC_COMPACT_36_DATA_SIZES[i - 1]) {
                                layers = i;
                                compact = true;
                                data_maxsize = AZTEC_COMPACT_36_DATA_SIZES[i - 1];
                            }
                        }
                        break;
                    case 4:
                        for (i = 32; i > 0; i--) {
                            if ((data_length + adjustment_size) < AZTEC_50_DATA_SIZES[i - 1]) {
                                layers = i;
                                compact = false;
                                data_maxsize = AZTEC_50_DATA_SIZES[i - 1];
                            }
                        }
                        for (i = comp_loop; i > 0; i--) {
                            if ((data_length + adjustment_size) < AZTEC_COMPACT_50_DATA_SIZES[i - 1]) {
                                layers = i;
                                compact = true;
                                data_maxsize = AZTEC_COMPACT_50_DATA_SIZES[i - 1];
                            }
                        }
                        break;
                }

                if (layers == 0) { /* Couldn't find a symbol which fits the data */
                    throw new OkapiException("Input too long (too many bits for selected ECC)");
                }

                /* Determine codeword bit length - Table 3 */
                codeword_size = 6; /* if (layers <= 2) */

                if ((layers >= 3) && (layers <= 8)) {
                    codeword_size = 8;
                }
                if ((layers >= 9) && (layers <= 22)) {
                    codeword_size = 10;
                }
                if (layers >= 23) {
                    codeword_size = 12;
                }

                j = 0;
                i = 0;

                do {
                    if ((j + 1) % codeword_size == 0) {
                        /* Last bit of codeword */
                        done = false;
                        count = 0;

                        /* Discover how many '1's in current codeword */
                        for (t = 0; t < (codeword_size - 1); t++) {
                            if (binaryString.charAt((i - (codeword_size - 1)) + t) == '1') {
                                count++;
                            }
                        }

                        if (count == (codeword_size - 1)) {
                            adjusted_string += '0';
                            j++;
                            done = true;
                        }

                        if (count == 0) {
                            adjusted_string += '1';
                            j++;
                            done = true;
                        }

                        if (!done) {
                            adjusted_string += binaryString.charAt(i);
                            j++;
                            i++;
                        }
                    } else {
                        adjusted_string += binaryString.charAt(i);
                        j++;
                        i++;
                    }
                } while (i < data_length);

                adjusted_length = adjusted_string.length();
                adjustment_size = adjusted_length - data_length;

                /* Add padding */
                remainder = adjusted_length % codeword_size;

                padbits = codeword_size - remainder;
                if (padbits == codeword_size) {
                    padbits = 0;
                }

                for (i = 0; i < padbits; i++) {
                    adjusted_string += "1";
                }
                adjusted_length = adjusted_string.length();

                count = 0;
                for (i = (adjusted_length - codeword_size); i < adjusted_length; i++) {
                    if (adjusted_string.charAt(i) == '1') {
                        count++;
                    }
                }
                if (count == codeword_size) {
                    adjusted_string = adjusted_string.substring(0, adjusted_length - 1) + '0';
                }

                encodeInfo += "Codewords: ";
                for (i = 0; i < (adjusted_length / codeword_size); i++) {
                    int l = 0, m = (1 << (codeword_size - 1));
                    for (j = 0; j < codeword_size; j++) {
                        if (adjusted_string.charAt((i * codeword_size) + j) == '1') {
                            l += m;
                        }
                        m = m >> 1;
                    }
                    encodeInfo += Integer.toString(l) + " ";
                }
                encodeInfo += "\n";

            } while (adjusted_length > data_maxsize);
            /* This loop will only repeat on the rare occasions when the rule about not having all 1s or all 0s
             means that the binary string has had to be lengthened beyond the maximum number of bits that can
             be encoded in a symbol of the selected size */
        } else {
            /* The size of the symbol has been specified by the user */
            compact = false;
            if ((readerInit) && ((preferredSize >= 2) && (preferredSize <= 4))) {
                preferredSize = 5;
            }
            if ((preferredSize >= 1) && (preferredSize <= 4)) {
                compact = true;
                layers = preferredSize;
            }
            if ((preferredSize >= 5) && (preferredSize <= 36)) {
                layers = preferredSize - 4;
            }

            /* Determine codeword bitlength - Table 3 */
            codeword_size = 6;
            if ((layers >= 3) && (layers <= 8)) {
                codeword_size = 8;
            }
            if ((layers >= 9) && (layers <= 22)) {
                codeword_size = 10;
            }
            if (layers >= 23) {
                codeword_size = 12;
            }
            j = 0;
            i = 0;
            do {
                if (((j + 1) % codeword_size) == 0) {
                    /* Last bit of codeword */
                    done = false;
                    count = 0;

                    /* Discover how many '1's in current codeword */
                    for (t = 0; t < (codeword_size - 1); t++) {
                        if (binaryString.charAt((i - (codeword_size - 1)) + t) == '1') {
                            count++;
                        }
                    }

                    if (count == (codeword_size - 1)) {
                        adjusted_string += '0';
                        j++;
                        done = true;
                    }

                    if (count == 0) {
                        adjusted_string += '1';
                        j++;
                        done = true;
                    }

                    if (!done) {
                        adjusted_string += binaryString.charAt(i);
                        j++;
                        i++;
                    }
                } else {
                    adjusted_string += binaryString.charAt(i);
                    j++;
                    i++;
                }
            } while (i < binaryString.length());

            adjusted_length = adjusted_string.length();
            remainder = adjusted_length % codeword_size;
            padbits = codeword_size - remainder;

            if (padbits == codeword_size) {
                padbits = 0;
            }
            for (i = 0; i < padbits; i++) {
                adjusted_string += "1";
            }

            adjusted_length = adjusted_string.length();
            count = 0;

            for (i = (adjusted_length - codeword_size); i < adjusted_length; i++) {
                if (adjusted_string.charAt(i) == '1') {
                    count++;
                }
            }

            if (count == codeword_size) {
                adjusted_string = adjusted_string.substring(0, adjusted_length - 1) + '0';
            }

            /* Check if the data actually fits into the selected symbol size */
            if (compact) {
                data_maxsize = codeword_size * (AZTEC_COMPACT_SIZES[layers - 1] - 3);
            } else {
                data_maxsize = codeword_size * (AZTEC_SIZES[layers - 1] - 3);
            }

            if (adjusted_length > data_maxsize) {
                throw new OkapiException("Data too long for specified Aztec Code symbol size");
            }

            encodeInfo += "Codewords: ";
            for (i = 0; i < (adjusted_length / codeword_size); i++) {
                int l = 0, m = (1 << (codeword_size - 1));
                for (j = 0; j < codeword_size; j++) {
                    if (adjusted_string.charAt((i * codeword_size) + j) == '1') {
                        l += m;
                    }
                    m = m >> 1;
                }
                encodeInfo += Integer.toString(l) + " ";
            }
            encodeInfo += "\n";
        }

        if (readerInit && layers > 22) {
            throw new OkapiException("Data too long for reader initialisation symbol");
        }

        data_blocks = adjusted_length / codeword_size;

        if (compact) {
            ecc_blocks = AZTEC_COMPACT_SIZES[layers - 1] - data_blocks;
        } else {
            ecc_blocks = AZTEC_SIZES[layers - 1] - data_blocks;
        }

        encodeInfo += "Compact Mode: " + compact + "\n";
        encodeInfo += "Layers: " + layers + '\n';
        encodeInfo += "Codeword Length: " + codeword_size + " bits\n";
        encodeInfo += "Data Codewords: " + data_blocks + '\n';
        encodeInfo += "ECC Codewords: " + ecc_blocks + '\n';

        int[] data_part = new int[data_blocks + 3];
        int[] ecc_part = new int[ecc_blocks + 3];

        /* Split into codewords and calculate Reed-Solomon error correction codes */
        switch (codeword_size) {
            case 6:
                for (i = 0; i < data_blocks; i++) {
                    for (weight = 0; weight < 6; weight++) {
                        if (adjusted_string.charAt((i * codeword_size) + weight) == '1') {
                            data_part[i] += (32 >> weight);
                        }
                    }
                }
                rs.init_gf(0x43);
                rs.init_code(ecc_blocks, 1);
                rs.encode(data_blocks, data_part);
                for (i = 0; i < ecc_blocks; i++) {
                    ecc_part[i] = rs.getResult(i);
                }
                for (i = (ecc_blocks - 1); i >= 0; i--) {
                    for (weight = 0x20; weight > 0; weight = weight >> 1) {
                        if ((ecc_part[i] & weight) != 0) {
                            adjusted_string += "1";
                        } else {
                            adjusted_string += "0";
                        }
                    }
                }
                break;
            case 8:
                for (i = 0; i < data_blocks; i++) {
                    for (weight = 0; weight < 8; weight++) {
                        if (adjusted_string.charAt((i * codeword_size) + weight) == '1') {
                            data_part[i] += (128 >> weight);
                        }
                    }
                }
                rs.init_gf(0x12d);
                rs.init_code(ecc_blocks, 1);
                rs.encode(data_blocks, data_part);
                for (i = 0; i < ecc_blocks; i++) {
                    ecc_part[i] = rs.getResult(i);
                }
                for (i = (ecc_blocks - 1); i >= 0; i--) {
                    for (weight = 0x80; weight > 0; weight = weight >> 1) {
                        if ((ecc_part[i] & weight) != 0) {
                            adjusted_string += "1";
                        } else {
                            adjusted_string += "0";
                        }
                    }
                }
                break;
            case 10:
                for (i = 0; i < data_blocks; i++) {
                    for (weight = 0; weight < 10; weight++) {
                        if (adjusted_string.charAt((i * codeword_size) + weight) == '1') {
                            data_part[i] += (512 >> weight);
                        }
                    }
                }
                rs.init_gf(0x409);
                rs.init_code(ecc_blocks, 1);
                rs.encode(data_blocks, data_part);
                for (i = 0; i < ecc_blocks; i++) {
                    ecc_part[i] = rs.getResult(i);
                }
                for (i = (ecc_blocks - 1); i >= 0; i--) {
                    for (weight = 0x200; weight > 0; weight = weight >> 1) {
                        if ((ecc_part[i] & weight) != 0) {
                            adjusted_string += "1";
                        } else {
                            adjusted_string += "0";
                        }
                    }
                }
                break;
            case 12:
                for (i = 0; i < data_blocks; i++) {
                    for (weight = 0; weight < 12; weight++) {
                        if (adjusted_string.charAt((i * codeword_size) + weight) == '1') {
                            data_part[i] += (2048 >> weight);
                        }
                    }
                }
                rs.init_gf(0x1069);
                rs.init_code(ecc_blocks, 1);
                rs.encode(data_blocks, data_part);
                for (i = 0; i < ecc_blocks; i++) {
                    ecc_part[i] = rs.getResult(i);
                }
                for (i = (ecc_blocks - 1); i >= 0; i--) {
                    for (weight = 0x800; weight > 0; weight = weight >> 1) {
                        if ((ecc_part[i] & weight) != 0) {
                            adjusted_string += "1";
                        } else {
                            adjusted_string += "0";
                        }
                    }
                }
                break;
        }

        /* Invert the data so that actual data is on the outside and reed-solomon on the inside */
        total_bits = (data_blocks + ecc_blocks) * codeword_size;
        for (i = 0; i < total_bits; i++) {
            bit_pattern += adjusted_string.charAt(total_bits - i - 1);
        }

        if (compact) {
            /* The first 2 bits represent the number of layers minus 1 */
            if (((layers - 1) & 0x02) != 0) {
                descriptor += '1';
            } else {
                descriptor += '0';
            }
            if (((layers - 1) & 0x01) != 0) {
                descriptor += '1';
            } else {
                descriptor += '0';
            }
            /* The next 6 bits represent the number of data blocks minus 1 */
            if (readerInit) {
                descriptor += '1';
            } else {
                if (((data_blocks - 1) & 0x20) != 0) {
                    descriptor += '1';
                } else {
                    descriptor += '0';
                }
            }
            for (i = 0x10; i > 0; i = i >> 1) {
                if (((data_blocks - 1) & i) != 0) {
                    descriptor += '1';
                } else {
                    descriptor += '0';
                }
            }
            encodeInfo += "Mode Message: " + descriptor + "\n";
            j = 2;
        } else {
            /* The first 5 bits represent the number of layers minus 1 */
            for (i = 0x10; i > 0; i = i >> 1) {
                if (((layers - 1) & i) != 0) {
                    descriptor += '1';
                } else {
                    descriptor += '0';
                }
            }

            /* The next 11 bits represent the number of data blocks minus 1 */
            if (readerInit) {
                descriptor += '1';
            } else {
                if (((data_blocks - 1) & 0x400) != 0) {
                    descriptor += '1';
                } else {
                    descriptor += '0';
                }
            }
            for (i = 0x200; i > 0; i = i >> 1) {
                if (((data_blocks - 1) & i) != 0) {
                    descriptor += '1';
                } else {
                    descriptor += '0';
                }
            }

            encodeInfo += "Mode Message: " + descriptor + "\n";
            j = 4;
        }

        /* Split into 4-bit codewords */
        for (i = 0; i < j; i++) {
            for (weight = 0; weight < 4; weight++) {
                if (descriptor.charAt((i * 4) + weight) == '1') {
                    desc_data[i] += (8 >> weight);
                }
            }
        }

        /* Add reed-solomon error correction with Galois field GF(16) and prime modulus
         x^4 + x + 1 (section 7.2.3)*/
        rs2.init_gf(0x13);
        if (compact) {
            rs2.init_code(5, 1);
            rs2.encode(2, desc_data);
            for (j = 0; j < 5; j++) {
                desc_ecc[j] = rs2.getResult(j);
            }
            for (i = 0; i < 5; i++) {
                for (weight = 0x08; weight > 0; weight = weight >> 1) {
                    if ((desc_ecc[4 - i] & weight) != 0) {
                        descriptor += '1';
                    } else {
                        descriptor += '0';
                    }
                }
            }
        } else {
            rs2.init_code(6, 1);
            rs2.encode(4, desc_data);
            for (j = 0; j < 6; j++) {
                desc_ecc[j] = rs2.getResult(j);
            }
            for (i = 0; i < 6; i++) {
                for (weight = 0x08; weight > 0; weight = weight >> 1) {
                    if ((desc_ecc[5 - i] & weight) != 0) {
                        descriptor += '1';
                    } else {
                        descriptor += '0';
                    }
                }
            }
        }

        readable = "";

        /* Plot all of the data into the symbol in pre-defined spiral pattern */
        if (compact) {

            row_count = 27 - (2 * AZTEC_COMPACT_OFFSET[layers - 1]);
            row_height = new int[row_count];
            row_height[0] = -1;
            pattern = new String[row_count];
            bin = "";
            for (y = AZTEC_COMPACT_OFFSET[layers - 1]; y < (27 - AZTEC_COMPACT_OFFSET[layers - 1]); y++) {
                for (x = AZTEC_COMPACT_OFFSET[layers - 1]; x < (27 - AZTEC_COMPACT_OFFSET[layers - 1]); x++) {
                    j = COMPACT_AZTEC_MAP[(y * 27) + x];

                    if (j == 0) {
                        bin += "0";
                    }
                    if (j == 1) {
                        bin += "1";
                    }

                    if (j >= 2) {
                        if ((j - 2) < bit_pattern.length()) {
                            bin += bit_pattern.charAt(j - 2);
                        } else {
                            if (j > 2000) {
                                bin += descriptor.charAt(j - 2000);
                            } else {
                                bin += "0";
                            }
                        }
                    }
                }
                row_height[y - AZTEC_COMPACT_OFFSET[layers - 1]] = 1;
                pattern[y - AZTEC_COMPACT_OFFSET[layers - 1]] = bin2pat(bin);
                bin = "";
            }

        } else {
            row_count = 151 - (2 * AZTEC_OFFSET[layers - 1]);
            row_height = new int[row_count];
            row_height[0] = -1;
            pattern = new String[row_count];
            bin = "";
            for (y = AZTEC_OFFSET[layers - 1]; y < (151 - AZTEC_OFFSET[layers - 1]); y++) {
                for (x = AZTEC_OFFSET[layers - 1]; x < (151 - AZTEC_OFFSET[layers - 1]); x++) {
                    j = AZTEC_MAP[x][y];
                    if (j == 1) {
                        bin += "1";
                    }
                    if (j == 0) {
                        bin += "0";
                    }
                    if (j >= 2) {
                        if ((j - 2) < bit_pattern.length()) {
                            bin += bit_pattern.charAt(j - 2);
                        } else {
                            if (j > 20000) {
                                bin += descriptor.charAt(j - 20000);
                            } else {
                                bin += "0";
                            }
                        }
                    }
                }
                row_height[y - AZTEC_OFFSET[layers - 1]] = 1;
                pattern[y - AZTEC_OFFSET[layers - 1]] = bin2pat(bin);
                bin = "";
            }
        }
    }

    private String generateAztecBinary() {

        /* Encode input data into a binary string */
        int i, j, k, bytes;
        int curtable, newtable, lasttable, chartype, maplength, blocks;
        int[] charmap = new int[2 * inputBytes.length];
        int[] typemap = new int[2 * inputBytes.length];
        int[] blockType = new int[inputBytes.length + 1];
        int[] blockLength = new int[inputBytes.length + 1];
        int weight;

        /* Lookup input string in encoding table */
        maplength = 0;

        if (inputDataType == DataType.GS1) {
            /* Add FNC1 to beginning of GS1 messages */
            charmap[maplength] = 0; // FLG
            typemap[maplength++] = 8; // PUNC
            charmap[maplength] = 400; // (0)
            typemap[maplength++] = 8; // PUNC
        }

        if (eciMode != 3) {
            int flagNumber;

            charmap[maplength] = 0; // FLG
            typemap[maplength++] = 8; // PUNC

            flagNumber = 6;

            if (eciMode < 100000) {
                flagNumber = 5;
            }

            if (eciMode < 10000) {
                flagNumber = 4;
            }

            if (eciMode < 1000) {
                flagNumber = 3;
            }

            if (eciMode < 100) {
                flagNumber = 2;
            }

            if (eciMode < 10) {
                flagNumber = 1;
            }

            charmap[maplength] = 400 + flagNumber;
            typemap[maplength++] = 8; // PUNC
        }

        for (i = 0; i < inputBytes.length; i++) {
            if ((inputDataType == DataType.GS1) && ((inputBytes[i] & 0xFF) == '[')) {
                /* FNC1 represented by FLG(0) */
                charmap[maplength] = 0; // FLG
                typemap[maplength++] = 8; // PUNC
                charmap[maplength] = 400; // (0)
                typemap[maplength++] = 8; // PUNC
            } else {
                if (((inputBytes[i] & 0xFF) > 0x7F) || ((inputBytes[i] & 0xFF) == 0x00)) {
                    charmap[maplength] = (inputBytes[i] & 0xFF);
                    typemap[maplength++] = 32; //BINARY
                } else {
                    charmap[maplength] = AZTEC_SYMBOL_CHAR[(inputBytes[i] & 0xFF)];
                    typemap[maplength++] = AZTEC_CODE_SET[(inputBytes[i] & 0xFF)];
                }
            }
        }

        /* Look for double character encoding possibilities */
        i = 0;
        do {
            if (((charmap[i] == 300) && (charmap[i + 1] == 11)) && ((typemap[i] == 8 /*PUNC */) && (typemap[i + 1] == 8 /*PUNC*/))) {
                /* CR LF combination */
                charmap[i] = 2;
                typemap[i] = 8; // PUNC
                if ((i + 1) != maplength) {
                    for (j = i + 1; j < maplength; j++) {
                        charmap[j] = charmap[j + 1];
                        typemap[j] = typemap[j + 1];
                    }
                }
                maplength--;
            }

            if (((charmap[i] == 302) && (charmap[i + 1] == 1)) && ((typemap[i] == 24) && (typemap[i + 1] == 23))) {
                /* . SP combination */
                charmap[i] = 3;
                typemap[i] = 8; // PUNC;
                if ((i + 1) != maplength) {
                    for (j = i + 1; j < maplength; j++) {
                        charmap[j] = charmap[j + 1];
                        typemap[j] = typemap[j + 1];
                    }
                }
                maplength--;
            }

            if (((charmap[i] == 301) && (charmap[i + 1] == 1)) && ((typemap[i] == 24) && (typemap[i + 1] == 23))) {
                /* , SP combination */
                charmap[i] = 4;
                typemap[i] = 8; //PUNC;
                if ((i + 1) != maplength) {
                    for (j = i + 1; j < maplength; j++) {
                        charmap[j] = charmap[j + 1];
                        typemap[j] = typemap[j + 1];
                    }
                }
                maplength--;
            }

            if (((charmap[i] == 21) && (charmap[i + 1] == 1)) && ((typemap[i] == 8) && (typemap[i + 1] == 23))) {
                /* : SP combination */
                charmap[i] = 5;
                typemap[i] = 8; //PUNC;
                if ((i + 1) != maplength) {
                    for (j = i + 1; j < maplength; j++) {
                        charmap[j] = charmap[j + 1];
                        typemap[j] = typemap[j + 1];
                    }
                }
                maplength--;
            }

            i++;
        } while (i < (maplength - 1));

        /* look for blocks of characters which use the same table */
        blocks = 1;
        blockType[0] = typemap[0];
        blockLength[0] = 1;
        for (i = 1; i < maplength; i++) {
            if (typemap[i] == typemap[i - 1]) {
                blockLength[blocks - 1]++;
            } else {
                blocks++;
                blockType[blocks - 1] = typemap[i];
                blockLength[blocks - 1] = 1;
            }
        }

        if ((blockType[0] & 1) != 0) {
            blockType[0] = 1;
        }
        if ((blockType[0] & 2) != 0) {
            blockType[0] = 2;
        }
        if ((blockType[0] & 4) != 0) {
            blockType[0] = 4;
        }
        if ((blockType[0] & 8) != 0) {
            blockType[0] = 8;
        }

        if (blocks > 1) {

            /* look for adjacent blocks which can use the same table (left to right search) */
            for (i = 1; i < blocks; i++) {
                if ((blockType[i] & blockType[i - 1]) != 0) {
                    blockType[i] = (blockType[i] & blockType[i - 1]);
                }
            }

            if ((blockType[blocks - 1] & 1) != 0) {
                blockType[blocks - 1] = 1;
            }
            if ((blockType[blocks - 1] & 2) != 0) {
                blockType[blocks - 1] = 2;
            }
            if ((blockType[blocks - 1] & 4) != 0) {
                blockType[blocks - 1] = 4;
            }
            if ((blockType[blocks - 1] & 8) != 0) {
                blockType[blocks - 1] = 8;
            }

            /* look for adjacent blocks which can use the same table (right to left search) */
            for (i = blocks - 2; i > 0; i--) {
                if ((blockType[i] & blockType[i + 1]) != 0) {
                    blockType[i] = (blockType[i] & blockType[i + 1]);
                }
            }

            /* determine the encoding table for characters which do not fit with adjacent blocks */
            for (i = 1; i < blocks; i++) {
                if ((blockType[i] & 8) != 0) {
                    blockType[i] = 8;
                }
                if ((blockType[i] & 4) != 0) {
                    blockType[i] = 4;
                }
                if ((blockType[i] & 2) != 0) {
                    blockType[i] = 2;
                }
                if ((blockType[i] & 1) != 0) {
                    blockType[i] = 1;
                }
            }

            /* if less than 4 characters are preceeded and followed by binary blocks
               then it is more efficient to also encode these in binary
            */

//            for (i = 1; i < blocks - 1; i++) {
//                if ((blockType[i - 1] == 32) && (blockLength[i] < 4)) {
//                    int nonBinaryLength = blockLength[i];
//                    for (int l = i; ((l < blocks) && (blockType[l] != 32)); l++) {
//                        nonBinaryLength += blockLength[l];
//                    }
//                    if (nonBinaryLength < 4) {
//                        blockType[i] = 32;
//                    }
//                }
//            }

            /* Combine blocks of the same type */
            i = 0;
            do {
                if (blockType[i] == blockType[i + 1]) {
                    blockLength[i] += blockLength[i + 1];
                    for (j = i + 1; j < blocks - 1; j++) {
                        blockType[j] = blockType[j + 1];
                        blockLength[j] = blockLength[j + 1];
                    }
                    blocks--;
                } else {
                    i++;
                }
            } while (i < blocks - 1);
        }

        /* Put the adjusted block data back into typemap */
        j = 0;
        for (i = 0; i < blocks; i++) {
            if ((blockLength[i] < 3) && (blockType[i] != 32)) { /* Shift character(s) needed */

                for (k = 0; k < blockLength[i]; k++) {
                    typemap[j + k] = blockType[i] + 64;
                }
            } else { /* Latch character (or byte mode) needed */

                for (k = 0; k < blockLength[i]; k++) {
                    typemap[j + k] = blockType[i];
                }
            }
            j += blockLength[i];
        }

        /* Don't shift an initial capital letter */
        if (typemap[0] == 65) {
            typemap[0] = 1;
        }

        /* Problem characters (those that appear in different tables with different values) can now be resolved into their tables */
        for (i = 0; i < maplength; i++) {
            if ((charmap[i] >= 300) && (charmap[i] < 400)) {
                curtable = typemap[i];
                if (curtable > 64) {
                    curtable -= 64;
                }
                switch (charmap[i]) {
                    case 300:
                        /* Carriage Return */
                        switch (curtable) {
                            case 8:
                                charmap[i] = 1;
                                break; // PUNC
                            case 4:
                                charmap[i] = 14;
                                break; // PUNC
                        }
                        break;
                    case 301:
                        /* Comma */
                        switch (curtable) {
                            case 8:
                                charmap[i] = 17;
                                break; // PUNC
                            case 16:
                                charmap[i] = 12;
                                break; // DIGIT
                        }
                        break;
                    case 302:
                        /* Full Stop */
                        switch (curtable) {
                            case 8:
                                charmap[i] = 19;
                                break; // PUNC
                            case 16:
                                charmap[i] = 13;
                                break; // DIGIT
                        }
                        break;
                }
            }
        }

//        if (debug) {
//            System.out.printf("Charmap: ");
//            for (i = 0; i < maplength; i++) {
//                System.out.printf("%d ", charmap[i]);
//            }
//            System.out.printf("\n");
//
//            System.out.printf("Typemap: ");
//            for (i = 0; i < maplength; i++) {
//                System.out.printf("%d ", typemap[i]);
//            }
//            System.out.printf("\n");
//        }

        StringBuilder binaryString = new StringBuilder();

        encodeInfo += "Encoding: ";

        curtable = 1; /* start with 1 table */

        lasttable = 1;
        for (i = 0; i < maplength; i++) {
            newtable = curtable;
            if ((typemap[i] != curtable) && (charmap[i] < 400)) {
                /* Change table */
                if (curtable == 32) {
                    /* If ending binary mode the current table is the same as when entering binary mode */
                    curtable = lasttable;
                    newtable = lasttable;
                }
                if (typemap[i] > 64) {
                    /* Shift character */
                    switch (typemap[i]) {
                        case (64 + 1):
                            /* To UPPER */
                            switch (curtable) {
                                case 2:
                                    /* US */
                                    binaryString.append(PENTBIT[28]);
                                    encodeInfo += "US ";
                                    break;
                                case 4:
                                    /* UL */
                                    binaryString.append(PENTBIT[29]);
                                    encodeInfo += "UL ";
                                    newtable = 1;
                                    break;
                                case 8:
                                    /* UL */
                                    binaryString.append(PENTBIT[31]);
                                    encodeInfo += "UL ";
                                    newtable = 1;
                                    break;
                                case 16:
                                    /* US */
                                    binaryString.append(QUADBIT[15]);
                                    encodeInfo += "US ";
                                    break;
                            }
                            break;
                        case (64 + 2):
                            /* To LOWER */
                            switch (curtable) {
                                case 1:
                                    /* LL */
                                    binaryString.append(PENTBIT[28]);
                                    encodeInfo += "LL ";
                                    newtable = 2;
                                    break;
                                case 4:
                                    /* LL */
                                    binaryString.append(PENTBIT[28]);
                                    encodeInfo += "LL ";
                                    newtable = 2;
                                    break;
                                case 8:
                                    /* UL LL */
                                    binaryString.append(PENTBIT[31]);
                                    encodeInfo += "UL ";
                                    binaryString.append(PENTBIT[28]);
                                    encodeInfo += "LL ";
                                    newtable = 2;
                                    break;
                                case 16:
                                    /* UL LL */
                                    binaryString.append(QUADBIT[14]);
                                    encodeInfo += "UL ";
                                    binaryString.append(PENTBIT[28]);
                                    encodeInfo += "LL ";
                                    newtable = 2;
                                    break;
                            }
                            break;
                        case (64 + 4):
                            /* To MIXED */
                            switch (curtable) {
                                case 1:
                                    /* ML */
                                    binaryString.append(PENTBIT[29]);
                                    encodeInfo += "ML ";
                                    newtable = 4;
                                    break;
                                case 2:
                                    /* ML */
                                    binaryString.append(PENTBIT[29]);
                                    encodeInfo += "ML ";
                                    newtable = 4;
                                    break;
                                case 8:
                                    /* UL ML */
                                    binaryString.append(PENTBIT[31]);
                                    encodeInfo += "UL ";
                                    binaryString.append(PENTBIT[29]);
                                    encodeInfo += "ML ";
                                    newtable = 4;
                                    break;
                                case 16:
                                    /* UL ML */
                                    binaryString.append(QUADBIT[14]);
                                    encodeInfo += "UL ";
                                    binaryString.append(PENTBIT[29]);
                                    encodeInfo += "ML ";
                                    newtable = 4;
                                    break;
                            }
                            break;
                        case (64 + 8):
                            /* To PUNC */
                            switch (curtable) {
                                case 1:
                                    /* PS */
                                    binaryString.append(PENTBIT[0]);
                                    encodeInfo += "PS ";
                                    break;
                                case 2:
                                    /* PS */
                                    binaryString.append(PENTBIT[0]);
                                    encodeInfo += "PS ";
                                    break;
                                case 4:
                                    /* PS */
                                    binaryString.append(PENTBIT[0]);
                                    encodeInfo += "PS ";
                                    break;
                                case 16:
                                    /* PS */
                                    binaryString.append(QUADBIT[0]);
                                    encodeInfo += "PS ";
                                    break;
                            }
                            break;
                        case (64 + 16):
                            /* To DIGIT */
                            switch (curtable) {
                                case 1:
                                    /* DL */
                                    binaryString.append(PENTBIT[30]);
                                    encodeInfo += "DL ";
                                    newtable = 16;
                                    break;
                                case 2:
                                    /* DL */
                                    binaryString.append(PENTBIT[30]);
                                    encodeInfo += "DL ";
                                    newtable = 16;
                                    break;
                                case 4:
                                    /* UL DL */
                                    binaryString.append(PENTBIT[29]);
                                    encodeInfo += "UL ";
                                    binaryString.append(PENTBIT[30]);
                                    encodeInfo += "DL ";
                                    newtable = 16;
                                    break;
                                case 8:
                                    /* UL DL */
                                    binaryString.append(PENTBIT[31]);
                                    encodeInfo += "UL ";
                                    binaryString.append(PENTBIT[30]);
                                    encodeInfo += "DL ";
                                    newtable = 16;
                                    break;
                            }
                            break;
                    }
                } else {
                    /* Latch character */
                    switch (typemap[i]) {
                        case 1:
                            /* To UPPER */
                            switch (curtable) {
                                case 2:
                                    /* ML UL */
                                    binaryString.append(PENTBIT[29]);
                                    encodeInfo += "ML ";
                                    binaryString.append(PENTBIT[29]);
                                    encodeInfo += "UL ";
                                    newtable = 1;
                                    break;
                                case 4:
                                    /* UL */
                                    binaryString.append(PENTBIT[29]);
                                    encodeInfo += "UL ";
                                    newtable = 1;
                                    break;
                                case 8:
                                    /* UL */
                                    binaryString.append(PENTBIT[31]);
                                    encodeInfo += "UL ";
                                    newtable = 1;
                                    break;
                                case 16:
                                    /* UL */
                                    binaryString.append(QUADBIT[14]);
                                    encodeInfo += "UL ";
                                    newtable = 1;
                                    break;
                            }
                            break;
                        case 2:
                            /* To LOWER */
                            switch (curtable) {
                                case 1:
                                    /* LL */
                                    binaryString.append(PENTBIT[28]);
                                    encodeInfo += "LL ";
                                    newtable = 2;
                                    break;
                                case 4:
                                    /* LL */
                                    binaryString.append(PENTBIT[28]);
                                    encodeInfo += "LL ";
                                    newtable = 2;
                                    break;
                                case 8:
                                    /* UL LL */
                                    binaryString.append(PENTBIT[31]);
                                    encodeInfo += "UL ";
                                    binaryString.append(PENTBIT[28]);
                                    encodeInfo += "LL ";
                                    newtable = 2;
                                    break;
                                case 16:
                                    /* UL LL */
                                    binaryString.append(QUADBIT[14]);
                                    encodeInfo += "UL ";
                                    binaryString.append(PENTBIT[28]);
                                    encodeInfo += "LL ";
                                    newtable = 2;
                                    break;
                            }
                            break;
                        case 4:
                            /* To MIXED */
                            switch (curtable) {
                                case 1:
                                    /* ML */
                                    binaryString.append(PENTBIT[29]);
                                    encodeInfo += "ML ";
                                    newtable = 4;
                                    break;
                                case 2:
                                    /* ML */
                                    binaryString.append(PENTBIT[29]);
                                    encodeInfo += "ML ";
                                    newtable = 4;
                                    break;
                                case 8:
                                    /* UL ML */
                                    binaryString.append(PENTBIT[31]);
                                    encodeInfo += "UL ";
                                    binaryString.append(PENTBIT[29]);
                                    encodeInfo += "ML ";
                                    newtable = 4;
                                    break;
                                case 16:
                                    /* UL ML */
                                    binaryString.append(QUADBIT[14]);
                                    encodeInfo += "UL ";
                                    binaryString.append(PENTBIT[29]);
                                    encodeInfo += "ML ";
                                    newtable = 4;
                                    break;
                            }
                            break;
                        case 8:
                            /* To PUNC */
                            switch (curtable) {
                                case 1:
                                    /* ML PL */
                                    binaryString.append(PENTBIT[29]);
                                    encodeInfo += "ML ";
                                    binaryString.append(PENTBIT[30]);
                                    encodeInfo += "PL ";
                                    newtable = 8;
                                    break;
                                case 2:
                                    /* ML PL */
                                    binaryString.append(PENTBIT[29]);
                                    encodeInfo += "ML ";
                                    binaryString.append(PENTBIT[30]);
                                    encodeInfo += "PL ";
                                    newtable = 8;
                                    break;
                                case 4:
                                    /* PL */
                                    binaryString.append(PENTBIT[30]);
                                    encodeInfo += "PL ";
                                    newtable = 8;
                                    break;
                                case 16:
                                    /* UL ML PL */
                                    binaryString.append(QUADBIT[14]);
                                    encodeInfo += "UL ";
                                    binaryString.append(PENTBIT[29]);
                                    encodeInfo += "ML ";
                                    binaryString.append(PENTBIT[30]);
                                    encodeInfo += "PL ";
                                    newtable = 8;
                                    break;
                            }
                            break;
                        case 16:
                            /* To DIGIT */
                            switch (curtable) {
                                case 1:
                                    /* DL */
                                    binaryString.append(PENTBIT[30]);
                                    encodeInfo += "DL ";
                                    newtable = 16;
                                    break;
                                case 2:
                                    /* DL */
                                    binaryString.append(PENTBIT[30]);
                                    encodeInfo += "DL ";
                                    newtable = 16;
                                    break;
                                case 4:
                                    /* UL DL */
                                    binaryString.append(PENTBIT[29]);
                                    encodeInfo += "UL ";
                                    binaryString.append(PENTBIT[30]);
                                    encodeInfo += "DL ";
                                    newtable = 16;
                                    break;
                                case 8:
                                    /* UL DL */
                                    binaryString.append(PENTBIT[31]);
                                    encodeInfo += "UL ";
                                    binaryString.append(PENTBIT[30]);
                                    encodeInfo += "DL ";
                                    newtable = 16;
                                    break;
                            }
                            break;
                        case 32:
                            /* To BINARY */
                            lasttable = curtable;
                            switch (curtable) {
                                case 1:
                                    /* BS */
                                    binaryString.append(PENTBIT[31]);
                                    encodeInfo += "BS ";
                                    newtable = 32;
                                    break;
                                case 2:
                                    /* BS */
                                    binaryString.append(PENTBIT[31]);
                                    encodeInfo += "BS ";
                                    newtable = 32;
                                    break;
                                case 4:
                                    /* BS */
                                    binaryString.append(PENTBIT[31]);
                                    encodeInfo += "BS ";
                                    newtable = 32;
                                    break;
                                case 8:
                                    /* UL BS */
                                    binaryString.append(PENTBIT[31]);
                                    encodeInfo += "UL ";
                                    binaryString.append(PENTBIT[31]);
                                    encodeInfo += "BS ";
                                    lasttable = 1;
                                    newtable = 32;
                                    break;
                                case 16:
                                    /* UL BS */
                                    binaryString.append(QUADBIT[14]);
                                    encodeInfo += "UL ";
                                    binaryString.append(PENTBIT[31]);
                                    encodeInfo += "BS ";
                                    lasttable = 1;
                                    newtable = 32;
                                    break;
                            }

                            bytes = 0;
                            do {
                                bytes++;
                            } while (typemap[i + (bytes - 1)] == 32);
                            bytes--;

                            if (bytes > 2079) {
                                throw new OkapiException("Input too long");
                            }

                            if (bytes > 31) { /* Put 00000 followed by 11-bit number of bytes less 31 */

                                binaryString.append("00000");
                                for (weight = 0x400; weight > 0; weight = weight >> 1) {
                                    if (((bytes - 31) & weight) != 0) {
                                        binaryString.append("1");
                                    } else {
                                        binaryString.append("0");
                                    }
                                }
                            } else { /* Put 5-bit number of bytes */

                                for (weight = 0x10; weight > 0; weight = weight >> 1) {
                                    if ((bytes & weight) != 0) {
                                        binaryString.append("1");
                                    } else {
                                        binaryString.append("0");
                                    }
                                }
                            }

                            break;
                    }
                }
            }
            /* Add data to the binary string */
            curtable = newtable;
            chartype = typemap[i];
            if (chartype > 64) {
                chartype -= 64;
            }
            switch (chartype) {
                case 1:
                case 2:
                case 4:
                case 8:
                    if (charmap[i] >= 400) {
                        encodeInfo += "FLG(" + Integer.toString(charmap[i] - 400) + ") ";
                        binaryString.append(TRIBIT[charmap[i] - 400]);
                        if (charmap[i] != 400) {
                            /* ECI */
                            binaryString.append(eciToBinary());
                        }
                    } else {
                        binaryString.append(PENTBIT[charmap[i]]);
                        encodeInfo += Integer.toString(charmap[i]) + " ";
                    }
                    break;
                case 16:
                    binaryString.append(QUADBIT[charmap[i]]);
                    encodeInfo += Integer.toString(charmap[i]) + " ";
                    break;
                case 32:
                    for (weight = 0x80; weight > 0; weight = weight >> 1) {
                        if ((charmap[i] & weight) != 0) {
                            binaryString.append("1");
                        } else {
                            binaryString.append("0");
                        }
                    }
                    encodeInfo += Integer.toString(charmap[i]) + " ";
                    break;
            }

        }

        encodeInfo += "\n";

        return binaryString.toString();
    }

    private String eciToBinary() {
        String binary = "";
        String eciNumber = Integer.toString(eciMode);
        int i;

        for (i = 0; i < eciNumber.length(); i++) {
            binary += QUADBIT[(eciNumber.charAt(i) - '0') + 2];
            encodeInfo += Character.toString(eciNumber.charAt(i)) + " ";
        }

        return binary;
    }
}
