package littlebot.robods;

import org.apache.http.util.ByteArrayBuffer;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by raystubbs on 23/03/15.
 */
public class DSPacketFactory {

    private static final int CONTROL_DISABLED = 0x0,
            CONTROL_TELEOP = 0x4,
            CONTROL_TEST = 0x5,
            CONTROL_AUTO = 0x6,
            CONTROL_ESTOP = 0x80;

    private static final int STATUS_OK = 0x10,
            STATUS_NO_CODE = 0x14,
            STATUS_REBOOTING = 0x18;

    private static final int POSITION_RED1 = 0,
            POSITION_RED2 = 1,
            POSITION_RED3 = 2,
            POSITION_BLUE1 = 3,
            POSITION_BLUE2 = 4,
            POSITION_BLUE3 = 5;


    private static final int POS_PACKET_INDEX = 0, POS_CONTROL_BYTE = 3, POS_ROBOT_STATUS = 4;

    private volatile ArrayList<Axis> axisList = new ArrayList<>();
    private volatile ArrayList<ButtonSymbol> buttonList = new ArrayList<>();
    private volatile int joystickCount = 0;
    private volatile short packetIndex = 0;

    private volatile boolean enabled;
    private volatile boolean autoEnabled;


    public void registerJoystick(JoystickView joystick) {
        registerAxis(joystick.getXAxis());
        registerAxis(joystick.getYAxis());
    }

    public void registerAxis(Axis axis) {
        axisList.add(axis);
        joystickCount = Math.max(joystickCount, axis.getJoystickNumber() + 1);  //+1 because indexing begins at 0
    }

    public void registerButton(ButtonView button) {
        final ButtonSymbol buttonRep = new ButtonSymbol(button.getJoystickNumber(), button.getButtonNumber());
        buttonList.add(buttonRep);
        joystickCount = Math.max(joystickCount, button.getJoystickNumber() + 1);

        button.setButtonListener(new ButtonView.ButtonListener() {
            @Override
            public void buttonPressed() {
                buttonRep.setPressed(true);
            }

            @Override
            public void buttonReleased() {
                buttonRep.setPressed(false);
            }
        });
    }

    public void registerEnableButton(EnableButton button) {


        button.addEnableListener(new EnableButton.EnableListener() {
            @Override
            public void onEnabled() {
                enabled = true;
            }

            @Override
            public void onDisabled() {
                enabled = false;
            }
        });
    }

    public void registerModeSwitch(ModeSwitch modeSwitch) {

        modeSwitch.setModeChangeListener(new ModeSwitch.ModeChangeListener() {
            @Override
            public void onTeleopEnabled() {
                autoEnabled = false;
            }

            @Override
            public void onAutoEnabled() {
                autoEnabled = true;
            }
        });
    }


    public byte[] getPacket() {

        ByteArrayBuffer data = new ByteArrayBuffer(500);

        data.append(shortToByte(packetIndex, true), 0, 2);

        //Unknown byte, seems to always be 1
        data.append(1);

        if (enabled) {
            if (autoEnabled)
                data.append(CONTROL_AUTO);
            else
                data.append(CONTROL_TELEOP);
        } else {
            data.append(CONTROL_DISABLED);
        }

        //Temporary, I don't know what this byte is for
        data.append(STATUS_OK);

        //Another unknown byte that is always 1
        data.append(1);

        //The joystick data comes next
        for (int i = 0; i < joystickCount; i++) {
            byte[] oneJoy = makeJoystickData(i);
            data.append(oneJoy, 0, oneJoy.length);
        }


        packetIndex++;

        return data.toByteArray();
    }

    //Generate and return a connection packet
    public byte[] getConnectionPacket() {
        ByteArrayBuffer data = new ByteArrayBuffer(500);
        byte[] generalData = getPacket();
        byte[] connectionData = makeConnectionData();

        data.append(generalData, 0, generalData.length);
        data.append(connectionData, 0, connectionData.length);

        return data.toByteArray();
    }

    private byte[] makeConnectionData() {

        Calendar calendar = Calendar.getInstance();

        byte year = (byte) calendar.get(Calendar.YEAR);             //Years from 1900
        byte month = (byte) calendar.get(Calendar.MONTH);           //Month of year, beginning with Feb as 0
        byte day = (byte) calendar.get(Calendar.DAY_OF_MONTH);      //Day of month
        byte hour = (byte) calendar.get(Calendar.HOUR_OF_DAY);      //hour of day
        byte minute = (byte) calendar.get(Calendar.MINUTE);          //minute of hour
        byte second = (byte) calendar.get(Calendar.SECOND);          //Second of minute
        byte othertime1 = 0;                                        //Other time units, not sure of the format; hopefully
        byte othertime2 = 0;                                                //0 will work
        byte othertime3 = 0;

        byte[] data = new byte[]{
                11,         //Size of time data in bytes
                15,         //First digit of DS version
                0,          //Second digit of DS version
                othertime1,
                othertime2,
                othertime3,
                second,
                minute,
                hour,
                day,
                month,
                year,
                8,      //Not sure what these last two are, but they were always these values under tested conditions.
                16,
                'M', 'S', 'T', '7', 'M', 'D', 'T'     //Timezone

        };

        return data;
    }

    //These are offsets from the beginning of the joystick's data
    private static final int JOY_POS_BYTE_COUNT = 0x0,
            JOY_POS_UNKNOWN1 = 0x1,
            JOY_POS_AXIS_COUNT = 0x2,
            JOY_POS_AXIS_1 = 0x3;

    //These are offsets from the last axis position, can't use absolute position
    // because it can change with the number of axis'
    private final int AXIS_OFFSET_BUTTON_COUNT = 1, AXIS_OFFSET_BUTTON_START = 2;

    private final int BUTTON_OFFSET_UNKNOWN2 = 1, BUTTON_OFFSET_UNKNOWN3 = 2, BUTTON_OFFSET_UNKNOWN4 = 3;

    private final int MASK_BUTTON_1 = 1,
            MASK_BUTTON_2 = 2,
            MASK_BUTTON_3 = 4,
            MASK_BUTTON_4 = 8,
            MASK_BUTTON_5 = 16,
            MASK_BUTTON_6 = 32,
            MASK_BUTTON_7 = 64,
            MASK_BUTTON_8 = 128;


    private byte[] makeJoystickData(int joyNum) {

        int axisCount = 0;
        int buttonCount = 0;

        for (Axis a : axisList) {
            if (a.getJoystickNumber() == joyNum)
                axisCount = Math.max(axisCount, a.getAxisNumber() + 1);     //Max of Axis Number + 1 because axis indexing
        }                                                                       //begins at 0

        for (ButtonSymbol b : buttonList) {
            if (b.getJoyNum() == joyNum)
                buttonCount = Math.max(buttonCount, b.getButtonNum() + 1);  //+1 because button indexing also begins at 0
        }

        int buttonByteCount = ((buttonCount % 8) == 0) ? (buttonCount / 8) : (buttonCount / 8 + 1);

        // 7 is the number of joystick and button independent bytes that are in the data set
        int byteCount = 7 + axisCount + buttonByteCount;

        byte[] data = new byte[byteCount];

        data[JOY_POS_BYTE_COUNT] = (byte) (byteCount - 1);   //-1 to exclude the byteCount byte

        //This is another unknown byte, which seems to be 0x0c at all times
        data[JOY_POS_UNKNOWN1] = (byte) 0x0c;

        data[JOY_POS_AXIS_COUNT] = (byte) axisCount;

        for (int i = 0; i < axisCount; i++) {
            data[JOY_POS_AXIS_1 + i] = (byte) getAxisValue(joyNum, i);
        }

        data[JOY_POS_AXIS_1 + axisCount - 1 + AXIS_OFFSET_BUTTON_COUNT] = (byte) buttonCount;

        int buttonStart = JOY_POS_AXIS_1 + axisCount - 1 + AXIS_OFFSET_BUTTON_START;

        for (int i = 0; i < buttonByteCount; i++) {

            byte buttonByte = 0;
            for (int k = 0; k < 8; k++) {

                boolean pressed = getButtonState(joyNum, k + i * 8);
                if (pressed)
                    buttonByte |= (byte) Math.pow(2, k);
            }

            data[buttonStart - i] = buttonByte;
        }

        //These are also unknown, but they always have the given values
        if (buttonCount == 0) {
            data[buttonStart + BUTTON_OFFSET_UNKNOWN2 - 1] = 0x1;
            data[buttonStart + BUTTON_OFFSET_UNKNOWN3 - 1] = (byte) 0xff;
            data[buttonStart + BUTTON_OFFSET_UNKNOWN4 - 1] = (byte) 0xff;
        } else {
            data[buttonStart + BUTTON_OFFSET_UNKNOWN2] = 0x1;
            data[buttonStart + BUTTON_OFFSET_UNKNOWN3] = (byte) 0xff;
            data[buttonStart + BUTTON_OFFSET_UNKNOWN4] = (byte) 0xff;
        }


        return data;
    }


    private int getAxisValue(int joyNum, int axisNum) {

        for (Axis a : axisList) {
            if (a.getJoystickNumber() == joyNum && a.getAxisNumber() == axisNum)
                return a.get();
        }

        return 0;
    }

    private boolean getButtonState(int joyNum, int buttonNum) {

        for (ButtonSymbol b : buttonList) {
            if (b.getJoyNum() == joyNum && b.getButtonNum() == buttonNum)
                return b.isPressed();
        }

        return false;
    }

    private short[] intToShort(int i, boolean bigEndian) {

        short high = (short) ((i & 0xffff0000) >> 16);
        short low = (short) (i & 0x0000ffff);

        if (bigEndian)
            return new short[]{high, low};
        else
            return new short[]{low, high};
    }

    private byte[] shortToByte(short s, boolean bigEndian) {

        byte high = (byte) ((s & 0xff00) >> 8);
        byte low = (byte) (s & 0x00ff);

        if (bigEndian)
            return new byte[]{high, low};
        else
            return new byte[]{low, high};
    }

    private byte[] intToByte(int i, boolean bigEndian) {

        short[] s = intToShort(i, bigEndian);

        byte[] low = shortToByte(s[0], bigEndian);
        byte[] high = shortToByte(s[1], bigEndian);

        if (bigEndian)
            return new byte[]{high[1], high[0], low[1], low[0]};
        else
            return new byte[]{low[0], low[1], high[0], high[1]};
    }


    private class ButtonSymbol {
        private int joyNum;
        private int buttonNum;
        private boolean pressed;

        public ButtonSymbol(int joy, int button) {
            joyNum = joy;
            buttonNum = button;
        }

        public int getJoyNum() {
            return joyNum;
        }

        public int getButtonNum() {
            return buttonNum;
        }

        public boolean isPressed() {
            return pressed;
        }

        public void setPressed(boolean pressed) {
            this.pressed = pressed;
        }
    }

}
