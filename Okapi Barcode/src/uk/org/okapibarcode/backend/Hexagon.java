package uk.org.okapibarcode.backend;

/**
 * Calculate a set of points to make a hexagon
 *
 * @author Robin Stuart <rstuart114@gmail.com>
 */
public class Hexagon {
    public double centreX;
    public double centreY;
    public double[] pointX = new double [6];
    public double[] pointY = new double [6];
    private double inkSpread = 1.25;
    
    private double[] yOffset = {
        1.0, 0.5, -0.5, -1.0, -0.5, 0.5
    };
    
    private double[] xOffset = {
        0.0, 0.86, 0.86, 0.0, -0.86, -0.86
    };
    
    public void setCentre(double x, double y) {
        int i;
        
        centreX = x;
        centreY = y;
        for (i = 0; i < 6; i++) {
            pointX[i] = centreX + (xOffset[i] * inkSpread);
            pointY[i] = centreY + (yOffset[i] * inkSpread);
        }
    }
}
