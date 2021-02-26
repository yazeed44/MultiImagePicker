package net.yazeed44.imagepicker.util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class MotionEventFixedViewPager extends androidx.viewpager.widget.ViewPager {

    public MotionEventFixedViewPager(Context context) {
        super(context);
    }

    public MotionEventFixedViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        try {
            return super.onTouchEvent(ev);
        } catch (IllegalArgumentException ex) {
        }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException ex) {
        }
        return false;
    }
}