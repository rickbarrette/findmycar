/**
 * Main.java
 * @date Nov 14, 2011
 * @author ricky barrette
 * @author Twenty Codes, LLC
 */
package com.TwentyCodes.android.FindMyCarLib;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.TwentyCodes.android.FindMyCarLib.UI.CustomViewPager;
import com.TwentyCodes.android.FindMyCarLib.UI.fragments.DirectionsListFragment;
import com.TwentyCodes.android.FindMyCarLib.UI.fragments.MapFragment;
import com.TwentyCodes.android.FindMyCarLib.UI.fragments.MapFragment.MapFragmentListener;
import com.TwentyCodes.android.FindMyCarLib.UI.fragments.NotesFragment;
import com.TwentyCodes.android.exception.ExceptionHandler;
import com.TwentyCodes.android.fragments.DirectionsListFragment.OnDirectionSelectedListener;
import com.TwentyCodes.android.location.ReverseGeocoder;
import com.TwentyCodes.android.overlays.DirectionsOverlay;
import com.google.ads.AdRequest;
import com.google.ads.AdView;
import com.google.android.maps.GeoPoint;
import com.jakewharton.android.viewpagerindicator.TitlePageIndicator;
import com.jakewharton.android.viewpagerindicator.TitledFragmentAdapter;

/**
 * This is the Main Activity of FMC Full & Lite
 * @author ricky barrette
 */
public class Main extends FragmentActivity implements MapFragmentListener, OnPageChangeListener, OnDirectionSelectedListener {

	private static final String SPLASH = "splash";
	private static final String TAG = "Main";
	private SharedPreferences mSettings;
	private Dialog mSplashDialog;
	private WakeLock mWakeLock;
	private MapFragment mMap;
	private NotesFragment mNotes;
	private CustomViewPager mPager;
	private ArrayList<Fragment> mFragments;
	private DirectionsListFragment mDirectionsFragment;
	private TitlePageIndicator mIndicator;
	public static boolean isFull = true;
	
	/**
	 * displays a dialog to inform that the gps is disabled and provides them with a shortcut to the settings page
	 * To make this dialog more friendly, i have removed the No button (to mimic google maps) and have enabled the dialog to be cancel via the back button.
	 * @author ricky barrette
	 */
	public static void enableGPSdialog(final Context context) {
		new AlertDialog.Builder(context)
        .setMessage(R.string.gps_is_disabled).setCancelable(true)
        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
        	public void onClick(DialogInterface dialog, int id) {
        		Intent callGPSSettingIntent = new Intent( android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            		context.startActivity(callGPSSettingIntent);
                    dialog.cancel();
                }
        	})
        .show();
	}
	/**
	 * displays a dialog informing user that this feature is found only in full
	 */
	public static void featureInFullDialog(final Context context) {
		new AlertDialog.Builder(context)
		.setTitle(R.string.feature_in_fmc_full)
		.setMessage(R.string.feature_in_fmc_full_description)
	    .setCancelable(false)
	            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int id) {
	                    Intent intent = new Intent(Intent.ACTION_VIEW);
	                    intent.setData(Uri.parse("market://details?id=com.TwentyCodes.android.FindMyCarFull"));
	                    context.startActivity(intent);
	            	}
	            })
	            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int id) {
	                	//do nothing
	            		}
	            })
	    .show();	
	}

	/**
	 * displays the welcome dialog
	 * 
	 * @author ricky barrette
	 */
	private void displayWelcomeDialog() {
		new AlertDialog.Builder(this)
		.setTitle(getText(R.string.welcome))
		.setMessage(R.string.welcome_msg).setCancelable(false)
		.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int id) {
				mSettings.edit().putBoolean(Settings.FIRST_BOOT, true).commit();
				
				LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
				
				/*
				 *  if the gps provider is disabled, then ask user if they want to enable it
				 *  else display the gps progress
				 */
				if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
					enableGPSdialog(Main.this);
				
				/*
				 * the map is no longer needed, clear it from memory
				 */
				mMap = null;
			}
		})
		.show();
		
	}
	
//	/**
//	 * displays lic dialog and welcome to find my car dialog
//	 */
//	public void eulaAlert (){
//		new AlertDialog.Builder(this)
//		.setTitle(R.string.eula)
//		.setMessage(R.string.eulaagreement).setCancelable(false)
//		.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
//			public void onClick(DialogInterface dialog, int id) {
//				mSettings.edit().putBoolean(Settings.ACCEPTED, true).commit();
//				update();
//			}
//		})
//		.setNegativeButton(R.string.decline, new DialogInterface.OnClickListener() {
//			public void onClick(DialogInterface dialog, int id) {
//				finish();
//			}
//		}
//		)
//		.show();
//	}
	
	/**
	 * called when a car is deleted
	 * (non-Javadoc)
	 * @see com.TwentyCodes.android.FindMyCarLib.UI.fragments.MapFragment.MapFragmentListener#onCarDeleted()
	 */
	@Override
	public void onCarDeleted() {
		mNotes.delete();
		mDirectionsFragment.clear();
	}
	
	/**
	 * called when a new car is marked
	 * (non-Javadoc)
	 * @see com.TwentyCodes.android.FindMyCarLib.UI.fragments.MapFragment.MapFragmentListener#onCarMarked(com.google.android.maps.GeoPoint)
	 */
	@Override
	public void onCarMarked(final GeoPoint point) {
		new Thread( new Runnable(){
			@Override
			public void run(){
				Location location = new Location("location");
				location.setLatitude(point.getLatitudeE6() /1e6);
				location.setLongitude(point.getLongitudeE6() /1e6);
				
				final String address = ReverseGeocoder.getAddressFromLocation(location);
				runOnUiThread( new Runnable(){
					@Override
					public void run(){
						mNotes.setAddressText(address);
					}
				});
			}
		}).start();
	}
	
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
		setContentView(R.layout.main);
		
		mSettings = this.getSharedPreferences(Settings.SETTINGS, 0);
		mMap = new MapFragment();
		mMap.setMapFragmentListener(this);
		
		//only display ads if this is the lite version
		if(!isFull){
			AdView ad = (AdView) findViewById(R.id.ad);
			ad.setVisibility(View.VISIBLE);
			ad.loadAd(new AdRequest());
		}
		
		if(icicle == null){
			//remove  notification from notification bar 
			NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			notificationManager.cancel(0);
			
		}
		
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
		
		/*
		 * Page titles
		 */
		String[] titles = new String[]{
				getString(R.string.directions),
				getString(R.string.map),
				getString(R.string.notes)
		};
		
		/*
		 * page icons
		 */
		int[] icons = new int[]{
				R.drawable.nav_action_bar,
				R.drawable.map_action_bar,
				R.drawable.notes_action_bar
		};
		
		mFragments = new ArrayList<Fragment>();
		
		mDirectionsFragment = new DirectionsListFragment(this);
		mFragments.add(mDirectionsFragment);
		mFragments.add(mMap);
		mNotes = new NotesFragment();
		mFragments.add(mNotes);
		
		//Populate the pager
		mPager = (CustomViewPager) findViewById(R.id.pager);
		
		/*
		 * this hack is for displaying nav
		 * empty msg
		 */
		mPager.setCurrentItem(0);
		mPager.setCurrentItem(2);
		mPager.setCurrentItem(1);
		
		
		if(mPager != null)
			mPager.setAdapter(new TitledFragmentAdapter(this.getSupportFragmentManager(), mFragments, titles, icons));
		
		//populate the pager's indicator
		mIndicator = (TitlePageIndicator)findViewById(R.id.indicator);
		if(mIndicator != null)
			mIndicator.setViewPager(mPager);
		
		mPager.setCurrentItem(1);
		mIndicator.setCurrentItem(1);
		mPager.setPagingEnabled(false);
		
		mIndicator.setOnPageChangeListener(this);
	}
	
	/**
	 * called when directions are displayed
	 * (non-Javadoc)
	 * @see com.TwentyCodes.android.FindMyCarLib.UI.fragments.MapFragment.MapFragmentListener#onDirectionsDisplayed(java.util.ArrayList, java.util.ArrayList, java.util.ArrayList, java.util.ArrayList, java.lang.String)
	 */
	@Override
	public void onDirectionsDisplayed(final DirectionsOverlay directions) {
		this.runOnUiThread(new Runnable(){
			@Override
			public void run(){
				mDirectionsFragment.setDirections(directions);
				mPager.setCurrentItem(2);
				
				mPager.setCurrentItem(0);
				mIndicator.setCurrentItem(0);
			}
		});
	}

	/**
	 * called when a direction is selected
	 * (non-Javadoc)
	 * @see com.TwentyCodes.android.location.DirectionsListFragment.OnDirectionSelectedListener#onDirectionSelected(com.google.android.maps.GeoPoint)
	 */
	@Override
	public void onDirectionSelected(GeoPoint point) {
		if(mMap != null) {
			mMap.panToGeoPoint(point, true);
			mPager.setCurrentItem(1);
			mIndicator.setCurrentItem(1);
		}
	}
	
	
	
	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// unused
		
	}

	/**
	 * called when the pager's page is changed
	 * we use this to dismiss the soft keyboard
	 * (non-Javadoc)
	 * @see android.support.v4.view.ViewPager.OnPageChangeListener#onPageScrollStateChanged(int)
	 */
	@Override
	public void onPageScrollStateChanged(int arg0) {
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(mPager.getWindowToken(), 0);
	}
	
	@Override
	public void onPageSelected(int arg0) {
		// unused
		
	}
	
	/**
	 * called when the activity is going to be paused
	 * we stop all location based services and release the wake lock if there is one
	 * @author ricky barrette
	 */
	@Override
	protected void onPause() {
		Log.i(TAG,"onPause()");
		
		removeSplashScreen();
		
		//remove wake lock if it is enabled
		if(mWakeLock.isHeld())
			mWakeLock.release();
		
		super.onPause();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.i(TAG,"onResume()");
		
		//set stay awake to preference
		if(mSettings.getBoolean(Settings.STAY_AWAKE, false)){
			if(!mWakeLock.isHeld()){
				mWakeLock.acquire();
			}
		}
			
	}
	
	/**
	 * (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onSaveInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onSaveInstanceState(Bundle icicle) {
		if(mSplashDialog != null)
			icicle.getBoolean(SPLASH, mSplashDialog.isShowing());
		super.onSaveInstanceState(icicle);
	}

	/**
	 * called when activity is stopped. lifecycle method.
	 * @author wwpowers
	 */
	@Override
	protected void onStop() {
		Log.i(TAG,"onStop()");
		
		if(mWakeLock.isHeld()){
			mWakeLock.release();
		}
		
		super.onStop();
		
	}
	
	/**
	 * displays a quit dialog
	 * @since 0.0.2
	 * @author ricky barrette 3-30-2010
	 */
	public void quitDialog(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getText(R.string.quit_dialog)).setCancelable(false)
		.setPositiveButton(getText(R.string.yes), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				finish();
			}
		})
		.setNegativeButton(getText(R.string.no), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		builder.show();
	}
	
	/**
	 * Removes the Dialog that displays the splash screen
	 */
	protected void removeSplashScreen() {
	    if (mSplashDialog != null) {
	        mSplashDialog.dismiss();
	        mSplashDialog = null;
	    }
	}

}