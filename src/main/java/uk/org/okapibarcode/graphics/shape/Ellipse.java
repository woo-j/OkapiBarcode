package uk.org.okapibarcode.graphics.shape;

/**
 *
 * @author shixi
 * @date 2022/8/4 3:25 PM
 */
public class Ellipse {

    public double x;

    public double y;

    public double width;

    public double height;

    public Ellipse(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Ellipse() {
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public void setFrameFromCenter(double centerX, double centerY,
                                   double cornerX, double cornerY) {
        double halfW = Math.abs(cornerX - centerX);
        double halfH = Math.abs(cornerY - centerY);
        setFrame(centerX - halfW, centerY - halfH, halfW * 2.0, halfH * 2.0);
    }
    public void setFrame(double x, double y, double w, double h) {
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;
    }
}
