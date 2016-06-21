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

import com.beust.jcommander.Parameter;
import java.awt.Color;
import uk.org.okapibarcode.backend.HumanReadableLocation;
/**
 *
 * @author <a href="mailto:rstuart114@gmail.com">Robin Stuart</a>
 */
public class Settings {
    
    @Parameter(names = "-cli", description = "Supress GUI loading", required = false)
    private boolean supressGui;
    
    @Parameter(names = {"-t", "--types"}, description = "Display table of barcode types", required = false)
    private boolean displayTypes;
    
    @Parameter(names = {"-i", "--input"}, description = "Read data from file", required = false)
    private String inputFile = "";
    
    @Parameter(names = {"-o", "--output"}, description = "Write image to file", required = false)
    private String outputFile = "out.png";
    
    @Parameter(names = {"-d", "--data"}, description = "Barcode content", required = false)
    private String inputData = "";
    
    @Parameter(names = {"-b", "--barcode"}, description = "Select barcode type", required = false)
    private int symbolType = 20;
    
    @Parameter(names = "--height", description = "Height of the symbol in multiples of x-dimension", required = false)
    private int symbolHeight;
    
//    @Parameter(names = {"-w", "--whitesp"}, description = "Width of whitespace in multiples of x-dimension", required = false)
//    private int symbolWhiteSpace = 0;
//    
//    @Parameter(names = "--border", description = "Width of border in multiples of x-dimension", required = false)
//    private int symbolBorder = 0;
    
//    @Parameter(names = "--box", description = "Add a box", required = false)
//    private boolean addBox = false;
//    
//    @Parameter(names = "--bind", description = "Add boundary bars", required = false)
//    private boolean addBinding = false;
    
    @Parameter(names = {"-r", "--reverse"}, description = "Reverse colours (white on black)", required = false)
    private boolean reverseColour;
    
    @Parameter(names = "--fg", description = "Specify a foreground (ink) colour", required = false)
    private String foregroundColour = "000000";
    
    @Parameter(names = "--bg", description = "Specify a background (paper) colour", required = false)
    private String backgroundColour = "FFFFFF";
    
    @Parameter(names = "--scale", description = "Adjust size of output image", required = false)
    private int symbolScale;
    
    // --directpng, --directeps, --directsvg, --dump
    
//    @Parameter(names = "--rotate", description = "Rotate symbol", required = false)
//    private int rotationAngle = 0;
    
    @Parameter(names = "--cols", description = "Number of columns in PDF417", required = false)
    private int symbolColumns;
    
    @Parameter(names = "--vers", description = "Set QR Code version number", required = false)
    private int symbolVersion;
    
    @Parameter(names = "--secure", description = "Set error correction level", required = false)
    private int symbolECC;
    
    @Parameter(names = "--primary", description = "Add structured primary message", required = false)
    private String primaryData = "";
    
    @Parameter(names = "--mode", description = "Set encoding mode", required = false)
    private int encodeMode;
    
    @Parameter(names = "--gs1", description = "Treat input as GS1 data", required = false)
    private boolean dataGs1Mode;
    
    @Parameter(names = "--binary", description = "Treat input as binary data", required = false)
    private boolean dataBinaryMode;
    
    @Parameter(names = "--notext", description = "Remove human readable text", required = false)
    private boolean supressHrt;
    
    @Parameter(names = "--textabove", description = "Place human readable text above symbol", required = false)
    private boolean superHrt;
    
    @Parameter(names = "--square", description = "Force Data Matrix symbols to be square", required = false)
    private boolean makeSquare;
    
    @Parameter(names = "--init", description = "Add reader initialisation code", required = false)
    private boolean addReaderInit;
    
    // --smalltext
    
    @Parameter(names = "--batch", description = "Treat each line of input as a separate data set", required = false)
    private boolean batchMode;

    /**
     * @return the supressGui
     */
    public boolean isGuiSupressed() {
        return supressGui;
    }
    
    /**
     * @return the displayTypes
     */
    public boolean isDisplayTypes() {
        return displayTypes;
    }

    /**
     * @return the inputFile
     */
    public String getInputFile() {
        return inputFile;
    }

    /**
     * @return the outputFile
     */
    public String getOutputFile() {
        return outputFile;
    }

    /**
     * @return the inputData
     */
    public String getInputData() {
        return inputData;
    }

    /**
     * @return the symbolType
     */
    public int getSymbolType() {
        return symbolType;
    }

    /**
     * @return the symbolHeight
     */
    public int getSymbolHeight() {
        return symbolHeight;
    }

//    /**
//     * @return the symbolWhiteSpace
//     */
//    public int getSymbolWhiteSpace() {
//        return symbolWhiteSpace;
//    }

//    /**
//     * @return the symbolBorder
//     */
//    public int getSymbolBorder() {
//        return symbolBorder;
//    }

//    /**
//     * @return the addBox
//     */
//    public boolean isAddBox() {
//        return addBox;
//    }
//
//    /**
//     * @return the addBinding
//     */
//    public boolean isAddBinding() {
//        return addBinding;
//    }

    /**
     * @return the reverseColour
     */
    public boolean isReverseColour() {
        return reverseColour;
    }

    /**
     * @return the foregroundColour
     */
    public Color getForegroundColour() {
        Color inkColour = Color.BLACK;
        String fgColour;
        
        fgColour = foregroundColour.toUpperCase();
        
        if (fgColour.matches("[0-9A-F]+") && (fgColour.length() == 6)) {
            inkColour = Color.decode("0x" + fgColour);
        }
        
        return inkColour;
    }

    /**
     * @return the backgroundColour
     */
    public Color getBackgroundColour() {
        Color paperColour = Color.WHITE;
        String bgColour;
        
        bgColour = backgroundColour.toUpperCase();
        
        if (bgColour.matches("[0-9A-F]+") && (bgColour.length() == 6)) {
            paperColour = Color.decode("0x" + bgColour);
        }
        
        return paperColour;
    }

    /**
     * @return the symbolScale
     */
    public int getSymbolScale() {
        return symbolScale;
    }

//    /**
//     * @return the rotationAngle
//     */
//    public int getRotationAngle() {
//        return rotationAngle;
//    }

    /**
     * @return the symbolColumns
     */
    public int getSymbolColumns() {
        return symbolColumns;
    }

    /**
     * @return the symbolVersion
     */
    public int getSymbolVersion() {
        return symbolVersion;
    }

    /**
     * @return the symbolECC
     */
    public int getSymbolECC() {
        return symbolECC;
    }

    /**
     * @return the primaryData
     */
    public String getPrimaryData() {
        return primaryData;
    }

    /**
     * @return the encodeMode
     */
    public int getEncodeMode() {
        return encodeMode;
    }

    /**
     * @return the dataGs1Mode
     */
    public boolean isDataGs1Mode() {
        return dataGs1Mode;
    }

    /**
     * @return the dataBinaryMode
     */
    public boolean isDataBinaryMode() {
        return dataBinaryMode;
    }

    /**
     * @return the supressHrt
     */
    public HumanReadableLocation getHrtPosition() {
        HumanReadableLocation temp = HumanReadableLocation.BOTTOM;
        
        if(superHrt) {
            temp = HumanReadableLocation.TOP;
        }
        
        if(supressHrt) {
            temp = HumanReadableLocation.NONE;
        }
        
        return temp;
    }

    /**
     * @return the makeSquare
     */
    public boolean isMakeSquare() {
        return makeSquare;
    }

    /**
     * @return the addReaderInit
     */
    public boolean isReaderInit() {
        return addReaderInit;
    }

    /**
     * @return the batchMode
     */
    public boolean isBatchMode() {
        return batchMode;
    }
    
}