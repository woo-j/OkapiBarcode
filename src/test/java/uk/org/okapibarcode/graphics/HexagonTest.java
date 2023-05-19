/*
 * Copyright 2023 Daniel Gredler
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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Hexagon}.
 */
public class HexagonTest {

    @Test
    public void testHexagon() {

        Hexagon hex1 = new Hexagon(1, 2);
        assertEquals(1, hex1.centreX, 0.001);
        assertEquals(2, hex1.centreY, 0.001);
        assertEquals("Hexagon[centreX=1.0, centreY=2.0]", hex1.toString());

        Hexagon hex2 = new Hexagon(1, 2);
        assertEqual(hex1, hex2);

        Object obj = new Object();
        assertNotEquals(hex1, obj);
        assertNotEquals(obj, hex1);

        assertNotEqual(hex1, new Hexagon(9, 2));
        assertNotEqual(hex1, new Hexagon(1, 9));
    }

    private static void assertEqual(Hexagon hex1, Hexagon hex2) {
        assertEquals(hex1.centreX, hex2.centreX);
        assertEquals(hex1.centreY, hex2.centreY);
        assertArrayEquals(hex1.pointX, hex2.pointX);
        assertArrayEquals(hex1.pointY, hex2.pointY);
        assertEquals(hex1, hex2);
        assertEquals(hex1.hashCode(), hex2.hashCode());
        assertEquals(hex1.toString(), hex2.toString());
    }

    private static void assertNotEqual(Hexagon hex1, Hexagon hex2) {
        assertNotEquals(hex1, hex2);
        assertNotEquals(hex1.hashCode(), hex2.hashCode());
        assertNotEquals(hex1.toString(), hex2.toString());
    }

}
