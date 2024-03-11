/*
 * Copyright 2024 Daniel Gredler
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
 * UPN QR is a specialized type of QR Code symbol used by the Bank Association of Slovenia for their
 * Universal Payment Order. It is mostly a spec-compliant QR Code, but it must use error correction
 * level M, it must use version 15 (size 77x77), it must use ECI 4 (ISO-8859-2), and data must be
 * encoded in byte mode.
 *
 * @author Daniel Gredler
 * @see <a href="https://upn-qr.si/uploads/files/EN_Tehnicni%20standard%20UPN%20QR.pdf">UPN QR Form Specification, Section 5</a>
 */
public class UpnQr extends QrCode {

    public UpnQr() {
        eciMode = 4;
        preferredVersion = 15;
        preferredEccLevel = EccLevel.M;
        improveEccLevelIfPossible = false;
        forceByteCompaction = true;
    }

    @Override
    public void setEciMode(int eciMode) {
        if (eciMode != 4) {
            throw new OkapiInputException("UPN QR requires ECI mode 4");
        }
        super.setEciMode(eciMode);
    }

    @Override
    public void setPreferredVersion(int version) {
        if (version != 15) {
            throw new OkapiInputException("UPN QR requires version 15");
        }
        super.setPreferredVersion(version);
    }

    @Override
    public void setPreferredEccLevel(EccLevel preferredEccLevel) {
        if (preferredEccLevel != EccLevel.M) {
            throw new OkapiInputException("UPN QR requires ECC level M");
        }
        super.setPreferredEccLevel(preferredEccLevel);
    }

    @Override
    public void setForceByteCompaction(boolean forceByteCompaction) {
        if (!forceByteCompaction) {
            throw new OkapiInputException("UPN QR requires forced byte compaction");
        }
        super.setForceByteCompaction(forceByteCompaction);
    }
}
