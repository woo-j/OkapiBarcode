package uk.org.okapibarcode.output;

import uk.org.okapibarcode.backend.HumanReadableAlignment;
import uk.org.okapibarcode.backend.Symbol;
import uk.org.okapibarcode.backend.TextBox;
import uk.org.okapibarcode.graphics.canvas.Canvas;
import uk.org.okapibarcode.graphics.color.Rgb;
import uk.org.okapibarcode.graphics.font.Font;
import uk.org.okapibarcode.graphics.shape.Ellipse;
import uk.org.okapibarcode.graphics.shape.Hexagon;
import uk.org.okapibarcode.graphics.shape.Rectangle;
import uk.org.okapibarcode.graphics.shape.SubtractArea;

import java.io.IOException;
import java.util.List;

import static uk.org.okapibarcode.backend.HumanReadableAlignment.CENTER;
import static uk.org.okapibarcode.backend.HumanReadableAlignment.JUSTIFY;

/**
 * @author shixi
 * @date 2022/8/5 2:31 PM
 */
public class CanvasRenderer implements SymbolRenderer {
    private final Canvas canvas;
    private final Rgb paper;
    private final Rgb link;
    private final double magnification;
    private final Font font;

    public CanvasRenderer(Canvas canvas, Rgb paper, Rgb link, double magnification, Font font) {
        this.canvas = canvas;
        this.paper = paper;
        this.link = link;
        this.magnification = magnification;
        this.font = font;
    }


    @Override
    public void render(Symbol symbol) throws IOException {
        int marginX = (int) (symbol.getQuietZoneHorizontal() * magnification);
        int marginY = (int) (symbol.getQuietZoneVertical() * magnification);
        Font f = font;
        if (f != null) {
            f = f.deriveFont(f.fontSize() * magnification);
        } else {
            f = canvas.newFont(symbol.getFontName(), (int)(symbol.getFontSize() * magnification));
            f = f.applyTracking(0);
        }
        if (paper != null) {
            int w = (int) (symbol.getWidth() * magnification);
            int h = (int) (symbol.getHeight() * magnification);
            canvas.setColor(paper);
            canvas.fillRect(new Rectangle(0, 0, w, h));
        }
        canvas.setColor(link);
        for (Rectangle rect : symbol.getRectangles()) {
            double x = (rect.x * magnification) + marginX;
            double y = (rect.y * magnification) + marginY;
            double w = rect.width * magnification;
            double h = rect.height * magnification;
            canvas.fillRect(new Rectangle(x, y, w, h));
        }
        for (TextBox text : symbol.getTexts()) {
            HumanReadableAlignment alignment = (text.alignment == JUSTIFY && text.text.length() == 1 ? CENTER : text.alignment);
            Font font = alignment != JUSTIFY ? f : f.configTracking(text.width * magnification, text.text, canvas);
            canvas.setFont(font);
            Rectangle bounds = canvas.metricText(text.text);
            float y = (float) (text.y * magnification) + marginY;
            float x;
            switch (alignment) {
                case LEFT:
                case JUSTIFY:
                    x = (float) ((magnification * text.x) + marginX);
                    break;
                case RIGHT:
                    x = (float) ((magnification * text.x) + (magnification * text.width) - bounds.getWidth() + marginX);
                    break;
                case CENTER:
                    x = (float) ((magnification * text.x) + (magnification * text.width / 2) - (bounds.getWidth() / 2) + marginX);
                    break;
                default:
                    throw new IllegalStateException("Unknown alignment: " + alignment);
            }
            canvas.drawString(text.text, x, y);
        }

        for (Hexagon hexagon : symbol.getHexagons()) {
            Hexagon newHexagon = new Hexagon(hexagon.centreX, hexagon.centreY);
            newHexagon.scala(magnification);
            newHexagon.offset(marginX, marginY);
            canvas.fill(newHexagon);
        }

        List<Ellipse> target = symbol.getTarget();
        for (int i = 0; i + 1 < target.size(); i += 2) {
            Ellipse outer = adjust(target.get(i), magnification, marginX, marginY);
            Ellipse inner = adjust(target.get(i + 1), magnification, marginX, marginY);
            SubtractArea subtractArea = new SubtractArea(outer, inner);
            canvas.fill(subtractArea);
        }
    }

    private static Ellipse adjust(Ellipse ellipse, double magnification, int marginX, int marginY) {
        double x = (ellipse.x * magnification) + marginX;
        double y = (ellipse.y * magnification) + marginY;
        double w = (ellipse.width * magnification) + marginX;
        double h = (ellipse.height * magnification) + marginY;
        return new Ellipse(x, y, w, h);
    }
}
