/*
 * Copyright 2014 Robin Stuart
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
package uk.org.okapibarcode.backend;

import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;

/**
 * Main calling class for all barcode symbologies
 *
 * @author Robin Stuart <rstuart114@gmail.com>
 * @version 0.5
 */
public class Barcode {
    private String symbology;
    private String content;
    private String compositeContent = "";
    public String error_msg;
    public int symbol_height;
    public int symbol_width;
    private boolean gs1 = false;
    private boolean hibc = false;
    private boolean readerInit = false;
    private Composite composite;
    private boolean isComposite;
    public String encodeInfo;
    private String primaryData = "";
    private int compositeUserMode;

    private int option1;
    private int option2;
    
    public ArrayList < Rectangle > rect = new ArrayList < > ();
    public ArrayList < TextBox > txt = new ArrayList < > ();
    public ArrayList < Hexagon > hex = new ArrayList < > ();
    public static ArrayList< Ellipse2D.Double > target = new ArrayList < > ();
    
    public void setNormalMode() {
        gs1 = false;
        hibc = false;
        readerInit = false;
        isComposite = false;
    }
    
    public void setGs1Mode() {
        gs1 = true;
        hibc = false;
        readerInit = false;
    }
    
    public void setHibcMode() {
        gs1 = false;
        hibc = true;
        readerInit = false;
        isComposite = false;
    }
    
    public void setInitMode() {
        gs1 = false;
        hibc = false;
        readerInit = true;
        isComposite = false;
    }
    
    public void setPrimary(String input) {
        primaryData = input;
    }
    
    public void setCompositeContent(String inputData) {
        compositeContent = inputData;
        isComposite = true;
    }
    
    public void setCompositePreferredMode(int input) {
        compositeUserMode = input;
    }

    public void setOption1(int input) {
        option1 = input;
    }

    public void setOption2(int input) {
        option2 = input;
    }
    
    public boolean encode(String inputSymbology, String inputData) {
        symbology = inputSymbology;
        content = inputData;
        return encode();
    }

    public boolean encode(String inputData) {
        content = inputData;
        return encode();
    }
    
    public boolean encode() {
        rect.clear();
        txt.clear();
        hex.clear();
        target.clear();
        
        // Perform some sanity checks on input
        if (content.isEmpty()) {
            error_msg = "No input data found";
            return false;
        }
        
        if (gs1) {
            switch(symbology) {
		case "BARCODE_CODE128":
		case "BARCODE_RSS_EXP":
		case "BARCODE_RSS_EXPSTACK":
		case "BARCODE_CODE16K":
		case "BARCODE_AZTEC":
		case "BARCODE_DATAMATRIX":
		case "BARCODE_CODEONE":
		case "BARCODE_CODE49":
		case "BARCODE_QRCODE":
                case "BARCODE_CODABLOCKF":
                    return encodeData();
                    // break;
                default:
                    error_msg = "Selected symbology doesn't support GS1";
                    return false;
            }
        }
        
        switch (symbology) {
            case "BARCODE_HIBC_128":
            case "BARCODE_HIBC_39":
            case "BARCODE_HIBC_DM":
            case "BARCODE_HIBC_QR":
            case "BARCODE_HIBC_PDF":
            case "BARCODE_HIBC_MICPDF":
            case "BARCODE_HIBC_AZTEC":
            case "BARCODE_HIBC_BLOCKF":
                setHibcMode();
                break;
            default:
                // do nothing
                break;
        }
        
        return encodeData();
    }

    public boolean encodeData() {
        boolean output;

        Upc upc = new Upc();
        Ean ean = new Ean();
        Code128 code128 = new Code128();
        Codabar codabar = new Codabar();
        Code2Of5 code2of5 = new Code2Of5();
        MsiPlessey msiPlessey = new MsiPlessey();
        Code3Of9 code3of9 = new Code3Of9();
        Logmars logmars = new Logmars();
        Code11 code11 = new Code11();
        Code93 code93 = new Code93();
        Pharmazentralnummer pzn = new Pharmazentralnummer();
        Code3Of9Extended code3of9ext = new Code3Of9Extended();
        Telepen telepen = new Telepen();
        Code49 code49 = new Code49();
        KoreaPost koreaPost = new KoreaPost();
        Code16k code16k = new Code16k();
        Postnet postnet = new Postnet();
        RoyalMail4State royalMail = new RoyalMail4State();
        KixCode kixCode = new KixCode();
        JapanPost japanPost = new JapanPost();
        AustraliaPost australiaPost = new AustraliaPost();
        ChannelCode channelCode = new ChannelCode();
        PharmaCode pharmaCode = new PharmaCode();
        PharmaCode2Track pharmaCode2t = new PharmaCode2Track();
        Code32 code32 = new Code32();
        Pdf417 pdf417 = new Pdf417();
        AztecCode aztecCode = new AztecCode();
        AztecRune aztecRune = new AztecRune();
        DataMatrix dataMatrix = new DataMatrix();
        UspsOneCode uspsOneCode = new UspsOneCode();
        QrCode qrCode = new QrCode();
        MicroQrCode microQrCode = new MicroQrCode();
        CodeOne codeOne = new CodeOne();
        GridMatrix gridMatrix = new GridMatrix();
        DataBar14 dataBar14 = new DataBar14();
        DataBarLimited dataBarLimited = new DataBarLimited();
        DataBarExpanded dataBarExpanded = new DataBarExpanded();
        MaxiCode maxiCode = new MaxiCode();
        CodablockF codablockF = new CodablockF();
        composite = new Composite();
        
        if (!(compositeContent.isEmpty())) {
            // A composite component needs to be added
            isComposite = true;
            composite.gs1 = true;
            composite.setSymbology(symbology);
            composite.setLinear(content);
            composite.setPreferred(compositeUserMode);
            if (!(composite.setContent(compositeContent))) {
                this.error_msg = composite.error_msg;
                return false;
            }
        }
        
        encodeInfo = "Symbology: " + symbology + '\n';
        
        output = false;
        switch (symbology) {
        case "BARCODE_UPCA":
            upc.setUpcaMode();
            if (isComposite) {
                upc.setLinkageFlag();
            } else {
                upc.unsetLinkageFlag();
            }
            if (upc.setContent(this.content)) {
                this.rect = upc.rect;
                this.symbol_height = upc.symbol_height;
                this.symbol_width = upc.symbol_width;
                this.txt = upc.txt;
                this.encodeInfo += upc.encodeInfo;
                output = true;
            } else {
                this.error_msg = upc.error_msg;
            };
            break;
        case "BARCODE_UPCE":
            upc.setUpceMode();
            if (isComposite) {
                upc.setLinkageFlag();
            } else {
                upc.unsetLinkageFlag();
            }
            if (upc.setContent(this.content)) {
                this.rect = upc.rect;
                this.symbol_height = upc.symbol_height;
                this.symbol_width = upc.symbol_width;
                this.txt = upc.txt;
                this.encodeInfo += upc.encodeInfo;
                output = true;
            } else {
                this.error_msg = upc.error_msg;
            };
            break;
        case "BARCODE_EANX":
            if (eanCalculateVersion() == 8) {
                ean.setEan8Mode();
            } else {
                ean.setEan13Mode();
            }
            
            if (isComposite) {
                ean.setLinkageFlag();
            } else {
                ean.unsetLinkageFlag();
            }
            if (ean.setContent(this.content)) {
                this.rect = ean.rect;
                this.symbol_height = ean.symbol_height;
                this.symbol_width = ean.symbol_width;
                this.txt = ean.txt;
                this.encodeInfo += ean.encodeInfo;
                output = true;
            } else {
                this.error_msg = ean.error_msg;
            };
            break;
        case "BARCODE_ITF14":
            code2of5.setITF14Mode();
            if (code2of5.setContent(this.content)) {
                this.rect = code2of5.rect;
                this.symbol_height = code2of5.symbol_height;
                this.symbol_width = code2of5.symbol_width;
                this.txt = code2of5.txt;
                this.encodeInfo += code2of5.encodeInfo;
                output = true;
            } else {
                this.error_msg = code2of5.error_msg;
            };
            break;
        case "BARCODE_CODE128":
        case "BARCODE_HIBC_128":
            if (isComposite) {
                switch (composite.getCcMode()) {
                    case 1:
                        code128.setCca();
                        break;
                    case 2:
                        code128.setCcb();
                        break;
                    case 3:
                        code128.setCcc();
                        break;
                }
            } else {
                code128.unsetCc();
            }
            code128.gs1 = this.gs1;
            code128.hibc = this.hibc;
            code128.readerInit = this.readerInit;
            if (code128.setContent(this.content)) {
                this.rect = code128.rect;
                this.symbol_height = code128.symbol_height;
                this.symbol_width = code128.symbol_width;
                this.txt = code128.txt;
                this.encodeInfo += code128.encodeInfo;
                output = true;
            } else {
                this.error_msg = code128.error_msg;
            };
            break;
        case "BARCODE_CODABAR":
            if (codabar.setContent(this.content)) {
                this.rect = codabar.rect;
                this.symbol_height = codabar.symbol_height;
                this.symbol_width = codabar.symbol_width;
                this.txt = codabar.txt;
                this.encodeInfo += codabar.encodeInfo;
                output = true;
            } else {
                this.error_msg = codabar.error_msg;
            };
            break;
        case "BARCODE_C25MATRIX":
            code2of5.setMatrixMode();
            if (code2of5.setContent(this.content)) {
                this.rect = code2of5.rect;
                this.symbol_height = code2of5.symbol_height;
                this.symbol_width = code2of5.symbol_width;
                this.txt = code2of5.txt;
                this.encodeInfo += code2of5.encodeInfo;
                output = true;
            } else {
                this.error_msg = code2of5.error_msg;
            };
            break;
        case "BARCODE_C25IND":
            code2of5.setIndustrialMode();
            if (code2of5.setContent(this.content)) {
                this.rect = code2of5.rect;
                this.symbol_height = code2of5.symbol_height;
                this.symbol_width = code2of5.symbol_width;
                this.txt = code2of5.txt;
                this.encodeInfo += code2of5.encodeInfo;
                output = true;
            } else {
                this.error_msg = code2of5.error_msg;
            };
            break;
        case "BARCODE_C25INTER":
            code2of5.setInterleavedMode();
            if (code2of5.setContent(this.content)) {
                this.rect = code2of5.rect;
                this.symbol_height = code2of5.symbol_height;
                this.symbol_width = code2of5.symbol_width;
                this.txt = code2of5.txt;
                this.encodeInfo += code2of5.encodeInfo;
                output = true;
            } else {
                this.error_msg = code2of5.error_msg;
            };
            break;
        case "BARCODE_MSI_PLESSEY":
            msiPlessey.option2 = this.option2;
            if (msiPlessey.setContent(this.content)) {
                this.rect = msiPlessey.rect;
                this.symbol_height = msiPlessey.symbol_height;
                this.symbol_width = msiPlessey.symbol_width;
                this.txt = msiPlessey.txt;
                this.encodeInfo += msiPlessey.encodeInfo;
                output = true;
            } else {
                this.error_msg = msiPlessey.error_msg;
            };
            break;
        case "BARCODE_CODE39":
        case "BARCODE_HIBC_39":
            code3of9.hibc = this.hibc;
            code3of9.option2 = this.option2;
            if (code3of9.setContent(this.content)) {
                this.rect = code3of9.rect;
                this.symbol_height = code3of9.symbol_height;
                this.symbol_width = code3of9.symbol_width;
                this.txt = code3of9.txt;
                this.encodeInfo += code3of9.encodeInfo;
                output = true;
            } else {
                this.error_msg = code3of9.error_msg;
            };
            break;
        case "BARCODE_LOGMARS":
            if (logmars.setContent(this.content)) {
                this.rect = logmars.rect;
                this.symbol_height = logmars.symbol_height;
                this.symbol_width = logmars.symbol_width;
                this.txt = logmars.txt;
                this.encodeInfo += logmars.encodeInfo;
                output = true;
            } else {
                this.error_msg = logmars.error_msg;
            };
            break;
        case "BARCODE_CODE11":
            if (code11.setContent(this.content)) {
                this.rect = code11.rect;
                this.symbol_height = code11.symbol_height;
                this.symbol_width = code11.symbol_width;
                this.txt = code11.txt;
                this.encodeInfo += code11.encodeInfo;
                output = true;
            } else {
                this.error_msg = code11.error_msg;
            };
            break;
        case "BARCODE_CODE93":
            if (code93.setContent(this.content)) {
                this.rect = code93.rect;
                this.symbol_height = code93.symbol_height;
                this.symbol_width = code93.symbol_width;
                this.txt = code93.txt;
                this.encodeInfo += code93.encodeInfo;
                output = true;
            } else {
                this.error_msg = code93.error_msg;
            };
            break;
        case "BARCODE_PZN":
            if (pzn.setContent(this.content)) {
                this.rect = pzn.rect;
                this.symbol_height = pzn.symbol_height;
                this.symbol_width = pzn.symbol_width;
                this.txt = pzn.txt;
                this.encodeInfo += pzn.encodeInfo;
                output = true;
            } else {
                this.error_msg = pzn.error_msg;
            };
            break;
        case "BARCODE_EXCODE39":
            code3of9ext.option2 = this.option2;
            if (code3of9ext.setContent(this.content)) {
                this.rect = code3of9ext.rect;
                this.symbol_height = code3of9ext.symbol_height;
                this.symbol_width = code3of9ext.symbol_width;
                this.txt = code3of9ext.txt;
                this.encodeInfo += code3of9ext.encodeInfo;
                output = true;
            } else {
                this.error_msg = code3of9ext.error_msg;
            };
            break;
        case "BARCODE_TELEPEN":
            telepen.setNormalMode();
            if (telepen.setContent(this.content)) {
                this.rect = telepen.rect;
                this.symbol_height = telepen.symbol_height;
                this.symbol_width = telepen.symbol_width;
                this.txt = telepen.txt;
                this.encodeInfo += telepen.encodeInfo;
                output = true;
            } else {
                this.error_msg = telepen.error_msg;
            };
            break;
        case "BARCODE_TELEPEN_NUM":
            telepen.setNumericMode();
            if (telepen.setContent(this.content)) {
                this.rect = telepen.rect;
                this.symbol_height = telepen.symbol_height;
                this.symbol_width = telepen.symbol_width;
                this.txt = telepen.txt;
                this.encodeInfo += telepen.encodeInfo;
                output = true;
            } else {
                this.error_msg = telepen.error_msg;
            };
            break;
        case "BARCODE_CODE49":
            if (code49.setContent(this.content)) {
                this.rect = code49.rect;
                this.symbol_height = code49.symbol_height;
                this.symbol_width = code49.symbol_width;
                this.txt = code49.txt;
                this.encodeInfo += code49.encodeInfo;
                output = true;
            } else {
                this.error_msg = code49.error_msg;
            };
            break;
        case "BARCODE_KOREAPOST":
            if (koreaPost.setContent(this.content)) {
                this.rect = koreaPost.rect;
                this.symbol_height = koreaPost.symbol_height;
                this.symbol_width = koreaPost.symbol_width;
                this.txt = koreaPost.txt;
                this.encodeInfo += koreaPost.encodeInfo;
                output = true;
            } else {
                this.error_msg = koreaPost.error_msg;
            };
            break;
        case "BARCODE_CODE16K":
            code16k.gs1 = this.gs1;
            code16k.hibc = this.hibc;
            code16k.readerInit = this.readerInit;
            if (code16k.setContent(this.content)) {
                this.rect = code16k.rect;
                this.symbol_height = code16k.symbol_height;
                this.symbol_width = code16k.symbol_width;
                this.txt = code16k.txt;
                this.encodeInfo += code16k.encodeInfo;
                output = true;
            } else {
                this.error_msg = code16k.error_msg;
            };
            break;
        case "BARCODE_C25IATA":
            code2of5.setIATAMode();
            if (code2of5.setContent(this.content)) {
                this.rect = code2of5.rect;
                this.symbol_height = code2of5.symbol_height;
                this.symbol_width = code2of5.symbol_width;
                this.txt = code2of5.txt;
                this.encodeInfo = code2of5.encodeInfo;
                output = true;
            } else {
                this.error_msg = code2of5.error_msg;
            };
            break;
        case "BARCODE_C25LOGIC":
            code2of5.setDataLogicMode();
            if (code2of5.setContent(this.content)) {
                this.rect = code2of5.rect;
                this.symbol_height = code2of5.symbol_height;
                this.symbol_width = code2of5.symbol_width;
                this.txt = code2of5.txt;
                this.encodeInfo += code2of5.encodeInfo;
                output = true;
            } else {
                this.error_msg = code2of5.error_msg;
            };
            break;
        case "BARCODE_DPLEIT":
            code2of5.setDPLeitMode();
            if (code2of5.setContent(this.content)) {
                this.rect = code2of5.rect;
                this.symbol_height = code2of5.symbol_height;
                this.symbol_width = code2of5.symbol_width;
                this.txt = code2of5.txt;
                this.encodeInfo += code2of5.encodeInfo;
                output = true;
            } else {
                this.error_msg = code2of5.error_msg;
            };
            break;
        case "BARCODE_DPIDENT":
            code2of5.setDPIdentMode();
            if (code2of5.setContent(this.content)) {
                this.rect = code2of5.rect;
                this.symbol_height = code2of5.symbol_height;
                this.symbol_width = code2of5.symbol_width;
                this.txt = code2of5.txt;
                this.encodeInfo += code2of5.encodeInfo;
                output = true;
            } else {
                this.error_msg = code2of5.error_msg;
            };
            break;
        case "BARCODE_POSTNET":
            postnet.setPostnet();
            if (postnet.setContent(this.content)) {
                this.rect = postnet.rect;
                this.symbol_height = postnet.symbol_height;
                this.symbol_width = postnet.symbol_width;
                this.txt = postnet.txt;
                this.encodeInfo += postnet.encodeInfo;
                output = true;
            } else {
                this.error_msg = postnet.error_msg;
            };
            break;
        case "BARCODE_PLANET":
            postnet.setPlanet();
            if (postnet.setContent(this.content)) {
                this.rect = postnet.rect;
                this.symbol_height = postnet.symbol_height;
                this.symbol_width = postnet.symbol_width;
                this.txt = postnet.txt;
                output = true;
            } else {
                this.error_msg = postnet.error_msg;
            };
            break;
        case "BARCODE_RM4SCC":
            if (royalMail.setContent(this.content)) {
                this.rect = royalMail.rect;
                this.symbol_height = royalMail.symbol_height;
                this.symbol_width = royalMail.symbol_width;
                this.txt = royalMail.txt;
                this.encodeInfo += royalMail.encodeInfo;
                output = true;
            } else {
                this.error_msg = royalMail.error_msg;
            }
            break;
        case "BARCODE_KIX":
            if (kixCode.setContent(this.content)) {
                this.rect = kixCode.rect;
                this.symbol_height = kixCode.symbol_height;
                this.symbol_width = kixCode.symbol_width;
                this.txt = kixCode.txt;
                this.encodeInfo += kixCode.encodeInfo;
                output = true;
            } else {
                this.error_msg = kixCode.error_msg;
            }
            break;
        case "BARCODE_JAPANPOST":
            if (japanPost.setContent(this.content)) {
                this.rect = japanPost.rect;
                this.symbol_height = japanPost.symbol_height;
                this.symbol_width = japanPost.symbol_width;
                this.txt = japanPost.txt;
                this.encodeInfo += japanPost.encodeInfo;
                output = true;
            } else {
                this.error_msg = japanPost.error_msg;
            }
            break;
        case "BARCODE_AUSPOST":
            australiaPost.setPostMode();
            if (australiaPost.setContent(this.content)) {
                this.rect = australiaPost.rect;
                this.symbol_height = australiaPost.symbol_height;
                this.symbol_width = australiaPost.symbol_width;
                this.txt = australiaPost.txt;
                this.encodeInfo += australiaPost.encodeInfo;
                output = true;
            } else {
                this.error_msg = australiaPost.error_msg;
            }
            break;
        case "BARCODE_AUSREPLY":
            australiaPost.setReplyMode();
            if (australiaPost.setContent(this.content)) {
                this.rect = australiaPost.rect;
                this.symbol_height = australiaPost.symbol_height;
                this.symbol_width = australiaPost.symbol_width;
                this.txt = australiaPost.txt;
                this.encodeInfo += australiaPost.encodeInfo;
                output = true;
            } else {
                this.error_msg = australiaPost.error_msg;
            }
            break;
        case "BARCODE_AUSROUTE":
            australiaPost.setRouteMode();
            if (australiaPost.setContent(this.content)) {
                this.rect = australiaPost.rect;
                this.symbol_height = australiaPost.symbol_height;
                this.symbol_width = australiaPost.symbol_width;
                this.txt = australiaPost.txt;
                this.encodeInfo += australiaPost.encodeInfo;
                output = true;
            } else {
                this.error_msg = australiaPost.error_msg;
            }
            break;
        case "BARCODE_AUSREDIRECT":
            australiaPost.setRedirectMode();
            if (australiaPost.setContent(this.content)) {
                this.rect = australiaPost.rect;
                this.symbol_height = australiaPost.symbol_height;
                this.symbol_width = australiaPost.symbol_width;
                this.txt = australiaPost.txt;
                this.encodeInfo += australiaPost.encodeInfo;
                output = true;
            } else {
                this.error_msg = australiaPost.error_msg;
            }
            break;
        case "BARCODE_CHANNEL":
            channelCode.option2 = this.option2;
            if (channelCode.setContent(this.content)) {
                this.rect = channelCode.rect;
                this.symbol_height = channelCode.symbol_height;
                this.symbol_width = channelCode.symbol_width;
                this.txt = channelCode.txt;
                this.encodeInfo += channelCode.encodeInfo;
                output = true;
            } else {
                this.error_msg = channelCode.error_msg;
            }
            break;
        case "BARCODE_PHARMA":
            if (pharmaCode.setContent(this.content)) {
                this.rect = pharmaCode.rect;
                this.symbol_height = pharmaCode.symbol_height;
                this.symbol_width = pharmaCode.symbol_width;
                this.txt = pharmaCode.txt;
                this.encodeInfo += pharmaCode.encodeInfo;
                output = true;
            } else {
                this.error_msg = pharmaCode.error_msg;
            }
            break;
        case "BARCODE_PHARMA_TWO":
            if (pharmaCode2t.setContent(this.content)) {
                this.rect = pharmaCode2t.rect;
                this.symbol_height = pharmaCode2t.symbol_height;
                this.symbol_width = pharmaCode2t.symbol_width;
                this.txt = pharmaCode2t.txt;
                this.encodeInfo += pharmaCode2t.encodeInfo;
                output = true;
            } else {
                this.error_msg = pharmaCode2t.error_msg;
            }
            break;
        case "BARCODE_CODE32":
            if (code32.setContent(this.content)) {
                this.rect = code32.rect;
                this.symbol_height = code32.symbol_height;
                this.symbol_width = code32.symbol_width;
                this.txt = code32.txt;
                this.encodeInfo += code32.encodeInfo;
                output = true;
            } else {
                this.error_msg = code32.error_msg;
            }
            break;
        case "BARCODE_PDF417":
        case "BARCODE_HIBC_PDF":
            pdf417.gs1 = this.gs1;
            pdf417.hibc = this.hibc;
            pdf417.option2 = this.option2;
            pdf417.readerInit = this.readerInit;
            pdf417.setNormalMode();
            if (pdf417.setContent(this.content)) {
                this.rect = pdf417.rect;
                this.symbol_height = pdf417.symbol_height;
                this.symbol_width = pdf417.symbol_width;
                this.txt = pdf417.txt;
                this.encodeInfo += pdf417.encodeInfo;
                output = true;
            } else {
                this.error_msg = pdf417.error_msg;
            }
            break;
        case "BARCODE_PDF417TRUNC":
            pdf417.gs1 = this.gs1;
            pdf417.hibc = this.hibc;
            pdf417.option2 = this.option2;
            pdf417.readerInit = this.readerInit;
            pdf417.setTruncMode();
            if (pdf417.setContent(this.content)) {
                this.rect = pdf417.rect;
                this.symbol_height = pdf417.symbol_height;
                this.symbol_width = pdf417.symbol_width;
                this.txt = pdf417.txt;
                this.encodeInfo += pdf417.encodeInfo;
                output = true;
            } else {
                this.error_msg = pdf417.error_msg;
            }
            break;
        case "BARCODE_MICROPDF417":
        case "BARCODE_HIBC_MICPDF":
            pdf417.gs1 = this.gs1;
            pdf417.hibc = this.hibc;
            pdf417.readerInit = this.readerInit;
            pdf417.option2 = this.option2;
            pdf417.setMicroMode();
            if (pdf417.setContent(this.content)) {
                this.rect = pdf417.rect;
                this.symbol_height = pdf417.symbol_height;
                this.symbol_width = pdf417.symbol_width;
                this.txt = pdf417.txt;
                this.encodeInfo += pdf417.encodeInfo;
                output = true;
            } else {
                this.error_msg = pdf417.error_msg;
            }
            break;
        case "BARCODE_AZTEC":
        case "BARCODE_HIBC_AZTEC":
            aztecCode.gs1 = this.gs1;
            aztecCode.hibc = this.hibc;
            aztecCode.readerInit = this.readerInit;
            aztecCode.option1 = this.option1;
            aztecCode.option2 = this.option2;
            if (aztecCode.setContent(this.content)) {
                this.rect = aztecCode.rect;
                this.symbol_height = aztecCode.symbol_height;
                this.symbol_width = aztecCode.symbol_width;
                this.txt = aztecCode.txt;
                this.encodeInfo += aztecCode.encodeInfo;
                output = true;
            } else {
                this.error_msg = aztecCode.error_msg;
            }
            break;
        case "BARCODE_AZRUNE":
            if (aztecRune.setContent(this.content)) {
                this.rect = aztecRune.rect;
                this.symbol_height = aztecRune.symbol_height;
                this.symbol_width = aztecRune.symbol_width;
                this.txt = aztecRune.txt;
                this.encodeInfo += aztecRune.encodeInfo;
                output = true;
            } else {
                this.error_msg = aztecRune.error_msg;
            }
            break;
        case "BARCODE_DATAMATRIX":
        case "BARCODE_HIBC_DM":
            dataMatrix.gs1 = this.gs1;
            dataMatrix.hibc = this.hibc;
            dataMatrix.readerInit = this.readerInit;
            dataMatrix.option2 = this.option2;
            if (option1 == 1) {
                dataMatrix.forceSquare(true);
            } else {
                dataMatrix.forceSquare(false);
            }
            if (dataMatrix.setContent(this.content)) {
                this.rect = dataMatrix.rect;
                this.symbol_height = dataMatrix.symbol_height;
                this.symbol_width = dataMatrix.symbol_width;
                this.txt = dataMatrix.txt;
                this.encodeInfo += dataMatrix.encodeInfo;
                output = true;
            } else {
                this.error_msg = dataMatrix.error_msg;
            }
            break;
        case "BARCODE_ONECODE":
            if (uspsOneCode.setContent(this.content)) {
                this.rect = uspsOneCode.rect;
                this.symbol_height = uspsOneCode.symbol_height;
                this.symbol_width = uspsOneCode.symbol_width;
                this.txt = uspsOneCode.txt;
                this.encodeInfo += uspsOneCode.encodeInfo;
                output = true;
            } else {
                this.error_msg = uspsOneCode.error_msg;
            }
            break;
        case "BARCODE_QRCODE":
        case "BARCODE_HIBC_QR":
            qrCode.gs1 = this.gs1;
            qrCode.hibc = this.hibc;
            qrCode.option1 = this.option1;
            qrCode.option2 = this.option2;
            qrCode.readerInit = this.readerInit;
            if (qrCode.setContent(this.content)) {
                this.rect = qrCode.rect;
                this.symbol_height = qrCode.symbol_height;
                this.symbol_width = qrCode.symbol_width;
                this.txt = qrCode.txt;
                this.encodeInfo += qrCode.encodeInfo;
                output = true;
            } else {
                this.error_msg = qrCode.error_msg;
            }
            break;
        case "BARCODE_MICROQR":
            microQrCode.option1 = this.option1;
            microQrCode.option2 = this.option2;
            if (microQrCode.setContent(this.content)) {
                this.rect = microQrCode.rect;
                this.symbol_height = microQrCode.symbol_height;
                this.symbol_width = microQrCode.symbol_width;
                this.txt = microQrCode.txt;
                this.encodeInfo += microQrCode.encodeInfo;
                output = true;
            } else {
                this.error_msg = microQrCode.error_msg;
            }
            break;
        case "BARCODE_CODEONE":
            codeOne.gs1 = this.gs1;
            codeOne.hibc = this.hibc;
            codeOne.readerInit = this.readerInit;
            codeOne.option2 = this.option2;
            if (codeOne.setContent(this.content)) {
                this.rect = codeOne.rect;
                this.symbol_height = codeOne.symbol_height;
                this.symbol_width = codeOne.symbol_width;
                this.txt = codeOne.txt;
                this.encodeInfo += codeOne.encodeInfo;
                output = true;
            } else {
                this.error_msg = codeOne.error_msg;
            }
            break;
        case "BARCODE_GRIDMATRIX":
            gridMatrix.gs1 = this.gs1;
            gridMatrix.hibc = this.hibc;
            gridMatrix.readerInit = this.readerInit;
            gridMatrix.option1 = this.option1;
            gridMatrix.option2 = this.option2;
            if (gridMatrix.setContent(this.content)) {
                this.rect = gridMatrix.rect;
                this.symbol_height = gridMatrix.symbol_height;
                this.symbol_width = gridMatrix.symbol_width;
                this.txt = gridMatrix.txt;
                this.encodeInfo += gridMatrix.encodeInfo;
                output = true;
            } else {
                this.error_msg = gridMatrix.error_msg;
            }
            break;
        case "BARCODE_RSS14":
            if (isComposite) {
                dataBar14.setLinkageFlag();
            } else {
                dataBar14.unsetLinkageFlag();
            }
            dataBar14.setLinearMode();
            if (dataBar14.setContent(this.content)) {
                this.rect = dataBar14.rect;
                this.symbol_height = dataBar14.symbol_height;
                this.symbol_width = dataBar14.symbol_width;
                this.txt = dataBar14.txt;
                this.encodeInfo += dataBar14.encodeInfo;
                output = true;
            } else {
                this.error_msg = dataBar14.error_msg;
            }
            break;
        case "BARCODE_RSS14STACK_OMNI":
            if (isComposite) {
                dataBar14.setLinkageFlag();
            } else {
                dataBar14.unsetLinkageFlag();
            }            
            dataBar14.setOmnidirectionalMode();
            if (dataBar14.setContent(this.content)) {
                this.rect = dataBar14.rect;
                this.symbol_height = dataBar14.symbol_height;
                this.symbol_width = dataBar14.symbol_width;
                this.txt = dataBar14.txt;
                this.encodeInfo += dataBar14.encodeInfo;
                output = true;
            } else {
                this.error_msg = dataBar14.error_msg;
            }
            break;
        case "BARCODE_RSS14STACK":
            if (isComposite) {
                dataBar14.setLinkageFlag();
            } else {
                dataBar14.unsetLinkageFlag();
            }            
            dataBar14.setStackedMode();
            if (dataBar14.setContent(this.content)) {
                this.rect = dataBar14.rect;
                this.symbol_height = dataBar14.symbol_height;
                this.symbol_width = dataBar14.symbol_width;
                this.txt = dataBar14.txt;
                this.encodeInfo += dataBar14.encodeInfo;
                output = true;
            } else {
                this.error_msg = dataBar14.error_msg;
            }
            break;
        case "BARCODE_RSS_LTD":
            if (isComposite) {
                dataBarLimited.setLinkageFlag();
            } else {
                dataBarLimited.unsetLinkageFlag();
            }            
            if (dataBarLimited.setContent(this.content)) {
                this.rect = dataBarLimited.rect;
                this.symbol_height = dataBarLimited.symbol_height;
                this.symbol_width = dataBarLimited.symbol_width;
                this.txt = dataBarLimited.txt;
                this.encodeInfo += dataBarLimited.encodeInfo;
                output = true;
            } else {
                this.error_msg = dataBarLimited.error_msg;
            }
            break;
        case "BARCODE_RSS_EXP":
            if (isComposite) {
                dataBarExpanded.setLinkageFlag();
            } else {
                dataBarExpanded.unsetLinkageFlag();
            }            
            dataBarExpanded.gs1 = true;
            dataBarExpanded.setNotStacked();
            if (dataBarExpanded.setContent(this.content)) {
                this.rect = dataBarExpanded.rect;
                this.symbol_height = dataBarExpanded.symbol_height;
                this.symbol_width = dataBarExpanded.symbol_width;
                this.txt = dataBarExpanded.txt;
                this.encodeInfo += dataBarExpanded.encodeInfo;
                output = true;
            } else {
                this.error_msg = dataBarExpanded.error_msg;
            }
            break;
        case "BARCODE_RSS_EXPSTACK":
            if (isComposite) {
                dataBarExpanded.setLinkageFlag();
            } else {
                dataBarExpanded.unsetLinkageFlag();
            }            
            dataBarExpanded.gs1 = true;
            dataBarExpanded.setStacked();
            if (dataBarExpanded.setContent(this.content)) {
                this.rect = dataBarExpanded.rect;
                this.symbol_height = dataBarExpanded.symbol_height;
                this.symbol_width = dataBarExpanded.symbol_width;
                this.txt = dataBarExpanded.txt;
                this.encodeInfo += dataBarExpanded.encodeInfo;
                output = true;
            } else {
                this.error_msg = dataBarExpanded.error_msg;
            }
            break;
        case "BARCODE_MAXICODE":
            maxiCode.setPrimary(this.primaryData);
            maxiCode.option1 = this.option1;
            if (maxiCode.setContent(this.content)) {
                this.hex = maxiCode.hex;
                this.target = maxiCode.target;
                this.symbol_height = maxiCode.symbol_height;
                this.symbol_width = maxiCode.symbol_width;
                this.encodeInfo += maxiCode.encodeInfo;
                output = true;
            } else {
                this.error_msg = maxiCode.error_msg;
            }
            break;
        case "BARCODE_CODABLOCKF":
        case "BARCODE_HIBC_BLOCKF":
            codablockF.gs1 = this.gs1;
            codablockF.hibc = this.hibc;
            if (codablockF.setContent(this.content)) {
                this.rect = codablockF.rect;
                this.symbol_height = codablockF.symbol_height;
                this.symbol_width = codablockF.symbol_width;
                this.txt = codablockF.txt;
                this.encodeInfo += codablockF.encodeInfo;
                output = true;
            } else {
                this.error_msg = codablockF.error_msg;
            };
            break;            
        default:
            this.error_msg = "Symbology not recognised";
            break;

        }
        
        if (!(compositeContent.isEmpty())) {
            combineComposite();
        }
        
        encodeInfo += "Symbol Width: " + this.symbol_width + '\n';
        encodeInfo += "Symbol Height: " + this.symbol_height + '\n';
        
        return output;
    }
    
    private void combineComposite() {
        // Put composite symbol and linear symbol together
        ArrayList < Rectangle > combine_rect = new ArrayList < > ();
        ArrayList < TextBox > combine_txt = new ArrayList < > ();
        int i;
        int top_shift = 0;
        int bottom_shift = 0;
        int max_x = 0;
        
        /* Determine horizontal alignment
                (according to section 12.3 of ISO/IEC 24723) */
        switch(symbology) {
            case "BARCODE_EANX":
                if (eanCalculateVersion() == 8) {
                    bottom_shift = 8;
                } else {
                    top_shift = 3;
                }
                break;
            case "BARCODE_CODE128":
                if (composite.getCcMode() == 3) {
                    // CC-C component with GS1-128 linear
                    bottom_shift = 7;
                }
                break;
            case "BARCODE_RSS14":
                bottom_shift = 4;
                break;
            case "BARCODE_RSS_LTD":
                top_shift = 1;
                break;
            case "BARCODE_RSS_EXP":
                top_shift = 2;
                break;
            case "BARCODE_UPCA":
            case "BARCODE_UPCE":
                top_shift = 3;
                break;
            case "BARCODE_RSS14STACK":
            case "BARCODE_RSS14STACK_OMNI":
                top_shift = 1;
                break;
            case "BARCODE_RSS_EXPSTACK":
                top_shift = 2;
                break;
        }
        
        for (i = 0; i < composite.rect.size(); i++) {
            Rectangle comprect = new Rectangle(composite.rect.get(i).x + top_shift, composite.rect.get(i).y, composite.rect.get(i).width, composite.rect.get(i).height);
            if ((composite.rect.get(i).x + top_shift + composite.rect.get(i).width) > max_x) {
                max_x = composite.rect.get(i).x + top_shift + composite.rect.get(i).width;
            }
            combine_rect.add(comprect);
        }
        
        for (i = 0; i < this.rect.size(); i++) {
            Rectangle linrect = new Rectangle(this.rect.get(i).x + bottom_shift, this.rect.get(i).y, this.rect.get(i).width, this.rect.get(i).height);
            linrect.y += composite.symbol_height;
            if ((this.rect.get(i).x + bottom_shift + this.rect.get(i).width) > max_x) {
                max_x = this.rect.get(i).x + bottom_shift + this.rect.get(i).width;
            }
            combine_rect.add(linrect);
        }
        
        for (i = 0; i < this.txt.size(); i++) {
            TextBox lintxt = new TextBox();
            lintxt.xPos = this.txt.get(i).xPos + bottom_shift;
            lintxt.yPos = this.txt.get(i).yPos;
            lintxt.arg = this.txt.get(i).arg;
            lintxt.yPos += composite.symbol_height;
            combine_txt.add(lintxt);
        }
        
        this.rect = combine_rect;
        this.txt = combine_txt;
        this.symbol_height += composite.symbol_height;
        
        if (composite.symbol_width > this.symbol_width) {
            this.symbol_width = max_x;
        }
    }
    
    private int eanCalculateVersion() {
        /* Determine if EAN-8 or EAN-13 is being used */
        
        int length = 0;
        int i;
        boolean latch;
        
        latch = true;
        for (i = 0; i < content.length(); i++) {
            if ((content.charAt(i) >= '0') && (content.charAt(i) <= '9')) {
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
