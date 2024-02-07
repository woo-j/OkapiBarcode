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
 * <p>Implements Code 32, also known as Italian Pharmacode, A variation of Code
 * 39 used by the Italian Ministry of Health ("Ministero della Sanità")
 *
 * <p>Requires a numeric input up to 8 digits in length. Check digit is
 * calculated.
 *
 * @author <a href="mailto:rstuart114@gmail.com">Robin Stuart</a>
 */
public class Code32 extends Symbol {

    private static final char[] TABLE = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'B', 'C', 'D', 'F',
        'G', 'H', 'J', 'K', 'L', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V',
        'W', 'X', 'Y', 'Z'
    };

    @Override
    protected void encode() {

        if (content.length() > 8) {
            throw OkapiInputException.inputTooLong();
        }

        if (!content.matches("[0-9]+")) {
            throw OkapiInputException.invalidCharactersInInput();
        }

        /* Add leading zeros as required */
        String localstr = "";
        for (int i = content.length(); i < 8; i++) {
            localstr += "0";
        }
        localstr += content;

        /* Calculate the check digit */
        int checksum = 0;
        int checkpart = 0;
        for (int i = 0; i < 4; i++) {
            checkpart = Character.getNumericValue(localstr.charAt(i * 2));
            checksum += checkpart;
            checkpart = 2 * Character.getNumericValue(localstr.charAt((i * 2) + 1));
            if (checkpart >= 10) {
                checksum += (checkpart - 10) + 1;
            } else {
                checksum += checkpart;
            }
        }

        /* Add check digit to data string */
        int checkdigit = checksum % 10;
        char check = (char) (checkdigit + '0');
        localstr += check;
        infoLine("Check Digit: " + check);

        /* Convert string into an integer value */
        int pharmacode = 0;
        for (int i = 0; i < localstr.length(); i++) {
            pharmacode *= 10;
            pharmacode += Character.getNumericValue(localstr.charAt(i));
        }

        /* Convert from decimal to base-32 */
        int devisor = 33554432;
        int[] codeword = new int[6];
        for (int i = 5; i >= 0; i--) {
            codeword[i] = pharmacode / devisor;
            int remainder = pharmacode % devisor;
            pharmacode = remainder;
            devisor /= 32;
        }

        /* Look up values in 'Tabella di conversione' */
        String risultante = "";
        for (int i = 5; i >= 0; i--) {
            risultante += TABLE[codeword[i]];
        }

        /* Plot the barcode using Code 39 */

        readable = "A" + localstr;
        pattern = new String[1];
        row_count = 1;
        row_height = new int[] { -1 };
        infoLine("Code 39 Equivalent: " + risultante);

        Code3Of9 c39 = new Code3Of9();
        c39.setContent(risultante);
        pattern[0] = c39.pattern[0];
    }
}
