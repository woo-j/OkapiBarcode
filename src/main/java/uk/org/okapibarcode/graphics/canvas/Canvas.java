package uk.org.okapibarcode.graphics.canvas;

import uk.org.okapibarcode.graphics.color.Rgb;
import uk.org.okapibarcode.graphics.font.Font;
import uk.org.okapibarcode.graphics.shape.Area;
import uk.org.okapibarcode.graphics.shape.Hexagon;
import uk.org.okapibarcode.graphics.shape.Rectangle;
import uk.org.okapibarcode.graphics.shape.SubtractArea;

/**
 * @author shixi
 * @date 2022/8/5 11:29 AM
 */
public interface Canvas {
    /**
     * fill rect area
     *
     * @param rectangle area
     */
    void fillRect(Rectangle rectangle);

    /**
     * draw string
     *
     * @param text text
     * @param x    x
     * @param y    y
     */
    void drawString(String text, double x, double y);


    /**
     * metric text rect
     *
     * @param text
     * @return
     */
    Rectangle metricText(String text);

    /**
     * fill hexagon
     *
     * @param hexagon
     */
    void fill(Hexagon hexagon);

    /**
     * fill area
     * @param area
     */
    void fill(Area area);

    /**
     * set color with rgb
     *
     * @param rgb
     */
    void setColor(Rgb rgb);

    /**
     * config font
     *
     * @param font
     */
    void setFont(Font font);

    Font getFont();

    Font newFont(String name,int size);
}
