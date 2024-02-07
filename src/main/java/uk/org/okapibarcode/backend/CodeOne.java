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

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
/**
 * <p>Implements Code One.
 *
 * <p>Code One is able to encode the ISO 8859-1 (Latin-1) character set or GS1
 * data. There are two types of Code One symbol: variable height symbols
 * which are roughly square (versions A thought to H) and fixed-height
 * versions (version S and T). Version S symbols can only encode numeric data.
 * The width of version S and version T symbols is determined by the length
 * of the input data.
 *
 * @author <a href="mailto:rstuart114@gmail.com">Robin Stuart</a>
 */
public class CodeOne extends Symbol {

    public enum Version {
        NONE, A, B, C, D, E, F, G, H, S, T
    }

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

    private static final int[] C1_HEIGHT = {
        16, 22, 28, 40, 52, 70, 104, 148
    };
    private static final int[] C1_WIDTH = {
        18, 22, 32, 42, 54, 76, 98, 134
    };
    private static final int[] C1_DATA_LENGTH = {
        10, 19, 44, 91, 182, 370, 732, 1480
    };
    private static final int[] C1_ECC_LENGTH = {
        10, 16, 26, 44, 70, 140, 280, 560
    };
    private static final int[] C1_BLOCKS = {
        1, 1, 1, 1, 1, 2, 4, 8
    };
    private static final int[] C1_DATA_BLOCKS = {
        10, 19, 44, 91, 182, 185, 183, 185
    };
    private static final int[] C1_ECC_BLOCKS = {
        10, 16, 26, 44, 70, 70, 70, 70
    };
    private static final int[] C1_GRID_WIDTH = {
        4, 5, 7, 9, 12, 17, 22, 30
    };
    private static final int[] C1_GRID_HEIGHT = {
        5, 7, 10, 15, 21, 30, 46, 68
    };

    private enum Mode {
        C1_ASCII, C1_C40, C1_DECIMAL, C1_TEXT, C1_EDI, C1_BYTE
    }

    private Version preferredVersion = Version.NONE;

    private int[] data = new int[1500];
    private int[][] datagrid = new int[136][120];
    private boolean[][] outputGrid = new boolean[148][134];

    /**
     * Sets the preferred symbol size / version. Versions A to H are square symbols.
     * Version S and T are fixed height symbols. This value may be ignored if the input
     * data does not fit in the specified version.
     *
     * @param version the preferred symbol version
     */
    public void setPreferredVersion(Version version) {
        preferredVersion = version;
    }

    /**
     * Returns the preferred symbol version.
     *
     * @return the preferred symbol version
     */
    public Version getPreferredVersion() {
        return preferredVersion;
    }

    @Override
    protected boolean gs1Supported() {
        return true;
    }

    @Override
    protected void encode() {

        int size = 1, i, j, data_blocks;
        int row, col;
        int sub_version = 0;
        int codewords;
        int[] ecc = new int[600];
        int[] stream = new int[2100];
        int block_width;
        int length = content.length();
        ReedSolomon rs = new ReedSolomon();
        int data_length;
        int data_cw, ecc_cw;
        int[] sub_data = new int[190];
        StringBuilder bin = new StringBuilder();

        if (!content.matches("[\u0000-\u00FF]+")) {
            throw OkapiInputException.invalidCharactersInInput();
        }

        if (preferredVersion == Version.S) {
            /* Version S */

            infoLine("Version: S");

            if (length > 18) {
                throw OkapiInputException.inputTooLong();
            }

            if (!content.matches("[0-9]+?")) {
                throw OkapiInputException.invalidCharactersInInput();
            }

            sub_version = 3;
            codewords = 12;
            block_width = 6; /* Version S-30 */
            if (length <= 12) {
                sub_version = 2;
                codewords = 8;
                block_width = 4;
            } /* Version S-20 */
            if (length <= 6) {
                sub_version = 1;
                codewords = 4;
                block_width = 2;
            } /* Version S-10 */

            BigInteger elreg = new BigInteger(content);

            for (i = 0; i < codewords; i++) {
                BigInteger codewordValue = elreg.shiftRight(5 * i);
                codewordValue = codewordValue.and(BigInteger.valueOf(0b11111));
                data[codewords - i - 1] = codewordValue.intValue();
            }

            logCodewords(codewords);

            rs.init_gf(0x25);
            rs.init_code(codewords, 1);
            rs.encode(codewords, data);

            infoLine("ECC Codeword Count: " + codewords);

            for (i = 0; i < codewords; i++) {
                stream[i] = data[i];
                stream[i + codewords] = rs.getResult(codewords - i - 1);
            }

            for (i = 0; i < 136; i++) {
                for (j = 0; j < 120; j++) {
                    datagrid[i][j] = '0';
                }
            }

            i = 0;
            for (row = 0; row < 2; row++) {
                for (col = 0; col < block_width; col++) {
                    if ((stream[i] & 0x10) != 0) {
                        datagrid[row * 2][col * 5] = '1';
                    }
                    if ((stream[i] & 0x08) != 0) {
                        datagrid[row * 2][(col * 5) + 1] = '1';
                    }
                    if ((stream[i] & 0x04) != 0) {
                        datagrid[row * 2][(col * 5) + 2] = '1';
                    }
                    if ((stream[i] & 0x02) != 0) {
                        datagrid[(row * 2) + 1][col * 5] = '1';
                    }
                    if ((stream[i] & 0x01) != 0) {
                        datagrid[(row * 2) + 1][(col * 5) + 1] = '1';
                    }
                    if ((stream[i + 1] & 0x10) != 0) {
                        datagrid[row * 2][(col * 5) + 3] = '1';
                    }
                    if ((stream[i + 1] & 0x08) != 0) {
                        datagrid[row * 2][(col * 5) + 4] = '1';
                    }
                    if ((stream[i + 1] & 0x04) != 0) {
                        datagrid[(row * 2) + 1][(col * 5) + 2] = '1';
                    }
                    if ((stream[i + 1] & 0x02) != 0) {
                        datagrid[(row * 2) + 1][(col * 5) + 3] = '1';
                    }
                    if ((stream[i + 1] & 0x01) != 0) {
                        datagrid[(row * 2) + 1][(col * 5) + 4] = '1';
                    }
                    i += 2;
                }
            }

            infoLine("Grid Size: " + block_width + " X " + 2);

            size = 9;
            row_count = 8;
            symbol_width = 10 * sub_version + 1;
        }

        if (preferredVersion == Version.T) {
            /* Version T */

            infoLine("Version: T");

            for (i = 0; i < 40; i++) {
                data[i] = 0;
            }
            data_length = encodeAsCode1Data();

            if (data_length > 38) {
                throw OkapiInputException.inputTooLong();
            }

            size = 10;
            sub_version = 3;
            data_cw = 38;
            ecc_cw = 22;
            block_width = 12;
            if (data_length <= 24) {
                sub_version = 2;
                data_cw = 24;
                ecc_cw = 16;
                block_width = 8;
            }
            if (data_length <= 10) {
                sub_version = 1;
                data_cw = 10;
                ecc_cw = 10;
                block_width = 4;
            }

            logCodewords(data_length);

            for (i = data_length; i < data_cw; i++) {
                data[i] = 129; /* Pad */
            }

            /* Calculate error correction data */
            rs.init_gf(0x12d);
            rs.init_code(ecc_cw, 1);
            rs.encode(data_cw, data);

            infoLine("ECC Codeword Count: " + ecc_cw);

            /* "Stream" combines data and error correction data */
            for (i = 0; i < data_cw; i++) {
                stream[i] = data[i];
            }
            for (i = 0; i < ecc_cw; i++) {
                stream[data_cw + i] = rs.getResult(ecc_cw - i - 1);
            }

            for (i = 0; i < 136; i++) {
                for (j = 0; j < 120; j++) {
                    datagrid[i][j] = '0';
                }
            }

            i = 0;
            for (row = 0; row < 5; row++) {
                for (col = 0; col < block_width; col++) {
                    if ((stream[i] & 0x80) != 0) {
                        datagrid[row * 2][col * 4] = '1';
                    }
                    if ((stream[i] & 0x40) != 0) {
                        datagrid[row * 2][(col * 4) + 1] = '1';
                    }
                    if ((stream[i] & 0x20) != 0) {
                        datagrid[row * 2][(col * 4) + 2] = '1';
                    }
                    if ((stream[i] & 0x10) != 0) {
                        datagrid[row * 2][(col * 4) + 3] = '1';
                    }
                    if ((stream[i] & 0x08) != 0) {
                        datagrid[(row * 2) + 1][col * 4] = '1';
                    }
                    if ((stream[i] & 0x04) != 0) {
                        datagrid[(row * 2) + 1][(col * 4) + 1] = '1';
                    }
                    if ((stream[i] & 0x02) != 0) {
                        datagrid[(row * 2) + 1][(col * 4) + 2] = '1';
                    }
                    if ((stream[i] & 0x01) != 0) {
                        datagrid[(row * 2) + 1][(col * 4) + 3] = '1';
                    }
                    i++;
                }
            }

            infoLine("Grid Size: " + block_width + " X " + 5);

            row_count = 16;
            symbol_width = (sub_version * 16) + 1;
        }

        if ((preferredVersion != Version.S) && (preferredVersion != Version.T)) {
            /* Version A to H */
            for (i = 0; i < 1500; i++) {
                data[i] = 0;
            }
            data_length = encodeAsCode1Data();

            for (i = 7; i >= 0; i--) {
                if (C1_DATA_LENGTH[i] >= data_length) {
                    size = i + 1;
                }
            }

            if (getSize(preferredVersion) > size) {
                size = getSize(preferredVersion);
            }

            char version = (char) ((size - 1) + 'A');
            infoLine("Version: " + version);
            logCodewords(data_length);

            for (i = data_length; i < C1_DATA_LENGTH[size - 1]; i++) {
                data[i] = 129; /* Pad */
            }

            /* Calculate error correction data */
            data_length = C1_DATA_LENGTH[size - 1];

            data_blocks = C1_BLOCKS[size - 1];

            rs.init_gf(0x12d);
            rs.init_code(C1_ECC_BLOCKS[size - 1], 0);
            for (i = 0; i < data_blocks; i++) {
                for (j = 0; j < C1_DATA_BLOCKS[size - 1]; j++) {
                    sub_data[j] = data[j * data_blocks + i];
                }
                rs.encode(C1_DATA_BLOCKS[size - 1], sub_data);
                for (j = 0; j < C1_ECC_BLOCKS[size - 1]; j++) {
                    ecc[C1_ECC_LENGTH[size - 1] - (j * data_blocks + i) - 1] = rs.getResult(j);
                }
            }

            infoLine("ECC Codeword Count: " + C1_ECC_LENGTH[size - 1]);

            /* "Stream" combines data and error correction data */
            for (i = 0; i < data_length; i++) {
                stream[i] = data[i];
            }
            for (i = 0; i < C1_ECC_LENGTH[size - 1]; i++) {
                stream[data_length + i] = ecc[i];
            }

            for (i = 0; i < 136; i++) {
                for (j = 0; j < 120; j++) {
                    datagrid[i][j] = '0';
                }
            }

            i = 0;
            for (row = 0; row < C1_GRID_HEIGHT[size - 1]; row++) {
                for (col = 0; col < C1_GRID_WIDTH[size - 1]; col++) {
                    if ((stream[i] & 0x80) != 0) {
                        datagrid[row * 2][col * 4] = '1';
                    }
                    if ((stream[i] & 0x40) != 0) {
                        datagrid[row * 2][(col * 4) + 1] = '1';
                    }
                    if ((stream[i] & 0x20) != 0) {
                        datagrid[row * 2][(col * 4) + 2] = '1';
                    }
                    if ((stream[i] & 0x10) != 0) {
                        datagrid[row * 2][(col * 4) + 3] = '1';
                    }
                    if ((stream[i] & 0x08) != 0) {
                        datagrid[(row * 2) + 1][col * 4] = '1';
                    }
                    if ((stream[i] & 0x04) != 0) {
                        datagrid[(row * 2) + 1][(col * 4) + 1] = '1';
                    }
                    if ((stream[i] & 0x02) != 0) {
                        datagrid[(row * 2) + 1][(col * 4) + 2] = '1';
                    }
                    if ((stream[i] & 0x01) != 0) {
                        datagrid[(row * 2) + 1][(col * 4) + 3] = '1';
                    }
                    i++;
                }
            }

            infoLine("Grid Size: " + C1_GRID_WIDTH[size - 1] + " X " + C1_GRID_HEIGHT[size - 1]);

            row_count = C1_HEIGHT[size - 1];
            symbol_width = C1_WIDTH[size - 1];
        }

        for (i = 0; i < 148; i++) {
            for (j = 0; j < 134; j++) {
                outputGrid[i][j] = false;
            }
        }

        switch (size) {
        case 1:
            /* Version A */
            plotCentralFinder(6, 3, 1);
            plotVerticalBar(4, 6, 1);
            plotVerticalBar(12, 5, 0);
            setGridModule(5, 12);
            plotSpigot(0);
            plotSpigot(15);
            plotDataBlock(0, 0, 5, 4, 0, 0);
            plotDataBlock(0, 4, 5, 12, 0, 2);
            plotDataBlock(5, 0, 5, 12, 6, 0);
            plotDataBlock(5, 12, 5, 4, 6, 2);
            break;
        case 2:
            /* Version B */
            plotCentralFinder(8, 4, 1);
            plotVerticalBar(4, 8, 1);
            plotVerticalBar(16, 7, 0);
            setGridModule(7, 16);
            plotSpigot(0);
            plotSpigot(21);
            plotDataBlock(0, 0, 7, 4, 0, 0);
            plotDataBlock(0, 4, 7, 16, 0, 2);
            plotDataBlock(7, 0, 7, 16, 8, 0);
            plotDataBlock(7, 16, 7, 4, 8, 2);
            break;
        case 3:
            /* Version C */
            plotCentralFinder(11, 4, 2);
            plotVerticalBar(4, 11, 1);
            plotVerticalBar(26, 13, 1);
            plotVerticalBar(4, 10, 0);
            plotVerticalBar(26, 10, 0);
            plotSpigot(0);
            plotSpigot(27);
            plotDataBlock(0, 0, 10, 4, 0, 0);
            plotDataBlock(0, 4, 10, 20, 0, 2);
            plotDataBlock(0, 24, 10, 4, 0, 4);
            plotDataBlock(10, 0, 10, 4, 8, 0);
            plotDataBlock(10, 4, 10, 20, 8, 2);
            plotDataBlock(10, 24, 10, 4, 8, 4);
            break;
        case 4:
            /* Version D */
            plotCentralFinder(16, 5, 1);
            plotVerticalBar(4, 16, 1);
            plotVerticalBar(20, 16, 1);
            plotVerticalBar(36, 16, 1);
            plotVerticalBar(4, 15, 0);
            plotVerticalBar(20, 15, 0);
            plotVerticalBar(36, 15, 0);
            plotSpigot(0);
            plotSpigot(12);
            plotSpigot(27);
            plotSpigot(39);
            plotDataBlock(0, 0, 15, 4, 0, 0);
            plotDataBlock(0, 4, 15, 14, 0, 2);
            plotDataBlock(0, 18, 15, 14, 0, 4);
            plotDataBlock(0, 32, 15, 4, 0, 6);
            plotDataBlock(15, 0, 15, 4, 10, 0);
            plotDataBlock(15, 4, 15, 14, 10, 2);
            plotDataBlock(15, 18, 15, 14, 10, 4);
            plotDataBlock(15, 32, 15, 4, 10, 6);
            break;
        case 5:
            /* Version E */
            plotCentralFinder(22, 5, 2);
            plotVerticalBar(4, 22, 1);
            plotVerticalBar(26, 24, 1);
            plotVerticalBar(48, 22, 1);
            plotVerticalBar(4, 21, 0);
            plotVerticalBar(26, 21, 0);
            plotVerticalBar(48, 21, 0);
            plotSpigot(0);
            plotSpigot(12);
            plotSpigot(39);
            plotSpigot(51);
            plotDataBlock(0, 0, 21, 4, 0, 0);
            plotDataBlock(0, 4, 21, 20, 0, 2);
            plotDataBlock(0, 24, 21, 20, 0, 4);
            plotDataBlock(0, 44, 21, 4, 0, 6);
            plotDataBlock(21, 0, 21, 4, 10, 0);
            plotDataBlock(21, 4, 21, 20, 10, 2);
            plotDataBlock(21, 24, 21, 20, 10, 4);
            plotDataBlock(21, 44, 21, 4, 10, 6);
            break;
        case 6:
            /* Version F */
            plotCentralFinder(31, 5, 3);
            plotVerticalBar(4, 31, 1);
            plotVerticalBar(26, 35, 1);
            plotVerticalBar(48, 31, 1);
            plotVerticalBar(70, 35, 1);
            plotVerticalBar(4, 30, 0);
            plotVerticalBar(26, 30, 0);
            plotVerticalBar(48, 30, 0);
            plotVerticalBar(70, 30, 0);
            plotSpigot(0);
            plotSpigot(12);
            plotSpigot(24);
            plotSpigot(45);
            plotSpigot(57);
            plotSpigot(69);
            plotDataBlock(0, 0, 30, 4, 0, 0);
            plotDataBlock(0, 4, 30, 20, 0, 2);
            plotDataBlock(0, 24, 30, 20, 0, 4);
            plotDataBlock(0, 44, 30, 20, 0, 6);
            plotDataBlock(0, 64, 30, 4, 0, 8);
            plotDataBlock(30, 0, 30, 4, 10, 0);
            plotDataBlock(30, 4, 30, 20, 10, 2);
            plotDataBlock(30, 24, 30, 20, 10, 4);
            plotDataBlock(30, 44, 30, 20, 10, 6);
            plotDataBlock(30, 64, 30, 4, 10, 8);
            break;
        case 7:
            /* Version G */
            plotCentralFinder(47, 6, 2);
            plotVerticalBar(6, 47, 1);
            plotVerticalBar(27, 49, 1);
            plotVerticalBar(48, 47, 1);
            plotVerticalBar(69, 49, 1);
            plotVerticalBar(90, 47, 1);
            plotVerticalBar(6, 46, 0);
            plotVerticalBar(27, 46, 0);
            plotVerticalBar(48, 46, 0);
            plotVerticalBar(69, 46, 0);
            plotVerticalBar(90, 46, 0);
            plotSpigot(0);
            plotSpigot(12);
            plotSpigot(24);
            plotSpigot(36);
            plotSpigot(67);
            plotSpigot(79);
            plotSpigot(91);
            plotSpigot(103);
            plotDataBlock(0, 0, 46, 6, 0, 0);
            plotDataBlock(0, 6, 46, 19, 0, 2);
            plotDataBlock(0, 25, 46, 19, 0, 4);
            plotDataBlock(0, 44, 46, 19, 0, 6);
            plotDataBlock(0, 63, 46, 19, 0, 8);
            plotDataBlock(0, 82, 46, 6, 0, 10);
            plotDataBlock(46, 0, 46, 6, 12, 0);
            plotDataBlock(46, 6, 46, 19, 12, 2);
            plotDataBlock(46, 25, 46, 19, 12, 4);
            plotDataBlock(46, 44, 46, 19, 12, 6);
            plotDataBlock(46, 63, 46, 19, 12, 8);
            plotDataBlock(46, 82, 46, 6, 12, 10);
            break;
        case 8:
            /* Version H */
            plotCentralFinder(69, 6, 3);
            plotVerticalBar(6, 69, 1);
            plotVerticalBar(26, 73, 1);
            plotVerticalBar(46, 69, 1);
            plotVerticalBar(66, 73, 1);
            plotVerticalBar(86, 69, 1);
            plotVerticalBar(106, 73, 1);
            plotVerticalBar(126, 69, 1);
            plotVerticalBar(6, 68, 0);
            plotVerticalBar(26, 68, 0);
            plotVerticalBar(46, 68, 0);
            plotVerticalBar(66, 68, 0);
            plotVerticalBar(86, 68, 0);
            plotVerticalBar(106, 68, 0);
            plotVerticalBar(126, 68, 0);
            plotSpigot(0);
            plotSpigot(12);
            plotSpigot(24);
            plotSpigot(36);
            plotSpigot(48);
            plotSpigot(60);
            plotSpigot(87);
            plotSpigot(99);
            plotSpigot(111);
            plotSpigot(123);
            plotSpigot(135);
            plotSpigot(147);
            plotDataBlock(0, 0, 68, 6, 0, 0);
            plotDataBlock(0, 6, 68, 18, 0, 2);
            plotDataBlock(0, 24, 68, 18, 0, 4);
            plotDataBlock(0, 42, 68, 18, 0, 6);
            plotDataBlock(0, 60, 68, 18, 0, 8);
            plotDataBlock(0, 78, 68, 18, 0, 10);
            plotDataBlock(0, 96, 68, 18, 0, 12);
            plotDataBlock(0, 114, 68, 6, 0, 14);
            plotDataBlock(68, 0, 68, 6, 12, 0);
            plotDataBlock(68, 6, 68, 18, 12, 2);
            plotDataBlock(68, 24, 68, 18, 12, 4);
            plotDataBlock(68, 42, 68, 18, 12, 6);
            plotDataBlock(68, 60, 68, 18, 12, 8);
            plotDataBlock(68, 78, 68, 18, 12, 10);
            plotDataBlock(68, 96, 68, 18, 12, 12);
            plotDataBlock(68, 114, 68, 6, 12, 14);
            break;
        case 9:
            /* Version S */
            plotHorizontalBar(5, 1);
            plotHorizontalBar(7, 1);
            setGridModule(6, 0);
            setGridModule(6, symbol_width - 1);
            resetGridModule(7, 1);
            resetGridModule(7, symbol_width - 2);
            switch (sub_version) {
            case 1:
                /* Version S-10 */
                setGridModule(0, 5);
                plotDataBlock(0, 0, 4, 5, 0, 0);
                plotDataBlock(0, 5, 4, 5, 0, 1);
                break;
            case 2:
                /* Version S-20 */
                setGridModule(0, 10);
                setGridModule(4, 10);
                plotDataBlock(0, 0, 4, 10, 0, 0);
                plotDataBlock(0, 10, 4, 10, 0, 1);
                break;
            case 3:
                /* Version S-30 */
                setGridModule(0, 15);
                setGridModule(4, 15);
                setGridModule(6, 15);
                plotDataBlock(0, 0, 4, 15, 0, 0);
                plotDataBlock(0, 15, 4, 15, 0, 1);
                break;
            }
            break;
        case 10:
            /* Version T */
            plotHorizontalBar(11, 1);
            plotHorizontalBar(13, 1);
            plotHorizontalBar(15, 1);
            setGridModule(12, 0);
            setGridModule(12, symbol_width - 1);
            setGridModule(14, 0);
            setGridModule(14, symbol_width - 1);
            resetGridModule(13, 1);
            resetGridModule(13, symbol_width - 2);
            resetGridModule(15, 1);
            resetGridModule(15, symbol_width - 2);
            switch (sub_version) {
            case 1:
                /* Version T-16 */
                setGridModule(0, 8);
                setGridModule(10, 8);
                plotDataBlock(0, 0, 10, 8, 0, 0);
                plotDataBlock(0, 8, 10, 8, 0, 1);
                break;
            case 2:
                /* Version T-32 */
                setGridModule(0, 16);
                setGridModule(10, 16);
                setGridModule(12, 16);
                plotDataBlock(0, 0, 10, 16, 0, 0);
                plotDataBlock(0, 16, 10, 16, 0, 1);
                break;
            case 3:
                /* Verion T-48 */
                setGridModule(0, 24);
                setGridModule(10, 24);
                setGridModule(12, 24);
                setGridModule(14, 24);
                plotDataBlock(0, 0, 10, 24, 0, 0);
                plotDataBlock(0, 24, 10, 24, 0, 1);
                break;
            }
            break;
        }

        readable = "";
        pattern = new String[row_count];
        row_height = new int[row_count];
        for (i = 0; i < row_count; i++) {
            bin.setLength(0);
            for (j = 0; j < symbol_width; j++) {
                if (outputGrid[i][j]) {
                    bin.append('1');
                } else {
                    bin.append('0');
                }
            }
            pattern[i] = bin2pat(bin);
            row_height[i] = 1;
        }
    }

    private void logCodewords(int count) {
        info("Codewords: ");
        for (int i = 0; i < count; i++) {
            infoSpace(data[i]);
        }
        infoLine();
    }

    private int encodeAsCode1Data() {
        Mode current_mode, next_mode;
        boolean latch;
        boolean done;
        int sourcePoint, targetPoint, i, j;
        int c40_p;
        int text_p;
        int edi_p;
        int byte_start = 0;
        int[] c40_buffer = new int[6];
        int[] text_buffer = new int[6];
        int[] edi_buffer = new int[6];
        String decimal_binary = "";
        int length;
        int shift_set, value;
        int data_left, decimal_count;
        int sub_value;
        int bits_left_in_byte, target_count;
        boolean isTwoDigits;

        inputData = toBytes(content, StandardCharsets.ISO_8859_1);
        length = inputData.length;

        sourcePoint = 0;
        targetPoint = 0;
        c40_p = 0;
        text_p = 0;
        edi_p = 0;

        if (inputDataType == DataType.GS1) {
            data[targetPoint] = 232;
            targetPoint++;
        } /* FNC1 */

        /* Step A */
        current_mode = Mode.C1_ASCII;
        next_mode = Mode.C1_ASCII;

        do {
            if (current_mode != next_mode) {
                /* Change mode */
                switch (next_mode) {
                case C1_C40:
                    data[targetPoint] = 230;
                    targetPoint++;
                    break;
                case C1_TEXT:
                    data[targetPoint] = 239;
                    targetPoint++;
                    break;
                case C1_EDI:
                    data[targetPoint] = 238;
                    targetPoint++;
                    break;
                case C1_BYTE:
                    data[targetPoint] = 231;
                    targetPoint++;
                    break;
                }
            }

            if ((current_mode != Mode.C1_BYTE) && (next_mode == Mode.C1_BYTE)) {
                byte_start = targetPoint;
            }
            current_mode = next_mode;

            if (current_mode == Mode.C1_ASCII) { /* Step B - ASCII encodation */
                next_mode = Mode.C1_ASCII;

                if ((length - sourcePoint) >= 21) { /* Step B1 */
                    j = 0;

                    for (i = 0; i < 21; i++) {
                        if ((inputData[sourcePoint + i] >= '0') && (inputData[sourcePoint + i] <= '9')) {
                            j++;
                        }
                    }

                    if (j == 21) {
                        next_mode = Mode.C1_DECIMAL;
                        decimal_binary += "1111";
                    }
                }

                if ((next_mode == Mode.C1_ASCII) && ((length - sourcePoint) >= 13)) { /* Step B2 */
                    j = 0;

                    for (i = 0; i < 13; i++) {
                        if ((inputData[sourcePoint + i] >= '0') && (inputData[sourcePoint + i] <= '9')) {
                            j++;
                        }
                    }

                    if (j == 13) {
                        latch = false;
                        for (i = sourcePoint + 13; i < length; i++) {
                            if (!((inputData[i] >= '0') &&
                                    (inputData[i] <= '9'))) {
                                latch = true;
                            }
                        }

                        if (!(latch)) {
                            next_mode = Mode.C1_DECIMAL;
                            decimal_binary += "1111";
                        }
                    }
                }

                if (next_mode == Mode.C1_ASCII) { /* Step B3 */
                    isTwoDigits = false;
                    if ((sourcePoint + 1) != length) {
                        if ((inputData[sourcePoint] >= '0') && (inputData[sourcePoint] <= '9')) {
                            if ((inputData[sourcePoint + 1] >= '0') && (inputData[sourcePoint + 1] <= '9')) {
                                // remaining data consists of two numeric digits
                                data[targetPoint] = (10 * (inputData[sourcePoint] - '0'))
                                        + (inputData[sourcePoint + 1] - '0') + 130;
                                targetPoint++;
                                sourcePoint += 2;
                                isTwoDigits = true;
                            }
                        }
                    }

                    if (!(isTwoDigits)) {
                        if (inputData[sourcePoint] == FNC1) {
                            if ((length - sourcePoint) >= 15) { /* Step B4 */
                                j = 0;

                                for (i = 0; i < 15; i++) {
                                    if ((inputData[sourcePoint + i] >= '0')
                                            && (inputData[sourcePoint + i] <= '9')) {
                                        j++;
                                    }
                                }

                                if (j == 15) {
                                    data[targetPoint] = 236; /* FNC1 and change to Decimal */
                                    targetPoint++;
                                    sourcePoint++;
                                    next_mode = Mode.C1_DECIMAL;
                                }
                            }

                            if ((length - sourcePoint) >= 7) { /* Step B5 */
                                j = 0;

                                for (i = 0; i < 7; i++) {
                                    if ((inputData[sourcePoint + i] >= '0')
                                            && (inputData[sourcePoint + i] <= '9')) {
                                        j++;
                                    }
                                }

                                if (j == 7) {
                                    latch = false;
                                    for (i = sourcePoint + 7; i < length; i++) {
                                        if (!((inputData[sourcePoint + i] >= '0')
                                                && (inputData[sourcePoint + i] <= '9'))) {
                                            latch = true;
                                        }
                                    }

                                    if (!(latch)) {
                                        data[targetPoint] = 236; /* FNC1 and change to Decimal */
                                        targetPoint++;
                                        sourcePoint++;
                                        next_mode = Mode.C1_DECIMAL;
                                    }
                                }
                            }
                        }

                        if (next_mode == Mode.C1_ASCII) {

                            /* Step B6 */
                            next_mode = lookAheadTest(length, sourcePoint, current_mode);

                            if (next_mode == Mode.C1_ASCII) {
                                if (inputData[sourcePoint] > 127) {
                                    /* Step B7 */
                                    data[targetPoint] = 235;
                                    targetPoint++; /* FNC4 */
                                    data[targetPoint] = (inputData[sourcePoint] - 128) + 1;
                                    targetPoint++;
                                    sourcePoint++;
                                } else {
                                    /* Step B8 */
                                    if (inputData[sourcePoint] == FNC1) {
                                        data[targetPoint] = 232;
                                        targetPoint++;
                                        sourcePoint++; /* FNC1 */
                                    } else {
                                        data[targetPoint] = inputData[sourcePoint] + 1;
                                        targetPoint++;
                                        sourcePoint++;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (current_mode == Mode.C1_C40) { /* Step C - C40 encodation */
                done = false;
                next_mode = Mode.C1_C40;
                if (c40_p == 0) {
                    if ((length - sourcePoint) >= 12) {
                        j = 0;

                        for (i = 0; i < 12; i++) {
                            if ((inputData[sourcePoint + i] >= '0')
                                    && (inputData[sourcePoint + i] <= '9')) {
                                j++;
                            }
                        }

                        if (j == 12) {
                            next_mode = Mode.C1_ASCII;
                            done = true;
                        }
                    }

                    if ((length - sourcePoint) >= 8) {
                        j = 0;

                        for (i = 0; i < 8; i++) {
                            if ((inputData[sourcePoint + i] >= '0')
                                    && (inputData[sourcePoint + i] <= '9')) {
                                j++;
                            }
                        }

                        if ((length - sourcePoint) == 8) {
                            latch = true;
                        } else {
                            latch = true;
                            for (j = sourcePoint + 8; j < length; j++) {
                                if ((inputData[j] <= '0') || (inputData[j] >= '9')) {
                                    latch = false;
                                }
                            }
                        }

                        if ((j == 8) && latch) {
                            next_mode = Mode.C1_ASCII;
                            done = true;
                        }
                    }

                    if (!(done)) {
                        next_mode = lookAheadTest(length, sourcePoint, current_mode);
                    }
                }

                if (next_mode != Mode.C1_C40) {
                    data[targetPoint] = 255;
                    targetPoint++; /* Unlatch */
                } else {
                    if (inputData[sourcePoint] > 127) {
                        c40_buffer[c40_p] = 1;
                        c40_p++;
                        c40_buffer[c40_p] = 30;
                        c40_p++; /* Upper Shift */
                        shift_set = C40_SHIFT[inputData[sourcePoint] - 128];
                        value = C40_VALUE[inputData[sourcePoint] - 128];
                    } else {
                        shift_set = C40_SHIFT[inputData[sourcePoint]];
                        value = C40_VALUE[inputData[sourcePoint]];
                    }

                    if (inputData[sourcePoint] == FNC1) {
                        shift_set = 2;
                        value = 27; /* FNC1 */
                    }

                    if (shift_set != 0) {
                        c40_buffer[c40_p] = shift_set - 1;
                        c40_p++;
                    }
                    c40_buffer[c40_p] = value;
                    c40_p++;

                    if (c40_p >= 3) {
                        int iv;

                        iv = (1600 * c40_buffer[0]) + (40 * c40_buffer[1])
                                + (c40_buffer[2]) + 1;
                        data[targetPoint] = iv / 256;
                        targetPoint++;
                        data[targetPoint] = iv % 256;
                        targetPoint++;

                        c40_buffer[0] = c40_buffer[3];
                        c40_buffer[1] = c40_buffer[4];
                        c40_buffer[2] = c40_buffer[5];
                        c40_buffer[3] = 0;
                        c40_buffer[4] = 0;
                        c40_buffer[5] = 0;
                        c40_p -= 3;
                    }
                    sourcePoint++;
                }
            }

            if (current_mode == Mode.C1_TEXT) { /* Step D - Text encodation */
                done = false;
                next_mode = Mode.C1_TEXT;
                if (text_p == 0) {
                    if ((length - sourcePoint) >= 12) {
                        j = 0;

                        for (i = 0; i < 12; i++) {
                            if ((inputData[sourcePoint + i] >= '0')
                                    && (inputData[sourcePoint + i] <= '9')) {
                                j++;
                            }
                        }

                        if (j == 12) {
                            next_mode = Mode.C1_ASCII;
                            done = true;
                        }
                    }

                    if ((length - sourcePoint) >= 8) {
                        j = 0;

                        for (i = 0; i < 8; i++) {
                            if ((inputData[sourcePoint + i] >= '0')
                                    && (inputData[sourcePoint + i] <= '9')) {
                                j++;
                            }
                        }

                        if ((length - sourcePoint) == 8) {
                            latch = true;
                        } else {
                            latch = true;
                            for (j = sourcePoint + 8; j < length; j++) {
                                if ((inputData[j] <= '0') || (inputData[j] >= '9')) {
                                    latch = false;
                                }
                            }
                        }

                        if ((j == 8) && latch) {
                            next_mode = Mode.C1_ASCII;
                            done = true;
                        }
                    }

                    if (!(done)) {
                        next_mode = lookAheadTest(length, sourcePoint, current_mode);
                    }
                }

                if (next_mode != Mode.C1_TEXT) {
                    data[targetPoint] = 255;
                    targetPoint++; /* Unlatch */
                } else {
                    if (inputData[sourcePoint] > 127) {
                        text_buffer[text_p] = 1;
                        text_p++;
                        text_buffer[text_p] = 30;
                        text_p++; /* Upper Shift */
                        shift_set = TEXT_SHIFT[inputData[sourcePoint] - 128];
                        value = TEXT_VALUE[inputData[sourcePoint] - 128];
                    } else {
                        shift_set = TEXT_SHIFT[inputData[sourcePoint]];
                        value = TEXT_VALUE[inputData[sourcePoint]];
                    }

                    if (inputData[sourcePoint] == FNC1) {
                        shift_set = 2;
                        value = 27; /* FNC1 */
                    }

                    if (shift_set != 0) {
                        text_buffer[text_p] = shift_set - 1;
                        text_p++;
                    }
                    text_buffer[text_p] = value;
                    text_p++;

                    if (text_p >= 3) {
                        int iv;

                        iv = (1600 * text_buffer[0]) + (40 * text_buffer[1])
                                + (text_buffer[2]) + 1;
                        data[targetPoint] = iv / 256;
                        targetPoint++;
                        data[targetPoint] = iv % 256;
                        targetPoint++;

                        text_buffer[0] = text_buffer[3];
                        text_buffer[1] = text_buffer[4];
                        text_buffer[2] = text_buffer[5];
                        text_buffer[3] = 0;
                        text_buffer[4] = 0;
                        text_buffer[5] = 0;
                        text_p -= 3;
                    }
                    sourcePoint++;
                }
            }

            if (current_mode == Mode.C1_EDI) { /* Step E - EDI Encodation */

                value = 0;
                next_mode = Mode.C1_EDI;
                if (edi_p == 0) {
                    if ((length - sourcePoint) >= 12) {
                        j = 0;

                        for (i = 0; i < 12; i++) {
                            if ((inputData[sourcePoint + i] >= '0')
                                    && (inputData[sourcePoint + i] <= '9')) {
                                j++;
                            }
                        }

                        if (j == 12) {
                            next_mode = Mode.C1_ASCII;
                        }
                    }

                    if ((length - sourcePoint) >= 8) {
                        j = 0;

                        for (i = 0; i < 8; i++) {
                            if ((inputData[sourcePoint + i] >= '0') &&
                                    (inputData[sourcePoint + i] <= '9')) {
                                j++;
                            }
                        }

                        if ((length - sourcePoint) == 8) {
                            latch = true;
                        } else {
                            latch = true;
                            for (j = sourcePoint + 8; j < length; j++) {
                                if ((inputData[j] <= '0') || (inputData[j] >= '9')) {
                                    latch = false;
                                }
                            }
                        }

                        if ((j == 8) && latch) {
                            next_mode = Mode.C1_ASCII;
                        }
                    }

                    if (!((isEdiEncodable(inputData[sourcePoint])
                            && isEdiEncodable(inputData[sourcePoint + 1]))
                            && isEdiEncodable(inputData[sourcePoint + 2]))) {
                        next_mode = Mode.C1_ASCII;
                    }
                }

                if (next_mode != Mode.C1_EDI) {
                    data[targetPoint] = 255;
                    targetPoint++; /* Unlatch */
                } else {
                    if (inputData[sourcePoint] == 13) {
                        value = 0;
                    }
                    if (inputData[sourcePoint] == '*') {
                        value = 1;
                    }
                    if (inputData[sourcePoint] == '>') {
                        value = 2;
                    }
                    if (inputData[sourcePoint] == ' ') {
                        value = 3;
                    }
                    if ((inputData[sourcePoint] >= '0') && (inputData[sourcePoint] <= '9')) {
                        value = inputData[sourcePoint] - '0' + 4;
                    }
                    if ((inputData[sourcePoint] >= 'A') && (inputData[sourcePoint] <= 'Z')) {
                        value = inputData[sourcePoint] - 'A' + 14;
                    }

                    edi_buffer[edi_p] = value;
                    edi_p++;

                    if (edi_p >= 3) {
                        int iv;

                        iv = (1600 * edi_buffer[0]) + (40 * edi_buffer[1])
                                + (edi_buffer[2]) + 1;
                        data[targetPoint] = iv / 256;
                        targetPoint++;
                        data[targetPoint] = iv % 256;
                        targetPoint++;

                        edi_buffer[0] = edi_buffer[3];
                        edi_buffer[1] = edi_buffer[4];
                        edi_buffer[2] = edi_buffer[5];
                        edi_buffer[3] = 0;
                        edi_buffer[4] = 0;
                        edi_buffer[5] = 0;
                        edi_p -= 3;
                    }
                    sourcePoint++;
                }
            }

            if (current_mode == Mode.C1_DECIMAL) { /* Step F - Decimal encodation */

                next_mode = Mode.C1_DECIMAL;

                data_left = length - sourcePoint;
                decimal_count = 0;

                if (data_left >= 1) {
                    if ((inputData[sourcePoint] >= '0') && (inputData[sourcePoint] <= '9')) {
                        decimal_count = 1;
                    }
                }
                if (data_left >= 2) {
                    if ((decimal_count == 1) && ((inputData[sourcePoint + 1] >= '0')
                            && (inputData[sourcePoint + 1] <= '9'))) {
                        decimal_count = 2;
                    }
                }
                if (data_left >= 3) {
                    if ((decimal_count == 2) && ((inputData[sourcePoint + 2] >= '0')
                            && (inputData[sourcePoint + 2] <= '9'))) {
                        decimal_count = 3;
                    }
                }

                if (decimal_count != 3) {

                    /* Finish Decimal mode and go back to ASCII */

                    decimal_binary += "111111"; /* Unlatch */

                    target_count = 3;
                    if (decimal_binary.length() <= 16) {
                        target_count = 2;
                    }
                    if (decimal_binary.length() <= 8) {
                        target_count = 1;
                    }
                    bits_left_in_byte = (8 * target_count) - decimal_binary.length();
                    if (bits_left_in_byte == 8) {
                        bits_left_in_byte = 0;
                    }

                    if (bits_left_in_byte == 2) {
                        decimal_binary += "01";
                    }

                    if ((bits_left_in_byte == 4) || (bits_left_in_byte == 6)) {
                        if (decimal_count >= 1) {
                            sub_value = inputData[sourcePoint] - '0' + 1;

                            for (i = 0x08; i > 0; i = i >> 1) {
                                if ((sub_value & i) != 0) {
                                    decimal_binary += "1";
                                } else {
                                    decimal_binary += "0";
                                }
                            }
                            sourcePoint++;
                        } else {
                            decimal_binary += "1111";
                        }
                    }

                    if (bits_left_in_byte == 6) {
                        decimal_binary += "01";
                    }

                    /* Binary buffer is full - transfer to data */
                    if (target_count >= 1) {
                        for(i = 0; i < 8; i++) {
                            if(decimal_binary.charAt(i) == '1') {
                                data[targetPoint] += 128 >> i;
                            }
                        }
                        targetPoint++;
                    }
                    if (target_count >= 2) {
                        for(i = 0; i < 8; i++) {
                            if(decimal_binary.charAt(8 + i) == '1') {
                                data[targetPoint] += 128 >> i;
                            }

                        }
                        targetPoint++;
                    }
                    if (target_count == 3) {
                        for(i = 0; i < 8; i++) {
                            if(decimal_binary.charAt(16 + i) == '1') {
                                data[targetPoint] += 128 >> i;
                            }

                        }
                        targetPoint++;
                    }

                    next_mode = Mode.C1_ASCII;
                } else {
                    /* There are three digits - convert the value to binary */
                    value = (100 * (inputData[sourcePoint] - '0'))
                            + (10 * (inputData[sourcePoint + 1] - '0'))
                            + (inputData[sourcePoint + 2] - '0') + 1;

                    for (i = 0x200; i > 0; i = i >> 1) {
                        if ((value & i) != 0) {
                            decimal_binary += "1";
                        } else {
                            decimal_binary += "0";
                        }
                    }
                    sourcePoint += 3;
                }

                if (decimal_binary.length() >= 24) {
                    /* Binary buffer is full - transfer to data */
                    for(i = 0; i < 8; i++) {
                        if (decimal_binary.charAt(i) == '1') {
                            data[targetPoint] += 128 >> i;
                        }

                        if (decimal_binary.charAt(8 + i) == '1') {
                            data[targetPoint + 1] += 128 >> i;
                        }

                        if (decimal_binary.charAt(16 + i) == '1') {
                            data[targetPoint + 2] += 128 >> i;
                        }

                    }
                    targetPoint += 3;

                    if (decimal_binary.length() > 24) {
                        decimal_binary = decimal_binary.substring(24);
                    }
                }
            }

            if (current_mode == Mode.C1_BYTE) {
                next_mode = Mode.C1_BYTE;

                if (inputData[sourcePoint] == FNC1) {
                    next_mode = Mode.C1_ASCII;
                } else {
                    if (inputData[sourcePoint] <= 127) {
                        next_mode = lookAheadTest(length, sourcePoint, current_mode);
                    }
                }

                if (next_mode != Mode.C1_BYTE) {
                    /* Insert byte field length */
                    if ((targetPoint - byte_start) <= 249) {
                        for (i = targetPoint; i >= byte_start; i--) {
                            data[i + 1] = data[i];
                        }
                        data[byte_start] = (targetPoint - byte_start);
                        targetPoint++;
                    } else {
                        for (i = targetPoint; i >= byte_start; i--) {
                            data[i + 2] = data[i];
                        }
                        data[byte_start] = 249 + ((targetPoint - byte_start) / 250);
                        data[byte_start + 1] = ((targetPoint - byte_start) % 250);
                        targetPoint += 2;
                    }
                } else {
                    data[targetPoint] = inputData[sourcePoint];
                    targetPoint++;
                    sourcePoint++;
                }
            }

            if (targetPoint > 1480) {
                /* Data is too large for symbol */
                throw OkapiInputException.inputTooLong();
            }

        } while (sourcePoint < length);

        /* Empty buffers */
        if (c40_p == 2) {
            int iv;

            c40_buffer[2] = 1;
            iv = (1600 * c40_buffer[0]) + (40 * c40_buffer[1])
                    + (c40_buffer[2]) + 1;
            data[targetPoint] = iv / 256;
            targetPoint++;
            data[targetPoint] = iv % 256;
            targetPoint++;
            data[targetPoint] = 255;
            targetPoint++; /* Unlatch */
        }
        if (c40_p == 1) {
            int iv;

            c40_buffer[1] = 1;
            c40_buffer[2] = 31; /* Pad */
            iv = (1600 * c40_buffer[0]) + (40 * c40_buffer[1])
                    + (c40_buffer[2]) + 1;
            data[targetPoint] = iv / 256;
            targetPoint++;
            data[targetPoint] = iv % 256;
            targetPoint++;
            data[targetPoint] = 255;
            targetPoint++; /* Unlatch */
        }
        if (text_p == 2) {
            int iv;

            text_buffer[2] = 1;
            iv = (1600 * text_buffer[0]) + (40 * text_buffer[1])
                    + (text_buffer[2]) + 1;
            data[targetPoint] = iv / 256;
            targetPoint++;
            data[targetPoint] = iv % 256;
            targetPoint++;
            data[targetPoint] = 255;
            targetPoint++; /* Unlatch */
        }
        if (text_p == 1) {
            int iv;

            text_buffer[1] = 1;
            text_buffer[2] = 31; /* Pad */
            iv = (1600 * text_buffer[0]) + (40 * text_buffer[1])
                    + (text_buffer[2]) + 1;
            data[targetPoint] = iv / 256;
            targetPoint++;
            data[targetPoint] = iv % 256;
            targetPoint++;
            data[targetPoint] = 255;
            targetPoint++; /* Unlatch */
        }

        if (current_mode == Mode.C1_DECIMAL) {
            /* Finish Decimal mode and go back to ASCII */

            decimal_binary += "111111"; /* Unlatch */

            target_count = 3;
            if (decimal_binary.length() <= 16) {
                target_count = 2;
            }
            if (decimal_binary.length() <= 8) {
                target_count = 1;
            }
            bits_left_in_byte = (8 * target_count) - decimal_binary.length();
            if (bits_left_in_byte == 8) {
                bits_left_in_byte = 0;
            }

            if (bits_left_in_byte == 2) {
                decimal_binary += "01";
            }

            if ((bits_left_in_byte == 4) || (bits_left_in_byte == 6)) {
                decimal_binary += "1111";
            }

            if (bits_left_in_byte == 6) {
                decimal_binary += "01";
            }

            /* Binary buffer is full - transfer to data */
            if (target_count >= 1) {
                for(i = 0; i < 8; i++) {
                    if(decimal_binary.charAt(i) == '1') {
                        data[targetPoint] += 128 >> i;
                    }
                }
                targetPoint++;
            }
            if (target_count >= 2) {
                for(i = 0; i < 8; i++) {
                    if(decimal_binary.charAt(8 + i) == '1') {
                        data[targetPoint] += 128 >> i;
                    }
                }
                targetPoint++;
            }
            if (target_count == 3) {
                for(i = 0; i < 8; i++) {
                    if(decimal_binary.charAt(16 + i) == '1') {
                        data[targetPoint] += 128 >> i;
                    }
                }
                targetPoint++;
            }
        }

        if (current_mode == Mode.C1_BYTE) {
            /* Insert byte field length */
            if ((targetPoint - byte_start) <= 249) {
                for (i = targetPoint; i >= byte_start; i--) {
                    data[i + 1] = data[i];
                }
                data[byte_start] = (targetPoint - byte_start);
                targetPoint++;
            } else {
                for (i = targetPoint; i >= byte_start; i--) {
                    data[i + 2] = data[i];
                }
                data[byte_start] = 249 + ((targetPoint - byte_start) / 250);
                data[byte_start + 1] = ((targetPoint - byte_start) % 250);
                targetPoint += 2;
            }
        }

        /* Re-check length of data */
        if (targetPoint > 1480) {
            /* Data is too large for symbol */
            throw OkapiInputException.inputTooLong();
        }

        return targetPoint;
    }

    private Mode lookAheadTest(int sourcelen, int position,
            Mode current_mode) {
        double ascii_count, c40_count, text_count, edi_count, byte_count;
        int reduced_char;
        int done, best_count, sp;
        Mode best_scheme;

        /* Step J */
        if (current_mode == Mode.C1_ASCII) {
            ascii_count = 0.0;
            c40_count = 1.0;
            text_count = 1.0;
            edi_count = 1.0;
            byte_count = 2.0;
        } else {
            ascii_count = 1.0;
            c40_count = 2.0;
            text_count = 2.0;
            edi_count = 2.0;
            byte_count = 3.0;
        }

        switch (current_mode) {
        case C1_C40:
            c40_count = 0.0;
            break;
        case C1_TEXT:
            text_count = 0.0;
            break;
        case C1_BYTE:
            byte_count = 0.0;
            break;
        case C1_EDI:
            edi_count = 0.0;
            break;
        }

        for (sp = position;
        (sp < sourcelen) && (sp <= (position + 8)); sp++) {

            if (inputData[sp] <= 127) {
                reduced_char = inputData[sp];
            } else {
                reduced_char = inputData[sp] - 127;
            }

            /* Step L */
            if ((inputData[sp] >= '0') && (inputData[sp] <= '9')) {
                ascii_count += 0.5;
            } else {
                ascii_count = roundUpToNextInteger(ascii_count);
                if (inputData[sp] > 127) {
                    ascii_count += 2.0;
                } else {
                    ascii_count += 1.0;
                }
            }

            /* Step M */
            done = 0;
            if (reduced_char == ' ') {
                c40_count += (2.0 / 3.0);
                done = 1;
            }
            if ((reduced_char >= '0') && (reduced_char <= '9')) {
                c40_count += (2.0 / 3.0);
                done = 1;
            }
            if ((reduced_char >= 'A') && (reduced_char <= 'Z')) {
                c40_count += (2.0 / 3.0);
                done = 1;
            }
            if (inputData[sp] > 127) {
                c40_count += (4.0 / 3.0);
            }
            if (done == 0) {
                c40_count += (4.0 / 3.0);
            }

            /* Step N */
            done = 0;
            if (reduced_char == ' ') {
                text_count += (2.0 / 3.0);
                done = 1;
            }
            if ((reduced_char >= '0') && (reduced_char <= '9')) {
                text_count += (2.0 / 3.0);
                done = 1;
            }
            if ((reduced_char >= 'a') && (reduced_char <= 'z')) {
                text_count += (2.0 / 3.0);
                done = 1;
            }
            if (inputData[sp] > 127) {
                text_count += (4.0 / 3.0);
            }
            if (done == 0) {
                text_count += (4.0 / 3.0);
            }

            /* Step O */
            done = 0;
            if (inputData[sp] == 13) {
                edi_count += (2.0 / 3.0);
                done = 1;
            }
            if (inputData[sp] == '*') {
                edi_count += (2.0 / 3.0);
                done = 1;
            }
            if (inputData[sp] == '>') {
                edi_count += (2.0 / 3.0);
                done = 1;
            }
            if (inputData[sp] == ' ') {
                edi_count += (2.0 / 3.0);
                done = 1;
            }
            if ((inputData[sp] >= '0') && (inputData[sp] <= '9')) {
                edi_count += (2.0 / 3.0);
                done = 1;
            }
            if ((inputData[sp] >= 'A') && (inputData[sp] <= 'Z')) {
                edi_count += (2.0 / 3.0);
                done = 1;
            }
            if (inputData[sp] > 127) {
                edi_count += (13.0 / 3.0);
            } else {
                if (done == 0) {
                    edi_count += (10.0 / 3.0);
                }
            }

            /* Step P */
            if (inputData[sp] == FNC1) {
                byte_count += 3.0;
            } else {
                byte_count += 1.0;
            }

        }

        ascii_count = roundUpToNextInteger(ascii_count);
        c40_count = roundUpToNextInteger(c40_count);
        text_count = roundUpToNextInteger(text_count);
        edi_count = roundUpToNextInteger(edi_count);
        byte_count = roundUpToNextInteger(byte_count);
        best_scheme = Mode.C1_ASCII;

        if (sp == sourcelen) {
            /* Step K */
            best_count = (int) edi_count;

            if (text_count <= best_count) {
                best_count = (int) text_count;
                best_scheme = Mode.C1_TEXT;
            }

            if (c40_count <= best_count) {
                best_count = (int) c40_count;
                best_scheme = Mode.C1_C40;
            }

            if (ascii_count <= best_count) {
                best_count = (int) ascii_count;
                best_scheme = Mode.C1_ASCII;
            }

            if (byte_count <= best_count) {
                best_scheme = Mode.C1_BYTE;
            }
        } else {
            /* Step Q */

            if (((edi_count + 1.0 <= ascii_count)
                    && (edi_count + 1.0 <= c40_count))
                    && ((edi_count + 1.0 <= byte_count)
                    && (edi_count + 1.0 <= text_count))) {
                best_scheme = Mode.C1_EDI;
            }

            if ((c40_count + 1.0 <= ascii_count)
                    && (c40_count + 1.0 <= text_count)) {

                if (c40_count < edi_count) {
                    best_scheme = Mode.C1_C40;
                } else {
                    if (c40_count == edi_count) {
                        if (preferEdi(sourcelen, position)) {
                            best_scheme = Mode.C1_EDI;
                        } else {
                            best_scheme = Mode.C1_C40;
                        }
                    }
                }
            }

            if (((text_count + 1.0 <= ascii_count)
                    && (text_count + 1.0 <= c40_count))
                    && ((text_count + 1.0 <= byte_count)
                    && (text_count + 1.0 <= edi_count))) {
                best_scheme = Mode.C1_TEXT;
            }

            if (((ascii_count + 1.0 <= byte_count)
                    && (ascii_count + 1.0 <= c40_count))
                    && ((ascii_count + 1.0 <= text_count)
                    && (ascii_count + 1.0 <= edi_count))) {
                best_scheme = Mode.C1_ASCII;
            }

            if (((byte_count + 1.0 <= ascii_count)
                    && (byte_count + 1.0 <= c40_count))
                    && ((byte_count + 1.0 <= text_count)
                    && (byte_count + 1.0 <= edi_count))) {
                best_scheme = Mode.C1_BYTE;
            }
        }

        return best_scheme;
    }

    private double roundUpToNextInteger(double input) {
        double fraction, output;

        fraction = input - (int) input;
        if (fraction > 0.01) {
            output = (input - fraction) + 1.0;
        } else {
            output = input;
        }

        return output;
    }

    private boolean preferEdi(int sourcelen, int position) {
        int i;

        for (i = position; isEdiEncodable(inputData[position + i])
                && ((position + i) < sourcelen); i++);

        if ((position + i) == sourcelen) {
            /* Reached end of input */
            return false;
        }

        if (inputData[position + i - 1] == 13) {
            return true;
        }
        if (inputData[position + i - 1] == '*') {
            return true;
        }
        if (inputData[position + i - 1] == '>') {
            return true;
        }

        return false;
    }

    private boolean isEdiEncodable(int input) {
        boolean result = false;

        if (input == 13) {
            result = true;
        }
        if (input == '*') {
            result = true;
        }
        if (input == '>') {
            result = true;
        }
        if (input == ' ') {
            result = true;
        }
        if ((input >= '0') && (input <= '9')) {
            result = true;
        }
        if ((input >= 'A') && (input <= 'Z')) {
            result = true;
        }

        return result;
    }

    private void plotCentralFinder(int start_row, int row_count, int full_rows) {
        for (int i = 0; i < row_count; i++) {
            if (i < full_rows) {
                plotHorizontalBar(start_row + (i * 2), 1);
            } else {
                plotHorizontalBar(start_row + (i * 2), 0);
                if (i != row_count - 1) {
                    setGridModule(start_row + (i * 2) + 1, 1);
                    setGridModule(start_row + (i * 2) + 1, symbol_width - 2);
                }
            }
        }
    }

    private void plotHorizontalBar(int row_no, int full) {
        if (full != 0) {
            for (int i = 0; i < symbol_width; i++) {
                setGridModule(row_no, i);
            }
        } else {
            for (int i = 1; i < symbol_width - 1; i++) {
                setGridModule(row_no, i);
            }
        }
    }

    private void plotVerticalBar(int column, int height, int top) {
        if (top != 0) {
            for (int i = 0; i < height; i++) {
                setGridModule(i, column);
            }
        } else {
            for (int i = 0; i < height; i++) {
                setGridModule(row_count - i - 1, column);
            }
        }
    }

    private void plotSpigot(int row_no) {
        for (int i = symbol_width - 1; i > 0; i--) {
            if (outputGrid[row_no][i - 1]) {
                setGridModule(row_no, i);
            }
        }
    }

    private void plotDataBlock(int start_row, int start_col, int height, int width, int row_offset, int col_offset) {
        for (int i = start_row; i < (start_row + height); i++) {
            for (int j = start_col; j < (start_col + width); j++) {
                if (datagrid[i][j] == '1') {
                    setGridModule(i + row_offset, j + col_offset);
                }
            }
        }
    }

    private void setGridModule(int row, int column) {
        outputGrid[row][column] = true;
    }

    private void resetGridModule(int row, int column) {
        outputGrid[row][column] = false;
    }

    private static int getSize(Version version) {
        switch(version) {
            case A:
                return 1;
            case B:
                return 2;
            case C:
                return 3;
            case D:
                return 4;
            case E:
                return 5;
            case F:
                return 6;
            case G:
                return 7;
            case H:
                return 8;
            default:
                return 0;
        }
    }
}
