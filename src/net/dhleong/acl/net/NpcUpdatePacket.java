package net.dhleong.acl.net;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.dhleong.acl.enums.BeamFrequency;
import net.dhleong.acl.enums.ConnectionType;
import net.dhleong.acl.enums.ShipSystem;
import net.dhleong.acl.util.BoolState;
import net.dhleong.acl.world.ArtemisNpc;
import net.dhleong.acl.world.ArtemisObject;

/**
 * Updates on enemy and allied ships.
 */
public class NpcUpdatePacket extends BaseArtemisPacket implements ObjectUpdatingPacket {
	private enum Bit {
		NAME,
		UNK_1_2,
		UNK_1_3,
		MAX_IMPULSE,
		MAX_TURN_RATE,
		IS_ENEMY,
		SHIP_TYPE,
		X,

		Y,
		Z,
		PITCH,
		ROLL,
		HEADING,
		VELOCITY,
		UNK_2_7,
		UNK_2_8,

		FORE_SHIELD,
		FORE_SHIELD_MAX,
		AFT_SHIELD,
		AFT_SHIELD_MAX,
		UNK_3_5,
		UNK_3_6,
		ELITE_ABILITIES,
		ELITE_STATE,

		UNK_4_1,
		UNK_4_2,
		UNK_4_3,
		UNK_4_4,
		UNK_4_5,
		UNK_4_6,
		UNK_4_7,
		UNK_4_8,

		UNK_5_1,
		UNK_5_2,
		BEAM_SYSTEM_DAMAGE,
		TORPEDO_SYSTEM_DAMAGE,
		SENSOR_SYSTEM_DAMAGE,
		MANEUVER_SYSTEM_DAMAGE,
		IMPULSE_SYSTEM_DAMAGE,
		WARP_SYSTEM_DAMAGE,

		FORE_SHIELD_SYSTEM_DAMAGE,
		AFT_SHIELD_SYSTEM_DAMAGE,
		SHIELD_FREQUENCY_A,
		SHIELD_FREQUENCY_B,
		SHIELD_FREQUENCY_C,
		SHIELD_FREQUENCY_D,
		SHIELD_FREQUENCY_E
	}

	private static final Bit[] SYSTEM_DAMAGES = new Bit[] {
		Bit.BEAM_SYSTEM_DAMAGE,
		Bit.TORPEDO_SYSTEM_DAMAGE,
		Bit.SENSOR_SYSTEM_DAMAGE,
		Bit.MANEUVER_SYSTEM_DAMAGE,
		Bit.IMPULSE_SYSTEM_DAMAGE,
		Bit.WARP_SYSTEM_DAMAGE,
		Bit.FORE_SHIELD_SYSTEM_DAMAGE,
		Bit.AFT_SHIELD_SYSTEM_DAMAGE
	};

	private static final Bit[] SHLD_FREQS = new Bit[] {
		Bit.SHIELD_FREQUENCY_A,
		Bit.SHIELD_FREQUENCY_B,
		Bit.SHIELD_FREQUENCY_C,
		Bit.SHIELD_FREQUENCY_D,
		Bit.SHIELD_FREQUENCY_E
    };

    private final List<ArtemisObject> mObjects = new ArrayList<ArtemisObject>();

    public NpcUpdatePacket(PacketReader reader) {
    	super(ConnectionType.SERVER, WORLD_TYPE);

    	while (reader.hasMore()) {
            float x, y, z, pitch, roll, heading, velocity, maxImpulse, maxTurnRate;
            float[] sysDamage = new float[SYSTEM_DAMAGES.length];
            float[] freqs = new float[SHLD_FREQS.length];
            int scanned = -1;
            String name = null;
            BoolState enemy;
            int hullId = -1;
            int elite = -1;
            int eliteState = -1;
            float shieldsFront, shieldsFrontMax;
            float shieldsRear, shieldsRearMax;

            reader.startObject(Bit.values());
            name = reader.readString(Bit.NAME);

            // no idea what these are
            reader.readObjectUnknown(Bit.UNK_1_2, 4);
            reader.readObjectUnknown(Bit.UNK_1_3, 4);

            maxImpulse = reader.readFloat(Bit.MAX_IMPULSE, -1);
            maxTurnRate = reader.readFloat(Bit.MAX_TURN_RATE, -1);

            if (reader.has(Bit.IS_ENEMY)) {
                enemy = BoolState.from(reader.readInt() == 1);
            } else {
            	enemy = BoolState.UNKNOWN;
            }

            hullId = reader.readInt(Bit.SHIP_TYPE, -1);
            x = reader.readFloat(Bit.X, Float.MIN_VALUE);
            y = reader.readFloat(Bit.Y, Float.MIN_VALUE);
            z = reader.readFloat(Bit.Z, Float.MIN_VALUE);
            pitch = reader.readFloat(Bit.PITCH, Float.MIN_VALUE);
            roll = reader.readFloat(Bit.ROLL, Float.MIN_VALUE);
            heading = reader.readFloat(Bit.HEADING, Float.MIN_VALUE);
            velocity = reader.readFloat(Bit.VELOCITY, -1);

            reader.readObjectUnknown(Bit.UNK_2_7, 1);
            reader.readObjectUnknown(Bit.UNK_2_8, 2);

            shieldsFront = reader.readFloat(Bit.FORE_SHIELD, Float.MIN_VALUE);
            shieldsFrontMax = reader.readFloat(Bit.FORE_SHIELD_MAX, -1);
            shieldsRear = reader.readFloat(Bit.AFT_SHIELD, Float.MIN_VALUE);
            shieldsRearMax = reader.readFloat(Bit.AFT_SHIELD_MAX, -1);

            reader.readObjectUnknown(Bit.UNK_3_5, 2);
            reader.readObjectUnknown(Bit.UNK_3_6, 1);

            elite = reader.readInt(Bit.ELITE_ABILITIES, -1);
            eliteState = reader.readInt(Bit.ELITE_STATE, -1);
            scanned = reader.readInt(Bit.UNK_4_1);

            reader.readObjectUnknown(Bit.UNK_4_2, 4);

            // TODO These must be system damages!
            reader.readObjectUnknown(Bit.UNK_4_3, 4);
            reader.readObjectUnknown(Bit.UNK_4_4, 1);
            reader.readObjectUnknown(Bit.UNK_4_5, 1);
            reader.readObjectUnknown(Bit.UNK_4_6, 1);
            reader.readObjectUnknown(Bit.UNK_4_7, 1);
            reader.readObjectUnknown(Bit.UNK_4_8, 4);
            reader.readObjectUnknown(Bit.UNK_5_1, 4);
            reader.readObjectUnknown(Bit.UNK_5_2, 4);

            // system damage
            for (int i = 0; i < SYSTEM_DAMAGES.length; i++) {
            	sysDamage[i] = reader.readFloat(SYSTEM_DAMAGES[i], -1);
            }

            // shield frequencies
            for (int i = 0; i < SHLD_FREQS.length; i++) {
                freqs[i] = reader.readFloat(SHLD_FREQS[i], -1);
            }

            ArtemisNpc obj = new ArtemisNpc(reader.getObjectId(), name, hullId);
            obj.setScanLevel((byte) scanned);
            obj.setEnemy(enemy);
            obj.setEliteBits(elite);
            obj.setEliteStateBits(eliteState);
            
            // shared updates
            obj.setX(x);
            obj.setY(y);
            obj.setZ(z);
            obj.setPitch(pitch);
            obj.setRoll(roll);
            obj.setHeading(heading);
            obj.setVelocity(velocity);
            obj.setTopSpeed(maxImpulse);
            obj.setTurnRate(maxTurnRate);
            
            obj.setShieldsFront(shieldsFront);
            obj.setShieldsFrontMax(shieldsFrontMax);
            obj.setShieldsRear(shieldsRear);
            obj.setShieldsRearMax(shieldsRearMax);

            for (ShipSystem sys : ShipSystem.values()) {
            	obj.setSystemDamage(sys, sysDamage[sys.ordinal()]);
            }

            for (BeamFrequency bf : BeamFrequency.values()) {
                obj.setShieldFreq(bf, freqs[bf.ordinal()]);
            }

            obj.setUnknownProps(reader.getUnknownObjectProps());
            mObjects.add(obj);
        }
    }

	@Override
	protected void appendPacketDetail(StringBuilder b) {
		for (ArtemisObject obj : mObjects) {
			b.append("\nObject ").append(obj.getId()).append(obj);
		}
	}

    @Override
    public void write(PacketWriter writer) throws IOException {
    	throw new UnsupportedOperationException(
    			getClass().getSimpleName() + " does not support write()"
    	);
    }

    @Override
    public List<ArtemisObject> getObjects() {
        return mObjects;
    }
}