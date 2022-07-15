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
 * <p>Implements the <a href="http://en.wikipedia.org/wiki/Pharmacode">Pharmacode</a>
 * bar code symbology.
 *
 * <p>Pharmacode is used for the identification of pharmaceuticals. The symbology is
 * able to encode whole numbers between 3 and 131070.
 *
 * @author <a href="mailto:rstuart114@gmail.com">Robin Stuart</a>
 */
public class Pharmacode extends Symbol {

    @Override
    protected void encode() {

        if (content.length() > 6) {
            throw new OkapiException("Input too long");
        }

        if (!content.matches("[0-9]+")) {
            throw new OkapiException("Invalid characters in data");
        }

        int tester = Integer.parseInt(content);
        if (tester < 3 || tester > 131070) {
            throw new OkapiException("Data out of range");
        }

        StringBuilder inter = new StringBuilder();
        do {
            if ((tester & 1) == 0) {
                inter.append('W');
                tester = (tester - 2) / 2;
            } else {
                inter.append('N');
                tester = (tester - 1) / 2;
            }
        } while (tester != 0);

        StringBuilder dest = new StringBuilder(inter.length() * 2);
        for (int i = inter.length() - 1; i >= 0; i--) {
            if (inter.charAt(i) == 'W') {
                dest.append("32");
            } else {
                dest.append("12");
            }
        }

        readable = "";
        pattern = new String[] { dest.toString() };
        row_count = 1;
        row_height = new int[] { -1 };
    }
}
