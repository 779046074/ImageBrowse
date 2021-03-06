package com.example.draglayout;

import android.content.Context;
import android.graphics.Point;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;

/**
 * 拖拽布局
 * Created by Boqin on 2017/2/22.
 * Modified by Boqin
 *
 * @Version
 */
public class DragLayout extends LinearLayout {

    private View mChildrenView;

    /** 保存mChildrenView的初始布局变化 */
    private Point mAutoBackOriginPos = new Point();

    /** DragLayout布局高度 */
    private int mTotalHeight;

    /** 缩放阈值 */
    private float mScaleThreshold = 0.7f;

    /** 缩放比例 */
    private float mScale;

    /** 子View位置变化接口 */
    private DragChangedListener mDragChangedListener;

    /** 手势检测工具 */
    private GestureDetector mGestureDetector;

    private ViewDragHelper mDragHelper;

    public DragLayout(Context context) {
        super(context);
        init(context);
    }

    public DragLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DragLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            mDragHelper.cancel();
            return false;
        }
        //DragHelper必须要持有一个DOWN事件，不然后续的操作无效
        if (action == MotionEvent.ACTION_DOWN) {
            mDragHelper.processTouchEvent(ev);
        }
        try {
            return mGestureDetector.onTouchEvent(ev) ;
        } catch (IllegalArgumentException e) {
            //uncomment if you really want to see these errors
            //e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        mDragHelper.processTouchEvent(ev);
        return true;
    }

    @Override
    public void computeScroll() {
        //如果滑动未完成进行执行布局更新
        if (mDragHelper.continueSettling(true)) {
            invalidate();
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        //获取初始化的子View
        mChildrenView = getChildAt(0);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        //当界面初始化布局的时候会获取初始值
        if (mChildrenView != null) {
            mAutoBackOriginPos.x = mChildrenView.getLeft();
            mAutoBackOriginPos.y = mChildrenView.getTop();
            mTotalHeight = getHeight();

        }
    }

    /**
     * 初始化设置
     */
    public void init(final Context context) {
        mDragHelper = ViewDragHelper.create(this, 1.0f, new ViewDragHelper.Callback() {
            @Override
            public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
                super.onViewPositionChanged(changedView, left, top, dx, dy);
                if (mDragChangedListener != null) {
                    mScale = 1 - (float) (top - mAutoBackOriginPos.y)/ mTotalHeight;
                    if (mDragChangedListener != null) {
                        mDragChangedListener.onViewPositionChanged(changedView, mScale > 1 ? 1 : mScale);
                    }
                }
            }

            @Override
            public void onViewReleased(View releasedChild, float xvel, float yvel) {
                //当拖拽结束后的操作
                super.onViewReleased(releasedChild, xvel, yvel);
                if (mScale < mScaleThreshold) {
                    if (mDragChangedListener != null) {
                        if (mDragChangedListener.onViewReleased()) {
                            return;
                        }
                    }
                }

                reset();
            }

            @Override
            public boolean tryCaptureView(View child, int pointerId) {
                return true;
            }

            @Override
            public int clampViewPositionHorizontal(View child, int left, int dx) {
                return left;
            }

            @Override
            public int clampViewPositionVertical(View child, int top, int dy) {
                return top;
            }

        });

        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if ((e2.getRawY() - e1.getRawY()) > ViewConfiguration.get(context).getScaledTouchSlop() / 2) {
                    return true;
                }
                return false;
            }

        });

    }

    /**
     * 设置位置变化监听
     */
    public void setDragListener(DragChangedListener dragChangedListener) {
        mDragChangedListener = dragChangedListener;
    }

    /**
     * 重置位置
     */
    private void reset() {
        //恢复到初始位置
        mDragHelper.settleCapturedViewAt(mAutoBackOriginPos.x, mAutoBackOriginPos.y);
        invalidate();
    }

}
