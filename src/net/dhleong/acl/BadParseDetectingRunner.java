package net.dhleong.acl;

import java.io.IOException;
import java.net.UnknownHostException;

import net.dhleong.acl.enums.BeamFrequency;
import net.dhleong.acl.enums.BridgeStation;
import net.dhleong.acl.enums.OrdnanceType;
import net.dhleong.acl.enums.ShipSystem;
import net.dhleong.acl.net.ObjectUpdatingPacket;
import net.dhleong.acl.net.player.PlayerUpdatePacket;
import net.dhleong.acl.net.setup.ReadyPacket;
import net.dhleong.acl.net.setup.ReadyPacket2;
import net.dhleong.acl.net.setup.SetStationPacket;
import net.dhleong.acl.world.ArtemisPlayer;
import net.dhleong.acl.world.ArtemisObject;
import net.dhleong.acl.world.BaseArtemisShip;

/**
 * Connects to a server and listens for
 *  packets that were incorrectly parsed,
 *  stopping when it finds one
 *  
 * @author dhleong
 *
 */
public class BadParseDetectingRunner {

    public static void main(final String[] args) {
                final String tgtIp = "10.211.55.4";
//        final String tgtIp = "192.168.1.30";
        final int tgtPort = 2010;

        final ThreadedArtemisNetworkInterface net; 
        try {
            net = new ThreadedArtemisNetworkInterface(tgtIp, tgtPort);
        } catch (final UnknownHostException e) {
            e.printStackTrace();
            return;
        } catch (final IOException e) {
            e.printStackTrace();
            return;
        }

        final SystemManager mgr = new SystemManager();
        net.addPacketListener(mgr);
        net.setOnConnectedListener(new OnConnectedListener() {

            @Override
            public void onConnected() {
                System.out.println("Connected to " + tgtIp);
            }

            @Override
            public void onDisconnected(final int errorCode) {
                System.out.println("Disconnected: " + errorCode);
            }
        });

        net.addPacketListener(new Object() {
            @PacketListener
            public void onPacket(final ArtemisPacket pkt) {
                if (pkt instanceof PlayerUpdatePacket) {
                    final PlayerUpdatePacket up = (PlayerUpdatePacket) pkt;

                    try {
                        testPlayer(up.getPlayer());
                    } catch (final RuntimeException e) {
                        System.out.println("--> " + up);
                        net.stop();
                        throw e;
                    }
                } else if (pkt instanceof ObjectUpdatingPacket) {
                    final ObjectUpdatingPacket up = (ObjectUpdatingPacket) pkt;
                    try {

                        for (final ArtemisObject p : up.getObjects()) {
                            if (p instanceof BaseArtemisShip)
                                testShip((BaseArtemisShip)p);
                            else
                                testObject(p);
                        }

                    } catch (final RuntimeException e) {
                        System.out.println("--> " + up);
                        net.stop();
                        throw e;
                    }
                }
            }

        });

        net.start();

        net.send(new ReadyPacket2());
        net.send(new ReadyPacket2());
        net.send(new SetStationPacket(BridgeStation.SCIENCE, true));
        net.send(new ReadyPacket());
        net.send(new ReadyPacket2());
    }

    public static void testPlayer(final ArtemisPlayer p) {
        testShip(p);

        assertRange(-1, 5000, p.getEnergy(), "energy");
        assertRange(-1, 6, p.getShipIndex(), "shipIndex");
        assertRange(-1, 32, p.getAvailableCoolant(), "maxCoolant");

        for (final ShipSystem sys : ShipSystem.values()) {
            if (p.getSystemEnergy(sys) != -1)
                assertRange(0, 1, p.getSystemEnergy(sys), sys + "energy");
            assertRange(-1, 1, p.getSystemHeat(sys), sys + "heat");
            assertRange(-1, 16, p.getSystemCoolant(sys), sys + "coolant");
        }

        for (OrdnanceType type : OrdnanceType.values()) {
            assertRange(-1, 99, p.getTorpedoCount(type), type.toString());
        }
    }

    public static void testShip(final BaseArtemisShip p) {
        testObject(p);

        assertRange(-1, 10000, p.getHullId(), "hullId");

        if (p.getHeading() != Float.MIN_VALUE)
            assertRange(-4, 4, p.getHeading(), "heading");

        // I guess they can go negative when destroyed...?
        assertRange(-50, 1000, p.getShieldsFrontMax(), "shieldFrontMax");
        assertRange(-50, 1000, p.getShieldsRearMax(), "shieldRearMax");
        assertNotEqual(0, p.getShieldsFrontMax(), "shieldFrontMax");
        assertNotEqual(0, p.getShieldsRearMax(), "shieldRearMax");
        assertRange(-50, 1000, p.getShieldsFront(), "shieldFront");
        assertRange(-50, 1000, p.getShieldsRear(), "shieldRear");
        
        for (BeamFrequency freq : BeamFrequency.values()) {
            assertRange(-1, 1f, p.getShieldFreq(freq), "shieldFreq(" + freq + ")");
        }
    }

    private static void assertNotEqual(final float expected, final float actual, final String label) {
        if (Math.abs(expected - actual) < 0.001)
            throw new RuntimeException(
                    String.format("Value ``%s'' is illegal value (%f)", 
                            label, expected));
    }

    public static void testObject(final ArtemisObject p) {
        assertRange(-1, 100020, p.getX(), "x");
        assertRange(-300, 300, p.getY(), "y");
        assertRange(-1, 100020, p.getZ(), "z");
    }

    private static void assertRange(final float low, final float high, final float value, final String label) {
        if (value < low || value > high) {
            throw new RuntimeException(
                    String.format("Value ``%s'' (%f) out of range [%f,%f]",
                            label, value, low, high));
        }
    }
}
