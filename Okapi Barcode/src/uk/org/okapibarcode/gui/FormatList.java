/*
 * Copyright 2014 Robin Stuart and Robert Elliott
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
 */package uk.org.okapibarcode.gui;

import java.util.*;
/**
 *
 * @author Robert Elliott <jakel2006@me.com>
 */
public class FormatList {
    ArrayList formats = new ArrayList();
    public FormatList() {
        formats.add("--------");
        formats.add("jpg");
        formats.add("png");
        formats.add("gif");
        formats.add("bmp");
        formats.add("svg");
        formats.add("eps");
    }
}

