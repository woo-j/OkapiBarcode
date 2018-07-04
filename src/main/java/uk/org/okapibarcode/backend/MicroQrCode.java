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

import java.io.UnsupportedEncodingException;

/**
 * Implements Micro QR Code
 * According to ISO/IEC 18004:2006
 * <br>
 * A miniature version of the QR Code symbol for short messages.
 * QR Code symbols can encode characters in the Latin-1 set and Kanji
 * characters which are members of the Shift-JIS encoding scheme.
 *
 * @author <a href="mailto:rstuart114@gmail.com">Robin Stuart</a>
 */
public class MicroQrCode extends Symbol {

    private enum qrMode {
        NULL, KANJI, BINARY, ALPHANUM, NUMERIC
    }
    public enum EccMode {
        L, M, Q, H
    }

    private qrMode[] inputMode;
    private String binary;
    private int[] binaryCount = new int[4];
    private int[] grid;
    private int[] eval;
    private int preferredVersion;

    /**
     * Sets the preferred symbol size. This value may be ignored if the
     * data string is too large to fit into the specified symbol. Input
     * values correspond to symbol sizes as shown in the following table.
     * <table summary="Range of Micro QR symbol sizes">
     * <tbody>
     * <tr>
     * <th>Input</th>
     * <th>Version</th>
     * <th>Symbol Size</th>
     * </tr>
     * <tr>
     * <td>1</td>
     * <td>M1</td>
     * <td>11 x 11</td>
     * </tr>
     * <tr>
     * <td>2</td>
     * <td>M2</td>
     * <td>13 x 13</td>
     * </tr>
     * <tr>
     * <td>3</td>
     * <td>M3</td>
     * <td>15 x 15</td>
     * </tr>
     * <tr>
     * <td>4</td>
     * <td>M4</td>
     * <td>17 x 17</td>
     * </tr>
     * </tbody>
     * </table>
     *
     * @param version Symbol size
     */
    public void setPreferredVersion(int version) {
        preferredVersion = version;
    }

    private EccMode preferredEccLevel = EccMode.L;

    /**
     * Set the amount of symbol space allocated to error correction.
     * Levels are predefined according to the following table:
     * <table summary="Micro QR Error correction levels">
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
     * @param eccMode Error correction level
     */
    public void setEccMode (EccMode eccMode) {
        preferredEccLevel = eccMode;
    }

    /* Table 5 - Encoding/Decoding table for Alphanumeric mode */
    private static final char[] RHODIUM = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D',
        'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R',
        'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', ' ', '$', '%', '*', '+', '-',
        '.', '/', ':'
    };

    private static final int[] QR_ANNEX_C1 = {
        /* Micro QR Code format information */
        0x4445, 0x4172, 0x4e2b, 0x4b1c, 0x55ae, 0x5099, 0x5fc0, 0x5af7, 0x6793,
        0x62a4, 0x6dfd, 0x68ca, 0x7678, 0x734f, 0x7c16, 0x7921, 0x06de, 0x03e9,
        0x0cb0, 0x0987, 0x1735, 0x1202, 0x1d5b, 0x186c, 0x2508, 0x203f, 0x2f66,
        0x2a51, 0x34e3, 0x31d4, 0x3e8d, 0x3bba
    };

    private static final int[] MICRO_QR_SIZES = {
        11, 13, 15, 17
    };

    @Override
    protected void encode() {
        int i, j, size;
        boolean[] version_valid = new boolean[4];
        int n_count, a_count;
        EccMode ecc_level;
        int version, autoversion;
        int bitmask;
        int format, format_full;
        String bin;
        boolean byteModeUsed;
        boolean alphanumModeUsed;
        boolean kanjiModeUsed;

        if (content.length() > 35) {
            throw new OkapiException("Input data too long");
        }

        inputCharCheck();

        for (i = 0; i < 4; i++) {
            version_valid[i] = true;
        }

        inputMode = new qrMode[40];
        selectEncodingMode();

        n_count = 0;
        a_count = 0;
        for (i = 0; i < content.length(); i++) {
            if ((content.charAt(i) >= '0') && (content.charAt(i) <= '9')) {
                n_count++;
            }
            if (isAlphanumeric(content.charAt(i))) {
                a_count++;
            }
        }

        if (a_count == content.length()) {
            /* All data can be encoded in Alphanumeric mode */
            for (i = 0; i < content.length(); i++) {
                inputMode[i] = qrMode.ALPHANUM;
            }
        }

        if (n_count == content.length()) {
            /* All data can be encoded in Numeric mode */
            for (i = 0; i < content.length(); i++) {
                inputMode[i] = qrMode.NUMERIC;
            }
        }

        byteModeUsed = false;
        alphanumModeUsed = false;
        kanjiModeUsed = false;

        for (i = 0; i < content.length(); i++) {
            if (inputMode[i] == qrMode.BINARY) {
                byteModeUsed = true;
            }

            if (inputMode[i] == qrMode.ALPHANUM) {
                alphanumModeUsed = true;
            }

            if (inputMode[i] == qrMode.KANJI) {
                kanjiModeUsed = true;
            }
        }

        getBinaryLength();

        /* Eliminate possivle versions depending on type of content */
        if (byteModeUsed) {
            version_valid[0] = false;
            version_valid[1] = false;
        }

        if (alphanumModeUsed) {
            version_valid[0] = false;
        }

        if (kanjiModeUsed) {
            version_valid[0] = false;
            version_valid[1] = false;
        }

        /* Eliminate possible versions depending on length of binary data */
        if (binaryCount[0] > 20) {
            version_valid[0] = false;
        }
        if (binaryCount[1] > 40) {
            version_valid[1] = false;
        }
        if (binaryCount[2] > 84) {
            version_valid[2] = false;
        }
        if (binaryCount[3] > 128) {
            throw new OkapiException("Input data too long");
        }

        /* Eliminate possible versions depending on error correction level specified */
        ecc_level = preferredEccLevel;

        if (ecc_level == EccMode.H) {
            throw new OkapiException("Error correction level H not available");
        }

        if (ecc_level == EccMode.Q) {
            version_valid[0] = false;
            version_valid[1] = false;
            version_valid[2] = false;
            if (binaryCount[3] > 80) {
                throw new OkapiException("Input data too long");
            }
        }

        if (ecc_level == EccMode.M) {
            version_valid[0] = false;
            if (binaryCount[1] > 32) {
                version_valid[1] = false;
            }
            if (binaryCount[2] > 68) {
                version_valid[2] = false;
            }
            if (binaryCount[3] > 112) {
                throw new OkapiException("Input data too long");
            }
        }

        autoversion = 3;
        if (version_valid[2]) {
            autoversion = 2;
        }
        if (version_valid[1]) {
            autoversion = 1;
        }
        if (version_valid[0]) {
            autoversion = 0;
        }

        version = autoversion;
        /* Get version from user */
        if((preferredVersion >= 1) && (preferredVersion <= 4)) {
            if(preferredVersion >= autoversion) {
                version = preferredVersion;
            }
        }

        /* If there is enough unused space then increase the error correction level */
        if (version == 3) {
            if (binaryCount[3] <= 112) {
                ecc_level = EccMode.M;
            }
            if (binaryCount[3] <= 80) {
                ecc_level = EccMode.Q;
            }
        }

        if (version == 2 && binaryCount[2] <= 68) {
            ecc_level = EccMode.M;
        }

        if (version == 1 && binaryCount[1] <= 32) {
            ecc_level = EccMode.M;
        }

        binary = "";
        generateBinary(version);
        if (binary.length() > 128) {
            throw new OkapiException("Input data too long");
        }

        switch (version) {
        case 0:
            generateM1Symbol();
            encodeInfo += "Version: M1\n";
            break;
        case 1:
            generateM2Symbol(ecc_level);
            encodeInfo += "Version: M2\n";
            encodeInfo += "ECC Level: " + levelToLetter(ecc_level) + "\n";
            break;
        case 2:
            generateM3Symbol(ecc_level);
            encodeInfo += "Version: M3\n";
            encodeInfo += "ECC Level: " + levelToLetter(ecc_level) + "\n";
            break;
        case 3:
            generateM4Symbol(ecc_level);
            encodeInfo += "Version: M4\n";
            encodeInfo += "ECC Level: " + levelToLetter(ecc_level) + "\n";
            break;
        }

        size = MICRO_QR_SIZES[version];

        grid = new int[size * size];

        for (i = 0; i < size; i++) {
            for (j = 0; j < size; j++) {
                grid[(i * size) + j] = 0;
            }
        }

        setupBitGrid(size);
        populateBitGrid(size);
        bitmask = applyBitmask(size);

        encodeInfo += "Mask Pattern: " + Integer.toBinaryString(bitmask) + "\n";

        /* Add format data */
        format = 0;
        switch (version) {
        case 1:
            switch (ecc_level) {
            case L:
                format = 1;
                break;
            case M:
                format = 2;
                break;
            }
            break;
        case 2:
            switch (ecc_level) {
            case L:
                format = 3;
                break;
            case M:
                format = 4;
                break;
            }
            break;
        case 3:
            switch (ecc_level) {
            case L:
                format = 5;
                break;
            case M:
                format = 6;
                break;
            case Q:
                format = 7;
                break;
            }
            break;
        }

        format_full = QR_ANNEX_C1[(format << 2) + bitmask];

        if ((format_full & 0x4000) != 0) {
            grid[(8 * size) + 1] += 0x01;
        }
        if ((format_full & 0x2000) != 0) {
            grid[(8 * size) + 2] += 0x01;
        }
        if ((format_full & 0x1000) != 0) {
            grid[(8 * size) + 3] += 0x01;
        }
        if ((format_full & 0x800) != 0) {
            grid[(8 * size) + 4] += 0x01;
        }
        if ((format_full & 0x400) != 0) {
            grid[(8 * size) + 5] += 0x01;
        }
        if ((format_full & 0x200) != 0) {
            grid[(8 * size) + 6] += 0x01;
        }
        if ((format_full & 0x100) != 0) {
            grid[(8 * size) + 7] += 0x01;
        }
        if ((format_full & 0x80) != 0) {
            grid[(8 * size) + 8] += 0x01;
        }
        if ((format_full & 0x40) != 0) {
            grid[(7 * size) + 8] += 0x01;
        }
        if ((format_full & 0x20) != 0) {
            grid[(6 * size) + 8] += 0x01;
        }
        if ((format_full & 0x10) != 0) {
            grid[(5 * size) + 8] += 0x01;
        }
        if ((format_full & 0x08) != 0) {
            grid[(4 * size) + 8] += 0x01;
        }
        if ((format_full & 0x04) != 0) {
            grid[(3 * size) + 8] += 0x01;
        }
        if ((format_full & 0x02) != 0) {
            grid[(2 * size) + 8] += 0x01;
        }
        if ((format_full & 0x01) != 0) {
            grid[(1 * size) + 8] += 0x01;
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
    }

    private void inputCharCheck() {
        int qmarkBefore, qmarkAfter;
        int i;
        byte[] temp;

        /* Check that input includes valid characters */

        if (content.matches("[\u0000-\u00FF]+")) {
            /* All characters in ISO 8859-1 */
            return;
        }

        /* Otherwise check for Shift-JIS characters */
        qmarkBefore = 0;
        for (i = 0; i < content.length(); i++) {
            if (content.charAt(i) == '?') {
                qmarkBefore++;
            }
        }

        try {
            temp = content.getBytes("SJIS");
        } catch (UnsupportedEncodingException e) {
            throw new OkapiException("Character encoding error");
        }

        qmarkAfter = 0;
        for (i = 0; i < temp.length; i++) {
            if (temp[i] == '?')  {
                qmarkAfter++;
            }
        }

        /* If these values are the same, conversion was successful */
        if (qmarkBefore != qmarkAfter) {
            throw new OkapiException("Invalid characters in input data");
        }
    }

    private char levelToLetter(EccMode ecc_mode) {
        switch (ecc_mode) {
            case L:
                return 'L';
            case M:
                return 'M';
            case Q:
                return 'Q';
            case H:
                return 'H';
            default:
                return ' ';
        }
    }

    private void selectEncodingMode() {
        int i, j;
        int mlen;
        int length = content.length();

        for (i = 0; i < length; i++) {
            if (content.charAt(i) > 0xff) {
                inputMode[i] = qrMode.KANJI;
            } else {
                inputMode[i] = qrMode.BINARY;
                if (isAlphanumeric(content.charAt(i))) {
                    inputMode[i] = qrMode.ALPHANUM;
                }
                if ((content.charAt(i) >= '0') && (content.charAt(i) <= '9')) {
                    inputMode[i] = qrMode.NUMERIC;
                }
            }
        }

        /* If less than 6 numeric digits together then don't use numeric mode */
        for (i = 0; i < length; i++) {
            if (inputMode[i] == qrMode.NUMERIC) {
                if (((i != 0) && (inputMode[i - 1] != qrMode.NUMERIC))
                        || (i == 0)) {
                    mlen = 0;
                    while (((mlen + i) < length)
                            && (inputMode[mlen + i] == qrMode.NUMERIC)) {
                        mlen++;
                    }
                    if (mlen < 6) {
                        for (j = 0; j < mlen; j++) {
                            inputMode[i + j] = qrMode.ALPHANUM;
                        }
                    }
                }
            }
        }

        /* If less than 4 alphanumeric characters together then don't use alphanumeric mode */
        for (i = 0; i < length; i++) {
            if (inputMode[i] == qrMode.ALPHANUM) {
                if (((i != 0) && (inputMode[i - 1] != qrMode.ALPHANUM))
                        || (i == 0)) {
                    mlen = 0;
                    while (((mlen + i) < length)
                            && (inputMode[mlen + i] == qrMode.ALPHANUM)) {
                        mlen++;
                    }
                    if (mlen < 6) {
                        for (j = 0; j < mlen; j++) {
                            inputMode[i + j] = qrMode.BINARY;
                        }
                    }
                }
            }
        }
    }

    private boolean isAlphanumeric(char cglyph) {
        /* Returns true if input glyph is in the Alphanumeric set */
        boolean retval = false;

        if ((cglyph >= '0') && (cglyph <= '9')) {
            retval = true;
        }
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

    private String toBinary(int data, int h) {
        String argument = "";
        for (;
        (h != 0); h >>= 1) {
            if ((data & h) != 0) {
                argument += "1";
            } else {
                argument += "0";
            }
        }
        return argument;
    }

    private void getBinaryLength() {
        int i;
        qrMode currentMode = qrMode.NULL;
        int blockLength;

        /* Always include a terminator */
        for (i = 0; i < 4; i++) {
            binaryCount[i] = 0;
        }

        for (i = 0; i < content.length(); i++) {
            if(currentMode != inputMode[i]) {

                blockLength = 0;
                do {
                    blockLength++;
                } while (((i + blockLength) < content.length())
                    && (inputMode[i + blockLength] == inputMode[i]));

                switch (inputMode[i]) {
                    case KANJI:
                        binaryCount[2] += 5 + (blockLength * 13);
                        binaryCount[3] += 7 + (blockLength * 13);

                        break;
                    case BINARY:
                        binaryCount[2] += 6 + (blockLength * 8);
                        binaryCount[3] += 8 + (blockLength * 8);
                        break;
                    case ALPHANUM:
                        int alphaLength;

                        if ((blockLength % 2) == 1) {
                            /* Odd length block */
                            alphaLength = ((blockLength - 1) / 2) * 11;
                            alphaLength += 6;
                        } else {
                            /* Even length block */
                            alphaLength = (blockLength / 2) * 11;
                        }

                        binaryCount[1] += 4 + alphaLength;
                        binaryCount[2] += 6 + alphaLength;
                        binaryCount[3] += 8 + alphaLength;
                        break;
                    case NUMERIC:
                        int numLength;

                        switch(blockLength % 3) {
                            case 1:
                                /* one digit left over */
                                numLength = ((blockLength - 1) / 3) * 10;
                                numLength += 4;
                                break;
                            case 2:
                                /* two digits left over */
                                numLength = ((blockLength - 2) / 3) * 10;
                                numLength += 7;
                                break;
                            default:
                                /* blockLength is a multiple of 3 */
                                numLength = (blockLength / 3) * 10;
                                break;
                        }

                        binaryCount[0] += 3 + numLength;
                        binaryCount[1] += 5 + numLength;
                        binaryCount[2] += 7 + numLength;
                        binaryCount[3] += 9 + numLength;
                        break;
                }
                currentMode = inputMode[i];
            }
        }

        /* Add terminator */
        if (binaryCount[1] < 37) {
            binaryCount[1] += 5;
        }

        if (binaryCount[2] < 81) {
            binaryCount[2] += 7;
        }

        if (binaryCount[3] < 125) {
            binaryCount[3] += 9;
        }
    }

    private void generateBinary(int version) {
        int position = 0;
        int blockLength, i;
        qrMode data_block;
        int msb, lsb, prod, jis;
        String oneChar;
        byte[] jisBytes;
        int count, first, second, third;

        encodeInfo += "Encoding: ";

        do {
            data_block = inputMode[position];
            blockLength = 0;
            do {
                blockLength++;
            } while (((blockLength + position) < content.length())
                    && (inputMode[position + blockLength] == data_block));

            switch (data_block) {
            case KANJI:
                /* Kanji mode */
                /* Mode indicator */
                switch (version) {
                case 2:
                    binary += "11";
                    break;
                case 3:
                    binary += "011";
                    break;
                }

                /* Character count indicator */
                binary += toBinary(blockLength, 1 << version); /* version = 2..3 */

                encodeInfo += "KANJ (" + Integer.toString(blockLength) + ") ";

                /* Character representation */
                for (i = 0; i < blockLength; i++) {
                    oneChar = "";
                    oneChar += content.charAt(position + i);

                    /* Convert Unicode input to Shift-JIS */
                    try {
                        jisBytes = oneChar.getBytes("SJIS");
                    } catch (UnsupportedEncodingException e) {
                        throw new OkapiException("Character encoding error");
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

                    binary += toBinary(prod, 0x1000);

                    encodeInfo += Integer.toString(prod) + " ";
                }

                break;
            case BINARY:
                /* Byte mode */
                /* Mode indicator */
                switch (version) {
                case 2:
                    binary += "10";
                    break;
                case 3:
                    binary += "010";
                    break;
                }

                /* Character count indicator */
                binary += toBinary(blockLength, 2 << version); /* version = 2..3 */

                encodeInfo += "BYTE (" + Integer.toString(blockLength) + ") ";

                /* Character representation */
                for (i = 0; i < blockLength; i++) {
                    int lbyte = content.charAt(position + i);

                    binary += toBinary(lbyte, 0x80);

                    encodeInfo += Integer.toString(lbyte) + " ";
                }

                break;
            case ALPHANUM:
                /* Alphanumeric mode */
                /* Mode indicator */
                switch (version) {
                case 1:
                    binary += "1";
                    break;
                case 2:
                    binary += "01";
                    break;
                case 3:
                    binary += "001";
                    break;
                }

                /* Character count indicator */
                binary += toBinary(blockLength, 2 << version); /* version = 1..3 */

                encodeInfo += "ALPH (" + Integer.toString(blockLength) + ") ";

                /* Character representation */
                i = 0;
                while (i < blockLength) {
                    first = positionOf(content.charAt(position + i), RHODIUM);
                    count = 1;
                    prod = first;

                    if ((i + 1) < blockLength) {
                        if (inputMode[position + i + 1] == qrMode.ALPHANUM) {
                            second = positionOf(content.charAt(position + i + 1), RHODIUM);
                            count = 2;
                            prod = (first * 45) + second;
                        }
                    }

                    binary += toBinary(prod, 1 << (5 * count)); /* count = 1..2 */

                    encodeInfo += Integer.toString(prod) + " ";

                    i += 2;
                }

                break;
            case NUMERIC:
                /* Numeric mode */
                /* Mode indicator */
                switch (version) {
                case 1:
                    binary += "0";
                    break;
                case 2:
                    binary += "00";
                    break;
                case 3:
                    binary += "000";
                    break;
                }

                /* Character count indicator */
                binary += toBinary(blockLength, 4 << version); /* version = 0..3 */

                encodeInfo += "NUMB (" + Integer.toString(blockLength) + ") ";

                /* Character representation */
                i = 0;
                while (i < blockLength) {
                    first = Character.getNumericValue(content.charAt(position + i));
                    count = 1;
                    prod = first;

                    if ((i + 1) < blockLength) {
                        if (inputMode[position + i + 1] == qrMode.NUMERIC) {
                            second = Character.getNumericValue(content.charAt(position + i + 1));
                            count = 2;
                            prod = (prod * 10) + second;
                        }
                    }

                    if ((i + 2) < blockLength) {
                        if (inputMode[position + i + 2] == qrMode.NUMERIC) {
                            third = Character.getNumericValue(content.charAt(position + i + 2));
                            count = 3;
                            prod = (prod * 10) + third;
                        }
                    }

                    binary += toBinary(prod, 1 << (3 * count)); /* count = 1..3 */

                    encodeInfo += Integer.toString(prod) + " ";

                    i += 3;
                }
                break;
            }

            position += blockLength;
        } while (position < content.length() - 1);

        /* Add terminator */
        switch(version) {
            case 0:
                binary += "000";
                break;
            case 1:
                if (binary.length() < 37) {
                    binary += "00000";
                }
                break;
            case 2:
                if (binary.length() < 81) {
                    binary += "0000000";
                }
                break;
            case 3:
                if (binary.length() < 125) {
                    binary += "000000000";
                }
                break;
        }

        encodeInfo += "\n";
    }

    private void generateM1Symbol() {
        int i, latch;
        int bits_total, bits_left, remainder;
        int data_codewords, ecc_codewords;
        int[] data_blocks = new int[4];
        int[] ecc_blocks = new int[3];
        ReedSolomon rs = new ReedSolomon();

        bits_total = 20;
        latch = 0;

        /* Manage last (4-bit) block */
        bits_left = bits_total - binary.length();
        if (bits_left <= 4) {
            for (i = 0; i < bits_left; i++) {
                binary += "0";
            }
            latch = 1;
        }

        if (latch == 0) {
            /* Complete current byte */
            remainder = 8 - (binary.length() % 8);
            if (remainder == 8) {
                remainder = 0;
            }
            for (i = 0; i < remainder; i++) {
                binary += "0";
            }

            /* Add padding */
            bits_left = bits_total - binary.length();
            if (bits_left > 4) {
                remainder = (bits_left - 4) / 8;
                for (i = 0; i < remainder; i++) {
                    if ((i & 1) != 0) {
                        binary += "00010001";
                    } else {
                        binary += "11101100";
                    }
                }
            }
            binary += "0000";
        }

        data_codewords = 3;
        ecc_codewords = 2;

        /* Copy data into codewords */
        for (i = 0; i < (data_codewords - 1); i++) {
            data_blocks[i] = 0;
            if (binary.charAt(i * 8) == '1') {
                data_blocks[i] += 0x80;
            }
            if (binary.charAt((i * 8) + 1) == '1') {
                data_blocks[i] += 0x40;
            }
            if (binary.charAt((i * 8) + 2) == '1') {
                data_blocks[i] += 0x20;
            }
            if (binary.charAt((i * 8) + 3) == '1') {
                data_blocks[i] += 0x10;
            }
            if (binary.charAt((i * 8) + 4) == '1') {
                data_blocks[i] += 0x08;
            }
            if (binary.charAt((i * 8) + 5) == '1') {
                data_blocks[i] += 0x04;
            }
            if (binary.charAt((i * 8) + 6) == '1') {
                data_blocks[i] += 0x02;
            }
            if (binary.charAt((i * 8) + 7) == '1') {
                data_blocks[i] += 0x01;
            }
        }
        data_blocks[2] = 0;
        if (binary.charAt(16) == '1') {
            data_blocks[2] += 0x08;
        }
        if (binary.charAt(17) == '1') {
            data_blocks[2] += 0x04;
        }
        if (binary.charAt(18) == '1') {
            data_blocks[2] += 0x02;
        }
        if (binary.charAt(19) == '1') {
            data_blocks[2] += 0x01;
        }

        encodeInfo += "Codewords: ";

        for (i = 0; i < data_codewords; i++) {
            encodeInfo += Integer.toString(data_blocks[i]) + " ";
        }
        encodeInfo += "\n";

        /* Calculate Reed-Solomon error codewords */
        rs.init_gf(0x11d);
        rs.init_code(ecc_codewords, 0);
        rs.encode(data_codewords, data_blocks);
        for (i = 0; i < ecc_codewords; i++) {
            ecc_blocks[i] = rs.getResult(i);
        }

        /* Add Reed-Solomon codewords to binary data */
        for (i = 0; i < ecc_codewords; i++) {
            binary += toBinary(ecc_blocks[ecc_codewords - i - 1], 0x80);
        }
    }

    private void generateM2Symbol(EccMode ecc_mode) {
        int i;
        int bits_total, bits_left, remainder;
        int data_codewords, ecc_codewords;
        int[] data_blocks = new int[6];
        int[] ecc_blocks = new int[7];
        ReedSolomon rs = new ReedSolomon();

        bits_total = 40; // ecc_mode == EccMode.L
        if (ecc_mode == EccMode.M) {
            bits_total = 32;
        }

        /* Complete current byte */
        remainder = 8 - (binary.length() % 8);
        if (remainder == 8) {
            remainder = 0;
        }
        for (i = 0; i < remainder; i++) {
            binary += "0";
        }

        /* Add padding */
        bits_left = bits_total - binary.length();
        remainder = bits_left / 8;
        for (i = 0; i < remainder; i++) {
            if ((i & 1) != 0) {
                binary += "00010001";
            } else {
                binary += "11101100";
            }
        }

        data_codewords = 5;
        ecc_codewords = 5; // ecc_mode == EccMode.L
        if (ecc_mode == EccMode.M) {
            data_codewords = 4;
            ecc_codewords = 6;
        }

        /* Copy data into codewords */
        for (i = 0; i < data_codewords; i++) {
            data_blocks[i] = 0;
            if (binary.charAt(i * 8) == '1') {
                data_blocks[i] += 0x80;
            }
            if (binary.charAt((i * 8) + 1) == '1') {
                data_blocks[i] += 0x40;
            }
            if (binary.charAt((i * 8) + 2) == '1') {
                data_blocks[i] += 0x20;
            }
            if (binary.charAt((i * 8) + 3) == '1') {
                data_blocks[i] += 0x10;
            }
            if (binary.charAt((i * 8) + 4) == '1') {
                data_blocks[i] += 0x08;
            }
            if (binary.charAt((i * 8) + 5) == '1') {
                data_blocks[i] += 0x04;
            }
            if (binary.charAt((i * 8) + 6) == '1') {
                data_blocks[i] += 0x02;
            }
            if (binary.charAt((i * 8) + 7) == '1') {
                data_blocks[i] += 0x01;
            }
        }

        encodeInfo += "Codewords: ";
        System.out.printf("\tCodewords: ");
        for (i = 0; i < data_codewords; i++) {
            encodeInfo += Integer.toString(data_blocks[i]) + " ";
        }
        encodeInfo += "\n";

        /* Calculate Reed-Solomon error codewords */
        rs.init_gf(0x11d);
        rs.init_code(ecc_codewords, 0);
        rs.encode(data_codewords, data_blocks);
        for (i = 0; i < ecc_codewords; i++) {
            ecc_blocks[i] = rs.getResult(i);
        }

        /* Add Reed-Solomon codewords to binary data */
        for (i = 0; i < ecc_codewords; i++) {
            binary += toBinary(ecc_blocks[ecc_codewords - i - 1], 0x80);
        }
    }

    private void generateM3Symbol(EccMode ecc_mode) {
        int i, latch;
        int bits_total, bits_left, remainder;
        int data_codewords, ecc_codewords;
        int[] data_blocks = new int[12];
        int[] ecc_blocks = new int[12];
        ReedSolomon rs = new ReedSolomon();

        latch = 0;

        bits_total = 84; // ecc_mode == EccMode.L
        if (ecc_mode == EccMode.M) {
            bits_total = 68;
        }

        /* Manage last (4-bit) block */
        bits_left = bits_total - binary.length();
        if (bits_left <= 4) {
            for (i = 0; i < bits_left; i++) {
                binary += "0";
            }
            latch = 1;
        }

        if (latch == 0) {
            /* Complete current byte */
            remainder = 8 - (binary.length() % 8);
            if (remainder == 8) {
                remainder = 0;
            }
            for (i = 0; i < remainder; i++) {
                binary += "0";
            }

            /* Add padding */
            bits_left = bits_total - binary.length();
            if (bits_left > 4) {
                remainder = (bits_left - 4) / 8;
                for (i = 0; i < remainder; i++) {
                    if ((i & 1) != 0) {
                        binary += "00010001";
                    } else {
                        binary += "11101100";
                    }
                }
            }
            binary += "0000";
        }

        data_codewords = 11;
        ecc_codewords = 6; // ecc_mode == EccMode.L
        if (ecc_mode == EccMode.M) {
            data_codewords = 9;
            ecc_codewords = 8;
        }

        /* Copy data into codewords */
        for (i = 0; i < (data_codewords - 1); i++) {
            data_blocks[i] = 0;
            if (binary.charAt(i * 8) == '1') {
                data_blocks[i] += 0x80;
            }
            if (binary.charAt((i * 8) + 1) == '1') {
                data_blocks[i] += 0x40;
            }
            if (binary.charAt((i * 8) + 2) == '1') {
                data_blocks[i] += 0x20;
            }
            if (binary.charAt((i * 8) + 3) == '1') {
                data_blocks[i] += 0x10;
            }
            if (binary.charAt((i * 8) + 4) == '1') {
                data_blocks[i] += 0x08;
            }
            if (binary.charAt((i * 8) + 5) == '1') {
                data_blocks[i] += 0x04;
            }
            if (binary.charAt((i * 8) + 6) == '1') {
                data_blocks[i] += 0x02;
            }
            if (binary.charAt((i * 8) + 7) == '1') {
                data_blocks[i] += 0x01;
            }
        }

        if (ecc_mode == EccMode.L) {
            data_blocks[10] = 0;
            if (binary.charAt(80) == '1') {
                data_blocks[10] += 0x08;
            }
            if (binary.charAt(81) == '1') {
                data_blocks[10] += 0x04;
            }
            if (binary.charAt(82) == '1') {
                data_blocks[10] += 0x02;
            }
            if (binary.charAt(83) == '1') {
                data_blocks[10] += 0x01;
            }
        }

        if (ecc_mode == EccMode.M) {
            data_blocks[8] = 0;
            if (binary.charAt(64) == '1') {
                data_blocks[8] += 0x08;
            }
            if (binary.charAt(65) == '1') {
                data_blocks[8] += 0x04;
            }
            if (binary.charAt(66) == '1') {
                data_blocks[8] += 0x02;
            }
            if (binary.charAt(67) == '1') {
                data_blocks[8] += 0x01;
            }
        }

        encodeInfo += "Codewords: ";
        System.out.printf("\tCodewords: ");
        for (i = 0; i < data_codewords; i++) {
            encodeInfo += Integer.toString(data_blocks[i]) + " ";
        }
        encodeInfo += "\n";

        /* Calculate Reed-Solomon error codewords */
        rs.init_gf(0x11d);
        rs.init_code(ecc_codewords, 0);
        rs.encode(data_codewords, data_blocks);
        for (i = 0; i < ecc_codewords; i++) {
            ecc_blocks[i] = rs.getResult(i);
        }

        /* Add Reed-Solomon codewords to binary data */
        for (i = 0; i < ecc_codewords; i++) {
            binary += toBinary(ecc_blocks[ecc_codewords - i - 1], 0x80);
        }
    }

    private void generateM4Symbol(EccMode ecc_mode) {
        int i;
        int bits_total, bits_left, remainder;
        int data_codewords, ecc_codewords;
        int[] data_blocks = new int[17];
        int[] ecc_blocks = new int[15];
        ReedSolomon rs = new ReedSolomon();

        bits_total = 128; // ecc_mode == EccMode.L
        if (ecc_mode == EccMode.M) {
            bits_total = 112;
        }
        if (ecc_mode == EccMode.Q) {
            bits_total = 80;
        }

        /* Complete current byte */
        remainder = 8 - (binary.length() % 8);
        if (remainder == 8) {
            remainder = 0;
        }
        for (i = 0; i < remainder; i++) {
            binary += "0";
        }

        /* Add padding */
        bits_left = bits_total - binary.length();
        remainder = bits_left / 8;
        for (i = 0; i < remainder; i++) {
            if ((i & 1) != 0) {
                binary += "00010001";
            } else {
                binary += "11101100";
            }
        }

        data_codewords = 16;
        ecc_codewords = 8; // ecc_mode == EccMode.L
        if (ecc_mode == EccMode.M) {
            data_codewords = 14;
            ecc_codewords = 10;
        }
        if (ecc_mode == EccMode.Q) {
            data_codewords = 10;
            ecc_codewords = 14;
        }

        /* Copy data into codewords */
        for (i = 0; i < data_codewords; i++) {
            data_blocks[i] = 0;
            if (binary.charAt(i * 8) == '1') {
                data_blocks[i] += 0x80;
            }
            if (binary.charAt((i * 8) + 1) == '1') {
                data_blocks[i] += 0x40;
            }
            if (binary.charAt((i * 8) + 2) == '1') {
                data_blocks[i] += 0x20;
            }
            if (binary.charAt((i * 8) + 3) == '1') {
                data_blocks[i] += 0x10;
            }
            if (binary.charAt((i * 8) + 4) == '1') {
                data_blocks[i] += 0x08;
            }
            if (binary.charAt((i * 8) + 5) == '1') {
                data_blocks[i] += 0x04;
            }
            if (binary.charAt((i * 8) + 6) == '1') {
                data_blocks[i] += 0x02;
            }
            if (binary.charAt((i * 8) + 7) == '1') {
                data_blocks[i] += 0x01;
            }
        }

        encodeInfo += "Codewords: ";
        System.out.printf("\tCodewords: ");
        for (i = 0; i < data_codewords; i++) {
            encodeInfo += Integer.toString(data_blocks[i]) + " ";
        }
        encodeInfo += "\n";

        /* Calculate Reed-Solomon error codewords */
        rs.init_gf(0x11d);
        rs.init_code(ecc_codewords, 0);
        rs.encode(data_codewords, data_blocks);
        for (i = 0; i < ecc_codewords; i++) {
            ecc_blocks[i] = rs.getResult(i);
        }

        /* Add Reed-Solomon codewords to binary data */
        for (i = 0; i < ecc_codewords; i++) {
            binary += toBinary(ecc_blocks[ecc_codewords - i - 1], 0x80);
        }
    }

    private void setupBitGrid(int size) {
        int i, toggle = 1;

        /* Add timing patterns */
        for (i = 0; i < size; i++) {
            if (toggle == 1) {
                grid[i] = 0x21;
                grid[(i * size)] = 0x21;
                toggle = 0;
            } else {
                grid[i] = 0x20;
                grid[(i * size)] = 0x20;
                toggle = 1;
            }
        }

        /* Add finder patterns */
        placeFinderPattern(size, 0, 0);

        /* Add separators */
        for (i = 0; i < 7; i++) {
            grid[(7 * size) + i] = 0x10;
            grid[(i * size) + 7] = 0x10;
        }
        grid[(7 * size) + 7] = 0x10;


        /* Reserve space for format information */
        for (i = 0; i < 8; i++) {
            grid[(8 * size) + i] += 0x20;
            grid[(i * size) + 8] += 0x20;
        }
        grid[(8 * size) + 8] += 0x20;
    }

    private void placeFinderPattern(int size, int x, int y) {
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

    private void populateBitGrid(int size) {
        boolean goingUp = true;
        int row = 0; /* right hand side */

        int i, n, x, y;

        n = binary.length();
        y = size - 1;
        i = 0;
        do {
            x = (size - 2) - (row * 2);

            if ((grid[(y * size) + (x + 1)] & 0xf0) == 0) {
                if (binary.charAt(i) == '1') {
                    grid[(y * size) + (x + 1)] = 0x01;
                } else {
                    grid[(y * size) + (x + 1)] = 0x00;
                }
                i++;
            }

            if (i < n) {
                if ((grid[(y * size) + x] & 0xf0) == 0) {
                    if (binary.charAt(i) == '1') {
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
            if (y == 0) {
                /* reached the top */
                row++;
                y = 1;
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

    private int applyBitmask(int size) {
        int x, y;
        int p;
        int local_pattern;
        int[] value = new int[8];
        int best_val, best_pattern;
        int bit;

        int[] mask = new int[size * size];
        eval = new int[size * size];

        /* Perform data masking */
        for (x = 0; x < size; x++) {
            for (y = 0; y < size; y++) {
                mask[(y * size) + x] = 0x00;

                if ((grid[(y * size) + x] & 0xf0) == 0) {
                    if ((y & 1) == 0) {
                        mask[(y * size) + x] += 0x01;
                    }

                    if ((((y / 2) + (x / 3)) & 1) == 0) {
                        mask[(y * size) + x] += 0x02;
                    }

                    if (((((y * x) & 1) + ((y * x) % 3)) & 1) == 0) {
                        mask[(y * size) + x] += 0x04;
                    }

                    if (((((y + x) & 1) + ((y * x) % 3)) & 1) == 0) {
                        mask[(y * size) + x] += 0x08;
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

                eval[(y * size) + x] = mask[(y * size) + x] ^ p;
            }
        }

        /* Evaluate result */
        for (local_pattern = 0; local_pattern < 4; local_pattern++) {
            value[local_pattern] = evaluateBitmask(size, local_pattern);
        }

        best_pattern = 0;
        best_val = value[0];
        for (local_pattern = 1; local_pattern < 4; local_pattern++) {
            if (value[local_pattern] > best_val) {
                best_pattern = local_pattern;
                best_val = value[local_pattern];
            }
        }

        /* Apply mask */
        for (x = 0; x < size; x++) {
            for (y = 0; y < size; y++) {
                bit = 0;
                switch (best_pattern) {
                case 0:
                    if ((mask[(y * size) + x] & 0x01) != 0) {
                        bit = 1;
                    }
                    break;
                case 1:
                    if ((mask[(y * size) + x] & 0x02) != 0) {
                        bit = 1;
                    }
                    break;
                case 2:
                    if ((mask[(y * size) + x] & 0x04) != 0) {
                        bit = 1;
                    }
                    break;
                case 3:
                    if ((mask[(y * size) + x] & 0x08) != 0) {
                        bit = 1;
                    }
                    break;
                }
                if (bit == 1) {
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

    private int evaluateBitmask(int size, int pattern) {
        int sum1, sum2, i, filter = 0, retval;

        switch (pattern) {
        case 0:
            filter = 0x01;
            break;
        case 1:
            filter = 0x02;
            break;
        case 2:
            filter = 0x04;
            break;
        case 3:
            filter = 0x08;
            break;
        }

        sum1 = 0;
        sum2 = 0;
        for (i = 1; i < size; i++) {
            if ((eval[(i * size) + size - 1] & filter) != 0) {
                sum1++;
            }
            if ((eval[((size - 1) * size) + i] & filter) != 0) {
                sum2++;
            }
        }

        if (sum1 <= sum2) {
            retval = (sum1 * 16) + sum2;
        } else {
            retval = (sum2 * 16) + sum1;
        }

        return retval;
    }
}
