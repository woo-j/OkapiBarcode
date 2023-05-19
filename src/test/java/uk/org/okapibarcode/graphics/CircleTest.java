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
 * Tests for {@link Circle}.
 */
public class CircleTest {

    @Test
    public void testCircle() {

        Circle circle1 = new Circle(1, 2, 3);
        assertEquals(1, circle1.centreX, 0.001);
        assertEquals(2, circle1.centreY, 0.001);
        assertEquals(3, circle1.radius, 0.001);
        assertEquals("Circle[centreX=1.0, centreY=2.0, radius=3.0]", circle1.toString());

        Circle circle2 = new Circle(1, 2, 3);
        assertEqual(circle1, circle2);

        Object obj = new Object();
        assertNotEquals(circle1, obj);
        assertNotEquals(obj, circle1);

        assertNotEqual(circle1, new Circle(9, 2, 3));
        assertNotEqual(circle1, new Circle(1, 9, 3));
        assertNotEqual(circle1, new Circle(1, 2, 9));
    }

    private static void assertEqual(Circle circle1, Circle circle2) {
        assertEquals(circle1.centreX, circle2.centreX);
        assertEquals(circle1.centreY, circle2.centreY);
        assertEquals(circle1.radius, circle2.radius);
        assertEquals(circle1, circle2);
        assertEquals(circle1.hashCode(), circle2.hashCode());
        assertEquals(circle1.toString(), circle2.toString());
    }

    private static void assertNotEqual(Circle circle1, Circle circle2) {
        assertNotEquals(circle1, circle2);
        assertNotEquals(circle1.hashCode(), circle2.hashCode());
        assertNotEquals(circle1.toString(), circle2.toString());
    }

}
