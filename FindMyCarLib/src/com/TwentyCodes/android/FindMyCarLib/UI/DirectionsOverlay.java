/**
 * DirectionsOverlay.java
 * @date Nov 10, 2011
 * @author ricky barrette
 * @author Twenty Codes, LLC
 */
package com.TwentyCodes.android.FindMyCarLib.UI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Color;
import android.util.Log;

import com.TwentyCodes.android.FindMyCarLib.debug.Debug;
import com.TwentyCodes.android.location.MapView;
import com.google.android.maps.GeoPoint;

/**
 * This Overlay class will be used to display provided by the Google Directions API on a map
 * @author ricky barrette
 */
public class DirectionsOverlay {
	
	private static final String TAG = "DirectionsOverlay";
	ArrayList<PathOverlay> mPath;
	ArrayList<String> mDirections;
	private MapView mMapView;
	private OnDirectionsCompleteListener mListener;
	private String mCopyRights;
	private ArrayList<GeoPoint> mPoints;
	private ArrayList<String> mDistance;
	private ArrayList<String> mDuration;
	private ArrayList<String> mWarnings;
	
	/**
	 * Downloads and Creates a new DirectionsOverlay from the provided points
	 * @param origin point
	 * @param destination point
	 * @author ricky barrette
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 * @throws IllegalStateException 
	 * @throws JSONException 
	 */
	public DirectionsOverlay(MapView map, GeoPoint origin, GeoPoint destination, OnDirectionsCompleteListener listener) throws IllegalStateException, ClientProtocolException, IOException, JSONException {
		mMapView = map;
		mListener = listener;
		String json = downloadJSON(generateUrl(origin, destination));
		drawPath(json);
	}
	
	/**
	 * Creates a new DirectionsOverlay from the provided String JSON
	 * @param json
	 * @throws JSONException
	 * @author ricky barrette
	 */
	public DirectionsOverlay(MapView map, String json, OnDirectionsCompleteListener listener) throws JSONException{
		mListener = listener;
		mMapView = map;
		drawPath(json);
	}
	
	/**
	 * Downloads Google Directions JSON from the Internet 
	 * @param url
	 * @return
	 * @throws IllegalStateException
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @author ricky barrette
	 */
	private String downloadJSON(String url) throws IllegalStateException, ClientProtocolException, IOException {
		if(Debug.DEBUG)
			Log.d(TAG, url);
		if(url == null)
			throw new NullPointerException();
		StringBuffer response = new StringBuffer();
		BufferedReader br = new BufferedReader(new InputStreamReader(new DefaultHttpClient().execute(new HttpGet(url)).getEntity().getContent()));
		String buff = null;
		while ((buff = br.readLine()) != null)
			response.append(buff);
		return response.toString();
	}
	
	/**
	 * Creates a new DirectionsOverlay from the json provided
	 * @param json of Google Directions API
	 * @author ricky barrette
	 * @return 
	 * @throws JSONException 
	 */
	public void drawPath(String json) throws JSONException{
		if(Debug.DEBUG){
			Log.d(TAG, "drawPath");
			Log.d(TAG, json);
		}
		mPath = new ArrayList<PathOverlay>();
		mDirections = new ArrayList<String>();
		mPoints = new ArrayList<GeoPoint>();
		mDistance = new ArrayList<String>();
		mDuration = new ArrayList<String>();
		
		//get first route
		JSONObject route = new JSONObject(json).getJSONArray("routes").getJSONObject(0);
		
		mCopyRights = route.getString("copyrights");
		//route.getString("status");
		
		JSONObject leg = route.getJSONArray("legs").getJSONObject(0);
		getDistance(leg);
		getDuration(leg);
//		mMapView.getOverlays().add(new PathOverlay(getGeoPoint(leg.getJSONObject("start_location")), 12, Color.GREEN));
//		mMapView.getOverlays().add(new PathOverlay(getGeoPoint(leg.getJSONObject("end_location")), 12, Color.RED));
		
		leg.getString("start_address");
		leg.getString("end_address");
		
//		JSONArray warnings = leg.getJSONArray("warnings");
//		for(int i = 0; i < warnings.length(); i++){
//			mWarnings.add(warnings.get)w
//		}w
		
		/*
		 * here we will parse the steps of the directions
		 */
		if(Debug.DEBUG)
			Log.d(TAG, "processing steps");
		JSONArray steps = leg.getJSONArray("steps");
		JSONObject step = null;
		for(int i = 0; i < steps.length(); i++){
			if(Debug.DEBUG)
				Log.d(TAG, "step "+i);
			
			step = steps.getJSONObject(i);
			
			if(Debug.DEBUG){
				Log.d(TAG, "start "+getGeoPoint(step.getJSONObject("start_location")).toString());
				Log.d(TAG, "end "+getGeoPoint(step.getJSONObject("end_location")).toString());
			}
			
//			if(Debug.DEBUG)
//				mMapView.getOverlays().add(new PathOverlay(getGeoPoint(step.getJSONObject("start_location")), getGeoPoint(step.getJSONObject("end_location")), Color.MAGENTA));
			
			decodePoly(step);

			mDuration.add(getDuration(step));
			
			mDistance.add(getDistance(step));
			
			mDirections.add(step.getString("html_instructions"));
//			Log.d("TEST", step.getString("html_instructions"));
			mPoints.add(getGeoPoint(step.getJSONObject("start_location")));
			
		}
		if(Debug.DEBUG)
			Log.d(TAG, "finished parsing");
		
		if(mMapView != null){
			mMapView.getOverlays().addAll(mPath);
			mMapView.postInvalidate();
		}
		
		if(mListener != null)
			mListener.onDirectionsComplete(DirectionsOverlay.this);
	}
	
	/**
	 * @param origin
	 * @param destination
	 * @return The Google API url for our directions 
	 * @author ricky barrette
	 */
	private String generateUrl(GeoPoint origin, GeoPoint destination){
		return "http://maps.googleapis.com/maps/api/directions/json?&origin="+
				Double.toString(origin.getLatitudeE6() / 1.0E6)+
				","+
				Double.toString(origin.getLongitudeE6() / 1.0E6)+
				"&destination="+
				Double.toString(destination.getLatitudeE6() / 1.0E6)+
				","+
				Double.toString(destination.getLongitudeE6() / 1.0E6)+
				"&sensor=true&mode=walking";
	}
	
	
	/**
	 * Deocodes googles polyline
	 * @param encoded
	 * @return a list of geopoints representing the path 
	 * @author Mark McClure http://facstaff.unca.edu/mcmcclur/googlemaps/encodepolyline/
	 * @author ricky barrette
	 * @throws JSONException 
	 */
	private void decodePoly(JSONObject step) throws JSONException {
		if(Debug.DEBUG)
			Log.d(TAG, "decodePoly");

		String encoded = step.getJSONObject("polyline").getString("points");
	    int index = 0, len = encoded.length();
	    int lat = 0, lng = 0;

	    GeoPoint last = null;
	    while (index < len) {
	        int b, shift = 0, result = 0;
	        do {
	            b = encoded.charAt(index++) - 63;
	            result |= (b & 0x1f) << shift;
	            shift += 5;
	        } while (b >= 0x20);
	        int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
	        lat += dlat;

	        shift = 0;
	        result = 0;
	        do {
	            b = encoded.charAt(index++) - 63;
	            result |= (b & 0x1f) << shift;
	            shift += 5;
	        } while (b >= 0x20);
	        int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
	        lng += dlng;

	        GeoPoint p = new GeoPoint((int) (((double) lat / 1E5) * 1E6), (int) (((double) lng / 1E5) * 1E6));
	        
	        if(Debug.DEBUG){
				Log.d(TAG, "current = "+ p.toString());
				if(last != null)
					Log.d(TAG, "last = "+ last.toString());
	        }
	        
	        
	        if(last != null)
	        	mPath.add(new PathOverlay(last, p, Color.RED));
	        
	        last = p;
	    }
	    
	}
	
	/**
	 * Converts a JSON location object into a GeoPoint
	 * @param point
	 * @return Geopoint parsed from the provided JSON Object
	 * @throws JSONException
	 * @author ricky barrette
	 */
	private GeoPoint getGeoPoint(JSONObject point) throws JSONException{
		return new GeoPoint((int) (point.getDouble("lat")*1E6), (int) (point.getDouble("lng")*1E6));
	}
	
	/**
	 * @param step
	 * @return the duration of a step 
	 * @throws JSONException
	 * @author ricky barrette
	 */
	private String getDuration(JSONObject step) throws JSONException{
		return step.getJSONObject("duration").getString("text");
	}
	
	/**
	 * @param step
	 * @return the distance of a step
	 * @throws JSONException
	 * @author ricky barrette
	 */
	private String getDistance(JSONObject step) throws JSONException{
		return step.getJSONObject("distance").getString("text");
	}

	/**
	 * @return the array of PathOverlays
	 * @author ricky barrette
	 */
	public ArrayList<PathOverlay> getPath(){
		return mPath;
	}
	
	/**
	 * Removes the directions overlay from the map view
	 * @author ricky barrette
	 */
	public void removePath() {
		if(mMapView.getOverlays().removeAll(mPath));		
	}

	/**
	 * @author ricky barrette
	 */
	public interface OnDirectionsCompleteListener{
		public void onDirectionsComplete(DirectionsOverlay directionsOverlay);
	}

	/**
	 * @return
	 * @author ricky barrette
	 */
	public ArrayList<String> getDirections() {
		return mDirections;
	}

	/**
	 * @return
	 * @author ricky barrette
	 */
	public ArrayList<GeoPoint> getPoints() {
		return mPoints;
	}

	/**
	 * @return
	 * @author ricky barrette
	 */
	public ArrayList<String> getDurations(){
		return mDuration;
	}
	
	/**
	 * @return
	 * @author ricky barrette
	 */
	public ArrayList<String> getDistances(){
		return mDistance;
	}

	/**
	 * @return
	 * @author ricky barrette
	 */
	public String getCopyrights(){
		return mCopyRights;
	}

	/**
	 * @return
	 * @author ricky barrette
	 */
	public ArrayList<String> getWarnings() {
		return mWarnings;
	}
}