package uk.org.okapibarcode.graphics.font.impl;

import uk.org.okapibarcode.graphics.canvas.Canvas;
import uk.org.okapibarcode.graphics.canvas.impl.AwtCanvas;
import uk.org.okapibarcode.graphics.font.Font;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;
import java.util.Collections;


/**
 * @author shixi
 * @date 2022/8/5 2:24 PM
 */
public class AwtFont extends Font {

    final java.awt.Font font;

    public AwtFont(java.awt.Font font) {
        this.font = font;
    }

    @Override
    public void apply(Canvas canvas) {
        if (canvas instanceof AwtCanvas) {
            AwtCanvas awtCanvas = (AwtCanvas) canvas;
            Graphics2D graphics = awtCanvas.graphics();
            graphics.setFont(font);
        }
    }

    @Override
    public Font configTracking(double width, String text, Canvas canvas) {
        Font oldFont = canvas.getFont();
        canvas.setFont(this);
        double originalWidth = canvas.metricText(text).getWidth();
        double extraSpace = width - originalWidth;
        double extraSpacePerGap = extraSpace / (text.length() - 1);
        double scaleX = (font.isTransformed() ? font.getTransform().getScaleX() : 1);
        double tracking = extraSpacePerGap / (font.getSize2D() * scaleX);
        java.awt.Font awtFont = font.deriveFont(Collections.singletonMap(TextAttribute.TRACKING, tracking));
        canvas.setFont(oldFont);
        return new AwtFont(awtFont);
    }

    @Override
    public Font applyTracking(double tracking) {
        java.awt.Font awtFont = font.deriveFont(Collections.singletonMap(TextAttribute.TRACKING, tracking));
        return new AwtFont(awtFont);

    }

    @Override
    public Font deriveFont(double fontSize) {
        java.awt.Font awtFont = font.deriveFont((float) fontSize);
        return new AwtFont(awtFont);
    }

    @Override
    public double fontSize() {
        return font.getSize2D();
    }
}
