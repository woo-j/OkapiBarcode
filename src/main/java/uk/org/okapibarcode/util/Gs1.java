/*
 * Copyright 2018 Robin Stuart, Daniel Gredler
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

package uk.org.okapibarcode.util;

import uk.org.okapibarcode.backend.OkapiException;

/**
 * GS1 utility class.
 */
public final class Gs1 {

    private Gs1() {
        // utility class
    }

    /**
     * Verifies that the specified data is in good GS1 format <tt>"[AI]data"</tt> pairs, and returns a reduced
     * version of the input string containing FNC1 escape sequences instead of AI brackets. With a few small
     * exceptions, this code matches the Zint GS1 validation code as closely as possible, in order to make it
     * easier to keep in sync.
     *
     * @param s the data string to verify
     * @param fnc1 the string to use to represent FNC1 in the output
     * @return the input data, verified and with FNC1 strings added at the appropriate positions
     * @see <a href="https://sourceforge.net/p/zint/code/ci/master/tree/backend/gs1.c">Corresponding Zint code</a>
     * @see <a href="http://www.gs1.org/docs/gsmp/barcodes/GS1_General_Specifications.pdf">GS1 specification</a>
     */
    public static String verify(String s, String fnc1) {

        // Enforce compliance with GS1 General Specification
        // http://www.gs1.org/docs/gsmp/barcodes/GS1_General_Specifications.pdf

        char[] source = s.toCharArray();
        StringBuilder reduced = new StringBuilder(source.length);
        int[] ai_value = new int[100];
        int[] ai_location = new int[100];
        int[] data_location = new int[100];
        int[] data_length = new int[100];
        int error_latch;

        /* Detect extended ASCII characters */
        for (int i = 0; i < source.length; i++) {
            if (source[i] >= 128) {
                throw new OkapiException("Extended ASCII characters are not supported by GS1");
            }
            if (source[i] < 32) {
                throw new OkapiException("Control characters are not supported by GS1");
            }
        }

        /* Make sure we start with an AI */
        if (source[0] != '[') {
            throw new OkapiException("Data does not start with an AI");
        }

        /* Check the position of the brackets */
        int bracket_level = 0;
        int max_bracket_level = 0;
        int ai_length = 0;
        int max_ai_length = 0;
        int min_ai_length = 5;
        int j = 0;
        boolean ai_latch = false;
        for (int i = 0; i < source.length; i++) {
            ai_length += j;
            if (((j == 1) && (source[i] != ']')) && ((source[i] < '0') || (source[i] > '9'))) {
                ai_latch = true;
            }
            if (source[i] == '[') {
                bracket_level++;
                j = 1;
            }
            if (source[i] == ']') {
                bracket_level--;
                if (ai_length < min_ai_length) {
                    min_ai_length = ai_length;
                }
                j = 0;
                ai_length = 0;
            }
            if (bracket_level > max_bracket_level) {
                max_bracket_level = bracket_level;
            }
            if (ai_length > max_ai_length) {
                max_ai_length = ai_length;
            }
        }
        min_ai_length--;

        if (bracket_level != 0) {
            /* Not all brackets are closed */
            throw new OkapiException("Malformed AI in input data (brackets don't match)");
        }

        if (max_bracket_level > 1) {
            /* Nested brackets */
            throw new OkapiException("Found nested brackets in input data");
        }

        if (max_ai_length > 4) {
            /* AI is too long */
            throw new OkapiException("Invalid AI in input data (AI too long)");
        }

        if (min_ai_length <= 1) {
            /* AI is too short */
            throw new OkapiException("Invalid AI in input data (AI too short)");
        }

        if (ai_latch) {
            /* Non-numeric data in AI */
            throw new OkapiException("Invalid AI in input data (non-numeric characters in AI)");
        }

        int ai_count = 0;
        for (int i = 1; i < source.length; i++) {
            if (source[i - 1] == '[') {
                ai_location[ai_count] = i;
                ai_value[ai_count] = 0;
                for (j = 0; source[i + j] != ']'; j++) {
                    ai_value[ai_count] *= 10;
                    ai_value[ai_count] += Character.getNumericValue(source[i + j]);
                }
                ai_count++;
            }
        }

        for (int i = 0; i < ai_count; i++) {
            data_location[i] = ai_location[i] + 3;
            if (ai_value[i] >= 100) {
                data_location[i]++;
            }
            if (ai_value[i] >= 1000) {
                data_location[i]++;
            }
            data_length[i] = source.length - data_location[i];
            for (j = source.length - 1; j >= data_location[i]; j--) {
                if (source[j] == '[') {
                    data_length[i] = j - data_location[i];
                }
            }
        }

        for (int i = 0; i < ai_count; i++) {
            if (data_length[i] == 0) {
                /* No data for given AI */
                throw new OkapiException("Empty data field in input data");
            }
        }

        // Check for valid AI values and data lengths according to GS1 General
        // Specification Release 18, January 2018
        for (int i = 0; i < ai_count; i++) {

            error_latch = 2;
            switch (ai_value[i]) {
                // Length 2 Fixed
                case 20: // VARIANT
                    if (data_length[i] != 2) {
                        error_latch = 1;
                    } else {
                        error_latch = 0;
                    }
                    break;

                // Length 3 Fixed
                case 422: // ORIGIN
                case 424: // COUNTRY PROCESS
                case 426: // COUNTRY FULL PROCESS
                    if (data_length[i] != 3) {
                        error_latch = 1;
                    } else {
                        error_latch = 0;
                    }
                    break;

                // Length 4 Fixed
                case 8111: // POINTS
                    if (data_length[i] != 4) {
                        error_latch = 1;
                    } else {
                        error_latch = 0;
                    }
                    break;

                // Length 6 Fixed
                case 11: // PROD DATE
                case 12: // DUE DATE
                case 13: // PACK DATE
                case 15: // BEST BY
                case 16: // SELL BY
                case 17: // USE BY
                case 7006: // FIRST FREEZE DATE
                case 8005: // PRICE PER UNIT
                    if (data_length[i] != 6) {
                        error_latch = 1;
                    } else {
                        error_latch = 0;
                    }
                    break;

                // Length 10 Fixed
                case 7003: // EXPIRY TIME
                    if (data_length[i] != 10) {
                        error_latch = 1;
                    } else {
                        error_latch = 0;
                    }
                    break;

                // Length 13 Fixed
                case 410: // SHIP TO LOC
                case 411: // BILL TO
                case 412: // PURCHASE FROM
                case 413: // SHIP FOR LOC
                case 414: // LOC NO
                case 415: // PAY TO
                case 416: // PROD/SERV LOC
                case 7001: // NSN
                    if (data_length[i] != 13) {
                        error_latch = 1;
                    } else {
                        error_latch = 0;
                    }
                    break;

                // Length 14 Fixed
                case 1: // GTIN
                case 2: // CONTENT
                case 8001: // DIMENSIONS
                    if (data_length[i] != 14) {
                        error_latch = 1;
                    } else {
                        error_latch = 0;
                    }
                    break;

                // Length 17 Fixed
                case 402: // GSIN
                    if (data_length[i] != 17) {
                        error_latch = 1;
                    } else {
                        error_latch = 0;
                    }
                    break;

                // Length 18 Fixed
                case 0: // SSCC
                case 8006: // ITIP
                case 8017: // GSRN PROVIDER
                case 8018: // GSRN RECIPIENT
                    if (data_length[i] != 18) {
                        error_latch = 1;
                    } else {
                        error_latch = 0;
                    }
                    break;

                // Length 2 Max
                case 7010: // PROD METHOD
                    if (data_length[i] > 2) {
                        error_latch = 1;
                    } else {
                        error_latch = 0;
                    }
                    break;

                // Length 3 Max
                case 427: // ORIGIN SUBDIVISION
                case 7008: // AQUATIC SPECIES
                    if (data_length[i] > 3) {
                        error_latch = 1;
                    } else {
                        error_latch = 0;
                    }
                    break;

                // Length 4 Max
                case 7004: // ACTIVE POTENCY
                    if (data_length[i] > 4) {
                        error_latch = 1;
                    } else {
                        error_latch = 0;
                    }
                    break;

                // Length 6 Max
                case 242: // MTO VARIANT
                    if (data_length[i] > 6) {
                        error_latch = 1;
                    } else {
                        error_latch = 0;
                    }
                    break;

                // Length 8 Max
                case 30: // VAR COUNT
                case 37: // COUNT
                    if (data_length[i] > 8) {
                        error_latch = 1;
                    } else {
                        error_latch = 0;
                    }
                    break;

                // Length 10 Max
                case 7009: // FISHING GEAR TYPE
                case 8019: // SRIN
                    if (data_length[i] > 10) {
                        error_latch = 1;
                    } else {
                        error_latch = 0;
                    }
                    break;

                // Length 12 Max
                case 7005: // CATCH AREA
                case 8011: // CPID SERIAL
                    if (data_length[i] > 12) {
                        error_latch = 1;
                    } else {
                        error_latch = 0;
                    }
                    break;

                // Length 20 Max
                case 10: // BATCH/LOT
                case 21: // SERIAL
                case 22: // CPV
                case 243: // PCN
                case 254: // GLN EXTENSION COMPONENT
                case 420: // SHIP TO POST
                case 7020: // REFURB LOT
                case 7021: // FUNC STAT
                case 7022: // REV STAT
                case 710: // NHRN PZN
                case 711: // NHRN CIP
                case 712: // NHRN CN
                case 713: // NHRN DRN
                case 714: // NHRN AIM
                case 8002: // CMT NO
                case 8012: // VERSION
                    if (data_length[i] > 20) {
                        error_latch = 1;
                    } else {
                        error_latch = 0;
                    }
                    break;

                // Length 25 Max
                case 8020: // REF NO
                    if (data_length[i] > 25) {
                        error_latch = 1;
                    } else {
                        error_latch = 0;
                    }
                    break;

                // Length 30 Max
                case 240: // ADDITIONAL ID
                case 241: // CUST PART NO
                case 250: // SECONDARY SERIAL
                case 251: // REF TO SOURCE
                case 400: // ORDER NUMBER
                case 401: // GINC
                case 403: // ROUTE
                case 7002: // MEAT CUT
                case 7023: // GIAI ASSEMBLY
                case 8004: // GIAI
                case 8010: // CPID
                case 8013: // BUDI-DI
                case 90: // INTERNAL
                    if (data_length[i] > 30) {
                        error_latch = 1;
                    } else {
                        error_latch = 0;
                    }
                    break;

                // Length 34 Max
                case 8007: // IBAN
                    if (data_length[i] > 34) {
                        error_latch = 1;
                    } else {
                        error_latch = 0;
                    }
                    break;

                // Length 70 Max
                case 8110: // Coupon code
                case 8112: // Paperless coupon code
                case 8200: // PRODUCT URL
                    if (data_length[i] > 70) {
                        error_latch = 1;
                    } else {
                        error_latch = 0;
                    }
                    break;

            }

            if (ai_value[i] == 253) { // GDTI
                if ((data_length[i] < 14) || (data_length[i] > 30)) {
                    error_latch = 1;
                } else {
                    error_latch = 0;
                }
            }

            if (ai_value[i] == 255) { // GCN
                if ((data_length[i] < 14) || (data_length[i] > 25)) {
                    error_latch = 1;
                } else {
                    error_latch = 0;
                }
            }

            if ((ai_value[i] >= 3100) && (ai_value[i] <= 3169)) {
                if (data_length[i] != 6) {
                    error_latch = 1;
                } else {
                    error_latch = 0;
                }
            }

            if ((ai_value[i] >= 3200) && (ai_value[i] <= 3379)) {
                if (data_length[i] != 6) {
                    error_latch = 1;
                } else {
                    error_latch = 0;
                }
            }

            if ((ai_value[i] >= 3400) && (ai_value[i] <= 3579)) {
                if (data_length[i] != 6) {
                    error_latch = 1;
                } else {
                    error_latch = 0;
                }
            }

            if ((ai_value[i] >= 3600) && (ai_value[i] <= 3699)) {
                if (data_length[i] != 6) {
                    error_latch = 1;
                } else {
                    error_latch = 0;
                }
            }

            if ((ai_value[i] >= 3900) && (ai_value[i] <= 3909)) { // AMOUNT
                if (data_length[i] > 15) {
                    error_latch = 1;
                } else {
                    error_latch = 0;
                }
            }

            if ((ai_value[i] >= 3910) && (ai_value[i] <= 3919)) { // AMOUNT
                if ((data_length[i] < 4) || (data_length[i] > 18)) {
                    error_latch = 1;
                } else {
                    error_latch = 0;
                }
            }

            if ((ai_value[i] >= 3920) && (ai_value[i] <= 3929)) { // PRICE
                if (data_length[i] > 15) {
                    error_latch = 1;
                } else {
                    error_latch = 0;
                }
            }

            if ((ai_value[i] >= 3930) && (ai_value[i] <= 3939)) { // PRICE
                if ((data_length[i] < 4) || (data_length[i] > 18)) {
                    error_latch = 1;
                } else {
                    error_latch = 0;
                }
            }

            if ((ai_value[i] >= 3940) && (ai_value[i] <= 3949)) { // PRCNT OFF
                if (data_length[i] != 4) {
                    error_latch = 1;
                } else {
                    error_latch = 0;
                }
            }

            if (ai_value[i] == 421) { // SHIP TO POST
                if ((data_length[i] < 4) || (data_length[i] > 12)) {
                    error_latch = 1;
                } else {
                    error_latch = 0;
                }
            }

            if ((ai_value[i] == 423) || (ai_value[i] == 425)) {
                // COUNTRY INITIAL PROCESS || COUNTRY DISASSEMBLY
                if ((data_length[i] < 4) || (data_length[i] > 15)) {
                    error_latch = 1;
                } else {
                    error_latch = 0;
                }
            }

            if (ai_value[i] == 7007) { // HARVEST DATE
                if ((data_length[i] < 6) || (data_length[i] > 12)) {
                    error_latch = 1;
                } else {
                    error_latch = 0;
                }
            }

            if ((ai_value[i] >= 7030) && (ai_value[i] <= 7039)) { // PROCESSOR #
                if ((data_length[i] < 4) || (data_length[i] > 30)) {
                    error_latch = 1;
                } else {
                    error_latch = 0;
                }
            }

            if (ai_value[i] == 8003) { // GRAI
                if ((data_length[i] < 15) || (data_length[i] > 30)) {
                    error_latch = 1;
                } else {
                    error_latch = 0;
                }
            }

            if (ai_value[i] == 8008) { // PROD TIME
                if ((data_length[i] < 9) || (data_length[i] > 12)) {
                    error_latch = 1;
                } else {
                    error_latch = 0;
                }
            }

            if ((ai_value[i] >= 91) && (ai_value[i] <= 99)) { // INTERNAL
                if (data_length[i] > 90) {
                    error_latch = 1;
                } else {
                    error_latch = 0;
                }
            }

            if (error_latch == 1) {
                throw new OkapiException("Invalid data length for AI");
            }

            if (error_latch == 2) {
                throw new OkapiException("Invalid AI value");
            }
        }

        /* Resolve AI data - put resulting string in 'reduced' */
        int last_ai = 0;
        boolean fixedLengthAI = true;
        for (int i = 0; i < source.length; i++) {
            if ((source[i] != '[') && (source[i] != ']')) {
                reduced.append(source[i]);
            }
            if (source[i] == '[') {
                /* Start of an AI string */
                if (!fixedLengthAI) {
                    reduced.append(fnc1);
                }
                last_ai = (10 * Character.getNumericValue(source[i + 1]))
                              + Character.getNumericValue(source[i + 2]);
                /* The following values from "GS-1 General Specification version 8.0 issue 2, May 2008"
                figure 5.4.8.2.1 - 1 "Element Strings with Pre-Defined Length Using Application Identifiers" */
                fixedLengthAI =
                        (last_ai >= 0 && last_ai <= 4) ||
                        (last_ai >= 11 && last_ai <= 20) ||
                        (last_ai == 23) || /* legacy support - see 5.3.8.2.2 */
                        (last_ai >= 31 && last_ai <= 36) ||
                        (last_ai == 41);
            }
        }

        return reduced.toString();
    }
}
