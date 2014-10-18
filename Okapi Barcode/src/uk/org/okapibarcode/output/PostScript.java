package uk.org.okapibarcode.output;

import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Output to EPS file
 *
 * @author Robin Stuart <rstuart114@gmail.com>
 */
public class PostScript {
    public static ArrayList<Rectangle> rectangle = new ArrayList<>();
    public static ArrayList<uk.org.okapibarcode.backend.TextBox> textbox = new ArrayList<>();
    public static ArrayList<uk.org.okapibarcode.backend.Hexagon> hexagon = new ArrayList<>();
    public static ArrayList<Ellipse2D.Double> ellipse = new ArrayList<>();
    private int symbol_width;
    private int symbol_height;
    private String symbol_text = "";
    double fgRed, fgGreen, fgBlue;
    double bgRed, bgGreen, bgBlue;
    
    public void setShapes(ArrayList<Rectangle> bcs, ArrayList<uk.org.okapibarcode.backend.TextBox> txt,
            ArrayList<uk.org.okapibarcode.backend.Hexagon> hex, ArrayList<Ellipse2D.Double> target) {
        rectangle = bcs;
        textbox = txt;
        hexagon = hex;
        ellipse = target;
    }
    
    public void setValues (String readable, int width, int height) {
        symbol_width = width;
        symbol_height = height;
        symbol_text = readable;
        fgRed = fgGreen = fgBlue = 0.0;
        bgRed = bgGreen = bgBlue = 1.0;
    }
    
    public boolean write(File file) {
        String outStream;
        int i, j;
        
        // All y-dimensions are reversed because EPS co-ord (0,0) is bottom left
        
        try (FileOutputStream fos = new FileOutputStream(file)) {
            // Header
            outStream = "%!PS-Adobe-3.0 EPSF-3.0\n";
            outStream += "%%Creator: Zint 3.0\n";
            if (symbol_text.length() == 0) {
                outStream += "%%Title: Zint Generated Symbol\n";
            } else {
                outStream += "%%Title: " + symbol_text + "\n";
            }
            outStream += "%%Pages: 0\n";
            outStream += "%%BoundingBox: 0 0 " + symbol_width + " " 
                    + symbol_height + "\n";
            outStream += "%%EndComments\n";
            
            // Definitions
            outStream += "/TL { setlinewidth moveto lineto stroke } bind def\n";
            outStream += "/TC { moveto 0 360 arc 360 0 arcn fill } bind def\n";
            outStream += "/TH { 0 setlinewidth moveto lineto lineto lineto lineto lineto closepath fill } bind def\n";
            outStream += "/TB { 2 copy } bind def\n";
            outStream += "/TR { newpath 4 1 roll exch moveto 1 index 0 rlineto 0 exch rlineto neg 0 rlineto closepath fill } bind def\n";
            outStream += "/TE { pop pop } bind def\n";
            outStream += "newpath\n";
            
            outStream += String.format("%.2f", fgRed) + " " 
                    + String.format("%.2f", fgGreen) + " "
                    + String.format("%.2f", fgBlue) + " setrgbcolor\n";
            outStream += String.format("%.2f", bgRed) + " " 
                    + String.format("%.2f", bgGreen) + " "
                    + String.format("%.2f", bgBlue) + " setrgbcolor\n";
            outStream += symbol_height + ".00 0.00 TB 0.00 " + symbol_width + ".00 TR\n";
            
            // Rectangles
            for (i = 0; i < rectangle.size(); i++) {
                if (i == 0) {
                    outStream += "TE\n";
                    outStream += String.format("%.2f", fgRed) + " " 
                            + String.format("%.2f", fgGreen) + " "
                            + String.format("%.2f", fgBlue) + " setrgbcolor\n";
                    outStream += rectangle.get(i).height + ".00 "
                            + (symbol_height - rectangle.get(i).y) + ".00 TB "
                            + rectangle.get(i).x + ".00 "
                            + rectangle.get(i).width + ".00 TR\n";
                } else {
                    if ((rectangle.get(i).height != rectangle.get(i - 1).height)
                                || (rectangle.get(i).y != rectangle.get(i - 1).y)) {
                        outStream += "TE\n";
                        outStream += String.format("%.2f", fgRed) + " " 
                                + String.format("%.2f", fgGreen) + " "
                                + String.format("%.2f", fgBlue) + " setrgbcolor\n";                        
                        outStream += rectangle.get(i).height + ".00 "
                                + (symbol_height - rectangle.get(i).y) + ".00 ";
                    }
                    outStream += "TB " + rectangle.get(i).x + ".00 "
                            + rectangle.get(i).width + ".00 TR\n";
                }
            }
            
            // Text
            for(i = 0; i < textbox.size(); i++) {
                if (i == 0) {
                    outStream += "TE\n";
                    outStream += String.format("%.2f", fgRed) + " " 
                            + String.format("%.2f", fgGreen) + " "
                            + String.format("%.2f", fgBlue) + " setrgbcolor\\n";
                }
                outStream += "matrix currentmatrix\n";
                outStream += "/Helvetica findfont\n";
                outStream += "8.00 scalefont setfont\n";
                outStream += " 0 0 moveto " 
                        + String.format("%.2f", textbox.get(i).xPos) 
                        + " " + String.format("%.2f", symbol_height - textbox.get(i).yPos)
                        + " translate 0.00 rotate 0 0 moveto\n";
                outStream += " (" + textbox.get(i).arg + ") stringwidth\n";
                outStream += "pop\n";
                outStream += "-2 div 0 rmoveto\n";
                outStream += " (" + textbox.get(i).arg + ") show\n";
                outStream += "setmatrix\n";
            }
            
            // Circles
            for (i = 0; i < ellipse.size(); i += 2) {
                if (i == 0) {
                    outStream += "TE\n";
                    outStream += String.format("%.2f", fgRed) + " " 
                            + String.format("%.2f", fgGreen) + " "
                            + String.format("%.2f", fgBlue) + " setrgbcolor\n";
                    outStream += String.format("%.2f", fgRed) + " " 
                            + String.format("%.2f", fgGreen) + " "
                            + String.format("%.2f", fgBlue) + " setrgbcolor\n";                    
                }
                outStream += String.format("%.2f", symbol_height - ellipse.get(i).x + (ellipse.get(i).width / 2))
                        + " " + String.format("%.2f", ellipse.get(i).y + (ellipse.get(i).width / 2))
                        + " " + String.format("%.2f", ellipse.get(i).width / 2)
                        + " " + String.format("%.2f", ellipse.get(i + 1).x + (ellipse.get(i + 1).width / 2))
                        + " " + String.format("%.2f", symbol_height - ellipse.get(i + 1).y + (ellipse.get(i + 1).width / 2))
                        + " " + String.format("%.2f", ellipse.get(i + 1).width / 2)
                        + " TC\n";
            }            
            
            // Hexagons
            for(i = 0; i < hexagon.size(); i++) {
                for(j = 0; j < 6; j++) {
                    outStream += String.format("%.2f", hexagon.get(i).pointX[j]) + " " 
                            + String.format("%.2f", symbol_height - hexagon.get(i).pointY[j]) + " ";
                }
                outStream += " TH\n";
            }
            
            // Footer
            outStream += "\nshowpage\n";
            
            // Output data to file
            for (i = 0; i < outStream.length(); i++) {
                fos.write(outStream.charAt(i));
            }
        }
        
        catch (IOException ioe) {
            System.err.println("I/O error: " + ioe.getMessage());
        }
        return false;
    }
}
