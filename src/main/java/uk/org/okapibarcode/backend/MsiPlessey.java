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

        int length = content.length();
        int i;
        String evenString;
        String oddString;
        String addupString;
        int spacer;
        int addup;
        int weight;
        int checkDigit1;
        int checkDigit2;

        if (!content.matches("[0-9]*")) {
            throw new OkapiException("Invalid characters in input");
        }

        int maxExpectedLength = 2 + ((length + maxCheckDigits(checkDigit)) * 8) + 3;
        StringBuilder intermediate = new StringBuilder(maxExpectedLength);
        intermediate.append("21"); // Start

        for (i = 0; i < length; i++) {
            char c = content.charAt(i);
            int n = Character.getNumericValue(c);
            intermediate.append(MSI_PLESS_TABLE[n]);
        }

        readable = content;

        if (checkDigit == CheckDigit.MOD10 || checkDigit == CheckDigit.MOD10_MOD10) {
            /* Add Modulo-10 check digit */
            evenString = "";
            oddString = "";

            spacer = content.length() & 1;

            for (i = content.length() - 1; i >= 0; i--) {
                if (spacer == 1) {
                    if ((i & 1) != 0) {
                        evenString = content.charAt(i) + evenString;
                    } else {
                        oddString = content.charAt(i) + oddString;
                    }
                } else {
                    if ((i & 1) != 0) {
                        oddString = content.charAt(i) + oddString;
                    } else {
                        evenString = content.charAt(i) + evenString;
                    }
                }
            }

            if (oddString.isEmpty()) {
                addupString = "0";
            } else {
                addupString = Integer.toString(Integer.parseInt(oddString) * 2);
            }

            addupString += evenString;

            addup = 0;
            for(i = 0; i < addupString.length(); i++) {
                addup += addupString.charAt(i) - '0';
            }

            checkDigit1 = 10 - (addup % 10);
            if (checkDigit1 == 10) {
                checkDigit1 = 0;
            }

            intermediate.append(MSI_PLESS_TABLE[checkDigit1]);
            readable += checkDigit1;
        }

        if (checkDigit == CheckDigit.MOD11 || checkDigit == CheckDigit.MOD11_MOD10) {
            /* Add a Modulo-11 check digit */
            weight = 2;
            addup = 0;
            for (i = content.length() - 1; i >= 0; i--) {
                addup += (content.charAt(i) - '0') * weight;
                weight++;

                if (weight == 8) {
                    weight = 2;
                }
            }

            checkDigit1 = 11 - (addup % 11);

            if (checkDigit1 == 11) {
                checkDigit1 = 0;
            }

            readable += checkDigit1;
            if (checkDigit1 == 10) {
                intermediate.append(MSI_PLESS_TABLE[1]);
                intermediate.append(MSI_PLESS_TABLE[0]);
            } else {
                intermediate.append(MSI_PLESS_TABLE[checkDigit1]);
            }
        }

        if (checkDigit == CheckDigit.MOD10_MOD10 || checkDigit == CheckDigit.MOD11_MOD10) {
            /* Add a second Modulo-10 check digit */
            evenString = "";
            oddString = "";

            spacer = readable.length() & 1;

            for (i = readable.length() - 1; i >= 0; i--) {
                if (spacer == 1) {
                    if ((i & 1) != 0) {
                        evenString = readable.charAt(i) + evenString;
                    } else {
                        oddString = readable.charAt(i) + oddString;
                    }
                } else {
                    if ((i & 1) != 0) {
                        oddString = readable.charAt(i) + oddString;
                    } else {
                        evenString = readable.charAt(i) + evenString;
                    }
                }
            }

            if(oddString.isEmpty()) {
                addupString = "0";
            } else {
                addupString = Integer.toString(Integer.parseInt(oddString) * 2);
            }

            addupString += evenString;

            addup = 0;
            for(i = 0; i < addupString.length(); i++) {
                addup += addupString.charAt(i) - '0';
            }

            checkDigit2 = 10 - (addup % 10);
            if (checkDigit2 == 10) {
                checkDigit2 = 0;
            }

            intermediate.append(MSI_PLESS_TABLE[checkDigit2]);
            readable += checkDigit2;
        }

        intermediate.append("121"); // Stop
        assert maxExpectedLength >= intermediate.length();
        assert maxExpectedLength - intermediate.length() <= 8;

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
