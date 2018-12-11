/*
 * Copyright 2014-2015 Robin Stuart, Daniel Gredler
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

package uk.org.okapibarcode.gui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import javax.swing.BorderFactory;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JTree;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

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
import uk.org.okapibarcode.backend.Code2Of5.ToFMode;
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
import uk.org.okapibarcode.backend.DataMatrix.ForceMode;
import uk.org.okapibarcode.backend.Ean;
import uk.org.okapibarcode.backend.GridMatrix;
import uk.org.okapibarcode.backend.HumanReadableLocation;
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
/**
 * The main Okapi Barcode UI.
 *
 * @author <a href="mailto:rstuart114@gmail.com">Robin Stuart</a>
 * @author Daniel Gredler
 */
public class OkapiUI extends javax.swing.JFrame implements TreeSelectionListener{

    /** Serial version UID. */
    private static final long serialVersionUID = -681156299104876221L;

    public static String dataInput = null; //Original User Input
    public static String compositeInput = null; // User input for composite symbol
    public static String outputf = null; //file to output to
    public static int factor = 1;
    public static int barHeight = 0;
    public static boolean debug = true;
    public static Object[] bc;
    public static Symbol symbol;
    DefaultMutableTreeNode treeTop = new DefaultMutableTreeNode("Symbologies");
    public static Color inkColour = new Color(0, 0, 0);
    public static Color paperColour = new Color(255, 255, 255);
    public static int moduleWidth = 4;
    public static int quietZoneHorizontal = 5;
    public static int quietZoneVertical = 5;
    private SymbolType selectedSymbol;

    /**
     * Creates new form OkapiUI: the main interface
     */
    public OkapiUI() {
        initComponents();
        createNodes (treeTop);

        symbolTree.getSelectionModel().setSelectionMode
                (TreeSelectionModel.SINGLE_TREE_SELECTION);
        symbolTree.addTreeSelectionListener(this);
        symbolTree.expandRow(0);
        symbolTree.expandRow(1);
        symbolTree.expandRow(7);
        symbolTree.setSelectionRow(8); // Selects Code 128 as default

        TextListener tl = new TextListener() {
            @Override
            public void textValueChanged(TextEvent e) {
                if (sequenceArea.getText().isEmpty()) {
                    runBatchButton.setEnabled(false);
                } else {
                    runBatchButton.setEnabled(true);
                }
            }
        };
        sequenceArea.addTextListener(tl);

        DocumentListener dl = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateMe();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateMe();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateMe();
            }

            public void updateMe() {
                dataInput = dataInputField.getText();
                compositeInput = compositeInputField.getText();
                encodeData();
            }
        };
        dataInputField.getDocument().addDocumentListener(dl);
        compositeInputField.getDocument().addDocumentListener(dl);

        folderField.setText(System.getProperty("user.home") + File.separator);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        aztecButtonGroup = new javax.swing.ButtonGroup();
        gridmatrixButtonGroup = new javax.swing.ButtonGroup();
        microQrButtonGroup = new javax.swing.ButtonGroup();
        qrButtonGroup = new javax.swing.ButtonGroup();
        symbolPane = new javax.swing.JScrollPane();
        symbolTree = new JTree(treeTop);
        mainTabs = new javax.swing.JTabbedPane();
        singlePanel = new javax.swing.JPanel();
        compositeInputField = new javax.swing.JTextField();
        compositeLabel = new javax.swing.JLabel();
        dataInputField = new javax.swing.JTextField();
        inputLabel = new javax.swing.JLabel();
        loadDataButton = new javax.swing.JButton();
        topPanel = new javax.swing.JPanel();
        errorLabel = new javax.swing.JLabel();
        addCompositeButton = new javax.swing.JButton();
        useGS1Check = new javax.swing.JCheckBox();
        useCompositeCheck = new javax.swing.JCheckBox();
        batchPanel = new javax.swing.JPanel();
        startField = new javax.swing.JTextField();
        stopField = new javax.swing.JTextField();
        incrementField = new javax.swing.JTextField();
        startLabel = new javax.swing.JLabel();
        stopLabel = new javax.swing.JLabel();
        incrementLabel = new javax.swing.JLabel();
        formatLabel = new javax.swing.JLabel();
        batchFileButton = new javax.swing.JButton();
        createButton = new javax.swing.JButton();
        folderField = new javax.swing.JTextField();
        prefixField = new javax.swing.JTextField();
        destinationLabel = new javax.swing.JLabel();
        prefixLabel = new javax.swing.JLabel();
        outFileNameLabel = new javax.swing.JLabel();
        outFileFormatLabel = new javax.swing.JLabel();
        outFilenameCombo = new javax.swing.JComboBox();
        outFormatCombo = new javax.swing.JComboBox();
        runBatchButton = new javax.swing.JButton();
        formatField = new javax.swing.JTextField();
        directoryButton = new javax.swing.JButton();
        resetButton = new javax.swing.JButton();
        sequenceArea = new java.awt.TextArea();
        batchOutputArea = new java.awt.TextArea();
        attributePanel = new javax.swing.JPanel();
        inkButton = new javax.swing.JButton();
        paperButton = new javax.swing.JButton();
        resetColourButton = new javax.swing.JButton();
        encodeInfoArea = new java.awt.TextArea();
        attributeScrollPane = new javax.swing.JScrollPane();
        attributeScrollPanel = new javax.swing.JPanel();
        aztecPanel = new javax.swing.JPanel();
        aztecAutoSize = new javax.swing.JRadioButton();
        aztecUserSize = new javax.swing.JRadioButton();
        aztecUserEcc = new javax.swing.JRadioButton();
        aztecUserSizeCombo = new javax.swing.JComboBox();
        aztecUserEccCombo = new javax.swing.JComboBox();
        channelPanel = new javax.swing.JPanel();
        channelNoOfChannels = new javax.swing.JLabel();
        channelChannelsCombo = new javax.swing.JComboBox();
        code39Panel = new javax.swing.JPanel();
        code39CheckLabel = new javax.swing.JLabel();
        code39CheckCombo = new javax.swing.JComboBox();
        codeOnePanel = new javax.swing.JPanel();
        codeOneSizeLabel = new javax.swing.JLabel();
        codeOneSizeCombo = new javax.swing.JComboBox();
        databarPanel = new javax.swing.JPanel();
        databarColumnsLabel = new javax.swing.JLabel();
        databarColumnsCombo = new javax.swing.JComboBox();
        datamatrixPanel = new javax.swing.JPanel();
        dataMatrixSizeLabel = new javax.swing.JLabel();
        dataMatrixSizeCombo = new javax.swing.JComboBox();
        dataMatrixSquareOnlyCheck = new javax.swing.JCheckBox();
        gridmatrixPanel = new javax.swing.JPanel();
        gridmatrixAutoSize = new javax.swing.JRadioButton();
        gridmatrixUserSize = new javax.swing.JRadioButton();
        gridmatrixUserEcc = new javax.swing.JRadioButton();
        gridmatrixUserSizeCombo = new javax.swing.JComboBox();
        gridmatrixUserEccCombo = new javax.swing.JComboBox();
        maxicodePanel = new javax.swing.JPanel();
        maxiEncodeModeLabel = new javax.swing.JLabel();
        maxiPrimaryDataLabel = new javax.swing.JLabel();
        maxiEncodingModeCombo = new javax.swing.JComboBox();
        maxiPrimaryData = new javax.swing.JTextField();
        microPdfPanel = new javax.swing.JPanel();
        microPdfColumnsLabel = new javax.swing.JLabel();
        microPdfColumnsCombo = new javax.swing.JComboBox();
        microQrPanel = new javax.swing.JPanel();
        microQrAutoSize = new javax.swing.JRadioButton();
        microQrUserSize = new javax.swing.JRadioButton();
        microQrUserEcc = new javax.swing.JRadioButton();
        microQrUserSizeCombo = new javax.swing.JComboBox();
        microQrUserEccCombo = new javax.swing.JComboBox();
        msiPanel = new javax.swing.JPanel();
        msiCheckDigitLabel = new javax.swing.JLabel();
        msiCheckDigitCombo = new javax.swing.JComboBox();
        pdfPanel = new javax.swing.JPanel();
        pdfDataColumnsLabel = new javax.swing.JLabel();
        pdfEccLabel = new javax.swing.JLabel();
        pdfColumnsCombo = new javax.swing.JComboBox();
        pdfEccCombo = new javax.swing.JComboBox();
        qrPanel = new javax.swing.JPanel();
        qrAutoSize = new javax.swing.JRadioButton();
        qrUserSize = new javax.swing.JRadioButton();
        qrUserEcc = new javax.swing.JRadioButton();
        qrUserSizeCombo = new javax.swing.JComboBox();
        qrUserEccCombo = new javax.swing.JComboBox();
        compositePanel = new javax.swing.JPanel();
        compositeModeLabel = new javax.swing.JLabel();
        compositeUserMode = new javax.swing.JComboBox();
        chkReaderInit = new javax.swing.JCheckBox();
        cmbHrtPosition = new javax.swing.JComboBox();
        txtShowHrt = new javax.swing.JLabel();
        lblXDimension = new javax.swing.JLabel();
        lblXDimensionPixels = new javax.swing.JLabel();
        lblBorderWidth = new javax.swing.JLabel();
        lblWhitespaceWidth = new javax.swing.JLabel();
        txtXDimension = new javax.swing.JTextField();
        txtBorderWidth = new javax.swing.JTextField();
        txtWhitespaceWidth = new javax.swing.JTextField();
        exitButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();
        aboutButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Okapi Barcode");
        setResizable(false);

        symbolPane.setViewportView(symbolTree);

        mainTabs.setName(""); // NOI18N

        compositeInputField.setEnabled(false);

        compositeLabel.setText("Composite:");
        compositeLabel.setEnabled(false);

        dataInputField.setText("Your Data Here!");

        inputLabel.setText("Data:");

        loadDataButton.setText("...");
        loadDataButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadDataButtonActionPerformed(evt);
            }
        });

        topPanel.setBackground(paperColour);
        topPanel.setPreferredSize(new java.awt.Dimension(480, 480));

        errorLabel.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        errorLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        errorLabel.setText("Error");

        javax.swing.GroupLayout topPanelLayout = new javax.swing.GroupLayout(topPanel);
        topPanel.setLayout(topPanelLayout);
        topPanelLayout.setHorizontalGroup(
            topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, topPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(errorLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 322, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(164, 164, 164))
        );
        topPanelLayout.setVerticalGroup(
            topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, topPanelLayout.createSequentialGroup()
                .addContainerGap(237, Short.MAX_VALUE)
                .addComponent(errorLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(194, 194, 194))
        );

        addCompositeButton.setText("...");
        addCompositeButton.setEnabled(false);
        addCompositeButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addCompositeButtonActionPerformed(evt);
            }
        });

        useGS1Check.setText("Use GS1 Data Encodation");
        useGS1Check.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useGS1CheckActionPerformed(evt);
            }
        });

        useCompositeCheck.setText("Add Composite Component");
        useCompositeCheck.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useCompositeCheckActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout singlePanelLayout = new javax.swing.GroupLayout(singlePanel);
        singlePanel.setLayout(singlePanelLayout);
        singlePanelLayout.setHorizontalGroup(
            singlePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, singlePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(singlePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(compositeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 216, Short.MAX_VALUE)
                    .addGroup(singlePanelLayout.createSequentialGroup()
                        .addComponent(inputLabel)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(singlePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(singlePanelLayout.createSequentialGroup()
                        .addComponent(useGS1Check, javax.swing.GroupLayout.PREFERRED_SIZE, 245, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(useCompositeCheck, javax.swing.GroupLayout.PREFERRED_SIZE, 236, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(singlePanelLayout.createSequentialGroup()
                        .addGroup(singlePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(dataInputField, javax.swing.GroupLayout.DEFAULT_SIZE, 520, Short.MAX_VALUE)
                            .addComponent(compositeInputField))
                        .addGap(8, 8, 8)
                        .addGroup(singlePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(loadDataButton, javax.swing.GroupLayout.DEFAULT_SIZE, 60, Short.MAX_VALUE)
                            .addComponent(addCompositeButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
            .addComponent(topPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 846, Short.MAX_VALUE)
        );
        singlePanelLayout.setVerticalGroup(
            singlePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, singlePanelLayout.createSequentialGroup()
                .addComponent(topPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 459, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 56, Short.MAX_VALUE)
                .addGroup(singlePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(useGS1Check)
                    .addComponent(useCompositeCheck))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(singlePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dataInputField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(inputLabel)
                    .addComponent(loadDataButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(singlePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(compositeInputField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(compositeLabel)
                    .addComponent(addCompositeButton))
                .addContainerGap())
        );

        mainTabs.addTab("Single", singlePanel);

        startField.setText("1");

        stopField.setText("10");

        incrementField.setText("1");

        startLabel.setText("Start Value:");

        stopLabel.setText("End Value:");

        incrementLabel.setText("Increment By:");

        formatLabel.setText("Format:");

        batchFileButton.setText("Import");
        batchFileButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                batchFileButtonActionPerformed(evt);
            }
        });

        createButton.setText("Create");
        createButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createButtonActionPerformed(evt);
            }
        });

        prefixField.setText("bcs_");

        destinationLabel.setText("Destination Path:");

        prefixLabel.setText("File Prefix:");

        outFileNameLabel.setText("File Name:");

        outFileFormatLabel.setText("File Format:");

        outFilenameCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Same as Data", "Line Number" }));

        outFormatCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Portable Network Graphic (*.png)", "Joint Photographic Expert Group Image (*.jpg)", "Graphics Interchange Format (*.gif)", "Windows Bitmap (*.bmp)", "Scalable Vector Graphic (*.svg)", "Encapsulated Post Script (*.eps)" }));

        runBatchButton.setText("Run Batch");
        runBatchButton.setEnabled(false);
        runBatchButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runBatchButtonActionPerformed(evt);
            }
        });

        formatField.setText("$$$$$$");

        directoryButton.setText("Select Directory");
        directoryButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                directoryButtonActionPerformed(evt);
            }
        });

        resetButton.setText("Reset");
        resetButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout batchPanelLayout = new javax.swing.GroupLayout(batchPanel);
        batchPanel.setLayout(batchPanelLayout);
        batchPanelLayout.setHorizontalGroup(
            batchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(batchPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(batchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(batchOutputArea, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(sequenceArea, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(batchPanelLayout.createSequentialGroup()
                        .addGroup(batchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(incrementLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 98, Short.MAX_VALUE)
                            .addComponent(stopLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(startLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(batchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(stopField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 182, Short.MAX_VALUE)
                            .addComponent(incrementField)
                            .addComponent(startField))
                        .addGap(18, 18, 18)
                        .addGroup(batchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(batchPanelLayout.createSequentialGroup()
                                .addComponent(formatLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(formatField, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, batchPanelLayout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addGroup(batchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(batchFileButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, batchPanelLayout.createSequentialGroup()
                                        .addComponent(resetButton, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(createButton, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                    .addGroup(batchPanelLayout.createSequentialGroup()
                        .addGroup(batchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(destinationLabel)
                            .addComponent(prefixLabel)
                            .addComponent(outFileNameLabel)
                            .addComponent(outFileFormatLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(batchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(outFilenameCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(prefixField, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(folderField, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(outFormatCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 365, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(batchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(directoryButton, javax.swing.GroupLayout.PREFERRED_SIZE, 131, Short.MAX_VALUE)
                            .addComponent(runBatchButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        batchPanelLayout.setVerticalGroup(
            batchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(batchPanelLayout.createSequentialGroup()
                .addGroup(batchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(startField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(startLabel)
                    .addComponent(formatLabel)
                    .addComponent(formatField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(batchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(stopField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(stopLabel)
                    .addComponent(resetButton)
                    .addComponent(createButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(batchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(incrementField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(incrementLabel)
                    .addComponent(batchFileButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sequenceArea, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(batchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(folderField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(destinationLabel)
                    .addComponent(directoryButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(batchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(prefixField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(prefixLabel))
                .addGroup(batchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(batchPanelLayout.createSequentialGroup()
                        .addGap(11, 11, 11)
                        .addComponent(outFileNameLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(outFileFormatLabel))
                    .addGroup(batchPanelLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(outFilenameCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(batchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(outFormatCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(runBatchButton))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(batchOutputArea, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        mainTabs.addTab("Batch", batchPanel);

        inkButton.setText("Select Ink Colour");
        inkButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inkButtonActionPerformed(evt);
            }
        });

        paperButton.setText("Select Paper Colour");
        paperButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                paperButtonActionPerformed(evt);
            }
        });

        resetColourButton.setText("Reset Colours");
        resetColourButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetColourButtonActionPerformed(evt);
            }
        });

        encodeInfoArea.setEditable(false);

        aztecPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Aztec Code"));

        aztecButtonGroup.add(aztecAutoSize);
        aztecAutoSize.setSelected(true);
        aztecAutoSize.setText("Automatic Resizing");
        aztecAutoSize.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aztecAutoSizeActionPerformed(evt);
            }
        });

        aztecButtonGroup.add(aztecUserSize);
        aztecUserSize.setText("Adjust Size To:");
        aztecUserSize.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aztecUserSizeActionPerformed(evt);
            }
        });

        aztecButtonGroup.add(aztecUserEcc);
        aztecUserEcc.setText("Add Minimum Error Correction:");
        aztecUserEcc.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aztecUserEccActionPerformed(evt);
            }
        });

        aztecUserSizeCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "15 X 15 Compact", "19 X 19 Compact", "23 X 23 Compact", "27 X 27 Compact", "19 X 19", "23 X 23", "27 X 27", "31 X 31", "37 X 37", "41 X 41", "45 X 45", "49 X 49", "53 X 53", "57 X 57", "61 X 61", "67 X 67", "71 X 71", "75 X 75", "79 X 79", "83 X 83", "87 X 87", "91 X 91", "95 X 95", "101 X 101", "105 X 105", "109 X 109", "113 X 113", "117 X 117", "121 X 121", "125 X 125", "131 X 131", "135 X 135", "139 X 139", "143 X 143", "147 X 147", "151 X 151" }));
        aztecUserSizeCombo.setEnabled(false);
        aztecUserSizeCombo.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aztecUserSizeComboActionPerformed(evt);
            }
        });

        aztecUserEccCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "10% + 3 words", "23% + 3 words", "36% + 3 words", "50% + 3 words" }));
        aztecUserEccCombo.setEnabled(false);
        aztecUserEccCombo.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aztecUserEccComboActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout aztecPanelLayout = new javax.swing.GroupLayout(aztecPanel);
        aztecPanel.setLayout(aztecPanelLayout);
        aztecPanelLayout.setHorizontalGroup(
            aztecPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(aztecPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(aztecPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(aztecAutoSize, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(aztecUserSize, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(aztecUserEcc, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(aztecPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(aztecUserEccCombo, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(aztecUserSizeCombo, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        aztecPanelLayout.setVerticalGroup(
            aztecPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(aztecPanelLayout.createSequentialGroup()
                .addComponent(aztecAutoSize)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(aztecPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(aztecUserSize, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(aztecUserSizeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(aztecPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(aztecUserEccCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(aztecUserEcc))
                .addGap(1, 1, 1))
        );

        channelPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Channel Code"));

        channelNoOfChannels.setText("Number of Channels:");

        channelChannelsCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Automatic", "3", "4", "5", "6", "7", "8" }));
        channelChannelsCombo.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                channelChannelsComboActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout channelPanelLayout = new javax.swing.GroupLayout(channelPanel);
        channelPanel.setLayout(channelPanelLayout);
        channelPanelLayout.setHorizontalGroup(
            channelPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(channelPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(channelNoOfChannels)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(channelChannelsCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        channelPanelLayout.setVerticalGroup(
            channelPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(channelPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(channelNoOfChannels)
                .addComponent(channelChannelsCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        code39Panel.setBorder(javax.swing.BorderFactory.createTitledBorder("Code 39"));

        code39CheckLabel.setText("Check Digit Option:");

        code39CheckCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "No Check Digit", "Mod-43 Check Digit" }));
        code39CheckCombo.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                code39CheckComboActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout code39PanelLayout = new javax.swing.GroupLayout(code39Panel);
        code39Panel.setLayout(code39PanelLayout);
        code39PanelLayout.setHorizontalGroup(
            code39PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(code39PanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(code39CheckLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(code39CheckCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        code39PanelLayout.setVerticalGroup(
            code39PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(code39PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(code39CheckLabel)
                .addComponent(code39CheckCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        codeOnePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Code One"));

        codeOneSizeLabel.setText("Symbol Size:");

        codeOneSizeCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Automatic", "16 X 18 (Version A)", "22 X 22 (Version B)", "28 X 32 (Version C)", "40 X 42 (Version D)", "52 X 54 (Version E)", "70 X 76 (Version F)", "104 X 98 (Version G)", "148 X 134 (Version H)", "8X Height (Version S)", "16X Height (Version T)" }));
        codeOneSizeCombo.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                codeOneSizeComboActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout codeOnePanelLayout = new javax.swing.GroupLayout(codeOnePanel);
        codeOnePanel.setLayout(codeOnePanelLayout);
        codeOnePanelLayout.setHorizontalGroup(
            codeOnePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(codeOnePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(codeOneSizeLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(codeOneSizeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        codeOnePanelLayout.setVerticalGroup(
            codeOnePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(codeOnePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(codeOneSizeLabel)
                .addComponent(codeOneSizeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        databarPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("GS1 DataBar Expanded Stacked"));

        databarColumnsLabel.setText("Number of Columns:");

        databarColumnsCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Automatic", "1", "2", "3", "4", "5", "6", "7", "8", "9" }));
        databarColumnsCombo.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                databarColumnsComboActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout databarPanelLayout = new javax.swing.GroupLayout(databarPanel);
        databarPanel.setLayout(databarPanelLayout);
        databarPanelLayout.setHorizontalGroup(
            databarPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(databarPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(databarColumnsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(databarColumnsCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        databarPanelLayout.setVerticalGroup(
            databarPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(databarPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(databarColumnsLabel)
                .addComponent(databarColumnsCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        datamatrixPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Data Matrix"));

        dataMatrixSizeLabel.setText("Symbol Size:");

        dataMatrixSizeCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Automatic", "10 X 10", "12 X 12", "14 X 14", "16 X 16", "18 X 18", "20 X 20", "22 X 22", "24 X 24", "26 X 26", "32 X 32", "36 X 36", "40 X 40", "44 X 44", "48 X 48", "52 X 52", "64 X 64", "72 X 72", "80 X 80", "88 X 88", "96 X 96", "104 X 104", "120 X 120", "132 X 132", "144 X 144", "8 X 18", "8 X 32", "12 X 26", "12 X 36", "16 X 36", "16 X 48" }));
        dataMatrixSizeCombo.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dataMatrixSizeComboActionPerformed(evt);
            }
        });

        dataMatrixSquareOnlyCheck.setSelected(true);
        dataMatrixSquareOnlyCheck.setText("Supress Rectangular Symbols in Automatic Mode");
        dataMatrixSquareOnlyCheck.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dataMatrixSquareOnlyCheckActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout datamatrixPanelLayout = new javax.swing.GroupLayout(datamatrixPanel);
        datamatrixPanel.setLayout(datamatrixPanelLayout);
        datamatrixPanelLayout.setHorizontalGroup(
            datamatrixPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(datamatrixPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(datamatrixPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(datamatrixPanelLayout.createSequentialGroup()
                        .addComponent(dataMatrixSquareOnlyCheck)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(datamatrixPanelLayout.createSequentialGroup()
                        .addComponent(dataMatrixSizeLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(dataMatrixSizeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        datamatrixPanelLayout.setVerticalGroup(
            datamatrixPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(datamatrixPanelLayout.createSequentialGroup()
                .addGroup(datamatrixPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dataMatrixSizeLabel)
                    .addComponent(dataMatrixSizeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dataMatrixSquareOnlyCheck))
        );

        gridmatrixPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Grid Matrix"));

        gridmatrixButtonGroup.add(gridmatrixAutoSize);
        gridmatrixAutoSize.setSelected(true);
        gridmatrixAutoSize.setText("Automatic Resizing");
        gridmatrixAutoSize.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gridmatrixAutoSizeActionPerformed(evt);
            }
        });

        gridmatrixButtonGroup.add(gridmatrixUserSize);
        gridmatrixUserSize.setText("Adjust Size To:");
        gridmatrixUserSize.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gridmatrixUserSizeActionPerformed(evt);
            }
        });

        gridmatrixButtonGroup.add(gridmatrixUserEcc);
        gridmatrixUserEcc.setText("Add Minimum Error Correction:");
        gridmatrixUserEcc.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gridmatrixUserEccActionPerformed(evt);
            }
        });

        gridmatrixUserSizeCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "18 X 18 (Version 1)", "30 X 30 (Version 2)", "42 X 42 (Version 3)", "54 X 54 (Version 4)", "66 X 66 (Version 5)", "78 X 78 (Version 6)", "90 X 90 (Version 7)", "102 X 102 (Version 8)", "114 X 114 (Version 9)", "126 X 126 (Version 10)", "138 X 138 (Version 11)", "150 X 150 (Version 12)", "162 X 162 (Version 13)" }));
        gridmatrixUserSizeCombo.setEnabled(false);
        gridmatrixUserSizeCombo.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gridmatrixUserSizeComboActionPerformed(evt);
            }
        });

        gridmatrixUserEccCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Approx 10%", "Approx 20%", "Approx 30%", "Approx 40%", "Approx 50%" }));
        gridmatrixUserEccCombo.setEnabled(false);
        gridmatrixUserEccCombo.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gridmatrixUserEccComboActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout gridmatrixPanelLayout = new javax.swing.GroupLayout(gridmatrixPanel);
        gridmatrixPanel.setLayout(gridmatrixPanelLayout);
        gridmatrixPanelLayout.setHorizontalGroup(
            gridmatrixPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(gridmatrixPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(gridmatrixPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(gridmatrixAutoSize, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(gridmatrixUserSize, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(gridmatrixUserEcc, javax.swing.GroupLayout.DEFAULT_SIZE, 248, Short.MAX_VALUE))
                .addGap(74, 74, 74)
                .addGroup(gridmatrixPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(gridmatrixUserSizeCombo, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gridmatrixUserEccCombo, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        gridmatrixPanelLayout.setVerticalGroup(
            gridmatrixPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(gridmatrixPanelLayout.createSequentialGroup()
                .addComponent(gridmatrixAutoSize)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(gridmatrixPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(gridmatrixUserSize)
                    .addComponent(gridmatrixUserSizeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(gridmatrixPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(gridmatrixUserEcc)
                    .addComponent(gridmatrixUserEccCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        maxicodePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Maxicode"));

        maxiEncodeModeLabel.setText("Encoding Mode:");

        maxiPrimaryDataLabel.setText("Primary Data:");
        maxiPrimaryDataLabel.setEnabled(false);

        maxiEncodingModeCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Structured Carrier Message (Mode 2)", "Structured Carrier Message (Mode 3)", "Standard Symbol (Mode 4)", "Full ECC Symbol (Mode 5)" }));
        maxiEncodingModeCombo.setSelectedIndex(2);
        maxiEncodingModeCombo.setToolTipText("");
        maxiEncodingModeCombo.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                maxiEncodingModeComboActionPerformed(evt);
            }
        });

        maxiPrimaryData.setText("Primary Data Here!");
        maxiPrimaryData.setEnabled(false);
        maxiPrimaryData.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                maxiPrimaryDataActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout maxicodePanelLayout = new javax.swing.GroupLayout(maxicodePanel);
        maxicodePanel.setLayout(maxicodePanelLayout);
        maxicodePanelLayout.setHorizontalGroup(
            maxicodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(maxicodePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(maxicodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(maxiEncodeModeLabel)
                    .addComponent(maxiPrimaryDataLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(maxicodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(maxiEncodingModeCombo, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(maxiPrimaryData, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        maxicodePanelLayout.setVerticalGroup(
            maxicodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(maxicodePanelLayout.createSequentialGroup()
                .addGroup(maxicodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maxiEncodeModeLabel)
                    .addComponent(maxiEncodingModeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(9, 9, 9)
                .addGroup(maxicodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maxiPrimaryData, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(maxiPrimaryDataLabel))
                .addGap(2, 2, 2))
        );

        microPdfPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Micro PDF417"));

        microPdfColumnsLabel.setText("Number of Data Columns:");

        microPdfColumnsCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Automatic", "1", "2", "3", "4" }));
        microPdfColumnsCombo.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                microPdfColumnsComboActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout microPdfPanelLayout = new javax.swing.GroupLayout(microPdfPanel);
        microPdfPanel.setLayout(microPdfPanelLayout);
        microPdfPanelLayout.setHorizontalGroup(
            microPdfPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(microPdfPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(microPdfColumnsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(microPdfColumnsCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        microPdfPanelLayout.setVerticalGroup(
            microPdfPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(microPdfColumnsCombo, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(microPdfColumnsLabel, javax.swing.GroupLayout.Alignment.TRAILING)
        );

        microQrPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Micro QR Code"));

        microQrButtonGroup.add(microQrAutoSize);
        microQrAutoSize.setSelected(true);
        microQrAutoSize.setText("Automatic Resizing");
        microQrAutoSize.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                microQrAutoSizeActionPerformed(evt);
            }
        });

        microQrButtonGroup.add(microQrUserSize);
        microQrUserSize.setText("Adjust Size To:");
        microQrUserSize.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                microQrUserSizeActionPerformed(evt);
            }
        });

        microQrButtonGroup.add(microQrUserEcc);
        microQrUserEcc.setText("Add Minimum Error Correction:");
        microQrUserEcc.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                microQrUserEccActionPerformed(evt);
            }
        });

        microQrUserSizeCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "11 X 11 (Version M1)", "13 X 13 (Version M2)", "15 X 15 (Version M3)", "17 X 17 (Version M4)" }));
        microQrUserSizeCombo.setEnabled(false);
        microQrUserSizeCombo.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                microQrUserSizeComboActionPerformed(evt);
            }
        });

        microQrUserEccCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Approx 20% (Level L)", "Approx 37% (Level M)", "Approx 55% (Level Q)" }));
        microQrUserEccCombo.setEnabled(false);
        microQrUserEccCombo.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                microQrUserEccComboActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout microQrPanelLayout = new javax.swing.GroupLayout(microQrPanel);
        microQrPanel.setLayout(microQrPanelLayout);
        microQrPanelLayout.setHorizontalGroup(
            microQrPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(microQrPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(microQrPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(microQrAutoSize, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(microQrUserSize, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(microQrUserEcc, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(14, 14, 14)
                .addGroup(microQrPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(microQrUserSizeCombo, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(microQrUserEccCombo, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        microQrPanelLayout.setVerticalGroup(
            microQrPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(microQrPanelLayout.createSequentialGroup()
                .addComponent(microQrAutoSize)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(microQrPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(microQrUserSize)
                    .addComponent(microQrUserSizeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(microQrPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(microQrUserEcc)
                    .addComponent(microQrUserEccCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        msiPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createTitledBorder("MSI Plessey"), "MSI Plessey"));

        msiCheckDigitLabel.setText("Check Digit:");

        msiCheckDigitCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "None", "Mod-10", "Mod-10 & Mod-10", "Mod-11", "Mod-11 & Mod-10" }));
        msiCheckDigitCombo.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                msiCheckDigitComboActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout msiPanelLayout = new javax.swing.GroupLayout(msiPanel);
        msiPanel.setLayout(msiPanelLayout);
        msiPanelLayout.setHorizontalGroup(
            msiPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(msiPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(msiCheckDigitLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(msiCheckDigitCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        msiPanelLayout.setVerticalGroup(
            msiPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(msiPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(msiCheckDigitLabel)
                .addComponent(msiCheckDigitCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pdfPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("PDF417"));

        pdfDataColumnsLabel.setText("Number of Data Columns:");

        pdfEccLabel.setText("Error Correction Capacity:");

        pdfColumnsCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Automatic", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20" }));
        pdfColumnsCombo.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pdfColumnsComboActionPerformed(evt);
            }
        });

        pdfEccCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Automatic", "2 words", "4 words", "8 words", "16 words", "32 words", "64 words", "128 words", "256 words", "512 words" }));
        pdfEccCombo.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pdfEccComboActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pdfPanelLayout = new javax.swing.GroupLayout(pdfPanel);
        pdfPanel.setLayout(pdfPanelLayout);
        pdfPanelLayout.setHorizontalGroup(
            pdfPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pdfPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pdfPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(pdfDataColumnsLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pdfEccLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(pdfPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pdfColumnsCombo, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pdfEccCombo, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        pdfPanelLayout.setVerticalGroup(
            pdfPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pdfPanelLayout.createSequentialGroup()
                .addGroup(pdfPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(pdfDataColumnsLabel)
                    .addComponent(pdfColumnsCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pdfPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(pdfEccLabel)
                    .addComponent(pdfEccCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        qrPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("QR Code"));

        qrButtonGroup.add(qrAutoSize);
        qrAutoSize.setSelected(true);
        qrAutoSize.setText("Automatic Resizing");
        qrAutoSize.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                qrAutoSizeActionPerformed(evt);
            }
        });

        qrButtonGroup.add(qrUserSize);
        qrUserSize.setText("Adjust Size To:");
        qrUserSize.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                qrUserSizeActionPerformed(evt);
            }
        });

        qrButtonGroup.add(qrUserEcc);
        qrUserEcc.setText("Add Minimum Error Correction:");
        qrUserEcc.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                qrUserEccActionPerformed(evt);
            }
        });

        qrUserSizeCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "21 X 21 (Version 1)", "25 X 25 (Version 2)", "29 X 29 (Version 3)", "33 X 33 (Version 4)", "37 X 37 (Version 5)", "41 X 41 (Version 6)", "45 X 45 (Version 7)", "49 X 49 (Version 8)", "53 X 53 (Version 9)", "57 X 57 (Version 10)", "61 X 61 (Version 11)", "65 X 65 (Version 12)", "69 X 69 (Version 13)", "73 X 73 (Version 14)", "77 X 77 (Version 15)", "81 X 81 (Version 16)", "85 X 85 (Version 17)", "89 X 89 (Version 18)", "93 X 93 (Version 19)", "97 X 97 (Version 20)", "101 X 101 (Version 21)", "105 X 105 (Version 22)", "109 X 109 (Version 23)", "113 X 113 (Version 24)", "117 X 117 (Version 25)", "121 X 121 (Version 26)", "125 X 125 (Version 27)", "129 X 129 (Version 28)", "133 X 133 (Version 29)", "137 X 137 (Version 30)", "141 X 141 (Version 31)", "145 X 145 (Version 32)", "149 X 149 (Version 33)", "153 X 153 (Version 34)", "157 X 157 (Version 35)", "161 X 161 (Version 36)", "165 X 165 (Version 37)", "169 X 169 (Version 38)", "173 X 173 (Version 39)", "177 X 177 (Version 40)" }));
        qrUserSizeCombo.setEnabled(false);
        qrUserSizeCombo.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                qrUserSizeComboActionPerformed(evt);
            }
        });

        qrUserEccCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Approx 20% (Level L)", "Approx 37% (Level M)", "Approx 55% (Level Q)", "Approx 65% (Level H)" }));
        qrUserEccCombo.setEnabled(false);
        qrUserEccCombo.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                qrUserEccComboActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout qrPanelLayout = new javax.swing.GroupLayout(qrPanel);
        qrPanel.setLayout(qrPanelLayout);
        qrPanelLayout.setHorizontalGroup(
            qrPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(qrPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(qrPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(qrAutoSize, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(qrUserSize, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(qrUserEcc, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(qrPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(qrUserSizeCombo, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(qrUserEccCombo, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        qrPanelLayout.setVerticalGroup(
            qrPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(qrPanelLayout.createSequentialGroup()
                .addComponent(qrAutoSize)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(qrPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(qrUserSize)
                    .addComponent(qrUserSizeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(qrPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(qrUserEcc)
                    .addComponent(qrUserEccCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        compositePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Composite Component"));

        compositeModeLabel.setText("Composite Component Mode:");

        compositeUserMode.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Automatic", "CC-A", "CC-B", "CC-C" }));
        compositeUserMode.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                compositeUserModeActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout compositePanelLayout = new javax.swing.GroupLayout(compositePanel);
        compositePanel.setLayout(compositePanelLayout);
        compositePanelLayout.setHorizontalGroup(
            compositePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(compositePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(compositeModeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(compositeUserMode, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        compositePanelLayout.setVerticalGroup(
            compositePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(compositePanelLayout.createSequentialGroup()
                .addComponent(compositeModeLabel)
                .addGap(0, 0, Short.MAX_VALUE))
            .addComponent(compositeUserMode, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout attributeScrollPanelLayout = new javax.swing.GroupLayout(attributeScrollPanel);
        attributeScrollPanel.setLayout(attributeScrollPanelLayout);
        attributeScrollPanelLayout.setHorizontalGroup(
            attributeScrollPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(aztecPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(channelPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(code39Panel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(codeOnePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(datamatrixPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(maxicodePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(microPdfPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(microQrPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(msiPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(pdfPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(qrPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(compositePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(gridmatrixPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(databarPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        attributeScrollPanelLayout.setVerticalGroup(
            attributeScrollPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(attributeScrollPanelLayout.createSequentialGroup()
                .addComponent(aztecPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(channelPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(code39Panel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(codeOnePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(compositePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(datamatrixPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(gridmatrixPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(databarPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(maxicodePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(microPdfPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(microQrPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(msiPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pdfPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(qrPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(168, Short.MAX_VALUE))
        );

        attributeScrollPane.setViewportView(attributeScrollPanel);

        chkReaderInit.setText("Add reader initialisation");
        chkReaderInit.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkReaderInitActionPerformed(evt);
            }
        });

        cmbHrtPosition.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Below symbol", "Above symbol", "Don't display" }));
        cmbHrtPosition.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbHrtPositionActionPerformed(evt);
            }
        });

        txtShowHrt.setText("Show human readable text:");

        lblXDimension.setText("X Dimension:");

        lblXDimensionPixels.setText("Pixels");

        lblBorderWidth.setText("Border Width:");

        lblWhitespaceWidth.setText("Whitespace Width:");

        txtXDimension.setText("4");
        txtXDimension.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtXDimensionFocusLost(evt);
            }
        });

        txtBorderWidth.setText("5");
        txtBorderWidth.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtBorderWidthFocusLost(evt);
            }
        });

        txtWhitespaceWidth.setText("0");
        txtWhitespaceWidth.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtWhitespaceWidthFocusLost(evt);
            }
        });

        javax.swing.GroupLayout attributePanelLayout = new javax.swing.GroupLayout(attributePanel);
        attributePanel.setLayout(attributePanelLayout);
        attributePanelLayout.setHorizontalGroup(
            attributePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(attributeScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 846, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, attributePanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(inkButton, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(paperButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(resetColourButton, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(122, 122, 122))
            .addGroup(attributePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(attributePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(attributePanelLayout.createSequentialGroup()
                        .addComponent(encodeInfoArea, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(attributePanelLayout.createSequentialGroup()
                        .addComponent(txtShowHrt, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(cmbHrtPosition, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(55, 55, 55)
                        .addComponent(chkReaderInit, javax.swing.GroupLayout.PREFERRED_SIZE, 230, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(30, 30, 30))
                    .addGroup(attributePanelLayout.createSequentialGroup()
                        .addComponent(lblXDimension)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtXDimension, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(3, 3, 3)
                        .addComponent(lblXDimensionPixels)
                        .addGap(81, 81, 81)
                        .addComponent(lblBorderWidth)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtBorderWidth, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lblWhitespaceWidth)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtWhitespaceWidth, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(71, 71, 71))))
        );
        attributePanelLayout.setVerticalGroup(
            attributePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(attributePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(attributePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(inkButton)
                    .addComponent(paperButton)
                    .addComponent(resetColourButton))
                .addGroup(attributePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(attributePanelLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(attributePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(chkReaderInit)
                            .addComponent(txtShowHrt)))
                    .addGroup(attributePanelLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbHrtPosition, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(9, 9, 9)
                .addGroup(attributePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblWhitespaceWidth, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(attributePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblXDimension)
                        .addComponent(lblXDimensionPixels)
                        .addComponent(lblBorderWidth)
                        .addComponent(txtXDimension, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtBorderWidth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtWhitespaceWidth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(attributeScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 345, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 38, Short.MAX_VALUE)
                .addComponent(encodeInfoArea, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        mainTabs.addTab("Attributes", attributePanel);

        exitButton.setText("Exit");
        exitButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitButtonActionPerformed(evt);
            }
        });

        saveButton.setText("Save");
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        aboutButton.setText("About");
        aboutButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(symbolPane, javax.swing.GroupLayout.PREFERRED_SIZE, 209, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(aboutButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(saveButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(exitButton))
                    .addComponent(mainTabs))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(symbolPane)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(mainTabs)
                        .addGap(11, 11, 11)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(exitButton)
                            .addComponent(saveButton)
                            .addComponent(aboutButton))
                        .addGap(5, 5, 5)))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void directoryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_directoryButtonActionPerformed
        // Select save directory
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Directory Select");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            folderField.setText(chooser.getSelectedFile().getAbsolutePath()
                    + File.separator);
        }
    }//GEN-LAST:event_directoryButtonActionPerformed

    private void aboutButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutButtonActionPerformed
        // Show information about Okapi
        AboutOkapi az = new AboutOkapi();
        az.setLocationRelativeTo(this);
        az.setVisible(true);
    }//GEN-LAST:event_aboutButtonActionPerformed

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        // Save the current barcode
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            SaveSymbol saveSymbol = new SaveSymbol();

            saveSymbol.removeAll();

            saveSymbol.setSize(saveSymbol.getPreferredSize());
            saveSymbol.setBorder(BorderFactory.createEmptyBorder());
            saveSymbol.setBackground(paperColour);
            try {
                SaveImage saveImage = new SaveImage();
                saveImage.save(file, saveSymbol);
            } catch (IOException e) {
                System.out.println("Cannot write to file " + fileChooser.getSelectedFile().toString() + ": " + e.getMessage());
            }
        }
    }//GEN-LAST:event_saveButtonActionPerformed

    private void exitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitButtonActionPerformed
        // Leave the program
        dispose();
    }//GEN-LAST:event_exitButtonActionPerformed

    private void loadDataButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadDataButtonActionPerformed
        // Load more data from file
        MoreData addme = new MoreData();
        addme.setLocationRelativeTo(this);
        addme.setVisible(true);
    }//GEN-LAST:event_loadDataButtonActionPerformed

    private void resetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetButtonActionPerformed
        // Clear sequence text
        sequenceArea.setText("");
    }//GEN-LAST:event_resetButtonActionPerformed

    private void createButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createButtonActionPerformed
        // Generate sequence
        int start;
        int stop;
        int step;
        int i;
        String resultant = "";

        try {
            start = Integer.parseInt(startField.getText());
            stop = Integer.parseInt(stopField.getText());
            step = Integer.parseInt(incrementField.getText());
        } catch (NumberFormatException nfe) {
            System.out.println("Invalid input value");
            return;
        }

        if (step <= 0) {
            System.out.println("Invalid increment value");
            return;
        }

        if (start >= stop) {
            System.out.println("Invalid sequence");
            return;
        }

        for (i = start; i <= stop; i += step) {
            resultant += applyFormat(Integer.toString(i));
            resultant += '\n';
        }

        sequenceArea.setText(resultant);
    }//GEN-LAST:event_createButtonActionPerformed

    private void batchFileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_batchFileButtonActionPerformed
        // Load a batch file
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                        new FileInputStream(file), "UTF8"))) {
                String str;

                sequenceArea.setText("");

                while ((str = in.readLine()) != null) {
                    sequenceArea.setText(sequenceArea.getText() + str + '\n');
                }
            } catch (UnsupportedEncodingException e) {
                System.out.println("Encoding exception");
            } catch (IOException e) {
                System.out.println("Cannot read from file" + fileChooser.getSelectedFile().toString());
            } catch (Exception e) {
                System.out.println("Exception");
            }

        }
    }//GEN-LAST:event_batchFileButtonActionPerformed

    private void runBatchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runBatchButtonActionPerformed
        // Perform batch encoding
        String thisData = "";
        String extension;
        String fullFileName;
        String outputCount;
        int inputCount = 0;
        String errorLog;
        String progressLog = "";
        int i, k;
        int lineCount = 0;
        double percentage;
        int countLength, currentLength;

        errorLog = "Starting batch process..." + '\n';
        batchOutputArea.setText(errorLog);

        switch(outFormatCombo.getSelectedIndex()) {
            case 0:
                extension = ".png";
                break;
            case 1:
                extension = ".jpg";
                break;
            case 2:
                extension = ".gif";
                break;
            case 3:
                extension = ".bmp";
                break;
            case 4:
                extension = ".svg";
                break;
            case 5:
            default:
                extension = ".eps";
                break;
        }

        for(i = 0; i < sequenceArea.getText().length(); i++) {
            if (sequenceArea.getText().charAt(i) == '\n') {
                inputCount++;
            }
        }

        countLength = Integer.toString(inputCount + 1).length();

        for(i = 0; i < sequenceArea.getText().length(); i++) {
            if (sequenceArea.getText().charAt(i) == '\n') {
                currentLength = Integer.toString(lineCount + 1).length();
                outputCount = "";
                for (k = 0; k < (countLength - currentLength); k++) {
                    // Add leading zeroes to file name
                    outputCount += "0";
                }
                outputCount += Integer.toString(lineCount + 1);
                if (!thisData.equals("")) {
                    if (outFilenameCombo.getSelectedIndex() == 0) {
                        fullFileName = folderField.getText() + prefixField.getText()
                            + osFriendly(thisData) + extension;
                    } else {
                        fullFileName = folderField.getText()
                                + prefixField.getText() + outputCount + extension;
                    }
                    File file = new File(fullFileName);
                    SaveSymbol saveSymbol = new SaveSymbol();

                    saveSymbol.removeAll();

                    saveSymbol.setSize(saveSymbol.getPreferredSize());
                    saveSymbol.setBorder(BorderFactory.createEmptyBorder());
                    saveSymbol.setBackground(paperColour);
                    try {
                        SaveImage saveImage = new SaveImage();
                        if (errorLabel.getText().isEmpty()) {
                            saveImage.save(file, saveSymbol);
                        } else {
                            errorLog += errorLabel.getText() + " at line " + (lineCount + 1) + '\n';
                        }
                    } catch (IOException e) {
                        errorLog += "I/O exception writing to " + fullFileName
                                + " at line " + (lineCount + 1) + ": " + e.getMessage() + '\n';
                    }
                }
                lineCount++;
                percentage = (double)(lineCount) / (double)(inputCount);
                percentage *= 100;
                progressLog = "Completed line " + lineCount + " of "
                        + inputCount + " (" + (int)(percentage) + "% done)";
                batchOutputArea.setText(errorLog + progressLog);
                thisData = "";
            } else {
                char currentChar = sequenceArea.getText().charAt(i);

                if ((currentChar != 0x0D) && (currentChar != 0x0A)) {
                    thisData += sequenceArea.getText().charAt(i);
                }
            }
        }
        progressLog += '\n' + "Finished!";
        batchOutputArea.setText(errorLog + progressLog);
    }//GEN-LAST:event_runBatchButtonActionPerformed

    private String osFriendly(String data) {
        /* Allow only permissable characters to be used in filenames */

        String dataCopy = "";
        char c;
        boolean valid;

        for (int i = 0; i < data.length(); i++) {
            c = data.charAt(i);

            valid = true;
            if ((c > 0x00) && (c < 0x20)) {
                valid = false;
            }
            if ((c > 0x7E) && (c < 0xA0)) {
                valid = false;
            }

            switch (c) {
                case '/':
                case '\\':
                case '?':
                case '%':
                case '*':
                case ':':
                case '|':
                case '"':
                case '<':
                case '>':
                case '.':
                case ',':
                case '=':
                case '+':
                case ']':
                case '[':
                case '!':
                case '@':
                    valid = false;
            }

            if (valid) {
                dataCopy += c;
            }

        }

        return dataCopy;
    }

    private void inkButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inkButtonActionPerformed
        // Select colour for foreground
        final JColorChooser chooser = new JColorChooser();
        ActionListener okListener = new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent evt) {
            setInk(chooser.getColor());
          }
        };

        boolean modal = false;

        JDialog dialog = JColorChooser.createDialog(this, "Ink Colour", modal,
                chooser, okListener, null);
        dialog.setVisible(true);

        dialog.addWindowListener(new WindowAdapter() {
            @Override
          public void windowClosing(WindowEvent evt) {
            setInk(chooser.getColor());
            dispose();
          }
        });
    }//GEN-LAST:event_inkButtonActionPerformed

    private void paperButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_paperButtonActionPerformed
        // Select colour for background
        final JColorChooser chooser = new JColorChooser();
        ActionListener okListener = new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent evt) {
            setPaper(chooser.getColor());
          }
        };

        boolean modal = false;

        JDialog dialog = JColorChooser.createDialog(this, "Paper Colour",
                modal, chooser, okListener, null);
        dialog.setVisible(true);

        dialog.addWindowListener(new WindowAdapter() {
            @Override
          public void windowClosing(WindowEvent evt) {
            setPaper(chooser.getColor());
            dispose();
          }
        });
    }//GEN-LAST:event_paperButtonActionPerformed

    private void resetColourButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetColourButtonActionPerformed
        // Put colours back to default black-on-white
        setInk(Color.BLACK);
        setPaper(Color.WHITE);
    }//GEN-LAST:event_resetColourButtonActionPerformed

    private void aztecUserSizeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aztecUserSizeActionPerformed
        // TODO add your handling code here:
        aztecUserSizeCombo.setEnabled(true);
        aztecUserEccCombo.setEnabled(false);
        encodeData();
    }//GEN-LAST:event_aztecUserSizeActionPerformed

    private void aztecAutoSizeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aztecAutoSizeActionPerformed
        // TODO add your handling code here:
        aztecUserSizeCombo.setEnabled(false);
        aztecUserEccCombo.setEnabled(false);
        encodeData();

    }//GEN-LAST:event_aztecAutoSizeActionPerformed

    private void aztecUserEccActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aztecUserEccActionPerformed
        // TODO add your handling code here:
        aztecUserSizeCombo.setEnabled(false);
        aztecUserEccCombo.setEnabled(true);
        encodeData();
    }//GEN-LAST:event_aztecUserEccActionPerformed

    private void channelChannelsComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_channelChannelsComboActionPerformed
        // TODO add your handling code here:
        encodeData();
    }//GEN-LAST:event_channelChannelsComboActionPerformed

    private void dataMatrixSquareOnlyCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dataMatrixSquareOnlyCheckActionPerformed
        // TODO add your handling code here:
        encodeData();
    }//GEN-LAST:event_dataMatrixSquareOnlyCheckActionPerformed

    private void gridmatrixAutoSizeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gridmatrixAutoSizeActionPerformed
        // TODO add your handling code here:
        gridmatrixUserSizeCombo.setEnabled(false);
        gridmatrixUserEccCombo.setEnabled(false);
        encodeData();
    }//GEN-LAST:event_gridmatrixAutoSizeActionPerformed

    private void gridmatrixUserSizeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gridmatrixUserSizeActionPerformed
        // TODO add your handling code here:
        gridmatrixUserSizeCombo.setEnabled(true);
        gridmatrixUserEccCombo.setEnabled(false);
        encodeData();
    }//GEN-LAST:event_gridmatrixUserSizeActionPerformed

    private void gridmatrixUserEccActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gridmatrixUserEccActionPerformed
        // TODO add your handling code here:
        gridmatrixUserSizeCombo.setEnabled(false);
        gridmatrixUserEccCombo.setEnabled(true);
        encodeData();
    }//GEN-LAST:event_gridmatrixUserEccActionPerformed

    private void microQrAutoSizeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_microQrAutoSizeActionPerformed
        // TODO add your handling code here:
        microQrUserSizeCombo.setEnabled(false);
        microQrUserEccCombo.setEnabled(false);
        encodeData();
    }//GEN-LAST:event_microQrAutoSizeActionPerformed

    private void microQrUserSizeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_microQrUserSizeActionPerformed
        // TODO add your handling code here:
        microQrUserSizeCombo.setEnabled(true);
        microQrUserEccCombo.setEnabled(false);
        encodeData();
    }//GEN-LAST:event_microQrUserSizeActionPerformed

    private void microQrUserEccActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_microQrUserEccActionPerformed
        // TODO add your handling code here:
        microQrUserSizeCombo.setEnabled(false);
        microQrUserEccCombo.setEnabled(true);
        encodeData();
    }//GEN-LAST:event_microQrUserEccActionPerformed

    private void qrAutoSizeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_qrAutoSizeActionPerformed
        // TODO add your handling code here:
        qrUserSizeCombo.setEnabled(false);
        qrUserEccCombo.setEnabled(false);
        encodeData();
    }//GEN-LAST:event_qrAutoSizeActionPerformed

    private void qrUserSizeComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_qrUserSizeComboActionPerformed
        // TODO add your handling code here:
        encodeData();
    }//GEN-LAST:event_qrUserSizeComboActionPerformed

    private void qrUserSizeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_qrUserSizeActionPerformed
        // TODO add your handling code here:
        qrUserSizeCombo.setEnabled(true);
        qrUserEccCombo.setEnabled(false);
        encodeData();
    }//GEN-LAST:event_qrUserSizeActionPerformed

    private void qrUserEccActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_qrUserEccActionPerformed
        // TODO add your handling code here:
        qrUserSizeCombo.setEnabled(false);
        qrUserEccCombo.setEnabled(true);
        encodeData();
    }//GEN-LAST:event_qrUserEccActionPerformed

    private void gridmatrixUserSizeComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gridmatrixUserSizeComboActionPerformed
        // TODO add your handling code here:
        encodeData();
    }//GEN-LAST:event_gridmatrixUserSizeComboActionPerformed

    private void maxiEncodingModeComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_maxiEncodingModeComboActionPerformed
        // TODO add your handling code here:
        if (maxiEncodingModeCombo.getSelectedIndex() == 0 || maxiEncodingModeCombo.getSelectedIndex() == 1) {
            maxiPrimaryData.setEnabled(true);
            maxiPrimaryDataLabel.setEnabled(true);
        } else {
            maxiPrimaryData.setEnabled(false);
            maxiPrimaryDataLabel.setEnabled(false);
        }
        encodeData();
    }//GEN-LAST:event_maxiEncodingModeComboActionPerformed

    private void addCompositeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addCompositeButtonActionPerformed
        // TODO add your handling code here:
        AddComposite addme = new AddComposite();
        addme.setLocationRelativeTo(this);
        addme.setVisible(true);
    }//GEN-LAST:event_addCompositeButtonActionPerformed

    private void aztecUserSizeComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aztecUserSizeComboActionPerformed
        // TODO add your handling code here:
        encodeData();
    }//GEN-LAST:event_aztecUserSizeComboActionPerformed

    private void aztecUserEccComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aztecUserEccComboActionPerformed
        // TODO add your handling code here:
        encodeData();
    }//GEN-LAST:event_aztecUserEccComboActionPerformed

    private void code39CheckComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_code39CheckComboActionPerformed
        // TODO add your handling code here:
        encodeData();
    }//GEN-LAST:event_code39CheckComboActionPerformed

    private void codeOneSizeComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_codeOneSizeComboActionPerformed
        // TODO add your handling code here:
        encodeData();
    }//GEN-LAST:event_codeOneSizeComboActionPerformed

    private void databarColumnsComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_databarColumnsComboActionPerformed
        // TODO add your handling code here:
        encodeData();
    }//GEN-LAST:event_databarColumnsComboActionPerformed

    private void dataMatrixSizeComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dataMatrixSizeComboActionPerformed
        // TODO add your handling code here:
        encodeData();
    }//GEN-LAST:event_dataMatrixSizeComboActionPerformed

    private void gridmatrixUserEccComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gridmatrixUserEccComboActionPerformed
        // TODO add your handling code here:
        encodeData();
    }//GEN-LAST:event_gridmatrixUserEccComboActionPerformed

    private void maxiPrimaryDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_maxiPrimaryDataActionPerformed
        // TODO add your handling code here:
        encodeData();
    }//GEN-LAST:event_maxiPrimaryDataActionPerformed

    private void microPdfColumnsComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_microPdfColumnsComboActionPerformed
        // TODO add your handling code here:
        encodeData();
    }//GEN-LAST:event_microPdfColumnsComboActionPerformed

    private void microQrUserSizeComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_microQrUserSizeComboActionPerformed
        // TODO add your handling code here:
        encodeData();
    }//GEN-LAST:event_microQrUserSizeComboActionPerformed

    private void microQrUserEccComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_microQrUserEccComboActionPerformed
        // TODO add your handling code here:
        encodeData();
    }//GEN-LAST:event_microQrUserEccComboActionPerformed

    private void msiCheckDigitComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_msiCheckDigitComboActionPerformed
        // TODO add your handling code here:
        encodeData();
    }//GEN-LAST:event_msiCheckDigitComboActionPerformed

    private void pdfColumnsComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pdfColumnsComboActionPerformed
        // TODO add your handling code here:
        encodeData();
    }//GEN-LAST:event_pdfColumnsComboActionPerformed

    private void pdfEccComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pdfEccComboActionPerformed
        // TODO add your handling code here:
        encodeData();
    }//GEN-LAST:event_pdfEccComboActionPerformed

    private void qrUserEccComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_qrUserEccComboActionPerformed
        // TODO add your handling code here:
        encodeData();
    }//GEN-LAST:event_qrUserEccComboActionPerformed

    private void useGS1CheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useGS1CheckActionPerformed
        // TODO add your handling code here:
        encodeData();
    }//GEN-LAST:event_useGS1CheckActionPerformed

    private void compositeUserModeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_compositeUserModeActionPerformed
        // TODO add your handling code here:
        encodeData();
    }//GEN-LAST:event_compositeUserModeActionPerformed

    private void useCompositeCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useCompositeCheckActionPerformed
        // TODO add your handling code here:
        if (useCompositeCheck.isSelected()) {
            compositeLabel.setEnabled(true);
            compositeInputField.setEnabled(true);
            addCompositeButton.setEnabled(true);
        } else {
            compositeLabel.setEnabled(false);
            compositeInputField.setEnabled(false);
            addCompositeButton.setEnabled(false);
        }
        encodeData();
    }//GEN-LAST:event_useCompositeCheckActionPerformed

    private void cmbHrtPositionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbHrtPositionActionPerformed
        // TODO add your handling code here:
        encodeData();
    }//GEN-LAST:event_cmbHrtPositionActionPerformed

    private void chkReaderInitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkReaderInitActionPerformed
        // TODO add your handling code here:
        encodeData();
    }//GEN-LAST:event_chkReaderInitActionPerformed

    private void txtBorderWidthFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtBorderWidthFocusLost
        // TODO: the name (and label?) of this text box no longer matches its purpose
        if (txtBorderWidth.getText().matches("[0-9]+")) {
            quietZoneHorizontal = Integer.parseInt(txtBorderWidth.getText());
            encodeData();
        } else {
            txtBorderWidth.setText(String.valueOf(quietZoneHorizontal));
        }
    }//GEN-LAST:event_txtBorderWidthFocusLost

    private void txtXDimensionFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtXDimensionFocusLost
        if (txtXDimension.getText().matches("[0-9]+")) {
            if (Integer.parseInt(txtXDimension.getText()) != 0) {
                moduleWidth = Integer.parseInt(txtXDimension.getText());
                encodeData();
            } else {
                txtXDimension.setText(String.valueOf(moduleWidth));
            }
        } else {
            txtXDimension.setText(String.valueOf(moduleWidth));
        }
    }//GEN-LAST:event_txtXDimensionFocusLost

    private void txtWhitespaceWidthFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtWhitespaceWidthFocusLost
        // TODO: the name (and label?) of this text box no longer matches its purpose
        if (txtWhitespaceWidth.getText().matches("[0-9]+")) {
            quietZoneVertical = Integer.parseInt(txtWhitespaceWidth.getText());
            encodeData();
        } else {
            txtWhitespaceWidth.setText(String.valueOf(quietZoneVertical));
        }
    }//GEN-LAST:event_txtWhitespaceWidthFocusLost

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException|InstantiationException|IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(OkapiUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(OkapiUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new OkapiUI().setVisible(true);
            }
        });
    }

    public static void setInk(Color input) {
        inkColour = input;
    }

    public static void setPaper(Color input) {
        paperColour = input;
    }

    @Override
    public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                symbolTree.getLastSelectedPathComponent();
        Object nodeInfo;

        if (node != null) {
            nodeInfo = node.getUserObject();
            if (node.isLeaf()) {
                selectedSymbol = (SymbolType)nodeInfo;
                dataInput = dataInputField.getText();
                compositeInput = compositeInputField.getText();

                switch(selectedSymbol.symbology) {
                    case AZTEC:
                    case CODE_128:
                    case CODE16K:
                    case CODE49:
                    case CODE_ONE:
                    case DATAMATRIX:
                    case QR:
                        useGS1Check.setEnabled(true);
                        break;
                    default:
                        useGS1Check.setEnabled(false);
                }

                switch(selectedSymbol.symbology) {
                    case EAN:
                    case CODE_128:
                    case UPC_E:
                    case DB14_STACKED_OMNIDIRECT:
                    case DB14_STACKED:
                    case DB_LIMITED:
                    case DB14:
                    case DB_EXPANDED:
                    case UPC_A:
                    case DB_EXPANDED_STACKED:
                        useCompositeCheck.setEnabled(true);
                        if (useCompositeCheck.isSelected()) {
                            compositeLabel.setEnabled(true);
                            compositeInputField.setEnabled(true);
                            addCompositeButton.setEnabled(true);
                        }
                        break;
                    default:
                        useCompositeCheck.setEnabled(false);
                        compositeLabel.setEnabled(false);
                        compositeInputField.setEnabled(false);
                        addCompositeButton.setEnabled(false);
                }

                encodeData();
            } else {
                // TODO: Output an error message
            }
        }
    }

    public void encodeData() {
        double bWidth;
        double bHeight;

        DrawSymbol drawSymbol = new DrawSymbol();
        topPanel.removeAll();

        drawSymbol.removeAll();
        errorLabel.setText("");

        if (dataInput.isEmpty()) {
            errorLabel.setText("No input data");
            topPanel.add(errorLabel);
            topPanel.updateUI();
            return;
        }

        try {
            symbol = getNewSymbol();
        } catch (OkapiException e) {
            errorLabel.setText(e.getMessage());
            topPanel.add(errorLabel);
            topPanel.updateUI();
            return;
        }

        bWidth = (double) topPanel.getWidth() / symbol.getWidth();
        bHeight = (double) topPanel.getHeight() / symbol.getHeight();

        if (bWidth < bHeight) {
            factor = (int) bWidth;
        } else {
            factor = (int) bHeight;
        }

        drawSymbol.setBorder(BorderFactory.createEmptyBorder());
        drawSymbol.setBackground(paperColour);
        drawSymbol.setBounds((topPanel.getWidth() - drawSymbol.getPreferredSize().width) / 2,
                (topPanel.getHeight() - drawSymbol.getPreferredSize().height) / 2,
                drawSymbol.getPreferredSize().width,
                drawSymbol.getPreferredSize().height);
        topPanel.add(drawSymbol);
        encodeInfoArea.setText(symbol.getEncodeInfo());
        topPanel.updateUI();
        pack();
    }

    private String applyFormat(String rawNumber) {
        String format = formatField.getText();
        String adjusted = "";
        String reversed = "";

        int formatLength, inputLength, i, position;
	char formatChar;

	inputLength = rawNumber.length();
	formatLength = format.length();

	position = inputLength;

	for(i = formatLength; i > 0; i--) {
            formatChar = format.charAt(i - 1);
            switch(formatChar) {
                case '#':
                    if (position > 0) {
                        adjusted += rawNumber.charAt(position - 1);
                        position--;
                    } else {
                        adjusted += ' ';
                    }
                    break;
                case '$':
                    if (position > 0) {
                        adjusted += rawNumber.charAt(position - 1);
                        position--;
                    } else {
                        adjusted += '0';
                    }
                    break;
                case '*':
                    if (position > 0) {
                        adjusted += rawNumber.charAt(position - 1);
                        position--;
                    } else {
                        adjusted += '*';
                    }
                    break;
                default:
                    adjusted += formatChar;
                    break;
            }
	}

	for(i = formatLength; i > 0; i--) {
            reversed += adjusted.charAt(i - 1);
	}

        return reversed;
    }

    private HumanReadableLocation getHrtLoc() {
        HumanReadableLocation temp = HumanReadableLocation.BOTTOM;

        switch(cmbHrtPosition.getSelectedIndex()) {
            case 0:
                temp = HumanReadableLocation.BOTTOM;
                break;
            case 1:
                temp = HumanReadableLocation.TOP;
                break;
            case 2:
                temp = HumanReadableLocation.NONE;
                break;
        }

        return temp;
    }

    private void setUniversals(Symbol symbol) {
        symbol.setModuleWidth(moduleWidth);
        symbol.setQuietZoneHorizontal(quietZoneHorizontal);
        symbol.setQuietZoneVertical(quietZoneVertical);
    }

    private Symbol getNewSymbol() throws OkapiException {

        boolean readerInit = chkReaderInit.isSelected();
        HumanReadableLocation hrtLoc = getHrtLoc();

        if (selectedSymbol == null) {
            throw new OkapiException("No symbology selected");
        }

        if ((useCompositeCheck.isEnabled() && useCompositeCheck.isSelected()) &&
                (!(compositeInput.isEmpty()))) {
            // Create a composite symbol
            Composite composite = new Composite();
            switch (selectedSymbol.symbology) {
                case UPC_A:
                    composite.setSymbology(Composite.LinearEncoding.UPCA);
                    break;
                case UPC_E:
                    composite.setSymbology(Composite.LinearEncoding.UPCE);
                    break;
                case EAN:
                    composite.setSymbology(Composite.LinearEncoding.EAN);
                    break;
                case CODE_128:
                    composite.setSymbology(Composite.LinearEncoding.CODE_128);
                    break;
                case DB14:
                    composite.setSymbology(Composite.LinearEncoding.DATABAR_14);
                    break;
                case DB14_STACKED_OMNIDIRECT:
                    composite.setSymbology(Composite.LinearEncoding.DATABAR_14_STACK_OMNI);
                    break;
                case DB14_STACKED:
                    composite.setSymbology(Composite.LinearEncoding.DATABAR_14_STACK);
                    break;
                case DB_LIMITED:
                    composite.setSymbology(Composite.LinearEncoding.DATABAR_LIMITED);
                    break;
                case DB_EXPANDED:
                    composite.setSymbology(Composite.LinearEncoding.DATABAR_EXPANDED);
                    break;
                case DB_EXPANDED_STACKED:
                    composite.setSymbology(Composite.LinearEncoding.DATABAR_EXPANDED_STACK);
                    break;
            }
            composite.setLinearContent(dataInput);
            switch(compositeUserMode.getSelectedIndex()) {
                case 1:
                    composite.setPreferredMode(Composite.CompositeMode.CC_A);
                    break;
                case 2:
                    composite.setPreferredMode(Composite.CompositeMode.CC_B);
                    break;
                case 3:
                    composite.setPreferredMode(Composite.CompositeMode.CC_C);
                    break;
            }
            setUniversals(composite);
            composite.setContent(compositeInput);
            return composite;
        } else {
            // Symbol is not composite
            switch (selectedSymbol.symbology) {
            case UPC_A:
                Upc upca = new Upc();
                upca.setMode(Upc.Mode.UPCA);
                upca.setContent(dataInput);
                setUniversals(upca);
                return upca;
            case UPC_E:
                Upc upce = new Upc();
                upce.setMode(Upc.Mode.UPCE);
                upce.setContent(dataInput);
                setUniversals(upce);
                return upce;
            case EAN:
                Ean ean = new Ean();
                if (eanCalculateVersion() == 8) {
                    ean.setMode(Ean.Mode.EAN8);
                } else {
                    ean.setMode(Ean.Mode.EAN13);
                }
                ean.setContent(dataInput);
                setUniversals(ean);
                return ean;
            case ITF14:
                Code2Of5 itf14 = new Code2Of5();
                itf14.setMode(ToFMode.ITF14);
                itf14.setHumanReadableLocation(hrtLoc);
                itf14.setContent(dataInput);
                setUniversals(itf14);
                return itf14;
            case CODE_128:
            case CODE_128_HIBC:
                Code128 code128 = new Code128();
                code128.unsetCc();
                if (useGS1Check.isSelected()) {
                    code128.setDataType(Symbol.DataType.GS1);
                }
                if (selectedSymbol.symbology == SymbolType.Encoding.CODE_128_HIBC) {
                    code128.setDataType(Symbol.DataType.HIBC);
                }
                code128.setReaderInit(readerInit);
                code128.setHumanReadableLocation(hrtLoc);
                code128.setContent(dataInput);
                setUniversals(code128);
                return code128;
            case NVE18:
                Nve18 nve18 = new Nve18();
                nve18.setHumanReadableLocation(hrtLoc);
                nve18.setContent(dataInput);
                setUniversals(nve18);
                return nve18;
            case CODABAR:
                Codabar codabar = new Codabar();
                codabar.setHumanReadableLocation(hrtLoc);
                codabar.setContent(dataInput);
                setUniversals(codabar);
                return codabar;
            case CODE25_MATRIX:
                Code2Of5 c25matrix = new Code2Of5();
                c25matrix.setMode(ToFMode.MATRIX);
                c25matrix.setHumanReadableLocation(hrtLoc);
                c25matrix.setContent(dataInput);
                setUniversals(c25matrix);
                return c25matrix;
            case CODE25_INDUSTRY:
                Code2Of5 c25ind = new Code2Of5();
                c25ind.setMode(ToFMode.INDUSTRIAL);
                c25ind.setHumanReadableLocation(hrtLoc);
                c25ind.setContent(dataInput);
                setUniversals(c25ind);
                return c25ind;
            case CODE25_INTERLEAVED:
                Code2Of5 c25inter = new Code2Of5();
                c25inter.setMode(ToFMode.INTERLEAVED);
                c25inter.setHumanReadableLocation(hrtLoc);
                c25inter.setContent(dataInput);
                setUniversals(c25inter);
                return c25inter;
            case MSI_PLESSEY:
                MsiPlessey msiPlessey = new MsiPlessey();
                switch(msiCheckDigitCombo.getSelectedIndex()) {
                    case 0:
                        msiPlessey.setCheckDigit(MsiPlessey.CheckDigit.NONE);
                        break;
                    case 1:
                        msiPlessey.setCheckDigit(MsiPlessey.CheckDigit.MOD10);
                        break;
                    case 2:
                        msiPlessey.setCheckDigit(MsiPlessey.CheckDigit.MOD10_MOD10);
                        break;
                    case 3:
                        msiPlessey.setCheckDigit(MsiPlessey.CheckDigit.MOD11);
                        break;
                    case 4:
                        msiPlessey.setCheckDigit(MsiPlessey.CheckDigit.MOD11_MOD10);
                        break;
                }
                msiPlessey.setHumanReadableLocation(hrtLoc);
                msiPlessey.setContent(dataInput);
                setUniversals(msiPlessey);
                return msiPlessey;
            case CODE39:
            case CODE39_HIBC:
                Code3Of9 code3of9 = new Code3Of9();
                if (selectedSymbol.symbology == SymbolType.Encoding.CODE39_HIBC) {
                    code3of9.setDataType(Symbol.DataType.HIBC);
                }
                switch(code39CheckCombo.getSelectedIndex()) {
                    case 0:
                        code3of9.setCheckDigit(Code3Of9.CheckDigit.NONE);
                        break;
                    case 1:
                        code3of9.setCheckDigit(Code3Of9.CheckDigit.MOD43);
                        break;
                }
                code3of9.setHumanReadableLocation(hrtLoc);
                code3of9.setContent(dataInput);
                setUniversals(code3of9);
                return code3of9;
            case DOD_LOGMARS:
                Logmars logmars = new Logmars();
                logmars.setHumanReadableLocation(hrtLoc);
                logmars.setContent(dataInput);
                setUniversals(logmars);
                return logmars;
            case CODE_11:
                Code11 code11 = new Code11();
                code11.setHumanReadableLocation(hrtLoc);
                code11.setContent(dataInput);
                setUniversals(code11);
                return code11;
            case CODE93:
                Code93 code93 = new Code93();
                code93.setHumanReadableLocation(hrtLoc);
                code93.setContent(dataInput);
                setUniversals(code93);
                return code93;
            case PZN:
                Pharmazentralnummer pzn = new Pharmazentralnummer();
                pzn.setHumanReadableLocation(hrtLoc);
                pzn.setContent(dataInput);
                setUniversals(pzn);
                return pzn;
            case CODE39_EXTENDED:
                Code3Of9Extended code3of9ext = new Code3Of9Extended();
                switch(code39CheckCombo.getSelectedIndex()) {
                    case 0:
                        code3of9ext.setCheckDigit(Code3Of9Extended.CheckDigit.NONE);
                        break;
                    case 1:
                        code3of9ext.setCheckDigit(Code3Of9Extended.CheckDigit.MOD43);
                        break;
                }
                code3of9ext.setHumanReadableLocation(hrtLoc);
                code3of9ext.setContent(dataInput);
                setUniversals(code3of9ext);
                return code3of9ext;
            case TELEPEN:
                Telepen telepen = new Telepen();
                telepen.setMode(Telepen.Mode.NORMAL);
                telepen.setHumanReadableLocation(hrtLoc);
                telepen.setContent(dataInput);
                setUniversals(telepen);
                return telepen;
            case TELEPEN_NUMERIC:
                Telepen telepenNum = new Telepen();
                telepenNum.setMode(Telepen.Mode.NUMERIC);
                telepenNum.setHumanReadableLocation(hrtLoc);
                telepenNum.setContent(dataInput);
                setUniversals(telepenNum);
                return telepenNum;
            case CODE49:
                Code49 code49 = new Code49();
                code49.setHumanReadableLocation(hrtLoc);
                code49.setContent(dataInput);
                setUniversals(code49);
                return code49;
            case KOREA_POST:
                KoreaPost koreaPost = new KoreaPost();
                koreaPost.setHumanReadableLocation(hrtLoc);
                koreaPost.setContent(dataInput);
                setUniversals(koreaPost);
                return koreaPost;
            case CODE16K:
                Code16k code16k = new Code16k();
                if (useGS1Check.isSelected()) {
                    code16k.setDataType(Symbol.DataType.GS1);
                }
                code16k.setReaderInit(readerInit);
                code16k.setContent(dataInput);
                setUniversals(code16k);
                return code16k;
            case CODE25_IATA:
                Code2Of5 c25iata = new Code2Of5();
                c25iata.setMode(ToFMode.IATA);
                c25iata.setHumanReadableLocation(hrtLoc);
                c25iata.setContent(dataInput);
                setUniversals(c25iata);
                return c25iata;
            case CODE25_DATALOGIC:
                Code2Of5 c25logic = new Code2Of5();
                c25logic.setMode(ToFMode.DATA_LOGIC);
                c25logic.setHumanReadableLocation(hrtLoc);
                c25logic.setContent(dataInput);
                setUniversals(c25logic);
                return c25logic;
            case DP_LEITCODE:
                Code2Of5 dpLeit = new Code2Of5();
                dpLeit.setMode(ToFMode.DP_LEITCODE);
                dpLeit.setHumanReadableLocation(hrtLoc);
                dpLeit.setContent(dataInput);
                setUniversals(dpLeit);
                return dpLeit;
            case DP_IDENTCODE:
                Code2Of5 dpIdent = new Code2Of5();
                dpIdent.setMode(ToFMode.DP_IDENTCODE);
                dpIdent.setHumanReadableLocation(hrtLoc);
                dpIdent.setContent(dataInput);
                setUniversals(dpIdent);
                return dpIdent;
            case USPS_POSTNET:
            case BRAZIL_CEPNET:
                Postnet postnet = new Postnet();
                postnet.setMode(Postnet.Mode.POSTNET);
                postnet.setContent(dataInput);
                setUniversals(postnet);
                return postnet;
            case USPS_PLANET:
                Postnet planet = new Postnet();
                planet.setMode(Postnet.Mode.PLANET);
                planet.setContent(dataInput);
                setUniversals(planet);
                return planet;
            case RM4SCC:
                RoyalMail4State royalMail = new RoyalMail4State();
                royalMail.setContent(dataInput);
                setUniversals(royalMail);
                return royalMail;
            case KIX_CODE:
                KixCode kixCode = new KixCode();
                kixCode.setContent(dataInput);
                setUniversals(kixCode);
                return kixCode;
            case JAPAN_POST:
                JapanPost japanPost = new JapanPost();
                japanPost.setContent(dataInput);
                setUniversals(japanPost);
                return japanPost;
            case AUSPOST:
                AustraliaPost auPost = new AustraliaPost();
                auPost.setPostMode();
                auPost.setContent(dataInput);
                setUniversals(auPost);
                return auPost;
            case AUSPOST_REPLY:
                AustraliaPost auReply = new AustraliaPost();
                auReply.setReplyMode();
                auReply.setContent(dataInput);
                setUniversals(auReply);
                return auReply;
            case AUSPOST_REROUTE:
                AustraliaPost auRoute = new AustraliaPost();
                auRoute.setRouteMode();
                auRoute.setContent(dataInput);
                setUniversals(auRoute);
                return auRoute;
            case AUSPOST_REDIRECT:
                AustraliaPost auRedirect = new AustraliaPost();
                auRedirect.setRedirectMode();
                auRedirect.setContent(dataInput);
                setUniversals(auRedirect);
                return auRedirect;
            case CHANNEL_CODE:
                ChannelCode channelCode = new ChannelCode();
                channelCode.setPreferredNumberOfChannels(channelChannelsCombo.getSelectedIndex() + 2);
                channelCode.setHumanReadableLocation(hrtLoc);
                channelCode.setContent(dataInput);
                setUniversals(channelCode);
                return channelCode;
            case PHARMA:
                Pharmacode pharmacode = new Pharmacode();
                pharmacode.setHumanReadableLocation(hrtLoc);
                pharmacode.setContent(dataInput);
                setUniversals(pharmacode);
                return pharmacode;
            case PHARMA_TWOTRACK:
                Pharmacode2Track pharmacode2t = new Pharmacode2Track();
                pharmacode2t.setContent(dataInput);
                setUniversals(pharmacode2t);
                return pharmacode2t;
            case CODE_32:
                Code32 code32 = new Code32();
                code32.setHumanReadableLocation(hrtLoc);
                code32.setContent(dataInput);
                setUniversals(code32);
                return code32;
            case PDF417:
            case PDF417_HIBC:
            case PDF417_TRUNCATED:
                Pdf417 pdf417 = new Pdf417();
                if (useGS1Check.isSelected()) {
                    pdf417.setDataType(Symbol.DataType.GS1);
                }
                if (selectedSymbol.symbology == SymbolType.Encoding.PDF417_HIBC) {
                    pdf417.setDataType(Symbol.DataType.HIBC);
                } else if (selectedSymbol.symbology == SymbolType.Encoding.PDF417_TRUNCATED) {
                    pdf417.setMode(Pdf417.Mode.TRUNCATED);
                }
                if (pdfEccCombo.getSelectedIndex() != 0) {
                    pdf417.setPreferredEccLevel(pdfEccCombo.getSelectedIndex() - 1);
                }
                if (pdfColumnsCombo.getSelectedIndex() != 0) {
                    pdf417.setDataColumns(pdfColumnsCombo.getSelectedIndex());
                }
                pdf417.setReaderInit(readerInit);
                pdf417.setContent(dataInput);
                setUniversals(pdf417);
                return pdf417;
            case PDF417_MICRO:
            case PDF417_MICRO_HIBC:
                Pdf417 microPdf417 = new Pdf417();
                microPdf417.setMode(Pdf417.Mode.MICRO);
                if (useGS1Check.isSelected()) {
                    microPdf417.setDataType(Symbol.DataType.GS1);
                }
                if (selectedSymbol.symbology == SymbolType.Encoding.PDF417_MICRO_HIBC) {
                    microPdf417.setDataType(Symbol.DataType.HIBC);
                }
                microPdf417.setReaderInit(readerInit);
                if (microPdfColumnsCombo.getSelectedIndex() != 0) {
                    microPdf417.setDataColumns(microPdfColumnsCombo.getSelectedIndex());
                }
                microPdf417.setContent(dataInput);
                setUniversals(microPdf417);
                return microPdf417;
            case AZTEC:
            case AZTEC_HIBC:
                AztecCode aztecCode = new AztecCode();
                if (useGS1Check.isSelected()) {
                    aztecCode.setDataType(Symbol.DataType.GS1);
                }
                if (selectedSymbol.symbology == SymbolType.Encoding.AZTEC_HIBC) {
                    aztecCode.setDataType(Symbol.DataType.HIBC);
                }
                aztecCode.setReaderInit(readerInit);
                if (aztecUserEcc.isSelected()) {
                    aztecCode.setPreferredEccLevel(aztecUserEccCombo.getSelectedIndex() + 1);
                }
                if (aztecUserSize.isSelected()) {
                    aztecCode.setPreferredSize(aztecUserSizeCombo.getSelectedIndex() + 1);
                }
                aztecCode.setContent(dataInput);
                setUniversals(aztecCode);
                return aztecCode;
            case AZTEC_RUNE:
                AztecRune aztecRune = new AztecRune();
                aztecRune.setContent(dataInput);
                setUniversals(aztecRune);
                return aztecRune;
            case DATAMATRIX:
            case DATAMATRIX_HIBC:
                DataMatrix dataMatrix = new DataMatrix();
                if (useGS1Check.isSelected()) {
                    dataMatrix.setDataType(Symbol.DataType.GS1);
                }
                if (selectedSymbol.symbology == SymbolType.Encoding.DATAMATRIX_HIBC) {
                    dataMatrix.setDataType(Symbol.DataType.HIBC);
                }
                dataMatrix.setReaderInit(readerInit);
                dataMatrix.setPreferredSize(dataMatrixSizeCombo.getSelectedIndex());
                dataMatrix.setForceMode(dataMatrixSquareOnlyCheck.isSelected() ? ForceMode.SQUARE : ForceMode.NONE);
                dataMatrix.setContent(dataInput);
                setUniversals(dataMatrix);
                return dataMatrix;
            case USPS_IMAIL:
                UspsOneCode uspsOneCode = new UspsOneCode();
                uspsOneCode.setContent(dataInput);
                setUniversals(uspsOneCode);
                return uspsOneCode;
            case USPS_IMPB:
                UspsPackage uspsPackage = new UspsPackage();
                uspsPackage.setContent(dataInput);
                setUniversals(uspsPackage);
                return uspsPackage;
            case QR:
            case QR_HIBC:
                QrCode qrCode = new QrCode();
                if (useGS1Check.isSelected()) {
                    qrCode.setDataType(Symbol.DataType.GS1);
                }
                if (selectedSymbol.symbology == SymbolType.Encoding.QR_HIBC) {
                    qrCode.setDataType(Symbol.DataType.HIBC);
                }
                if (qrUserEcc.isSelected()) {
                    switch(qrUserEccCombo.getSelectedIndex()) {
                        case 0:
                            qrCode.setPreferredEccLevel(QrCode.EccLevel.L);
                            break;
                        case 1:
                            qrCode.setPreferredEccLevel(QrCode.EccLevel.M);
                            break;
                        case 2:
                            qrCode.setPreferredEccLevel(QrCode.EccLevel.Q);
                            break;
                        case 3:
                            qrCode.setPreferredEccLevel(QrCode.EccLevel.H);
                            break;
                    }
                }
                if (qrUserSize.isSelected()) {
                    qrCode.setPreferredVersion(qrUserSizeCombo.getSelectedIndex() + 1);
                }
                qrCode.setReaderInit(readerInit);
                qrCode.setContent(dataInput);
                setUniversals(qrCode);
                return qrCode;
            case QR_MICRO:
                MicroQrCode microQrCode = new MicroQrCode();
                if (microQrUserEcc.isSelected()) {
                    switch(microQrUserEccCombo.getSelectedIndex()) {
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
                }
                if (microQrUserSize.isSelected()) {
                    microQrCode.setPreferredVersion(microQrUserSizeCombo.getSelectedIndex() + 1);
                }
                microQrCode.setContent(dataInput);
                setUniversals(microQrCode);
                return microQrCode;
            case CODE_ONE:
                CodeOne codeOne = new CodeOne();
                if (useGS1Check.isSelected()) {
                    codeOne.setDataType(Symbol.DataType.GS1);
                }
                codeOne.setReaderInit(readerInit);
                switch(codeOneSizeCombo.getSelectedIndex()) {
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
                setUniversals(codeOne);
                return codeOne;
            case GRIDMATRIX:
                GridMatrix gridMatrix = new GridMatrix();
                if (useGS1Check.isSelected()) {
                    gridMatrix.setDataType(Symbol.DataType.GS1);
                }
                gridMatrix.setReaderInit(readerInit);
                if (gridmatrixUserEcc.isSelected()) {
                    gridMatrix.setPreferredEccLevel(gridmatrixUserEccCombo.getSelectedIndex() + 1);
                }
                if (gridmatrixUserSize.isSelected()) {
                    gridMatrix.setPreferredVersion(gridmatrixUserSizeCombo.getSelectedIndex() + 1);
                }
                gridMatrix.setContent(dataInput);
                setUniversals(gridMatrix);
                return gridMatrix;
            case DB14:
                DataBar14 dataBar14 = new DataBar14();
                dataBar14.setLinearMode();
                dataBar14.setHumanReadableLocation(hrtLoc);
                dataBar14.setContent(dataInput);
                setUniversals(dataBar14);
                return dataBar14;
            case DB14_STACKED_OMNIDIRECT:
                DataBar14 dataBar14so = new DataBar14();
                dataBar14so.setOmnidirectionalMode();
                dataBar14so.setContent(dataInput);
                setUniversals(dataBar14so);
                return dataBar14so;
            case DB14_STACKED:
                DataBar14 dataBar14s = new DataBar14();
                dataBar14s.setStackedMode();
                dataBar14s.setContent(dataInput);
                setUniversals(dataBar14s);
                return dataBar14s;
            case DB_LIMITED:
                DataBarLimited dataBarLimited = new DataBarLimited();
                dataBarLimited.setHumanReadableLocation(hrtLoc);
                dataBarLimited.setContent(dataInput);
                setUniversals(dataBarLimited);
                return dataBarLimited;
            case DB_EXPANDED:
                DataBarExpanded dataBarE = new DataBarExpanded();
                dataBarE.setNotStacked();
                dataBarE.setContent(dataInput);
                setUniversals(dataBarE);
                return dataBarE;
            case DB_EXPANDED_STACKED:
                DataBarExpanded dataBarES = new DataBarExpanded();
                dataBarES.setNoOfColumns(databarColumnsCombo.getSelectedIndex());
                dataBarES.setStacked();
                dataBarES.setContent(dataInput);
                setUniversals(dataBarES);
                return dataBarES;
            case MAXICODE:
                MaxiCode maxiCode = new MaxiCode();
                maxiCode.setPrimary(maxiPrimaryData.getText());
                if (readerInit) {
                    maxiCode.setMode(6);
                } else {
                    maxiCode.setMode(maxiEncodingModeCombo.getSelectedIndex() + 2);
                }
                maxiCode.setContent(dataInput);
                setUniversals(maxiCode);
                return maxiCode;
            case CODABLOCK_F:
            case CODABLOCK_HIBC:
                CodablockF codablockF = new CodablockF();
                if (useGS1Check.isSelected()) {
                    codablockF.setDataType(Symbol.DataType.GS1);
                }
                if (selectedSymbol.symbology == SymbolType.Encoding.CODABLOCK_HIBC) {
                    codablockF.setDataType(Symbol.DataType.HIBC);
                }
                codablockF.setContent(dataInput);
                setUniversals(codablockF);
                return codablockF;
            default:
                // Should never happen
                throw new OkapiException("Symbology not recognised: " + selectedSymbol.guiLabel);
            }
        }
    }

    private int eanCalculateVersion() {
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

    private static void createNodes(DefaultMutableTreeNode top) {
        // Defines symbology selection tree

        DefaultMutableTreeNode symbolType;
        DefaultMutableTreeNode symbolSubType;
        DefaultMutableTreeNode symbolName;

        symbolType = new DefaultMutableTreeNode("One-Dimensional");
        top.add(symbolType);

        symbolName = new DefaultMutableTreeNode(new SymbolType("Channel Code", SymbolType.Encoding.CHANNEL_CODE));
        symbolType.add(symbolName);

        symbolName = new DefaultMutableTreeNode(new SymbolType("Codabar", SymbolType.Encoding.CODABAR));
        symbolType.add(symbolName);

        symbolName = new DefaultMutableTreeNode(new SymbolType("Code 11", SymbolType.Encoding.CODE_11));
        symbolType.add(symbolName);

        symbolSubType = new DefaultMutableTreeNode("Code 2 of 5");
        symbolType.add(symbolSubType);

        symbolName = new DefaultMutableTreeNode(new SymbolType("Standard", SymbolType.Encoding.CODE25_MATRIX));
        symbolSubType.add(symbolName);

        symbolName = new DefaultMutableTreeNode(new SymbolType("IATA", SymbolType.Encoding.CODE25_IATA));
        symbolSubType.add(symbolName);

        symbolName = new DefaultMutableTreeNode(new SymbolType("Industrial", SymbolType.Encoding.CODE25_INDUSTRY));
        symbolSubType.add(symbolName);

        symbolName = new DefaultMutableTreeNode(new SymbolType("Interleaved", SymbolType.Encoding.CODE25_INTERLEAVED));
        symbolSubType.add(symbolName);

        symbolName = new DefaultMutableTreeNode(new SymbolType("Data Logic", SymbolType.Encoding.CODE25_DATALOGIC));
        symbolSubType.add(symbolName);

        symbolName = new DefaultMutableTreeNode(new SymbolType("ITF-14", SymbolType.Encoding.ITF14));
        symbolSubType.add(symbolName);

        symbolSubType = new DefaultMutableTreeNode("Code 39");
        symbolType.add(symbolSubType);

        symbolName = new DefaultMutableTreeNode(new SymbolType("Standard", SymbolType.Encoding.CODE39));
        symbolSubType.add(symbolName);

        symbolName = new DefaultMutableTreeNode(new SymbolType("Extended", SymbolType.Encoding.CODE39_EXTENDED));
        symbolSubType.add(symbolName);

        symbolName = new DefaultMutableTreeNode(new SymbolType("Code 93", SymbolType.Encoding.CODE93));
        symbolSubType.add(symbolName);

        symbolName = new DefaultMutableTreeNode(new SymbolType("LOGMARS", SymbolType.Encoding.DOD_LOGMARS));
        symbolSubType.add(symbolName);

        symbolSubType = new DefaultMutableTreeNode("Code 128");
        symbolType.add(symbolSubType);

        symbolName = new DefaultMutableTreeNode(new SymbolType("Code 128", SymbolType.Encoding.CODE_128));
        symbolSubType.add(symbolName);

        symbolName = new DefaultMutableTreeNode(new SymbolType("NVE-18", SymbolType.Encoding.NVE18));
        symbolSubType.add(symbolName);

        symbolName = new DefaultMutableTreeNode(new SymbolType("European Article Number", SymbolType.Encoding.EAN));
        symbolType.add(symbolName);

        symbolName = new DefaultMutableTreeNode(new SymbolType("MSI Plessey", SymbolType.Encoding.MSI_PLESSEY));
        symbolType.add(symbolName);

        symbolSubType = new DefaultMutableTreeNode("Telepen");
        symbolType.add(symbolSubType);

        symbolName = new DefaultMutableTreeNode(new SymbolType("Alpha", SymbolType.Encoding.TELEPEN));
        symbolSubType.add(symbolName);

        symbolName = new DefaultMutableTreeNode(new SymbolType("Numeric", SymbolType.Encoding.TELEPEN_NUMERIC));
        symbolSubType.add(symbolName);

        symbolSubType = new DefaultMutableTreeNode("Universal Product Code");
        symbolType.add(symbolSubType);

        symbolName = new DefaultMutableTreeNode(new SymbolType("Version A", SymbolType.Encoding.UPC_A));
        symbolSubType.add(symbolName);

        symbolName = new DefaultMutableTreeNode(new SymbolType("Version E", SymbolType.Encoding.UPC_E));
        symbolSubType.add(symbolName);

        symbolType = new DefaultMutableTreeNode("Stacked");
        top.add(symbolType);

        symbolName = new DefaultMutableTreeNode(new SymbolType("Codablock-F", SymbolType.Encoding.CODABLOCK_F));
        symbolType.add(symbolName);

        symbolName = new DefaultMutableTreeNode(new SymbolType("Code 16K", SymbolType.Encoding.CODE16K));
        symbolType.add(symbolName);

        symbolName = new DefaultMutableTreeNode(new SymbolType("Code 49", SymbolType.Encoding.CODE49));
        symbolType.add(symbolName);

        symbolName = new DefaultMutableTreeNode(new SymbolType("PDF417", SymbolType.Encoding.PDF417));
        symbolType.add(symbolName);

        symbolName = new DefaultMutableTreeNode(new SymbolType("PDF417 Truncated", SymbolType.Encoding.PDF417_TRUNCATED));
        symbolType.add(symbolName);

        symbolName = new DefaultMutableTreeNode(new SymbolType("Micro PDF417", SymbolType.Encoding.PDF417_MICRO));
        symbolType.add(symbolName);

        symbolType = new DefaultMutableTreeNode("Two-Dimensional");
        top.add(symbolType);

        symbolName = new DefaultMutableTreeNode(new SymbolType("Aztec Code", SymbolType.Encoding.AZTEC));
        symbolType.add(symbolName);

        symbolName = new DefaultMutableTreeNode(new SymbolType("Aztec Runes", SymbolType.Encoding.AZTEC_RUNE));
        symbolType.add(symbolName);

        symbolName = new DefaultMutableTreeNode(new SymbolType("Data Matrix", SymbolType.Encoding.DATAMATRIX));
        symbolType.add(symbolName);

        symbolName = new DefaultMutableTreeNode(new SymbolType("Code One", SymbolType.Encoding.CODE_ONE));
        symbolType.add(symbolName);

        symbolName = new DefaultMutableTreeNode(new SymbolType("Grid Matrix", SymbolType.Encoding.GRIDMATRIX));
        symbolType.add(symbolName);

        symbolName = new DefaultMutableTreeNode(new SymbolType("Maxicode", SymbolType.Encoding.MAXICODE));
        symbolType.add(symbolName);

        symbolName = new DefaultMutableTreeNode(new SymbolType("QR Code", SymbolType.Encoding.QR));
        symbolType.add(symbolName);

        symbolName = new DefaultMutableTreeNode(new SymbolType("Micro QR Code", SymbolType.Encoding.QR_MICRO));
        symbolType.add(symbolName);

        symbolType = new DefaultMutableTreeNode("GS1 DataBar");
        top.add(symbolType);

        symbolName = new DefaultMutableTreeNode(new SymbolType("Omnidirectional", SymbolType.Encoding.DB14));
        symbolType.add(symbolName);

        symbolName = new DefaultMutableTreeNode(new SymbolType("Stacked", SymbolType.Encoding.DB14_STACKED));
        symbolType.add(symbolName);

        symbolName = new DefaultMutableTreeNode(new SymbolType("Stacked Omnidirectional", SymbolType.Encoding.DB14_STACKED_OMNIDIRECT));
        symbolType.add(symbolName);

        symbolName = new DefaultMutableTreeNode(new SymbolType("Limited", SymbolType.Encoding.DB_LIMITED));
        symbolType.add(symbolName);

        symbolName = new DefaultMutableTreeNode(new SymbolType("Expanded Omnidirectional", SymbolType.Encoding.DB_EXPANDED));
        symbolType.add(symbolName);

        symbolName = new DefaultMutableTreeNode(new SymbolType("Expanded Stacked Omnidirectional", SymbolType.Encoding.DB_EXPANDED_STACKED));
        symbolType.add(symbolName);

        symbolType = new DefaultMutableTreeNode("Postal");
        top.add(symbolType);

        symbolSubType = new DefaultMutableTreeNode("Australia Post");
        symbolType.add(symbolSubType);

        symbolName = new DefaultMutableTreeNode(new SymbolType("Customer", SymbolType.Encoding.AUSPOST));
        symbolSubType.add(symbolName);

        symbolName = new DefaultMutableTreeNode(new SymbolType("Reply Paid", SymbolType.Encoding.AUSPOST_REPLY));
        symbolSubType.add(symbolName);

        symbolName = new DefaultMutableTreeNode(new SymbolType("Routing", SymbolType.Encoding.AUSPOST_REROUTE));
        symbolSubType.add(symbolName);

        symbolName = new DefaultMutableTreeNode(new SymbolType("Redirect", SymbolType.Encoding.AUSPOST_REDIRECT));
        symbolSubType.add(symbolName);

        symbolName = new DefaultMutableTreeNode(new SymbolType("Brazilian CEPNet", SymbolType.Encoding.BRAZIL_CEPNET));
        symbolType.add(symbolName);

        symbolSubType = new DefaultMutableTreeNode("Deutsche Post");
        symbolType.add(symbolSubType);

        symbolName = new DefaultMutableTreeNode(new SymbolType("Leitcode", SymbolType.Encoding.DP_LEITCODE));
        symbolSubType.add(symbolName);

        symbolName = new DefaultMutableTreeNode(new SymbolType("Identcode", SymbolType.Encoding.DP_IDENTCODE));
        symbolSubType.add(symbolName);

        symbolName = new DefaultMutableTreeNode(new SymbolType("Dutch Post KIX", SymbolType.Encoding.KIX_CODE));
        symbolType.add(symbolName);

        symbolName = new DefaultMutableTreeNode(new SymbolType("Japan Post", SymbolType.Encoding.JAPAN_POST));
        symbolType.add(symbolName);

        symbolName = new DefaultMutableTreeNode(new SymbolType("Korea Post", SymbolType.Encoding.KOREA_POST));
        symbolType.add(symbolName);

        symbolName = new DefaultMutableTreeNode(new SymbolType("Royal Mail", SymbolType.Encoding.RM4SCC));
        symbolType.add(symbolName);

        symbolSubType = new DefaultMutableTreeNode("USPS");
        symbolType.add(symbolSubType);

        symbolName = new DefaultMutableTreeNode(new SymbolType("Intelligent Mail", SymbolType.Encoding.USPS_IMAIL));
        symbolSubType.add(symbolName);

        symbolName = new DefaultMutableTreeNode(new SymbolType("IM Package Barcode", SymbolType.Encoding.USPS_IMPB));
        symbolSubType.add(symbolName);

        symbolName = new DefaultMutableTreeNode(new SymbolType("PostNet", SymbolType.Encoding.USPS_POSTNET));
        symbolSubType.add(symbolName);

        symbolName = new DefaultMutableTreeNode(new SymbolType("PLANET", SymbolType.Encoding.USPS_PLANET));
        symbolSubType.add(symbolName);

        symbolType = new DefaultMutableTreeNode("Medical");
        top.add(symbolType);

        symbolName = new DefaultMutableTreeNode(new SymbolType("Code 32", SymbolType.Encoding.CODE_32));
        symbolType.add(symbolName);

        symbolSubType = new DefaultMutableTreeNode("HIBC");
        symbolType.add(symbolSubType);

        symbolName = new DefaultMutableTreeNode(new SymbolType("Aztec Code", SymbolType.Encoding.AZTEC_HIBC));
        symbolSubType.add(symbolName);

        symbolName = new DefaultMutableTreeNode(new SymbolType("Codablock-F", SymbolType.Encoding.CODABLOCK_HIBC));
        symbolSubType.add(symbolName);

        symbolName = new DefaultMutableTreeNode(new SymbolType("Code 39", SymbolType.Encoding.CODE39_HIBC));
        symbolSubType.add(symbolName);

        symbolName = new DefaultMutableTreeNode(new SymbolType("Code 128", SymbolType.Encoding.CODE_128_HIBC));
        symbolSubType.add(symbolName);

        symbolName = new DefaultMutableTreeNode(new SymbolType("Data Matrix", SymbolType.Encoding.DATAMATRIX_HIBC));
        symbolSubType.add(symbolName);

        symbolName = new DefaultMutableTreeNode(new SymbolType("PDF417", SymbolType.Encoding.PDF417_HIBC));
        symbolSubType.add(symbolName);

        symbolName = new DefaultMutableTreeNode(new SymbolType("Micro PDF417", SymbolType.Encoding.PDF417_MICRO_HIBC));
        symbolSubType.add(symbolName);

        symbolName = new DefaultMutableTreeNode(new SymbolType("QR Code", SymbolType.Encoding.QR_HIBC));
        symbolSubType.add(symbolName);

        symbolSubType = new DefaultMutableTreeNode("Pharmacode");
        symbolType.add(symbolSubType);

        symbolName = new DefaultMutableTreeNode(new SymbolType("One Track", SymbolType.Encoding.PHARMA));
        symbolSubType.add(symbolName);

        symbolName = new DefaultMutableTreeNode(new SymbolType("Two Track", SymbolType.Encoding.PHARMA_TWOTRACK));
        symbolSubType.add(symbolName);

        symbolName = new DefaultMutableTreeNode(new SymbolType("PZN8", SymbolType.Encoding.PZN));
        symbolType.add(symbolName);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton aboutButton;
    private javax.swing.JButton addCompositeButton;
    private javax.swing.JPanel attributePanel;
    private javax.swing.JScrollPane attributeScrollPane;
    private javax.swing.JPanel attributeScrollPanel;
    private javax.swing.JRadioButton aztecAutoSize;
    private javax.swing.ButtonGroup aztecButtonGroup;
    private javax.swing.JPanel aztecPanel;
    private javax.swing.JRadioButton aztecUserEcc;
    private javax.swing.JComboBox aztecUserEccCombo;
    private javax.swing.JRadioButton aztecUserSize;
    private javax.swing.JComboBox aztecUserSizeCombo;
    private javax.swing.JButton batchFileButton;
    private java.awt.TextArea batchOutputArea;
    private javax.swing.JPanel batchPanel;
    private javax.swing.JComboBox channelChannelsCombo;
    private javax.swing.JLabel channelNoOfChannels;
    private javax.swing.JPanel channelPanel;
    private javax.swing.JCheckBox chkReaderInit;
    private javax.swing.JComboBox cmbHrtPosition;
    private javax.swing.JComboBox code39CheckCombo;
    private javax.swing.JLabel code39CheckLabel;
    private javax.swing.JPanel code39Panel;
    private javax.swing.JPanel codeOnePanel;
    private javax.swing.JComboBox codeOneSizeCombo;
    private javax.swing.JLabel codeOneSizeLabel;
    public static javax.swing.JTextField compositeInputField;
    private javax.swing.JLabel compositeLabel;
    private javax.swing.JLabel compositeModeLabel;
    private javax.swing.JPanel compositePanel;
    private javax.swing.JComboBox compositeUserMode;
    private javax.swing.JButton createButton;
    public static javax.swing.JTextField dataInputField;
    private javax.swing.JComboBox dataMatrixSizeCombo;
    private javax.swing.JLabel dataMatrixSizeLabel;
    private javax.swing.JCheckBox dataMatrixSquareOnlyCheck;
    private javax.swing.JComboBox databarColumnsCombo;
    private javax.swing.JLabel databarColumnsLabel;
    private javax.swing.JPanel databarPanel;
    private javax.swing.JPanel datamatrixPanel;
    private javax.swing.JLabel destinationLabel;
    private javax.swing.JButton directoryButton;
    private java.awt.TextArea encodeInfoArea;
    private javax.swing.JLabel errorLabel;
    private javax.swing.JButton exitButton;
    private javax.swing.JTextField folderField;
    private javax.swing.JTextField formatField;
    private javax.swing.JLabel formatLabel;
    private javax.swing.JRadioButton gridmatrixAutoSize;
    private javax.swing.ButtonGroup gridmatrixButtonGroup;
    private javax.swing.JPanel gridmatrixPanel;
    private javax.swing.JRadioButton gridmatrixUserEcc;
    private javax.swing.JComboBox gridmatrixUserEccCombo;
    private javax.swing.JRadioButton gridmatrixUserSize;
    private javax.swing.JComboBox gridmatrixUserSizeCombo;
    private javax.swing.JTextField incrementField;
    private javax.swing.JLabel incrementLabel;
    private javax.swing.JButton inkButton;
    private javax.swing.JLabel inputLabel;
    private javax.swing.JLabel lblBorderWidth;
    private javax.swing.JLabel lblWhitespaceWidth;
    private javax.swing.JLabel lblXDimension;
    private javax.swing.JLabel lblXDimensionPixels;
    private javax.swing.JButton loadDataButton;
    private javax.swing.JTabbedPane mainTabs;
    private javax.swing.JLabel maxiEncodeModeLabel;
    private javax.swing.JComboBox maxiEncodingModeCombo;
    private javax.swing.JTextField maxiPrimaryData;
    private javax.swing.JLabel maxiPrimaryDataLabel;
    private javax.swing.JPanel maxicodePanel;
    private javax.swing.JComboBox microPdfColumnsCombo;
    private javax.swing.JLabel microPdfColumnsLabel;
    private javax.swing.JPanel microPdfPanel;
    private javax.swing.JRadioButton microQrAutoSize;
    private javax.swing.ButtonGroup microQrButtonGroup;
    private javax.swing.JPanel microQrPanel;
    private javax.swing.JRadioButton microQrUserEcc;
    private javax.swing.JComboBox microQrUserEccCombo;
    private javax.swing.JRadioButton microQrUserSize;
    private javax.swing.JComboBox microQrUserSizeCombo;
    private javax.swing.JComboBox msiCheckDigitCombo;
    private javax.swing.JLabel msiCheckDigitLabel;
    private javax.swing.JPanel msiPanel;
    private javax.swing.JLabel outFileFormatLabel;
    private javax.swing.JLabel outFileNameLabel;
    private javax.swing.JComboBox outFilenameCombo;
    private javax.swing.JComboBox outFormatCombo;
    private javax.swing.JButton paperButton;
    private javax.swing.JComboBox pdfColumnsCombo;
    private javax.swing.JLabel pdfDataColumnsLabel;
    private javax.swing.JComboBox pdfEccCombo;
    private javax.swing.JLabel pdfEccLabel;
    private javax.swing.JPanel pdfPanel;
    private javax.swing.JTextField prefixField;
    private javax.swing.JLabel prefixLabel;
    private javax.swing.JRadioButton qrAutoSize;
    private javax.swing.ButtonGroup qrButtonGroup;
    private javax.swing.JPanel qrPanel;
    private javax.swing.JRadioButton qrUserEcc;
    private javax.swing.JComboBox qrUserEccCombo;
    private javax.swing.JRadioButton qrUserSize;
    private javax.swing.JComboBox qrUserSizeCombo;
    private javax.swing.JButton resetButton;
    private javax.swing.JButton resetColourButton;
    private javax.swing.JButton runBatchButton;
    private javax.swing.JButton saveButton;
    private java.awt.TextArea sequenceArea;
    private javax.swing.JPanel singlePanel;
    private javax.swing.JTextField startField;
    private javax.swing.JLabel startLabel;
    private javax.swing.JTextField stopField;
    private javax.swing.JLabel stopLabel;
    private javax.swing.JScrollPane symbolPane;
    private javax.swing.JTree symbolTree;
    private static javax.swing.JPanel topPanel;
    private javax.swing.JTextField txtBorderWidth;
    private javax.swing.JLabel txtShowHrt;
    private javax.swing.JTextField txtWhitespaceWidth;
    private javax.swing.JTextField txtXDimension;
    private javax.swing.JCheckBox useCompositeCheck;
    private javax.swing.JCheckBox useGS1Check;
    // End of variables declaration//GEN-END:variables
}
