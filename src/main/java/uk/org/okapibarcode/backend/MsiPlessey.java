/*
 * Copyright 2014 Robin Stuart
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
 * <p>Implements the MSI (Modified Plessey) bar code symbology.
 *
 * <p>MSI Plessey can encode a string of numeric digits and has a range of check digit options.
 *
 * @author <a href="mailto:rstuart114@gmail.com">Robin Stuart</a>
 * @author Daniel Gredler
 */
public class MsiPlessey extends Symbol {

    public enum CheckDigit {
        NONE, MOD10, MOD10_MOD10, MOD11, MOD11_MOD10
    }

    private final static String[] MSI_PLESS_TABLE = {
        "12121212", "12121221", "12122112", "12122121", "12211212",
        "12211221", "12212112", "12212121", "21121212", "21121221"
    };

    private CheckDigit checkDigit = CheckDigit.NONE;
    private boolean checkDigitInHumanReadableText = true;
    private double moduleWidthRatio = 2;

    /**
     * Sets the check digit scheme to use. Options are: None, Modulo-10, 2 x Modulo-10, Modulo-11, or Modulo-11 &amp; 10.
     *
     * @param checkDigit the type of check digit to add to symbol
     */
    public void setCheckDigit(CheckDigit checkDigit) {
        this.checkDigit = checkDigit;
    }

    /**
     * Returns the check digit scheme being used.
     *
     * @return the check digit scheme being used
     */
    public CheckDigit getCheckDigit() {
        return checkDigit;
    }

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

        if (!content.matches("[0-9]*")) {
            throw new OkapiException("Invalid characters in input");
        }

        int length = content.length();
        int maxExpectedLength = 2 + ((length + maxCheckDigits(checkDigit)) * 8) + 3;
        StringBuilder intermediate = new StringBuilder(maxExpectedLength);
        intermediate.append("21"); // Start

        for (int i = 0; i < length; i++) {
            char c = content.charAt(i);
            int n = Character.getNumericValue(c);
            intermediate.append(MSI_PLESS_TABLE[n]);
        }

        String data = content;

        if (checkDigit == CheckDigit.MOD10 || checkDigit == CheckDigit.MOD10_MOD10) {
            /* Add a Modulo-10 check digit */
            int checkDigit = calcMod10(content);
            intermediate.append(MSI_PLESS_TABLE[checkDigit]);
            data += checkDigit;
        }

        if (checkDigit == CheckDigit.MOD11 || checkDigit == CheckDigit.MOD11_MOD10) {
            /* Add a Modulo-11 check digit */
            int checkDigit = calcMod11(content);
            data += checkDigit;
            if (checkDigit == 10) {
                intermediate.append(MSI_PLESS_TABLE[1]);
                intermediate.append(MSI_PLESS_TABLE[0]);
            } else {
                intermediate.append(MSI_PLESS_TABLE[checkDigit]);
            }
        }

        if (checkDigit == CheckDigit.MOD10_MOD10 || checkDigit == CheckDigit.MOD11_MOD10) {
            /* Add a second Modulo-10 check digit */
            int checkDigit = calcMod10(data);
            intermediate.append(MSI_PLESS_TABLE[checkDigit]);
            data += checkDigit;
        }

        intermediate.append("121"); // Stop

        assert maxExpectedLength >= intermediate.length();
        assert maxExpectedLength - intermediate.length() <= 8;

        readable = (checkDigitInHumanReadableText ? data : content);
        pattern = new String[] { intermediate.toString() };
        row_count = 1;
        row_height = new int[] { -1 };
    }

    private static int maxCheckDigits(CheckDigit scheme) {
        switch (scheme) {
            case NONE: return 0;
            case MOD10: return 1;
            case MOD11: return 2;
            case MOD10_MOD10: return 2;
            case MOD11_MOD10: return 3;
            default: throw new IllegalStateException("Unknown check digit scheme: " + scheme);
        }
    }

    private static int calcMod10(String s) {

        int sum = 0;
        int parity = s.length() % 2;
        for (int i = s.length() - 1; i >= 0; i--) {
            int val = s.charAt(i) - '0';
            if (i % 2 == parity) {
                sum += val;
            } else {
                val *= 2;
                sum += val % 10;
                sum += val / 10;
            }
        }

        int checkDigit = 10 - (sum % 10);
        if (checkDigit == 10) {
            checkDigit = 0;
        }

        return checkDigit;
    }

    private static int calcMod11(String s) {

        int sum = 0;
        int weight = 2;
        for (int i = s.length() - 1; i >= 0; i--) {
            sum += (s.charAt(i) - '0') * weight;
            weight++;
            if (weight == 8) {
                weight = 2;
            }
        }

        int checkDigit = 11 - (sum % 11);
        if (checkDigit == 11) {
            checkDigit = 0;
        }

        return checkDigit;
    }

    /** {@inheritDoc} */
    @Override
    protected double getModuleWidth(int originalWidth) {
        if (originalWidth == 1) {
            return 1;
        } else {
            return moduleWidthRatio;
        }
    }
}
