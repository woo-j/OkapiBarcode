package uk.org.okapibarcode.backend;

/**
 *
 * @author Robin Stuart <rstuart114@gmail.com>
 */
public class Code93 extends Symbol {

    private String[] C93Ctrl = {
        "bU", "aA", "aB", "aC", "aD", "aE", "aF", "aG", "aH", "aI", "aJ", "aK", 
        "aL", "aM", "aN", "aO", "aP", "aQ", "aR", "aS", "aT", "aU", "aV", "aW", 
        "aX", "aY", "aZ", "bA", "bB", "bC", "bD", "bE", " ", "cA", "cB", "cC", 
        "cD", "cE", "cF", "cG", "cH", "cI", "cJ", "cK", "cL", "cM", "cN", "cO", 
        "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "cZ", "bF", "bG", 
        "bH", "bI", "bJ", "bV", "A", "B", "C", "D", "E", "F", "G", "H", "I", 
        "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", 
        "X", "Y", "Z", "bK", "bL", "bM", "bN", "bO", "bW", "dA", "dB", "dC", 
        "dD", "dE", "dF", "dG", "dH", "dI", "dJ", "dK", "dL", "dM", "dN", "dO", 
        "dP", "dQ", "dR", "dS", "dT", "dU", "dV", "dW", "dX", "dY", "dZ", "bP", 
        "bQ", "bR", "bS", "bT"
    };

    private String[] C93Table = {
        "131112", "111213", "111312", "111411", "121113", "121212", "121311", 
        "111114", "131211", "141111", "211113", "211212", "211311", "221112", 
        "221211", "231111", "112113", "112212", "112311", "122112", "132111", 
        "111123", "111222", "111321", "121122", "131121", "212112", "212211", 
        "211122", "211221", "221121", "222111", "112122", "112221", "122121", 
        "123111", "121131", "311112", "311211", "321111", "112131", "113121", 
        "211131", "121221", "312111", "311121", "122211"
    };

    private char[] LookUp = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 
        'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 
        'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '-', '.', ' ', '$', '/', '+', 
        '%', 'a', 'b', 'c', 'd'
    };

    @Override
    public boolean encode() {
        String buffer = "";
        String p = "";
        String dest;
        int l = content.length();
        int asciicode;
        int[] values;
        values = new int[107];
        int weight, c, k;

        for (int i = 0; i < l; i++) {
            asciicode = (int) content.charAt(i);
            buffer += C93Ctrl[asciicode];
        }

        l = buffer.length();
        for (int i = 0; i < l; i++) {
            values[i] = positionOf(buffer.charAt(i), LookUp);
        }

        /* Check digit C */
        c = 0;
        weight = 1;
        for (int i = l - 1; i >= 0; i--) {
            c += values[i] * weight;
            weight++;
            if (weight == 21) weight = 1;
        }
        c = c % 47;

        values[l] = c;

        /* Check digit K */
        k = 0;
        weight = 1;
        for (int i = l; i >= 0; i--) {
            k += values[i] * weight;
            weight++;
            if (weight == 16) weight = 1;
        }
        k = k % 47;
        
        encodeInfo += "Check Digit C: " + c + "\n";
        encodeInfo += "Check Digit K: " + k + "\n";

        l++;
        values[l] = k;
        l++;

        dest = "111141";

        for (int i = 0; i < l; i++) {
            p += C93Table[values[i]];
        }

        dest += p;
        dest += "1111411";

        readable = content + LookUp[c] + LookUp[k];
        pattern = new String[1];
        pattern[0] = dest;
        row_count = 1;
        row_height = new int[1];
        row_height[0] = -1;
        plotSymbol();
        return true;
    }
}
