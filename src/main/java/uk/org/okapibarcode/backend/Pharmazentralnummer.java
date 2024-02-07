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
 * <p>PZN8 is a Code 39 based symbology used by the pharmaceutical industry in
 * Germany. PZN8 encodes a 7-digit number and includes a modulo-10 check digit.
 *
 * @author <a href="mailto:rstuart114@gmail.com">Robin Stuart</a>
 */
public class Pharmazentralnummer extends Symbol {

    @Override
    protected void encode() {

        int len = content.length();
        if (len > 7) {
            throw OkapiInputException.inputTooLong();
        }

        if (!content.matches("[0-9]+")) {
            throw OkapiInputException.invalidCharactersInInput();
        }

        StringBuilder localstr = new StringBuilder();
        localstr.append('-');
        int zeroes = 7 - len + 1;
        for (int i = 1; i < zeroes; i++) {
            localstr.append('0');
        }
        localstr.append(content);

        int count = 0;
        for (int i = 1; i < 8; i++) {
            count += i * Character.getNumericValue(localstr.charAt(i));
        }

        int check_digit = count % 11;
        if (check_digit == 10) {
            throw new OkapiInputException("Not a valid PZN identifier, check digit is 10");
        }

        infoLine("Check Digit: " + check_digit);
        localstr.append((char) (check_digit + '0'));

        Code3Of9 code39 = new Code3Of9();
        code39.setContent(localstr.toString());

        readable = "PZN" + localstr;
        pattern = new String[1];
        pattern[0] = code39.pattern[0];
        row_count = 1;
        row_height = new int[] { -1 };
    }
}
