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
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
/**
 * Generic barcode symbology class
 *
 * @author <a href="mailto:rstuart114@gmail.com">Robin Stuart</a>
 * @version 0.1
 */
public abstract class Symbol {
    private static final char[] TECHNETIUM = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
        '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
        'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '-',
        '.', ' ', '$', '/', '+', '%'
    };

    public String content;
    public String readable;
    public String[] pattern;
    public int row_count;
    public int[] row_height;
    public boolean debug = false;
    public String error_msg;
    public int symbol_height;
    public int symbol_width;
    public int default_height;
    public boolean gs1;
    public boolean hibc;
    public boolean readerInit;
    public String encodeInfo = "";

    public int option1;
    public int option2;

    public ArrayList < Rectangle > rect = new ArrayList < > ();
    public ArrayList < TextBox > txt = new ArrayList < > ();
    public ArrayList < Hexagon > hex = new ArrayList < > ();
    public ArrayList<Ellipse2D.Double> target = new ArrayList < > ();

    public Symbol() {
        readable = "";
        row_count = 0;
        error_msg = "";
        default_height = 40;
        symbol_height = 0;
        symbol_width = 0;
        gs1 = false;
    }

    public int positionOf(char thischar, char[] LookUp) {
        int i, outval = 0;

        for (i = 0; i < LookUp.length; i++) {
            if (thischar == LookUp[i]) {
                outval = i;
            }
        }
        return outval;
    }

    public String bin2pat(String bin) {
        boolean black;
        int i, l;
        String pat = "";

        black = true;
        l = 0;
        for (i = 0; i < bin.length(); i++) {
            if (black) {
                if (bin.charAt(i) == '1') {
                    l++;
                } else {
                    black = false;
                    pat += (char)(l + '0');
                    l = 1;
                }
            } else {
                if (bin.charAt(i) == '0') {
                    l++;
                } else {
                    black = true;
                    pat += (char)(l + '0');
                    l = 1;
                }
            }
        }
        pat += (char)(l + '0');

        return pat;
    }

    public boolean setContent(String input_data) {
        int i;

        content = input_data; // default action

        if (gs1) {
            content = gs1SanityCheck(input_data);
        }

        if (gs1) {
            readable = "";
            for (i = 0; i < input_data.length(); i++) {
                switch(input_data.charAt(i)) {
                    case '[': readable += '(';
                        break;
                    case ']': readable += ')';
                        break;
                    default: readable += input_data.charAt(i);
                        break;
                }
            }
        }

        if (hibc) {
            content = hibcProcess(input_data);
        }

        if (content.length() != 0) {
            return encode();
        } else {
            return false;
        }
    }

    abstract boolean encode();

    public void plotSymbol() {
        int xBlock, yBlock;
        int x, y, w, h;
        boolean black;

        rect.clear();
        txt.clear();
        y = 0;
        h = 0;
        for (yBlock = 0; yBlock < row_count; yBlock++) {
            black = true;
            x = 0;
            for (xBlock = 0; xBlock < pattern[yBlock].length(); xBlock++) {
                if (black == true) {
                    black = false;
                    w = pattern[yBlock].charAt(xBlock) - '0';
                    if (row_height[yBlock] == -1) {
                        h = default_height;
                    } else {
                        h = row_height[yBlock];
                    }
                    Rectangle thisrect = new Rectangle(x, y, w, h);
                    if ((w != 0.0) && (h != 0.0)) {
                        rect.add(thisrect);
                    }
                    if ((x + w) > symbol_width) {
                        symbol_width = x + w;
                    }
                } else {
                    black = true;
                }
                x += (double)(pattern[yBlock].charAt(xBlock) - '0');
            }
            if ((y + h) > symbol_height) {
                symbol_height = y + h;
            }
            y += h;
        }

        if (!(readable.isEmpty())) {
            TextBox thistext = new TextBox();
            // Calculated position is approximately central
            thistext.setvalues(((symbol_width - (5.0 * readable.length())) / 2),
                    symbol_height + 8.0, readable);
            txt.add(thistext);
        }
    }

    public String gs1SanityCheck(String source) {
        // Enforce compliance with GS1 General Specification
        // http://www.gs1.org/docs/gsmp/barcodes/GS1_General_Specifications.pdf

        String reduced = "";

        int i, j, last_ai;
        boolean ai_latch;
        String ai_string;
        int bracket_level, max_bracket_level, ai_length, max_ai_length, min_ai_length;
        int[] ai_value = new int[100];
        int[] ai_location = new int[100];
        int ai_count;
        int[] data_location = new int[100];
        int[] data_length = new int[100];
        int src_len = source.length();
        int error_latch;

        /* Detect extended ASCII characters */
        for (i = 0; i < src_len; i++) {
            if (source.charAt(i) >= 128) {
                error_msg += "Extended ASCII characters are not supported by GS1";
                return "";
            }
            if (source.charAt(i) < 32) {
                error_msg += "Control characters are not supported by GS1";
                return "";
            }
        }

        if (source.charAt(0) != '[') {
            error_msg += "Data does not start with an AI";
            return "";
        }

        /* Check the position of the brackets */
        bracket_level = 0;
        max_bracket_level = 0;
        ai_length = 0;
        max_ai_length = 0;
        min_ai_length = 5;
        j = 0;
        ai_latch = false;
        for (i = 0; i < src_len; i++) {
            ai_length += j;
            if (((j == 1) && (source.charAt(i) != ']'))
                    && ((source.charAt(i) < '0') || (source.charAt(i) > '9'))) {
                ai_latch = true;
            }
            if (source.charAt(i) == '[') {
                bracket_level++;
                j = 1;
            }
            if (source.charAt(i) == ']') {
                bracket_level--;
                if (ai_length < min_ai_length) {
                    min_ai_length = ai_length;
                }
                j = 0;
                ai_length = 0;
            }
            if (bracket_level > max_bracket_level) {
                max_bracket_level = bracket_level;
            }
            if (ai_length > max_ai_length) {
                max_ai_length = ai_length;
            }
        }
        min_ai_length--;

        if (bracket_level != 0) {
            /* Not all brackets are closed */
            error_msg += "Malformed AI in input data (brackets don't match)";
            return "";
        }

        if (max_bracket_level > 1) {
            /* Nested brackets */
            error_msg += "Found nested brackets in input data";
            return "";
        }

        if (max_ai_length > 4) {
            /* AI is too long */
            error_msg += "Invalid AI in input data (AI too long)";
            return "";
        }

        if (min_ai_length <= 1) {
            /* AI is too short */
            error_msg += "Invalid AI in input data (AI too short)";
            return "";
        }

        if (ai_latch) {
            /* Non-numeric data in AI */
            error_msg += "Invalid AI in input data (non-numeric characters in AI)";
            return "";
        }

        ai_count = 0;
        for (i = 1; i < src_len; i++) {
            if (source.charAt(i - 1) == '[') {
                ai_location[ai_count] = i;
                ai_value[ai_count] = 0;
                for (j = 0; source.charAt(i + j) != ']'; j++) {
                    ai_value[ai_count] *= 10;
                    ai_value[ai_count] += Character.getNumericValue(source.charAt(i + j));
                }
                ai_count++;
            }
        }

        for (i = 0; i < ai_count; i++) {
            data_location[i] = ai_location[i] + 3;
            if (ai_value[i] >= 100) {
                data_location[i]++;
            }
            if (ai_value[i] >= 1000) {
                data_location[i]++;
            }
            data_length[i] = source.length() - data_location[i];
            for(j = source.length() - 1; j >= data_location[i]; j--) {
                if (source.charAt(j) == '[') {
                    data_length[i] = j - data_location[i];
                }
            }
        }

        for (i = 0; i < ai_count; i++) {
            if (data_length[i] == 0) {
                /* No data for given AI */
                error_msg += "Empty data field in input data";
                return "";
            }
        }

        error_latch = 0;
        ai_string = "";
        for (i = 0; i < ai_count; i++) {
            switch (ai_value[i]) {
            case 0:
                if (data_length[i] != 18) {
                    error_latch = 1;
                }
                break;
            case 1:
            case 2:
            case 3:
                if (data_length[i] != 14) {
                    error_latch = 1;
                }
                break;
            case 4:
                if (data_length[i] != 16) {
                    error_latch = 1;
                }
                break;
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
            case 19:
                if (data_length[i] != 6) {
                    error_latch = 1;
                }
                break;
            case 20:
                if (data_length[i] != 2) {
                    error_latch = 1;
                }
                break;
            case 23:
            case 24:
            case 25:
            case 39:
            case 40:
            case 41:
            case 42:
            case 70:
            case 80:
            case 81:
                error_latch = 2;
                break;
            }
            if (
            ((ai_value[i] >= 100) && (ai_value[i] <= 179))
                    || ((ai_value[i] >= 1000) && (ai_value[i] <= 1799))
                    || ((ai_value[i] >= 200) && (ai_value[i] <= 229))
                    || ((ai_value[i] >= 2000) && (ai_value[i] <= 2299))
                    || ((ai_value[i] >= 300) && (ai_value[i] <= 309))
                    || ((ai_value[i] >= 3000) && (ai_value[i] <= 3099))
                    || ((ai_value[i] >= 31) && (ai_value[i] <= 36))
                    || ((ai_value[i] >= 310) && (ai_value[i] <= 369))) {
                error_latch = 2;
            }
            if ((ai_value[i] >= 3100) && (ai_value[i] <= 3699)) {
                if (data_length[i] != 6) {
                    error_latch = 1;
                }
            }
            if (
            ((ai_value[i] >= 370) && (ai_value[i] <= 379))
                    || ((ai_value[i] >= 3700) && (ai_value[i] <= 3799))) {
                error_latch = 2;
            }
            if ((ai_value[i] >= 410) && (ai_value[i] <= 415)) {
                if (data_length[i] != 13) {
                    error_latch = 1;
                }
            }
            if (
            ((ai_value[i] >= 4100) && (ai_value[i] <= 4199))
                    || ((ai_value[i] >= 700) && (ai_value[i] <= 703))
                    || ((ai_value[i] >= 800) && (ai_value[i] <= 810))
                    || ((ai_value[i] >= 900) && (ai_value[i] <= 999))
                    || ((ai_value[i] >= 9000) && (ai_value[i] <= 9999))) {
                error_latch = 2;
            }

            if (error_latch == 1) {
                error_msg = "Invalid data length for AI";
                return "";
            }

            if (error_latch == 2) {
                error_msg = "Invalid AI value";
                return "";
            }
        }

        /* Resolve AI data - put resulting string in 'reduced' */
        j = 0;
        last_ai = 0;
        ai_latch = false;
        for (i = 0; i < src_len; i++) {
            if ((source.charAt(i) != '[') && (source.charAt(i) != ']')) {
                reduced += source.charAt(i);
            }
            if (source.charAt(i) == '[') {
                /* Start of an AI string */
                if (ai_latch) {
                    reduced += '[';
                }
                last_ai = (10 * Character.getNumericValue(source.charAt(i + 1)))
                        + Character.getNumericValue(source.charAt(i + 2));
                System.out.println("last AI " + last_ai);
                if ( ((last_ai >= 0) && (last_ai <= 4))
                        || ((last_ai >= 11) && (last_ai <= 20))
                        || (last_ai == 23) /* legacy support - see 5.3.8.2.2 */
                        || ((last_ai >= 31) && (last_ai <= 36))
                        || (last_ai == 41)) {
                    // The end of the current data block doesn't need FNC1
                    ai_latch = false;
                } else {
                    // The end of the current data block does need FNC1
                    ai_latch = true;
                }
            }
            /* The ']' character is simply dropped from the input */
        }

        /* the character '[' in the reduced string refers to the FNC1 character */
        return reduced;
    }

    public String hibcProcess(String source) {
	int counter, i;
        String to_process;
        char check_digit;

	if(source.length() > 36) {
		error_msg = "Data too long for HIBC LIC";
		return "";
	}
	source = source.toUpperCase();
        if (!(source.matches("[A-Z0-9-\\. \\$/+\\%]+?"))) {
            error_msg = "Invalid characters in input";
            return "";
        }

	counter = 41;
	for(i = 0; i < source.length(); i++) {
            counter += positionOf(source.charAt(i), TECHNETIUM);
	}
	counter = counter % 43;

	if(counter < 10) {
		check_digit = (char) (counter + '0');
	} else {
		if(counter < 36) {
			check_digit = (char) ((counter - 10) + 'A');
		} else {
			switch(counter) {
				case 36: check_digit = '-'; break;
				case 37: check_digit = '.'; break;
				case 38: check_digit = ' '; break;
				case 39: check_digit = '$'; break;
				case 40: check_digit = '/'; break;
				case 41: check_digit = '+'; break;
				case 42: check_digit = '%'; break;
				default: check_digit = ' '; break; /* Keep compiler happy */
			}
		}
	}

        encodeInfo += "HIBC Check Digit: " + counter + " (" + check_digit + ")\n";

	to_process = "+" + source + check_digit;
        return to_process;
    }

    /**
     * Returns the intermediate coding of this bar code. Symbol types that use the test
     * infrastructure should override this method.
     *
     * @return the intermediate coding of this bar code
     */
    protected int[] getCodewords() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns this bar code's pattern, converted into a set of corresponding codewords.
     * Useful for bar codes that encode their content as a pattern.
     *
     * @param size the number of digits in each codeword
     * @return this bar code's pattern, converted into a set of corresponding codewords
     */
    protected int[] getPatternAsCodewords(int size) {
        if (pattern == null || pattern.length == 0) {
            return new int[0];
        } else {
            int count = pattern[0].length() / size;
            int[] codewords = new int[pattern.length * count];
            for (int i = 0; i < pattern.length; i++) {
                String row = pattern[i];
                for (int j = 0; j < count; j++) {
                    codewords[(i * count) + j] = Integer.parseInt(row.substring(j * size, (j + 1) * size));
                }
            }
            return codewords;
        }
    }
}
