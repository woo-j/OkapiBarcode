/*
 * Copyright 2018 Daniel Gredler
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import uk.org.okapibarcode.backend.DataMatrix.ForceMode;

/**
 * {@link DataMatrix} tests that can't be run via the {@link SymbolTest}.
 */
public class DataMatrixTest {

    @Test
    public void testActualSize() {

        DataMatrix dm = new DataMatrix();

        try {
            dm.getActualSize();
            fail("Expected error.");
        } catch (IllegalStateException e) {
            assertEquals("Actual size not calculated until symbol is encoded.", e.getMessage());
        }

        try {
            dm.getActualWidth();
            fail("Expected error.");
        } catch (IllegalStateException e) {
            assertEquals("Actual size not calculated until symbol is encoded.", e.getMessage());
        }

        try {
            dm.getActualHeight();
            fail("Expected error.");
        } catch (IllegalStateException e) {
            assertEquals("Actual size not calculated until symbol is encoded.", e.getMessage());
        }

        dm.setPreferredSize(3);
        dm.setContent("ABC");
        assertEquals(3, dm.getPreferredSize());
        assertEquals(3, dm.getActualSize());
        assertEquals(14, dm.getActualWidth());
        assertEquals(14, dm.getActualHeight());
        assertEquals(ForceMode.NONE, dm.getForceMode());

        dm.setPreferredSize(27);
        dm.setContent("ABC");
        assertEquals(27, dm.getPreferredSize());
        assertEquals(27, dm.getActualSize());
        assertEquals(26, dm.getActualWidth());
        assertEquals(12, dm.getActualHeight());
        assertEquals(ForceMode.NONE, dm.getForceMode());

        dm.setForceMode(ForceMode.RECTANGULAR);
        dm.setPreferredSize(0); // no preference
        dm.setContent("ABC");
        assertEquals(0, dm.getPreferredSize());
        assertEquals(25, dm.getActualSize());
        assertEquals(18, dm.getActualWidth());
        assertEquals(8, dm.getActualHeight());
        assertEquals(ForceMode.RECTANGULAR, dm.getForceMode());

        dm.setForceMode(ForceMode.SQUARE);
        dm.setPreferredSize(0); // no preference
        dm.setContent("ABC");
        assertEquals(0, dm.getPreferredSize());
        assertEquals(1, dm.getActualSize());
        assertEquals(10, dm.getActualWidth());
        assertEquals(10, dm.getActualHeight());
        assertEquals(ForceMode.SQUARE, dm.getForceMode());
    }
}
