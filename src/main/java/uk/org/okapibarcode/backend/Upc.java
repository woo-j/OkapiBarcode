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

import static uk.org.okapibarcode.backend.HumanReadableLocation.NONE;
import static uk.org.okapibarcode.backend.HumanReadableLocation.TOP;

import java.awt.geom.Rectangle2D;
/**
 * Implements UPC bar code symbology
 * According to BS EN 797:1996
 * <br>
 * UPC-A requires an 11 digit article number. The check digit is calculated.
 * UPC-E is a zero-compressed version of UPC-A developed for smaller packages.
 * The code requires a 6 digit article number (digits 0-9). The check digit
 * is calculated. Also supports Number System 1 encoding by entering a 7-digit
 * article number stating with the digit 1. In addition EAN-2 and EAN-5 add-on
 * symbols can be added using the + character followed by the add-on data.
 *
 * @author <a href="mailto:jakel2006@me.com">Robert Elliott</a>
 */
public class Upc extends Symbol {

    public static enum Mode {
        UPCA, UPCE
    };

    private boolean useAddOn;
    private String addOnContent;
    private Mode mode;
    private boolean linkageFlag;

    private String[] setAC = {
        "3211", "2221", "2122", "1411", "1132", "1231", "1114", "1312",
        "1213", "3112"
    };
    private String[] setB = {
        "1123", "1222", "2212", "1141", "2311", "1321", "4111", "2131",
        "3121", "2113"
    };
    private String[] UPCParity0 = {
        "BBBAAA", "BBABAA", "BBAABA", "BBAAAB", "BABBAA", "BAABBA", "BAAABB",
        "BABABA", "BABAAB", "BAABAB"
    }; /* Number set for UPC-E symbol (EN Table 4) */
    private String[] UPCParity1 = {
        "AAABBB", "AABABB", "AABBAB", "AABBBA", "ABAABB", "ABBAAB", "ABBBAA",
        "ABABAB", "ABABBA", "ABBABA"
    }; /* Not covered by BS EN 797 */

    public Upc() {
        mode = Mode.UPCA;
        linkageFlag = false;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public Mode getMode() {
        return mode;
    }

    public void setLinkageFlag() {
        linkageFlag = true;
    }

    public void unsetLinkageFlag() {
        linkageFlag = false;
    }

    @Override
    public void setHumanReadableLocation(HumanReadableLocation humanReadableLocation) {
        if (humanReadableLocation == TOP) {
            throw new IllegalArgumentException("Cannot display human-readable text above UPC bar codes.");
        } else {
            super.setHumanReadableLocation(humanReadableLocation);
        }
    }

    @Override
    public boolean encode() {
        boolean retval;
        AddOn addOn = new AddOn();
        String addOnData = "";

        separateContent();
        if(content.length() == 0) {
            error_msg = "Missing UPC data";
            retval = false;
        } else {
            if (mode == Mode.UPCA) {
                retval = upca();
            } else {
                retval = upce();
            }
        }

        if (useAddOn) {
            addOnData = addOn.calcAddOn(addOnContent);
            if (addOnData.length() == 0) {
                error_msg = "Invalid Add-On data";
                retval = false;
            } else {
                pattern[0] = pattern[0] + "9" + addOnData;

                //add leading zeroes to add-on text
                if(addOnContent.length() == 1) {
                    addOnContent = "0" + addOnContent;
                }
                if(addOnContent.length() == 3) {
                    addOnContent = "0" + addOnContent;
                }
                if(addOnContent.length() == 4) {
                    addOnContent = "0" + addOnContent;
                }
            }
        }

        if (retval) {
            plotSymbol();
        }
        return retval;
    }

    private void separateContent() {
        int splitPoint;

        splitPoint = content.indexOf('+');
        if(splitPoint != -1) {
            // There is a '+' in the input data, use an add-on EAN2 or EAN5
            useAddOn = true;
            addOnContent = content.substring(splitPoint + 1);
            content = content.substring(0, splitPoint);
            if(debug) {
                System.out.println("Content: " + content);
                System.out.println("Addon:   " + addOnContent);
            }
        }
    }

    private boolean upca() {
        String accumulator;
        String dest;
        int i;
        char check;

        if (!(content.matches("[0-9]+"))) {
            error_msg = "Invalid characters in input";
            return false;
        }

        if (content.length() > 11) {
            error_msg = "Input data too long";
            return false;
        }

        accumulator = "";
        for (i = content.length(); i < 11; i++) {
            accumulator += "0";
        }
        accumulator += content;
        check = calcDigit(accumulator);
        accumulator += check;
        dest = "111";
        for (i = 0; i < 12; i++) {
            if (i == 6) {
                dest += "11111";
            }
            dest += setAC[Character.getNumericValue(accumulator.charAt(i))];
        }
        dest += "111";

        encodeInfo += "Check Digit: " + check + "\n";

        readable = accumulator;
        pattern = new String[1];
        pattern[0] = dest;
        row_count = 1;
        row_height = new int[1];
        row_height[0] = -1;
        return true;
    }

    private boolean upce() {
        int i, num_system;
        char emode, check;
        String source, parity, dest;
        char[] equivalent = new char[12];
        String equiv = "";

        if (!(content.matches("[0-9]+"))) {
            error_msg = "Invalid characters in input";
            return false;
        }

        if (content.length() > 7) {
            error_msg = "Input data too long";
            return false;
        }

        source = "";
        for (i = content.length(); i < 7; i++) {
            source += "0";
        }
        source += content;

        /* Two number systems can be used - system 0 and system 1 */
        switch (source.charAt(0)) {
        case '0':
            num_system = 0;
            break;
        case '1':
            num_system = 1;
            break;
        default:
            error_msg = "Invalid input data";
            return false;
        }

        /* Expand the zero-compressed UPCE code to make a UPCA equivalent (EN Table 5) */
        emode = source.charAt(6);
        for (i = 0; i < 11; i++) {
            equivalent[i] = '0';
        }
        equivalent[0] = source.charAt(0);
        equivalent[1] = source.charAt(1);
        equivalent[2] = source.charAt(2);

        switch (emode) {
        case '0':
        case '1':
        case '2':
            equivalent[3] = emode;
            equivalent[8] = source.charAt(3);
            equivalent[9] = source.charAt(4);
            equivalent[10] = source.charAt(5);
            break;
        case '3':
            equivalent[3] = source.charAt(3);
            equivalent[9] = source.charAt(4);
            equivalent[10] = source.charAt(5);
            if (((source.charAt(3) == '0') || (source.charAt(3) == '1'))
                    || (source.charAt(3) == '2')) {
                /* Note 1 - "X3 shall not be equal to 0, 1 or 2" */
                error_msg = "Invalid UPC-E data";
                return false;
            }
            break;
        case '4':
            equivalent[3] = source.charAt(3);
            equivalent[4] = source.charAt(4);
            equivalent[10] = source.charAt(5);
            if (source.charAt(4) == '0') {
                /* Note 2 - "X4 shall not be equal to 0" */
                error_msg = "Invalid UPC-E data";
                return false;
            }
            break;
        case '5':
        case '6':
        case '7':
        case '8':
        case '9':
            equivalent[3] = source.charAt(3);
            equivalent[4] = source.charAt(4);
            equivalent[5] = source.charAt(5);
            equivalent[10] = emode;
            if (source.charAt(5) == '0') {
                /* Note 3 - "X5 shall not be equal to 0" */
                error_msg = "Invalid UPC-E data";
                return false;
            }
            break;
        }

        for (i = 0; i < 11; i++) {
            equiv += equivalent[i];
        }

        /* Get the check digit from the expanded UPCA code */
        check = calcDigit(equiv);

        encodeInfo += "Check Digit: " + check + "\n";

        /* Use the number system and check digit information to choose a parity scheme */
        if (num_system == 1) {
            parity = UPCParity1[check - '0'];
        } else {
            parity = UPCParity0[check - '0'];
        }

        /* Take all this information and make the barcode pattern */

        /* start character */
        dest = "111";

        for (i = 0; i <= 5; i++) {
            switch (parity.charAt(i)) {
            case 'A':
                dest += setAC[source.charAt(i + 1) - '0'];
                break;
            case 'B':
                dest += setB[source.charAt(i + 1) - '0'];
                break;
            }
        }

        /* stop character */
        dest += "111111";

        readable = source + check;
        pattern = new String[1];
        pattern[0] = dest;
        row_count = 1;
        row_height = new int[1];
        row_height[0] = -1;
        return true;
    }

    private char calcDigit(String x) {
        int count = 0;
        int c, cdigit;
        for (int i = 0; i < 11; i++) {
            c = Character.getNumericValue(x.charAt(i));
            if ((i % 2) == 0) {
                c = c * 3;
            }
            count = count + c;
        }
        cdigit = 10 - (count % 10);
        if (cdigit == 10) {
            cdigit = 0;
        }

        return (char)(cdigit + '0');
    }

    @Override
    protected void plotSymbol() {
        int xBlock;
        int x, y, w, h;
        boolean black;
        int compositeOffset = 0;
        int shortLongDiff = 5;

        rectangles.clear();
        texts.clear();
        black = true;
        x = 0;
        if (linkageFlag) {
            compositeOffset = 6;
        }
        for (xBlock = 0; xBlock < pattern[0].length(); xBlock++) {
            if (black) {
                y = 0;
                black = false;
                w = pattern[0].charAt(xBlock) - '0';
                h = default_height;
                /* Add extension to guide bars */
                if (mode == Mode.UPCA) {
                    if ((x < 10) || (x > 84)) {
                        h += shortLongDiff;
                    }
                    if ((x > 45) && (x < 49)) {
                        h += shortLongDiff;
                    }
                    if (x > 95) {
                        // Drop add-on
                        h -= 8;
                        y = 8;
                    }
                    if (linkageFlag) {
                        if ((x == 0) || (x == 94)) {
                            h += 2;
                            y -= 2;
                        }
                    }
                } else {
                    if ((x < 4) || (x > 45)) {
                        h += shortLongDiff;
                    }
                    if (x > 52) {
                        // Drop add-on
                        h -= 8;
                        y = 8;
                    }
                    if (linkageFlag) {
                        if ((x == 0) || (x == 50)) {
                            h += 2;
                            y -= 2;
                        }
                    }
                }
                Rectangle2D.Double rect = new Rectangle2D.Double(x + 6, y + compositeOffset, w, h);
                rectangles.add(rect);
                if ((x + w + 12) > symbol_width) {
                    symbol_width = x + w + 12;
                }
            } else {
                black = true;
            }
            x += pattern[0].charAt(xBlock) - '0';
        }

        if (linkageFlag) {
            // Add separator for composite symbology
            if (mode == Mode.UPCA) {
                rectangles.add(new Rectangle2D.Double(0 + 6, 0, 1, 2));
                rectangles.add(new Rectangle2D.Double(94 + 6, 0, 1, 2));
                rectangles.add(new Rectangle2D.Double(-1 + 6, 2, 1, 2));
                rectangles.add(new Rectangle2D.Double(95 + 6, 2, 1, 2));
            } else { // UPCE
                rectangles.add(new Rectangle2D.Double(0 + 6, 0, 1, 2));
                rectangles.add(new Rectangle2D.Double(50 + 6, 0, 1, 2));
                rectangles.add(new Rectangle2D.Double(-1 + 6, 2, 1, 2));
                rectangles.add(new Rectangle2D.Double(51 + 6, 2, 1, 2));
            }
        }

        symbol_height = default_height + 5;

        /* Now add the text */
        if (humanReadableLocation != NONE) {
            double baseline = getHeight() + fontSize - shortLongDiff + compositeOffset;
            double addOnBaseline = 6.0 + compositeOffset;
            if (mode == Mode.UPCA) {
                texts.add(new TextBox(3, baseline, readable.substring(0, 1)));
                texts.add(new TextBox(34, baseline, readable.substring(1, 6)));
                texts.add(new TextBox(73, baseline, readable.substring(6, 11)));
                texts.add(new TextBox(104, baseline, readable.substring(11, 12)));
                if (useAddOn) {
                    if (addOnContent.length() == 2) {
                        texts.add(new TextBox(118, addOnBaseline, addOnContent));
                    } else {
                        texts.add(new TextBox(133, addOnBaseline, addOnContent));
                    }
                }
            } else { // UPCE
                texts.add(new TextBox(3, baseline, readable.substring(0, 1)));
                texts.add(new TextBox(30, baseline, readable.substring(1, 7)));
                texts.add(new TextBox(61, baseline, readable.substring(7, 8)));
                if (useAddOn) {
                    if (addOnContent.length() == 2) {
                        texts.add(new TextBox(75, addOnBaseline, addOnContent));
                    } else {
                        texts.add(new TextBox(90, addOnBaseline, addOnContent));
                    }
                }
            }
        }
    }
}
