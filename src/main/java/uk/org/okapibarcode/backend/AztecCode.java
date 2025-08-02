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

import static java.nio.charset.StandardCharsets.US_ASCII;
import static uk.org.okapibarcode.util.Arrays.insertArray;

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

    /** The types of Aztec Code symbol sizes allowed.  */
    public enum Mode {
        /** Allow only normal Aztec Code symbol sizes. */
        NORMAL,
        /** Allow only compact Aztec Code symbol sizes. */
        COMPACT,
        /** Allow both normal and compact Aztec Code symbol sizes. */
        ANY;
    }

    /* 27 x 27 data grid */
    private static final int[] COMPACT_AZTEC_MAP = {
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

    /* From Table 2:
     *
     * 1 = upper
     * 2 = lower
     * 4 = mixed
     * 8 = punctuation
     * 16 = digits
     * 32 = binary
     *
     * Values can be OR'ed, so e.g. 12 = 4 | 8, and 23 = 1 | 2 | 4 | 16
     */
    private static final int[] AZTEC_CODE_SET = {
        32, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 12, 32, 32, 32, 32, 32, 32,
        32, 32, 32, 32, 32, 32, 32, 4, 4, 4, 4, 4, 23, 8, 8, 8, 8, 8, 8, 8,
        8, 8, 8, 8, 24, 8, 24, 8, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 8, 8,
        8, 8, 8, 8, 4, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 8, 4, 8, 4, 4, 4, 2, 2, 2,
        2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
        2, 2, 2, 8, 4, 8, 4, 4
    };

    /* From Table 2 */
    private static final int[] AZTEC_SYMBOL_CHAR = {
        0, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 300, 14, 15, 16, 17, 18, 19,
        20, 21, 22, 23, 24, 25, 26, 15, 16, 17, 18, 19, 1, 6, 7, 8, 9, 10, 11, 12,
        13, 14, 15, 16, 301, 18, 302, 20, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 21, 22,
        23, 24, 25, 26, 20, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16,
        17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 27, 21, 28, 22, 23, 24, 2, 3, 4,
        5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24,
        25, 26, 27, 29, 25, 30, 26, 27
    };

    /* Problem characters are:
     * 300: Carriage Return (ASCII 13)
     * 301: Comma (ASCII 44)
     * 302: Full Stop (ASCII 46)
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

    /* Codewords per symbol */
    private static final int[] AZTEC_SIZES = {
        21, 48, 60, 88, 120, 156, 196, 240, 230, 272, 316, 364, 416, 470, 528, 588, 652, 720, 790,
        864, 940, 1020, 920, 992, 1066, 1144, 1224, 1306, 1392, 1480, 1570, 1664
    };

    private static final int[] AZTEC_COMPACT_SIZES = {
        17, 40, 51, 76
    };

    /* Data bits per symbol maximum with 10% error correction */
    private static final int[] AZTEC_10_DATA_SIZES = {
        96, 246, 408, 616, 840, 1104, 1392, 1704, 2040, 2420, 2820, 3250, 3720, 4200, 4730,
        5270, 5840, 6450, 7080, 7750, 8430, 9150, 9900, 10680, 11484, 12324, 13188, 14076,
        15000, 15948, 16920, 17940
    };

    /* Data bits per symbol maximum with 23% error correction */
    private static final int[] AZTEC_23_DATA_SIZES = {
        84, 204, 352, 520, 720, 944, 1184, 1456, 1750, 2070, 2410, 2780, 3180, 3590, 4040,
        4500, 5000, 5520, 6060, 6630, 7210, 7830, 8472, 9132, 9816, 10536, 11280, 12036,
        12828, 13644, 14472, 15348
    };

    /* Data bits per symbol maximum with 36% error correction */
    private static final int[] AZTEC_36_DATA_SIZES = {
        66, 168, 288, 432, 592, 776, 984, 1208, 1450, 1720, 2000, 2300, 2640, 2980, 3350,
        3740, 4150, 4580, 5030, 5500, 5990, 6500, 7032, 7584, 8160, 8760, 9372, 9996, 10656,
        11340, 12024, 12744
    };

    /* Data bits per symbol maximum with 50% error correction */
    private static final int[] AZTEC_50_DATA_SIZES = {
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

    private Mode mode;
    private int preferredSize = 0;
    private int preferredEccLevel = 2;
    private String structuredAppendMessageId;
    private int structuredAppendPosition = 1;
    private int structuredAppendTotal = 1;

    /**
     * Creates a new instance, using mode {@link Mode#ANY}.
     */
    public AztecCode() {
        this(Mode.ANY);
    }

    /**
     * Creates a new instance, using the specified mode.
     *
     * @param mode whether to allow normal sizes, compact sizes, or all sizes
     */
    public AztecCode(Mode mode) {
        this.mode = mode;
        this.humanReadableLocation = HumanReadableLocation.NONE;
    }

    /**
     * Sets the mode (normal sizes only, compact sizes only, or all sizes).
     *
     * @param mode the mode (normal sizes only, compact sizes only, or all sizes)
     */
    public void setMode(Mode mode) {
        this.mode = mode;
    }

    /**
     * Returns the mode (normal sizes only, compact sizes only, or all sizes).
     *
     * @return the mode (normal sizes only, compact sizes only, or all sizes)
     */
    public Mode getMode() {
        return mode;
    }

    /**
     * <p>Sets a preferred symbol size. This value may be ignored if data string is
     * too large to fit in the specified symbol size. Values correspond to symbol
     * sizes as shown in the following table:
     *
     * <table>
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
     * <p> Note that sizes 1 to 4 are the "compact" Aztec Code symbols; sizes 5 to 36
     * are the "full-range" Aztec Code symbols.
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
     * Returns the preferred symbol size.
     *
     * @return the preferred symbol size
     */
    public int getPreferredSize() {
        return preferredSize;
    }

    /**
     * Sets the preferred minimum amount of symbol space dedicated to error
     * correction. This value will be ignored if a symbol size has been set by
     * <code>setPreferredSize</code>. Valid options are:
     *
     * <table>
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

    /**
     * Returns the preferred error correction level.
     *
     * @return the preferred error correction level
     */
    public int getPreferredEccLevel() {
        return preferredEccLevel;
    }

    /**
     * If this Aztec Code symbol is part of a series of Aztec Code symbols appended in a structured
     * format, this method sets the position of this symbol in the series. Valid values are 1 through
     * 26 inclusive.
     *
     * @param position the position of this Aztec Code symbol in the structured append series
     */
    public void setStructuredAppendPosition(int position) {
        if (position < 1 || position > 26) {
            throw new IllegalArgumentException("Invalid Aztec Code structured append position: " + position);
        }
        this.structuredAppendPosition = position;
    }

    /**
     * Returns the position of this Aztec Code symbol in a series of symbols using structured append.
     * If this symbol is not part of such a series, this method will return <code>1</code>.
     *
     * @return the position of this Aztec Code symbol in a series of symbols using structured append
     */
    public int getStructuredAppendPosition() {
        return structuredAppendPosition;
    }

    /**
     * If this Aztec Code symbol is part of a series of Aztec Code symbols appended in a structured
     * format, this method sets the total number of symbols in the series. Valid values are
     * 1 through 26 inclusive. A value of 1 indicates that this symbol is not part of a structured
     * append series.
     *
     * @param total the total number of Aztec Code symbols in the structured append series
     */
    public void setStructuredAppendTotal(int total) {
        if (total < 1 || total > 26) {
            throw new IllegalArgumentException("Invalid Aztec Code structured append total: " + total);
        }
        this.structuredAppendTotal = total;
    }

    /**
     * Returns the size of the series of Aztec Code symbols using structured append that this symbol
     * is part of. If this symbol is not part of a structured append series, this method will return
     * <code>1</code>.
     *
     * @return size of the series that this symbol is part of
     */
    public int getStructuredAppendTotal() {
        return structuredAppendTotal;
    }

    /**
     * If this Aztec Code symbol is part of a series of Aztec Code symbols appended in a structured format,
     * this method sets the unique message ID for the series. Values may not contain spaces and must contain
     * only printable ASCII characters. Message IDs are optional.
     *
     * @param messageId the unique message ID for the series that this symbol is part of
     */
    public void setStructuredAppendMessageId(String messageId) {
        if (messageId != null && !messageId.matches("^[\\x21-\\x7F]+$")) {
            throw new IllegalArgumentException("Invalid Aztec Code structured append message ID: " + messageId);
        }
        this.structuredAppendMessageId = messageId;
    }

    /**
     * Returns the unique message ID of the series of Aztec Code symbols using structured append that this
     * symbol is part of. If this symbol is not part of a structured append series, this method will return
     * <code>null</code>.
     *
     * @return the unique message ID for the series that this symbol is part of
     */
    public String getStructuredAppendMessageId() {
        return structuredAppendMessageId;
    }

    @Override
    public boolean supportsGs1() {
        return true;
    }

    @Override
    public boolean supportsEci() {
        return true;
    }

    @Override
    protected void encode() {

        int layers;
        boolean compact;
        StringBuilder adjustedString;

        eciProcess(); // Get ECI mode

        /* Optional structured append (Section 8 of spec) */
        /* ML + UL start flag handled later, not part of data */
        if (structuredAppendTotal != 1) {
            StringBuilder prefix = new StringBuilder();
            if (structuredAppendMessageId != null) {
                prefix.append(' ').append(structuredAppendMessageId).append(' ');
            }
            prefix.append((char) (structuredAppendPosition + 64)); // 1-26 as A-Z
            prefix.append((char) (structuredAppendTotal + 64)); // 1-26 as A-Z
            int[] prefixArray = toBytes(prefix.toString(), US_ASCII);
            inputData = insertArray(inputData, 0, prefixArray);
        }

        String binaryString = generateAztecBinary();
        int dataLength = binaryString.length();

        if (preferredSize == 0) {

            /* The size of the symbol can be determined by Okapi */

            int dataMaxSize = 0;
            int compLoop = (readerInit ? 1 : 4);

            do {
                /* Decide what size symbol to use - the smallest that fits the data */

                int[] dataSizes;
                int[] compactDataSizes;

                switch (preferredEccLevel) {
                    /* For each level of error correction work out the smallest symbol which the data will fit in */
                    case 1:
                        dataSizes = AZTEC_10_DATA_SIZES;
                        compactDataSizes = AZTEC_COMPACT_10_DATA_SIZES;
                        break;
                    case 2:
                        dataSizes = AZTEC_23_DATA_SIZES;
                        compactDataSizes = AZTEC_COMPACT_23_DATA_SIZES;
                        break;
                    case 3:
                        dataSizes = AZTEC_36_DATA_SIZES;
                        compactDataSizes = AZTEC_COMPACT_36_DATA_SIZES;
                        break;
                    case 4:
                        dataSizes = AZTEC_50_DATA_SIZES;
                        compactDataSizes = AZTEC_COMPACT_50_DATA_SIZES;
                        break;
                    default:
                        throw new OkapiInputException("Unrecognized ECC level: " + preferredEccLevel);
                }

                layers = 0;
                compact = false;

                if (mode == Mode.NORMAL || mode == Mode.ANY) {
                    for (int i = 32; i > 0; i--) {
                        if (dataLength < dataSizes[i - 1]) {
                            layers = i;
                            compact = false;
                            dataMaxSize = dataSizes[i - 1];
                        }
                    }
                }

                if (mode == Mode.COMPACT || mode == Mode.ANY) {
                    for (int i = compLoop; i > 0; i--) {
                        if (dataLength < compactDataSizes[i - 1]) {
                            layers = i;
                            compact = true;
                            dataMaxSize = compactDataSizes[i - 1];
                        }
                    }
                }

                if (layers == 0) {
                    /* Couldn't find a symbol which fits the data */
                    throw new OkapiInputException("Input too long (too many bits for selected ECC)");
                }

                adjustedString = adjustBinaryString(binaryString, compact, layers);
                dataLength = adjustedString.length();

            } while (dataLength > dataMaxSize);
            /* This loop will only repeat on the rare occasions when the rule about not having all 1s or all 0s
             means that the binary string has had to be lengthened beyond the maximum number of bits that can
             be encoded in a symbol of the selected size */

        } else {

            /* The size of the symbol has been specified by the user */

            if (preferredSize >= 1 && preferredSize <= 4) {
                compact = true;
                layers = preferredSize;
            } else {
                compact = false;
                layers = preferredSize - 4;
            }

            if ((compact && mode == Mode.NORMAL) || (!compact && mode == Mode.COMPACT)) {
                throw new OkapiInputException("Aztec mode " + mode + " and preferred size " + preferredSize + " are incompatible");
            }

            adjustedString = adjustBinaryString(binaryString, compact, layers);

            /* Check if the data actually fits into the selected symbol size */
            int codewordSize = getCodewordSize(layers);
            int[] sizes = (compact ? AZTEC_COMPACT_SIZES : AZTEC_SIZES);
            int dataMaxSize = codewordSize * (sizes[layers - 1] - 3);
            if (adjustedString.length() > dataMaxSize) {
                throw new OkapiInputException("Data too long for specified Aztec Code symbol size");
            }
        }

        if (readerInit && compact && layers > 1) {
            throw new OkapiInputException("Symbol is too large for reader initialization");
        }

        if (readerInit && layers > 22) {
            throw new OkapiInputException("Symbol is too large for reader initialization");
        }

        int codewordSize = getCodewordSize(layers);
        int dataBlocks = adjustedString.length() / codewordSize;

        int eccBlocks;
        if (compact) {
            eccBlocks = AZTEC_COMPACT_SIZES[layers - 1] - dataBlocks;
        } else {
            eccBlocks = AZTEC_SIZES[layers - 1] - dataBlocks;
        }

        infoLine("Compact Mode: ", compact);
        infoLine("Layers: ", layers);
        infoLine("Codeword Length: ", codewordSize + " bits");
        infoLine("Data Codewords: ", dataBlocks);
        infoLine("ECC Codewords: ", eccBlocks);

        /* Add ECC data to the adjusted string */
        addErrorCorrection(adjustedString, codewordSize, dataBlocks, eccBlocks);

        /* Invert the data so that actual data is on the outside and reed-solomon on the inside */
        for (int i = 0; i < adjustedString.length() / 2; i++) {
            int mirror = adjustedString.length() - i - 1;
            char c = adjustedString.charAt(i);
            adjustedString.setCharAt(i, adjustedString.charAt(mirror));
            adjustedString.setCharAt(mirror, c);
        }

        /* Create the descriptor / mode message */
        String descriptor = createDescriptor(compact, layers, dataBlocks);

        /* Plot all of the data into the symbol in pre-defined spiral pattern */
        if (compact) {

            readable = "";
            row_count = 27 - (2 * AZTEC_COMPACT_OFFSET[layers - 1]);
            row_height = new int[row_count];
            row_height[0] = -1;
            pattern = new String[row_count];
            for (int y = AZTEC_COMPACT_OFFSET[layers - 1]; y < (27 - AZTEC_COMPACT_OFFSET[layers - 1]); y++) {
                StringBuilder bin = new StringBuilder(27);
                for (int x = AZTEC_COMPACT_OFFSET[layers - 1]; x < (27 - AZTEC_COMPACT_OFFSET[layers - 1]); x++) {
                    int j = COMPACT_AZTEC_MAP[(y * 27) + x];
                    if (j == 0) {
                        bin.append('0');
                    }
                    if (j == 1) {
                        bin.append('1');
                    }
                    if (j >= 2) {
                        if (j - 2 < adjustedString.length()) {
                            bin.append(adjustedString.charAt(j - 2));
                        } else {
                            if (j >= 2000) {
                                bin.append(descriptor.charAt(j - 2000));
                            } else {
                                bin.append('0');
                            }
                        }
                    }
                }
                row_height[y - AZTEC_COMPACT_OFFSET[layers - 1]] = moduleWidth;
                pattern[y - AZTEC_COMPACT_OFFSET[layers - 1]] = bin2pat(bin);
            }

        } else {

            readable = "";
            row_count = 151 - (2 * AZTEC_OFFSET[layers - 1]);
            row_height = new int[row_count];
            row_height[0] = -1;
            pattern = new String[row_count];
            for (int y = AZTEC_OFFSET[layers - 1]; y < (151 - AZTEC_OFFSET[layers - 1]); y++) {
                StringBuilder bin = new StringBuilder(151);
                for (int x = AZTEC_OFFSET[layers - 1]; x < (151 - AZTEC_OFFSET[layers - 1]); x++) {
                    int j = AZTEC_MAP[x][y];
                    if (j == 1) {
                        bin.append('1');
                    }
                    if (j == 0) {
                        bin.append('0');
                    }
                    if (j >= 2) {
                        if (j - 2 < adjustedString.length()) {
                            bin.append(adjustedString.charAt(j - 2));
                        } else {
                            if (j >= 20000) {
                                bin.append(descriptor.charAt(j - 20000));
                            } else {
                                bin.append('0');
                            }
                        }
                    }
                }
                row_height[y - AZTEC_OFFSET[layers - 1]] = moduleWidth;
                pattern[y - AZTEC_OFFSET[layers - 1]] = bin2pat(bin);
            }
        }
    }

    private String generateAztecBinary() {

        /* Encode input data into a binary string */
        int i, j, k, bytes;
        int curtable, newtable, lasttable, chartype, maplength, blocks;
        int[] charmap = new int[2 * inputData.length];
        int[] typemap = new int[2 * inputData.length];
        int[] blockType = new int[inputData.length + 1];
        int[] blockLength = new int[inputData.length + 1];

        /* Lookup input string in encoding table */
        maplength = 0;

        /* Add FNC1 to beginning of GS1 messages */
        if (inputDataType == DataType.GS1) {
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

        for (i = 0; i < inputData.length; i++) {
            if (inputData[i] == FNC1) {
                /* FNC1 represented by FLG(0) */
                charmap[maplength] = 0; // FLG
                typemap[maplength++] = 8; // PUNC
                charmap[maplength] = 400; // (0)
                typemap[maplength++] = 8; // PUNC
            } else {
                if ((inputData[i] > 0x7F) || (inputData[i] == 0x00)) {
                    charmap[maplength] = inputData[i];
                    typemap[maplength++] = 32; //BINARY
                } else {
                    charmap[maplength] = AZTEC_SYMBOL_CHAR[inputData[i]];
                    typemap[maplength++] = AZTEC_CODE_SET[inputData[i]];
                }
            }
        }

        /* Look for double character encoding possibilities */
        for (i = 0; i < (maplength - 1); i++) {
            if (((charmap[i] == 300) && (charmap[i + 1] == 11)) && ((typemap[i] == 12) && (typemap[i + 1] == 4))) {
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
        }

        /* look for blocks of characters which use the same table */
        blocks = 0;
        for (i = 0; i < maplength; i++) {
            if (i > 0 && typemap[i] == typemap[i - 1]) {
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

            /* if less than 4 characters are preceded and followed by binary blocks
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
        if (maplength > 0 && typemap[0] == 65) {
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

        StringBuilder binaryString = new StringBuilder();
        info("Encoding: ");
        curtable = 1; /* start with 1 table */
        lasttable = 1;

        /* Optional structured append start flag (Section 8 of spec) */
        if (structuredAppendTotal != 1) {
            binaryString.append(PENTBIT[29]);
            info("ML ");
            binaryString.append(PENTBIT[29]);
            info("UL ");
        }

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
                                    info("US ");
                                    break;
                                case 4:
                                    /* UL */
                                    binaryString.append(PENTBIT[29]);
                                    info("UL ");
                                    newtable = 1;
                                    break;
                                case 8:
                                    /* UL */
                                    binaryString.append(PENTBIT[31]);
                                    info("UL ");
                                    newtable = 1;
                                    break;
                                case 16:
                                    /* US */
                                    binaryString.append(QUADBIT[15]);
                                    info("US ");
                                    break;
                            }
                            break;
                        case (64 + 2):
                            /* To LOWER */
                            switch (curtable) {
                                case 1:
                                    /* LL */
                                    binaryString.append(PENTBIT[28]);
                                    info("LL ");
                                    newtable = 2;
                                    break;
                                case 4:
                                    /* LL */
                                    binaryString.append(PENTBIT[28]);
                                    info("LL ");
                                    newtable = 2;
                                    break;
                                case 8:
                                    /* UL LL */
                                    binaryString.append(PENTBIT[31]);
                                    info("UL ");
                                    binaryString.append(PENTBIT[28]);
                                    info("LL ");
                                    newtable = 2;
                                    break;
                                case 16:
                                    /* UL LL */
                                    binaryString.append(QUADBIT[14]);
                                    info("UL ");
                                    binaryString.append(PENTBIT[28]);
                                    info("LL ");
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
                                    info("ML ");
                                    newtable = 4;
                                    break;
                                case 2:
                                    /* ML */
                                    binaryString.append(PENTBIT[29]);
                                    info("ML ");
                                    newtable = 4;
                                    break;
                                case 8:
                                    /* UL ML */
                                    binaryString.append(PENTBIT[31]);
                                    info("UL ");
                                    binaryString.append(PENTBIT[29]);
                                    info("ML ");
                                    newtable = 4;
                                    break;
                                case 16:
                                    /* UL ML */
                                    binaryString.append(QUADBIT[14]);
                                    info("UL ");
                                    binaryString.append(PENTBIT[29]);
                                    info("ML ");
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
                                    info("PS ");
                                    break;
                                case 2:
                                    /* PS */
                                    binaryString.append(PENTBIT[0]);
                                    info("PS ");
                                    break;
                                case 4:
                                    /* PS */
                                    binaryString.append(PENTBIT[0]);
                                    info("PS ");
                                    break;
                                case 16:
                                    /* PS */
                                    binaryString.append(QUADBIT[0]);
                                    info("PS ");
                                    break;
                            }
                            break;
                        case (64 + 16):
                            /* To DIGIT */
                            switch (curtable) {
                                case 1:
                                    /* DL */
                                    binaryString.append(PENTBIT[30]);
                                    info("DL ");
                                    newtable = 16;
                                    break;
                                case 2:
                                    /* DL */
                                    binaryString.append(PENTBIT[30]);
                                    info("DL ");
                                    newtable = 16;
                                    break;
                                case 4:
                                    /* UL DL */
                                    binaryString.append(PENTBIT[29]);
                                    info("UL ");
                                    binaryString.append(PENTBIT[30]);
                                    info("DL ");
                                    newtable = 16;
                                    break;
                                case 8:
                                    /* UL DL */
                                    binaryString.append(PENTBIT[31]);
                                    info("UL ");
                                    binaryString.append(PENTBIT[30]);
                                    info("DL ");
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
                                    info("ML ");
                                    binaryString.append(PENTBIT[29]);
                                    info("UL ");
                                    newtable = 1;
                                    break;
                                case 4:
                                    /* UL */
                                    binaryString.append(PENTBIT[29]);
                                    info("UL ");
                                    newtable = 1;
                                    break;
                                case 8:
                                    /* UL */
                                    binaryString.append(PENTBIT[31]);
                                    info("UL ");
                                    newtable = 1;
                                    break;
                                case 16:
                                    /* UL */
                                    binaryString.append(QUADBIT[14]);
                                    info("UL ");
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
                                    info("LL ");
                                    newtable = 2;
                                    break;
                                case 4:
                                    /* LL */
                                    binaryString.append(PENTBIT[28]);
                                    info("LL ");
                                    newtable = 2;
                                    break;
                                case 8:
                                    /* UL LL */
                                    binaryString.append(PENTBIT[31]);
                                    info("UL ");
                                    binaryString.append(PENTBIT[28]);
                                    info("LL ");
                                    newtable = 2;
                                    break;
                                case 16:
                                    /* UL LL */
                                    binaryString.append(QUADBIT[14]);
                                    info("UL ");
                                    binaryString.append(PENTBIT[28]);
                                    info("LL ");
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
                                    info("ML ");
                                    newtable = 4;
                                    break;
                                case 2:
                                    /* ML */
                                    binaryString.append(PENTBIT[29]);
                                    info("ML ");
                                    newtable = 4;
                                    break;
                                case 8:
                                    /* UL ML */
                                    binaryString.append(PENTBIT[31]);
                                    info("UL ");
                                    binaryString.append(PENTBIT[29]);
                                    info("ML ");
                                    newtable = 4;
                                    break;
                                case 16:
                                    /* UL ML */
                                    binaryString.append(QUADBIT[14]);
                                    info("UL ");
                                    binaryString.append(PENTBIT[29]);
                                    info("ML ");
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
                                    info("ML ");
                                    binaryString.append(PENTBIT[30]);
                                    info("PL ");
                                    newtable = 8;
                                    break;
                                case 2:
                                    /* ML PL */
                                    binaryString.append(PENTBIT[29]);
                                    info("ML ");
                                    binaryString.append(PENTBIT[30]);
                                    info("PL ");
                                    newtable = 8;
                                    break;
                                case 4:
                                    /* PL */
                                    binaryString.append(PENTBIT[30]);
                                    info("PL ");
                                    newtable = 8;
                                    break;
                                case 16:
                                    /* UL ML PL */
                                    binaryString.append(QUADBIT[14]);
                                    info("UL ");
                                    binaryString.append(PENTBIT[29]);
                                    info("ML ");
                                    binaryString.append(PENTBIT[30]);
                                    info("PL ");
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
                                    info("DL ");
                                    newtable = 16;
                                    break;
                                case 2:
                                    /* DL */
                                    binaryString.append(PENTBIT[30]);
                                    info("DL ");
                                    newtable = 16;
                                    break;
                                case 4:
                                    /* UL DL */
                                    binaryString.append(PENTBIT[29]);
                                    info("UL ");
                                    binaryString.append(PENTBIT[30]);
                                    info("DL ");
                                    newtable = 16;
                                    break;
                                case 8:
                                    /* UL DL */
                                    binaryString.append(PENTBIT[31]);
                                    info("UL ");
                                    binaryString.append(PENTBIT[30]);
                                    info("DL ");
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
                                    info("BS ");
                                    newtable = 32;
                                    break;
                                case 2:
                                    /* BS */
                                    binaryString.append(PENTBIT[31]);
                                    info("BS ");
                                    newtable = 32;
                                    break;
                                case 4:
                                    /* BS */
                                    binaryString.append(PENTBIT[31]);
                                    info("BS ");
                                    newtable = 32;
                                    break;
                                case 8:
                                    /* UL BS */
                                    binaryString.append(PENTBIT[31]);
                                    info("UL ");
                                    binaryString.append(PENTBIT[31]);
                                    info("BS ");
                                    lasttable = 1;
                                    newtable = 32;
                                    break;
                                case 16:
                                    /* UL BS */
                                    binaryString.append(QUADBIT[14]);
                                    info("UL ");
                                    binaryString.append(PENTBIT[31]);
                                    info("BS ");
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
                                throw OkapiInputException.inputTooLong();
                            }

                            if (bytes > 31) {
                                /* Put 00000 followed by 11-bit number of bytes less 31 */
                                binaryString.append("00000");
                                for (int weight = 0x400; weight > 0; weight = weight >> 1) {
                                    if (((bytes - 31) & weight) != 0) {
                                        binaryString.append('1');
                                    } else {
                                        binaryString.append('0');
                                    }
                                }
                            } else {
                                /* Put 5-bit number of bytes */
                                for (int weight = 0x10; weight > 0; weight = weight >> 1) {
                                    if ((bytes & weight) != 0) {
                                        binaryString.append('1');
                                    } else {
                                        binaryString.append('0');
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
                        info("FLG(" + (charmap[i] - 400) + ") ");
                        binaryString.append(TRIBIT[charmap[i] - 400]);
                        if (charmap[i] != 400) {
                            /* ECI */
                            binaryString.append(eciToBinary());
                        }
                    } else {
                        binaryString.append(PENTBIT[charmap[i]]);
                        infoSpace(charmap[i]);
                    }
                    break;
                case 16:
                    binaryString.append(QUADBIT[charmap[i]]);
                    infoSpace(charmap[i]);
                    break;
                case 32:
                    for (int weight = 0x80; weight > 0; weight = weight >> 1) {
                        if ((charmap[i] & weight) != 0) {
                            binaryString.append('1');
                        } else {
                            binaryString.append('0');
                        }
                    }
                    infoSpace(charmap[i]);
                    break;
            }
        }

        infoLine();

        return binaryString.toString();
    }

    /** Adjusts bit stream so that no codewords are all 0s or all 1s, per Section 7.3.1.2 */
    private StringBuilder adjustBinaryString(String binaryString, boolean compact, int layers) {

        StringBuilder adjustedString = new StringBuilder();
        int codewordSize = getCodewordSize(layers);
        int ones = 0;

        /* Insert dummy digits needed to prevent codewords of all 0s or all 1s */
        for (int i = 0; i < binaryString.length(); i++) {
            if ((adjustedString.length() + 1) % codewordSize == 0) {
                if (ones == codewordSize - 1) {
                    // codeword of B-1 1s, add dummy 0
                    adjustedString.append('0');
                    i--;
                } else if (ones == 0) {
                    // codeword of B-1 0s, add dummy 1
                    adjustedString.append('1');
                    i--;
                } else {
                    // no dummy value needed
                    adjustedString.append(binaryString.charAt(i));
                }
                ones = 0;
            } else {
                adjustedString.append(binaryString.charAt(i));
                if (binaryString.charAt(i) == '1') {
                    ones++;
                }
            }
        }

        /* Add padding */
        int adjustedLength = adjustedString.length();
        int remainder = adjustedLength % codewordSize;
        int padBits = codewordSize - remainder;
        if (padBits == codewordSize) {
            padBits = 0;
        }
        for (int i = 0; i < padBits; i++) {
            adjustedString.append('1');
        }
        adjustedLength = adjustedString.length();

        /* Make sure padding didn't create an invalid (all 1s) codeword */
        ones = 0;
        for (int i = adjustedLength - codewordSize; i < adjustedLength && i >= 0; i++) {
            if (adjustedString.charAt(i) == '1') {
                ones++;
            }
        }
        if (ones == codewordSize) {
            adjustedString.setCharAt(adjustedLength - 1, '0');
        }

        /* Log the codewords */
        info("Codewords: ");
        for (int i = 0; i < (adjustedLength / codewordSize); i++) {
            int l = 0, m = (1 << (codewordSize - 1));
            for (int j = 0; j < codewordSize; j++) {
                if (adjustedString.charAt((i * codewordSize) + j) == '1') {
                    l += m;
                }
                m = m >> 1;
            }
            infoSpace(l);
        }
        infoLine();

        /* Return the adjusted bit string */
        return adjustedString;
    }

    private String eciToBinary() {
        String eciNumber = Integer.toString(eciMode);
        StringBuilder binary = new StringBuilder(4 * eciNumber.length());
        for (int i = 0; i < eciNumber.length(); i++) {
            binary.append(QUADBIT[(eciNumber.charAt(i) - '0') + 2]);
            infoSpace(eciNumber.charAt(i));
        }
        return binary.toString();
    }

    /** Creates the descriptor / mode message, per Section 7.2 */
    private String createDescriptor(boolean compact, int layers, int dataBlocks) {

        StringBuilder descriptor = new StringBuilder();
        int descDataSize;

        if (compact) {
            /* The first 2 bits represent the number of layers minus 1 */
            if (((layers - 1) & 0x02) != 0) {
                descriptor.append('1');
            } else {
                descriptor.append('0');
            }
            if (((layers - 1) & 0x01) != 0) {
                descriptor.append('1');
            } else {
                descriptor.append('0');
            }
            /* The next 6 bits represent the number of data blocks minus 1 */
            if (readerInit) {
                descriptor.append('1');
            } else {
                if (((dataBlocks - 1) & 0x20) != 0) {
                    descriptor.append('1');
                } else {
                    descriptor.append('0');
                }
            }
            for (int i = 0x10; i > 0; i = i >> 1) {
                if (((dataBlocks - 1) & i) != 0) {
                    descriptor.append('1');
                } else {
                    descriptor.append('0');
                }
            }
            descDataSize = 2;
        } else {
            /* The first 5 bits represent the number of layers minus 1 */
            for (int i = 0x10; i > 0; i = i >> 1) {
                if (((layers - 1) & i) != 0) {
                    descriptor.append('1');
                } else {
                    descriptor.append('0');
                }
            }

            /* The next 11 bits represent the number of data blocks minus 1 */
            if (readerInit) {
                descriptor.append('1');
            } else {
                if (((dataBlocks - 1) & 0x400) != 0) {
                    descriptor.append('1');
                } else {
                    descriptor.append('0');
                }
            }
            for (int i = 0x200; i > 0; i = i >> 1) {
                if (((dataBlocks - 1) & i) != 0) {
                    descriptor.append('1');
                } else {
                    descriptor.append('0');
                }
            }
            descDataSize = 4;
        }

        infoLine("Mode Message: ", descriptor);

        /* Split into 4-bit codewords */
        int[] desc_data = new int[descDataSize];
        for (int i = 0; i < descDataSize; i++) {
            for (int weight = 0; weight < 4; weight++) {
                if (descriptor.charAt((i * 4) + weight) == '1') {
                    desc_data[i] += (8 >> weight);
                }
            }
        }

        /* Add Reed-Solomon error correction with Galois Field GF(16) and prime modulus x^4 + x + 1 (Section 7.2.3) */
        ReedSolomon rs = new ReedSolomon();
        rs.init_gf(0x13);
        if (compact) {
            rs.init_code(5, 1);
            rs.encode(2, desc_data);
            int[] desc_ecc = new int[6];
            for (int i = 0; i < 5; i++) {
                desc_ecc[i] = rs.getResult(i);
            }
            for (int i = 0; i < 5; i++) {
                for (int weight = 0x08; weight > 0; weight = weight >> 1) {
                    if ((desc_ecc[4 - i] & weight) != 0) {
                        descriptor.append('1');
                    } else {
                        descriptor.append('0');
                    }
                }
            }
        } else {
            rs.init_code(6, 1);
            rs.encode(4, desc_data);
            int[] desc_ecc = new int[6];
            for (int i = 0; i < 6; i++) {
                desc_ecc[i] = rs.getResult(i);
            }
            for (int i = 0; i < 6; i++) {
                for (int weight = 0x08; weight > 0; weight = weight >> 1) {
                    if ((desc_ecc[5 - i] & weight) != 0) {
                        descriptor.append('1');
                    } else {
                        descriptor.append('0');
                    }
                }
            }
        }

        return descriptor.toString();
    }

    /** Adds error correction data to the specified binary string, which already contains the primary data */
    private void addErrorCorrection(StringBuilder adjustedString, int codewordSize, int dataBlocks, int eccBlocks) {

        int x, poly, startWeight;

        /* Split into codewords and calculate Reed-Solomon error correction codes */
        switch (codewordSize) {
            case 6:
                x = 32;
                poly = 0x43;
                startWeight = 0x20;
                break;
            case 8:
                x = 128;
                poly = 0x12d;
                startWeight = 0x80;
                break;
            case 10:
                x = 512;
                poly = 0x409;
                startWeight = 0x200;
                break;
            case 12:
                x = 2048;
                poly = 0x1069;
                startWeight = 0x800;
                break;
            default:
                throw new OkapiInternalException("Unrecognized codeword size: " + codewordSize);
        }

        ReedSolomon rs = new ReedSolomon();
        int[] data = new int[dataBlocks + 3];
        int[] ecc = new int[eccBlocks + 3];

        for (int i = 0; i < dataBlocks; i++) {
            for (int weight = 0; weight < codewordSize; weight++) {
                if (adjustedString.charAt((i * codewordSize) + weight) == '1') {
                    data[i] += (x >> weight);
                }
            }
        }

        rs.init_gf(poly);
        rs.init_code(eccBlocks, 1);
        rs.encode(dataBlocks, data);

        for (int i = 0; i < eccBlocks; i++) {
            ecc[i] = rs.getResult(i);
        }

        for (int i = (eccBlocks - 1); i >= 0; i--) {
            for (int weight = startWeight; weight > 0; weight = weight >> 1) {
                if ((ecc[i] & weight) != 0) {
                    adjustedString.append('1');
                } else {
                    adjustedString.append('0');
                }
            }
        }
    }

    /** Determines codeword bit length - Table 3 */
    private static int getCodewordSize(int layers) {
        if (layers >= 23) {
            return 12;
        } else if (layers >= 9 && layers <= 22) {
            return 10;
        } else if (layers >= 3 && layers <= 8) {
            return 8;
        } else {
            assert layers <= 2;
            return 6;
        }
    }
}
