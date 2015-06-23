package net.dhleong.acl.protocol;

import java.io.IOException;

import net.dhleong.acl.enums.ConnectionType;
import net.dhleong.acl.iface.Debugger;
import net.dhleong.acl.iface.PacketWriter;

/**
 * Implements common packet functionality.
 */
public abstract class BaseArtemisPacket implements ArtemisPacket {
	/**
	 * Causes the packet's payload to be written to the given PacketWriter.
	 */
    protected abstract void writePayload(PacketWriter writer);

    /**
     * Writes packet type-specific details (debug info) to be written to the
     * given StringBuilder.
     */
    protected abstract void appendPacketDetail(StringBuilder b);

    private final ConnectionType mConnectionType;
    private final int mType;

    /**
     * @param connectionType The packet's ConnectionType
     * @param packetType The packet's type value
     */
    public BaseArtemisPacket(ConnectionType connectionType, int packetType) {
        mConnectionType = connectionType;
        mType = packetType;
    }

    @Override
    public ConnectionType getConnectionType() {
        return mConnectionType;
    }

    @Override
    public int getType() {
        return mType;
    }

    @Override
    public final void writeTo(PacketWriter writer, Debugger debugger) throws IOException {
    	writer.start(mConnectionType, mType);
    	writePayload(writer);
    	writer.flush(debugger);
    }

    @Override
    public final String toString() {
    	StringBuilder b = new StringBuilder();
    	b.append('[').append(getClass().getSimpleName()).append("] ");
    	appendPacketDetail(b);
    	return b.toString();
    }
}