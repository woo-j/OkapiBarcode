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
 * Implements Channel Code according to ANSI/AIM BC12-1998. Channel code encodes whole integer values
 * between 0 and 7,742,862.
 *
 * @author <a href="mailto:rstuart114@gmail.com">Robin Stuart</a>
 */
public class ChannelCode extends Symbol {

    private int[] space = new int[11];
    private int[] bar = new int[11];
    private double currentValue;
    private double targetValue;
    private int requestedNumberOfChannels;

    /**
     * Sets the number of channels used to encode data. This setting will be ignored if the value to be
     * encoded requires more channels.
     *
     * @param channels number of channels (between 3 and 8, inclusive)
     */
    public void setNumberOfChannels(int channels) {
        if (channels >= 3 && channels <= 8) {
            requestedNumberOfChannels = channels;
        }
    }

    @Override
    public boolean encode() {

        int channels;
        int i;
        int leadingZeroCount;

        if (content.length() > 7) {
            error_msg = "Input too long";
            return false;
        }

        if (!content.matches("[0-9]+")) {
            error_msg = "Invalid characters in input";
            return false;
        }

        if (requestedNumberOfChannels <= 2 || requestedNumberOfChannels > 8) {
            channels = 3;
        } else {
            channels = requestedNumberOfChannels;
        }

        targetValue = Integer.parseInt(content);

        switch (channels) {
            case 3:
                if (targetValue > 26) {
                    channels++;
                }
            case 4:
                if (targetValue > 292) {
                    channels++;
                }
            case 5:
                if (targetValue > 3493) {
                    channels++;
                }
            case 6:
                if (targetValue > 44072) {
                    channels++;
                }
            case 7:
                if (targetValue > 576688) {
                    channels++;
                }
            case 8:
                if (targetValue > 7742862) {
                    channels++;
                }
        }

        if (channels == 9) {
            error_msg = "Value out of range";
            return false;
        }

        encodeInfo += "Channels Used: " + channels + '\n';

        for (i = 0; i < 11; i++) {
            bar[i] = 0;
            space[i] = 0;
        }

        bar[0] = space[1] = bar[1] = space[2] = bar[2] = 1;
        currentValue = 0;
        pattern = new String[1];
        nextSpace(channels, 3, channels, channels);

        leadingZeroCount = channels - 1 - content.length();

        readable = "";
        for (i = 0; i < leadingZeroCount; i++) {
            readable += "0";
        }
        readable += content;

        row_count = 1;
        row_height = new int[] { -1 };

        return true;
    }

    private void nextSpace(int channels, int i, int maxSpace, int maxBar) {
        for (int s = (i < channels + 2) ? 1 : maxSpace; s <= maxSpace; s++) {
            space[i] = s;
            nextBar(channels, i, maxBar, maxSpace + 1 - s);
        }
    }

    private void nextBar(int channels, int i, int maxBar, int maxSpace) {
        int b = (space[i] + bar[i - 1] + space[i - 1] + bar[i - 2] > 4) ? 1 : 2;
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
        if (currentValue == targetValue) {
            /* Target reached - save the generated pattern */
            StringBuilder sb = new StringBuilder();
            sb.append("11110");
            for (int i = 0; i < 11; i++) {
                sb.append((char) (space[i] + '0'));
                sb.append((char) (bar[i] + '0'));
            }
            pattern[0] = sb.toString();
        }
    }
}
