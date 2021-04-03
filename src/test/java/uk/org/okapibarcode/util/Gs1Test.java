/*
 * Copyright 2021 Daniel Gredler
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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import uk.org.okapibarcode.backend.OkapiException;

/**
 * Tests for {@link Gs1}.
 */
public class Gs1Test {

    @Test
    public void testVerify() {

        checkVerify("[253]123456789012",                        "Invalid data length for AI");
        checkVerify("[253]1234567890123",                       "2531234567890123");
        checkVerify("[253]123456789012345678901234567890",      "253123456789012345678901234567890");
        checkVerify("[253]1234567890123456789012345678901",     "Invalid data length for AI");

        checkVerify("[255]123456789012",                        "Invalid data length for AI");
        checkVerify("[255]1234567890123",                       "2551234567890123");
        checkVerify("[255]1234567890123456789012345",           "2551234567890123456789012345");
        checkVerify("[255]12345678901234567890123456",          "Invalid data length for AI");

        checkVerify("[3100]12345",                              "Invalid data length for AI");
        checkVerify("[3100]123456",                             "3100123456");
        checkVerify("[3100]1234567",                            "Invalid data length for AI");

        checkVerify("[3200]12345",                              "Invalid data length for AI");
        checkVerify("[3200]123456",                             "3200123456");
        checkVerify("[3200]1234567",                            "Invalid data length for AI");

        checkVerify("[3400]12345",                              "Invalid data length for AI");
        checkVerify("[3400]123456",                             "3400123456");
        checkVerify("[3400]1234567",                            "Invalid data length for AI");

        checkVerify("[3600]12345",                              "Invalid data length for AI");
        checkVerify("[3600]123456",                             "3600123456");
        checkVerify("[3600]1234567",                            "Invalid data length for AI");

        checkVerify("[3900]123456789012345",                    "3900123456789012345");
        checkVerify("[3900]1234567890123456",                   "Invalid data length for AI");

        checkVerify("[3910]12",                                 "Invalid data length for AI");
        checkVerify("[3910]123",                                "3910123");
        checkVerify("[3910]123456789012345678",                 "3910123456789012345678");
        checkVerify("[3910]1234567890123456789",                "Invalid data length for AI");

        checkVerify("[3920]123456789012345",                    "3920123456789012345");
        checkVerify("[3920]1234567890123456",                   "Invalid data length for AI");

        checkVerify("[3930]12",                                 "Invalid data length for AI");
        checkVerify("[3930]123",                                "3930123");
        checkVerify("[3930]123456789012345678",                 "3930123456789012345678");
        checkVerify("[3930]1234567890123456789",                "Invalid data length for AI");

        checkVerify("[3940]123",                                "Invalid data length for AI");
        checkVerify("[3940]1234",                               "39401234");
        checkVerify("[3940]12345",                              "Invalid data length for AI");

        checkVerify("[421]12",                                  "Invalid data length for AI");
        checkVerify("[421]123",                                 "421123");
        checkVerify("[421]123456789012",                        "421123456789012");
        checkVerify("[421]1234567890123",                       "Invalid data length for AI");

        checkVerify("[423]12",                                  "Invalid data length for AI");
        checkVerify("[423]123",                                 "423123");
        checkVerify("[423]123456789012345",                     "423123456789012345");
        checkVerify("[423]1234567890123456",                    "Invalid data length for AI");

        checkVerify("[425]12",                                  "Invalid data length for AI");
        checkVerify("[425]123",                                 "425123");
        checkVerify("[425]123456789012345",                     "425123456789012345");
        checkVerify("[425]1234567890123456",                    "Invalid data length for AI");

        checkVerify("[7007]12345",                              "Invalid data length for AI");
        checkVerify("[7007]123456",                             "7007123456");
        checkVerify("[7007]123456789012",                       "7007123456789012");
        checkVerify("[7007]1234567890123",                      "Invalid data length for AI");

        checkVerify("[7030]12",                                 "Invalid data length for AI");
        checkVerify("[7030]123",                                "7030123");
        checkVerify("[7030]123456789012345678901234567890",     "7030123456789012345678901234567890");
        checkVerify("[7030]1234567890123456789012345678901",    "Invalid data length for AI");

        checkVerify("[8003]1234567890123",                      "Invalid data length for AI");
        checkVerify("[8003]12345678901234",                     "800312345678901234");
        checkVerify("[8003]123456789012345678901234567890",     "8003123456789012345678901234567890");
        checkVerify("[8003]1234567890123456789012345678901",    "Invalid data length for AI");

        checkVerify("[8008]1234567",                            "Invalid data length for AI");
        checkVerify("[8008]12345678",                           "800812345678");
        checkVerify("[8008]123456789012",                       "8008123456789012");
        checkVerify("[8008]1234567890123",                      "Invalid data length for AI");
    }

    private static void checkVerify(String value, String expectedOutput) {
        String output;
        try {
            output = Gs1.verify(value, null);
        } catch (OkapiException e) {
            output = e.getMessage();
        }
        assertEquals(expectedOutput, output);
    }
}
