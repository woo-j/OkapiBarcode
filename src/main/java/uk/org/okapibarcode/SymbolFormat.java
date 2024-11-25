/*
 * Copyright 2015 Robin Stuart
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
package uk.org.okapibarcode;

/**
 * Barcode Format.
 *
 * @author anyonetff
 */
public enum SymbolFormat {

    svg("svg", "image/svg+xml"),
    eps("eps", "application/postscript"),
    png("png", "image/png"),
    jpg("jpg", "image/jpeg"),
    gif("gif", "image/gif"),
    bmp("bmp", "image/bmp");

    private final String value;

    private final String contentType;

    private SymbolFormat(String value, String mime) {
        this.value = value;
        this.contentType = mime;
    }

    public String getValue() {
        return value;
    }

    public String getContentType() {
        return contentType;
    }

}
