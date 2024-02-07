/*
 * Copyright 2024 Daniel Gredler
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
 * An Okapi Barcode exception triggered by input provided by the user,
 * or by configuration provided by the user. This type of exception can
 * be expected to occur occasionally, unless the user is very carefully
 * sanitizing all input. In the web world, this exception would be
 * similar to an HTTP 4xx status code.
 *
 * @author Daniel Gredler
 * @see OkapiInternalException
 */
public class OkapiInputException extends OkapiException {

    /** Serial version UID. */
    private static final long serialVersionUID = 5881510760716621289L;

    /**
     * Creates a new instance.
     *
     * @param message the error message
     */
    public OkapiInputException(String message) {
        super(message);
    }

    /**
     * Creates a new exception instance for scenarios where the user provided un-encodable characters.
     *
     * @return a new exception instance for scenarios where the user provided un-encodable characters
     */
    public static OkapiInputException invalidCharactersInInput() {
        return new OkapiInputException("Invalid characters in input data");
    }

    /**
     * Creates a new exception instance for scenarios where the user provided too much data.
     *
     * @return a new exception instance for scenarios where the user provided too much data
     */
    public static OkapiInputException inputTooLong() {
        return new OkapiInputException("Input data too long");
    }
}
