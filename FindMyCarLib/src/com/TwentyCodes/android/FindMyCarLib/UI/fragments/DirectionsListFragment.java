/**
 * DirectionsListFragment.java
 * @date Nov 25, 2011
 * @author ricky barrette
 * @author Twenty Codes, LLC
 */
package com.TwentyCodes.android.FindMyCarLib.UI.fragments;

import java.util.ArrayList;

import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.TwentyCodes.android.FindMyCarLib.DirectionsAdapter;
import com.TwentyCodes.android.FindMyCarLib.Main;
import com.TwentyCodes.android.FindMyCarLib.R;
import com.TwentyCodes.android.FindMyCarLib.UI.DirectionsOverlay;
import com.google.android.maps.GeoPoint;

/**
 * This fragment will be used to display directions to the user. 
 * When a specific direction is clicked, the corrispoding geopoint is returned via listener
 * @author ricky barrette
 */
public class DirectionsListFragment extends ListFragment {
	
	public interface OnDirectionSelectedListener{
		public void onDirectionSelected(GeoPoint SelectedPoint);
	}

	private OnDirectionSelectedListener mListener;
	private ArrayList<GeoPoint> mPoints;

	/**
	 * Creates a new Directions List Fragment
	 * @author ricky barrette
	 */
	public DirectionsListFragment() {
		super();
	}

	/**
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

	/**
	 * Creates a new Directions List Fragment
	 * @param listener
	 * @author ricky barrette
	 */
	public DirectionsListFragment(OnDirectionSelectedListener listener) {
		this();
		mListener = listener;
	}
	
	/**
	 * Displays the directions from the provided DirectionsOverlay object
	 * @param directions
	 * @author ricky barrette
	 */
	public void setDirections(final DirectionsOverlay directions) {
		mPoints = directions.getPoints();
		this.setListAdapter(new DirectionsAdapter(getActivity(), directions));
	}

	/**
	 * Called when a list item is clicked.
	 * Checks to see if the list item is a direction, if to it reports the selected direction's geopoint to the listener
	 * (non-Javadoc)
	 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
	 */
	@Override
	public void onListItemClick(ListView l, View w, int position, long id) {
		if(position < mPoints.size())
			if(mListener != null)
				mListener.onDirectionSelected(mPoints.get(position));
	}

	/**
	 * Deletes all content in the listview
	 * @author ricky barrette
	 */
	public void clear() {
		this.setListAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, new ArrayList<String>()));
	}
}
