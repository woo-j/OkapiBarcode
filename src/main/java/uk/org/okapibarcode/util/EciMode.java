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

import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

public class EciMode {

    public static final EciMode NONE = new EciMode(-1, null);

    public final int mode;
    public final Charset charset;

    private EciMode(int mode, Charset charset) {
        this.mode = mode;
        this.charset = charset;
    }

    public static EciMode of(String data, String charsetName, int mode) {
        try {
            Charset charset = Charset.forName(charsetName);
            if (charset.canEncode() && charset.newEncoder().canEncode(data)) {
                return new EciMode(mode, charset);
            } else {
                return NONE;
            }
        } catch (UnsupportedCharsetException e) {
            return NONE;
        }
    }

    public EciMode or(String data, String charsetName, int mode) {
        if (!this.equals(NONE)) {
            return this;
        } else {
            return of(data, charsetName, mode);
        }
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof EciMode && ((EciMode) other).mode == this.mode;
    }

    @Override
    public int hashCode() {
        return Integer.valueOf(mode).hashCode();
    }

    @Override
    public String toString() {
        return "EciMode[mode=" + mode + ", charset=" + charset + "]";
    }
}
