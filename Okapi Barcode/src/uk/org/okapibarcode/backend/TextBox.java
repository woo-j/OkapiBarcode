package uk.org.okapibarcode.backend;

/**
 * A simple text item class
 *
 * @author Robin Stuart <rstuart114@gmail.com>
 */
public class TextBox {
    public double xPos;
    public double yPos;
    public String arg;
    
    public void TextBox() {
        xPos = 0.0;
        yPos = 0.0;
        arg = "";
    }
    
    public void setvalues(double x, double y, String a) {
        xPos = x;
        yPos = y;
        arg = a;
    }
    
    public void printvalues() {
        System.out.println("Text  X:" + xPos + " Y:" + yPos + " A:" + arg);
    }
}
