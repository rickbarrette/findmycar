/**
 * @author Twenty Codes
 * @author WWPowers
 * @author ricky barrette
 */
package com.TwentyCodes.android.FindMyCarLib.UI.fragments;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;
import android.widget.Toast;

import com.TwentyCodes.android.FindMyCarLib.AlarmReceiver;
import com.TwentyCodes.android.FindMyCarLib.ParkingTimerService;
import com.TwentyCodes.android.FindMyCarLib.R;
import com.TwentyCodes.android.FindMyCarLib.Settings;

/**
 * This class will be the dialog that allows the user to input their settings for the parking timer, start a new parking timer, or delete an existing 
 * parking timer.
 * @author warren 
 */
public class ParkingTimerFragment extends Fragment implements OnClickListener, OnTimeChangedListener {
	
	private TimePicker mPicker;
	private CheckBox chNotify;
	private EditText etNotify;
	private static boolean mIsOutsideDialog = false;
	private static final String TAG = "ParkingTimerDialog";
	private static SharedPreferences settings;
	
	/**
	 * (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		this.getActivity().setTitle(R.string.parking_timer);
		View view = inflater.inflate(R.layout.parkingtimer_layout, container, false);
		settings = getActivity().getSharedPreferences(Settings.SETTINGS, 0);
		mPicker = (TimePicker) view.findViewById(R.id.tpParkingTimerTimePicker);
		view.findViewById(R.id.btSetTimer).setOnClickListener(this);
		view.findViewById(R.id.btRemoveTimer).setOnClickListener(this);
		chNotify = (CheckBox) view.findViewById(R.id.chNotify);
		etNotify = (EditText) view.findViewById(R.id.etNotify);
		mPicker.setIs24HourView(true);
		mPicker.setCurrentHour(0);
		mPicker.setCurrentMinute(5);
		mPicker.setOnTimeChangedListener(this);
		setTitle(mPicker);
	
		return view;
	}
	

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btSetTimer) {
			setTimer();
			getActivity().finish();
		} else if (v.getId() == R.id.btRemoveTimer) {
			stopTimer();
		}
		
	}
	
	/**
     * @author - WWPowers
     * @param value - value to be checked 
     * @return - true if value meets reqs. false if it does not
     * This method checks if the given value is an integer that is greater than or equal to 1. 
     */
    private boolean checkValue(String value) {
    	char chrValue;
    	int num;
    	if (value.length() == 0) {
    		return false;
    	}
    	for(int i = 0; i < value.length(); i++) {
    		chrValue = value.charAt(i);
    		try {
    			num = Integer.parseInt(Character.toString(chrValue));
    		} catch (NumberFormatException e) {
    			return false;
    		}
    	}
    	num = Integer.parseInt(value);
    	if (num < 0) {
    		return false;
    	}
    	
    	return true;
    }
    
    /**
     * returns the int value displayed by a edit text
     * returns 0 if the value is not parse-able
     * @param EditText text
     * @return int value
     * @author WWPowers
     */
	private int getValue(EditText text) {
    	Editable value = text.getText();
    	String strValue = value.toString();
    	if (!checkValue(strValue)) {
    		toastLong(getActivity().getText(R.string.pick_time_greaterthan_zero));
    	} else {
    		return Integer.parseInt(strValue);
    	}
    	return 0;
    }
	
    /**
	 * cancels the parking timer alarms
	 * @author WWPowers
	 * @author ricky barrette
	 */
	public void stopTimer() {
		settings = getActivity().getSharedPreferences(Settings.SETTINGS, 0);
		mIsOutsideDialog = true;
		if (! settings.getBoolean(Settings.PARKING_TIMER_ALARM, false)){
			if (!mIsOutsideDialog) {
				toastLong(getActivity().getText(R.string.no_timer));
				mIsOutsideDialog = true;
//	    		//cancel ongoing notification
	    		NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
	    		notificationManager.cancel(656);
			}
			
		} else {
			settings.edit().remove(Settings.PARKING_TIMER_ALARM).commit();
			
		   /* 
			* to cancel the alarms we will create a new Intent and a new PendingIntent with the
			* same requestCode as the PendingIntent alarm we want to cancel.
			* Note: The intent and PendingIntent have to be the same as the ones used to create the alarms.
			*/;
			AlarmManager am = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
    		
    		Intent notifyIntent = new Intent(getActivity(), AlarmReceiver.class);
    		notifyIntent.putExtra("requestCode", 0);
    		PendingIntent sender = PendingIntent.getBroadcast(getActivity(), 1234567, notifyIntent, 0);
    		am.cancel(sender);
    		
    		Intent timerUpIntent = new Intent(getActivity(), AlarmReceiver.class);
    		timerUpIntent.putExtra("requestCode", 1);
    		PendingIntent sender2 = PendingIntent.getBroadcast(getActivity(), 123123, timerUpIntent, 0);
    		am.cancel(sender2);
    		
    		stopTimerService();
    		
    		toastLong("Timer Canceled");
		}
	}
	
	/**
	 * this method is a convince method which will set a timer in the alarm manager class
	 */	
	private void setTimer() {
		long hours = mPicker.getCurrentHour();
		long minutes = mPicker.getCurrentMinute();
		long notify = getValue(etNotify);
		if ((hours == 0) && (minutes == 0)) {
			toastLong(getActivity().getText(R.string.pick_time_greaterthan_zero));
		} if (((hours * 60) + minutes) > 1440) {
			toastLong(getActivity().getText(R.string.pick_timer_lessthan_24));
		} else {
			
			if (! settings.getBoolean(Settings.PARKING_TIMER_ALARM, false)){
				
				minutes = minutes + (hours * 60);
				minutes = minutes * 60000;
				
				//if the parking timer ongoing notification is enabled, then start the service to display it.
				if (settings.getBoolean(Settings.PARKING_TIMER_ONGOING_NOTIFICATION_ISENABLED, true)){
					startTimerService(minutes);
				}
				
				notify = getValue(etNotify);
	        	
				Calendar cal = Calendar.getInstance();
				
				Intent notifyIntent = new Intent(getActivity(), AlarmReceiver.class);
	    		notifyIntent.putExtra("requestCode", 0);
	    		PendingIntent sender = PendingIntent.getBroadcast(getActivity(), 1234567, notifyIntent, 0);

	    		Intent timerUpIntent = new Intent(getActivity(), AlarmReceiver.class);
	    		timerUpIntent.putExtra("requestCode", 1);
	    		PendingIntent sender2 = PendingIntent.getBroadcast(getActivity(), 123123, timerUpIntent, 0);

	    		AlarmManager am = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
	    		
	    		if ((notify != 0) && (chNotify.isChecked()) && (notify < (minutes / 60000))) {
        			notify = notify * 60000;
        			notify = (minutes - notify);
        			am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis() + notify, sender); 
	    		}
	    		
	    		am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis() + minutes, sender2); 

				toastLong(getActivity().getText(R.string.timer_set_for) +" "+ mPicker.getCurrentHour() +" "+ getActivity().getText(R.string.timer_set_for_hours) 
						+" "+ mPicker.getCurrentMinute() +" "+ getActivity().getText(R.string.timer_set_for_minutes));
				settings.edit().putBoolean(Settings.PARKING_TIMER_ALARM, true).commit();
			} else {
				toastLong(getActivity().getText(R.string.timer_already_set));
			}
		}
		
	}
	
	/**
	 * displays a toast message msg
	 * @param msg
	 * @author ricky barrette
	 */
    private void toastLong(CharSequence msg) {
		Toast toast = Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG);
		toast.show();
	}

    /**
     * updates the tile of the parking timer dialog when ever the time picker incrimented
     * @author wwpowers
     */
	@Override
	public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
		setTitle(mPicker);	
	}
	
	/**
	 * This method is a convenience method to return the time of the day plus the parking timer 
	 * @return - title of dialog as string
	 */
	private void setTitle(TimePicker picker) {
		int hourOfDay = picker.getCurrentHour();
		int minute = picker.getCurrentMinute();
		Calendar calendar = Calendar.getInstance();
		//hours
		hourOfDay = hourOfDay + calendar.get(Calendar.HOUR_OF_DAY);
		if (hourOfDay > 24){
			hourOfDay = hourOfDay - 24;
		}
		//minutes
		minute = minute + calendar.get(Calendar.MINUTE);
		if (minute > 59) {
			hourOfDay++;
			minute = minute - 60;
		}
		//update title
		
//		if (hourOfDay > 12) {
//			this.setTitle(getActivity().getText(R.string.parking_timer) + "  " + padHour(hourOfDay - 12) + ":" + padMinute(minute) + " PM");
//		} else {
//			this.setTitle(getActivity().getText(R.string.parking_timer) + "  " + (hourOfDay) + ":" + padMinute(minute) + " AM");
//		}
	}
	
//	/**
//	 * Convenience method for fixing 0AM to 12AM
//	 * @param hour
//	 * @return String hour of day
//	 * @author ricky barrette
//	 */
//	private static String padHour(int hour){
//		 if (hour == 0){
//			 return String.valueOf(12);
//		 }
//		 return String.valueOf(hour);
//	}
//	
//	/**
//	 * Convenience method for formatting time
//	 * @param minute - minute of day
//	 * @return - String of minute of day plus an zero in front of it if the value is less than 10
//	 */
//	private static String padMinute(int minute) {
//	    if (minute >= 10){
//	        return String.valueOf(minute);
//	    }
//	    return "0" + String.valueOf(minute);
//	}
	
	/**
	 * This method will create the remote service, bind to it and pass it the time, then unbind
	 * @param time - length of parking timer in milliseconds
	 */
	
	protected void startTimerService(long minutes) {
//		if (!mServiceStarted) {
//			Intent i = new Intent();
//			i.setClassName("com.TwentyCodes.android.FindMyCarFull", "com.TwentyCodes.android.FindMyCarFull.ParkingTimerService");
//			mCtx.startService(i);
//			mServiceStarted = true;
//			Log.i(TAG, "startTimerService.service started");
//		} else {
//			Log.i(TAG, "startTimerService.service is already started");
//		}
		if (! settings.getBoolean(Settings.PARKING_TIMER_SERVICE, false)) {
			
			Intent i = new Intent(getActivity(), ParkingTimerService.class);
			Bundle bundle = new Bundle();
			bundle.putLong("minutes", minutes);
			if (settings.getString(Settings.PARKING_TIMER_NOTIFICATION_COLOR, "Black").equalsIgnoreCase("Black")) {
				Log.i(TAG, "startTimerService.retrieved color: " + settings.getString(Settings.PARKING_TIMER_NOTIFICATION_COLOR, "Null"));
				bundle.putInt("color", R.drawable.show_car_black);
			} else {
				bundle.putInt("color", R.drawable.show_car_white);
			}
//			bundle.putInt(Settings.PARKING_TIMER_UPDATE_INTERVAL, settings.getInt(Settings.PARKING_TIMER_UPDATE_INTERVAL, 60));
			i.putExtra("minutes", bundle);
			getActivity().startService(i);
			settings.edit().putBoolean(Settings.PARKING_TIMER_SERVICE, true).commit();
		} else {
			Log.i(TAG, "startTimerService.service is already started");
			
		}
		
	}
	
	/**
	 * This method will stop the service if it is running
	 */
	
	public void stopTimerService() {
		if (settings.getBoolean(Settings.PARKING_TIMER_SERVICE, false)) {
			Intent i = new Intent();
			i.setClassName("com.TwentyCodes.android.FindMyCarFull", "com.TwentyCodes.android.FindMyCarFull.ParkingTimerService");
			getActivity().stopService(i);
			NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
    		notificationManager.cancel(656);
			settings.edit().remove(Settings.PARKING_TIMER_SERVICE).commit();
		} else {
			Log.i(TAG, "stopTimerService.service is not running. unable to stop");
		}
	}
	

}
