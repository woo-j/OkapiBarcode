/*
 * Copyright 2026 Daniel Gredler
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

package uk.org.okapibarcode.output;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.Writer;

/**
 * A custom character stream writer which wraps a JDK writer and provides
 * optimized append methods for numbers.
 *
 * @author Daniel Gredler
 */
final class CustomWriter implements Appendable, Closeable, Flushable {

    private final Writer writer; // wrapped writer
    private final char[] buffer; // for number formatting

    public CustomWriter(Writer writer) {
        this.writer = writer;
        this.buffer = new char[24];
    }

    public CustomWriter append(double val) throws IOException {

        boolean negative = (val < 0);
        long n = Math.abs(Math.round(val * 100));
        long fraction = n % 100;
        long whole = n / 100;

        int pos = buffer.length;
        buffer[--pos] = (char) ('0' + (fraction % 10));
        buffer[--pos] = (char) ('0' + (fraction / 10));
        buffer[--pos] = '.';

        do {
            buffer[--pos] = (char) ('0' + whole % 10);
            whole /= 10;
        } while (whole != 0);

        if (negative) {
            buffer[--pos] = '-';
        }

        writer.write(buffer, pos, buffer.length - pos);

        return this;
    }

    public CustomWriter append(long val) throws IOException {

        boolean negative = (val < 0);
        long n = (negative ? val : -val);

        int pos = buffer.length;
        do {
            buffer[--pos] = (char) ('0' - (n % 10));
            n /= 10;
        } while (n != 0);

        if (negative) {
            buffer[--pos] = '-';
        }

        writer.write(buffer, pos, buffer.length - pos);

        return this;
    }

    @Override
    public CustomWriter append(char c) throws IOException {
        writer.append(c);
        return this;
    }

    @Override
    public CustomWriter append(CharSequence cs) throws IOException {
        writer.append(cs);
        return this;
    }

    @Override
    public CustomWriter append(CharSequence cs, int start, int end) throws IOException {
        writer.append(cs, start, end);
        return this;
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }

    @Override
    public void flush() throws IOException {
        writer.flush();
    }
}
