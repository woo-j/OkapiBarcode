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

import static uk.org.okapibarcode.util.Arrays.positionOf;

import java.nio.CharBuffer;
import java.nio.charset.Charset;

/**
 * <p>Implements QR Code bar code symbology According to ISO/IEC 18004:2015.
 *
 * <p>The maximum capacity of a (version 40) QR Code symbol is 7089 numeric digits,
 * 4296 alphanumeric characters or 2953 bytes of data. QR Code symbols can also
 * be used to encode GS1 data. QR Code symbols can encode characters in the
 * Latin-1 set and Kanji characters which are members of the Shift-JIS encoding
 * scheme.
 *
 * @author <a href="mailto:rstuart114@gmail.com">Robin Stuart</a>
 */
public class QrCode extends Symbol {

    public enum EccLevel {
        L, M, Q, H
    }

    private enum QrMode {
        NULL, KANJI, BINARY, ALPHANUM, NUMERIC
    }

    /* Table 5 - Encoding/Decoding table for Alphanumeric mode */
    private static final char[] RHODIUM = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E',
        'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
        'U', 'V', 'W', 'X', 'Y', 'Z', ' ', '$', '%', '*', '+', '-', '.', '/', ':'
    };

    private static final int[] QR_DATA_CODEWORDS_L = {
        19, 34, 55, 80, 108, 136, 156, 194, 232, 274, 324, 370, 428, 461, 523, 589, 647,
        721, 795, 861, 932, 1006, 1094, 1174, 1276, 1370, 1468, 1531, 1631,
        1735, 1843, 1955, 2071, 2191, 2306, 2434, 2566, 2702, 2812, 2956
    };

    private static final int[] QR_DATA_CODEWORDS_M = {
        16, 28, 44, 64, 86, 108, 124, 154, 182, 216, 254, 290, 334, 365, 415, 453, 507,
        563, 627, 669, 714, 782, 860, 914, 1000, 1062, 1128, 1193, 1267,
        1373, 1455, 1541, 1631, 1725, 1812, 1914, 1992, 2102, 2216, 2334
    };

    private static final int[] QR_DATA_CODEWORDS_Q = {
        13, 22, 34, 48, 62, 76, 88, 110, 132, 154, 180, 206, 244, 261, 295, 325, 367,
        397, 445, 485, 512, 568, 614, 664, 718, 754, 808, 871, 911,
        985, 1033, 1115, 1171, 1231, 1286, 1354, 1426, 1502, 1582, 1666
    };

    private static final int[] QR_DATA_CODEWORDS_H = {
        9, 16, 26, 36, 46, 60, 66, 86, 100, 122, 140, 158, 180, 197, 223, 253, 283,
        313, 341, 385, 406, 442, 464, 514, 538, 596, 628, 661, 701,
        745, 793, 845, 901, 961, 986, 1054, 1096, 1142, 1222, 1276
    };

    private static final int[] QR_BLOCKS_L = {
        1, 1, 1, 1, 1, 2, 2, 2, 2, 4, 4, 4, 4, 4, 6, 6, 6, 6, 7, 8, 8, 9, 9, 10, 12, 12,
        12, 13, 14, 15, 16, 17, 18, 19, 19, 20, 21, 22, 24, 25
    };

    private static final int[] QR_BLOCKS_M = {
        1, 1, 1, 2, 2, 4, 4, 4, 5, 5, 5, 8, 9, 9, 10, 10, 11, 13, 14, 16, 17, 17, 18, 20,
        21, 23, 25, 26, 28, 29, 31, 33, 35, 37, 38, 40, 43, 45, 47, 49
    };

    private static final int[] QR_BLOCKS_Q = {
        1, 1, 2, 2, 4, 4, 6, 6, 8, 8, 8, 10, 12, 16, 12, 17, 16, 18, 21, 20, 23, 23, 25,
        27, 29, 34, 34, 35, 38, 40, 43, 45, 48, 51, 53, 56, 59, 62, 65, 68
    };

    private static final int[] QR_BLOCKS_H = {
        1, 1, 2, 4, 4, 4, 5, 6, 8, 8, 11, 11, 16, 16, 18, 16, 19, 21, 25, 25, 25, 34, 30,
        32, 35, 37, 40, 42, 45, 48, 51, 54, 57, 60, 63, 66, 70, 74, 77, 81
    };

    private static final int[] QR_TOTAL_CODEWORDS = {
        26, 44, 70, 100, 134, 172, 196, 242, 292, 346, 404, 466, 532, 581, 655, 733, 815,
        901, 991, 1085, 1156, 1258, 1364, 1474, 1588, 1706, 1828, 1921, 2051,
        2185, 2323, 2465, 2611, 2761, 2876, 3034, 3196, 3362, 3532, 3706
    };

    private static final int[] QR_SIZES = {
        21, 25, 29, 33, 37, 41, 45, 49, 53, 57, 61, 65, 69, 73, 77, 81, 85, 89, 93, 97,
        101, 105, 109, 113, 117, 121, 125, 129, 133, 137, 141, 145, 149, 153, 157, 161, 165, 169, 173, 177
    };

    private static final int[] QR_ALIGN_LOOPSIZE = {
        0, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 7
    };

    private static final int[] QR_TABLE_E1 = {
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

    private static final int[] QR_ANNEX_C = {
        /* Format information bit sequences */
        0x5412, 0x5125, 0x5e7c, 0x5b4b, 0x45f9, 0x40ce, 0x4f97, 0x4aa0, 0x77c4, 0x72f3, 0x7daa, 0x789d,
        0x662f, 0x6318, 0x6c41, 0x6976, 0x1689, 0x13be, 0x1ce7, 0x19d0, 0x0762, 0x0255, 0x0d0c, 0x083b,
        0x355f, 0x3068, 0x3f31, 0x3a06, 0x24b4, 0x2183, 0x2eda, 0x2bed
    };

    private static final long[] QR_ANNEX_D = {
        /* Version information bit sequences */
        0x07c94, 0x085bc, 0x09a99, 0x0a4d3, 0x0bbf6, 0x0c762, 0x0d847, 0x0e60d, 0x0f928, 0x10b78,
        0x1145d, 0x12a17, 0x13532, 0x149a6, 0x15683, 0x168c9, 0x177ec, 0x18ec4, 0x191e1, 0x1afab,
        0x1b08e, 0x1cc1a, 0x1d33f, 0x1ed75, 0x1f250, 0x209d5, 0x216f0, 0x228ba, 0x2379f, 0x24b0b,
        0x2542e, 0x26a64, 0x27541, 0x28c69
    };

    private int preferredVersion;
    private EccLevel preferredEccLevel = EccLevel.L;

    /**
     * Sets the preferred symbol size / version. This value may be ignored if the data
     * string is too large to fit into the specified symbol. Input values correspond
     * to symbol sizes as shown in the following table:
     *
     * <table summary="Available QR Code sizes">
     * <tbody>
     * <tr><th>Input</th><th>Symbol Size</th><th>Input</th><th>Symbol Size</th></tr>
     * <tr><td>1    </td><td>21 x 21    </td><td>21   </td><td>101 x 101  </td></tr>
     * <tr><td>2    </td><td>25 x 25    </td><td>22   </td><td>105 x 105  </td></tr>
     * <tr><td>3    </td><td>29 x 29    </td><td>23   </td><td>109 x 109  </td></tr>
     * <tr><td>4    </td><td>33 x 33    </td><td>24   </td><td>113 x 113  </td></tr>
     * <tr><td>5    </td><td>37 x 37    </td><td>25   </td><td>117 x 117  </td></tr>
     * <tr><td>6    </td><td>41 x 41    </td><td>26   </td><td>121 x 121  </td></tr>
     * <tr><td>7    </td><td>45 x 45    </td><td>27   </td><td>125 x 125  </td></tr>
     * <tr><td>8    </td><td>49 x 49    </td><td>28   </td><td>129 x 129  </td></tr>
     * <tr><td>9    </td><td>53 x 53    </td><td>29   </td><td>133 x 133  </td></tr>
     * <tr><td>10   </td><td>57 x 57    </td><td>30   </td><td>137 x 137  </td></tr>
     * <tr><td>11   </td><td>61 x 61    </td><td>31   </td><td>141 x 141  </td></tr>
     * <tr><td>12   </td><td>65 x 65    </td><td>32   </td><td>145 x 145  </td></tr>
     * <tr><td>13   </td><td>69 x 69    </td><td>33   </td><td>149 x 149  </td></tr>
     * <tr><td>14   </td><td>73 x 73    </td><td>34   </td><td>153 x 153  </td></tr>
     * <tr><td>15   </td><td>77 x 77    </td><td>35   </td><td>157 x 157  </td></tr>
     * <tr><td>16   </td><td>81 x 81    </td><td>36   </td><td>161 x 161  </td></tr>
     * <tr><td>17   </td><td>85 x 85    </td><td>37   </td><td>165 x 165  </td></tr>
     * <tr><td>18   </td><td>89 x 89    </td><td>38   </td><td>169 x 169  </td></tr>
     * <tr><td>19   </td><td>93 x 93    </td><td>39   </td><td>173 x 173  </td></tr>
     * <tr><td>20   </td><td>97 x 97    </td><td>40   </td><td>177 x 177  </td></tr>
     * </tbody>
     * </table>
     *
     * @param version the preferred symbol version
     */
    public void setPreferredVersion(int version) {
        preferredVersion = version;
    }

    /**
     * Returns the preferred symbol version.
     *
     * @return the preferred symbol version
     */
    public int getPreferredVersion() {
        return preferredVersion;
    }

    /**
     * Sets the preferred amount of symbol space allocated to error correction. This value may
     * be ignored if there is room for a higher error correction level. Levels are predefined
     * according to the following table:
     *
     * <table summary="QR Code error correction levels">
     * <tbody>
     * <tr><th>ECC Level  </th><th>Error Correction Capacity</th><th>Recovery Capacity</th></tr>
     * <tr><td>L (default)</td><td>Approx 20% of symbol     </td><td>Approx 7%        </td></tr>
     * <tr><td>M          </td><td>Approx 37% of symbol     </td><td>Approx 15%       </td></tr>
     * <tr><td>Q          </td><td>Approx 55% of symbol     </td><td>Approx 25%       </td></tr>
     * <tr><td>H          </td><td>Approx 65% of symbol     </td><td>Approx 30%       </td></tr>
     * </tbody>
     * </table>
     *
     * @param preferredEccLevel the preferred error correction level
     */
    public void setPreferredEccLevel(EccLevel preferredEccLevel) {
        this.preferredEccLevel = preferredEccLevel;
    }

    /**
     * Returns the preferred amount of symbol space allocated to error correction.
     *
     * @return the preferred amount of symbol space allocated to error correction
     */
    public EccLevel getPreferredEccLevel() {
        return this.preferredEccLevel;
    }

    @Override
    protected boolean gs1Supported() {
        return true;
    }

    @Override
    protected void encode() {

        int i, j;
        int est_binlen;
        EccLevel ecc_level;
        int max_cw;
        int targetCwCount, version, blocks;
        int size;
        int bitmask;
        boolean gs1 = (inputDataType == DataType.GS1);

        eciProcess(); // Get ECI mode

        if (eciMode == 20) {
            /* Shift-JIS encoding, use Kanji mode */
            Charset c = Charset.forName("Shift_JIS");
            inputData = new int[content.length()];
            for (i = 0; i < inputData.length; i++) {
                CharBuffer buffer = CharBuffer.wrap(content, i, i + 1);
                byte[] bytes = c.encode(buffer).array();
                int value = (bytes.length == 2 ? ((bytes[0] & 0xff) << 8) | (bytes[1] & 0xff) : bytes[0]);
                inputData[i] = value;
            }
        } else {
            /* inputData already initialized in eciProcess() */
        }

        QrMode[] inputMode = new QrMode[inputData.length];
        defineMode(inputMode, inputData);
        est_binlen = getBinaryLength(40, inputMode, inputData, gs1, eciMode);

        ecc_level = this.preferredEccLevel;
        switch (this.preferredEccLevel) {
            case L:
            default:
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
        }

        if (est_binlen > (8 * max_cw)) {
            throw new OkapiException("Input too long for selected error correction level");
        }

        // ZINT NOTE: this block is different from the corresponding block of code in Zint;
        // it is simplified, but the simplification required that the applyOptimisation method
        // be changed to be free of side effects (by putting the optimized mode array into a
        // new array instead of modifying the existing array)

        version = 40;
        for (i = 39; i >= 0; i--) {
            int[] dataCodewords;
            switch (ecc_level) {
                case L:
                default:
                    dataCodewords = QR_DATA_CODEWORDS_L;
                    break;
                case M:
                    dataCodewords = QR_DATA_CODEWORDS_M;
                    break;
                case Q:
                    dataCodewords = QR_DATA_CODEWORDS_Q;
                    break;
                case H:
                    dataCodewords = QR_DATA_CODEWORDS_H;
                    break;
            }
            int proposedVersion = i + 1;
            int proposedBinLen = getBinaryLength(proposedVersion, inputMode, inputData, gs1, eciMode);
            if ((8 * dataCodewords[i]) >= proposedBinLen) {
                version = proposedVersion;
                est_binlen = proposedBinLen;
            }
        }

        inputMode = applyOptimisation(version, inputMode);

        // ZINT NOTE: end of block of code that is different

// TODO: delete this
//
//        autosize = 40;
//        for (i = 39; i >= 0; i--) {
//            switch (ecc_level) {
//                case L:
//                    if ((8 * QR_DATA_CODEWORDS_L[i]) >= est_binlen) {
//                        autosize = i + 1;
//                    }
//                    break;
//                case M:
//                    if ((8 * QR_DATA_CODEWORDS_M[i]) >= est_binlen) {
//                        autosize = i + 1;
//                    }
//                    break;
//                case Q:
//                    if ((8 * QR_DATA_CODEWORDS_Q[i]) >= est_binlen) {
//                        autosize = i + 1;
//                    }
//                    break;
//                case H:
//                    if ((8 * QR_DATA_CODEWORDS_H[i]) >= est_binlen) {
//                        autosize = i + 1;
//                    }
//                    break;
//            }
//        }
//
//        // Now see if the optimized binary will fit in a smaller symbol.
//        canShrink = true;
//
//        do {
//            if (autosize == 1) {
//                est_binlen = getBinaryLength(autosize, inputMode, inputData, gs1, eciMode); // TODO: added
//                canShrink = false;
//            } else {
//                est_binlen = getBinaryLength(autosize - 1, inputMode, inputData, gs1, eciMode);
//
//                switch (ecc_level) {
//                    case L:
//                        if ((8 * QR_DATA_CODEWORDS_L[autosize - 2]) < est_binlen) {
//                            canShrink = false;
//                        }
//                        break;
//                    case M:
//                        if ((8 * QR_DATA_CODEWORDS_M[autosize - 2]) < est_binlen) {
//                            canShrink = false;
//                        }
//                        break;
//                    case Q:
//                        if ((8 * QR_DATA_CODEWORDS_Q[autosize - 2]) < est_binlen) {
//                            canShrink = false;
//                        }
//                        break;
//                    case H:
//                        if ((8 * QR_DATA_CODEWORDS_H[autosize - 2]) < est_binlen) {
//                            canShrink = false;
//                        }
//                        break;
//                }
//
//                if (canShrink) {
//                    // Optimization worked - data will fit in a smaller symbol
//                    autosize--;
//                } else {
//                    // Data did not fit in the smaller symbol, revert to original size
//                    est_binlen = getBinaryLength(autosize, inputMode, inputData, gs1, eciMode);
//                }
//            }
//        } while (canShrink);
//
//        version = autosize;

        if ((preferredVersion >= 1) && (preferredVersion <= 40)) {
            /* If the user has selected a larger symbol than the smallest available,
             then use the size the user has selected, and re-optimize for this
             symbol size.
             */
            if (preferredVersion > version) {
                version = preferredVersion;
                est_binlen = getBinaryLength(preferredVersion, inputMode, inputData, gs1, eciMode);
                inputMode = applyOptimisation(version, inputMode);
            }
            if (preferredVersion < version) {
                throw new OkapiException("Input too long for selected symbol size");
            }
        }

        /* Ensure maximum error correction capacity */
        if (est_binlen <= (QR_DATA_CODEWORDS_M[version - 1] * 8)) {
            ecc_level = EccLevel.M;
        }
        if (est_binlen <= (QR_DATA_CODEWORDS_Q[version - 1] * 8)) {
            ecc_level = EccLevel.Q;
        }
        if (est_binlen <= (QR_DATA_CODEWORDS_H[version - 1] * 8)) {
            ecc_level = EccLevel.H;
        }

        targetCwCount = QR_DATA_CODEWORDS_L[version - 1];
        blocks = QR_BLOCKS_L[version - 1];
        switch (ecc_level) {
            case M:
                targetCwCount = QR_DATA_CODEWORDS_M[version - 1];
                blocks = QR_BLOCKS_M[version - 1];
                break;
            case Q:
                targetCwCount = QR_DATA_CODEWORDS_Q[version - 1];
                blocks = QR_BLOCKS_Q[version - 1];
                break;
            case H:
                targetCwCount = QR_DATA_CODEWORDS_H[version - 1];
                blocks = QR_BLOCKS_H[version - 1];
                break;
        }

        int[] datastream = new int[targetCwCount + 1];
        int[] fullstream = new int[QR_TOTAL_CODEWORDS[version - 1] + 1];

        qrBinary(datastream, version, targetCwCount, inputMode, inputData, gs1, eciMode, est_binlen);
        addEcc(fullstream, datastream, version, targetCwCount, blocks);

        size = QR_SIZES[version - 1];

        byte[] grid = new byte[size * size];

        encodeInfo += "Version: " + version + "\n";
        encodeInfo += "ECC Level: " + ecc_level.name() + "\n";

        setupGrid(grid, size, version);
        populateGrid(grid, size, fullstream, QR_TOTAL_CODEWORDS[version - 1]);

        if (version >= 7) {
            addVersionInfo(grid, size, version);
        }

        bitmask = applyBitmask(grid, size, ecc_level);
        encodeInfo += "Mask Pattern: " + Integer.toBinaryString(bitmask) + "\n";
        addFormatInfo(grid, size, ecc_level, bitmask);

        readable = "";
        pattern = new String[size];
        row_count = size;
        row_height = new int[size];
        for (i = 0; i < size; i++) {
            StringBuilder bin = new StringBuilder(size);
            for (j = 0; j < size; j++) {
                if ((grid[(i * size) + j] & 0x01) != 0) {
                    bin.append('1');
                } else {
                    bin.append('0');
                }
            }
            pattern[i] = bin2pat(bin.toString());
            row_height[i] = 1;
        }
    }

    /** Place Kanji / Binary / Alphanumeric / Numeric values in inputMode. */
    private static void defineMode(QrMode[] inputMode, int[] inputData) {

        int mlen;

        for (int i = 0; i < inputData.length; i++) {
            if (inputData[i] > 0xff) {
                inputMode[i] = QrMode.KANJI;
            } else {
                inputMode[i] = QrMode.BINARY;
                if (isAlpha(inputData[i])) {
                    inputMode[i] = QrMode.ALPHANUM;
                }
                if (inputData[i] == FNC1) {
                    inputMode[i] = QrMode.ALPHANUM;
                }
                if (isNumeric(inputData[i])) {
                    inputMode[i] = QrMode.NUMERIC;
                }
            }
        }

        // TODO: uncomment
//        /* If less than 6 numeric digits together then don't use numeric mode */
//        for (int i = 0; i < inputMode.length; i++) {
//            if (inputMode[i] == QrMode.NUMERIC) {
//                if (((i != 0) && (inputMode[i - 1] != QrMode.NUMERIC)) || (i == 0)) {
//                    mlen = 0;
//                    while (((mlen + i) < inputMode.length) && (inputMode[mlen + i] == QrMode.NUMERIC)) {
//                        mlen++;
//                    };
//                    if (mlen < 6) {
//                        for (int j = 0; j < mlen; j++) {
//                            inputMode[i + j] = QrMode.ALPHANUM;
//                        }
//                    }
//                }
//            }
//        }
//
//        /* If less than 4 alphanumeric characters together then don't use alphanumeric mode */
//        for (int i = 0; i < inputMode.length; i++) {
//            if (inputMode[i] == QrMode.ALPHANUM) {
//                if (((i != 0) && (inputMode[i - 1] != QrMode.ALPHANUM)) || (i == 0)) {
//                    mlen = 0;
//                    while (((mlen + i) < inputMode.length) && (inputMode[mlen + i] == QrMode.ALPHANUM)) {
//                        mlen++;
//                    };
//                    if (mlen < 4) {
//                        for (int j = 0; j < mlen; j++) {
//                            inputMode[i + j] = QrMode.BINARY;
//                        }
//                    }
//                }
//            }
//        }
    }

    /** Calculate the actual bit length of the proposed binary string. */
    private static int getBinaryLength(int version, QrMode[] inputModeUnoptimized, int[] inputData, boolean gs1, int eciMode) {

        int i, j;
        QrMode currentMode;
        int inputLength = inputModeUnoptimized.length;
        int count = 0;
        int alphaLength;
        int percent = 0;

        // ZINT NOTE: in Zint, this call modifies the input mode array directly; here, we leave
        // the original array alone so that subsequent binary length checks don't irrevocably
        // optimize the mode array for the wrong QR Code version
        QrMode[] inputMode = applyOptimisation(version, inputModeUnoptimized);

        currentMode = QrMode.NULL;

        if (gs1) {
            count += 4;
        }

        if (eciMode != 3) {
            count += 12;
        }

        for (i = 0; i < inputLength; i++) {
            if (inputMode[i] != currentMode) {
                count += 4;
                switch (inputMode[i]) {
                    case KANJI:
                        count += tribus(version, 8, 10, 12);
                        count += (blockLength(i, inputMode) * 13);
                        break;
                    case BINARY:
                        count += tribus(version, 8, 16, 16);
                        for (j = i; j < (i + blockLength(i, inputMode)); j++) {
                            if (inputData[j] > 0xff) {
                                count += 16;
                            } else {
                                count += 8;
                            }
                        }
                        break;
                    case ALPHANUM:
                        count += tribus(version, 9, 11, 13);
                        alphaLength = blockLength(i, inputMode);
                        // In alphanumeric mode % becomes %%
                        if (gs1) {
                            for (j = i; j < (i + alphaLength); j++) { // TODO: need to do this only if in GS1 mode? or is the other code wrong? https://sourceforge.net/p/zint/tickets/104/#227b
                                if (inputData[j] == '%') {
                                    percent++;
                                }
                            }
                        }
                        alphaLength += percent;
                        switch (alphaLength % 2) {
                            case 0:
                                count += (alphaLength / 2) * 11;
                                break;
                            case 1:
                                count += ((alphaLength - 1) / 2) * 11;
                                count += 6;
                                break;
                        }
                        break;
                    case NUMERIC:
                        count += tribus(version, 10, 12, 14);
                        switch (blockLength(i, inputMode) % 3) {
                            case 0:
                                count += (blockLength(i, inputMode) / 3) * 10;
                                break;
                            case 1:
                                count += ((blockLength(i, inputMode) - 1) / 3) * 10;
                                count += 4;
                                break;
                            case 2:
                                count += ((blockLength(i, inputMode) - 2) / 3) * 10;
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

    /**
     * Implements a custom optimization algorithm, because implementation of the algorithm
     * shown in Annex J.2 created LONGER binary sequences.
     */
    private static QrMode[] applyOptimisation(int version, QrMode[] inputMode) {

        int inputLength = inputMode.length;
        int blockCount = 0;
        int i, j;
        QrMode currentMode = QrMode.NULL;

        for (i = 0; i < inputLength; i++) {
            if (inputMode[i] != currentMode) {
                currentMode = inputMode[i];
                blockCount++;
            }
        }

        int[] blockLength = new int[blockCount];
        QrMode[] blockMode = new QrMode[blockCount];

        j = -1;
        currentMode = QrMode.NULL;
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
                if (blockMode[i] == QrMode.BINARY) {
                    switch (blockMode[i + 1]) {
                        case KANJI:
                            if (blockLength[i + 1] < tribus(version, 4, 5, 6)) {
                                blockMode[i + 1] = QrMode.BINARY;
                            }
                            break;
                        case ALPHANUM:
                            if (blockLength[i + 1] < tribus(version, 7, 8, 9)) {
                                blockMode[i + 1] = QrMode.BINARY;
                            }
                            break;
                        case NUMERIC:
                            if (blockLength[i + 1] < tribus(version, 3, 4, 5)) {
                                blockMode[i + 1] = QrMode.BINARY;
                            }
                            break;
                    }
                }

                if ((blockMode[i] == QrMode.ALPHANUM)
                        && (blockMode[i + 1] == QrMode.NUMERIC)) {
                    if (blockLength[i + 1] < tribus(version, 6, 8, 10)) {
                        blockMode[i + 1] = QrMode.ALPHANUM;
                    }
                }
            }

            // Search backward
            for (i = blockCount - 1; i > 0; i--) {
                if (blockMode[i] == QrMode.BINARY) {
                    switch (blockMode[i - 1]) {
                        case KANJI:
                            if (blockLength[i - 1] < tribus(version, 4, 5, 6)) {
                                blockMode[i - 1] = QrMode.BINARY;
                            }
                            break;
                        case ALPHANUM:
                            if (blockLength[i - 1] < tribus(version, 7, 8, 9)) {
                                blockMode[i - 1] = QrMode.BINARY;
                            }
                            break;
                        case NUMERIC:
                            if (blockLength[i - 1] < tribus(version, 3, 4, 5)) {
                                blockMode[i - 1] = QrMode.BINARY;
                            }
                            break;
                    }
                }

                if ((blockMode[i] == QrMode.ALPHANUM)
                        && (blockMode[i - 1] == QrMode.NUMERIC)) {
                    if (blockLength[i - 1] < tribus(version, 6, 8, 10)) {
                        blockMode[i - 1] = QrMode.ALPHANUM;
                    }
                }
            }
        }

        // ZINT NOTE: this method is different from the original Zint code in that it creates a
        // new array to hold the optimized values and returns it, rather than modifying the
        // original array; this allows this method to be called as many times as we want without
        // worrying about side effects

        QrMode[] optimized = new QrMode[inputMode.length];

        j = 0;
        for (int block = 0; block < blockCount; block++) {
            currentMode = blockMode[block];
            for (i = 0; i < blockLength[block]; i++) {
                optimized[j] = currentMode;
                j++;
            }
        }

        return optimized;
    }

    /** Find the length of the block starting from 'start'. */
    private static int blockLength(int start, QrMode[] inputMode) {

        QrMode mode = inputMode[start];
        int count = 0;
        int i = start;

        do {
            count++;
        } while (((i + count) < inputMode.length) && (inputMode[i + count] == mode));

        return count;
    }

    /** Choose from three numbers based on version. */
    private static int tribus(int version, int a, int b, int c) {
        if (version < 10) {
            return a;
        } else if (version >= 10 && version <= 26) {
            return b;
        } else {
            return c;
        }
    }

    /** Returns true if input is in the Alphanumeric set (see Table J.1) */
    private static boolean isAlpha(int c) {
        return (c >= '0' && c <= '9') ||
               (c >= 'A' && c <= 'Z') ||
               (c == ' ') ||
               (c == '$') ||
               (c == '%') ||
               (c == '*') ||
               (c == '+') ||
               (c == '-') ||
               (c == '.') ||
               (c == '/') ||
               (c == ':');
    }

    /** Returns true if input is in the Numeric set (see Table J.1) */
    private static boolean isNumeric(int c) {
        return (c >= '0' && c <= '9');
    }

    /** Converts input data to a binary stream and adds padding. */
    private void qrBinary(int[] datastream, int version, int target_binlen, QrMode[] inputMode, int[] inputData, boolean gs1, int eciMode, int est_binlen) {

        // TODO: make encodeInfo a StringBuilder, make this method static?

        int position = 0;
        int short_data_block_length, i;
        int padbits;
        int current_binlen, current_bytes;
        int toggle;
        QrMode data_block;

        StringBuilder binary = new StringBuilder(est_binlen + 12);

        if (gs1) {
            binary.append("0101"); /* FNC1 */
        }

        if (eciMode != 3) {
            binary.append("0111"); /* ECI (Table 4) */
            if (eciMode <= 127) {
                binaryAppend(eciMode, 8, binary); /* 000000 to 000127 */
            } else if (eciMode <= 16383) {
                binaryAppend(0x8000 + eciMode, 16, binary); /* 000000 to 016383 */
            } else {
                binaryAppend(0xC00000 + eciMode, 24, binary); /* 000000 to 999999 */
            }
        }

        encodeInfo += "Encoding: ";

        do {
            data_block = inputMode[position];
            short_data_block_length = 0;
            do {
                short_data_block_length++;
            } while (((short_data_block_length + position) < inputMode.length)
                    && (inputMode[position + short_data_block_length] == data_block));

            switch (data_block) {

                case KANJI:
                    /* Kanji mode */
                    /* Mode indicator */
                    binary.append("1000");

                    /* Character count indicator */
                    binaryAppend(short_data_block_length, tribus(version, 8, 10, 12), binary);

                    encodeInfo += "KNJI ";

                    /* Character representation */
                    for (i = 0; i < short_data_block_length; i++) {
                        int jis = inputData[position + i];
                        if (jis >= 0x8140 && jis <= 0x9ffc) {
                            jis -= 0x8140;
                        } else if (jis >= 0xe040 && jis <= 0xebbf) {
                            jis -= 0xc140;
                        }
                        int prod = ((jis >> 8) * 0xc0) + (jis & 0xff);
                        binaryAppend(prod, 13, binary);
                        encodeInfo += Integer.toString(prod) + " ";
                    }

                    break;

                case BINARY:
                    /* Byte mode */
                    /* Mode indicator */
                    binary.append("0100");

                    /* Character count indicator */
                    binaryAppend(short_data_block_length, tribus(version, 8, 16, 16), binary);

                    encodeInfo += "BYTE ";

                    /* Character representation */
                    for (i = 0; i < short_data_block_length; i++) {
                        int b = inputData[position + i];
                        if (b == FNC1) {
                            b = 0x1d; /* FNC1 */
                        }
                        binaryAppend(b, 8, binary);
                        encodeInfo += Integer.toString(b) + " ";
                    }

                    break;

                case ALPHANUM:
                    /* Alphanumeric mode */
                    /* Mode indicator */
                    binary.append("0010");

                    /* If in GS1 mode, expand FNC1 -> '%' and expand '%' -> '%%' in a new array */
                    int percentCount = 0;
                    if (gs1) {
                        for (i = 0; i < short_data_block_length; i++) {
                            if (inputData[position + i] == '%') {
                                percentCount++;
                            }
                        }
                    }
                    int[] inputExpanded = new int[short_data_block_length + percentCount];
                    percentCount = 0;
                    for (i = 0; i < short_data_block_length; i++) {
                        int c = inputData[position + i];
                        if (c == FNC1) {
                            inputExpanded[i + percentCount] = '%'; /* FNC1 */
                        } else {
                            inputExpanded[i + percentCount] = c;
                            if (gs1 && c == '%') {
                                percentCount++;
                                inputExpanded[i + percentCount] = c;
                            }
                        }
                    }

                    /* Character count indicator */
                    binaryAppend(inputExpanded.length, tribus(version, 9, 11, 13), binary);

                    encodeInfo += "ALPH ";

                    /* Character representation */
                    for (i = 0; i + 1 < inputExpanded.length; i += 2) {
                        int first = positionOf((char) inputExpanded[i], RHODIUM);
                        int second = positionOf((char) inputExpanded[i + 1], RHODIUM);
                        int prod = (first * 45) + second;
                        int count = 2;
                        binaryAppend(prod, 1 + (5 * count), binary);
                        encodeInfo += Integer.toString(prod) + " ";
                    }
                    if (inputExpanded.length % 2 != 0) {
                        int first = positionOf((char) inputExpanded[inputExpanded.length - 1], RHODIUM);
                        int prod = first;
                        int count = 1;
                        binaryAppend(prod, 1 + (5 * count), binary);
                        encodeInfo += Integer.toString(prod) + " ";
                    }

                    break;

                case NUMERIC:
                    /* Numeric mode */
                    /* Mode indicator */
                    binary.append("0001");

                    /* Character count indicator */
                    binaryAppend(short_data_block_length, tribus(version, 10, 12, 14), binary);

                    encodeInfo += "NUMB ";

                    /* Character representation */
                    i = 0;
                    while (i < short_data_block_length) {

                        int first = Character.getNumericValue(inputData[position + i]);
                        int count = 1;
                        int prod = first;

                        if (i + 1 < short_data_block_length) {
                            int second = Character.getNumericValue(inputData[position + i + 1]);
                            count = 2;
                            prod = (prod * 10) + second;

                            if (i + 2 < short_data_block_length) {
                                int third = Character.getNumericValue(inputData[position + i + 2]);
                                count = 3;
                                prod = (prod * 10) + third;
                            }
                        }

                        binaryAppend(prod, 1 + (3 * count), binary);

                        encodeInfo += Integer.toString(prod) + " ";

                        i += count;
                    };

                    break;
            }

            position += short_data_block_length;

        } while (position < inputMode.length);

        encodeInfo += "\n";

        /* Terminator */
        binary.append("0000");

        current_binlen = binary.length();
        padbits = 8 - (current_binlen % 8);
        if (padbits == 8) {
            padbits = 0;
        }
        current_bytes = (current_binlen + padbits) / 8;

        /* Padding bits */
        for (i = 0; i < padbits; i++) {
            binary.append('0');
        }

        /* Put data into 8-bit codewords */
        for (i = 0; i < current_bytes; i++) {
            datastream[i] = 0x00;
            for (int p = 0; p < 8; p++) {
                if (binary.charAt((i * 8) + p) == '1') {
                    datastream[i] += (0x80 >> p);
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
    }

    private static void binaryAppend(int value, int length, StringBuilder binary) {
        int start = 0x01 << (length - 1);
        for (int i = 0; i < length; i++) {
            if ((value & (start >> i)) != 0) {
                binary.append('1');
            } else {
                binary.append('0');
            }
        }
    }

    /** Splits data into blocks, adds error correction and then interleaves the blocks and error correction data. */
    private static void addEcc(int[] fullstream, int[] datastream, int version, int data_cw, int blocks) {

        int ecc_cw = QR_TOTAL_CODEWORDS[version - 1] - data_cw;
        int short_data_block_length = data_cw / blocks;
        int qty_long_blocks = data_cw % blocks;
        int qty_short_blocks = blocks - qty_long_blocks;
        int ecc_block_length = ecc_cw / blocks;
        int i, j, length_this_block, posn;

        int[] data_block = new int[short_data_block_length + 2];
        int[] ecc_block = new int[ecc_block_length + 2];
        int[] interleaved_data = new int[data_cw + 2];
        int[] interleaved_ecc = new int[ecc_cw + 2];

        posn = 0;

        for (i = 0; i < blocks; i++) {
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

            ReedSolomon rs = new ReedSolomon();
            rs.init_gf(0x11d);
            rs.init_code(ecc_block_length, 0);
            rs.encode(length_this_block, data_block);

            for (j = 0; j < ecc_block_length; j++) {
                ecc_block[j] = rs.getResult(j);
            }

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
    }

    private static void setupGrid(byte[] grid, int size, int version) {

        int i;
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
        placeFinder(grid, size, 0, 0);
        placeFinder(grid, size, 0, size - 7);
        placeFinder(grid, size, size - 7, 0);

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
            int loopsize = QR_ALIGN_LOOPSIZE[version - 1];
            for (int x = 0; x < loopsize; x++) {
                for (int y = 0; y < loopsize; y++) {
                    int xcoord = QR_TABLE_E1[((version - 2) * 7) + x];
                    int ycoord = QR_TABLE_E1[((version - 2) * 7) + y];
                    if ((grid[(ycoord * size) + xcoord] & 0x10) == 0) {
                        placeAlign(grid, size, xcoord, ycoord);
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
        grid[(8 * size) + 8] += 0x20; // TODO: suspicious! in Zint, this is 20... https://sourceforge.net/p/zint/tickets/108/
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

    private static void placeFinder(byte[] grid, int size, int x, int y) {

        int[] finder = {
            1, 1, 1, 1, 1, 1, 1,
            1, 0, 0, 0, 0, 0, 1,
            1, 0, 1, 1, 1, 0, 1,
            1, 0, 1, 1, 1, 0, 1,
            1, 0, 1, 1, 1, 0, 1,
            1, 0, 0, 0, 0, 0, 1,
            1, 1, 1, 1, 1, 1, 1
        };

        for (int xp = 0; xp < 7; xp++) {
            for (int yp = 0; yp < 7; yp++) {
                if (finder[xp + (7 * yp)] == 1) {
                    grid[((yp + y) * size) + (xp + x)] = 0x11;
                } else {
                    grid[((yp + y) * size) + (xp + x)] = 0x10;
                }
            }
        }
    }

    private static void placeAlign(byte[] grid, int size, int x, int y) {

        int[] alignment = {
            1, 1, 1, 1, 1,
            1, 0, 0, 0, 1,
            1, 0, 1, 0, 1,
            1, 0, 0, 0, 1,
            1, 1, 1, 1, 1
        };

        x -= 2;
        y -= 2; /* Input values represent centre of pattern */

        for (int xp = 0; xp < 5; xp++) {
            for (int yp = 0; yp < 5; yp++) {
                if (alignment[xp + (5 * yp)] == 1) {
                    grid[((yp + y) * size) + (xp + x)] = 0x11;
                } else {
                    grid[((yp + y) * size) + (xp + x)] = 0x10;
                }
            }
        }
    }

    // TODO: third parameter name is different from Zint, need to make sure this is correct; https://sourceforge.net/p/zint/tickets/109/
    private static void populateGrid(byte[] grid, int size, int[] fullstream, int cw) {

        boolean goingUp = true;
        int row = 0; /* right hand side */

        int i, n, y;

        n = cw * 8;
        y = size - 1;
        i = 0;
        do {
            int x = (size - 2) - (row * 2);
            if (x < 6) {
                x--; /* skip over vertical timing pattern */
            }

            if ((grid[(y * size) + (x + 1)] & 0xf0) == 0) {
                if (cwbit(fullstream, i)) {
                    grid[(y * size) + (x + 1)] = 0x01;
                } else {
                    grid[(y * size) + (x + 1)] = 0x00;
                }
                i++;
            }

            if (i < n) {
                if ((grid[(y * size) + x] & 0xf0) == 0) {
                    if (cwbit(fullstream, i)) {
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

    private static boolean cwbit(int[] fullstream, int i) {
        return ((fullstream[i / 8] & (0x80 >> (i % 8))) != 0);
    }

    private static int applyBitmask(byte[] grid, int size, EccLevel ecc_level) {

        int x, y;
        char p;
        int pattern;
        int best_val, best_pattern;
        int[] penalty = new int[8];
        byte[] mask = new byte[size * size];
        byte[] eval = new byte[size * size];

        /* Perform data masking */
        for (x = 0; x < size; x++) {
            for (y = 0; y < size; y++) {
                mask[(y * size) + x] = 0x00;
                // all eight bit mask variants are encoded in the 8 bits of the bytes that make up the mask array
                if ((grid[(y * size) + x] & 0xf0) == 0) { // exclude areas not to be masked
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

        /* Apply data masks to grid, result in eval */
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
        for (pattern = 0; pattern < 8; pattern++) {
            addFormatInfoEval(eval, size, ecc_level, pattern);
            penalty[pattern] = evaluate(eval, size, pattern);
        }

        best_pattern = 0;
        best_val = penalty[0];
        for (pattern = 1; pattern < 8; pattern++) {
            if (penalty[pattern] < best_val) {
                best_pattern = pattern;
                best_val = penalty[pattern];
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

    /** Adds format information to eval. */
    private static void addFormatInfoEval(byte[] eval, int size, EccLevel ecc_level, int pattern) {

        int format = pattern;
        int seq;
        int i;

        switch(ecc_level) {
            case L: format += 0x08; break;
            case Q: format += 0x18; break;
            case H: format += 0x10; break;
        }

        seq = QR_ANNEX_C[format];

        for (i = 0; i < 6; i++) {
            eval[(i * size) + 8] = (byte) ((((seq >> i) & 0x01) != 0) ? (0x01 >> pattern) : 0x00);
        }

        for (i = 0; i < 8; i++) {
            eval[(8 * size) + (size - i - 1)] = (byte) ((((seq >> i) & 0x01) != 0) ? (0x01 >> pattern) : 0x00);
        }

        for (i = 0; i < 6; i++) {
            eval[(8 * size) + (5 - i)] = (byte) ((((seq >> (i + 9)) & 0x01) != 0) ? (0x01 >> pattern) : 0x00);
        }

        for (i = 0; i < 7; i++) {
            eval[(((size - 7) + i) * size) + 8] = (byte) ((((seq >> (i + 8)) & 0x01) != 0) ? (0x01 >> pattern) : 0x00);
        }

        eval[(7 * size) + 8] = (byte) ((((seq >> 6) & 0x01) != 0) ? (0x01 >> pattern) : 0x00);
        eval[(8 * size) + 8] = (byte) ((((seq >> 7) & 0x01) != 0) ? (0x01 >> pattern) : 0x00);
        eval[(8 * size) + 7] = (byte) ((((seq >> 8) & 0x01) != 0) ? (0x01 >> pattern) : 0x00);
    }

    private static int evaluate(byte[] eval, int size, int pattern) {

        int x, y, block, weight;
        int result = 0;
        int state;
        int p;
        int dark_mods;
        int percentage, k;
        int a, b, afterCount, beforeCount;
        byte[] local = new byte[size * size];

        // all eight bit mask variants have been encoded in the 8 bits of the bytes
        // that make up the grid array; select them for evaluation according to the
        // desired pattern
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
                if (((local[(y * size) + x] == local[((y + 1) * size) + x]) &&
                        (local[(y * size) + x] == local[(y * size) + (x + 1)])) &&
                        (local[(y * size) + x] == local[((y + 1) * size) + (x + 1)])) {
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
                        // Pattern is preceded or followed by light area 4 modules wide
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
                        // Pattern is preceded or followed by light area 4 modules wide
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

    /* Adds format information to grid. */
    private static void addFormatInfo(byte[] grid, int size, EccLevel ecc_level, int pattern) {

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

        seq = QR_ANNEX_C[format];

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

    /** Adds version information. */
    private static void addVersionInfo(byte[] grid, int size, int version) {
        // TODO: Zint masks with 0x41 instead of 0x01; which is correct? https://sourceforge.net/p/zint/tickets/110/
        long version_data = QR_ANNEX_D[version - 7];
        for (int i = 0; i < 6; i++) {
            grid[((size - 11) * size) + i] += (version_data >> (i * 3)) & 0x01;
            grid[((size - 10) * size) + i] += (version_data >> ((i * 3) + 1)) & 0x01;
            grid[((size - 9) * size) + i] += (version_data >> ((i * 3) + 2)) & 0x01;
            grid[(i * size) + (size - 11)] += (version_data >> (i * 3)) & 0x01;
            grid[(i * size) + (size - 10)] += (version_data >> ((i * 3) + 1)) & 0x01;
            grid[(i * size) + (size - 9)] += (version_data >> ((i * 3) + 2)) & 0x01;
        }
    }
}
