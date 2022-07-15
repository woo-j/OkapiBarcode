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

import java.awt.geom.Rectangle2D;

/**
 * <p>Implements the Two-Track Pharmacode bar code symbology.
 *
 * <p>Pharmacode Two-Track is an alternative system to Pharmacode One-Track used
 * for the identification of pharmaceuticals. The symbology is able to encode
 * whole numbers between 4 and 64570080.
 *
 * @author <a href="mailto:rstuart114@gmail.com">Robin Stuart</a>
 */
public class Pharmacode2Track extends Symbol {

    @Override
    protected void encode() {

        if (content.length() > 8) {
            throw new OkapiException("Input too long");
        }

        if (!content.matches("[0-9]+")) {
            throw new OkapiException("Invalid characters in data");
        }

        int tester = Integer.parseInt(content);
        if (tester < 4 || tester > 64570080) {
            throw new OkapiException("Data out of range");
        }

        StringBuilder dest = new StringBuilder();
        do {
            switch (tester % 3) {
                case 0:
                    dest.append('F');
                    tester = (tester - 3) / 3;
                    break;
                case 1:
                    dest.append('D');
                    tester = (tester - 1) / 3;
                    break;
                case 2:
                    dest.append('A');
                    tester = (tester - 2) / 3;
                    break;
            }
        } while (tester != 0);

        dest.reverse();
        infoLine("Encoding: " + dest);

        readable = "";
        pattern = new String[] { dest.toString() };
        row_count = 1;
        row_height = new int[] { -1 };
    }

    @Override
    protected void plotSymbol() {

        int x = 0;
        int w = 1;
        int y = 0;
        int h = 0;

        rectangles.clear();

        for (int xBlock = 0; xBlock < pattern[0].length(); xBlock++) {
            switch (pattern[0].charAt(xBlock)) {
                case 'A':
                    y = 0;
                    h = default_height / 2;
                    break;
                case 'D':
                    y = default_height / 2;
                    h = default_height / 2;
                    break;
                case 'F':
                    y = 0;
                    h = default_height;
                    break;
            }
            Rectangle2D.Double rect = new Rectangle2D.Double(x, y, w, h);
            rectangles.add(rect);
            x += 2;
        }

        symbol_width = pattern[0].length() * 2;
        symbol_height = default_height;
    }
}
