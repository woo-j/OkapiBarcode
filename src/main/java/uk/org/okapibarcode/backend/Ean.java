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

import static uk.org.okapibarcode.backend.HumanReadableLocation.BOTTOM;
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
 * <p>Add-on content can be appended to the main symbol content by adding a <tt>'+'</tt> character,
 * followed by the add-on content (up to 5 digits).
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
    private int guardPatternExtraHeight = 5;
    private boolean linkageFlag;
    private EanUpcAddOn addOn;

    /** Creates a new instance. */
    public Ean() {
        this.humanReadableAlignment = HumanReadableAlignment.JUSTIFY;
    }

    /**
     * Sets the EAN mode (EAN-8 or EAN-13). The default is EAN-13.
     *
     * @param mode the EAN mode (EAN-8 or EAN-13)
     */
    public void setMode(Mode mode) {
        this.mode = mode;
    }

    /**
     * Returns the EAN mode (EAN-8 or EAN-13).
     *
     * @return the EAN mode (EAN-8 or EAN-13)
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
    protected void encode() {

        separateContent();

        if (content.isEmpty()) {
            throw new OkapiException("Missing EAN data");
        }

        if (mode == Mode.EAN8) {
            ean8();
        } else {
            ean13();
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

    private void ean13() {

        content = validateAndPad(content, 12);

        char check = calcDigit(content);
        encodeInfo += "Check Digit: " + check + "\n";

        String hrt = content + check;
        char parityChar = hrt.charAt(0);
        String parity = EAN13_PARITY[parityChar - '0'];
        encodeInfo += "Parity Digit: " + parityChar + "\n";

        StringBuilder dest = new StringBuilder("111");
        for (int i = 1; i < 13; i++) {
            if (i == 7) {
                dest.append("11111");
            }
            if (i <= 6) {
                if (parity.charAt(i - 1) == 'B') {
                    dest.append(EAN_SET_B[hrt.charAt(i) - '0']);
                } else {
                    dest.append(EAN_SET_A[hrt.charAt(i) - '0']);
                }
            } else {
                dest.append(EAN_SET_A[hrt.charAt(i) - '0']);
            }
        }
        dest.append("111");

        readable = hrt;
        pattern = new String[] { dest.toString() };
        row_count = 1;
        row_height = new int[] { -1 };
    }

    private void ean8() {

        content = validateAndPad(content, 7);

        char check = calcDigit(content);
        encodeInfo += "Check Digit: " + check + "\n";

        String hrt = content + check;

        StringBuilder dest = new StringBuilder("111");
        for (int i = 0; i < 8; i++) {
            if (i == 4) {
                dest.append("11111");
            }
            dest.append(EAN_SET_A[hrt.charAt(i) - '0']);
        }
        dest.append("111");

        readable = hrt;
        pattern = new String[] { dest.toString() };
        row_count = 1;
        row_height = new int[] { -1 };
    }

    protected static String validateAndPad(String s, int targetLength) {

        if (!s.matches("[0-9]+")) {
            throw new OkapiException("Invalid characters in input");
        }

        if (s.length() > targetLength) {
            throw new OkapiException("Input data too long");
        }

        if (s.length() < targetLength) {
            for (int i = s.length(); i < targetLength; i++) {
                s = '0' + s;
            }
        }

        return s;
    }

    protected static char calcDigit(String s) {

        int count = 0;
        int p = 0;

        for (int i = s.length() - 1; i >= 0; i--) {
            int c = Character.getNumericValue(s.charAt(i));
            if (p % 2 == 0) {
                c = c * 3;
            }
            count += c;
            p++;
        }

        int cdigit = 10 - (count % 10);
        if (cdigit == 10) {
            cdigit = 0;
        }

        return (char) (cdigit + '0');
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
                if (mode == Mode.EAN13) {
                    if (x < 3 || x > 91 || (x > 45 && x < 49)) {
                        h += guardPatternExtraHeight;
                    }
                    if (linkageFlag && (x == 0 || x == 94)) {
                        h += 2;
                        y -= 2;
                    }
                } else {
                    if (x < 3 || x > 62 || (x > 30 && x < 35)) {
                        h += guardPatternExtraHeight;
                    }
                    if (linkageFlag && (x == 0 || x == 66)) {
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
            if (mode == Mode.EAN13) {
                rectangles.add(new Rectangle2D.Double(scale(0),  0, scale(1), 2));
                rectangles.add(new Rectangle2D.Double(scale(94), 0, scale(1), 2));
                rectangles.add(new Rectangle2D.Double(scale(-1), 2, scale(1), 2));
                rectangles.add(new Rectangle2D.Double(scale(95), 2, scale(1), 2));
            } else { // EAN8
                rectangles.add(new Rectangle2D.Double(scale(0),  0, scale(1), 2));
                rectangles.add(new Rectangle2D.Double(scale(66), 0, scale(1), 2));
                rectangles.add(new Rectangle2D.Double(scale(-1), 2, scale(1), 2));
                rectangles.add(new Rectangle2D.Double(scale(67), 2, scale(1), 2));
            }
            symbol_height += 4;
        }

        /* Now add the text */
        if (humanReadableLocation == BOTTOM) {
            symbol_height -= guardPatternExtraHeight;
            double baseline = symbol_height + fontSize;
            if (mode == Mode.EAN13) {
                texts.add(new TextBox(scale(-9), baseline, scale(4),  readable.substring(0, 1), HumanReadableAlignment.RIGHT));
                texts.add(new TextBox(scale(5),  baseline, scale(39), readable.substring(1, 7), humanReadableAlignment));
                texts.add(new TextBox(scale(51), baseline, scale(39), readable.substring(7, 13), humanReadableAlignment));
            } else { // EAN8
                texts.add(new TextBox(scale(5),  baseline, scale(25), readable.substring(0, 4), humanReadableAlignment));
                texts.add(new TextBox(scale(37), baseline, scale(25), readable.substring(4, 8), humanReadableAlignment));
            }
        } else if (humanReadableLocation == TOP) {
            double baseline = fontSize;
            int width = (mode == Mode.EAN13 ? 94 : 66);
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
