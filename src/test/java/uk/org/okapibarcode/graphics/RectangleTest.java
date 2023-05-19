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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Rectangle}.
 */
public class RectangleTest {

    @Test
    public void testRectangle() {

        Rectangle rect1 = new Rectangle(1, 2, 3, 4);
        assertEquals(1, rect1.x, 0.001);
        assertEquals(2, rect1.y, 0.001);
        assertEquals(3, rect1.width, 0.001);
        assertEquals(4, rect1.height, 0.001);
        assertEquals("Rectangle[x=1.0, y=2.0, width=3.0, height=4.0]", rect1.toString());

        Rectangle rect2 = new Rectangle(1, 2, 3, 4);
        assertEqual(rect1, rect2);

        Object obj = new Object();
        assertNotEquals(rect1, obj);
        assertNotEquals(obj, rect1);

        assertNotEqual(rect1, new Rectangle(9, 2, 3, 4));
        assertNotEqual(rect1, new Rectangle(1, 9, 3, 4));
        assertNotEqual(rect1, new Rectangle(1, 2, 9, 4));
        assertNotEqual(rect1, new Rectangle(1, 2, 3, 9));
    }

    private static void assertEqual(Rectangle rect1, Rectangle rect2) {
        assertEquals(rect1.x, rect2.x);
        assertEquals(rect1.y, rect2.y);
        assertEquals(rect1.width, rect2.width);
        assertEquals(rect1.height, rect2.height);
        assertEquals(rect1, rect2);
        assertEquals(rect1.hashCode(), rect2.hashCode());
        assertEquals(rect1.toString(), rect2.toString());
    }

    private static void assertNotEqual(Rectangle rect1, Rectangle rect2) {
        assertNotEquals(rect1, rect2);
        assertNotEquals(rect1.hashCode(), rect2.hashCode());
        assertNotEquals(rect1.toString(), rect2.toString());
    }

}
