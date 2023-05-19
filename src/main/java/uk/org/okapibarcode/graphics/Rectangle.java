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
 * A rectangular shape.
 *
 * @author Daniel Gredler
 */
public final class Rectangle {

    /** The X position of the rectangle's left boundary. */
    public final double x;

    /** The Y position of the rectangle's top boundary. */
    public final double y;

    /** The width of the rectangle. */
    public final double width;

    /** The height of the rectangle. */
    public double height;

    /**
     * Creates a new instance.
     *
     * @param x the X position of the rectangle's left boundary
     * @param y the Y position of the rectangle's top boundary
     * @param width the width of the rectangle
     * @param height the height of the rectangle
     */
    public Rectangle(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Rectangle)) {
            return false;
        }
        Rectangle r = (Rectangle) other;
        return x == r.x && y == r.y && width == r.width && height == r.height;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hash(x, y, width, height);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "Rectangle[x=" + x + ", y=" + y + ", width=" + width + ", height=" + height + "]";
    }
}
