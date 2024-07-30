/*
 * Copyright 2014 Robin Stuart
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
 * A hexagonal shape.
 *
 * @author <a href="mailto:rstuart114@gmail.com">Robin Stuart</a>
 */
public final class Hexagon {

    private static final double INK_SPREAD = 1.25;

    private static final double[] OFFSET_X = { 0.0, 0.86, 0.86, 0.0, -0.86, -0.86 };
    private static final double[] OFFSET_Y = { 1.0, 0.5, -0.5, -1.0, -0.5,   0.5 };

    public final double centreX;
    public final double centreY;
    public final int factor;

    public Hexagon(double centreX, double centreY, int factor) {
        this.centreX = centreX;
        this.centreY = centreY;
        this.factor = factor;
    }

    public double getX(int vertex) {
        if (vertex < 0 || vertex > 5) {
            throw new IllegalArgumentException("Vertex must be between 0 and 5");
        }
        return centreX + (OFFSET_X[vertex] * INK_SPREAD * factor);
    }

    public double getY(int vertex) {
        if (vertex < 0 || vertex > 5) {
            throw new IllegalArgumentException("Vertex must be between 0 and 5");
        }
        return centreY + (OFFSET_Y[vertex] * INK_SPREAD * factor);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Hexagon)) {
            return false;
        }
        Hexagon h = (Hexagon) other;
        return centreX == h.centreX && centreY == h.centreY && factor == h.factor;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hash(centreX, centreY, factor);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "Hexagon[centreX=" + centreX + ", centreY=" + centreY + ", factor=" + factor + "]";
    }
}
