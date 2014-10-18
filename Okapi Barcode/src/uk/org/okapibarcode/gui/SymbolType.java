package uk.org.okapibarcode.gui;

/**
 * Simple container for symbology information
 * 
 * @author Robin Stuart <rstuart114@gmail.com>
 */
public class SymbolType {
    String guiLabel; // GUI interface name
    String mnemonic; // Zint API name
    int tbarEquiv; // tbarcode compatible option number
    
    public SymbolType(String label, String name, int number) {
        guiLabel = label;
        mnemonic = name;
        tbarEquiv = number;
    }
    
    @Override
    public String toString(){
        return guiLabel;
    }
}
