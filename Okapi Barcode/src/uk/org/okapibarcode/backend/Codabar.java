package uk.org.okapibarcode.backend;


/**
 * Implements Codabar
 * According to BS EN 798:1996
 *
 * @author Robert Elliott <jakel2116@me.com>
 * @version 0.2
 */
public class Codabar extends Symbol {

    private String[] codabarTable = {"11111221", "11112211", "11121121", "22111111", "11211211", "21111211",
        "12111121", "12112111", "12211111", "21121111", "11122111", "11221111", "21112121", "21211121",
        "21212111", "11212121", "11221211", "12121121", "11121221", "11122211"};
    
    private char[] characterSet = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '-', '$', ':', '/', '+', '.', 'A', 'B', 'C', 'D'};
    
    @Override
    public boolean encode() {
        if (!(content.matches("[A-D]{1}[0-9:/\\$\\.\\+-]+[A-D]{1}"))) {
            error_msg = "Invalid characters in input";
            return false;
        }

        String horizontalSpacing = "";
        
        int l = content.length();
        for (int i = 1; i < l; i++) {
             horizontalSpacing += codabarTable[positionOf(content.charAt(i), characterSet)];
        }
    
        readable = content;
        pattern = new String[1];
        pattern[0] = horizontalSpacing;
        row_count = 1;
        row_height = new int[1];
        row_height[0] = -1;
        plotSymbol();
        return true;
    }
}
