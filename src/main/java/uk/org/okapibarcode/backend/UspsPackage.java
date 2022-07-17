/*
 * Copyright 2015 Robin Stuart
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

import java.awt.geom.Rectangle2D;

/**
 * <p>Implements USPS Intelligent Mail Package Barcode (IMpb), a linear barcode based on GS1-128.
 * Includes additional data checks.
 *
 * @author <a href="mailto:rstuart114@gmail.com">Robin Stuart</a>
 * @see <a href="https://ribbs.usps.gov/intelligentmail_package/documents/tech_guides/BarcodePackageIMSpec.pdf">IMpb Specification</a>
 */
public class UspsPackage extends Symbol {

    @Override
    protected void encode() {

        if (!content.matches("[0-9\\[\\]]+")) {
            /* Input must be numeric only */
            throw new OkapiException("Invalid IMpb data");
        }

        if (content.length() % 2 != 0) {
            /* Input must be even length */
            throw new OkapiException("Invalid IMpb data");
        }

        Code128 code128 = new Code128();
        code128.unsetCc();
        code128.setDataType(DataType.GS1);
        code128.setContent(content);

        boolean fourTwenty = content.length() >= 5 &&
                             content.charAt(0) == '[' &&
                             content.charAt(1) == '4' &&
                             content.charAt(2) == '2' &&
                             content.charAt(3) == '0' &&
                             content.charAt(4) == ']';

        StringBuilder hrt = new StringBuilder();
        int start = (fourTwenty ? content.indexOf('[', 5) : 0);
        if (start != -1) {
            int digits = 0;
            for (int i = start; i < content.length(); i++) {
                char c = content.charAt(i);
                if (c >= '0' && c <= '9') {
                    hrt.append(c);
                    digits++;
                    if (digits % 4 == 0) {
                        hrt.append(' ');
                    }
                }
            }
        }

        encodeInfo = code128.encodeInfo;
        readable = hrt.toString();
        pattern = new String[] { code128.pattern[0] };
        row_count = 1;
        row_height = new int[] { -1 };
    }

    @Override
    protected void plotSymbol() {

        rectangles.clear();
        texts.clear();

        int offset = 20;
        int yoffset = 15;
        int x = 0;
        int y = yoffset;
        int h = default_height;
        boolean black = true;

        for (int xBlock = 0; xBlock < pattern[0].length(); xBlock++) {
            int w = pattern[0].charAt(xBlock) - '0';
            if (black) {
                Rectangle2D.Double rect = new Rectangle2D.Double(x + offset, y, w, h);
                rectangles.add(rect);
                symbol_width = x + w + (2 * offset);
            }
            black = !black;
            x += w;
        }
        symbol_height = h + (2 * yoffset);

        // add boundary bars
        Rectangle2D.Double topBar = new Rectangle2D.Double(0, 0, symbol_width, 2);
        Rectangle2D.Double bottomBar = new Rectangle2D.Double(0, symbol_height - 2, symbol_width, 2);
        rectangles.add(topBar);
        rectangles.add(bottomBar);

        // add banner and human-readable text
        texts.add(new TextBox(0, 12.0, symbol_width, "USPS TRACKING #", humanReadableAlignment));
        texts.add(new TextBox(0, symbol_height - 6.0, symbol_width, readable, humanReadableAlignment));
    }

    @Override
    public int getHeight() {
        // because of the custom layout logic in plotSymbol(), we store all height components (e.g. human
        // readable text height, quiet zone, boundary bars) here; this is not the case for most symbologies
        return symbol_height;
    }

    /** {@inheritDoc} */
    @Override
    protected int[] getCodewords() {
        return getPatternAsCodewords(6);
    }
}
