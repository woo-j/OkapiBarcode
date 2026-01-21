/*
 * Copyright 2022 Daniel Gredler
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
 * <p>Implements the original Plessey bar code symbology (also known as UK Plessey).
 *
 * <p>Plessey can encode a string of hexadecimal digits and uses a CRC-8 check digit.
 *
 * @author Daniel Gredler
 */
public class Plessey extends Symbol {

    private boolean checkDigitInHumanReadableText = true;
    private double moduleWidthRatio = 2;

    /**
     * Sets whether or not the check digit is shown in the human-readable text. Defaults to {@code true}.
     *
     * @param checkDigitInHumanReadableText whether or not the check digit is shown in the human-readable text
     */
    public void setCheckDigitInHumanReadableText(boolean checkDigitInHumanReadableText) {
        this.checkDigitInHumanReadableText = checkDigitInHumanReadableText;
    }

    /**
     * Returns whether or not the check digit is shown in the human-readable text.
     *
     * @return whether or not the check digit is shown in the human-readable text
     */
    public boolean getCheckDigitInHumanReadableText() {
        return checkDigitInHumanReadableText;
    }

    /**
     * Sets the ratio of wide bar width to narrow bar width. Valid values are usually
     * between {@code 2} and {@code 3}. The default value is {@code 2}.
     *
     * @param moduleWidthRatio the ratio of wide bar width to narrow bar width
     */
    public void setModuleWidthRatio(double moduleWidthRatio) {
        this.moduleWidthRatio = moduleWidthRatio;
    }

    /**
     * Returns the ratio of wide bar width to narrow bar width.
     *
     * @return the ratio of wide bar width to narrow bar width
     */
    public double getModuleWidthRatio() {
        return moduleWidthRatio;
    }

    /** {@inheritDoc} */
    @Override
    protected void encode() {

        if (!content.matches("[0-9A-F]*")) {
            throw OkapiInputException.invalidCharactersInInput();
        }

        int length = content.length();
        int maxExpectedLength = 8 + (length * 8) + 16 + 3 + 8;
        StringBuilder intermediate = new StringBuilder(maxExpectedLength);
        intermediate.append("21211221"); // start

        int[] bits = new int[4 * (length + 2)];
        String[] patterns = { "12", "21" }; // 0, 1

        for (int i = 0; i < length; i++) {
            char c = content.charAt(i);
            int n = Character.getNumericValue(c);
            // update pattern
            intermediate.append(patterns[(n >> 0) & 1]);
            intermediate.append(patterns[(n >> 1) & 1]);
            intermediate.append(patterns[(n >> 2) & 1]);
            intermediate.append(patterns[(n >> 3) & 1]);
            // update check digit bits
            bits[(4 * i) + 0] = (n >> 0) & 1;
            bits[(4 * i) + 1] = (n >> 1) & 1;
            bits[(4 * i) + 2] = (n >> 2) & 1;
            bits[(4 * i) + 3] = (n >> 3) & 1;
        }

        // calculate check digit
        int[] generator = { 1, 1, 1, 1, 0, 1, 0, 0, 1 };
        for (int i = 0; i < 4 * length; i++) {
            if (bits[i] == 1) {
                for (int j = 0; j < 9; j++) {
                    bits[i + j] ^= generator[j];
                }
            }
        }

        // add check digit to pattern
        for (int i = bits.length - 8; i < bits.length; i++) {
            intermediate.append(patterns[bits[i]]);
        }

        intermediate.append("201").append("21211212"); // termination + reverse start
        assert maxExpectedLength == intermediate.length();

        readable = content;
        if (checkDigitInHumanReadableText) {
            int i1 = bits[bits.length - 8] | (bits[bits.length - 7] << 1) | (bits[bits.length - 6] << 2) | (bits[bits.length - 5] << 3);
            int i2 = bits[bits.length - 4] | (bits[bits.length - 3] << 1) | (bits[bits.length - 2] << 2) | (bits[bits.length - 1] << 3);
            readable += Integer.toHexString(i1).toUpperCase();
            readable += Integer.toHexString(i2).toUpperCase();
        }

        pattern = new String[] { intermediate.toString() };
        rowHeight = new int[] { defaultHeight };
        rowCount = 1;
    }

    /** {@inheritDoc} */
    @Override
    protected double getModuleWidth(int originalWidth) {
        if (originalWidth == 1) {
            return 1;
        } else if (originalWidth == 0) {
            return 0;
        } else {
            return moduleWidthRatio;
        }
    }
}
