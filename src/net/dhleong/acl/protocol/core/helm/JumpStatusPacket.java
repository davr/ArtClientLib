package net.dhleong.acl.protocol.core.helm;

import net.dhleong.acl.enums.ConnectionType;
import net.dhleong.acl.iface.PacketFactory;
import net.dhleong.acl.iface.PacketFactoryRegistry;
import net.dhleong.acl.iface.PacketReader;
import net.dhleong.acl.iface.PacketWriter;
import net.dhleong.acl.protocol.ArtemisPacket;
import net.dhleong.acl.protocol.ArtemisPacketException;
import net.dhleong.acl.protocol.BaseArtemisPacket;

/**
 * Indicates that a jump has begun or ended.
 * @author dhleong
 */
public class JumpStatusPacket extends BaseArtemisPacket {
    private static final int TYPE = 0xf754c8fe;

	public static void register(PacketFactoryRegistry registry) {
		PacketFactory factory = new PacketFactory() {
			@Override
			public Class<? extends ArtemisPacket> getFactoryClass() {
				return JumpStatusPacket.class;
			}

			@Override
			public ArtemisPacket build(PacketReader reader)
					throws ArtemisPacketException {
				return new JumpStatusPacket(reader);
			}
		};
		registry.register(ConnectionType.SERVER, TYPE, MSG_TYPE_BEGIN, factory);
		registry.register(ConnectionType.SERVER, TYPE, MSG_TYPE_END, factory);
	}

    /**
     * Jump "begin"; that is, the countdown has begun
     */
    public static final byte MSG_TYPE_BEGIN = 0x0c;

    /**
     * Jump "end"; there's still some cooldown (~5 seconds)
     */
    public static final byte MSG_TYPE_END = 0x0d;

    private final boolean begin;

    private JumpStatusPacket(PacketReader reader) throws ArtemisPacketException {
        super(ConnectionType.SERVER, TYPE);
        int subtype = reader.readInt();

        if (subtype == MSG_TYPE_BEGIN) {
        	begin = true;
        } else if (subtype == MSG_TYPE_END) {
        	begin = false;
        } else {
        	throw new ArtemisPacketException(
        			"Expected subtype " + MSG_TYPE_BEGIN + " or " +
        			MSG_TYPE_END + ", got " + subtype
        	);
        }
    }

    public JumpStatusPacket(boolean countingDown) {
        super(ConnectionType.SERVER, TYPE);
    	begin = countingDown;
    }

    /**
     * Returns true if the jump is starting (countdown has begun); false if the
     * jump has ended.
     * @return
     */
    public boolean isCountdown() {
        return begin;
    }

	@Override
	protected void writePayload(PacketWriter writer) {
		writer.writeInt(begin ? MSG_TYPE_BEGIN : MSG_TYPE_END);
	}

	@Override
	protected void appendPacketDetail(StringBuilder b) {
		b.append(begin ? "begin" : "end");
	}
}