/*
 * Copyright 2015 Robin Stuart
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

import static java.awt.Color.BLACK;
import static java.awt.Color.WHITE;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import uk.org.okapibarcode.backend.AustraliaPost;
import uk.org.okapibarcode.backend.AztecCode;
import uk.org.okapibarcode.backend.AztecRune;
import uk.org.okapibarcode.backend.ChannelCode;
import uk.org.okapibarcode.backend.Codabar;
import uk.org.okapibarcode.backend.CodablockF;
import uk.org.okapibarcode.backend.Code11;
import uk.org.okapibarcode.backend.Code128;
import uk.org.okapibarcode.backend.Code16k;
import uk.org.okapibarcode.backend.Code2Of5;
import uk.org.okapibarcode.backend.Code32;
import uk.org.okapibarcode.backend.Code3Of9;
import uk.org.okapibarcode.backend.Code3Of9Extended;
import uk.org.okapibarcode.backend.Code49;
import uk.org.okapibarcode.backend.Code93;
import uk.org.okapibarcode.backend.CodeOne;
import uk.org.okapibarcode.backend.Composite;
import uk.org.okapibarcode.backend.DataBar14;
import uk.org.okapibarcode.backend.DataBarExpanded;
import uk.org.okapibarcode.backend.DataBarLimited;
import uk.org.okapibarcode.backend.DataMatrix;
import uk.org.okapibarcode.backend.Ean;
import uk.org.okapibarcode.backend.GridMatrix;
import uk.org.okapibarcode.backend.JapanPost;
import uk.org.okapibarcode.backend.KixCode;
import uk.org.okapibarcode.backend.KoreaPost;
import uk.org.okapibarcode.backend.Logmars;
import uk.org.okapibarcode.backend.MaxiCode;
import uk.org.okapibarcode.backend.MicroQrCode;
import uk.org.okapibarcode.backend.MsiPlessey;
import uk.org.okapibarcode.backend.Nve18;
import uk.org.okapibarcode.backend.OkapiException;
import uk.org.okapibarcode.backend.Pdf417;
import uk.org.okapibarcode.backend.Pharmacode;
import uk.org.okapibarcode.backend.Pharmacode2Track;
import uk.org.okapibarcode.backend.Pharmazentralnummer;
import uk.org.okapibarcode.backend.Postnet;
import uk.org.okapibarcode.backend.QrCode;
import uk.org.okapibarcode.backend.RoyalMail4State;
import uk.org.okapibarcode.backend.Symbol;
import uk.org.okapibarcode.backend.Telepen;
import uk.org.okapibarcode.backend.Upc;
import uk.org.okapibarcode.backend.UspsOneCode;
import uk.org.okapibarcode.backend.UspsPackage;
import uk.org.okapibarcode.output.PostScriptRenderer;
import uk.org.okapibarcode.output.SvgRenderer;

/**
 *
 * @author <a href="mailto:rstuart114@gmail.com">Robin Stuart</a>
 */
public class MakeBarcode {
    
    public void MakeBarcode() {
    }
    
    public void process(Settings settings, String dataInput) {
        int type = settings.getSymbolType();
        int magnification = 1;
        int borderSize = 0;
        Symbol symbol;
        String extension = "";
        
        try {
            /* values marked "Legacy" are for compatability purposes
               and should not be documented.
            */
            switch(type) {
                case 1:
                    // Code 11
                    Code11 code11 = new Code11();
                    code11.setContent(dataInput);
                    symbol = code11;
                    break;
                case 2:
                    // Code 2 of 5
                    Code2Of5 c25matrix = new Code2Of5();
                    c25matrix.setMatrixMode();
                    c25matrix.setContent(dataInput);
                    symbol = c25matrix;
                    break;
                case 3:
                    //Interleaved 2 of 5
                    Code2Of5 c25inter = new Code2Of5();
                    c25inter.setInterleavedMode();
                    c25inter.setContent(dataInput);
                    symbol = c25inter;
                    break;
                case 4:
                    // IATA 2 of 5
                    Code2Of5 c25iata = new Code2Of5();
                    c25iata.setIATAMode();
                    c25iata.setContent(dataInput);       
                    symbol = c25iata;
                    break;
                case 6:
                    // Data Logic
                    Code2Of5 c25logic = new Code2Of5();
                    c25logic.setDataLogicMode();
                    c25logic.setContent(dataInput);
                    symbol = c25logic;
                    break;
                case 7:
                    // Industrial 2 of 5
                    Code2Of5 c25ind = new Code2Of5();
                    c25ind.setIndustrialMode();
                    c25ind.setContent(dataInput);
                    symbol = c25ind;                    
                    break;
                case 8:
                case 99:
                case 101: // Legacy
                    // Code 39
                    Code3Of9 code3of9 = new Code3Of9();
                    if ((type == 99) || (type == 101)) {
                        code3of9.setDataType(Symbol.DataType.HIBC);
                    }
                    code3of9.setContent(dataInput);
                    symbol = code3of9;
                    break;
                case 9:
                    // Extended Code 39
                    Code3Of9Extended code3of9ext = new Code3Of9Extended();
                    code3of9ext.setContent(dataInput);
                    symbol = code3of9ext;
                    break;
                case 13:
                case 14: // Legacy
                case 15: // Legacy
                case 10: // Legacy
                case 11: // Legacy
                case 12: // Legacy
                    // EAN
                    Ean ean = new Ean();
                    if (eanCalculateVersion(dataInput) == 8) {
                        ean.setMode(Ean.Mode.EAN8);
                    } else {
                        ean.setMode(Ean.Mode.EAN13);
                    }
                    ean.setContent(dataInput);
                    symbol = ean;
                    break;
                case 18:
                    // Codabar
                    Codabar codabar = new Codabar();
                    codabar.setContent(dataInput); 
                    symbol = codabar;
                    break;
                case 20:
                case 60:
                case 98:
                case 16: // Legacy
                case 59: // Legacy
                case 61: // Legacy
                case 100: // Legacy
                    // Code 128
                    Code128 code128 = new Code128();
                    code128.unsetCc();
                    if (settings.isDataGs1Mode() || (type == 16)) {
                        code128.setDataType(Symbol.DataType.GS1);
                    }
                    if ((type == 98) || (type == 100)) {
                        code128.setDataType(Symbol.DataType.HIBC);
                    }
                    if (type == 60) {
                        code128.stopModeC();
                    }
                    if (settings.isReaderInit()) {
                        code128.setReaderInit();
                    }
                    code128.setContent(dataInput);
                    symbol = code128;
                    break;                    
                case 21:
                    // Leitcode
                    Code2Of5 dpLeit = new Code2Of5();
                    dpLeit.setDPLeitMode();
                    dpLeit.setContent(dataInput);
                    symbol = dpLeit;
                    break;
                case 22:
                    // Identcode
                    Code2Of5 dpIdent = new Code2Of5();
                    dpIdent.setDPIdentMode();
                    dpIdent.setContent(dataInput);
                    symbol = dpIdent;
                    break;
                case 23:
                    // Code 16k
                    Code16k code16k = new Code16k();
                    if (settings.isDataGs1Mode()) {
                        code16k.setDataType(Symbol.DataType.GS1);
                    }
                    if (settings.isReaderInit()) {
                        code16k.setReaderInit();
                    }
                    code16k.setContent(dataInput);
                    symbol = code16k;
                    break;
                case 24:
                    // Code 49
                    Code49 code49 = new Code49();
                    code49.setContent(dataInput);
                    symbol = code49;
                    break;
                case 25:
                    // Code 93
                    Code93 code93 = new Code93();
                    code93.setContent(dataInput);
                    symbol = code93;
                    break;
                case 29:
                    // Databar-14
                    DataBar14 dataBar14 = new DataBar14();
                    dataBar14.setLinearMode();
                    dataBar14.setContent(dataInput);
                    symbol = dataBar14;
                    break;
                case 30:
                    // Databar Limited
                    DataBarLimited dataBarLimited = new DataBarLimited();
                    dataBarLimited.setContent(dataInput);
                    symbol = dataBarLimited;
                    break;
                case 31:
                    // Databar Expanded
                    DataBarExpanded dataBarE = new DataBarExpanded();
                    dataBarE.setNotStacked();
                    dataBarE.setContent(dataInput);
                    symbol = dataBarE;
                    break;
                case 32:
                    // Telepen Alpha
                    Telepen telepen = new Telepen();
                    telepen.setNormalMode();
                    telepen.setContent(dataInput);
                    symbol = telepen;
                    break;
                case 34:
                case 35: // Legacy
                case 36: // Legacy
                    // UPC-A
                    Upc upca = new Upc();
                    upca.setMode(Upc.Mode.UPCA);
                    upca.unsetLinkageFlag();
                    upca.setContent(dataInput);
                    symbol = upca;
                    break;
                case 37:
                case 38: // Legacy
                case 39: // Legacy
                    // UPC-E
                    Upc upce = new Upc();
                    upce.setMode(Upc.Mode.UPCE);
                    upce.unsetLinkageFlag();
                    upce.setContent(dataInput);
                    symbol = upce;
                    break;
                case 40:
                case 41: // Legacy
                case 42: // Legacy
                case 43: // Legacy
                case 44: // Legacy
                case 45: // Legacy
                case 54:
                    // Postnet and Brizillian CepNet
                    Postnet postnet = new Postnet();
                    postnet.setPostnet();
                    postnet.setContent(dataInput);
                    symbol = postnet;
                    break;
                case 47:
                    // MSI Plessey
                    MsiPlessey msiPlessey = new MsiPlessey();
                    msiPlessey.setContent(dataInput);
                    symbol = msiPlessey;
                    break;
                case 50:
                    // LOGMARS
                    Logmars logmars = new Logmars();
                    logmars.setContent(dataInput);
                    symbol = logmars;
                    break;
                case 51:
                    // Pharmacode One-Track
                    Pharmacode pharmacode = new Pharmacode();
                    pharmacode.setContent(dataInput);
                    symbol = pharmacode;
                    break;
                case 53:
                    // Pharmacode Two-Track
                    Pharmacode2Track pharmacode2t = new Pharmacode2Track();
                    pharmacode2t.setContent(dataInput);
                    symbol = pharmacode2t;
                    break;
                case 55:
                case 56:
                case 106:
                case 107: // Legacy
                    // PDF417
                    Pdf417 pdf417 = new Pdf417();
                    pdf417.setNormalMode();
                    if (settings.isDataGs1Mode()) {
                        pdf417.setDataType(Symbol.DataType.GS1);
                    }
                    if ((type == 106) || (type == 107)) {
                        pdf417.setDataType(Symbol.DataType.HIBC);
                    } else if (type == 56) {
                        pdf417.setTruncMode();
                    }
                    pdf417.setPreferredEccLevel(settings.getSymbolECC() - 1);
                    pdf417.setNumberOfColumns(settings.getSymbolColumns());
                    if (settings.isReaderInit()) {
                        pdf417.setReaderInit();
                    }
                    pdf417.setContent(dataInput);
                    symbol = pdf417;
                    break;
                case 57:
                    // Maxicode
                    MaxiCode maxiCode = new MaxiCode();
                    maxiCode.setPrimary(settings.getPrimaryData());
                    maxiCode.setMode(settings.getEncodeMode());
                    maxiCode.setContent(dataInput);
                    symbol = maxiCode;
                    break;
                case 58:
                case 104:
                case 105: // Legacy
                    // QR Code
                    QrCode qrCode = new QrCode();
                    if (settings.isDataGs1Mode()) {
                        qrCode.setDataType(Symbol.DataType.GS1);
                    }
                    if ((type == 104) || (type == 105)) {
                        qrCode.setDataType(Symbol.DataType.HIBC);
                    }
                    switch(settings.getSymbolECC()) {
                        case 0:
                            qrCode.setEccMode(QrCode.EccMode.L);
                            break;
                        case 1:
                            qrCode.setEccMode(QrCode.EccMode.M);
                            break;
                        case 2:
                            qrCode.setEccMode(QrCode.EccMode.Q);
                            break;
                        case 3:
                            qrCode.setEccMode(QrCode.EccMode.H);
                            break;
                    }
                    qrCode.setPreferredVersion(settings.getSymbolVersion());
                    if (settings.isReaderInit()) {
                        qrCode.setReaderInit();
                    }
                    qrCode.setContent(dataInput);
                    symbol = qrCode;
                    break;
                case 63:
                case 64: // Legacy
                case 65: // Legacy
                    // Australia Post Standard Customer
                    AustraliaPost auPost = new AustraliaPost();
                    auPost.setPostMode();
                    auPost.setContent(dataInput);
                    symbol = auPost;
                    break;
                case 66:
                    // Australia Post Reply Paid
                    AustraliaPost auReply = new AustraliaPost();
                    auReply.setReplyMode();
                    auReply.setContent(dataInput);
                    symbol = auReply;
                    break;
                case 67:
                    // Australia Post Re-Routing
                    AustraliaPost auRoute = new AustraliaPost();
                    auRoute.setRouteMode();
                    auRoute.setContent(dataInput);
                    symbol = auRoute;
                    break;
                case 68:
                    // Australia Post Redirection
                    AustraliaPost auRedirect = new AustraliaPost();
                    auRedirect.setRedirectMode();
                    auRedirect.setContent(dataInput);
                    symbol = auRedirect;
                    break;
                case 70:
                    // RM4SCC
                    RoyalMail4State royalMail = new RoyalMail4State();
                    royalMail.setContent(dataInput);
                    symbol = royalMail;
                    break;
                case 71:
                case 102:
                case 103: // Legacy
                    // Data Matrix
                    DataMatrix dataMatrix = new DataMatrix();
                    if (settings.isDataGs1Mode()) {
                        dataMatrix.setDataType(Symbol.DataType.GS1);
                    }
                    if ((type == 102) || (type == 103)) {
                        dataMatrix.setDataType(Symbol.DataType.HIBC);
                    }
                    if (settings.isReaderInit()) {
                        dataMatrix.setReaderInit();
                    }
                    dataMatrix.setPreferredSize(settings.getSymbolVersion());
                    dataMatrix.forceSquare(settings.isMakeSquare());
                    dataMatrix.setContent(dataInput);
                    symbol = dataMatrix;
                    break;
                case 74:
                case 110:
                case 111: // Legacy
                    // Codablock-F
                    CodablockF codablockF = new CodablockF();
                    if (settings.isDataGs1Mode()) {
                        codablockF.setDataType(Symbol.DataType.GS1);
                    }
                    if ((type == 110) || (type == 111)) {
                        codablockF.setDataType(Symbol.DataType.HIBC);
                    }
                    codablockF.setContent(dataInput);
                    symbol = codablockF;
                    break;
                case 75:
                    // NVE-18
                    Nve18 nve18 = new Nve18();
                    nve18.setContent(dataInput);
                    symbol = nve18;
                    break;
                case 76:
                    // Japanese Post
                    JapanPost japanPost = new JapanPost();
                    japanPost.setContent(dataInput);
                    symbol = japanPost;
                    break;
                case 77:
                    // Korea Post
                    KoreaPost koreaPost = new KoreaPost();
                    koreaPost.setContent(dataInput);
                    symbol = koreaPost;
                    break;
                case 79:
                    // Databar-14 Stacked
                    DataBar14 dataBar14s = new DataBar14();
                    dataBar14s.setStackedMode();
                    dataBar14s.setContent(dataInput);
                    symbol = dataBar14s;
                    break;
                case 80:
                    // Databar-14 Stacked Omnidirectional
                    DataBar14 dataBar14so = new DataBar14();
                    dataBar14so.setOmnidirectionalMode();
                    dataBar14so.setContent(dataInput);
                    symbol = dataBar14so;
                    break;
                case 81:
                    // Databar Expanded Stacked
                    DataBarExpanded dataBarES = new DataBarExpanded();
                    dataBarES.setNoOfColumns(settings.getSymbolColumns());
                    dataBarES.setStacked();
                    dataBarES.setContent(dataInput);
                    symbol = dataBarES;
                    break;
                case 82:
                case 83: // Legacy
                    // Planet
                    Postnet planet = new Postnet();
                    planet.setPlanet();
                    planet.setContent(dataInput);
                    symbol = planet;
                    break;
                case 84:
                case 108:
                case 109: // Legacy
                    // MicroPDF
                    Pdf417 microPdf417 = new Pdf417();
                    microPdf417.setNormalMode();
                    if (settings.isDataGs1Mode()) {
                        microPdf417.setDataType(Symbol.DataType.GS1);
                    }
                    if ((type == 108) || (type == 109)) {
                        microPdf417.setDataType(Symbol.DataType.HIBC);
                    }
                    if (settings.isReaderInit()) {
                        microPdf417.setReaderInit();
                    }
                    microPdf417.setNumberOfColumns(settings.getSymbolColumns());
                    microPdf417.setMicroMode();
                    microPdf417.setContent(dataInput);
                    symbol = microPdf417;
                    break;
                case 85:
                    // USPS Intelligent Mail
                    UspsOneCode uspsIMail = new UspsOneCode();
                    uspsIMail.setContent(dataInput);
                    symbol = uspsIMail;
                    break;
                case 87:
                    // Telepen Numeric
                    Telepen telepenNum = new Telepen();
                    telepenNum.setNumericMode();
                    telepenNum.setContent(dataInput);
                    symbol = telepenNum;
                    break;
                case 89:
                    // ITF-14
                    Code2Of5 itf14 = new Code2Of5();
                    itf14.setITF14Mode();
                    itf14.setContent(dataInput);
                    symbol = itf14;
                    break;
                case 90:
                    // Dutch Post KIX Code
                    KixCode kixCode = new KixCode();
                    kixCode.setContent(dataInput); 
                    symbol = kixCode;
                    break;
                case 92:
                case 112:
                    // Aztec Code
                    AztecCode aztecCode = new AztecCode();
                    if (settings.isDataGs1Mode()) {
                        aztecCode.setDataType(Symbol.DataType.GS1);
                    }
                    if (type == 112) {
                        aztecCode.setDataType(Symbol.DataType.HIBC);
                    }
                    if (settings.isReaderInit()) {
                        aztecCode.setReaderInit();
                    }
                    aztecCode.setPreferredEccLevel(settings.getSymbolECC());
                    aztecCode.setPreferredSize(settings.getSymbolVersion());
                    aztecCode.setContent(dataInput);
                    symbol = aztecCode;
                    break;
                case 93:
                    // Code 32
                    Code32 code32 = new Code32();
                    code32.setContent(dataInput);
                    symbol = code32;
                    break;                    
                case 97:
                    // Micro QR Code
                    MicroQrCode microQrCode = new MicroQrCode();
                    switch(settings.getSymbolECC()) {
                        case 0:
                            microQrCode.setEccMode(MicroQrCode.EccMode.L);
                            break;
                        case 1:
                            microQrCode.setEccMode(MicroQrCode.EccMode.M);
                            break;
                        case 2:
                            microQrCode.setEccMode(MicroQrCode.EccMode.Q);
                            break;
                        case 3:
                            microQrCode.setEccMode(MicroQrCode.EccMode.H);
                            break;
                    }
                    microQrCode.setPreferredVersion(settings.getSymbolVersion());
                    microQrCode.setContent(dataInput);
                    symbol = microQrCode;
                    break;
                case 113:
                case 52: // Legacy
                    // PZN-8
                    Pharmazentralnummer pzn = new Pharmazentralnummer();
                    pzn.setContent(dataInput);
                    symbol = pzn;
                    break;
                case 117:
                    // USPS Intelligent Mail Package
                    UspsPackage uspsPackage = new UspsPackage();
                    uspsPackage.setContent(dataInput);
                    symbol = uspsPackage;
                    break;
                case 128:
                    // Aztec Runes
                    AztecRune aztecRune = new AztecRune();
                    aztecRune.setContent(dataInput);
                    symbol = aztecRune;
                    break;
                case 130:
                    // Composite symbol with EAN linear
                    Composite compositeEan = new Composite();
                    compositeEan.setSymbology(Composite.LinearEncoding.EAN);
                    compositeEan.setLinear(settings.getPrimaryData());
                    compositeEan.setContent(dataInput);
                    symbol = compositeEan;
                    break;
                case 131:
                    // Composite with Code 128 linear
                    Composite compositeC128 = new Composite();
                    compositeC128.setSymbology(Composite.LinearEncoding.CODE_128);
                    compositeC128.setLinear(settings.getPrimaryData());
                    compositeC128.setContent(dataInput);
                    symbol = compositeC128;
                    break;
                case 132:
                    // Composite with Databar-14
                    Composite compositeDb14 = new Composite();
                    compositeDb14.setSymbology(Composite.LinearEncoding.DATABAR_14);
                    compositeDb14.setLinear(settings.getPrimaryData());
                    compositeDb14.setContent(dataInput);
                    symbol = compositeDb14;
                    break;
                case 133:
                    // Composite with Databar Limited
                    Composite compositeDbLtd = new Composite();
                    compositeDbLtd.setSymbology(Composite.LinearEncoding.DATABAR_LIMITED);
                    compositeDbLtd.setLinear(settings.getPrimaryData());
                    compositeDbLtd.setContent(dataInput);
                    symbol = compositeDbLtd;
                    break;
                case 134:
                    // Composite with Databar Extended
                    Composite compositeDbExt = new Composite();
                    compositeDbExt.setSymbology(Composite.LinearEncoding.DATABAR_EXPANDED);
                    compositeDbExt.setLinear(settings.getPrimaryData());
                    compositeDbExt.setContent(dataInput);
                    symbol = compositeDbExt;
                    break;
                case 135:
                    // Composite with UPC-A
                    Composite compositeUpcA = new Composite();
                    compositeUpcA.setSymbology(Composite.LinearEncoding.UPCA);
                    compositeUpcA.setLinear(settings.getPrimaryData());
                    compositeUpcA.setContent(dataInput);
                    symbol = compositeUpcA;
                    break;
                case 136:
                    // Composite with UPC-E
                    Composite compositeUpcE = new Composite();
                    compositeUpcE.setSymbology(Composite.LinearEncoding.UPCE);
                    compositeUpcE.setLinear(settings.getPrimaryData());
                    compositeUpcE.setContent(dataInput);
                    symbol = compositeUpcE;
                    break;
                case 137:
                    // Composite with Databar-14 Stacked
                    Composite compositeDb14Stack = new Composite();
                    compositeDb14Stack.setSymbology(Composite.LinearEncoding.DATABAR_14_STACK);
                    compositeDb14Stack.setLinear(settings.getPrimaryData());
                    compositeDb14Stack.setContent(dataInput);
                    symbol = compositeDb14Stack;
                    break;
                case 138:
                    // Composite with Databar-14 Stacked Omnidirectional
                    Composite compositeDb14SO = new Composite();
                    compositeDb14SO.setSymbology(Composite.LinearEncoding.DATABAR_14_STACK_OMNI);
                    compositeDb14SO.setLinear(settings.getPrimaryData());
                    compositeDb14SO.setContent(dataInput);
                    symbol = compositeDb14SO;
                    break;
                case 139:
                    // Composite with Databar-14 Expanded Stacked
                    Composite compositeDb14ES = new Composite();
                    compositeDb14ES.setSymbology(Composite.LinearEncoding.DATABAR_EXPANDED_STACK);
                    compositeDb14ES.setLinear(settings.getPrimaryData());
                    compositeDb14ES.setContent(dataInput);
                    symbol = compositeDb14ES;
                    break;                    
                case 140:
                    // Channel Code
                    ChannelCode channelCode = new ChannelCode();
                    channelCode.setNumberOfChannels(settings.getSymbolColumns());
                    channelCode.setContent(dataInput);
                    symbol = channelCode;
                    break;
                case 141:
                    // Code One
                    CodeOne codeOne = new CodeOne();
                    if (settings.isDataGs1Mode()) {
                        codeOne.setDataType(Symbol.DataType.GS1);
                    }
                    if (settings.isReaderInit()) {
                        codeOne.setReaderInit();
                    }
                    switch(settings.getSymbolVersion()) {
                        case 0:
                            codeOne.setPreferredVersion(CodeOne.Version.NONE);
                            break;
                        case 1:
                            codeOne.setPreferredVersion(CodeOne.Version.A);
                            break;
                        case 2:
                            codeOne.setPreferredVersion(CodeOne.Version.B);
                            break;
                        case 3:
                            codeOne.setPreferredVersion(CodeOne.Version.C);
                            break;
                        case 4:
                            codeOne.setPreferredVersion(CodeOne.Version.D);
                            break;
                        case 5:
                            codeOne.setPreferredVersion(CodeOne.Version.E);
                            break;
                        case 6:
                            codeOne.setPreferredVersion(CodeOne.Version.F);
                            break;
                        case 7:
                            codeOne.setPreferredVersion(CodeOne.Version.G);
                            break;
                        case 8:
                            codeOne.setPreferredVersion(CodeOne.Version.H);
                            break;
                        case 9:
                            codeOne.setPreferredVersion(CodeOne.Version.S);
                            break;
                        case 10:
                            codeOne.setPreferredVersion(CodeOne.Version.T);
                            break;
                    }
                    codeOne.setContent(dataInput);
                    symbol = codeOne;
                    break;
                case 142:
                    // Grid Matrix
                    GridMatrix gridMatrix = new GridMatrix();
                    if (settings.isDataGs1Mode()) {
                        gridMatrix.setDataType(Symbol.DataType.GS1);
                    }
                    if (settings.isReaderInit()) {
                        gridMatrix.setReaderInit();
                    }
                    gridMatrix.setPreferredEccLevel(settings.getSymbolECC());
                    gridMatrix.setPreferredVersion(settings.getSymbolVersion());
                    gridMatrix.setContent(dataInput);
                    symbol = gridMatrix;
                    break;
                default:
                    // Invalid
                    System.out.println("Invaid barcode type");
                    return;
            }
        } catch (OkapiException e) {
            System.out.printf("Encoding error: %s\n", e.getMessage());
            return;
        }
        
        File file = new File(settings.getOutputFile());
        
        try {
            int i = file.getName().lastIndexOf('.');
            if (i > 0) {
                extension = file.getName().substring(i + 1);
            }
            
            switch (extension) {
//                case "png":
//                case "gif":
//                case "jpg":
//                case "bmp":
//                    BufferedImage image = new BufferedImage(symbol.getWidth(), symbol.getHeight(), BufferedImage.TYPE_INT_ARGB);
//                    Graphics2D g2d = image.createGraphics();
//                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//
//                    Java2DRenderer renderer = new Java2DRenderer(g2d, magnification, OkapiUI.paperColour, OkapiUI.inkColour);
//                    renderer.render(symbol);
//                    break;
                case "svg":
                    SvgRenderer svg = new SvgRenderer(new FileOutputStream(file), magnification, WHITE, BLACK, borderSize);
                    svg.render(symbol);
                    break;
                case "eps":
                    PostScriptRenderer eps = new PostScriptRenderer(new FileOutputStream(file), magnification, WHITE, BLACK, borderSize);
                    eps.render(symbol);
                    break;
                default:
                    System.out.println("Unsupported output format");
                    break;
            }

        } catch (FileNotFoundException e){
            System.out.printf("File Not Found\n");
            return;
        } catch (IOException e) {
            System.out.printf("Write Error\n");
            return;
        }        
    }
    
    private int eanCalculateVersion(String dataInput) {
        /* Determine if EAN-8 or EAN-13 is being used */

        int length = 0;
        int i;
        boolean latch;

        latch = true;
        for (i = 0; i < dataInput.length(); i++) {
            if ((dataInput.charAt(i) >= '0') && (dataInput.charAt(i) <= '9')) {
                if (latch) {
                    length++;
                }
            } else {
                latch = false;
            }
        }

        if (length <= 7) {
            // EAN-8
            return 8;
        } else {
            // EAN-13
            return 13;
        }
    }
}
