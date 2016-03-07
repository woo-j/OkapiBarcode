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
 * Implements Code 3 of 9 Extended, also known as Code 39e and Code39+
 * <p>
 * Supports encoding of all characters in the 7-bit ASCII table. A
 * modulo-43 check digit can be added if required.
 *
 * @author <a href="mailto:rstuart114@gmail.com">Robin Stuart</a>
 */
public class Code3Of9Extended extends Symbol {

    private final String[] ECode39 = {
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

    public enum CheckDigit {
        NONE, MOD43
    }
    
    private CheckDigit checkOption;
    
    public Code3Of9Extended() {
        checkOption = CheckDigit.NONE;
    }
    
    /**
     * Select addition of optional Modulo-43 check digit or encoding without
     * check digit.
     * @param checkMode Check digit option
     */
    public void setCheckDigit(CheckDigit checkMode) {
        checkOption = checkMode;
    }
    
    @Override
    public boolean encode() {
        String buffer = "";
        int l = content.length();
        int asciicode;
        Code3Of9 c = new Code3Of9();
        
        if (checkOption == CheckDigit.MOD43) {
            c.setCheckDigit(Code3Of9.CheckDigit.MOD43);
        }

        if (!content.matches("[\u0000-\u007F]+")) {
            error_msg = "Invalid characters in input data";
            return false;
        }

        for (int i = 0; i < l; i++) {
            asciicode = content.charAt(i);
            buffer += ECode39[asciicode];
        }

        try {
            c.setContent(buffer);
        } catch (OkapiException e) {
            error_msg = e.getMessage();
            return false;
        }
        readable = content;
        pattern = new String[1];
        pattern[0] = c.pattern[0];
        row_count = 1;
        row_height = new int[1];
        row_height[0] = -1;
        plotSymbol();
        return true;
    }
}
