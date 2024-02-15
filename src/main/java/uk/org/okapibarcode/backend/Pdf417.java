/*
 * Copyright 2014-2017 Robin Stuart, Daniel Gredler
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

import static uk.org.okapibarcode.util.Arrays.positionOf;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import uk.org.okapibarcode.util.EciMode;

/**
 * <p>Implements PDF417 bar code symbology and MicroPDF417 bar code symbology
 * according to ISO/IEC 15438:2006 and ISO/IEC 24728:2006 respectively.
 *
 * <p>PDF417 supports encoding up to the ISO standard maximum symbol size of 925
 * codewords which (at error correction level 0) allows a maximum data size
 * of 1850 text characters, or 2710 digits. The maximum size MicroPDF417 symbol
 * can hold 250 alphanumeric characters or 366 digits.
 *
 * @author <a href="mailto:rstuart114@gmail.com">Robin Stuart</a>
 * @author Daniel Gredler
 */
public class Pdf417 extends Symbol {

    public enum Mode {
        /** Normal PDF417. */
        NORMAL,
        /** Truncated PDF417. */
        TRUNCATED,
        /** MicroPDF417. */
        MICRO
    }

    private enum EncodingMode {
        FALSE, TEX, BYT, NUM
    }

    private int[] codeWords = new int[2700];
    private int codeWordCount;
    private Mode symbolMode = Mode.NORMAL;
    private boolean forceByteCompaction;
    private Integer columns;
    private Integer rows;
    private int preferredEccLevel = -1;
    private int structuredAppendFileId = 0;
    private int structuredAppendPosition = 1;
    private int structuredAppendTotal = 1;
    private String structuredAppendFileName;
    private boolean structuredAppendIncludeSegmentCount;

    private static final int MAX_NUMERIC_COMPACTION_BLOCK_SIZE = 44;

    private static final int[] COEFRS = {
        /* k = 2 */
        27, 917,

        /* k = 4 */
        522, 568, 723, 809,

        /* k = 8 */
        237, 308, 436, 284, 646, 653, 428, 379,

        /* k = 16 */
        274, 562, 232, 755, 599, 524, 801, 132, 295, 116, 442, 428, 295, 42, 176, 65,

        /* k = 32 */
        361, 575, 922, 525, 176, 586, 640, 321, 536, 742, 677, 742, 687, 284, 193, 517,
        273, 494, 263, 147, 593, 800, 571, 320, 803, 133, 231, 390, 685, 330, 63, 410,

        /* k = 64 */
        539, 422, 6, 93, 862, 771, 453, 106, 610, 287, 107, 505, 733, 877, 381, 612,
        723, 476, 462, 172, 430, 609, 858, 822, 543, 376, 511, 400, 672, 762, 283, 184,
        440, 35, 519, 31, 460, 594, 225, 535, 517, 352, 605, 158, 651, 201, 488, 502,
        648, 733, 717, 83, 404, 97, 280, 771, 840, 629, 4, 381, 843, 623, 264, 543,

        /* k = 128 */
        521, 310, 864, 547, 858, 580, 296, 379, 53, 779, 897, 444, 400, 925, 749, 415,
        822, 93, 217, 208, 928, 244, 583, 620, 246, 148, 447, 631, 292, 908, 490, 704,
        516, 258, 457, 907, 594, 723, 674, 292, 272, 96, 684, 432, 686, 606, 860, 569,
        193, 219, 129, 186, 236, 287, 192, 775, 278, 173, 40, 379, 712, 463, 646, 776,
        171, 491, 297, 763, 156, 732, 95, 270, 447, 90, 507, 48, 228, 821, 808, 898,
        784, 663, 627, 378, 382, 262, 380, 602, 754, 336, 89, 614, 87, 432, 670, 616,
        157, 374, 242, 726, 600, 269, 375, 898, 845, 454, 354, 130, 814, 587, 804, 34,
        211, 330, 539, 297, 827, 865, 37, 517, 834, 315, 550, 86, 801, 4, 108, 539,

        /* k = 256 */
        524, 894, 75, 766, 882, 857, 74, 204, 82, 586, 708, 250, 905, 786, 138, 720,
        858, 194, 311, 913, 275, 190, 375, 850, 438, 733, 194, 280, 201, 280, 828, 757,
        710, 814, 919, 89, 68, 569, 11, 204, 796, 605, 540, 913, 801, 700, 799, 137,
        439, 418, 592, 668, 353, 859, 370, 694, 325, 240, 216, 257, 284, 549, 209, 884,
        315, 70, 329, 793, 490, 274, 877, 162, 749, 812, 684, 461, 334, 376, 849, 521,
        307, 291, 803, 712, 19, 358, 399, 908, 103, 511, 51, 8, 517, 225, 289, 470,
        637, 731, 66, 255, 917, 269, 463, 830, 730, 433, 848, 585, 136, 538, 906, 90,
        2, 290, 743, 199, 655, 903, 329, 49, 802, 580, 355, 588, 188, 462, 10, 134,
        628, 320, 479, 130, 739, 71, 263, 318, 374, 601, 192, 605, 142, 673, 687, 234,
        722, 384, 177, 752, 607, 640, 455, 193, 689, 707, 805, 641, 48, 60, 732, 621,
        895, 544, 261, 852, 655, 309, 697, 755, 756, 60, 231, 773, 434, 421, 726, 528,
        503, 118, 49, 795, 32, 144, 500, 238, 836, 394, 280, 566, 319, 9, 647, 550,
        73, 914, 342, 126, 32, 681, 331, 792, 620, 60, 609, 441, 180, 791, 893, 754,
        605, 383, 228, 749, 760, 213, 54, 297, 134, 54, 834, 299, 922, 191, 910, 532,
        609, 829, 189, 20, 167, 29, 872, 449, 83, 402, 41, 656, 505, 579, 481, 173,
        404, 251, 688, 95, 497, 555, 642, 543, 307, 159, 924, 558, 648, 55, 497, 10,

        /* k = 512 */
        352, 77, 373, 504, 35, 599, 428, 207, 409, 574, 118, 498, 285, 380, 350, 492,
        197, 265, 920, 155, 914, 299, 229, 643, 294, 871, 306, 88, 87, 193, 352, 781,
        846, 75, 327, 520, 435, 543, 203, 666, 249, 346, 781, 621, 640, 268, 794, 534,
        539, 781, 408, 390, 644, 102, 476, 499, 290, 632, 545, 37, 858, 916, 552, 41,
        542, 289, 122, 272, 383, 800, 485, 98, 752, 472, 761, 107, 784, 860, 658, 741,
        290, 204, 681, 407, 855, 85, 99, 62, 482, 180, 20, 297, 451, 593, 913, 142,
        808, 684, 287, 536, 561, 76, 653, 899, 729, 567, 744, 390, 513, 192, 516, 258,
        240, 518, 794, 395, 768, 848, 51, 610, 384, 168, 190, 826, 328, 596, 786, 303,
        570, 381, 415, 641, 156, 237, 151, 429, 531, 207, 676, 710, 89, 168, 304, 402,
        40, 708, 575, 162, 864, 229, 65, 861, 841, 512, 164, 477, 221, 92, 358, 785,
        288, 357, 850, 836, 827, 736, 707, 94, 8, 494, 114, 521, 2, 499, 851, 543,
        152, 729, 771, 95, 248, 361, 578, 323, 856, 797, 289, 51, 684, 466, 533, 820,
        669, 45, 902, 452, 167, 342, 244, 173, 35, 463, 651, 51, 699, 591, 452, 578,
        37, 124, 298, 332, 552, 43, 427, 119, 662, 777, 475, 850, 764, 364, 578, 911,
        283, 711, 472, 420, 245, 288, 594, 394, 511, 327, 589, 777, 699, 688, 43, 408,
        842, 383, 721, 521, 560, 644, 714, 559, 62, 145, 873, 663, 713, 159, 672, 729,
        624, 59, 193, 417, 158, 209, 563, 564, 343, 693, 109, 608, 563, 365, 181, 772,
        677, 310, 248, 353, 708, 410, 579, 870, 617, 841, 632, 860, 289, 536, 35, 777,
        618, 586, 424, 833, 77, 597, 346, 269, 757, 632, 695, 751, 331, 247, 184, 45,
        787, 680, 18, 66, 407, 369, 54, 492, 228, 613, 830, 922, 437, 519, 644, 905,
        789, 420, 305, 441, 207, 300, 892, 827, 141, 537, 381, 662, 513, 56, 252, 341,
        242, 797, 838, 837, 720, 224, 307, 631, 61, 87, 560, 310, 756, 665, 397, 808,
        851, 309, 473, 795, 378, 31, 647, 915, 459, 806, 590, 731, 425, 216, 548, 249,
        321, 881, 699, 535, 673, 782, 210, 815, 905, 303, 843, 922, 281, 73, 469, 791,
        660, 162, 498, 308, 155, 422, 907, 817, 187, 62, 16, 425, 535, 336, 286, 437,
        375, 273, 610, 296, 183, 923, 116, 667, 751, 353, 62, 366, 691, 379, 687, 842,
        37, 357, 720, 742, 330, 5, 39, 923, 311, 424, 242, 749, 321, 54, 669, 316,
        342, 299, 534, 105, 667, 488, 640, 672, 576, 540, 316, 486, 721, 610, 46, 656,
        447, 171, 616, 464, 190, 531, 297, 321, 762, 752, 533, 175, 134, 14, 381, 433,
        717, 45, 111, 20, 596, 284, 736, 138, 646, 411, 877, 669, 141, 919, 45, 780,
        407, 164, 332, 899, 165, 726, 600, 325, 498, 655, 357, 752, 768, 223, 849, 647,
        63, 310, 863, 251, 366, 304, 282, 738, 675, 410, 389, 244, 31, 121, 303, 263
    };

    private static final String[] CODAGEMC = {
        "urA", "xfs", "ypy", "unk", "xdw", "yoz", "pDA", "uls", "pBk", "eBA",
        "pAs", "eAk", "prA", "uvs", "xhy", "pnk", "utw", "xgz", "fDA", "pls", "fBk", "frA", "pvs",
        "uxy", "fnk", "ptw", "uwz", "fls", "psy", "fvs", "pxy", "ftw", "pwz", "fxy", "yrx", "ufk",
        "xFw", "ymz", "onA", "uds", "xEy", "olk", "ucw", "dBA", "oks", "uci", "dAk", "okg", "dAc",
        "ovk", "uhw", "xaz", "dnA", "ots", "ugy", "dlk", "osw", "ugj", "dks", "osi", "dvk", "oxw",
        "uiz", "dts", "owy", "dsw", "owj", "dxw", "oyz", "dwy", "dwj", "ofA", "uFs", "xCy", "odk",
        "uEw", "xCj", "clA", "ocs", "uEi", "ckk", "ocg", "ckc", "ckE", "cvA", "ohs", "uay", "ctk",
        "ogw", "uaj", "css", "ogi", "csg", "csa", "cxs", "oiy", "cww", "oij", "cwi", "cyy", "oFk",
        "uCw", "xBj", "cdA", "oEs", "uCi", "cck", "oEg", "uCb", "ccc", "oEa", "ccE", "oED", "chk",
        "oaw", "uDj", "cgs", "oai", "cgg", "oab", "cga", "cgD", "obj", "cib", "cFA", "oCs", "uBi",
        "cEk", "oCg", "uBb", "cEc", "oCa", "cEE", "oCD", "cEC", "cas", "cag", "caa", "cCk", "uAr",
        "oBa", "oBD", "cCB", "tfk", "wpw", "yez", "mnA", "tds", "woy", "mlk", "tcw", "woj", "FBA",
        "mks", "FAk", "mvk", "thw", "wqz", "FnA", "mts", "tgy", "Flk", "msw", "Fks", "Fkg", "Fvk",
        "mxw", "tiz", "Fts", "mwy", "Fsw", "Fsi", "Fxw", "myz", "Fwy", "Fyz", "vfA", "xps", "yuy",
        "vdk", "xow", "yuj", "qlA", "vcs", "xoi", "qkk", "vcg", "xob", "qkc", "vca", "mfA", "tFs",
        "wmy", "qvA", "mdk", "tEw", "wmj", "qtk", "vgw", "xqj", "hlA", "Ekk", "mcg", "tEb", "hkk",
        "qsg", "hkc", "EvA", "mhs", "tay", "hvA", "Etk", "mgw", "taj", "htk", "qww", "vij", "hss",
        "Esg", "hsg", "Exs", "miy", "hxs", "Eww", "mij", "hww", "qyj", "hwi", "Eyy", "hyy", "Eyj",
        "hyj", "vFk", "xmw", "ytj", "qdA", "vEs", "xmi", "qck", "vEg", "xmb", "qcc", "vEa", "qcE",
        "qcC", "mFk", "tCw", "wlj", "qhk", "mEs", "tCi", "gtA", "Eck", "vai", "tCb", "gsk", "Ecc",
        "mEa", "gsc", "qga", "mED", "EcC", "Ehk", "maw", "tDj", "gxk", "Egs", "mai", "gws", "qii",
        "mab", "gwg", "Ega", "EgD", "Eiw", "mbj", "gyw", "Eii", "gyi", "Eib", "gyb", "gzj", "qFA",
        "vCs", "xli", "qEk", "vCg", "xlb", "qEc", "vCa", "qEE", "vCD", "qEC", "qEB", "EFA", "mCs",
        "tBi", "ghA", "EEk", "mCg", "tBb", "ggk", "qag", "vDb", "ggc", "EEE", "mCD", "ggE", "qaD",
        "ggC", "Eas", "mDi", "gis", "Eag", "mDb", "gig", "qbb", "gia", "EaD", "giD", "gji", "gjb",
        "qCk", "vBg", "xkr", "qCc", "vBa", "qCE", "vBD", "qCC", "qCB", "ECk", "mBg", "tAr", "gak",
        "ECc", "mBa", "gac", "qDa", "mBD", "gaE", "ECC", "gaC", "ECB", "EDg", "gbg", "gba", "gbD",
        "vAq", "vAn", "qBB", "mAq", "EBE", "gDE", "gDC", "gDB", "lfA", "sps", "wey", "ldk", "sow",
        "ClA", "lcs", "soi", "Ckk", "lcg", "Ckc", "CkE", "CvA", "lhs", "sqy", "Ctk", "lgw", "sqj",
        "Css", "lgi", "Csg", "Csa", "Cxs", "liy", "Cww", "lij", "Cwi", "Cyy", "Cyj", "tpk", "wuw",
        "yhj", "ndA", "tos", "wui", "nck", "tog", "wub", "ncc", "toa", "ncE", "toD", "lFk", "smw",
        "wdj", "nhk", "lEs", "smi", "atA", "Cck", "tqi", "smb", "ask", "ngg", "lEa", "asc", "CcE",
        "asE", "Chk", "law", "snj", "axk", "Cgs", "trj", "aws", "nii", "lab", "awg", "Cga", "awa",
        "Ciw", "lbj", "ayw", "Cii", "ayi", "Cib", "Cjj", "azj", "vpA", "xus", "yxi", "vok", "xug",
        "yxb", "voc", "xua", "voE", "xuD", "voC", "nFA", "tms", "wti", "rhA", "nEk", "xvi", "wtb",
        "rgk", "vqg", "xvb", "rgc", "nEE", "tmD", "rgE", "vqD", "nEB", "CFA", "lCs", "sli", "ahA",
        "CEk", "lCg", "slb", "ixA", "agk", "nag", "tnb", "iwk", "rig", "vrb", "lCD", "iwc", "agE",
        "naD", "iwE", "CEB", "Cas", "lDi", "ais", "Cag", "lDb", "iys", "aig", "nbb", "iyg", "rjb",
        "CaD", "aiD", "Cbi", "aji", "Cbb", "izi", "ajb", "vmk", "xtg", "ywr", "vmc", "xta", "vmE",
        "xtD", "vmC", "vmB", "nCk", "tlg", "wsr", "rak", "nCc", "xtr", "rac", "vna", "tlD", "raE",
        "nCC", "raC", "nCB", "raB", "CCk", "lBg", "skr", "aak", "CCc", "lBa", "iik", "aac", "nDa",
        "lBD", "iic", "rba", "CCC", "iiE", "aaC", "CCB", "aaB", "CDg", "lBr", "abg", "CDa", "ijg",
        "aba", "CDD", "ija", "abD", "CDr", "ijr", "vlc", "xsq", "vlE", "xsn", "vlC", "vlB", "nBc",
        "tkq", "rDc", "nBE", "tkn", "rDE", "vln", "rDC", "nBB", "rDB", "CBc", "lAq", "aDc", "CBE",
        "lAn", "ibc", "aDE", "nBn", "ibE", "rDn", "CBB", "ibC", "aDB", "ibB", "aDq", "ibq", "ibn",
        "xsf", "vkl", "tkf", "nAm", "nAl", "CAo", "aBo", "iDo", "CAl", "aBl", "kpk", "BdA", "kos",
        "Bck", "kog", "seb", "Bcc", "koa", "BcE", "koD", "Bhk", "kqw", "sfj", "Bgs", "kqi", "Bgg",
        "kqb", "Bga", "BgD", "Biw", "krj", "Bii", "Bib", "Bjj", "lpA", "sus", "whi", "lok", "sug",
        "loc", "sua", "loE", "suD", "loC", "BFA", "kms", "sdi", "DhA", "BEk", "svi", "sdb", "Dgk",
        "lqg", "svb", "Dgc", "BEE", "kmD", "DgE", "lqD", "BEB", "Bas", "kni", "Dis", "Bag", "knb",
        "Dig", "lrb", "Dia", "BaD", "Bbi", "Dji", "Bbb", "Djb", "tuk", "wxg", "yir", "tuc", "wxa",
        "tuE", "wxD", "tuC", "tuB", "lmk", "stg", "nqk", "lmc", "sta", "nqc", "tva", "stD", "nqE",
        "lmC", "nqC", "lmB", "nqB", "BCk", "klg", "Dak", "BCc", "str", "bik", "Dac", "lna", "klD",
        "bic", "nra", "BCC", "biE", "DaC", "BCB", "DaB", "BDg", "klr", "Dbg", "BDa", "bjg", "Dba",
        "BDD", "bja", "DbD", "BDr", "Dbr", "bjr", "xxc", "yyq", "xxE", "yyn", "xxC", "xxB", "ttc",
        "wwq", "vvc", "xxq", "wwn", "vvE", "xxn", "vvC", "ttB", "vvB", "llc", "ssq", "nnc", "llE",
        "ssn", "rrc", "nnE", "ttn", "rrE", "vvn", "llB", "rrC", "nnB", "rrB", "BBc", "kkq", "DDc",
        "BBE", "kkn", "bbc", "DDE", "lln", "jjc", "bbE", "nnn", "BBB", "jjE", "rrn", "DDB", "jjC",
        "BBq", "DDq", "BBn", "bbq", "DDn", "jjq", "bbn", "jjn", "xwo", "yyf", "xwm", "xwl", "tso",
        "wwf", "vto", "xwv", "vtm", "tsl", "vtl", "lko", "ssf", "nlo", "lkm", "rno", "nlm", "lkl",
        "rnm", "nll", "rnl", "BAo", "kkf", "DBo", "lkv", "bDo", "DBm", "BAl", "jbo", "bDm", "DBl",
        "jbm", "bDl", "jbl", "DBv", "jbv", "xwd", "vsu", "vst", "nku", "rlu", "rlt", "DAu", "bBu",
        "jDu", "jDt", "ApA", "Aok", "keg", "Aoc", "AoE", "AoC", "Aqs", "Aqg", "Aqa", "AqD", "Ari",
        "Arb", "kuk", "kuc", "sha", "kuE", "shD", "kuC", "kuB", "Amk", "kdg", "Bqk", "kvg", "kda",
        "Bqc", "kva", "BqE", "kvD", "BqC", "AmB", "BqB", "Ang", "kdr", "Brg", "kvr", "Bra", "AnD",
        "BrD", "Anr", "Brr", "sxc", "sxE", "sxC", "sxB", "ktc", "lvc", "sxq", "sgn", "lvE", "sxn",
        "lvC", "ktB", "lvB", "Alc", "Bnc", "AlE", "kcn", "Drc", "BnE", "AlC", "DrE", "BnC", "AlB",
        "DrC", "BnB", "Alq", "Bnq", "Aln", "Drq", "Bnn", "Drn", "wyo", "wym", "wyl", "swo", "txo",
        "wyv", "txm", "swl", "txl", "kso", "sgf", "lto", "swv", "nvo", "ltm", "ksl", "nvm", "ltl",
        "nvl", "Ako", "kcf", "Blo", "ksv", "Dno", "Blm", "Akl", "bro", "Dnm", "Bll", "brm", "Dnl",
        "Akv", "Blv", "Dnv", "brv", "yze", "yzd", "wye", "xyu", "wyd", "xyt", "swe", "twu", "swd",
        "vxu", "twt", "vxt", "kse", "lsu", "ksd", "ntu", "lst", "rvu", "ypk", "zew", "xdA", "yos",
        "zei", "xck", "yog", "zeb", "xcc", "yoa", "xcE", "yoD", "xcC", "xhk", "yqw", "zfj", "utA",
        "xgs", "yqi", "usk", "xgg", "yqb", "usc", "xga", "usE", "xgD", "usC", "uxk", "xiw", "yrj",
        "ptA", "uws", "xii", "psk", "uwg", "xib", "psc", "uwa", "psE", "uwD", "psC", "pxk", "uyw",
        "xjj", "ftA", "pws", "uyi", "fsk", "pwg", "uyb", "fsc", "pwa", "fsE", "pwD", "fxk", "pyw",
        "uzj", "fws", "pyi", "fwg", "pyb", "fwa", "fyw", "pzj", "fyi", "fyb", "xFA", "yms", "zdi",
        "xEk", "ymg", "zdb", "xEc", "yma", "xEE", "ymD", "xEC", "xEB", "uhA", "xas", "yni", "ugk",
        "xag", "ynb", "ugc", "xaa", "ugE", "xaD", "ugC", "ugB", "oxA", "uis", "xbi", "owk", "uig",
        "xbb", "owc", "uia", "owE", "uiD", "owC", "owB", "dxA", "oys", "uji", "dwk", "oyg", "ujb",
        "dwc", "oya", "dwE", "oyD", "dwC", "dys", "ozi", "dyg", "ozb", "dya", "dyD", "dzi", "dzb",
        "xCk", "ylg", "zcr", "xCc", "yla", "xCE", "ylD", "xCC", "xCB", "uak", "xDg", "ylr", "uac",
        "xDa", "uaE", "xDD", "uaC", "uaB", "oik", "ubg", "xDr", "oic", "uba", "oiE", "ubD", "oiC",
        "oiB", "cyk", "ojg", "ubr", "cyc", "oja", "cyE", "ojD", "cyC", "cyB", "czg", "ojr", "cza",
        "czD", "czr", "xBc", "ykq", "xBE", "ykn", "xBC", "xBB", "uDc", "xBq", "uDE", "xBn", "uDC",
        "uDB", "obc", "uDq", "obE", "uDn", "obC", "obB", "cjc", "obq", "cjE", "obn", "cjC", "cjB",
        "cjq", "cjn", "xAo", "ykf", "xAm", "xAl", "uBo", "xAv", "uBm", "uBl", "oDo", "uBv", "oDm",
        "oDl", "cbo", "oDv", "cbm", "cbl", "xAe", "xAd", "uAu", "uAt", "oBu", "oBt", "wpA", "yes",
        "zFi", "wok", "yeg", "zFb", "woc", "yea", "woE", "yeD", "woC", "woB", "thA", "wqs", "yfi",
        "tgk", "wqg", "yfb", "tgc", "wqa", "tgE", "wqD", "tgC", "tgB", "mxA", "tis", "wri", "mwk",
        "tig", "wrb", "mwc", "tia", "mwE", "tiD", "mwC", "mwB", "FxA", "mys", "tji", "Fwk", "myg",
        "tjb", "Fwc", "mya", "FwE", "myD", "FwC", "Fys", "mzi", "Fyg", "mzb", "Fya", "FyD", "Fzi",
        "Fzb", "yuk", "zhg", "hjs", "yuc", "zha", "hbw", "yuE", "zhD", "hDy", "yuC", "yuB", "wmk",
        "ydg", "zEr", "xqk", "wmc", "zhr", "xqc", "yva", "ydD", "xqE", "wmC", "xqC", "wmB", "xqB",
        "tak", "wng", "ydr", "vik", "tac", "wna", "vic", "xra", "wnD", "viE", "taC", "viC", "taB",
        "viB", "mik", "tbg", "wnr", "qyk", "mic", "tba", "qyc", "vja", "tbD", "qyE", "miC", "qyC",
        "miB", "qyB", "Eyk", "mjg", "tbr", "hyk", "Eyc", "mja", "hyc", "qza", "mjD", "hyE", "EyC",
        "hyC", "EyB", "Ezg", "mjr", "hzg", "Eza", "hza", "EzD", "hzD", "Ezr", "ytc", "zgq", "grw",
        "ytE", "zgn", "gny", "ytC", "glz", "ytB", "wlc", "ycq", "xnc", "wlE", "ycn", "xnE", "ytn",
        "xnC", "wlB", "xnB", "tDc", "wlq", "vbc", "tDE", "wln", "vbE", "xnn", "vbC", "tDB", "vbB",
        "mbc", "tDq", "qjc", "mbE", "tDn", "qjE", "vbn", "qjC", "mbB", "qjB", "Ejc", "mbq", "gzc",
        "EjE", "mbn", "gzE", "qjn", "gzC", "EjB", "gzB", "Ejq", "gzq", "Ejn", "gzn", "yso", "zgf",
        "gfy", "ysm", "gdz", "ysl", "wko", "ycf", "xlo", "ysv", "xlm", "wkl", "xll", "tBo", "wkv",
        "vDo", "tBm", "vDm", "tBl", "vDl", "mDo", "tBv", "qbo", "vDv", "qbm", "mDl", "qbl", "Ebo",
        "mDv", "gjo", "Ebm", "gjm", "Ebl", "gjl", "Ebv", "gjv", "yse", "gFz", "ysd", "wke", "xku",
        "wkd", "xkt", "tAu", "vBu", "tAt", "vBt", "mBu", "qDu", "mBt", "qDt", "EDu", "gbu", "EDt",
        "gbt", "ysF", "wkF", "xkh", "tAh", "vAx", "mAx", "qBx", "wek", "yFg", "zCr", "wec", "yFa",
        "weE", "yFD", "weC", "weB", "sqk", "wfg", "yFr", "sqc", "wfa", "sqE", "wfD", "sqC", "sqB",
        "lik", "srg", "wfr", "lic", "sra", "liE", "srD", "liC", "liB", "Cyk", "ljg", "srr", "Cyc",
        "lja", "CyE", "ljD", "CyC", "CyB", "Czg", "ljr", "Cza", "CzD", "Czr", "yhc", "zaq", "arw",
        "yhE", "zan", "any", "yhC", "alz", "yhB", "wdc", "yEq", "wvc", "wdE", "yEn", "wvE", "yhn",
        "wvC", "wdB", "wvB", "snc", "wdq", "trc", "snE", "wdn", "trE", "wvn", "trC", "snB", "trB",
        "lbc", "snq", "njc", "lbE", "snn", "njE", "trn", "njC", "lbB", "njB", "Cjc", "lbq", "azc",
        "CjE", "lbn", "azE", "njn", "azC", "CjB", "azB", "Cjq", "azq", "Cjn", "azn", "zio", "irs",
        "rfy", "zim", "inw", "rdz", "zil", "ily", "ikz", "ygo", "zaf", "afy", "yxo", "ziv", "ivy",
        "adz", "yxm", "ygl", "itz", "yxl", "wco", "yEf", "wto", "wcm", "xvo", "yxv", "wcl", "xvm",
        "wtl", "xvl", "slo", "wcv", "tno", "slm", "vro", "tnm", "sll", "vrm", "tnl", "vrl", "lDo",
        "slv", "nbo", "lDm", "rjo", "nbm", "lDl", "rjm", "nbl", "rjl", "Cbo", "lDv", "ajo", "Cbm",
        "izo", "ajm", "Cbl", "izm", "ajl", "izl", "Cbv", "ajv", "zie", "ifw", "rFz", "zid", "idy",
        "icz", "yge", "aFz", "ywu", "ygd", "ihz", "ywt", "wce", "wsu", "wcd", "xtu", "wst", "xtt",
        "sku", "tlu", "skt", "vnu", "tlt", "vnt", "lBu", "nDu", "lBt", "rbu", "nDt", "rbt", "CDu",
        "abu", "CDt", "iju", "abt", "ijt", "ziF", "iFy", "iEz", "ygF", "ywh", "wcF", "wsh", "xsx",
        "skh", "tkx", "vlx", "lAx", "nBx", "rDx", "CBx", "aDx", "ibx", "iCz", "wFc", "yCq", "wFE",
        "yCn", "wFC", "wFB", "sfc", "wFq", "sfE", "wFn", "sfC", "sfB", "krc", "sfq", "krE", "sfn",
        "krC", "krB", "Bjc", "krq", "BjE", "krn", "BjC", "BjB", "Bjq", "Bjn", "yao", "zDf", "Dfy",
        "yam", "Ddz", "yal", "wEo", "yCf", "who", "wEm", "whm", "wEl", "whl", "sdo", "wEv", "svo",
        "sdm", "svm", "sdl", "svl", "kno", "sdv", "lro", "knm", "lrm", "knl", "lrl", "Bbo", "knv",
        "Djo", "Bbm", "Djm", "Bbl", "Djl", "Bbv", "Djv", "zbe", "bfw", "npz", "zbd", "bdy", "bcz",
        "yae", "DFz", "yiu", "yad", "bhz", "yit", "wEe", "wgu", "wEd", "wxu", "wgt", "wxt", "scu",
        "stu", "sct", "tvu", "stt", "tvt", "klu", "lnu", "klt", "nru", "lnt", "nrt", "BDu", "Dbu",
        "BDt", "bju", "Dbt", "bjt", "jfs", "rpy", "jdw", "roz", "jcy", "jcj", "zbF", "bFy", "zjh",
        "jhy", "bEz", "jgz", "yaF", "yih", "yyx", "wEF", "wgh", "wwx", "xxx", "sch", "ssx", "ttx",
        "vvx", "kkx", "llx", "nnx", "rrx", "BBx", "DDx", "bbx", "jFw", "rmz", "jEy", "jEj", "bCz",
        "jaz", "jCy", "jCj", "jBj", "wCo", "wCm", "wCl", "sFo", "wCv", "sFm", "sFl", "kfo", "sFv",
        "kfm", "kfl", "Aro", "kfv", "Arm", "Arl", "Arv", "yDe", "Bpz", "yDd", "wCe", "wau", "wCd",
        "wat", "sEu", "shu", "sEt", "sht", "kdu", "kvu", "kdt", "kvt", "Anu", "Bru", "Ant", "Brt",
        "zDp", "Dpy", "Doz", "yDF", "ybh", "wCF", "wah", "wix", "sEh", "sgx", "sxx", "kcx", "ktx",
        "lvx", "Alx", "Bnx", "Drx", "bpw", "nuz", "boy", "boj", "Dmz", "bqz", "jps", "ruy", "jow",
        "ruj", "joi", "job", "bmy", "jqy", "bmj", "jqj", "jmw", "rtj", "jmi", "jmb", "blj", "jnj",
        "jli", "jlb", "jkr", "sCu", "sCt", "kFu", "kFt", "Afu", "Aft", "wDh", "sCh", "sax", "kEx",
        "khx", "Adx", "Avx", "Buz", "Duy", "Duj", "buw", "nxj", "bui", "bub", "Dtj", "bvj", "jus",
        "rxi", "jug", "rxb", "jua", "juD", "bti", "jvi", "btb", "jvb", "jtg", "rwr", "jta", "jtD",
        "bsr", "jtr", "jsq", "jsn", "Bxj", "Dxi", "Dxb", "bxg", "nyr", "bxa", "bxD", "Dwr", "bxr",
        "bwq", "bwn", "pjk", "urw", "ejA", "pbs", "uny", "ebk", "pDw", "ulz", "eDs", "pBy", "eBw",
        "zfc", "fjk", "prw", "zfE", "fbs", "pny", "zfC", "fDw", "plz", "zfB", "fBy", "yrc", "zfq",
        "frw", "yrE", "zfn", "fny", "yrC", "flz", "yrB", "xjc", "yrq", "xjE", "yrn", "xjC", "xjB",
        "uzc", "xjq", "uzE", "xjn", "uzC", "uzB", "pzc", "uzq", "pzE", "uzn", "pzC", "djA", "ors",
        "ufy", "dbk", "onw", "udz", "dDs", "oly", "dBw", "okz", "dAy", "zdo", "drs", "ovy", "zdm",
        "dnw", "otz", "zdl", "dly", "dkz", "yno", "zdv", "dvy", "ynm", "dtz", "ynl", "xbo", "ynv",
        "xbm", "xbl", "ujo", "xbv", "ujm", "ujl", "ozo", "ujv", "ozm", "ozl", "crk", "ofw", "uFz",
        "cns", "ody", "clw", "ocz", "cky", "ckj", "zcu", "cvw", "ohz", "zct", "cty", "csz", "ylu",
        "cxz", "ylt", "xDu", "xDt", "ubu", "ubt", "oju", "ojt", "cfs", "oFy", "cdw", "oEz", "ccy",
        "ccj", "zch", "chy", "cgz", "ykx", "xBx", "uDx", "cFw", "oCz", "cEy", "cEj", "caz", "cCy",
        "cCj", "FjA", "mrs", "tfy", "Fbk", "mnw", "tdz", "FDs", "mly", "FBw", "mkz", "FAy", "zFo",
        "Frs", "mvy", "zFm", "Fnw", "mtz", "zFl", "Fly", "Fkz", "yfo", "zFv", "Fvy", "yfm", "Ftz",
        "yfl", "wro", "yfv", "wrm", "wrl", "tjo", "wrv", "tjm", "tjl", "mzo", "tjv", "mzm", "mzl",
        "qrk", "vfw", "xpz", "hbA", "qns", "vdy", "hDk", "qlw", "vcz", "hBs", "qky", "hAw", "qkj",
        "hAi", "Erk", "mfw", "tFz", "hrk", "Ens", "mdy", "hns", "qty", "mcz", "hlw", "Eky", "hky",
        "Ekj", "hkj", "zEu", "Evw", "mhz", "zhu", "zEt", "hvw", "Ety", "zht", "hty", "Esz", "hsz",
        "ydu", "Exz", "yvu", "ydt", "hxz", "yvt", "wnu", "xru", "wnt", "xrt", "tbu", "vju", "tbt",
        "vjt", "mju", "mjt", "grA", "qfs", "vFy", "gnk", "qdw", "vEz", "gls", "qcy", "gkw", "qcj",
        "gki", "gkb", "Efs", "mFy", "gvs", "Edw", "mEz", "gtw", "qgz", "gsy", "Ecj", "gsj", "zEh",
        "Ehy", "zgx", "gxy", "Egz", "gwz", "ycx", "ytx", "wlx", "xnx", "tDx", "vbx", "mbx", "gfk",
        "qFw", "vCz", "gds", "qEy", "gcw", "qEj", "gci", "gcb", "EFw", "mCz", "ghw", "EEy", "ggy",
        "EEj", "ggj", "Eaz", "giz", "gFs", "qCy", "gEw", "qCj", "gEi", "gEb", "ECy", "gay", "ECj",
        "gaj", "gCw", "qBj", "gCi", "gCb", "EBj", "gDj", "gBi", "gBb", "Crk", "lfw", "spz", "Cns",
        "ldy", "Clw", "lcz", "Cky", "Ckj", "zCu", "Cvw", "lhz", "zCt", "Cty", "Csz", "yFu", "Cxz",
        "yFt", "wfu", "wft", "sru", "srt", "lju", "ljt", "arA", "nfs", "tpy", "ank", "ndw", "toz",
        "als", "ncy", "akw", "ncj", "aki", "akb", "Cfs", "lFy", "avs", "Cdw", "lEz", "atw", "ngz",
        "asy", "Ccj", "asj", "zCh", "Chy", "zax", "axy", "Cgz", "awz", "yEx", "yhx", "wdx", "wvx",
        "snx", "trx", "lbx", "rfk", "vpw", "xuz", "inA", "rds", "voy", "ilk", "rcw", "voj", "iks",
        "rci", "ikg", "rcb", "ika", "afk", "nFw", "tmz", "ivk", "ads", "nEy", "its", "rgy", "nEj",
        "isw", "aci", "isi", "acb", "isb", "CFw", "lCz", "ahw", "CEy", "ixw", "agy", "CEj", "iwy",
        "agj", "iwj", "Caz", "aiz", "iyz", "ifA", "rFs", "vmy", "idk", "rEw", "vmj", "ics", "rEi",
        "icg", "rEb", "ica", "icD", "aFs", "nCy", "ihs", "aEw", "nCj", "igw", "raj", "igi", "aEb",
        "igb", "CCy", "aay", "CCj", "iiy", "aaj", "iij", "iFk", "rCw", "vlj", "iEs", "rCi", "iEg",
        "rCb", "iEa", "iED", "aCw", "nBj", "iaw", "aCi", "iai", "aCb", "iab", "CBj", "aDj", "ibj",
        "iCs", "rBi", "iCg", "rBb", "iCa", "iCD", "aBi", "iDi", "aBb", "iDb", "iBg", "rAr", "iBa",
        "iBD", "aAr", "iBr", "iAq", "iAn", "Bfs", "kpy", "Bdw", "koz", "Bcy", "Bcj", "Bhy", "Bgz",
        "yCx", "wFx", "sfx", "krx", "Dfk", "lpw", "suz", "Dds", "loy", "Dcw", "loj", "Dci", "Dcb",
        "BFw", "kmz", "Dhw", "BEy", "Dgy", "BEj", "Dgj", "Baz", "Diz", "bfA", "nps", "tuy", "bdk",
        "now", "tuj", "bcs", "noi", "bcg", "nob", "bca", "bcD", "DFs", "lmy", "bhs", "DEw", "lmj",
        "bgw", "DEi", "bgi", "DEb", "bgb", "BCy", "Day", "BCj", "biy", "Daj", "bij", "rpk", "vuw",
        "xxj", "jdA", "ros", "vui", "jck", "rog", "vub", "jcc", "roa", "jcE", "roD", "jcC", "bFk",
        "nmw", "ttj", "jhk", "bEs", "nmi", "jgs", "rqi", "nmb", "jgg", "bEa", "jga", "bED", "jgD",
        "DCw", "llj", "baw", "DCi", "jiw", "bai", "DCb", "jii", "bab", "jib", "BBj", "DDj", "bbj",
        "jjj", "jFA", "rms", "vti", "jEk", "rmg", "vtb", "jEc", "rma", "jEE", "rmD", "jEC", "jEB",
        "bCs", "nli", "jas", "bCg", "nlb", "jag", "rnb", "jaa", "bCD", "jaD", "DBi", "bDi", "DBb",
        "jbi", "bDb", "jbb", "jCk", "rlg", "vsr", "jCc", "rla", "jCE", "rlD", "jCC", "jCB", "bBg",
        "nkr", "jDg", "bBa", "jDa", "bBD", "jDD", "DAr", "bBr", "jDr", "jBc", "rkq", "jBE", "rkn",
        "jBC", "jBB", "bAq", "jBq", "bAn", "jBn", "jAo", "rkf", "jAm", "jAl", "bAf", "jAv", "Apw",
        "kez", "Aoy", "Aoj", "Aqz", "Bps", "kuy", "Bow", "kuj", "Boi", "Bob", "Amy", "Bqy", "Amj",
        "Bqj", "Dpk", "luw", "sxj", "Dos", "lui", "Dog", "lub", "Doa", "DoD", "Bmw", "ktj", "Dqw",
        "Bmi", "Dqi", "Bmb", "Dqb", "Alj", "Bnj", "Drj", "bpA", "nus", "txi", "bok", "nug", "txb",
        "boc", "nua", "boE", "nuD", "boC", "boB", "Dms", "lti", "bqs", "Dmg", "ltb", "bqg", "nvb",
        "bqa", "DmD", "bqD", "Bli", "Dni", "Blb", "bri", "Dnb", "brb", "ruk", "vxg", "xyr", "ruc",
        "vxa", "ruE", "vxD", "ruC", "ruB", "bmk", "ntg", "twr", "jqk", "bmc", "nta", "jqc", "rva",
        "ntD", "jqE", "bmC", "jqC", "bmB", "jqB", "Dlg", "lsr", "bng", "Dla", "jrg", "bna", "DlD",
        "jra", "bnD", "jrD", "Bkr", "Dlr", "bnr", "jrr", "rtc", "vwq", "rtE", "vwn", "rtC", "rtB",
        "blc", "nsq", "jnc", "blE", "nsn", "jnE", "rtn", "jnC", "blB", "jnB", "Dkq", "blq", "Dkn",
        "jnq", "bln", "jnn", "rso", "vwf", "rsm", "rsl", "bko", "nsf", "jlo", "bkm", "jlm", "bkl",
        "jll", "Dkf", "bkv", "jlv", "rse", "rsd", "bke", "jku", "bkd", "jkt", "Aey", "Aej", "Auw",
        "khj", "Aui", "Aub", "Adj", "Avj", "Bus", "kxi", "Bug", "kxb", "Bua", "BuD", "Ati", "Bvi",
        "Atb", "Bvb", "Duk", "lxg", "syr", "Duc", "lxa", "DuE", "lxD", "DuC", "DuB", "Btg", "kwr",
        "Dvg", "lxr", "Dva", "BtD", "DvD", "Asr", "Btr", "Dvr", "nxc", "tyq", "nxE", "tyn", "nxC",
        "nxB", "Dtc", "lwq", "bvc", "nxq", "lwn", "bvE", "DtC", "bvC", "DtB", "bvB", "Bsq", "Dtq",
        "Bsn", "bvq", "Dtn", "bvn", "vyo", "xzf", "vym", "vyl", "nwo", "tyf", "rxo", "nwm", "rxm",
        "nwl", "rxl", "Dso", "lwf", "bto", "Dsm", "jvo", "btm", "Dsl", "jvm", "btl", "jvl", "Bsf",
        "Dsv", "btv", "jvv", "vye", "vyd", "nwe", "rwu", "nwd", "rwt", "Dse", "bsu", "Dsd", "jtu",
        "bst", "jtt", "vyF", "nwF", "rwh", "DsF", "bsh", "jsx", "Ahi", "Ahb", "Axg", "kir", "Axa",
        "AxD", "Agr", "Axr", "Bxc", "kyq", "BxE", "kyn", "BxC", "BxB", "Awq", "Bxq", "Awn", "Bxn",
        "lyo", "szf", "lym", "lyl", "Bwo", "kyf", "Dxo", "lyv", "Dxm", "Bwl", "Dxl", "Awf", "Bwv",
        "Dxv", "tze", "tzd", "lye", "nyu", "lyd", "nyt", "Bwe", "Dwu", "Bwd", "bxu", "Dwt", "bxt",
        "tzF", "lyF", "nyh", "BwF", "Dwh", "bwx", "Aiq", "Ain", "Ayo", "kjf", "Aym", "Ayl", "Aif",
        "Ayv", "kze", "kzd", "Aye", "Byu", "Ayd", "Byt", "szp"
    };

    private static final char[] BR_SET = {
        'A', 'B', 'C', 'D', 'E', 'F', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
        'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '*', '+', '-'
    };

    private static final String[] PDF_TTF = {
        "00000", "00001", "00010", "00011", "00100", "00101", "00110", "00111",
        "01000", "01001", "01010", "01011", "01100", "01101", "01110", "01111", "10000", "10001",
        "10010", "10011", "10100", "10101", "10110", "10111", "11000", "11001", "11010",
        "11011", "11100", "11101", "11110", "11111", "01", "1111111101010100", "11111101000101001"
    };

    private static final int[] ASCII_X = {
        7, 8, 8, 4, 12, 4, 4, 8, 8, 8, 12, 4, 12, 12, 12, 12, 4, 4, 4, 4, 4, 4, 4, 4,
        4, 4, 12, 8, 8, 4, 8, 8, 8, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 8, 8, 8, 4, 8, 8, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
        2, 2, 2, 2, 8, 8, 8, 8
    };

    private static final int[] ASCII_Y = {
        26, 10, 20, 15, 18, 21, 10, 28, 23, 24, 22, 20, 13, 16, 17, 19, 0, 1, 2, 3,
        4, 5, 6, 7, 8, 9, 14, 0, 1, 23, 2, 25, 3, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
        16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 4, 5, 6, 24, 7, 8, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
        11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 21, 27, 9
    };

    private static final int[] MICRO_AUTOSIZE = {
        4, 6,  7, 8, 8,  10, 10, 12, 12, 13, 14, 16, 18, 18, 19, 20, 24, 24, 24, 29, 30, 33, 34, 37, 39, 46, 54, 58, 70, 72, 82, 90, 108, 126, // max codeword counts
        1, 14, 2, 7, 24, 3,  15, 25, 4,  8,  16, 5,  17, 26, 9,  6,  10, 18, 27, 11, 28, 12, 19, 13, 29, 20, 30, 21, 22, 31, 23, 32, 33,  34   // corresponding variant
    };

    /* Rows, columns, error codewords, k-offset of valid MicroPDF417 sizes from ISO/IEC 24728:2006 */
    private static final int[] MICRO_VARIANTS = {
        1,  1,  1,  1,  1,  1,  2, 2,  2,  2,  2,  2,  2,  3,  3,  3,  3,   3,   3,   3,   3,   3,   3,   4, 4,  4,  4,  4,   4,   4,   4,   4,   4,   4,  // columns
        11, 14, 17, 20, 24, 28, 8, 11, 14, 17, 20, 23, 26, 6,  8,  10, 12,  15,  20,  26,  32,  38,  44,  4, 6,  8,  10, 12,  15,  20,  26,  32,  38,  44, // rows
        7,  7,  7,  8,  8,  8,  8, 9,  9,  10, 11, 13, 15, 12, 14, 16, 18,  21,  26,  32,  38,  44,  50,  8, 12, 14, 16, 18,  21,  26,  32,  38,  44,  50, // k (EC codewords)
        0,  0,  0,  7,  7,  7,  7, 15, 15, 24, 34, 57, 84, 45, 70, 99, 115, 133, 154, 180, 212, 250, 294, 7, 45, 70, 99, 115, 133, 154, 180, 212, 250, 294 // offset
    };

    /* Following is Left RAP, Centre RAP, Right RAP and Start Cluster from ISO/IEC 24728:2006 tables 10, 11 and 12 */
    private static final int[] RAP_TABLE = {
        1, 8, 36, 19, 9,  25, 1, 1, 8, 36, 19, 9,  27, 1, 7, 15, 25, 37, 1,  1,  21, 15, 1,  47, 1, 7, 15, 25, 37, 1,  1,  21, 15, 1,  // left RAP
        0, 0, 0,  0,  0,  0,  0, 0, 0, 0,  0,  0,  0,  1, 7, 15, 25, 37, 17, 9,  29, 31, 25, 19, 1, 7, 15, 25, 37, 17, 9,  29, 31, 25, // centre RAP
        9, 8, 36, 19, 17, 33, 1, 9, 8, 36, 19, 17, 35, 1, 7, 15, 25, 37, 33, 17, 37, 47, 49, 43, 1, 7, 15, 25, 37, 33, 17, 37, 47, 49, // right RAP
        0, 3, 6,  0,  6,  0,  0, 0, 3, 6,  0,  6,  6,  0, 0, 6,  0,  0,  0,  0,  6,  6,  0,  3,  0, 0, 6,  0,  0,  0,  0,  6,  6,  0   // start cluster
    };

    /* Left and Right Row Address Pattern from Table 2 */
    private static final String[] RAPLR = {
        "",       "221311", "311311", "312211", "222211", "213211", "214111", "223111",
        "313111", "322111", "412111", "421111", "331111", "241111", "232111", "231211", "321211",
        "411211", "411121", "411112", "321112", "312112", "311212", "311221", "311131", "311122",
        "311113", "221113", "221122", "221131", "221221", "222121", "312121", "321121", "231121",
        "231112", "222112", "213112", "212212", "212221", "212131", "212122", "212113", "211213",
        "211123", "211132", "211141", "211231", "211222", "211312", "211321", "211411", "212311"
    };

    /* Centre Row Address Pattern from Table 2 */
    private static final String[] RAPC = {
        "",       "112231", "121231", "122131", "131131", "131221", "132121", "141121",
        "141211", "142111", "133111", "132211", "131311", "122311", "123211", "124111", "115111",
        "114211", "114121", "123121", "123112", "122212", "122221", "121321", "121411", "112411",
        "113311", "113221", "113212", "113122", "122122", "131122", "131113", "122113", "113113",
        "112213", "112222", "112312", "112321", "111421", "111331", "111322", "111232", "111223",
        "111133", "111124", "111214", "112114", "121114", "121123", "121132", "112132", "112141"
    };

    /* MicroPDF417 coefficients from ISO/IEC 24728:2006 Annex F */
    private static final int[] MICRO_COEFFS = {
        /* k = 7 */
        76, 925, 537, 597, 784, 691, 437,

        /* k = 8 */
        237, 308, 436, 284, 646, 653, 428, 379,

        /* k = 9 */
        567, 527, 622, 257, 289, 362, 501, 441, 205,

        /* k = 10 */
        377, 457, 64, 244, 826, 841, 818, 691, 266, 612,

        /* k = 11 */
        462, 45, 565, 708, 825, 213, 15, 68, 327, 602, 904,

        /* k = 12 */
        597, 864, 757, 201, 646, 684, 347, 127, 388, 7, 69, 851,

        /* k = 13 */
        764, 713, 342, 384, 606, 583, 322, 592, 678, 204, 184, 394, 692,

        /* k = 14 */
        669, 677, 154, 187, 241, 286, 274, 354, 478, 915, 691, 833, 105, 215,

        /* k = 15 */
        460, 829, 476, 109, 904, 664, 230, 5, 80, 74, 550, 575, 147, 868, 642,

        /* k = 16 */
        274, 562, 232, 755, 599, 524, 801, 132, 295, 116, 442, 428, 295, 42, 176, 65,

        /* k = 18 */
        279, 577, 315, 624, 37, 855, 275, 739, 120, 297, 312, 202, 560, 321, 233, 756,
        760, 573,

        /* k = 21 */
        108, 519, 781, 534, 129, 425, 681, 553, 422, 716, 763, 693, 624, 610, 310, 691,
        347, 165, 193, 259, 568,

        /* k = 26 */
        443, 284, 887, 544, 788, 93, 477, 760, 331, 608, 269, 121, 159, 830, 446, 893,
        699, 245, 441, 454, 325, 858, 131, 847, 764, 169,

        /* k = 32 */
        361, 575, 922, 525, 176, 586, 640, 321, 536, 742, 677, 742, 687, 284, 193, 517,
        273, 494, 263, 147, 593, 800, 571, 320, 803, 133, 231, 390, 685, 330, 63, 410,

        /* k = 38 */
        234, 228, 438, 848, 133, 703, 529, 721, 788, 322, 280, 159, 738, 586, 388, 684,
        445, 680, 245, 595, 614, 233, 812, 32, 284, 658, 745, 229, 95, 689, 920, 771,
        554, 289, 231, 125, 117, 518,

        /* k = 44 */
        476, 36, 659, 848, 678, 64, 764, 840, 157, 915, 470, 876, 109, 25, 632, 405,
        417, 436, 714, 60, 376, 97, 413, 706, 446, 21, 3, 773, 569, 267, 272, 213,
        31, 560, 231, 758, 103, 271, 572, 436, 339, 730, 82, 285,

        /* k = 50 */
        923, 797, 576, 875, 156, 706, 63, 81, 257, 874, 411, 416, 778, 50, 205, 303,
        188, 535, 909, 155, 637, 230, 534, 96, 575, 102, 264, 233, 919, 593, 865, 26,
        579, 623, 766, 146, 10, 739, 246, 127, 71, 244, 211, 477, 920, 876, 427, 820,
        718, 435
    };

    /**
     * Creates a new PDF417 symbol instance.
     */
    public Pdf417() {
        setBarHeight(3);
    }

    /**
     * Sets the default bar height (height of a single row) for this symbol (default value is <code>3</code>).
     *
     * @param barHeight the default bar height for this symbol
     */
    @Override
    public void setBarHeight(int barHeight) {
        super.setBarHeight(barHeight);
    }

    /**
     * Sets the width of the symbol by specifying the number of columns
     * of data codewords. Valid values are 1-30 for PDF417 and 1-4
     * for MicroPDF417.
     *
     * @param columns the number of data columns in the symbol
     */
    public void setDataColumns(int columns) {
        this.columns = columns;
    }

    /**
     * Returns the number of data columns used by this symbol, or {@code null}
     * if the number of data columns has not been set.
     *
     * @return the number of data columns used by this symbol
     */
    public Integer getDataColumns() {
        return columns;
    }

    /**
     * Sets the height of the symbol by specifying the number of rows
     * of data codewords. Valid values are 3-90 for PDF417 and 4-44
     * for MicroPDF417.
     *
     * @param rows the number of rows in the symbol
     */
    public void setRows(int rows) {
        this.rows = rows;
    }

    /**
     * Returns the number of rows used by this symbol, or {@code null} if
     * the number of rows has not been set.
     *
     * @return the number of rows used by this symbol
     */
    public Integer getRows() {
        return rows;
    }

    /**
     * Set the amount of the symbol which is dedicated to error correction
     * codewords. The number of codewords of error correction data is
     * determined by 2<sup>(eccLevel + 1)</sup>. This attribute is ignored
     * when using {@link Mode#MICRO micro} mode.
     *
     * @param eccLevel level of error correction (0-8)
     */
    public void setPreferredEccLevel(int eccLevel) {
        if (eccLevel < 0 || eccLevel > 8) {
            throw new IllegalArgumentException("ECC level must be between 0 and 8.");
        }
        preferredEccLevel = eccLevel;
    }

    /**
     * Returns the preferred error correction level.
     *
     * @return the preferred error correction level
     */
    public int getPreferredEccLevel() {
        return preferredEccLevel;
    }

    /**
     * Forces the use of the specified MicroPDF417 variant. Only valid
     * when using {@link Mode#MICRO micro} mode.
     *
     * @param variant the MicroPDF417 variant to use
     */
    public void setVariant(int variant) {
        if (symbolMode != Mode.MICRO) {
            throw new IllegalArgumentException("Can only set variant when using MICRO mode.");
        }
        if (variant < 1 || variant > 34) {
            throw new IllegalArgumentException("Variant must be between 1 and 34.");
        }
        this.columns = MICRO_VARIANTS[variant - 1];
        this.rows = MICRO_VARIANTS[variant - 1 + 34];
    }

    /**
     * If this PDF417 symbol is part of a series of PDF417 symbols appended in a structured format
     * (Macro PDF417), this method sets the position of this symbol in the series. Valid values are
     * 1 through 99,999 inclusive.
     *
     * @param position the position of this PDF417 symbol in the structured append series
     */
    public void setStructuredAppendPosition(int position) {
        if (position < 1 || position > 99_999) {
            throw new IllegalArgumentException("Invalid PDF417 structured append position: " + position);
        }
        this.structuredAppendPosition = position;
    }

    /**
     * Returns the position of this PDF417 symbol in a series of symbols using structured append
     * (Macro PDF417). If this symbol is not part of such a series, this method will return <code>1</code>.
     *
     * @return the position of this PDF417 symbol in a series of symbols using structured append
     */
    public int getStructuredAppendPosition() {
        return structuredAppendPosition;
    }

    /**
     * If this PDF417 symbol is part of a series of PDF417 symbols appended in a structured format
     * (Macro PDF417), this method sets the total number of symbols in the series. Valid values are
     * 1 through 99,999 inclusive. A value of 1 indicates that this symbol is not part of a structured
     * append series.
     *
     * @param total the total number of PDF417 symbols in the structured append series
     */
    public void setStructuredAppendTotal(int total) {
        if (total < 1 || total > 99_999) {
            throw new IllegalArgumentException("Invalid PDF417 structured append total: " + total);
        }
        this.structuredAppendTotal = total;
    }

    /**
     * Returns the size of the series of PDF417 symbols using structured append (Macro PDF417) that
     * this symbol is part of. If this symbol is not part of a structured append series, this method
     * will return <code>1</code>.
     *
     * @return size of the series that this symbol is part of
     */
    public int getStructuredAppendTotal() {
        return structuredAppendTotal;
    }

    /**
     * If this PDF417 symbol is part of a series of PDF417 symbols appended in a structured format
     * (Macro PDF417), this method sets the unique file ID for the series. Valid values are 0 through
     * 899 inclusive.
     *
     * @param fileId the unique file ID for the series that this symbol is part of
     */
    public void setStructuredAppendFileId(int fileId) {
        if (fileId < 0 || fileId > 899) {
            throw new IllegalArgumentException("Invalid PDF417 structured append file ID: " + fileId);
        }
        this.structuredAppendFileId = fileId;
    }

    /**
     * Returns the unique file ID of the series of PDF417 symbols using structured append (Macro PDF417)
     * that this symbol is part of. If this symbol is not part of a structured append series, this method
     * will return <code>0</code>.
     *
     * @return the unique file ID for the series that this symbol is part of
     */
    public int getStructuredAppendFileId() {
        return structuredAppendFileId;
    }

    /**
     * If this PDF417 symbol is part of a series of PDF417 symbols appended in a structured format
     * (Macro PDF417), this method sets the optional file name field for the symbol series.
     *
     * @param structuredAppendFileName the file name for the series that this symbol is part of
     */
    public void setStructuredAppendFileName(String structuredAppendFileName) {
        this.structuredAppendFileName = structuredAppendFileName;
    }

    /**
     * Returns the optional file name of the series of PDF417 symbols using structured append (Macro PDF417)
     * that this symbol is part of. If this symbol is not part of a structured append series, this method
     * will return <code>null</code>.
     *
     * @return the optional file name for the series that this symbol is part of
     */
    public String getStructuredAppendFileName() {
        return structuredAppendFileName;
    }

    /**
     * If this PDF417 symbol is part of a series of PDF417 symbols appended in a structured format
     * (Macro PDF417), this method sets whether or not the optional segment count field is included
     * in the control block. Since the segment count is optional, it is not included by default.
     *
     * @param structuredAppendIncludeSegmentCount whether or not the segment count is included
     */
    public void setStructuredAppendIncludeSegmentCount(boolean structuredAppendIncludeSegmentCount) {
        this.structuredAppendIncludeSegmentCount = structuredAppendIncludeSegmentCount;
    }

    /**
     * Returns whether or not the segment count is included in the control block if this symbol is part of
     * a series of PDF417 symbols appended in a structured format (Macro PDF417). By default, the segment
     * count is not included.
     *
     * @return whether or not the segment count is included
     */
    public boolean getStructuredAppendIncludeSegmentCount() {
        return structuredAppendIncludeSegmentCount;
    }

    /**
     * Sets the type of PDF417 symbol created. Defaults to {@link Mode#NORMAL}.
     *
     * @param mode the type of PDF417 symbol created
     */
    public void setMode(Mode mode) {
        symbolMode = mode;
    }

    /**
     * Returns the type of PDF417 symbol created. Defaults to {@link Mode#NORMAL}.
     *
     * @return the type of PDF417 symbol created
     */
    public Mode getMode() {
        return symbolMode;
    }

    /**
     * <p>Sets the binary data to be encoded and triggers encoding, forcing the use
     * of byte compaction mode. Input data will be assumed to be of the type set by
     * {@link #setDataType(DataType)}.
     *
     * <p>Use {@link #setContent(String)} instead if the data to be encoded is not
     * binary data or you do not want to force the use of byte compaction mode.
     *
     * <p><b>NOTE:</b> Forcing the use of byte compaction mode is usually sub-optimal,
     * and will result in larger symbols than would otherwise be possible. This method
     * should only be used if your downstream systems <b>require</b> the use of byte
     * compaction mode, which is <b>not</b> usually the case.
     *
     * @param data the data to encode
     * @throws OkapiException if no data or data is invalid
     * @see #setContent(String)
     */
    public void setContent(byte[] data) {
        this.forceByteCompaction = true;
        super.setContent(new String(data, StandardCharsets.ISO_8859_1));
    }

    @Override
    protected void encode() {

        eciProcess();

        switch (symbolMode) {
            case MICRO:
                processMicroPdf417();
                break;
            case NORMAL:
            case TRUNCATED:
            default:
                processPdf417();
                break;
        }
    }

    private void processPdf417() {
        int j, loop, offset;
        int[] mccorrection = new int[520];
        int total;
        int c1, c2, c3;
        int[] dummy = new int[35];
        int selectedECCLevel;
        StringBuilder codebarre = new StringBuilder();
        StringBuilder bin = new StringBuilder();

        List< Block > blocks = createBlocks(inputData, forceByteCompaction);

        /* now compress the data */
        codeWordCount = 0;

        if (readerInit) {
            codeWords[codeWordCount] = 921; /* Reader Initialisation */
            codeWordCount++;
        }

        processEci(eciMode);

        int blockCount = 0;
        for (int i = 0; i < blocks.size(); i++) {
            Block block = blocks.get(i);
            switch (block.mode) {
                case TEX:
                    /* text mode */
                    boolean firstBlock = (i == 0);
                    processText(inputData, blockCount, block.length, firstBlock);
                    break;
                case BYT:
                    /* octet stream mode */
                    EncodingMode lastMode = (i == 0 ? EncodingMode.TEX : blocks.get(i - 1).mode);
                    processBytes(inputData, blockCount, block.length, lastMode);
                    break;
                case NUM:
                    /* numeric mode */
                    processNumbers(inputData, blockCount, block.length, false);
                    break;
                default:
                    throw new OkapiInternalException("Unknown block type: " + block.mode);
            }
            blockCount += block.length;
        }

        int dataCount = codeWordCount;
        int macroCount = addMacroCodewords();

        /* Now take care of the number of CWs per row */

        // if we have to default the ECC level, do so per the
        // recommendations in the specification (Table E.1)
        selectedECCLevel = preferredEccLevel;
        if (selectedECCLevel < 0) {
            if (codeWordCount <= 40) {
                selectedECCLevel = 2;
            } else if (codeWordCount <= 160) {
                selectedECCLevel = 3;
            } else if (codeWordCount <= 320) {
                selectedECCLevel = 4;
            } else if (codeWordCount <= 863) {
                selectedECCLevel = 5;
            } else {
                selectedECCLevel = 6;
            }
        }

        int k = 1 << (selectedECCLevel + 1); // error correction codeword count
        int needed = 1 + dataCount + macroCount + k; // not including padding codewords

        validateRows(3, 90);
        validateColumns(1, 30);

        if (columns != null) {
            if (rows != null) {
                // user specified both columns and rows; make sure the data fits
                if (columns * rows < needed) {
                    throw new OkapiInputException("Too few rows (" + rows + ") and columns (" + columns + ") to hold codewords (" + needed + ")");
                }
            } else {
                // user only specified column count; figure out row count
                rows = (int) Math.max(Math.ceil(needed / (double) columns), 3);
            }
        } else {
            if (rows != null) {
                // user only specified row count; figure out column count
                columns = (int) Math.ceil(needed / (double) rows);
            } else {
                // user didn't specify columns or rows; figure both out
                columns = (int) (0.5 + Math.sqrt((needed - 1) / 3.0));
                rows = (int) Math.ceil(needed / (double) columns);
            }
        }

        validateRows(3, 90);
        validateColumns(1, 30);

        /* add the padding (before the Macro PDF417 control block, if it exists) */
        int padCount = (columns * rows) - codeWordCount - k - 1;
        int macroStart = codeWordCount - macroCount;
        System.arraycopy(codeWords, macroStart, codeWords, macroStart + padCount, macroCount);
        Arrays.fill(codeWords, macroStart, macroStart + padCount, 900);
        codeWordCount += padCount;

        /* add the length descriptor */
        for (int i = codeWordCount; i > 0; i--) {
            codeWords[i] = codeWords[i - 1];
        }
        codeWordCount++;
        codeWords[0] = codeWordCount;

        /* add codeword info to debug output */
        info("Codewords: ");
        for (int i = 0; i < codeWordCount; i++) {
            infoSpace(codeWords[i]);
        }
        infoLine();
        infoLine("Length Descriptor Codewords: 1");
        infoLine("Data Codewords: " + dataCount);
        infoLine("Padding Codewords: " + padCount);
        infoLine("Macro Codewords: " + macroCount);
        infoLine("ECC Codewords: " + k);
        infoLine("Total Codewords: " + (columns * rows));

        /* 796 - we now take care of the Reed Solomon codes */
        switch (selectedECCLevel) {
            case 1:
                offset = 2;
                break;
            case 2:
                offset = 6;
                break;
            case 3:
                offset = 14;
                break;
            case 4:
                offset = 30;
                break;
            case 5:
                offset = 62;
                break;
            case 6:
                offset = 126;
                break;
            case 7:
                offset = 254;
                break;
            case 8:
                offset = 510;
                break;
            default:
                offset = 0;
                break;
        }

        for (loop = 0; loop < 520; loop++) {
            mccorrection[loop] = 0;
        }

        for (int i = 0; i < codeWordCount; i++) {
            total = (codeWords[i] + mccorrection[k - 1]) % 929;
            for (j = k - 1; j > 0; j--) {
                mccorrection[j] = (mccorrection[j - 1] + 929 - (total * COEFRS[offset + j]) % 929) % 929;
            }
            mccorrection[0] = (929 - (total * COEFRS[offset + j]) % 929) % 929;
        }

        /* we add these codes to the string */
        for (int i = k - 1; i >= 0; i--) {
            codeWords[codeWordCount++] = mccorrection[i] != 0 ? 929 - mccorrection[i] : 0;
        }

        /* make sure total codeword count isn't too high */
        if (codeWordCount > 929) {
            throw new OkapiInputException("Too many codewords required (" + codeWordCount + ", but max is 929)");
        }

        assert 1 + dataCount + padCount + macroCount + k == columns * rows;
        assert codeWordCount == columns * rows;

        /* 818 - The CW string is finished */
        c1 = (rows - 1) / 3;
        c2 = (selectedECCLevel * 3) + (rows - 1) % 3;
        c3 = columns - 1;

        readable = "";
        row_count = rows;
        pattern = new String[rows];
        row_height = new int[rows];
        infoLine("Grid Size: " + columns + " X " + rows);

        /* we now encode each row */
        for (int i = 0; i < rows; i++) {
            for (j = 0; j < columns; j++) {
                dummy[j + 1] = codeWords[i * columns + j];
            }
            k = (i / 3) * 30;
            switch (i % 3) {
                case 0:
                    offset = 0; // cluster 0
                    dummy[0] = k + c1; // left row indicator
                    dummy[columns + 1] = k + c3; // right row indicator
                    break;
                case 1:
                    offset = 929; // cluster 3
                    dummy[0] = k + c2; // left row indicator
                    dummy[columns + 1] = k + c1; // right row indicator
                    break;
                case 2:
                    offset = 1858; // cluster 6
                    dummy[0] = k + c3; // left row indicator
                    dummy[columns + 1] = k + c2; // right row indicator
                    break;
            }
            codebarre.setLength(0);
            codebarre.append("+*");
            for (j = 0; j <= columns + 1; j++) {
                if (!(symbolMode == Mode.TRUNCATED && j > columns)) {
                    codebarre.append(CODAGEMC[offset + dummy[j]]);
                    codebarre.append('*');
                }
            }
            if(symbolMode != Mode.TRUNCATED) {
                codebarre.append('-');
            }
            bin.setLength(0);
            for (j = 0; j < codebarre.length(); j++) {
                bin.append(PDF_TTF[positionOf(codebarre.charAt(j), BR_SET)]);
            }
            pattern[i] = bin2pat(bin);
            row_height[i] = default_height;
        }
    }

    private void processMicroPdf417() { /* like PDF417 only much smaller! */

        int k, j, longueur, offset;
        int total;
        int LeftRAPStart, CentreRAPStart, RightRAPStart, StartCluster;
        int LeftRAP, CentreRAP, RightRAP, Cluster, flip, loop;
        int[] dummy = new int[5];
        int[] mccorrection = new int[50];
        StringBuilder codebarre = new StringBuilder();
        StringBuilder bin = new StringBuilder();

        /* Encoding starts out the same as PDF417, so use the same code */

        List< Block > blocks = createBlocks(inputData, forceByteCompaction);

        /* 541 - now compress the data */
        codeWordCount = 0;
        if (readerInit) {
            codeWords[codeWordCount] = 921; /* Reader Initialisation */
            codeWordCount++;
        }

        processEci(eciMode);

        int blockCount = 0;
        for (int i = 0; i < blocks.size(); i++) {
            Block block = blocks.get(i);
            switch (block.mode) {
                case TEX:
                    /* text mode */
                    processText(inputData, blockCount, block.length, false); // TODO: this shouldn't always be false?
                    break;
                case BYT:
                    /* octet stream mode */
                    EncodingMode lastMode = (i == 0 ? EncodingMode.TEX : blocks.get(i - 1).mode);
                    processBytes(inputData, blockCount, block.length, lastMode);
                    break;
                case NUM:
                    /* numeric mode */
                    processNumbers(inputData, blockCount, block.length, false);
                    break;
                default:
                    throw new OkapiInternalException("Unknown block type: " + block.mode);
            }
            blockCount += block.length;
        }

        int dataCount = codeWordCount;
        int macroCount = addMacroCodewords();

        /* This is where it all changes! */

        validateRows(4, 44);
        validateColumns(1, 4);

        if (columns != null) {
            int max;
            switch (columns) {
                case 1:
                    max = 20;
                    break;
                case 2:
                    max = 37;
                    break;
                case 3:
                    max = 82;
                    break;
                case 4:
                    max = 126;
                    break;
                default:
                    throw new OkapiInputException("Invalid column count: " + columns);
            }
            if (codeWordCount > max) {
                throw new OkapiInputException("Too few columns (" + columns + ") to hold data codewords (" + codeWordCount + ")");
            }
        }

        /* Now figure out which variant of the symbol to use and load values accordingly */

        int variant = getMicroPdf417Variant(codeWordCount, columns, rows);

        /* Now we have the variant we can load the data */

        variant--;
        columns = MICRO_VARIANTS[variant]; /* columns */
        rows = MICRO_VARIANTS[variant + 34]; /* rows */
        k = MICRO_VARIANTS[variant + 68]; /* number of EC CWs */
        longueur = (columns * rows) - k; /* number of non-EC CWs */
        int padCount = longueur - codeWordCount; /* amount of padding required */
        offset = MICRO_VARIANTS[variant + 102]; /* coefficient offset */

        /* add the padding (before the Macro PDF417 control block, if it exists) */
        int macroStart = codeWordCount - macroCount;
        System.arraycopy(codeWords, macroStart, codeWords, macroStart + padCount, macroCount);
        Arrays.fill(codeWords, macroStart, macroStart + padCount, 900);
        codeWordCount += padCount;

        /* add codeword info to debug output */
        info("Codewords: ");
        for (int i = 0; i < codeWordCount; i++) {
            infoSpace(codeWords[i]);
        }
        infoLine();
        infoLine("Data Codewords: " + dataCount);
        infoLine("Padding Codewords: " + padCount);
        infoLine("Macro Codewords: " + macroCount);
        infoLine("ECC Codewords: " + k);
        infoLine("Total Codewords: " + (columns * rows));

        /* Reed-Solomon error correction */
        longueur = codeWordCount;
        for (loop = 0; loop < 50; loop++) {
            mccorrection[loop] = 0;
        }

        for (int i = 0; i < longueur; i++) {
            total = (codeWords[i] + mccorrection[k - 1]) % 929;
            for (j = k - 1; j >= 0; j--) {
                if (j == 0) {
                    mccorrection[j] = (929 - (total * MICRO_COEFFS[offset + j]) % 929) % 929;
                } else {
                    mccorrection[j] = (mccorrection[j - 1] + 929 - (total * MICRO_COEFFS[offset + j]) % 929) % 929;
                }
            }
        }

        for (j = 0; j < k; j++) {
            if (mccorrection[j] != 0) {
                mccorrection[j] = 929 - mccorrection[j];
            }
        }
        /* we add these codes to the string */
        for (int i = k - 1; i >= 0; i--) {
            codeWords[codeWordCount] = mccorrection[i];
            codeWordCount++;
        }

        assert dataCount + padCount + macroCount + k == columns * rows;
        assert codeWordCount == columns * rows;

        /* Now get the RAP (Row Address Pattern) start values */
        LeftRAPStart = RAP_TABLE[variant];
        CentreRAPStart = RAP_TABLE[variant + 34];
        RightRAPStart = RAP_TABLE[variant + 68];
        StartCluster = RAP_TABLE[variant + 102] / 3;

        /* That's all values loaded, get on with the encoding */

        LeftRAP = LeftRAPStart;
        CentreRAP = CentreRAPStart;
        RightRAP = RightRAPStart;
        Cluster = StartCluster; /* Cluster can be 0, 1 or 2 for Cluster(0), Cluster(3) and Cluster(6) */

        readable = "";
        pattern = new String[rows];
        row_count = rows;
        row_height = new int[rows];

        infoLine("Grid Size: " + columns + " X " + row_count);

        for (int i = 0; i < rows; i++) {
            codebarre.setLength(0);
            offset = 929 * Cluster;
            for (j = 0; j < 5; j++) {
                dummy[j] = 0;
            }
            for (j = 0; j < columns; j++) {
                dummy[j + 1] = codeWords[i * columns + j];
            }

            /* Copy the data into codebarre */
            codebarre.append(RAPLR[LeftRAP]);
            codebarre.append('1');
            codebarre.append(CODAGEMC[offset + dummy[1]]);
            codebarre.append('1');
            if (columns == 3) {
                codebarre.append(RAPC[CentreRAP]);
            }
            if (columns >= 2) {
                codebarre.append('1');
                codebarre.append(CODAGEMC[offset + dummy[2]]);
                codebarre.append('1');
            }
            if (columns == 4) {
                codebarre.append(RAPC[CentreRAP]);
            }
            if (columns >= 3) {
                codebarre.append('1');
                codebarre.append(CODAGEMC[offset + dummy[3]]);
                codebarre.append('1');
            }
            if (columns == 4) {
                codebarre.append('1');
                codebarre.append(CODAGEMC[offset + dummy[4]]);
                codebarre.append('1');
            }
            codebarre.append(RAPLR[RightRAP]);
            codebarre.append('1'); /* stop */

            /* Now codebarre is a mixture of letters and numbers */

            flip = 1;
            bin.setLength(0);
            for (loop = 0; loop < codebarre.length(); loop++) {
                if ((codebarre.charAt(loop) >= '0') && (codebarre.charAt(loop) <= '9')) {
                    for (k = 0; k < Character.getNumericValue(codebarre.charAt(loop)); k++) {
                        if (flip == 0) {
                            bin.append('0');
                        } else {
                            bin.append('1');
                        }
                    }
                    if (flip == 0) {
                        flip = 1;
                    } else {
                        flip = 0;
                    }
                } else {
                    bin.append(PDF_TTF[positionOf(codebarre.charAt(loop), BR_SET)]);
                }
            }

            /* so now pattern[] holds the string of '1's and '0's. - copy this to the symbol */
            pattern[i] = bin2pat(bin);
            row_height[i] = default_height;

            /* Set up RAPs and Cluster for next row */
            LeftRAP++;
            CentreRAP++;
            RightRAP++;
            Cluster++;

            if (LeftRAP == 53) {
                LeftRAP = 1;
            }
            if (CentreRAP == 53) {
                CentreRAP = 1;
            }
            if (RightRAP == 53) {
                RightRAP = 1;
            }
            if (Cluster == 3) {
                Cluster = 0;
            }
        }
    }

    private void validateRows(int min, int max) {
        if (rows != null) {
            if (rows < min) {
                throw new OkapiInputException("Too few rows (" + rows + ")");
            } else if (rows > max) {
                throw new OkapiInputException("Too many rows (" + rows + ")");
            }
        }
    }

    private void validateColumns(int min, int max) {
        if (columns != null) {
            if (columns < min) {
                throw new OkapiInputException("Too few columns (" + columns + ")");
            } else if (columns > max) {
                throw new OkapiInputException("Too many columns (" + columns + ")");
            }
        }
    }

    private static EncodingMode chooseMode(int codeascii) {
        if (codeascii >= '0' && codeascii <= '9') {
            return EncodingMode.NUM;
        } else if (codeascii == '\t' || codeascii == '\n' || codeascii == '\r' || (codeascii >= ' ' && codeascii <= '~')) {
            return EncodingMode.TEX;
        } else {
            return EncodingMode.BYT;
        }
    }

    private static int getMicroPdf417Variant(int codeWordCount, Integer columns, Integer rows) {
        for (int i = 0; i < 34; i++) {
            int maxCodewordCount = MICRO_AUTOSIZE[i];
            if (codeWordCount <= maxCodewordCount) {
                int variant = MICRO_AUTOSIZE[i + 34];
                int columnsForThisVariant = MICRO_VARIANTS[variant - 1];
                int rowsForThisVariant = MICRO_VARIANTS[variant - 1 + 34];
                if ((columns == null || columns == columnsForThisVariant) && (rows == null || rows == rowsForThisVariant)) {
                    return variant;
                }
            }
        }
        throw new OkapiInputException("Unable to determine MicroPDF417 variant for " + codeWordCount + " codewords");
    }

    /** Determines the encoding block groups for the specified data. */
    private static List< Block > createBlocks(int[] data, boolean forceByteCompaction) {

        if (forceByteCompaction) {
            return Collections.singletonList(new Block(EncodingMode.BYT, data.length));
        }

        List< Block > blocks = new ArrayList<>();
        Block current = null;

        for (int i = 0; i < data.length; i++) {
            EncodingMode mode = chooseMode(data[i]);
            if ((current != null && current.mode == mode) &&
                (mode != EncodingMode.NUM || current.length < MAX_NUMERIC_COMPACTION_BLOCK_SIZE)) {
                current.length++;
            } else {
                current = new Block(mode, 1);
                blocks.add(current);
            }
        }

        smoothBlocks(blocks);

        return blocks;
    }

    /** Combines adjacent blocks of different types in very specific scenarios. */
    private static void smoothBlocks(List< Block > blocks) {

        for (int i = 0; i < blocks.size(); i++) {
            Block block = blocks.get(i);
            EncodingMode last = (i > 0 ? blocks.get(i - 1).mode : EncodingMode.FALSE);
            EncodingMode next = (i < blocks.size() - 1 ? blocks.get(i + 1).mode : EncodingMode.FALSE);
            if (block.mode == EncodingMode.NUM) {
                if (i == 0) { /* first block */
                    if (next == EncodingMode.TEX && block.length < 8) {
                        block.mode = EncodingMode.TEX;
                    } else if (next == EncodingMode.BYT && block.length == 1) {
                        block.mode = EncodingMode.BYT;
                    }
                } else if (i == blocks.size() - 1) { /* last block */
                    if (last == EncodingMode.TEX && block.length < 7) {
                        block.mode = EncodingMode.TEX;
                    } else if (last == EncodingMode.BYT && block.length == 1) {
                        block.mode = EncodingMode.BYT;
                    }
                } else { /* not first or last block */
                    if (last == EncodingMode.BYT && next == EncodingMode.BYT && block.length < 4) {
                        block.mode = EncodingMode.BYT;
                    } else if (last == EncodingMode.BYT && next == EncodingMode.TEX && block.length < 4) {
                        block.mode = EncodingMode.TEX;
                    } else if (last == EncodingMode.TEX && next == EncodingMode.BYT && block.length < 5) {
                        block.mode = EncodingMode.TEX;
                    } else if (last == EncodingMode.TEX && next == EncodingMode.TEX && block.length < 8) {
                        block.mode = EncodingMode.TEX;
                    } else if (last == EncodingMode.NUM && next == EncodingMode.TEX && block.length < 8) {
                        block.mode = EncodingMode.TEX;
                    }
                }
            }
        }

        mergeBlocks(blocks);

        for (int i = 0; i < blocks.size(); i++) {
            Block block = blocks.get(i);
            EncodingMode last = (i > 0 ? blocks.get(i - 1).mode : EncodingMode.FALSE);
            EncodingMode next = (i < blocks.size() - 1 ? blocks.get(i + 1).mode : EncodingMode.FALSE);
            if (block.mode == EncodingMode.TEX && i > 0) { /* not the first */
                if (i == blocks.size() - 1) { /* the last one */
                    if (last == EncodingMode.BYT && block.length == 1) {
                        block.mode = EncodingMode.BYT;
                    }
                } else { /* not the last one */
                    if (last == EncodingMode.BYT && next == EncodingMode.BYT && block.length < 5) {
                        block.mode = EncodingMode.BYT;
                    }
                    if (((last == EncodingMode.BYT && next != EncodingMode.BYT) ||
                         (last != EncodingMode.BYT && next == EncodingMode.BYT)) && (block.length < 3)) {
                        block.mode = EncodingMode.BYT;
                    }
                }
            }
        }

        mergeBlocks(blocks);
    }

    /** Combines adjacent blocks of the same type. */
    private static void mergeBlocks(List< Block > blocks) {
        for (int i = 1; i < blocks.size(); i++) {
            Block b1 = blocks.get(i - 1);
            Block b2 = blocks.get(i);
            if ((b1.mode == b2.mode) &&
                (b1.mode != EncodingMode.NUM || b1.length + b2.length <= MAX_NUMERIC_COMPACTION_BLOCK_SIZE)) {
                b1.length += b2.length;
                blocks.remove(i);
                i--;
            }
        }
    }

    private void processEci(int eci) {
        if (eci == 3) {
            return; // default, no need to specify
        }
        /* Encoding ECI assignment number, from ISO/IEC 15438 Table 8 */
        if (eci <= 899) {
            codeWords[codeWordCount++] = 927;
            codeWords[codeWordCount++] = eci;
        } else if (eci >= 900 && eci <= 810899) {
            codeWords[codeWordCount++] = 926;
            codeWords[codeWordCount++] = (eci / 900) - 1;
            codeWords[codeWordCount++] = eci % 900;
        } else if (eci >= 810900 && eci <= 811799) {
            codeWords[codeWordCount++] = 925;
            codeWords[codeWordCount++] = eci - 810900;
        }
    }

    private void processText(int[] data, int start, int length, boolean skipLatch) {
        int j, blockIndext, curtable;
        int codeascii;
        int wnet = 0;
        int[] listet0 = new int[length];
        int[] listet1 = new int[length];
        int[] chainet = new int[length * 4];

        /* listet will contain the table numbers and the value of each characters */
        for (blockIndext = 0; blockIndext < length; blockIndext++) {
            codeascii = data[start + blockIndext];
            switch (codeascii) {
            case '\t':
                listet0[blockIndext] = 12;
                listet1[blockIndext] = 12;
                break;
            case '\n':
                listet0[blockIndext] = 8;
                listet1[blockIndext] = 15;
                break;
            case '\r':
                listet0[blockIndext] = 12;
                listet1[blockIndext] = 11;
                break;
            default:
                listet0[blockIndext] = ASCII_X[codeascii - 32];
                listet1[blockIndext] = ASCII_Y[codeascii - 32];
                break;
            }
        }

        curtable = 1; /* default table */
        for (j = 0; j < length; j++) {
            if ((listet0[j] & curtable) != 0) { /* The character is in the current table */
                chainet[wnet] = listet1[j];
                wnet++;
            } else { /* Obliged to change table */
                boolean flag = false; /* True if we change table for only one character */
                if (j == (length - 1)) {
                    flag = true;
                } else {
                    if ((listet0[j] & listet0[j + 1]) == 0) {
                        flag = true;
                    }
                }

                if (flag) { /* we change only one character - look for temporary switch */
                    if (((listet0[j] & 1) != 0) && (curtable == 2)) { /* T_UPP */
                        chainet[wnet] = 27;
                        chainet[wnet + 1] = listet1[j];
                        wnet += 2;
                    }
                    if ((listet0[j] & 8) != 0) { /* T_PUN */
                        chainet[wnet] = 29;
                        chainet[wnet + 1] = listet1[j];
                        wnet += 2;
                    }
                    if (!((((listet0[j] & 1) != 0) && (curtable == 2)) || ((listet0[j] & 8) != 0))) {
                        /* No temporary switch available */
                        flag = false;
                    }
                }

                if (!(flag)) {
                    int newtable;

                    if (j == (length - 1)) {
                        newtable = listet0[j];
                    } else {
                        if ((listet0[j] & listet0[j + 1]) == 0) {
                            newtable = listet0[j];
                        } else {
                            newtable = listet0[j] & listet0[j + 1];
                        }
                    }

                    /* Maintain the first if several tables are possible */
                    switch (newtable) {
                    case 3:
                    case 5:
                    case 7:
                    case 9:
                    case 11:
                    case 13:
                    case 15:
                        newtable = 1;
                        break;
                    case 6:
                    case 10:
                    case 14:
                        newtable = 2;
                        break;
                    case 12:
                        newtable = 4;
                        break;
                    }

                    /* select the switch */
                    switch (curtable) {
                    case 1:
                        switch (newtable) {
                        case 2:
                            chainet[wnet] = 27;
                            wnet++;
                            break;
                        case 4:
                            chainet[wnet] = 28;
                            wnet++;
                            break;
                        case 8:
                            chainet[wnet] = 28;
                            wnet++;
                            chainet[wnet] = 25;
                            wnet++;
                            break;
                        }
                        break;
                    case 2:
                        switch (newtable) {
                        case 1:
                            chainet[wnet] = 28;
                            wnet++;
                            chainet[wnet] = 28;
                            wnet++;
                            break;
                        case 4:
                            chainet[wnet] = 28;
                            wnet++;
                            break;
                        case 8:
                            chainet[wnet] = 28;
                            wnet++;
                            chainet[wnet] = 25;
                            wnet++;
                            break;
                        }
                        break;
                    case 4:
                        switch (newtable) {
                        case 1:
                            chainet[wnet] = 28;
                            wnet++;
                            break;
                        case 2:
                            chainet[wnet] = 27;
                            wnet++;
                            break;
                        case 8:
                            chainet[wnet] = 25;
                            wnet++;
                            break;
                        }
                        break;
                    case 8:
                        switch (newtable) {
                        case 1:
                            chainet[wnet] = 29;
                            wnet++;
                            break;
                        case 2:
                            chainet[wnet] = 29;
                            wnet++;
                            chainet[wnet] = 27;
                            wnet++;
                            break;
                        case 4:
                            chainet[wnet] = 29;
                            wnet++;
                            chainet[wnet] = 28;
                            wnet++;
                            break;
                        }
                        break;
                    }
                    curtable = newtable;
                    /* at last we add the character */
                    chainet[wnet] = listet1[j];
                    wnet++;
                }
            }
        }

        if ((wnet & 1) != 0) {
            chainet[wnet] = 29;
            wnet++;
        }

        /* Now translate the string chainet into codewords */

        if (!skipLatch) {
            // text compaction mode is the default mode for PDF417,
            // so no need for an explicit latch if this is the first block
            codeWords[codeWordCount] = 900;
            codeWordCount++;
        }

        for (j = 0; j < wnet; j += 2) {
            int cw_number = (30 * chainet[j]) + chainet[j + 1];
            codeWords[codeWordCount] = cw_number;
            codeWordCount++;
        }
    }

    private void processBytes(int[] data, int start, int length, EncodingMode lastMode) {
        int len = 0;
        int chunkLen = 0;
        BigInteger mantisa;
        BigInteger total;
        BigInteger word;

        mantisa = new BigInteger("0");
        total = new BigInteger("0");

        if (length == 1 && lastMode == EncodingMode.TEX) {
            codeWords[codeWordCount++] = 913;
            codeWords[codeWordCount++] = data[start];
        } else {
            /* select the switch for multiple of 6 bytes */
            if (length % 6 == 0) {
                codeWords[codeWordCount++] = 924;
            } else {
                codeWords[codeWordCount++] = 901;
            }

            while (len < length) {
                chunkLen = length - len;
                if (6 <= chunkLen) /* Take groups of 6 */{
                    chunkLen = 6;
                    len += chunkLen;
                    total = BigInteger.valueOf(0);

                    while ((chunkLen--) != 0) {
                        mantisa = BigInteger.valueOf(data[start++]);
                        total = total.or(mantisa.shiftLeft(chunkLen * 8));
                    }

                    chunkLen = 5;

                    while ((chunkLen--) != 0) {

                        word = total.mod(BigInteger.valueOf(900));
                        codeWords[codeWordCount + chunkLen] = word.intValue();
                        total = total.divide(BigInteger.valueOf(900));
                    }
                    codeWordCount += 5;
                } else /* If it remain a group of less than 6 bytes */{
                    len += chunkLen;
                    while ((chunkLen--) != 0) {
                        codeWords[codeWordCount++] = data[start++];
                    }
                }
            }
        }
    }

    private void processNumbers(int[] data, int start, int length, boolean skipLatch) {

        BigInteger tVal, dVal;
        int[] d = new int[16];
        int cw_count;

        if (!skipLatch) {
            // we don't need to latch to numeric mode in some cases, e.g.
            // during numeric compaction of the Macro PDF417 segment index
            codeWords[codeWordCount++] = 902;
        }

        StringBuilder t = new StringBuilder(length + 1);
        t.append('1');
        for (int i = 0; i < length; i++) {
            t.append((char) data[start + i]);
        }

        tVal = new BigInteger(t.toString());

        cw_count = 0;
        do {
            dVal = tVal.mod(BigInteger.valueOf(900));
            d[cw_count] = dVal.intValue();
            tVal = tVal.divide(BigInteger.valueOf(900));
            cw_count++;
        } while (tVal.compareTo(BigInteger.ZERO) == 1);

        for (int i = cw_count - 1; i >= 0; i--) {
            codeWords[codeWordCount++] = d[i];
        }
    }

    private void processFiveDigits(int i) {
        assert i >= 0;
        assert i <= 99_999;
        int[] data = new int[5];
        for (int x = data.length - 1; x >= 0; x--) {
            data[x] = '0' + (i % 10);
            i /= 10;
        }
        processNumbers(data, 0, data.length, true);
    }

    /** Adds the Macro PDF417 control block codewords (if any). */
    private int addMacroCodewords() {

        // if the structured append series size is 1, this isn't
        // actually part of a structured append series
        if (structuredAppendTotal == 1) {
            return 0;
        }

        int macroStart = codeWordCount;

        // add the Macro marker codeword
        codeWords[codeWordCount++] = 928;

        // add the segment index, padded with leading zeros to five digits
        // use numeric compaction, but no latch
        int segmentIndex = structuredAppendPosition - 1;
        processFiveDigits(segmentIndex);

        // add the file ID (base 900, which is easy since we limit
        // file ID values to the range 0 to 899)
        codeWords[codeWordCount++] = structuredAppendFileId;

        // optional fields: add the file name, if specified
        if (structuredAppendFileName != null && !structuredAppendFileName.isEmpty()) {
            codeWords[codeWordCount++] = 923;
            codeWords[codeWordCount++] = 000;
            EciMode eci = EciMode.of(structuredAppendFileName, "ISO8859_1", 3).or(structuredAppendFileName, "UTF8", 26);
            int[] data2 = toBytes(structuredAppendFileName, eci.charset);
            processEci(eci.mode);
            processText(data2, 0, data2.length, true);
        }

        // optional fields: add segment count, if requested
        if (structuredAppendIncludeSegmentCount) {
            codeWords[codeWordCount++] = 923;
            codeWords[codeWordCount++] = 001;
            processFiveDigits(structuredAppendTotal);
        }

        // add the terminator to the last symbol of the series
        boolean last = (structuredAppendPosition == structuredAppendTotal);
        if (last) {
            codeWords[codeWordCount++] = 922;
        }

        return codeWordCount - macroStart;
    }

    private static class Block {

        public EncodingMode mode;
        public int length;

        public Block(EncodingMode mode, int length) {
            this.mode = mode;
            this.length = length;
        }

        @Override
        public String toString() {
            return mode + "x" + length;
        }
    }
}
