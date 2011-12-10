/**
* @author Twenty Codes
* @author ricky barrette
*/
package com.TwentyCodes.android.FindMyCarLib;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * this is the receiver and handler for alarms that have been previously set by us
 * @author ricky barrette
 */
public class AlarmReceiver extends BroadcastReceiver {

	private Context mContext;
	protected NotificationManager mNotificationManager;
	private int SIMPLE_NOTFICATION_ID = 0;

	/**
	 * receives a broadcast
	 * when a broadcast is received, we extract the bundle from the intent and then extract the requestCode from the bundle
	 * using the requestCode, we know if it is a notify notification of a time up notification
	 * @author ricky barrette
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		mContext = context;
		mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		switch(intent.getExtras().getInt("requestCode")){
			case 0:
				notifyNotification();
				break;
			case 1:
				timeUpNotification();
//				mContext.deleteFile("AppService.txt");
				context.getSharedPreferences(Settings.SETTINGS, 0).edit().remove(Settings.PARKING_TIMER_ALARM).commit();
				context.getSharedPreferences(Settings.SETTINGS, 0).edit().remove(Settings.PARKING_TIMER_SERVICE).commit();
				mNotificationManager.cancel(ParkingTimerService.SIMPLE_NOTFICATION_ID);
				break;
		}
	}
	
	/**
	 * parking timer is up notification
	 * @author ricky barrette
	 */
	private void timeUpNotification(){
		final Notification notifyDetails = new Notification(R.drawable.show_car_black, mContext.getText(R.string.your_time_up_ticket),System.currentTimeMillis());
		notifyDetails.defaults |= Notification.DEFAULT_SOUND;
		notifyDetails.defaults |= Notification.DEFAULT_VIBRATE;
		notifyDetails.defaults |= Notification.DEFAULT_LIGHTS;
		notifyDetails.flags |= Notification.FLAG_INSISTENT;
		notifyDetails.flags |= Notification.FLAG_SHOW_LIGHTS;
		notifyDetails.flags |= Notification.FLAG_AUTO_CANCEL;
		Context context = mContext.getApplicationContext();
	    
		Intent startFMC = new Intent(mContext, Main.class);
		     
		PendingIntent intent = PendingIntent.getActivity(mContext, 0, startFMC, android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
		
		notifyDetails.setLatestEventInfo(context, mContext.getText(R.string.your_timer_up_title), mContext.getText(R.string.your_timer_up_msg), intent);
		
		mNotificationManager.notify(SIMPLE_NOTFICATION_ID, notifyDetails);
	}
	
	/**
	 * timer almost up notification
	 * @author ricky barrette
	 */
	private void notifyNotification(){	
		final Notification notifyDetails = new Notification(R.drawable.show_car_black,
		mContext.getText(R.string.your_timer_almost_up_ticket),System.currentTimeMillis());
		notifyDetails.defaults |= Notification.DEFAULT_SOUND;
		notifyDetails.defaults |= Notification.DEFAULT_VIBRATE;
		notifyDetails.defaults |= Notification.DEFAULT_LIGHTS;
		notifyDetails.flags |= Notification.FLAG_SHOW_LIGHTS;
		notifyDetails.flags |= Notification.FLAG_AUTO_CANCEL;
		 
		Intent startFMC = new Intent(mContext, Main.class);
		     
		PendingIntent intent = PendingIntent.getActivity(mContext, 0, startFMC, android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
		
		notifyDetails.setLatestEventInfo(mContext, mContext.getText(R.string.your_timer_almost_up_title), mContext.getText(R.string.your_timer_almost_up_msg), intent);
		
		mNotificationManager.notify(SIMPLE_NOTFICATION_ID, notifyDetails);
	}
	
}