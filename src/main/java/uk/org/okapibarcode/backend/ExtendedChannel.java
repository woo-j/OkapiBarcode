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
package uk.org.okapibarcode.backend;

import java.io.UnsupportedEncodingException;

/**
 * Scan and encode data using Extended Channel Interpretations according
 * to AIM ITS/04-023, 15th July 2004
 *
 * @author Robin Stuart <rstuart114@gmail.com>
 */
public class ExtendedChannel {
    
    private int eciMode;
    private byte[] outputData = null;
    
    /**
     * Get calculated optimal ECI mode for encoded data
     * @return ECI mode
     */
    public int getMode() {
        return eciMode;
    }
    
    private static final String ISO_2 = "[\u0000-\u009f" +
            "\u00A0" +
            "\u0104" +
            "\u02D8" +
            "\u0141" +
            "\u00A4" +
            "\u013D" +
            "\u015A" +
            "\u00A7" +
            "\u00A8" +
            "\u0160" +
            "\u015E" +
            "\u0164" +
            "\u0179" +
            "\u00AD" +
            "\u017D" +
            "\u017B" +
            "\u00B0" +
            "\u0105" +
            "\u02DB" +
            "\u0142" +
            "\u00B4" +
            "\u013E" +
            "\u015B" +
            "\u02C7" +
            "\u00B8" +
            "\u0161" +
            "\u015F" +
            "\u0165" +
            "\u017A" +
            "\u02DD" +
            "\u017E" +
            "\u017C" +
            "\u0154" +
            "\u00C1" +
            "\u00C2" +
            "\u0102" +
            "\u00C4" +
            "\u0139" +
            "\u0106" +
            "\u00C7" +
            "\u010C" +
            "\u00C9" +
            "\u0118" +
            "\u00CB" +
            "\u011A" +
            "\u00CD" +
            "\u00CE" +
            "\u010E" +
            "\u0110" +
            "\u0143" +
            "\u0147" +
            "\u00D3" +
            "\u00D4" +
            "\u0150" +
            "\u00D6" +
            "\u00D7" +
            "\u0158" +
            "\u016E" +
            "\u00DA" +
            "\u0170" +
            "\u00DC" +
            "\u00DD" +
            "\u0162" +
            "\u00DF" +
            "\u0155" +
            "\u00E1" +
            "\u00E2" +
            "\u0103" +
            "\u00E4" +
            "\u013A" +
            "\u0107" +
            "\u00E7" +
            "\u010D" +
            "\u00E9" +
            "\u0119" +
            "\u00EB" +
            "\u011B" +
            "\u00ED" +
            "\u00EE" +
            "\u010F" +
            "\u0111" +
            "\u0144" +
            "\u0148" +
            "\u00F3" +
            "\u00F4" +
            "\u0151" +
            "\u00F6" +
            "\u00F7" +
            "\u0159" +
            "\u016F" +
            "\u00FA" +
            "\u0171" +
            "\u00FC" +
            "\u00FD" +
            "\u0163" +
            "\u02D9" +
            "]+";
    
    private static final String ISO_3 = "[\u0000-\u009f" +
            "\u00A0" +	
            "\u0126" +	
            "\u02D8" +	
            "\u00A3" +	
            "\u00A4" +	
            "\u0124" +	
            "\u00A7" +	
            "\u00A8" +	
            "\u0130" +	
            "\u015E" +	
            "\u011E" +	
            "\u0134" +	
            "\u00AD" +	
            "\u017B" +	
            "\u00B0" +	
            "\u0127" +	
            "\u00B2-\u00B5" +	
            "\u0125" +	
            "\u00B7" +	
            "\u00B8" +	
            "\u0131" +	
            "\u015F" +	
            "\u011F" +	
            "\u0135" +	
            "\u00BD" +	
            "\u017C" +	
            "\u00C0" +	
            "\u00C1" +	
            "\u00C2" +	
            "\u00C4" +	
            "\u010A" +	
            "\u0108" +	
            "\u00C7-\u00CF" +	
            "\u00D1-\u00D4" +		
            "\u0120" +	
            "\u00D6" +	
            "\u00D7" +	
            "\u011C" +	
            "\u00D9" +	
            "\u00DA" +	
            "\u00DB" +	
            "\u00DC" +	
            "\u016C" +	
            "\u015C" +	
            "\u00DF" +	
            "\u00E0" +	
            "\u00E1" +	
            "\u00E2" +	
            "\u00E4" +	
            "\u010B" +	
            "\u0109" +	
            "\u00E7-\u00EF" +	
            "\u00F1-\u00F4" +	
            "\u0121" +	
            "\u00F6" +	
            "\u00F7" +	
            "\u011D" +	
            "\u00F9" +	
            "\u00FA" +	
            "\u00FB" +	
            "\u00FC" +	
            "\u016D" +	
            "\u015D" +	
            "\u02D9" +
            "]+";
    
    private static final String ISO_4 = "[\u0000-\u009f" +
            "\u00A0" +	
            "\u0104" +	
            "\u0138" +	
            "\u0156" +	
            "\u00A4" +	
            "\u0128" +	
            "\u013B" +	
            "\u00A7" +	
            "\u00A8" +	
            "\u0160" +	
            "\u0112" +	
            "\u0122" +	
            "\u0166" +	
            "\u00AD" +	
            "\u017D" +	
            "\u00AF" +	
            "\u00B0" +	
            "\u0105" +	
            "\u02DB" +	
            "\u0157" +	
            "\u00B4" +	
            "\u0129" +	
            "\u013C" +	
            "\u02C7" +	
            "\u00B8" +	
            "\u0161" +	
            "\u0113" +	
            "\u0123" +	
            "\u0167" +	
            "\u014A" +	
            "\u017E" +	
            "\u014B" +	
            "\u0100" +	
            "\u00C1-\u00C6" +	
            "\u012E" +	
            "\u010C" +	
            "\u00C9" +	
            "\u0118" +	
            "\u00CB" +	
            "\u0116" +	
            "\u00CD" +	
            "\u00CE" +	
            "\u012A" +	
            "\u0110" +	
            "\u0145" +	
            "\u014C" +	
            "\u0136" +	
            "\u00D4" +	
            "\u00D5" +	
            "\u00D6" +	
            "\u00D7" +	
            "\u00D8" +	
            "\u0172" +	
            "\u00DA" +	
            "\u00DB" +	
            "\u00DC" +	
            "\u0168" +	
            "\u016A" +	
            "\u00DF" +	
            "\u0101" +	
            "\u00E1-\u00E6" +	
            "\u012F" +	
            "\u010D" +	
            "\u00E9" +	
            "\u0119" +	
            "\u00EB" +	
            "\u0117" +	
            "\u00ED" +	
            "\u00EE" +	
            "\u012B" +	
            "\u0111" +	
            "\u0146" +	
            "\u014D" +	
            "\u0137" +	
            "\u00F4-\u00F8" +		
            "\u0173" +	
            "\u00FA" +	
            "\u00FB" +	
            "\u00FC" +	
            "\u0169" +	
            "\u016B" +	
            "\u02D9" +	
            "]+";
    
    private static final String ISO_5 = "[\u0000-\u009f" +
            "\u00A0" +	
            "\u0401-\u040C" +	
            "\u00AD" +	
            "\u040E-\u044F" +		
            "\u2116" +	
            "\u0451-\u045C" +	
            "\u00A7" +	
            "\u045E" +	
            "\u045F" + 
            "]+";
    
    private static final String ISO_6 = "[\u0000-\u009F" +
            "\u00A0" +	
            "\u00A4" +	
            "\u060C" +	
            "\u00AD" +	
            "\u061B" +	
            "\u061F" +	
            "\u0621-\u063A" +	
            "\u0640-\u0652" +	
            "]+";
    
    private static final String ISO_7 = "[\u0000-\u009F" +
            "\u00A0" +	
            "\u2018" +	
            "\u2019" +	
            "\u00A3" +	
            "\u20AC" +	
            "\u20AF" +	
            "\u00A6" +	
            "\u00A7" +	
            "\u00A8" +	
            "\u00A9" +	
            "\u037A" +	
            "\u00AB" +	
            "\u00AC" +	
            "\u00AD" +	
            "\u2015" +	
            "\u00B0" +	
            "\u00B1" +	
            "\u00B2" +	
            "\u00B3" +	
            "\u0384" +	
            "\u0385" +	
            "\u0386" +	
            "\u00B7" +	
            "\u0388" +	
            "\u0389" +	
            "\u038A" +	
            "\u00BB" +	
            "\u038C" +	
            "\u00BD" +	
            "\u038E-\u03A1" +	
            "\u03A3-\u03CE" +
            "]+";
    
    private static final String ISO_8 = "[\u0000-\u009F" +
            "\u00A0-\u00B9" +	
            "\u00F7" +	
            "\u00BB" +	
            "\u00BC" +	
            "\u00BD" +	
            "\u00BE" +	
            "\u2017" +	
            "\u05D0-\u05EA" +	
            "\u200E" +	
            "\u200F" +
            "]+";
    
    private static final String ISO_9 = "[\u0000-\u00CF" +
            "\u011E" +
            "\u00D1-\u00DC" +
            "\u0130" +
            "\u015E" +
            "\u00DF-\u00EF" +
            "\u011F" +
            "\u00F1-\u00FC" +
            "\u0131" +
            "\u015F" +
            "\u00FF" +
            "]+";
    
    
    private static final String ISO_10 = "[\u0000-\u007F" +
            "\u00A0" +	
            "\u0104" +	
            "\u0112" +	
            "\u0122" +	
            "\u012A" +	
            "\u0128" +	
            "\u0136" +	
            "\u00A7" +	
            "\u013B" +	
            "\u0110" +	
            "\u0160" +	
            "\u0166" +	
            "\u017D" +	
            "\u00AD" +	
            "\u016A" +	
            "\u014A" +	
            "\u00B0" +	
            "\u0105" +	
            "\u0113" +	
            "\u0123" +	
            "\u012B" +	
            "\u0129" +	
            "\u0137" +	
            "\u00B7" +	
            "\u013C" +	
            "\u0111" +	
            "\u0161" +	
            "\u0167" +	
            "\u017E" +	
            "\u2015" +	
            "\u016B" +	
            "\u014B" +	
            "\u0100" +	
            "\u00C1-\u00C6" +	
            "\u012E" +	
            "\u010C" +	
            "\u00C9" +	
            "\u0118" +	
            "\u00CB" +	
            "\u0116" +	
            "\u00CD" +	
            "\u00CE" +	
            "\u00CF" +	
            "\u00D0" +	
            "\u0145" +	
            "\u014C" +	
            "\u00D3" +	
            "\u00D4" +	
            "\u00D5" +	
            "\u00D6" +	
            "\u0168" +	
            "\u00D8" +	
            "\u0172" +	
            "\u00DA-\u00DF" +	
            "\u0101" +	
            "\u00E1-\u00E6" +	
            "\u012F" +	
            "\u010D" +	
            "\u00E9" +	
            "\u0119" +	
            "\u00EB" +	
            "\u0117" +	
            "\u00ED" +	
            "\u00EE" +	
            "\u00EF" +	
            "\u00F0" +	
            "\u0146" +	
            "\u014D" +	
            "\u00F3" +	
            "\u00F4" +	
            "\u00F5" +	
            "\u00F6" +	
            "\u0169" +	
            "\u00F8" +	
            "\u0173" +	
            "\u00FA" +	
            "\u00FB" +	
            "\u00FC" +	
            "\u00FD" +	
            "\u00FE" +	
            "\u0138" +
            "]+";
    
    private static final String ISO_11 = "[\u0000-\u007E" +
            "\u00A0" +
            "\u0E01-\u0E3A" +
            "\u0E3F-\u0E5B" +
            "]+";
    
    private static final String ISO_13 = "[\u0000-\u007E" +
            "\u00A0" +	
            "\u201D" +	
            "\u00A2" +	
            "\u00A3" +	
            "\u00A4" +	
            "\u201E" +	
            "\u00A6" +	
            "\u00A7" +	
            "\u00D8" +	
            "\u00A9" +	
            "\u0156" +	
            "\u00AB" +	
            "\u00AC" +	
            "\u00AD" +	
            "\u00AE" +	
            "\u00C6" +	
            "\u00B0" +	
            "\u00B1" +	
            "\u00B2" +	
            "\u00B3" +	
            "\u201C" +	
            "\u00B5" +	
            "\u00B6" +	
            "\u00B7" +	
            "\u00F8" +	
            "\u00B9" +	
            "\u0157" +	
            "\u00BB" +	
            "\u00BC" +	
            "\u00BD" +	
            "\u00BE" +	
            "\u00E6" +	
            "\u0104" +	
            "\u012E" +	
            "\u0100" +	
            "\u0106" +	
            "\u00C4" +	
            "\u00C5" +	
            "\u0118" +	
            "\u0112" +	
            "\u010C" +	
            "\u00C9" +	
            "\u0179" +	
            "\u0116" +	
            "\u0122" +	
            "\u0136" +	
            "\u012A" +	
            "\u013B" +	
            "\u0160" +	
            "\u0143" +	
            "\u0145" +	
            "\u00D3" +	
            "\u014C" +	
            "\u00D5" +	
            "\u00D6" +	
            "\u00D7" +	
            "\u0172" +	
            "\u0141" +	
            "\u015A" +	
            "\u016A" +	
            "\u00DC" +	
            "\u017B" +	
            "\u017D" +	
            "\u00DF" +	
            "\u0105" +	
            "\u012F" +	
            "\u0101" +	
            "\u0107" +
            "\u00E4" +	
            "\u00E5" +	
            "\u0119" +	
            "\u0113" +	
            "\u010D" +	
            "\u00E9" +	
            "\u017A" +	
            "\u0117" +	
            "\u0123" +	
            "\u0137" +	
            "\u012B" +	
            "\u013C" +	
            "\u0161" +	
            "\u0144" +	
            "\u0146" +	
            "\u00F3" +	
            "\u014D" +	
            "\u00F5" +	
            "\u00F6" +	
            "\u00F7" +	
            "\u0173" +	
            "\u0142" +	
            "\u015B" +	
            "\u016B" +	
            "\u00FC" +	
            "\u017C" +	
            "\u017E" +	
            "\u2019" +	
            "]+";
            
    private static final String ISO_14 = "[\u0000-\u007E" +
            "\u00A0" +	
            "\u1E02" +	
            "\u1E03" +	
            "\u00A3" +	
            "\u010A" +	
            "\u010B" +	
            "\u1E0A" +	
            "\u00A7" +	
            "\u1E80" +	
            "\u00A9" +	
            "\u1E82" +	
            "\u1E0B" +	
            "\u1EF2" +	
            "\u00AD" +	
            "\u00AE" +	
            "\u0178" +	
            "\u1E1E" +	
            "\u1E1F" +	
            "\u0120" +	
            "\u0121" +	
            "\u1E40" +	
            "\u1E41" +	
            "\u00B6" +	
            "\u1E56" +	
            "\u1E81" +	
            "\u1E57" +	
            "\u1E83" +	
            "\u1E60" +	
            "\u1EF3" +	
            "\u1E84" +	
            "\u1E85" +	
            "\u1E61" +	
            "\u00C0-\u00CF" +
            "\u0174" +
            "\u00D1-\u00D6" +
            "\u1E6A" +
            "\u00D8-\u00DD" +
            "\u0176" +
            "\u00DF-\u00EF" +
            "\u0175" +
            "\u00F1-\u00F6" +
            "\u1E6B" +
            "\u00F8-\u00FD" +
            "\u0177" +
            "\u00FF" +
            "]+";
    
    private static final String ISO_15 = "[\u0000-\u00A3" +
            "\u20AC" +
            "\u00A5" +
            "\u0160" +
            "\u00A7" +
            "\u0161" +
            "\u00A9-\u00B3" +
            "\u017D" +
            "\u00B5" +
            "\u00B6" +
            "\u00B7" +
            "\u017E" +
            "\u00B9" +
            "\u00BA" +
            "\u00BB" +
            "\u0152" +
            "\u0153" +
            "\u0178" +
            "\u00BF-\u00FF" +
            "]+";
    
    private static final String ISO_16 = "[\u0000-\u009F" +
            "\u00A0" +	
            "\u0104" +	
            "\u0105" +	
            "\u0141" +	
            "\u20AC" +	
            "\u201E" +	
            "\u0160" +	
            "\u00A7" +	
            "\u0161" +	
            "\u00A9" +	
            "\u0218" +	
            "\u00AB" +	
            "\u0179" +	
            "\u00AD" +	
            "\u017A" +	
            "\u017B" +	
            "\u00B0" +	
            "\u00B1" +	
            "\u010C" +	
            "\u0142" +	
            "\u017D" +	
            "\u201D" +	
            "\u00B6" +	
            "\u00B7" +	
            "\u017E" +	
            "\u010D" +	
            "\u0219" +	
            "\u00BB" +	
            "\u0152" +	
            "\u0153" +	
            "\u0178" +	
            "\u017C" +	
            "\u00C0" +	
            "\u00C1" +	
            "\u00C2" +	
            "\u0102" +	
            "\u00C4" +	
            "\u0106" +	
            "\u00C6-\u00CF" +
            "\u0110" +	
            "\u0143" +	
            "\u00D2" +	
            "\u00D3" +	
            "\u00D4" +	
            "\u0150" +	
            "\u00D6" +	
            "\u015A" +	
            "\u0170" +	
            "\u00D9" +	
            "\u00DA" +	
            "\u00DB" +	
            "\u00DC" +	
            "\u0118" +	
            "\u021A" +	
            "\u00DF" +	
            "\u00E0" +	
            "\u00E1" +	
            "\u00E2" +	
            "\u0103" +	
            "\u00E4" +	
            "\u0107" +	
            "\u00E6-\u00EF" +	
            "\u0111" +
            "\u0144" +	
            "\u00F2" +	
            "\u00F3" +	
            "\u00F4" +	
            "\u0151" +	
            "\u00F6" +	
            "\u015B" +	
            "\u0171" +	
            "\u00F9" +	
            "\u00FA" +	
            "\u00FB" +	
            "\u00FC" +	
            "\u0119" +	
            "\u021B" +	
            "\u00FF" +	
            "]+";
    
    private static final String W_1250 = "[\u0000-\u007F" +
            "\u20AC" +
            "\u201A" +
            "\u201E" +
            "\u2026" +
            "\u2020" +
            "\u2021" +
            "\u2030" +
            "\u0160" +
            "\u2039" +
            "\u015A" +
            "\u0164" +
            "\u017D" +
            "\u0179" +
            "\u2018" +
            "\u2019" +
            "\u201C" +
            "\u201D" +
            "\u2022" +
            "\u2013" +
            "\u2014" +
            "\u2122" +
            "\u0161" +
            "\u203A" +
            "\u015B" +
            "\u0165" +
            "\u017E" +
            "\u017A" +
            "\u00A0" +
            "\u02C7" +
            "\u02D8" +
            "\u0141" +
            "\u00A4" +
            "\u0104" +
            "\u00A6" +
            "\u00A7" +
            "\u00A8" +
            "\u00A9" +
            "\u015E" +
            "\u00AB" +
            "\u00AC" +
            "\u00AD" +
            "\u00AE" +
            "\u017B" +
            "\u00B0" +
            "\u00B1" +
            "\u02DB" +
            "\u0142" +
            "\u00B4-\u00B8" +
            "\u0105" +
            "\u015F" +
            "\u00BB" +
            "\u013D" +
            "\u02DD" +
            "\u013E" +
            "\u017C" +
            "\u0154" +
            "\u00C1" +
            "\u00C2" +
            "\u0102" +
            "\u00C4" +
            "\u0139" +
            "\u0106" +
            "\u00C7" +
            "\u010C" +
            "\u00C9" +
            "\u0018" +
            "\u00CB" +
            "\u011A" +
            "\u00CD" +
            "\u00CE" +
            "\u010E" +
            "\u0110" +
            "\u0143" +
            "\u0147" +
            "\u00D3" +
            "\u00D4" +
            "\u0150" +
            "\u00D6" +
            "\u00D7" +
            "\u0158" +
            "\u016E" +
            "\u00DA" +
            "\u0170" +
            "\u00DC" +
            "\u00DD" +
            "\u0162" +
            "\u00DF" +
            "\u0155" +
            "\u00E1" +
            "\u00E2" +
            "\u0103" +
            "\u00E4" +
            "\u013A" +
            "\u0107" +
            "\u00E7" +
            "\u010D" +
            "\u00E9" +
            "\u0119" +
            "\u00E9" +
            "\u0119" +
            "\u00EB" +
            "\u011B" +
            "\u00ED" +
            "\u00EE" +
            "\u010F" +
            "\u0111" +
            "\u0144" +
            "\u0148" +
            "\u00F3" +
            "\u00F4" +
            "\u0151" +
            "\u00F6" +
            "\u00F7" +
            "\u0159" +
            "\u016F" +
            "\u00FA" +
            "\u0171" +
            "\u00FC" +
            "\u00FD" +
            "\u0163" +
            "\u02D9" +
            "]+";
    
    private static final String W_1251 = "[\u0000-\u007F" +
            "\u0402" +
            "\u0403" +
            "\u201A" +
            "\u0453" +
            "\u201E" +
            "\u2026" +
            "\u2020" +
            "\u2021" +
            "\u20AC" +
            "\u2030" +
            "\u0409" +
            "\u2039" +
            "\u040A" +
            "\u040C" +
            "\u040B" +
            "\u040F" +
            "\u0452" +
            "\u2018" +
            "\u2019" +
            "\u201C" +
            "\u201D" +
            "\u2022" +
            "\u2013" +
            "\u2014" +
            "\u2122" +
            "\u0459" +
            "\u203A" +
            "\u045A" +
            "\u045C" +
            "\u045B" +
            "\u045F" +
            "\u00A0" +
            "\u040E" +
            "\u045E" +
            "\u0408" +
            "\u00A4" +
            "\u0490" +
            "\u00A6" +
            "\u00A7" +
            "\u0401" +
            "\u00A9" +
            "\u0404" +
            "\u00AB-\u00AE" +
            "\u0407" +
            "\u00B0" +
            "\u00B1" +
            "\u0406" +
            "\u0456" +
            "\u0491" +
            "\u00B5" +
            "\u00B6" +
            "\u00B7" +
            "\u0451" +
            "\u2116" +
            "\u0454" +
            "\u00BB" +
            "\u0458" +
            "\u0405" +
            "\u0455" +
            "\u0457" +
            "\u0410-\u044F" +
            "]+";
    
    private static final String W_1252 = "[\u0000-\u007F" +
            "\u20AC" +
            "\u201A" +
            "\u0192" +
            "\u201E" +
            "\u2026" +
            "\u2020" +
            "\u2021" +
            "\u02C6" +
            "\u2030" +
            "\u0160" +
            "\u2039" +
            "\u0152" +
            "\u017D" +
            "\u2018" +
            "\u2019" +
            "\u201C" +
            "\u201D" +
            "\u2022" +
            "\u2013" +
            "\u2014" +
            "\u02DC" +
            "\u2122" +
            "\u0161" +
            "\u203A" +
            "\u0153" +
            "\u017E" +
            "\u0178" +
            "\u00A0-\u00FF" +
            "]+";
    
    private static final String W_1256 = "[\u0000-\u007F" +
            "\u20AC" +
            "\u067E" +
            "\u201A" +
            "\u0192" +
            "\u201E" +
            "\u2026" +
            "\u2020" +
            "\u2021" +
            "\u02C6" +
            "\u2030" +
            "\u0679" +
            "\u2039" +
            "\u0152" +
            "\u0686" +
            "\u0698" +
            "\u0688" +
            "\u06AF" +
            "\u2018" +
            "\u2019" +
            "\u201C" +
            "\u201D" +
            "\u2022" +
            "\u2013" +
            "\u2014" +
            "\u06A9" +
            "\u2122" +
            "\u0691" +
            "\u203A" +
            "\u0153" +
            "\u200C" +
            "\u200D" +
            "\u06BA" +
            "\u00A0" +
            "\u060C" +
            "\u00A2-\u00A9" +
            "\u06BE" +
            "\u00AB-\u00B9" +
            "\u061B" +
            "\u00BB-\u00BE" +
            "\u061F" +
            "\u06C1" +
            "\u0621-\u0636" +
            "\u00D7" +
            "\u0637-\u063A" +
            "\u0640-\u0643" +
            "\u00E0" +
            "\u0644" +
            "\u00E2" +
            "\u0645-\u0648" +
            "\u00E7-\u00EB" +
            "\u0649" +
            "\u064A" +
            "\u00EE" +
            "\u00EF" +
            "\u064B-\u064E" +
            "\u00F4" +
            "\u064F" +
            "\u0650" +
            "\u00F7" +
            "\u0651" +
            "\u00F9" +
            "\u0652" +
            "\u00FB" +
            "\u00FC" +
            "\u200E" +
            "\u200F" +
            "\u06D2" +
            "]+";
    
    /**
     * Scan data for compatability with ECI character sets, then encode
     * and return encoded data in optimised character set.
     * @param inputData String to be encoded
     * @return Byte data in optimised character set
     */
    public byte[] getBytes(String inputData) {
        /*
            Below is a list of Possible ECI encoding modes according to
            "International Technical Standard: Extended Channel Interpretations
            Part 3: Register", Document ITS/04-023, 15th July 2004.
            '+' denotes character sets detected in this class.
        
            00 - PDF417 Default GLI (Depreciated)
            01 - PDF417 Latin 1 GLI (Depreciated)
            02 - PDF417 Default ECI (Depreciated)
            03 + ISO/IEC 8859-1 Latin alphabet No. 1
            04 + ISO/IEC 8859-2 Latin alphabet No. 2
            05 + ISO/IEC 8859-3 Latin alphabet No. 3
            06 + ISO/IEC 8859-4 Latin alphabet No. 4
            07 + ISO/IEC 8859-5 Latin/Cyrillic alphabet
            08 + ISO/IEC 8859-6 Latin/Arabic alphabet
            09 + ISO/IEC 8859-7 Latin/Greek alphabet
            10 + ISO/IEC 8859-8 Latin/Hebrew alphabet
            11 + ISO/IEC 8859-9 Latin alphabet No. 5
            12 + ISO/IEC 8859-10 Latin alphabet No. 6
            13 + ISO/IEC 8859-11 Latin/Thai alphabet
            14 - Reserved
            15 + ISO/IEC 8859-13 Latin alphabet No. 7 (Baltic Rim)
            16 + ISO/IEC 8859-14 Latin alphabet No. 8 (Celtic)
            17 + ISO/IEC 8859-15 Latin alphabet No. 9
            18 + ISO/IEC 8859-16 Latin alphabet No. 10
            19 - Reserved
            20 - Shift JIS (JIS X 0208 Annex 1 + JIS X 0201)
            21 + Windows 1250 Latin 2 (Central Europe)
            22 + Windows 1251 Cyrillic
            23 + Windows 1252 Latin 1
            24 + Windows 1256 Arabic
            25 - ISO/IEC 10646 UCS-2
            26 + ISO/IEC 10646 UTF-8
            27 - ISO/IEC 646:1991
            28 - Big5 (Taiwan) Chinese Character Set
            29 - GB (PRC) Chinese Character Set
            30 - Korean Character Set
            899 - 8-bit binary data
        */
        
        /* ISO 8859-1 */
        if (inputData.matches("[\u0000-\u00ff]+")) {
            try {
                outputData = inputData.getBytes("ISO8859_1");
            } catch (UnsupportedEncodingException e) {
                outputData = null;
            }
        }
        
        if (outputData != null) {
            eciMode = 3;
            return outputData;
        }
        
        /* ISO 8859-2 */
        if(inputData.matches(ISO_2)) {
            try {
                outputData = inputData.getBytes("ISO8859_2");
            } catch (UnsupportedEncodingException e) {
                outputData = null;
            }
        }
        
        if (outputData != null) {
            eciMode = 4;
            return outputData;
        }
        
        /* ISO 8859-3 */
        if(inputData.matches(ISO_3)) {
            try {
                outputData = inputData.getBytes("ISO8859_3");
            } catch (UnsupportedEncodingException e) {
                outputData = null;
            }
        }
        
        if (outputData != null) {
            eciMode = 5;
            return outputData;
        }     
        
        /* ISO 8859-4 */
        if(inputData.matches(ISO_4)) {
            try {
                outputData = inputData.getBytes("ISO8859_4");
            } catch (UnsupportedEncodingException e) {
                outputData = null;
            }
        }
        
        if (outputData != null) {
            eciMode = 6;
            return outputData;
        }
        
        /* ISO 8859-5 */
        if(inputData.matches(ISO_5)) {
            try {
                outputData = inputData.getBytes("ISO8859_5");
            } catch (UnsupportedEncodingException e) {
                outputData = null;
            }
        }
        
        if (outputData != null) {
            eciMode = 7;
            return outputData;
        }
        
        /* ISO 8859-6 */
        if(inputData.matches(ISO_6)) {
            try {
                outputData = inputData.getBytes("ISO8859_6");
            } catch (UnsupportedEncodingException e) {
                outputData = null;
            }
        }
        
        if (outputData != null) {
            eciMode = 8;
            return outputData;
        }
        
        /* ISO 8859-7 */
        if(inputData.matches(ISO_7)) {
            try {
                outputData = inputData.getBytes("ISO8859_7");
            } catch (UnsupportedEncodingException e) {
                outputData = null;
            }
        }
        
        if (outputData != null) {
            eciMode = 9;
            return outputData;
        }     
        
        /* ISO 8859-8 */
        if(inputData.matches(ISO_8)) {
            try {
                outputData = inputData.getBytes("ISO8859_8");
            } catch (UnsupportedEncodingException e) {
                outputData = null;
            }
        }
        
        if (outputData != null) {
            eciMode = 10;
            return outputData;
        }
        
        /* ISO 8859-9 */
        if(inputData.matches(ISO_9)) {
            try {
                outputData = inputData.getBytes("ISO8859_9");
            } catch (UnsupportedEncodingException e) {
                outputData = null;
            }
        }
        
        if (outputData != null) {
            eciMode = 11;
            return outputData;
        }
        
        /* ISO 8859-10 */
        if(inputData.matches(ISO_10)) {
            ISO_8859_10 iso10 = new ISO_8859_10();
            outputData = iso10.getBytes(inputData);
        }
        
        if (outputData != null) {
            eciMode = 12;
            return outputData;
        }        
        
        /* ISO 8859-11 */
        if(inputData.matches(ISO_11)) {
            try {
                outputData = inputData.getBytes("x-iso-8859-11");
            } catch (UnsupportedEncodingException e) {
                outputData = null;
            }
        }
        
        if (outputData != null) {
            eciMode = 13;
            return outputData;
        }
        
        /* ISO 8859-13 */
        if(inputData.matches(ISO_13)) {
            try {
                outputData = inputData.getBytes("ISO8859_13");
            } catch (UnsupportedEncodingException e) {
                outputData = null;
            }
        }
        
        if (outputData != null) {
            eciMode = 15;
            return outputData;
        }
        
        /* ISO 8859-14 */
        if(inputData.matches(ISO_14)) {
            ISO_8859_14 iso14 = new ISO_8859_14();
            outputData = iso14.getBytes(inputData);
        }
        
        if (outputData != null) {
            eciMode = 16;
            return outputData;
        }
        
        /* ISO 8859-15 */
        if(inputData.matches(ISO_15)) {
            try {
                outputData = inputData.getBytes("ISO8859_15");
            } catch (UnsupportedEncodingException e) {
                outputData = null;
            }
        }
        
        if (outputData != null) {
            eciMode = 17;
            return outputData;
        }
        
        /* ISO 8859-16 */
        if(inputData.matches(ISO_16)) {
            ISO_8859_16 iso16 = new ISO_8859_16();
            outputData = iso16.getBytes(inputData);
        }
        
        if (outputData != null) {
            eciMode = 18;
            return outputData;
        }
        
        /* Windows-1250 */
        if(inputData.matches(W_1250)) {
            try {
                outputData = inputData.getBytes("Cp1250");
            } catch (UnsupportedEncodingException e) {
                outputData = null;
            }
        }
        
        if (outputData != null) {
            eciMode = 21;
            return outputData;
        }
        
        /* Windows-1251 */
        if(inputData.matches(W_1251)) {
            try {
                outputData = inputData.getBytes("Cp1251");
            } catch (UnsupportedEncodingException e) {
                outputData = null;
            }
        }
        
        if (outputData != null) {
            eciMode = 22;
            return outputData;
        }
        
        /* Windows-1252 */
        if(inputData.matches(W_1252)) {
            try {
                outputData = inputData.getBytes("Cp1252");
            } catch (UnsupportedEncodingException e) {
                outputData = null;
            }
        }
        
        if (outputData != null) {
            eciMode = 23;
            return outputData;
        }
        
        /* Windows-1256 */
        if(inputData.matches(W_1256)) {
            try {
                outputData = inputData.getBytes("Cp1256");
            } catch (UnsupportedEncodingException e) {
                outputData = null;
            }
        }
        
        if (outputData != null) {
            eciMode = 24;
            return outputData;
        }
        
        /* Default - UTF-8 */
        try {
            outputData = inputData.getBytes("UTF8");
        } catch (UnsupportedEncodingException e) {
            outputData = null;
            eciMode = 0;
        }
        
        eciMode = 26;
        return outputData;
    }
}
