package uk.org.okapibarcode.graphics.canvas.impl;

import uk.org.okapibarcode.graphics.canvas.Canvas;
import uk.org.okapibarcode.graphics.color.Rgb;
import uk.org.okapibarcode.graphics.font.Font;
import uk.org.okapibarcode.graphics.font.impl.AwtFont;
import uk.org.okapibarcode.graphics.shape.*;
import uk.org.okapibarcode.graphics.shape.Rectangle;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

/**
 * awt graphics
 *
 * @author shixi
 * @date 2022/8/5 2:09 PM
 */
public class AwtCanvas implements Canvas {
    final Graphics2D graphics;
    Font font;

    public AwtCanvas(Graphics2D graphics) {
        this.graphics = graphics;
        font = new AwtFont(this.graphics.getFont());
    }

    public Graphics2D graphics() {
        return graphics;
    }

    @Override
    public void fillRect(Rectangle rectangle) {
        Rectangle2D.Double rect = new Rectangle2D.Double(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
        graphics.fill(rect);
    }

    @Override
    public void drawString(String text, double x, double y) {
        graphics.drawString(text, (float) x, (float) y);
    }


    @Override
    public Rectangle metricText(String text) {
        Rectangle2D bounds = graphics.getFontMetrics().getStringBounds(text, graphics);
        return new Rectangle(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
    }

    @Override
    public void fill(Hexagon hexagon) {
        Polygon polygon = new Polygon();
        for (int j = 0; j < 6; j++) {
            polygon.addPoint((int) hexagon.pointX[j], (int) hexagon.pointY[j]);
        }
        graphics.fill(polygon);
    }

    @Override
    public void fill(Area area) {

        if (area instanceof SubtractArea) {
            SubtractArea subtractArea = (SubtractArea) area;
            Ellipse outer = subtractArea.getOuter();
            Ellipse inner = subtractArea.getInner();
            java.awt.geom.Area awtArea = new java.awt.geom.Area(new Ellipse2D.Double(outer.x, outer.y, outer.width, outer.height));
            awtArea.subtract(new java.awt.geom.Area(new Ellipse2D.Double(inner.x, inner.y, inner.width, inner.height)));
            graphics.fill(awtArea);
        }
    }

    @Override
    public void setColor(Rgb rgb) {
        graphics.setColor(new Color(rgb.r, rgb.g, rgb.b));
    }

    @Override
    public void setFont(Font font) {
        this.font = font;
        font.apply(this);
    }

    @Override
    public Font getFont() {
        return this.font;
    }

    @Override
    public Font newFont(String name, int size) {
        return new AwtFont(new java.awt.Font(name, java.awt.Font.PLAIN, size));
    }
}
