package littlebot.robods.communication;

import java.net.DatagramPacket;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Provides methods for interacting with common elements of both packet types.
 * Also provides the ability to set up a {@link DatagramPacket} for
 * transmission.
 *
 * @author Ben Wolsieffer
 */
public abstract class FRCPacket {

    public static abstract class Section {

        public static int HEADER_LENGTH = 2;

        /**
         * Used to adjust FRCPacket#sectionsLength correctly after the data
         * length has been updated.
         */
        private int oldLength;
        /**
         * Flag that is set when the length has been changed. Cleared after
         * {@link FRCPacket#toDatagramPacket(DatagramPacket)} is called.
         */
        private boolean lengthChanged;
        /**
         * The total length of this section. This includes the length and
         * section type bytes.
         */
        private int length = 0;
        /**
         * The type of this section.
         */
        private final byte type;
        /**
         * Whether this section should continue to be part of the packet through
         * multiple calls of {@link FRCPacket#toDatagramPacket(DatagramPacket)}.
         * If false, the section is removed after it has been sent once.
         */
        private final boolean persistent;

        /**
         * Creates a new section with the specified data length, type and
         * persistence value.
         *
         * @param dataLength the length of the data (not including length or
         * type bytes)
         * @param type the type identifier for this section
         * @param persistent whether the section should persist over multiple
         * packet update cycles or only one
         */
        public Section(int dataLength, int type, boolean persistent) {
            setDataLength(dataLength);
            this.type = (byte) type;
            this.persistent = persistent;
        }

        /**
         * Sets the length of the data in this section. This does not effect the
         * data array until the next time the packet is updated.
         *
         * @param dataLength the length of the data (not including length or
         * type bytes)
         */
        public void setDataLength(int dataLength) {
            // Must be synchronized to prevent changes while the packet is
            // being updated. The other synchronized block is located in
            // FRCPacket.toDatagramPacket(DatagramPacket).
            synchronized (this) {
                // The total length includes the length and type bytes
                int newLength = dataLength + HEADER_LENGTH;
                if (newLength != length) {
                    if(!lengthChanged) {
                        // Save the old length for correcting FRCPacket.sectionsLength
                        oldLength = length;
                    }
                    length = newLength;
                    // Tell FRCPacket.toDatagramPacket(DatagramPacket) that the
                    // length has been changed
                    lengthChanged = true;
                }
            }
        }

        /**
         * Gets the length of the data in this section.
         *
         * @return the length of the data
         */
        public int getDataLength() {
            synchronized (this) {
                return length - HEADER_LENGTH;
            }
        }

        /**
         * Writes the entire section to the specified array, starting at its
         * offset.
         *
         * @param data the data array to update
         */
        private void update(int offset, byte[] data) {
            data[offset++] = (byte) (length - 1);
            data[offset++] = type;
            updateData(data, offset, getDataLength());
        }

        /**
         * Update the data of this section. Subclasses must update every byte of
         * their section of the array. It should be expected that this method
         * will be called from a different thread than any of the packet data
         * manipulation methods.
         *
         * @param data the array to write to
         * @param offset the start of the data in the array
         * @param length the length of the data
         */
        public abstract void updateData(byte[] data, int offset, int length);
    }

    public static final int INDEX_LENGTH = 2;

    /**
     * The length of the body of the packet. This does not include the packet
     * index.
     */
    private final int bodyLength;

    private byte[] data;
    private int index = 1;
    private int sectionsLength;
    private final LinkedList<Section> sections = new LinkedList<>();

    /**
     * Creates a new packet with the specified body length.
     *
     * @param bodyLength the length of the body.
     */
    public FRCPacket(int bodyLength) {
        this.bodyLength = bodyLength;
        // 2 extra bytes for the packet index
        data = new byte[bodyLength + INDEX_LENGTH];
    }

    /**
     * Update the data of the body of the packet. Subclasses must update every
     * byte of their section of the array. It should be expected that this
     * method will be called from a different thread than any of the packet data
     * manipulation methods.
     *
     * @param data the array to write to
     * @param offset the start of the data in the array
     * @param length the length of the data
     */
    public abstract void updateBody(byte[] data, int offset, int length);

    /**
     * Adds the specified section to the end of the packet.
     *
     * @param section the section to add
     */
    protected void addSection(Section section) {
        addSection(sections.size(), section);
    }

    /**
     * Adds the specified section to the packet.
     *
     * @param index the index to add the section at
     * @param section the section to add \
     */
    protected void addSection(int index, Section section) {
        synchronized (sections) {
            sections.add(index, section);
            ensureDataLength();
        }
    }

    protected Section removeSection(int index) {
        synchronized (sections) {
            Section removedSection = sections.remove(index);
            postRemoveSection(removedSection);
            return removedSection;
        }
    }

    protected void removeSection(Section section) {
        synchronized (sections) {
            // LinkedList could be slow for this operation, so it only called
            // when there is no other way
            sections.remove(section);
            postRemoveSection(section);
        }
    }

    /**
     * Updates lengths and offsets after a section was removed.
     *
     * @param section the section that was removed
     */
    private void postRemoveSection(Section section) {
        synchronized (sections) {
            sectionsLength -= section.length;
        }
    }

    /**
     * Makes sure the data array is long enough to hold all of the packet. If
     * not, a new (empty) array is created that is the right size.
     */
    private void ensureDataLength() {
        synchronized (sections) {
            int totalLength = getLength();
            if (totalLength > data.length) {
                // Old data is not copied because update() methods are required
                // to write every byte every time they are called
                data = new byte[totalLength];
            }
        }
    }

    /**
     * Updates the packet's body and all its sections, and then sets the
     * provided {@link DatagramPacket}'s data array. This should be called every
     * time the packet is about to be sent.
     *
     * @param packet the packet to use with this data
     */
    public synchronized void toDatagramPacket(DatagramPacket packet) {
        int offset = 0;

        // Index is common to both types of packets
        data[offset++] = (byte) (++index >> 8);
        data[offset++] = (byte) index;

        // Update the body of the packet.
        updateBody(data, offset, bodyLength);
        offset += bodyLength;

        // Nobody can touch the sections while they are being updated
        synchronized (sections) {
            for (Section section : sections) {
                // Lock on the section to prevent another thread from changing
                // the length while updating
                synchronized (section) {
                    // If the length has changed, update accordingly
                    if (section.lengthChanged) {
                        // Update sectionsLength
                        sectionsLength += section.length - section.oldLength;
                        // Make sure the array is big enough
                        ensureDataLength();
                        // Reset the flag
                        section.lengthChanged = false;
                    }
                    // Update section
                    section.update(offset, data);
                    offset += section.length;
                }
            }
            // Set DatagramPacket data array
            packet.setData(data, 0, getLength());
            // Remove all non-persistent sections
            for (Iterator<Section> iter = sections.iterator(); iter.hasNext(); ) {
                Section section = iter.next();
                if (!section.persistent) {
                    iter.remove();
                    postRemoveSection(section);
                }
            }
        }
    }

    /**
     * Get the total length of the packet.
     *
     * @return the packet length
     */
    public int getLength() {
        // Total length is equal to the size of the index (2 bytes) + size
        // of body + size of sections
        return INDEX_LENGTH + bodyLength + sectionsLength;
    }
}