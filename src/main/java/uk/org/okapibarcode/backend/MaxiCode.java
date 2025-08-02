/*
 * Copyright 2014-2015 Robin Stuart, Daniel Gredler
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

import static uk.org.okapibarcode.util.Arrays.contains;
import static uk.org.okapibarcode.util.Arrays.insertArray;

import java.util.Arrays;

import uk.org.okapibarcode.graphics.Circle;
import uk.org.okapibarcode.graphics.Hexagon;

/**
 * <p>
 * Implements MaxiCode according to ISO 16023:2000.
 *
 * <p>
 * MaxiCode employs a pattern of hexagons around a central 'bulls-eye'
 * finder pattern. Encoding in several modes is supported, but encoding in
 * Mode 2 and 3 require primary messages to be set. Input characters can be
 * any from the ISO 8859-1 (Latin-1) character set.
 *
 * <p>
 * TODO: Add ECI functionality.
 *
 * @author <a href="mailto:rstuart114@gmail.com">Robin Stuart</a>
 * @author Daniel Gredler
 */
public class MaxiCode extends Symbol {

    /** MaxiCode module sequence, from ISO/IEC 16023 Figure 5 (30 x 33 data grid). */
    private static final int[] MAXICODE_GRID = {
        122, 121, 128, 127, 134, 133, 140, 139, 146, 145, 152, 151, 158, 157, 164, 163, 170, 169, 176, 175, 182, 181, 188, 187, 194, 193, 200, 199, 0,   0,
        124, 123, 130, 129, 136, 135, 142, 141, 148, 147, 154, 153, 160, 159, 166, 165, 172, 171, 178, 177, 184, 183, 190, 189, 196, 195, 202, 201, 817, 0,
        126, 125, 132, 131, 138, 137, 144, 143, 150, 149, 156, 155, 162, 161, 168, 167, 174, 173, 180, 179, 186, 185, 192, 191, 198, 197, 204, 203, 819, 818,
        284, 283, 278, 277, 272, 271, 266, 265, 260, 259, 254, 253, 248, 247, 242, 241, 236, 235, 230, 229, 224, 223, 218, 217, 212, 211, 206, 205, 820, 0,
        286, 285, 280, 279, 274, 273, 268, 267, 262, 261, 256, 255, 250, 249, 244, 243, 238, 237, 232, 231, 226, 225, 220, 219, 214, 213, 208, 207, 822, 821,
        288, 287, 282, 281, 276, 275, 270, 269, 264, 263, 258, 257, 252, 251, 246, 245, 240, 239, 234, 233, 228, 227, 222, 221, 216, 215, 210, 209, 823, 0,
        290, 289, 296, 295, 302, 301, 308, 307, 314, 313, 320, 319, 326, 325, 332, 331, 338, 337, 344, 343, 350, 349, 356, 355, 362, 361, 368, 367, 825, 824,
        292, 291, 298, 297, 304, 303, 310, 309, 316, 315, 322, 321, 328, 327, 334, 333, 340, 339, 346, 345, 352, 351, 358, 357, 364, 363, 370, 369, 826, 0,
        294, 293, 300, 299, 306, 305, 312, 311, 318, 317, 324, 323, 330, 329, 336, 335, 342, 341, 348, 347, 354, 353, 360, 359, 366, 365, 372, 371, 828, 827,
        410, 409, 404, 403, 398, 397, 392, 391, 80,  79,  0,   0,   14,  13,  38,  37,  3,   0,   45,  44,  110, 109, 386, 385, 380, 379, 374, 373, 829, 0,
        412, 411, 406, 405, 400, 399, 394, 393, 82,  81,  41,  0,   16,  15,  40,  39,  4,   0,   0,   46,  112, 111, 388, 387, 382, 381, 376, 375, 831, 830,
        414, 413, 408, 407, 402, 401, 396, 395, 84,  83,  42,  0,   0,   0,   0,   0,   6,   5,   48,  47,  114, 113, 390, 389, 384, 383, 378, 377, 832, 0,
        416, 415, 422, 421, 428, 427, 104, 103, 56,  55,  17,  0,   0,   0,   0,   0,   0,   0,   21,  20,  86,  85,  434, 433, 440, 439, 446, 445, 834, 833,
        418, 417, 424, 423, 430, 429, 106, 105, 58,  57,  0,   0,   0,   0,   0,   0,   0,   0,   23,  22,  88,  87,  436, 435, 442, 441, 448, 447, 835, 0,
        420, 419, 426, 425, 432, 431, 108, 107, 60,  59,  0,   0,   0,   0,   0,   0,   0,   0,   0,   24,  90,  89,  438, 437, 444, 443, 450, 449, 837, 836,
        482, 481, 476, 475, 470, 469, 49,  0,   31,  0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   1,   54,  53,  464, 463, 458, 457, 452, 451, 838, 0,
        484, 483, 478, 477, 472, 471, 50,  0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   466, 465, 460, 459, 454, 453, 840, 839,
        486, 485, 480, 479, 474, 473, 52,  51,  32,  0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   2,   0,   43,  468, 467, 462, 461, 456, 455, 841, 0,
        488, 487, 494, 493, 500, 499, 98,  97,  62,  61,  0,   0,   0,   0,   0,   0,   0,   0,   0,   27,  92,  91,  506, 505, 512, 511, 518, 517, 843, 842,
        490, 489, 496, 495, 502, 501, 100, 99,  64,  63,  0,   0,   0,   0,   0,   0,   0,   0,   29,  28,  94,  93,  508, 507, 514, 513, 520, 519, 844, 0,
        492, 491, 498, 497, 504, 503, 102, 101, 66,  65,  18,  0,   0,   0,   0,   0,   0,   0,   19,  30,  96,  95,  510, 509, 516, 515, 522, 521, 846, 845,
        560, 559, 554, 553, 548, 547, 542, 541, 74,  73,  33,  0,   0,   0,   0,   0,   0,   11,  68,  67,  116, 115, 536, 535, 530, 529, 524, 523, 847, 0,
        562, 561, 556, 555, 550, 549, 544, 543, 76,  75,  0,   0,   8,   7,   36,  35,  12,  0,   70,  69,  118, 117, 538, 537, 532, 531, 526, 525, 849, 848,
        564, 563, 558, 557, 552, 551, 546, 545, 78,  77,  0,   34,  10,  9,   26,  25,  0,   0,   72,  71,  120, 119, 540, 539, 534, 533, 528, 527, 850, 0,
        566, 565, 572, 571, 578, 577, 584, 583, 590, 589, 596, 595, 602, 601, 608, 607, 614, 613, 620, 619, 626, 625, 632, 631, 638, 637, 644, 643, 852, 851,
        568, 567, 574, 573, 580, 579, 586, 585, 592, 591, 598, 597, 604, 603, 610, 609, 616, 615, 622, 621, 628, 627, 634, 633, 640, 639, 646, 645, 853, 0,
        570, 569, 576, 575, 582, 581, 588, 587, 594, 593, 600, 599, 606, 605, 612, 611, 618, 617, 624, 623, 630, 629, 636, 635, 642, 641, 648, 647, 855, 854,
        728, 727, 722, 721, 716, 715, 710, 709, 704, 703, 698, 697, 692, 691, 686, 685, 680, 679, 674, 673, 668, 667, 662, 661, 656, 655, 650, 649, 856, 0,
        730, 729, 724, 723, 718, 717, 712, 711, 706, 705, 700, 699, 694, 693, 688, 687, 682, 681, 676, 675, 670, 669, 664, 663, 658, 657, 652, 651, 858, 857,
        732, 731, 726, 725, 720, 719, 714, 713, 708, 707, 702, 701, 696, 695, 690, 689, 684, 683, 678, 677, 672, 671, 666, 665, 660, 659, 654, 653, 859, 0,
        734, 733, 740, 739, 746, 745, 752, 751, 758, 757, 764, 763, 770, 769, 776, 775, 782, 781, 788, 787, 794, 793, 800, 799, 806, 805, 812, 811, 861, 860,
        736, 735, 742, 741, 748, 747, 754, 753, 760, 759, 766, 765, 772, 771, 778, 777, 784, 783, 790, 789, 796, 795, 802, 801, 808, 807, 814, 813, 862, 0,
        738, 737, 744, 743, 750, 749, 756, 755, 762, 761, 768, 767, 774, 773, 780, 779, 786, 785, 792, 791, 798, 797, 804, 803, 810, 809, 816, 815, 864, 863
    };

    /**
     * ASCII character to Code Set mapping, from ISO/IEC 16023 Appendix A.
     * 1 = Set A, 2 = Set B, 3 = Set C, 4 = Set D, 5 = Set E.
     * 0 refers to special characters that fit into more than one set (e.g. GS).
     */
    private static final int[] MAXICODE_SET = {
        5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 0, 5, 5, 5, 5, 5, 5,
        5, 5, 5, 5, 5, 5, 5, 5, 0, 0, 0, 5, 0, 2, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 0, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 2,
        2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2,
        2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
        2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4,
        4, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
        5, 4, 5, 5, 5, 5, 5, 5, 4, 5, 3, 4, 3, 5, 5, 4, 4, 3, 3, 3,
        4, 3, 5, 4, 4, 3, 3, 4, 3, 3, 3, 4, 3, 3, 3, 3, 3, 3, 3, 3,
        3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
        3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
        4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4
    };

    /** ASCII character to symbol value, from ISO/IEC 16023 Appendix A. */
    private static final int[] MAXICODE_SYMBOL_CHAR = {
        0,  1,  2,  3,  4,  5,  6,  7,  8,  9,  10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
        20, 21, 22, 23, 24, 25, 26, 30, 28, 29, 30, 35, 32, 53, 34, 35, 36, 37, 38, 39,
        40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 37,
        38, 39, 40, 41, 52, 1,  2,  3,  4,  5,  6,  7,  8,  9,  10, 11, 12, 13, 14, 15,
        16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 42, 43, 44, 45, 46, 0,  1,  2,  3,
        4,  5,  6,  7,  8,  9,  10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23,
        24, 25, 26, 32, 54, 34, 35, 36, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 47, 48,
        49, 50, 51, 52, 53, 54, 55, 56, 57, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 36,
        37, 37, 38, 39, 40, 41, 42, 43, 38, 44, 37, 39, 38, 45, 46, 40, 41, 39, 40, 41,
        42, 42, 47, 43, 44, 43, 44, 45, 45, 46, 47, 46, 0,  1,  2,  3,  4,  5,  6,  7,
        8,  9,  10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 32,
        33, 34, 35, 36, 0,  1,  2,  3,  4,  5,  6,  7,  8,  9,  10, 11, 12, 13, 14, 15,
        16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 32, 33, 34, 35, 36
    };

    private int mode;
    private int structuredAppendPosition = 1;
    private int structuredAppendTotal = 1;
    private String primaryData = "";
    private String postalCode; // modes 2 and 3 only
    private int country; // modes 2 and 3 only
    private int service; // modes 2 and 3 only
    private int[] codewords;
    private int[] set = new int[144];
    private int[] character = new int[144];
    private boolean[][] grid = new boolean[33][30];

    /**
     * Creates a new instance, using mode 4 (standard symbol).
     */
    public MaxiCode() {
        this(4);
    }

    /**
     * Creates a new instance, using the specified mode.
     *
     * @param mode the MaxiCode mode to use
     */
    public MaxiCode(int mode) {
        setMode(mode);
        this.humanReadableLocation = HumanReadableLocation.NONE;
    }

    /**
     * Sets the MaxiCode mode to use. Only modes 2 to 6 are supported:
     * <ul>
     *   <li>Mode 2: Structured carrier message (US domestic transportation)</li>
     *   <li>Mode 3: Structured carrier message (international transportation)</li>
     *   <li>Mode 4: Standard symbol</li>
     *   <li>Mode 5: Full EEC (enhanced error correction)</li>
     *   <li>Mode 6: Reader programming</li>
     * </ul>
     *
     * @param mode the MaxiCode mode to use
     */
    public void setMode(int mode) {
        if (mode < 2 || mode > 6) {
            throw new IllegalArgumentException("Invalid MaxiCode mode: " + mode);
        }
        this.mode = mode;
    }

    /**
     * Returns the MaxiCode mode being used. Only modes 2 to 6 are supported.
     *
     * @return the MaxiCode mode being used
     */
    public int getMode() {
        return mode;
    }

    /**
     * If this MaxiCode symbol is part of a series of MaxiCode symbols appended in a structured format, this method sets the
     * position of this symbol in the series. Valid values are 1 through 8 inclusive.
     *
     * @param position the position of this MaxiCode symbol in the structured append series
     */
    public void setStructuredAppendPosition(int position) {
        if (position < 1 || position > 8) {
            throw new IllegalArgumentException("Invalid MaxiCode structured append position: " + position);
        }
        this.structuredAppendPosition = position;
    }

    /**
     * Returns the position of this MaxiCode symbol in a series of symbols using structured append. If this symbol is not part of
     * such a series, this method will return <code>1</code>.
     *
     * @return the position of this MaxiCode symbol in a series of symbols using structured append
     */
    public int getStructuredAppendPosition() {
        return structuredAppendPosition;
    }

    /**
     * If this MaxiCode symbol is part of a series of MaxiCode symbols appended in a structured format, this method sets the total
     * number of symbols in the series. Valid values are 1 through 8 inclusive. A value of 1 indicates that this symbol is not
     * part of a structured append series.
     *
     * @param total the total number of MaxiCode symbols in the structured append series
     */
    public void setStructuredAppendTotal(int total) {
        if (total < 1 || total > 8) {
            throw new IllegalArgumentException("Invalid MaxiCode structured append total: " + total);
        }
        this.structuredAppendTotal = total;
    }

    /**
     * Returns the size of the series of MaxiCode symbols using structured append that this symbol is part of. If this symbol is
     * not part of a structured append series, this method will return <code>1</code>.
     *
     * @return size of the series that this symbol is part of
     */
    public int getStructuredAppendTotal() {
        return structuredAppendTotal;
    }

    /**
     * Sets the primary data. Should only be used for modes 2 and 3. Must conform to the following structure:
     *
     * <table>
     *   <tr><th>Characters</th><th>Meaning</th></tr>
     *   <tr><td>1-9</td><td>Postal code data which can consist of up to 9 digits (for mode 2) or up to 6
     *                       alphanumeric characters (for mode 3). Remaining unused characters should be
     *                       filled with the SPACE character (ASCII 32).</td></tr>
     *   <tr><td>10-12</td><td>Three-digit country code according to ISO-3166.</td></tr>
     *   <tr><td>13-15</td><td>Three digit service code. This depends on your parcel courier.</td></tr>
     * </table>
     *
     * @param primary the primary data
     */
    public void setPrimary(String primary) {
        primaryData = primary;
    }

    /**
     * Returns the primary data for this MaxiCode symbol. Should only be used for modes 2 and 3.
     *
     * @return the primary data for this MaxiCode symbol
     */
    public String getPrimary() {
        return primaryData;
    }

    /**
     * Returns the postal code encoded by this symbol, as extracted from the
     * {@link #setPrimary(String) primary data}. Only available after
     * {@link #setContent(String) encoding} a mode 2 or mode 3 symbol.
     *
     * @return the postal code encoded by this symbol
     */
    public String getPostalCode() {
        if (mode != 2 && mode != 3) {
            throw new OkapiInputException("Postal code is not available for MaxiCode mode: " + mode);
        }
        return postalCode;
    }

    /**
     * Returns the country encoded by this symbol, as extracted from the
     * {@link #setPrimary(String) primary data}. Only available after
     * {@link #setContent(String) encoding} a mode 2 or mode 3 symbol.
     *
     * @return the country encoded by this symbol
     */
    public int getCountry() {
        if (mode != 2 && mode != 3) {
            throw new OkapiInputException("Country is not available for MaxiCode mode: " + mode);
        }
        return country;
    }

    /**
     * Returns the service encoded by this symbol, as extracted from the
     * {@link #setPrimary(String) primary data}. Only available after
     * {@link #setContent(String) encoding} a mode 2 or mode 3 symbol.
     *
     * @return the service encoded by this symbol
     */
    public int getService() {
        if (mode != 2 && mode != 3) {
            throw new OkapiInputException("Service is not available for MaxiCode mode: " + mode);
        }
        return service;
    }

    @Override
    public boolean supportsEci() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    protected void encode() {

        eciProcess();

        // mode 2 -> mode 3 if postal code isn't strictly numeric
        if (mode == 2) {
            for (int i = 0; i < 9 && i < primaryData.length(); i++) {
                char c = primaryData.charAt(i);
                if ((c < '0' || c > '9') && c != ' ') {
                    mode = 3;
                    break;
                }
            }
        }

        // initialize the set and character arrays
        processText();

        // start building the codeword array, starting with a copy of the character data
        // insert primary message if this is a structured carrier message; insert mode otherwise
        codewords = Arrays.copyOf(character, character.length);
        if (mode == 2 || mode == 3) {
            int[] primary = getPrimaryCodewords();
            codewords = insertArray(codewords, 0, primary);
        } else {
            codewords = insertArray(codewords, 0, new int[] { mode });
        }

        // insert structured append flag if necessary
        if (structuredAppendTotal > 1) {

            int[] flag = new int[2];
            flag[0] = 33; // padding
            flag[1] = ((structuredAppendPosition - 1) << 3) | (structuredAppendTotal - 1); // position + total

            int index;
            if (mode == 2 || mode == 3) {
                index = 10; // first two data symbols in the secondary message
            } else {
                index = 1; // first two data symbols in the primary message (first symbol at index 0 isn't a data symbol)
            }

            codewords = insertArray(codewords, index, flag);
        }

        int secondaryMax, secondaryECMax;
        if (mode == 5) {
            // 68 data codewords, 56 error corrections in secondary message
            secondaryMax = 68;
            secondaryECMax = 56;
        } else {
            // 84 data codewords, 40 error corrections in secondary message
            secondaryMax = 84;
            secondaryECMax = 40;
        }

        // truncate data codewords to maximum data space available
        int totalMax = secondaryMax + 10;
        if (codewords.length > totalMax) {
            codewords = Arrays.copyOfRange(codewords, 0, totalMax);
        }

        // insert primary error correction between primary message and secondary message (always EEC)
        int[] primary = Arrays.copyOfRange(codewords, 0, 10);
        int[] primaryCheck = getErrorCorrection(primary, 10);
        codewords = insertArray(codewords, 10, primaryCheck);

        // calculate secondary error correction
        int[] secondary = Arrays.copyOfRange(codewords, 20, codewords.length);
        int[] secondaryOdd = new int[secondary.length / 2];
        int[] secondaryEven = new int[secondary.length / 2];
        for (int i = 0; i < secondary.length; i++) {
            if ((i & 1) != 0) { // odd
                secondaryOdd[(i - 1) / 2] = secondary[i];
            } else { // even
                secondaryEven[i / 2] = secondary[i];
            }
        }
        int[] secondaryECOdd = getErrorCorrection(secondaryOdd, secondaryECMax / 2);
        int[] secondaryECEven = getErrorCorrection(secondaryEven, secondaryECMax / 2);

        // add secondary error correction after secondary message
        codewords = Arrays.copyOf(codewords, codewords.length + secondaryECOdd.length + secondaryECEven.length);
        for (int i = 0; i < secondaryECOdd.length; i++) {
            codewords[20 + secondaryMax + (2 * i) + 1] = secondaryECOdd[i];
        }
        for (int i = 0; i < secondaryECEven.length; i++) {
            codewords[20 + secondaryMax + (2 * i)] = secondaryECEven[i];
        }

        infoLine("Mode: ", mode);
        infoLine("ECC Codewords: ", secondaryECMax);
        info("Codewords: ");
        for (int i = 0; i < codewords.length; i++) {
            infoSpace(codewords[i]);
        }
        infoLine();

        // copy data into symbol grid
        int[] bit_pattern = new int[7];
        for (int i = 0; i < 33; i++) {
            for (int j = 0; j < 30; j++) {

                int block = (MAXICODE_GRID[(i * 30) + j] + 5) / 6;
                int bit = (MAXICODE_GRID[(i * 30) + j] + 5) % 6;

                if (block != 0) {

                    bit_pattern[0] = (codewords[block - 1] & 0x20) >> 5;
                    bit_pattern[1] = (codewords[block - 1] & 0x10) >> 4;
                    bit_pattern[2] = (codewords[block - 1] & 0x8) >> 3;
                    bit_pattern[3] = (codewords[block - 1] & 0x4) >> 2;
                    bit_pattern[4] = (codewords[block - 1] & 0x2) >> 1;
                    bit_pattern[5] = (codewords[block - 1] & 0x1);

                    if (bit_pattern[bit] != 0) {
                        grid[i][j] = true;
                    } else {
                        grid[i][j] = false;
                    }
                }
            }
        }

        // add orientation markings
        grid[0][28] = true;  // top right filler
        grid[0][29] = true;
        grid[9][10] = true;  // top left marker
        grid[9][11] = true;
        grid[10][11] = true;
        grid[15][7] = true;  // left hand marker
        grid[16][8] = true;
        grid[16][20] = true; // right hand marker
        grid[17][20] = true;
        grid[22][10] = true; // bottom left marker
        grid[23][10] = true;
        grid[22][17] = true; // bottom right marker
        grid[23][17] = true;

        // the following is provided for compatibility, but the results are not useful
        row_count = 33;
        readable = "";
        pattern = new String[33];
        row_height = new int[33];
        for (int i = 0; i < 33; i++) {
            StringBuilder bin = new StringBuilder(30);
            for (int j = 0; j < 30; j++) {
                if (grid[i][j]) {
                    bin.append("1");
                } else {
                    bin.append("0");
                }
            }
            pattern[i] = bin2pat(bin);
            row_height[i] = moduleWidth;
        }
    }

    /**
     * Extracts the postal code, country code and service code from the primary data and
     * returns the corresponding primary message codewords.
     *
     * @return the primary message codewords
     */
    private int[] getPrimaryCodewords() {

        assert mode == 2 || mode == 3;

        if (primaryData.length() != 15) {
            throw new OkapiInputException("Invalid primary string");
        }

        // check that country code and service are numeric
        for (int i = 9; i < 15; i++) {
            if (primaryData.charAt(i) < '0' || primaryData.charAt(i) > '9') {
                throw new OkapiInputException("Invalid primary string");
            }
        }

        country = Integer.parseInt(primaryData.substring(9, 12));
        service = Integer.parseInt(primaryData.substring(12, 15));

        if (mode == 2) {
            // remove any space padding, pad with zeroes if necessary
            postalCode = primaryData.substring(0, 9);
            int index = postalCode.indexOf(' ');
            if (index != -1) {
                postalCode = postalCode.substring(0, index);
            }
            while (country == 840 && postalCode.length() < 9) {
                postalCode += "0"; // per Annex B, section B.1, paragraph 4.a
            }
        } else {
            assert mode == 3;
            // characters not encodable in Code Set A are converted to spaces
            char[] chars = primaryData.substring(0, 6).toUpperCase().toCharArray();
            for (int i = 0; i < chars.length; i++) {
                int set = MAXICODE_SET[chars[i]];
                if (set != 0 && set != 1) {
                    chars[i] = ' ';
                }
            }
            postalCode = new String(chars);
        }

        infoLine("Postal Code: ", postalCode);
        infoLine("Country Code: ", country);
        infoLine("Service: ", service);

        if (mode == 2) {
            return getMode2PrimaryCodewords(postalCode, country, service);
        } else {
            assert mode == 3;
            return getMode3PrimaryCodewords(postalCode, country, service);
        }
    }

    /**
     * Returns the primary message codewords for mode 2. Assumes that the postal code has
     * already been validated to contain only numeric data.
     *
     * @param postalCode the postal code
     * @param country the country code
     * @param service the service code
     * @return the primary message, as codewords
     */
    private static int[] getMode2PrimaryCodewords(String postalCode, int country, int service) {

        int postcodeNum = Integer.parseInt(postalCode);

        int[] primary = new int[10];
        primary[0] = ((postcodeNum & 0x03) << 4) | 2;
        primary[1] = ((postcodeNum & 0xfc) >> 2);
        primary[2] = ((postcodeNum & 0x3f00) >> 8);
        primary[3] = ((postcodeNum & 0xfc000) >> 14);
        primary[4] = ((postcodeNum & 0x3f00000) >> 20);
        primary[5] = ((postcodeNum & 0x3c000000) >> 26) | ((postalCode.length() & 0x3) << 4);
        primary[6] = ((postalCode.length() & 0x3c) >> 2) | ((country & 0x3) << 4);
        primary[7] = (country & 0xfc) >> 2;
        primary[8] = ((country & 0x300) >> 8) | ((service & 0xf) << 2);
        primary[9] = ((service & 0x3f0) >> 4);

        return primary;
    }

    /**
     * Returns the primary message codewords for mode 3. Assumes that the postal code has
     * already been validated to contain only characters in Code Set A.
     *
     * @param postalCode the postal code
     * @param country the country code
     * @param service the service code
     * @return the primary message, as codewords
     */
    private static int[] getMode3PrimaryCodewords(String postalCode, int country, int service) {

        int[] values = new int[postalCode.length()];

        for (int i = 0; i < values.length; i++) {
            char c = postalCode.charAt(i);
            values[i] = c;
            if (c >= 'A' && c <= 'Z') {
                values[i] -= 64; // shift to Code Set A value
            }
        }

        int[] primary = new int[10];
        primary[0] = ((values[5] & 0x03) << 4) | 3;
        primary[1] = ((values[4] & 0x03) << 4) | ((values[5] & 0x3c) >> 2);
        primary[2] = ((values[3] & 0x03) << 4) | ((values[4] & 0x3c) >> 2);
        primary[3] = ((values[2] & 0x03) << 4) | ((values[3] & 0x3c) >> 2);
        primary[4] = ((values[1] & 0x03) << 4) | ((values[2] & 0x3c) >> 2);
        primary[5] = ((values[0] & 0x03) << 4) | ((values[1] & 0x3c) >> 2);
        primary[6] = ((values[0] & 0x3c) >> 2) | ((country & 0x3) << 4);
        primary[7] = (country & 0xfc) >> 2;
        primary[8] = ((country & 0x300) >> 8) | ((service & 0xf) << 2);
        primary[9] = ((service & 0x3f0) >> 4);

        return primary;
    }

    /**
     * Formats text according to Appendix A, populating the {@link #set} and {@link #character} arrays.
     *
     * @return true if the content fits in this symbol and was formatted; false otherwise
     */
    private void processText() {

        int length = content.length();
        int i, j, count, current_set;

        if (length > 138) {
            throw OkapiInputException.inputTooLong();
        }

        for (i = 0; i < 144; i++) {
            set[i] = -1;
            character[i] = 0;
        }

        for (i = 0; i < length; i++) {
            /* Look up characters in table from Appendix A - this gives
             value and code set for most characters */
            set[i] = MAXICODE_SET[inputData[i]];
            character[i] = MAXICODE_SYMBOL_CHAR[inputData[i]];
        }

        // If a character can be represented in more than one code set, pick which version to use.
        if (set[0] == 0) {
            if (character[0] == 13) {
                character[0] = 0;
            }
            set[0] = 1;
        }

        for (i = 1; i < length; i++) {
            if (set[i] == 0) {
                /* Special character that can be represented in more than one code set. */
                if (character[i] == 13) {
                    /* Carriage Return */
                    set[i] = bestSurroundingSet(i, length, 1, 5);
                    if (set[i] == 5) {
                        character[i] = 13;
                    } else {
                        character[i] = 0;
                    }
                } else if (character[i] == 28) {
                    /* FS */
                    set[i] = bestSurroundingSet(i, length, 1, 2, 3, 4, 5);
                    if (set[i] == 5) {
                        character[i] = 32;
                    }
                } else if (character[i] == 29) {
                    /* GS */
                    set[i] = bestSurroundingSet(i, length, 1, 2, 3, 4, 5);
                    if (set[i] == 5) {
                        character[i] = 33;
                    }
                } else if (character[i] == 30) {
                    /* RS */
                    set[i] = bestSurroundingSet(i, length, 1, 2, 3, 4, 5);
                    if (set[i] == 5) {
                        character[i] = 34;
                    }
                } else if (character[i] == 32) {
                    /* Space */
                    set[i] = bestSurroundingSet(i, length, 1, 2, 3, 4, 5);
                    if (set[i] == 1) {
                        character[i] = 32;
                    } else if (set[i] == 2) {
                        character[i] = 47;
                    } else {
                        character[i] = 59;
                    }
                } else if (character[i] == 44) {
                    /* Comma */
                    set[i] = bestSurroundingSet(i, length, 1, 2);
                    if (set[i] == 2) {
                        character[i] = 48;
                    }
                } else if (character[i] == 46) {
                    /* Full Stop */
                    set[i] = bestSurroundingSet(i, length, 1, 2);
                    if (set[i] == 2) {
                        character[i] = 49;
                    }
                } else if (character[i] == 47) {
                    /* Slash */
                    set[i] = bestSurroundingSet(i, length, 1, 2);
                    if (set[i] == 2) {
                        character[i] = 50;
                    }
                } else if (character[i] == 58) {
                    /* Colon */
                    set[i] = bestSurroundingSet(i, length, 1, 2);
                    if (set[i] == 2) {
                        character[i] = 51;
                    }
                }
            }
        }

        for (i = length; i < set.length; i++) {
            /* Add the padding */
            if (set[length - 1] == 2) {
                set[i] = 2;
            } else {
                set[i] = 1;
            }
            character[i] = 33;
        }

        /* Find candidates for number compression (not allowed in primary message in modes 2 and 3). */
        if (mode == 2 || mode == 3) {
            j = 9;
        } else {
            j = 0;
        }
        count = 0;
        for (i = j; i < 143; i++) {
            if (set[i] == 1 && character[i] >= 48 && character[i] <= 57) {
                /* Character is a number */
                count++;
            } else {
                count = 0;
            }
            if (count == 9) {
                /* Nine digits in a row can be compressed */
                set[i] = 6;
                set[i - 1] = 6;
                set[i - 2] = 6;
                set[i - 3] = 6;
                set[i - 4] = 6;
                set[i - 5] = 6;
                set[i - 6] = 6;
                set[i - 7] = 6;
                set[i - 8] = 6;
                count = 0;
            }
        }

        /* Add shift and latch characters */
        current_set = 1;
        i = 0;
        do {
            if ((set[i] != current_set) && (set[i] != 6)) {
                switch (set[i]) {
                    case 1:
                        if (i + 1 < set.length && set[i + 1] == 1) {
                            if (i + 2 < set.length && set[i + 2] == 1) {
                                if (i + 3 < set.length && set[i + 3] == 1) {
                                    /* Latch A */
                                    insert(i, 63);
                                    current_set = 1;
                                    length++;
                                    i += 3;
                                } else {
                                    /* 3 Shift A */
                                    insert(i, 57);
                                    length++;
                                    i += 2;
                                }
                            } else {
                                /* 2 Shift A */
                                insert(i, 56);
                                length++;
                                i++;
                            }
                        } else {
                            /* Shift A */
                            insert(i, 59);
                            length++;
                        }
                        break;
                    case 2:
                        if (i + 1 < set.length && set[i + 1] == 2) {
                            /* Latch B */
                            insert(i, 63);
                            current_set = 2;
                            length++;
                            i++;
                        } else {
                            /* Shift B */
                            insert(i, 59);
                            length++;
                        }
                        break;
                    case 3:
                        if (i + 3 < set.length && set[i + 1] == 3 && set[i + 2] == 3 && set[i + 3] == 3) {
                            /* Lock In C */
                            insert(i, 60);
                            insert(i, 60);
                            current_set = 3;
                            length++;
                            i += 3;
                        } else {
                            /* Shift C */
                            insert(i, 60);
                            length++;
                        }
                        break;
                    case 4:
                        if (i + 3 < set.length && set[i + 1] == 4 && set[i + 2] == 4 && set[i + 3] == 4) {
                            /* Lock In D */
                            insert(i, 61);
                            insert(i, 61);
                            current_set = 4;
                            length++;
                            i += 3;
                        } else {
                            /* Shift D */
                            insert(i, 61);
                            length++;
                        }
                        break;
                    case 5:
                        if (i + 3 < set.length && set[i + 1] == 5 && set[i + 2] == 5 && set[i + 3] == 5) {
                            /* Lock In E */
                            insert(i, 62);
                            insert(i, 62);
                            current_set = 5;
                            length++;
                            i += 3;
                        } else {
                            /* Shift E */
                            insert(i, 62);
                            length++;
                        }
                        break;
                    default:
                        throw new OkapiInternalException("Unexpected set " + set[i] + " at index " + i + ".");
                }
                i++;
            }
            i++;
        } while (i < set.length);

        /* Number compression has not been forgotten! It's handled below. */
        i = 0;
        do {
            if (set[i] == 6) {
                /* Number compression */
                int value = 0;
                for (j = 0; j < 9; j++) {
                    value *= 10;
                    value += (character[i + j] - '0');
                }
                character[i] = 31; /* NS */
                character[i + 1] = (value & 0x3f000000) >> 24;
                character[i + 2] = (value & 0xfc0000) >> 18;
                character[i + 3] = (value & 0x3f000) >> 12;
                character[i + 4] = (value & 0xfc0) >> 6;
                character[i + 5] = (value & 0x3f);
                i += 6;
                for (j = i; j < 140; j++) {
                    set[j] = set[j + 3];
                    character[j] = character[j + 3];
                }
                length -= 3;
            } else {
                i++;
            }
        } while (i < set.length);

        /* Inject ECI codes to beginning of data, according to Table 3 */
        if (eciMode != 3) {
            insert(0, 27); // ECI
            if (eciMode >= 0 && eciMode <= 31) {
                insert(1, eciMode & 0x1F);
                length += 2;
            }
            if (eciMode >= 32 && eciMode <= 1023) {
                insert(1, 0x20 + (eciMode >> 6));
                insert(2, eciMode & 0x3F);
                length += 3;
            }
            if (eciMode >= 1024 && eciMode <= 32767) {
                insert(1, 0x30 + (eciMode >> 12));
                insert(2, (eciMode >> 6) & 0x3F);
                insert(3, eciMode & 0x3F);
                length += 4;
            }
            if (eciMode >= 32768 && eciMode <= 999999) {
                insert(1, 0x38 + (eciMode >> 18));
                insert(2, (eciMode >> 12) & 0x3F);
                insert(3, (eciMode >> 6) & 0x3F);
                insert(4, eciMode & 0x3F);
                length += 5;
            }
        }

        /* Make sure we haven't exceeded the maximum data length. */
        int maxLength;
        if (mode == 2 || mode == 3) {
            maxLength = 84;
        } else if (mode == 4 || mode == 6) {
            maxLength = 93;
        } else {
            assert mode == 5;
            maxLength = 77;
        }
        if (length > maxLength) {
            throw OkapiInputException.inputTooLong();
        }
    }

    /**
     * Guesses the best set to use at the specified index by looking at the surrounding sets. In general, characters in
     * lower-numbered sets are more common, so we choose them if we can. If no good surrounding sets can be found, the default
     * value returned is the first value from the valid set.
     *
     * @param index the current index
     * @param length the maximum length to look at
     * @param valid the valid sets for this index
     * @return the best set to use at the specified index
     */
    private int bestSurroundingSet(int index, int length, int... valid) {
        int option1 = set[index - 1];
        if (index + 1 < length) {
            // we have two options to check
            int option2 = set[index + 1];
            if (contains(valid, option1) && contains(valid, option2)) {
                return Math.min(option1, option2);
            } else if (contains(valid, option1)) {
                return option1;
            } else if (contains(valid, option2)) {
                return option2;
            } else {
                return valid[0];
            }
        } else {
            // we only have one option to check
            if (contains(valid, option1)) {
                return option1;
            } else {
                return valid[0];
            }
        }
    }

    /**
     * Moves everything up so that the specified shift or latch character can be inserted.
     *
     * @param position the position beyond which everything needs to be shifted
     * @param c the latch or shift character to insert at the specified position, after everything has been shifted
     */
    private void insert(int position, int c) {
        for (int i = 143; i > position; i--) {
            set[i] = set[i - 1];
            character[i] = character[i - 1];
        }
        character[position] = c;
    }

    /**
     * Returns the error correction codewords for the specified data codewords.
     *
     * @param codewords the codewords that we need error correction codewords for
     * @param ecclen the number of error correction codewords needed
     * @return the error correction codewords for the specified data codewords
     */
    private static int[] getErrorCorrection(int[] codewords, int ecclen) {

        ReedSolomon rs = new ReedSolomon();
        rs.init_gf(0x43);
        rs.init_code(ecclen, 1);
        rs.encode(codewords.length, codewords);

        int[] results = new int[ecclen];
        for (int i = 0; i < ecclen; i++) {
            results[i] = rs.getResult(results.length - 1 - i);
        }

        return results;
    }

    /** {@inheritDoc} */
    @Override
    protected void plotSymbol() {

        resetPlotElements();

        // this is a very different symbology, but for consistency with all of the other
        // 2D matrix symbologies, we scale the symbol up by the module width (the hexagons
        // are considered "modules" in the MaxiCode spec)
        int m = moduleWidth;
        symbol_height = 72 * m;
        symbol_width = 74 * m;

        // hexagons
        for (int row = 0; row < 33; row++) {
            for (int col = 0; col < 30; col++) {
                if (grid[row][col]) {
                    double x = ((2.46 * col) + 1.23) * m;
                    if ((row & 1) != 0) {
                        x += 1.23 * m;
                    }
                    double y = ((2.135 * row) + 1.43) * m;
                    hexagons.add(new Hexagon(x, y, m));
                }
            }
        }

        // circles
        double x = 35.76 * m;
        double y = 35.60 * m;
        target.add(new Circle(x, y, 10.85 * m));
        target.add(new Circle(x, y, 8.97 * m));
        target.add(new Circle(x, y, 7.10 * m));
        target.add(new Circle(x, y, 5.22 * m));
        target.add(new Circle(x, y, 3.31 * m));
        target.add(new Circle(x, y, 1.43 * m));
    }

    /** {@inheritDoc} */
    @Override
    protected int[] getCodewords() {
        return codewords;
    }
}
