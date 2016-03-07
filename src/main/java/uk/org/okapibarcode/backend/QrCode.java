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
 * Implements QR Code bar code symbology According to ISO/IEC 18004:2015
 * <br>
 * The maximum capacity of a (version 40) QR Code symbol is 7089 numeric digits,
 * 4296 alphanumeric characters or 2953 bytes of data. QR Code symbols can also
 * be used to encode GS1 data. QR Code symbols can encode characters in the
 * Latin-1 set and Kanji characters which are members of the Shift-JIS encoding
 * scheme.
 *
 * @author <a href="mailto:rstuart114@gmail.com">Robin Stuart</a>
 */
public class QrCode extends Symbol {

    private enum qrMode {

        NULL, KANJI, BINARY, ALPHANUM, NUMERIC
    }

    public enum EccMode {

        L, M, Q, H
    }
    private qrMode[] inputMode;
    private qrMode[] proposedMode;
    private String binary;
    private int[] datastream;
    private int[] fullstream;
    private int[] inputData;
    private byte[] grid;
    private byte[] eval;
    private int preferredVersion = 0;
    private int inputLength;

    /**
     * Sets the preferred symbol size. This value may be ignored if the data
     * string is too large to fit into the specified symbol. Input values
     * correspond to symbol sizes as shown in the following table.
     * <table summary="Available QR Code sizes">
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
     * 21 x 21</p></td>
     * <td><p>
     * 21</p></td>
     * <td><p>
     * 101 x 101</p></td>
     * </tr>
     * <tr>
     * <td><p>
     * 2</p></td>
     * <td><p>
     * 25 x 25</p></td>
     * <td><p>
     * 22</p></td>
     * <td><p>
     * 105 x 105</p></td>
     * </tr>
     * <tr>
     * <td><p>
     * 3</p></td>
     * <td><p>
     * 29 x 29</p></td>
     * <td><p>
     * 23</p></td>
     * <td><p>
     * 109 x 109</p></td>
     * </tr>
     * <tr>
     * <td><p>
     * 4</p></td>
     * <td><p>
     * 33 x 33</p></td>
     * <td><p>
     * 24</p></td>
     * <td><p>
     * 113 x 113</p></td>
     * </tr>
     * <tr>
     * <td><p>
     * 5</p></td>
     * <td><p>
     * 37 x 37</p></td>
     * <td><p>
     * 25</p></td>
     * <td><p>
     * 117 x 117</p></td>
     * </tr>
     * <tr>
     * <td><p>
     * 6</p></td>
     * <td><p>
     * 41 x 41</p></td>
     * <td><p>
     * 26</p></td>
     * <td><p>
     * 121 x 121</p></td>
     * </tr>
     * <tr>
     * <td><p>
     * 7</p></td>
     * <td><p>
     * 45 x 45</p></td>
     * <td><p>
     * 27</p></td>
     * <td><p>
     * 125 x 125</p></td>
     * </tr>
     * <tr>
     * <td><p>
     * 8</p></td>
     * <td><p>
     * 49 x 49</p></td>
     * <td><p>
     * 28</p></td>
     * <td><p>
     * 129 x 129</p></td>
     * </tr>
     * <tr>
     * <td><p>
     * 9</p></td>
     * <td><p>
     * 53 x 53</p></td>
     * <td><p>
     * 29</p></td>
     * <td><p>
     * 133 x 133</p></td>
     * </tr>
     * <tr>
     * <td><p>
     * 10</p></td>
     * <td><p>
     * 57 x 57</p></td>
     * <td><p>
     * 30</p></td>
     * <td><p>
     * 137 x 137</p></td>
     * </tr>
     * <tr>
     * <td><p>
     * 11</p></td>
     * <td><p>
     * 61 x 61</p></td>
     * <td><p>
     * 31</p></td>
     * <td><p>
     * 141 x 141</p></td>
     * </tr>
     * <tr>
     * <td><p>
     * 12</p></td>
     * <td><p>
     * 65 x 65</p></td>
     * <td><p>
     * 32</p></td>
     * <td><p>
     * 145 x 145</p></td>
     * </tr>
     * <tr>
     * <td><p>
     * 13</p></td>
     * <td><p>
     * 69 x 69</p></td>
     * <td><p>
     * 33</p></td>
     * <td><p>
     * 149 x 149</p></td>
     * </tr>
     * <tr>
     * <td><p>
     * 14</p></td>
     * <td><p>
     * 73 x 73</p></td>
     * <td><p>
     * 34</p></td>
     * <td><p>
     * 153 x 153</p></td>
     * </tr>
     * <tr>
     * <td><p>
     * 15</p></td>
     * <td><p>
     * 77 x 77</p></td>
     * <td><p>
     * 35</p></td>
     * <td><p>
     * 157 x 157</p></td>
     * </tr>
     * <tr>
     * <td><p>
     * 16</p></td>
     * <td><p>
     * 81 x 81</p></td>
     * <td><p>
     * 36</p></td>
     * <td><p>
     * 161 x 161</p></td>
     * </tr>
     * <tr>
     * <td><p>
     * 17</p></td>
     * <td><p>
     * 85 x 85</p></td>
     * <td><p>
     * 37</p></td>
     * <td><p>
     * 165 x 165</p></td>
     * </tr>
     * <tr>
     * <td><p>
     * 18</p></td>
     * <td><p>
     * 89 x 89</p></td>
     * <td><p>
     * 38</p></td>
     * <td><p>
     * 169 x 169</p></td>
     * </tr>
     * <tr>
     * <td><p>
     * 19</p></td>
     * <td><p>
     * 93 x 93</p></td>
     * <td><p>
     * 39</p></td>
     * <td><p>
     * 173 x 173</p></td>
     * </tr>
     * <tr>
     * <td><p>
     * 20</p></td>
     * <td><p>
     * 97 x 97</p></td>
     * <td><p>
     * 40</p></td>
     * <td><p>
     * 177 x 177</p></td>
     * </tr>
     * </tbody>
     * </table>
     *
     * @param version Symbol version number required
     */
    public void setPreferredVersion(int version) {
        preferredVersion = version;
    }

    private EccMode preferredEccLevel = EccMode.L;

    /**
     * Set the amount of symbol space allocated to error correction. Levels are
     * predefined according to the following table:
     * <table summary="QR Code error correction levels">
     * <tbody>
     * <tr>
     * <th>ECC Level</th>
     * <th>Error Correction Capacity</th>
     * <th>Recovery Capacity</th>
     * </tr>
     * <tr>
     * <td>L (default)</td>
     * <td>Approx 20% of symbol</td>
     * <td>Approx 7%</td>
     * </tr>
     * <tr>
     * <td>M</td>
     * <td>Approx 37% of symbol</td>
     * <td>Approx 15%</td>
     * </tr>
     * <tr>
     * <td>Q</td>
     * <td>Approx 55% of symbol</td>
     * <td>Approx 25%</td>
     * </tr>
     * <tr>
     * <td>H</td>
     * <td>Approx 65% of symbol</td>
     * <td>Approx 30%</td>
     * </tr>
     * </tbody>
     * </table>
     *
     * @param eccMode Error correction level
     */
    public void setEccMode(EccMode eccMode) {
        preferredEccLevel = eccMode;
    }

    /* Table 5 - Encoding/Decoding table for Alphanumeric mode */
    private final char[] rhodium = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E',
        'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
        'U', 'V', 'W', 'X', 'Y', 'Z', ' ', '$', '%', '*', '+', '-', '.', '/', ':'
    };

    private final int[] qr_data_codewords_L = {
        19, 34, 55, 80, 108, 136, 156, 194, 232, 274, 324, 370, 428, 461, 523, 589, 647,
        721, 795, 861, 932, 1006, 1094, 1174, 1276, 1370, 1468, 1531, 1631,
        1735, 1843, 1955, 2071, 2191, 2306, 2434, 2566, 2702, 2812, 2956
    };

    private final int[] qr_data_codewords_M = {
        16, 28, 44, 64, 86, 108, 124, 154, 182, 216, 254, 290, 334, 365, 415, 453, 507,
        563, 627, 669, 714, 782, 860, 914, 1000, 1062, 1128, 1193, 1267,
        1373, 1455, 1541, 1631, 1725, 1812, 1914, 1992, 2102, 2216, 2334
    };

    private final int[] qr_data_codewords_Q = {
        13, 22, 34, 48, 62, 76, 88, 110, 132, 154, 180, 206, 244, 261, 295, 325, 367,
        397, 445, 485, 512, 568, 614, 664, 718, 754, 808, 871, 911,
        985, 1033, 1115, 1171, 1231, 1286, 1354, 1426, 1502, 1582, 1666
    };

    private final int[] qr_data_codewords_H = {
        9, 16, 26, 36, 46, 60, 66, 86, 100, 122, 140, 158, 180, 197, 223, 253, 283,
        313, 341, 385, 406, 442, 464, 514, 538, 596, 628, 661, 701,
        745, 793, 845, 901, 961, 986, 1054, 1096, 1142, 1222, 1276
    };

    private final int[] qr_blocks_L = {
        1, 1, 1, 1, 1, 2, 2, 2, 2, 4, 4, 4, 4, 4, 6, 6, 6, 6, 7, 8, 8, 9, 9, 10, 12, 12,
        12, 13, 14, 15, 16, 17, 18, 19, 19, 20, 21, 22, 24, 25
    };

    private final int[] qr_blocks_M = {
        1, 1, 1, 2, 2, 4, 4, 4, 5, 5, 5, 8, 9, 9, 10, 10, 11, 13, 14, 16, 17, 17, 18, 20,
        21, 23, 25, 26, 28, 29, 31, 33, 35, 37, 38, 40, 43, 45, 47, 49
    };

    private final int[] qr_blocks_Q = {
        1, 1, 2, 2, 4, 4, 6, 6, 8, 8, 8, 10, 12, 16, 12, 17, 16, 18, 21, 20, 23, 23, 25,
        27, 29, 34, 34, 35, 38, 40, 43, 45, 48, 51, 53, 56, 59, 62, 65, 68
    };

    private final int[] qr_blocks_H = {
        1, 1, 2, 4, 4, 4, 5, 6, 8, 8, 11, 11, 16, 16, 18, 16, 19, 21, 25, 25, 25, 34, 30,
        32, 35, 37, 40, 42, 45, 48, 51, 54, 57, 60, 63, 66, 70, 74, 77, 81
    };

    private final int[] qr_total_codewords = {
        26, 44, 70, 100, 134, 172, 196, 242, 292, 346, 404, 466, 532, 581, 655, 733, 815,
        901, 991, 1085, 1156, 1258, 1364, 1474, 1588, 1706, 1828, 1921, 2051,
        2185, 2323, 2465, 2611, 2761, 2876, 3034, 3196, 3362, 3532, 3706
    };

    private final int[] qr_sizes = {
        21, 25, 29, 33, 37, 41, 45, 49, 53, 57, 61, 65, 69, 73, 77, 81, 85, 89, 93, 97,
        101, 105, 109, 113, 117, 121, 125, 129, 133, 137, 141, 145, 149, 153, 157, 161, 165, 169, 173, 177
    };

    private final int[] qr_align_loopsize = {
        0, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 7
    };

    private final int[] qr_table_e1 = {
        6, 18, 0, 0, 0, 0, 0,
        6, 22, 0, 0, 0, 0, 0,
        6, 26, 0, 0, 0, 0, 0,
        6, 30, 0, 0, 0, 0, 0,
        6, 34, 0, 0, 0, 0, 0,
        6, 22, 38, 0, 0, 0, 0,
        6, 24, 42, 0, 0, 0, 0,
        6, 26, 46, 0, 0, 0, 0,
        6, 28, 50, 0, 0, 0, 0,
        6, 30, 54, 0, 0, 0, 0,
        6, 32, 58, 0, 0, 0, 0,
        6, 34, 62, 0, 0, 0, 0,
        6, 26, 46, 66, 0, 0, 0,
        6, 26, 48, 70, 0, 0, 0,
        6, 26, 50, 74, 0, 0, 0,
        6, 30, 54, 78, 0, 0, 0,
        6, 30, 56, 82, 0, 0, 0,
        6, 30, 58, 86, 0, 0, 0,
        6, 34, 62, 90, 0, 0, 0,
        6, 28, 50, 72, 94, 0, 0,
        6, 26, 50, 74, 98, 0, 0,
        6, 30, 54, 78, 102, 0, 0,
        6, 28, 54, 80, 106, 0, 0,
        6, 32, 58, 84, 110, 0, 0,
        6, 30, 58, 86, 114, 0, 0,
        6, 34, 62, 90, 118, 0, 0,
        6, 26, 50, 74, 98, 122, 0,
        6, 30, 54, 78, 102, 126, 0,
        6, 26, 52, 78, 104, 130, 0,
        6, 30, 56, 82, 108, 134, 0,
        6, 34, 60, 86, 112, 138, 0,
        6, 30, 58, 86, 114, 142, 0,
        6, 34, 62, 90, 118, 146, 0,
        6, 30, 54, 78, 102, 126, 150,
        6, 24, 50, 76, 102, 128, 154,
        6, 28, 54, 80, 106, 132, 158,
        6, 32, 58, 84, 110, 136, 162,
        6, 26, 54, 82, 110, 138, 166,
        6, 30, 58, 86, 114, 142, 170
    };

    private final int[] qr_annex_c = {
        /* Format information bit sequences */
        0x5412, 0x5125, 0x5e7c, 0x5b4b, 0x45f9, 0x40ce, 0x4f97, 0x4aa0, 0x77c4, 0x72f3, 0x7daa, 0x789d,
        0x662f, 0x6318, 0x6c41, 0x6976, 0x1689, 0x13be, 0x1ce7, 0x19d0, 0x0762, 0x0255, 0x0d0c, 0x083b,
        0x355f, 0x3068, 0x3f31, 0x3a06, 0x24b4, 0x2183, 0x2eda, 0x2bed
    };

    private final long[] qr_annex_d = {
        /* Version information bit sequences */
        0x07c94, 0x085bc, 0x09a99, 0x0a4d3, 0x0bbf6, 0x0c762, 0x0d847, 0x0e60d, 0x0f928, 0x10b78,
        0x1145d, 0x12a17, 0x13532, 0x149a6, 0x15683, 0x168c9, 0x177ec, 0x18ec4, 0x191e1, 0x1afab,
        0x1b08e, 0x1cc1a, 0x1d33f, 0x1ed75, 0x1f250, 0x209d5, 0x216f0, 0x228ba, 0x2379f, 0x24b0b,
        0x2542e, 0x26a64, 0x27541, 0x28c69
    };

    @Override
    public boolean encode() {
        int i, j;
        int est_binlen;
        EccMode ecc_level;
        int max_cw;
        int autosize;
        int targetCwCount, version, blocks;
        int size;
        int bitmask;
        String bin;
        boolean canShrink;

        /* This code uses modeFirstFix to make an estimate of the symbol size
         needed to contain the data. It then optimises the encoding and
         checks to see if the the data will fit in a smaller
         symbol, recalculating the binary length if necessary.
         */
        inputMode = new qrMode[content.length()];
        modeFirstFix();
        est_binlen = estimate_binary_length();

        ecc_level = preferredEccLevel;
        switch (preferredEccLevel) {
            case L:
                max_cw = 2956;
                break;
            case M:
                max_cw = 2334;
                break;
            case Q:
                max_cw = 1666;
                break;
            case H:
                max_cw = 1276;
                break;
            default:
                max_cw = 2956;
                break;
        }

        autosize = 40;
        for (i = 39; i >= 0; i--) {
            switch (ecc_level) {
                case L:
                    if ((8 * qr_data_codewords_L[i]) >= est_binlen) {
                        autosize = i + 1;
                    }
                    break;
                case M:
                    if ((8 * qr_data_codewords_M[i]) >= est_binlen) {
                        autosize = i + 1;
                    }
                    break;
                case Q:
                    if ((8 * qr_data_codewords_Q[i]) >= est_binlen) {
                        autosize = i + 1;
                    }
                    break;
                case H:
                    if ((8 * qr_data_codewords_H[i]) >= est_binlen) {
                        autosize = i + 1;
                    }
                    break;
            }
        }

        // The first guess of symbol size is in autosize. Use this to optimise.
        est_binlen = getBinaryLength(autosize);

        if (est_binlen > (8 * max_cw)) {
            error_msg = "Input too long for selected error correction level";
            return false;
        }

        // Now see if the optimised binary will fit in a smaller symbol.
        canShrink = true;

        do {
            if (autosize == 1) {
                canShrink = false;
            } else {
                if (tribus(autosize - 1, 1, 2, 3) != tribus(autosize, 1, 2, 3)) {
                    // Length of binary needed to encode the data in the smaller symbol is different, recalculate
                    est_binlen = getBinaryLength(autosize - 1);
                }

                switch (ecc_level) {
                    case L:
                        if ((8 * qr_data_codewords_L[autosize - 2]) < est_binlen) {
                            canShrink = false;
                        }
                        break;
                    case M:
                        if ((8 * qr_data_codewords_M[autosize - 2]) < est_binlen) {
                            canShrink = false;
                        }
                        break;
                    case Q:
                        if ((8 * qr_data_codewords_Q[autosize - 2]) < est_binlen) {
                            canShrink = false;
                        }
                        break;
                    case H:
                        if ((8 * qr_data_codewords_H[autosize - 2]) < est_binlen) {
                            canShrink = false;
                        }
                        break;
                }

                if (canShrink) {
                    // Optimisation worked - data will fit in a smaller symbol
                    autosize--;
                } else {
                    // Data did not fit in the smaller symbol, revert to original size
                    if (tribus(autosize - 1, 1, 2, 3) != tribus(autosize, 1, 2, 3)) {
                        est_binlen = getBinaryLength(autosize);
                    }
                }
            }
        } while (canShrink);

        version = autosize;

        if ((preferredVersion >= 1) && (preferredVersion <= 40)) {
            /* If the user has selected a larger symbol than the smallest available,
             then use the size the user has selected, and re-optimise for this
             symbol size.
             */
            if (preferredVersion > version) {
                version = preferredVersion;
                est_binlen = getBinaryLength(preferredVersion);
            }
        }

        /* Ensure maxium error correction capacity */
        if (est_binlen <= (qr_data_codewords_M[version - 1] * 8)) {
            ecc_level = EccMode.M;
        }
        if (est_binlen <= (qr_data_codewords_Q[version - 1] * 8)) {
            ecc_level = EccMode.Q;
        }
        if (est_binlen <= (qr_data_codewords_H[version - 1] * 8)) {
            ecc_level = EccMode.H;
        }

        targetCwCount = qr_data_codewords_L[version - 1];
        blocks = qr_blocks_L[version - 1];
        switch (ecc_level) {
            case M:
                targetCwCount = qr_data_codewords_M[version - 1];
                blocks = qr_blocks_M[version - 1];
                break;
            case Q:
                targetCwCount = qr_data_codewords_Q[version - 1];
                blocks = qr_blocks_Q[version - 1];
                break;
            case H:
                targetCwCount = qr_data_codewords_H[version - 1];
                blocks = qr_blocks_H[version - 1];
                break;
        }

        datastream = new int[targetCwCount + 1];
        fullstream = new int[qr_total_codewords[version - 1] + 1];

        if (!(qr_binary(version, targetCwCount))) {
            /* Invalid characters used - stop encoding */
            return false;
        }

        add_ecc(version, targetCwCount, blocks);

        size = qr_sizes[version - 1];

        grid = new byte[size * size];

        encodeInfo += "Version: " + version + "\n";
        encodeInfo += "ECC Level: ";
        switch (ecc_level) {
            case L:
                encodeInfo += "L\n";
                break;
            case M:
                encodeInfo += "M\n";
                break;
            case Q:
                encodeInfo += "Q\n";
                break;
            case H:
            default:
                encodeInfo += "H\n";
                break;
        }

        for (i = 0; i < size; i++) {
            for (j = 0; j < size; j++) {
                grid[(i * size) + j] = 0;
            }
        }

        setup_grid(size, version);
        populate_grid(size, qr_total_codewords[version - 1]);
        bitmask = apply_bitmask(size, ecc_level);
        encodeInfo += "Mask Pattern: " + Integer.toBinaryString(bitmask) + "\n";
        add_format_info(size, ecc_level, bitmask);
        if (version >= 7) {
            add_version_info(size, version);
        }

        readable = "";
        pattern = new String[size];
        row_count = size;
        row_height = new int[size];
        for (i = 0; i < size; i++) {
            bin = "";
            for (j = 0; j < size; j++) {
                if ((grid[(i * size) + j] & 0x01) != 0) {
                    bin += "1";
                } else {
                    bin += "0";
                }
            }
            pattern[i] = bin2pat(bin);
            row_height[i] = 1;
        }

        plotSymbol();
        return true;
    }

    private void modeFirstFix() {
        int i;

        eciProcess(); // Get ECI mode

        if (eciMode == 20) {
            /* Shift-JIS encoding, use Kanji mode */
            inputLength = content.length();
            inputData = new int[inputLength];
            for (i = 0; i < inputLength; i++) {
                inputData[i] = (int) content.charAt(i);
            }
        } else {
            /* Any other encoding method */
            inputLength = inputBytes.length;
            inputData = new int[inputLength];
            for (i = 0; i < inputLength; i++) {
                inputData[i] = inputBytes[i] & 0xFF;
            }
        }

        inputMode = new qrMode[inputLength];

        for (i = 0; i < inputLength; i++) {
            if (inputData[i] > 0xff) {
                inputMode[i] = qrMode.KANJI;
            } else {
                inputMode[i] = qrMode.BINARY;
                if (isXAlpha((char) (inputData[i] & 0xFF))) {
                    inputMode[i] = qrMode.ALPHANUM;
                }
                if ((inputDataType == DataType.GS1) && (inputData[i] == '[')) {
                    inputMode[i] = qrMode.ALPHANUM;
                }
                if (isXNumeric((char) (inputData[i] & 0xff))) {
                    inputMode[i] = qrMode.NUMERIC;
                }
            }
        }
    }

    private int getBinaryLength(int version) {
        /* Calculate the actial bitlength of the proposed binary string */
        qrMode currentMode;
        int i, j;
        int count = 0;

        applyOptimisation(version);

        currentMode = qrMode.NULL;

        if (eciMode != 3) {
            count += 12;
        }

        if (inputDataType == DataType.GS1) {
            count += 4;
        }

        for (i = 0; i < inputLength; i++) {
            if (inputMode[i] != currentMode) {
                count += 4;
                switch (inputMode[i]) {
                    case KANJI:
                        count += tribus(version, 8, 10, 12);
                        count += (blockLength(i) * 13);
                        break;
                    case BINARY:
                        count += tribus(version, 8, 16, 16);
                        for (j = i; j < (i + blockLength(i)); j++) {
                            if (inputData[j] > 0xff) {
                                count += 16;
                            } else {
                                count += 8;
                            }
                        }
                        break;
                    case ALPHANUM:
                        count += tribus(version, 9, 11, 13);
                        switch (blockLength(i) % 2) {
                            case 0:
                                count += (blockLength(i) / 2) * 11;
                                break;
                            case 1:
                                count += ((blockLength(i) - 1) / 2) * 11;
                                count += 6;
                                break;
                        }
                        break;
                    case NUMERIC:
                        count += tribus(version, 10, 12, 14);
                        switch (blockLength(i) % 3) {
                            case 0:
                                count += (blockLength(i) / 3) * 10;
                                break;
                            case 1:
                                count += ((blockLength(i) - 1) / 3) * 10;
                                count += 4;
                                break;
                            case 2:
                                count += ((blockLength(i) - 2) / 3) * 10;
                                count += 7;
                                break;
                        }
                        break;
                }
                currentMode = inputMode[i];
            }
        }

        return count;
    }

    private void applyOptimisation(int version) {
        /* Implements a custom optimisation algorithm, because implementation
         of the algorithm shown in Annex J.2 created LONGER binary sequences.
         */

        int blockCount = 0;
        int i, j;
        qrMode currentMode = qrMode.NULL;

        for (i = 0; i < inputLength; i++) {
            if (inputMode[i] != currentMode) {
                currentMode = inputMode[i];
                blockCount++;
            }
        }

        int[] blockLength = new int[blockCount];
        qrMode[] blockMode = new qrMode[blockCount];

        j = -1;
        currentMode = qrMode.NULL;
        for (i = 0; i < inputLength; i++) {
            if (inputMode[i] != currentMode) {
                j++;
                blockLength[j] = 1;
                blockMode[j] = inputMode[i];
                currentMode = inputMode[i];
            } else {
                blockLength[j]++;
            }
        }

        if (blockCount > 1) {
            // Search forward
            for (i = 0; i <= (blockCount - 2); i++) {
                if (blockMode[i] == qrMode.BINARY) {
                    switch (blockMode[i + 1]) {
                        case KANJI:
                            if (blockLength[i + 1] < tribus(version, 4, 5, 6)) {
                                blockMode[i + 1] = qrMode.BINARY;
                            }
                            break;
                        case ALPHANUM:
                            if (blockLength[i + 1] < tribus(version, 7, 8, 9)) {
                                blockMode[i + 1] = qrMode.BINARY;
                            }
                            break;
                        case NUMERIC:
                            if (blockLength[i + 1] < tribus(version, 3, 4, 5)) {
                                blockMode[i + 1] = qrMode.BINARY;
                            }
                            break;
                    }
                }

                if ((blockMode[i] == qrMode.ALPHANUM)
                        && (blockMode[i + 1] == qrMode.NUMERIC)) {
                    if (blockLength[i + 1] < tribus(version, 6, 8, 10)) {
                        blockMode[i + 1] = qrMode.ALPHANUM;
                    }
                }
            }

            // Search backward
            for (i = blockCount - 1; i > 0; i--) {
                if (blockMode[i] == qrMode.BINARY) {
                    switch (blockMode[i - 1]) {
                        case KANJI:
                            if (blockLength[i - 1] < tribus(version, 4, 5, 6)) {
                                blockMode[i - 1] = qrMode.BINARY;
                            }
                            break;
                        case ALPHANUM:
                            if (blockLength[i - 1] < tribus(version, 7, 8, 9)) {
                                blockMode[i - 1] = qrMode.BINARY;
                            }
                            break;
                        case NUMERIC:
                            if (blockLength[i - 1] < tribus(version, 3, 4, 5)) {
                                blockMode[i - 1] = qrMode.BINARY;
                            }
                            break;
                    }
                }

                if ((blockMode[i] == qrMode.ALPHANUM)
                        && (blockMode[i - 1] == qrMode.NUMERIC)) {
                    if (blockLength[i - 1] < tribus(version, 6, 8, 10)) {
                        blockMode[i - 1] = qrMode.ALPHANUM;
                    }
                }
            }
        }

        j = 0;
        for (int block = 0; block < blockCount; block++) {
            currentMode = blockMode[block];
            for (i = 0; i < blockLength[block]; i++) {
                inputMode[j] = currentMode;
                j++;
            }
        }
    }

    private int blockLength(int start) {
        /* Find the length of the block starting from 'start' */
        int i, count;
        qrMode mode = inputMode[start];

        count = 0;
        i = start;

        do {
            count++;
        } while (((i + count) < inputLength) && (inputMode[i + count] == mode));

        return count;
    }

    private int tribus(int version, int a, int b, int c) {
        /* Choose from three numbers based on version */
        int RetVal;

        RetVal = c;

        if (version < 10) {
            RetVal = a;
        }

        if ((version >= 10) && (version <= 26)) {
            RetVal = b;
        }

        return RetVal;
    }

    private boolean isXAlpha(char cglyph) {
        /* Returns true if input is in exclusive Alphanumeric set (Table J.1) */
        boolean retval = false;

        if ((cglyph >= 'A') && (cglyph <= 'Z')) {
            retval = true;
        }
        switch (cglyph) {
            case ' ':
            case '$':
            case '%':
            case '*':
            case '+':
            case '-':
            case '.':
            case '/':
            case ':':
                retval = true;
                break;
        }

        return retval;
    }

    private boolean isXNumeric(char cglyph) {
        /* Returns true if input is in exclusive Numeric set (Table J.1) */
        boolean retval;

        if ((cglyph >= '0') && (cglyph <= '9')) {
            retval = true;
        } else {
            retval = false;
        }

        return retval;
    }

    private int estimate_binary_length() {
        /* Make an estimate (worst case scenario) of how long the binary string will be */
        int i, count = 0;
        qrMode current = qrMode.NULL;
        int a_count = 0;
        int n_count = 0;

        if (eciMode != 3) {
            count += 12;
        }

        if (inputDataType == DataType.GS1) {
            count += 4;
        }

        for (i = 0; i < inputLength; i++) {
            if (inputMode[i] != current) {
                switch (inputMode[i]) {
                    case KANJI:
                        count += 12 + 4;
                        current = qrMode.KANJI;
                        break;
                    case BINARY:
                        count += 16 + 4;
                        current = qrMode.BINARY;
                        break;
                    case ALPHANUM:
                        count += 13 + 4;
                        current = qrMode.ALPHANUM;
                        a_count = 0;
                        break;
                    case NUMERIC:
                        count += 14 + 4;
                        current = qrMode.NUMERIC;
                        n_count = 0;
                        break;
                }
            }

            switch (inputMode[i]) {
                case KANJI:
                    count += 13;
                    break;
                case BINARY:
                    count += 8;
                    break;
                case ALPHANUM:
                    a_count++;
                    if ((a_count & 1) == 0) {
                        count += 5; // 11 in total
                        a_count = 0;
                    } else {
                        count += 6;
                    }
                    break;
                case NUMERIC:
                    n_count++;
                    if ((n_count % 3) == 0) {
                        count += 3; // 10 in total
                        n_count = 0;
                    } else if ((n_count & 1) == 0) {
                        count += 3; // 7 in total
                    } else {
                        count += 4;
                    }
                    break;
            }
        }

        return count;
    }

    private boolean qr_binary(int version, int target_binlen) {
        /* Convert input data to a binary stream and add padding */
        int position = 0;
        int short_data_block_length, i, scheme = 1;
        int padbits;
        int current_binlen, current_bytes;
        int toggle;
        boolean alphanumPercent;
        String oneChar;
        qrMode data_block;
        int jis;
        byte[] jisBytes;
        int msb, lsb, prod;
        int count, first, second, third;
        int weight;

        binary = "";

        /* Note: Shift-JIS characters can be encoded in either Kanji
         mode or Byte mode. If no ECI code is given, a sequence in Byte
         mode could be interpreted in two ways - for example 0xE4 0x6E
         could refer to U+E4 and U+6E (än) or U+8205 (舅). To avoid
         Mojibake, if using ECI 20 (i.e. if only valid Shift-JIS
         characters are found in the input data), this code will insert
         an explicit ECI 20 instruction. Symbols without an ECI can then
         be assumed to be ECI 3 (ISO 8859-1).
         */
        if (eciMode != 3) {
            binary += "0111"; /* ECI */

            if ((eciMode >= 0) && (eciMode <= 127)) {
                binary += "0";
                qr_bscan(eciMode, 0x40);
            }

            if ((eciMode >= 128) && (eciMode <= 16383)) {
                binary += "10";
                qr_bscan(eciMode, 0x1000);
            }

            if ((eciMode >= 16384) && (eciMode <= 999999)) {
                binary += "110";
                qr_bscan(eciMode, 0x100000);
            }
        }

        if (inputDataType == DataType.GS1) {
            binary += "0101"; /* FNC1 */

        }

        if (version <= 9) {
            scheme = 1;
        } else if ((version >= 10) && (version <= 26)) {
            scheme = 2;
        } else if (version >= 27) {
            scheme = 3;
        }

        encodeInfo += "Encoding: ";

        alphanumPercent = false;

        do {
            data_block = inputMode[position];
            short_data_block_length = 0;
            do {
                short_data_block_length++;
            } while (((short_data_block_length + position) < inputLength)
                    && (inputMode[position + short_data_block_length] == data_block));

            switch (data_block) {
                case KANJI:
                    /* Kanji mode */
                    /* Mode indicator */
                    binary += "1000";

                    /* Character count indicator */
                    qr_bscan(short_data_block_length, 0x20 << (scheme * 2)); /* scheme = 1..3 */

                    encodeInfo += "KNJI ";

                    /* Character representation */
                    for (i = 0; i < short_data_block_length; i++) {
                        oneChar = "";
                        oneChar += (char) inputData[position + i];

                        /* Convert Unicode input to Shift-JIS */
                        try {
                            jisBytes = oneChar.getBytes("SJIS");
                        } catch (UnsupportedEncodingException e) {
                            error_msg = "Shift-JIS character conversion error";
                            return false;
                        }

                        jis = ((jisBytes[0] & 0xFF) << 8) + (jisBytes[1] & 0xFF);

                        if (jis > 0x9fff) {
                            jis -= 0xc140;
                        } else {
                            jis -= 0x8140;
                        }
                        msb = (jis & 0xff00) >> 8;
                        lsb = (jis & 0xff);
                        prod = (msb * 0xc0) + lsb;

                        qr_bscan(prod, 0x1000);

                        encodeInfo += Integer.toString(prod) + " ";
                    }
                    break;
                case BINARY:
                    /* Byte mode */
                    /* Mode indicator */
                    binary += "0100";
                    int kanjiModifiedLength = short_data_block_length;

                    for (i = 0; i < short_data_block_length; i++) {
                        if (inputData[position + i] > 0xff) {
                            kanjiModifiedLength++;
                        }
                    }

                    /* Character count indicator */
                    qr_bscan(kanjiModifiedLength, scheme > 1 ? 0x8000 : 0x80); /* scheme = 1..3 */

                    encodeInfo += "BYTE ";

                    /* Character representation */
                    for (i = 0; i < short_data_block_length; i++) {
                        if (inputData[position + i] > 0xff) {
                            // Convert Kanji character to Shift-JIS
                            oneChar = "";
                            oneChar += (char) inputData[position + i];

                            /* Convert Unicode input to Shift-JIS */
                            try {
                                jisBytes = oneChar.getBytes("SJIS");
                            } catch (UnsupportedEncodingException e) {
                                error_msg = "Shift-JIS character conversion error";
                                return false;
                            }

                            qr_bscan((int) jisBytes[0] & 0xff, 0x80);
                            qr_bscan((int) jisBytes[1] & 0xff, 0x80);

                            encodeInfo += "(" + Integer.toString((int) jisBytes[0] & 0xff) + " ";
                            encodeInfo += Integer.toString((int) jisBytes[1] & 0xff) + ") ";
                        } else {
                            // Process 8-bit byte
                            int lbyte = (int) (inputData[position + i] & 0xFF);

                            if ((inputDataType == DataType.GS1) && (lbyte == '[')) {
                                lbyte = 0x1d; /* FNC1 */

                            }

                            qr_bscan(lbyte, 0x80);

                            encodeInfo += Integer.toString(lbyte) + " ";
                        }
                    }

                    break;
                case ALPHANUM:
                    /* Alphanumeric mode */
                    /* Mode indicator */
                    binary += "0010";

                    /* Character count indicator */
                    qr_bscan(short_data_block_length, 0x40 << (2 * scheme)); /* scheme = 1..3 */

                    encodeInfo += "ALPH ";

                    /* Character representation */
                    i = 0;
                    while (i < short_data_block_length) {

                        if (!alphanumPercent) {
                            if ((inputDataType == DataType.GS1) && (inputData[position + i] == '%')) {
                                first = positionOf('%', rhodium);
                                second = positionOf('%', rhodium);
                                count = 2;
                                prod = (first * 45) + second;
                                i++;
                            } else {
                                if ((inputDataType == DataType.GS1) && (inputData[position + i] == '[')) {
                                    first = positionOf('%', rhodium); /* FNC1 */

                                } else {
                                    first = positionOf((char) (inputData[position + i] & 0xFF), rhodium);
                                }
                                count = 1;
                                i++;
                                prod = first;

                                if (i < short_data_block_length) {
                                    if (inputMode[position + i] == qrMode.ALPHANUM) {
                                        if ((inputDataType == DataType.GS1) && (inputData[position + i] == '%')) {
                                            second = positionOf('%', rhodium);
                                            count = 2;
                                            prod = (first * 45) + second;
                                            alphanumPercent = true;
                                        } else {
                                            if ((inputDataType == DataType.GS1) && (inputData[position + i] == '[')) {
                                                second = positionOf('%', rhodium); /* FNC1 */

                                            } else {
                                                second = positionOf((char) (inputData[position + i] & 0xFF), rhodium);
                                            }
                                            count = 2;
                                            i++;
                                            prod = (first * 45) + second;
                                        }
                                    }
                                }
                            }
                        } else {
                            first = positionOf('%', rhodium);
                            count = 1;
                            i++;
                            prod = first;
                            alphanumPercent = false;

                            if (i < short_data_block_length) {
                                if (inputMode[position + i] == qrMode.ALPHANUM) {
                                    if ((inputDataType == DataType.GS1) && (inputData[position + i] == '%')) {
                                        second = positionOf('%', rhodium);
                                        count = 2;
                                        prod = (first * 45) + second;
                                        alphanumPercent = true;
                                    } else {
                                        if ((inputDataType == DataType.GS1) && (inputData[position + i] == '[')) {
                                            second = positionOf('%', rhodium); /* FNC1 */

                                        } else {
                                            second = positionOf((char) (inputData[position + i] & 0xFF), rhodium);
                                        }
                                        count = 2;
                                        i++;
                                        prod = (first * 45) + second;
                                    }
                                }
                            }
                        }

                        qr_bscan(prod, count == 2 ? 0x400 : 0x20); /* count = 1..2 */

                        encodeInfo += Integer.toString(prod) + " ";
                    }
                    ;
                    break;
                case NUMERIC:
                    /* Numeric mode */
                    /* Mode indicator */
                    binary += "0001";

                    /* Character count indicator */
                    qr_bscan(short_data_block_length, 0x80 << (2 * scheme)); /* scheme = 1..3 */

                    encodeInfo += "NUMB ";

                    /* Character representation */
                    i = 0;
                    while (i < short_data_block_length) {

                        first = Character.getNumericValue(inputData[position + i]);
                        count = 1;
                        prod = first;

                        if ((i + 1) < short_data_block_length) {
                            second = Character.getNumericValue(inputData[position + i + 1]);
                            count = 2;
                            prod = (prod * 10) + second;
                        }

                        if ((i + 2) < short_data_block_length) {
                            third = Character.getNumericValue(inputData[position + i + 2]);
                            count = 3;
                            prod = (prod * 10) + third;
                        }

                        qr_bscan(prod, 1 << (3 * count)); /* count = 1..3 */

                        encodeInfo += Integer.toString(prod) + " ";

                        i += count;
                    }
                    ;
                    break;
            }
            position += short_data_block_length;
        } while (position < inputLength);

        encodeInfo += "\n";

        /* Terminator */
        binary += "0000";

        current_binlen = binary.length();
        padbits = 8 - (current_binlen % 8);
        if (padbits == 8) {
            padbits = 0;
        }
        current_bytes = (current_binlen + padbits) / 8;

        /* Padding bits */
        for (i = 0; i < padbits; i++) {
            binary += "0";
        }

        /* Put data into 8-bit codewords */
        for (i = 0; i < current_bytes; i++) {
            datastream[i] = 0x00;
            for (weight = 0; weight < 8; weight++) {
                if (binary.charAt((i * 8) + weight) == '1') {
                    datastream[i] += (0x80 >> weight);
                }
            }
        }

        /* Add pad codewords */
        toggle = 0;
        for (i = current_bytes; i < target_binlen; i++) {
            if (toggle == 0) {
                datastream[i] = 0xec;
                toggle = 1;
            } else {
                datastream[i] = 0x11;
                toggle = 0;
            }
        }

        encodeInfo += "Codewords: ";
        for (i = 0; i < target_binlen; i++) {
            encodeInfo += Integer.toString(datastream[i]) + " ";
        }
        encodeInfo += "\n";

        return true;
    }

    private void qr_bscan(int data, int h) {

        for (;
                (h != 0); h >>= 1) {
            if ((data & h) != 0) {
                binary += "1";
            } else {
                binary += "0";
            }
        }

    }

    private void add_ecc(int version, int data_cw, int blocks) {
        /* Split data into blocks, add error correction and then interleave the blocks and error correction data */
        int ecc_cw = qr_total_codewords[version - 1] - data_cw;
        int short_data_block_length = data_cw / blocks;
        int qty_long_blocks = data_cw % blocks;
        int qty_short_blocks = blocks - qty_long_blocks;
        int ecc_block_length = ecc_cw / blocks;
        int i, j, k, length_this_block, posn;

        int[] data_block = new int[short_data_block_length + 2];
        int[] ecc_block = new int[ecc_block_length + 2];
        int[] interleaved_data = new int[data_cw + 2];
        int[] interleaved_ecc = new int[ecc_cw + 2];

        posn = 0;

        for (i = 0; i < blocks; i++) {
            ReedSolomon rs = new ReedSolomon();
            if (i < qty_short_blocks) {
                length_this_block = short_data_block_length;
            } else {
                length_this_block = short_data_block_length + 1;
            }

            for (j = 0; j < ecc_block_length; j++) {
                ecc_block[j] = 0;
            }

            for (j = 0; j < length_this_block; j++) {
                data_block[j] = datastream[posn + j];
            }

            rs.init_gf(0x11d);
            rs.init_code(ecc_block_length, 0);
            rs.encode(length_this_block, data_block);
            for (k = 0; k < ecc_block_length; k++) {
                ecc_block[k] = rs.getResult(k);
            }
//            if (debug) {
//                System.out.printf("\tBlock %d: ", i + 1);
//                for (j = 0; j < length_this_block; j++) {
//                    System.out.printf("%d ", data_block[j]);
//                }
//                if (i < qty_short_blocks) {
//                    System.out.printf("   ");
//                }
//                System.out.printf(" // ");
//                for (j = 0; j < ecc_block_length; j++) {
//                    System.out.printf("%2X ", ecc_block[ecc_block_length - j - 1]);
//                }
//                System.out.printf("\n");
//            }

            for (j = 0; j < short_data_block_length; j++) {
                interleaved_data[(j * blocks) + i] = data_block[j];
            }

            if (i >= qty_short_blocks) {
                interleaved_data[(short_data_block_length * blocks) + (i - qty_short_blocks)] = data_block[short_data_block_length];
            }

            for (j = 0; j < ecc_block_length; j++) {
                interleaved_ecc[(j * blocks) + i] = ecc_block[ecc_block_length - j - 1];
            }

            posn += length_this_block;
        }

        for (j = 0; j < data_cw; j++) {
            fullstream[j] = interleaved_data[j];
        }
        for (j = 0; j < ecc_cw; j++) {
            fullstream[j + data_cw] = interleaved_ecc[j];
        }

//        if (debug) {
//            System.out.printf("\tStream: ");
//            for (j = 0; j < (data_cw + ecc_cw); j++) {
//                System.out.printf("%d ", fullstream[j]);
//            }
//            System.out.printf("\n");
//        }
    }

    private void setup_grid(int size, int version) {
        int i;
        int loopsize, x, y, xcoord, ycoord;
        boolean toggle = true;

        /* Add timing patterns */
        for (i = 0; i < size; i++) {
            if (toggle) {
                grid[(6 * size) + i] = 0x21;
                grid[(i * size) + 6] = 0x21;
                toggle = false;
            } else {
                grid[(6 * size) + i] = 0x20;
                grid[(i * size) + 6] = 0x20;
                toggle = true;
            }
        }

        /* Add finder patterns */
        place_finder(size, 0, 0);
        place_finder(size, 0, size - 7);
        place_finder(size, size - 7, 0);

        /* Add separators */
        for (i = 0; i < 7; i++) {
            grid[(7 * size) + i] = 0x10;
            grid[(i * size) + 7] = 0x10;
            grid[(7 * size) + (size - 1 - i)] = 0x10;
            grid[(i * size) + (size - 8)] = 0x10;
            grid[((size - 8) * size) + i] = 0x10;
            grid[((size - 1 - i) * size) + 7] = 0x10;
        }
        grid[(7 * size) + 7] = 0x10;
        grid[(7 * size) + (size - 8)] = 0x10;
        grid[((size - 8) * size) + 7] = 0x10;

        /* Add alignment patterns */
        if (version != 1) {
            /* Version 1 does not have alignment patterns */

            loopsize = qr_align_loopsize[version - 1];
            for (x = 0; x < loopsize; x++) {
                for (y = 0; y < loopsize; y++) {
                    xcoord = qr_table_e1[((version - 2) * 7) + x];
                    ycoord = qr_table_e1[((version - 2) * 7) + y];

                    if ((grid[(ycoord * size) + xcoord] & 0x10) == 0) {
                        place_align(size, xcoord, ycoord);
                    }
                }
            }
        }

        /* Reserve space for format information */
        for (i = 0; i < 8; i++) {
            grid[(8 * size) + i] += 0x20;
            grid[(i * size) + 8] += 0x20;
            grid[(8 * size) + (size - 1 - i)] = 0x20;
            grid[((size - 1 - i) * size) + 8] = 0x20;
        }
        grid[(8 * size) + 8] += 0x20;
        grid[((size - 1 - 7) * size) + 8] = 0x21; /* Dark Module from Figure 25 */

        /* Reserve space for version information */
        if (version >= 7) {
            for (i = 0; i < 6; i++) {
                grid[((size - 9) * size) + i] = 0x20;
                grid[((size - 10) * size) + i] = 0x20;
                grid[((size - 11) * size) + i] = 0x20;
                grid[(i * size) + (size - 9)] = 0x20;
                grid[(i * size) + (size - 10)] = 0x20;
                grid[(i * size) + (size - 11)] = 0x20;
            }
        }
    }

    private void place_finder(int size, int x, int y) {
        int xp, yp;

        int[] finder = {
            1, 1, 1, 1, 1, 1, 1,
            1, 0, 0, 0, 0, 0, 1,
            1, 0, 1, 1, 1, 0, 1,
            1, 0, 1, 1, 1, 0, 1,
            1, 0, 1, 1, 1, 0, 1,
            1, 0, 0, 0, 0, 0, 1,
            1, 1, 1, 1, 1, 1, 1
        };

        for (xp = 0; xp < 7; xp++) {
            for (yp = 0; yp < 7; yp++) {
                if (finder[xp + (7 * yp)] == 1) {
                    grid[((yp + y) * size) + (xp + x)] = 0x11;
                } else {
                    grid[((yp + y) * size) + (xp + x)] = 0x10;
                }
            }
        }
    }

    private void place_align(int size, int x, int y) {
        int xp, yp;

        int[] alignment = {
            1, 1, 1, 1, 1,
            1, 0, 0, 0, 1,
            1, 0, 1, 0, 1,
            1, 0, 0, 0, 1,
            1, 1, 1, 1, 1
        };

        x -= 2;
        y -= 2; /* Input values represent centre of pattern */

        for (xp = 0; xp < 5; xp++) {
            for (yp = 0; yp < 5; yp++) {
                if (alignment[xp + (5 * yp)] == 1) {
                    grid[((yp + y) * size) + (xp + x)] = 0x11;
                } else {
                    grid[((yp + y) * size) + (xp + x)] = 0x10;
                }
            }
        }
    }

    private void populate_grid(int size, int cw) {
        boolean goingUp = true;
        int row = 0; /* right hand side */

        int i, n, x, y;

        n = cw * 8;
        y = size - 1;
        i = 0;
        do {
            x = (size - 2) - (row * 2);
            if (x < 6) {
                x--; /* skip over vertical timing pattern */

            }

            if ((grid[(y * size) + (x + 1)] & 0xf0) == 0) {
                if (cwbit(i)) {
                    grid[(y * size) + (x + 1)] = 0x01;
                } else {
                    grid[(y * size) + (x + 1)] = 0x00;
                }
                i++;
            }

            if (i < n) {
                if ((grid[(y * size) + x] & 0xf0) == 0) {
                    if (cwbit(i)) {
                        grid[(y * size) + x] = 0x01;
                    } else {
                        grid[(y * size) + x] = 0x00;
                    }
                    i++;
                }
            }

            if (goingUp) {
                y--;
            } else {
                y++;
            }
            if (y == -1) {
                /* reached the top */
                row++;
                y = 0;
                goingUp = false;
            }
            if (y == size) {
                /* reached the bottom */
                row++;
                y = size - 1;
                goingUp = true;
            }
        } while (i < n);
    }

    private boolean cwbit(int i) {
        boolean resultant = false;

        if ((fullstream[i / 8] & (0x80 >> (i % 8))) != 0) {
            resultant = true;
        }

        return resultant;
    }

    private int apply_bitmask(int size, EccMode ecc_level) {
        int x, y;
        char p;
        int local_pattern;
        int best_val, best_pattern;
        int[] penalty = new int[8];
        byte[] mask = new byte[size * size];
        eval = new byte[size * size];


        /* Perform data masking */
        for (x = 0; x < size; x++) {
            for (y = 0; y < size; y++) {
                mask[(y * size) + x] = 0x00;

                if ((grid[(y * size) + x] & 0xf0) == 0) {
                    if (((y + x) & 1) == 0) {
                        mask[(y * size) + x] += 0x01;
                    }
                    if ((y & 1) == 0) {
                        mask[(y * size) + x] += 0x02;
                    }
                    if ((x % 3) == 0) {
                        mask[(y * size) + x] += 0x04;
                    }
                    if (((y + x) % 3) == 0) {
                        mask[(y * size) + x] += 0x08;
                    }
                    if ((((y / 2) + (x / 3)) & 1) == 0) {
                        mask[(y * size) + x] += 0x10;
                    }
                    if ((((y * x) & 1) + ((y * x) % 3)) == 0) {
                        mask[(y * size) + x] += 0x20;
                    }
                    if (((((y * x) & 1) + ((y * x) % 3)) & 1) == 0) {
                        mask[(y * size) + x] += 0x40;
                    }
                    if (((((y + x) & 1) + ((y * x) % 3)) & 1) == 0) {
                        mask[(y * size) + x] += 0x80;
                    }
                }
            }
        }

        for (x = 0; x < size; x++) {
            for (y = 0; y < size; y++) {
                if ((grid[(y * size) + x] & 0x01) != 0) {
                    p = 0xff;
                } else {
                    p = 0x00;
                }

                eval[(y * size) + x] = (byte) (mask[(y * size) + x] ^ p);
            }
        }


        /* Evaluate result */
        for (local_pattern = 0; local_pattern < 8; local_pattern++) {
            add_format_info_eval(size, ecc_level, local_pattern);
            penalty[local_pattern] = evaluate(size, local_pattern);         
        }

        best_pattern = 0;
        best_val = penalty[0];
        for (local_pattern = 1; local_pattern < 8; local_pattern++) {
            if (penalty[local_pattern] < best_val) {
                best_pattern = local_pattern;
                best_val = penalty[local_pattern];
            }
        }

        /* Apply mask */
        for (x = 0; x < size; x++) {
            for (y = 0; y < size; y++) {
                if ((mask[(y * size) + x] & (0x01 << best_pattern)) != 0) {
                    if ((grid[(y * size) + x] & 0x01) != 0) {
                        grid[(y * size) + x] = 0x00;
                    } else {
                        grid[(y * size) + x] = 0x01;
                    }
                }
            }
        }

        return best_pattern;
    }
    
    private void add_format_info_eval(int size, EccMode ecc_level, int pattern) {
	/* Add format information to grid */

	int format = pattern;
	int seq;
	int i;

	switch(ecc_level) {
		case L: format += 0x08; break;
		case Q: format += 0x18; break;
		case H: format += 0x10; break;
	}

	seq = qr_annex_c[format];
	
        for (i = 0; i < 6; i++) {
            if (((seq >> i) & 0x01) != 0) {
                eval[(i * size) + 8] = (byte)(0x01 >> pattern);
            } else {
                eval[(i * size) + 8] = 0;
            }
        }

        for (i = 0; i < 8; i++) {
            if (((seq >> i) & 0x01) != 0) {
                eval[(8 * size) + (size - i - 1)] = (byte)(0x01 >> pattern);
            } else {
                eval[(8 * size) + (size - i - 1)] = 0;
            }
        }

        for (i = 0; i < 6; i++) {
            if (((seq >> (i + 9)) & 0x01) != 0) {
                eval[(8 * size) + (5 - i)] = (byte)(0x01 >> pattern);
            } else {
                eval[(8 * size) + (5 - i)] = 0;
            }
        }

        for (i = 0; i < 7; i++) {
            if (((seq >> (i + 8)) & 0x01) != 0) {
                eval[(((size - 7) + i) * size) + 8] = (byte)(0x01 >> pattern);
            } else {
                eval[(((size - 7) + i) * size) + 8] = 0;
            }
        }

        if (((seq >> 6) & 0x01) != 0) {
            eval[(7 * size) + 8] = (byte)(0x01 >> pattern);
        } else {
            eval[(7 * size) + 8] = 0;
        }
        
        if (((seq >> 7) & 0x01) != 0) {
            eval[(8 * size) + 8] = (byte)(0x01 >> pattern);
        } else {
            eval[(8 * size) + 8] = 0;
        }
        
        if (((seq >> 8) & 0x01) != 0) {
            eval[(8 * size) + 7] = (byte)(0x01 >> pattern);
        } else {
            eval[(8 * size) + 7] = 0;
        }
    }

    private int evaluate(int size, int pattern) {
        int x, y, block;
        int result = 0;
        int state;
        int p;
        int weight;
        int dark_mods;
        int percentage, k;
        int a, b, afterCount, beforeCount;
        byte[] local = new byte[size * size];

        for (x = 0; x < size; x++) {
            for (y = 0; y < size; y++) {
                if ((eval[(y * size) + x] & (0x01 << pattern)) != 0) {
                    local[(y * size) + x] = '1';
                } else {
                    local[(y * size) + x] = '0';
                }
            }
        }

        /* Test 1: Adjacent modules in row/column in same colour */
        /* Vertical */
        for (x = 0; x < size; x++) {
            state = local[x];
            block = 0;
            for (y = 0; y < size; y++) {
                if (local[(y * size) + x] == state) {
                    block++;
                } else {
                    if (block > 5) {
                        result += (3 + (block - 5));
                    }
                    block = 0;
                    state = local[(y * size) + x];
                }
            }
            if (block > 5) {
                result += (3 + (block - 5));
            }
        }

        /* Horizontal */
        for (y = 0; y < size; y++) {
            state = local[y * size];
            block = 0;
            for (x = 0; x < size; x++) {
                if (local[(y * size) + x] == state) {
                    block++;
                } else {
                    if (block > 5) {
                        result += (3 + (block - 5));
                    }
                    block = 0;
                    state = local[(y * size) + x];
                }
            }
            if (block > 5) {
                result += (3 + (block - 5));
            }
        }

        /* Test 2: Block of modules in same color */
        for (x = 0; x < size - 1; x++) {
            for (y = 0; y < size - 1; y++) {
                if (((local[(y * size) + x] == local[((y + 1) * size) + x])
                        && (local[(y * size) + x] == local[(y * size) + (x + 1)]))
                        && (local[(y * size) + x] == local[((y + 1) * size) + (x + 1)])) {
                    result += 3;
                }
            }
        }

        /* Test 3: 1:1:3:1:1 ratio pattern in row/column */
        /* Vertical */
        for (x = 0; x < size; x++) {
            for (y = 0; y < (size - 7); y++) {
                p = 0;
                for (weight = 0; weight < 7; weight++) {
                    if (local[((y + weight) * size) + x] == '1') {
                        p += (0x40 >> weight);
                    }
                }
                if (p == 0x5d) {
                    /* Pattern found, check before and after */
                    beforeCount = 0;
                    for (b = (y - 4); b < y; b++) {
                        if (b < 0) {
                            beforeCount++;
                        } else {
                            if (local[(b * size) + x] == '0') {
                                beforeCount++;
                            } else {
                                beforeCount = 0;
                            }
                        }
                    }

                    afterCount = 0;
                    for (a = (y + 7); a <= (y + 10); a++) {
                        if (a >= size) {
                            afterCount++;
                        } else {
                            if (local[(a * size) + x] == '0') {
                                afterCount++;
                            } else {
                                afterCount = 0;
                            }
                        }
                    }

                    if ((beforeCount == 4) || (afterCount == 4)) {
                        /* Pattern is preceeded or followed by light area
                         4 modules wide */
                        result += 40;
                    }
                }
            }
        }

        /* Horizontal */
        for (y = 0; y < size; y++) {
            for (x = 0; x < (size - 7); x++) {
                p = 0;
                for (weight = 0; weight < 7; weight++) {
                    if (local[(y * size) + x + weight] == '1') {
                        p += (0x40 >> weight);
                    }
                }
                if (p == 0x5d) {
                    /* Pattern found, check before and after */
                    beforeCount = 0;
                    for (b = (x - 4); b < x; b++) {
                        if (b < 0) {
                            beforeCount++;
                        } else {
                            if (local[(y * size) + b] == '0') {
                                beforeCount++;
                            } else {
                                beforeCount = 0;
                            }
                        }
                    }

                    afterCount = 0;
                    for (a = (x + 7); a <= (x + 10); a++) {
                        if (a >= size) {
                            afterCount++;
                        } else {
                            if (local[(y * size) + a] == '0') {
                                afterCount++;
                            } else {
                                afterCount = 0;
                            }
                        }
                    }

                    if ((beforeCount == 4) || (afterCount == 4)) {
                        /* Pattern is preceeded or followed by light area
                         4 modules wide */
                        result += 40;
                    }
                }
            }
        }

        /* Test 4: Proportion of dark modules in entire symbol */
        dark_mods = 0;
        for (x = 0; x < size; x++) {
            for (y = 0; y < size; y++) {
                if (local[(y * size) + x] == '1') {
                    dark_mods++;
                }
            }
        }
        percentage = 100 * (dark_mods / (size * size));
        if (percentage <= 50) {
            k = ((100 - percentage) - 50) / 5;
        } else {
            k = (percentage - 50) / 5;
        }

        result += 10 * k;

        return result;
    }

    private void add_format_info(int size, EccMode ecc_level, int pattern) {
        /* Add format information to grid */

        int format = pattern;
        int seq;
        int i;

        switch (ecc_level) {
            case L:
                format += 0x08;
                break;
            case Q:
                format += 0x18;
                break;
            case H:
                format += 0x10;
                break;
        }

        seq = qr_annex_c[format];

        for (i = 0; i < 6; i++) {
            grid[(i * size) + 8] += (seq >> i) & 0x01;
        }

        for (i = 0; i < 8; i++) {
            grid[(8 * size) + (size - i - 1)] += (seq >> i) & 0x01;
        }

        for (i = 0; i < 6; i++) {
            grid[(8 * size) + (5 - i)] += (seq >> (i + 9)) & 0x01;
        }

        for (i = 0; i < 7; i++) {
            grid[(((size - 7) + i) * size) + 8] += (seq >> (i + 8)) & 0x01;
        }

        grid[(7 * size) + 8] += (seq >> 6) & 0x01;
        grid[(8 * size) + 8] += (seq >> 7) & 0x01;
        grid[(8 * size) + 7] += (seq >> 8) & 0x01;
    }

    private void add_version_info(int size, int version) {
        /* Add version information */
        int i;

        long version_data = qr_annex_d[version - 7];
        for (i = 0; i < 6; i++) {
            grid[((size - 11) * size) + i] += (version_data >> (i * 3)) & 0x01;
            grid[((size - 10) * size) + i] += (version_data >> ((i * 3) + 1)) & 0x01;
            grid[((size - 9) * size) + i] += (version_data >> ((i * 3) + 2)) & 0x01;
            grid[(i * size) + (size - 11)] += (version_data >> (i * 3)) & 0x01;
            grid[(i * size) + (size - 10)] += (version_data >> ((i * 3) + 1)) & 0x01;
            grid[(i * size) + (size - 9)] += (version_data >> ((i * 3) + 2)) & 0x01;
        }
    }
}
