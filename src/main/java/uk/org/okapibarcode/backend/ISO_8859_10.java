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
 * Convert a String to ISO 8859-10 (Latin-6) encoding
 *
 * @author Robin Stuart <rstuart114@gmail.com>
 */
public class ISO_8859_10 {
    
    private final static String byteToCharTable = 

        "\u0000\u0001\u0002\u0003\u0004\u0005\u0006\u0007" +     // 0x00 - 0x07
        "\b\t\n\u000B\f\r\u000E\u000F" +     			// 0x08 - 0x0F
        "\u0010\u0011\u0012\u0013\u0014\u0015\u0016\u0017" +     // 0x10 - 0x17
        "\u0018\u0019\u001A\u001B\u001C\u001D\u001E\u001F" +     // 0x18 - 0x1F
        "\u0020\u0021\"\u0023\u0024\u0025\u0026\'" +     	// 0x20 - 0x27
        "\u0028\u0029\u002A\u002B\u002C\u002D\u002E\u002F" +     // 0x28 - 0x2F
        "\u0030\u0031\u0032\u0033\u0034\u0035\u0036\u0037" +     // 0x30 - 0x37
        "\u0038\u0039\u003A\u003B\u003C\u003D\u003E\u003F" +     // 0x38 - 0x3F
        "\u0040\u0041\u0042\u0043\u0044\u0045\u0046\u0047" +     // 0x40 - 0x47
        "\u0048\u0049\u004A\u004B\u004C\u004D\u004E\u004F" +     // 0x48 - 0x4F
        "\u0050\u0051\u0052\u0053\u0054\u0055\u0056\u0057" +     // 0x50 - 0x57
        "\u0058\u0059\u005A\u005B\\\u005D\u005E\u005F" +     	// 0x58 - 0x5F
        "\u0060\u0061\u0062\u0063\u0064\u0065\u0066\u0067" +     // 0x60 - 0x67
        "\u0068\u0069\u006A\u006B\u006C\u006D\u006E\u006F" +     // 0x68 - 0x6F
        "\u0070\u0071\u0072\u0073\u0074\u0075\u0076\u0077" +     // 0x70 - 0x77
        "\u0078\u0079\u007A\u007B\u007C\u007D\u007E\u007F" +     // 0x78 - 0x7F	

        "\u0080\u0081\u0082\u0083\u0084\u0085\u0086\u0087" +     // 0x80 - 0x87
        "\u0088\u0089\u008A\u008B\u008C\u008D\u008E\u008F" +     // 0x88 - 0x8F
        "\u0090\u0091\u0092\u0093\u0094\u0095\u0096\u0097" +     // 0x90 - 0x97
        "\u0098\u0099\u009A\u009B\u009C\u009D\u009E\u009F" +     // 0x98 - 0x9F
        "\u00A0\u0104\u0112\u0122\u012A\u012B\u0136\u00A7" +     // 0xA0 - 0xA7
        "\u013B\u0110\u0160\u0166\u017D\u00AD\u016A\u014A" +     // 0xA8 - 0xAF
        "\u00B0\u0105\u0113\u0123\u012B\u0129\u0137\u00B7" +     // 0xB0 - 0xB7
        "\u013C\u0111\u0161\u0167\u017E\u2015\u016B\u014B" +     // 0xB8 - 0xBF
        "\u0100\u00C1\u00C2\u00C3\u00C4\u00C5\u00C6\u012E" +     // 0xC0 - 0xC7
        "\u010C\u00C9\u0118\u00CB\u0116\u00CD\u00CE\u00CF" +     // 0xC8 - 0xCF
        "\u00D0\u0145\u014C\u00D3\u00D4\u00D5\u00D6\u0168" +     // 0xD0 - 0xD7
        "\u00D8\u0172\u00DA\u00DB\u00DC\u00DD\u00DE\u00DF" +     // 0xD8 - 0xDF
        "\u0101\u00E1\u00E2\u0103\u00E4\u00E5\u00E6\u012F" +     // 0xE0 - 0xE7
        "\u010D\u00E9\u0119\u00EB\u0117\u00ED\u00EE\u00EF" +     // 0xE8 - 0xEF
        "\u00F0\u0146\u014D\u00F3\u00F4\u00F5\u00F6\u0169" +     // 0xF0 - 0xF7
        "\u00F8\u0173\u00FA\u00FB\u00FC\u00FD\u00FE\u0138";      // 0xF8 - 0xFF
    
    /**
     * Convert input String to ISO 8859-10 byte code
     * @param input Data to be converted
     * @return Text in ISO 8859-10 byte format
     */
    public byte[] getBytes(String input) {
        int i;
        int nib;
        char c;
        byte thisChar[] = new byte[1];
        byte output[] = new byte[0];
        
        for(i = 0; i < input.length(); i++) {
            c = input.charAt(i);
            if (c <= 0x7f) {
                thisChar[0] = (byte) c;
            } else {
                for (nib = 0x80; nib <= 0xff; nib++) {
                    if (input.charAt(i) == byteToCharTable.charAt(nib)) {
                        thisChar[0] = (byte) nib;
                    }
                }
            }
            
            output = concatenateByteArrays(output, thisChar);
        }
        
        return output;
    }

    byte[] concatenateByteArrays(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length]; 
        System.arraycopy(a, 0, result, 0, a.length); 
        System.arraycopy(b, 0, result, a.length, b.length); 
        return result;
    }
    
}
