/**
 * @author Twenty Codes
 * @author wwpowers
 * @author ricky barrette
 */
package com.TwentyCodes.android.FindMyCarLib;

import com.TwentyCodes.android.FindMyCarLib.UI.fragments.MapFragment;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

public class ParkingTimerService extends Service {

	private static final String TAG = "ParkingTimerService";
	private long mMinutes;
	protected Context mContext = this;
	protected NotificationManager mNotificationManager;
	public static int SIMPLE_NOTFICATION_ID = 656;
	public PendingIntent intent;
	private static long mPeriod = 60000L; //this is the period in which the notification will be updated. 
	private int mImage; //value for notification image
	private Timer mTimer;
	
	/**
	 * auto generated, not used
	 * (non-Javadoc)
	 * @see android.app.Service#onBind(android.content.Intent)
	 * @param arg0
	 * @return
	 */
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	/**
	 * auto generated, not used
	 * (non-Javadoc)
	 * @see android.app.Service#onCreate()
	 */
	@Override
	public void onCreate() {
		super.onCreate();
	}
	
	/**
	 * if the service stops, remove the notification
	 * (non-Javadoc)
	 * @see android.app.Service#onDestroy()
	 * @author ricky barrette
	 */
	@Override
	public void onDestroy(){
		mNotificationManager.cancel(656);
		mTimer.cancel();
		super.onDestroy();
	}

	/**
	 * This method is called when startService is called. only used in 2.x android.
	 * @author wwpowers
	 */
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand.Service started with start id of: " + startId);
        getData(intent);
        return START_STICKY;
    }
	
	/**
	 * To keep backwards compatibility we override onStart which is the equivalent of onStartCommand in pre android 2.x
	 * @author wwpowers
	 */
	@Override
	public void onStart(Intent intent, int startId) {
		Log.i(TAG, "onStart.Service started with start id of: " + startId);
		getData(intent);
	}
	
	/**
	 * extracts time in milliseconds from from a bundle that was packaged into the intent, and starts the Timer()
	 * @param intent
	 * @author wwpowers
	 */
	private void getData(Intent intent) {
		Bundle bundle = intent.getBundleExtra("minutes");
		//set the update interval to update the ongoing parking timer notification
		mPeriod = Integer.parseInt(getSharedPreferences(Settings.SETTINGS, 0).getString(Settings.PARKING_TIMER_UPDATE_INTERVAL, "60"));
        mMinutes = bundle.getLong("minutes");
        mImage = bundle.getInt("color");
        mTimer = new Timer(mMinutes);
        mTimer.start();
	}
	
	/**
	 * this internal class will handle all timing functions of this service.
	 * when the Timer() is started it will create an ongoing notification the display how much time is left for the parking timer,
	 * and will update every mPeriod milliseconds
	 * @author ricky barrette
	 */
	class Timer extends CountDownTimer {

		private Notification mNotifyDetails;

		/**
		 * creates a new Timer that creates an ongoing notification the display how much time is left for the parking timer,
		 * and will update every mPeriod milliseconds
		 * @param millisInFuture
		 * @author ricky barrette
		 */
		public Timer(long millisInFuture) {
			super(millisInFuture, mPeriod);
			mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
			
			mNotifyDetails = new Notification(mImage, stringTime(millisInFuture) , System.currentTimeMillis());
			mNotifyDetails.flags |= Notification.FLAG_ONGOING_EVENT;
			Intent startFMC = new Intent(mContext, MapFragment.class);
			intent = PendingIntent.getActivity(mContext, 0, startFMC, android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
			mNotifyDetails.setLatestEventInfo(mContext, "Parking Timer", stringTime(millisInFuture), intent);
			mNotificationManager.notify(SIMPLE_NOTFICATION_ID, mNotifyDetails);
		}

		/**
		 * removes the ongoing parking timer notification and kills the service
		 * (non-Javadoc)
		 * @see android.os.CountDownTimer#onFinish()
		 * @author ricky barrette
		 */
		@Override
		public void onFinish() {
			mNotificationManager.cancel(656);
			stopSelf();
		}

		/**
		 * updates the ongoing Parking Timer notification
		 * (non-Javadoc)
		 * @see android.os.CountDownTimer#onTick(long)
		 * @param millisUntilFinished
		 * @author ricky barrette
		 */
		@Override
		public void onTick(long millisUntilFinished) {
			mMinutes = millisUntilFinished;

			mNotifyDetails.setLatestEventInfo(mContext, "Parking Timer", stringTime(millisUntilFinished), intent);
			mNotificationManager.notify(SIMPLE_NOTFICATION_ID, mNotifyDetails);
		}
		
		/**
		 * convince method for formating milliseconds into hour : minutes format
		 * @param mills
		 * @return human readable hour : minutes format
		 * @author ricky barrette
		 */
		private String stringTime(long mills){
			int hours = (int) (mills / 3600000);
			mills = mills - (hours * 3600000);
			int minutes = (int) ( mills / 60000);
			int seconds = (int) (mills % 60000);
			seconds = seconds / 1000;
			return hours +" : "+ padTime(minutes) +" : "+ padTime(seconds);
		}
		
		/**
		 * convince method for formating the seconds string
		 * @param seconds
		 * @return formated string
		 * @author ricky barrette
		 */
		private String padTime(int time){
			if (time <= 9)
				return "0"+ time;
			return ""+ time;
		}
	}
}