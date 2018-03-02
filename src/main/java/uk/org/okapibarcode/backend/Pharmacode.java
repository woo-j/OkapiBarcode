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
 * Implements the <a href="http://en.wikipedia.org/wiki/Pharmacode">Pharmacode</a>
 * bar code symbology.
 * <br>
 * Pharmacode is used for the identification of pharmaceuticals. The symbology
 * is able to encode whole numbers between 3 and 131070.
 *
 * @author <a href="mailto:rstuart114@gmail.com">Robin Stuart</a>
 */
public class Pharmacode extends Symbol {

    @Override
    public boolean encode() {
        int tester = 0;
        int i;

        String inter = "";
        String dest = "";

        if (content.length() > 6) {
            error_msg = "Input too long";
            return false;
        }

        if (!(content.matches("[0-9]+"))) {
            error_msg = "Invalid characters in data";
            return false;
        }

        for (i = 0; i < content.length(); i++) {
            tester *= 10;
            tester += Character.getNumericValue(content.charAt(i));
        }

        if ((tester < 3) || (tester > 131070)) {
            error_msg = "Data out of range";
            return false;
        }

        do {
            if ((tester & 1) == 0) {
                inter += "W";
                tester = (tester - 2) / 2;
            } else {
                inter += "N";
                tester = (tester - 1) / 2;
            }
        } while (tester != 0);

        for (i = inter.length() - 1; i >= 0; i--) {
            if (inter.charAt(i) == 'W') {
                dest += "32";
            } else {
                dest += "12";
            }
        }

        readable = "";
        pattern = new String[1];
        pattern[0] = dest;
        row_count = 1;
        row_height = new int[1];
        row_height[0] = -1;

        return true;
    }
}
