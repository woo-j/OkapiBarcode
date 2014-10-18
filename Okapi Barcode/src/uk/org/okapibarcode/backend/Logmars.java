package uk.org.okapibarcode.backend;

/**
 *
 * @author Robin Stuart <rstuart114@gmail.com>
 */
public class Logmars extends Symbol {
    private String[] Code39LM = {
        "1113313111", "3113111131", "1133111131", "3133111111", "1113311131", 
        "3113311111", "1133311111", "1113113131", "3113113111", "1133113111", 
        "3111131131", "1131131131", "3131131111", "1111331131", "3111331111", 
        "1131331111", "1111133131", "3111133111", "1131133111", "1111333111", 
        "3111111331", "1131111331", "3131111311", "1111311331", "3111311311", 
        "1131311311", "1111113331", "3111113311", "1131113311", "1111313311", 
        "3311111131", "1331111131", "3331111111", "1311311131", "3311311111", 
        "1331311111", "1311113131", "3311113111", "1331113111", "1313131111", 
        "1313111311", "1311131311", "1113131311"
    };

    private char[] LookUp = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 
        'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 
        'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '-', '.', ' ', '$', '/', '+', 
        '%'
    };

    @Override
    public boolean encode() {

        if (!(content.matches("[0-9A-Z\\. \\-$/+%]+?"))) {
            error_msg = "Invalid characters in input";
            return false;
        }
        String p = "";
        int l = content.length();
        int charval, counter = 0;
        char thischar;
        char check_digit;
        for (int i = 0; i < l; i++) {
            thischar = content.charAt(i);
            charval = positionOf(thischar, LookUp);
            counter += charval;
            p += Code39LM[charval];
        }

        counter = counter % 43;
        check_digit = LookUp[counter];
        encodeInfo += "Check Digit: " + check_digit + "\n";
        p += Code39LM[counter];

        readable = content + LookUp[counter];
        pattern = new String[1];
        pattern[0] = "1311313111" + p + "131131311";
        row_count = 1;
        row_height = new int[1];
        row_height[0] = -1;
        plotSymbol();
        return true;
    }
}
