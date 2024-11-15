/*
 * Copyright 2014-2015 Robin Stuart, Robert Elliott, Daniel Gredler
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.org.okapibarcode.util.Integers.normalizeRotation;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Integers}.
 */
public class IntegersTest {

    @Test
    void testNormalizeRotation() {

        // test positive rotations
        assertEquals(0, normalizeRotation(0));
        assertEquals(90, normalizeRotation(90));
        assertEquals(180, normalizeRotation(180));
        assertEquals(270, normalizeRotation(270));
        assertEquals(0, normalizeRotation(360));
        assertEquals(90, normalizeRotation(450));

        // test negative rotations
        assertEquals(270, normalizeRotation(-90));
        assertEquals(180, normalizeRotation(-180));
        assertEquals(90, normalizeRotation(-270));
        assertEquals(0, normalizeRotation(-360));
        assertEquals(270, normalizeRotation(-450));

        // test invalid rotations
        assertThrows(IllegalArgumentException.class, () -> normalizeRotation(45));
        assertThrows(IllegalArgumentException.class, () -> normalizeRotation(-45));
    }
}