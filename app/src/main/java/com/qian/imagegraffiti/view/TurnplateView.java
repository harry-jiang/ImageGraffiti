package com.qian.imagegraffiti.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.qian.imagegraffiti.R;

public class TurnplateView extends View implements OnTouchListener {


    private OnTurnplateListener onTurnplateListener;

    public void setOnTurnplateListener(OnTurnplateListener onTurnplateListener) {
        this.onTurnplateListener = onTurnplateListener;
    }

    /**
     * 画笔：点、线
     */
    private Paint mPaint = new Paint();
    /**
     * 画笔：文字
     */
    private Paint tPaint = new Paint();
    /**
     * 画笔：圆
     */
    private Paint paintCircle = new Paint();
    /**
     * 图标列表
     */
    private Bitmap[] icons = new Bitmap[3];
    /**
     * point列表
     */
    private Point[] points;
    /**
     * 数目
     */
    private static final int PONIT_NUM = 3;

    /**
     * 圆心坐标
     */
    private int mPointX = 0, mPointY = 0;
    /**
     * 半径
     */
    private int mRadius = 0;
    /**
     * 每两个点间隔的角度
     */
    private int mDegreeDelta;
    /**
     * 每次转动的角度差
     */
    private int tempDegree = 0;
    /**
     * 选中的图标标识 999：未选中任何图标
     */
    private int chooseBtn = 999;

    /**
     * 选择拍照
     */
    private static final int ACTION_TAKE_PHOTO = 0;
    /**
     * 选择美化图片
     */
    private static final int ACTION_CHOOSE_PHOTO = 1;
    /**
     * 选择拼接图片
     */
    private static final int ACTION_PINJIE_PHOTO = 2;

    private Matrix mMatrix = new Matrix();

    /*
     * 屏幕宽度和高度
     */
    private int pwidth;
    private int pheight;
    private Bitmap logo;

    public TurnplateView(Context context, int width, int height, int radius) {
        super(context);
        this.setBackgroundColor(Color.CYAN);//设置背景颜色
        mPaint.setColor(Color.GREEN);//设置画笔颜色
        mPaint.setStrokeWidth(0);
        mPaint.setTextSize(0);
        tPaint.setColor(Color.WHITE);
        tPaint.setTextSize(40);
        paintCircle.setAntiAlias(true);
        paintCircle.setColor(Color.MAGENTA);
        loadIcons();
        mPointX = width / 2;
        mPointY = height / 2;
        mRadius = radius;
        pwidth = width;
        pheight = height;

        initPoints();
        computeCoordinates();
    }

    /**
     * 方法名：loadBitmaps
     * 功能：装载图片
     * 参数：
     *
     * @param key
     * @param d   创建时间：2011-11-28
     */
    public void loadBitmaps(int key, Drawable d) {
        Bitmap bitmap = Bitmap.createBitmap(120, 120, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        d.setBounds(0, 0, 120, 120);
        d.draw(canvas);
        icons[key] = bitmap;
    }

    /**
     * 方法名：loadLogo
     * 功能：装载图片
     * <p/>
     * 创建时间：2011-11-28
     */
    public void loadLogo(Drawable d) {
        Bitmap bitmap = Bitmap.createBitmap(240, 120, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        d.setBounds(0, 0, 240, 120);
        d.draw(canvas);
        logo = bitmap;
    }


    /**
     * 方法名：loadIcons
     * 功能：获取所有图片
     * 参数：
     * <p/>
     * 创建时间：2011-11-28
     */
    public void loadIcons() {
        Resources r = getResources();
        loadBitmaps(ACTION_TAKE_PHOTO, r.getDrawable(R.mipmap.item_bg_orange_b));//拍照
        loadBitmaps(ACTION_CHOOSE_PHOTO, r.getDrawable(R.mipmap.item_bg_red_b));//美化图片
        loadBitmaps(ACTION_PINJIE_PHOTO, r.getDrawable(R.mipmap.item_bg_green_c));//软件介绍
        loadLogo(r.getDrawable(R.mipmap.heart2));
    }


    /**
     * 方法名：initPoints
     * 功能：初始化每个点
     * 参数：
     * <p/>
     * 创建时间：2011-11-28
     */

    private void initPoints() {
        points = new Point[PONIT_NUM];
        Point point;
        int angle = 0;
        mDegreeDelta = 360 / PONIT_NUM;

        for (int index = 0; index < PONIT_NUM; index++) {
            point = new Point();
            point.angle = angle;
            angle += mDegreeDelta;
            point.bitmap = icons[index];
            point.flag = index;
            points[index] = point;

        }
    }

    /**
     * 方法名：resetPointAngle
     * 功能：重新计算每个点的角度
     * 参数：
     *
     * @param x
     * @param y 创建时间：2011-11-28
     */
    private void resetPointAngle(float x, float y) {
        int degree = computeMigrationAngle(x, y);
        for (int index = 0; index < PONIT_NUM; index++) {
            points[index].angle += degree;
            if (points[index].angle > 360) {
                points[index].angle -= 360;
            } else if (points[index].angle < 0) {
                points[index].angle += 360;
            }

        }
    }

    /**
     * 方法名：computeCoordinates
     * 功能：计算每个点的坐标
     * 参数：
     * <p/>
     * 创建时间：2011-11-28
     */
    private void computeCoordinates() {
        Point point;
        for (int index = 0; index < PONIT_NUM; index++) {
            point = points[index];
            point.x = mPointX + (float) (mRadius * Math.cos(point.angle * Math.PI / 180));
            point.y = mPointY + (float) (mRadius * Math.sin(point.angle * Math.PI / 180));
            point.x_c = mPointX + (point.x - mPointX) / 2;
            point.y_c = mPointY + (point.y - mPointY) / 2;
        }
    }

    /**
     * 方法名：computeMigrationAngle
     * 功能：计算偏移角度
     * 参数：
     *
     * @param x
     * @param y 创建时间：2011-11-28
     *///
    private int computeMigrationAngle(float x, float y) {
        int a = 0;
        float distance = (float) Math.sqrt(((x - mPointX) * (x - mPointX) + (y - mPointY) * (y - mPointY)));
        int degree = (int) (Math.acos((x - mPointX) / distance) * 180 / Math.PI);
        if (y < mPointY) {
            degree = -degree;
        }
        if (tempDegree != 0) {
            a = degree - tempDegree;
        }
        tempDegree = degree;
        return a;
    }


    /**
     * 方法名：computeCurrentDistance
     * 功能：计算触摸的位置与各个元点的距离
     * 参数：
     *
     * @param x
     * @param y
     * @return 创建时间：2011-11-29
     */
    private void computeCurrentDistance(float x, float y) {
        for (Point point : points) {
            float distance = (float) Math.sqrt(((x - point.x) * (x - point.x) + (y - point.y) * (y - point.y)));
            if (distance < 31) {
                chooseBtn = point.flag;
                break;
            } else {
                chooseBtn = 999;
            }
        }
    }

    private void switchScreen(MotionEvent event) {
        computeCurrentDistance(event.getX(), event.getY());
        //Log.e(TAG,chooseBtn+"");
        onTurnplateListener.onPointTouch(chooseBtn);

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                resetPointAngle(event.getX(), event.getY());
                computeCoordinates();
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                switchScreen(event);
                tempDegree = 0;
                invalidate();
                break;
            case MotionEvent.ACTION_CANCEL:
                //系统在运行到一定程度下无法继续响应你的后续动作时会产生此事件。
                //一般仅在代码中将其视为异常分支情况处理
                break;
        }
        return true;
    }

    @Override
    public void onDraw(Canvas canvas) {

        //canvas.drawText("MX  Image", mPointX-120, pheight/5, tPaint);
        canvas.drawBitmap(logo, mPointX - 120, pheight / 10, null);
        canvas.drawCircle(mPointX, mPointY, mRadius, paintCircle);
        canvas.drawPoint(mPointX, mPointY, mPaint);

        for (int index = 0; index < PONIT_NUM; index++) {
            canvas.drawPoint(points[index].x_c, points[index].y_c, mPaint);
            drawInCenter(canvas, points[index].bitmap, points[index].x, points[index].y, points[index].flag);
        }


    }

    /**
     * 方法名：drawInCenter
     * 功能：把点放到图片中心处
     * 参数：
     *
     * @param canvas
     * @param bitmap
     * @param left
     * @param top    创建时间：2011-11-28
     */
    void drawInCenter(Canvas canvas, Bitmap bitmap, float left, float top, int flag) {
        canvas.drawPoint(left, top, mPaint);
        if (chooseBtn == flag) {
            //Log.e("Width", bitmap.getWidth()+";"+70f/bitmap.getWidth());
            //Log.e("Height", bitmap.getHeight()+";"+70f/bitmap.getHeight());
            mMatrix.setScale(100f / bitmap.getWidth(), 100f / bitmap.getHeight());
            mMatrix.postTranslate(left - 50, top - 50);
            canvas.drawBitmap(bitmap, mMatrix, null);
        } else {
            canvas.drawBitmap(bitmap, left - bitmap.getWidth() / 2, top - bitmap.getHeight() / 2, null);
        }


    }

    class Point {

        /**
         * 位置标识
         */
        int flag;
        /**
         * 图片
         */
        Bitmap bitmap;

        /**
         * 角度
         */
        int angle;

        /**
         * x坐标
         */
        float x;

        /**
         * y坐标
         */
        float y;

        /**
         * 点与圆心的中心x坐标
         */
        float x_c;
        /**
         * 点与圆心的中心y坐标
         */
        float y_c;
    }

    public static interface OnTurnplateListener {

        public void onPointTouch(int flag);

    }

    @Override
    public boolean onTouch(View arg0, MotionEvent arg1) {
        return false;
    }


}