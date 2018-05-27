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

import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

/**
 * <p>Implements Grid Matrix bar code symbology according to AIMD014.
 *
 * <p>Grid Matrix is a matrix symbology which can encode characters in the ISO/IEC 8859-1 (Latin-1)
 * character set as well as those in the GB-2312 character set. Input is assumed to be formatted as
 * a UTF string.
 *
 * @author <a href="mailto:rstuart114@gmail.com">Robin Stuart</a>
 */
public class GridMatrix extends Symbol {

    private static final char[] SHIFT_SET = {
        /* From Table 7 - Encoding of control characters */
        0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, /* NULL -> SI */
        0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1a, 0x1b, 0x1c, 0x1d, 0x1e, 0x1f, /* DLE -> US */
        '!', '"', '#', '$', '%', '&', '\'', '(', ')', '*', '+', ',', '-', '.', '/', ':',
        ';', '<', '=', '>', '?', '@', '[', '\\', ']', '^', '_', '`', '{', '|', '}', '~'
    };

    private static final int[] GM_RECOMMEND_CW = {
        9, 30, 59, 114, 170, 237, 315, 405, 506, 618, 741, 875, 1021
    };
    private static final int[] GM_MAX_CW = {
        11, 40, 79, 146, 218, 305, 405, 521, 650, 794, 953, 1125, 1313
    };

    private static final int[] GM_DATA_CODEWORDS = {
        0, 15, 13, 11, 9,
        45, 40, 35, 30, 25,
        89, 79, 69, 59, 49,
        146, 130, 114, 98, 81,
        218, 194, 170, 146, 121,
        305, 271, 237, 203, 169,
        405, 360, 315, 270, 225,
        521, 463, 405, 347, 289,
        650, 578, 506, 434, 361,
        794, 706, 618, 530, 441,
        953, 847, 741, 635, 529,
        1125, 1000, 875, 750, 625,
        1313, 1167, 1021, 875, 729
    };

    private static final int[] GM_N1 = {
        18, 50, 98, 81, 121, 113, 113, 116, 121, 126, 118, 125, 122
    };
    private static final int[] GM_B1 = {
        1, 1, 1, 2, 2, 2, 2, 3, 2, 7, 5, 10, 6
    };
    private static final int[] GM_B2 = {
        0, 0, 0, 0, 0, 1, 2, 2, 4, 0, 4, 0, 6
    };

    private static final int[] GM_EBEB = {
        /* E1 B3 E2 B4 */
        0, 0, 0, 0, // version 1
        3, 1, 0, 0,
        5, 1, 0, 0,
        7, 1, 0, 0,
        9, 1, 0, 0,
        5, 1, 0, 0, // version 2
        10, 1, 0, 0,
        15, 1, 0, 0,
        20, 1, 0, 0,
        25, 1, 0, 0,
        9, 1, 0, 0, // version 3
        19, 1, 0, 0,
        29, 1, 0, 0,
        39, 1, 0, 0,
        49, 1, 0, 0,
        8, 2, 0, 0, // version 4
        16, 2, 0, 0,
        24, 2, 0, 0,
        32, 2, 0, 0,
        41, 1, 10, 1,
        12, 2, 0, 0, // version 5
        24, 2, 0, 0,
        36, 2, 0, 0,
        48, 2, 0, 0,
        61, 1, 60, 1,
        11, 3, 0, 0, // version 6
        23, 1, 22, 2,
        34, 2, 33, 1,
        45, 3, 0, 0,
        57, 1, 56, 2,
        12, 1, 11, 3, // version 7
        23, 2, 22, 2,
        34, 3, 33, 1,
        45, 4, 0, 0,
        57, 1, 56, 3,
        12, 2, 11, 3, // version 8
        23, 5, 0, 0,
        35, 3, 34, 2,
        47, 1, 46, 4,
        58, 4, 57, 1,
        12, 6, 0, 0, // version 9
        24, 6, 0, 0,
        36, 6, 0, 0,
        48, 6, 0, 0,
        61, 1, 60, 5,
        13, 4, 12, 3, // version 10
        26, 1, 25, 6,
        38, 5, 37, 2,
        51, 2, 50, 5,
        63, 7, 0, 0,
        12, 6, 11, 3, // version 11
        24, 4, 23, 5,
        36, 2, 35, 7,
        47, 9, 0, 0,
        59, 7, 58, 2,
        13, 5, 12, 5, // version 12
        25, 10, 0, 0,
        38, 5, 37, 5,
        50, 10, 0, 0,
        63, 5, 62, 5,
        13, 1, 12, 11, //version 13
        25, 3, 24, 9,
        37, 5, 36, 7,
        49, 7, 48, 5,
        61, 9, 60, 3
    };

    private static final int[] GM_MACRO_MATRIX = {
        728, 625, 626, 627, 628, 629, 630, 631, 632, 633, 634, 635, 636, 637, 638, 639, 640, 641, 642, 643, 644, 645, 646, 647, 648, 649, 650,
        727, 624, 529, 530, 531, 532, 533, 534, 535, 536, 537, 538, 539, 540, 541, 542, 543, 544, 545, 546, 547, 548, 549, 550, 551, 552, 651,
        726, 623, 528, 441, 442, 443, 444, 445, 446, 447, 448, 449, 450, 451, 452, 453, 454, 455, 456, 457, 458, 459, 460, 461, 462, 553, 652,
        725, 622, 527, 440, 361, 362, 363, 364, 365, 366, 367, 368, 369, 370, 371, 372, 373, 374, 375, 376, 377, 378, 379, 380, 463, 554, 653,
        724, 621, 526, 439, 360, 289, 290, 291, 292, 293, 294, 295, 296, 297, 298, 299, 300, 301, 302, 303, 304, 305, 306, 381, 464, 555, 654,
        723, 620, 525, 438, 359, 288, 225, 226, 227, 228, 229, 230, 231, 232, 233, 234, 235, 236, 237, 238, 239, 240, 307, 382, 465, 556, 655,
        722, 619, 524, 437, 358, 287, 224, 169, 170, 171, 172, 173, 174, 175, 176, 177, 178, 179, 180, 181, 182, 241, 308, 383, 466, 557, 656,
        721, 618, 523, 436, 357, 286, 223, 168, 121, 122, 123, 124, 125, 126, 127, 128, 129, 130, 131, 132, 183, 242, 309, 384, 467, 558, 657,
        720, 617, 522, 435, 356, 285, 222, 167, 120, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 133, 184, 243, 310, 385, 468, 559, 658,
        719, 616, 521, 434, 355, 284, 221, 166, 119, 80, 49, 50, 51, 52, 53, 54, 55, 56, 91, 134, 185, 244, 311, 386, 469, 560, 659,
        718, 615, 520, 433, 354, 283, 220, 165, 118, 79, 48, 25, 26, 27, 28, 29, 30, 57, 92, 135, 186, 245, 312, 387, 470, 561, 660,
        717, 614, 519, 432, 353, 282, 219, 164, 117, 78, 47, 24, 9, 10, 11, 12, 31, 58, 93, 136, 187, 246, 313, 388, 471, 562, 661,
        716, 613, 518, 431, 352, 281, 218, 163, 116, 77, 46, 23, 8, 1, 2, 13, 32, 59, 94, 137, 188, 247, 314, 389, 472, 563, 662,
        715, 612, 517, 430, 351, 280, 217, 162, 115, 76, 45, 22, 7, 0, 3, 14, 33, 60, 95, 138, 189, 248, 315, 390, 473, 564, 663,
        714, 611, 516, 429, 350, 279, 216, 161, 114, 75, 44, 21, 6, 5, 4, 15, 34, 61, 96, 139, 190, 249, 316, 391, 474, 565, 664,
        713, 610, 515, 428, 349, 278, 215, 160, 113, 74, 43, 20, 19, 18, 17, 16, 35, 62, 97, 140, 191, 250, 317, 392, 475, 566, 665,
        712, 609, 514, 427, 348, 277, 214, 159, 112, 73, 42, 41, 40, 39, 38, 37, 36, 63, 98, 141, 192, 251, 318, 393, 476, 567, 666,
        711, 608, 513, 426, 347, 276, 213, 158, 111, 72, 71, 70, 69, 68, 67, 66, 65, 64, 99, 142, 193, 252, 319, 394, 477, 568, 667,
        710, 607, 512, 425, 346, 275, 212, 157, 110, 109, 108, 107, 106, 105, 104, 103, 102, 101, 100, 143, 194, 253, 320, 395, 478, 569, 668,
        709, 606, 511, 424, 345, 274, 211, 156, 155, 154, 153, 152, 151, 150, 149, 148, 147, 146, 145, 144, 195, 254, 321, 396, 479, 570, 669,
        708, 605, 510, 423, 344, 273, 210, 209, 208, 207, 206, 205, 204, 203, 202, 201, 200, 199, 198, 197, 196, 255, 322, 397, 480, 571, 670,
        707, 604, 509, 422, 343, 272, 271, 270, 269, 268, 267, 266, 265, 264, 263, 262, 261, 260, 259, 258, 257, 256, 323, 398, 481, 572, 671,
        706, 603, 508, 421, 342, 341, 340, 339, 338, 337, 336, 335, 334, 333, 332, 331, 330, 329, 328, 327, 326, 325, 324, 399, 482, 573, 672,
        705, 602, 507, 420, 419, 418, 417, 416, 415, 414, 413, 412, 411, 410, 409, 408, 407, 406, 405, 404, 403, 402, 401, 400, 483, 574, 673,
        704, 601, 506, 505, 504, 503, 502, 501, 500, 499, 498, 497, 496, 495, 494, 493, 492, 491, 490, 489, 488, 487, 486, 485, 484, 575, 674,
        703, 600, 599, 598, 597, 596, 595, 594, 593, 592, 591, 590, 589, 588, 587, 586, 585, 584, 583, 582, 581, 580, 579, 578, 577, 576, 675,
        702, 701, 700, 699, 698, 697, 696, 695, 694, 693, 692, 691, 690, 689, 688, 687, 686, 685, 684, 683, 682, 681, 680, 679, 678, 677, 676
    };

    private static final char[] MIXED_ALPHANUM_SET = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D',
        'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
        'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h',
        'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
        'w', 'x', 'y', 'z', ' '
    };

    private enum Mode {
        NULL, GM_NUMBER, GM_LOWER, GM_UPPER, GM_MIXED, GM_CONTROL, GM_BYTE, GM_CHINESE
    };

    private int[] inputData;
    private String binary;
    private int[] word = new int[1460];
    private boolean[] grid;
    private Mode appxDnextSection = Mode.NULL;
    private Mode appxDlastSection = Mode.NULL;
    private int preferredVersion = 0;
    private int preferredEccLevel = -1;

    /**
     * Set preferred size, or "version" of the symbol according to the following
     * table. This value may be ignored if the data to be encoded does not fit
     * into a symbol of the selected size.
     * <table summary="Available Grid Matrix symbol sizes">
     * <tbody>
     * <tr>
     * <th><p>
     * Input</p></th>
     * <th><p>
     * Size</p></th>
     * </tr>
     * <tr>
     * <td><p>
     * 1</p></td>
     * <td><p>
     * 18 x 18</p></td>
     * </tr>
     * <tr>
     * <td><p>
     * 2</p></td>
     * <td><p>
     * 30 x 30</p></td>
     * </tr>
     * <tr>
     * <td><p>
     * 3</p></td>
     * <td><p>
     * 42 x 42</p></td>
     * </tr>
     * <tr>
     * <td><p>
     * 4</p></td>
     * <td><p>
     * 54 x 54</p></td>
     * </tr>
     * <tr>
     * <td><p>
     * 5</p></td>
     * <td><p>
     * 66 x 66</p></td>
     * </tr>
     * <tr>
     * <td><p>
     * 6</p></td>
     * <td><p>
     * 78 x 78</p></td>
     * </tr>
     * <tr>
     * <td><p>
     * 7</p></td>
     * <td><p>
     * 90x 90</p></td>
     * </tr>
     * <tr>
     * <td><p>
     * 8</p></td>
     * <td><p>
     * 102 x 102</p></td>
     * </tr>
     * <tr>
     * <td><p>
     * 9</p></td>
     * <td><p>
     * 114 x 114</p></td>
     * </tr>
     * <tr>
     * <td><p>
     * 10</p></td>
     * <td><p>
     * 126 x 126</p></td>
     * </tr>
     * <tr>
     * <td><p>
     * 11</p></td>
     * <td><p>
     * 138 x 138</p></td>
     * </tr>
     * <tr>
     * <td><p>
     * 12</p></td>
     * <td><p>
     * 150 x 150</p></td>
     * </tr>
     * <tr>
     * <td><p>
     * 13</p></td>
     * <td><p>
     * 162 x 162</p></td>
     * </tr>
     * </tbody>
     * </table>
     *
     * @param version Symbol version
     */
    public void setPreferredVersion(int version) {
        preferredVersion = version;
    }

    /**
     * Set the preferred amount of the symbol which should be dedicated to error
     * correction data. Values should be selected from the following table:
     * <table summary="Available options for error correction capacity">
     * <tbody>
     * <tr>
     * <th><p>
     * Mode</p></th>
     * <th><p>
     * Error Correction Capacity</p></th>
     * </tr>
     * <tr>
     * <td><p>
     * 1</p></td>
     * <td><p>
     * Approximately 10%</p></td>
     * </tr>
     * <tr>
     * <td><p>
     * 2</p></td>
     * <td><p>
     * Approximately 20%</p></td>
     * </tr>
     * <tr>
     * <td><p>
     * 3</p></td>
     * <td><p>
     * Approximately 30%</p></td>
     * </tr>
     * <tr>
     * <td><p>
     * 4</p></td>
     * <td><p>
     * Approximately 40%</p></td>
     * </tr>
     * <tr>
     * <td><p>
     * 5</p></td>
     * <td><p>
     * Approximately 50%</p></td>
     * </tr>
     * </tbody>
     * </table>
     *
     * @param eccLevel Error correction mode
     */
    public void setPreferredEccLevel(int eccLevel) {
        preferredEccLevel = eccLevel;
    }

    @Override
    protected void encode() {
        int size, modules, dark, error_number;
        int auto_layers, min_layers, layers, auto_ecc_level, min_ecc_level, ecc_level;
        int x, y, i;
        int data_cw, input_latch = 0;
        int data_max;
        int length;
        String bin;

        for (i = 0; i < 1460; i++) {
            word[i] = 0;
        }

        try {
            Charset gb2312 = Charset.forName("GB2312");
            if (gb2312.newEncoder().canEncode(content)) {
                /* GB2312 will work, use Chinese compaction */
                inputBytes = content.getBytes(gb2312);
                inputData = new int[inputBytes.length];
                length = 0;
                for (i = 0; i < inputBytes.length; i++) {
                    if (((inputBytes[i] & 0xFF) >= 0xA1) && ((inputBytes[i] & 0xFF) <= 0xF7)) {
                        /* Double byte character */
                        inputData[length] = ((inputBytes[i] & 0xFF) * 256) + (inputBytes[i + 1] & 0xFF);
                        i++;
                        length++;
                    } else {
                        /* Single byte character */
                        inputData[length] = inputBytes[i] & 0xFF;
                        length++;
                    }
                }
                encodeInfo += "Using GB2312 character encoding\n";
                eciMode = 29;
            } else {
                /* GB2312 encoding won't work, use other ECI mode */
                eciProcess(); // Get ECI mode
                length = inputBytes.length;
                inputData = new int[length];
                for (i = 0; i < length; i++) {
                    inputData[i] = inputBytes[i] & 0xFF;
                }
            }
        } catch (UnsupportedCharsetException e) {
            throw new OkapiException("Byte conversion encoding error");
        }

        error_number = encodeGridMatrixBinary(length, readerInit);
        if (error_number != 0) {
            throw new OkapiException("Input data too long");
        }

        /* Determine the size of the symbol */
        data_cw = binary.length() / 7;

        auto_layers = 1;
        for (i = 0; i < 13; i++) {
            if (GM_RECOMMEND_CW[i] < data_cw) {
                auto_layers = i + 1;
            }
        }

        min_layers = 13;
        for (i = 12; i > 0; i--) {
            if (GM_MAX_CW[(i - 1)] >= data_cw) {
                min_layers = i;
            }
        }
        layers = auto_layers;
        auto_ecc_level = 3;
        if (layers == 1) {
            auto_ecc_level = 5;
        }
        if ((layers == 2) || (layers == 3)) {
            auto_ecc_level = 4;
        }
        min_ecc_level = 1;
        if (layers == 1) {
            min_ecc_level = 4;
        }
        if ((layers == 2) || (layers == 3)) {
            min_ecc_level = 2;
        }
        ecc_level = auto_ecc_level;

        if ((preferredVersion >= 1) && (preferredVersion <= 13)) {
            input_latch = 1;
            if (preferredVersion > min_layers) {
                layers = preferredVersion;
            } else {
                layers = min_layers;
            }
        }

        if (input_latch == 1) {
            auto_ecc_level = 3;
            if (layers == 1) {
                auto_ecc_level = 5;
            }
            if ((layers == 2) || (layers == 3)) {
                auto_ecc_level = 4;
            }
            ecc_level = auto_ecc_level;
            if (data_cw > GM_DATA_CODEWORDS[(5 * (layers - 1)) + (ecc_level - 1)]) {
                layers++;
            }
        }

        if (input_latch == 0) {
            if ((preferredEccLevel >= 1) && (preferredEccLevel <= 5)) {
                if (preferredEccLevel > min_ecc_level) {
                    ecc_level = preferredEccLevel;
                } else {
                    ecc_level = min_ecc_level;
                }
            }
            if (data_cw > GM_DATA_CODEWORDS[(5 * (layers - 1)) + (ecc_level - 1)]) {
                do {
                    layers++;
                } while ((data_cw > GM_DATA_CODEWORDS[(5 * (layers - 1)) + (ecc_level - 1)]) && (layers <= 13));
            }
        }

        data_max = 1313;
        switch (ecc_level) {
            case 2:
                data_max = 1167;
                break;
            case 3:
                data_max = 1021;
                break;
            case 4:
                data_max = 875;
                break;
            case 5:
                data_max = 729;
                break;
        }

        if (data_cw > data_max) {
            throw new OkapiException("Input data too long");
        }

        addErrorCorrection(data_cw, layers, ecc_level);
        size = 6 + (layers * 12);
        modules = 1 + (layers * 2);

        encodeInfo += "Layers: " + layers + "\n";
        encodeInfo += "ECC Level: " + ecc_level + "\n";
        encodeInfo += "Data Codewords: " + data_cw + "\n";
        encodeInfo += "ECC Codewords: " + GM_DATA_CODEWORDS[((layers - 1) * 5)
                + (ecc_level - 1)] + "\n";
        encodeInfo += "Grid Size: " + modules + " X " + modules + "\n";

        grid = new boolean[size * size];

        for (x = 0; x < size; x++) {
            for (y = 0; y < size; y++) {
                grid[(y * size) + x] = false;
            }
        }

        placeDataInGrid(modules, size);
        addLayerId(size, layers, modules, ecc_level);

        /* Add macromodule frames */
        for (x = 0; x < modules; x++) {
            dark = 1 - (x & 1);
            for (y = 0; y < modules; y++) {
                if (dark == 1) {
                    for (i = 0; i < 5; i++) {
                        grid[((y * 6) * size) + (x * 6) + i] = true;
                        grid[(((y * 6) + 5) * size) + (x * 6) + i] = true;
                        grid[(((y * 6) + i) * size) + (x * 6)] = true;
                        grid[(((y * 6) + i) * size) + (x * 6) + 5] = true;
                    }
                    grid[(((y * 6) + 5) * size) + (x * 6) + 5] = true;
                    dark = 0;
                } else {
                    dark = 1;
                }
            }
        }

        /* Copy values to symbol */
        symbol_width = size;
        row_count = size;
        row_height = new int[row_count];
        pattern = new String[row_count];

        for (x = 0; x < size; x++) {
            bin = "";
            for (y = 0; y < size; y++) {
                if (grid[(x * size) + y]) {
                    bin += "1";
                } else {
                    bin += "0";
                }
            }
            row_height[x] = 1;
            pattern[x] = bin2pat(bin);
        }
    }

    private int encodeGridMatrixBinary(int length, boolean reader) {
        /* Create a binary stream representation of the input data.
         7 sets are defined - Chinese characters, Numerals, Lower case letters, Upper case letters,
         Mixed numerals and letters, Control characters and 8-bit binary data */
        int sp, glyph = 0;
        Mode current_mode, next_mode, last_mode;
        int c1, c2;
        boolean done;
        int p = 0, ppos;
        int punt = 0;
        int number_pad_posn;
        int byte_count_posn = 0, byte_count = 0;
        int shift, i;
        int[] numbuf = new int[3];
        String temp_binary;
        Mode[] modeMap = calculateModeMap(length);

        binary = "";

        sp = 0;
        current_mode = Mode.NULL;
        number_pad_posn = 0;

        encodeInfo += "Encoding: ";

        if (reader) {
            binary += "1010"; /* FNC3 - Reader Initialisation */
            encodeInfo += "INIT ";
        }

        if ((eciMode != 3) && (eciMode != 29)) {
            binary += "1100"; /* ECI */

            if ((eciMode >= 0) && (eciMode <= 1023)) {
                binary += "0";
                for (i = 0x200; i > 0; i = i >> 1) {
                    if ((eciMode & i) != 0) {
                        binary += "1";
                    } else {
                        binary += "0";
                    }
                }
            }

            if ((eciMode >= 1024) && (eciMode <= 32767)) {
                binary += "10";
                for (i = 0x4000; i > 0; i = i >> 1) {
                    if ((eciMode & i) != 0) {
                        binary += "1";
                    } else {
                        binary += "0";
                    }
                }
            }

            if ((eciMode >= 32768) && (eciMode <= 811799)) {
                binary += "11";
                for (i = 0x80000; i > 0; i = i >> 1) {
                    if ((eciMode & i) != 0) {
                        binary += "1";
                    } else {
                        binary += "0";
                    }
                }
            }

            encodeInfo += "ECI " + Integer.toString(eciMode) + " ";
        }

        do {
            next_mode = modeMap[sp];

            if (next_mode != current_mode) {
                switch (current_mode) {
                    case NULL:
                        switch (next_mode) {
                            case GM_CHINESE:
                                binary += "0001";
                                break;
                            case GM_NUMBER:
                                binary += "0010";
                                break;
                            case GM_LOWER:
                                binary += "0011";
                                break;
                            case GM_UPPER:
                                binary += "0100";
                                break;
                            case GM_MIXED:
                                binary += "0101";
                                break;
                            case GM_BYTE:
                                binary += "0111";
                                break;
                        }
                        break;
                    case GM_CHINESE:
                        switch (next_mode) {
                            case GM_NUMBER:
                                binary += "1111111100001";
                                break; // 8161
                            case GM_LOWER:
                                binary += "1111111100010";
                                break; // 8162
                            case GM_UPPER:
                                binary += "1111111100011";
                                break; // 8163
                            case GM_MIXED:
                                binary += "1111111100100";
                                break; // 8164
                            case GM_BYTE:
                                binary += "1111111100101";
                                break; // 8165
                        }
                        break;
                    case GM_NUMBER:
                        /* add numeric block padding value */
                        temp_binary = binary.substring(0, number_pad_posn);
                        switch (p) {
                            case 1:
                                temp_binary += "10";
                                break; // 2 pad digits
                            case 2:
                                temp_binary += "01";
                                break; // 1 pad digit
                            case 3:
                                temp_binary += "00";
                                break; // 0 pad digits
                        }
                        temp_binary += binary.substring(number_pad_posn, binary.length());
                        binary = temp_binary;

                        switch (next_mode) {
                            case GM_CHINESE:
                                binary += "1111111011";
                                break; // 1019
                            case GM_LOWER:
                                binary += "1111111100";
                                break; // 1020
                            case GM_UPPER:
                                binary += "1111111101";
                                break; // 1021
                            case GM_MIXED:
                                binary += "1111111110";
                                break; // 1022
                            case GM_BYTE:
                                binary += "1111111111";
                                break; // 1023
                        }
                        break;
                    case GM_LOWER:
                    case GM_UPPER:
                        switch (next_mode) {
                            case GM_CHINESE:
                                binary += "11100";
                                break; // 28
                            case GM_NUMBER:
                                binary += "11101";
                                break; // 29
                            case GM_LOWER:
                            case GM_UPPER:
                                binary += "11110";
                                break; // 30
                            case GM_MIXED:
                                binary += "1111100";
                                break; // 124
                            case GM_BYTE:
                                binary += "1111110";
                                break; // 126
                        }
                        break;
                    case GM_MIXED:
                        switch (next_mode) {
                            case GM_CHINESE:
                                binary += "1111110001";
                                break; // 1009
                            case GM_NUMBER:
                                binary += "1111110010";
                                break; // 1010
                            case GM_LOWER:
                                binary += "1111110011";
                                break; // 1011
                            case GM_UPPER:
                                binary += "1111110100";
                                break; // 1012
                            case GM_BYTE:
                                binary += "1111110111";
                                break; // 1015
                        }
                        break;
                    case GM_BYTE:
                        /* add byte block length indicator */
                        addByteCount(byte_count_posn, byte_count);
                        byte_count = 0;
                        switch (next_mode) {
                            case GM_CHINESE:
                                binary += "0001";
                                break; // 1
                            case GM_NUMBER:
                                binary += "0010";
                                break; // 2
                            case GM_LOWER:
                                binary += "0011";
                                break; // 3
                            case GM_UPPER:
                                binary += "0100";
                                break; // 4
                            case GM_MIXED:
                                binary += "0101";
                                break; // 5
                        }
                        break;
                }

                switch (next_mode) {
                    case GM_CHINESE:
                        encodeInfo += "CHIN ";
                        break;
                    case GM_NUMBER:
                        encodeInfo += "NUMB ";
                        break;
                    case GM_LOWER:
                        encodeInfo += "LOWR ";
                        break;
                    case GM_UPPER:
                        encodeInfo += "UPPR ";
                        break;
                    case GM_MIXED:
                        encodeInfo += "MIXD ";
                        break;
                    case GM_BYTE:
                        encodeInfo += "BYTE ";
                        break;
                }

            }
            last_mode = current_mode;
            current_mode = next_mode;

            switch (current_mode) {
                case GM_CHINESE:
                    done = false;
                    if (inputData[sp] > 0xff) {
                        /* GB2312 character */
                        c1 = (inputData[sp] & 0xff00) >> 8;
                        c2 = inputData[sp] & 0xff;

                        if ((c1 >= 0xa0) && (c1 <= 0xa9)) {
                            glyph = (0x60 * (c1 - 0xa1)) + (c2 - 0xa0);
                        }
                        if ((c1 >= 0xb0) && (c1 <= 0xf7)) {
                            glyph = (0x60 * (c1 - 0xb0 + 9)) + (c2 - 0xa0);
                        }
                        done = true;
                    }
                    if (!(done)) {
                        if (sp != (length - 1)) {
                            if ((inputData[sp] == 13) && (inputData[sp + 1] == 10)) {
                                /* End of Line */
                                glyph = 7776;
                                sp++;
                            }
                            done = true;
                        }
                    }
                    if (!(done)) {
                        if (sp != (length - 1)) {
                            if (((inputData[sp] >= '0') && (inputData[sp] <= '9')) && ((inputData[sp + 1] >= '0') && (inputData[sp + 1] <= '9'))) {
                                /* Two digits */
                                glyph = 8033 + (10 * (inputData[sp] - '0')) + (inputData[sp + 1] - '0');
                                sp++;
                            }
                        }
                    }
                    if (!(done)) {
                        /* Byte value */
                        glyph = 7777 + inputData[sp];
                    }

                    encodeInfo += Integer.toString(glyph) + " ";

                    for (i = 0x1000; i > 0; i = i >> 1) {
                        if ((glyph & i) != 0) {
                            binary += "1";
                        } else {
                            binary += "0";
                        }
                    }
                    sp++;
                    break;

                case GM_NUMBER:
                    if (last_mode != current_mode) {
                        /* Reserve a space for numeric digit padding value (2 bits) */
                        number_pad_posn = binary.length();
                    }
                    p = 0;
                    ppos = -1;

                    /* Numeric compression can also include certain combinations of
                     non-numeric character */
                    numbuf[0] = '0';
                    numbuf[1] = '0';
                    numbuf[2] = '0';
                    do {
                        if ((inputData[sp] >= '0') && (inputData[sp] <= '9')) {
                            numbuf[p] = inputData[sp];
                            p++;
                        }
                        switch (inputData[sp]) {
                            case ' ':
                            case '+':
                            case '-':
                            case '.':
                            case ',':
                                punt = inputData[sp];
                                ppos = p;
                                break;
                        }
                        if (sp < (length - 1)) {
                            if ((inputData[sp] == 13) && (inputData[sp + 1] == 10)) {
                                /* <end of line> */
                                punt = inputData[sp];
                                sp++;
                                ppos = p;
                            }
                        }
                        sp++;
                    } while ((p < 3) && (sp < length));

                    if (ppos != -1) {
                        switch (punt) {
                            case ' ':
                                glyph = 0;
                                break;
                            case '+':
                                glyph = 3;
                                break;
                            case '-':
                                glyph = 6;
                                break;
                            case '.':
                                glyph = 9;
                                break;
                            case ',':
                                glyph = 12;
                                break;
                            case 0x13:
                                glyph = 15;
                                break;
                        }
                        glyph += ppos;
                        glyph += 1000;

                        encodeInfo += Integer.toString(glyph) + " ";

                        for (i = 0x200; i > 0; i = i >> 1) {
                            if ((glyph & i) != 0) {
                                binary += "1";
                            } else {
                                binary += "0";
                            }
                        }
                    }

                    glyph = (100 * (numbuf[0] - '0')) + (10 * (numbuf[1] - '0')) + (numbuf[2] - '0');
                    encodeInfo += Integer.toString(glyph) + " ";

                    for (i = 0x200; i > 0; i = i >> 1) {
                        if ((glyph & i) != 0) {
                            binary += "1";
                        } else {
                            binary += "0";
                        }
                    }
                    break;

                case GM_BYTE:
                    if (last_mode != current_mode) {
                        /* Reserve space for byte block length indicator (9 bits) */
                        byte_count_posn = binary.length();
                    }
                    if (byte_count == 512) {
                        /* Maximum byte block size is 512 bytes. If longer is needed then start a new block */
                        addByteCount(byte_count_posn, byte_count);
                        binary += "0111";
                        byte_count_posn = binary.length();
                        byte_count = 0;
                    }

                    glyph = inputData[sp];
                    encodeInfo += Integer.toString(glyph) + " ";
                    for (i = 0x80; i > 0; i = i >> 1) {
                        if ((glyph & i) != 0) {
                            binary += "1";
                        } else {
                            binary += "0";
                        }
                    }
                    sp++;
                    byte_count++;
                    break;

                case GM_MIXED:
                    shift = 1;
                    if ((inputData[sp] >= '0') && (inputData[sp] <= '9')) {
                        shift = 0;
                    }
                    if ((inputData[sp] >= 'A') && (inputData[sp] <= 'Z')) {
                        shift = 0;
                    }
                    if ((inputData[sp] >= 'a') && (inputData[sp] <= 'z')) {
                        shift = 0;
                    }
                    if (inputData[sp] == ' ') {
                        shift = 0;
                    }

                    if (shift == 0) {
                        /* Mixed Mode character */
                        glyph = positionOf((char) inputData[sp], MIXED_ALPHANUM_SET);
                        encodeInfo += Integer.toString(glyph) + " ";

                        for (i = 0x20; i > 0; i = i >> 1) {
                            if ((glyph & i) != 0) {
                                binary += "1";
                            } else {
                                binary += "0";
                            }
                        }
                    } else {
                        /* Shift Mode character */
                        binary += "1111110110"; /* 1014 - shift indicator */

                        addShiftCharacter(inputData[sp]);
                    }

                    sp++;
                    break;

                case GM_UPPER:
                    shift = 1;
                    if ((inputData[sp] >= 'A') && (inputData[sp] <= 'Z')) {
                        shift = 0;
                    }
                    if (inputData[sp] == ' ') {
                        shift = 0;
                    }

                    if (shift == 0) {
                        /* Upper Case character */
                        glyph = positionOf((char) inputData[sp], MIXED_ALPHANUM_SET) - 10;
                        if (glyph == 52) {
                            // Space character
                            glyph = 26;
                        }
                        encodeInfo += Integer.toString(glyph) + " ";

                        for (i = 0x10; i > 0; i = i >> 1) {
                            if ((glyph & i) != 0) {
                                binary += "1";
                            } else {
                                binary += "0";
                            }
                        }

                    } else {
                        /* Shift Mode character */
                        binary += "1111101"; /* 127 - shift indicator */

                        addShiftCharacter(inputData[sp]);
                    }

                    sp++;
                    break;

                case GM_LOWER:
                    shift = 1;
                    if ((inputData[sp] >= 'a') && (inputData[sp] <= 'z')) {
                        shift = 0;
                    }
                    if (inputData[sp] == ' ') {
                        shift = 0;
                    }

                    if (shift == 0) {
                        /* Lower Case character */
                        glyph = positionOf((char) inputData[sp], MIXED_ALPHANUM_SET) - 36;
                        encodeInfo += Integer.toString(glyph) + " ";

                        for (i = 0x10; i > 0; i = i >> 1) {
                            if ((glyph & i) != 0) {
                                binary += "1";
                            } else {
                                binary += "0";
                            }
                        }

                    } else {
                        /* Shift Mode character */
                        binary += "1111101"; /* 127 - shift indicator */

                        addShiftCharacter(inputData[sp]);
                    }

                    sp++;
                    break;
            }
            if (binary.length() > 9191) {
                return 1;
            }

        } while (sp < length);

        encodeInfo += "\n";

        if (current_mode == Mode.GM_NUMBER) {
            /* add numeric block padding value */
            temp_binary = binary.substring(0, number_pad_posn);
            switch (p) {
                case 1:
                    temp_binary += "10";
                    break; // 2 pad digits
                case 2:
                    temp_binary += "01";
                    break; // 1 pad digit
                case 3:
                    temp_binary += "00";
                    break; // 0 pad digits
            }
            temp_binary += binary.substring(number_pad_posn, binary.length());
            binary = temp_binary;
        }

        if (current_mode == Mode.GM_BYTE) {
            /* Add byte block length indicator */
            addByteCount(byte_count_posn, byte_count);
        }

        /* Add "end of data" character */
        switch (current_mode) {
            case GM_CHINESE:
                binary += "1111111100000";
                break; // 8160
            case GM_NUMBER:
                binary += "1111111010";
                break; // 1018
            case GM_LOWER:
            case GM_UPPER:
                binary += "11011";
                break; // 27
            case GM_MIXED:
                binary += "1111110000";
                break; // 1008
            case GM_BYTE:
                binary += "0000";
                break; // 0
        }

        /* Add padding bits if required */
        p = 7 - (binary.length() % 7);
        if (p == 7) {
            p = 0;
        }
        for (i = 0; i < p; i++) {
            binary += "0";
        }

        if (binary.length() > 9191) {
            return 1;
        }

        return 0;
    }

    private Mode[] calculateModeMap(int length) {
        Mode[] modeMap = new Mode[length];
        int i;
        int digitStart, digitLength;
        boolean digits;
        int spaceStart, spaceLength;
        boolean spaces;
        int[] segmentLength;
        Mode[] segmentType;
        int[] segmentStart;
        int segmentCount;

        // Step 1
        // Characters in GB2312 are encoded as Chinese characters
        for (i = 0; i < length; i++) {
            modeMap[i] = Mode.NULL;
            if (inputData[i] > 0xFF) {
                modeMap[i] = Mode.GM_CHINESE;
            }
        }

        // Consecutive <end of line> characters, if preceeded by or followed
        // by chinese characters, are encoded as chinese characters.
        if (length > 3) {
            i = 1;
            do {
                if ((inputData[i] == 13) && (inputData[i + 1] == 10)) {
                    // End of line (CR/LF)

                    if (modeMap[i - 1] == Mode.GM_CHINESE) {
                        modeMap[i] = Mode.GM_CHINESE;
                        modeMap[i + 1] = Mode.GM_CHINESE;
                    }
                    i += 2;
                } else {
                    i++;
                }
            } while (i < length - 1);

            i = length - 3;
            do {
                if ((inputData[i] == 13) && (inputData[i + 1] == 10)) {
                    // End of line (CR/LF)
                    if (modeMap[i + 2] == Mode.GM_CHINESE) {
                        modeMap[i] = Mode.GM_CHINESE;
                        modeMap[i + 1] = Mode.GM_CHINESE;
                    }
                    i -= 2;
                } else {
                    i--;
                }
            } while (i > 0);
        }

        // Digit pairs between chinese characters encode as chinese characters.
        digits = false;
        digitLength = 0;
        digitStart = 0;
        for (i = 1; i < length - 1; i++) {
            if ((inputData[i] >= 48) && (inputData[i] <= 57)) {
                // '0' to '9'
                if (digits == false) {
                    digits = true;
                    digitLength = 1;
                    digitStart = i;
                } else {
                    digitLength++;
                }
            } else {
                if (digits == true) {
                    if ((digitLength % 2) == 0) {
                        if ((modeMap[digitStart - 1] == Mode.GM_CHINESE) &&
                                (modeMap[i] == Mode.GM_CHINESE)) {
                            for(int j = 0; j < digitLength; j++) {
                                modeMap[i - j - 1] = Mode.GM_CHINESE;
                            }
                        }
                    }
                    digits = false;
                }
            }
        }

        // Step 2: all characters 'a' to 'z' are lowercase.
        for (i = 0; i < length; i++) {
            if ((inputData[i] >= 97) && (inputData[i] <= 122)) {
                modeMap[i] = Mode.GM_LOWER;
            }
        }

        // Step 3: all characters 'A' to 'Z' are uppercase.
        for (i = 0; i < length; i++) {
            if ((inputData[i] >= 65) && (inputData[i] <= 90)) {
                modeMap[i] = Mode.GM_UPPER;
            }
        }

        // Step 4: find consecutive <space> characters preceeded or followed
        // by uppercase or lowercase.
        spaces = false;
        spaceLength = 0;
        spaceStart = 0;
        for (i = 1; i < length - 1; i++) {
            if (inputData[i] == 32) {
                if (spaces == false) {
                    spaces = true;
                    spaceLength = 1;
                    spaceStart = i;
                } else {
                    spaceLength++;
                }
            } else {
                if (spaces == true) {

                    Mode modeX = modeMap[spaceStart - 1];
                    Mode modeY = modeMap[i];

                    if ((modeX == Mode.GM_LOWER) || (modeX == Mode.GM_UPPER)) {
                        for (int j = 0; j < spaceLength; j++) {
                            modeMap[i - j - 1] = modeX;
                        }
                    } else {
                        if ((modeY == Mode.GM_LOWER) || (modeY == Mode.GM_UPPER)) {
                            for (int j = 0; j < spaceLength; j++) {
                                modeMap[i - j - 1] = modeY;
                            }
                        }
                    }
                    spaces = false;
                }
            }
        }

        // Step 5: Unassigned characters '0' to '9' are assigned as numerals.
        // Non-numeric characters in table 7 are also assigned as numerals.
        for(i = 0; i < length; i++) {
            if(modeMap[i] == Mode.NULL) {
                if ((inputData[i] >= 48) && (inputData[i] <= 57)) {
                    // '0' to '9'
                    modeMap[i] = Mode.GM_NUMBER;
                } else {
                    switch (inputData[i]) {
                        case 32: // Space
                        case 43: // '+'
                        case 45: // '-'
                        case 46: // "."
                        case 44: // ","
                            modeMap[i] = Mode.GM_NUMBER;
                            break;
                        case 13: // CR
                            if (i < length - 1) {
                                if (inputData[i + 1] == 10) { // LF
                                    // <end of line>
                                    modeMap[i] = Mode.GM_NUMBER;
                                    modeMap[i + 1] = Mode.GM_NUMBER;
                                }
                            }
                    }
                }
            }
        }

        // Step 6: The remining unassigned bytes are assigned as 8-bit binary
        for(i = 0; i < length; i++) {
            if (modeMap[i] == Mode.NULL) {
                modeMap[i] = Mode.GM_BYTE;
            }
        }

        // break into segments
        segmentLength = new int[length];
        segmentType = new Mode[length];
        segmentStart = new int[length];

        segmentCount = 0;
        segmentLength[0] = 1;
        segmentType[0] = modeMap[0];
        segmentStart[0] = 0;
        for (i = 1; i < length; i++) {
            if (modeMap[i] == modeMap[i - 1]) {
                segmentLength[segmentCount]++;
            } else {
                segmentCount++;
                segmentLength[segmentCount] = 1;
                segmentType[segmentCount] = modeMap[i];
                segmentStart[segmentCount] = i;
            }
        }

        // A segment can be a control segment if
        // a) It is not the start segment of the data stream
        // b) All characters are control characters
        // c) The length of the segment is no more than 3
        // d) The previous segment is not chinese
        if (segmentCount > 1) {
            for (i = 1; i < segmentCount; i++) { // (a)
                if ((segmentLength[i] <= 3) && (segmentType[i - 1] != Mode.GM_CHINESE)) { // (c) and (d)
                    boolean controlLatch = true;
                    for (int j = 0; j < segmentLength[i]; j++) {
                        boolean thischarLatch = false;
                        for(int k = 0; k < 63; k++) {
                            if (inputData[segmentStart[i] + j] == SHIFT_SET[k]) {
                                thischarLatch = true;
                            }
                        }

                        if (!(thischarLatch)) {
                            // This character is not a control character
                            controlLatch = false;
                        }
                    }

                    if (controlLatch) { // (b)
                        segmentType[i] = Mode.GM_CONTROL;
                    }
                }
            }
        }

        // Stages 7 to 9
        if (segmentCount >= 3) {
            for (i = 0; i < segmentCount - 1; i++) {
                Mode pm, tm, nm, lm;
                int tl, nl, ll, position;
                boolean lastSegment = false;

                if (i == 0) {
                    pm = Mode.NULL;
                } else {
                    pm = segmentType[i - 1];
                }

                tm = segmentType[i];
                tl = segmentLength[i];

                nm = segmentType[i + 1];
                nl = segmentLength[i + 1];

                lm = segmentType[i + 2];
                ll = segmentLength[i + 2];

                position = segmentStart[i];

                if (i + 2 == segmentCount) {
                    lastSegment = true;
                }

//                System.out.printf("\nSegment %d %s %s [%d] %s [%d] %s [%d]\n", i, modeToString(pm),
//                        modeToString(tm), tl, modeToString(nm), nl, modeToString(lm), ll);

                segmentType[i] = getBestMode(pm, tm, nm, lm, tl, nl, ll, position, lastSegment);

                if (segmentType[i] == Mode.GM_CONTROL) {
                    segmentType[i] = segmentType[i - 1];
                }

//                System.out.printf("Best fit %s %s %s\n", modeToString(segmentType[i]),
//                        modeToString(appxDnextSection), modeToString(appxDlastSection));
            }

            segmentType[i] = appxDnextSection;
            segmentType[i + 1] = appxDlastSection;

            if (segmentType[i] == Mode.GM_CONTROL) {
                segmentType[i] = segmentType[i - 1];
            }
            if (segmentType[i + 1] == Mode.GM_CONTROL) {
                segmentType[i + 1] = segmentType[i];
            }

// Uncomment these lines to override mode selection and generate symbol as shown
// in image D.1 for the test data "AAT2556 电池充电器＋降压转换器 200mA至2A tel:86 019 82512738"
//            segmentType[9] = gmMode.GM_LOWER;
//            segmentType[10] = gmMode.GM_LOWER;
        }

        // Copy segments back to modeMap
        for (i = 0; i < segmentCount; i++) {
            for (int j = 0; j < segmentLength[i]; j++) {
                modeMap[segmentStart[i] + j] = segmentType[i];
            }
        }

        return modeMap;
    }

    private boolean isTransitionValid(Mode previousMode, Mode thisMode) {
        // Filters possible encoding data types from table D.1
        boolean isValid = false;

        switch (previousMode) {
            case GM_CHINESE:
                switch (thisMode) {
                    case GM_CHINESE:
                    case GM_BYTE:
                        isValid = true;
                        break;
                }
                break;
            case GM_NUMBER:
                switch (thisMode) {
                    case GM_NUMBER:
                    case GM_MIXED:
                    case GM_BYTE:
                    case GM_CHINESE:
                        isValid = true;
                        break;
                }
                break;
            case GM_LOWER:
                switch (thisMode) {
                    case GM_LOWER:
                    case GM_MIXED:
                    case GM_BYTE:
                    case GM_CHINESE:
                        isValid = true;
                        break;
                }
                break;
            case GM_UPPER:
                switch (thisMode) {
                    case GM_UPPER:
                    case GM_MIXED:
                    case GM_BYTE:
                    case GM_CHINESE:
                        isValid = true;
                        break;
                }
                break;
            case GM_CONTROL:
                switch (thisMode) {
                    case GM_CONTROL:
                    case GM_BYTE:
                    case GM_CHINESE:
                        isValid = true;
                        break;
                }
                break;
            case GM_BYTE:
                switch (thisMode) {
                    case GM_BYTE:
                    case GM_CHINESE:
                        isValid = true;
                        break;
                }
                break;
        }

        return isValid;
    }

    private Mode intToMode(int input) {
        Mode retVal;

        switch (input) {
            case 1:
                retVal = Mode.GM_CHINESE;
                break;
            case 2:
                retVal = Mode.GM_BYTE;
                break;
            case 3:
                retVal = Mode.GM_CONTROL;
                break;
            case 4:
                retVal = Mode.GM_MIXED;
                break;
            case 5:
                retVal = Mode.GM_UPPER;
                break;
            case 6:
                retVal = Mode.GM_LOWER;
                break;
            case 7:
                retVal = Mode.GM_NUMBER;
                break;
            default:
                retVal = Mode.NULL;
                break;
        }

        return retVal;
    }

    private Mode getBestMode(Mode pm, Mode tm, Mode nm, Mode lm, int tl, int nl, int ll, int position, boolean lastSegment) {
        int tmi, nmi, lmi;
        Mode bestMode = tm;
        int binaryLength;
        int bestBinaryLength = Integer.MAX_VALUE;

        for(tmi = 1; tmi < 8; tmi++) {
            if (isTransitionValid(tm, intToMode(tmi))) {
                for(nmi = 1; nmi < 8; nmi++) {
                    if (isTransitionValid(nm, intToMode(nmi))) {
                        for (lmi = 1; lmi < 8; lmi++) {
                            if (isTransitionValid(lm, intToMode(lmi))) {
                                binaryLength = getBinaryLength(pm, intToMode(tmi), intToMode(nmi), intToMode(lmi), tl, nl, ll, position, lastSegment);

                                if (binaryLength <= bestBinaryLength) {
                                    bestMode = intToMode(tmi);
                                    appxDnextSection = intToMode(nmi);
                                    appxDlastSection = intToMode(lmi);
                                    bestBinaryLength = binaryLength;
                                }
                            }
                        }
                    }
                }
            }
        }

        return bestMode;
    }

//    private String modeToString(gmMode mode) {
//        String output;
//
//        switch (mode) {
//            case GM_CHINESE:
//                output = "CHINESE";
//                break;
//            case GM_NUMBER:
//                output = "NUMBER";
//                break;
//            case GM_LOWER:
//                output = "LOWER";
//                break;
//            case GM_UPPER:
//                output = "UPPER";
//                break;
//            case GM_MIXED:
//                output = "MIXED";
//                break;
//            case GM_CONTROL:
//                output = "CONTROL";
//                break;
//            case GM_BYTE:
//                output = "BYTE";
//                break;
//            default:
//                output = "NULL";
//                break;
//        }
//
//        return output;
//    }

    private int getBinaryLength(Mode pm, Mode tm, Mode nm, Mode lm, int tl, int nl, int ll, int position, boolean lastSegment) {
        int binaryLength;

//        System.out.printf("P %s, T %s, N %s, L %s ", modeToString(pm), modeToString(tm), modeToString(nm), modeToString(lm));

        binaryLength = getChunkLength(pm, tm, tl, position);
        binaryLength += getChunkLength(tm, nm, nl, (position + tl));
        binaryLength += getChunkLength(nm, lm, ll, (position + tl + nl));

        if (lastSegment) {
            switch (lm) {
                case GM_CHINESE:
                    binaryLength += 13;
                    break;
                case GM_NUMBER:
                    binaryLength += 10;
                    break;
                case GM_LOWER:
                case GM_UPPER:
                    binaryLength += 5;
                    break;
                case GM_MIXED:
                    binaryLength += 10;
                    break;
                case GM_BYTE:
                    binaryLength += 4;
                    break;
            }
//            System.out.printf("LAST ");
        }

//        System.out.printf(" = %db\n", binaryLength);

        return binaryLength;
    }

    private int getChunkLength(Mode lastMode, Mode thisMode, int thisLength, int position) {
        int byteLength;

        switch (thisMode) {
            case GM_CHINESE:
                byteLength = calcChineseLength(position, thisLength);
                break;
            case GM_NUMBER:
                byteLength = calcNumberLength(position, thisLength);
                break;
            case GM_LOWER:
                byteLength = 5 * thisLength;
                break;
            case GM_UPPER:
                byteLength = 5 * thisLength;
                break;
            case GM_MIXED:
                byteLength = calcMixedLength(position, thisLength);
                break;
            case GM_CONTROL:
                byteLength = 6 * thisLength;
                break;
            default:
            //case GM_BYTE:
                byteLength = calcByteLength(position, thisLength);
                break;
        }

        switch (lastMode) {
            case NULL:
                byteLength += 4;
                break;
            case GM_CHINESE:
                if ((thisMode != Mode.GM_CHINESE) && (thisMode != Mode.GM_CONTROL)) {
                    byteLength += 13;
                }
                break;
            case GM_NUMBER:
                if ((thisMode != Mode.GM_CHINESE) && (thisMode != Mode.GM_CONTROL)) {
                    byteLength += 10;
                }
                break;
            case GM_LOWER:
                switch (thisMode) {
                    case GM_CHINESE:
                    case GM_NUMBER:
                    case GM_UPPER:
                        byteLength += 5;
                        break;
                    case GM_MIXED:
                    case GM_CONTROL:
                    case GM_BYTE:
                        byteLength += 7;
                        break;
                }
                break;
            case GM_UPPER:
                switch (thisMode) {
                    case GM_CHINESE:
                    case GM_NUMBER:
                    case GM_LOWER:
                        byteLength += 5;
                        break;
                    case GM_MIXED:
                    case GM_CONTROL:
                    case GM_BYTE:
                        byteLength += 7;
                        break;
                }
                break;
            case GM_MIXED:
                if (thisMode != Mode.GM_MIXED) {
                    byteLength += 10;
                }
                break;
            case GM_BYTE:
                if (thisMode != Mode.GM_BYTE) {
                    byteLength += 4;
                }
                break;
        }

        if ((lastMode != Mode.GM_BYTE) && (thisMode == Mode.GM_BYTE)) {
            byteLength += 9;
        }

        if ((lastMode != Mode.GM_NUMBER) && (thisMode == Mode.GM_NUMBER)) {
            byteLength += 2;
        }

//        System.out.printf("%db ", byteLength);

        return byteLength;
    }

    private int calcChineseLength(int position, int length) {
        int i = 0;
        int bits = 0;

        do {
            bits += 13;

            if (i < length) {
                if ((inputData[position + i] == 13) && (inputData[position + i + 1] == 10)) {
                    // <end of line>
                    i++;
                }

                if (((inputData[position + i] >= 48) && (inputData[position + i] <= 57)) &&
                       ((inputData[position + i + 1] >= 48) && (inputData[position + i + 1] <= 57))) {
                    // two digits
                    i++;
                }
            }
            i++;
        } while (i < length);

        return bits;
    }

    private int calcMixedLength(int position, int length) {
        int bits = 0;
        int i;

        for (i = 0; i < length; i++) {
            bits += 6;
            for(int k = 0; k < 63; k++) {
                if (inputData[position + i] == SHIFT_SET[k]) {
                    bits += 10;
                }
            }
        }

        return bits;
    }

    private int calcNumberLength(int position, int length) {
        int i;
        int bits = 0;
        int numbers = 0;
        int nonnumbers = 0;

        for (i = 0; i < length; i++) {
            if ((inputData[position + i] >= 48) && (inputData[position + i] <= 57)) {
                numbers++;
            } else {
                nonnumbers++;
            }

            if (i != 0) {
                if ((inputData[position + i] == 10) && (inputData[position + i - 1] == 13)) {
                    // <end of line>
                    nonnumbers--;
                }
            }

            if (numbers == 3) {
                if (nonnumbers == 1) {
                    bits += 20;
                } else {
                    bits += 10;
                }
                if (nonnumbers > 1) {
                    // Invalid encoding
                    bits += 100;
                }
                numbers = 0;
                nonnumbers = 0;
            }
        }

        if (numbers > 0) {
            if (nonnumbers == 1) {
                bits += 20;
            } else {
                bits += 10;
            }
        }

        if (nonnumbers > 1) {
            // Invalid
            bits += 100;
        }

        if (!((inputData[position + i - 1] >= 48) && (inputData[position + i - 1] <= 57))) {
            // Data must end with a digit
            bits += 100;
        }


        return bits;
    }

    private int calcByteLength(int position, int length) {
        int i;
        int bits = 0;

        for (i = 0; i < length; i++) {
            if (inputData[position + i] <= 0xFF) {
                bits += 8;
            } else {
                bits += 16;
            }
        }

        return bits;
    }

    private void addByteCount(int byte_count_posn, int byte_count) {
        /* Add the length indicator for byte encoded blocks */
        int i;
        String temp_binary;

        temp_binary = binary.substring(0, byte_count_posn);
        for (i = 0; i < 9; i++) {
            if ((byte_count & (0x100 >> i)) != 0) {
                temp_binary += "0";
            } else {
                temp_binary += "1";
            }
        }
        temp_binary += binary.substring(byte_count_posn, binary.length());
        binary = temp_binary;
    }

    void addShiftCharacter(int shifty) {
        /* Add a control character to the data stream */
        int i;
        int glyph = 0;

        for (i = 0; i < 64; i++) {
            if (SHIFT_SET[i] == shifty) {
                glyph = i;
            }
        }

        encodeInfo += "SHT/" + Integer.toString(glyph) + " ";

        for (i = 0x20; i > 0; i = i >> 1) {
            if ((glyph & i) != 0) {
                binary += "1";
            } else {
                binary += "0";
            }
        }
    }

    private void addErrorCorrection(int data_posn, int layers, int ecc_level) {
        int data_cw, i, j, wp;
        int n1, b1, n2, b2, e1, b3, e2;
        int block_size, data_size, ecc_size;
        int[] data = new int[1320];
        int[] block = new int[130];
        int[] data_block = new int[115];
        int[] ecc_block = new int[70];
        ReedSolomon rs = new ReedSolomon();

        data_cw = GM_DATA_CODEWORDS[((layers - 1) * 5) + (ecc_level - 1)];

        for (i = 0; i < 1320; i++) {
            data[i] = 0;
        }

        /* Convert from binary sream to 7-bit codewords */
        for (i = 0; i < data_posn; i++) {
            for (j = 0; j < 7; j++) {
                if (binary.charAt((i * 7) + j) == '1') {
                    data[i] += 0x40 >> j;
                }
            }
        }

        encodeInfo += "Codewords: ";
        for (i = 0; i < data_posn; i++) {
            encodeInfo += Integer.toString(data[i]) + " ";
        }
        encodeInfo += "\n";

        /* Add padding codewords */
        data[data_posn] = 0x00;
        for (i = (data_posn + 1); i < data_cw; i++) {
            if ((i & 1) != 0) {
                data[i] = 0x7e;
            } else {
                data[i] = 0x00;
            }
        }

        /* Get block sizes */
        n1 = GM_N1[(layers - 1)];
        b1 = GM_B1[(layers - 1)];
        n2 = n1 - 1;
        b2 = GM_B2[(layers - 1)];
        e1 = GM_EBEB[((layers - 1) * 20) + ((ecc_level - 1) * 4)];
        b3 = GM_EBEB[((layers - 1) * 20) + ((ecc_level - 1) * 4) + 1];
        e2 = GM_EBEB[((layers - 1) * 20) + ((ecc_level - 1) * 4) + 2];

        /* Split the data into blocks */
        wp = 0;
        for (i = 0; i < (b1 + b2); i++) {
            if (i < b1) {
                block_size = n1;
            } else {
                block_size = n2;
            }
            if (i < b3) {
                ecc_size = e1;
            } else {
                ecc_size = e2;
            }
            data_size = block_size - ecc_size;

            /* printf("block %d/%d: data %d / ecc %d\n", i + 1, (b1 + b2), data_size, ecc_size);*/
            for (j = 0; j < data_size; j++) {
                data_block[j] = data[wp];
                wp++;
            }

            /* Calculate ECC data for this block */
            rs.init_gf(0x89);
            rs.init_code(ecc_size, 1);
            rs.encode(data_size, data_block);
            for (j = 0; j < ecc_size; j++) {
                ecc_block[j] = rs.getResult(j);
            }

            /* Correct error correction data but in reverse order */
            for (j = 0; j < data_size; j++) {
                block[j] = data_block[j];
            }
            for (j = 0; j < ecc_size; j++) {
                block[(j + data_size)] = ecc_block[ecc_size - j - 1];
            }

            for (j = 0; j < n2; j++) {
                word[((b1 + b2) * j) + i] = block[j];
            }
            if (block_size == n1) {
                word[((b1 + b2) * (n1 - 1)) + i] = block[(n1 - 1)];
            }
        }
    }

    private void placeDataInGrid(int modules, int size) {
        int x, y, macromodule, offset;

        offset = 13 - ((modules - 1) / 2);
        for (y = 0; y < modules; y++) {
            for (x = 0; x < modules; x++) {
                macromodule = GM_MACRO_MATRIX[((y + offset) * 27) + (x + offset)];
                placeMacroModule(x, y, word[macromodule * 2], word[(macromodule * 2) + 1], size);
            }
        }
    }

    void placeMacroModule(int x, int y, int word1, int word2, int size) {
        int i, j;

        i = (x * 6) + 1;
        j = (y * 6) + 1;

        if ((word2 & 0x40) != 0) {
            grid[(j * size) + i + 2] = true;
        }
        if ((word2 & 0x20) != 0) {
            grid[(j * size) + i + 3] = true;
        }
        if ((word2 & 0x10) != 0) {
            grid[((j + 1) * size) + i] = true;
        }
        if ((word2 & 0x08) != 0) {
            grid[((j + 1) * size) + i + 1] = true;
        }
        if ((word2 & 0x04) != 0) {
            grid[((j + 1) * size) + i + 2] = true;
        }
        if ((word2 & 0x02) != 0) {
            grid[((j + 1) * size) + i + 3] = true;
        }
        if ((word2 & 0x01) != 0) {
            grid[((j + 2) * size) + i] = true;
        }
        if ((word1 & 0x40) != 0) {
            grid[((j + 2) * size) + i + 1] = true;
        }
        if ((word1 & 0x20) != 0) {
            grid[((j + 2) * size) + i + 2] = true;
        }
        if ((word1 & 0x10) != 0) {
            grid[((j + 2) * size) + i + 3] = true;
        }
        if ((word1 & 0x08) != 0) {
            grid[((j + 3) * size) + i] = true;
        }
        if ((word1 & 0x04) != 0) {
            grid[((j + 3) * size) + i + 1] = true;
        }
        if ((word1 & 0x02) != 0) {
            grid[((j + 3) * size) + i + 2] = true;
        }
        if ((word1 & 0x01) != 0) {
            grid[((j + 3) * size) + i + 3] = true;
        }
    }

    private void addLayerId(int size, int layers, int modules, int ecc_level) {
        /* Place the layer ID into each macromodule */

        int i, j, layer, start, stop;
        int[] layerid = new int[layers + 1];
        int[] id = new int[modules * modules];


        /* Calculate Layer IDs */
        for (i = 0; i <= layers; i++) {
            if (ecc_level == 1) {
                layerid[i] = 3 - (i % 4);
            } else {
                layerid[i] = (i + 5 - ecc_level) % 4;
            }
        }

        for (i = 0; i < modules; i++) {
            for (j = 0; j < modules; j++) {
                id[(i * modules) + j] = 0;
            }
        }

        /* Calculate which value goes in each macromodule */
        start = modules / 2;
        stop = modules / 2;
        for (layer = 0; layer <= layers; layer++) {
            for (i = start; i <= stop; i++) {
                id[(start * modules) + i] = layerid[layer];
                id[(i * modules) + start] = layerid[layer];
                id[((modules - start - 1) * modules) + i] = layerid[layer];
                id[(i * modules) + (modules - start - 1)] = layerid[layer];
            }
            start--;
            stop++;
        }

        /* Place the data in the grid */
        for (i = 0; i < modules; i++) {
            for (j = 0; j < modules; j++) {
                if ((id[(i * modules) + j] & 0x02) != 0) {
                    grid[(((i * 6) + 1) * size) + (j * 6) + 1] = true;
                }
                if ((id[(i * modules) + j] & 0x01) != 0) {
                    grid[(((i * 6) + 1) * size) + (j * 6) + 2] = true;
                }
            }
        }
    }
}
