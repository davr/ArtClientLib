package net.dhleong.acl.protocol.core.world;

import net.dhleong.acl.enums.ObjectType;
import net.dhleong.acl.enums.Upgrade;
import net.dhleong.acl.iface.PacketReader;
import net.dhleong.acl.iface.PacketWriter;
import net.dhleong.acl.world.ArtemisAnomaly;
import net.dhleong.acl.world.ArtemisObject;

public class AnomalyParser extends AbstractObjectParser {
	private enum Bit {
		X,
		Y,
		Z,
		UPGRADE
	}
	private static final Bit[] BITS = Bit.values();

	AnomalyParser() {
		super(ObjectType.ANOMALY);
	}

	@Override
	public Bit[] getBits() {
		return BITS;
	}

	@Override
	protected ArtemisAnomaly parseImpl(PacketReader reader) {
        ArtemisAnomaly anomaly = new ArtemisAnomaly(reader.getObjectId());
        anomaly.setX(reader.readFloat(Bit.X, Float.MIN_VALUE));
        anomaly.setY(reader.readFloat(Bit.Y, Float.MIN_VALUE));
        anomaly.setZ(reader.readFloat(Bit.Z, Float.MIN_VALUE));

        if (reader.has(Bit.UPGRADE)) {
        	anomaly.setUpgrade(Upgrade.values()[reader.readInt()]);
        }

        return anomaly;
	}

	@Override
	public void write(ArtemisObject obj, PacketWriter writer) {
		ArtemisAnomaly anomaly = (ArtemisAnomaly) obj;
		writer.writeFloat(Bit.X, anomaly.getX(), Float.MIN_VALUE);
		writer.writeFloat(Bit.Y, anomaly.getY(), Float.MIN_VALUE);
		writer.writeFloat(Bit.Z, anomaly.getZ(), Float.MIN_VALUE);

		Upgrade upgrade = anomaly.getUpgrade();

		if (upgrade != null) {
			writer.writeInt(Bit.UPGRADE, upgrade.ordinal(), -1);
		}
	}
}