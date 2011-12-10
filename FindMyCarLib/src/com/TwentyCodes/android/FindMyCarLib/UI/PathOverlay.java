/**
 * PathOverlay.java
 * @date Nov 11, 2011
 * @author ricky barrette
 * @author Twenty Codes, LLC
 */
package com.TwentyCodes.android.FindMyCarLib.UI;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

/**
 * This overlay class is used to draw a path and points on a map
 * @author ricky barrette
 */
public class PathOverlay extends Overlay {

	private static final int PATH = 0;
	private static final int POINT = 1;
	private GeoPoint mStart;
	private GeoPoint mEnd;
	private int mColor;
	private int mMode;
	private int mRadius;

	/**
	 * Creates a new PathOverlay in path mode
	 * @author ricky barrette
	 */
	public PathOverlay(GeoPoint start, GeoPoint end, int color) {
		mStart = start;
		mEnd = end;
		mColor = color;
		mMode = PATH;
	}
	
	/**
	 * Creates a new PathOverlay in point mode. This is used to draw end points.
	 * @param point
	 * @param radius
	 * @param color
	 * @author ricky barrette
	 */
	public PathOverlay(GeoPoint point, int radius, int color){
		mMode = POINT;
		mRadius = radius;
		mStart = point;
	}
	
	/**
	 * 
	 * @param canvas canvas to be drawn on
	 * @param mapView 
	 * @param shadow 
	 * @param when
	 */
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		Projection projection = mapView.getProjection();
		Paint paint = new Paint();
		paint.setColor(mColor);
		paint.setAntiAlias(true);
		Point point = new Point();
		projection.toPixels(mStart, point);
		
		switch (mMode){
			case POINT:
				RectF oval = new RectF(point.x - mRadius, point.y - mRadius, point.x + mRadius, point.y + mRadius);
				canvas.drawOval(oval, paint);
			case PATH:
				Point point2 = new Point();
				projection.toPixels(mEnd, point2);
				paint.setStrokeWidth(5);
				paint.setAlpha(120);
				canvas.drawLine(point.x, point.y, point2.x, point2.y, paint);
		}
		super.draw(canvas, mapView, shadow);
	}
}