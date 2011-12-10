/**
 * @author Twenty Codes
 * @author - WWPowers 3-26-2010
 * @author ricky barrette 9-29-2010
 */

package com.TwentyCodes.android.FindMyCarLib.UI;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;

import com.TwentyCodes.android.FindMyCarLib.R;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

/**
 * this class will be used as an overlay that will be added to a mapview's <overlay> list. 
 * @author ricky barrette
 * @date Sep 29, 2010
 */
public class FindMyCarOverlay extends Overlay {
	
	public static final String TAG = "FindMyCarOverlay";
	private Context mContext;
	private GeoPoint mPoint;
	
	/**
	 * a simple car overlay item that will be added to the map view's overlay list
	 * @param context
	 * @param geopoint of where the overlay is representing
	 * @author ricky barrette
	 */
	public FindMyCarOverlay(Context context, GeoPoint geopoint) {
		mContext = context;
		mPoint = geopoint;
	}
	
	/**
	 * we override this methods so we can provide a drawable and a location to draw on the canvas.
	 * (non-Javadoc)
	 * @see com.google.android.maps.Overlay#draw(android.graphics.Canvas, com.google.android.maps.MapView, boolean)
	 * @param canvas
	 * @param mapView
	 * @param shadow
	 * @author ricky barrette
	 */
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow){
		Bitmap carBitmap = BitmapFactory.decodeResource( mContext.getResources(), R.drawable.car);
		Projection projection = mapView.getProjection();
		Point point = null; 
		point = projection.toPixels(mPoint, point);
		canvas.drawBitmap(
			carBitmap, 
			point.x - (carBitmap.getWidth()  / 2), 
			point.y - (carBitmap.getHeight() / 2), 
			null
		);
		super.draw(canvas, mapView, shadow);
	}
	
	/**
	 * displays the context menu when user taps on car overlay
	 * @author ricky barrette
	 */
	@Override
	public boolean onTap(GeoPoint point, MapView mapView){
		Point pointTap = mapView.getProjection().toPixels(point, null);
		Point pointMap = mapView.getProjection().toPixels(mPoint, null);
		if (pointTap.x - pointMap.x >= -25 
				&& pointTap.x - pointMap.x <= 25
				&& pointMap.y - pointTap.y >= -25
				&& pointMap.y - pointTap.y <= 25) {
			//TODO overlay was taped
			return true;
		}
		super.onTap(point, mapView);
		return true;
	}
}