/**
 * DirectionsListFragment.java
 * @date Mar 5, 2012
 * @author ricky barrette
 * @author Twenty Codes, LLC
 */
package com.TwentyCodes.android.FindMyCarLib.UI.fragments;

import com.TwentyCodes.android.FindMyCarLib.Main;
import com.TwentyCodes.android.FindMyCarLib.R;
import com.TwentyCodes.android.location.OnDirectionSelectedListener;

/**
 * This is our direction's list fragment.
 * @author ricky barrette
 */
public class DirectionsListFragment extends	com.TwentyCodes.android.fragments.DirectionsListFragment {
	
	/**
	 * Creates a new Directions List Fragment
	 * @param listener
	 * @author ricky barrette
	 */
	public DirectionsListFragment(OnDirectionSelectedListener listener) {
		super(listener);
	}

	/**
	 * We override onstart to set emptry list messasges
	 * (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onStart()
	 */
	@Override
	public void onStart() {
		this.setListShown(true);
		if(Main.isFull)
			this.setEmptyText(getActivity().getText(R.string.directions_empty_msg));
		else
			this.setEmptyText(getActivity().getText(R.string.nav_only_in_full));
		super.onStart();
	}

}
