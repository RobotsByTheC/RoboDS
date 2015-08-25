package littlebot.robods.communication;

import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Represents data that can be sent from the driver station to the robot.
 * Designed to be used with {@link DatagramPacket}s via the {@link
 * #toDatagramPacket(DatagramPacket)} method. This class is not thread safe and
 * is designed ot be interacted with from the UI thread of an application.
 *
 * @author Ben Wolsieffer
 * @see DatagramPacket
 */
public class DriverStationPacket extends FRCPacket {

    public enum Mode {
        TELEOPERATED(0x0),
        AUTONOMOUS(0x2),
        TEST(0x1);

        private int value;

        Mode(int value) {
            this.value = value;
        }
    }

    public enum Alliance {
        BLUE(2),
        RED(-1);

        private int value;

        Alliance(int value) {
            this.value = value;
        }
    }

    public static class Joystick extends Section {

        private static final int MIN_DATA_LENGTH = 3;

        private final ArrayList<Float> axes = new ArrayList<>();
        private final ArrayList<Boolean> buttons = new ArrayList<>();
        private final ArrayList<Integer> povHats = new ArrayList<>();

        private final boolean real;

        /**
         * Creates a new joystick.
         */
        public Joystick() {
            this(true);
        }

        private Joystick(boolean real) {
            super(MIN_DATA_LENGTH, 0x0c, true);
            this.real = real;
        }

        private boolean isReal() {
            return real;
        }

        /**
         * Gets the number of axes this joystick has.
         *
         * @return the axis count
         */
        public int getAxisCount() {
            synchronized (axes) {
                return axes.size();
            }
        }

        /**
         * Sets the number of axes this joystick has.
         *
         * @param count the number of axes the joystick should have
         */
        public void setAxisCount(int count) {
            synchronized (axes) {
                resizeList(axes, count, 0.0f);
                updateDataLength();
            }
        }

        /**
         * Gets the value of the axis at the specified index. The value will be
         * between -1.0 and 1.0.
         *
         * @param index the index of the axis
         * @return the value of the axis
         */
        public float getAxisValue(int index) {
            synchronized (axes) {
                return axes.get(index);
            }
        }

        /**
         * Sets the value of the axis at the specified index. The value must be
         * between -1.0 and 1.0.
         *
         * @param index the index of the axis to set
         * @param value the value of the axis
         * @throws IllegalArgumentException if the value is not between -1.0 and
         * 1.0
         */
        public void setAxisValue(int index, float value) {
            if (value <= 1.0 && value >= -1.0) {
                synchronized (axes) {
                    axes.set(index, value);
                }
            } else {
                throw new IllegalArgumentException("Value must be between -1.0  and 1.0");
            }
        }

        /**
         * Gets the number of buttons that this joystick has.
         *
         * @return the button count
         */
        public int getButtonCount() {
            synchronized (buttons) {
                return buttons.size();
            }
        }

        /**
         * Gets the number of buttons that this joystick has.
         *
         * @return the button count
         */
        public void setButtonCount(int count) {
            synchronized (buttons) {
                resizeList(buttons, count, false);
                updateDataLength();
            }
        }

        /**
         * Gets whether the button at the specified index is pressed
         *
         * @param index the index of the button
         * @return true if the button is pressed
         */
        public boolean isButtonPressed(int index) {
            synchronized (buttons) {
                return buttons.get(index);
            }
        }

        /**
         * Sets whether the button at the specified index is pressed
         *
         * @param index the index of the button
         * @param pressed if the button is pressed
         */
        public void setButtonPressed(int index, boolean pressed) {
            synchronized (buttons) {
                buttons.set(index, pressed);
            }
        }

        /**
         * Gets the number of POV hats on this joystick.
         *
         * @return the POV hat count
         */
        public int getPOVHatCount() {
            synchronized (povHats) {
                return povHats.size();
            }
        }

        /**
         * Sets the number of POV hats on this joystick.
         *
         * @param count the POV hat count
         */
        public void setPOVHatCount(int count) {
            synchronized (povHats) {
                resizeList(povHats, count, 0);
                updateDataLength();
            }
        }

        /**
         * Gets the angle of the specified POV hat. D-pads are often represented
         * as POV hats. An angle of -1 means that the POV hat is not pressed.
         *
         * @param index the index of the POV hat
         * @return the angle of the POV hat
         */
        public int getPOVHatAngle(int index) {
            synchronized (povHats) {
                return povHats.get(index);
            }
        }

        /**
         * Sets the angle of the specified POV hat. D-pads are often represented
         * as POV hats. An angle of -1 means that the POV hat is not pressed.
         *
         * @param index the index of the POV hat
         * @param angle the angle of the POV hat
         */
        public void setPOVHatAngle(int index, int angle) {
            synchronized (povHats) {
                povHats.set(index, angle);
            }
        }

        /**
         * Gets whether the joystick is empty.
         *
         * @return true if the joystick has no axes, buttons, or POV hats
         */
        public boolean isEmpty() {
            boolean axesEmpty;
            synchronized (axes) {
                axesEmpty = axes.size() == 0;
            }
            boolean buttonsEmpty;
            synchronized (buttons) {
                buttonsEmpty = buttons.size() == 0;
            }
            boolean povHatsEmpty;
            synchronized (povHats) {
                povHatsEmpty = povHats.size() == 0;
            }
            return axesEmpty && buttonsEmpty && povHatsEmpty;
        }

        /**
         * Gets the number of bytes necessary to write the buttons.
         *
         * @return the number of bytes for the buttons
         */
        private int getButtonBytes() {
            int numButtons = getButtonCount();
            // 8 buttons per byte
            return ((numButtons % 8) == 0) ? (numButtons / 8) : (numButtons / 8 + 1);
        }

        /**
         * Updates the length of the section to hold the axes and buttons.
         */
        private void updateDataLength() {
            // 3 extra bytes are necessary to store the button, axis and pov hat
            // counts
            setDataLength(MIN_DATA_LENGTH + getAxisCount() + getButtonBytes() + (getPOVHatCount() * 2));
        }

        /**
         * Write the joystick and axis data to the packet.
         *
         * @param data the array to write to
         * @param offset the start of the data in the array
         * @param length the length of the data
         */
        @Override
        public void updateData(byte[] data, int offset, int length) {
            synchronized (axes) {
                // Axes
                int axisCount = getAxisCount();
                // Axis count byte
                data[offset++] = (byte) axisCount;
                // Loop through each axis and write its value
                for (int i = 0; i < axisCount; i++) {
                    float axis = axes.get(i);
                    byte byteValue;
                    if (axis > 0) {
                        byteValue = (byte) (axis * 127);
                    } else {
                        byteValue = (byte) (axis * 128);
                    }
                    data[offset + i] = byteValue;
                }
                offset += axisCount;
            }
            synchronized (buttons) {
                // Buttons
                int buttonBytes = getButtonBytes();
                // Button count byte
                int buttonCount = getButtonCount();
                data[offset++] = (byte) buttonCount;
                int b = 0;
                for (int i = 0; i < buttonBytes; i++) {
                    // Each byte contains up to 8 buttons
                    byte buttonByte = 0;
                    for (int k = 0; k < 8; k++) {
                        if (b < buttonCount) {
                            if (buttons.get(b++)) {
                                buttonByte |= (byte) 1 << k;
                            }
                        } else {
                            break;
                        }
                    }
                    data[offset + i] = buttonByte;
                }
                offset += buttonBytes;
            }
            synchronized (povHats) {
                // POV Hats
                int povHatCount = getPOVHatCount();

                data[offset++] = (byte) povHatCount;
                for (int i = 0; i < povHatCount; i++) {
                    int angle = povHats.get(i);
                    int povStart = offset + (2 * i);
                    data[povStart] = (byte) (angle >> 8);
                    data[povStart + 1] = (byte) angle;
                }
            }
        }
    }

    private static class Time extends Section {

        private final Calendar calendar = Calendar.getInstance();

        public Time() {
            super(10, 0x0f, false);
        }

        @Override
        public void updateData(byte[] data, int offset, int length) {
            calendar.setTimeInMillis(System.currentTimeMillis());

            data[offset++] = 0; // Unknown
            data[offset++] = 0; // Unknown
            data[offset++] = 0; // Unknown
            data[offset++] = 0; // Unknown
            data[offset++] = (byte) calendar.get(Calendar.SECOND);
            data[offset++] = (byte) calendar.get(Calendar.MINUTE);
            data[offset++] = (byte) calendar.get(Calendar.HOUR_OF_DAY);
            data[offset++] = (byte) calendar.get(Calendar.DAY_OF_MONTH);
            data[offset++] = (byte) calendar.get(Calendar.MONTH);
            // Years since 1900
            data[offset++] = (byte) (calendar.get(Calendar.YEAR) - 1900);
        }
    }

    private static class Timezone extends Section {

        private static final String[] timezonesNoDst = {
                "BST11",
                "HST10",
                "AST9",
                "PST8",
                "MST7",
                "CST6",
                "EST5",
                "AST4",
                "GRNLNDST3",
                "FALKST2",
                "AZOREST1",
                "CUT0",
                "NFT-1",
                "WET-2",
                "MEST-3",
                "WST-4",
                "PAKST-5",
                "TASHST-6",
                "THAIST-7",
                "WAUST-8",
                "JST-9",
                "KORST-9",
                "EET-10",
                "MET-11",
                "NZST-12"
        };

        private static final String[] timezonesDst = {
                "BST11BDT",
                "HST10HDT",
                "AST9ADT",
                "PST8PDT",
                "MST7MDT",
                "CST6CDT",
                "EST5EDT",
                "AST4ADT",
                "GRNLNDST3GRNLNDDT",
                "FALKST2FALKDT",
                "AZOREST1AZOREDT",
                "CUT0GDT",
                "NFT-1DFT",
                "WET-2WET",
                "MEST-3MEDT",
                "WST-4WDT",
                "PAKST-5PAKDT",
                "TASHST-6TASHDT",
                "THAIST-7THAIDT",
                "WAUST-8WAUDT",
                "JST-9JSTDT",
                "KORST-9KORDT",
                "EET-10EETDT",
                "MET-11METDT",
                "NZST-12NZDT"
        };

        private final byte[] timezone;

        public Timezone() {
            super(7, 0x10, false);

            TimeZone timezone = TimeZone.getDefault();
            int timeOffset = timezone.getRawOffset();
            int absTimeOffset = Math.abs(timeOffset);
            int timeOffsetHours = absTimeOffset / 3600000;
            absTimeOffset -= timeOffsetHours  * 3600000;
            int timeOffsetMinutes = (int) TimeUnit.MILLISECONDS.toMinutes(absTimeOffset);
            if (timeOffset > 0) {
                if (timeOffsetHours != 0) {
                    timeOffsetHours *= -1;
                } else {
                    timeOffsetMinutes *= -1;
                }
            }
            String timezoneName;
            // If the timezone is offset on the hour
            if (timeOffsetMinutes == 0) {
                // Check to see if the offset is within the defined range
                if (timeOffsetHours <= 11 && timeOffsetHours >= -12) {
                    // Use the correct string based on daylight savings
                    timezoneName = getDefinedTimezone(timeOffsetHours, timezone.useDaylightTime());
                } else {
                    // If the hour is not known, make up a timezone
                    timezoneName = getFakeTimezone(timeOffsetHours, 0, timezone.useDaylightTime());
                }
            } else {
                // If there are minutes in the offset, make up a timezone
                timezoneName = getFakeTimezone(timeOffsetHours, timeOffsetMinutes, timezone.useDaylightTime());
            }

            this.timezone = timezoneName.getBytes();
            setDataLength(this.timezone.length);
        }

        @Override
        public void updateData(byte[] data, int offset, int length) {
            System.arraycopy(timezone, 0, data, offset, length);
        }

        /**
         * Get a timezone defined in the list based on its offset and whether it
         * has daylight savings. Will crash if given a timezone less than -12 or
         * greater than 11.
         *
         * @param offsetHours the number of hours the timezone is offset from
         * UTC
         * @param dst whether the timezone has daylight savings
         * @return the name of the timezone for the roboRIO
         */
        private String getDefinedTimezone(int offsetHours, boolean dst) {
            if (dst) {
                return timezonesDst[11 - offsetHours];
            } else {
                return timezonesNoDst[11 - offsetHours];
            }
        }

        /**
         * Create a timezone based on the hour and minute offsets and whether or
         * not it has daylight savings. The roboRIO doesn't really care if the
         * timezone is from the list as long as it follows the correct format.
         *
         * @param offsetHours the hours component of the offset from UTC
         * @param offsetMinutes the minutes component of the offset from UTC
         * @param dst whether the timezone has daylight savings
         * @return the name of the timezone for the roboRIO
         */
        private String getFakeTimezone(int offsetHours, int offsetMinutes, boolean dst) {
            StringBuilder zone = new StringBuilder("TIM");
            // If the offset is less than an hour, the sign needs special treatment
            if (offsetHours == 0 && offsetMinutes < 0) {
                zone.append("-0:").append(-offsetMinutes);
            } else {
                zone.append(offsetHours);
                if (offsetMinutes != 0) {
                    zone.append(":").append(offsetMinutes);
                }
            }
            if (dst) {
                zone.append("DST");
            }
            return zone.toString();
        }
    }

    private final ArrayList<Joystick> joysticks = new ArrayList<>();
    private Mode mode = Mode.TELEOPERATED;
    private boolean enabled = false;
    private boolean emergencyStopped = false;
    private Alliance alliance = Alliance.BLUE;
    private int position = 1;

    /**
     * Create a new packet to send from the driver station to the robot.
     */
    public DriverStationPacket() {
        super(4);
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public Mode getMode() {
        return mode;
    }

    public void setEnabled(boolean enabled) {
        if (!emergencyStopped) {
            this.enabled = enabled;
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEmergencyStopped(boolean emergencyStopped) {
        this.emergencyStopped = emergencyStopped;
        enabled = false;
    }

    public boolean isEmergencyStopped() {
        return emergencyStopped;
    }

    public void setAlliance(Alliance alliance) {
        this.alliance = alliance;
    }

    public Alliance getAlliance() {
        return alliance;
    }

    public void setPosition(int position) {
        if (position >= 1 && position <= 3) {
            this.position = position;
        } else {
            throw new IllegalArgumentException("Position must be 1, 2 or 3, was given:" + position);
        }
    }

    public int getPosition() {
        return position;
    }

    /**
     * Add a joystick to the packet.
     *
     * @param joystick the joystick
     * @throws IllegalArgumentException if the joystick has already been added
     */
    public void addJoystick(int index, Joystick joystick) {
        if (index < joysticks.size()) {
            if (!joysticks.get(index).isReal()) {
                joysticks.set(index, joystick);
                removeSection(index);
            } else {
                throw new IllegalArgumentException("This packet already contains a joystick at index " + index);
            }
        } else {
            for (int i = joysticks.size(); i < index; i++) {
                Joystick fakeJoystick = new Joystick(false);
                addSection(i, fakeJoystick);
                joysticks.add(i, fakeJoystick);
            }
            joysticks.add(index, joystick);
        }
        addSection(index, joystick);
    }

    /**
     * Remove a joystick from this packet.
     *
     * @param joystick the joystick to remove
     */
    public void removeJoystick(Joystick joystick) {
        removeSection(joystick);

        int index = joysticks.indexOf(joystick);
        if (index == -1) {
            throw new IllegalArgumentException("Joystick was not added to this packet.");
        } else if (index == joysticks.size() - 1) {
            joysticks.remove(joystick);
        } else {
            joysticks.set(index, new Joystick(false));
        }
    }

    public Joystick getJoystick(int index) {
        if (index < joysticks.size()) {
            Joystick joystick = joysticks.get(index);
            if (joystick.isReal()) {
                return joystick;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public void addTime() {
        addSection(new Time());
        addSection(new Timezone());
    }

    /**
     * Writes the body of the packet. This includes the mode, alliance color and
     * position.
     *
     * @param data the data array for the packet
     * @param offset the offset where the body starts
     * @param length the length of the body
     */
    @Override
    public void updateBody(byte[] data, int offset, int length) {
        data[offset++] = 0x01; // Unknown
        data[offset] = 0;
        data[offset] = (byte) mode.value;
        if (emergencyStopped) {
            data[offset] |= 0x80;
        } else {
            if (enabled) {
                data[offset] |= 0x4;
            }
        }
        offset++;
        data[offset++] = 0x10; // Unknown
        data[offset] = (byte) (alliance.value + position);
    }

    /**
     * Resizes a {@link List} while inserting the specified value for all new
     * elements.
     *
     * @param list the list to resize
     * @param size the new size of the list
     * @param defaultValue the value to add when new elements have to be
     * created
     * @param <T> the type of the list contents
     */
    private static <T> void resizeList(List<T> list, int size, T defaultValue) {
        int currentSize = list.size();
        if (size > currentSize) {
            if (list instanceof ArrayList) {
                ((ArrayList<T>) list).ensureCapacity(size);
            }
            for (int i = currentSize; i < size; i++) {
                list.add(defaultValue);
            }
        } else if (size < currentSize) {
            for (int i = currentSize - 1; i >= size; i--) {
                list.remove(i);
            }
        }
    }
}