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
