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

package uk.org.okapibarcode.output;

import static org.junit.Assert.assertArrayEquals;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import uk.org.okapibarcode.backend.Code93;
import uk.org.okapibarcode.backend.MaxiCode;
import uk.org.okapibarcode.backend.Symbol;

/**
 * Tests for {@link PostScriptRenderer}.
 */
public class PostScriptRendererTest {

    @Test
    public void testCode93Basic() throws IOException {
        Code93 code93 = new Code93();
        code93.setContent("123456789");
        test(code93, 1, Color.WHITE, Color.BLACK, 5, "code93-basic.eps");
    }

    @Test
    public void testCode93Margin() throws IOException {
        Code93 code93 = new Code93();
        code93.setContent("123456789");
        test(code93, 1, Color.WHITE, Color.BLACK, 20, "code93-margin-size-20.eps");
    }

    @Test
    public void testCode93Magnification() throws IOException {
        Code93 code93 = new Code93();
        code93.setContent("123456789");
        test(code93, 2, Color.WHITE, Color.BLACK, 5, "code93-magnification-2.eps");
    }

    @Test
    public void testMaxiCodeBasic() throws IOException {
        MaxiCode maxicode = new MaxiCode();
        maxicode.setMode(4);
        maxicode.setContent("123456789");
        test(maxicode, 5, Color.WHITE, Color.BLACK, 5, "maxicode-basic.eps");
    }

    private void test(Symbol symbol, double magnification, Color paper, Color ink, int margin, String expectationFile) throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PostScriptRenderer renderer = new PostScriptRenderer(baos, magnification, paper, ink, margin);
        renderer.render(symbol);
        byte[] actual = baos.toByteArray();

        InputStream is = getClass().getResourceAsStream(expectationFile);
        byte[] expected = new byte[is.available()];
        is.read(expected);

        assertArrayEquals(expected, actual);
    }
}
