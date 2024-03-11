/*
 * Copyright 2014-2018 Daniel Gredler
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

import java.util.Objects;

/**
 * An RGB color.
 *
 * @author Daniel Gredler
 */
public final class Color {

    public static final Color WHITE = new Color(255, 255, 255);
    public static final Color BLACK = new Color(0, 0, 0);
    public static final Color RED   = new Color(255, 0, 0);
    public static final Color GREEN = new Color(0, 255, 0);
    public static final Color BLUE  = new Color(0, 0, 255);

    public final int red;
    public final int green;
    public final int blue;

    public Color(int red, int green, int blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public Color(int rgb) {
        this.red = (rgb >> 16) & 0xFF;
        this.green = (rgb >> 8) & 0xFF;
        this.blue = rgb & 0xFF;
    }

    public Color(String rrggbb) {
        if (rrggbb.length() != 6) {
            throw new IllegalArgumentException("Invalid hex RRGGBB value: " + rrggbb);
        }
        for (int i = 0; i < rrggbb.length(); i++) {
            char c = rrggbb.charAt(i);
            if (!(c >= '0' && c <= '9') && !(c >= 'A' && c <= 'F') && !(c >= 'a' && c <= 'f')) {
                throw new IllegalArgumentException("Invalid hex RRGGBB value: " + rrggbb);
            }
        }
        this.red = Integer.parseInt(rrggbb, 0, 2, 16);
        this.green = Integer.parseInt(rrggbb, 2, 4, 16);
        this.blue = Integer.parseInt(rrggbb, 4, 6, 16);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Color)) {
            return false;
        }
        Color c = (Color) other;
        return red == c.red && green == c.green && blue == c.blue;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hash(red, green, blue);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "Color[red=" + red + ", green=" + green + ", blue=" + blue + "]";
    }
}
