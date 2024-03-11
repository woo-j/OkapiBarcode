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
import static org.junit.jupiter.api.Assertions.assertThrows;

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

        Color color4 = new Color("010203");
        assertEqual(color1, color4);

        Color color5 = new Color("09afAF");
        Color color6 = new Color("09AFaf");
        assertEqual(color5, color6);

        assertNotEqual(color1, Color.BLACK);
        assertNotEqual(color1, new Color(9, 2, 3));
        assertNotEqual(color1, new Color(1, 9, 3));
        assertNotEqual(color1, new Color(1, 2, 9));

        Object obj = new Object();
        assertNotEquals(color1, obj);
        assertNotEquals(obj, color1);

        assertThrows(IllegalArgumentException.class, () -> new Color(""));
        assertThrows(IllegalArgumentException.class, () -> new Color("AB"));
        assertThrows(IllegalArgumentException.class, () -> new Color("ABCDEG"));
        assertThrows(IllegalArgumentException.class, () -> new Color("ABCDE."));
        assertThrows(IllegalArgumentException.class, () -> new Color("ABCDE:"));
        assertThrows(IllegalArgumentException.class, () -> new Color("ABCDE@"));
        assertThrows(IllegalArgumentException.class, () -> new Color("ABCDE`"));
        assertThrows(IllegalArgumentException.class, () -> new Color("ABCDEg"));
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
        assertNotEquals(color1, color2);
        assertNotEquals(color1.hashCode(), color2.hashCode());
        assertNotEquals(color1.toString(), color2.toString());
    }

}
