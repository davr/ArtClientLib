package net.dhleong.acl.enums;

/**
 * The four possible torpedo tube states.
 * @author rjwut
 */
public enum TubeState {
	UNLOADED, // the tube is ready to load
	LOADED,   // the tube is ready to fire or unload
	LOADING,  // wait for the tube to finish loading
	UNLOADING // wait for the tube to finish unloading
}