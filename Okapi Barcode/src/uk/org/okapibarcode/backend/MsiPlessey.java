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
public class MsiPlessey extends Symbol {

    String MSI_PlessTable[] = {
        "12121212", "12121221", "12122112", "12122121", "12211212", "12211221", 
        "12212112", "12212121", "21121212", "21121221"
    };

    public boolean encode() {
        if (!(content.matches("[0-9]+?"))) {
            error_msg = "Invalid characters in input";
            return false;
        }

        String p = "";
        String dest;
        int l = content.length();
        dest = "21"; // Start
        for (int i = 0; i < l; i++) {
            p += MSI_PlessTable[Character.getNumericValue(content.charAt(i))];
        }
        dest += p;
        dest += "121"; // Stop
        readable = content;
        pattern = new String[1];
        pattern[0] = dest;
        row_count = 1;
        row_height = new int[1];
        row_height[0] = -1;
        plotSymbol();
        return true;
    }
}
