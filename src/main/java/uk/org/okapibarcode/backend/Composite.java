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

import static uk.org.okapibarcode.util.Arrays.positionOf;

import java.awt.geom.Rectangle2D;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>Implements GS1 Composite symbology according to ISO/IEC 24723:2010.
 *
 * <p>Composite symbols comprise a 2D element which encodes GS1 data and a
 * "linear" element which can be UPC, EAN, Code 128 or GS1 DataBar symbol.
 *
 * @author <a href="mailto:rstuart114@gmail.com">Robin Stuart</a>
 */
public class Composite extends Symbol {

    /* CC-A component coefficients from ISO/IEC 24728:2006 Annex F */
    private static final int[] CCA_COEFFS = {
        /* k = 4 */
        522, 568, 723, 809,
        /* k = 5 */
        427, 919, 460, 155, 566,
        /* k = 6 */
        861, 285, 19, 803, 17, 766,
        /* k = 7 */
        76, 925, 537, 597, 784, 691, 437,
        /* k = 8 */
        237, 308, 436, 284, 646, 653, 428, 379
    };

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

    /* rows, error codewords, k-offset of valid CC-A sizes from ISO/IEC 24723:2006 Table 9 */
    private static final int[] CCA_VARIANTS = {
        5, 6, 7, 8, 9, 10, 12, 4, 5, 6, 7, 8, 3, 4, 5, 6, 7, 4, 4, 5, 5, 6, 6, 7, 4, 5, 6, 7, 7, 4, 5, 6, 7, 8, 0, 0, 4, 4, 9, 9, 15, 0, 4, 9, 15, 15, 0, 4, 9, 15, 22
    };

    /* following is Left RAP, Centre RAP, Right RAP and Start Cluster from ISO/IEC 24723:2006 tables 10 and 11 */
    private static final int[] A_RAP_TABLE = {
        39, 1, 32, 8, 14, 43, 20, 11, 1, 5, 15, 21, 40, 43, 46, 34, 29, 0, 0, 0, 0, 0, 0, 0, 43, 33, 37, 47, 1, 20, 23, 26, 14, 9, 19, 33, 12, 40, 46, 23, 52, 23, 13, 17, 27, 33, 52, 3, 6, 46, 41, 6, 0, 3, 3, 3, 0, 3, 3, 0, 3, 6, 6, 0, 0, 0, 0, 3
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

    /* Left and Right Row Address Pattern from Table 2 */
    private static final String[] RAPLR = {
        "", "221311", "311311", "312211", "222211", "213211", "214111", "223111",
        "313111", "322111", "412111", "421111", "331111", "241111", "232111", "231211", "321211",
        "411211", "411121", "411112", "321112", "312112", "311212", "311221", "311131", "311122",
        "311113", "221113", "221122", "221131", "221221", "222121", "312121", "321121", "231121",
        "231112", "222112", "213112", "212212", "212221", "212131", "212122", "212113", "211213",
        "211123", "211132", "211141", "211231", "211222", "211312", "211321", "211411", "212311"
    };

    /* Centre Row Address Pattern from Table 2 */
    private static final String[] RAPC = {
        "", "112231", "121231", "122131", "131131", "131221", "132121", "141121",
        "141211", "142111", "133111", "132211", "131311", "122311", "123211", "124111", "115111",
        "114211", "114121", "123121", "123112", "122212", "122221", "121321", "121411", "112411",
        "113311", "113221", "113212", "113122", "122122", "131122", "131113", "122113", "113113",
        "112213", "112222", "112312", "112321", "111421", "111331", "111322", "111232", "111223",
        "111133", "111124", "111214", "112114", "121114", "121123", "121132", "112132", "112141"
    };

    private static final int[] MICRO_VARIANTS = {
        1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
        11, 14, 17, 20, 24, 28, 8, 11, 14, 17, 20, 23, 26, 6, 8, 10, 12, 15, 20, 26, 32, 38, 44, 4, 6, 8, 10, 12, 15, 20, 26, 32, 38, 44,
        7, 7, 7, 8, 8, 8, 8, 9, 9, 10, 11, 13, 15, 12, 14, 16, 18, 21, 26, 32, 38, 44, 50, 8, 12, 14, 16, 18, 21, 26, 32, 38, 44, 50,
        0, 0, 0, 7, 7, 7, 7, 15, 15, 24, 34, 57, 84, 45, 70, 99, 115, 133, 154, 180, 212, 250, 294, 7, 45, 70, 99, 115, 133, 154, 180, 212, 250, 294
    };

    /* rows, columns, error codewords, k-offset */
    /* MicroPDF417 coefficients from ISO/IEC 24728:2006 Annex F */
    private static final int[] MICROCOEFFS = {
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

    /* following is Left RAP, Centre RAP, Right RAP and Start Cluster from ISO/IEC 24728:2006 tables 10, 11 and 12 */
    private static final int[] RAP_TABLE = {
        1, 8, 36, 19, 9, 25, 1, 1, 8, 36, 19, 9, 27, 1, 7, 15, 25, 37, 1, 1, 21, 15, 1, 47, 1, 7, 15, 25, 37, 1, 1, 21, 15, 1,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 7, 15, 25, 37, 17, 9, 29, 31, 25, 19, 1, 7, 15, 25, 37, 17, 9, 29, 31, 25,
        9, 8, 36, 19, 17, 33, 1, 9, 8, 36, 19, 17, 35, 1, 7, 15, 25, 37, 33, 17, 37, 47, 49, 43, 1, 7, 15, 25, 37, 33, 17, 37, 47, 49,
        0, 3, 6, 0, 6, 0, 0, 0, 3, 6, 0, 6, 6, 0, 0, 6, 0, 0, 0, 0, 6, 6, 0, 3, 0, 0, 6, 0, 0, 0, 0, 6, 6, 0
    };

    public static enum LinearEncoding {
        UPCA, UPCE, EAN, CODE_128, DATABAR_14, DATABAR_14_STACK,
        DATABAR_14_STACK_OMNI, DATABAR_LIMITED, DATABAR_EXPANDED,
        DATABAR_EXPANDED_STACK
    }

    private static enum GeneralFieldMode {
        NUMERIC, ALPHA, ISOIEC, INVALID_CHAR, ANY_ENC, ALPHA_OR_ISO
    };

    public static enum CompositeMode {
        CC_A, CC_B, CC_C
    }

    private String binary_string;
    private int ecc;
    private LinearEncoding symbology = LinearEncoding.CODE_128;
    private String general_field;
    private GeneralFieldMode[] general_field_type;
    private int cc_width;
    private int[][] pwr928 = new int[69][7];
    private int[] codeWords = new int[180];
    private int codeWordCount;
    private int[] bitStr = new int[13];
    private int[] inputData;
    private CompositeMode cc_mode;
    private String linearContent;
    private CompositeMode userPreferredMode = CompositeMode.CC_A;
    private int target_bitsize;
    private int remainder;
    private int linearWidth; // Width of Code 128 linear

    public Composite() {
        inputDataType = Symbol.DataType.GS1;
    }

    @Override
    public void setDataType(DataType dataType) {
        if (dataType != Symbol.DataType.GS1) {
            throw new IllegalArgumentException("Only GS1 data type is supported for GS1 Composite symbology.");
        }
    }

    @Override
    protected boolean gs1Supported() {
        return true;
    }

    /**
     * Set the type of linear component included in the composite symbol,
     * this will determine how the lower part of the symbol is encoded.
     *
     * @param linearSymbology The symbology of the linear component
     */
    public void setSymbology(LinearEncoding linearSymbology) {
        symbology = linearSymbology;
    }

    /**
     * Returns the type of linear component included in the composite symbol.
     *
     * @return the type of linear component included in the composite symbol
     */
    public LinearEncoding getSymbology() {
        return symbology;
    }

    /**
     * Set the data to be encoded in the linear component of the composite symbol.
     *
     * @param linearContent the linear data in GS1 format
     */
    public void setLinearContent(String linearContent) {
        this.linearContent = linearContent;
    }

    /**
     * Returns the data encoded in the linear component of the composite symbol.
     *
     * @return the data encoded in the linear component of the composite symbol
     */
    public String getLinearContent() {
        return linearContent;
    }

    /**
     * Set the preferred encoding method for the 2D component of the composite symbol.
     * This value may be ignored if the amount of data supplied is too big for the selected
     * encoding. Mode CC-C can only be used with a Code 128 linear component.
     *
     * @param userMode Preferred mode
     */
    public void setPreferredMode(CompositeMode userMode) {
        userPreferredMode = userMode;
    }

    /**
     * Returns the preferred encoding method for the 2D component of the composite symbol.
     *
     * @return the preferred encoding method for the 2D component of the composite symbol
     */
    public CompositeMode getPreferredMode() {
        return userPreferredMode;
    }

    @Override
    protected void encode() {

        List < Rectangle2D.Double > linear_rect = new ArrayList<>();
        List < TextBox > linear_txt = new ArrayList<>();
        List < Rectangle2D.Double > combine_rect = new ArrayList<>();
        List < TextBox > combine_txt = new ArrayList<>();
        String linear_encodeInfo = null;
        int linear_height = 0;
        int top_shift = 0;
        int bottom_shift = 0;
        int max_x = 0;
        int i;
        linearWidth = 0;

        if (linearContent.isEmpty()) {
            throw new OkapiException("No linear data set");
        }

        // Manage composite component encoding first
        encodeComposite();

        // Then encode linear component
        switch (symbology) {
            case UPCA:
                Upc upca = new Upc();
                upca.setMode(Upc.Mode.UPCA);
                upca.setLinkageFlag(true);
                upca.setContent(linearContent);
                linear_rect = upca.rectangles;
                linear_txt = upca.texts;
                linear_height = upca.symbol_height;
                linear_encodeInfo = upca.encodeInfo;
                bottom_shift = 6;
                top_shift = 3;
                break;
            case UPCE:
                Upc upce = new Upc();
                upce.setMode(Upc.Mode.UPCE);
                upce.setLinkageFlag(true);
                upce.setContent(linearContent);
                linear_rect = upce.rectangles;
                linear_txt = upce.texts;
                linear_height = upce.symbol_height;
                linear_encodeInfo = upce.encodeInfo;
                bottom_shift = 6;
                top_shift = 3;
                break;
            case EAN:
                Ean ean = new Ean();
                if (eanCalculateVersion() == 8) {
                    ean.setMode(Ean.Mode.EAN8);
                    bottom_shift = 14;
                } else {
                    ean.setMode(Ean.Mode.EAN13);
                    bottom_shift = 6;
                    top_shift = 3;
                }
                ean.setLinkageFlag(true);
                ean.setContent(linearContent);
                linear_rect = ean.rectangles;
                linear_txt = ean.texts;
                linear_height = ean.symbol_height;
                linear_encodeInfo = ean.encodeInfo;
                break;
            case CODE_128:
                Code128 code128 = new Code128();
                switch (cc_mode) {
                    case CC_A:
                        code128.setCca();
                        break;
                    case CC_B:
                        code128.setCcb();
                        break;
                    case CC_C:
                        code128.setCcc();
                        bottom_shift = 7;
                        break;
                }
                code128.setDataType(Symbol.DataType.GS1);
                code128.setContent(linearContent);
                linearWidth = code128.symbol_width;
                linear_rect = code128.rectangles;
                linear_txt = code128.texts;
                linear_height = code128.symbol_height;
                linear_encodeInfo = code128.encodeInfo;
                break;
            case DATABAR_14:
                DataBar14 dataBar14 = new DataBar14();
                dataBar14.setLinkageFlag();
                dataBar14.setLinearMode();
                dataBar14.setContent(linearContent);
                linear_rect = dataBar14.rectangles;
                linear_txt = dataBar14.texts;
                linear_height = dataBar14.symbol_height;
                linear_encodeInfo = dataBar14.encodeInfo;
                bottom_shift = 4;
                break;
            case DATABAR_14_STACK_OMNI:
                DataBar14 dataBar14SO = new DataBar14();
                dataBar14SO.setLinkageFlag();
                dataBar14SO.setOmnidirectionalMode();
                dataBar14SO.setContent(linearContent);
                linear_rect = dataBar14SO.rectangles;
                linear_txt = dataBar14SO.texts;
                linear_height = dataBar14SO.symbol_height;
                linear_encodeInfo = dataBar14SO.encodeInfo;
                top_shift = 1;
                break;
            case DATABAR_14_STACK:
                DataBar14 dataBar14S = new DataBar14();
                dataBar14S.setLinkageFlag();
                dataBar14S.setStackedMode();
                dataBar14S.setContent(linearContent);
                linear_rect = dataBar14S.rectangles;
                linear_txt = dataBar14S.texts;
                linear_height = dataBar14S.symbol_height;
                linear_encodeInfo = dataBar14S.encodeInfo;
                top_shift = 1;
                break;
            case DATABAR_LIMITED:
                DataBarLimited dataBarLimited = new DataBarLimited();
                dataBarLimited.setLinkageFlag();
                dataBarLimited.setContent(linearContent);
                linear_rect = dataBarLimited.rectangles;
                linear_txt = dataBarLimited.texts;
                linear_height = dataBarLimited.symbol_height;
                linear_encodeInfo = dataBarLimited.encodeInfo;
                top_shift = 1;
                break;
            case DATABAR_EXPANDED:
                DataBarExpanded dataBarExpanded = new DataBarExpanded();
                dataBarExpanded.setLinkageFlag();
                dataBarExpanded.setNotStacked();
                dataBarExpanded.setContent(linearContent);
                linear_rect = dataBarExpanded.rectangles;
                linear_txt = dataBarExpanded.texts;
                linear_height = dataBarExpanded.symbol_height;
                linear_encodeInfo = dataBarExpanded.encodeInfo;
                top_shift = 2;
                break;
            case DATABAR_EXPANDED_STACK:
                DataBarExpanded dataBarExpandedS = new DataBarExpanded();
                dataBarExpandedS.setLinkageFlag();
                dataBarExpandedS.setStacked();
                dataBarExpandedS.setContent(linearContent);
                linear_rect = dataBarExpandedS.rectangles;
                linear_txt = dataBarExpandedS.texts;
                linear_height = dataBarExpandedS.symbol_height;
                linear_encodeInfo = dataBarExpandedS.encodeInfo;
                top_shift = 2;
                break;
            default:
                throw new OkapiException("Linear symbol not recognised");
        }

        if (cc_mode == CompositeMode.CC_C && symbology == LinearEncoding.CODE_128) {
            /* Width of composite component depends on width of linear component,
               so recalculate. */
            row_count = 0;
            rectangles.clear();
            symbol_height = 0;
            symbol_width = 0;
            encodeInfo = "";
            encodeComposite();
        }

        if (cc_mode != CompositeMode.CC_C && symbology == LinearEncoding.CODE_128) {
            if (linearWidth > symbol_width) {
                top_shift = (linearWidth - symbol_width) / 2;
            }
        }

        for (i = 0; i < rectangles.size(); i++) {
            Rectangle2D.Double comprect = new Rectangle2D.Double(rectangles.get(i).x + top_shift, rectangles.get(i).y, rectangles.get(i).width, rectangles.get(i).height);
            if ((rectangles.get(i).x + top_shift + rectangles.get(i).width) > max_x) {
                max_x = (int) (rectangles.get(i).x + top_shift + rectangles.get(i).width);
            }
            combine_rect.add(comprect);
        }

        for (i = 0; i < linear_rect.size(); i++) {
            Rectangle2D.Double linrect = new Rectangle2D.Double(linear_rect.get(i).x + bottom_shift, linear_rect.get(i).y, linear_rect.get(i).width, linear_rect.get(i).height);
            linrect.y += symbol_height;
            if ((linear_rect.get(i).x + bottom_shift + linear_rect.get(i).width) > max_x) {
                max_x = (int) (linear_rect.get(i).x + bottom_shift + linear_rect.get(i).width);
            }
            combine_rect.add(linrect);
        }

        for (i = 0; i < linear_txt.size(); i++) {
            double x = linear_txt.get(i).x + bottom_shift;
            double y = linear_txt.get(i).y + symbol_height;
            double width = linear_txt.get(i).width;
            String text = linear_txt.get(i).text;
            TextBox lintxt = new TextBox(x, y, width, text, humanReadableAlignment);
            combine_txt.add(lintxt);
        }

        rectangles = combine_rect;
        texts = combine_txt;
        symbol_height += linear_height;
        symbol_width = max_x;
        encodeInfo += linear_encodeInfo;
    }

    private void encodeComposite() {

        if (content.length() > 2990) {
            throw new OkapiException("2D component input data too long");
        }

        cc_mode = userPreferredMode;

        if (cc_mode == CompositeMode.CC_C && symbology != LinearEncoding.CODE_128) {
            /* CC-C can only be used with a GS1-128 linear part */
            throw new OkapiException("Invalid mode (CC-C only valid with GS1-128 linear component)");
        }

        switch (symbology) {
            /* Determine width of 2D component according to ISO/IEC 24723 Table 1 */
            case EAN:
                if (eanCalculateVersion() == 8) {
                    cc_width = 3;
                } else {
                    cc_width = 4;
                }
                break;
            case UPCE:
            case DATABAR_14_STACK_OMNI:
            case DATABAR_14_STACK:
                cc_width = 2;
                break;
            case DATABAR_LIMITED:
                cc_width = 3;
                break;
            case CODE_128:
            case DATABAR_14:
            case DATABAR_EXPANDED:
            case UPCA:
            case DATABAR_EXPANDED_STACK:
                cc_width = 4;
                break;
        }

        encodeInfo += "Composite Width: " + Integer.toString(cc_width) + "\n";

        if (cc_mode == CompositeMode.CC_A && !cc_binary_string()) {
            cc_mode = CompositeMode.CC_B;
        }

        if (cc_mode == CompositeMode.CC_B) { /* If the data didn't fit into CC-A it is recalculated for CC-B */
            if (!cc_binary_string()) {
                if (symbology != LinearEncoding.CODE_128) {
                    throw new OkapiException("Input too long");
                } else {
                    cc_mode = CompositeMode.CC_C;
                }
            }
        }

        if (cc_mode == CompositeMode.CC_C) {
            /* If the data didn't fit in CC-B (and linear
             * part is GS1-128) it is recalculated for CC-C */
            if (!cc_binary_string()) {
                throw new OkapiException("Input too long");
            }
        }

        switch (cc_mode) { /* Note that ecc_level is only relevant to CC-C */
            case CC_A:
                cc_a();
                encodeInfo += "Composite Type: CC-A\n";
                break;
            case CC_B:
                cc_b();
                encodeInfo += "Composite Type: CC-B\n";
                break;
            case CC_C:
                cc_c();
                encodeInfo += "Composite Type: CC-C\n";
                break;
        }

        super.plotSymbol();
    }

    @Override
    protected void plotSymbol() {
        // empty
    }

    private int eanCalculateVersion() {
        /* Determine if EAN-8 or EAN-13 is being used */

        int length = 0;
        int i;
        boolean latch;

        latch = true;
        for (i = 0; i < linearContent.length(); i++) {
            if ((linearContent.charAt(i) >= '0') && (linearContent.charAt(i) <= '9')) {
                if (latch) {
                    length++;
                }
            } else {
                latch = false;
            }
        }

        if (length <= 7) {
            // EAN-8
            return 8;
        } else {
            // EAN-13
            return 13;
        }
    }

    private boolean calculateSymbolSize() {
        int i;
        int binary_length = binary_string.length();
        if (cc_mode == CompositeMode.CC_A) {
            /* CC-A 2D component - calculate remaining space */
            switch (cc_width) {
                case 2:
                    if (binary_length > 167) {
                        return false;
                    }
                    if (binary_length <= 167) {
                        target_bitsize = 167;
                    }
                    if (binary_length <= 138) {
                        target_bitsize = 138;
                    }
                    if (binary_length <= 118) {
                        target_bitsize = 118;
                    }
                    if (binary_length <= 108) {
                        target_bitsize = 108;
                    }
                    if (binary_length <= 88) {
                        target_bitsize = 88;
                    }
                    if (binary_length <= 78) {
                        target_bitsize = 78;
                    }
                    if (binary_length <= 59) {
                        target_bitsize = 59;
                    }
                    break;
                case 3:
                    if (binary_length > 167) {
                        return false;
                    }
                    if (binary_length <= 167) {
                        target_bitsize = 167;
                    }
                    if (binary_length <= 138) {
                        target_bitsize = 138;
                    }
                    if (binary_length <= 118) {
                        target_bitsize = 118;
                    }
                    if (binary_length <= 98) {
                        target_bitsize = 98;
                    }
                    if (binary_length <= 78) {
                        target_bitsize = 78;
                    }
                    break;
                case 4:
                    if (binary_length > 197) {
                        return false;
                    }
                    if (binary_length <= 197) {
                        target_bitsize = 197;
                    }
                    if (binary_length <= 167) {
                        target_bitsize = 167;
                    }
                    if (binary_length <= 138) {
                        target_bitsize = 138;
                    }
                    if (binary_length <= 108) {
                        target_bitsize = 108;
                    }
                    if (binary_length <= 78) {
                        target_bitsize = 78;
                    }
                    break;
            }
        }

        if (cc_mode == CompositeMode.CC_B) {
            /* CC-B 2D component - calculated from ISO/IEC 24728 Table 1  */
            switch (cc_width) {
                case 2:
                    if (binary_length > 336) {
                        return false;
                    }
                    if (binary_length <= 336) {
                        target_bitsize = 336;
                    }
                    if (binary_length <= 296) {
                        target_bitsize = 296;
                    }
                    if (binary_length <= 256) {
                        target_bitsize = 256;
                    }
                    if (binary_length <= 208) {
                        target_bitsize = 208;
                    }
                    if (binary_length <= 160) {
                        target_bitsize = 160;
                    }
                    if (binary_length <= 104) {
                        target_bitsize = 104;
                    }
                    if (binary_length <= 56) {
                        target_bitsize = 56;
                    }
                    break;
                case 3:
                    if (binary_length > 768) {
                        return false;
                    }
                    if (binary_length <= 768) {
                        target_bitsize = 768;
                    }
                    if (binary_length <= 648) {
                        target_bitsize = 648;
                    }
                    if (binary_length <= 536) {
                        target_bitsize = 536;
                    }
                    if (binary_length <= 416) {
                        target_bitsize = 416;
                    }
                    if (binary_length <= 304) {
                        target_bitsize = 304;
                    }
                    if (binary_length <= 208) {
                        target_bitsize = 208;
                    }
                    if (binary_length <= 152) {
                        target_bitsize = 152;
                    }
                    if (binary_length <= 112) {
                        target_bitsize = 112;
                    }
                    if (binary_length <= 72) {
                        target_bitsize = 72;
                    }
                    if (binary_length <= 32) {
                        target_bitsize = 32;
                    }
                    break;
                case 4:
                    if (binary_length > 1184) {
                        return false;
                    }
                    if (binary_length <= 1184) {
                        target_bitsize = 1184;
                    }
                    if (binary_length <= 1016) {
                        target_bitsize = 1016;
                    }
                    if (binary_length <= 840) {
                        target_bitsize = 840;
                    }
                    if (binary_length <= 672) {
                        target_bitsize = 672;
                    }
                    if (binary_length <= 496) {
                        target_bitsize = 496;
                    }
                    if (binary_length <= 352) {
                        target_bitsize = 352;
                    }
                    if (binary_length <= 264) {
                        target_bitsize = 264;
                    }
                    if (binary_length <= 208) {
                        target_bitsize = 208;
                    }
                    if (binary_length <= 152) {
                        target_bitsize = 152;
                    }
                    if (binary_length <= 96) {
                        target_bitsize = 96;
                    }
                    if (binary_length <= 56) {
                        target_bitsize = 56;
                    }
                    break;
            }
        }

        if (cc_mode == CompositeMode.CC_C) {
            /* CC-C 2D Component is a bit more complex! */
            int byte_length, codewords_used, ecc_level, ecc_codewords, rows;
            int codewords_total, target_codewords, target_bytesize;

            byte_length = binary_length / 8;
            if (binary_length % 8 != 0) {
                byte_length++;
            }

            codewords_used = (byte_length / 6) * 5;
            codewords_used += byte_length % 6;

            ecc_level = 7;
            if (codewords_used <= 1280) {
                ecc_level = 6;
            }
            if (codewords_used <= 640) {
                ecc_level = 5;
            }
            if (codewords_used <= 320) {
                ecc_level = 4;
            }
            if (codewords_used <= 160) {
                ecc_level = 3;
            }
            if (codewords_used <= 40) {
                ecc_level = 2;
            }
            ecc = ecc_level;
            ecc_codewords = 1;
            for (i = 1; i <= (ecc_level + 1); i++) {
                ecc_codewords *= 2;
            }

            codewords_used += ecc_codewords;
            codewords_used += 3;

            if (linearWidth == 0) {
                /* Linear component not yet calculated */
                cc_width = (int)(0.5 + Math.sqrt((codewords_used) / 3.0));
            } else {
                cc_width = (linearWidth - 53) / 17;
            }

            if ((codewords_used / cc_width) > 90) {
                /* stop the symbol from becoming too high */
                cc_width = cc_width + 1;
            }

            rows = codewords_used / cc_width;
            if (codewords_used % cc_width != 0) {
                rows++;
            }

            while (cc_width > (3 * rows)) {
                /* stop the symbol from becoming too wide (section 10) */
                cc_width--;

                rows = codewords_used / cc_width;
                if (codewords_used % cc_width != 0) {
                    rows++;
                }
            };

            codewords_total = cc_width * rows;

            target_codewords = codewords_total - ecc_codewords;
            target_codewords -= 3;

            target_bytesize = 6 * (target_codewords / 5);
            target_bytesize += target_codewords % 5;

            target_bitsize = 8 * target_bytesize;
        }

        remainder = target_bitsize - binary_length;
        return true;
    }

    private boolean cc_binary_string() {
        /* Handles all data encodation from section 5 of ISO/IEC 24723 */
        int encoding_method, read_posn, d1, d2, value, alpha_pad;
        int i, j, ai_crop, fnc1_latch;
        int group_val;
        int ai90_mode;
        boolean latch;
        int alpha, alphanum, numeric, test1, test2, test3, next_ai_posn;
        int numeric_value, table3_letter;
        String numeric_part;
        String ninety;
        int latchOffset;

        encoding_method = 1;
        read_posn = 0;
        ai_crop = 0;
        fnc1_latch = 0;
        alpha_pad = 0;
        ai90_mode = 0;
        ecc = 0;
        value = 0;
        target_bitsize = 0;

        if (content.charAt(0) == '1' && (content.charAt(1) == '0' || content.charAt(1) == '1' || content.charAt(1) == '7') && content.length() >= 8) {
            /* Source starts (10), (11) or (17) */
            encoding_method = 2;
        }

        if (content.charAt(0) == '9' && content.charAt(1) == '0') {
            /* Source starts (90) */
            encoding_method = 3;
        }

        encodeInfo += "Composite Encodation: ";
        switch (encoding_method) {
            case 1:
                encodeInfo += "0\n";
                break;
            case 2:
                encodeInfo += "10\n";
                break;
            case 3:
                encodeInfo += "11\n";
                break;
        }

        binary_string = "";

        if (encoding_method == 1) {
            binary_string += "0";
        }

        if (encoding_method == 2) {
            /* Encoding Method field "10" - date and lot number */

            binary_string += "10";

            if (content.charAt(1) == '0') {
                /* No date data */
                binary_string += "11";
                read_posn = 2;
            } else {
                /* Production Date (11) or Expiration Date (17) */
                group_val = ((10 * (content.charAt(2) - '0')) + (content.charAt(3) - '0')) * 384;
                group_val += (((10 * (content.charAt(4) - '0')) + (content.charAt(5) - '0')) - 1) * 32;
                group_val += (10 * (content.charAt(6) - '0')) + (content.charAt(7) - '0');

                for (j = 0; j < 16; j++) {
                    if ((group_val & (0x8000 >> j)) == 0) {
                        binary_string += "0";
                    } else {
                        binary_string += "1";
                    }
                }

                if (content.charAt(1) == '1') {
                    /* Production Date AI 11 */
                    binary_string += "0";
                } else {
                    /* Expiration Date AI 17 */
                    binary_string += "1";
                }
                read_posn = 8;
            }

            if((read_posn + 2) < content.length()) {
                if ((content.charAt(read_posn) == '1') && (content.charAt(read_posn + 1) == '0')) {
                    /* Followed by AI 10 - strip this from general field */
                    read_posn += 2;
                } else {
                    /* An FNC1 character needs to be inserted in the general field */
                    fnc1_latch = 1;
                }
            } else {
                fnc1_latch = 1;
            }
        }

        if (encoding_method == 3) {
            /* Encodation Method field of "11" - AI 90 */
            /* "This encodation method may be used if an element string with an AI
             90 occurs at the start of the data message, and if the data field
             following the two-digit AI 90 starts with an alphanumeric string which
             complies with a specific format." (para 5.2.2) */

            j = content.length();
            for (i = content.length(); i > 2; i--) {
                if (content.charAt(i - 1) == '[') {
                    j = i;
                }
            }

            ninety = content.substring(2, j - 1);

//            if (debug) {
//                System.out.println("          AI90 substring: " + ninety);
//            }

            /* Find out if the AI 90 data is alphabetic or numeric or both */

            alpha = 0;
            alphanum = 0;
            numeric = 0;

            for (i = 0; i < ninety.length(); i++) {

                if ((ninety.charAt(i) >= 'A') && (ninety.charAt(i) <= 'Z')) {
                    /* Character is alphabetic */
                    alpha += 1;
                }

                if ((ninety.charAt(i) >= '0') && (ninety.charAt(i) <= '9')) {
                    /* Character is numeric */
                    numeric += 1;
                }

                switch (ninety.charAt(i)) {
                    case '*':
                    case ',':
                    case '-':
                    case '.':
                    case '/':
                        alphanum += 1;
                        break;
                }

                if (!(((ninety.charAt(i) >= '0') && (ninety.charAt(i) <= '9')) || ((ninety.charAt(i) >= 'A') && (ninety.charAt(i) <= 'Z')))) {
                    if ((ninety.charAt(i) != '*') && (ninety.charAt(i) != ',') && (ninety.charAt(i) != '-') && (ninety.charAt(i) != '.') && (ninety.charAt(i) != '/')) {
                        /* An Invalid AI 90 character */
                        throw new OkapiException("Invalid AI 90 data");
                    }
                }
            }

//            if (debug) {
//                System.out.print("          AI90 is ");
//                if (alphanum > 0) {
//                    System.out.println("alphanumeric");
//                } else {
//                    if (alpha > 0) {
//                        System.out.println("alphabetic");
//                    } else {
//                        System.out.println("numeric");
//                    }
//                }
//            }

            /* must start with 0, 1, 2 or 3 digits followed by an uppercase character */
            test1 = -1;
            for (i = 3; i >= 0; i--) {
                if ((ninety.charAt(i) >= 'A') && (ninety.charAt(i) <= 'Z')) {
                    test1 = i;
                }
            }

            test2 = 0;
            for (i = 0; i < test1; i++) {
                if (!((ninety.charAt(i) >= '0') && (ninety.charAt(i) <= '9'))) {
                    test2 = 1;
                }
            }

            /* leading zeros are not permitted */
            test3 = 0;
            if ((test1 >= 1) && (ninety.charAt(0) == '0')) {
                test3 = 1;
            }

            if ((test1 != -1) && (test2 != 1) && (test3 == 0)) {
                /* Encodation method "11" can be used */
//                if (debug) {
//                    System.out.println("          Using encoding method 11");
//                }
                binary_string += "11";

                numeric -= test1;
                alpha--;

                /* Decide on numeric, alpha or alphanumeric mode */
                /* Alpha mode is a special mode for AI 90 */

                if (alphanum > 0) {
                    /* Alphanumeric mode */
                    binary_string += "0";
                    ai90_mode = 1;
                } else {
                    if (alpha > numeric) {
                        /* Alphabetic mode */
                        binary_string += "11";
                        ai90_mode = 2;
                    } else {
                        /* Numeric mode */
                        binary_string += "10";
                        ai90_mode = 3;
                    }
                }

                next_ai_posn = 2 + ninety.length();

                if (content.charAt(next_ai_posn) == '[') {
                    /* There are more AIs afterwords */
                    if ((content.charAt(next_ai_posn + 1) == '2') && (content.charAt(next_ai_posn + 2) == '1')) {
                        /* AI 21 follows */
                        ai_crop = 1;
                    }

                    if ((content.charAt(next_ai_posn + 1) == '8') && (content.charAt(next_ai_posn + 2) == '0') && (content.charAt(next_ai_posn + 3) == '0') && (content.charAt(next_ai_posn + 4) == '4')) {
                        /* AI 8004 follows */
                        ai_crop = 2;
                    }
                }

                switch (ai_crop) {
                    case 0:
                        binary_string += "0";
                        break;
                    case 1:
                        binary_string += "10";
                        break;
                    case 2:
                        binary_string += "11";
                        break;
                }

                if (test1 == 0) {
                    numeric_part = "0";
                } else {
                    numeric_part = ninety.substring(0, test1);
                }

                numeric_value = 0;
                for (i = 0; i < numeric_part.length(); i++) {
                    numeric_value *= 10;
                    numeric_value += numeric_part.charAt(i) - '0';
                }

                table3_letter = -1;
                if (numeric_value < 31) {
                    switch (ninety.charAt(test1)) {
                        case 'B':
                            table3_letter = 0;
                            break;
                        case 'D':
                            table3_letter = 1;
                            break;
                        case 'H':
                            table3_letter = 2;
                            break;
                        case 'I':
                            table3_letter = 3;
                            break;
                        case 'J':
                            table3_letter = 4;
                            break;
                        case 'K':
                            table3_letter = 5;
                            break;
                        case 'L':
                            table3_letter = 6;
                            break;
                        case 'N':
                            table3_letter = 7;
                            break;
                        case 'P':
                            table3_letter = 8;
                            break;
                        case 'Q':
                            table3_letter = 9;
                            break;
                        case 'R':
                            table3_letter = 10;
                            break;
                        case 'S':
                            table3_letter = 11;
                            break;
                        case 'T':
                            table3_letter = 12;
                            break;
                        case 'V':
                            table3_letter = 13;
                            break;
                        case 'W':
                            table3_letter = 14;
                            break;
                        case 'Z':
                            table3_letter = 15;
                            break;
                    }
                }

                if (table3_letter != -1) {
                    /* Encoding can be done according to 5.2.2 c) 2) */
                    /* five bit binary string representing value before letter */
                    for (j = 0; j < 5; j++) {
                        if ((numeric_value & (0x10 >> j)) == 0x00) {
                            binary_string += "0";
                        } else {
                            binary_string += "1";
                        }
                    }

                    /* followed by four bit representation of letter from Table 3 */
                    for (j = 0; j < 4; j++) {
                        if ((table3_letter & (0x08 >> j)) == 0x00) {
                            binary_string += "0";
                        } else {
                            binary_string += "1";
                        }
                    }
                } else {
                    /* Encoding is done according to 5.2.2 c) 3) */
                    binary_string += "11111";
                    /* ten bit representation of number */
                    for (j = 0; j < 10; j++) {
                        if ((numeric_value & (0x200 >> j)) == 0x00) {
                            binary_string += "0";
                        } else {
                            binary_string += "1";
                        }
                    }

                    /* five bit representation of ASCII character */
                    for (j = 0; j < 5; j++) {
                        if (((ninety.charAt(test1) - 65) & (0x10 >> j)) == 0x00) {
                            binary_string += "0";
                        } else {
                            binary_string += "1";
                        }
                    }
                }

                read_posn = test1 + 3;
            } else {
//                if (debug) {
//                    System.out.println("          AI90 compression rejected, use general field");
//                }
                /* Use general field encodation instead */
                binary_string += "0";
                read_posn = 0;
            }


            /* Now encode the rest of the AI 90 data field */
            if (ai90_mode == 2) {
                /* Alpha encodation (section 5.2.3) */
                do {
                    if ((content.charAt(read_posn) >= '0') && (content.charAt(read_posn) <= '9')) {
                        for (j = 0; j < 5; j++) {
                            if (((content.charAt(read_posn) + 4) & (0x10 >> j)) == 0x00) {
                                binary_string += "0";
                            } else {
                                binary_string += "1";
                            }
                        }
                    }

                    if ((content.charAt(read_posn) >= 'A') && (content.charAt(read_posn) <= 'Z')) {
                        for (j = 0; j < 6; j++) {
                            if (((content.charAt(read_posn) - 65) & (0x20 >> j)) == 0x00) {
                                binary_string += "0";
                            } else {
                                binary_string += "1";
                            }
                        }
                    }

                    if (content.charAt(read_posn) == '[') {
                        binary_string += "11111";
                    }

                    read_posn++;
                } while ((content.charAt(read_posn - 1) != '[') && (read_posn < content.length()));
                alpha_pad = 1; /* This is overwritten if a general field is encoded */
            }

            if (ai90_mode == 1) {
                /* Alphanumeric mode */
                do {
                    if ((content.charAt(read_posn) >= '0') && (content.charAt(read_posn) <= '9')) {
                        for (j = 0; j < 5; j++) {
                            if (((content.charAt(read_posn) - 43) & (0x10 >> j)) == 0x00) {
                                binary_string += "0";
                            } else {
                                binary_string += "1";
                            }
                        }
                    }

                    if ((content.charAt(read_posn) >= 'A') && (content.charAt(read_posn) <= 'Z')) {
                        for (j = 0; j < 6; j++) {
                            if (((content.charAt(read_posn) - 33) & (0x20 >> j)) == 0x00) {
                                binary_string += "0";
                            } else {
                                binary_string += "1";
                            }
                        }
                    }

                    switch (content.charAt(read_posn)) {
                        case '[':
                            binary_string += "01111";
                            break;
                        case '*':
                            binary_string += "111010";
                            break;
                        case ',':
                            binary_string += "111011";
                            break;
                        case '-':
                            binary_string += "111100";
                            break;
                        case '.':
                            binary_string += "111101";
                            break;
                        case '/':
                            binary_string += "111110";
                            break;
                    }

                    read_posn++;
                } while ((content.charAt(read_posn - 1) != '[') && (content.charAt(read_posn - 1) != '\0'));
            }

            read_posn += (2 * ai_crop);
        }

//        if (debug) {
//            System.out.println("     Compressed binary: " + binary_string);
//        }

        /* The compressed data field has been processed if appropriate - the
         rest of the data (if any) goes into a general-purpose data compaction field */

        j = 0;
        general_field = "";
        if (fnc1_latch == 1) {
            /* Encodation method "10" has been used but it is not followed by
             AI 10, so a FNC1 character needs to be added */
            general_field += "[";
        }

        general_field += content.substring(read_posn);

//        if (debug) {
//            System.out.println("     General field: " + general_field);
//        }

        latch = false;
        if (general_field.length() != 0) {
            alpha_pad = 0;


            general_field_type = new GeneralFieldMode[general_field.length()];

            for (i = 0; i < general_field.length(); i++) {
                /* Table 13 - ISO/IEC 646 encodation */
                if ((general_field.charAt(i) < ' ') || (general_field.charAt(i) > 'z')) {
                    general_field_type[i] = GeneralFieldMode.INVALID_CHAR;
                    latch = true;
                } else {
                    general_field_type[i] = GeneralFieldMode.ISOIEC;
                }

                if (general_field.charAt(i) == '#') {
                    general_field_type[i] = GeneralFieldMode.INVALID_CHAR;
                    latch = true;
                }
                if (general_field.charAt(i) == '$') {
                    general_field_type[i] = GeneralFieldMode.INVALID_CHAR;
                    latch = true;
                }
                if (general_field.charAt(i) == '@') {
                    general_field_type[i] = GeneralFieldMode.INVALID_CHAR;
                    latch = true;
                }
                if (general_field.charAt(i) == 92) {
                    general_field_type[i] = GeneralFieldMode.INVALID_CHAR;
                    latch = true;
                }
                if (general_field.charAt(i) == '^') {
                    general_field_type[i] = GeneralFieldMode.INVALID_CHAR;
                    latch = true;
                }
                if (general_field.charAt(i) == 96) {
                    general_field_type[i] = GeneralFieldMode.INVALID_CHAR;
                    latch = true;
                }

                /* Table 12 - Alphanumeric encodation */
                if ((general_field.charAt(i) >= 'A') && (general_field.charAt(i) <= 'Z')) {
                    general_field_type[i] = GeneralFieldMode.ALPHA_OR_ISO;
                }
                if (general_field.charAt(i) == '*') {
                    general_field_type[i] = GeneralFieldMode.ALPHA_OR_ISO;
                }
                if (general_field.charAt(i) == ',') {
                    general_field_type[i] = GeneralFieldMode.ALPHA_OR_ISO;
                }
                if (general_field.charAt(i) == '-') {
                    general_field_type[i] = GeneralFieldMode.ALPHA_OR_ISO;
                }
                if (general_field.charAt(i) == '.') {
                    general_field_type[i] = GeneralFieldMode.ALPHA_OR_ISO;
                }
                if (general_field.charAt(i) == '/') {
                    general_field_type[i] = GeneralFieldMode.ALPHA_OR_ISO;
                }

                /* Numeric encodation */
                if ((general_field.charAt(i) >= '0') && (general_field.charAt(i) <= '9')) {
                    general_field_type[i] = GeneralFieldMode.ANY_ENC;
                }
                if (general_field.charAt(i) == '[') {
                    /* FNC1 can be encoded in any system */
                    general_field_type[i] = GeneralFieldMode.ANY_ENC;
                }

            }

            if (latch) {
                /* Invalid characters in input data */
                throw new OkapiException("Invalid characters in input data");
            }

            for (i = 0; i < general_field.length() - 1; i++) {
                if ((general_field_type[i] == GeneralFieldMode.ISOIEC) && (general_field.charAt(i + 1) == '[')) {
                    general_field_type[i + 1] = GeneralFieldMode.ISOIEC;
                }
            }

            for (i = 0; i < general_field.length() - 1; i++) {
                if ((general_field_type[i] == GeneralFieldMode.ALPHA_OR_ISO) && (general_field.charAt(i + 1) == '[')) {
                    general_field_type[i + 1] = GeneralFieldMode.ALPHA_OR_ISO;
                }
            }

            latch = applyGeneralFieldRules();

            i = 0;
            do {
                switch (general_field_type[i]) {
                    case NUMERIC:
                        if (i != 0) {
                            if ((general_field_type[i - 1] != GeneralFieldMode.NUMERIC) && (general_field.charAt(i - 1) != '[')) {
                                binary_string += "000"; /* Numeric latch */
                            }
                        }

                        if (general_field.charAt(i) != '[') {
                            d1 = general_field.charAt(i) - '0';
                        } else {
                            d1 = 10;
                        }

                        if (i < general_field.length() - 1) {
                            if (general_field.charAt(i + 1) != '[') {
                                d2 = general_field.charAt(i + 1) - '0';
                            } else {
                                d2 = 10;
                            }
                        } else {
                            d2 = 10;
                        }

                        if ((d1 != 10) || (d2 != 10)) {
                            /* If (d1==10)&&(d2==10) then input is either FNC1,FNC1 or FNC1,EOL */
                            value = (11 * d1) + d2 + 8;

                            for (j = 0; j < 7; j++) {
                                if ((value & 0x40 >> j) == 0x00) {
                                    binary_string += "0";
                                } else {
                                    binary_string += "1";
                                }
                            }

                            i += 2;
                        }
                        break;

                    case ALPHA:
                        if (i != 0) {
                            if ((general_field_type[i - 1] == GeneralFieldMode.NUMERIC) || (general_field.charAt(i - 1) == '[')) {
                                binary_string += "0000"; /* Alphanumeric latch */
                            }
                            if (general_field_type[i - 1] == GeneralFieldMode.ISOIEC) {
                                binary_string += "00100"; /* ISO/IEC 646 latch */
                            }
                        }

                        if ((general_field.charAt(i) >= '0') && (general_field.charAt(i) <= '9')) {

                            value = general_field.charAt(i) - 43;

                            for (j = 0; j < 5; j++) {
                                if ((value & (0x10 >> j)) == 0x00) {
                                    binary_string += "0";
                                } else {
                                    binary_string += "1";
                                }
                            }
                        }

                        if ((general_field.charAt(i) >= 'A') && (general_field.charAt(i) <= 'Z')) {

                            value = general_field.charAt(i) - 33;

                            for (j = 0; j < 6; j++) {
                                if ((value & (0x20 >> j)) == 0x00) {
                                    binary_string += "0";
                                } else {
                                    binary_string += "1";
                                }
                            }
                        }

                        if (general_field.charAt(i) == '[') {
                            binary_string += "01111"; /* FNC1/Numeric latch */
                        }
                        if (general_field.charAt(i) == '*') {
                            binary_string += "111010"; /* asterisk */
                        }
                        if (general_field.charAt(i) == ',') {
                            binary_string += "111011"; /* comma */
                        }
                        if (general_field.charAt(i) == '-') {
                            binary_string += "111100"; /* minus or hyphen */
                        }
                        if (general_field.charAt(i) == '.') {
                            binary_string += "111101"; /* period or full stop */
                        }
                        if (general_field.charAt(i) == '/') {
                            binary_string += "111110"; /* slash or solidus */
                        }

                        i++;
                        break;

                    case ISOIEC:
                        if (i != 0) {
                            if ((general_field_type[i - 1] == GeneralFieldMode.NUMERIC) || (general_field.charAt(i - 1) == '[')) {
                                binary_string += "0000"; /* Alphanumeric latch */
                                binary_string += "00100"; /* ISO/IEC 646 latch */
                            }
                            if (general_field_type[i - 1] == GeneralFieldMode.ALPHA) {
                                binary_string += "00100"; /* ISO/IEC 646 latch */
                            }
                        }

                        if ((general_field.charAt(i) >= '0') && (general_field.charAt(i) <= '9')) {

                            value = general_field.charAt(i) - 43;

                            for (j = 0; j < 5; j++) {
                                if ((value & (0x10 >> j)) == 0x00) {
                                    binary_string += "0";
                                } else {
                                    binary_string += "1";
                                }
                            }
                        }

                        if ((general_field.charAt(i) >= 'A') && (general_field.charAt(i) <= 'Z')) {

                            value = general_field.charAt(i) - 1;

                            for (j = 0; j < 7; j++) {
                                if ((value & (0x40 >> j)) == 0x00) {
                                    binary_string += "0";
                                } else {
                                    binary_string += "1";
                                }
                            }
                        }

                        if ((general_field.charAt(i) >= 'a') && (general_field.charAt(i) <= 'z')) {

                            value = general_field.charAt(i) - 7;

                            for (j = 0; j < 7; j++) {
                                if ((value & (0x40 >> j)) == 0x00) {
                                    binary_string += "0";
                                } else {
                                    binary_string += "1";
                                }
                            }
                        }

                        if (general_field.charAt(i) == '[') {
                            binary_string += "01111"; /* FNC1/Numeric latch */
                        }
                        if (general_field.charAt(i) == '!') {
                            binary_string += "11101000"; /* exclamation mark */
                        }
                        if (general_field.charAt(i) == 34) {
                            binary_string += "11101001"; /* quotation mark */
                        }
                        if (general_field.charAt(i) == 37) {
                            binary_string += "11101010"; /* percent sign */
                        }
                        if (general_field.charAt(i) == '&') {
                            binary_string += "11101011"; /* ampersand */
                        }
                        if (general_field.charAt(i) == 39) {
                            binary_string += "11101100"; /* apostrophe */
                        }
                        if (general_field.charAt(i) == '(') {
                            binary_string += "11101101"; /* left parenthesis */
                        }
                        if (general_field.charAt(i) == ')') {
                            binary_string += "11101110"; /* right parenthesis */
                        }
                        if (general_field.charAt(i) == '*') {
                            binary_string += "11101111"; /* asterisk */
                        }
                        if (general_field.charAt(i) == '+') {
                            binary_string += "11110000"; /* plus sign */
                        }
                        if (general_field.charAt(i) == ',') {
                            binary_string += "11110001"; /* comma */
                        }
                        if (general_field.charAt(i) == '-') {
                            binary_string += "11110010"; /* minus or hyphen */
                        }
                        if (general_field.charAt(i) == '.') {
                            binary_string += "11110011"; /* period or full stop */
                        }
                        if (general_field.charAt(i) == '/') {
                            binary_string += "11110100"; /* slash or solidus */
                        }
                        if (general_field.charAt(i) == ':') {
                            binary_string += "11110101"; /* colon */
                        }
                        if (general_field.charAt(i) == ';') {
                            binary_string += "11110110"; /* semicolon */
                        }
                        if (general_field.charAt(i) == '<') {
                            binary_string += "11110111"; /* less-than sign */
                        }
                        if (general_field.charAt(i) == '=') {
                            binary_string += "11111000"; /* equals sign */
                        }
                        if (general_field.charAt(i) == '>') {
                            binary_string += "11111001"; /* greater-than sign */
                        }
                        if (general_field.charAt(i) == '?') {
                            binary_string += "11111010"; /* question mark */
                        }
                        if (general_field.charAt(i) == '_') {
                            binary_string += "11111011"; /* underline or low line */
                        }
                        if (general_field.charAt(i) == ' ') {
                            binary_string += "11111100"; /* space */
                        }

                        i++;
                        break;
                }


                latchOffset = 0;
                if (latch) {
                    latchOffset = 1;
                }
            } while ((i + latchOffset) < general_field.length());
        }

        if (!calculateSymbolSize()) {
            return false;
        }

        if (latch) {
            i = general_field.length() - 1;
            /* There is still one more numeric digit to encode */

            if (general_field.charAt(i) == '[') {
                binary_string += "000001111";
            } else {
                if ((remainder >= 4) && (remainder <= 6)) {
                    d1 = general_field.charAt(i) - '0';
                    d1++;

                    for (j = 0; j < 4; j++) {
                        if ((value & (0x08 >> j)) == 0x00) {
                            binary_string += "0";
                        } else {
                            binary_string += "1";
                        }
                    }
                } else {
                    d1 = general_field.charAt(i) - '0';
                    d2 = 10;

                    value = (11 * d1) + d2 + 8;

                    for (j = 0; j < 7; j++) {
                        if ((value & (0x40 >> j)) == 0x00) {
                            binary_string += "0";
                        } else {
                            binary_string += "1";
                        }
                    }
                    /* This may push the symbol up to the next size */
                }
            }
        }

        if (binary_string.length() > 11805) { /* (2361 * 5) */
            throw new OkapiException("Input too long");
        }

        /* size of the symbol may have changed when adding data in the above sequence */
        if (!calculateSymbolSize()) {
            return false;
        }

        encodeInfo += "Composite Binary Length: " + Integer.toString(binary_string.length()) + "\n";
        displayBinaryString();

        if (binary_string.length() < target_bitsize) {
            /* Now add padding to binary string */
            if (alpha_pad == 1) {
                binary_string += "11111";
                alpha_pad = 0;
                /* Extra FNC1 character required after Alpha encodation (section 5.2.3) */
            }

            if ((general_field.length() != 0) && (general_field_type[general_field.length() - 1] == GeneralFieldMode.NUMERIC)) {
                binary_string += "0000";
            }

            while (binary_string.length() < target_bitsize) {
                binary_string += "00100";
            }

            binary_string = binary_string.substring(0, target_bitsize);
        }

        return true;
    }

    private void displayBinaryString() {
        int i, nibble;
        /* Display binary string as hexadecimal */

        encodeInfo += "Composite Binary String: ";
        nibble = 0;
        for(i = 0; i < binary_string.length(); i++) {
            switch (i % 4) {
                case 0:
                    if (binary_string.charAt(i) == '1') {
                        nibble += 8;
                    }
                    break;
                case 1:
                    if (binary_string.charAt(i) == '1') {
                        nibble += 4;
                    }
                    break;
                case 2:
                    if (binary_string.charAt(i) == '1') {
                        nibble += 2;
                    }
                    break;
                case 3:
                    if (binary_string.charAt(i) == '1') {
                        nibble += 1;
                    }
                    encodeInfo += Integer.toHexString(nibble);
                    nibble = 0;
                    break;
            }
        }

        if ((binary_string.length() % 4) != 0) {
            encodeInfo += Integer.toHexString(nibble);
        }
        encodeInfo += "\n";
    }

    private boolean applyGeneralFieldRules() {
        /* Attempts to apply encoding rules from secions 7.2.5.5.1 to 7.2.5.5.3
         of ISO/IEC 24724:2006 */

        int block_count, i, j, k;
        GeneralFieldMode current, next, last;
        int[] blockLength = new int[200];
        GeneralFieldMode[] blockType = new GeneralFieldMode[200];

        block_count = 0;

        blockLength[block_count] = 1;
        blockType[block_count] = general_field_type[0];

        for (i = 1; i < general_field.length(); i++) {
            current = general_field_type[i];
            last = general_field_type[i - 1];

            if (current == last) {
                blockLength[block_count] = blockLength[block_count] + 1;
            } else {
                block_count++;
                blockLength[block_count] = 1;
                blockType[block_count] = general_field_type[i];
            }
        }

        block_count++;

        for (i = 0; i < block_count; i++) {
            current = blockType[i];
            next = blockType[i + 1];

            if ((current == GeneralFieldMode.ISOIEC) && (i != (block_count - 1))) {
                if ((next == GeneralFieldMode.ANY_ENC) && (blockLength[i + 1] >= 4)) {
                    blockType[i + 1] = GeneralFieldMode.NUMERIC;
                }
                if ((next == GeneralFieldMode.ANY_ENC) && (blockLength[i + 1] < 4)) {
                    blockType[i + 1] = GeneralFieldMode.ISOIEC;
                }
                if ((next == GeneralFieldMode.ALPHA_OR_ISO) && (blockLength[i + 1] >= 5)) {
                    blockType[i + 1] = GeneralFieldMode.ALPHA;
                }
                if ((next == GeneralFieldMode.ALPHA_OR_ISO) && (blockLength[i + 1] < 5)) {
                    blockType[i + 1] = GeneralFieldMode.ISOIEC;
                }
            }

            if (current == GeneralFieldMode.ALPHA_OR_ISO) {
                blockType[i] = GeneralFieldMode.ALPHA;
            }

            if ((current == GeneralFieldMode.ALPHA) && (i != (block_count - 1))) {
                if ((next == GeneralFieldMode.ANY_ENC) && (blockLength[i + 1] >= 6)) {
                    blockType[i + 1] = GeneralFieldMode.NUMERIC;
                }
                if ((next == GeneralFieldMode.ANY_ENC) && (blockLength[i + 1] < 6)) {
                    if ((i == block_count - 2) && (blockLength[i + 1] >= 4)) {
                        blockType[i + 1] = GeneralFieldMode.NUMERIC;
                    } else {
                        blockType[i + 1] = GeneralFieldMode.ALPHA;
                    }
                }
            }

            if (current == GeneralFieldMode.ANY_ENC) {
                blockType[i] = GeneralFieldMode.NUMERIC;
            }
        }

        if (block_count > 1) {
            i = 1;
            while (i < block_count) {
                if (blockType[i - 1] == blockType[i]) {
                    /* bring together */
                    blockLength[i - 1] = blockLength[i - 1] + blockLength[i];
                    j = i + 1;

                    /* decreace the list */
                    while (j < block_count) {
                        blockLength[j - 1] = blockLength[j];
                        blockType[j - 1] = blockType[j];
                        j++;
                    }
                    block_count--;
                    i--;
                }
                i++;
            }
        }

        for (i = 0; i < block_count - 1; i++) {
            if ((blockType[i] == GeneralFieldMode.NUMERIC) && ((blockLength[i] & 1) != 0)) {
                /* Odd size numeric block */
                blockLength[i] = blockLength[i] - 1;
                blockLength[i + 1] = blockLength[i + 1] + 1;
            }
        }

        j = 0;
        for (i = 0; i < block_count; i++) {
            for (k = 0; k < blockLength[i]; k++) {
                general_field_type[j] = blockType[i];
                j++;
            }
        }

        if ((blockType[block_count - 1] == GeneralFieldMode.NUMERIC) && ((blockLength[block_count - 1] & 1) != 0)) {
            /* If the last block is numeric and an odd size, further
             processing needs to be done outside this procedure */
            return true;
        } else {
            return false;
        }
    }

    private void cc_a() {
        /* CC-A 2D component */
        int i, strpos, segment, cwCnt, variant, rows;
        int k, offset, j, total;
        int[] rsCodeWords = new int[8];
        int LeftRAPStart, RightRAPStart, CentreRAPStart, StartCluster;
        int LeftRAP, RightRAP, CentreRAP, Cluster;
        int[] dummy = new int[5];
        int flip, loop;
        String codebarre;
        String bin;
        String local_source; /* A copy of source but with padding zeroes to make 208 bits */

        variant = 0;

        for (i = 0; i < 13; i++) {
            bitStr[i] = 0;
        }
        for (i = 0; i < 28; i++) {
            codeWords[i] = 0;
        }

        local_source = binary_string;
        for (i = binary_string.length(); i < 208; i++) {
            local_source += "0";
        }

        for (segment = 0; segment < 13; segment++) {
            strpos = segment * 16;
            bitStr[segment] = 0;
            for (i = 0; i < 16; i++) {
                if (local_source.charAt(strpos + i) == '1') {
                    bitStr[segment] += 0x8000 >> i;
                }
            }
        }

        init928();
        /* encode codeWords from bitStr */
        cwCnt = encode928(binary_string.length());

        switch (cc_width) {
            case 2:
                switch (cwCnt) {
                    case 6:
                        variant = 0;
                        break;
                    case 8:
                        variant = 1;
                        break;
                    case 9:
                        variant = 2;
                        break;
                    case 11:
                        variant = 3;
                        break;
                    case 12:
                        variant = 4;
                        break;
                    case 14:
                        variant = 5;
                        break;
                    case 17:
                        variant = 6;
                        break;
                }
                break;
            case 3:
                switch (cwCnt) {
                    case 8:
                        variant = 7;
                        break;
                    case 10:
                        variant = 8;
                        break;
                    case 12:
                        variant = 9;
                        break;
                    case 14:
                        variant = 10;
                        break;
                    case 17:
                        variant = 11;
                        break;
                }
                break;
            case 4:
                switch (cwCnt) {
                    case 8:
                        variant = 12;
                        break;
                    case 11:
                        variant = 13;
                        break;
                    case 14:
                        variant = 14;
                        break;
                    case 17:
                        variant = 15;
                        break;
                    case 20:
                        variant = 16;
                        break;
                }
                break;
        }

        rows = CCA_VARIANTS[variant];
        k = CCA_VARIANTS[17 + variant];
        offset = CCA_VARIANTS[34 + variant];

        /* Reed-Solomon error correction */

        for (i = 0; i < 8; i++) {
            rsCodeWords[i] = 0;
        }
        total = 0;
        encodeInfo += "Composite Codewords: ";
        for (i = 0; i < cwCnt; i++) {
            total = (codeWords[i] + rsCodeWords[k - 1]) % 929;
            for (j = k - 1; j >= 0; j--) {
                if (j == 0) {
                    rsCodeWords[j] = (929 - (total * CCA_COEFFS[offset + j]) % 929) % 929;
                } else {
                    rsCodeWords[j] = (rsCodeWords[j - 1] + 929 - (total * CCA_COEFFS[offset + j]) % 929) % 929;
                }
            }
            encodeInfo += Integer.toString(codeWords[i]) + " ";
        }
        encodeInfo += "\n";

        for (j = 0; j < k; j++) {
            if (rsCodeWords[j] != 0) {
                rsCodeWords[j] = 929 - rsCodeWords[j];
            }
        }

        for (i = k - 1; i >= 0; i--) {
            codeWords[cwCnt] = rsCodeWords[i];
            cwCnt++;
        }

        /* Place data into table */
        LeftRAPStart = A_RAP_TABLE[variant];
        CentreRAPStart = A_RAP_TABLE[variant + 17];
        RightRAPStart = A_RAP_TABLE[variant + 34];
        StartCluster = A_RAP_TABLE[variant + 51] / 3;

        LeftRAP = LeftRAPStart;
        CentreRAP = CentreRAPStart;
        RightRAP = RightRAPStart;
        Cluster = StartCluster; /* Cluster can be 0, 1 or 2 for Cluster(0), Cluster(3) and Cluster(6) */

        readable = "";
        row_count = rows;
        pattern = new String[row_count];
        row_height = new int[row_count];

        for (i = 0; i < rows; i++) {
            codebarre = "";
            offset = 929 * Cluster;
            for (j = 0; j < 5; j++) {
                dummy[j] = 0;
            }
            for (j = 0; j < cc_width; j++) {
                dummy[j + 1] = codeWords[i * cc_width + j];
            }
            /* Copy the data into codebarre */
            codebarre += RAPLR[LeftRAP];
            codebarre += "1";
            codebarre += CODAGEMC[offset + dummy[1]];
            codebarre += "1";
            if (cc_width == 3) {
                codebarre += RAPC[CentreRAP];
            }
            if (cc_width >= 2) {
                codebarre += "1";
                codebarre += CODAGEMC[offset + dummy[2]];
                codebarre += "1";
            }
            if (cc_width == 4) {
                codebarre += RAPC[CentreRAP];
            }
            if (cc_width >= 3) {
                codebarre += "1";
                codebarre += CODAGEMC[offset + dummy[3]];
                codebarre += "1";
            }
            if (cc_width == 4) {
                codebarre += "1";
                codebarre += CODAGEMC[offset + dummy[4]];
                codebarre += "1";
            }
            codebarre += RAPLR[RightRAP];
            codebarre += "1"; /* stop */

            /* Now codebarre is a mixture of letters and numbers */

            flip = 1;
            bin = "";
            for (loop = 0; loop < codebarre.length(); loop++) {
                if ((codebarre.charAt(loop) >= '0') && (codebarre.charAt(loop) <= '9')) {
                    for (k = 0; k < codebarre.charAt(loop) - '0'; k++) {
                        if (flip == 0) {
                            bin += '0';
                        } else {
                            bin += '1';
                        }
                    }
                    if (flip == 0) {
                        flip = 1;
                    } else {
                        flip = 0;
                    }
                } else {
                    bin += PDF_TTF[positionOf(codebarre.charAt(loop), BR_SET)];
                }
            }

            row_height[i] = 2;
            pattern[i] = bin2pat(bin);

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

    /* initialize pwr928 encoding table */
    private void init928() {
        int i, j, v;
        int[] cw = new int[7];
        cw[6] = 1;
        for (i = 5; i >= 0; i--) {
            cw[i] = 0;
        }

        for (i = 0; i < 7; i++) {
            pwr928[0][i] = cw[i];
        }
        for (j = 1; j < 69; j++) {
            for (v = 0, i = 6; i >= 1; i--) {
                v = (2 * cw[i]) + (v / 928);
                pwr928[j][i] = cw[i] = v % 928;
            }
            pwr928[j][0] = cw[0] = (2 * cw[0]) + (v / 928);
        }
    }

    /* converts bit string to base 928 values, codeWords[0] is highest order */
    private int encode928(int bitLng) {
        int i, j, b, bitCnt, cwNdx, cwCnt, cwLng;
        for (cwNdx = cwLng = b = 0; b < bitLng; b += 69, cwNdx += 7) {
            bitCnt = min(bitLng - b, 69);
            cwLng += cwCnt = bitCnt / 10 + 1;
            for (i = 0; i < cwCnt; i++) {
                codeWords[cwNdx + i] = 0; /* init 0 */
            }
            for (i = 0; i < bitCnt; i++) {
                if (getBit(b + bitCnt - i - 1)) {
                    for (j = 0; j < cwCnt; j++) {
                        codeWords[cwNdx + j] += pwr928[i][j + 7 - cwCnt];
                    }
                }
            }
            for (i = cwCnt - 1; i > 0; i--) {
                /* add "carries" */
                codeWords[cwNdx + i - 1] += codeWords[cwNdx + i] / 928L;
                codeWords[cwNdx + i] %= 928L;
            }
        }
        return (cwLng);
    }

    private int min(int first, int second) {
        if (first <= second) {
            return first;
        } else {
            return second;
        }
    }

    /* gets bit in bitString at bitPos */
    private boolean getBit(int arg) {
        if ((bitStr[arg >> 4] & (0x8000 >> (arg & 15))) != 0) {
            return true;
        } else {
            return false;
        }
    }

    private void cc_b() {
        /* CC-B 2D component */
        int length, i, binloc;
        int k, j, longueur, offset;
        int[] mccorrection = new int[50];
        int total;
        int[] dummy = new int[5];
        String codebarre;
        String bin;
        int variant, LeftRAPStart, CentreRAPStart, RightRAPStart, StartCluster;
        int LeftRAP, CentreRAP, RightRAP, Cluster, flip, loop;
        int option_2, rows;
        inputData = new int[(binary_string.length() / 8) + 3];

        length = binary_string.length() / 8;

        for (i = 0; i < length; i++) {
            binloc = i * 8;

            inputData[i] = 0;
            for (j = 0; j < 8; j++) {
                if (binary_string.charAt(binloc + j) == '1') {
                    inputData[i] += 0x80 >> j;
                }
            }
        }

        codeWordCount = 0;

        /* "the CC-B component shall have codeword 920 in the first symbol character position" (section 9a) */
        codeWords[codeWordCount] = 920;
        codeWordCount++;

        byteprocess(0, length);

        /* Now figure out which variant of the symbol to use and load values accordingly */

        variant = 0;

        if (cc_width == 2) {
            variant = 13;
            if (codeWordCount <= 33) {
                variant = 12;
            }
            if (codeWordCount <= 29) {
                variant = 11;
            }
            if (codeWordCount <= 24) {
                variant = 10;
            }
            if (codeWordCount <= 19) {
                variant = 9;
            }
            if (codeWordCount <= 13) {
                variant = 8;
            }
            if (codeWordCount <= 8) {
                variant = 7;
            }
        }

        if (cc_width == 3) {
            variant = 23;
            if (codeWordCount <= 70) {
                variant = 22;
            }
            if (codeWordCount <= 58) {
                variant = 21;
            }
            if (codeWordCount <= 46) {
                variant = 20;
            }
            if (codeWordCount <= 34) {
                variant = 19;
            }
            if (codeWordCount <= 24) {
                variant = 18;
            }
            if (codeWordCount <= 18) {
                variant = 17;
            }
            if (codeWordCount <= 14) {
                variant = 16;
            }
            if (codeWordCount <= 10) {
                variant = 15;
            }
            if (codeWordCount <= 6) {
                variant = 14;
            }
        }

        if (cc_width == 4) {
            variant = 34;
            if (codeWordCount <= 108) {
                variant = 33;
            }
            if (codeWordCount <= 90) {
                variant = 32;
            }
            if (codeWordCount <= 72) {
                variant = 31;
            }
            if (codeWordCount <= 54) {
                variant = 30;
            }
            if (codeWordCount <= 39) {
                variant = 29;
            }
            if (codeWordCount <= 30) {
                variant = 28;
            }
            if (codeWordCount <= 24) {
                variant = 27;
            }
            if (codeWordCount <= 18) {
                variant = 26;
            }
            if (codeWordCount <= 12) {
                variant = 25;
            }
            if (codeWordCount <= 8) {
                variant = 24;
            }
        }

        /* Now we have the variant we can load the data - from here on the same as MicroPDF417 code */
        variant--;
        option_2 = MICRO_VARIANTS[variant]; /* columns */
        rows = MICRO_VARIANTS[variant + 34]; /* rows */
        k = MICRO_VARIANTS[variant + 68]; /* number of EC CWs */
        longueur = (option_2 * rows) - k; /* number of non-EC CWs */
        i = longueur - codeWordCount; /* amount of padding required */
        offset = MICRO_VARIANTS[variant + 102]; /* coefficient offset */

        /* We add the padding */
        while (i > 0) {
            codeWords[codeWordCount] = 900;
            codeWordCount++;
            i--;
        }

        /* Reed-Solomon error correction */
        longueur = codeWordCount;
        for (loop = 0; loop < 50; loop++) {
            mccorrection[loop] = 0;
        }
        total = 0;
        encodeInfo += "Composite Codewords: ";
        for (i = 0; i < longueur; i++) {
            total = (codeWords[i] + mccorrection[k - 1]) % 929;
            for (j = k - 1; j >= 0; j--) {
                if (j == 0) {
                    mccorrection[j] = (929 - (total * MICROCOEFFS[offset + j]) % 929) % 929;
                } else {
                    mccorrection[j] = (mccorrection[j - 1] + 929 - (total * MICROCOEFFS[offset + j]) % 929) % 929;
                }
            }
            encodeInfo += Integer.toString(codeWords[i]) + " ";
        }
        encodeInfo += "\n";

        for (j = 0; j < k; j++) {
            if (mccorrection[j] != 0) {
                mccorrection[j] = 929 - mccorrection[j];
            }
        }
        /* we add these codes to the string */
        for (i = k - 1; i >= 0; i--) {
            codeWords[codeWordCount] = mccorrection[i];
            codeWordCount++;
        }

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
        row_count = rows;
        pattern = new String[row_count];
        row_height = new int[row_count];

        for (i = 0; i < rows; i++) {
            codebarre = "";
            offset = 929 * Cluster;
            for (j = 0; j < 5; j++) {
                dummy[j] = 0;
            }
            for (j = 0; j < option_2; j++) {
                dummy[j + 1] = codeWords[i * option_2 + j];
            }
            /* Copy the data into codebarre */
            codebarre += RAPLR[LeftRAP];
            codebarre += "1";
            codebarre += CODAGEMC[offset + dummy[1]];
            codebarre += "1";
            if (cc_width == 3) {
                codebarre += RAPC[CentreRAP];
            }
            if (cc_width >= 2) {
                codebarre += "1";
                codebarre += CODAGEMC[offset + dummy[2]];
                codebarre += "1";
            }
            if (cc_width == 4) {
                codebarre += RAPC[CentreRAP];
            }
            if (cc_width >= 3) {
                codebarre += "1";
                codebarre += CODAGEMC[offset + dummy[3]];
                codebarre += "1";
            }
            if (cc_width == 4) {
                codebarre += "1";
                codebarre += CODAGEMC[offset + dummy[4]];
                codebarre += "1";
            }
            codebarre += RAPLR[RightRAP];
            codebarre += "1"; /* stop */

            /* Now codebarre is a mixture of letters and numbers */

            flip = 1;
            bin = "";
            for (loop = 0; loop < codebarre.length(); loop++) {
                if ((codebarre.charAt(loop) >= '0') && (codebarre.charAt(loop) <= '9')) {
                    for (k = 0; k < codebarre.charAt(loop) - '0'; k++) {
                        if (flip == 0) {
                            bin += '0';
                        } else {
                            bin += '1';
                        }
                    }
                    if (flip == 0) {
                        flip = 1;
                    } else {
                        flip = 0;
                    }
                } else {
                    bin += PDF_TTF[positionOf(codebarre.charAt(loop), BR_SET)];
                }
            }

            pattern[i] = bin2pat(bin);
            row_height[i] = 2;

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

    private void cc_c() {
        /* CC-C 2D component - byte compressed PDF417 */
        int length, i, binloc, k;
        int offset, longueur, loop, total, j;
        int[] mccorrection = new int[520];
        int c1, c2, c3;
        int[] dummy = new int[35];
        String codebarre;
        String bin;
        inputData = new int[(binary_string.length() / 8) + 4];

        length = binary_string.length() / 8;

        System.out.printf("Binlen: %d, bin: %s\n", binary_string.length(), binary_string);

        for (i = 0; i < length; i++) {
            binloc = i * 8;

            inputData[i] = 0;
            for (j = 0; j < 8; j++) {
                if (binary_string.charAt(binloc + j) == '1') {
                    inputData[i] += 0x80 >> j;
                }
            }
            System.out.printf("%d ", inputData[i]);
        }
        System.out.printf("\n");

        codeWordCount = 0;

        codeWords[codeWordCount] = 0; /* space for length descriptor */
        codeWordCount++;
        codeWords[codeWordCount] = 920; /* CC-C identifier */
        codeWordCount++;

        byteprocess(0, length);

        codeWords[0] = codeWordCount;

        k = 1;
        for (i = 1; i <= (ecc + 1); i++) {
            k *= 2;
        }

        /* 796 - we now take care of the Reed Solomon codes */
        switch (ecc) {
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

        longueur = codeWordCount;
        for (loop = 0; loop < 520; loop++) {
            mccorrection[loop] = 0;
        }
        total = 0;
        encodeInfo += "Composite Codewords: ";
        for (i = 0; i < longueur; i++) {
            total = (codeWords[i] + mccorrection[k - 1]) % 929;
            for (j = k - 1; j >= 0; j--) {
                if (j == 0) {
                    mccorrection[j] = (929 - (total * COEFRS[offset + j]) % 929) % 929;
                } else {
                    mccorrection[j] = (mccorrection[j - 1] + 929 - (total * COEFRS[offset + j]) % 929) % 929;
                }
            }
            encodeInfo += Integer.toString(codeWords[i]) + " ";
        }
        encodeInfo += "\n";

        for (j = 0; j < k; j++) {
            if (mccorrection[j] != 0) {
                mccorrection[j] = 929 - mccorrection[j];
            }
        }
        /* we add these codes to the string */
        for (i = k - 1; i >= 0; i--) {
            codeWords[codeWordCount] = mccorrection[i];
            codeWordCount++;
        }

        /* 818 - The CW string is finished */
        c1 = (codeWordCount / cc_width - 1) / 3;
        c2 = ecc * 3 + (codeWordCount / cc_width - 1) % 3;
        c3 = cc_width - 1;

        readable = "";
        row_count = codeWordCount / cc_width;
        pattern = new String[row_count];
        row_height = new int[row_count];

        /* we now encode each row */
        for (i = 0; i <= (codeWordCount / cc_width) - 1; i++) {
            for (j = 0; j < cc_width; j++) {
                dummy[j + 1] = codeWords[i * cc_width + j];
            }
            k = (i / 3) * 30;
            switch (i % 3) {
                /* follows this pattern from US Patent 5,243,655:
                 Row 0: L0 (row #, # of rows)         R0 (row #, # of columns)
                 Row 1: L1 (row #, security level)    R1 (row #, # of rows)
                 Row 2: L2 (row #, # of columns)      R2 (row #, security level)
                 Row 3: L3 (row #, # of rows)         R3 (row #, # of columns)
                 etc. */
                case 0:
                    dummy[0] = k + c1;
                    dummy[cc_width + 1] = k + c3;
                    break;
                case 1:
                    dummy[0] = k + c2;
                    dummy[cc_width + 1] = k + c1;
                    break;
                case 2:
                    dummy[0] = k + c3;
                    dummy[cc_width + 1] = k + c2;
                    break;
            }
            codebarre = "+*"; /* Start with a start char and a separator */

            for (j = 0; j <= cc_width + 1; j++) {
                switch (i % 3) {
                    case 1:
                        offset = 929; /* cluster(3) */ break;
                    case 2:
                        offset = 1858; /* cluster(6) */ break;
                    default:
                        offset = 0; /* cluster(0) */ break;
                }
                codebarre += CODAGEMC[offset + dummy[j]];
                codebarre += "*";
            }
            codebarre += "-";

            bin = "";
            for (loop = 0; loop < codebarre.length(); loop++) {
                bin += PDF_TTF[positionOf(codebarre.charAt(loop), BR_SET)];
            }
            pattern[i] = bin2pat(bin);
            row_height[i] = 3;
        }
    }

    private void byteprocess(int start, int length) {
        int len = 0;
        int chunkLen = 0;
        BigInteger mantisa;
        BigInteger total;
        BigInteger word;

        /* select the switch for multiple of 6 bytes */
        if ((binary_string.length() % 6) == 0) {
            codeWords[codeWordCount++] = 924;
        } else {
            codeWords[codeWordCount++] = 901;
        }

        while (len < length) {
            chunkLen = length - len;
            if (6 <= chunkLen) /* Take groups of 6 */ {
                chunkLen = 6;
                len += chunkLen;
                total = BigInteger.valueOf(0);

                while ((chunkLen--) != 0) {
                    mantisa = BigInteger.valueOf(inputData[start++]);
                    total = total.or(mantisa.shiftLeft(chunkLen * 8));
                }

                chunkLen = 5;

                while ((chunkLen--) != 0) {

                    word = total.mod(BigInteger.valueOf(900));
                    codeWords[codeWordCount + chunkLen] = word.intValue();
                    total = total.divide(BigInteger.valueOf(900));
                }
                codeWordCount += 5;
            } else /*  If it remain a group of less than 6 bytes   */ {
                len += chunkLen;
                while ((chunkLen--) != 0) {
                    codeWords[codeWordCount++] = inputData[start++];
                }
            }
        }
    }
}
