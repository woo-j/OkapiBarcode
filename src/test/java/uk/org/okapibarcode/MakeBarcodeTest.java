/*
 * Copyright 2018 Daniel Gredler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.org.okapibarcode;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author tangfeifei
 */
public class MakeBarcodeTest {

    private final String data_na = "1234567890ABC";

    private final String data_ean13 = "6901236348338";

    private final String data_ean13_ = "690123634833";

    private final String data_gs1 = "[01]06901236348338[10]batch666[11]231127[21]SERIAL999(30)5(3102)666999";

    private final String data_gs1_ = "(01)06901236348338(10)batch666(11)231127(21)SERIAL999(30)5(3102)666999";

    private final String data_unicode = "12345上山打老虎";

    private final String svg_result = "<?xml";

    public MakeBarcodeTest() {
    }

    @Test
    public void testCode128() {

        System.out.println("-------------code128---------------");

        MakeBarcode makeBarcode = new MakeBarcode();

        Settings settings = new Settings();

        settings.setSymbolType(SymbolType.code128.getValue());

        settings.setSymbolHeight(50);
        settings.setSymbolWhiteSpace(5);
        settings.setSymbolScale(2);

        byte[] data = makeBarcode.processToByte(
                settings, data_na,
                SymbolFormat.svg.getValue());

        assertNotNull(data);

        String svg = new String(data);

        System.out.println(svg);

        assertTrue(svg.startsWith(svg_result));
    }

    @Test
    public void testEanx() {

        System.out.println("-------------ean13---------------");

        MakeBarcode makeBarcode = new MakeBarcode();

        Settings settings = new Settings();

        settings.setSymbolType(SymbolType.eanx.getValue());

        // the method validateAndPad is not smart.
        byte[] data = makeBarcode.processToByte(
                settings, data_ean13,
                SymbolFormat.svg.getValue());

        assertNotNull(data);

        String svg = new String(data);

        System.out.println(svg);

        assertTrue(svg.startsWith(svg_result));
    }

    @Test
    public void testEanx_() {

        System.out.println("-------------ean13_---------------");

        MakeBarcode makeBarcode = new MakeBarcode();

        Settings settings = new Settings();

        settings.setSymbolType(SymbolType.eanx.getValue());

        byte[] data = makeBarcode.processToByte(
                settings, data_ean13_,
                SymbolFormat.svg.getValue());

        assertNotNull(data);

        String svg = new String(data);

        System.out.println(svg);

        assertTrue(svg.startsWith(svg_result));
    }

    @Test
    public void testCode128_GS1() {

        System.out.println("-------------GS1-128---------------");

        MakeBarcode makeBarcode = new MakeBarcode();

        Settings settings = new Settings();

        settings.setSymbolType(SymbolType.code128.getValue());

        settings.setDataGs1Mode(true);

        byte[] data = makeBarcode.processToByte(
                settings, data_gs1,
                SymbolFormat.svg.getValue());

        assertNotNull(data);

        String svg = new String(data);

        System.out.println(svg);

        assertTrue(svg.startsWith(svg_result));
    }

    @Test
    public void testDatamatrix_GS1() {

        System.out.println("-------------datamatrix---------------");

        MakeBarcode makeBarcode = new MakeBarcode();

        Settings settings = new Settings();

        settings.setSymbolType(SymbolType.datamatrix.getValue());

        settings.setDataGs1Mode(true);
        settings.setSymbolWhiteSpace(5);
        settings.setSymbolScale(2);

        byte[] data = makeBarcode.processToByte(
                settings, data_gs1_,
                SymbolFormat.svg.getValue());

        assertNotNull(data);

        String svg = new String(data);

        System.out.println(svg);

        assertTrue(svg.startsWith(svg_result));
    }

    @Test
    public void testQrcode() {

        System.out.println("-------------qrcode---------------");

        MakeBarcode makeBarcode = new MakeBarcode();

        Settings settings = new Settings();

        settings.setSymbolType(SymbolType.qrcode.getValue());

        settings.setSymbolWhiteSpace(5);
        settings.setSymbolScale(2);

        byte[] data = makeBarcode.processToByte(
                settings, data_unicode,
                SymbolFormat.svg.getValue());

        assertNotNull(data);

        String svg = new String(data);

        System.out.println(svg);

        assertTrue(svg.startsWith(svg_result));
    }

    @Test
    public void testPdf417() {

        System.out.println("-------------pdf417---------------");

        MakeBarcode makeBarcode = new MakeBarcode();

        Settings settings = new Settings();

        settings.setSymbolType(SymbolType.pdf417.getValue());

        settings.setSymbolWhiteSpace(5);
        settings.setSymbolScale(2);

        byte[] data = makeBarcode.processToByte(
                settings, data_na + data_na,
                SymbolFormat.svg.getValue());

        assertNotNull(data);

        String svg = new String(data);

        System.out.println(svg);

        assertTrue(svg.startsWith(svg_result));
    }

}
