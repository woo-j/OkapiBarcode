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
 *
 * @author <a href="mailto:rstuart114@gmail.com">Robin Stuart</a>
 */
public class MsiPlessey extends Symbol {

    public enum CheckDigit {
        NONE, MOD10, MOD10_MOD10, MOD11, MOD11_MOD10
    }
    
    private CheckDigit checkOption;
    
    public MsiPlessey() {
        checkOption = CheckDigit.NONE;
    }
    
    public void setCheckDigit(CheckDigit checkMode) {
        checkOption = checkMode;
    }
    
    private final String MSI_PlessTable[] = {
        "12121212", "12121221", "12122112", "12122121", "12211212", "12211221",
        "12212112", "12212121", "21121212", "21121221"
    };

    @Override
    public boolean encode() {
        String intermediate;
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

        if (!(content.matches("[0-9]+?"))) {
            error_msg = "Invalid characters in input";
            return false;
        }

        intermediate = "21"; // Start
        for (i = 0; i < length; i++) {
            intermediate += MSI_PlessTable[Character.getNumericValue(content.charAt(i))];
        }

        readable = content;

        if ((checkOption == CheckDigit.MOD10) || (checkOption == CheckDigit.MOD10_MOD10)) {
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

            if (oddString.length() == 0) {
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

            intermediate += MSI_PlessTable[checkDigit1];
            readable += checkDigit1;
        }

        if ((checkOption == CheckDigit.MOD11) || (checkOption == CheckDigit.MOD11_MOD10)) {
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
                intermediate += MSI_PlessTable[1];
                intermediate += MSI_PlessTable[0];
            } else {
                intermediate += MSI_PlessTable[checkDigit1];
            }
        }

        if ((checkOption == CheckDigit.MOD10_MOD10) || (checkOption == CheckDigit.MOD11_MOD10)) {
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

            if(oddString.length() == 0) {
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

            intermediate += MSI_PlessTable[checkDigit2];
            readable += checkDigit2;
        }

        intermediate += "121"; // Stop

        pattern = new String[1];
        pattern[0] = intermediate;
        row_count = 1;
        row_height = new int[1];
        row_height[0] = -1;
        plotSymbol();
        return true;
    }
}
