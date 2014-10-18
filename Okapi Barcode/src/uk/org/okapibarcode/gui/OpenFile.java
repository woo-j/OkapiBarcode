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
