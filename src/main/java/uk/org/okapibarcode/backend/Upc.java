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
import static uk.org.okapibarcode.backend.Ean.validateAndPad;
import static uk.org.okapibarcode.backend.HumanReadableLocation.BOTTOM;
import static uk.org.okapibarcode.backend.HumanReadableLocation.NONE;
import static uk.org.okapibarcode.backend.HumanReadableLocation.TOP;

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
        "3211", "2221", "2122", "1411", "1132", "1231", "1114", "1312", "1213", "3112"
    };

    private static final String[] SET_B = {
        "1123", "1222", "2212", "1141", "2311", "1321", "4111", "2131", "3121", "2113"
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
    private boolean showCheckDigit = true;
    private int guardPatternExtraHeight = 5;
    private boolean linkageFlag;
    private EanUpcAddOn addOn;

    /** Creates a new instance. */
    public Upc() {
        this.humanReadableAlignment = HumanReadableAlignment.JUSTIFY;
    }

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
     * Sets whether or not to show the check digit in the human-readable text.
     *
     * @param showCheckDigit whether or not to show the check digit in the human-readable text
     */
    public void setShowCheckDigit(boolean showCheckDigit) {
        this.showCheckDigit = showCheckDigit;
    }

    /**
     * Returns whether or not to show the check digit in the human-readable text.
     *
     * @return whether or not to show the check digit in the human-readable text
     */
    public boolean getShowCheckDigit() {
        return showCheckDigit;
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
    }

    private void separateContent() {
        int splitPoint = content.indexOf('+');
        if (splitPoint == -1) {
            // there is no add-on data
            addOn = null;
        } else if (splitPoint == content.length() - 1) {
            // we found the add-on separator, but no add-on data
            throw new OkapiException("Invalid add-on data");
        } else {
            // there is a '+' in the input data, use an add-on EAN2 or EAN5
            addOn = new EanUpcAddOn();
            addOn.font = this.font;
            addOn.fontName = this.fontName;
            addOn.fontSize = this.fontSize;
            addOn.humanReadableLocation = (this.humanReadableLocation == NONE ? NONE : TOP);
            addOn.moduleWidth = this.moduleWidth;
            addOn.default_height = this.default_height + this.guardPatternExtraHeight - 8;
            addOn.setContent(content.substring(splitPoint + 1));
            content = content.substring(0, splitPoint);
        }
    }

    private void upca() {

        content = validateAndPad(content, 11);

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

        content = validateAndPad(content, 7);

        String expanded = expandToEquivalentUpcA(content, true);
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

    /**
     * Expands the zero-compressed UPC-E code to make a UPC-A equivalent (EN Table 5).
     *
     * @param content the UPC-E code to expand
     * @param validate whether or not to validate the input
     * @return the UPC-A equivalent of the specified UPC-E code
     */
    protected String expandToEquivalentUpcA(String content, boolean validate) {

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
                if (validate && (upce[3] == '0' || upce[3] == '1' || upce[3] == '2')) {
                    /* Note 1 - "X3 shall not be equal to 0, 1 or 2" */
                    throw new OkapiException("Invalid UPC-E data");
                }
                break;
            case '4':
                upca[3] = upce[3];
                upca[4] = upce[4];
                upca[10] = upce[5];
                if (validate && upce[4] == '0') {
                    /* Note 2 - "X4 shall not be equal to 0" */
                    throw new OkapiException("Invalid UPC-E data");
                }
                break;
            default:
                upca[3] = upce[3];
                upca[4] = upce[4];
                upca[5] = upce[5];
                upca[10] = emode;
                if (validate && upce[5] == '0') {
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
        boolean black = true;
        int compositeOffset = (linkageFlag ? 6 : 0); // space for composite separator above
        int hrtOffset = (humanReadableLocation == TOP ? getTheoreticalHumanReadableHeight() : 0); // space for HRT above

        rectangles.clear();
        texts.clear();
        x = 0;

        /* Draw the bars in the symbology */
        for (xBlock = 0; xBlock < pattern[0].length(); xBlock++) {

            w = pattern[0].charAt(xBlock) - '0';

            if (black) {
                y = 0;
                h = default_height;
                /* Add extension to guide bars */
                if (mode == Mode.UPCA) {
                    if (x < 10 || x > 84 || (x > 45 && x < 49)) {
                        h += guardPatternExtraHeight;
                    }
                    if (linkageFlag && (x == 0 || x == 94)) {
                        h += 2;
                        y -= 2;
                    }
                } else {
                    if (x < 4 || x > 45) {
                        h += guardPatternExtraHeight;
                    }
                    if (linkageFlag && (x == 0 || x == 50)) {
                        h += 2;
                        y -= 2;
                    }
                }
                Rectangle2D.Double rect = new Rectangle2D.Double(scale(x), y + compositeOffset + hrtOffset, scale(w), h);
                rectangles.add(rect);
                symbol_width = Math.max(symbol_width, (int) rect.getMaxX());
                symbol_height = Math.max(symbol_height, (int) rect.getHeight());
            }

            black = !black;
            x += w;
        }

        /* Add separator for composite symbology, if necessary */
        if (linkageFlag) {
            if (mode == Mode.UPCA) {
                rectangles.add(new Rectangle2D.Double(scale(0),  0, scale(1), 2));
                rectangles.add(new Rectangle2D.Double(scale(94), 0, scale(1), 2));
                rectangles.add(new Rectangle2D.Double(scale(-1), 2, scale(1), 2));
                rectangles.add(new Rectangle2D.Double(scale(95), 2, scale(1), 2));
            } else { // UPCE
                rectangles.add(new Rectangle2D.Double(scale(0),  0, scale(1), 2));
                rectangles.add(new Rectangle2D.Double(scale(50), 0, scale(1), 2));
                rectangles.add(new Rectangle2D.Double(scale(-1), 2, scale(1), 2));
                rectangles.add(new Rectangle2D.Double(scale(51), 2, scale(1), 2));
            }
            symbol_height += 4;
        }

        /* Now add the text */
        if (humanReadableLocation == BOTTOM) {
            symbol_height -= guardPatternExtraHeight;
            double baseline = symbol_height + fontSize;
            if (mode == Mode.UPCA) {
                texts.add(new TextBox(scale(-9), baseline, scale(4), readable.substring(0, 1), HumanReadableAlignment.RIGHT));
                texts.add(new TextBox(scale(12), baseline, scale(32), readable.substring(1, 6), humanReadableAlignment));
                texts.add(new TextBox(scale(51), baseline, scale(32), readable.substring(6, 11), humanReadableAlignment));
                if (showCheckDigit) {
                    texts.add(new TextBox(scale(97), baseline, scale(4), readable.substring(11, 12), HumanReadableAlignment.LEFT));
                }
            } else { // UPCE
                texts.add(new TextBox(scale(-9), baseline, scale(4), readable.substring(0, 1), HumanReadableAlignment.RIGHT));
                texts.add(new TextBox(scale(5), baseline, scale(39), readable.substring(1, 7), humanReadableAlignment));
                if (showCheckDigit) {
                    texts.add(new TextBox(scale(53), baseline, scale(4), readable.substring(7, 8), HumanReadableAlignment.LEFT));
                }
            }
        } else if (humanReadableLocation == TOP) {
            double baseline = fontSize;
            int width = (mode == Mode.UPCA ? 94 : 50);
            texts.add(new TextBox(scale(0), baseline, scale(width), readable, humanReadableAlignment));
        }

        /* Now add the add-on symbol, if necessary */
        if (addOn != null) {
            int gap = 9;
            int baseX = symbol_width + scale(gap);
            Rectangle2D.Double r1 = rectangles.get(0);
            Rectangle2D.Double ar1 = addOn.rectangles.get(0);
            int baseY = (int) (r1.y + r1.getHeight() - ar1.y - ar1.getHeight());
            for (TextBox t : addOn.getTexts()) {
                texts.add(new TextBox(baseX + t.x, baseY + t.y, t.width, t.text, t.alignment));
            }
            for (Rectangle2D.Double r : addOn.getRectangles()) {
                rectangles.add(new Rectangle2D.Double(baseX + r.x, baseY + r.y, r.width, r.height));
            }
            symbol_width += scale(gap) + addOn.symbol_width;
            pattern[0] = pattern[0] + gap + addOn.pattern[0];
        }
    }

    /** Scales the specified width or x-dimension according to the current module width. */
    private int scale(int w) {
        return moduleWidth * w;
    }
}
