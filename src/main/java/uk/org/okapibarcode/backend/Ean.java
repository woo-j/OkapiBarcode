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
 * <p>Implements EAN bar code symbology according to BS EN 797:1996.
 *
 * <p>European Article Number data can be encoded in EAN-8 or EAN-13 format requiring a 7-digit
 * or 12-digit input respectively. EAN-13 numbers map to Global Trade Identification Numbers (GTIN)
 * whereas EAN-8 symbols are generally for internal use only. Check digit is calculated and should not
 * be in input data. Leading zeroes are added as required.
 *
 * @author <a href="mailto:jakel2006@me.com">Robert Elliott</a>
 */
public class Ean extends Symbol {

    public enum Mode {
        EAN8, EAN13
    };

    private static final String[] EAN13_PARITY = {
        "AAAAAA", "AABABB", "AABBAB", "AABBBA", "ABAABB", "ABBAAB", "ABBBAA",
        "ABABAB", "ABABBA", "ABBABA"
    };

    private static final String[] EAN_SET_A = {
        "3211", "2221", "2122", "1411", "1132", "1231", "1114", "1312", "1213", "3112"
    };

    private static final String[] EAN_SET_B = {
        "1123", "1222", "2212", "1141", "2311", "1321", "4111", "2131", "3121", "2113"
    };

    private Mode mode = Mode.EAN13;
    private boolean linkageFlag;
    private String addOnContent;

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public Mode getMode() {
        return mode;
    }

    protected void setLinkageFlag() {
        linkageFlag = true;
    }

    protected void unsetLinkageFlag() {
        linkageFlag = false;
    }

    @Override
    public void setHumanReadableAlignment(HumanReadableAlignment humanReadableAlignment) {
        throw new UnsupportedOperationException("EAN human-readable text alignment cannot be changed.");
    }

    @Override
    public void setHumanReadableLocation(HumanReadableLocation humanReadableLocation) {
        if (humanReadableLocation == TOP) {
            throw new IllegalArgumentException("Cannot display human-readable text above EAN bar codes.");
        } else {
            super.setHumanReadableLocation(humanReadableLocation);
        }
    }

    @Override
    protected void encode() {

        separateContent();

        if (content.length() == 0) {
            throw new OkapiException("Missing EAN data");
        } else {
            if (mode == Mode.EAN8) {
                ean8();
            } else {
                ean13();
            }
        }

        if (addOnContent != null) {
            String addOnData = AddOn.calcAddOn(addOnContent);
            if (addOnData.length() == 0) {
                throw new OkapiException("Invalid Add-On data");
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
    }

    private void separateContent() {
        int splitPoint = content.indexOf('+');
        if (splitPoint != -1) {
            // There is a '+' in the input data, use an add-on EAN2 or EAN5
            addOnContent = content.substring(splitPoint + 1);
            content = content.substring(0, splitPoint);
        } else {
            addOnContent = null;
        }
    }

    private void ean13() {
        String accumulator = "";
        String dest, parity;
        int i;

        if (!content.matches("[0-9]+")) {
            throw new OkapiException("Invalid characters in input");
        }

        if (content.length() > 12) {
            throw new OkapiException("Input data too long");
        }

        for (i = content.length(); i < 12; i++) {
            accumulator += "0";
        }
        accumulator += content;

        accumulator += calcDigit(accumulator);

        parity = EAN13_PARITY[accumulator.charAt(0) - '0'];

        encodeInfo += "Parity Digit: " + accumulator.charAt(0) + "\n";

        /* Start character */
        dest = "111";

        for (i = 1; i < 13; i++) {
            if (i == 7) {
                dest += "11111";
            }

            if ((i >= 1) && (i <= 6)) {
                if (parity.charAt(i - 1) == 'B') {
                    dest += EAN_SET_B[accumulator.charAt(i) - '0'];
                } else {
                    dest += EAN_SET_A[accumulator.charAt(i) - '0'];
                }
            } else {
                dest += EAN_SET_A[accumulator.charAt(i) - '0'];
            }
        }

        dest += "111";

        readable = accumulator;
        pattern = new String[1];
        pattern[0] = dest;
        row_count = 1;
        row_height = new int[1];
        row_height[0] = -1;
    }

    private void ean8() {
        String accumulator = "";
        int i;
        String dest;

        if (!content.matches("[0-9]+")) {
            throw new OkapiException("Invalid characters in input");
        }

        if (content.length() > 7) {
            throw new OkapiException("Input data too long");
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
            dest += EAN_SET_A[Character.getNumericValue(accumulator.charAt(i))];
        }
        dest += "111";

        readable = accumulator;
        pattern = new String[1];
        pattern[0] = dest;
        row_count = 1;
        row_height = new int[1];
        row_height[0] = -1;
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
                if (mode == Mode.EAN13) {
                    if ((x < 3) || (x > 91)) {
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
                }
                if (mode == Mode.EAN8) {
                    if ((x < 3) || (x > 62)) {
                        h += shortLongDiff;
                    }
                    if ((x > 30) && (x < 35)) {
                        h += shortLongDiff;
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
                Rectangle2D.Double rect = new Rectangle2D.Double(x + 6, y + compositeOffset, w, h);
                rectangles.add(rect);
                if ((x + w + 12) > symbol_width) {
                    symbol_width = x + w + 12;
                }
            } else {
                black = true;
            }
            x += (double)(pattern[0].charAt(xBlock) - '0');

        }

        if (linkageFlag) {
            // Add separator for composite symbology
            if (mode == Mode.EAN13) {
                rectangles.add(new Rectangle2D.Double(0 + 6, 0, 1, 2));
                rectangles.add(new Rectangle2D.Double(94 + 6, 0, 1, 2));
                rectangles.add(new Rectangle2D.Double(-1 + 6, 2, 1, 2));
                rectangles.add(new Rectangle2D.Double(95 + 6, 2, 1, 2));
            } else {
                rectangles.add(new Rectangle2D.Double(0 + 6, 0, 1, 2));
                rectangles.add(new Rectangle2D.Double(66 + 6, 0, 1, 2));
                rectangles.add(new Rectangle2D.Double(-1 + 6, 2, 1, 2));
                rectangles.add(new Rectangle2D.Double(67 + 6, 2, 1, 2));
            }
        }

        symbol_height = default_height + 5; // TODO: wonky, images are taller than necessary

        /* Now add the text */
        if (humanReadableLocation != NONE) {
            double baseline = getHeight() + fontSize - shortLongDiff + compositeOffset;
            double addOnBaseline = 6.0 + compositeOffset;
            if (mode == Mode.EAN13) {
                texts.add(new TextBox(0, baseline, 6, readable.substring(0, 1)));
                texts.add(new TextBox(9, baseline, 43, readable.substring(1, 7)));
                texts.add(new TextBox(55, baseline, 43, readable.substring(7, 13)));
                if (addOnContent != null) {
                    int width = (addOnContent.length() == 2 ? 20 : 47);
                    texts.add(new TextBox(110, addOnBaseline, width, addOnContent));
                }
            } else { // EAN8
                texts.add(new TextBox(9, baseline, 29, readable.substring(0, 4)));
                texts.add(new TextBox(41, baseline, 29, readable.substring(4, 8)));
                if (addOnContent != null) {
                    int width = (addOnContent.length() == 2 ? 20 : 47);
                    texts.add(new TextBox(82, addOnBaseline, width, addOnContent));
                }
            }
        }
    }
}
