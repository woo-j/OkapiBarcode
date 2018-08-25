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
 * Encodes add-on barcodes for UPC/EAN.
 *
 * @author <a href="mailto:rstuart114@gmail.com">Robin Stuart</a>
 */
public final class AddOn {

    private static final String[] EAN_SET_A = {
        "3211", "2221", "2122", "1411", "1132", "1231", "1114", "1312", "1213",
        "3112"
    };

    private static final String[] EAN_SET_B = {
        "1123", "1222", "2212", "1141", "2311", "1321", "4111", "2131", "3121",
        "2113"
    };

    private static final String[] EAN2_PARITY = {
        "AA", "AB", "BA", "BB"
    };

    private static final String[] EAN5_PARITY = {
        "BBAAA", "BABAA", "BAABA", "BAAAB", "ABBAA", "AABBA", "AAABB", "ABABA",
        "ABAAB", "AABAB"
    };

    private AddOn() {
        // utility class, cannot instantiate
    }

    public static String calcAddOn(String content) {

        if (!content.matches("[0-9]{1,5}")) {
            return "";
        }

        if (content.length() > 2) {
            return ean5(content);
        } else {
            return ean2(content);
        }
    }

    private static String ean2(String content) {

        StringBuilder accumulator = new StringBuilder(2);
        for (int i = content.length(); i < 2; i++) {
            accumulator.append('0');
        }
        accumulator.append(content);

        int sum = ((accumulator.charAt(0) - '0') * 10) + (accumulator.charAt(1) - '0');
        String parity = EAN2_PARITY[sum % 4];

        StringBuilder sb = new StringBuilder();
        sb.append("112"); /* Start */
        for (int i = 0; i < 2; i++) {
            int val = Character.getNumericValue(accumulator.charAt(i));
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

        StringBuilder accumulator = new StringBuilder(5);
        for (int i = content.length(); i < 5; i++) {
            accumulator.append('0');
        }
        accumulator.append(content);

        int sum = 0;
        for (int i = 0; i < 5; i++) {
            if ((i % 2) == 0) {
                sum += 3 * (accumulator.charAt(i) - '0');
            } else {
                sum += 9 * (accumulator.charAt(i) - '0');
            }
        }
        String parity = EAN5_PARITY[sum % 10];

        StringBuilder sb = new StringBuilder();
        sb.append("112"); /* Start */
        for (int i = 0; i < 5; i++) {
            int val = Character.getNumericValue(accumulator.charAt(i));
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
