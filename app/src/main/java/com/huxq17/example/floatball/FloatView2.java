package com.huxq17.example.floatball;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import static android.view.View.VISIBLE;


public class FloatView2 implements View.OnTouchListener {

    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParams;
    private float mLastX;
    private float mLastY;
    private boolean isHiddenWhenExit = false;
    private int mLayoutGravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
    private boolean isAdded;

    private FadeOutRunnable mFadeOutRunnable = new FadeOutRunnable();
    private boolean hasTouchFloatBall = false;


    private static final int TOP_STATUS_BAR_HEIGHT = 25;
    private static final int MAX_ELEVATION = 64;
    private Params params;

    private Context context;
    private View ball;
    private int maxMarginLeft;
    private int maxMarginTop;
    private int downX;
    private int downY;
    private int xDelta;
    private int yDelta;
    private DisplayMetrics dm;

    public FloatView2(Params params) {
        this.params = params;
        this.context = params.context;
        init();
    }

    public View getBall() {
        return ball;
    }

    public void setVisibility(int visibility) {
        if (ball != null) {
            ball.setVisibility(visibility);
        }
    }

    public void show() {
        if (mWindowManager == null) {
            createWindowManager();
        }
        if (mWindowManager == null) {
            return;
        }
        ViewParent parent = ball.getParent();
        if (parent != null && parent instanceof ViewGroup) {
            ((ViewGroup) parent).removeView(ball);
        }
        setVisibility(VISIBLE);
        if (!isAdded) {
            mWindowManager.addView(ball, mLayoutParams);
//            fadeOutFloatBall();
            isAdded = true;
        }
    }

    private void fadeOutFloatBall() {
        ball.removeCallbacks(mFadeOutRunnable);
        ball.postDelayed(mFadeOutRunnable, 2000);
    }

    /**
     * 设置WindowManager
     */
    private void createWindowManager() {
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mLayoutParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        } else {
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        mLayoutParams.format = PixelFormat.RGBA_8888;
        mLayoutParams.gravity = mLayoutGravity;
        mLayoutParams.width = params.width;
        mLayoutParams.height = params.height;
    }

    private void init() {
        if (params.ball == null) {
            ball = new View(context);
        } else {
            ball = params.ball;
        }

        if (params.resId != 0) {
            ball.setBackgroundResource(params.resId);
        }

        ball.setBackgroundColor(Color.RED);

        ball.setOnTouchListener(this);
        ViewCompat.setElevation(ball, MAX_ELEVATION);

        dm = context.getResources().getDisplayMetrics();
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;

//        ViewGroup.MarginLayoutParams layoutParams;
//        layoutParams = new ViewGroup.MarginLayoutParams(params.width, params.height);
//        maxMarginLeft = screenWidth - params.width;
//        layoutParams.width = params.width;
//        layoutParams.height = params.height;
//        layoutParams.leftMargin = maxMarginLeft - params.rightMargin;
//        maxMarginTop = screenHeight - params.height - (int) (context.getResources().getDisplayMetrics().density * TOP_STATUS_BAR_HEIGHT);
//        layoutParams.topMargin = maxMarginTop - params.bottomMargin;
//        layoutParams.bottomMargin = 0;
//        layoutParams.rightMargin = 0;
//
//        ball.setLayoutParams(layoutParams);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        final int touchX = (int) event.getRawX();
        final int touchY = (int) event.getRawY();
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                downX = touchX;
                downY = touchY;
                ViewGroup.MarginLayoutParams lParams = (ViewGroup.MarginLayoutParams) ball.getLayoutParams();
                xDelta = touchX - lParams.leftMargin;
                yDelta = touchY - lParams.topMargin;
                break;
            case MotionEvent.ACTION_UP:
                if (downX == touchX && downY == touchY) {
                    if (params.onClickListener != null) {
                        params.onClickListener.onClick(ball);
                    }
                } else {
                    Animation animation = new Animation() {
                        @Override
                        protected void applyTransformation(float interpolatedTime, Transformation t) {
                            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) ball
                                    .getLayoutParams();

                            int curLeftMargin = layoutParams.leftMargin;

                            if (touchX < dm.widthPixels / 2) {
                                layoutParams.leftMargin = (int) (curLeftMargin - curLeftMargin * interpolatedTime);
                            } else {
                                layoutParams.leftMargin = (int) (curLeftMargin + (maxMarginLeft - curLeftMargin) * interpolatedTime);
                            }

                            ball.setLayoutParams(layoutParams);
                        }
                    };
                    animation.setDuration(params.duration);
                    ball.startAnimation(animation);
                }

                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                break;
            case MotionEvent.ACTION_POINTER_UP:
                break;
            case MotionEvent.ACTION_MOVE:
                ViewGroup.MarginLayoutParams layoutParams;
                layoutParams = (ViewGroup.MarginLayoutParams) ball.getLayoutParams();

                int leftMargin;

                if (touchX - xDelta <= 0) {
                    leftMargin = 0;
                } else if (touchX - xDelta < maxMarginLeft) {
                    leftMargin = touchX - xDelta;
                } else {
                    leftMargin = maxMarginLeft;
                }

                layoutParams.leftMargin = leftMargin;

                int topMargin;

                if (touchY - yDelta <= 0) {
                    topMargin = 0;
                } else if (touchY - yDelta < maxMarginTop) {
                    topMargin = touchY - yDelta;
                } else {
                    topMargin = maxMarginTop;
                }

                layoutParams.topMargin = topMargin;
                layoutParams.rightMargin = 0;
                layoutParams.bottomMargin = 0;
                ball.setLayoutParams(layoutParams);
                break;
        }

        ball.getRootView().invalidate();

        return true;
    }

    public static class Builder {
        private Params P;

        public Builder(Context context, ViewGroup rootView) {
            P = new Params(context);
        }

        public Builder setRightMargin(int rightMargin) {
            P.rightMargin = rightMargin;
            return this;
        }

        public Builder setBottomMargin(int bottomMargin) {
            P.bottomMargin = bottomMargin;
            return this;
        }

        public Builder setWidth(int width) {
            P.width = width;
            return this;
        }

        public Builder setHeight(int height) {
            P.height = height;
            return this;
        }

        public Builder setRes(int resId) {
            P.resId = resId;
            return this;
        }

        public Builder setBall(View view) {
            P.ball = view;
            return this;
        }

        public Builder setDuration(int duration) {
            P.duration = duration;
            return this;
        }

        public Builder setOnClickListener(View.OnClickListener onClickListener) {
            P.onClickListener = onClickListener;
            return this;
        }

        public FloatView2 build() {
            FloatView2 floatBall = new FloatView2(P);
            return floatBall;
        }
    }

    private static class Params {
        public static final int DEFAULT_BALL_WIDTH = 180;
        public static final int DEFAULT_BALL_HEIGHT = 180;
        public static final int DEFAULT_DURATION = 500;
        private int duration = DEFAULT_DURATION;
        private Context context;
        private int rightMargin;
        private int bottomMargin;
        private int resId;
        private int width = DEFAULT_BALL_WIDTH;
        private int height = DEFAULT_BALL_HEIGHT;
        private View.OnClickListener onClickListener;
        private View ball;

        public Params(Context context) {
            this.context = context;
        }
    }

    private class FadeOutRunnable implements Runnable {

        @Override
        public void run() {
            if (!hasTouchFloatBall) {
                ball.setBackgroundResource(params.resId);
            }
        }
    }
}