package com.qian.imagegraffiti.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;

public class ToneView {
    private ColorMatrix mLightnessMatrix;
    private ColorMatrix mSaturationMatrix;
    private ColorMatrix mHueMatrix;
    private ColorMatrix mConMatrix;
    private ColorMatrix mAllMatrix;

    /**
     * 亮度
     */
    private float mLightnessValue = 1F;

    /**
     * 饱和度
     */
    private float mSaturationValue = 0F;

    /**
     * 色相
     */
    private float mHueValue = 0F;
    /**
     * 对比度
     */
    private float mContrastValue = 0F;

    private final int MIDDLE_VALUE = 127;

    /*
    * 处理前的图片
    */
    private Bitmap srcBitmap, resultBitmap;

    Paint paint;

    public ToneView() {
        mAllMatrix = new ColorMatrix();
        mLightnessMatrix = new ColorMatrix(); // 用于颜色变换的矩阵，android位图颜色变化处理主要是靠该对象完成
        mSaturationMatrix = new ColorMatrix();
        mHueMatrix = new ColorMatrix();
        mConMatrix = new ColorMatrix();
        paint = new Paint(); // 新建paint
        paint.setAntiAlias(true); // 设置抗锯齿,也即是边缘做平滑处理
    }

    public Bitmap setSaturation(int saturation) {
        mSaturationValue = (float) (saturation * 1.0D / MIDDLE_VALUE);
        mSaturationMatrix.reset();
        mSaturationMatrix.setSaturation(mSaturationValue);
        return drawBitmap();
    }

    public Bitmap setHue(int value) {
        mHueValue = (float) ((value - MIDDLE_VALUE) * 1.0D / MIDDLE_VALUE * 180);
        mLightnessMatrix.reset(); // 设为默认值
        mLightnessMatrix.setRotate(0, mHueValue); // 控制让红色区在色轮上旋转hueColor的角度
        mLightnessMatrix.setRotate(1, mHueValue); // 控制让绿红色区在色轮上旋转hueColor的角度
        mLightnessMatrix.setRotate(2, mHueValue); // 控制让蓝色区在色轮上旋转hueColor的角度
        return drawBitmap();

    }

    public Bitmap setLight(int value) {
        mLightnessValue = (float) (value * 1.0D / MIDDLE_VALUE);
        mHueMatrix.reset();
        mHueMatrix.setScale(mLightnessValue, mLightnessValue, mLightnessValue, 1); // 红、绿、蓝三分量按相同的比例,最后一个参数1表示透明度不做变化，此函数详细说明参考

        return drawBitmap();
    }

    public Bitmap setCon(int con) {
        mContrastValue = (float) (con * 1.0D / MIDDLE_VALUE);
        mConMatrix.reset();
        mConMatrix.set(new float[]{
                mContrastValue, 0, 0, 0, 128 * (1 - mContrastValue),
                0, mContrastValue, 0, 0, 128 * (1 - mContrastValue),
                0, 0, mContrastValue, 0, 128 * (1 - mContrastValue),
                0, 0, 0, 1, 0});
        return drawBitmap();
    }

    public void setBitmap(Bitmap bm) {
        srcBitmap = bm;
    }

    private Bitmap drawBitmap() {
        mAllMatrix.reset();
        mAllMatrix.postConcat(mHueMatrix);
        mAllMatrix.postConcat(mSaturationMatrix); // 效果叠加
        mAllMatrix.postConcat(mLightnessMatrix); // 效果叠加
        mAllMatrix.postConcat(mConMatrix);//叠加对比度效果
        // 创建一个相同尺寸的可变的位图区,用于绘制调色后的图片
        resultBitmap = BitmapUtils.createBitmap(srcBitmap);
        Canvas canvas = new Canvas(resultBitmap); // 得到画笔对象
        paint.setColorFilter(new ColorMatrixColorFilter(mAllMatrix));// 设置颜色变换效果
        canvas.drawBitmap(srcBitmap, 0, 0, paint); // 将颜色变化后的图片输出到新创建的位图区
        return resultBitmap;
    }

    public Bitmap getResultBitmap() {
        return resultBitmap;
    }
}
