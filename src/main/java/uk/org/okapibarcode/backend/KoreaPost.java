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
 * <p>Implements Korea Post Barcode. Input should consist of of a six-digit number. A Modulo-10
 * check digit is calculated and added, and should not form part of the input data.
 *
 * @author <a href="mailto:rstuart114@gmail.com">Robin Stuart</a>
 */
public class KoreaPost extends Symbol {

    private static final String[] KOREA_TABLE = {
        "1313150613", "0713131313", "0417131313", "1506131313", "0413171313",
        "17171313", "1315061313", "0413131713", "17131713", "13171713"
    };

    @Override
    public boolean encode() {

        if (!content.matches("[0-9]+")) {
            error_msg = "Invalid characters in input";
            return false;
        }

        if (content.length() > 6) {
            error_msg = "Input data too long";
            return false;
        }

        String padded = "";
        for (int i = 0; i < (6 - content.length()); i++) {
            padded += "0";
        }
        padded += content;

        int total = 0;
        String accumulator = "";
        for (int i = 0; i < padded.length(); i++) {
            int j = Character.getNumericValue(padded.charAt(i));
            accumulator += KOREA_TABLE[j];
            total += j;
        }

        int checkd = 10 - (total % 10);
        if (checkd == 10) {
            checkd = 0;
        }
        encodeInfo += "Check Digit: " + checkd + "\n";
        accumulator += KOREA_TABLE[checkd];

        readable = padded + checkd;
        pattern = new String[] { accumulator };
        row_count = 1;
        row_height = new int[] { -1 };

        plotSymbol();

        return true;
    }
}
