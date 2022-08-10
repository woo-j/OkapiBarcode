package uk.org.okapibarcode.output;

import org.junit.Test;
import uk.org.okapibarcode.backend.*;
import uk.org.okapibarcode.graphics.canvas.impl.AwtCanvas;
import uk.org.okapibarcode.graphics.color.Rgb;
import uk.org.okapibarcode.graphics.font.impl.AwtFont;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collections;

/**
 * @author shixi
 * @date 2022/8/10 5:15 PM
 */
public class CanvasRendererTest {

    @Test
    public void testRender() throws IOException {
        BufferedImage image=new BufferedImage(100,100,BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        SymbolRenderer symbolRenderer = new CanvasRenderer(new AwtCanvas(graphics), Rgb.WHITE,Rgb.BLACK,1,new AwtFont(graphics.getFont()));
        Symbol symbol=new Code128();
        symbol.setContent("abc");
        symbolRenderer.render(symbol);
        System.out.println('a');
    }

    @Test
   public void testPaperColor() throws IOException {
        Code128 code128 = new Code128();
        code128.setFontName(SymbolTest.DEJA_VU_SANS.getFontName());
        code128.setContent("123456");

        DataMatrix datamatrix = new DataMatrix();
        datamatrix.setContent("ABCDEFG");

        MaxiCode maxicode = new MaxiCode();
        maxicode.setMode(4);
        maxicode.setContent("ABCDEFG");

        int width = 750;
        int height = 650;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setPaint(new GradientPaint(0, 0, Color.ORANGE, width, height, Color.GREEN));
        g2d.fillRect(0, 0, width, height);
        CanvasRenderer renderer=new CanvasRenderer(new AwtCanvas(g2d),null,Rgb.BLACK,4,null);
        g2d.translate(25, 25);
        renderer.render(code128);
        g2d.translate(300, 0);
        renderer.render(datamatrix);
        g2d.translate(100, 0);
        renderer.render(maxicode);

        CanvasRenderer renderer2=new CanvasRenderer(new AwtCanvas(g2d),Rgb.WHITE,Rgb.BLACK,4,null);

        g2d.translate(-400, 300);
        renderer2.render(code128);
        g2d.translate(300, 0);
        renderer2.render(datamatrix);
        g2d.translate(100, 0);
        renderer2.render(maxicode);

        String filename = "java-2d-paper-color.png";
        BufferedImage expected = ImageIO.read(getClass().getResourceAsStream(filename));
        String dirName = filename.substring(0, filename.lastIndexOf('.'));
        SymbolTest.assertEqual(expected, image, dirName);
    }
    @Test
    public void testCustomFontStrikethrough() throws Exception {

        Font font = SymbolTest.DEJA_VU_SANS.deriveFont((float) 18);
        font = font.deriveFont(Collections.singletonMap(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON));

        testFont(font, HumanReadableAlignment.CENTER, "java-2d-custom-font-strikethrough.png");
    }

    @Test
    public void testCustomFontJustifyAndTransform() throws Exception {

        Font font = SymbolTest.DEJA_VU_SANS.deriveFont((float) 3);
        font = font.deriveFont(AffineTransform.getScaleInstance(5, 1));

        testFont(font, HumanReadableAlignment.JUSTIFY, "java-2d-custom-font-justify-transform.png");
    }

    private static void testFont(Font font, HumanReadableAlignment alignment, String filename) throws IOException {

        Code128 code128 = new Code128();
        code128.setHumanReadableAlignment(alignment);
//        code128.setFont(font);
        code128.setFontName(font.getFontName());
        code128.setFontSize(font.getSize());
        code128.setContent("123456");

        int magnification = 4;
        int w = code128.getWidth() * magnification;
        int h = code128.getHeight() * magnification;

        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g2d = image.createGraphics();

        CanvasRenderer renderer = new CanvasRenderer(new AwtCanvas(g2d), Rgb.WHITE,Rgb.BLACK,magnification, new AwtFont(font));
        renderer.render(code128);

        BufferedImage expected = ImageIO.read(Java2DRendererTest.class.getResourceAsStream(filename));
        String dirName = filename.substring(0, filename.lastIndexOf('.'));
        SymbolTest.assertEqual(expected, image, dirName);
    }
}