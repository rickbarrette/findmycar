/**
 * @author Twenty Codes
 * @author ricky barrette
 */

package com.TwentyCodes.android.FindMyCarLib.UI.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.TwentyCodes.android.FindMyCarLib.R;
import com.TwentyCodes.android.FindMyCarLib.Settings;

/**
 * displays a notes dialog where the user can enter and save notes
 * @author ricky barrette
 */
public class NotesFragment extends Fragment {

	private EditText mText;
	private SharedPreferences mSettings;
	private TextView mAddress;

	/**
	 * (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onPause()
	 */
	@Override
	public void onPause() {
		mSettings.edit().putString(Settings.NOTE, mText.getText().toString()).commit();
//		Toast.makeText(getActivity(), R.string.saved, Toast.LENGTH_LONG).show();
		super.onPause();
	}

	/**
	 * (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onResume()
	 */
	@Override
	public void onResume() {
		/*
		 * display saved notes from file
		 */
		mText.setText(mSettings.getString(Settings.NOTE, ""));
		
		/*
		 * display address from file
		 */
		mAddress.setText(mSettings.getString(Settings.ADDRESS, ""));
		super.onResume();
	}


	/**
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater,
	 *      android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.notes, container, false);
		mSettings = getActivity().getSharedPreferences(Settings.SETTINGS, 0);
		mText = (EditText) view.findViewById(R.id.editText);
		mAddress = (TextView) view.findViewById(R.id.tvAddress);
		return view;
	}

	/**
	 * deletes the note
	 * @author ricky barrette
	 */
	public void delete() {
		if(mText != null)
			mText.setText("");
		if(mAddress != null)
			mAddress.setText("");
		if(mSettings != null)
			mSettings.edit().remove(Settings.NOTE).remove(Settings.ADDRESS).commit();
	}
}