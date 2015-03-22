/*
 * Copyright 2014-2015 Robin Stuart, Daniel Gredler
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

/**
 * A simple text item class.
 *
 * @author <a href="mailto:rstuart114@gmail.com">Robin Stuart</a>
 * @author Daniel Gredler
 */
public class TextBox {

    /** X position. */
    public final double x;

    /** Y position. */
    public final double y;

    /** Text value. */
    public final String text;

    /**
     * Creates a new instance.
     *
     * @param x the X position
     * @param y the Y position
     * @param text the text value
     */
    public TextBox(double x, double y, String text) {
        this.x = x;
        this.y = y;
        this.text = text;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "TextBox[x=" + x + ", y=" + y + ", text=" + text + "]";
    }
}
