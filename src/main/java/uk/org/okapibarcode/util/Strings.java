/*
 * Copyright 2018 Daniel Gredler
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

package uk.org.okapibarcode.util;

import java.nio.charset.StandardCharsets;

import uk.org.okapibarcode.backend.OkapiException;

/**
 * String utility class.
 *
 * @author Daniel Gredler
 */
public final class Strings {

    private Strings() {
        // utility class
    }

    /**
     * Replaces any special placeholders with their raw values (not including FNC values).
     *
     * @param s the string to check for placeholders
     * @param lenient whether or not to be lenient with unrecognized escape sequences
     * @return the specified string, with placeholders replaced
     * @see <a href="http://www.zint.org.uk/Manual.aspx?type=p&page=4">Zint placeholders</a>
     */
    public static String replacePlaceholders(String s, boolean lenient) {
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c != '\\') {
                sb.append(c);
            } else {
                if (i + 1 >= s.length()) {
                    String msg = "Error processing escape sequences: expected escape character, found end of string";
                    throw new OkapiException(msg);
                } else {
                    char c2 = s.charAt(i + 1);
                    switch (c2) {
                        case '0':
                            sb.append('\u0000'); // null
                            i++;
                            break;
                        case 'E':
                            sb.append('\u0004'); // end of transmission
                            i++;
                            break;
                        case 'a':
                            sb.append('\u0007'); // bell
                            i++;
                            break;
                        case 'b':
                            sb.append('\u0008'); // backspace
                            i++;
                            break;
                        case 't':
                            sb.append('\u0009'); // horizontal tab
                            i++;
                            break;
                        case 'n':
                            sb.append('\n'); // line feed
                            i++;
                            break;
                        case 'v':
                            sb.append('\u000b'); // vertical tab
                            i++;
                            break;
                        case 'f':
                            sb.append('\u000c'); // form feed
                            i++;
                            break;
                        case 'r':
                            sb.append('\r'); // carriage return
                            i++;
                            break;
                        case 'e':
                            sb.append('\u001b'); // escape
                            i++;
                            break;
                        case 'G':
                            sb.append('\u001d'); // group separator
                            i++;
                            break;
                        case 'R':
                            sb.append('\u001e'); // record separator
                            i++;
                            break;
                        case '\\':
                            sb.append('\\'); // escape the escape character
                            i++;
                            break;
                        case 'x':
                            if (i + 3 >= s.length()) {
                                String msg = "Error processing escape sequences: expected hex sequence, found end of string";
                                throw new OkapiException(msg);
                            } else {
                                char c3 = s.charAt(i + 2);
                                char c4 = s.charAt(i + 3);
                                if (isHex(c3) && isHex(c4)) {
                                    byte b = (byte) Integer.parseInt("" + c3 + c4, 16);
                                    sb.append(new String(new byte[] { b }, StandardCharsets.ISO_8859_1));
                                    i += 3;
                                } else {
                                    String msg = "Error processing escape sequences: expected hex sequence, found '" + c3 + c4 + "'";
                                    throw new OkapiException(msg);
                                }
                            }
                            break;
                        default:
                            if (lenient) {
                                sb.append(c);
                            } else {
                                throw new OkapiException("Error processing escape sequences: expected valid escape character, found '" + c2 + "'");
                            }
                    }
                }
            }
        }
        return sb.toString();
    }

    private static boolean isHex(char c) {
        return (c >= '0' && c <= '9') || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f');
    }
}
