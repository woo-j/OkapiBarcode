/*
 * Copyright 2015 Daniel Gredler
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

import org.junit.Test;

/**
 * {@link MaxiCode} tests that can't be run via the {@link SymbolTest}.
 */
public class MaxiCodeTest {

    @Test
    public void testHumanReadableHeight() {
        MaxiCode maxicode = new MaxiCode();
        maxicode.setMode(4);
        maxicode.setContent("ABC");
        assertEquals(0, maxicode.getHumanReadableHeight());
    }

}
