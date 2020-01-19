package com.airbnb.lottie;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by zujinhao on 2017/11/29.
 */

public class YXLottieAnimationView extends LottieAnimationView {
    private Runnable mPauseAnimationRunnable;

    public YXLottieAnimationView(Context context) {
        super(context);
        init();
    }

    public YXLottieAnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public YXLottieAnimationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        mPauseAnimationRunnable = new Runnable() {
            @Override
            public void run() {
                if (isAnimating()){
                    pauseAnimation();
                }
            }
        };
    }


    public void playAnimationDelayPaused(long delayed){
        playAnimation();
        if (delayed > getDuration()){
            return;
        }
        postDelayed(mPauseAnimationRunnable,delayed);
    }


    public void reset(){
        removeCallbacks(mPauseAnimationRunnable);
    }
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(mPauseAnimationRunnable);
    }
}
