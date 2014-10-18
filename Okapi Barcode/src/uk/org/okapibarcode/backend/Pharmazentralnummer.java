package uk.org.okapibarcode.backend;

/**
 *
 * @author Robin Stuart <rstuart114@gmail.com>
 */
public class Pharmazentralnummer extends Symbol {

    /* Pharmazentral Nummer is a Code 3 of 9 symbol with an extra
     * check digit.
     */

    @Override
    public boolean encode() {
        int l = content.length();
        String localstr;
        int zeroes, count = 0, check_digit;
        Code3Of9 c = new Code3Of9();

        if (l > 6) {
            error_msg = "Input data too long";
            return false;
        }

        if (!(content.matches("[0-9]+?"))) {
            error_msg = "Invalid characters in input";
            return false;
        }

        localstr = "-";
        zeroes = 6 - l + 1;
        for (int i = 1; i < zeroes; i++)
        localstr += '0';

        localstr += content;

        for (int i = 1; i < 7; i++) {
            count += (i + 1) * Character.getNumericValue(localstr.charAt(i));
        }

        check_digit = count % 11;
        if (check_digit == 11) {
            check_digit = 0;
        }
        if (check_digit == 10) {
            error_msg = "Not a valid PZN identifier";
            return false;
        }
        
        encodeInfo += "Check Digit: " + check_digit + "\n";

        localstr += (char)(check_digit + '0');

        if (!(c.setContent(localstr))) {
            error_msg = c.error_msg;
            return false;
        }

        readable = "PZN" + localstr + (char)(check_digit + '0');
        pattern = new String[1];
        pattern[0] = c.pattern[0];
        row_count = 1;
        row_height = new int[1];
        row_height[0] = -1;
        plotSymbol();
        return true;
    }
}
