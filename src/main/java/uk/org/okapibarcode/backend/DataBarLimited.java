/*
 * Copyright 2014-2018 Robin Stuart, Daniel Gredler
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

import java.math.BigInteger;

/**
 * <p>Implements GS1 DataBar Limited according to ISO/IEC 24724:2011.
 *
 * <p>Input data should be a 12-digit or 13-digit Global Trade Identification Number (GTIN) without
 * check digit or Application Identifier [01].
 *
 * @author <a href="mailto:rstuart114@gmail.com">Robin Stuart</a>
 */
public class DataBarLimited extends Symbol {

    private static final int[] T_EVEN_LTD = {
        28, 728, 6454, 203, 2408, 1, 16632
    };

    private static final int[] MODULES_ODD_LTD = {
        17, 13, 9, 15, 11, 19, 7
    };

    private static final int[] MODULES_EVEN_LTD = {
        9, 13, 17, 11, 15, 7, 19
    };

    private static final int[] WIDEST_ODD_LTD = {
        6, 5, 3, 5, 4, 8, 1
    };

    private static final int[] WIDEST_EVEN_LTD = {
        3, 4, 6, 4, 5, 1, 8
    };

    private static final int[] CHECKSUM_WEIGHT_LTD = { /* Table 7 */
        1, 3, 9, 27, 81, 65, 17, 51, 64, 14, 42, 37, 22, 66,
        20, 60, 2, 6, 18, 54, 73, 41, 34, 13, 39, 28, 84, 74
    };

    private static final int[] FINDER_PATTERN_LTD = {
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 3, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 3, 2, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 3, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 3, 2, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 2, 1, 2, 3, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 1,
        1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 3, 2, 1, 1,
        1, 1, 1, 1, 1, 2, 1, 1, 1, 2, 3, 1, 1, 1,
        1, 1, 1, 1, 1, 2, 1, 2, 1, 1, 3, 1, 1, 1,
        1, 1, 1, 1, 1, 3, 1, 1, 1, 1, 3, 1, 1, 1,
        1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 3, 2, 1, 1,
        1, 1, 1, 2, 1, 1, 1, 1, 1, 2, 3, 1, 1, 1,
        1, 1, 1, 2, 1, 1, 1, 2, 1, 1, 3, 1, 1, 1,
        1, 1, 1, 2, 1, 2, 1, 1, 1, 1, 3, 1, 1, 1,
        1, 1, 1, 3, 1, 1, 1, 1, 1, 1, 3, 1, 1, 1,
        1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 3, 2, 1, 1,
        1, 2, 1, 1, 1, 1, 1, 1, 1, 2, 3, 1, 1, 1,
        1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 3, 1, 1, 1,
        1, 2, 1, 1, 1, 2, 1, 1, 1, 1, 3, 1, 1, 1,
        1, 2, 1, 2, 1, 1, 1, 1, 1, 1, 3, 1, 1, 1,
        1, 3, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 2, 3, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 2, 3, 2, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 2, 2, 1, 2, 2, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 3, 2, 1, 2, 1, 1, 1,
        1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 2, 2, 1, 1,
        1, 1, 1, 1, 1, 2, 1, 1, 2, 2, 2, 1, 1, 1,
        1, 1, 1, 1, 1, 2, 1, 2, 2, 1, 2, 1, 1, 1,
        1, 1, 1, 1, 1, 3, 1, 1, 2, 1, 2, 1, 1, 1,
        1, 1, 1, 2, 1, 1, 1, 1, 2, 1, 2, 2, 1, 1,
        1, 1, 1, 2, 1, 1, 1, 1, 2, 2, 2, 1, 1, 1,
        1, 1, 1, 2, 1, 1, 1, 2, 2, 1, 2, 1, 1, 1,
        1, 1, 1, 2, 1, 2, 1, 1, 2, 1, 2, 1, 1, 1,
        1, 1, 1, 3, 1, 1, 1, 1, 2, 1, 2, 1, 1, 1,
        1, 2, 1, 1, 1, 1, 1, 1, 2, 1, 2, 2, 1, 1,
        1, 2, 1, 1, 1, 1, 1, 1, 2, 2, 2, 1, 1, 1,
        1, 2, 1, 1, 1, 1, 1, 2, 2, 1, 2, 1, 1, 1,
        1, 2, 1, 1, 1, 2, 1, 1, 2, 1, 2, 1, 1, 1,
        1, 2, 1, 2, 1, 1, 1, 1, 2, 1, 2, 1, 1, 1,
        1, 3, 1, 1, 1, 1, 1, 1, 2, 1, 2, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 3, 2, 1, 2, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 2, 3, 1, 1, 2, 1, 1,
        1, 1, 1, 2, 1, 1, 1, 1, 3, 1, 1, 2, 1, 1,
        1, 2, 1, 1, 1, 1, 1, 1, 3, 1, 1, 2, 1, 1,
        1, 1, 1, 1, 1, 1, 2, 1, 1, 1, 2, 3, 1, 1,
        1, 1, 1, 1, 1, 1, 2, 1, 1, 2, 2, 2, 1, 1,
        1, 1, 1, 1, 1, 1, 2, 1, 1, 3, 2, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 2, 2, 1, 1, 2, 2, 1, 1,
        1, 1, 1, 2, 1, 1, 2, 1, 1, 1, 2, 2, 1, 1,
        1, 1, 1, 2, 1, 1, 2, 1, 1, 2, 2, 1, 1, 1,
        1, 1, 1, 2, 1, 1, 2, 2, 1, 1, 2, 1, 1, 1,
        1, 1, 1, 2, 1, 2, 2, 1, 1, 1, 2, 1, 1, 1,
        1, 1, 1, 3, 1, 1, 2, 1, 1, 1, 2, 1, 1, 1,
        1, 2, 1, 1, 1, 1, 2, 1, 1, 1, 2, 2, 1, 1,
        1, 2, 1, 1, 1, 1, 2, 1, 1, 2, 2, 1, 1, 1,
        1, 2, 1, 2, 1, 1, 2, 1, 1, 1, 2, 1, 1, 1,
        1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 2, 3, 1, 1,
        1, 1, 1, 1, 2, 1, 1, 1, 1, 2, 2, 2, 1, 1,
        1, 1, 1, 1, 2, 1, 1, 1, 1, 3, 2, 1, 1, 1,
        1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 2, 2, 1, 1,
        1, 1, 1, 1, 2, 1, 1, 2, 1, 2, 2, 1, 1, 1,
        1, 1, 1, 1, 2, 2, 1, 1, 1, 1, 2, 2, 1, 1,
        1, 2, 1, 1, 2, 1, 1, 1, 1, 1, 2, 2, 1, 1,
        1, 2, 1, 1, 2, 1, 1, 1, 1, 2, 2, 1, 1, 1,
        1, 2, 1, 1, 2, 1, 1, 2, 1, 1, 2, 1, 1, 1,
        1, 2, 1, 1, 2, 2, 1, 1, 1, 1, 2, 1, 1, 1,
        1, 2, 1, 2, 2, 1, 1, 1, 1, 1, 2, 1, 1, 1,
        1, 3, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 1,
        1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 2, 3, 1, 1,
        1, 1, 2, 1, 1, 1, 1, 1, 1, 2, 2, 2, 1, 1,
        1, 1, 2, 1, 1, 1, 1, 1, 1, 3, 2, 1, 1, 1,
        1, 1, 2, 1, 1, 1, 1, 2, 1, 1, 2, 2, 1, 1,
        1, 1, 2, 1, 1, 1, 1, 2, 1, 2, 2, 1, 1, 1,
        1, 1, 2, 1, 1, 1, 1, 3, 1, 1, 2, 1, 1, 1,
        1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 2, 2, 1, 1,
        1, 1, 2, 1, 1, 2, 1, 1, 1, 2, 2, 1, 1, 1,
        1, 1, 2, 2, 1, 1, 1, 1, 1, 1, 2, 2, 1, 1,
        2, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 1, 1,
        2, 1, 1, 1, 1, 1, 1, 1, 1, 3, 2, 1, 1, 1,
        2, 1, 1, 1, 1, 1, 1, 2, 1, 1, 2, 2, 1, 1,
        2, 1, 1, 1, 1, 1, 1, 2, 1, 2, 2, 1, 1, 1,
        2, 1, 1, 1, 1, 1, 1, 3, 1, 1, 2, 1, 1, 1,
        2, 1, 1, 1, 1, 2, 1, 1, 1, 2, 2, 1, 1, 1,
        2, 1, 1, 1, 1, 2, 1, 2, 1, 1, 2, 1, 1, 1,
        2, 1, 1, 2, 1, 1, 1, 1, 1, 2, 2, 1, 1, 1,
        2, 1, 1, 1, 1, 1, 1, 1, 2, 2, 1, 2, 1, 1 /* ISO/IEC 24724-2011 57 */
    };

    private boolean linkageFlag;

    @Override
    public void setDataType(DataType dummy) {
        // Do nothing!
    }

    /**
     * Although this is a GS1 symbology, input data is expected to omit the [01] Application Identifier,
     * as well as the check digit. Thus, the input data is not considered GS1-format data.
     */
    @Override
    protected boolean gs1Supported() {
        return false;
    }

    protected void setLinkageFlag() {
        linkageFlag = true;
    }

    protected void unsetLinkageFlag() {
        linkageFlag = false;
    }

    @Override
    protected void encode() {
        BigInteger accum;
        BigInteger left_reg;
        BigInteger right_reg;
        int left_group;
        int right_group;
        int i, j;
        int left_character;
        int right_character;
        int left_odd;
        int right_odd;
        int left_even;
        int right_even;
        int[] left_widths = new int[14];
        int[] right_widths = new int[14];
        int checksum;
        int[] check_elements = new int[14];
        int[] total_widths = new int[46];
        String bin;
        String notbin;
        boolean bar_latch;
        int writer;
        int check_digit = 0;
        int count = 0;
        String hrt;
        int compositeOffset = 0;

        if (content.length() > 13) {
            throw new OkapiException("Input too long");
        }

        if (!content.matches("[0-9]+?")) {
            throw new OkapiException("Invalid characters in input");
        }

        if (content.length() == 13 && content.charAt(0) != '0' && content.charAt(0) != '1') {
            throw new OkapiException("Input out of range");
        }

        accum = new BigInteger(content);

        if (linkageFlag) {
            /* Add symbol linkage flag */
            accum = accum.add(new BigInteger("2015133531096"));
        }

        /* Calculate left and right pair values */
        left_reg = accum.divide(new BigInteger("2013571"));
        right_reg = accum.mod(new BigInteger("2013571"));

//        if (debug) {
//            System.out.println("left " + left_reg.toString());
//            System.out.println("right " + right_reg.toString());
//        }

        left_group = 0;
        if (left_reg.compareTo(new BigInteger("183063")) == 1) {
            left_group = 1;
        }
        if (left_reg.compareTo(new BigInteger("820063")) == 1) {
            left_group = 2;
        }
        if (left_reg.compareTo(new BigInteger("1000775")) == 1) {
            left_group = 3;
        }
        if (left_reg.compareTo(new BigInteger("1491020")) == 1) {
            left_group = 4;
        }
        if (left_reg.compareTo(new BigInteger("1979844")) == 1) {
            left_group = 5;
        }
        if (left_reg.compareTo(new BigInteger("1996938")) == 1) {
            left_group = 6;
        }

        right_group = 0;
        if (right_reg.compareTo(new BigInteger("183063")) == 1) {
            right_group = 1;
        }
        if (right_reg.compareTo(new BigInteger("820063")) == 1) {
            right_group = 2;
        }
        if (right_reg.compareTo(new BigInteger("1000775")) == 1) {
            right_group = 3;
        }
        if (right_reg.compareTo(new BigInteger("1491020")) == 1) {
            right_group = 4;
        }
        if (right_reg.compareTo(new BigInteger("1979844")) == 1) {
            right_group = 5;
        }
        if (right_reg.compareTo(new BigInteger("1996938")) == 1) {
            right_group = 6;
        }

        encodeInfo += "Data Characters: " + Integer.toString(left_group + 1) +
                " " + Integer.toString(right_group + 1) + "\n";

//        if (debug) {
//            System.out.println("left group " + (left_group + 1));
//            System.out.println("right group " + (right_group + 1));
//        }

        switch(left_group) {
            case 1:
                left_reg = left_reg.subtract(new BigInteger("183064"));
                break;
            case 2:
                left_reg = left_reg.subtract(new BigInteger("820064"));
                break;
            case 3:
                left_reg = left_reg.subtract(new BigInteger("1000776"));
                break;
            case 4:
                left_reg = left_reg.subtract(new BigInteger("1491021"));
                break;
            case 5:
                left_reg = left_reg.subtract(new BigInteger("1979845"));
                break;
            case 6:
                left_reg = left_reg.subtract(new BigInteger("1996939"));
                break;
        }

        switch(right_group) {
            case 1:
                right_reg = right_reg.subtract(new BigInteger("183064"));
                break;
            case 2:
                right_reg = right_reg.subtract(new BigInteger("820064"));
                break;
            case 3:
                right_reg = right_reg.subtract(new BigInteger("1000776"));
                break;
            case 4:
                right_reg = right_reg.subtract(new BigInteger("1491021"));
                break;
            case 5:
                right_reg = right_reg.subtract(new BigInteger("1979845"));
                break;
            case 6:
                right_reg = right_reg.subtract(new BigInteger("1996939"));
                break;
        }

        left_character = left_reg.intValue();
        right_character = right_reg.intValue();

        left_odd = left_character / T_EVEN_LTD[left_group];
        left_even = left_character % T_EVEN_LTD[left_group];
        right_odd = right_character / T_EVEN_LTD[right_group];
        right_even = right_character % T_EVEN_LTD[right_group];

//        if (debug) {
//            System.out.println("left char " + left_character);
//            System.out.println("right char " + right_character);
//            System.out.println("left even " + left_even);
//            System.out.println("right even " + right_even);
//            System.out.println("left odd " + left_odd);
//            System.out.println("right odd " + right_odd);
//        }

        int[] widths = getWidths(left_odd, MODULES_ODD_LTD[left_group], 7, WIDEST_ODD_LTD[left_group], 1);
        left_widths[0] = widths[0];
        left_widths[2] = widths[1];
        left_widths[4] = widths[2];
        left_widths[6] = widths[3];
        left_widths[8] = widths[4];
        left_widths[10] = widths[5];
        left_widths[12] = widths[6];

        widths = getWidths(left_even, MODULES_EVEN_LTD[left_group], 7, WIDEST_EVEN_LTD[left_group], 0);
        left_widths[1] = widths[0];
        left_widths[3] = widths[1];
        left_widths[5] = widths[2];
        left_widths[7] = widths[3];
        left_widths[9] = widths[4];
        left_widths[11] = widths[5];
        left_widths[13] = widths[6];

        widths = getWidths(right_odd, MODULES_ODD_LTD[right_group], 7, WIDEST_ODD_LTD[right_group], 1);
        right_widths[0] = widths[0];
        right_widths[2] = widths[1];
        right_widths[4] = widths[2];
        right_widths[6] = widths[3];
        right_widths[8] = widths[4];
        right_widths[10] = widths[5];
        right_widths[12] = widths[6];

        widths = getWidths(right_even, MODULES_EVEN_LTD[right_group], 7, WIDEST_EVEN_LTD[right_group], 0);
        right_widths[1] = widths[0];
        right_widths[3] = widths[1];
        right_widths[5] = widths[2];
        right_widths[7] = widths[3];
        right_widths[9] = widths[4];
        right_widths[11] = widths[5];
        right_widths[13] = widths[6];

        checksum = 0;
        /* Calculate the checksum */
        for(i = 0; i < 14; i++) {
                checksum += CHECKSUM_WEIGHT_LTD[i] * left_widths[i];
                checksum += CHECKSUM_WEIGHT_LTD[i + 14] * right_widths[i];
        }
        checksum %= 89;

        encodeInfo += "Checksum: " + Integer.toString(checksum) + "\n";

//        if (debug) {
//            System.out.println("checksum " + checksum);
//        }

        for(i = 0; i < 14; i++) {
                check_elements[i] = FINDER_PATTERN_LTD[i + (checksum * 14)];
        }

        total_widths[0] = 1;
        total_widths[1] = 1;
        total_widths[44] = 1;
        total_widths[45] = 1;
        for(i = 0; i < 14; i++) {
                total_widths[i + 2] = left_widths[i];
                total_widths[i + 16] = check_elements[i];
                total_widths[i + 30] = right_widths[i];
        }

        bin = "";
        notbin = "";
        writer = 0;
        bar_latch = false;
        for (i = 0; i < 46; i++) {
            for (j = 0; j < total_widths[i]; j++) {
                if (bar_latch) {
                    bin += "1";
                    notbin += "0";
                } else {
                    bin += "0";
                    notbin += "1";
                }
                writer++;
            }
            if (bar_latch) {
                bar_latch = false;
            } else {
                bar_latch = true;
            }
        }

        if (symbol_width < (writer + 20)) {
            symbol_width = writer + 20;
        }

//    /* add separator pattern if composite symbol */
//    if(symbol->symbology == BARCODE_RSS_LTD_CC) {
//        for(i = 4; i < 70; i++) {
//                if(!(module_is_set(symbol, separator_row + 1, i))) {
//                    set_module(symbol, separator_row, i);
//                }
//            }
//    }

        /* Calculate check digit from Annex A and place human readable text */

        readable = "(01)";
        hrt = "";
        for (i = content.length(); i < 13; i++) {
            hrt += "0";
        }
        hrt += content;

        for (i = 0; i < 13; i++) {
            count += (hrt.charAt(i) - '0');
            if ((i & 1) == 0) {
                count += 2 * (hrt.charAt(i) - '0');
            }
        }

        check_digit = 10 - (count % 10);
        if (check_digit == 10) {
            check_digit = 0;
        }

        hrt += (char) (check_digit + '0');
        readable += hrt;

        if (linkageFlag) {
            compositeOffset = 1;
        }

        row_count = 1 + compositeOffset;
        row_height = new int[1 + compositeOffset];
        row_height[0 + compositeOffset] = -1;
        pattern = new String[1 + compositeOffset];
        pattern[0 + compositeOffset] = "0:" + bin2pat(bin);

        if (linkageFlag) {
            // Add composite symbol separator
            notbin = notbin.substring(4, 70);
            row_height[0] = 1;
            pattern[0] = "0:04" + bin2pat(notbin);
        }
    }

    private static int getCombinations(int n, int r) {

        int i, j;
        int maxDenom, minDenom;
        int val;

        if (n - r > r) {
            minDenom = r;
            maxDenom = n - r;
        } else {
            minDenom = n - r;
            maxDenom = r;
        }

        val = 1;
        j = 1;

        for (i = n; i > maxDenom; i--) {
            val *= i;
            if (j <= minDenom) {
                val /= j;
                j++;
            }
        }

        for (; j <= minDenom; j++) {
            val /= j;
        }

        return val;
    }

    static int[] getWidths(int val, int n, int elements, int maxWidth, int noNarrow) {

        int bar;
        int elmWidth;
        int mxwElement;
        int subVal, lessVal;
        int narrowMask = 0;
        int[] widths = new int[elements];

        for (bar = 0; bar < elements - 1; bar++) {
            for (elmWidth = 1, narrowMask |= (1 << bar); ;
                    elmWidth++, narrowMask &= ~ (1 << bar)) {
                /* get all combinations */
                subVal = getCombinations(n - elmWidth - 1, elements - bar - 2);
                /* less combinations with no single-module element */
                if ((noNarrow == 0) && (narrowMask == 0)
                        && (n - elmWidth - (elements - bar - 1) >= elements - bar - 1)) {
                    subVal -= getCombinations(n - elmWidth - (elements - bar), elements - bar - 2);
                }
                /* less combinations with elements > maxVal */
                if (elements - bar - 1 > 1) {
                    lessVal = 0;
                    for (mxwElement = n - elmWidth - (elements - bar - 2);
                    mxwElement > maxWidth;
                    mxwElement--) {
                        lessVal += getCombinations(n - elmWidth - mxwElement - 1, elements - bar - 3);
                    }
                    subVal -= lessVal * (elements - 1 - bar);
                } else if (n - elmWidth > maxWidth) {
                    subVal--;
                }
                val -= subVal;
                if (val < 0) break;
            }
            val += subVal;
            n -= elmWidth;
            widths[bar] = elmWidth;
        }

        widths[bar] = n;

        return widths;
    }
}
