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

/**
 * Simple container for symbology information
 *
 * @author <a href="mailto:rstuart114@gmail.com">Robin Stuart</a>
 */
public class SymbolType {
    public enum Encoding { CHANNEL_CODE, CODABAR, CODE_11, CODE25_MATRIX,
        CODE25_IATA, CODE25_INDUSTRY, CODE25_INTERLEAVED, CODE25_DATALOGIC, ITF14,
        CODE39, CODE39_EXTENDED, CODE93, DOD_LOGMARS, CODE_128, NVE18, EAN,
        MSI_PLESSEY, TELEPEN, TELEPEN_NUMERIC, UPC_A, UPC_E, CODABLOCK_F, CODE16K,
        CODE49, PDF417, PDF417_TRUNCATED, PDF417_MICRO, AZTEC, AZTEC_RUNE,
        DATAMATRIX, CODE_ONE, GRIDMATRIX, MAXICODE, QR, QR_MICRO,
        DB14, DB14_STACKED, DB14_STACKED_OMNIDIRECT, DB_LIMITED,
        DB_EXPANDED, DB_EXPANDED_STACKED, AUSPOST, AUSPOST_REPLY,
        AUSPOST_REROUTE, AUSPOST_REDIRECT, BRAZIL_CEPNET, DP_LEITCODE,
        DP_IDENTCODE, KIX_CODE, JAPAN_POST, KOREA_POST, RM4SCC, USPS_IMAIL,
        CODE39_HIBC, USPS_POSTNET, USPS_PLANET, CODE_32, AZTEC_HIBC, CODABLOCK_HIBC,
        CODE_128_HIBC, DATAMATRIX_HIBC, PDF417_HIBC, PDF417_MICRO_HIBC,
        QR_HIBC, PHARMA, PHARMA_TWOTRACK, PZN
    } 

    String guiLabel; // GUI interface name
    Encoding symbology;
    
    public SymbolType(String label, Encoding encoding) {
        guiLabel = label;
        symbology = encoding;
    }

    @Override
    public String toString(){
        return guiLabel;
    }
}
