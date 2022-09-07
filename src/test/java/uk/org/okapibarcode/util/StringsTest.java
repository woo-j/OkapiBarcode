/*
 * Copyright 2020 Daniel Gredler
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.org.okapibarcode.util.Strings.escape;
import static uk.org.okapibarcode.util.Strings.unescape;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Strings}.
 */
public class StringsTest {

    @Test
    public void testUnescape() {
        assertEquals("", unescape("", false));
        assertEquals("abc", unescape("abc", false));
        assertEquals("abc\u0000abc", unescape("abc\\0abc", false));
        assertEquals("abc\u0004abc", unescape("abc\\Eabc", false));
        assertEquals("abc\u0007abc", unescape("abc\\aabc", false));
        assertEquals("abc\u0008abc", unescape("abc\\babc", false));
        assertEquals("abc\u0009abc", unescape("abc\\tabc", false));
        assertEquals("abc\nabc", unescape("abc\\nabc", false));
        assertEquals("abc\u000babc", unescape("abc\\vabc", false));
        assertEquals("abc\u000cabc", unescape("abc\\fabc", false));
        assertEquals("abc\rabc", unescape("abc\\rabc", false));
        assertEquals("abc\u001babc", unescape("abc\\eabc", false));
        assertEquals("abc\u001dabc", unescape("abc\\Gabc", false));
        assertEquals("abc\u001eabc", unescape("abc\\Rabc", false));
        assertEquals("abc\\abc", unescape("abc\\\\abc", false));
        assertEquals("abcXabc", unescape("abc\\x58abc", false));
        assertEquals("abc\\qabc", unescape("abc\\qabc", true));
        assertEquals("abc\u001babc", unescape("abc\\u001babc", false));
        assertEquals("abcXabc", unescape("abc\\u0058abc", false));
        assertEquals("abcXabc", unescape("abc\u0058abc", false));
    }

    @Test
    public void testEscapeUnescape() {
        checkEscapeUnescape("");
        checkEscapeUnescape("abc");
        checkEscapeUnescape("abc\\0abc");
        checkEscapeUnescape("abc\\Eabc");
        checkEscapeUnescape("abc\\aabc");
        checkEscapeUnescape("abc\\babc");
        checkEscapeUnescape("abc\\tabc");
        checkEscapeUnescape("abc\\nabc");
        checkEscapeUnescape("abc\\vabc");
        checkEscapeUnescape("abc\\fabc");
        checkEscapeUnescape("abc\\rabc");
        checkEscapeUnescape("abc\\eabc");
        checkEscapeUnescape("abc\\Gabc");
        checkEscapeUnescape("abc\\Rabc");
        checkEscapeUnescape("abc\\\\abc");
        checkEscapeUnescape("abc\\x11abc");
        checkEscapeUnescape("1.2,3@4#5%6^7&8*9(0)p[W]Q{r}n`R~x");
    }

    private static void checkEscapeUnescape(String s) {
        assertEquals(s, escape(unescape(s, false)));
    }
}
