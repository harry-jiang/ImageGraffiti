package com.qian.imagegraffiti;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.qian.imagegraffiti.utils.FileUtils;
import com.qian.imagegraffiti.view.TurnplateView;

import java.io.File;

public class HomeActivity extends Activity implements TurnplateView.OnTurnplateListener {
    //拍照动作
    private static final int ACTION_TAKE_PHOTO = 1;
    private static final int FLAG_CHOOSE = 2;

    private String mCurrentPhotoPath;
    private File f = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //获取屏幕的宽度和高度
        DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;
        TurnplateView view = new TurnplateView(getApplicationContext(), width, height, 100);
        view.setOnTurnplateListener(this);
        setContentView(view);

    }


    @Override
    public void onPointTouch(int flag) {
        switch (flag) {
            case 0://选择了拍照
                Toast.makeText(this, "您选择了快速拍照啦", Toast.LENGTH_SHORT).show();
                takePictureIntent(ACTION_TAKE_PHOTO);
                break;
            case 1://选择一张图片来美化
                Toast.makeText(this, "您选择了美化图片啦", Toast.LENGTH_SHORT).show();
                chooseImageIntent(FLAG_CHOOSE);
                break;
            case 2://选择拼接图片
                //Toast.makeText(this, "关于软件的介绍。。。", Toast.LENGTH_SHORT).show();
                //concatImageIntent(FLAG_CONCAT);
                Intent intent = new Intent(this, IntroduceActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.anim_down_in, R.anim.anim_down_in);
                break;

        }
    }

    private void takePictureIntent(int actionCode) {
        Intent tpIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        try {
            f = FileUtils.createImageFile();
            mCurrentPhotoPath = f.getAbsolutePath();
            /*String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
			f = new  File(Environment.getExternalStorageDirectory()+"/dcim/"+
					imageFileName+JPEG_FILE_SUFFIX);*/
            mCurrentPhotoPath = f.getAbsolutePath();
            tpIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
        } catch (Exception e) {
            e.printStackTrace();
            f = null;
            mCurrentPhotoPath = null;
        }
        startActivityForResult(tpIntent, actionCode);
    }

    private void chooseImageIntent(int actionCode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
        intent.setType("image/*");
        startActivityForResult(intent, FLAG_CHOOSE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == ACTION_TAKE_PHOTO) {//拍照

                handleCameraPhoto();

            } else if (requestCode == FLAG_CHOOSE) {//选择照片
                Uri uri = data.getData();
                Log.d("may", "uri=" + uri + ", authority=" + uri.getAuthority());
                if (!TextUtils.isEmpty(uri.getAuthority())) {
                    Cursor cursor = getContentResolver().query(uri, new String[]{MediaStore.Images.Media.DATA}, null, null, null);
                    if (null == cursor) {
                        Toast.makeText(this, R.string.no_found, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    cursor.moveToFirst();
                    String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    Log.d("may", "path1=" + path);
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra("path", path);
                    startActivity(intent);
                } else {
                    Log.d("may", "path2=" + uri.getPath());
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra("path", uri.getPath());
                    startActivity(intent);
                }
            }
        } else {
            Log.d("wsh", "结果集代码不OK");
        }
    }

    /**
     * 当拍照完毕，启动新的Activity
     */
    private void handleCameraPhoto() {
        //Bundle extras = intent.getExtras();
        //启动另一个Activity，显示美化图片的主界面
        Intent intent = new Intent(this, MainActivity.class);
        Log.d("may", "mCurrentPhotoPath=" + mCurrentPhotoPath);
        intent.putExtra("mCurrentPhotoPath", mCurrentPhotoPath);
        startActivity(intent);

    }
}
