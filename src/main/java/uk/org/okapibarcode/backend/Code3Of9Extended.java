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
 * <p>Implements Code 3 of 9 Extended, also known as Code 39e and Code39+.
 *
 * <p>Supports encoding of all characters in the 7-bit ASCII table. A modulo-43
 * check digit can be added if required.
 *
 * @author <a href="mailto:rstuart114@gmail.com">Robin Stuart</a>
 */
public class Code3Of9Extended extends Symbol {

    /** The types of Code 39 Extended check digits available. */
    public enum CheckDigit {
        /** No check digit. */
        NONE,
        /** One mod 43 check digit. */
        MOD43
    }

    private static final String[] E_CODE_39 = {
        "%U", "$A", "$B", "$C", "$D", "$E", "$F", "$G", "$H", "$I", "$J", "$K",
        "$L", "$M", "$N", "$O", "$P", "$Q", "$R", "$S", "$T", "$U", "$V", "$W",
        "$X", "$Y", "$Z", "%A", "%B", "%C", "%D", "%E", " ", "/A", "/B", "/C",
        "/D", "/E", "/F", "/G", "/H", "/I", "/J", "/K", "/L", "-", ".", "/O",
        "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "/Z", "%F", "%G",
        "%H", "%I", "%J", "%V", "A", "B", "C", "D", "E", "F", "G", "H", "I",
        "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W",
        "X", "Y", "Z", "%K", "%L", "%M", "%N", "%O", "%W", "+A", "+B", "+C",
        "+D", "+E", "+F", "+G", "+H", "+I", "+J", "+K", "+L", "+M", "+N", "+O",
        "+P", "+Q", "+R", "+S", "+T", "+U", "+V", "+W", "+X", "+Y", "+Z", "%P",
        "%Q", "%R", "%S", "%T"
    };

    private CheckDigit checkDigit = CheckDigit.NONE;
    private double moduleWidthRatio = 2;

    /**
     * Sets the ratio of wide bar width to narrow bar width. Valid values are usually between
     * {@code 2} and {@code 3}. The default value is {@code 2}.
     *
     * @param moduleWidthRatio the ratio of wide bar width to narrow bar width
     */
    public void setModuleWidthRatio(double moduleWidthRatio) {
        this.moduleWidthRatio = moduleWidthRatio;
    }

    /**
     * Returns the ratio of wide bar width to narrow bar width.
     *
     * @return the ratio of wide bar width to narrow bar width
     */
    public double getModuleWidthRatio() {
        return moduleWidthRatio;
    }

    /**
     * Sets the check digit scheme (no check digit, or a modulo-43 check digit). By default, no check digit is added.
     *
     * @param checkDigit the check digit scheme
     */
    public void setCheckDigit(CheckDigit checkDigit) {
        this.checkDigit = checkDigit;
    }

    /**
     * Returns the check digit scheme (no check digit, or a modulo-43 check digit). By default, no check digit is added.
     *
     * @return the check digit scheme
     */
    public CheckDigit getCheckDigit() {
        return checkDigit;
    }

    @Override
    protected void encode() {

        if (!content.matches("[\u0000-\u007F]+")) {
            throw OkapiInputException.invalidCharactersInInput();
        }

        StringBuilder s = new StringBuilder(content.length() * 2);
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            s.append(E_CODE_39[c]);
        }

        Code3Of9 code39 = new Code3Of9();
        if (checkDigit == CheckDigit.MOD43) {
            code39.setCheckDigit(Code3Of9.CheckDigit.MOD43);
        }
        code39.setModuleWidthRatio(moduleWidthRatio);
        code39.setContent(s.toString());

        readable = content;
        pattern = new String[] { code39.pattern[0] };
        row_count = 1;
        row_height = new int[] { -1 };
    }

    /** {@inheritDoc} */
    @Override
    protected double getModuleWidth(int originalWidth) {
        if (originalWidth == 1) {
            return 1;
        } else {
            return moduleWidthRatio;
        }
    }
}
