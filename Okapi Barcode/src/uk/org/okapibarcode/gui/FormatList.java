package uk.org.okapibarcode.gui;

import java.util.*;
/**
 *
 * @author Robert Elliott <jakel2006@me.com>
 */
public class FormatList {
    ArrayList formats = new ArrayList();
    public FormatList() {
        formats.add("--------");
        formats.add("jpg");
        formats.add("png");
        formats.add("gif");
        formats.add("bmp");
        formats.add("svg");
        formats.add("eps");
    }
}

