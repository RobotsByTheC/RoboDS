package littlebot.robods.communication;

/**
 * Completely useless, this class was added for future expansion
 */
public class RobotPacket extends FRCPacket {

    public static final int MAX_LENGTH = 51;


    /**
     * Creates a new packet with the specified body length.
     */
    public RobotPacket() {
        super(0);
    }

    @Override
    public void updateBody(byte[] data, int offset, int length) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }
}
