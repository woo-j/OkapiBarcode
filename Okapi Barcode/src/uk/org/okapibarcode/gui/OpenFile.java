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
package uk.org.okapibarcode.gui;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;

/**
 * Open a file and read contents
 * 
 * @author Robin Stuart <rstuart114@gmail.com>
 */
public class OpenFile {
    public static String ReadFile (File file, boolean isBatch) throws IOException {
        String file_data = "";
        FileInputStream fis = new FileInputStream(file);
        int count;
        
        if (file.isFile() && file.canRead()) {
            if (isBatch) {
                for (count = 0; count < file.length(); count++) {
                    file_data += (char) fis.read();
                }
            } else {
                // Limit size of input
                if (file.length() < 3000) {
                    for (count = 0; count < file.length(); count++) {
                        file_data += (char) fis.read();
                    }
                } else {
                    System.out.println("Input file too big");
                }
            }
        } else {
            System.out.println("I/O Error");
        }
        return file_data;
    }
}
