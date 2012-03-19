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
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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
import com.TwentyCodes.android.FindMyCarLib.debug.Debug;
import com.TwentyCodes.android.SkyHook.SkyHookRegistration;
import com.TwentyCodes.android.exception.ExceptionHandler;
import com.TwentyCodes.android.fragments.DirectionsListFragment.OnDirectionSelectedListener;
import com.TwentyCodes.android.location.ReverseGeocoder;
import com.TwentyCodes.android.overlays.DirectionsOverlay;
import com.google.ads.AdRequest;
import com.google.ads.AdView;
import com.google.android.maps.GeoPoint;
import com.jakewharton.android.viewpagerindicator.TitlePageIndicator;
import com.jakewharton.android.viewpagerindicator.TitledFragmentAdapter;
import com.skyhookwireless.wps.RegistrationCallback;
import com.skyhookwireless.wps.WPSContinuation;
import com.skyhookwireless.wps.WPSReturnCode;

/**
 * This is the Main Activity of FMC Full & Lite
 * @author ricky barrette
 */
public class Main extends FragmentActivity implements RegistrationCallback, MapFragmentListener, OnPageChangeListener, OnDirectionSelectedListener {

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
	
	/**
	 * (non-Javadoc)
	 * @see com.skyhookwireless.wps._sdkjc#done()
	 * @author ricky barrette
	 */
	@Override
	public void done() {
		// UNUSED
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
	 * (non-Javadoc)
	 * @see com.skyhookwireless.wps._sdkjc#handleError(com.skyhookwireless.wps.WPSReturnCode)
	 * @param arg0
	 * @return
	 * @author ricky barrette
	 */
	@Override
	public WPSContinuation handleError(WPSReturnCode arg0) {
		
		Log.e(TAG,"there was an error regestering you "+ arg0.toString());
		return WPSContinuation.WPS_CONTINUE;
	}

	/**
	 * called when skyhook successfully registers a new user.
	 * @see com.skyhookwireless.wps.RegistrationCallback#handleSuccess()
	 * @author ricky barrette
	 */
	@Override
	public void handleSuccess() {
		Log.d(TAG,"successfully registered new user");
		mSettings.edit().putBoolean(Settings.IS_REGISTERED, true).commit();
	}
	
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
		
		if(icicle != null){
			if (icicle.containsKey(SPLASH)) 
				// Show splash screen if still loading
				if (icicle.getBoolean(SPLASH)) {
					showSplashScreen();
				}
			
			// Rebuild your UI with your saved state here
		} else {
			showSplashScreen();
			// Do your heavy loading here on a background thread
//			new Thread( new Runnable(){
//				@Override
//				public void run(){
					//registers user with skyhook
					new SkyHookRegistration(Main.this).registerNewUser(Main.this);
//					
//				}
//			}).start();
			
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
	 * parses all old save files from 2.0.6b34 and older into shared_prefs file settings.xml . it will parse the following files
	 * AppLat.txt ,
	 * AppLon.txt ,
	 * FirstBoot.txt ,
	 * StayAwake.txt ,
	 * Notes.txt ,
	 * Address.txt , 
	 * AppUnit.txt , & 
	 * AppService.txt
	 * @return true if files were committed successful to shared_prefs settings.xml
	 * @author ricky barrette
	 */
	private boolean parseOldSaveFilesToSharedPrefs(){
		/*
		 * get the file stream class to read all old files
		 * and get the editor for shared_prefs settings.xml
		 */
		FileStream fs = new FileStream(this);
		Editor editor = mSettings.edit();
		
		/*
		 * parse in the old files and save them
		 */
		editor.putInt(Settings.LAT, fs.readInteger("AppLat.txt"));
		editor.putInt(Settings.LON, fs.readInteger("AppLon.txt"));
		editor.putBoolean(Settings.STAY_AWAKE, fs.readBoolean("StayAwake.txt"));
		
		if(fs.readBoolean("AppUnit.txt")){
			editor.putString(Settings.MEASUREMENT_UNIT, "Metric");
		} else {
			editor.putString(Settings.MEASUREMENT_UNIT, "Standard");
		}
		
		editor.putBoolean(Settings.PARKING_TIMER_ALARM, fs.readBoolean("AppService.txt"));
		editor.putBoolean(Settings.FIRST_BOOT, fs.readBoolean("FirstBoot.txt"));
		editor.putString(Settings.NOTE, fs.readString("Notes.txt"));
		editor.putString(Settings.ADDRESS, fs.readString("Address.txt"));
	
		/*
		 * remove old files
		 */
		deleteFile("AppLat.txt");
		deleteFile("AppLon.txt");
		deleteFile("StayAwake.txt");
		deleteFile("AppUnit.txt");
		deleteFile("AppService.txt");
		deleteFile("FirstBoot.txt");
		deleteFile("Notes.txt");
		deleteFile("Address.txt");
		
		/*
		 * commit the changes
		 */
		return editor.commit();
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
	/**
	 * Shows the splash screen over the full Activity
	 */
	protected void showSplashScreen() {
//		mMap.setGPSDialogEnabled(false);
	    mSplashDialog = new Dialog(this, android.R.style.Theme_Translucent);
	    mSplashDialog.setContentView(R.layout.powered_by_skyhook);
	    mSplashDialog.setCancelable(false);
	    mSplashDialog.show();
	 
	    // Set Runnable to remove splash screen just in case
	    final Handler handler = new Handler();
	    handler.postDelayed(new Runnable() {
	      @Override
	      public void run() {
	        removeSplashScreen();
	        
	        /*
	         * uncomment the following to display the eula
	         */
//	      //loads first boot dialog if this is the first boot
//			if (! mSettings.getBoolean(Settings.ACCEPTED, false) || Debug.FORCE_FIRSTBOOT_DIALOG)
//				eulaAlert();
//			else
				update();
	      }
	    }, 2000);
	}
	/**
	 * check to see if there was an update installed. if the update needs to do any upgrades, it will be done here
	 * @author ricky barrette
	 */
	private void update() {
		
		/*
		 * get build number and compare to saved build number, then check to see if there is something we need to do
		 */
		try {
			int build_number = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionCode;
		
			/*
			 * if there is no build number saved && there is a FirstBoot.txt file then update old save file system to shared_prefs
			 */
			if(mSettings.getInt(Settings.BUILD_NUMBER, 0) == 0 && new FileStream(this).readBoolean("FirstBoot.txt")){
				Log.v(TAG, "updateding save files to shared_prefs");
				parseOldSaveFilesToSharedPrefs();
			}
			
			/*
			 * if this is the first time running this build display welcome dialog
			 */
			if(mSettings.getInt(Settings.BUILD_NUMBER, 0) < build_number || Debug.FORCE_FIRSTBOOT_DIALOG){
				displayWelcomeDialog();
			} 
			
			mSettings.edit().putInt(Settings.BUILD_NUMBER, build_number).commit();
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
	}	
}