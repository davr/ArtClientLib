package net.dhleong.acl.protocol.core.setup;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;

import net.dhleong.acl.enums.ConnectionType;
import net.dhleong.acl.iface.BaseDebugger;
import net.dhleong.acl.iface.Debugger;
import net.dhleong.acl.iface.OutputStreamDebugger;
import net.dhleong.acl.iface.PacketReader;
import net.dhleong.acl.iface.PacketWriter;
import net.dhleong.acl.protocol.ArtemisPacket;
import net.dhleong.acl.protocol.ArtemisPacketException;
import net.dhleong.acl.protocol.TestPacketFile;

/**
 * Abstract class that can be extended for testing individual packet types.
 */
public abstract class AbstractPacketTester<T extends ArtemisPacket> {
	// Are we running in debug mode?
	private static final boolean DEBUG = ManagementFactory.getRuntimeMXBean().getInputArguments().toString().indexOf("jdwp") >= 0;

	protected static final float EPSILON = 0.00000001f; // for float equality checks

	/**
	 * Invoked by AbstractPacketTester when it has successfully parsed the
	 * desired number of packets with no bytes left over. The resulting packets
	 * are passed in; subclasses should evaluate them to ensure that they
	 * contain the expected data and throw an assert if not.
	 */
	protected abstract void testPackets(List<T> packets);

	private Debugger debugger = DEBUG ? new OutputStreamDebugger() : new BaseDebugger();

	/**
	 * Loads the test packet file at the indicated path and reads the given
	 * number of packets from it. They are then passed to testPackets();
	 * subclasses will override this to perform type-specific tests for those
	 * packets. Finally, the packets will be written out to a stream, and the
	 * resulting bytes compared to the original file.
	 */
	protected void execute(String resourcePath, ConnectionType type, int packetCount) {
		try {
			// Load test packet file
			URL url = TestPacketFile.class.getResource(resourcePath);
			TestPacketFile file = new TestPacketFile(url);

			if (DEBUG) {
				System.out.println("\n### " + resourcePath);
			}

			// Parse the desired number of packets
			PacketReader reader = file.toPacketReader(type);
			List<T> list = new ArrayList<T>(packetCount);
	
			for (int i = 0; i < packetCount; i++) {
				T pkt = (T) reader.readPacket(debugger);
				Assert.assertNotNull(pkt);
				list.add(pkt);
				Assert.assertFalse(reader.hasMore()); // Any bytes left over?
			}

			// Delegate to subclass for type-specific tests
			testPackets(list);

			// Write packets back out
			if (DEBUG) {
				System.out.println("Writing packets...");
			}

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PacketWriter writer = new PacketWriter(baos);
	
			for (T pkt : list) {
				pkt.writeTo(writer, debugger);
			}

			// Compare written bytes to originals
			Assert.assertTrue(file.matches(baos));

			if (DEBUG) {
				System.out.println("Input and output bytes match");
			}
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		} catch (ArtemisPacketException ex) {
			throw new RuntimeException(ex);
		}
	}
}