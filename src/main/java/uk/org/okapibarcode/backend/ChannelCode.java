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
 * <p>Implements Channel Code according to ANSI/AIM BC12-1998.
 *
 * <p>Channel Code encodes whole integer values between 0 and 7,742,862.
 *
 * @author <a href="mailto:rstuart114@gmail.com">Robin Stuart</a>
 */
public class ChannelCode extends Symbol {

    private int preferredNumberOfChannels = 3;

    private int[] space = new int[11];
    private int[] bar = new int[11];
    private int currentValue;
    private int targetValue;

    /**
     * Sets the preferred number of channels used to encode data. This setting will be
     * ignored if the value to be encoded requires more channels.
     *
     * @param channels the preferred number of channels (3 to 8, inclusive)
     */
    public void setPreferredNumberOfChannels(int channels) {
        if (channels < 3 || channels > 8) {
            throw new IllegalArgumentException("Invalid number of channels: " + channels);
        }
        preferredNumberOfChannels = channels;
    }

    /**
     * Returns the preferred number of channels used to encode data.
     *
     * @return the preferred number of channels used to encode data
     */
    public int getPreferredNumberOfChannels() {
        return preferredNumberOfChannels;
    }

    @Override
    protected void encode() {

        if (content.length() > 7) {
            throw OkapiInputException.inputTooLong();
        }

        if (!content.matches("[0-9]+")) {
            throw OkapiInputException.invalidCharactersInInput();
        }

        int channels = preferredNumberOfChannels;
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
            throw new OkapiInputException("Value out of range");
        }

        infoLine("Channels Used: ", channels);

        for (int i = 0; i < 11; i++) {
            bar[i] = 0;
            space[i] = 0;
        }

        bar[0] = space[1] = bar[1] = space[2] = bar[2] = 1;
        currentValue = 0;
        pattern = new String[1];
        nextSpace(channels, 3, channels, channels);

        StringBuilder text = new StringBuilder();
        int leadingZeroCount = channels - 1 - content.length();
        for (int i = 0; i < leadingZeroCount; i++) {
            text.append('0');
        }
        text.append(content);

        readable = text.toString();
        row_count = 1;
        row_height = new int[] { -1 };
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
