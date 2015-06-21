/*
 * Copyright 2014-2015 Robin Stuart, Robert Elliott
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

import uk.org.okapibarcode.gui.OkapiUI;
import com.beust.jcommander.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * Starts the Okapi Barcode UI.
 *
 * @author <a href="mailto:rstuart114@gmail.com">Robin Stuart</a>
 * @author <a href="mailto:jakel2006@me.com">Robert Elliott</a>
 */
public class OkapiBarcode {

    /**
     * Starts the Okapi Barcode UI.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Settings settings = new Settings();
        new JCommander(settings, args);

        if (settings.isGuiSupressed() == false) {
            OkapiUI okapiUi = new OkapiUI();
            okapiUi.setVisible(true);
        } else {
            int returnValue;
            
            returnValue = commandLine(settings);
            
            if (returnValue != 0) {
                System.out.println("An error occurred");
            }
        }
    }

    private static int commandLine(Settings settings) {
        
        String inputData = settings.getInputData();
        String inputFile = settings.getInputFile();
        
        if (settings.isDisplayTypes()) {
            System.out.print(
                " 1: Code 11           54: Brazilian CepNet         97: Micro QR Code\n" +
		" 2: Standard 2of5     55: PDF417                   98: HIBC Code 128\n" +
		" 3: Interleaved 2of5  56: PDF417 Trunc             99: HIBC Code 39\n" +
		" 4: IATA 2of5         57: Maxicode                102: HIBC Data Matrix\n" +
		" 6: Data Logic        58: QR Code                 104: HIBC QR Code\n" +
		" 7: Industrial 2of5   60: Code 128-B              106: HIBC PDF417\n" +
		" 8: Code 39           63: AP Standard Customer    108: HIBC MicroPDF417\n" +
		" 9: Extended Code 39  66: AP Reply Paid           110: HIBC Codablock-F\n" +
		"13: EAN               67: AP Routing              112: HIBC Aztec Code\n" +
		"18: Codabar           68: AP Redirection          113: PZN-8\n" +
		"20: Code 128          70: RM4SCC                  117: USPS IMpb\n" +
		"21: Leitcode          71: Data Matrix             128: Aztec Runes\n" +
		"22: Identcode         74: Codablock-F             129: Code 32\n" +
		"23: Code 16k          75: NVE-18                  130: Comp EAN\n" +
		"24: Code 49           76: Japanese Post           131: Comp GS1-128\n" +
		"25: Code 93           77: Korea Post              132: Comp Databar-14\n" +
		"29: Databar-14        79: Databar-14 Stack        133: Comp Databar Ltd\n" +
		"30: Databar Limited   80: Databar-14 Stack Omni   134: Comp Databar Ext\n" +
		"31: Databar Extended  81: Databar Extended Stack  135: Comp UPC-A\n" +
		"32: Telepen Alpha     82: Planet                  136: Comp UPC-E\n" +
		"34: UPC-A             84: MicroPDF                137: Comp Databar-14 Stack\n" +
		"37: UPC-E             85: USPS Intelligent Mail   138: Comp Databar Stack Omni\n" +
		"40: Postnet           87: Telepen Numeric         139: Comp Databar Ext Stack\n" +
		"47: MSI Plessey       89: ITF-14                  140: Channel Code\n" +
		"50: Logmars           90: KIX Code                141: Code One \n" +
                "51: Pharma One-Track  92: Aztec Code              142: Grid Matrix\n" +
                "53: Pharma Two-Track  93: Code 32\n" );
        }
        
        if (inputData.isEmpty() && inputFile.isEmpty()) {
            System.out.println("error: No data received, no symbol generated");
            return 0;
        }
        
        if (inputFile.isEmpty()) {
            if (!(settings.isDataBinaryMode())) {
                inputData = escapeCharProcess(inputData);
            }
            MakeBarcode mb = new MakeBarcode();
            mb.process(settings, inputData, settings.getOutputFile());
        } else {
            processFile(settings);
        }
        
        return 0;
    }
    
    private static void processFile(Settings settings) {
        File name = new File(settings.getInputFile());
        FileInputStream fis;
        byte[] inputBytes;
        String inputData;
        int counter = 0;
        
        if (!(settings.isBatchMode())) {
            // Encode all data from selected file in one symbol
            try {
                fis = new FileInputStream(name);
                inputBytes = new byte[fis.available()];
                fis.read(inputBytes);
                inputData = new String(inputBytes, "UTF-8");
                MakeBarcode mb = new MakeBarcode();
                mb.process(settings, inputData, settings.getOutputFile());
            } catch (IOException e) {
                System.out.println("File Read Error");
            } 
        } else {
            // Encode each line of input data in a seperate symbol
            try {
                BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                        new FileInputStream(name), "UTF8"));

                while ((inputData = in.readLine()) != null) {
                    counter++;
                    MakeBarcode mb = new MakeBarcode();
                    mb.process(settings, inputData, calcFileName(settings, counter));
                }
            } catch (UnsupportedEncodingException e) {
                System.out.println("Encoding exception");
            } catch (IOException e) {
                System.out.println("File Read Error");
            }
        }
    }    
    
    private static String calcFileName(Settings settings, int counter) {
        String fileName = "";
        String number;
        int spaces = 0;
        int blanks;
        int blankPosition;
        String template;
        
        number = Integer.toString(counter);

        if (settings.getOutputFile().equals("out.png")) {
            // No filename set by user
            template = "~~~~~.png";
        } else {
            template = settings.getOutputFile();
        }
        
        for (int i = 0; i < template.length(); i++) {
            switch(template.charAt(i)) {
                case '#':
                case '~':
                    spaces++;
                    break;
            }
        }
        
        blanks = spaces - number.length();
        
        if (blanks < 0) {
            // Not enough room in template for file number
            System.out.println("Invalid output filename");
            return "out.png";
        }
        
        blankPosition = 0;
        
        for (int i = 0; i < template.length(); i++) {
            switch(template.charAt(i)) {
                case '#':
                    if (blankPosition >= blanks) {
                        fileName += number.charAt(blankPosition - blanks);
                    } else {
                        fileName += ' ';
                    }
                    blankPosition++;
                    break;
                case '~':
                    if (blankPosition >= blanks) {
                        fileName += number.charAt(blankPosition - blanks);
                    } else {
                        fileName += '0';
                    }
                    blankPosition++;
                    break;
                default:
                    fileName += template.charAt(i);
                    break;
            }
        }
        
        return fileName;
    }
    
    private static String escapeCharProcess(String inputString) {
        String outputString = "";
        int i = 0;
        
        do {
            if (inputString.charAt(i) == '\\') {
                if (i < inputString.length() - 1) {
                    switch(inputString.charAt(i + 1)) {
                        case '0': /* Null */
                            outputString += 0x00; 
                            break; 
			case 'E': /* End of Transmission */
                            outputString += 0x04; 
                            break; 
			case 'a': /* Bell */
                            outputString += 0x07; 
                            break; 
			case 'b': /* Backspace */
                            outputString += 0x08; 
                            break; 
			case 't': /* Horizontal tab */
                            outputString += 0x09; 
                            break; 
			case 'n': /* Line feed */
                            outputString += 0x0a; 
                            break; 
			case 'v': /* Vertical tab */
                            outputString += 0x0b; 
                            break; 
			case 'f': /* Form feed */
                            outputString += 0x0c; 
                            break; 
			case 'r': /* Carriage return */
                            outputString += 0x0d; 
                            break; 
			case 'e': /* Escape */
                            outputString += 0x1b; 
                            break; 
			case 'G': /* Group Separator */
                            outputString +=0x1d; 
                            break; 
			case 'R': /* Record Separator */
                            outputString += 0x1e;
                            break; 
			default:    
                            outputString += '\\';
                            outputString += inputString.charAt(i); 
                            break;
                    }
                    i += 2;
                } else {
                    outputString += '\\';
                    i++;
                }
            } else {
                outputString += inputString.charAt(i);
                i++;
            }
        } while(i < inputString.length());
        
        return outputString;
    }

}
