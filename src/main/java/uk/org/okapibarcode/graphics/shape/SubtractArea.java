package uk.org.okapibarcode.graphics.shape;

/**
 * 裁剪区域
 *
 * @author shixi
 * @date 2022/8/9 5:02 PM
 */
public class SubtractArea extends Area {
    Ellipse outer;
    Ellipse inner;

    public SubtractArea(Ellipse outer, Ellipse inner) {
        this.outer = outer;
        this.inner = inner;
    }

    public Ellipse getOuter() {
        return outer;
    }

    public Ellipse getInner() {
        return inner;
    }
}
