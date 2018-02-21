/*
 * Copyright 2018 Daniel Gredler
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

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for {@link AddOn}.
 */
public class AddOnTest {

    @Test
    public void testCalcAddOn() {
        AddOn addOn = new AddOn();
        Assert.assertEquals("",                                addOn.calcAddOn(""));
        Assert.assertEquals("1123211111222",                   addOn.calcAddOn("1"));
        Assert.assertEquals("1122221112122",                   addOn.calcAddOn("12"));
        Assert.assertEquals("1121123111123112221112122111411", addOn.calcAddOn("123"));
        Assert.assertEquals("1123211111222112212111411111132", addOn.calcAddOn("1234"));
        Assert.assertEquals("1121222112122111141111132111231", addOn.calcAddOn("12345"));
        Assert.assertEquals("",                                addOn.calcAddOn("123456"));
        Assert.assertEquals("",                                addOn.calcAddOn("1234567"));
    }
}
