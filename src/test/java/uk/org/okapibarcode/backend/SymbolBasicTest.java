package uk.org.okapibarcode.backend;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertArrayEquals;
import static uk.org.okapibarcode.backend.Symbol.FNC1;
import static uk.org.okapibarcode.backend.Symbol.FNC2;
import static uk.org.okapibarcode.backend.Symbol.FNC3;
import static uk.org.okapibarcode.backend.Symbol.FNC4;
import static uk.org.okapibarcode.backend.Symbol.toBytes;

import java.nio.charset.Charset;

import org.junit.Test;

public class SymbolBasicTest {

    @Test
    public void testToBytes() {

        assertArrayEquals(null,                     toBytes("\u00e9", US_ASCII));
        assertArrayEquals(new int[] { 0xC3, 0xA9 }, toBytes("\u00e9", UTF_8));

        testToBytes(Charset.forName("ISO-8859-1"));
        testToBytes(Charset.forName("ISO-8859-2"));
        testToBytes(Charset.forName("ISO-8859-3"));
        testToBytes(Charset.forName("ISO-8859-4"));
        testToBytes(Charset.forName("ISO-8859-5"));
        testToBytes(Charset.forName("ISO-8859-6"));
        testToBytes(Charset.forName("ISO-8859-7"));
        testToBytes(Charset.forName("ISO-8859-8"));
        testToBytes(Charset.forName("ISO-8859-9"));
        testToBytes(Charset.forName("ISO-8859-11"));
        testToBytes(Charset.forName("ISO-8859-13"));
        testToBytes(Charset.forName("ISO-8859-15"));
        testToBytes(Charset.forName("windows-1250"));
        testToBytes(Charset.forName("windows-1251"));
        testToBytes(Charset.forName("windows-1252"));
        testToBytes(Charset.forName("windows-1256"));
        testToBytes(Charset.forName("SJIS"));
        testToBytes(Charset.forName("UTF-8"));
    }

    private static void testToBytes(Charset charset) {
        assertArrayEquals(new int[] {},                           toBytes("",          charset));
        assertArrayEquals(new int[] { 'a' },                      toBytes("a",         charset));
        assertArrayEquals(new int[] { 'a', 'b', 'c' },            toBytes("abc",       charset));
        assertArrayEquals(new int[] { FNC1 },                     toBytes("\\<FNC1>",  charset));
        assertArrayEquals(new int[] { 'a', FNC1 },                toBytes("a\\<FNC1>", charset));
        assertArrayEquals(new int[] { FNC1, 'a' },                toBytes("\\<FNC1>a", charset));
        assertArrayEquals(new int[] { FNC2 },                     toBytes("\\<FNC2>",  charset));
        assertArrayEquals(new int[] { 'a', FNC2 },                toBytes("a\\<FNC2>", charset));
        assertArrayEquals(new int[] { FNC2, 'a' },                toBytes("\\<FNC2>a", charset));
        assertArrayEquals(new int[] { FNC3 },                     toBytes("\\<FNC3>",  charset));
        assertArrayEquals(new int[] { 'a', FNC3 },                toBytes("a\\<FNC3>", charset));
        assertArrayEquals(new int[] { FNC3, 'a' },                toBytes("\\<FNC3>a", charset));
        assertArrayEquals(new int[] { FNC4 },                     toBytes("\\<FNC4>",  charset));
        assertArrayEquals(new int[] { 'a', FNC4 },                toBytes("a\\<FNC4>", charset));
        assertArrayEquals(new int[] { FNC4, 'a' },                toBytes("\\<FNC4>a", charset));
        assertArrayEquals(new int[] { FNC4, 'a', 'x', 'y', 'z' }, toBytes("\\<FNC4>a", charset, 'x', 'y', 'z'));
    }

}
