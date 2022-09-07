/*
 * Copyright 2019 Daniel Gredler
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.org.okapibarcode.backend.UspsOneCode.formatHumanReadableText;

import org.junit.jupiter.api.Test;

/**
 * {@link UspsOneCode} tests that can't be run via the {@link SymbolTest}.
 */
public class UspsOneCodeTest {

    @Test
    public void testFormatHumanReadableText() {

        // 6-digit mailer IDs: 000000-899999
        // 9-digit mailer IDs: 900000000-999999999

        // 11.3.1.1, Table 26 (6-digit mailer ID)
        // 11.3.2.3, Table 36 (6-digit mailer ID, BI = 93)
        assertEquals("12 123 123456 123456789",               formatHumanReadableText("12.123.123456.123456789"));             // zip code: none
        assertEquals("93 123 123456 123456789 12345",         formatHumanReadableText("93.123.123456.123456789.12345"));       // zip code: 5 digits
        assertEquals("12 123 123456 123456789 12345 6789",    formatHumanReadableText("12.123.123456.123456789.123456789"));   // zip code: 9 digits
        assertEquals("93 123 123456 123456789 12345 6789 01", formatHumanReadableText("93.123.123456.123456789.12345678901")); // zip code: 11 digits

        // 11.3.1.1, Table 27 (9-digit mailer ID)
        // 11.3.2.3, Table 37 (9-digit mailer ID, BI = 93)
        assertEquals("12 123 900000000 123456",               formatHumanReadableText("12.123.900000000.123456"));             // zip code: none
        assertEquals("93 123 900000000 123456 12345",         formatHumanReadableText("93.123.900000000.123456.12345"));       // zip code: 5 digits
        assertEquals("12 123 900000000 123456 12345 6789",    formatHumanReadableText("12.123.900000000.123456.123456789"));   // zip code: 9 digits
        assertEquals("93 123 900000000 123456 12345 6789 01", formatHumanReadableText("93.123.900000000.123456.12345678901")); // zip code: 11 digits

        // 11.3.1.2, Table 29 (STID = 050 or 052, no mailer ID)
        assertEquals("12 050 123456789012345",               formatHumanReadableText("12.050.123456789012345"));             // zip code: none
        assertEquals("12 052 123456789012345 12345 6789",    formatHumanReadableText("12.052.123456789012345.123456789"));   // zip code: 9 digits
        assertEquals("12 052 123456789012345 12345 6789 01", formatHumanReadableText("12.052.123456789012345.12345678901")); // zip code: 11 digits

        // 11.3.2.1, Table 32 (BI = 94, STID = 009)
        assertEquals("94 009 1 1234 123 12 12345",               formatHumanReadableText("94.009.1.1234.123.12.12345"));             // zip code: none
        assertEquals("94 009 1 1234 123 12 12345 12345",         formatHumanReadableText("94.009.1.1234.123.12.12345.12345"));       // zip code: 5 digits
        assertEquals("94 009 1 1234 123 12 12345 12345 6789",    formatHumanReadableText("94.009.1.1234.123.12.12345.123456789"));   // zip code: 9 digits
        assertEquals("94 009 1 1234 123 12 12345 12345 6789 01", formatHumanReadableText("94.009.1.1234.123.12.12345.12345678901")); // zip code: 11 digits

        // 11.3.2.2, Table 34 (BI = 94, STID = 009, MPE = 5)
        assertEquals("94 009 5 1234 1234567890",               formatHumanReadableText("94.009.5.1234.1234567890"));             // zip code: none
        assertEquals("94 009 5 1234 1234567890 12345",         formatHumanReadableText("94.009.5.1234.1234567890.12345"));       // zip code: 5 digits
        assertEquals("94 009 5 1234 1234567890 12345 6789",    formatHumanReadableText("94.009.5.1234.1234567890.123456789"));   // zip code: 9 digits
        assertEquals("94 009 5 1234 1234567890 12345 6789 01", formatHumanReadableText("94.009.5.1234.1234567890.12345678901")); // zip code: 11 digits
    }

}
