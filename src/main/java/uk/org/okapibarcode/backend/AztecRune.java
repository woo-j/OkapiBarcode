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
 * <p>Implements the Aztec Runes bar code symbology according to ISO/IEC 24778:2008 Annex A.
 *
 * <p>Aztec Runes is a fixed-size matrix symbology which can encode whole integer values between 0 and 255.
 *
 * @author <a href="mailto:rstuart114@gmail.com">Robin Stuart</a>
 */
public class AztecRune extends Symbol {

    private static final int[] BIT_PLACEMENT_MAP = {
         1, 1, 2, 3, 4, 5, 6, 7, 8, 0, 1,
         1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        29, 1, 0, 0, 0, 0, 0, 0, 0, 1, 9,
        28, 1, 0, 1, 1, 1, 1, 1, 0, 1, 10,
        27, 1, 0, 1, 0, 0, 0, 1, 0, 1, 11,
        26, 1, 0, 1, 0, 1, 0, 1, 0, 1, 12,
        25, 1, 0, 1, 0, 0, 0, 1, 0, 1, 13,
        24, 1, 0, 1, 1, 1, 1, 1, 0, 1, 14,
        23, 1, 0, 0, 0, 0, 0, 0, 0, 1, 15,
         0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
         0, 0, 22, 21, 20, 19, 18, 17, 16, 0, 0
    };

    @Override
    protected void encode() {

        if (!content.matches("[0-9]+")) {
            throw OkapiInputException.invalidCharactersInInput();
        }

        int decimalValue = 0;
        switch (content.length()) {
            case 1:
                decimalValue = (content.charAt(0) - '0');
                break;
            case 2:
                decimalValue = 10 * (content.charAt(0) - '0');
                decimalValue += (content.charAt(1) - '0');
                break;
            case 3:
                decimalValue = 100 * (content.charAt(0) - '0');
                decimalValue += 10 * (content.charAt(1) - '0');
                decimalValue += (content.charAt(2) - '0');
                break;
            default:
                throw new OkapiInputException("Input too large");
        }

        if (decimalValue > 255) {
            throw new OkapiInputException("Input too large");
        }

        StringBuilder binaryDataStream = new StringBuilder(28);
        for (int i = 0x80; i > 0; i = i >> 1) {
            if ((decimalValue & i) != 0) {
                binaryDataStream.append('1');
            } else {
                binaryDataStream.append('0');
            }
        }

        int[] dataCodeword = new int[3];
        dataCodeword[0] = 0;
        dataCodeword[1] = 0;

        for (int i = 0; i < 2; i++) {
            if (binaryDataStream.charAt(i * 4) == '1') {
                dataCodeword[i] += 8;
            }
            if (binaryDataStream.charAt((i * 4) + 1) == '1') {
                dataCodeword[i] += 4;
            }
            if (binaryDataStream.charAt((i * 4) + 2) == '1') {
                dataCodeword[i] += 2;
            }
            if (binaryDataStream.charAt((i * 4) + 3) == '1') {
                dataCodeword[i] += 1;
            }
        }

        int[] errorCorrectionCodeword = new int[6];

        ReedSolomon rs = new ReedSolomon();
        rs.init_gf(0x13);
        rs.init_code(5, 1);
        rs.encode(2, dataCodeword);

        for (int i = 0; i < 5; i++) {
            errorCorrectionCodeword[i] = rs.getResult(i);
        }

        for (int i = 0; i < 5; i++) {
            if ((errorCorrectionCodeword[4 - i] & 0x08) != 0) {
                binaryDataStream.append('1');
            } else {
                binaryDataStream.append('0');
            }
            if ((errorCorrectionCodeword[4 - i] & 0x04) != 0) {
                binaryDataStream.append('1');
            } else {
                binaryDataStream.append('0');
            }
            if ((errorCorrectionCodeword[4 - i] & 0x02) != 0) {
                binaryDataStream.append('1');
            } else {
                binaryDataStream.append('0');
            }
            if ((errorCorrectionCodeword[4 - i] & 0x01) != 0) {
                binaryDataStream.append('1');
            } else {
                binaryDataStream.append('0');
            }
        }

        StringBuilder reversedBinaryDataStream = new StringBuilder(28);
        for (int i = 0; i < binaryDataStream.length(); i++) {
            if ((i & 1) == 0) {
                if (binaryDataStream.charAt(i) == '0') {
                    reversedBinaryDataStream.append('1');
                } else {
                    reversedBinaryDataStream.append('0');
                }
            } else {
                reversedBinaryDataStream.append(binaryDataStream.charAt(i));
            }
        }

        infoLine("Binary: " + reversedBinaryDataStream);

        readable = "";
        pattern = new String[11];
        row_count = 11;
        row_height = new int[11];

        for (int row = 0; row < 11; row++) {
            StringBuilder rowBinary = new StringBuilder(11);
            for (int column = 0; column < 11; column++) {
                if (BIT_PLACEMENT_MAP[(row * 11) + column] == 1) {
                    rowBinary.append('1');
                }
                if (BIT_PLACEMENT_MAP[(row * 11) + column] == 0) {
                    rowBinary.append('0');
                }
                if (BIT_PLACEMENT_MAP[(row * 11) + column] >= 2) {
                    rowBinary.append(reversedBinaryDataStream.charAt(BIT_PLACEMENT_MAP[(row * 11) + column] - 2));
                }
            }
            pattern[row] = bin2pat(rowBinary);
            row_height[row] = 1;
        }
    }
}
