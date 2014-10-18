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
 * Implements Code 32 (AKA Italian Pharmacode)
 * 
 * @author Robin Stuart <rstuart114@gmail.com>
 * @version 0.1
 */
public class Code32 extends Symbol {
    private char[] tabella = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'B', 'C', 'D', 'F', 
        'G', 'H', 'J', 'K', 'L', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 
        'W', 'X', 'Y', 'Z'
    };

    @Override
    public boolean encode() {
        int i, checksum, checkpart, checkdigit;
        int pharmacode, remainder, devisor;
        String localstr, risultante;
        int[] codeword = new int[6];
        Code3Of9 c39 = new Code3Of9();

        if (content.length() > 8) {
            error_msg = "Input too long";
            return false;
        }

        if (!(content.matches("[0-9]+?"))) {
            error_msg = "Invalid characters in input";
            return false;
        }

        /* Add leading zeros as required */
        localstr = "";
        for (i = content.length(); i < 8; i++) {
            localstr += "0";
        }
        localstr += content;

        /* Calculate the check digit */
        checksum = 0;
        checkpart = 0;
        for (i = 0; i < 4; i++) {
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
        checkdigit = checksum % 10;
        localstr += (char)(checkdigit + '0');
        encodeInfo += "Check Digit: " + (char)(checkdigit + '0');
        encodeInfo += '\n';

        /* Convert string into an integer value */
        pharmacode = 0;
        for (i = 0; i < localstr.length(); i++) {
            pharmacode *= 10;
            pharmacode += Character.getNumericValue(localstr.charAt(i));
        }

        /* Convert from decimal to base-32 */
        devisor = 33554432;
        for (i = 5; i >= 0; i--) {
            codeword[i] = pharmacode / devisor;
            remainder = pharmacode % devisor;
            pharmacode = remainder;
            devisor /= 32;
        }

        /* Look up values in 'Tabella di conversione' */
        risultante = "";
        for (i = 5; i >= 0; i--) {
            risultante += tabella[codeword[i]];
        }

        /* Plot the barcode using Code 39 */

        readable = "A" + localstr;
        pattern = new String[1];
        row_count = 1;
        row_height = new int[1];
        row_height[0] = -1;
        if (debug) {
            System.out.println("Encoded: " + risultante);
        }
        encodeInfo += "Code 39 Equivalent: " + risultante + '\n';
        if (c39.setContent(risultante)) {
            this.pattern[0] = c39.pattern[0];
            this.plotSymbol();
        } else {
            error_msg = c39.error_msg;
        }
        return true;
    }
}
