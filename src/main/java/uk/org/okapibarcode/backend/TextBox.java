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

/**
 * A simple text item class
 *
 * @author Robin Stuart <rstuart114@gmail.com>
 */
public class TextBox {
    public double xPos;
    public double yPos;
    public String arg;

    public void TextBox() {
        xPos = 0.0;
        yPos = 0.0;
        arg = "";
    }

    public void setvalues(double x, double y, String a) {
        xPos = x;
        yPos = y;
        arg = a;
    }

    public void printvalues() {
        System.out.println("Text  X:" + xPos + " Y:" + yPos + " A:" + arg);
    }
}
