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
 * Implements Micro QR Code
 * According to ISO/IEC 18004:2006
 *
 * @author Robin Stuart <rstuart114@gmail.com>
 */
public class MicroQrCode extends Symbol {
    private enum qrMode {
        NULL, KANJI, BINARY, ALPHANUM, NUMERIC
    }
    private enum eccMode {
        L, M, Q, H
    }

    private qrMode[] inputMode;
    private String binary;
    private boolean inter_byte_used;
    private boolean inter_alphanum_used;
    private boolean inter_kanji_used;
    private int[] binary_count = new int[4];
    private String full_stream;
    private int[] grid;
    private int[] eval;

    private char[] rhodium = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 
        'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 
        'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', ' ', '$', '%', '*', '\'', '+', 
        '-', '.', '/', ':'
    };

    private int[] qr_annex_c1 = {
        /* Micro QR Code format information */
        0x4445, 0x4172, 0x4e2b, 0x4b1c, 0x55ae, 0x5099, 0x5fc0, 0x5af7, 0x6793, 
        0x62a4, 0x6dfd, 0x68ca, 0x7678, 0x734f, 0x7c16, 0x7921, 0x06de, 0x03e9, 
        0x0cb0, 0x0987, 0x1735, 0x1202, 0x1d5b, 0x186c, 0x2508, 0x203f, 0x2f66, 
        0x2a51, 0x34e3, 0x31d4, 0x3e8d, 0x3bba
    };

    static int micro_qr_sizes[] = {
        11, 13, 15, 17
    };

    @Override
    public boolean encode() {
        int i, j, size;
        boolean[] version_valid = new boolean[4];
        int n_count, a_count;
        eccMode ecc_level;
        int version, autoversion;
        int bitmask;
        int format, format_full;
        String bin;

        if (content.length() > 35) {
            error_msg = "Input data too long";
            return false;
        }

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

        if (!(generateIntermediate())) {
            error_msg = "Input data too long";
            return false;
        }

        getBinaryLength();

        /* Eliminate possivle versions depending on type of content */
        if (inter_byte_used) {
            version_valid[0] = false;
            version_valid[1] = false;
        }

        if (inter_alphanum_used) {
            version_valid[0] = false;
        }

        if (inter_kanji_used) {
            version_valid[0] = false;
            version_valid[1] = false;
        }

        /* Eliminate possible versions depending on length of binary data */
        if (binary_count[0] > 20) {
            version_valid[0] = false;
        }
        if (binary_count[1] > 40) {
            version_valid[1] = false;
        }
        if (binary_count[2] > 84) {
            version_valid[2] = false;
        }
        if (binary_count[3] > 128) {
            error_msg = "Input data too long";
            return false;
        }

        /* Eliminate possible versions depending on error correction level specified */
        ecc_level = eccMode.L;
        //if((symbol->option_1 >= 1) && (symbol->option_2 <= 4)) {
        //	ecc_level = symbol->option_1;
        //}
        //FIXME: Get value from user
        if (ecc_level == eccMode.H) {
            error_msg = "Error correction level H not available";
            return false;
        }

        if (ecc_level == eccMode.Q) {
            version_valid[0] = false;
            version_valid[1] = false;
            version_valid[2] = false;
            if (binary_count[3] > 80) {
                error_msg = "Input data too long";
                return false;
            }
        }

        if (ecc_level == eccMode.M) {
            version_valid[0] = false;
            if (binary_count[1] > 32) {
                version_valid[1] = false;
            }
            if (binary_count[2] > 68) {
                version_valid[2] = false;
            }
            if (binary_count[3] > 112) {
                error_msg = "Input data too long";
                return false;
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
        //	if((symbol->option_2 >= 1) && (symbol->option_2 <= 4)) {
        //		if(symbol->option_2 >= autoversion) {
        //			version = symbol->option_2;
        //		}
        //	}


        /* If there is enough unused space then increase the error correction level */
        if (version == 3) {
            if (binary_count[3] <= 112) {
                ecc_level = eccMode.M;
            }
            if (binary_count[3] <= 80) {
                ecc_level = eccMode.Q;
            }
        }

        if (version == 2) {
            if (binary_count[2] <= 68) {
                ecc_level = eccMode.M;
            }
        }

        if (version == 1) {
            if (binary_count[1] <= 32) {
                ecc_level = eccMode.M;
            }
        }

        full_stream = "";
        expandBinaryString(version);

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

        size = micro_qr_sizes[version];

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

        format_full = qr_annex_c1[(format << 2) + bitmask];

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

        plotSymbol();
        return true;
    }
    
    private char levelToLetter(eccMode ecc_mode) {
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

    private boolean generateIntermediate() {
        /* Convert input data to an "intermediate stage" where data is binary encoded but
	   control information is not */
        int position = 0;
        int short_data_block_length, i;
        qrMode data_block;
        int msb, lsb, prod, jis;
        String oneChar;
        byte[] jisBytes;
        int count, first, second, third;

        binary = "";
        inter_kanji_used = false;
        inter_byte_used = false;
        inter_alphanum_used = false;

        if (debug) {
            for (i = 0; i < content.length(); i++) {
                switch (inputMode[i]) {
                case KANJI:
                    System.out.print("K");
                    break;
                case BINARY:
                    System.out.print("B");
                    break;
                case ALPHANUM:
                    System.out.print("A");
                    break;
                case NUMERIC:
                    System.out.print("N");
                    break;
                }
            }
            System.out.println();
        }

        do {
            if (binary.length() > 128) {
                return false;
            }

            data_block = inputMode[position];
            short_data_block_length = 0;
            do {
                short_data_block_length++;
            } while (((short_data_block_length + position) < content.length()) 
                    && (inputMode[position + short_data_block_length] == data_block));

            switch (data_block) {
            case KANJI:
                /* Kanji mode */
                /* Mode indicator */
                binary += "K";
                inter_kanji_used = true;

                /* Character count indicator */
                binary += (char) short_data_block_length;

                if (debug) {
                    System.out.printf("Kanji block (length %d)\n", 
                            short_data_block_length);
                }

                /* Character representation */
                for (i = 0; i < short_data_block_length; i++) {
                    oneChar = "";
                    oneChar += content.charAt(position + i);

                    /* Convert Unicode input to Shift-JIS */
                    try {
                        jisBytes = oneChar.getBytes("SJIS");
                    } catch (UnsupportedEncodingException e) {
                        error_msg = "Invalid character(s) in input data";
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

                    binary += toBinary(prod, 0x1000);

                    if (debug) {
                        System.out.printf("\t0x%4X\n", prod);
                    }

                    if (binary.length() > 128) {
                        return false;
                    }
                }

                if (debug) {
                    System.out.printf("\n");
                }

                break;
            case BINARY:
                /* Byte mode */
                /* Mode indicator */
                binary += "B";
                inter_byte_used = true;

                /* Character count indicator */
                binary += (char) short_data_block_length;

                if (debug) {
                    System.out.printf("Byte block (length %d)\n\t", 
                            short_data_block_length);
                }

                /* Character representation */
                for (i = 0; i < short_data_block_length; i++) {
                    int lbyte = content.charAt(position + i);

                    binary += toBinary(lbyte, 0x80);

                    if (debug) {
                        System.out.printf("0x%4X ", lbyte);
                    }

                    if (binary.length() > 128) {
                        return false;
                    }
                }

                if (debug) {
                    System.out.printf("\n");
                }

                break;
            case ALPHANUM:
                /* Alphanumeric mode */
                /* Mode indicator */
                binary += "A";
                inter_alphanum_used = true;

                /* Character count indicator */
                binary += (char) short_data_block_length;

                if (debug) {
                    System.out.printf("Alpha block (length %d)\n\t", 
                            short_data_block_length);
                }

                /* Character representation */
                i = 0;
                while (i < short_data_block_length) {
                    first = positionOf(content.charAt(position + i), rhodium);
                    count = 1;
                    prod = first;

                    if (inputMode[position + i + 1] == qrMode.ALPHANUM) {
                        second = positionOf(content.charAt(position + i + 1), rhodium);
                        count = 2;
                        prod = (first * 45) + second;
                    }

                    binary += toBinary(prod, 1 << (5 * count)); /* count = 1..2 */

                    if (debug) {
                        System.out.printf("0x%4X ", prod);
                    }

                    if (binary.length() > 128) {
                        return false;
                    }

                    i += 2;
                };

                if (debug) {
                    System.out.printf("\n");
                }

                break;
            case NUMERIC:
                /* Numeric mode */
                /* Mode indicator */
                binary += "N";

                /* Character count indicator */
                binary += (char) short_data_block_length;

                if (debug) {
                    System.out.printf("Number block (length %d)\n\t", 
                            short_data_block_length);
                }

                /* Character representation */
                i = 0;
                while (i < short_data_block_length) {
                    first = Character.getNumericValue(content.charAt(position + i));
                    count = 1;
                    prod = first;

                    if (inputMode[position + i + 1] == qrMode.NUMERIC) {
                        second = Character.getNumericValue(content.charAt(position + i + 1));
                        count = 2;
                        prod = (prod * 10) + second;
                    }

                    if (inputMode[position + i + 2] == qrMode.NUMERIC) {
                        third = Character.getNumericValue(content.charAt(position + i + 2));
                        count = 3;
                        prod = (prod * 10) + third;
                    }

                    binary += toBinary(prod, 1 << (3 * count)); /* count = 1..3 */

                    if (debug) {
                        System.out.printf("0x%4X (%d)", prod, prod);
                    }

                    if (binary.length() > 128) {
                        return false;
                    }

                    i += 3;
                };

                if (debug) {
                    System.out.printf("\n");
                }

                break;
            }

            position += short_data_block_length;
        } while (position < content.length() - 1);

        return true;
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

        for (i = 0; i < 4; i++) {
            binary_count[i] = 0;
        }

        i = 0;
        do {
            if ((binary.charAt(i) == '0') || (binary.charAt(i) == '1')) {
                binary_count[0]++;
                binary_count[1]++;
                binary_count[2]++;
                binary_count[3]++;
                i++;
            } else {
                switch (binary.charAt(i)) {
                case 'K':
                    binary_count[2] += 5;
                    binary_count[3] += 7;
                    i += 2;
                    break;
                case 'B':
                    binary_count[2] += 6;
                    binary_count[3] += 8;
                    i += 2;
                    break;
                case 'A':
                    binary_count[1] += 4;
                    binary_count[2] += 6;
                    binary_count[3] += 8;
                    i += 2;
                    break;
                case 'N':
                    binary_count[0] += 3;
                    binary_count[1] += 5;
                    binary_count[2] += 7;
                    binary_count[3] += 9;
                    i += 2;
                    break;
                }
            }
        } while (i < binary.length());
    }

    private void expandBinaryString(int version) {
        int i;

        i = 0;
        do {
            switch (binary.charAt(i)) {
            case '1':
                full_stream += "1";
                i++;
                break;
            case '0':
                full_stream += "0";
                i++;
                break;
            case 'N':
                /* Numeric Mode */
                /* Mode indicator */
                switch (version) {
                case 1:
                    full_stream += "0";
                    break;
                case 2:
                    full_stream += "00";
                    break;
                case 3:
                    full_stream += "000";
                    break;
                }

                /* Character count indicator */
                full_stream += toBinary(binary.charAt(i + 1), 4 << version); /* version = 0..3 */

                i += 2;
                break;
            case 'A':
                /* Alphanumeric Mode */
                /* Mode indicator */
                switch (version) {
                case 1:
                    full_stream += "1";
                    break;
                case 2:
                    full_stream += "01";
                    break;
                case 3:
                    full_stream += "001";
                    break;
                }

                /* Character count indicator */
                full_stream += toBinary(binary.charAt(i + 1), 2 << version); /* version = 1..3 */

                i += 2;
                break;
            case 'B':
                /* Byte Mode */
                /* Mode indicator */
                switch (version) {
                case 2:
                    full_stream += "10";
                    break;
                case 3:
                    full_stream += "010";
                    break;
                }

                /* Character count indicator */
                full_stream += toBinary(binary.charAt(i + 1), 2 << version); /* version = 2..3 */

                i += 2;
                break;
            case 'K':
                /* Kanji Mode */
                /* Mode indicator */
                switch (version) {
                case 2:
                    full_stream += "11";
                    break;
                case 3:
                    full_stream += "011";
                    break;
                }

                /* Character count indicator */
                full_stream += toBinary(binary.charAt(i + 1), 1 << version); /* version = 2..3 */

                i += 2;
                break;
            }

        } while (i < binary.length());
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

        /* Add terminator */
        bits_left = bits_total - full_stream.length();
        if (bits_left <= 3) {
            for (i = 0; i < bits_left; i++) {
                full_stream += "0";
            }
            latch = 1;
        } else {
            full_stream += "000";
        }

        if (latch == 0) {
            /* Manage last (4-bit) block */
            bits_left = bits_total - full_stream.length();
            if (bits_left <= 4) {
                for (i = 0; i < bits_left; i++) {
                    full_stream += "0";
                }
                latch = 1;
            }
        }

        if (latch == 0) {
            /* Complete current byte */
            remainder = 8 - (full_stream.length() % 8);
            if (remainder == 8) {
                remainder = 0;
            }
            for (i = 0; i < remainder; i++) {
                full_stream += "0";
            }

            /* Add padding */
            bits_left = bits_total - full_stream.length();
            if (bits_left > 4) {
                remainder = (bits_left - 4) / 8;
                for (i = 0; i < remainder; i++) {
                    if ((i & 1) != 0) {
                        full_stream += "00010001";
                    } else {
                        full_stream += "11101100";
                    }
                }
            }
            full_stream += "0000";
        }

        data_codewords = 3;
        ecc_codewords = 2;

        /* Copy data into codewords */
        for (i = 0; i < (data_codewords - 1); i++) {
            data_blocks[i] = 0;
            if (full_stream.charAt(i * 8) == '1') {
                data_blocks[i] += 0x80;
            }
            if (full_stream.charAt((i * 8) + 1) == '1') {
                data_blocks[i] += 0x40;
            }
            if (full_stream.charAt((i * 8) + 2) == '1') {
                data_blocks[i] += 0x20;
            }
            if (full_stream.charAt((i * 8) + 3) == '1') {
                data_blocks[i] += 0x10;
            }
            if (full_stream.charAt((i * 8) + 4) == '1') {
                data_blocks[i] += 0x08;
            }
            if (full_stream.charAt((i * 8) + 5) == '1') {
                data_blocks[i] += 0x04;
            }
            if (full_stream.charAt((i * 8) + 6) == '1') {
                data_blocks[i] += 0x02;
            }
            if (full_stream.charAt((i * 8) + 7) == '1') {
                data_blocks[i] += 0x01;
            }
        }
        data_blocks[2] = 0;
        if (full_stream.charAt(16) == '1') {
            data_blocks[2] += 0x08;
        }
        if (full_stream.charAt(17) == '1') {
            data_blocks[2] += 0x04;
        }
        if (full_stream.charAt(18) == '1') {
            data_blocks[2] += 0x02;
        }
        if (full_stream.charAt(19) == '1') {
            data_blocks[2] += 0x01;
        }

        /* Calculate Reed-Solomon error codewords */
        rs.init_gf(0x11d);
        rs.init_code(ecc_codewords, 0);
        rs.encode(data_codewords, data_blocks);
        for (i = 0; i < ecc_codewords; i++) {
            ecc_blocks[i] = rs.getResult(i);
        }

        /* Add Reed-Solomon codewords to binary data */
        for (i = 0; i < ecc_codewords; i++) {
            full_stream += toBinary(ecc_blocks[ecc_codewords - i - 1], 0x80);
        }
    }

    private void generateM2Symbol(eccMode ecc_mode) {
        int i, latch;
        int bits_total, bits_left, remainder;
        int data_codewords, ecc_codewords;
        int[] data_blocks = new int[6];
        int[] ecc_blocks = new int[7];
        ReedSolomon rs = new ReedSolomon();

        latch = 0;

        bits_total = 40; // ecc_mode == eccMode.L
        if (ecc_mode == eccMode.M) {
            bits_total = 32;
        }

        /* Add terminator */
        bits_left = bits_total - full_stream.length();
        if (bits_left <= 5) {
            for (i = 0; i < bits_left; i++) {
                full_stream += "0";
            }
            latch = 1;
        } else {
            full_stream += "00000";
        }

        if (latch == 0) {
            /* Complete current byte */
            remainder = 8 - (full_stream.length() % 8);
            if (remainder == 8) {
                remainder = 0;
            }
            for (i = 0; i < remainder; i++) {
                full_stream += "0";
            }

            /* Add padding */
            bits_left = bits_total - full_stream.length();
            remainder = bits_left / 8;
            for (i = 0; i < remainder; i++) {
                if ((i & 1) != 0) {
                    full_stream += "00010001";
                } else {
                    full_stream += "11101100";
                }
            }
        }

        data_codewords = 5;
        ecc_codewords = 5; // ecc_mode == eccMode.L
        if (ecc_mode == eccMode.M) {
            data_codewords = 4;
            ecc_codewords = 6;
        }

        /* Copy data into codewords */
        for (i = 0; i < data_codewords; i++) {
            data_blocks[i] = 0;
            if (full_stream.charAt(i * 8) == '1') {
                data_blocks[i] += 0x80;
            }
            if (full_stream.charAt((i * 8) + 1) == '1') {
                data_blocks[i] += 0x40;
            }
            if (full_stream.charAt((i * 8) + 2) == '1') {
                data_blocks[i] += 0x20;
            }
            if (full_stream.charAt((i * 8) + 3) == '1') {
                data_blocks[i] += 0x10;
            }
            if (full_stream.charAt((i * 8) + 4) == '1') {
                data_blocks[i] += 0x08;
            }
            if (full_stream.charAt((i * 8) + 5) == '1') {
                data_blocks[i] += 0x04;
            }
            if (full_stream.charAt((i * 8) + 6) == '1') {
                data_blocks[i] += 0x02;
            }
            if (full_stream.charAt((i * 8) + 7) == '1') {
                data_blocks[i] += 0x01;
            }
        }

        /* Calculate Reed-Solomon error codewords */
        rs.init_gf(0x11d);
        rs.init_code(ecc_codewords, 0);
        rs.encode(data_codewords, data_blocks);
        for (i = 0; i < ecc_codewords; i++) {
            ecc_blocks[i] = rs.getResult(i);
        }

        /* Add Reed-Solomon codewords to binary data */
        for (i = 0; i < ecc_codewords; i++) {
            full_stream += toBinary(ecc_blocks[ecc_codewords - i - 1], 0x80);
        }
    }

    private void generateM3Symbol(eccMode ecc_mode) {
        int i, latch;
        int bits_total, bits_left, remainder;
        int data_codewords, ecc_codewords;
        int[] data_blocks = new int[12];
        int[] ecc_blocks = new int[12];
        ReedSolomon rs = new ReedSolomon();

        latch = 0;

        bits_total = 84; // ecc_mode == eccMode.L
        if (ecc_mode == eccMode.M) {
            bits_total = 68;
        }

        /* Add terminator */
        bits_left = bits_total - full_stream.length();
        if (bits_left <= 7) {
            for (i = 0; i < bits_left; i++) {
                full_stream += "0";
            }
            latch = 1;
        } else {
            full_stream += "0000000";
        }

        if (latch == 0) {
            /* Manage last (4-bit) block */
            bits_left = bits_total - full_stream.length();
            if (bits_left <= 4) {
                for (i = 0; i < bits_left; i++) {
                    full_stream += "0";
                }
                latch = 1;
            }
        }

        if (latch == 0) {
            /* Complete current byte */
            remainder = 8 - (full_stream.length() % 8);
            if (remainder == 8) {
                remainder = 0;
            }
            for (i = 0; i < remainder; i++) {
                full_stream += "0";
            }

            /* Add padding */
            bits_left = bits_total - full_stream.length();
            if (bits_left > 4) {
                remainder = (bits_left - 4) / 8;
                for (i = 0; i < remainder; i++) {
                    if ((i & 1) != 0) {
                        full_stream += "00010001";
                    } else {
                        full_stream += "11101100";
                    }
                }
            }
            full_stream += "0000";
        }

        data_codewords = 11;
        ecc_codewords = 6; // ecc_mode == eccMode.L
        if (ecc_mode == eccMode.M) {
            data_codewords = 9;
            ecc_codewords = 8;
        }

        /* Copy data into codewords */
        for (i = 0; i < (data_codewords - 1); i++) {
            data_blocks[i] = 0;
            if (full_stream.charAt(i * 8) == '1') {
                data_blocks[i] += 0x80;
            }
            if (full_stream.charAt((i * 8) + 1) == '1') {
                data_blocks[i] += 0x40;
            }
            if (full_stream.charAt((i * 8) + 2) == '1') {
                data_blocks[i] += 0x20;
            }
            if (full_stream.charAt((i * 8) + 3) == '1') {
                data_blocks[i] += 0x10;
            }
            if (full_stream.charAt((i * 8) + 4) == '1') {
                data_blocks[i] += 0x08;
            }
            if (full_stream.charAt((i * 8) + 5) == '1') {
                data_blocks[i] += 0x04;
            }
            if (full_stream.charAt((i * 8) + 6) == '1') {
                data_blocks[i] += 0x02;
            }
            if (full_stream.charAt((i * 8) + 7) == '1') {
                data_blocks[i] += 0x01;
            }
        }

        if (ecc_mode == eccMode.L) {
            data_blocks[11] = 0;
            if (full_stream.charAt(80) == '1') {
                data_blocks[2] += 0x08;
            }
            if (full_stream.charAt(81) == '1') {
                data_blocks[2] += 0x04;
            }
            if (full_stream.charAt(82) == '1') {
                data_blocks[2] += 0x02;
            }
            if (full_stream.charAt(83) == '1') {
                data_blocks[2] += 0x01;
            }
        }

        if (ecc_mode == eccMode.M) {
            data_blocks[9] = 0;
            if (full_stream.charAt(64) == '1') {
                data_blocks[2] += 0x08;
            }
            if (full_stream.charAt(65) == '1') {
                data_blocks[2] += 0x04;
            }
            if (full_stream.charAt(66) == '1') {
                data_blocks[2] += 0x02;
            }
            if (full_stream.charAt(67) == '1') {
                data_blocks[2] += 0x01;
            }
        }

        /* Calculate Reed-Solomon error codewords */
        rs.init_gf(0x11d);
        rs.init_code(ecc_codewords, 0);
        rs.encode(data_codewords, data_blocks);
        for (i = 0; i < ecc_codewords; i++) {
            ecc_blocks[i] = rs.getResult(i);
        }

        /* Add Reed-Solomon codewords to binary data */
        for (i = 0; i < ecc_codewords; i++) {
            full_stream += toBinary(ecc_blocks[ecc_codewords - i - 1], 0x80);
        }
    }

    private void generateM4Symbol(eccMode ecc_mode) {
        int i, latch;
        int bits_total, bits_left, remainder;
        int data_codewords, ecc_codewords;
        int[] data_blocks = new int[17];
        int[] ecc_blocks = new int[15];
        ReedSolomon rs = new ReedSolomon();

        latch = 0;

        bits_total = 128; // ecc_mode == eccMode.L
        if (ecc_mode == eccMode.M) {
            bits_total = 112;
        }
        if (ecc_mode == eccMode.Q) {
            bits_total = 80;
        }

        /* Add terminator */
        bits_left = bits_total - full_stream.length();
        if (bits_left <= 9) {
            for (i = 0; i < bits_left; i++) {
                full_stream += "0";
            }
            latch = 1;
        } else {
            full_stream += "000000000";
        }

        if (latch == 0) {
            /* Complete current byte */
            remainder = 8 - (full_stream.length() % 8);
            if (remainder == 8) {
                remainder = 0;
            }
            for (i = 0; i < remainder; i++) {
                full_stream += "0";
            }

            /* Add padding */
            bits_left = bits_total - full_stream.length();
            remainder = bits_left / 8;
            for (i = 0; i < remainder; i++) {
                if ((i & 1) != 0) {
                    full_stream += "00010001";
                } else {
                    full_stream += "11101100";
                }
            }
        }

        data_codewords = 16;
        ecc_codewords = 8; // ecc_mode == eccMode.L
        if (ecc_mode == eccMode.M) {
            data_codewords = 14;
            ecc_codewords = 10;
        }
        if (ecc_mode == eccMode.Q) {
            data_codewords = 10;
            ecc_codewords = 14;
        }

        /* Copy data into codewords */
        for (i = 0; i < data_codewords; i++) {
            data_blocks[i] = 0;
            if (full_stream.charAt(i * 8) == '1') {
                data_blocks[i] += 0x80;
            }
            if (full_stream.charAt((i * 8) + 1) == '1') {
                data_blocks[i] += 0x40;
            }
            if (full_stream.charAt((i * 8) + 2) == '1') {
                data_blocks[i] += 0x20;
            }
            if (full_stream.charAt((i * 8) + 3) == '1') {
                data_blocks[i] += 0x10;
            }
            if (full_stream.charAt((i * 8) + 4) == '1') {
                data_blocks[i] += 0x08;
            }
            if (full_stream.charAt((i * 8) + 5) == '1') {
                data_blocks[i] += 0x04;
            }
            if (full_stream.charAt((i * 8) + 6) == '1') {
                data_blocks[i] += 0x02;
            }
            if (full_stream.charAt((i * 8) + 7) == '1') {
                data_blocks[i] += 0x01;
            }
        }

        /* Calculate Reed-Solomon error codewords */
        rs.init_gf(0x11d);
        rs.init_code(ecc_codewords, 0);
        rs.encode(data_codewords, data_blocks);
        for (i = 0; i < ecc_codewords; i++) {
            ecc_blocks[i] = rs.getResult(i);
        }

        /* Add Reed-Solomon codewords to binary data */
        for (i = 0; i < ecc_codewords; i++) {
            full_stream += toBinary(ecc_blocks[ecc_codewords - i - 1], 0x80);
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
        grid[(8 * size) + 8] += 20;
    }

    private void placeFinderPattern(int size, int x, int y) {
        int xp, yp;

        int finder[] = {
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

        n = full_stream.length();
        y = size - 1;
        i = 0;
        do {
            x = (size - 2) - (row * 2);

            if ((grid[(y * size) + (x + 1)] & 0xf0) == 0) {
                if (full_stream.charAt(i) == '1') {
                    grid[(y * size) + (x + 1)] = 0x01;
                } else {
                    grid[(y * size) + (x + 1)] = 0x00;
                }
                i++;
            }

            if (i < n) {
                if ((grid[(y * size) + x] & 0xf0) == 0) {
                    if (full_stream.charAt(i) == '1') {
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
        int pattern;
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
        for (pattern = 0; pattern < 4; pattern++) {
            value[pattern] = evaluateBitmask(size, pattern);
        }

        best_pattern = 0;
        best_val = value[0];
        for (pattern = 1; pattern < 4; pattern++) {
            if (value[pattern] > best_val) {
                best_pattern = pattern;
                best_val = value[pattern];
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
