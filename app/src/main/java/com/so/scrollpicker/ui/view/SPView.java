package com.so.scrollpicker.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.so.scrollpicker.R;
import com.so.scrollpicker.utils.UIUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 滚动选择器
 * Created by sorrower on 2018/7/15.
 */

public class SPView extends View {
    /**
     * 画笔
     */
    private Paint mPaint;
    private Paint mLinePaint;
    /**
     * 显示数据
     */
    private List<String> mData;
    /**
     * 计时器
     */
    private MyTimerTask mTask;
    private Timer mTimer;
    /**
     * 视图高度
     */
    private int mWidth;
    /**
     * 视图高度
     */
    private int mHeight;
    /**
     * 最大字号
     */
    private float mMax;
    /**
     * 最小字号
     */
    private float mMin;
    /**
     * 滑动距离
     */
    private float mMoveLen = 0;
    /**
     * 最大透明度
     */
    private float mMaxAlpha = 255;
    /**
     * 最小透明度
     */
    private float mMinAlpha = 100;
    /**
     * 当前选中
     */
    private int mCurSelected;
    /**
     * 字体间距与最小字号之比
     */
    public static final float DIS = 3.0f;
    /**
     * 上次点击y值
     */
    private float mLastDownY;
    /**
     * 自动回滚到中间的速度
     */
    public static final float BACK_SPEED = 2.0f;
    /**
     * 选择监听
     */
    private onSelectListener mSelectListener;

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // 逐步回滚, 直到小于指定值, 选中目标
            if (Math.abs(mMoveLen) < BACK_SPEED) {
                // 选中
                mMoveLen = 0;
                if (mTask != null) {
                    mTask.cancel();
                    mTask = null;
                    select();
                }
            } else {
                // 滚动
                mMoveLen = mMoveLen - mMoveLen / Math.abs(mMoveLen) * BACK_SPEED;
            }
            invalidate();
        }
    };

    public SPView(Context context) {
        super(context);
        init();
    }

    public SPView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * 构建初始化
     */
    private void init() {
        mTimer = new Timer();

        mData = new ArrayList<String>();

        mPaint = new Paint();
        // 设置抗锯齿
        mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        // 设置文本对齐方式
        mPaint.setTextAlign(Paint.Align.CENTER);
        // 设置画笔颜色
        mPaint.setColor(UIUtil.getColor(R.color.colorText));

        mLinePaint = new Paint();
        mLinePaint.setColor(UIUtil.getColor(R.color.colorPrimaryTrans));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // 获取测量宽高
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 依据测量高度计算字号
        mMax = mHeight / 3.5f;
        mMin = mMax / 2.5f;

        // 依据曲线设置字号
        float scale = parabola(mMax, mMoveLen);
        float size = (mMax - mMin) * scale + mMin;
        mPaint.setTextSize(size);

        // 依据曲线设置透明度
        mPaint.setAlpha((int) ((mMaxAlpha - mMinAlpha) * scale + mMinAlpha));

        // 依据基线居中绘制字体
        float y = mHeight / 2.0f + mMoveLen;
        Paint.FontMetricsInt fmi = mPaint.getFontMetricsInt();
//        LogUtil.i("top: " + fmi.top + " bottom: " + fmi.bottom
//                + " ascent: " + fmi.ascent + " descent: " + fmi.descent);

        float baseline = y - (fmi.top + fmi.bottom) / 2.0f;
//        float baseline = y - (fmi.ascent + fmi.descent) / 2.0f;

        canvas.drawText(mData.get(mCurSelected),
                mWidth / 2.0f, baseline, mPaint);

        canvas.drawRect(0, baseline + UIUtil.dp2px(4),
                mWidth, baseline + UIUtil.dp2px(4) + 1, mLinePaint);

        // 绘制其余数据
        for (int i = 1; (mCurSelected - i) >= 0; i++) {
            // 绘制上方数据
            drawOthers(canvas, i, -1);
        }
        for (int i = 1; (mCurSelected + i) < mData.size(); i++) {
            // 绘制下方数据
            drawOthers(canvas, i, 1);
        }
    }

    /**
     * 绘制非选中字体
     *
     * @param canvas 画布
     * @param pos    位置
     * @param upDown 类型
     */
    private void drawOthers(Canvas canvas, int pos, int upDown) {
        // 依据偏离和曲线设置字号
        float dis = DIS * mMin * pos + upDown * mMoveLen;
        float scale = parabola(mMax, dis);
        float size = (mMax - mMin) * scale + mMin;
        mPaint.setTextSize(size);

        // 依据偏离和曲线设置透明度
        mPaint.setAlpha((int) ((mMaxAlpha - mMinAlpha) * scale + mMinAlpha));

        // 依据偏离和基线居中绘制字体
        float y = mHeight / 2.0f + upDown * dis;
        Paint.FontMetricsInt fmi = mPaint.getFontMetricsInt();
//        LogUtil.i("top: " + fmi.top + " bottom: " + fmi.bottom
//                + " ascent: " + fmi.ascent + " descent: " + fmi.descent);

        float baseline = y - (fmi.top + fmi.bottom) / 2.0f;
//        float baseline = y - (fmi.ascent + fmi.descent) / 2.0f;

        canvas.drawText(mData.get(mCurSelected + upDown * pos),
                mWidth / 2.0f, baseline, mPaint);

        canvas.drawRect(0, baseline + UIUtil.dp2px(4),
                mWidth, baseline + UIUtil.dp2px(4) + 1, mLinePaint);
    }

    /**
     * 抛物线
     *
     * @param zero 零点坐标
     * @param x    偏移量
     * @return scale
     */
    private float parabola(float zero, float x) {
        float f = (float) (1 - Math.pow(x / zero, 2));
        return f < 0 ? 0 : f;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                if (mTask != null) {
                    mTask.cancel();
                    mTask = null;
                }
                mLastDownY = event.getY();
            }
            break;

            case MotionEvent.ACTION_MOVE: {
                mMoveLen += (event.getY() - mLastDownY);

                if (mMoveLen > DIS * mMin / 2) {
                    tailToHead();
                    mMoveLen = mMoveLen - DIS * mMin;
                } else if (mMoveLen < -DIS * mMin / 2) {
                    headToTail();
                    mMoveLen = mMoveLen + DIS * mMin;
                }

                mLastDownY = event.getY();
                invalidate();
            }
            break;

            case MotionEvent.ACTION_UP: {
                // 移动过小就不移动
                if (Math.abs(mMoveLen) < 0.001) {
                    mMoveLen = 0;
                    break;
                }
                if (mTask != null) {
                    mTask.cancel();
                    mTask = null;
                }

                mTask = new MyTimerTask(mHandler);
                mTimer.schedule(mTask, 0, 10);
            }
            break;
        }
        return true;
    }

    private class MyTimerTask extends TimerTask {
        Handler handler;

        public MyTimerTask(Handler handler) {
            this.handler = handler;
        }

        @Override
        public void run() {
            handler.sendMessage(handler.obtainMessage());
        }
    }

    public void setData(List<String> datas) {
        mData = datas;
        mCurSelected = datas.size() / 2;
        invalidate();
    }

    public interface onSelectListener {
        void onSelect(String text);
    }

    public void setOnSelectListener(onSelectListener listener) {
        mSelectListener = listener;
    }

    private void select() {
        if (mSelectListener != null)
            mSelectListener.onSelect(mData.get(mCurSelected));
    }

    private void headToTail() {
        String head = mData.get(0);
        mData.remove(0);
        mData.add(head);
    }

    private void tailToHead() {
        String tail = mData.get(mData.size() - 1);
        mData.remove(mData.size() - 1);
        mData.add(0, tail);
    }
}