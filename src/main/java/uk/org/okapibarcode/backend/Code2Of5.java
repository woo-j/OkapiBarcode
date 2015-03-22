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

import java.awt.Rectangle;

/**
 *
 * @author <a href="mailto:jakel2006@me.com">Robert Elliott</a>
 */
public class Code2Of5 extends Symbol {

    private enum tof_mode {
        MATRIX, INDUSTRIAL, IATA, DATA_LOGIC, INTERLEAVED, ITF14, DPLEIT, DPIDENT
    }
    private tof_mode mode;

    private String[] C25MatrixTable = {
        "113311", "311131", "131131", "331111", "113131", "313111", "133111", "111331", "311311", "131311"
    };
    private String[] C25IndustTable = {
        "1111313111", "3111111131", "1131111131", "3131111111", "1111311131", "3111311111", "1131311111", "1111113131", "3111113111", "1131113111"
    };
    private String[] C25InterTable = {
        "11331", "31113", "13113", "33111", "11313", "31311", "13311", "11133", "31131", "13131"
    };

    public Code2Of5() {
        mode = tof_mode.MATRIX;
    }

    public void setMatrixMode() {
        mode = tof_mode.MATRIX;
    }

    public void setIndustrialMode() {
        mode = tof_mode.INDUSTRIAL;
    }

    public void setIATAMode() {
        mode = tof_mode.IATA;
    }

    public void setDataLogicMode() {
        mode = tof_mode.DATA_LOGIC;
    }

    public void setInterleavedMode() {
        mode = tof_mode.INTERLEAVED;
    }

    public void setITF14Mode() {
        mode = tof_mode.ITF14;
    }

    public void setDPLeitMode() {
        mode = tof_mode.DPLEIT;
    }

    public void setDPIdentMode() {
        mode = tof_mode.DPIDENT;
    }

    @Override
    public boolean encode() {
        boolean retval = false;

        switch (mode) {
        case MATRIX:
            retval = dataMatrixTof();
            break;
        case INDUSTRIAL:
            retval = industrialTof();
            break;
        case IATA:
            retval = iataTof();
            break;
        case INTERLEAVED:
            retval = interleavedTof();
            break;
        case DATA_LOGIC:
            retval = dataLogic();
            break;
        case ITF14:
            retval = itf14();
            break;
        case DPLEIT:
            retval = deutschePostLeitcode();
            break;
        case DPIDENT:
            retval = deutschePostIdentcode();
            break;
        }

        if (retval && debug) {
            System.out.println("Calculated: " + readable);

        } else {
            System.out.println("2 of 5 FAIL");
        }

        if (retval == true) {
            plotSymbol();
        }
        return retval;
    }

    private boolean dataMatrixTof() {

        if (!(content.matches("[0-9]+?"))) {
            error_msg = "Invalid characters in input";
            return false;
        }

        String dest = "411111";

        for (int i = 0; i < content.length(); i++) {
            dest += C25MatrixTable[Character.getNumericValue(content.charAt(i))];
        }
        dest += "41111";

        readable = content;
        pattern = new String[1];
        pattern[0] = dest;
        row_count = 1;
        row_height = new int[1];
        row_height[0] = -1;
        return true;
    }

    private boolean industrialTof() {
        if (!(content.matches("[0-9]+?"))) {
            error_msg = "Invalid characters in input";
            return false;
        }

        String dest = "313111";
        readable = content;

        for (int i = 0; i < readable.length(); i++) {
            dest += C25IndustTable[Character.getNumericValue(readable.charAt(i))];
        }

        dest += "31113";

        pattern = new String[1];
        pattern[0] = dest;
        row_count = 1;
        row_height = new int[1];
        row_height[0] = -1;
        return true;
    }

    private boolean iataTof() {
        if (!(content.matches("[0-9]+?"))) {
            error_msg = "Invalid characters in input";
            return false;
        }

        String dest = "1111";
        readable = content;

        for (int i = 0; i < readable.length(); i++) {
            dest += C25IndustTable[Character.getNumericValue(readable.charAt(i))];
        }

        dest += "311";

        pattern = new String[1];
        pattern[0] = dest;
        row_count = 1;
        row_height = new int[1];
        row_height[0] = -1;
        return true;
    }

    private boolean dataLogic() {
        if (!(content.matches("[0-9]+?"))) {
            error_msg = "Invalid characters in input";
            return false;
        }

        String dest = "1111";
        readable = content;

        for (int i = 0; i < readable.length(); i++) {
            dest += C25MatrixTable[Character.getNumericValue(readable.charAt(i))];
        }

        dest += "311";

        pattern = new String[1];
        pattern[0] = dest;
        row_count = 1;
        row_height = new int[1];
        row_height[0] = -1;
        return true;
    }

    private boolean interleavedTof() {
        int i;
        String dest;

        if ((content.length() & 1) == 0) {
            readable = content;
        } else {
            readable = "0" + content;
        }
        if (!(readable.matches("[0-9]+?"))) {
            error_msg = "Invalid characters in input";
            return false;
        }

        dest = "1111";

        for (i = 0; i < readable.length(); i += 2) {
            dest += interlace(i, i + 1);
        }

        dest += "211";

        pattern = new String[1];
        pattern[0] = dest;
        row_count = 1;
        row_height = new int[1];
        row_height[0] = -1;
        return true;
    }

    private String interlace(int x, int y) {
        char a = readable.charAt(x);
        char b = readable.charAt(y);

        String one = C25InterTable[Character.getNumericValue(a)];
        String two = C25InterTable[Character.getNumericValue(b)];
        String f = "";

        for (int i = 0; i < 5; i++) {
            f += one.charAt(i);
            f += two.charAt(i);

        }

        return f;
    }

    private boolean itf14() {
        int i, count = 0;
        int input_length = content.length();
        String dest;

        if (!(content.matches("[0-9]+?"))) {
            error_msg = "Invalid characters in input";
            return false;
        }

        if (input_length > 13) {
            error_msg = "Input data too long";
            return false;
        }

        readable = "";
        for (i = input_length; i < 13; i++) {
            readable += "0";
        }

        readable += content;
        for (i = 12; i >= 0; i--) {
            count += readable.charAt(i) - '0';

            if ((i & 1) == 0) {
                count += 2 * (readable.charAt(i) - '0');
            }
        }

        readable += (char)(((10 - (count % 10)) % 10) + '0');
        encodeInfo += "Check Digit: " + (char)(((10 - (count % 10)) % 10) + '0');
        encodeInfo += '\n';

        dest = "1111";

        for (i = 0; i < readable.length(); i += 2) {
            dest += interlace(i, i + 1);
        }

        dest += "211";

        pattern = new String[1];
        pattern[0] = dest;
        row_count = 1;
        row_height = new int[1];
        row_height[0] = -1;
        return true;
    }

    private boolean deutschePostLeitcode() {
        int i, count = 0;
        int input_length = content.length();
        String dest;

        if (!(content.matches("[0-9]+?"))) {
            error_msg = "Invalid characters in input";
            return false;
        }

        if (input_length > 13) {
            error_msg = "Input data too long";
            return false;
        }

        readable = "";
        for (i = input_length; i < 13; i++) {
            readable += "0";
        }

        readable += content;

        for (i = 12; i >= 0; i--) {
            count += 4 * (readable.charAt(i) - '0');

            if ((i & 1) != 0) {
                count += 5 * (readable.charAt(i) - '0');
            }
        }

        readable += (char)(((10 - (count % 10)) % 10) + '0');
        encodeInfo += "Check digit: " + (char)(((10 - (count % 10)) % 10) + '0');
        encodeInfo += '\n';

        dest = "1111";

        for (i = 0; i < readable.length(); i += 2) {
            dest += interlace(i, i + 1);
        }

        dest += "211";

        pattern = new String[1];
        pattern[0] = dest;
        row_count = 1;
        row_height = new int[1];
        row_height[0] = -1;
        return true;
    }

    private boolean deutschePostIdentcode() {
        int i, count = 0;
        int input_length = content.length();
        String dest;

        if (!(content.matches("[0-9]+?"))) {
            error_msg = "Invalid characters in input";
            return false;
        }

        if (input_length > 11) {
            error_msg = "Input data too long";
            return false;
        }

        readable = "";
        for (i = input_length; i < 11; i++) {
            readable += "0";
        }

        readable += content;
        for (i = 10; i >= 0; i--) {
            count += 4 * (readable.charAt(i) - '0');

            if ((i & 1) != 0) {
                count += 5 * (readable.charAt(i) - '0');
            }
        }

        readable += (char)(((10 - (count % 10)) % 10) + '0');
        encodeInfo += "Check Digit: " + (char)(((10 - (count % 10)) % 10) + '0');
        encodeInfo += '\n';

        dest = "1111";

        for (i = 0; i < readable.length(); i += 2) {
            dest += interlace(i, i + 1);
        }

        dest += "211";

        pattern = new String[1];
        pattern[0] = dest;
        row_count = 1;
        row_height = new int[1];
        row_height[0] = -1;
        return true;
    }

    @Override
    public void plotSymbol() {
        int xBlock;
        int x, y, w, h;
        boolean black;
        int offset = 0;

        rect.clear();
        txt.clear();
        y = 0;
        h = 0;
        black = true;
        x = 0;
        if (mode == tof_mode.ITF14) {
            offset = 20;
        }
        for(xBlock = 0; xBlock < pattern[0].length(); xBlock++) {
            if (black == true) {
                black = false;
                w = pattern[0].charAt(xBlock) - '0';
                if(row_height[0] == -1) {
                    h = default_height;
                } else {
                    h = row_height[0];
                }
                Rectangle thisrect = new Rectangle(x + offset, y, w, h);
                if((w != 0.0) && (h != 0.0)) {
                    rect.add(thisrect);
                }
                symbol_width = x + w + (2 * offset);
            } else {
                black = true;
            }
            x += (double) (pattern[0].charAt(xBlock) - '0');
        }
        symbol_height = h;

        if (mode == tof_mode.ITF14) {
            // Add bounding box
            Rectangle topBar = new Rectangle(0, 0, symbol_width, 4);
            Rectangle bottomBar = new Rectangle(0, symbol_height - 4, symbol_width, 4);
            Rectangle leftBar = new Rectangle(0, 0, 4, symbol_height);
            Rectangle rightBar = new Rectangle(symbol_width - 4, 0, 4, symbol_height);
            rect.add(topBar);
            rect.add(bottomBar);
            rect.add(leftBar);
            rect.add(rightBar);
        }
        if (!(readable.isEmpty())) {
            // Calculated position is approximately central
            TextBox text = new TextBox(((symbol_width - (5.0 * readable.length())) / 2), symbol_height + 8.0, readable);
            txt.add(text);
        }
    }
}
