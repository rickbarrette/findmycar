/**
 * @author Twenty Codes
 * @author WWPowers
 * @author ricky barrette
 * @date 3-26-2010
 */
package com.TwentyCodes.android.FindMyCarLib.UI.fragments;

import java.io.IOException;
import java.text.DecimalFormat;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.TwentyCodes.android.FindMyCarLib.Main;
import com.TwentyCodes.android.FindMyCarLib.ParkignTimerActivity;
import com.TwentyCodes.android.FindMyCarLib.R;
import com.TwentyCodes.android.FindMyCarLib.Settings;
import com.TwentyCodes.android.FindMyCarLib.UI.DirectionsOverlay;
import com.TwentyCodes.android.FindMyCarLib.UI.DirectionsOverlay.OnDirectionsCompleteListener;
import com.TwentyCodes.android.FindMyCarLib.UI.FindMyCarOverlay;
import com.TwentyCodes.android.SkyHook.SkyHookUserOverlay;
import com.TwentyCodes.android.location.GeoPointLocationListener;
import com.TwentyCodes.android.location.GeoUtils;
import com.TwentyCodes.android.location.MapView;
import com.TwentyCodes.android.location.MidPoint;
import com.google.android.maps.GeoPoint;

/**
 * this is the main class FindMyCar Full
 * @author WWPowers
 * @author ricky barrette
 */
public class MapFragment extends Fragment implements GeoPointLocationListener, OnDirectionsCompleteListener, OnClickListener {

	public boolean hasLeftCar;
	public boolean isCarFound;
//	private boolean isShowingBoth = false;
	private static final int ACCURACY = 0;
	private static final int DISTANCE = 1;
	private static final int FOUND_CAR = 2;
	private static final int SHOWBOTH = 3;
	protected static final int MIDPOINT = 4;
	private static final String TAG = "FindMyCarFull";
	public static FindMyCarOverlay mCarOverlay;
	public static GeoPoint mCarPoint;
	public static TextView mDistance;
	public static boolean isMetric = true;
	public static TextView mAccuracy;
	public static SkyHookUserOverlay mUserOverlay;
	public static MapView mMapView;
	private SharedPreferences mSettings;
	private Handler mHandler;
	private ProgressDialog mProgress;
	private boolean isGPSDialogEnabled = true;
	protected DirectionsOverlay mDirections;
	private MapFragmentListener mListener;
	
	public interface MapFragmentListener{
		public void onCarDeleted();
		public void onDirectionsDisplayed(DirectionsOverlay directions);
	}
	
	/**
	 * returns a string distance that is based on the users measurement unit preference 
	 * @param distance in kilometers
	 * @return string distance
	 * @author ricky barrette
	 */
	public static String distance(double distance) {
		DecimalFormat threeDForm = new DecimalFormat("#.###");
		DecimalFormat twoDForm = new DecimalFormat("#.##"); 
		
		/*
		 * if blnUnit is true, the distance computed will be in metric units, 
		 * else, standard units are used
		 * meters are used until 1 kilometer is reached, then kilometers are used
		 * feet are used until 1 mile is reached, then miles are used
		 */
		if(isMetric){
			if (distance < 1){
				distance = distance * 1000;
				return twoDForm.format(distance) +" m";
			}
			return threeDForm.format(distance) +" Km";
		}
		distance = distance / 1.609344;
		if (distance < 1){
			distance = distance * 5280;
			return twoDForm.format(distance) +" ft";
		}
		return twoDForm.format(distance) +" mi";
	}

	/**
	 * pans maps to where the a geopoint is, and if zoomIn is true, zooms in to level 20
	 * @param GeoPoint point - lat and lon of point to pan to
	 * @param boolean zoomIn - true if map needs to be zoomed in
	 * @return boolean false it geopoint is null
	 * @author ricky barrette
	 */
	public boolean panToGeoPoint(GeoPoint point, boolean zoomIn) {
		if (point != null) {
			if (mMapView != null) {
				try {
					mMapView.getController().stopAnimation(false);
					mMapView.getController().setCenter(point);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (zoomIn) {
					mMapView.getController().setZoom((mMapView.getMaxZoomLevel() - 2));
				}
			} else {
				Log.e(TAG, "panToGeoPoint call. mapcontroller was null");
			}
		} else {
			Log.e(TAG, "panToGeoPoint call. geopoint was null");
			return false;
		}
		return true;
	}
	
	/**
	 * removes the car overlay from the mapview.
	 * @return true if successful
	 * @author ricky barrette
	 */
	public boolean removeCar(){
		
		mCarPoint = null;
		mDistance.setText("0");
		mSettings.edit().remove(Settings.LAT).remove(Settings.LON).commit();
		if(mListener != null)
			mListener.onCarDeleted();
		if(mDirections != null) {
			mDirections.removePath();
			mDirections = null;
		}
		
		try {
			mMapView.getOverlays().remove(mCarOverlay);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public MapView getMap(){
		return mMapView;
	}

	/**
	 * loads saved settings from files
	 * @author ricky barrette
	 */
	private void loadSettings(){
		
		int lat = mSettings.getInt(Settings.LAT, 0);//mFileStream.readInteger(getString(R.string.lat)); 
		int lon = mSettings.getInt(Settings.LON, 0);//mFileStream.readInteger(getString(R.string.lon));
		
		//sets car geopoint up if lat and lon != 0
		if (lat != 0 && lon != 0) {
	        setCar(new GeoPoint(lat, lon));
		}

		//sets measurement unit preference
		String mu = mSettings.getString(Settings.MEASUREMENT_UNIT, null);
		if(mu != null){
			if(mu.equalsIgnoreCase("Standard")){
				isMetric = false;
			}
			if(mu.equalsIgnoreCase("Metric")){
				isMetric = true;
			}
		}
		
		//load compass options
		String compass_option = mSettings.getString(Settings.COMPASS_OPTION, "Small");
		int px;
		Resources r = getResources();
		if(compass_option.equalsIgnoreCase("Large")){
			px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 110, r.getDisplayMetrics());
			mUserOverlay.setCompassDrawables(R.drawable.needle_lrg, R.drawable.compass_lrg, px, px);
		} else if(compass_option.equalsIgnoreCase("Small")){
			px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, r.getDisplayMetrics());
			mUserOverlay.setCompassDrawables(R.drawable.needle_sm, R.drawable.compass_sm, px, px);
		} else {
			px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 70, r.getDisplayMetrics());
			mUserOverlay.setCompassDrawables(R.drawable.needle_med, R.drawable.compass_med, px, px);
		}
		 
	}
	
	/**
	 * using the users lat/lon saves car location to lat/lon files and passes that geopoint info to setCar
	 * also writes address to notes
	 * @author ricky barrette 3-31-2010
	 * @author WWPowers 3-31-2010
	 */
	private void markCar() {
		//removed old parking timer
//		ParkingTimerDialog.stopTimer(this);
		
		GeoPoint user = mUserOverlay.getUserLocation();
		
		/*
		 * if the user location is not null then
		 * save car lat and lon to files
		 * pass geopoint info to set car, which will setup and show the car overlay
		 * get address info and add it to the notes file
		 * 
		 * else inform user that they dont have a gps signal
		 */
		if (user != null){
			mSettings.edit()
				.putInt(Settings.LAT, user.getLatitudeE6())
				.putInt(Settings.LON, user.getLongitudeE6())
				.commit();
			
			setCar(user);
			
			//TODO get address
			
		} else {
			Toast.makeText(getActivity(), R.string.no_gps_signal, Toast.LENGTH_LONG).show();
		}
	}
	/**
	 * ask user if they want to replace current car marker with a new one
	 * @since 0.1.1
	 * @author ricky barrette
	 */
	public void markCarDialog(){
		new AlertDialog.Builder(getActivity())
		.setMessage(R.string.mark_car_warning).setCancelable(false)
		.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				removeCar();
				markCar();
				dialog.cancel();
			}
		})
		.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		})
		.show();
	}
	
	/**
	 * (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreateOptionsMenu(android.view.Menu, android.view.MenuInflater)
	 */
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) { 
		inflater.inflate(R.menu.map_action_bar, menu);
	}
	
	/* (non-Javadoc)
	 * @see com.google.android.maps.MapActivity#onCreate(android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		container.removeAllViews();
		
		View view = inflater.inflate(R.layout.map, container, false);
		
		mMapView = (MapView) view.findViewById(R.id.mapview);
		setUiHandler();
		
		mAccuracy = (TextView) view.findViewById(R.id.tvAccuracy2);
		mDistance = (TextView) view.findViewById(R.id.tvDistance2);
		mSettings = getActivity().getSharedPreferences(Settings.SETTINGS, Context.MODE_WORLD_WRITEABLE);
		
		view.findViewById(R.id.my_location).setOnClickListener(this);
		view.findViewById(R.id.mark_my_location).setOnClickListener(this);
		view.findViewById(R.id.show_both).setOnClickListener(this);
		view.findViewById(R.id.parking_timer).setOnClickListener(this);
		view.findViewById(R.id.directions).setOnClickListener(this);
		return view;
	}
	
	@Override
	public void onDirectionsComplete(final DirectionsOverlay directionsOverlay) {
		if(mProgress != null){
			mProgress.dismiss();
			mProgress = null;
		}
		if(mListener != null)
			mListener.onDirectionsDisplayed(directionsOverlay);
	}
	/**
	 * here we will overrride onLocationChanged() so we can update the FindMyCarUI
	 * (non-Javadoc)
	 * @see com.TwentyCodes.android.SkyHook.map.SkyHookUserOverlay#onLocationChanged(com.google.android.maps.GeoPoint, int)
	 * @param point
	 * @param accuracy
	 * @author Ricky Barrette
	 */
	@Override
	public void onLocationChanged(final GeoPoint point, final int accuracy){
		
		Log.d(TAG, "FMC onLocationChanged()");
				
		new Thread(new Runnable(){
			@Override
			public void run(){
				mHandler.sendMessage(mHandler.obtainMessage(ACCURACY, distance(accuracy/1E3)));
				
//				if (point != null)
//					if(isShowingBoth)
//						if(GeoUtils.isPointOffMap(mMapView,point) || GeoUtils.isPointOffMap(mMapView, mCarPoint))
//							mHandler.sendEmptyMessage(SHOWBOTH);
//				
				if (mCarPoint != null && point != null){
					double distance = GeoUtils.distanceKm(point, mCarPoint);
					mHandler.sendMessage(mHandler.obtainMessage(DISTANCE, distance(distance)));

					//value is set in KM. if user has gone 30 feet from car app is set to check for arrival
					if (distance >  0.009144){ 
						hasLeftCar = true;
					}
					//if user has gone back into 30 foot radius and has not found the car and has left the car then notify user of finding of car
					if (distance <=  0.009144 && isCarFound == false && hasLeftCar == true){
						isCarFound = true;
						
						mHandler.sendEmptyMessage(FOUND_CAR);
					}
				}				
			}
		}).start();
    		
	}

	/**
	 * handles menu selection 
	 * @since 0.0.2
	 * @author ricky barrette 3-30-2010
	 */
	@Override
	public boolean onOptionsItemSelected (MenuItem item) {	
		if(item.getItemId() == R.id.delete_car)
			removeCar();
		else if (item.getItemId() == R.id.settings) { 
			startActivity(new Intent().setClass(getActivity(), Settings.class));
			return true;
		} else if (item.getItemId() == R.id.map_mode)
			changeMapMode();
		
		return false;
	}
	
	
	/**
	 * (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onPause()
	 */
	@Override
	public void onPause() {
		mUserOverlay.disableCompass();
		mUserOverlay.disableMyLocation();
		mMapView.getOverlays().remove(mUserOverlay);
		mUserOverlay = null;
		super.onPause();
	}
	
	/**
	 * Called each time the menu is created starting with the second instance. 
	 * Used to dynamicly modify the options menu
	 */
	
	/**
	 * (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onResume()
	 */
	@Override
	public void onResume() {
		if(mUserOverlay == null)
			mUserOverlay = new SkyHookUserOverlay(mMapView, getActivity());
		
		mMapView.getOverlays().add(mUserOverlay);
		
		//start all location based services
		mUserOverlay.enableMyLocation();
		if(!isGPSDialogEnabled)
			mUserOverlay.disableGPSDialog();
		
		mUserOverlay.registerListener(this);
		mUserOverlay.enableCompass();
		
		loadSettings();
		super.onResume();
	}
	
	/**
	 * reorders the overlays to the UserOverlay always on top
	 * @author ricky barrette
	 */
	private void reorderOverlays() {
		mMapView.getOverlays().remove(mUserOverlay);
		mMapView.getOverlays().add(mUserOverlay);
	}

	/**
	 * removes the previous car overlay and replaces it with a new car overlay that
	 * represents the users car at a specific geopoint
	 * 
	 * @param point for geopoint of car
	 * @author WWPowers 3-31-2010
	 * @author ricky barrette
	 */
	public void setCar(GeoPoint point) {
		isCarFound = false;
        hasLeftCar = false;
		mCarPoint = point;
		mCarOverlay = new FindMyCarOverlay(getActivity(), point);
		mMapView.getOverlays().add(mCarOverlay);
		mUserOverlay.setDestination(mCarPoint);
		reorderOverlays();
	}
	
	/**
	 * enables the GPS dialog
	 * @param b
	 * @author ricky barrette
	 */
	public void setGPSDialogEnabled(boolean b) {
		isGPSDialogEnabled = b;
		if(mUserOverlay != null)
			if(b)
				mUserOverlay.enableGPSDialog();
			else
				mUserOverlay.disableGPSDialog();		
	}
	
	/**
	 * Sets up the UI handler.
	 * The UI handler will process messages from processing threads
	 * 
	 * @author ricky barrette
	 */
	private void setUiHandler() {
		mHandler = new Handler(){
			@Override
			public void handleMessage(Message msg){
				switch(msg.what){
					case ACCURACY:
						mAccuracy.setText( (String) msg.obj );
						break;
					case DISTANCE:
						mDistance.setText((String) msg.obj);
						break;
					case FOUND_CAR:
						/* remove the directions overlay & delete all navigation files when the car is found
						 * this will prevent old directions from being displayed after the car is found.
						 */

						if(mDirections != null) {
							mDirections.removePath();
							mDirections = null;
						}
						
						Vibrator vib = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
						new AlertDialog.Builder(getActivity())
							.setTitle(R.string.yay)
							.setMessage(R.string.found_car).setCancelable(false)
							.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								
							}
						}).show();
						vib.vibrate(100);
						mDistance.setText("0");
						break;
					case SHOWBOTH:
						showBoth();
						break;
					case MIDPOINT:
						MidPoint mp = (MidPoint) msg.obj;
						panToGeoPoint(mp.getMidPoint(), false);
						mp.zoomToSpan(mMapView);
						break;
						
				}
			}
		};
	}

	/**
	 * computes a geopoint the is the central geopoint  between the user and the car. 
	 * also it zooms so both marks are visible on the map 
	 * @author ricky barrette
	 */
	protected void showBoth(){
		if(mUserOverlay != null) {
			if (mCarPoint == null){
				Toast.makeText(getActivity(), R.string.mark_car_first, Toast.LENGTH_LONG).show();
			} else if (mUserOverlay.getUserLocation() == null){
				Toast.makeText(getActivity(), R.string.no_gps_signal, Toast.LENGTH_LONG).show();
			} else {
				if (mMapView != null) {
					mMapView.getController().stopAnimation(false);
					mUserOverlay.followUser(false);
//					isShowingBoth = true;
					final GeoPoint user = mUserOverlay.getUserLocation();
					
					/*
					 * here we null check our next set of value before we send them off to geoutils
					 * if they have became null for some reason we disable show both mode
					 */
					if(mCarPoint!= null && user != null){
						new Thread(new Runnable(){
							@Override
							public void run(){
								mHandler.sendMessage(mHandler.obtainMessage(MIDPOINT, GeoUtils.midPoint(mCarPoint, user)));
							}
						}).start();
						
					} 
//					else 
//						isShowingBoth = false;
		
					
				} else {
					Log.e(TAG, "showBoth.mMapView is null");
				}
			}
		}
	}

	/**
	 * Sets the listener for this map fragment
	 * @param listener
	 * @author ricky barrette
	 */
	public void setMapFragmentListener(MapFragmentListener listener) {
		mListener = listener;
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.show_both)
			showBoth();
		else if(v.getId() == R.id.mark_my_location)	
			markMyLocation();
		else if (v.getId() == R.id.my_location)
			myLocation();
		else if (v.getId() == R.id.directions)
			directions();
		else if (v.getId() == R.id.map_mode)
			changeMapMode();
		else if (v.getId() == R.id.parking_timer)
			if (! Main.isFull)
				Main.featureInFullDialog(getActivity());
			else
				getActivity().startActivity(new Intent(getActivity(), ParkignTimerActivity.class));
	}

	/**
	 * Marks the user's location
	 * @author ricky barrette
	 */
	private void markMyLocation() {
		mUserOverlay.followUser(true);
//		isShowingBoth = false;
		
		/*
		 * if we have a gps signal, then pan to user location and then
		 * if there is no car, mark the car location as the users location
		 * else show mark car dialog 
		 * 
		 * we switch from MyLocationOverlay.getMyLocation() to referencing the static variable MyCustomLocationOverlay.gpUser
		 * because for some reason getMyLocation() would become null.
		 * @author ricky barrette 
		 */
		if (! panToGeoPoint(mUserOverlay.getUserLocation(), true)){
			Toast.makeText(getActivity(), R.string.no_gps_signal, Toast.LENGTH_LONG).show();
		} else {
			if (mCarPoint != null){
				markCarDialog();
			} else {
				markCar();
			}
		}
	}

	/**
	 * pans the map to the user's location
	 * @author ricky barrette
	 */
	private void myLocation() {
		mUserOverlay.followUser(true);
//		isShowingBoth = false;
		
		/*
		 * if we have a gps signal, then pan to user location 
		 * else notify user that there is no GPS signal
		 * 
		 * we switch from MyLocationOverlay.getMyLocation() to referencing the static variable MyCustomLocationOverlay.gpUser
		 * because for some reason getMyLocation() would become null.
		 * @author ricky barrette 
		 */
		if (! panToGeoPoint(mUserOverlay.getUserLocation(), true)){
			Toast.makeText(getActivity(), R.string.no_gps_signal, Toast.LENGTH_LONG).show();
		} 
	}

	/**
	 * Displays the walking directions on the map 
	 * @author ricky barrette
	 */
	private void directions() {
		if (Main.isFull) {
			/*
			 * if there is no car marked, then notify user
			 * else check to see if there is directions
			 */
			if (mCarPoint == null) {
				Toast.makeText(getActivity(), R.string.mark_car_first, Toast.LENGTH_LONG).show();
			} else {
				
				/*
				 * Remove old directions if the exist
				 */
				if(mDirections != null)
					mDirections.removePath();
				
					/* 
					 * if there is no location fix then notify user
					 * else download directions and display them	
					 */
					if (mUserOverlay.getUserLocation() == null) {
						Toast.makeText(getActivity(), R.string.no_gps_signal, Toast.LENGTH_LONG).show();
					} else {
						
						mProgress = ProgressDialog.show(getActivity(), getText(R.string.directions), getText(R.string.calculating), true);
						new Thread(new Runnable(){

							/**
							 * Notifys user about the error that occurred outside of the UI thread
							 * @param e
							 * @author ricky barrette
							 */
							public void notify(final Exception e){
								e.printStackTrace();
								getActivity().runOnUiThread(new Runnable(){
									@Override
									public void run(){
										Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
										mProgress.dismiss();
									}
								});
							}
							
							@Override
							public void run(){
								try {
									mDirections = new DirectionsOverlay(mMapView, mUserOverlay.getUserLocation(), mCarPoint, MapFragment.this);
								} catch (IllegalStateException e) {
									notify(e);
								} catch (ClientProtocolException e) {
									notify(e);
								} catch (IOException e) {
									notify(e);
								} catch (JSONException e) {
									notify(e);
								}									
							}
						}).start();

					}
			}
		
		} else
			Main.featureInFullDialog(getActivity());
	}

	/**
	 * changes the map mode
	 * @author ricky barrette
	 */
	private void changeMapMode() {
		if(mMapView != null)
			mMapView.setSatellite(!mMapView.isSatellite());
	}
		
}