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

package uk.org.okapibarcode.util;

import uk.org.okapibarcode.backend.OkapiException;

/**
 * Array utility class.
 *
 * @author Daniel Gredler
 */
public final class Arrays {

    private Arrays() {
        // utility class
    }

    /**
     * Returns the position of the specified value in the specified array.
     *
     * @param value the value to search for
     * @param array the array to search in
     * @return the position of the specified value in the specified array
     */
    public static int positionOf(char value, char[] array) {
        for (int i = 0; i < array.length; i++) {
            if (value == array[i]) {
                return i;
            }
        }
        throw new OkapiException("Unable to find character '" + value + "' in character array.");
    }

    /**
     * Returns the position of the specified value in the specified array.
     *
     * @param value the value to search for
     * @param array the array to search in
     * @return the position of the specified value in the specified array
     */
    public static int positionOf(int value, int[] array) {
        for (int i = 0; i < array.length; i++) {
            if (value == array[i]) {
                return i;
            }
        }
        throw new OkapiException("Unable to find integer '" + value + "' in integer array.");
    }

    /**
     * Returns <code>true</code> if the specified array contains the specified value.
     *
     * @param array the array to check in
     * @param value the value to check for
     * @return true if the specified array contains the specified value
     */
    public static boolean contains(int[] array, int value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == value) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns <code>true</code> if the specified array contains the specified sub-array at the specified index.
     *
     * @param array the array to search in
     * @param searchFor the sub-array to search for
     * @param index the index at which to search
     * @return whether or not the specified array contains the specified sub-array at the specified index
     */
    public static boolean containsAt(byte[] array, byte[] searchFor, int index) {
        for (int i = 0; i < searchFor.length; i++) {
            if (index + i >= array.length || array[index + i] != searchFor[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Inserts the specified array into the specified original array at the specified index.
     *
     * @param original the original array into which we want to insert another array
     * @param index the index at which we want to insert the array
     * @param inserted the array that we want to insert
     * @return the combined array
     */
    public static int[] insertArray(int[] original, int index, int[] inserted) {
        int[] modified = new int[original.length + inserted.length];
        System.arraycopy(original, 0, modified, 0, index);
        System.arraycopy(inserted, 0, modified, index, inserted.length);
        System.arraycopy(original, index, modified, index + inserted.length, modified.length - index - inserted.length);
        return modified;
    }
}
