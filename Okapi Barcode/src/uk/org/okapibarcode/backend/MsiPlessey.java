package uk.org.okapibarcode.backend;

/**
 *
 * @author Robin Stuart <rstuart114@gmail.com>
 */
public class MsiPlessey extends Symbol {

    String MSI_PlessTable[] = {
        "12121212", "12121221", "12122112", "12122121", "12211212", "12211221", 
        "12212112", "12212121", "21121212", "21121221"
    };

    public boolean encode() {
        if (!(content.matches("[0-9]+?"))) {
            error_msg = "Invalid characters in input";
            return false;
        }

        String p = "";
        String dest;
        int l = content.length();
        dest = "21"; // Start
        for (int i = 0; i < l; i++) {
            p += MSI_PlessTable[Character.getNumericValue(content.charAt(i))];
        }
        dest += p;
        dest += "121"; // Stop
        readable = content;
        pattern = new String[1];
        pattern[0] = dest;
        row_count = 1;
        row_height = new int[1];
        row_height[0] = -1;
        plotSymbol();
        return true;
    }
}
