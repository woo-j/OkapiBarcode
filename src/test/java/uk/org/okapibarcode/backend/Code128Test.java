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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * {@link Code128} tests that can't be run via the {@link SymbolTest}.
 */
public class Code128Test {

    @Test
    public void testCanReuse() {

        Code128 code128 = new Code128();
        code128.setContent("11");
        assertArrayEquals(new int[] { 211232, 231212, 122132, 233111, 2 }, code128.getCodewords());
        assertEquals("Encoding: STARTC 11 \n" +
                     "Data Codewords: 2\n" +
                     "Check Digit: 13\n",
                     code128.getEncodeInfo());

        code128.setContent("12");
        assertArrayEquals(new int[] { 211232, 112232, 122231, 233111, 2 }, code128.getCodewords());
        assertEquals("Encoding: STARTC 12 \n" +
                        "Data Codewords: 2\n" +
                        "Check Digit: 14\n",
                        code128.getEncodeInfo());
    }

}
