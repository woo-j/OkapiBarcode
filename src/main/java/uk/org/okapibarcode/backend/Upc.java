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

import static uk.org.okapibarcode.backend.Ean.calcDigit;
import static uk.org.okapibarcode.backend.HumanReadableLocation.NONE;

import java.awt.geom.Rectangle2D;
import java.util.Arrays;

/**
 * <p>Implements UPC bar code symbology according to BS EN 797:1996.
 *
 * <p>UPC-A requires an 11 digit article number. The check digit is calculated.
 * UPC-E is a zero-compressed version of UPC-A developed for smaller packages.
 * The code requires a 6 digit article number (digits 0-9). The check digit
 * is calculated. Also supports Number System 1 encoding by entering a 7-digit
 * article number stating with the digit 1.
 *
 * <p>EAN-2 and EAN-5 add-on symbols can be added using the '+' character followed
 * by the add-on data.
 *
 * @author <a href="mailto:jakel2006@me.com">Robert Elliott</a>
 */
public class Upc extends Symbol {

    public static enum Mode {
        UPCA, UPCE
    };

    private static final String[] SET_AC = {
        "3211", "2221", "2122", "1411", "1132", "1231", "1114", "1312",
        "1213", "3112"
    };

    private static final String[] SET_B = {
        "1123", "1222", "2212", "1141", "2311", "1321", "4111", "2131",
        "3121", "2113"
    };

    /* Number set for UPC-E symbol (EN Table 4) */
    private static final String[] UPC_PARITY_0 = {
        "BBBAAA", "BBABAA", "BBAABA", "BBAAAB", "BABBAA", "BAABBA", "BAAABB",
        "BABABA", "BABAAB", "BAABAB"
    };

    /* Not covered by BS EN 797 */
    private static final String[] UPC_PARITY_1 = {
        "AAABBB", "AABABB", "AABBAB", "AABBBA", "ABAABB", "ABBAAB", "ABBBAA",
        "ABABAB", "ABABBA", "ABBABA"
    };

    private Mode mode = Mode.UPCA;
    private int guardPatternExtraHeight = 5;
    private boolean linkageFlag;
    private String addOnContent;

    /**
     * Sets the UPC mode (UPC-A or UPC-E). The default is UPC-A.
     *
     * @param mode the UPC mode (UPC-A or UPC-E)
     */
    public void setMode(Mode mode) {
        this.mode = mode;
    }

    /**
     * Returns the UPC mode (UPC-A or UPC-E).
     *
     * @return the UPC mode (UPC-A or UPC-E)
     */
    public Mode getMode() {
        return mode;
    }

    /**
     * Sets the extra height used for the guard patterns. The default value is <code>5</code>.
     *
     * @param guardPatternExtraHeight the extra height used for the guard patterns
     */
    public void setGuardPatternExtraHeight(int guardPatternExtraHeight) {
        this.guardPatternExtraHeight = guardPatternExtraHeight;
    }

    /**
     * Returns the extra height used for the guard patterns.
     *
     * @return the extra height used for the guard patterns
     */
    public int getGuardPatternExtraHeight() {
        return guardPatternExtraHeight;
    }

    /**
     * Sets the linkage flag. If set to <code>true</code>, this symbol is part of a composite symbol.
     *
     * @param linkageFlag the linkage flag
     */
    protected void setLinkageFlag(boolean linkageFlag) {
        this.linkageFlag = linkageFlag;
    }

    @Override
    public void setHumanReadableAlignment(HumanReadableAlignment humanReadableAlignment) {
        throw new UnsupportedOperationException("UPC human-readable text alignment cannot be changed.");
    }

    @Override
    protected void encode() {

        separateContent();

        if (content.isEmpty()) {
            throw new OkapiException("Missing UPC data");
        }

        if (mode == Mode.UPCA) {
            upca();
        } else {
            upce();
        }

        if (addOnContent != null) {

            String addOnData = AddOn.calcAddOn(addOnContent);
            if (addOnData.isEmpty()) {
                throw new OkapiException("Invalid add-on data");
            }

            pattern[0] = pattern[0] + "9" + addOnData;

            // add leading zeroes to add-on text
            if (addOnContent.length() == 1) {
                addOnContent = "0" + addOnContent;
            }
            if (addOnContent.length() == 3) {
                addOnContent = "0" + addOnContent;
            }
            if (addOnContent.length() == 4) {
                addOnContent = "0" + addOnContent;
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

    private void upca() {

        if (!content.matches("[0-9]+")) {
            throw new OkapiException("Invalid characters in input");
        }

        if (content.length() > 11) {
            throw new OkapiException("Input data too long");
        }

        if (content.length() < 11) {
            for (int i = content.length(); i < 11; i++) {
                content = '0' + content;
            }
        }

        char check = calcDigit(content);
        encodeInfo += "Check Digit: " + check + "\n";

        String hrt = content + check;

        StringBuilder dest = new StringBuilder("111");
        for (int i = 0; i < 12; i++) {
            if (i == 6) {
                dest.append("11111");
            }
            dest.append(SET_AC[hrt.charAt(i) - '0']);
        }
        dest.append("111");

        readable = hrt;
        pattern = new String[] { dest.toString() };
        row_count = 1;
        row_height = new int[] { -1 };
    }

    private void upce() {

        if (!content.matches("[0-9]+")) {
            throw new OkapiException("Invalid characters in input");
        }

        if (content.length() > 7) {
            throw new OkapiException("Input data too long");
        }

        if (content.length() < 7) {
            for (int i = content.length(); i < 7; i++) {
                content = '0' + content;
            }
        }

        String expanded = expandToEquivalentUpcA(content);
        encodeInfo += "UPC-A Equivalent: " + expanded + "\n";

        char check = calcDigit(expanded);
        encodeInfo += "Check Digit: " + check + "\n";

        String hrt = content + check;

        int numberSystem = getNumberSystem(content);
        String[] parityArray = (numberSystem == 1 ? UPC_PARITY_1 : UPC_PARITY_0);
        String parity = parityArray[check - '0'];

        StringBuilder dest = new StringBuilder("111");
        for (int i = 0; i < 6; i++) {
            if (parity.charAt(i) == 'A') {
                dest.append(SET_AC[content.charAt(i + 1) - '0']);
            } else { // B
                dest.append(SET_B[content.charAt(i + 1) - '0']);
            }
        }
        dest.append("111111");

        readable = hrt;
        pattern = new String[] { dest.toString() };
        row_count = 1;
        row_height = new int[] { -1 };
    }

    /** Expands the zero-compressed UPCE code to make a UPCA equivalent (EN Table 5). */
    private static String expandToEquivalentUpcA(String content) {

        char[] upce = content.toCharArray();
        char[] upca = new char[11];
        Arrays.fill(upca, '0');
        upca[0] = upce[0];
        upca[1] = upce[1];
        upca[2] = upce[2];

        char emode = upce[6];

        switch (emode) {
            case '0':
            case '1':
            case '2':
                upca[3] = emode;
                upca[8] = upce[3];
                upca[9] = upce[4];
                upca[10] = upce[5];
                break;
            case '3':
                upca[3] = upce[3];
                upca[9] = upce[4];
                upca[10] = upce[5];
                if (upce[3] == '0' || upce[3] == '1' || upce[3] == '2') {
                    /* Note 1 - "X3 shall not be equal to 0, 1 or 2" */
                    throw new OkapiException("Invalid UPC-E data");
                }
                break;
            case '4':
                upca[3] = upce[3];
                upca[4] = upce[4];
                upca[10] = upce[5];
                if (upce[4] == '0') {
                    /* Note 2 - "X4 shall not be equal to 0" */
                    throw new OkapiException("Invalid UPC-E data");
                }
                break;
            default:
                upca[3] = upce[3];
                upca[4] = upce[4];
                upca[5] = upce[5];
                upca[10] = emode;
                if (upce[5] == '0') {
                    /* Note 3 - "X5 shall not be equal to 0" */
                    throw new OkapiException("Invalid UPC-E data");
                }
                break;
        }

        return new String(upca);
    }

    /** Two number systems can be used: system 0 and system 1. */
    private static int getNumberSystem(String content) {
        switch (content.charAt(0)) {
            case '0':
                return 0;
            case '1':
                return 1;
            default:
                throw new OkapiException("Invalid input data");
        }
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
                    if (linkageFlag && (x == 0) || (x == 94)) {
                        h += 2;
                        y -= 2;
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
                    if (linkageFlag && (x == 0) || (x == 50)) {
                        h += 2;
                        y -= 2;
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

        symbol_height = default_height + 5; // TODO: wonky, images are taller than necessary

        /* Now add the text */
        if (humanReadableLocation != NONE) {
            double baseline = symbol_height + fontSize - shortLongDiff + compositeOffset;
            double addOnBaseline = 6.0 + compositeOffset;
            if (mode == Mode.UPCA) {
                texts.add(new TextBox(0, baseline, 6, readable.substring(0, 1)));
                texts.add(new TextBox(16, baseline, 36, readable.substring(1, 6)));
                texts.add(new TextBox(55, baseline, 36, readable.substring(6, 11)));
                texts.add(new TextBox(101, baseline, 6, readable.substring(11, 12)));
                if (addOnContent != null) {
                    int width = (addOnContent.length() == 2 ? 20 : 47);
                    texts.add(new TextBox(110, addOnBaseline, width, addOnContent));
                }
            } else { // UPCE
                texts.add(new TextBox(0, baseline, 6, readable.substring(0, 1)));
                texts.add(new TextBox(9, baseline, 43, readable.substring(1, 7)));
                texts.add(new TextBox(57, baseline, 6, readable.substring(7, 8)));
                if (addOnContent != null) {
                    int width = (addOnContent.length() == 2 ? 20 : 47);
                    texts.add(new TextBox(66, addOnBaseline, width, addOnContent));
                }
            }
        }
    }
}
