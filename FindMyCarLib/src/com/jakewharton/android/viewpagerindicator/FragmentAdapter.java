/**
 * FragmentAdapter.java
 * @date Aug 6, 2011
 * @author Twenty Codes, LLC
 * @author ricky barrette
 */
package com.jakewharton.android.viewpagerindicator;

import java.util.ArrayList;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * This adaptor maintains the How and What fragments
 * @author ricky
 */
class FragmentAdapter extends FragmentPagerAdapter {

	private ArrayList<Fragment> mFragments;

	/**
	 * Creates a new FragmentAdaptor
	 * @param fm
	 * @param fragments to be displayed
	 * @author ricky barrette
	 */
	public FragmentAdapter(FragmentManager fm, ArrayList<Fragment> fragments) {
		super(fm);
		this.mFragments = fragments;
	}

	@Override
	public Fragment getItem(int position) {
		return this.mFragments.get(position);			
	}

	@Override
	public int getCount() {
		return this.mFragments.size();
	}
	
	
}