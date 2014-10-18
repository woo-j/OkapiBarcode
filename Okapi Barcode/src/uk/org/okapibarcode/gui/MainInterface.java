/*
 * Copyright 2014 Robin Stuart and Robert Elliott
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

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;    

import java.util.ArrayList;

import java.io.File;

/**
 *
 * @author Robert Elliott <jakel2006@me.com>
 */
public class MainInterface extends JFrame implements TreeSelectionListener {

    SubPanels sub = new SubPanels();
    JPanel topPanel = new JPanel();
    JPanel savePanel = new JPanel();
    JPanel bottomPanel = new JPanel();
    JPanel barCode = new JPanel();
    JPanel dataInputPanel = new JPanel();
    JPanel compositeInputPanel = new JPanel();
    JPanel formatPanel = new JPanel();
    JPanel buttonPanel = new JPanel();
    JLabel dataInputLabel = new JLabel("Code");
    JLabel errorLabel = new JLabel("");
    JLabel compositeInputLabel = new JLabel("2D");
    public static JTextField dataInputField = new JTextField();
    public static JTextField compositeInputField = new JTextField();
    //Lists
    JScrollPane symbolScrollPane;
    JButton addDataButton = new JButton("...");
    JButton sequenceButton = new JButton("123...");
    JButton aboutButton = new JButton("About");
    JButton saveButton = new JButton("Save");
    JButton exitButton = new JButton("Exit");
    Dimension size = new Dimension(900, 700);
    public static String dataInput = null; //Original User Input
    public static String compositeInput = null; // User input for composite symbol
    public static String symbology = null; //Chosen Symbology
    public static String outputf = null; //file to output to
    public static String errorOutput = null; //Error string
    public static boolean subPanel = false;
    public static int width = 0;
    public static int height = 61;
    public static int factor = 0;
    public static int barHeight = 0;
    public static boolean debug = true;
    public static Object[] bc;
    public static ArrayList<Rectangle> bcs = new ArrayList<>();
    public static ArrayList<uk.org.okapibarcode.backend.TextBox> txt = new ArrayList<>();
    public static ArrayList<uk.org.okapibarcode.backend.Hexagon> hex = new ArrayList<>();
    public static ArrayList<Ellipse2D.Double> target = new ArrayList<>();
    private JTree tree;

    public void reset() {
        bcs.clear();
    }

    public MainInterface() {
        ActionListener al;
        DocumentListener dl;
        Insets insets = getInsets();
        setLayout(null);
        setPreferredSize(size);
        setTitle("Zint Barcode Studio 3.0 Alpha");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        DefaultMutableTreeNode treeTop = new DefaultMutableTreeNode("Symbologies");
        createNodes (treeTop);
        tree = new JTree(treeTop);
        
        tree.getSelectionModel().setSelectionMode
                (TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeSelectionListener(this);
        tree.expandRow(1);
        tree.setSelectionRow(7); // Selects Code 128 as default

        symbolScrollPane = new JScrollPane(tree);
                  
        add(topPanel);
        add(bottomPanel);
        add(buttonPanel);
        add(symbolScrollPane);
        add(savePanel);
        savePanel.setVisible(false);

        topPanel.setBounds(202, 0, size.width - 202, size.height - (size.height - 500));
        topPanel.setBorder(BorderFactory.createEmptyBorder());
        topPanel.setBackground(Color.white);

        bottomPanel.setBounds(202, 502, size.width - 204, 100);
        bottomPanel.setBorder(BorderFactory.createBevelBorder(1));
        bottomPanel.add(dataInputPanel);
        bottomPanel.add(compositeInputPanel);
        bottomPanel.add(formatPanel);
        
        buttonPanel.setBounds(202, size.height - 100, size.width - 204, 100);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder());
        buttonPanel.add(aboutButton, BorderLayout.LINE_START);
        buttonPanel.add(saveButton, BorderLayout.EAST);
        buttonPanel.add(exitButton, BorderLayout.LINE_END);
        
        symbolScrollPane.setBorder(BorderFactory.createEmptyBorder());
        symbolScrollPane.setBounds(2, 2, 198, size.height - 100);

        errorLabel.setBounds(202, size.height - (size.height - 502), size.width - 202, 40);
        errorLabel.setBackground(Color.red);

        dataInputField.setColumns(40);
        dl = new DocumentListener() {
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
                dataInput = dataInputField.getText().toString();
                compositeInput = compositeInputField.getText().toString();
                encodeData();
            }
        };
        dataInputField.getDocument().addDocumentListener(dl);
        dataInputField.setText("Your Data Here!");
        compositeInputField.setColumns(40);
        dataInputPanel.setBounds(insets.left + 200, insets.top + 200, size.width - 200, 50);
        dataInputPanel.add(dataInputLabel, BorderLayout.LINE_START);
        dataInputPanel.add(dataInputField, BorderLayout.CENTER);
        dataInputPanel.add(addDataButton, BorderLayout.EAST);
        dataInputPanel.add(sequenceButton, BorderLayout.LINE_END);

        compositeInputPanel.add(compositeInputLabel, BorderLayout.LINE_START);
        compositeInputPanel.add(compositeInputField, BorderLayout.EAST);
        
        al = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                MoreData addme = new MoreData();
                addme.setLocationRelativeTo(MainInterface.this);
                addme.setVisible(true);
            }
        };
        addDataButton.addActionListener(al);
        al = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                Sequence sequence = new Sequence();
                sequence.setLocationRelativeTo(MainInterface.this);
                sequence.setVisible(true);
            }
        };
        sequenceButton.addActionListener(al);
        al = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                dispose();
            }
        };
        exitButton.addActionListener(al);
        al = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                AboutOkapi az = new AboutOkapi();
                az.setLocationRelativeTo(MainInterface.this);
                az.setVisible(true);
            }
        };
        aboutButton.addActionListener(al);
        
        al = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                JFileChooser fileChooser = new JFileChooser();
                if (fileChooser.showSaveDialog(MainInterface.this) == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    try {
                        SaveImage saveImage = new SaveImage();
                        dataInput = dataInputField.getText().toString();
                        compositeInput = compositeInputField.getText().toString();
                        encodeData(); 
                        saveImage.SaveImage(file, savePanel);
                    } catch (Exception e) {
                        System.out.println("Cannot wright to file" + fileChooser.getSelectedFile().toString());
                    }
                }
            }
        };
        saveButton.addActionListener(al);
    }
    
    private static void createNodes(DefaultMutableTreeNode top) {
        // Defines symbology selection tree
        
        DefaultMutableTreeNode symbolType = null;
        DefaultMutableTreeNode symbolSubType = null;
        DefaultMutableTreeNode symbolName = null;
        
        symbolType = new DefaultMutableTreeNode("One-Dimensional");
        top.add(symbolType);
        
        symbolName = new DefaultMutableTreeNode(new SymbolType("Channel Code", "BARCODE_CHANNEL", 140));
        symbolType.add(symbolName);        
        
        symbolName = new DefaultMutableTreeNode(new SymbolType("Codabar", "BARCODE_CODABAR", 18));
        symbolType.add(symbolName);        
        
        symbolName = new DefaultMutableTreeNode(new SymbolType("Code 11", "BARCODE_CODE11", 1));
        symbolType.add(symbolName);
        
        symbolSubType = new DefaultMutableTreeNode("Code 2 of 5");
        symbolType.add(symbolSubType);
        
        symbolName = new DefaultMutableTreeNode(new SymbolType("Standard", "BARCODE_C25MATRIX", 2));
        symbolSubType.add(symbolName);
        
        symbolName = new DefaultMutableTreeNode(new SymbolType("IATA", "BARCODE_C25IATA", 4));
        symbolSubType.add(symbolName);
        
        symbolName = new DefaultMutableTreeNode(new SymbolType("Industrial", "BARCODE_C25IND", 7));
        symbolSubType.add(symbolName);
        
        symbolName = new DefaultMutableTreeNode(new SymbolType("Interleaved", "BARCODE_C25INTER", 3));
        symbolSubType.add(symbolName);
        
        symbolName = new DefaultMutableTreeNode(new SymbolType("Data Logic", "BARCODE_C25LOGIC", 6));
        symbolSubType.add(symbolName);
        
        symbolName = new DefaultMutableTreeNode(new SymbolType("ITF-14", "BARCODE_ITF14", 89));
        symbolSubType.add(symbolName);
        
        symbolSubType = new DefaultMutableTreeNode("Code 39");
        symbolType.add(symbolSubType);
        
        symbolName = new DefaultMutableTreeNode(new SymbolType("Standard", "BARCODE_CODE39", 8));
        symbolSubType.add(symbolName);
        
        symbolName = new DefaultMutableTreeNode(new SymbolType("Extended", "BARCODE_EXCODE39", 9));
        symbolSubType.add(symbolName);
        
        symbolName = new DefaultMutableTreeNode(new SymbolType("Code 93", "BARCODE_CODE93", 25));
        symbolSubType.add(symbolName);
        
        symbolName = new DefaultMutableTreeNode(new SymbolType("LOGMARS", "BARCODE_LOGMARS", 50));
        symbolSubType.add(symbolName);
        
        symbolName = new DefaultMutableTreeNode(new SymbolType("Code 128", "BARCODE_CODE128", 20));
        symbolType.add(symbolName);        
        
        symbolName = new DefaultMutableTreeNode(new SymbolType("European Article Number", "BARCODE_EANX", 13));
        symbolType.add(symbolName);
        
        symbolName = new DefaultMutableTreeNode(new SymbolType("MSI Plessey", "BARCODE_MSI_PLESSEY", 47));
        symbolType.add(symbolName);
        
        symbolSubType = new DefaultMutableTreeNode("Telepen");
        symbolType.add(symbolSubType);
        
        symbolName = new DefaultMutableTreeNode(new SymbolType("Alpha", "BARCODE_TELEPEN", 32));
        symbolSubType.add(symbolName);
        
        symbolName = new DefaultMutableTreeNode(new SymbolType("Numeric", "BARCODE_TELEPEN_NUM", 87));
        symbolSubType.add(symbolName);        
        
        symbolSubType = new DefaultMutableTreeNode("Universal Product Code");
        symbolType.add(symbolSubType);
        
        symbolName = new DefaultMutableTreeNode(new SymbolType("Version A", "BARCODE_UPCA", 34));
        symbolSubType.add(symbolName);
        
        symbolName = new DefaultMutableTreeNode(new SymbolType("Version E", "BARCODE_UPCE", 37));
        symbolSubType.add(symbolName);
        
        symbolType = new DefaultMutableTreeNode("Stacked");
        top.add(symbolType);
        
        symbolName = new DefaultMutableTreeNode(new SymbolType("Codablock-F", "BARCODE_CODABLOCKF", 74));
        symbolType.add(symbolName); 
        
        symbolName = new DefaultMutableTreeNode(new SymbolType("Code 16K", "BARCODE_CODE16K", 23));
        symbolType.add(symbolName);
        
        symbolName = new DefaultMutableTreeNode(new SymbolType("Code 49", "BARCODE_CODE49", 24));
        symbolType.add(symbolName);    
        
        symbolName = new DefaultMutableTreeNode(new SymbolType("PDF417", "BARCODE_PDF417", 55));
        symbolType.add(symbolName);
        
        symbolName = new DefaultMutableTreeNode(new SymbolType("PDF417 Truncated", "BARCODE_PDF417TRUNC", 56));
        symbolType.add(symbolName);
        
        symbolName = new DefaultMutableTreeNode(new SymbolType("Micro PDF417", "BARCODE_MICROPDF417", 84));
        symbolType.add(symbolName);
        
        symbolType = new DefaultMutableTreeNode("Two-Dimensional");
        top.add(symbolType);        
        
        symbolName = new DefaultMutableTreeNode(new SymbolType("Aztec Code", "BARCODE_AZTEC", 92));
        symbolType.add(symbolName);            
        
        symbolName = new DefaultMutableTreeNode(new SymbolType("Aztec Runes", "BARCODE_AZRUNE", 128));
        symbolType.add(symbolName);            
        
        symbolName = new DefaultMutableTreeNode(new SymbolType("Data Matrix", "BARCODE_DATAMATRIX", 71));
        symbolType.add(symbolName);   
        
        symbolName = new DefaultMutableTreeNode(new SymbolType("Code One", "BARCODE_CODEONE", 141));
        symbolType.add(symbolName);           
        
        symbolName = new DefaultMutableTreeNode(new SymbolType("Grid Matrix", "BARCODE_GRIDMATRIX", 142));
        symbolType.add(symbolName);              
        
        symbolName = new DefaultMutableTreeNode(new SymbolType("Maxicode", "BARCODE_MAXICODE", 57));
        symbolType.add(symbolName);                  
        
        symbolName = new DefaultMutableTreeNode(new SymbolType("QR Code", "BARCODE_QRCODE", 58));
        symbolType.add(symbolName);    
        
        symbolName = new DefaultMutableTreeNode(new SymbolType("Micro QR Code", "BARCODE_MICROQR", 97));
        symbolType.add(symbolName);         
        
        symbolType = new DefaultMutableTreeNode("GS1 DataBar");
        top.add(symbolType);
        
        symbolName = new DefaultMutableTreeNode(new SymbolType("DB-14", "BARCODE_RSS14", 29));
        symbolType.add(symbolName);
        
        symbolName = new DefaultMutableTreeNode(new SymbolType("DB-14 Stacked", "BARCODE_RSS14STACK", 79));
        symbolType.add(symbolName);
        
        symbolName = new DefaultMutableTreeNode(new SymbolType("DB-14 Stacked Omni", "BARCODE_RSS14STACK_OMNI", 80));
        symbolType.add(symbolName);                
        
        symbolName = new DefaultMutableTreeNode(new SymbolType("Limited", "BARCODE_RSS_LTD", 30));
        symbolType.add(symbolName);
        
        symbolName = new DefaultMutableTreeNode(new SymbolType("Expanded", "BARCODE_RSS_EXP", 31));
        symbolType.add(symbolName);
        
        symbolName = new DefaultMutableTreeNode(new SymbolType("Expanded Stacked", "BARCODE_RSS_EXPSTACK", 81));
        symbolType.add(symbolName);
        
        symbolType = new DefaultMutableTreeNode("Postal");
        top.add(symbolType);
        
        symbolSubType = new DefaultMutableTreeNode("Australia Post");
        symbolType.add(symbolSubType);   
        
        symbolName = new DefaultMutableTreeNode(new SymbolType("Customer", "BARCODE_AUSPOST", 63));
        symbolSubType.add(symbolName);
        
        symbolName = new DefaultMutableTreeNode(new SymbolType("Reply Paid", "BARCODE_AUSREPLY", 66));
        symbolSubType.add(symbolName);
        
        symbolName = new DefaultMutableTreeNode(new SymbolType("Routing", "BARCODE_AUSROUTE", 67));
        symbolSubType.add(symbolName);       
        
        symbolName = new DefaultMutableTreeNode(new SymbolType("Redirect", "BARCODE_AUSREDIRECT", 68));
        symbolSubType.add(symbolName);        
        
        symbolSubType = new DefaultMutableTreeNode("Deutsche Post");
        symbolType.add(symbolSubType);
        
        symbolName = new DefaultMutableTreeNode(new SymbolType("Leitcode", "BARCODE_DPLEIT", 21));
        symbolSubType.add(symbolName);
        
        symbolName = new DefaultMutableTreeNode(new SymbolType("Identcode", "BARCODE_DPIDENT", 22));
        symbolSubType.add(symbolName);
        
        symbolName = new DefaultMutableTreeNode(new SymbolType("Dutch Post KIX", "BARCODE_KIX", 90));
        symbolType.add(symbolName);     
        
        symbolName = new DefaultMutableTreeNode(new SymbolType("Japan Post", "BARCODE_JAPANPOST", 76));
        symbolType.add(symbolName);     
        
        symbolName = new DefaultMutableTreeNode(new SymbolType("Korea Post", "BARCODE_KOREAPOST", 77));
        symbolType.add(symbolName);         
        
        symbolName = new DefaultMutableTreeNode(new SymbolType("Royal Mail", "BARCODE_RM4SCC", 70));
        symbolType.add(symbolName);             
        
        symbolSubType = new DefaultMutableTreeNode("USPS");
        symbolType.add(symbolSubType);          
        
        symbolName = new DefaultMutableTreeNode(new SymbolType("OneCode", "BARCODE_ONECODE", 85));
        symbolSubType.add(symbolName);
        
        symbolName = new DefaultMutableTreeNode(new SymbolType("PostNet", "BARCODE_POSTNET", 40));
        symbolSubType.add(symbolName);       
        
        symbolName = new DefaultMutableTreeNode(new SymbolType("PLANET", "BARCODE_PLANET", 82));
        symbolSubType.add(symbolName);         
        
        symbolType = new DefaultMutableTreeNode("Medical");
        top.add(symbolType);
        
        symbolName = new DefaultMutableTreeNode(new SymbolType("Code 32", "BARCODE_CODE32", 129));
        symbolType.add(symbolName);
        
        symbolSubType = new DefaultMutableTreeNode("HIBC");
        symbolType.add(symbolSubType);
        
        symbolName = new DefaultMutableTreeNode(new SymbolType("Aztec Code", "BARCODE_HIBC_AZTEC", 112));
        symbolSubType.add(symbolName);        
        
        symbolName = new DefaultMutableTreeNode(new SymbolType("Codablock-F", "BARCODE_HIBC_BLOCKF", 111));
        symbolSubType.add(symbolName);        
        
        symbolName = new DefaultMutableTreeNode(new SymbolType("Code 39", "BARCODE_HIBC_39", 99));
        symbolSubType.add(symbolName);
        
        symbolName = new DefaultMutableTreeNode(new SymbolType("Code 128", "BARCODE_HIBC_128", 98));
        symbolSubType.add(symbolName);        
        
        symbolName = new DefaultMutableTreeNode(new SymbolType("Data Matrix", "BARCODE_HIBC_DM", 102));
        symbolSubType.add(symbolName);        
        
        symbolName = new DefaultMutableTreeNode(new SymbolType("PDF417", "BARCODE_HIBC_PDF", 106));
        symbolSubType.add(symbolName);
        
        symbolName = new DefaultMutableTreeNode(new SymbolType("Micro PDF417", "BARCODE_HIBC_MICPDF", 108));
        symbolSubType.add(symbolName);
        
        symbolName = new DefaultMutableTreeNode(new SymbolType("QR Code", "BARCODE_HIBC_QR", 104));
        symbolSubType.add(symbolName);      
        
        symbolSubType = new DefaultMutableTreeNode("Pharmacode");
        symbolType.add(symbolSubType);
        
        symbolName = new DefaultMutableTreeNode(new SymbolType("One Track", "BARCODE_PHARMA", 51));
        symbolSubType.add(symbolName);
        
        symbolName = new DefaultMutableTreeNode(new SymbolType("Two Track", "BARCODE_PHARMA_TWO", 53));
        symbolSubType.add(symbolName);
                
        symbolName = new DefaultMutableTreeNode(new SymbolType("PZN", "BARCODE_PZN", 52));
        symbolType.add(symbolName);                
    }
    
    public static void setData(String newData) {
        dataInputField.setText(newData);
    }
    
    public static String getData() {
        return dataInputField.getText();
    }
    
    @Override
    public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                tree.getLastSelectedPathComponent();
        SymbolType selectedSymbol;
        Object nodeInfo;
        
        if (node != null) {
            nodeInfo = node.getUserObject();
            if (node.isLeaf()) {
                selectedSymbol = (SymbolType)nodeInfo;
                symbology = selectedSymbol.mnemonic;
                dataInput = dataInputField.getText().toString();
                compositeInput = compositeInputField.getText().toString();
                encodeData();       
            }
        }
    }
    
    public void encodeData() {
        int pWidth;
        int pHeight;
        double bWidth;
        double bHeight;
        reset();
        Encoder en = new Encoder();
        DrawSymbol drawSymbol = new DrawSymbol();
        SaveSymbol saveSymbol = new SaveSymbol();

        topPanel.removeAll();
        savePanel.removeAll();        
        
        if (dataInput.isEmpty()) {
            topPanel.add(errorLabel);
            errorLabel.setText("No input data");
            topPanel.updateUI();
            return;
        }
        
        if (!(en.encodeMe())) {
            topPanel.add(errorLabel);
            errorLabel.setText(errorOutput);
            topPanel.updateUI();
            return;
        } else {
            errorLabel.setText("");
        }

        pWidth = topPanel.getWidth();
        bWidth = pWidth / width;

        pHeight = topPanel.getHeight();
        bHeight = pHeight / height;

        if (bWidth < bHeight) {
            factor = (int) bWidth;
        } else {
            factor = (int) bHeight;
        }

        topPanel.add(drawSymbol);
        savePanel.setSize(saveSymbol.getPreferredSize());
        savePanel.setBorder(BorderFactory.createEmptyBorder());
        savePanel.setBackground(Color.white);
        savePanel.add(saveSymbol);
        topPanel.updateUI();
        pack();
    }
}
