/**
 * @author Twenty Codes
 * @author ricky barrette
 */
package com.TwentyCodes.android.FindMyCarLib;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;

/**
 * @author ricky barrette
 *
 */
public class Settings extends PreferenceActivity implements	OnPreferenceChangeListener {

	/*
	 * the following strings are for the shared_prefs settings.xml
	 */
	public final static String SETTINGS = "settings";
	public final static String LAT = "lat";
	public final static String LON = "lon";
	public final static String MEASUREMENT_UNIT = "measurement_unit";
	public final static String LAYERS = "layers";
	public final static String STAY_AWAKE = "stay_awake";
	public final static String FIRST_BOOT = "first_boot";
	public final static String ADDRESS = "address";
	public final static String NOTE = "note";
	public final static String PARKING_TIMER_ALARM = "parking_timer_alarm";
	public final static String BUILD_NUMBER = "build_number";
	public final static String PARKING_TIMER_SERVICE = "parking_timer_service";
	public final static String PARKING_TIMER_UPDATE_INTERVAL ="parking_timer_update_interval";
	public final static String PARKING_TIMER_ONGOING_NOTIFICATION_ISENABLED ="parking_timer_ongoing_notification_isenabled";
	public final static String PARKING_TIMER_NOTIFICATION_COLOR = "parking_timer_notification_color";
	public final static String DIRECTIONS = "directions";
	public final static String IS_REGISTERED = "is_registered";
	public final static String COMPASS_OPTION = "compass_option";
//	private final static String TAG = "Settings";
	protected static final String ACCEPTED = "accepted";

	/**
	 * 
	 * (non-Javadoc)
	 * @see android.preference.PreferenceActivity#onCreate(android.os.Bundle)
	 * @param icicle
	 * @author ricky barrette
	 */
	@Override
	public void onCreate(Bundle icicle){
		super.onCreate(icicle);
		
		//set shared_prefs name
    	getPreferenceManager().setSharedPreferencesName(SETTINGS);
    	
    	//load preferences xml. this load relies on only wether the app is full or not. it will show the check license option if full and leave it out if lite
    	addPreferencesFromResource(R.xml.settings);
    	
    	findPreference(MEASUREMENT_UNIT).setOnPreferenceChangeListener(this);
//    	findPreference(PARKING_TIMER_UPDATE_INTERVAL).setOnPreferenceChangeListener(this);
    	
//    	if(Main.isFull){
//    		findPreference(PARKING_TIMER_ONGOING_NOTIFICATION_ISENABLED).setEnabled(true);
//    		findPreference(PARKING_TIMER_UPDATE_INTERVAL).setOnPreferenceChangeListener(this);
//    	}

	}
	
	@Override
    protected void onResume() {
        super.onResume();
        SharedPreferences shared_prefs = getPreferenceManager().getSharedPreferences();
    	findPreference(MEASUREMENT_UNIT).setSummary(shared_prefs.getString(MEASUREMENT_UNIT, "Metric"));
    	
    }

	/**
	 * (non-Javadoc)
	 * @see android.preference.Preference.OnPreferenceChangeListener#onPreferenceChange(android.preference.Preference, java.lang.Object)
	 * @param preference
	 * @param newValue
	 * @return
	 * @author ricky barrette
	 */
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		String key = preference.getKey();

		if(key.equals(MEASUREMENT_UNIT)){
			preference.setSummary(newValue.toString());
			return true;
		}		
		
//		if(key.equalsIgnoreCase(PARKING_TIMER_UPDATE_INTERVAL)){
//			try {
//				Integer.parseInt(newValue.toString());
//				Toast.makeText(this, getText(R.string.update_interval_updated_to)+ " " + newValue.toString() + " " + getText(R.string.seconds), Toast.LENGTH_LONG).show();
//			} catch (NumberFormatException e) {
//				e.printStackTrace();
//				Toast.makeText(this, getText(R.string.the_vaule_was_not_a_number_update_interval_60), Toast.LENGTH_LONG).show();
//				getPreferenceManager().getSharedPreferences().edit().putInt(PARKING_TIMER_UPDATE_INTERVAL, 60);
//			}
//			return true;
//		}
		return false;
	}
}