package net.dhleong.acl.world;

import java.util.SortedMap;

import net.dhleong.acl.enums.ObjectType;

/**
 * Bases
 */
public class ArtemisBase extends BaseArtemisShielded {
	private int mIndex = -1;

	public ArtemisBase(int objId) {
        super(objId);
    }

	@Override
    public ObjectType getType() {
        return ObjectType.BASE;
    }

	/**
	 * This base's index value. In non-scripted scenarios, DS1's index is 0,
	 * DS2's index is 1, etc. This value is unique even if the names aren't.
	 * Unspecified: -1
	 */
	public int getIndex() {
		return mIndex;
	}

	public void setIndex(int index) {
		mIndex = index;
	}

    @Override
    public void updateFrom(ArtemisObject eng) {
        super.updateFrom(eng);
        
        if (eng instanceof ArtemisBase) {
            ArtemisBase base = (ArtemisBase) eng;

            if (base.mIndex != -1) {
            	mIndex = base.mIndex;
            }
        }
    }

    @Override
	public void appendObjectProps(SortedMap<String, Object> props, boolean includeUnspecified) {
    	super.appendObjectProps(props, includeUnspecified);
    	putProp(props, "Base index", mIndex, -1, includeUnspecified);
    }
}