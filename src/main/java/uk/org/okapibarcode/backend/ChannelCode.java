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

/**
 * Implements Channel Code
 * According to ANSI/AIM BC12-1998
 *
 * @author <a href="mailto:rstuart114@gmail.com">Robin Stuart</a>
 * @version 0.1
 */
public class ChannelCode extends Symbol {
    private int[] space = new int[11];
    private int[] bar = new int[11];
    private double currentValue;
    private double targetValue;
    private String horizontalSpacing;

    @Override
    public boolean encode() {
        int numberOfChannels;
        int i;
        int leadingZeroCount;
        int requestedNumberOfChannels = option2 + 2;

        targetValue = 0;
        horizontalSpacing = "";

        if (content.length() > 7) {
            error_msg = "Input too long";
            return false;
        }

        if (!(content.matches("[0-9]+?"))) {
            error_msg = "Invalid characters in input";
            return false;
        }

        if ((requestedNumberOfChannels <= 2) || (requestedNumberOfChannels > 8)) {
            numberOfChannels = 3;
        } else {
            numberOfChannels = requestedNumberOfChannels;
        }

        for (i = 0; i < content.length(); i++) {
            targetValue *= 10;
            targetValue += Character.getNumericValue(content.charAt(i));
        }

        switch (numberOfChannels) {
        case 3:
            if (targetValue > 26) {
                numberOfChannels++;
            }
        case 4:
            if (targetValue > 292) {
                numberOfChannels++;
            }
        case 5:
            if (targetValue > 3493) {
                numberOfChannels++;
            }
        case 6:
            if (targetValue > 44072) {
                numberOfChannels++;
            }
        case 7:
            if (targetValue > 576688) {
                numberOfChannels++;
            }
        case 8:
            if (targetValue > 7742862) {
                numberOfChannels++;
            }
        }

        if (numberOfChannels == 9) {
            error_msg = "Value out of range";
            return false;
        }

        encodeInfo += "Channels Used: " + numberOfChannels + '\n';

        for (i = 0; i < 11; i++) {
            bar[i] = 0;
            space[i] = 0;
        }

        bar[0] = space[1] = bar[1] = space[2] = bar[2] = 1;
        currentValue = 0;
        nextSpace(numberOfChannels, 3, numberOfChannels, numberOfChannels);

        leadingZeroCount = numberOfChannels - 1 - content.length();

        readable = "";
        for (i = 0; i < leadingZeroCount; i++) {
            readable += "0";
        }
        readable += content;

        pattern = new String[1];
        pattern[0] = horizontalSpacing;
        row_count = 1;
        row_height = new int[1];
        row_height[0] = -1;
        plotSymbol();
        return true;
    }

    private void nextSpace(int channels, int i, int maxSpace, int maxBar) {
        int s;

        for (s = (i < channels + 2) ? 1 : maxSpace; s <= maxSpace; s++) {
            space[i] = s;
            nextBar(channels, i, maxBar, maxSpace + 1 - s);
        }
    }

    private void nextBar(int channels, int i, int maxBar, int maxSpace) {
        int b;

        b = (space[i] + bar[i - 1] + space[i - 1] + bar[i - 2] > 4) ? 1 : 2;
        if (i < channels + 2) {
            for (; b <= maxBar; b++) {
                bar[i] = b;
                nextSpace(channels, i + 1, maxSpace, maxBar + 1 - b);
            }
        } else if (b <= maxBar) {
            bar[i] = maxBar;
            checkIfDone();
            currentValue++;
        }
    }

    private void checkIfDone() {
        int i;

        if (currentValue == targetValue) {
            /* Target reached - save the generated pattern */
            horizontalSpacing = "11110";
            for (i = 0; i < 11; i++) {
                horizontalSpacing += (char)(space[i] + '0');
                horizontalSpacing += (char)(bar[i] + '0');
            }
        }
    }
}
