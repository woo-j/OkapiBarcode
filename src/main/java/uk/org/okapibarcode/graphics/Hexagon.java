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
    public final double[] pointX = new double[6];
    public final double[] pointY = new double[6];

    public Hexagon(double centreX, double centreY) {
        this.centreX = centreX;
        this.centreY = centreY;
        for (int i = 0; i < 6; i++) {
            pointX[i] = centreX + (OFFSET_X[i] * INK_SPREAD);
            pointY[i] = centreY + (OFFSET_Y[i] * INK_SPREAD);
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Hexagon)) {
            return false;
        }
        Hexagon h = (Hexagon) other;
        return centreX == h.centreX && centreY == h.centreY;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hash(centreX, centreY);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "Hexagon[centreX=" + centreX + ", centreY=" + centreY + "]";
    }
}
