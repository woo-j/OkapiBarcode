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
import java.awt.*;
import java.awt.Font.*;
import java.awt.event.*;
import java.awt.font.TextAttribute;
import java.util.*;
import java.text.*;

/**
 *
 * @author jakel2006@me.com
 */
public class Squares extends JPanel {

    private java.util.List<Rectangle> squares = new ArrayList<Rectangle>();

    public void addSquare(int x, int y, int width, int height) {
        Rectangle rect = new Rectangle(x, y, width, height);
        squares.add(rect);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(MainInterface.width*MainInterface.factor, MainInterface.height*MainInterface.factor);
    }

    @Override
    protected void paintComponent(Graphics g) {
        this.setBackground(Color.white);
        super.paintComponent(g);
        Map<TextAttribute, Object> attributes = new HashMap<TextAttribute, Object>();
        attributes.put(TextAttribute.TRACKING, 0);
        Font f = new Font("Arial", Font.PLAIN, 10*(int)(MainInterface.factor*0.9)).deriveFont(attributes);

        Graphics2D g2 = (Graphics2D) g;
        for (Rectangle rect : squares) {
            g2.setColor(Color.BLACK);
            g2.fill(rect);
        }
        
        for (int i = 0; i < MainInterface.txt.size(); i++){
            g2.setFont(f);
             g2.drawString(MainInterface.txt.get(i).arg, (float)MainInterface.txt.get(i).xPos*MainInterface.factor, (float)MainInterface.txt.get(i).yPos*MainInterface.factor);
        }  
    }
}
