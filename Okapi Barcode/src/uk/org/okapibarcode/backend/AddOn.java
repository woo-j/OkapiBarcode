package uk.org.okapibarcode.backend;

/**
 * Encode Add-On barcodes from UPC/EAN
 *
 * @author Robin Stuart <rstuart114@gmail.com>
 */
public class AddOn{
    private String content;
    private String dest;
    
    private String[] EANsetA = {
        "3211", "2221", "2122", "1411", "1132", "1231", "1114", "1312", "1213", 
        "3112"
    };
    private String[] EANsetB = {
        "1123", "1222", "2212", "1141", "2311", "1321", "4111", "2131", "3121", 
        "2113"
    };

    private String[] EAN2Parity = {
        "AA", "AB", "BA", "BB"
    };
    private String[] EAN5Parity = {
        "BBAAA", "BABAA", "BAABA", "BAAAB", "ABBAA", "AABBA", "AAABB", "ABABA", 
        "ABAAB", "AABAB"
    };
    
    public String calcAddOn(String input) {
        dest = "";
        content = input;
        
        if (!(content.matches("[0-9]{1,5}"))) {
            return "";
        }
        
        if (content.length() > 2) {
            ean5();
        } else {
            ean2();
        }
        
        return dest;
    }
    
    private void ean2() {
        String parity;
        String accumulator = "";
        int i, code_value;
        
        if (!(content.matches("[0-9]+?"))) {
            return;
        }

        for (i = content.length(); i < 2; i++) {
            accumulator += "0";
        }
        accumulator += content;

        code_value = (int)(((accumulator.charAt(0) - '0') * 10) 
                + (accumulator.charAt(1) - '0'));
        parity = EAN2Parity[code_value % 4];

        dest = "112"; /* Start */
        for (i = 0; i < 2; i++) {
            if ((parity.charAt(i) == 'B')) {
                dest += EANsetB[Character.getNumericValue(accumulator.charAt(i))];
            } else {
                dest += EANsetA[Character.getNumericValue(accumulator.charAt(i))];
            }
            if (i != 1) { /* Glyph separator */
                dest += "11";
            }
        }
    }

    private void ean5() {
        String parity;
        String accumulator = "";
        int i, parity_sum;
        
        if (!(content.matches("[0-9]+?"))) {
            return;
        }

        for (i = content.length(); i < 5; i++) {
            accumulator += "0";
        }
        accumulator += content;

        parity_sum = 0;
        for (i = 0; i < 5; i++) {
            if ((i % 2) == 0) {
                parity_sum += 3 * (int)(accumulator.charAt(i) - '0');
            } else {
                parity_sum += 9 * (int)(accumulator.charAt(i) - '0');
            }
        }

        parity = EAN5Parity[parity_sum % 10];

        dest = "112"; /* Start */
        for (i = 0; i < 5; i++) {
            if ((parity.charAt(i) == 'B')) {
                dest += EANsetB[Character.getNumericValue(accumulator.charAt(i))];
            } else {
                dest += EANsetA[Character.getNumericValue(accumulator.charAt(i))];
            }
            if (i != 4) { /* Glyph separator */
                dest += "11";
            }
        }
    }
}
