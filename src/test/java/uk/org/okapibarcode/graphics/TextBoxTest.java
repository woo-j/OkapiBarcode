/*
 * Copyright 2022 Daniel Gredler
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

package uk.org.okapibarcode.graphics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link TextBox}.
 */
public class TextBoxTest {

    @Test
    public void testTextBox() {

        TextBox text1 = new TextBox(1, 2, 3, "abc", TextAlignment.RIGHT);
        assertEquals(1, text1.x, 0.001);
        assertEquals(2, text1.y, 0.001);
        assertEquals(3, text1.width, 0.001);
        assertEquals("abc", text1.text);
        assertEquals(TextAlignment.RIGHT, text1.alignment);
        assertEquals("TextBox[x=1.0, y=2.0, width=3.0, text=abc, alignment=RIGHT]", text1.toString());

        TextBox text2 = new TextBox(1, 2, 3, "abc", TextAlignment.RIGHT);
        assertEqual(text1, text2);

        Object obj = new Object();
        assertNotEquals(text1, obj);
        assertNotEquals(obj, text1);

        assertNotEqual(text1, new TextBox(9, 2, 3, "abc", TextAlignment.RIGHT));
        assertNotEqual(text1, new TextBox(1, 9, 3, "abc", TextAlignment.RIGHT));
        assertNotEqual(text1, new TextBox(1, 2, 9, "abc", TextAlignment.RIGHT));
        assertNotEqual(text1, new TextBox(1, 2, 3, "999", TextAlignment.RIGHT));
        assertNotEqual(text1, new TextBox(1, 2, 3, "abc", TextAlignment.LEFT));
    }

    private static void assertEqual(TextBox text1, TextBox text2) {
        assertEquals(text1.x, text2.x);
        assertEquals(text1.y, text2.y);
        assertEquals(text1.width, text2.width);
        assertEquals(text1.text, text2.text);
        assertEquals(text1.alignment, text2.alignment);
        assertEquals(text1, text2);
        assertEquals(text1.hashCode(), text2.hashCode());
        assertEquals(text1.toString(), text2.toString());
    }

    private static void assertNotEqual(TextBox text1, TextBox text2) {
        assertNotEquals(text1, text2);
        assertNotEquals(text1.hashCode(), text2.hashCode());
        assertNotEquals(text1.toString(), text2.toString());
    }

}
