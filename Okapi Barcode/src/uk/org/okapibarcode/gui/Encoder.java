package uk.org.okapibarcode.gui;

import uk.org.okapibarcode.backend.Barcode;
/**
 *
 * @author Robert Elliott <jakel2006@me.com>
 */
public class Encoder {

    public boolean encodeMe() {
        Barcode barcode = new Barcode();

        OkapiUI.errorOutput = "";
        OkapiUI.encodeInfo = "";

        if (OkapiUI.symbology == null) {
            OkapiUI.errorOutput = "No symbology selected";
            OkapiUI.encodeInfo = "Error: No symbology selected";
            return false;
        }
        
        barcode.setNormalMode();
        if (OkapiUI.dataInput.charAt(0) == '[') {
            // FIXME: Should get option from user instead
            barcode.setGs1Mode();
        }
        
        if (!(OkapiUI.compositeInput.isEmpty())) {
            barcode.setCompositeContent(OkapiUI.compositeInput);
        }

        if (barcode.encode(OkapiUI.symbology, OkapiUI.dataInput)) {
            OkapiUI.bcs = barcode.rect;
            OkapiUI.height = barcode.symbol_height;
            OkapiUI.width = barcode.symbol_width;
            OkapiUI.txt = barcode.txt;
            OkapiUI.hex = barcode.hex;
            OkapiUI.target = barcode.target;
            OkapiUI.encodeInfo = barcode.encodeInfo;
        } else {
            OkapiUI.errorOutput = barcode.error_msg;
            OkapiUI.encodeInfo = barcode.error_msg;
        }

        if (!(OkapiUI.txt.isEmpty())) {
            // Add some space for text
            OkapiUI.height += 10;
        }
        if (!(OkapiUI.errorOutput.isEmpty())) {
//            System.out.print("Encoding error: ");
//            System.out.println(MainInterface.errorOutput);
            return false;
        } else {
//            System.out.println("Success!");
            return true;
        }
    }
}
