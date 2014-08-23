package net.dhleong.acl.protocol.core;

import net.dhleong.acl.enums.ConnectionType;
import net.dhleong.acl.enums.Console;
import net.dhleong.acl.iface.PacketFactory;
import net.dhleong.acl.iface.PacketFactoryRegistry;
import net.dhleong.acl.iface.PacketReader;
import net.dhleong.acl.iface.PacketWriter;
import net.dhleong.acl.protocol.ArtemisPacket;
import net.dhleong.acl.protocol.ArtemisPacketException;
import net.dhleong.acl.protocol.BaseArtemisPacket;

/**
 * A packet sent by the game master console to the server which causes a message
 * to be displayed on a client.
 * @author rjwut
 */
public class GameMasterMessagePacket extends BaseArtemisPacket {
    private static final int TYPE = 0x809305a7;

	public static void register(PacketFactoryRegistry registry) {
		registry.register(ConnectionType.CLIENT, TYPE, new PacketFactory() {
			@Override
			public Class<? extends ArtemisPacket> getFactoryClass() {
				return GameMasterMessagePacket.class;
			}

			@Override
			public ArtemisPacket build(PacketReader reader)
					throws ArtemisPacketException {
				return new GameMasterMessagePacket(reader);
			}
		});
	}

    private final Console mConsole;
    private final String mSender;
    private final String mMessage;

    private GameMasterMessagePacket(PacketReader reader) {
        super(ConnectionType.CLIENT, TYPE);
        int console = reader.readInt();

        if (console != 0) {
        	console--;

        	if (console < 0 || console > Console.COMMUNICATIONS.ordinal()) {
        		throw new IllegalArgumentException("Invalid console value: " + console);
        	}

        	mConsole = Console.values()[console];
        } else {
        	mConsole = null;
        }

        mSender = reader.readString();
        mMessage = reader.readString();
    }

    /**
     * A message from the game master that will be received as a normal COMMs
     * message.
     * Convenience constructor for GameMasterMessage(String, String, null).
     */
    public GameMasterMessagePacket(String sender, String message) {
    	this(sender, message, null);
    }

    /**
     * A message from the game master that will be received by one of the
     * consoles. If the console argument is null, it will be recieved as a
     * normal COMMs message. Otherwise, it will be displayed as a popup on the
     * named Console. Only the six main console types (MAIN_SCREEN, HELM,
     * WEAPONS, ENGINEERING, SCIENCE, COMMUNICATIONS) are allowed.
     */
    public GameMasterMessagePacket(String sender, String message, Console console) {
        super(ConnectionType.CLIENT, TYPE);

        if (console.ordinal() > Console.COMMUNICATIONS.ordinal()) {
        	throw new IllegalArgumentException("Invalid console: " + console);
        }

        mSender = sender;
        mMessage = message;
        mConsole = console;
    }

    public String getSender() {
        return mSender;
    }

    public String getMessage() {
    	return mMessage;
    }

    public Console getConsole() {
    	return mConsole;
    }

    @Override
	protected void writePayload(PacketWriter writer) {
		writer.writeInt(mConsole == null ? 0 : mConsole.ordinal() - 1);
	}

	@Override
	protected void appendPacketDetail(StringBuilder b) {
		if (mConsole == null) {
			b.append(" [COMMs message] ");
		} else {
			b.append(" [").append(mConsole).append(" popup] ");
		}

		b.append(mSender).append(": ").append(mMessage);
	}
}