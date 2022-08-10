package uk.org.okapibarcode.graphics.font;


import uk.org.okapibarcode.graphics.canvas.Canvas;

/**
 * @author shixi
 * @date 2022/8/5 3:01 PM
 */

public class Font {


    /**
     * apply font
     *
     * @param canvas
     */
    public void apply(Canvas canvas) {
        throw new IllegalStateException("apply not impl");
    }

    public Font configTracking(double width,String text,Canvas canvas) {
        throw new IllegalStateException("configTracking not impl");
    }

    public Font applyTracking(double tracking) {
        throw new IllegalStateException("applyTracking not impl");
    }

    /**
     * change font size and get a new font
     *
     * @param fontSize
     * @return
     */
    public Font deriveFont(double fontSize) {
        throw new IllegalStateException("configTracking not impl");
    }

    public double fontSize(){
        throw new IllegalStateException("fontSize not impl");
    }


}
