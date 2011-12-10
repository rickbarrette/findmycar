/**
 * ParkignTimerActivity.java
 * @date Dec 4, 2011
 * @author ricky barrette
 * @author Twenty Codes, LLC
 */
package com.TwentyCodes.android.FindMyCarLib;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

/**
 * @author ricky barrette
 */
public class ParkignTimerActivity extends FragmentActivity {

	/**
	 * (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		this.setContentView(R.layout.parking_timer_activity);
	}

}
