/**
 * DelegatedImageButton.java
 * @date Mar 9, 2012
 * @author ricky barrette
 * @author Twenty Codes, LLC
 */
package com.TwentyCodes.android.FindMyCarLib.UI;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.TouchDelegate;
import android.view.View;
import android.view.View.OnAttachStateChangeListener;
import android.widget.LinearLayout;

/**
 * This button is a simple image button that extends it's touch hit rect
 * @author ricky barrette
 */
public class DelegatedImageButton extends LinearLayout implements OnAttachStateChangeListener {

	/**
	 * @param context
	 * @author ricky barrette
	 */
	public DelegatedImageButton(Context context) {
		super(context);
		this.addOnAttachStateChangeListener(this);
	}

	/**
	 * @param context
	 * @param attrs
	 * @author ricky barrette
	 */
	public DelegatedImageButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.addOnAttachStateChangeListener(this);
	}

	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 * @author ricky barrette
	 */
	public DelegatedImageButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.addOnAttachStateChangeListener(this);
	}
	
	public void initDelegate(){
		final View parent = (View) this.getParent();
		parent.post(new Runnable() {
		    @Override
		    public void run() {
		        Rect bounds = new Rect();
		        DelegatedImageButton.this.getHitRect(bounds);
		        bounds.top -= 40;
		        bounds.left -= 40;
		        bounds.bottom += 40;
		        bounds.right += 40;
		        TouchDelegate touchDelegate = new TouchDelegate(bounds, DelegatedImageButton.this);
		 
		        if (View.class.isInstance(DelegatedImageButton.this.getParent())) {
		            ((View) DelegatedImageButton.this.getParent()).setTouchDelegate(touchDelegate);
		        }
		    }
		});
	}

	@Override
	public void onViewAttachedToWindow(View v) {
		initDelegate();
	}

	@Override
	public void onViewDetachedFromWindow(View v) {
		// UNUSED
	}
}