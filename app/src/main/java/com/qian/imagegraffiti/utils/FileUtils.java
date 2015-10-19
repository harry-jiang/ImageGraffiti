package com.qian.imagegraffiti.utils;

import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jqian on 2015/10/19.
 */
public class FileUtils {
    private static final String CAMERA_DIR = "/dcim/";
    //文件前缀、后缀
    private static final String JPEG_FILE_PREFIX = "IMG_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";
    private static final String CAMERASAMPLE = "CameraSample";

    public static File getAlbumStorageDir(String albumName) {
        //初始化相册存储路径
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            return new File(
                    Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_PICTURES
                    ),
                    albumName
            );
        } else {
            return new File(
                    Environment.getExternalStorageDirectory()
                            + CAMERA_DIR
                            + albumName
            );
        }

    }

    public static File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
        File albumF = getAlbumDir();
        File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF);
        return imageF;
    }

    //获取相册路径
    public static File getAlbumDir() {
        File storageDir = null;

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {

            storageDir = getAlbumStorageDir(CAMERASAMPLE);

            if (storageDir != null) {
                if (!storageDir.mkdirs()) {
                    if (!storageDir.exists()) {
                        Log.d("CameraSample", "failed to create directory");
                        return null;
                    }
                }
            }

        } else {
            Log.v("fileUtils", "External storage is not mounted READ/WRITE.");
        }

        return storageDir;
    }
}
