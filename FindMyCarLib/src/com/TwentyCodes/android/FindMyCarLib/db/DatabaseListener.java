/**
 * OnDatabaseUpgradeCompeteListener.java
 * @date Nov 10, 2011
 * @author Twenty Codes, LLC
 * @author ricky barrette
 */
package com.TwentyCodes.android.FindMyCarLib.db;

/**
 * This interface will be used to listen to see when the database events are complete
 * @author ricky barrette
 */
public interface DatabaseListener {

	public void onDatabaseUpgradeComplete();

	public void onLocationDeletionComplete();

	public void onRestoreComplete();

	public void onDatabaseUpgrade();

}