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

import java.awt.geom.Rectangle2D;

/**
 * Implements the <a href="http://auspost.com.au/media/documents/a-guide-to-printing-the-4state-barcode-v31-mar2012.pdf">Australia Post 4-State barcode</a>.
 *
 * @author <a href="mailto:rstuart114@gmail.com">Robin Stuart</a>
 */
public class AustraliaPost extends Symbol {

    private static final char[] CHARACTER_SET = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D',
        'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V',
        'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
        'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', ' ', '#'
    };

    private static final String[] N_ENCODING_TABLE = {
        "00", "01", "02", "10", "11", "12", "20", "21", "22", "30"
    };

    private static final String[] C_ENCODING_TABLE = {
        "222", "300", "301", "302", "310", "311", "312", "320", "321", "322",
        "000", "001", "002", "010", "011", "012", "020", "021", "022", "100", "101", "102", "110",
        "111", "112", "120", "121", "122", "200", "201", "202", "210", "211", "212", "220", "221",
        "023", "030", "031", "032", "033", "103", "113", "123", "130", "131", "132", "133", "203",
        "213", "223", "230", "231", "232", "233", "303", "313", "323", "330", "331", "332", "333",
        "003", "013"
    };

    private static final String[] BAR_VALUE_TABLE = {
        "000", "001", "002", "003", "010", "011", "012", "013", "020", "021",
        "022", "023", "030", "031", "032", "033", "100", "101", "102", "103", "110", "111", "112",
        "113", "120", "121", "122", "123", "130", "131", "132", "133", "200", "201", "202", "203",
        "210", "211", "212", "213", "220", "221", "222", "223", "230", "231", "232", "233", "300",
        "301", "302", "303", "310", "311", "312", "313", "320", "321", "322", "323", "330", "331",
        "332", "333"
    };

    private enum ausMode {AUSPOST, AUSREPLY, AUSROUTE, AUSREDIRECT};

    private ausMode mode;

    public AustraliaPost() {
        mode = ausMode.AUSPOST;
    }

    /**
     * Specify encoding of Australia Post Standard Customer Barcode,
     * Customer Barcode 2 or Customer Barcode 3 (37-bar, 52-bar and 67-bar
     * symbols) depending on input data length. Valid data characters are 0-9,
     * A-Z, a-z, space and hash (#). A Format Control Code (FCC) is added and
     * should not be included in the input data.
     * <p>
     * Input data should include a 8-digit Deliver Point ID
     * (DPID) optionally followed by customer information as shown below.
     * <table summary="Permitted Australia Post input data">
        <tbody>
          <tr>
            <th><p>Input Length</p></th>
            <th><p>Required Input Format</p></th>
            <th><p>Symbol Length</p></th>
            <th><p>FCC</p></th>
            <th><p>Encoding Table</p></th>
          </tr>
          <tr>
            <td><p>8</p></td>
            <td><p>99999999</p></td>
            <td><p>37-bar</p></td>
            <td><p>11</p></td>
            <td><p>None</p></td>
          </tr>
          <tr>
            <td><p>13</p></td>
            <td><p>99999999AAAAA</p></td>
            <td><p>52-bar</p></td>
            <td><p>59</p></td>
            <td><p>C</p></td>
          </tr>
          <tr>
            <td><p>16</p></td>
            <td><p>9999999999999999</p></td>
            <td><p>52-bar</p></td>
            <td><p>59</p></td>
            <td><p>N</p></td>
          </tr>
          <tr>
            <td><p>18</p></td>
            <td><p>99999999AAAAAAAAAA</p></td>
            <td><p>67-bar</p></td>
            <td><p>62</p></td>
            <td><p>C</p></td>
          </tr>
          <tr>
            <td><p>23</p></td>
            <td><p>99999999999999999999999</p></td>
            <td><p>67-bar</p></td>
            <td><p>62</p></td>
            <td><p>N</p></td>
          </tr>
        </tbody>
      </table>
     */
    public void setPostMode() {
        mode = ausMode.AUSPOST;
    }

    /**
     * Specify encoding of a Reply Paid version of the Australia Post
     * 4-State Barcode (FCC 45) which requires an 8-digit DPID input.
     */
    public void setReplyMode() {
        mode = ausMode.AUSREPLY;
    }

    /**
     * Specify encoding of a Routing version of the Australia Post 4-State
     * Barcode (FCC 87) which requires an 8-digit DPID input.
     */
    public void setRouteMode() {
        mode = ausMode.AUSROUTE;
    }

    /**
     * Specify encoding of a Redirection version of the Australia Post 4-State
     * Barcode (FCC 92) which requires an 8-digit DPID input.
     */
    public void setRedirectMode() {
        mode = ausMode.AUSREDIRECT;
    }

    /** {@inheritDoc} */
    @Override
    protected void encode() {
        String formatControlCode = "00";
        String deliveryPointId;
        String barStateValues;
        String zeroPaddedInput = "";
        int i;

        switch(mode) {
            case AUSPOST:
                switch(content.length()) {
                    case 8: formatControlCode = "11";
                        break;
                    case 13: formatControlCode = "59";
                        break;
                    case 16: formatControlCode = "59";
                        if (!content.matches("[0-9]+")) {
                            throw new OkapiException("Invalid characters in data");
                        }
                        break;
                    case 18: formatControlCode = "62";
                        break;
                    case 23: formatControlCode = "62";
                        if (!content.matches("[0-9]+")) {
                            throw new OkapiException("Invalid characters in data");
                        }
                        break;
                    default: throw new OkapiException("Auspost input is wrong length");
                }
                break;
            case AUSREPLY:
                if (content.length() > 8) {
                    throw new OkapiException("Auspost input is too long");
                } else {
                    formatControlCode = "45";
                }
                break;
            case AUSROUTE:
                if (content.length() > 8) {
                    throw new OkapiException("Auspost input is too long");
                } else {
                    formatControlCode = "87";
                }
                break;
            case AUSREDIRECT:
                if (content.length() > 8) {
                    throw new OkapiException("Auspost input is too long");
                } else {
                    formatControlCode = "92";
                }
                break;
        }

        encodeInfo += "FCC: " + formatControlCode + '\n';

        if(mode != ausMode.AUSPOST) {
            for (i = content.length(); i < 8; i++) {
                zeroPaddedInput += "0";
            }
        }
        zeroPaddedInput += content;

        if (!content.matches("[0-9A-Za-z #]+")) {
            throw new OkapiException("Invalid characters in data");
        }

        /* Verify that the first 8 characters are numbers */
        deliveryPointId = zeroPaddedInput.substring(0, 8);

        if (!deliveryPointId.matches("[0-9]+")) {
            throw new OkapiException("Invalid characters in DPID");
        }

        encodeInfo += "DPID: " + deliveryPointId + '\n';

        /* Start */
        barStateValues = "13";

        /* Encode the FCC */
        for(i = 0; i < 2; i++) {
            barStateValues += N_ENCODING_TABLE[formatControlCode.charAt(i) - '0'];
        }

        /* Delivery Point Identifier (DPID) */
        for(i = 0; i < 8; i++) {
            barStateValues += N_ENCODING_TABLE[deliveryPointId.charAt(i) - '0'];
        }

        /* Customer Information */
        switch(zeroPaddedInput.length()) {
            case 13:
            case 18:
                for(i = 8; i < zeroPaddedInput.length(); i++) {
                    barStateValues += C_ENCODING_TABLE[positionOf(zeroPaddedInput.charAt(i), CHARACTER_SET)];
                }
                break;
            case 16:
            case 23:
                for(i = 8; i < zeroPaddedInput.length(); i++) {
                    barStateValues += N_ENCODING_TABLE[positionOf(zeroPaddedInput.charAt(i), CHARACTER_SET)];
                }
                break;
        }

        /* Filler bar */
        switch(barStateValues.length()) {
            case 22:
            case 37:
            case 52:
                barStateValues += "3";
                break;
        }

        /* Reed Solomon error correction */
        barStateValues += calcReedSolomon(barStateValues);

        /* Stop character */
        barStateValues += "13";

        encodeInfo += "Total length: " + barStateValues.length() + '\n';
        encodeInfo += "Encoding: ";
        for (i = 0; i < barStateValues.length(); i++) {
            switch (barStateValues.charAt(i)) {
                case '1':
                    encodeInfo += "A";
                    break;
                case '2':
                    encodeInfo += "D";
                    break;
                case '0':
                    encodeInfo += "F";
                    break;
                case '3':
                    encodeInfo += "T";
                    break;
            }
        }
        encodeInfo += "\n";

        readable = "";
        pattern = new String[1];
        pattern[0] = barStateValues;
        row_count = 1;
        row_height = new int[1];
        row_height[0] = -1;
    }

    private String calcReedSolomon(String oldBarStateValues) {
        ReedSolomon rs = new ReedSolomon();
        String newBarStateValues = "";

        /* Adds Reed-Solomon error correction to auspost */

        int barStateCount;
        int tripleValueCount = 0;
        int[] tripleValue = new int[31];

        for(barStateCount = 2; barStateCount < oldBarStateValues.length(); barStateCount += 3, tripleValueCount++) {
            tripleValue[tripleValueCount] = barStateToDecimal(oldBarStateValues.charAt(barStateCount), 4)
                    + barStateToDecimal(oldBarStateValues.charAt(barStateCount + 1), 2)
                    + barStateToDecimal(oldBarStateValues.charAt(barStateCount + 2), 0);
	}

        rs.init_gf(0x43);
        rs.init_code(4, 1);
        rs.encode(tripleValueCount, tripleValue);

        for(barStateCount = 4; barStateCount > 0; barStateCount--) {
            newBarStateValues += BAR_VALUE_TABLE[rs.getResult(barStateCount - 1)];
        }

        return newBarStateValues;
    }

    private int barStateToDecimal (char oldBarStateValues, int shift) {
        return (oldBarStateValues - '0') << shift;
    }

    /** {@inheritDoc} */
    @Override
    protected void plotSymbol() {
        int xBlock;
        int x, y, w, h;

        rectangles.clear();
        x = 0;
        w = 1;
        y = 0;
        h = 0;
        for(xBlock = 0; xBlock < pattern[0].length(); xBlock++) {
            switch(pattern[0].charAt(xBlock)) {
                case '1':
                    y = 0;
                    h = 5;
                    break;
                case '2':
                    y = 3;
                    h = 5;
                    break;
                case '0':
                    y = 0;
                    h = 8;
                    break;
                case '3':
                    y = 3;
                    h = 2;
                    break;
            }

            Rectangle2D.Double rect = new Rectangle2D.Double(x, y, w, h);
            rectangles.add(rect);

            x += 2;
        }

        symbol_width = ((pattern[0].length() - 1) * 2) + 1; // no whitespace needed after the final bar
        symbol_height = 8;
    }
}
