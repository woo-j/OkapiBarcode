/*
 * Copyright 2024 Daniel Gredler
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

import static uk.org.okapibarcode.util.Strings.deleteLastLine;

import java.util.Locale;

/**
 * <p>Implements DPD Code, a linear barcode based on Code 128. Data is expected to be 27 or 28 characters long and use
 * the format "IPPPPPPPTTTTTTTTTTTTTTSSSCCC", where I is the identification tag (omitted if this is a "relabel" barcode),
 * P is the destination postal code (7 alphanumerics), T is the tracking number (4 alphanumerics followed by 10 digits),
 * S is the service code (3 digits) and C is the ISO country code for the destination country (3 digits). A modulo-36
 * check character is added automatically to the human-readable text, but not to the data encoded in the symbol.
 *
 * @author Daniel Gredler
 * @see <a href="https://esolutions.dpd.com/dokumente/DPD_Parcel_Label_Specification_2.4.1_EN.pdf">DPD Label Specification</a>
 */
public class DpdCode extends Symbol {

    @Override
    protected void encode() {

        content = content.toUpperCase(Locale.US);

        if (content.length() < 27) {
            throw new OkapiInputException("Input data too short");
        }

        if (content.length() > 28) {
            throw OkapiInputException.inputTooLong();
        }

        if (!content.matches(".?[0-9A-Z]{11}[0-9]{16}")) {
            throw OkapiInputException.invalidCharactersInInput();
        }

        Code128 code128 = new Code128();
        code128.setContent(content);

        int mod = 36;
        int cd = mod;
        int start = (content.length() == 27 ? 0 : 1);
        for (int i = start; i < content.length(); i++) {
            char c = content.charAt(i);
            cd += (c >= 'A' ? c - 'A' + 10 : c - '0');
            if (cd > mod) {
                cd -= mod;
            }
            cd *= 2;
            if (cd > mod) {
                cd -= mod + 1;
            }
        }
        cd = mod + 1 - cd;
        if (cd == mod) {
            cd = 0;
        }
        char check = (char) (cd < 10 ? cd + '0' : (cd - 10) + 'A');

        String hrt = content.substring(start, start + 4) + " " +       // PPPP
                     content.substring(start + 4, start + 7) + " " +   // PPP
                     content.substring(start + 7, start + 11) + " " +  // TTTT
                     content.substring(start + 11, start + 15) + " " + // TTTT
                     content.substring(start + 15, start + 19) + " " + // TTTT
                     content.substring(start + 19, start + 21) + " " + // TT
                     content.substring(start + 21, start + 24) + " " + // SSS
                     content.substring(start + 24, start + 27) + " " + // CCC
                     check;                                            // D

        readable = hrt;
        pattern = new String[] { code128.pattern[0] };
        rowHeight = new int[] { defaultHeight };
        rowCount = 1;
        encodeInfo = deleteLastLine(code128.encodeInfo); // remove shape count, our shape count is added later
    }

    /** {@inheritDoc} */
    @Override
    protected int[] getCodewords() {
        return getPatternAsCodewords(6);
    }
}
