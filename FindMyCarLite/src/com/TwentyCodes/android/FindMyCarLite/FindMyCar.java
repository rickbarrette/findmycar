/**
 * @author Twenty Codes, LLC
 * @author ricky barrette
 * @date Oct 16, 2010
 */
package com.TwentyCodes.android.FindMyCarLite;

import android.os.Bundle;

import com.TwentyCodes.android.FindMyCarLib.Main;

/**
 * this is the Lite version of find my car. here we will extend the Full version, and override only the methods we need to change 
 * @author ricky barrette
 */
public class FindMyCar extends Main {
	
	@Override
	public void onCreate (Bundle savedInstanceState) {
		isFull = false;
		super.onCreate(savedInstanceState);
	}
	

}