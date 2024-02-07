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
 * An Okapi Barcode exception triggered by an unexpected internal error,
 * i.e. probably not the user's fault. This type of exception should
 * ideally never occur, because it probably indicates a logic error
 * within Okapi itself. In the web world, this exception would be similar
 * to an HTTP 5xx status code.
 *
 * @author Daniel Gredler
 * @see OkapiInputException
 */
public class OkapiInternalException extends OkapiException {

    /** Serial version UID. */
    private static final long serialVersionUID = 1976843420413870023L;

    /**
     * Creates a new instance.
     *
     * @param message the error message
     */
    public OkapiInternalException(String message) {
        super(message);
    }

}
