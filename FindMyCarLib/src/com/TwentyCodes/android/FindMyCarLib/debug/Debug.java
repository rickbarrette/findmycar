/**
 * Debug.java
 * @date Mar 24, 2011
 * @author ricky barrette
 * @author Twenty Codes, LLC
 */
package com.TwentyCodes.android.FindMyCarLib.debug;

/**
 * A Convince class to hold constant variables to force test situations
 * @author ricky barrette
 */
public class Debug {

	public static final boolean DEBUG = false;

	/**
	 * When set to true, this will force the first boot dialog to be displayed at every boot
	 * @author ricky barrette
	 */
	public static final boolean FORCE_FIRSTBOOT_DIALOG = false;
	
	/**
	 * Drops the ringer database table every time the database is created
	 */
	public static boolean DROP_TABLE_EVERY_TIME = false;
}
