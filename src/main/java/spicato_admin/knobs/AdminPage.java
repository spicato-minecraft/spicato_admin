package spicato_admin.knobs;

/**
 * Which admin screen a knob belongs on. Play is for pack balance; Debug is for
 * UAT / diagnostics and must not mix into Play.
 */
public enum AdminPage {
	PLAY,
	DEBUG
}
