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

import static uk.org.okapibarcode.util.Arrays.positionOf;

import uk.org.okapibarcode.graphics.Rectangle;

/**
 * Implements the <a href="http://auspost.com.au/media/documents/a-guide-to-printing-the-4state-barcode-v31-mar2012.pdf">Australia Post 4-State barcode</a>.
 *
 * @author <a href="mailto:rstuart114@gmail.com">Robin Stuart</a>
 */
public class AustraliaPost extends Symbol {

    /**
     * The different Australia Post barcode variants available to encode.
     */
    public enum Mode {
        /**
         * <p>Australia Post Standard Customer Barcode, Customer Barcode 2, or Customer Barcode 3
         * (37-bar, 52-bar and 67-bar symbols) depending on input data length. Valid data characters
         * are 0-9, A-Z, a-z, space and hash (#). A Format Control Code (FCC) is added and should not
         * be included in the input data.
         *
         * <p>Input data should include a 8-digit Deliver Point ID (DPID) optionally followed by
         * customer information as shown below.
         *
         * <table>
         *   <tbody>
         *     <tr>
         *       <th>Input Length</th>
         *       <th>Required Input Format</th>
         *       <th>Symbol Length</th>
         *       <th>FCC</th>
         *       <th>Encoding Table</th>
         *     </tr>
         *     <tr>
         *       <td>8</td>
         *       <td>99999999</td>
         *       <td>37-bar</td>
         *       <td>11</td>
         *       <td>None</td>
         *     </tr>
         *     <tr>
         *       <td>13</td>
         *       <td>99999999AAAAA</td>
         *       <td>52-bar</td>
         *       <td>59</td>
         *       <td>C</td>
         *     </tr>
         *     <tr>
         *       <td>16</td>
         *       <td>9999999999999999</td>
         *       <td>52-bar</td>
         *       <td>59</td>
         *       <td>N</td>
         *     </tr>
         *     <tr>
         *       <td>18</td>
         *       <td>99999999AAAAAAAAAA</td>
         *       <td>67-bar</td>
         *       <td>62</td>
         *       <td>C</td>
         *     </tr>
         *     <tr>
         *       <td>23</td>
         *       <td>99999999999999999999999</td>
         *       <td>67-bar</td>
         *       <td>62</td>
         *       <td>N</td>
         *     </tr>
         *   </tbody>
         * </table>
         */
        POST,
        /**
         * Reply Paid version of the Australia Post 4-State Barcode (FCC 45) which requires an 8-digit DPID input.
         */
        REPLY,
        /**
         * Routing version of the Australia Post 4-State Barcode (FCC 87) which requires an 8-digit DPID input.
         */
        ROUTE,
        /**
         * Redirection version of the Australia Post 4-State Barcode (FCC 92) which requires an 8-digit DPID input.
         */
        REDIRECT
    }

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

    private Mode mode = Mode.POST;

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public Mode getMode() {
        return mode;
    }

    /** {@inheritDoc} */
    @Override
    protected void encode() {

        if (!content.matches("[0-9A-Za-z #]+")) {
            throw OkapiInputException.invalidCharactersInInput();
        }

        String formatControlCode = "00";
        switch (mode) {
            case POST:
                switch (content.length()) {
                    case 8:
                        formatControlCode = "11";
                        break;
                    case 13:
                        formatControlCode = "59";
                        break;
                    case 16:
                        formatControlCode = "59";
                        if (!content.matches("[0-9]+")) {
                            throw OkapiInputException.invalidCharactersInInput();
                        }
                        break;
                    case 18:
                        formatControlCode = "62";
                        break;
                    case 23:
                        formatControlCode = "62";
                        if (!content.matches("[0-9]+")) {
                            throw OkapiInputException.invalidCharactersInInput();
                        }
                        break;
                    default:
                        throw new OkapiInputException("Input length must be one of 8, 13, 16, 18 or 23");
                }
                break;
            case REPLY:
                if (content.length() > 8) {
                    throw OkapiInputException.inputTooLong();
                }
                formatControlCode = "45";
                break;
            case ROUTE:
                if (content.length() > 8) {
                    throw OkapiInputException.inputTooLong();
                }
                formatControlCode = "87";
                break;
            case REDIRECT:
                if (content.length() > 8) {
                    throw OkapiInputException.inputTooLong();
                }
                formatControlCode = "92";
                break;
        }

        infoLine("FCC: " + formatControlCode);

        StringBuilder zeroPaddedInput = new StringBuilder();
        if (mode != Mode.POST) {
            for (int i = content.length(); i < 8; i++) {
                zeroPaddedInput.append('0');
            }
        }
        zeroPaddedInput.append(content);

        /* Verify that the first 8 characters are numbers */
        String deliveryPointId = zeroPaddedInput.substring(0, 8);
        if (!deliveryPointId.matches("[0-9]+")) {
            throw new OkapiInputException("Invalid characters in DPID");
        }
        infoLine("DPID: " + deliveryPointId);

        /* Start */
        StringBuilder barStateValues = new StringBuilder();
        barStateValues.append("13");

        /* Encode the FCC */
        for (int i = 0; i < 2; i++) {
            barStateValues.append(N_ENCODING_TABLE[formatControlCode.charAt(i) - '0']);
        }

        /* Delivery Point Identifier (DPID) */
        for (int i = 0; i < 8; i++) {
            barStateValues.append(N_ENCODING_TABLE[deliveryPointId.charAt(i) - '0']);
        }

        /* Customer Information */
        switch (zeroPaddedInput.length()) {
            case 13:
            case 18:
                for (int i = 8; i < zeroPaddedInput.length(); i++) {
                    barStateValues.append(C_ENCODING_TABLE[positionOf(zeroPaddedInput.charAt(i), CHARACTER_SET)]);
                }
                break;
            case 16:
            case 23:
                for (int i = 8; i < zeroPaddedInput.length(); i++) {
                    barStateValues.append(N_ENCODING_TABLE[positionOf(zeroPaddedInput.charAt(i), CHARACTER_SET)]);
                }
                break;
        }

        /* Filler bar */
        switch (barStateValues.length()) {
            case 22:
            case 37:
            case 52:
                barStateValues.append('3');
                break;
        }

        /* Reed Solomon error correction */
        barStateValues.append(calcReedSolomon(barStateValues));

        /* Stop character */
        barStateValues.append("13");

        infoLine("Total Length: " + barStateValues.length());
        info("Encoding: ");
        for (int i = 0; i < barStateValues.length(); i++) {
            switch (barStateValues.charAt(i)) {
                case '1':
                    info('A');
                    break;
                case '2':
                    info('D');
                    break;
                case '0':
                    info('F');
                    break;
                case '3':
                    info('T');
                    break;
            }
        }
        infoLine();

        readable = "";
        pattern = new String[] { barStateValues.toString() };
        row_count = 1;
        row_height = new int[] { -1 };
    }

    private CharSequence calcReedSolomon(CharSequence oldBarStateValues) {

        /* Adds Reed-Solomon error correction to auspost */

        int barStateCount;
        int tripleValueCount = 0;
        int[] tripleValue = new int[31];

        for (barStateCount = 2; barStateCount < oldBarStateValues.length(); barStateCount += 3, tripleValueCount++) {
            tripleValue[tripleValueCount] = barStateToDecimal(oldBarStateValues.charAt(barStateCount), 4)
                            + barStateToDecimal(oldBarStateValues.charAt(barStateCount + 1), 2)
                            + barStateToDecimal(oldBarStateValues.charAt(barStateCount + 2), 0);
        }

        ReedSolomon rs = new ReedSolomon();
        rs.init_gf(0x43);
        rs.init_code(4, 1);
        rs.encode(tripleValueCount, tripleValue);

        StringBuilder newBarStateValues = new StringBuilder();
        for (barStateCount = 4; barStateCount > 0; barStateCount--) {
            newBarStateValues.append(BAR_VALUE_TABLE[rs.getResult(barStateCount - 1)]);
        }

        return newBarStateValues;
    }

    private int barStateToDecimal(char oldBarStateValues, int shift) {
        return (oldBarStateValues - '0') << shift;
    }

    /** {@inheritDoc} */
    @Override
    protected void plotSymbol() {

        int x = 0;
        int w = 1;
        int y = 0;
        int h = 0;

        resetPlotElements();

        for (int xBlock = 0; xBlock < pattern[0].length(); xBlock++) {
            switch (pattern[0].charAt(xBlock)) {
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
            Rectangle rect = new Rectangle(x, y, w, h);
            rectangles.add(rect);
            x += 2;
        }

        symbol_width = ((pattern[0].length() - 1) * 2) + 1; // no whitespace needed after the final bar
        symbol_height = 8;
    }
}
