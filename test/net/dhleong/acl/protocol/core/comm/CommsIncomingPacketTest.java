package net.dhleong.acl.protocol.core.comm;

import java.util.List;

import org.junit.Assert;

import org.junit.Test;

import net.dhleong.acl.enums.ConnectionType;
import net.dhleong.acl.protocol.core.setup.AbstractPacketTester;

public class CommsIncomingPacketTest extends AbstractPacketTester<CommsIncomingPacket> {
	@Test
	public void test() {
		execute("core/comm/CommsIncomingPacket.txt", ConnectionType.SERVER, 1);
	}

	@Override
	protected void testPackets(List<CommsIncomingPacket> packets) {
		CommsIncomingPacket pkt = packets.get(0);
		Assert.assertEquals(0, pkt.getPriority());
		Assert.assertEquals("DS1", pkt.getFrom());
		Assert.assertEquals("Hi.\nHow are you?", pkt.getMessage());
	}
}