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

import java.io.UnsupportedEncodingException;

/**
 * Implements Grid Matrix bar code symbology According to AIMD014
 * <br>
 * Grid Matrix is a matrix symbology which can encode characters in the ISO/IEC
 * 8859-1 (Latin-1) character set as well as those in the GB-2312 character set.
 * Input is assumed to be formatted as a UTF string.
 *
 * @author <a href="mailto:rstuart114@gmail.com">Robin Stuart</a>
 */
public class GridMatrix extends Symbol {

    private final char[] shift_set = {
        /* From Table 7 - Encoding of control characters */
        0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, /* NULL -> SI */
        0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1a, 0x1b, 0x1c, 0x1d, 0x1e, 0x1f, /* DLE -> US */
        '!', '"', '#', '$', '%', '&', '\'', '(', ')', '*', '+', ',', '-', '.', '/', ':',
        ';', '<', '=', '>', '?', '@', '[', '\\', ']', '^', '_', '`', '{', '|', '}', '~'
    };

    private final int[] gm_recommend_cw = {
        9, 30, 59, 114, 170, 237, 315, 405, 506, 618, 741, 875, 1021
    };
    private final int[] gm_max_cw = {
        11, 40, 79, 146, 218, 305, 405, 521, 650, 794, 953, 1125, 1313
    };

    private final int[] gm_data_codewords = {
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

    private final int[] gm_n1 = {
        18, 50, 98, 81, 121, 113, 113, 116, 121, 126, 118, 125, 122
    };
    private final int[] gm_b1 = {
        1, 1, 1, 2, 2, 2, 2, 3, 2, 7, 5, 10, 6
    };
    private final int[] gm_b2 = {
        0, 0, 0, 0, 0, 1, 2, 2, 4, 0, 4, 0, 6
    };

    private final int[] gm_ebeb = {
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

    private final int[] gm_macro_matrix = {
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

    private final char[] MIXED_ALPHANUM_SET = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D',
        'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
        'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h',
        'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
        'w', 'x', 'y', 'z', ' '
    };

    private enum gmMode {

        NULL, GM_NUMBER, GM_LOWER, GM_UPPER, GM_MIXED, GM_CONTROL, GM_BYTE, GM_CHINESE
    };
    private int[] inputIntArray;
    private String binary;
    private int[] word = new int[1460];
    private boolean[] grid;
    private boolean chineseLatch;

    private int preferredVersion = 0;

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

    private int preferredEccLevel = -1;

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
    public boolean encode() {
        int size, modules, dark, error_number;
        int auto_layers, min_layers, layers, auto_ecc_level, min_ecc_level, ecc_level;
        int x, y, i;
        int data_cw, input_latch = 0;
        int data_max;
        int length;
        String bin;
        int qmarksBefore, qmarksAfter;

        for (i = 0; i < 1460; i++) {
            word[i] = 0;
        }
        
        if (debug) {
            System.out.printf("Grid Matrix Content=\"%s\"\n", content);
        }

        try {
            /* Try converting to GB2312 */
            qmarksBefore = 0;
            for (i = 0; i < content.length(); i++) {
                if (content.charAt(i) == '?') {
                    qmarksBefore++;
                }
            }
            inputBytes = content.getBytes("EUC_CN");
            qmarksAfter = 0;
            for (i = 0; i < inputBytes.length; i++) {
                if (inputBytes[i] == '?') {
                    qmarksAfter++;
                }
            }
            if (qmarksBefore == qmarksAfter) {
                /* GB2312 encoding worked, use chinese compaction */
                inputIntArray = new int[inputBytes.length];
                length = 0;
                for (i = 0; i < inputBytes.length; i++) {
                    if ((inputBytes[i] >= 0xA1) && (inputBytes[i] <= 0xF7)) {
                        /* Double byte character */
                        inputIntArray[i] = ((inputBytes[i] & 0xFF) * 256) + (inputBytes[i + 1] & 0xFF);
                        i++;
                        length++;
                    } else {
                        /* Single byte character */
                        inputIntArray[i] = inputBytes[i] & 0xFF;
                        length++;
                    }
                }
                if (debug) {
                    System.out.printf("\tUsing GB2312 character encoding\n");
                }
                eciMode = 29;
                chineseLatch = true;
            } else {
                /* GB2312 encoding didn't work, use other ECI mode */
                eciProcess(); // Get ECI mode
                length = inputBytes.length;
                for (i = 0; i < length; i++) {
                    inputIntArray[i] = inputBytes[i] & 0xFF;
                }                
                chineseLatch = false;
            }
        } catch (UnsupportedEncodingException e) {
            error_msg = "Byte conversion encoding error";
            return false;
        }

        error_number = encodeGridMatrixBinary(length, readerInit);
        if (error_number != 0) {
            error_msg = "Input data too long";
            return false;
        }

        /* Determine the size of the symbol */
        data_cw = binary.length() / 7;

        auto_layers = 13;
        for (i = 12; i > 0; i--) {
            if (gm_recommend_cw[(i - 1)] >= data_cw) {
                auto_layers = i;
            }
        }
        min_layers = 13;
        for (i = 12; i > 0; i--) {
            if (gm_max_cw[(i - 1)] >= data_cw) {
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
            if (data_cw > gm_data_codewords[(5 * (layers - 1)) + (ecc_level - 1)]) {
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
            if (data_cw > gm_data_codewords[(5 * (layers - 1)) + (ecc_level - 1)]) {
                do {
                    layers++;
                } while ((data_cw > gm_data_codewords[(5 * (layers - 1)) + (ecc_level - 1)]) && (layers <= 13));
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
            error_msg = "Input data too long";
            return false;
        }

        addErrorCorrection(data_cw, layers, ecc_level);
        size = 6 + (layers * 12);
        modules = 1 + (layers * 2);

        encodeInfo += "Layers: " + layers + "\n";
        encodeInfo += "ECC Level: " + ecc_level + "\n";
        encodeInfo += "Data Codewords: " + data_cw + "\n";
        encodeInfo += "ECC Codewords: " + gm_data_codewords[((layers - 1) * 5)
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

        plotSymbol();
        return true;
    }

    private int encodeGridMatrixBinary(int length, boolean reader) {
        /* Create a binary stream representation of the input data.
         7 sets are defined - Chinese characters, Numerals, Lower case letters, Upper case letters,
         Mixed numerals and latters, Control characters and 8-bit binary data */
        int sp, glyph = 0;
        gmMode current_mode, next_mode, last_mode;
        int c1, c2;
        boolean done;
        int p = 0, ppos;
        int punt = 0;
        int number_pad_posn;
        int byte_count_posn = 0, byte_count = 0;
        int shift, i;
        int[] numbuf = new int[3];
        String temp_binary;

        binary = "";

        sp = 0;
        current_mode = gmMode.NULL;
        number_pad_posn = 0;
        
        if (debug) {
            System.out.printf("\tIntermediate encoding: ");
        }

        if (reader) {
            binary += "1010"; /* FNC3 - Reader Initialisation */
            if (debug) {
                System.out.printf("INIT ");
            }

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
            
            if (debug) {
                System.out.printf("ECI %d ", eciMode);
            }
        }

        do {
            next_mode = seekForward(length, sp, current_mode);

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
                if (debug) {
                    switch (next_mode) {
                        case GM_CHINESE:
                            System.out.printf("CHIN ");
                            break;
                        case GM_NUMBER:
                            System.out.printf("NUMB ");
                            break;
                        case GM_LOWER:
                            System.out.printf("LOWR ");
                            break;
                        case GM_UPPER:
                            System.out.printf("UPPR ");
                            break;
                        case GM_MIXED:
                            System.out.printf("MIXD ");
                            break;
                        case GM_BYTE:
                            System.out.printf("BYTE ");
                            break;
                    }
                }
            }
            last_mode = current_mode;
            current_mode = next_mode;

            switch (current_mode) {
                case GM_CHINESE:
                    done = false;
                    if (inputIntArray[sp] > 0xff) {
                        /* GB2312 character */
                        c1 = (inputIntArray[sp] & 0xff00) >> 8;
                        c2 = inputIntArray[sp] & 0xff;

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
                            if ((inputIntArray[sp] == 0x13) && (inputIntArray[sp + 1] == 0x10)) {
                                /* End of Line */
                                glyph = 7776;
                                sp++;
                            }
                            done = true;
                        }
                    }
                    if (!(done)) {
                        if (sp != (length - 1)) {
                            if (((inputIntArray[sp] >= '0') && (inputIntArray[sp] <= '9')) && ((inputIntArray[sp + 1] >= '0') && (inputIntArray[sp + 1] <= '9'))) {
                                /* Two digits */
                                glyph = 8033 + (10 * (inputIntArray[sp] - '0')) + (inputIntArray[sp + 1] - '0');
                                sp++;
                            }
                        }
                    }
                    if (!(done)) {
                        /* Byte value */
                        glyph = 7777 + inputIntArray[sp];
                    }

                    if (debug) {
                        System.out.printf("%d ", glyph);
                    }

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
                        if ((inputIntArray[sp] >= '0') && (inputIntArray[sp] <= '9')) {
                            numbuf[p] = inputIntArray[sp];
                            sp++;
                            p++;
                        }
                        switch (inputIntArray[sp]) {
                            case ' ':
                            case '+':
                            case '-':
                            case '.':
                            case ',':
                                punt = inputIntArray[sp];
                                sp++;
                                ppos = p;
                                break;
                        }
                        if (sp < (length - 1)) {
                            if ((inputIntArray[sp] == 0x13) && (inputIntArray[sp + 1] == 0x10)) {
                                /* <end of line> */
                                punt = inputIntArray[sp];
                                sp += 2;
                                ppos = p;
                            }
                        }
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

                        if (debug) {
                            System.out.printf("%d ", glyph);
                        }

                        for (i = 0x200; i > 0; i = i >> 1) {
                            if ((glyph & i) != 0) {
                                binary += "1";
                            } else {
                                binary += "0";
                            }
                        }
                    }

                    glyph = (100 * (numbuf[0] - '0')) + (10 * (numbuf[1] - '0')) + (numbuf[2] - '0');
                    if (debug) {
                        System.out.printf("%d ", glyph);
                    }

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

                    glyph = inputIntArray[sp];
                    if (debug) {
                        System.out.printf("%d ", glyph);
                    }
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
                    if ((inputIntArray[sp] >= '0') && (inputIntArray[sp] <= '9')) {
                        shift = 0;
                    }
                    if ((inputIntArray[sp] >= 'A') && (inputIntArray[sp] <= 'Z')) {
                        shift = 0;
                    }
                    if ((inputIntArray[sp] >= 'a') && (inputIntArray[sp] <= 'z')) {
                        shift = 0;
                    }
                    if (inputIntArray[sp] == ' ') {
                        shift = 0;
                    }

                    if (shift == 0) {
                        /* Mixed Mode character */
                        glyph = positionOf((char) inputIntArray[sp], MIXED_ALPHANUM_SET);
                        if (debug) {
                            System.out.printf("%d ", glyph);
                        }

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

                        addShiftCharacter(inputIntArray[sp]);
                    }

                    sp++;
                    break;

                case GM_UPPER:
                    shift = 1;
                    if ((inputIntArray[sp] >= 'A') && (inputIntArray[sp] <= 'Z')) {
                        shift = 0;
                    }
                    if (inputIntArray[sp] == ' ') {
                        shift = 0;
                    }

                    if (shift == 0) {
                        /* Upper Case character */
                        //glyph = posn("ABCDEFGHIJKLMNOPQRSTUVWXYZ ", gbdata[sp]);
                        glyph = positionOf((char) inputIntArray[sp], MIXED_ALPHANUM_SET) - 10;
                        if (glyph == 52) {
                            // Space character
                            glyph = 26;
                        }
                        if (debug) {
                            System.out.printf("%d ", glyph);
                        }

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

                        addShiftCharacter(inputIntArray[sp]);
                    }

                    sp++;
                    break;

                case GM_LOWER:
                    shift = 1;
                    if ((inputIntArray[sp] >= 'a') && (inputIntArray[sp] <= 'z')) {
                        shift = 0;
                    }
                    if (inputIntArray[sp] == ' ') {
                        shift = 0;
                    }

                    if (shift == 0) {
                        /* Lower Case character */
                        //glyph = posn("abcdefghijklmnopqrstuvwxyz ", gbdata[sp]);
                        glyph = positionOf((char) inputIntArray[sp], MIXED_ALPHANUM_SET) - 36;
                        if (debug) {
                            System.out.printf("%d ", glyph);
                        }

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

                        addShiftCharacter(inputIntArray[sp]);
                    }

                    sp++;
                    break;
            }
            if (binary.length() > 9191) {
                return 1;
            }

        } while (sp < length);
        
        if (debug) {
            System.out.printf("\n");
        }

        if (current_mode == gmMode.GM_NUMBER) {
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

        if (current_mode == gmMode.GM_BYTE) {
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

    private gmMode seekForward(int length, int position, gmMode current_mode) {
        /* In complete contrast to the method recommended in Annex D of the ANSI standard this
         code uses a look-ahead test in the same manner as Data Matrix. This decision was made
         because the "official" algorithm does not provide clear methods for dealing with all
         possible combinations of input data */

        int number_count, byte_count, mixed_count, upper_count, lower_count, chinese_count;
        int sp, done;
        int best_count, last = -1;
        gmMode best_mode;

        if (inputIntArray[position] > 0xff) {
            return gmMode.GM_CHINESE;
        }

        switch (current_mode) {
            case GM_CHINESE:
                number_count = 13;
                byte_count = 13;
                mixed_count = 13;
                upper_count = 13;
                lower_count = 13;
                chinese_count = 0;
                break;
            case GM_NUMBER:
                number_count = 0;
                byte_count = 10;
                mixed_count = 10;
                upper_count = 10;
                lower_count = 10;
                chinese_count = 10;
                break;
            case GM_LOWER:
                number_count = 5;
                byte_count = 7;
                mixed_count = 7;
                upper_count = 5;
                lower_count = 0;
                chinese_count = 5;
                break;
            case GM_UPPER:
                number_count = 5;
                byte_count = 7;
                mixed_count = 7;
                upper_count = 0;
                lower_count = 5;
                chinese_count = 5;
                break;
            case GM_MIXED:
                number_count = 10;
                byte_count = 10;
                mixed_count = 0;
                upper_count = 10;
                lower_count = 10;
                chinese_count = 10;
                break;
            case GM_BYTE:
                number_count = 4;
                byte_count = 0;
                mixed_count = 4;
                upper_count = 4;
                lower_count = 4;
                chinese_count = 4;
                break;
            default:
                /* Start of symbol */
                number_count = 4;
                byte_count = 4;
                mixed_count = 4;
                upper_count = 4;
                lower_count = 4;
                chinese_count = 4;
        }

        for (sp = position;
                (sp < length) && (sp <= (position + 8)); sp++) {

            done = 0;

            if (inputIntArray[sp] >= 0xff) {
                byte_count += 17;
                mixed_count += 23;
                upper_count += 18;
                lower_count += 18;
                chinese_count += 13;
                done = 1;
            }

            if ((inputIntArray[sp] >= 'a') && (inputIntArray[sp] <= 'z')) {
                byte_count += 8;
                mixed_count += 6;
                upper_count += 10;
                lower_count += 5;
                chinese_count += 13;
                done = 1;
            }

            if ((inputIntArray[sp] >= 'A') && (inputIntArray[sp] <= 'Z')) {
                byte_count += 8;
                mixed_count += 6;
                upper_count += 5;
                lower_count += 10;
                chinese_count += 13;
                done = 1;
            }

            if ((inputIntArray[sp] >= '0') && (inputIntArray[sp] <= '9')) {
                byte_count += 8;
                mixed_count += 6;
                upper_count += 8;
                lower_count += 8;
                chinese_count += 13;
                done = 1;
            }

            if (inputIntArray[sp] == ' ') {
                byte_count += 8;
                mixed_count += 6;
                upper_count += 5;
                lower_count += 5;
                chinese_count += 13;
                done = 1;
            }

            if (done == 0) {
                /* Control character */
                byte_count += 8;
                mixed_count += 16;
                upper_count += 13;
                lower_count += 13;
                chinese_count += 13;
            }

            if (inputIntArray[sp] >= 0x7f) {
                mixed_count += 20;
                upper_count += 20;
                lower_count += 20;
            }
        }

        /* Adjust for <end of line> */
        for (sp = position;
                (sp < (length - 1)) && (sp <= (position + 7)); sp++) {
            if ((inputIntArray[sp] == 0x13) && (inputIntArray[sp] == 0x10)) {
                chinese_count -= 13;
            }
        }

        /* Adjust for double digits */
        for (sp = position;
                (sp < (length - 1)) && (sp <= (position + 7)); sp++) {
            if (sp != last) {
                if (((inputIntArray[sp] >= '0') && (inputIntArray[sp] <= '9')) && ((inputIntArray[sp + 1] >= '0') && (inputIntArray[sp + 1] <= '9'))) {
                    chinese_count -= 13;
                    last = sp + 1;
                }
            }
        }

        /* Numeric mode is more complex */
        number_count += numberModeCost(length, position);

//        if (debug) {
//            System.out.printf("C %d / B %d / M %d / U %d / L %d / N %d\n", chinese_count, byte_count, mixed_count, upper_count, lower_count, number_count);
//        }

        if (chineseLatch) {
            /* GB2312 encoding enabled */
            best_count = chinese_count;
            best_mode = gmMode.GM_CHINESE;
            if (byte_count <= best_count) {
                best_count = byte_count;
                best_mode = gmMode.GM_BYTE;
            }
        } else {
            /* UTF-8 encoding enabled */
            best_count = byte_count;
            best_mode = gmMode.GM_BYTE;
        }

        if (mixed_count <= best_count) {
            best_count = mixed_count;
            best_mode = gmMode.GM_MIXED;
        }

        if (upper_count <= best_count) {
            best_count = upper_count;
            best_mode = gmMode.GM_UPPER;
        }

        if (lower_count <= best_count) {
            best_count = lower_count;
            best_mode = gmMode.GM_LOWER;
        }

        if (number_count <= best_count) {
            best_mode = gmMode.GM_NUMBER;
        }

        return best_mode;

    }

    private int numberModeCost(int length, int position) {
        /* Attempt to calculate the 'cost' of using numeric mode from a given position in number of bits */
        /* Also ensures that numeric mode is not selected when it cannot be used: for example in
         a string which has "2.2.0" (cannot have more than one non-numeric character for each
         block of three numeric characters) */
        int sp;
        int numb = 0, nonum = 0, done;
        int tally = 0;

        sp = position;

        do {
            done = 0;

            if ((inputIntArray[sp] >= '0') && (inputIntArray[sp] <= '9')) {
                numb++;
                done = 1;
            }
            switch (inputIntArray[sp]) {
                case ' ':
                case '+':
                case '-':
                case '.':
                case ',':
                    nonum++;
                    done = 1;
            }
            if ((sp + 1) < length) {
                if ((inputIntArray[sp] == 0x13) && (inputIntArray[sp + 1] == 0x10)) {
                    nonum++;
                    done = 1;
                    sp++;
                }
            }

            if (done == 0) {
                tally += 80;
            } else {
                if (numb == 3) {
                    if (nonum == 0) {
                        tally += 10;
                    }
                    if (nonum == 1) {
                        tally += 20;
                    }
                    if (nonum > 1) {
                        tally += 80;
                    }
                    numb = 0;
                    nonum = 0;
                }
            }

            sp++;
        } while ((sp < length) && (sp <= (position + 8)));

        if (numb == 0) {
            tally += 80;
        }

        if (numb > 1) {
            if (nonum == 0) {
                tally += 10;
            }
            if (nonum == 1) {
                tally += 20;
            }
            if (nonum > 1) {
                tally += 80;
            }
        }

        return tally;
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
            if (shift_set[i] == shifty) {
                glyph = i;
            }
        }

        if (debug) {
            System.out.printf("SFT/%d ", glyph);
        }

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

        data_cw = gm_data_codewords[((layers - 1) * 5) + (ecc_level - 1)];

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
        n1 = gm_n1[(layers - 1)];
        b1 = gm_b1[(layers - 1)];
        n2 = n1 - 1;
        b2 = gm_b2[(layers - 1)];
        e1 = gm_ebeb[((layers - 1) * 20) + ((ecc_level - 1) * 4)];
        b3 = gm_ebeb[((layers - 1) * 20) + ((ecc_level - 1) * 4) + 1];
        e2 = gm_ebeb[((layers - 1) * 20) + ((ecc_level - 1) * 4) + 2];

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
                macromodule = gm_macro_matrix[((y + offset) * 27) + (x + offset)];
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
