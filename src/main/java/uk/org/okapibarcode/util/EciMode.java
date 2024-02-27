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

import static uk.org.okapibarcode.util.Arrays.contains;

import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Represents an ECI (Extended Channel Interpretation) mode. Each ECI mode corresponds to a particular
 * character set, and allows international text to be encoded in barcodes which support ECI.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Extended_Channel_Interpretation">ECI Wikipedia Page</a>
 * @see <a href="https://docs.oracle.com/javase/8/docs/technotes/guides/intl/encoding.doc.html">Java 8 Supported Encodings</a>
 * @see <a href="https://docs.oracle.com/en/java/javase/21/intl/supported-encodings.html">Java 21 Supported Encodings</a>
 */
public final class EciMode {

    /** Represents "no ECI" or "missing ECI". */
    public static final EciMode NONE = new EciMode(-1, null);

    /** The available ECI modes, in priority order. */
    public static List< EciMode > ECIS = Collections.unmodifiableList(Arrays.asList(
        EciMode.of(3, "ISO-8859-1"),
        EciMode.of(4, "ISO-8859-2"),
        EciMode.of(5, "ISO-8859-3"),
        EciMode.of(6, "ISO-8859-4"),
        EciMode.of(7, "ISO-8859-5"),
        EciMode.of(8, "ISO-8859-6"),
        EciMode.of(9, "ISO-8859-7"),
        EciMode.of(10, "ISO-8859-8"),
        EciMode.of(11, "ISO-8859-9"),
        EciMode.of(12, "ISO-8859-10"), // not usually supported by Java
        EciMode.of(13, "ISO-8859-11"),
        EciMode.of(15, "ISO-8859-13"),
        EciMode.of(16, "ISO-8859-14"), // not usually supported by Java
        EciMode.of(17, "ISO-8859-15"),
        EciMode.of(18, "ISO-8859-16"), // not usually supported by older Java versions
        EciMode.of(21, "windows-1250"),
        EciMode.of(22, "windows-1251"),
        EciMode.of(23, "windows-1252"),
        EciMode.of(24, "windows-1256"),
        EciMode.of(20, "Shift_JIS"),
        EciMode.of(26, "UTF-8"),
        // UTF-8 is the final fallback when automatically detecting the ECI, since it can encode anything.
        // The ECI modes below are available to be requested explicitly, but are never used automatically.
        EciMode.of(0, "IBM437"),
        EciMode.of(1, "ISO-8859-1"),
        EciMode.of(2, "IBM437"),
        EciMode.of(25, "UTF-16BE"),
        EciMode.of(27, "US-ASCII"),
        EciMode.of(28, "Big5"),
        EciMode.of(29, "GB2312"),
        EciMode.of(30, "EUC-KR"),
        EciMode.of(31, "GBK"),
        EciMode.of(32, "GB18030"),
        EciMode.of(33, "UTF-16LE"),
        EciMode.of(34, "UTF-32BE"),
        EciMode.of(35, "UTF-32LE")));

    public final int mode;
    public final Charset charset;

    private EciMode(int mode, Charset charset) {
        this.mode = mode;
        this.charset = charset;
    }

    private static EciMode of(int mode, String charsetName) {
        try {
            return new EciMode(mode, Charset.forName(charsetName));
        } catch (UnsupportedCharsetException e) {
            return NONE;
        }
    }

    public static EciMode chooseFor(String data, int... filter) {
        for (EciMode eci : ECIS) {
            if (eci.charset != null
                && eci.charset.canEncode()
                && eci.charset.newEncoder().canEncode(data)
                && (filter.length == 0 || contains(filter, eci.mode))) {
                return eci;
            }
        }
        return NONE;
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
