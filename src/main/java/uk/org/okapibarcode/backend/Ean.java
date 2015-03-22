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

import java.awt.Rectangle;
/**
 * Implements EAN bar code symbology
 * According to BS EN 797:1996
 *
 * @author <a href="mailto:jakel2006@me.com">Robert Elliott</a>
 * @version 0.2
 */
public class Ean extends Symbol {

    private boolean useAddOn;
    private String addOnContent;

    private enum ean_mode {
        EAN8, EAN13
    };

    private ean_mode mode;
    private boolean linkageFlag;

    private String[] EAN13Parity = {
        "AAAAAA", "AABABB", "AABBAB", "AABBBA", "ABAABB", "ABBAAB", "ABBBAA",
        "ABABAB", "ABABBA", "ABBABA"
    };
    private String[] EANsetA = {
        "3211", "2221", "2122", "1411", "1132", "1231", "1114", "1312", "1213",
        "3112"
    };
    private String[] EANsetB = {
        "1123", "1222", "2212", "1141", "2311", "1321", "4111", "2131", "3121",
        "2113"
    };

    public Ean() {
        mode = ean_mode.EAN13;
        useAddOn = false;
        addOnContent = "";
        linkageFlag = false;
    }

    public void setEan8Mode() {
        mode = ean_mode.EAN8;
    }

    public void setEan13Mode() {
        mode = ean_mode.EAN13;
    }

    public void setLinkageFlag() {
        linkageFlag = true;
    }

    public void unsetLinkageFlag() {
        linkageFlag = false;
    }

    @Override
    public boolean encode() {
        boolean retval = false;
        AddOn addOn = new AddOn();
        String addOnData = "";

        separateContent();

        if(content.length() == 0) {
            error_msg = "Missing EAN data";
            retval = false;
        } else {
            switch (mode) {
            case EAN8:
                retval = ean8();
                break;
            case EAN13:
                retval = ean13();
                break;
            }
        }

        if ((retval) && (useAddOn)) {
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

    private boolean ean13() {
        String accumulator = "";
        String dest, parity;
        int i;

        if (!(content.matches("[0-9]+?"))) {
            error_msg = "Invalid characters in input";
            return false;
        }

        if (content.length() > 12) {
            error_msg = "Input data too long";
            return false;
        }

        for (i = content.length(); i < 12; i++) {
            accumulator += "0";
        }
        accumulator += content;

        accumulator += calcDigit(accumulator);

        parity = EAN13Parity[accumulator.charAt(0) - '0'];

        encodeInfo += "Parity Digit: " + accumulator.charAt(0) + "\n";

        /* Start character */
        dest = "111";

        for (i = 1; i < 13; i++) {
            if (i == 7) {
                dest += "11111";
            }

            if ((i >= 1) && (i <= 6)) {
                if (parity.charAt(i - 1) == 'B') {
                    dest += EANsetB[accumulator.charAt(i) - '0'];
                } else {
                    dest += EANsetA[accumulator.charAt(i) - '0'];
                }
            } else {
                dest += EANsetA[accumulator.charAt(i) - '0'];
            }
        }

        dest += "111";

        readable = accumulator;
        pattern = new String[1];
        pattern[0] = dest;
        row_count = 1;
        row_height = new int[1];
        row_height[0] = -1;
        return true;
    }

    private boolean ean8() {
        String accumulator = "";
        int i;
        String dest;

        if (!(content.matches("[0-9]+?"))) {
            error_msg = "Invalid characters in input";
            return false;
        }

        if (content.length() > 7) {
            error_msg = "Input data too long";
            return false;
        }

        for (i = content.length(); i < 7; i++) {
            accumulator += "0";
        }
        accumulator += content;

        accumulator += calcDigit(accumulator);

        dest = "111";
        for (i = 0; i < 8; i++) {
            if (i == 4) {
                dest += "11111";
            }
            dest += EANsetA[Character.getNumericValue(accumulator.charAt(i))];
        }
        dest += "111";

        readable = accumulator;
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
        int p = 0;
        for (int i = x.length() - 1; i >= 0; i--) {
            c = Character.getNumericValue(x.charAt(i));
            if ((p % 2) == 0) {
                c = c * 3;
            }
            count += c;
            p++;
        }
        cdigit = 10 - (count % 10);
        if (cdigit == 10) {
            cdigit = 0;
        }

        encodeInfo += "Check Digit: " + cdigit + "\n";

        return (char)(cdigit + '0');
    }

    @Override
    public void plotSymbol() {
        int xBlock;
        int x, y, w, h;
        boolean black;
        int compositeOffset = 0;

        rect.clear();
        txt.clear();
        black = true;
        x = 0;
        if (linkageFlag) {
            compositeOffset = 6;
        }
        for (xBlock = 0; xBlock < pattern[0].length(); xBlock++) {
            if (black == true) {
                y = 0;
                black = false;
                w = pattern[0].charAt(xBlock) - '0';
                h = default_height;
                /* Add extension to guide bars */
                if (mode == ean_mode.EAN13) {
                    if ((x < 3) || (x > 91)) {
                        h += 5;
                    }
                    if ((x > 45) && (x < 49)) {
                        h += 5;
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
                }
                if (mode == ean_mode.EAN8) {
                    if ((x < 3) || (x > 62)) {
                        h += 5;
                    }
                    if ((x > 30) && (x < 35)) {
                        h += 5;
                    }
                    if (x > 66) {
                        // Drop add-on
                        h -= 8;
                        y = 8;
                    }
                    if (linkageFlag) {
                        if ((x == 0) || (x == 66)) {
                            h += 2;
                            y -= 2;
                        }
                    }
                }
                Rectangle thisrect = new Rectangle(x + 6, y + compositeOffset, w, h);
                rect.add(thisrect);
                if ((x + w + 12) > symbol_width) {
                    symbol_width = x + w + 12;
                }
            } else {
                black = true;
            }
            x += (double)(pattern[0].charAt(xBlock) - '0');

        }
        if (linkageFlag) {
            // Add seperator for composite symbology
            if (mode == ean_mode.EAN13) {
                rect.add(new Rectangle(0 + 6, 0, 1, 2));
                rect.add(new Rectangle(94 + 6, 0, 1, 2));
                rect.add(new Rectangle(-1 + 6, 2, 1, 2));
                rect.add(new Rectangle(95 + 6, 2, 1, 2));
            } else {
                rect.add(new Rectangle(0 + 6, 0, 1, 2));
                rect.add(new Rectangle(66 + 6, 0, 1, 2));
                rect.add(new Rectangle(-1 + 6, 2, 1, 2));
                rect.add(new Rectangle(67 + 6, 2, 1, 2));
            }
        }
        symbol_height = default_height + 5;
        /* Now add the text */
        if (mode == ean_mode.EAN13) {
            TextBox t1 = new TextBox(0.0, symbol_height + 3.0 + compositeOffset, readable.substring(0, 1));
            txt.add(t1);
            TextBox t2 = new TextBox(17.0, symbol_height + 3.0 + compositeOffset, readable.substring(1, 7));
            txt.add(t2);
            TextBox t3 = new TextBox(63.0, symbol_height + 3.0 + compositeOffset, readable.substring(7, 13));
            txt.add(t3);
            if (useAddOn) {
                if(addOnContent.length() == 2) {
                    TextBox t4 = new TextBox(114.0, 6.0 + compositeOffset, addOnContent);
                    txt.add(t4);
                } else {
                    TextBox t4 = new TextBox(124.0, 6.0 + compositeOffset, addOnContent);
                    txt.add(t4);
                }
            }
        }
        if (mode == ean_mode.EAN8) {
            TextBox t1 = new TextBox(15.0, symbol_height + 3.0 + compositeOffset, readable.substring(0, 4));
            txt.add(t1);
            TextBox t2 = new TextBox(46.0, symbol_height + 3.0 + compositeOffset, readable.substring(4, 8));
            txt.add(t2);
            if (useAddOn) {
                if(addOnContent.length() == 2) {
                    TextBox t4 = new TextBox(86.0, 6.0 + compositeOffset, addOnContent);
                    txt.add(t4);
                } else {
                    TextBox t4 = new TextBox(96.0, 6.0 + compositeOffset, addOnContent);
                    txt.add(t4);
                }
            }
        }
    }
}
