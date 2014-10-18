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

/**
 *
 * @author Robert Elliott <jakel2006@me.com>
 */
public class DrawBarcode {
    Squares sqs = new Squares();
    int x;
    int y;
    int w;
    int h;
    int i = 0;
    int p = 0;

    public void drawMe() {
        MainInterface.subPanel = false;
        System.out.println("Barcode width: " + MainInterface.width * MainInterface.factor);
        System.out.println("---++++----");
        for (i = 0; i != MainInterface.bcs.size(); i++) {
            x = (int)MainInterface.bcs.get(i).x * MainInterface.factor;
            y = (int)MainInterface.bcs.get(i).y * MainInterface.factor;
            h = (int)MainInterface.bcs.get(i).height * MainInterface.factor;
            w = (int)MainInterface.bcs.get(i).width * MainInterface.factor;
            //System.out.println(x + " " + y + " " + w + " " + h);
            sqs.addSquare(x, y, w, h);
        }
    }
}
