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
 * Implements Code 11 bar code symbology
 *
 * @author Robin Stuart <rstuart114@gmail.com>
 * @version 0.2
 */
public class Code11 extends Symbol {
    private String[] code11Table = {
        "111121", "211121", "121121", "221111", "112121", "212111", "122111",
            "111221", "211211", "211111", "112111"
    };

    private char characterSet[] = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-'
    };

    @Override
    public boolean encode() {
        if (!(content.matches("[0-9-]+?"))) {
            error_msg = "Invalid characters in input";
            return false;
        }

        String horizontalSpacing;
        int i;
        int length = content.length();
        int checkDigitC, weightC = 1, countC = 0, checkDigitK, weightK = 1, countK = 0;
        int[] weight = new int[128];
        char thisCharacter;

        horizontalSpacing = "112211";
        for (i = 0; i < length; i++) {
            thisCharacter = content.charAt(i);
            weight[i] = positionOf(thisCharacter, characterSet);
            horizontalSpacing += code11Table[weight[i]];
        }

        /* Calculate C checksum */
        for (i = length - 1; i >= 0; i--) {
            countC += (weightC * weight[i]);
            weightC++;

            if (weightC > 10) {
                weightC = 1;
            }
        }
        checkDigitC = countC % 11;

        encodeInfo += "Check Digit C: " + checkDigitC + "\n";

        weight[length] = checkDigitC;

        /* Calculate K checksum */
        for (i = length; i >= 0; i--) {
            countK += (weightK * weight[i]);
            weightK++;

            if (weightK > 9) {
                weightK = 1;
            }
        }
        checkDigitK = countK % 11;

        encodeInfo += "Check Digit K: " + checkDigitK + "\n";

        horizontalSpacing += code11Table[checkDigitC];
        horizontalSpacing += code11Table[checkDigitK];
        horizontalSpacing += "11221";


        readable = content + (char)(checkDigitC + '0') + (char)(checkDigitK + '0');
        pattern = new String[1];
        pattern[0] = horizontalSpacing;
        row_count = 1;
        row_height = new int[1];
        row_height[0] = -1;
        plotSymbol();
        return true;
    }
}
