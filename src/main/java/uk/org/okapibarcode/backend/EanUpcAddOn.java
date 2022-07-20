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
 * <p>Implements EAN/UPC add-on bar code symbology according to BS EN 797:1996.
 *
 * @see Ean
 * @see Upc
 * @author <a href="mailto:rstuart114@gmail.com">Robin Stuart</a>
 */
public class EanUpcAddOn extends Symbol {

    private static final String[] EAN_SET_A = {
        "3211", "2221", "2122", "1411", "1132", "1231", "1114", "1312", "1213", "3112"
    };

    private static final String[] EAN_SET_B = {
        "1123", "1222", "2212", "1141", "2311", "1321", "4111", "2131", "3121", "2113"
    };

    private static final String[] EAN2_PARITY = {
        "AA", "AB", "BA", "BB"
    };

    private static final String[] EAN5_PARITY = {
        "BBAAA", "BABAA", "BAABA", "BAAAB", "ABBAA", "AABBA", "AAABB", "ABABA", "ABAAB", "AABAB"
    };

    @Override
    protected void encode() {

        if (!content.matches("[0-9]+")) {
            throw new OkapiException("Invalid characters in data");
        }

        if (content.length() > 5) {
            throw new OkapiException("Input data too long");
        }

        int targetLength = (content.length() > 2 ? 5 : 2);

        if (content.length() < targetLength) {
            for (int i = content.length(); i < targetLength; i++) {
                content = '0' + content;
            }
        }

        String bars = (targetLength == 2 ? ean2(content) : ean5(content));

        readable = content;
        pattern = new String[] { bars };
        row_count = 1;
        row_height = new int[] { -1 };
    }

    private static String ean2(String content) {

        int sum = ((content.charAt(0) - '0') * 10) + (content.charAt(1) - '0');
        String parity = EAN2_PARITY[sum % 4];

        StringBuilder sb = new StringBuilder();
        sb.append("112"); /* Start */
        for (int i = 0; i < 2; i++) {
            int val = content.charAt(i) - '0';
            if (parity.charAt(i) == 'B') {
                sb.append(EAN_SET_B[val]);
            } else {
                sb.append(EAN_SET_A[val]);
            }
            if (i != 1) { /* Glyph separator */
                sb.append("11");
            }
        }

        return sb.toString();
    }

    private static String ean5(String content) {

        int sum = 0;
        for (int i = 0; i < 5; i++) {
            if (i % 2 == 0) {
                sum += 3 * (content.charAt(i) - '0');
            } else {
                sum += 9 * (content.charAt(i) - '0');
            }
        }
        String parity = EAN5_PARITY[sum % 10];

        StringBuilder sb = new StringBuilder();
        sb.append("112"); /* Start */
        for (int i = 0; i < 5; i++) {
            int val = content.charAt(i) - '0';
            if (parity.charAt(i) == 'B') {
                sb.append(EAN_SET_B[val]);
            } else {
                sb.append(EAN_SET_A[val]);
            }
            if (i != 4) { /* Glyph separator */
                sb.append("11");
            }
        }

        return sb.toString();
    }
}
