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
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

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
    private static final EciMode ISO_8859_1 = new EciMode(3, "ISO-8859-1");
    private static final EciMode UTF_8 = new EciMode(26, "UTF-8");
    private static final EciMode US_ASCII = new EciMode(27, "US-ASCII");

    /** The available ECI modes, in priority order. */
    public static final LinkedHashMap<Integer, EciMode> ECIS = new LinkedHashMap<>();

    static {
        ECIS.put(3, ISO_8859_1);
        ECIS.put(4, new EciMode(4, "ISO-8859-2"));
        ECIS.put(5, new EciMode(5, "ISO-8859-3"));
        ECIS.put(6, new EciMode(6, "ISO-8859-4"));
        ECIS.put(7, new EciMode(7, "ISO-8859-5"));
        ECIS.put(8, new EciMode(8, "ISO-8859-6"));
        ECIS.put(9, new EciMode(9, "ISO-8859-7"));
        ECIS.put(10, new EciMode(10, "ISO-8859-8"));
        ECIS.put(11, new EciMode(11, "ISO-8859-9"));
        ECIS.put(12, new EciMode(12, "ISO-8859-10")); // not usually supported by Jav);a
        ECIS.put(13, new EciMode(13, "ISO-8859-11"));
        ECIS.put(15, new EciMode(15, "ISO-8859-13"));
        ECIS.put(16, new EciMode(16, "ISO-8859-14")); // not usually supported by Jav);a
        ECIS.put(17, new EciMode(17, "ISO-8859-15"));
        ECIS.put(18, new EciMode(18, "ISO-8859-16")); // not usually supported by older Java version);s
        ECIS.put(21, new EciMode(21, "windows-1250"));
        ECIS.put(22, new EciMode(22, "windows-1251"));
        ECIS.put(23, new EciMode(23, "windows-1252"));
        ECIS.put(24, new EciMode(24, "windows-1256"));
        ECIS.put(20, new EciMode(20, "Shift_JIS"));
        ECIS.put(26, UTF_8);
        // UTF-8 is the final fallback when automatically detecting the ECI, since it can encode anything.
        // The ECI modes below are available to be requested explicitly, but are never used automatically.
        ECIS.put(0, new EciMode(0, "IBM437"));
        ECIS.put(1, new EciMode(1, "ISO-8859-1"));
        ECIS.put(2, new EciMode(2, "IBM437"));
        ECIS.put(25, new EciMode(25, "UTF-16BE"));
        ECIS.put(27, US_ASCII);
        ECIS.put(28, new EciMode(28, "Big5"));
        ECIS.put(29, new EciMode(29, "GB2312"));
        ECIS.put(30, new EciMode(30, "EUC-KR"));
        ECIS.put(31, new EciMode(31, "GBK"));
        ECIS.put(32, new EciMode(32, "GB18030"));
        ECIS.put(33, new EciMode(33, "UTF-16LE"));
        ECIS.put(34, new EciMode(34, "UTF-32BE"));
        ECIS.put(35, new EciMode(35, "UTF-32LE"));
    }

    public final int mode;
    public final String charsetName;

    private static final HashMap<EciMode, Charset> ECI_CHARSETS = new HashMap<>(ECIS.size());

    static {
        ECI_CHARSETS.put(ISO_8859_1, StandardCharsets.ISO_8859_1);
        ECI_CHARSETS.put(UTF_8, StandardCharsets.UTF_8);
        ECI_CHARSETS.put(US_ASCII, StandardCharsets.US_ASCII);
    }

    private EciMode(int mode, String charsetName) {
        this.mode = mode;
        this.charsetName = charsetName;
    }

    public Charset getCharset() {
        return ECI_CHARSETS.computeIfAbsent(this, this::loadCharset);
    }

    public boolean isSupported() {
        return getCharset() != null;
    }

    private Charset loadCharset(EciMode eciMode) {
        try {
            return Charset.forName(eciMode.charsetName);
        } catch (UnsupportedCharsetException e) {
            return null;
        }
    }

    public static EciMode chooseFor(String data, int... filter) {
        for (Map.Entry<Integer, EciMode> eci : ECIS.entrySet()) {
            Charset charset = eci.getValue().getCharset();
            if (charset != null
                    && charset.canEncode()
                    && charset.newEncoder().canEncode(data)
                    && (filter.length == 0 || contains(filter, eci.getKey()))) {
                return eci.getValue();
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
        return Integer.hashCode(mode);
    }

    @Override
    public String toString() {
        return "EciMode[mode=" + mode + ", charset=" + charsetName + "]";
    }
}
