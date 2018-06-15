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

package uk.org.okapibarcode.util;

/**
 * Double utility class.
 *
 * @author Daniel Gredler
 */
public final class Doubles {

    private Doubles() {
        // utility class
    }

    /**
     * It's usually not a good idea to check floating point numbers for exact equality. This method allows us to check for
     * approximate equality.
     *
     * @param d1 the first double
     * @param d2 the second double
     * @return whether or not the two doubles are approximately equal (to within 0.0001)
     */
    public static boolean roughlyEqual(double d1, double d2) {
        return Math.abs(d1 - d2) < 0.0001;
    }
}
