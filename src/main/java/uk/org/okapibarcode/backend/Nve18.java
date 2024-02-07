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

/**
 * <p>Calculate NVE-18 (Nummer der Versandeinheit), also known as SSCC-18 (Serial Shipping Container Code).
 *
 * <p>Encodes a 17-digit number, adding a modulo-10 check digit.
 *
 * @author <a href="mailto:rstuart114@gmail.com">Robin Stuart</a>
 */
public class Nve18 extends Symbol {

    @Override
    protected void encode() {

        if (content.length() > 17) {
            throw OkapiInputException.inputTooLong();
        }

        if (!content.matches("[0-9]+")) {
            throw OkapiInputException.invalidCharactersInInput();
        }

        // add leading zeroes
        StringBuilder gs1 = new StringBuilder();
        int zeros = 17 - content.length();
        for (int i = 0; i < zeros; i++) {
            gs1.append('0');
        }
        gs1.append(content);

        // add modulus-10 check digit
        int p = 0;
        int count = 0;
        for (int i = gs1.length() - 1; i >= 0; i--) {
            int c = Character.getNumericValue(gs1.charAt(i));
            if ((p % 2) == 0) {
                c = c * 3;
            }
            count += c;
            p++;
        }
        int check = 10 - (count % 10);
        if (check == 10) {
            check = 0;
        }

        infoLine("NVE Check Digit: " + check);

        content = "[00]" + gs1 + check;

        // defer to Code 128
        Code128 code128 = new Code128();
        code128.setDataType(DataType.GS1);
        code128.setHumanReadableLocation(humanReadableLocation);
        code128.setContent(content);

        readable = code128.readable;
        pattern = code128.pattern;
        row_count = code128.row_count;
        row_height = code128.row_height;
        symbol_height = code128.symbol_height;
        symbol_width = code128.symbol_width;
        rectangles = code128.rectangles;
        texts = code128.texts;

        info(code128.encodeInfo);
    }

}
