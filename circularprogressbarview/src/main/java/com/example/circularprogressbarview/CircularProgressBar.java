package com.example.circularprogressbarview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.example.circularprogressbarview.system.ScreenUtil;


public class CircularProgressBar extends View {
    /**
     * 完成回调代码
     */
    public static final int COMPLETE = 1;
    /**
     * 错误回调代码
     */
    public static final int ERROR = 2;
    /**
     * 当前进度
     */
    int progress = 360;

    /**
     * 最大进度
     */
    private int max = 360;

    /**
     * 宽
     */
    private float mWidth;

    /**
     * 高
     */
    private float mHeight;

    /**
     * 当前所在线程
     */
    private boolean mThread;

    /**
     * 圆的画笔
     */
    private Paint mPaint;

    /**
     * 截取的路径
     */
    private Path mDstPath;

    /**
     * 半径
     */
    private float radius;

    /**
     * 粗细
     */
    private int stroke;

    /**
     * 最小半径
     */
    private int minRadius;
    /**
     * 开始角度
     */
    private int startDot = -90;
    /**
     * 启动动画
     */
    private ValueAnimator mStartAnim;
    /**
     * 对号动画进度
     */
    private float mMarkAnimatedValue;
    /**
     * 对号路径管理器
     */
    private PathMeasure mMarkPathMeasure;
    /**
     * 对号路径长度
     */
    private float mMarkPathLength;
    /**
     * 对号动画对象
     */
    private ValueAnimator mRightMarkValueAnimator;

    /**
     * 错误动画进度
     */
    private float mErrorAnimatedValue;
    /**
     * 错误路径管理器
     */
    private PathMeasure mErrorPathMeasure;
    /**
     * 错误路径长度
     */
    private float mErrorPathLength;
    /**
     * 错误动画对象
     */
    private ValueAnimator mRightErrorValueAnimator;

    /**
     * 错误动画进度
     */
    private float mError2AnimatedValue;
    /**
     * 错误路径管理器
     */
    private PathMeasure mError2PathMeasure;
    /**
     * 错误路径长度
     */
    private float mError2PathLength;
    /**
     * 错误动画对象
     */
    private ValueAnimator mRightError2ValueAnimator;

    /**
     * 进度条收缩最大值 360则会头尾连接起来
     */
    private final int defProgress = 270;
    /**
     * 完成的颜色
     */
    private int completeColor;
    /**
     * 错误的颜色
     */
    private int errorColor;
    /**
     * 开始渐变进度条颜色
     */
    private int startProgressColor;
    /**
     * 结束渐变进度条颜色
     */
    private int endProgressColor;
    /**
     * 渐变颜色数组
     */
    private int[] mMinColors;

    public CircularProgressBar(Context context) {
        this(context, null);
    }

    public CircularProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircularProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
    }

    //初始化
    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CircularProgressBar);
        if (ta != null) {
            //宽高
            mWidth = ta.getLayoutDimension(R.styleable.CircularProgressBar_android_layout_width,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            mHeight = ta.getLayoutDimension(R.styleable.CircularProgressBar_android_layout_height,
                    ViewGroup.LayoutParams.WRAP_CONTENT);

            completeColor = ta.getColor(R.styleable.CircularProgressBar_completeColor, Color.rgb(255, 202, 114));

            errorColor = ta.getColor(R.styleable.CircularProgressBar_errorColor, Color.RED);

            startProgressColor = ta.getColor(R.styleable.CircularProgressBar_startProgressColor, Color.rgb(254, 235, 203));

            endProgressColor = ta.getColor(R.styleable.CircularProgressBar_endProgressColor, Color.rgb(255, 202, 114));

            mMinColors = new int[]{startProgressColor, endProgressColor};
            //获取属性完毕 释放资源
            ta.recycle();
        } else {
            mWidth = ViewGroup.LayoutParams.WRAP_CONTENT;
            mHeight = ViewGroup.LayoutParams.WRAP_CONTENT;
            completeColor = Color.rgb(255, 202, 114);
            errorColor = Color.RED;
            mMinColors = new int[]{Color.rgb(254, 235, 203), Color.rgb(255, 202, 114)};
        }
        initPaint();
    }

    private void initPaint() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);//抗锯齿
        mPaint.setColor(completeColor); //颜色
        mPaint.setStyle(Paint.Style.STROKE);//实心FILL    空心STROKE
        mPaint.setTextSize(40);
        mDstPath = new Path();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setStyle(Paint.Style.STROKE);//设置画笔类型为空心
        //mPaint.setColor(0xFF4080F4);
        //这是画蓝弧，第三个参数false是指，不连接到圆心

        Matrix matrix = new Matrix();
        matrix.setRotate(startDot + defProgress + 15, mWidth / 2,
                mWidth / 2);
        //先创建一个渲染器
        SweepGradient mSweepGradient = new SweepGradient(mWidth / 2,
                mWidth / 2, //以圆弧中心作为扫描渲染的中心以便实现需要的效果
                mMinColors, //颜色数组
                null);
        mSweepGradient.setLocalMatrix(matrix);
        //把渐变设置到笔刷
        mPaint.setShader(mSweepGradient);

        canvas.drawArc(getOval(minRadius), startDot, 360 * progress / max, false, mPaint);

        mPaint.setStyle(Paint.Style.FILL);//设置画笔类型为填充
        //画一个跨度360的弧，那肯定是个圆了，然后第三个参数为true，表示连接到圆心，这就变成了一个实心圆。你也可以直接drawCircle
        //if (progress != 0 || progress == max) {
        //    //mPaint.setColor(Color.RED);
        //    ////开始的圆点
        //    //canvas.drawArc(getStartCireleRectF(minRadius), startDot, 360, false, mPaint);//画一个点
        //
        //
        //    mPaint.setColor(Color.GREEN);
        //    //结束的圆点
        //    canvas.drawCircle(getCx(minRadius), getCy(minRadius), stroke / 2, mPaint);
        //}
        if (mDstPath == null) {
            return;
        }
        if (isCompleteDraw) {
            mPaint.setShader(null);
            mPaint.setColor(completeColor);
            // 刷新当前截取 Path
            mDstPath.reset();

            // 避免硬件加速的Bug
            mDstPath.lineTo(0, 0);

            // 截取片段
            float stop = mMarkPathLength * mMarkAnimatedValue;
            mMarkPathMeasure.getSegment(0, stop, mDstPath, true);
            mPaint.setStyle(Paint.Style.STROKE);//设置画笔类型为空心
            // 绘制截取的片段
            canvas.drawPath(mDstPath, mPaint);
        }

        if (isErrorDraw1) {
            mPaint.setShader(null);
            mPaint.setColor(errorColor);
            // 刷新当前截取 Path
            mDstPath.reset();

            // 避免硬件加速的Bug
            mDstPath.lineTo(0, 0);

            // 截取片段
            float stop = mErrorPathLength * mErrorAnimatedValue;
            mErrorPathMeasure.getSegment(0, stop, mDstPath, true);
            mPaint.setStyle(Paint.Style.STROKE);//设置画笔类型为空心
            // 绘制截取的片段
            canvas.drawPath(mDstPath, mPaint);
        }

        if (isErrorDraw2) {
            mPaint.setShader(null);
            mPaint.setColor(errorColor);
            // 刷新当前截取 Path
            mDstPath.reset();

            // 避免硬件加速的Bug
            mDstPath.lineTo(0, 0);

            // 截取片段
            float stop = mError2PathLength * mError2AnimatedValue;
            mError2PathMeasure.getSegment(0, stop, mDstPath, true);
            mPaint.setStyle(Paint.Style.STROKE);//设置画笔类型为空心
            // 绘制截取的片段
            canvas.drawPath(mDstPath, mPaint);
        }
    }

    /**
     * 初始化对号动画
     */
    private void initMarkAnimator() {
        Path path = new Path();
        // 对号起点
        float startX = (float) (0.3 * mWidth);
        float startY = (float) (0.5 * mWidth);
        path.moveTo(startX, startY);

        // 对号拐角点
        float cornerX = (float) (0.43 * mWidth);
        float cornerY = (float) (0.66 * mWidth);
        path.lineTo(cornerX, cornerY);

        // 对号终点
        float endX = (float) (0.75 * mWidth);
        float endY = (float) (0.4 * mWidth);
        path.lineTo(endX, endY);
        // PathMeasure
        mMarkPathMeasure = new PathMeasure();
        // 重新关联Path
        mMarkPathMeasure.setPath(path, false);

        // 此时为对号 Path 的长度
        mMarkPathLength = mMarkPathMeasure.getLength();

        mRightMarkValueAnimator = ValueAnimator.ofFloat(0, 1);
        // 动画过程
        mRightMarkValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mMarkAnimatedValue = (float) animation.getAnimatedValue();
                reDraw();
            }
        });

        // 动画时间
        mRightMarkValueAnimator.setDuration(300);

        // 插值器
        mRightMarkValueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mRightMarkValueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (mOnCompleteListener != null) {
                    mOnCompleteListener.onLoadComplete(COMPLETE);
                }
            }
        });
    }

    /**
     * 初始化错误动画
     */
    private void initErrorAnimator() {
        Path path = new Path();
        // 对号起点
        float startX = (float) (0.3 * mWidth);
        float startY = (float) (0.3 * mWidth);
        path.moveTo(startX, startY);

        // 对号拐角点
        float endX = (float) (0.7 * mWidth);
        float endY = (float) (0.7 * mWidth);
        path.lineTo(endX, endY);


        // PathMeasure
        mErrorPathMeasure = new PathMeasure();
        // 重新关联Path
        mErrorPathMeasure.setPath(path, false);

        // 此时为对号 Path 的长度
        mErrorPathLength = mErrorPathMeasure.getLength();

        mRightErrorValueAnimator = ValueAnimator.ofFloat(0, 1);
        // 动画过程
        mRightErrorValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mErrorAnimatedValue = (float) animation.getAnimatedValue();
                reDraw();
            }
        });

        // 动画时间
        mRightErrorValueAnimator.setDuration(300);

        // 插值器
        mRightErrorValueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mRightErrorValueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                isErrorDraw2 = true;
                mRightError2ValueAnimator.start();
            }
        });
    }

    /**
     * 初始化错误动画
     */
    private void initError2Animator() {

        Path path = new Path();
        // 对号起点
        float startX = (float) (0.7 * mWidth);
        float startY = (float) (0.3 * mWidth);
        path.moveTo(startX, startY);

        // 对号拐角点
        float endX = (float) (0.3 * mWidth);
        float endY = (float) (0.7 * mWidth);
        path.lineTo(endX, endY);

        // PathMeasure
        mError2PathMeasure = new PathMeasure();
        // 重新关联Path
        mError2PathMeasure.setPath(path, false);

        // 此时为对号 Path 的长度
        mError2PathLength = mError2PathMeasure.getLength();

        mRightError2ValueAnimator = ValueAnimator.ofFloat(0, 1);
        // 动画过程
        mRightError2ValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mError2AnimatedValue = (float) animation.getAnimatedValue();
                reDraw();
            }
        });

        // 动画时间
        mRightError2ValueAnimator.setDuration(300);

        // 插值器
        mRightError2ValueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mRightError2ValueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (mOnCompleteListener != null) {
                    mOnCompleteListener.onLoadComplete(ERROR);
                }
            }
        });
    }

    /**
     * 完成时回调接口
     */
    private OnLoadCompleteListener mOnCompleteListener;

    /**
     * 设置完成回调
     *
     * @param onCompleteListener
     */
    public void setOnCompleteListener(OnLoadCompleteListener onCompleteListener) {
        mOnCompleteListener = onCompleteListener;
    }

    /**
     * 加载完成/错误 回调接口
     */
    public interface OnLoadCompleteListener {
        /**
         * @param type 1完成   2错误
         */
        void onLoadComplete(int type);
    }

    private RectF getOval(int minRadius) {
        return new RectF(2 * minRadius, 2 * minRadius, radius * 2 + 2 * minRadius,
                radius * 2 + 2 * minRadius);
    }

    private RectF getStartCireleRectF(int minRadius) {
        return new RectF(radius + minRadius, minRadius, radius + 3 * minRadius, 3 * minRadius);
    }

    private float getCx(int minRadius) {
        return (float) (radius + 2 * minRadius + radius * Math.sin(360 * progress / max * Math.PI / 180));
    }

    private float getCy(int minRadius) {
        return (float) (radius + 2 * minRadius - radius * Math.cos(360 * progress / max * Math.PI / 180));
    }


    /**
     * 刷新绘制
     */
    private void reDraw() {
        if (mThread) {
            invalidate();
        } else {
            postInvalidate();
        }
    }

    /**
     * 测量当前控件高宽
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
        if (mWidth > mHeight) {
            mWidth = mHeight;
        }

        //轨迹宽 最小圆半径的两倍
        stroke = ScreenUtil.dip2px(getContext(), 3);/*(int) ((mWidth / 2) * 0.15f);*/

        minRadius = stroke / 2;

        //最大圆的半径
        radius = (mWidth - minRadius * 4) / 2;

        mPaint.setStrokeWidth(stroke);

        initMarkAnimator();
        initErrorAnimator();
        initError2Animator();
    }

    /**
     * 在容器调用addView的时候第一个被调用的方法 在这时候就可以获取到父容器了
     */
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        //if (roobotEyeClick) {//在控件添加到容器中的时候获取容器View拦截事件
        //    View parent = (View) getParent();
        //    if (parent != null) {//判断是否获取到了父容器,虽然不太可能为NULL
        //        parent.setOnTouchListener(new OnTouchListener() {//拦截父容器的事件
        //            @Override
        //            public boolean onTouch(View v, MotionEvent event) {
        //                return CircularProgressBar.this.onTouchEvent(event);
        //            }
        //        });
        //    }
        //}
        mThread = Thread.currentThread().getName().equals("main");
        play();
    }

    /**
     * 循环添加进度条动画
     */
    private Runnable startAdd = new Runnable() {
        @Override
        public void run() {
            if (isPlayAnim)
                startAnim(2);
        }
    };

    /**
     * 回收进度条动画
     */
    private Runnable startEnd = new Runnable() {
        @Override
        public void run() {
            if (isPlayAnim)
                startAnim(3);
        }
    };

    /**
     * 完成动画
     */
    private Runnable completeAnim = new Runnable() {
        @Override
        public void run() {
            startAnim(4);
        }
    };

    private boolean isStart;
    /**
     * 是否已经启动了
     */
    private boolean isPlayAnim;
    /**
     * 是否完成
     */
    private boolean isComplete;
    /**
     * 是否完成
     */
    private boolean isError;
    /**
     * 是否绘制对号
     */
    private boolean isCompleteDraw = true;
    /**
     * 是否绘制错号
     */
    private boolean isErrorDraw1;
    /**
     * 是否绘制错号
     */
    private boolean isErrorDraw2;

    /**
     * 停止
     */
    public void stop() {
        if (isStart) {
            isStart = false;
            isPlayAnim = false;
            if (mStartAnim != null)
                mStartAnim.cancel();
        }
    }

    /**
     * 开始
     */
    public void play() {
        if (!isStart) {
            isStart = true;
            isComplete = false;
            isError = false;
            isCompleteDraw = false;
            isErrorDraw1 = false;
            isErrorDraw2 = false;
            mStartAnim = startAnim(1);
        }
    }

    /**
     * 完成
     */
    public void complete() {
        if (isStart) {
            isStart = false;
            isPlayAnim = false;
            isComplete = true;
            if (mStartAnim != null)
                mStartAnim.cancel();
        }
    }

    /**
     * 错误
     */
    public void error() {
        if (isStart) {
            isStart = false;
            isPlayAnim = false;
            isError = true;
            if (mStartAnim != null)
                mStartAnim.cancel();
        }
    }

    /**
     * 创建动画
     */
    private ValueAnimator startAnim(int type) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0f, 1f);
        switch (type) {
            case 1:
                if (!isPlayAnim) {
                    valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            startDot += 5;
                            reDraw();
                        }
                    });
                    valueAnimator.setDuration(500);
                    valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
                    valueAnimator.addListener(new AnimatorListenerAdapter() {

                        @Override
                        public void onAnimationCancel(Animator animation) {
                            super.onAnimationCancel(animation);
                            if (isComplete || isError) {
                                post(completeAnim);
                            }
                        }
                    });
                    post(startAdd);
                    isPlayAnim = true;
                }
                break;
            case 2:
                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        if (isPlayAnim) {
                            float fraction = animation.getAnimatedFraction();
                            progress = (int) (defProgress * fraction) + 10;
                            reDraw();
                        }
                    }
                });
                valueAnimator.setDuration(1000);
                valueAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        postDelayed(startEnd, 500);
                    }
                });
                break;
            case 3:
                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    private int lastProgress;
                    private int lastStartDot;

                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        if (isPlayAnim) {
                            float fraction = animation.getAnimatedFraction();
                            int v = (int) (defProgress * fraction);
                            startDot = (startDot + (v - lastStartDot) - 1);
                            lastStartDot = v;
                            int p = (int) (defProgress * fraction);
                            progress = (progress - (p - lastProgress));
                            lastProgress = p;
                            reDraw();
                        }
                    }
                });
                valueAnimator.setDuration(1000);
                valueAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        postDelayed(startAdd, 50);
                    }
                });
                break;
            case 4:
                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    private int curProgress;

                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float fraction = animation.getAnimatedFraction();
                        int p = 370 - progress;
                        curProgress = progress;
                        progress = curProgress + (int) (p * fraction);
                        reDraw();
                    }
                });

                valueAnimator.setDuration(500);
                valueAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        if (isComplete) {
                            isCompleteDraw = true;
                            mRightMarkValueAnimator.start();
                        }
                        if (isError) {
                            isErrorDraw1 = true;
                            mRightErrorValueAnimator.start();
                        }
                        reDraw();
                    }
                });
                break;
        }

        valueAnimator.start();
        return valueAnimator;
    }
}
