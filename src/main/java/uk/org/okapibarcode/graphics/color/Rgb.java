package uk.org.okapibarcode.graphics.color;

/**
 * @author shixi
 * @date 2022/8/5 11:31 AM
 */

public class Rgb {

    public static final Rgb BLACK = new Rgb(0, 0, 0);
    public static final Rgb WHITE = new Rgb(255, 255, 255);
    public final int r;
    public final int g;
    public final int b;

    public Rgb(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public Rgb(int rgb) {
        this(rgb & 0xffff0000, rgb & 0xff00ff00, rgb & 0xff0000ff);
    }
}
