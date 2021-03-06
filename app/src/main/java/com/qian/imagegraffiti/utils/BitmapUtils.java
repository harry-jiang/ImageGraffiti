package com.qian.imagegraffiti.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;

/**
 * Created by jqian on 2015/10/19.
 */
public class BitmapUtils {
    public static Bitmap getBitmapFromFile(File dst, int width, int height) {
        if (null != dst && dst.exists()) {
            BitmapFactory.Options opts = null;
            if (width > 0 && height > 0) {
                opts = new BitmapFactory.Options();
                opts.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(dst.getPath(), opts);
                // 计算图片缩放比例
                final int minSideLength = Math.min(width, height);
                opts.inSampleSize = computeSampleSize(opts, minSideLength,
                        width * height);
                opts.inJustDecodeBounds = false;
                opts.inInputShareable = true;
                opts.inPurgeable = true;
            }
            try {
                return BitmapFactory.decodeFile(dst.getPath(), opts);
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static int computeSampleSize(BitmapFactory.Options options,
                                        int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength,
                maxNumOfPixels);

        int roundedSize;
        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }

        return roundedSize;
    }

    private static int computeInitialSampleSize(BitmapFactory.Options options,
                                                int minSideLength, int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;

        int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math
                .sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(Math
                .floor(w / minSideLength), Math.floor(h / minSideLength));

        if (upperBound < lowerBound) {
            // return the larger one when there is no overlapping zone.
            return lowerBound;
        }

        if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
            return 1;
        } else if (minSideLength == -1) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }

    public static Bitmap createBitmap(Bitmap bm) {
        Bitmap bmp = null;
        bmp = Bitmap.createBitmap(bm.getWidth(), bm.getHeight(), Bitmap.Config.ARGB_8888);
        if (bmp == null) {
            Log.d("wsh", "bmp为空 ARGB_8888");
            bmp = Bitmap.createBitmap(bm.getWidth(), bm.getHeight(), Bitmap.Config.ARGB_4444);
            if (bmp == null) {
                bmp = Bitmap.createBitmap(bm.getWidth(), bm.getHeight(), Bitmap.Config.ALPHA_8);
                if (bmp == null) {
                    bmp = Bitmap.createBitmap(bm.getWidth(), bm.getHeight(), Bitmap.Config.RGB_565);
                }
            }
        }
        return bmp;
    }
}
