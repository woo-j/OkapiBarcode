package uk.org.okapibarcode.graphics.shape;

/**
 * @author shixi
 * @date 2022/8/4 3:27 PM
 */
public class Rectangle {

    public double x;

    public double y;

    public double width;

    public double height;

    public Rectangle(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
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
    public double getMaxX() {
        return getX() + getWidth();
    }
    public double getMaxY() {
        return getY() + getHeight();
    }
}
