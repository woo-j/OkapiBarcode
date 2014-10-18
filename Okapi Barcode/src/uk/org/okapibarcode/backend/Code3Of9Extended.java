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
 *
 * @author Robin Stuart <rstuart114@gmail.com>
 */
public class Code3Of9Extended extends Symbol {

    String ECode39[] = {
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

    @Override
    public boolean encode() {
        String buffer = "";
        int l = content.length();
        int asciicode;
        Code3Of9 c = new Code3Of9();

        //FIXME: Filter out extended ASCII and Unicode
        if (!(content.matches("[\\x00-\\x7F]+"))) {
            error_msg = "Invalid characters in input";
            return false;
        }

        for (int i = 0; i < l; i++) {
            asciicode = (int) content.charAt(i);
            buffer += ECode39[asciicode];
        }

        if (!(c.setContent(buffer))) {
            error_msg = c.error_msg;
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
