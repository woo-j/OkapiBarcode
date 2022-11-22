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

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link TextBox}.
 */
public class TextBoxTest {

    @Test
    public void testTextBox() {
        TextBox text = new TextBox(1, 2, 3, "abc", TextAlignment.RIGHT);
        assertEquals(1, text.x, 0.001);
        assertEquals(2, text.y, 0.001);
        assertEquals(3, text.width, 0.001);
        assertEquals("abc", text.text);
        assertEquals(TextAlignment.RIGHT, text.alignment);
        assertEquals("TextBox[x=1.0, y=2.0, width=3.0, text=abc, alignment=RIGHT]", text.toString());
    }

}
