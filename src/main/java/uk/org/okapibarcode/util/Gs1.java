/*
 * Copyright 2018 Robin Stuart, Daniel Gredler
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

package uk.org.okapibarcode.util;

import java.util.HashMap;
import java.util.Map;

import uk.org.okapibarcode.backend.OkapiInputException;

/**
 * GS1 utility class.
 */
public final class Gs1 {

    private static final byte DIGITS = 1; // 0-9
    private static final byte CHARS_82 = 2; // GS1 charset 82
    private static final byte CHARS_39 = 3; // GS1 charset 39
    private static final byte CHARS_UPPERCASE = 4; // A-Z
    private static final byte FLAG = 5; // 0-1

    private static final Map< Integer, byte[] > AIS = new HashMap<>();

    static {
        AIS.put(00,   new byte[] { DIGITS, 18, 18 }); // N2+N18
        AIS.put(01,   new byte[] { DIGITS, 14, 14 }); // N2+N14
        AIS.put(02,   new byte[] { DIGITS, 14, 14 }); // N2+N14
        AIS.put(10,   new byte[] { CHARS_82, 0, 20 }); // N2+X..20
        AIS.put(11,   new byte[] { DIGITS, 6, 6 }); // N2+N6
        AIS.put(12,   new byte[] { DIGITS, 6, 6 }); // N2+N6
        AIS.put(13,   new byte[] { DIGITS, 6, 6 }); // N2+N6
        AIS.put(15,   new byte[] { DIGITS, 6, 6 }); // N2+N6
        AIS.put(16,   new byte[] { DIGITS, 6, 6 }); // N2+N6
        AIS.put(17,   new byte[] { DIGITS, 6, 6 }); // N2+N6
        AIS.put(20,   new byte[] { DIGITS, 2, 2 }); // N2+N2
        AIS.put(21,   new byte[] { CHARS_82, 0, 20 }); // N2+X..20
        AIS.put(22,   new byte[] { CHARS_82, 0, 20 }); // N2+X..20
        AIS.put(235,  new byte[] { CHARS_82, 0, 28 }); // N3+X..28
        AIS.put(240,  new byte[] { CHARS_82, 0, 30 }); // N3+X..30
        AIS.put(241,  new byte[] { CHARS_82, 0, 30 }); // N3+X..30
        AIS.put(242,  new byte[] { DIGITS, 0, 6 }); // N3+N..6
        AIS.put(243,  new byte[] { CHARS_82, 0, 20 }); // N3+X..20
        AIS.put(250,  new byte[] { CHARS_82, 0, 30 }); // N3+X..30
        AIS.put(251,  new byte[] { CHARS_82, 0, 30 }); // N3+X..30
        AIS.put(253,  new byte[] { DIGITS, 13, 13, CHARS_82, 0, 17 }); // N3+N13+X..17
        AIS.put(254,  new byte[] { CHARS_82, 0, 20 }); // N3+X..20
        AIS.put(255,  new byte[] { DIGITS, 13, 13, DIGITS, 0, 12 }); // N3+N13+N..12
        AIS.put(30,   new byte[] { DIGITS, 0, 8 }); // N2+N..8
        AIS.put(3100, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3101, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3102, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3103, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3104, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3105, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3110, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3111, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3112, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3113, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3114, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3115, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3120, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3121, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3122, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3123, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3124, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3125, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3130, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3131, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3132, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3133, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3134, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3135, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3140, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3141, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3142, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3143, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3144, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3145, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3150, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3151, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3152, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3153, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3154, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3155, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3160, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3161, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3162, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3163, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3164, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3165, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3200, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3201, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3202, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3203, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3204, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3205, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3210, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3211, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3212, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3213, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3214, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3215, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3220, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3221, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3222, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3223, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3224, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3225, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3230, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3231, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3232, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3233, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3234, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3235, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3240, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3241, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3242, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3243, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3244, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3245, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3250, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3251, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3252, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3253, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3254, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3255, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3260, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3261, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3262, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3263, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3264, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3265, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3270, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3271, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3272, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3273, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3274, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3275, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3280, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3281, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3282, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3283, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3284, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3285, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3290, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3291, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3292, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3293, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3294, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3295, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3300, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3301, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3302, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3303, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3304, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3305, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3310, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3311, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3312, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3313, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3314, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3315, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3320, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3321, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3322, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3323, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3324, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3325, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3330, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3331, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3332, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3333, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3334, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3335, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3340, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3341, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3342, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3343, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3344, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3345, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3350, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3351, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3352, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3353, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3354, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3355, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3360, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3361, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3362, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3363, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3364, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3365, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3370, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3371, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3372, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3373, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3374, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3375, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3400, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3401, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3402, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3403, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3404, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3405, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3410, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3411, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3412, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3413, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3414, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3415, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3420, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3421, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3422, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3423, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3424, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3425, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3430, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3431, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3432, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3433, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3434, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3435, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3440, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3441, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3442, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3443, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3444, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3445, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3450, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3451, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3452, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3453, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3454, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3455, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3460, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3461, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3462, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3463, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3464, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3465, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3470, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3471, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3472, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3473, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3474, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3475, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3480, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3481, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3482, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3483, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3484, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3485, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3490, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3491, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3492, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3493, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3494, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3495, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3500, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3501, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3502, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3503, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3504, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3505, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3510, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3511, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3512, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3513, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3514, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3515, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3520, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3521, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3522, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3523, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3524, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3525, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3530, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3531, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3532, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3533, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3534, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3535, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3540, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3541, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3542, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3543, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3544, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3545, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3550, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3551, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3552, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3553, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3554, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3555, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3560, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3561, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3562, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3563, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3564, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3565, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3570, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3571, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3572, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3573, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3574, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3575, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3600, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3601, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3602, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3603, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3604, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3605, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3610, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3611, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3612, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3613, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3614, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3615, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3620, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3621, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3622, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3623, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3624, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3625, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3630, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3631, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3632, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3633, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3634, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3635, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3640, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3641, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3642, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3643, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3644, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3645, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3650, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3651, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3652, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3653, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3654, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3655, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3660, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3661, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3662, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3663, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3664, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3665, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3670, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3671, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3672, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3673, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3674, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3675, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3680, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3681, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3682, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3683, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3684, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3685, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3690, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3691, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3692, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3693, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3694, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3695, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(37,   new byte[] { DIGITS, 0, 8 }); // N2+N..8
        AIS.put(3900, new byte[] { DIGITS, 0, 15 }); // N4+N..15
        AIS.put(3901, new byte[] { DIGITS, 0, 15 }); // N4+N..15
        AIS.put(3902, new byte[] { DIGITS, 0, 15 }); // N4+N..15
        AIS.put(3903, new byte[] { DIGITS, 0, 15 }); // N4+N..15
        AIS.put(3904, new byte[] { DIGITS, 0, 15 }); // N4+N..15
        AIS.put(3905, new byte[] { DIGITS, 0, 15 }); // N4+N..15
        AIS.put(3906, new byte[] { DIGITS, 0, 15 }); // N4+N..15
        AIS.put(3907, new byte[] { DIGITS, 0, 15 }); // N4+N..15
        AIS.put(3908, new byte[] { DIGITS, 0, 15 }); // N4+N..15
        AIS.put(3909, new byte[] { DIGITS, 0, 15 }); // N4+N..15
        AIS.put(3910, new byte[] { DIGITS, 3, 3, DIGITS, 0, 15 }); // N4+N3+N..15
        AIS.put(3911, new byte[] { DIGITS, 3, 3, DIGITS, 0, 15 }); // N4+N3+N..15
        AIS.put(3912, new byte[] { DIGITS, 3, 3, DIGITS, 0, 15 }); // N4+N3+N..15
        AIS.put(3913, new byte[] { DIGITS, 3, 3, DIGITS, 0, 15 }); // N4+N3+N..15
        AIS.put(3914, new byte[] { DIGITS, 3, 3, DIGITS, 0, 15 }); // N4+N3+N..15
        AIS.put(3915, new byte[] { DIGITS, 3, 3, DIGITS, 0, 15 }); // N4+N3+N..15
        AIS.put(3916, new byte[] { DIGITS, 3, 3, DIGITS, 0, 15 }); // N4+N3+N..15
        AIS.put(3917, new byte[] { DIGITS, 3, 3, DIGITS, 0, 15 }); // N4+N3+N..15
        AIS.put(3918, new byte[] { DIGITS, 3, 3, DIGITS, 0, 15 }); // N4+N3+N..15
        AIS.put(3919, new byte[] { DIGITS, 3, 3, DIGITS, 0, 15 }); // N4+N3+N..15
        AIS.put(3920, new byte[] { DIGITS, 0, 15 }); // N4+N..15
        AIS.put(3921, new byte[] { DIGITS, 0, 15 }); // N4+N..15
        AIS.put(3922, new byte[] { DIGITS, 0, 15 }); // N4+N..15
        AIS.put(3923, new byte[] { DIGITS, 0, 15 }); // N4+N..15
        AIS.put(3924, new byte[] { DIGITS, 0, 15 }); // N4+N..15
        AIS.put(3925, new byte[] { DIGITS, 0, 15 }); // N4+N..15
        AIS.put(3926, new byte[] { DIGITS, 0, 15 }); // N4+N..15
        AIS.put(3927, new byte[] { DIGITS, 0, 15 }); // N4+N..15
        AIS.put(3928, new byte[] { DIGITS, 0, 15 }); // N4+N..15
        AIS.put(3929, new byte[] { DIGITS, 0, 15 }); // N4+N..15
        AIS.put(3930, new byte[] { DIGITS, 3, 3, DIGITS, 0, 15 }); // N4+N3+N..15
        AIS.put(3931, new byte[] { DIGITS, 3, 3, DIGITS, 0, 15 }); // N4+N3+N..15
        AIS.put(3932, new byte[] { DIGITS, 3, 3, DIGITS, 0, 15 }); // N4+N3+N..15
        AIS.put(3933, new byte[] { DIGITS, 3, 3, DIGITS, 0, 15 }); // N4+N3+N..15
        AIS.put(3934, new byte[] { DIGITS, 3, 3, DIGITS, 0, 15 }); // N4+N3+N..15
        AIS.put(3935, new byte[] { DIGITS, 3, 3, DIGITS, 0, 15 }); // N4+N3+N..15
        AIS.put(3936, new byte[] { DIGITS, 3, 3, DIGITS, 0, 15 }); // N4+N3+N..15
        AIS.put(3937, new byte[] { DIGITS, 3, 3, DIGITS, 0, 15 }); // N4+N3+N..15
        AIS.put(3938, new byte[] { DIGITS, 3, 3, DIGITS, 0, 15 }); // N4+N3+N..15
        AIS.put(3939, new byte[] { DIGITS, 3, 3, DIGITS, 0, 15 }); // N4+N3+N..15
        AIS.put(3940, new byte[] { DIGITS, 4, 4 }); // N4+N4
        AIS.put(3941, new byte[] { DIGITS, 4, 4 }); // N4+N4
        AIS.put(3942, new byte[] { DIGITS, 4, 4 }); // N4+N4
        AIS.put(3943, new byte[] { DIGITS, 4, 4 }); // N4+N4
        AIS.put(3950, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3951, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3952, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3953, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3954, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3955, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3956, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3957, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3958, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(3959, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(400,  new byte[] { CHARS_82, 0, 30 }); // N3+X..30
        AIS.put(401,  new byte[] { CHARS_82, 0, 30 }); // N3+X..30
        AIS.put(402,  new byte[] { DIGITS, 17, 17 }); // N3+N17
        AIS.put(403,  new byte[] { CHARS_82, 0, 30 }); // N3+X..30
        AIS.put(410,  new byte[] { DIGITS, 13, 13 }); // N3+N13
        AIS.put(411,  new byte[] { DIGITS, 13, 13 }); // N3+N13
        AIS.put(412,  new byte[] { DIGITS, 13, 13 }); // N3+N13
        AIS.put(413,  new byte[] { DIGITS, 13, 13 }); // N3+N13
        AIS.put(414,  new byte[] { DIGITS, 13, 13 }); // N3+N13
        AIS.put(415,  new byte[] { DIGITS, 13, 13 }); // N3+N13
        AIS.put(416,  new byte[] { DIGITS, 13, 13 }); // N3+N13
        AIS.put(417,  new byte[] { DIGITS, 13, 13 }); // N3+N13
        AIS.put(420,  new byte[] { CHARS_82, 0, 20 }); // N3+X..20
        AIS.put(421,  new byte[] { DIGITS, 3, 3, CHARS_82, 0, 9 }); // N3+N3+X..9
        AIS.put(422,  new byte[] { DIGITS, 3, 3 }); // N3+N3
        AIS.put(423,  new byte[] { DIGITS, 3, 3, DIGITS, 0, 12 }); // N3+N3+N..12
        AIS.put(424,  new byte[] { DIGITS, 3, 3 }); // N3+N3
        AIS.put(425,  new byte[] { DIGITS, 3, 3, DIGITS, 0, 12 }); // N3+N3+N..12
        AIS.put(426,  new byte[] { DIGITS, 3, 3 }); // N3+N3
        AIS.put(427,  new byte[] { CHARS_82, 0, 3 }); // N3+X..3
        AIS.put(4300, new byte[] { CHARS_82, 0, 35 }); // N4+X..35
        AIS.put(4301, new byte[] { CHARS_82, 0, 35 }); // N4+X..35
        AIS.put(4302, new byte[] { CHARS_82, 0, 70 }); // N4+X..70
        AIS.put(4303, new byte[] { CHARS_82, 0, 70 }); // N4+X..70
        AIS.put(4304, new byte[] { CHARS_82, 0, 70 }); // N4+X..70
        AIS.put(4305, new byte[] { CHARS_82, 0, 70 }); // N4+X..70
        AIS.put(4306, new byte[] { CHARS_82, 0, 70 }); // N4+X..70
        AIS.put(4307, new byte[] { CHARS_UPPERCASE, 2, 2 }); // N4+X2
        AIS.put(4308, new byte[] { CHARS_82, 0, 30 }); // N4+X..30
        AIS.put(4310, new byte[] { CHARS_82, 0, 35 }); // N4+X..35
        AIS.put(4311, new byte[] { CHARS_82, 0, 35 }); // N4+X..35
        AIS.put(4312, new byte[] { CHARS_82, 0, 70 }); // N4+X..70
        AIS.put(4313, new byte[] { CHARS_82, 0, 70 }); // N4+X..70
        AIS.put(4314, new byte[] { CHARS_82, 0, 70 }); // N4+X..70
        AIS.put(4315, new byte[] { CHARS_82, 0, 70 }); // N4+X..70
        AIS.put(4316, new byte[] { CHARS_82, 0, 70 }); // N4+X..70
        AIS.put(4317, new byte[] { CHARS_UPPERCASE, 2, 2 }); // N4+X2
        AIS.put(4318, new byte[] { CHARS_82, 0, 20 }); // N4+X..20
        AIS.put(4319, new byte[] { CHARS_82, 0, 30 }); // N4+X..30
        AIS.put(4320, new byte[] { CHARS_82, 0, 35 }); // N4+X..35
        AIS.put(4321, new byte[] { FLAG, 1, 1 }); // N4+N1
        AIS.put(4322, new byte[] { FLAG, 1, 1 }); // N4+N1
        AIS.put(4323, new byte[] { FLAG, 1, 1 }); // N4+N1
        AIS.put(4324, new byte[] { DIGITS, 10, 10 }); // N4+N10
        AIS.put(4325, new byte[] { DIGITS, 10, 10 }); // N4+N10
        AIS.put(4326, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(7001, new byte[] { DIGITS, 13, 13 }); // N4+N13
        AIS.put(7002, new byte[] { CHARS_82, 0, 30 }); // N4+X..30
        AIS.put(7003, new byte[] { DIGITS, 10, 10 }); // N4+N10
        AIS.put(7004, new byte[] { DIGITS, 0, 4 }); // N4+N..4
        AIS.put(7005, new byte[] { CHARS_82, 0, 12 }); // N4+X..12
        AIS.put(7006, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(7007, new byte[] { DIGITS, 6, 12 }); // N4+N6..12
        AIS.put(7008, new byte[] { CHARS_82, 0, 3 }); // N4+X..3
        AIS.put(7009, new byte[] { CHARS_82, 0, 10 }); // N4+X..10
        AIS.put(7010, new byte[] { CHARS_82, 0, 2 }); // N4+X..2
        AIS.put(7020, new byte[] { CHARS_82, 0, 20 }); // N4+X..20
        AIS.put(7021, new byte[] { CHARS_82, 0, 20 }); // N4+X..20
        AIS.put(7022, new byte[] { CHARS_82, 0, 20 }); // N4+X..20
        AIS.put(7023, new byte[] { CHARS_82, 0, 30 }); // N4+X..30
        AIS.put(7030, new byte[] { DIGITS, 3, 3, CHARS_82, 0, 27 }); // N4+N3+X..27
        AIS.put(7031, new byte[] { DIGITS, 3, 3, CHARS_82, 0, 27 }); // N4+N3+X..27
        AIS.put(7032, new byte[] { DIGITS, 3, 3, CHARS_82, 0, 27 }); // N4+N3+X..27
        AIS.put(7033, new byte[] { DIGITS, 3, 3, CHARS_82, 0, 27 }); // N4+N3+X..27
        AIS.put(7034, new byte[] { DIGITS, 3, 3, CHARS_82, 0, 27 }); // N4+N3+X..27
        AIS.put(7035, new byte[] { DIGITS, 3, 3, CHARS_82, 0, 27 }); // N4+N3+X..27
        AIS.put(7036, new byte[] { DIGITS, 3, 3, CHARS_82, 0, 27 }); // N4+N3+X..27
        AIS.put(7037, new byte[] { DIGITS, 3, 3, CHARS_82, 0, 27 }); // N4+N3+X..27
        AIS.put(7038, new byte[] { DIGITS, 3, 3, CHARS_82, 0, 27 }); // N4+N3+X..27
        AIS.put(7039, new byte[] { DIGITS, 3, 3, CHARS_82, 0, 27 }); // N4+N3+X..27
        AIS.put(7040, new byte[] { DIGITS, 1, 1, CHARS_82, 3, 3 }); // N4+N1+X3
        AIS.put(710,  new byte[] { CHARS_82, 0, 20 }); // N3+X..20
        AIS.put(711,  new byte[] { CHARS_82, 0, 20 }); // N3+X..20
        AIS.put(712,  new byte[] { CHARS_82, 0, 20 }); // N3+X..20
        AIS.put(713,  new byte[] { CHARS_82, 0, 20 }); // N3+X..20
        AIS.put(714,  new byte[] { CHARS_82, 0, 20 }); // N3+X..20
        AIS.put(715,  new byte[] { CHARS_82, 0, 20 }); // N3+X..20
        AIS.put(7230, new byte[] { CHARS_82, 2, 30 }); // N4+X2+X..28
        AIS.put(7231, new byte[] { CHARS_82, 2, 30 }); // N4+X2+X..28
        AIS.put(7232, new byte[] { CHARS_82, 2, 30 }); // N4+X2+X..28
        AIS.put(7233, new byte[] { CHARS_82, 2, 30 }); // N4+X2+X..28
        AIS.put(7234, new byte[] { CHARS_82, 2, 30 }); // N4+X2+X..28
        AIS.put(7235, new byte[] { CHARS_82, 2, 30 }); // N4+X2+X..28
        AIS.put(7236, new byte[] { CHARS_82, 2, 30 }); // N4+X2+X..28
        AIS.put(7237, new byte[] { CHARS_82, 2, 30 }); // N4+X2+X..28
        AIS.put(7238, new byte[] { CHARS_82, 2, 30 }); // N4+X2+X..28
        AIS.put(7239, new byte[] { CHARS_82, 2, 30 }); // N4+X2+X..28
        AIS.put(7240, new byte[] { CHARS_82, 0, 20 }); // N4+X..20
        AIS.put(8001, new byte[] { DIGITS, 14, 14 }); // N4+N14
        AIS.put(8002, new byte[] { CHARS_82, 0, 20 }); // N4+X..20
        AIS.put(8003, new byte[] { DIGITS, 14, 14, CHARS_82, 0, 16 }); // N4+N14+X..16
        AIS.put(8004, new byte[] { CHARS_82, 0, 30 }); // N4+X..30
        AIS.put(8005, new byte[] { DIGITS, 6, 6 }); // N4+N6
        AIS.put(8006, new byte[] { DIGITS, 18, 18 }); // N4+N14+N2+N2
        AIS.put(8007, new byte[] { CHARS_82, 0, 34 }); // N4+X..34
        AIS.put(8008, new byte[] { DIGITS, 8, 12 }); // N4+N8+N..4
        AIS.put(8009, new byte[] { CHARS_82, 0, 50 }); // N4+X..50
        AIS.put(8010, new byte[] { CHARS_39, 0, 30 }); // N4+Y..30
        AIS.put(8011, new byte[] { DIGITS, 0, 12 }); // N4+N..12
        AIS.put(8012, new byte[] { CHARS_82, 0, 20 }); // N4+X..20
        AIS.put(8013, new byte[] { CHARS_82, 0, 25 }); // N4+X..25
        AIS.put(8017, new byte[] { DIGITS, 18, 18 }); // N4+N18
        AIS.put(8018, new byte[] { DIGITS, 18, 18 }); // N4+N18
        AIS.put(8019, new byte[] { DIGITS, 0, 10 }); // N4+N..10
        AIS.put(8020, new byte[] { CHARS_82, 0, 25 }); // N4+X..25
        AIS.put(8026, new byte[] { DIGITS, 18, 18 }); // N4+N14+N2+N2
        AIS.put(8110, new byte[] { CHARS_82, 0, 70 }); // N4+X..70
        AIS.put(8111, new byte[] { DIGITS, 4, 4 }); // N4+N4
        AIS.put(8112, new byte[] { CHARS_82, 0, 70 }); // N4+X..70
        AIS.put(8200, new byte[] { CHARS_82, 0, 70 }); // N4+X..70
        AIS.put(90,   new byte[] { CHARS_82, 0, 30 }); // N2+X..30
        AIS.put(91,   new byte[] { CHARS_82, 0, 90 }); // N2+X..90
        AIS.put(92,   new byte[] { CHARS_82, 0, 90 }); // N2+X..90
        AIS.put(93,   new byte[] { CHARS_82, 0, 90 }); // N2+X..90
        AIS.put(94,   new byte[] { CHARS_82, 0, 90 }); // N2+X..90
        AIS.put(95,   new byte[] { CHARS_82, 0, 90 }); // N2+X..90
        AIS.put(96,   new byte[] { CHARS_82, 0, 90 }); // N2+X..90
        AIS.put(97,   new byte[] { CHARS_82, 0, 90 }); // N2+X..90
        AIS.put(98,   new byte[] { CHARS_82, 0, 90 }); // N2+X..90
        AIS.put(99,   new byte[] { CHARS_82, 0, 90 }); // N2+X..90
    }

    private Gs1() {
        // utility class
    }

    /**
     * Verifies that the specified data is in good GS1 format {@code "[AI]data"} pairs, and returns a reduced
     * version of the input string containing FNC1 escape sequences instead of AI brackets. With a few small
     * exceptions, this code matches the Zint GS1 validation code as closely as possible, in order to make it
     * easier to keep in sync.
     *
     * @param s the data string to verify
     * @param fnc1 the string to use to represent FNC1 in the output
     * @return the input data, verified and with FNC1 strings added at the appropriate positions
     * @see <a href="https://sourceforge.net/p/zint/code/ci/master/tree/backend/gs1.c">Corresponding Zint code</a>
     * @see <a href="http://www.gs1.org/docs/gsmp/barcodes/GS1_General_Specifications.pdf">GS1 specification</a>
     */
    public static String verify(String s, String fnc1) {

        // Enforce compliance with GS1 General Specification
        // https://www.gs1.org/docs/barcodes/GS1_General_Specifications.pdf

        char[] source = s.toCharArray();
        StringBuilder reduced = new StringBuilder(source.length);
        int[] ai_value = new int[100];
        int[] ai_location = new int[100];
        int[] data_location = new int[100];
        int[] data_length = new int[100];

        /* Make sure we start with an AI */
        if (source.length == 0 || source[0] != '[') {
            throw new OkapiInputException("Data does not start with an AI");
        }

        /* Check the position of the brackets */
        int bracket_level = 0;
        int max_bracket_level = 0;
        int ai_length = 0;
        int max_ai_length = 0;
        int min_ai_length = 5;
        int j = 0;
        boolean ai_latch = false;
        for (int i = 0; i < source.length; i++) {
            ai_length += j;
            if (((j == 1) && (source[i] != ']')) && ((source[i] < '0') || (source[i] > '9'))) {
                ai_latch = true;
            }
            if (source[i] == '[') {
                bracket_level++;
                j = 1;
            }
            if (source[i] == ']') {
                bracket_level--;
                if (ai_length < min_ai_length) {
                    min_ai_length = ai_length;
                }
                j = 0;
                ai_length = 0;
            }
            if (bracket_level > max_bracket_level) {
                max_bracket_level = bracket_level;
            }
            if (ai_length > max_ai_length) {
                max_ai_length = ai_length;
            }
        }
        min_ai_length--;

        if (bracket_level != 0) {
            /* Not all brackets are closed */
            throw new OkapiInputException("Malformed AI in input data (brackets don't match)");
        }

        if (max_bracket_level > 1) {
            /* Nested brackets */
            throw new OkapiInputException("Found nested brackets in input data");
        }

        if (max_ai_length > 4) {
            /* AI is too long */
            throw new OkapiInputException("Invalid AI in input data (AI too long)");
        }

        if (min_ai_length <= 1) {
            /* AI is too short */
            throw new OkapiInputException("Invalid AI in input data (AI too short)");
        }

        if (ai_latch) {
            /* Non-numeric data in AI */
            throw new OkapiInputException("Invalid AI in input data (non-numeric characters in AI)");
        }

        int ai_count = 0;
        for (int i = 1; i < source.length; i++) {
            if (source[i - 1] == '[') {
                ai_location[ai_count] = i;
                ai_value[ai_count] = 0;
                for (j = 0; source[i + j] != ']'; j++) {
                    ai_value[ai_count] *= 10;
                    ai_value[ai_count] += Character.getNumericValue(source[i + j]);
                }
                ai_count++;
            }
        }

        for (int i = 0; i < ai_count; i++) {
            data_location[i] = ai_location[i] + 3;
            if (ai_value[i] >= 100) {
                data_location[i]++;
            }
            if (ai_value[i] >= 1000) {
                data_location[i]++;
            }
            data_length[i] = source.length - data_location[i];
            for (j = source.length - 1; j >= data_location[i]; j--) {
                if (source[j] == '[') {
                    data_length[i] = j - data_location[i];
                }
            }
        }

        /* Check for valid AI values and data lengths according to GS1 General Specification */
        for (int i = 0; i < ai_count; i++) { // loop through AIs

            int ai = ai_value[i];
            byte[] info = AIS.get(ai);
            if (info == null) {
                throw new OkapiInputException("Invalid AI value " + ai);
            }

            int processed = 0;

            for (j = 0; j + 2 < info.length; j += 3) { // for each AI, loop through data segments

                byte type = info[j];
                byte min = info[j + 1];
                byte max = info[j + 2];

                int limit;
                if (j + 3 == info.length) {
                    // last segment
                    limit = data_length[i] - processed;
                    if (limit < min || limit > max) {
                        throw new OkapiInputException("Invalid data length for AI " + ai);
                    }
                } else {
                    // not last segment
                    limit = max;
                    assert min == max;
                    if (processed + limit > data_length[i]) {
                        throw new OkapiInputException("Invalid data length for AI " + ai);
                    }
                }

                for (int k = 0; k < limit; k++) { // for each data segment, validate all data
                    int index = data_location[i] + processed + k;
                    char c = source[index];
                    boolean valid =
                       (type == DIGITS && c >= '0' && c <= '9') ||
                       (type == CHARS_82 && ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '%' && c <= '?') || c == '_' || c == '!' || c == '"')) ||
                       (type == CHARS_39 && ((c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '#' || c == '-' || c == '/')) ||
                       (type == FLAG && c >= '0' && c <= '1') ||
                       (type == CHARS_UPPERCASE && c >= 'A' && c <= 'Z');
                    if (!valid) {
                        throw new OkapiInputException("Invalid data value for AI " + ai);
                    }
                }

                processed += max; // we assume that all non-final segments have min == max (i.e. only final segments have variable lengths)
            }
        }

        /* Resolve AI data, put resulting string in 'reduced' */
        int last_ai = 0;
        boolean fixedLengthAI = true;
        for (int i = 0; i < source.length; i++) {
            if (source[i] != '[' && source[i] != ']') {
                reduced.append(source[i]);
            }
            if (source[i] == '[') {
                // start of an AI string
                if (!fixedLengthAI) {
                    reduced.append(fnc1);
                }
                last_ai = (10 * Character.getNumericValue(source[i + 1]))
                              + Character.getNumericValue(source[i + 2]);
                // The following values from "GS-1 General Specification version 8.0 issue 2, May 2008" figure 5.4.8.2.1-1
                // "Element Strings with Pre-Defined Length Using Application Identifiers" (using first two digits of AI)
                fixedLengthAI =
                        (last_ai >= 0 && last_ai <= 4) ||
                        (last_ai >= 11 && last_ai <= 20) ||
                        (last_ai >= 31 && last_ai <= 36) ||
                        (last_ai == 41);
            }
        }

        return reduced.toString();
    }
}
