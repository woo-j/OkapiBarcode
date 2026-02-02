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

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="mailto:rstuart114@gmail.com">Robin Stuart</a>
 */
public class ReedSolomon {

    private static final Map< Key, ReedSolomon > INSTANCES = new ConcurrentHashMap<>();

    private int rlen;
    private short logmod;
    private short[] logt;
    private short[] alog;
    private short[] rspoly;

    public static ReedSolomon get(int poly, int nsym, int index, boolean cache) {
        if (cache) {
            Key key = new Key(poly, nsym, index);
            return INSTANCES.computeIfAbsent(key, k -> new ReedSolomon(k.poly, k.nsym, k.index));
        } else {
            return new ReedSolomon(poly, nsym, index);
        }
    }

    private ReedSolomon(int poly, int nsym, int index) {
        init_gf(poly);
        init_code(nsym, index);
    }

    private void init_gf(int poly) {
        // Find the top bit, and hence the symbol size
        // Ensure size is small enough to fit in short[]
        int leading = Integer.numberOfLeadingZeros(poly);
        int m = 31 - leading;
        int b = 1 << m;
        if (m > 12) {
            throw new OkapiInternalException("Expected 12 bits or fewer, but got " + m);
        }
        // Calculate the log / alog tables
        logmod = (short) (b - 1);
        logt = new short[logmod + 1];
        alog = new short[logmod];
        for (short p = 1, v = 0; v < logmod; v++) {
            alog[v] = p;
            logt[p] = v;
            p <<= 1;
            if ((p & b) != 0) {
                p ^= poly;
            }
        }
    }

    private void init_code(int nsym, int index) {
        rlen = nsym;
        rspoly = new short[nsym + 1];
        rspoly[0] = 1;
        for (int i = 1; i <= nsym; i++) {
            rspoly[i] = 1;
            for (int k = i - 1; k > 0; k--) {
                if (rspoly[k] != 0) {
                    rspoly[k] = alog[(logt[rspoly[k]] + index) % logmod];
                }
                rspoly[k] ^= rspoly[k - 1];
            }
            rspoly[0] = alog[(logt[rspoly[0]] + index) % logmod];
            index++;
        }
    }

    public int[] encode(int len, int[] data) {
        int[] res = new int[rlen];
        for (int i = 0; i < len; i++) {
            int m = res[rlen - 1] ^ data[i];
            for (int k = rlen - 1; k > 0; k--) {
                if (m != 0 && rspoly[k] != 0) {
                    res[k] = res[k - 1] ^ alog[(logt[m] + logt[rspoly[k]]) % logmod];
                } else {
                    res[k] = res[k - 1];
                }
            }
            if (m != 0 && rspoly[0] != 0) {
                res[0] = alog[(logt[m] + logt[rspoly[0]]) % logmod];
            } else {
                res[0] = 0;
            }
        }
        return res;
    }

    private static final class Key {
        private final int poly;
        private final int nsym;
        private final int index;
        public Key(int poly, int nsym, int index) {
            this.poly = poly;
            this.nsym = nsym;
            this.index = index;
        }
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Key)) {
                return false;
            }
            Key other = (Key) obj;
            return poly == other.poly && nsym == other.nsym && index == other.index;
        }
        @Override
        public int hashCode() {
            return Objects.hash(poly, nsym, index);
        }
    }
}
