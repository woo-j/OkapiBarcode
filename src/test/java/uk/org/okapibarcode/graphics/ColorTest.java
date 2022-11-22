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
 * Tests for {@link Color}.
 */
public class ColorTest {

    @Test
    public void testColor() {

        Color color1 = new Color(1, 2, 3);
        Color color2 = new Color(-16711165);
        assertEqual(color1, color2);

        Color color3 = new Color(new java.awt.Color(-16711165).getRGB());
        assertEqual(color1, color3);

        Color color4 = Color.BLACK;
        assertNotEqual(color1, color4);
    }

    private static void assertEqual(Color color1, Color color2) {
        assertEquals(color1.red, color2.red);
        assertEquals(color1.green, color2.green);
        assertEquals(color1.blue, color2.blue);
        assertEquals(color1, color2);
        assertEquals(color1.hashCode(), color2.hashCode());
        assertEquals(color1.toString(), color2.toString());
    }

    private static void assertNotEqual(Color color1, Color color2) {
        assertNotEquals(color1.red, color2.red);
        assertNotEquals(color1.green, color2.green);
        assertNotEquals(color1.blue, color2.blue);
        assertNotEquals(color1, color2);
        assertNotEquals(color1.hashCode(), color2.hashCode());
        assertNotEquals(color1.toString(), color2.toString());
    }

}
