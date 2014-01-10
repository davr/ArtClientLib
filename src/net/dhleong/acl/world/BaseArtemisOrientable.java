package net.dhleong.acl.world;

import java.util.SortedMap;

/**
 * Base implementation for ArtemisOrientable objects.
 * @author rjwut
 */
public abstract class BaseArtemisOrientable extends BaseArtemisObject
		implements ArtemisOrientable {
    public BaseArtemisOrientable(int objId, String name) {
		super(objId, name);
	}

	private float mHeading = Float.MIN_VALUE;
    private float mPitch = Float.MIN_VALUE;
    private float mRoll = Float.MIN_VALUE;

    @Override
	public float getHeading() {
    	return mHeading;
	}

	@Override
	public void setHeading(float heading) {
		mHeading = heading;
	}

	@Override
	public float getPitch() {
		return mPitch;
	}

	@Override
	public void setPitch(float pitch) {
		mPitch = pitch;
	}

	@Override
	public float getRoll() {
		return mRoll;
	}

	@Override
	public void setRoll(float roll) {
		mRoll = roll;
	}

	@Override
    public void updateFrom(ArtemisObject obj) {
		super.updateFrom(obj);

		if (obj instanceof ArtemisOrientable) {
			ArtemisOrientable cast = (ArtemisOrientable) obj;
			float heading = cast.getHeading();
	        float pitch = cast.getPitch();
	        float roll = cast.getRoll();

	        if (heading != Float.MIN_VALUE) {
	        	mHeading = heading;
	        }

	        if (pitch != Float.MIN_VALUE) {
	        	mPitch = pitch;
	        }

	        if (roll != Float.MIN_VALUE) {
	        	mRoll = roll;
	        }
		}
    }

	@Override
	protected void appendObjectProps(SortedMap<String, Object> props,
			boolean includeUnspecified) {
		super.appendObjectProps(props, includeUnspecified);
    	putProp(props, "Heading", mHeading, Float.MIN_VALUE, includeUnspecified);
    	putProp(props, "Pitch", mPitch, Float.MIN_VALUE, includeUnspecified);
    	putProp(props, "Roll", mRoll, Float.MIN_VALUE, includeUnspecified);
    }
}