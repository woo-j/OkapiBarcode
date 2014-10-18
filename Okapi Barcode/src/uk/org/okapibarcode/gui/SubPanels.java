package uk.org.okapibarcode.gui;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

/**
 *
 * @author Robert Elliott <jakel2006@me.com>
 */
public class SubPanels extends JPanel implements ItemListener {

    public JTextField tAddon = new JTextField();
    JCheckBox cm = new JCheckBox("Addon");
    JLabel l = new JLabel("Add");

    public void getSubPanels() {
        if (MainInterface.symbology != null) {
            switch (MainInterface.symbology){
                case "UPCA":
                    upcean();
                    break;
            }
        }
    }
    
    public void upcean()
    {
        tAddon.setVisible(false);
        tAddon.setColumns(10);
        cm.setSelected(false);
        cm.addItemListener(this);
        add(cm);
        add(tAddon);
    }

    public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            tAddon.setVisible(true);
            updateUI();
        }

        if (e.getStateChange() == ItemEvent.DESELECTED) {
            tAddon.setText("");
            tAddon.setVisible(false);
            updateUI();
        }
    }
}
