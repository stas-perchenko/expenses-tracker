package com.alperez.expensestracker.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ViewFlipper;

import com.alperez.expensestracker.R;

/**
 * Created by stanislav.perchenko on 15-Sep-15.
 */
public class SlidingViewFlipper extends ViewFlipper {

    public SlidingViewFlipper(Context context) {
        super(context);
    }

    public SlidingViewFlipper(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getResources().obtainAttributes(attrs, R.styleable.SlidingViewFlipper);
        try {
            int id = a.getResourceId(R.styleable.SlidingViewFlipper_incrementInAnimation, -1);
            if (id > 0) {
                nextNewAnimation = AnimationUtils.loadAnimation(context, id);
            }
            id = a.getResourceId(R.styleable.SlidingViewFlipper_incrementOutAnimation, -1);
            if (id > 0) {
                nextOldAnimation = AnimationUtils.loadAnimation(context, id);
            }
            id = a.getResourceId(R.styleable.SlidingViewFlipper_decrementInAnimation, -1);
            if (id > 0) {
                prevNewAnimation = AnimationUtils.loadAnimation(context, id);
            }
            id = a.getResourceId(R.styleable.SlidingViewFlipper_decrementOutAnimation, -1);
            if (id > 0) {
                prevOldAnimation = AnimationUtils.loadAnimation(context, id);
            }
        } finally {
            a.recycle();
        }
    }


    private Animation nextOldAnimation;
    private Animation nextNewAnimation;
    private Animation prevOldAnimation;
    private Animation prevNewAnimation;

    @Override
    public void showNext() {
        setInAnimation(nextNewAnimation);
        setOutAnimation(nextOldAnimation);
        super.showNext();
    }

    public void showNext(int absPageIndex) {
        setInAnimation(nextNewAnimation);
        setOutAnimation(nextOldAnimation);
        super.setDisplayedChild(absPageIndex);
    }

    @Override
    public void showPrevious() {
        setInAnimation(prevNewAnimation);
        setOutAnimation(prevOldAnimation);
        super.showPrevious();
    }

    public void showPrevious(int absPageIndex) {
        setInAnimation(prevNewAnimation);
        setOutAnimation(prevOldAnimation);
        super.setDisplayedChild(absPageIndex);
    }

    public void showFirst() {
        setInAnimation(prevNewAnimation);
        setOutAnimation(prevOldAnimation);
        super.setDisplayedChild(0);
    }



    @Override
    protected void onDetachedFromWindow() {
        try {
            super.onDetachedFromWindow();
        } catch (IllegalArgumentException e) {
            stopFlipping();
        }
    }

}
