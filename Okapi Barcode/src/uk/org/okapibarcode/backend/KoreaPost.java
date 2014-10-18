package uk.org.okapibarcode.backend;

/**
 *
 * @author Robin Stuart <rstuart114@gmail.com>
 */
public class KoreaPost extends Symbol {

    String[] KoreaTable = {
        "1313150613", "0713131313", "0417131313", "1506131313", "0413171313", 
        "17171313", "1315061313", "0413131713", "17131713", "13171713"
    };

    @Override
    public boolean encode() {
        String accumulator = "";

        if (!(content.matches("[0-9]+?"))) {
            error_msg = "Invalid characters in input";
            return false;
        }
        
        if (content.length() > 6) {
            error_msg = "Input data too long";
            return false;
        }

        String add_zero = "";
        int i, j, total = 0, checkd;

        for (i = 0; i < (6 - content.length()); i++) {
            add_zero += "0";
        }
        add_zero += content;

        if (debug) {
            System.out.print("Data: " + add_zero + "\t");
        }

        for (i = 0; i < add_zero.length(); i++) {
            j = Character.getNumericValue(add_zero.charAt(i));
            accumulator += KoreaTable[j];
            total += j;
        }

        checkd = 10 - (total % 10);
        if (checkd == 10) {
            checkd = 0;
        }
        if (debug) {
            System.out.println("Check: " + checkd);
        }
        encodeInfo += "Check Digit: " + checkd + "\n";
        
        accumulator += KoreaTable[checkd];

        readable = add_zero + checkd;
        pattern = new String[1];
        pattern[0] = accumulator;
        row_count = 1;
        row_height = new int[1];
        row_height[0] = -1;
        plotSymbol();
        return true;
    }
}
