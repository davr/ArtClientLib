package net.dhleong.acl.protocol.core.comm;

import net.dhleong.acl.enums.ConnectionType;
import net.dhleong.acl.iface.PacketFactory;
import net.dhleong.acl.iface.PacketFactoryRegistry;
import net.dhleong.acl.iface.PacketReader;
import net.dhleong.acl.iface.PacketWriter;
import net.dhleong.acl.protocol.ArtemisPacket;
import net.dhleong.acl.protocol.ArtemisPacketException;
import net.dhleong.acl.protocol.BaseArtemisPacket;

/**
 * Received when an incoming COMMs message arrives.
 */
public class CommsIncomingPacket extends BaseArtemisPacket {
    private static final int TYPE = 0xD672C35F;

	public static void register(PacketFactoryRegistry registry) {
		registry.register(ConnectionType.SERVER, TYPE, new PacketFactory() {
			@Override
			public Class<? extends ArtemisPacket> getFactoryClass() {
				return CommsIncomingPacket.class;
			}

			@Override
			public ArtemisPacket build(PacketReader reader)
					throws ArtemisPacketException {
				return new CommsIncomingPacket(reader);
			}
		});
	}

    private final int mPriority;
    private final String mFrom;
    private final String mMessage;

    private CommsIncomingPacket(PacketReader reader) {
        super(ConnectionType.SERVER, TYPE);
        mPriority = reader.readInt();
        mFrom = reader.readString();
        mMessage = reader.readString().replace('^', '\n');
    }

    public CommsIncomingPacket(int priority, String from, String message) {
    	super(ConnectionType.SERVER, TYPE);

    	if (priority < 0 || priority > 8) {
    		throw new IllegalArgumentException("Invalid priority: " + priority);
    	}

    	if (from == null) {
    		throw new IllegalArgumentException("You must provide a sender name");
    	}

    	if (message == null) {
    		throw new IllegalArgumentException("You must provide a message");
    	}

    	mPriority = priority;
    	mFrom = from;
    	mMessage = message;
    }

    /**
     * Returns the message priority, with lower values having higher priority.
     * @return An integer between 0 and 8, inclusive
     */
    public int getPriority() {
        return mPriority;
    }

    /**
     * A String identifying the sender. This may not correspond to the name of
     * a game entity. For example, some messages from stations or friendly ships
     * have additional detail after the entity's name ("DS3 TSN Base"). Messages
     * in scripted scenarios can have any String for the sender.
     */
    public String getFrom() {
        return mFrom;
    }

    /**
     * The content of the message.
     */
    public String getMessage() {
        return mMessage;
    }

	@Override
	protected void writePayload(PacketWriter writer) {
		writer.writeInt(mPriority);
		writer.writeString(mFrom);
		writer.writeString(mMessage.replace('\n', '^'));
	}

	@Override
	protected void appendPacketDetail(StringBuilder b) {
		b.append("from ").append(mFrom).append(": ").append(mMessage);
	}
}