/*
 * Copyright 2021 Daniel Gredler
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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import uk.org.okapibarcode.backend.OkapiException;

/**
 * Tests for {@link Gs1}.
 */
public class Gs1Test {

    @Test
    public void testVerify() {

        checkVerify("01234",                                    "Data does not start with an AI");
        checkVerify("[04]123",                                  "Invalid AI value 4");
        checkVerify("[999]123",                                 "Invalid AI value 999");
        checkVerify("[01",                                      "Malformed AI in input data (brackets don't match)");
        checkVerify("[01]123[3400",                             "Malformed AI in input data (brackets don't match)");
        checkVerify("[34[00]]123",                              "Found nested brackets in input data");
        checkVerify("[01]123[34[00]]123",                       "Found nested brackets in input data");
        checkVerify("[34009]123",                               "Invalid AI in input data (AI too long)");
        checkVerify("[01]123[34009]123",                        "Invalid AI in input data (AI too long)");
        checkVerify("[]123",                                    "Invalid AI in input data (AI too short)");
        checkVerify("[01]123[]123",                             "Invalid AI in input data (AI too short)");
        checkVerify("[0A]123",                                  "Invalid AI in input data (non-numeric characters in AI)");
        checkVerify("[0/]123",                                  "Invalid AI in input data (non-numeric characters in AI)");
        checkVerify("[0:]123",                                  "Invalid AI in input data (non-numeric characters in AI)");

        checkVerify("[01]12345678901234[98]ABC",                "011234567890123498ABC");
        checkVerify("[10]123[98]ABC[99]123456",                 "10123|98ABC|99123456");
        checkVerify("[02]12345678901234[412]1234567890123[10]", "0212345678901234412123456789012310");
        checkVerify("[410]1234567890123[98]ABC",                "410123456789012398ABC");

        checkVerify("[00]",                                     "Invalid data length for AI 0");
        checkVerify("[00]12345678901234567",                    "Invalid data length for AI 0");
        checkVerify("[00]123456789012345678",                   "00123456789012345678");
        checkVerify("[00]1234567890123456789",                  "Invalid data length for AI 0");
        checkVerify("[00]12345678901234567A",                   "Invalid data value for AI 0");

        checkVerify("[01]",                                     "Invalid data length for AI 1");
        checkVerify("[01]1234567890123",                        "Invalid data length for AI 1");
        checkVerify("[01]12345678901234",                       "0112345678901234");
        checkVerify("[01]123456789012345",                      "Invalid data length for AI 1");
        checkVerify("[01]1234567890123/",                       "Invalid data value for AI 1");

        checkVerify("[02]",                                     "Invalid data length for AI 2");
        checkVerify("[02]1234567890123",                        "Invalid data length for AI 2");
        checkVerify("[02]12345678901234",                       "0212345678901234");
        checkVerify("[02]123456789012345",                      "Invalid data length for AI 2");
        checkVerify("[02]1234567890123/",                       "Invalid data value for AI 2");

        checkVerify("[10]",                                     "10");
        checkVerify("[10]1",                                    "101");
        checkVerify("[10]123",                                  "10123");
        checkVerify("[10]" + chars82(20),                       "10" + chars82(20));
        checkVerify("[10]" + chars82(21),                       "Invalid data length for AI 10");
        checkVerify("[10]\r",                                   "Invalid data value for AI 10");

        checkVerify("[11]",                                     "Invalid data length for AI 11");
        checkVerify("[11]12345",                                "Invalid data length for AI 11");
        checkVerify("[11]123456",                               "11123456");
        checkVerify("[11]1234567",                              "Invalid data length for AI 11");
        checkVerify("[11]12345}",                               "Invalid data value for AI 11");

        checkVerify("[12]",                                     "Invalid data length for AI 12");
        checkVerify("[12]12345",                                "Invalid data length for AI 12");
        checkVerify("[12]123456",                               "12123456");
        checkVerify("[12]1234567",                              "Invalid data length for AI 12");
        checkVerify("[12]12345}",                               "Invalid data value for AI 12");

        checkVerify("[13]",                                     "Invalid data length for AI 13");
        checkVerify("[13]12345",                                "Invalid data length for AI 13");
        checkVerify("[13]123456",                               "13123456");
        checkVerify("[13]1234567",                              "Invalid data length for AI 13");
        checkVerify("[13]12345}",                               "Invalid data value for AI 13");

        checkVerify("[15]",                                     "Invalid data length for AI 15");
        checkVerify("[15]12345",                                "Invalid data length for AI 15");
        checkVerify("[15]123456",                               "15123456");
        checkVerify("[15]1234567",                              "Invalid data length for AI 15");
        checkVerify("[15]12345}",                               "Invalid data value for AI 15");

        checkVerify("[16]",                                     "Invalid data length for AI 16");
        checkVerify("[16]12345",                                "Invalid data length for AI 16");
        checkVerify("[16]123456",                               "16123456");
        checkVerify("[16]1234567",                              "Invalid data length for AI 16");
        checkVerify("[16]12345}",                               "Invalid data value for AI 16");

        checkVerify("[17]",                                     "Invalid data length for AI 17");
        checkVerify("[17]12345",                                "Invalid data length for AI 17");
        checkVerify("[17]123456",                               "17123456");
        checkVerify("[17]1234567",                              "Invalid data length for AI 17");
        checkVerify("[17]12345}",                               "Invalid data value for AI 17");

        checkVerify("[20]",                                     "Invalid data length for AI 20");
        checkVerify("[20]1",                                    "Invalid data length for AI 20");
        checkVerify("[20]12",                                   "2012");
        checkVerify("[20]123",                                  "Invalid data length for AI 20");
        checkVerify("[20]1a",                                   "Invalid data value for AI 20");

        checkVerify("[21]",                                     "21");
        checkVerify("[21]1",                                    "211");
        checkVerify("[21]123",                                  "21123");
        checkVerify("[21]" + chars82(20),                       "21" + chars82(20));
        checkVerify("[21]" + chars82(21),                       "Invalid data length for AI 21");
        checkVerify("[21]`",                                    "Invalid data value for AI 21");

        checkVerify("[22]",                                     "22");
        checkVerify("[22]1",                                    "221");
        checkVerify("[22]123",                                  "22123");
        checkVerify("[22]" + chars82(20),                       "22" + chars82(20));
        checkVerify("[22]" + chars82(21),                       "Invalid data length for AI 22");
        checkVerify("[22]`",                                    "Invalid data value for AI 22");

        checkVerify("[235]",                                    "235");
        checkVerify("[235]1",                                   "2351");
        checkVerify("[235]123",                                 "235123");
        checkVerify("[235]" + chars82(28),                      "235" + chars82(28));
        checkVerify("[235]" + chars82(29),                      "Invalid data length for AI 235");
        checkVerify("[235]#",                                   "Invalid data value for AI 235");

        checkVerify("[240]",                                    "240");
        checkVerify("[240]1",                                   "2401");
        checkVerify("[240]123",                                 "240123");
        checkVerify("[240]" + chars82(30),                      "240" + chars82(30));
        checkVerify("[240]" + chars82(31),                      "Invalid data length for AI 240");
        checkVerify("[240]#",                                   "Invalid data value for AI 240");

        checkVerify("[241]",                                    "241");
        checkVerify("[241]1",                                   "2411");
        checkVerify("[241]123",                                 "241123");
        checkVerify("[241]" + chars82(30),                      "241" + chars82(30));
        checkVerify("[241]" + chars82(31),                      "Invalid data length for AI 241");
        checkVerify("[241]#",                                   "Invalid data value for AI 241");

        checkVerify("[242]",                                    "242");
        checkVerify("[242]1",                                   "2421");
        checkVerify("[242]123",                                 "242123");
        checkVerify("[242]123456",                              "242123456");
        checkVerify("[242]1234567",                             "Invalid data length for AI 242");
        checkVerify("[242]A",                                   "Invalid data value for AI 242");

        checkVerify("[243]",                                    "243");
        checkVerify("[243]1",                                   "2431");
        checkVerify("[243]123",                                 "243123");
        checkVerify("[243]" + chars82(20),                      "243" + chars82(20));
        checkVerify("[243]" + chars82(21),                      "Invalid data length for AI 243");
        checkVerify("[243]#",                                   "Invalid data value for AI 243");

        checkVerify("[250]",                                    "250");
        checkVerify("[250]1",                                   "2501");
        checkVerify("[250]123",                                 "250123");
        checkVerify("[250]" + chars82(30),                      "250" + chars82(30));
        checkVerify("[250]" + chars82(31),                      "Invalid data length for AI 250");
        checkVerify("[250]#",                                   "Invalid data value for AI 250");

        checkVerify("[251]",                                    "251");
        checkVerify("[251]1",                                   "2511");
        checkVerify("[251]123",                                 "251123");
        checkVerify("[251]" + chars82(30),                      "251" + chars82(30));
        checkVerify("[251]" + chars82(31),                      "Invalid data length for AI 251");
        checkVerify("[251]#",                                   "Invalid data value for AI 251");

        checkVerify("[253]",                                    "Invalid data length for AI 253");
        checkVerify("[253]" + digits(12),                       "Invalid data length for AI 253");
        checkVerify("[253]" + digits(13),                       "253" + digits(13));
        checkVerify("[253]" + digits(13) + chars82(1),          "253" + digits(13) + chars82(1));
        checkVerify("[253]" + digits(13) + chars82(17),         "253" + digits(13) + chars82(17));
        checkVerify("[253]" + digits(13) + chars82(18),         "Invalid data length for AI 253");
        checkVerify("[253]" + digits(12) + "#",                 "Invalid data value for AI 253");
        checkVerify("[253]" + digits(13) + chars82(16) + "\r",  "Invalid data value for AI 253");

        checkVerify("[254]",                                    "254");
        checkVerify("[254]1",                                   "2541");
        checkVerify("[254]123",                                 "254123");
        checkVerify("[254]" + chars82(20),                      "254" + chars82(20));
        checkVerify("[254]" + chars82(21),                      "Invalid data length for AI 254");
        checkVerify("[254]#",                                   "Invalid data value for AI 254");

        checkVerify("[255]",                                    "Invalid data length for AI 255");
        checkVerify("[255]" + digits(12),                       "Invalid data length for AI 255");
        checkVerify("[255]" + digits(13),                       "255" + digits(13));
        checkVerify("[255]" + digits(14),                       "255" + digits(14));
        checkVerify("[255]" + digits(25),                       "255" + digits(25));
        checkVerify("[255]" + digits(26),                       "Invalid data length for AI 255");
        checkVerify("[255]" + digits(12) + "*",                 "Invalid data value for AI 255");

        checkVerify("[30]",                                     "30");
        checkVerify("[30]12345",                                "3012345");
        checkVerify("[30]12345678",                             "3012345678");
        checkVerify("[30]123456789",                            "Invalid data length for AI 30");
        checkVerify("[30]1234L}",                               "Invalid data value for AI 30");

        checkVerify("[3100]12345",                              "Invalid data length for AI 3100");
        checkVerify("[3100]123456",                             "3100123456");
        checkVerify("[3100]1234567",                            "Invalid data length for AI 3100");

        checkVerify("[3200]12345",                              "Invalid data length for AI 3200");
        checkVerify("[3200]123456",                             "3200123456");
        checkVerify("[3200]1234567",                            "Invalid data length for AI 3200");

        checkVerify("[3400]12345",                              "Invalid data length for AI 3400");
        checkVerify("[3400]123456",                             "3400123456");
        checkVerify("[3400]1234567",                            "Invalid data length for AI 3400");

        checkVerify("[3600]12345",                              "Invalid data length for AI 3600");
        checkVerify("[3600]123456",                             "3600123456");
        checkVerify("[3600]1234567",                            "Invalid data length for AI 3600");

        checkVerify("[3900]123456789012345",                    "3900123456789012345");
        checkVerify("[3900]1234567890123456",                   "Invalid data length for AI 3900");

        checkVerify("[3910]12",                                 "Invalid data length for AI 3910");
        checkVerify("[3910]123",                                "3910123");
        checkVerify("[3910]123456789012345678",                 "3910123456789012345678");
        checkVerify("[3910]1234567890123456789",                "Invalid data length for AI 3910");

        checkVerify("[3920]123456789012345",                    "3920123456789012345");
        checkVerify("[3920]1234567890123456",                   "Invalid data length for AI 3920");

        checkVerify("[3930]12",                                 "Invalid data length for AI 3930");
        checkVerify("[3930]123",                                "3930123");
        checkVerify("[3930]123456789012345678",                 "3930123456789012345678");
        checkVerify("[3930]1234567890123456789",                "Invalid data length for AI 3930");

        checkVerify("[3940]123",                                "Invalid data length for AI 3940");
        checkVerify("[3940]1234",                               "39401234");
        checkVerify("[3940]12345",                              "Invalid data length for AI 3940");

        checkVerify("[421]12",                                  "Invalid data length for AI 421");
        checkVerify("[421]123",                                 "421123");
        checkVerify("[421]123456789012",                        "421123456789012");
        checkVerify("[421]1234567890123",                       "Invalid data length for AI 421");

        checkVerify("[423]12",                                  "Invalid data length for AI 423");
        checkVerify("[423]123",                                 "423123");
        checkVerify("[423]123456789012345",                     "423123456789012345");
        checkVerify("[423]1234567890123456",                    "Invalid data length for AI 423");

        checkVerify("[425]12",                                  "Invalid data length for AI 425");
        checkVerify("[425]123",                                 "425123");
        checkVerify("[425]123456789012345",                     "425123456789012345");
        checkVerify("[425]1234567890123456",                    "Invalid data length for AI 425");

        checkVerify("[4307]U",                                  "Invalid data length for AI 4307");
        checkVerify("[4307]US",                                 "4307US");
        checkVerify("[4307]USA",                                "Invalid data length for AI 4307");
        checkVerify("[4307]us",                                 "Invalid data value for AI 4307");
        checkVerify("[4307]U@",                                 "Invalid data value for AI 4307");
        checkVerify("[4307]U^",                                 "Invalid data value for AI 4307");

        checkVerify("[4321]",                                   "Invalid data length for AI 4321");
        checkVerify("[4321]0",                                  "43210");
        checkVerify("[4321]1",                                  "43211");
        checkVerify("[4321]11",                                 "Invalid data length for AI 4321");
        checkVerify("[4321]2",                                  "Invalid data value for AI 4321");
        checkVerify("[4321]X",                                  "Invalid data value for AI 4321");
        checkVerify("[4321]/",                                  "Invalid data value for AI 4321");

        checkVerify("[7007]12345",                              "Invalid data length for AI 7007");
        checkVerify("[7007]123456",                             "7007123456");
        checkVerify("[7007]123456789012",                       "7007123456789012");
        checkVerify("[7007]1234567890123",                      "Invalid data length for AI 7007");

        checkVerify("[7030]12",                                 "Invalid data length for AI 7030");
        checkVerify("[7030]123",                                "7030123");
        checkVerify("[7030]123456789012345678901234567890",     "7030123456789012345678901234567890");
        checkVerify("[7030]1234567890123456789012345678901",    "Invalid data length for AI 7030");

        checkVerify("[8003]1234567890123",                      "Invalid data length for AI 8003");
        checkVerify("[8003]12345678901234",                     "800312345678901234");
        checkVerify("[8003]123456789012345678901234567890",     "8003123456789012345678901234567890");
        checkVerify("[8003]1234567890123456789012345678901",    "Invalid data length for AI 8003");

        checkVerify("[8008]1234567",                            "Invalid data length for AI 8008");
        checkVerify("[8008]12345678",                           "800812345678");
        checkVerify("[8008]123456789012",                       "8008123456789012");
        checkVerify("[8008]1234567890123",                      "Invalid data length for AI 8008");

        checkVerify("[8010]",                                   "8010");
        checkVerify("[8010]1",                                  "80101");
        checkVerify("[8010]123",                                "8010123");
        checkVerify("[8010]" + chars39(30),                     "8010" + chars39(30));
        checkVerify("[8010]" + chars39(31),                     "Invalid data length for AI 8010");
        checkVerify("[8010]!",                                  "Invalid data value for AI 8010");
        checkVerify("[8010]*",                                  "Invalid data value for AI 8010");
        checkVerify("[8010].",                                  "Invalid data value for AI 8010");
        checkVerify("[8010]:",                                  "Invalid data value for AI 8010");
        checkVerify("[8010]@",                                  "Invalid data value for AI 8010");
        checkVerify("[8010]^",                                  "Invalid data value for AI 8010");
        checkVerify("[8010]b",                                  "Invalid data value for AI 8010");

        checkVerify("[8011]",                                   "8011");
        checkVerify("[8011]1",                                  "80111");
        checkVerify("[8011]123",                                "8011123");
        checkVerify("[8011]" + digits(12),                      "8011" + digits(12));
        checkVerify("[8011]" + digits(13),                      "Invalid data length for AI 8011");
        checkVerify("[8011]#",                                  "Invalid data value for AI 8011");

        checkVerify("[8012]",                                   "8012");
        checkVerify("[8012]1",                                  "80121");
        checkVerify("[8012]123",                                "8012123");
        checkVerify("[8012]" + chars82(20),                     "8012" + chars82(20));
        checkVerify("[8012]" + chars82(21),                     "Invalid data length for AI 8012");
        checkVerify("[8012]#",                                  "Invalid data value for AI 8012");

        checkVerify("[8013]",                                   "8013");
        checkVerify("[8013]1",                                  "80131");
        checkVerify("[8013]123",                                "8013123");
        checkVerify("[8013]" + chars82(25),                     "8013" + chars82(25));
        checkVerify("[8013]" + chars82(26),                     "Invalid data length for AI 8013");
        checkVerify("[8013]#",                                  "Invalid data value for AI 8013");

        checkVerify("[8017]" + digits(17),                      "Invalid data length for AI 8017");
        checkVerify("[8017]" + digits(18),                      "8017" + digits(18));
        checkVerify("[8017]" + digits(19),                      "Invalid data length for AI 8017");
        checkVerify("[8017]" + digits(17) + "_",                "Invalid data value for AI 8017");

        checkVerify("[8018]" + digits(17),                      "Invalid data length for AI 8018");
        checkVerify("[8018]" + digits(18),                      "8018" + digits(18));
        checkVerify("[8018]" + digits(19),                      "Invalid data length for AI 8018");
        checkVerify("[8018]" + digits(17) + "#",                "Invalid data value for AI 8018");

        checkVerify("[8019]",                                   "8019");
        checkVerify("[8019]1",                                  "80191");
        checkVerify("[8019]1234567890",                         "80191234567890");
        checkVerify("[8019]12345678901",                        "Invalid data length for AI 8019");
        checkVerify("[8019]123456789A",                         "Invalid data value for AI 8019");

        checkVerify("[8020]",                                   "8020");
        checkVerify("[8020]1",                                  "80201");
        checkVerify("[8020]123",                                "8020123");
        checkVerify("[8020]" + chars82(25),                     "8020" + chars82(25));
        checkVerify("[8020]" + chars82(26),                     "Invalid data length for AI 8020");
        checkVerify("[8020]#",                                  "Invalid data value for AI 8020");

        checkVerify("[8026]",                                   "Invalid data length for AI 8026");
        checkVerify("[8026]12345678901234567",                  "Invalid data length for AI 8026");
        checkVerify("[8026]123456789012345678",                 "8026123456789012345678");
        checkVerify("[8026]1234567890123456789",                "Invalid data length for AI 8026");
        checkVerify("[8026]12345678901234567:",                 "Invalid data value for AI 8026");
        checkVerify("[8026]12345678901234567/",                 "Invalid data value for AI 8026");

        checkVerify("[8110]",                                   "8110");
        checkVerify("[8110]1",                                  "81101");
        checkVerify("[8110]123",                                "8110123");
        checkVerify("[8110]" + chars82(70),                     "8110" + chars82(70));
        checkVerify("[8110]" + chars82(71),                     "Invalid data length for AI 8110");

        checkVerify("[8111]",                                   "Invalid data length for AI 8111");
        checkVerify("[8111]1",                                  "Invalid data length for AI 8111");
        checkVerify("[8111]1234",                               "81111234");
        checkVerify("[8111]12345",                              "Invalid data length for AI 8111");
        checkVerify("[8111]ABCD",                               "Invalid data value for AI 8111");

        checkVerify("[8112]",                                   "8112");
        checkVerify("[8112]1",                                  "81121");
        checkVerify("[8112]123",                                "8112123");
        checkVerify("[8112]" + chars82(70),                     "8112" + chars82(70));
        checkVerify("[8112]" + chars82(71),                     "Invalid data length for AI 8112");

        checkVerify("[8200]",                                   "8200");
        checkVerify("[8200]1",                                  "82001");
        checkVerify("[8200]123",                                "8200123");
        checkVerify("[8200]" + chars82(70),                     "8200" + chars82(70));
        checkVerify("[8200]" + chars82(71),                     "Invalid data length for AI 8200");

        checkVerify("[90]",                                     "90");
        checkVerify("[90]1",                                    "901");
        checkVerify("[90]123",                                  "90123");
        checkVerify("[90]" + chars82(30),                       "90" + chars82(30));
        checkVerify("[90]" + chars82(31),                       "Invalid data length for AI 90");

        checkVerify("[91]",                                     "91");
        checkVerify("[91]1",                                    "911");
        checkVerify("[91]1_2!3\"4",                             "911_2!3\"4");
        checkVerify("[91]" + chars82(90),                       "91" + chars82(90));
        checkVerify("[91]" + chars82(91),                       "Invalid data length for AI 91");
        checkVerify("[91]123\t",                                "Invalid data value for AI 91");
        checkVerify("[91]123@",                                 "Invalid data value for AI 91");
        checkVerify("[91]123^",                                 "Invalid data value for AI 91");
        checkVerify("[91]123`",                                 "Invalid data value for AI 91");
        checkVerify("[91]123{",                                 "Invalid data value for AI 91");
        checkVerify("[91]123$",                                 "Invalid data value for AI 91");

        checkVerify("[92]",                                     "92");
        checkVerify("[92]1",                                    "921");
        checkVerify("[92]123",                                  "92123");
        checkVerify("[92]" + chars82(90),                       "92" + chars82(90));
        checkVerify("[92]" + chars82(91),                       "Invalid data length for AI 92");

        checkVerify("[93]",                                     "93");
        checkVerify("[93]1",                                    "931");
        checkVerify("[93]123",                                  "93123");
        checkVerify("[93]" + chars82(90),                       "93" + chars82(90));
        checkVerify("[93]" + chars82(91),                       "Invalid data length for AI 93");

        checkVerify("[94]",                                     "94");
        checkVerify("[94]1",                                    "941");
        checkVerify("[94]123",                                  "94123");
        checkVerify("[94]" + chars82(90),                       "94" + chars82(90));
        checkVerify("[94]" + chars82(91),                       "Invalid data length for AI 94");

        checkVerify("[95]",                                     "95");
        checkVerify("[95]1",                                    "951");
        checkVerify("[95]123",                                  "95123");
        checkVerify("[95]" + chars82(90),                       "95" + chars82(90));
        checkVerify("[95]" + chars82(91),                       "Invalid data length for AI 95");

        checkVerify("[96]",                                     "96");
        checkVerify("[96]1",                                    "961");
        checkVerify("[96]123",                                  "96123");
        checkVerify("[96]" + chars82(90),                       "96" + chars82(90));
        checkVerify("[96]" + chars82(91),                       "Invalid data length for AI 96");

        checkVerify("[97]",                                     "97");
        checkVerify("[97]1",                                    "971");
        checkVerify("[97]123",                                  "97123");
        checkVerify("[97]" + chars82(90),                       "97" + chars82(90));
        checkVerify("[97]" + chars82(91),                       "Invalid data length for AI 97");

        checkVerify("[98]",                                     "98");
        checkVerify("[98]1",                                    "981");
        checkVerify("[98]123",                                  "98123");
        checkVerify("[98]" + chars82(90),                       "98" + chars82(90));
        checkVerify("[98]" + chars82(91),                       "Invalid data length for AI 98");

        checkVerify("[99]",                                     "99");
        checkVerify("[99]1",                                    "991");
        checkVerify("[99]123",                                  "99123");
        checkVerify("[99]" + chars82(90),                       "99" + chars82(90));
        checkVerify("[99]" + chars82(91),                       "Invalid data length for AI 99");
    }

    private static void checkVerify(String value, String expectedOutput) {
        String output;
        try {
            output = Gs1.verify(value, "|");
        } catch (OkapiException e) {
            output = e.getMessage();
        }
        assertEquals(expectedOutput, output);
    }

    private static String chars82(int length) {
        String charset82 = "!\"%&'()*+,-./0123456789:;<=>?ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz"; // see GS1 spec section 7.11
        return chars(length, charset82);
    }

    private static String chars39(int length) {
        String charset39 = "#-/0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"; // see GS1 spec section 7.11
        return chars(length, charset39);
    }

    private static String digits(int length) {
        String digits = "0123456789";
        return chars(length, digits);
    }

    private static String chars(int length, String charset) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(charset.charAt(i % charset.length()));
        }
        return sb.toString();
    }
}
