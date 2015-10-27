package com.qian.imagegraffiti;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.qian.imagegraffiti.animation.ReverseAnimation;
import com.qian.imagegraffiti.utils.BitmapUtils;
import com.qian.imagegraffiti.utils.FileUtils;
import com.qian.imagegraffiti.view.CropImageView;
import com.qian.imagegraffiti.view.menu.OnMenuClickListener;
import com.qian.imagegraffiti.view.menu.SecondaryListMenuView;
import com.qian.imagegraffiti.view.menu.ToneMenuView;
import com.qian.imagegraffiti.view.model.EditImage;
import com.qian.imagegraffiti.view.model.ImageFrameAdder;
import com.qian.imagegraffiti.view.model.ImageSpecific;
import com.qian.imagegraffiti.view.model.ToneView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Stack;

/**
 * Created by jqian on 2015/10/19.
 */
public class MainActivity extends Activity implements SeekBar.OnSeekBarChangeListener {
    TextView handle_name;
    /**
     * 拍照获得的路径
     */
    private String takePath = null;
    /**
     * 选择照片获得的路径
     */
    private String pickPath = null;
    private String mCurrentPhotoPath;
    private Bitmap mBitmap;
    /**
     * 临时保存
     */
    private Bitmap mTmpBmp;
    /**
     * CropImageView
     */
    private CropImageView mImageView;
    /**
     * EditImage
     */
    private EditImage mEditImage;
    /**
     * ImageFrameAdder
     */
    private ImageFrameAdder mImageFrame;
    /**
     * ImageSpecific
     */
    private ImageSpecific mImageSpecific;

    ProgressDialog mProgressDialog;

    //以当前系统日期时间命名文件名
    private String timeStamp;
    private String imageFileName;
    //文件类型
    private static final String IMAGE_MIME_TYPE = "image/jpg";
    private static final Uri STORAGE_URI = Images.Media.EXTERNAL_CONTENT_URI;


    private final int STATE_CROP = 0x1;
    private final int STATE_DOODLE = STATE_CROP << 1;
    private final int STATE_NONE = STATE_CROP << 2;
    private final int STATE_TONE = STATE_CROP << 3;
    private final int STATE_REVERSE = STATE_CROP << 4;
    private final int STATE_HAND_WRITE = STATE_CROP << 5;
    private int mState;

    /**
     * 调色
     */
    private final int FLAG_TONE = 0x1;
    /**
     * 边框
     */
    private final int FLAG_FRAME = FLAG_TONE + 1;
    /**
     * 添加边框
     */
    private final int FLAG_FRAME_ADD = FLAG_TONE + 6;
    /**
     * 涂鸦
     */
    private final int FLAG_FRAME_DOODLE = FLAG_TONE + 7;
    /**
     * 特效
     */
    private final int FLAG_FRAME_SPECIFIC = FLAG_TONE + 10;
    /**
     * 编辑
     */
    private final int FLAG_EDIT = FLAG_TONE + 2;
    /**
     * 裁剪
     */
    private final int FLAG_EDIT_CROP = FLAG_TONE + 3;
    /**
     * 旋转
     */
    private final int FLAG_EDIT_ROTATE = FLAG_TONE + 4;
    /**
     * 缩放
     */
    private final int FLAG_EDIT_RESIZE = FLAG_TONE + 5;
    /**
     * 反转
     */
    private final int FLAG_EDIT_REVERSE = FLAG_TONE + 8;
    /**
     * 手写涂鸦
     */
    private final int FLAG_HAND_WRITING = FLAG_TONE + 9;

    /**
     * 调色菜单
     */
    private ToneMenuView mToneMenu;
    private ToneView mToneView;

    /**
     * 反转动画
     */
    private ReverseAnimation mReverseAnim;
    private int mImageViewWidth;
    private int mImageViewHeight;

    /**
     * 二级菜单
     */
    private SecondaryListMenuView mSecondaryListMenu;

    //旋转
    private final int[] ROTATE_IMGRES = new int[]{R.mipmap.ic_menu_rotate_left, R.mipmap.ic_menu_rotate_right};
    private final int[] ROTATE_TEXTS = new int[]{R.string.rotate_left, R.string.rotate_right};

    //添加边框
    private final int[] FRAME_ADD_IMAGES = new int[]{R.drawable.frame_around1, R.drawable.frame_around2, R.mipmap.frame_small1};

    //涂鸦
    private final int[] FRAME_DOODLE = new int[]{R.mipmap.btn_handwrite, R.mipmap.cloudy, R.mipmap.xiaoku, R.mipmap.xiaohong,
            R.mipmap.huzi, R.mipmap.tuer, R.mipmap.ali1, R.mipmap.ali2,
    };
    //反转
    private final int[] EDIT_REVERSE = new int[]{R.mipmap.btn_rotate_horizontalrotate, R.mipmap.btn_rotate_verticalrotate};

    //------------------------------------
    private View mSaveAll, mSaveStep, layout_buttom, layout_menu_edit, layout_menu_resize, layout_menu_rotate, layout_menu_reverse;

    private Stack<View> menuStack;

    Animation anim_in, anim_out;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 全屏显示
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.image_main);
        initView();
        initData();
        menuStack = new Stack<View>();
        showAfterMenuView(layout_buttom);
    }

    private void initView() {
        //顶部布局区域2：默认进入
        mSaveAll = findViewById(R.id.layout_save1);
        //顶部布局区域1：当进行图像处理时
        mSaveStep = findViewById(R.id.layout_save2);
        layout_buttom = findViewById(R.id.layout_buttom);
        layout_menu_edit = findViewById(R.id.layout_menu_edit);
        layout_menu_resize = findViewById(R.id.layout_menu_resize);
        layout_menu_rotate = findViewById(R.id.layout_menu_rotate);
        layout_menu_reverse = findViewById(R.id.layout_menu_reverse);
        //用于显示当前操作的名称
        handle_name = (TextView) findViewById(R.id.handle_name);
        anim_in = AnimationUtils.loadAnimation(this, R.anim.anim_down_in);
        anim_out = AnimationUtils.loadAnimation(this, R.anim.anim_down_out);
    }

    private void initData() {
        Intent intent = getIntent();
        pickPath = intent.getStringExtra("path");
        if (null == pickPath) {//如果path为空，说明是拍照而不是选择照片
            takePath = intent.getStringExtra("mCurrentPhotoPath");
            mCurrentPhotoPath = takePath;
            Log.d("may", "MainActivity--->takePath=" + takePath);
            /**end 拍照*/
        } else {
            /**start 选择照片*/
            Log.d("may", "MainActivity--->pickPath=" + pickPath);
            mCurrentPhotoPath = pickPath;
            /**end 选择照片*/
        }
        Log.d("may", "MainActivity--->pickPath=" + pickPath);
        mBitmap = BitmapUtils.getBitmapFromFile(new File(mCurrentPhotoPath), 640, 640);

        mTmpBmp = mBitmap.copy(Bitmap.Config.ARGB_8888, true);
        mImageView = (CropImageView) findViewById(R.id.crop_image);
        mImageView.setImageBitmap(mBitmap);
        mImageView.setImageBitmapResetBase(mBitmap, true);

        mEditImage = new EditImage(this, mImageView, mBitmap);
        mImageFrame = new ImageFrameAdder(this, mImageView, mBitmap);
        mImageView.setEditImage(mEditImage);

        mImageSpecific = new ImageSpecific(this);
    }

    public void onClick(View v) {
        int flag = -1;
        switch (v.getId()) {
            case R.id.btn_save1:
                String path = saveBitmap(mBitmap);
                if (mProgressDialog != null) {
                    mProgressDialog.dismiss();
                    mProgressDialog = null;
                }
//                Intent data = new Intent(this, ResultActivity.class);
//                data.putExtra("path", path);
//                startActivity(data);
                return;
            case R.id.btn_cancel1:
                //删除拍照保存原文件
                if (null != takePath) {//如果是拍照
                    File file = new File(takePath);
                    file.delete();
                }
                setResult(RESULT_CANCELED);
                finish();
                return;
            case R.id.btn_save2:
                if (mState == STATE_CROP) {
                    mTmpBmp = mEditImage.cropAndSave(mTmpBmp);
                } else if (mState == STATE_DOODLE) {
                    mTmpBmp = mImageFrame.combinate(mTmpBmp);
                } else if (mState == STATE_HAND_WRITE) {//合并手写涂鸦图片
                    //mTmpBmp = mImageFrame.combinate(mTmpBmp);
                    mTmpBmp = mImageView.combinate();
                } else if (mState == STATE_TONE) {
                    // 在菜单消失时要变调色状态为NONE状态
                    mTmpBmp = mToneView.getBitmap();
                } else if (mState == STATE_REVERSE) {
                    // 反转完成，要将ImageView反转，再设置图片
                    mReverseAnim.cancel();
                    mReverseAnim = null;
                }
                mBitmap = mTmpBmp;
                showSaveAll();
                reset();

                mEditImage.mSaving = true;
                mImageViewWidth = mImageView.getWidth();
                mImageViewHeight = mImageView.getHeight();
                showBeforeMenuView();
                return;
            case R.id.btn_cancel2:
                if (mState == STATE_CROP) {
                    mEditImage.cropCancel();
                } else if (mState == STATE_DOODLE) {
                    mImageFrame.cancelCombinate();
                } else if (mState == STATE_HAND_WRITE) {
                    mTmpBmp = mImageView.cancelCombinate();
                } else if (mState == STATE_REVERSE) {
                    mReverseAnim.cancel();
                }
                showSaveAll();
                resetToOriginal();
                showBeforeMenuView();
                return;
            case R.id.edit:
                showAfterMenuView(layout_menu_edit);
                break;
            case R.id.tone:
                initTone();
                showSaveStep();
                return;
            case R.id.frame:
                initSecondaryMenu(FLAG_FRAME_ADD, 11);
                return;
            case R.id.myDoodle:
                flag = FLAG_FRAME_DOODLE;
                initSecondaryMenu(FLAG_FRAME_DOODLE, 11);
                return;
            case R.id.mySpecial:
                initSecondaryMenu(FLAG_FRAME_SPECIFIC, 11);
                return;
        }

        //   initMenu(flag);
    }

    private String saveBitmap(Bitmap bm) {
        mProgressDialog = ProgressDialog.show(this, null, getResources().getString(R.string.save_bitmap));
        mProgressDialog.show();
        //保存图片文件的方法
        File f = null;
        try {
            f = FileUtils.createImageFile();
            mCurrentPhotoPath = f.getAbsolutePath();
            BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(f));
            bm.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        //删除拍照保存原文件
        if (null != takePath) {//如果是拍照
            File file = new File(takePath);
            file.delete();
        }
        //把图片保存到手机相册(图库)里
        ContentResolver contentResolver = getContentResolver();
        ContentValues values = new ContentValues(7);

        values.put(Images.Media.TITLE, f.getName());
        values.put(MediaStore.Images.Media.DISPLAY_NAME, imageFileName);
        values.put(Images.Media.DATE_TAKEN, timeStamp);
        values.put(Images.Media.MIME_TYPE, IMAGE_MIME_TYPE);
        //values.put(Images.Media.ORIENTATION, degree[0]);
        values.put(Images.Media.DATA, mCurrentPhotoPath);
        values.put(Images.Media.SIZE, f.length());

        contentResolver.insert(STORAGE_URI, values);

        return mCurrentPhotoPath;
        //return mEditImage.saveToLocal(bm);
    }

    private void showSaveStep() {
        startAnimationIn(mSaveStep);
        startAnimationOut(mSaveAll);
    }

    private void showSaveAll() {
        startAnimationIn(mSaveAll);
        startAnimationOut(mSaveStep);
    }

    /**
     * 重新设置一下图片
     */
    private void reset() {
        mImageView.setImageBitmap(mTmpBmp);
        mImageView.invalidate();
    }

    private void resetToOriginal() {
        mTmpBmp = mBitmap;
        mImageView.setImageBitmap(mBitmap);
        // 已经保存图片
        mEditImage.mSaving = true;
        // 清空裁剪操作
        mImageView.mHighlightViews.clear();
    }

    private void initTone() {
        if (null == mToneMenu) {
            mToneMenu = new ToneMenuView(this);
        }

        mToneMenu.show();

        mState = STATE_TONE;

        mToneView = mToneMenu.getToneView();
        mToneMenu.setHueBarListener(this);
        mToneMenu.setLumBarListener(this);
        mToneMenu.setSaturationBarListener(this);
        mToneMenu.setConBarListener(this);
    }

    /**
     * 初始化二级菜单
     *
     * @param flag
     * @param left
     */
    private void initSecondaryMenu(int flag, int left) {
        mSecondaryListMenu = new SecondaryListMenuView(this);
        mSecondaryListMenu.setBackgroundResource(R.drawable.popup_bottom_tip);
        mSecondaryListMenu.setTextSize(16);
        mSecondaryListMenu.setWidth(300);
        mSecondaryListMenu.setMargin(left);
        switch (flag) {

            case FLAG_EDIT_REVERSE: // 反转
                mSecondaryListMenu.setImageRes(EDIT_REVERSE);
                //mSecondaryListMenu.setHeight(80);
                //      mSecondaryListMenu.setOnMenuClickListener(reverseListener());
                break;
            case FLAG_FRAME_ADD: // 添加边框
                //mSecondaryListMenu.setWidth(400);
                mSecondaryListMenu.setImageRes(FRAME_ADD_IMAGES);
                mSecondaryListMenu.setMargin(180);
                mSecondaryListMenu.setBtmMargin(55);
                //    mSecondaryListMenu.setOnMenuClickListener(addFrameListener());
                break;
            case FLAG_FRAME_DOODLE: // 涂鸦
                mSecondaryListMenu.setImageRes(FRAME_DOODLE);
                mSecondaryListMenu.setMargin(180);
                mSecondaryListMenu.setBtmMargin(55);
                //mSecondaryListMenu.setText(DOODLE_TEXTS);
                //       mSecondaryListMenu.setOnMenuClickListener(doodleListener());
                break;
            case FLAG_FRAME_SPECIFIC: // 特效
                mSecondaryListMenu.setWidth(420);
                mSecondaryListMenu.setHeight(100);
                mSecondaryListMenu.setMargin(80);
                mSecondaryListMenu.setBtmMargin(55);
                mSecondaryListMenu.setText(getResources().getStringArray(R.array.specific_item));
                //       mSecondaryListMenu.setOnMenuClickListener(specificListener());
                break;
        }

        mSecondaryListMenu.show();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        int flag = -1;
        switch ((Integer) seekBar.getTag()) {
            case 1: // 饱和度
                flag = 1;
                mToneView.setSaturation(progress);
                break;
            case 2: // 色调
                flag = 0;
                mToneView.setHue(progress);
                break;
            case 3: // 亮度
                flag = 2;
                mToneView.setLum(progress);
                break;
            case 4://对比度
                flag = 3;
                mToneView.setCon(progress);
                break;
        }
        Bitmap bm = mToneView.handleImage(mTmpBmp, flag);
        //Bitmap bm = mToneView.handleImage(mBitmap, flag);
        mImageView.setImageBitmapResetBase(bm, true);
        mImageView.center(true, true);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    public void onEditMenuClick(View view) {
        switch (view.getId()) {
            case R.id.btn_menu_crop://剪裁
                crop();
                handle_name.setText(R.string.crop);
                layout_menu_edit.setVisibility(View.GONE);
                break;
            case R.id.btn_menu_rotate://旋转
                showAfterMenuView(layout_menu_rotate);
                break;
            case R.id.btn_menu_resize://缩放
                showAfterMenuView(layout_menu_resize);
                handle_name.setText(R.string.resize);
                break;
            case R.id.btn_menu_reverse_transform://翻转
                showAfterMenuView(layout_menu_reverse);
                handle_name.setText(R.string.reverse_transform);
                break;
        }
        showSaveStep();
    }

    /**
     * 裁剪
     */
    private void crop() {
        // 进入裁剪状态
        prepare(STATE_CROP, CropImageView.STATE_HIGHLIGHT, false);
        mEditImage.crop(mTmpBmp);
        reset();
    }

    /**
     * 旋转
     *
     * @param view
     */
    public void onRotateMenuClick(View view) {
        switch (view.getId()) {
            case R.id.btn_rotate_left: // 左旋转
                rotate(-90);
                break;
            case R.id.btn_rotate_right: // 右旋转
                rotate(90);
                break;
        }
    }

    /**
     * 旋转
     *
     * @param degree
     */
    private void rotate(float degree) {
        // 未进入特殊状态
        mImageViewWidth = mImageView.getWidth();
        mImageViewHeight = mImageView.getHeight();
        Bitmap bm = mEditImage.rotate(mTmpBmp, degree);
        mTmpBmp = bm;
        reset();
    }

    /**
     * 缩放
     *
     * @param view
     */
    public void onResizeMenuClick(View view) {
        String title = getString(R.string.resize);
        float scale = 1.0F;
        switch (view.getId()) {
            case R.id.btn_resize_one_to_two:
                scale /= 2;
                title = title + getString(R.string.resize_one_to_two);
                break;
            case R.id.btn_resize_one_to_three:
                scale /= 3;
                title = title + getString(R.string.resize_one_to_three);
                break;
            case R.id.btn_resize_one_to_four:
                scale /= 4;
                title = title + getString(R.string.resize_one_to_four);
                break;
            case R.id.btn_resize_two_to_one:
                scale *= 2;
                title = title + getString(R.string.resize_two_to_one);
                break;
        }
        resize(scale, title);
    }

    /**
     * 缩放
     */
    private void resize(float scale, String title) {
        // 未进入特殊状态
        prepare(STATE_NONE, CropImageView.STATE_NONE, true);
        handle_name.setText(title);
        Bitmap bmp = mEditImage.resize(mTmpBmp, scale);
        mTmpBmp = bmp;
        reset();
    }

    /**
     * 翻转
     *
     * @param view
     */
    public void onReverseMenuClick(View view) {
        switch (view.getId()) {
            case R.id.btn_rotate_horizontalrotate: // 水平翻转
                reverse(ReverseAnimation.HORIZONTAL);
                break;
            case R.id.btn_rotate_verticalrotate: // 垂直翻转
                reverse(ReverseAnimation.VERTICAL);
                break;
        }
    }

    private void reverse(int flag)
    {
        // 未进入特殊状态
        prepare(STATE_REVERSE, CropImageView.STATE_NONE, true);

        int type = 0;
        switch (flag)
        {
            case 0:
                type = ReverseAnimation.HORIZONTAL;
                break;
            case 1:
                type = ReverseAnimation.VERTICAL;
                break;
        }

        mReverseAnim = new ReverseAnimation(0F, 180F, mImageViewWidth == 0 ? mImageView.getWidth() / 2 : mImageViewWidth / 2, mImageViewHeight == 0 ? mImageView.getHeight() / 2 : mImageViewHeight / 2, 0, true);
        mReverseAnim.setReverseType(type);
        mReverseAnim.setDuration(1000);
        mReverseAnim.setFillEnabled(true);
        mReverseAnim.setFillAfter(true);
        mImageView.startAnimation(mReverseAnim);
        Bitmap bm = mEditImage.reverse(mTmpBmp, flag);
        mTmpBmp = bm;
//		reset();
    }

    /**
     * 显示下一级菜单
     * @param view
     */
    private void showAfterMenuView(View view) {
        if (view == null) {
            return;
        }
        if (!menuStack.empty()) {
            View topView = menuStack.peek();
            startAnimationOut(topView);
        }

        startAnimationIn(view);
        menuStack.push(view);
    }

    /**
     * 菜单显示动画
     * @param view
     */
    private void startAnimationIn(View view) {
        view.clearAnimation();
        view.startAnimation(anim_in);
        view.setVisibility(View.VISIBLE);
    }
    /**
     * 菜单消失动画
     * @param view
     */
    private void startAnimationOut(View view) {
        view.clearAnimation();
        view.startAnimation(anim_out);
        view.setVisibility(view == layout_buttom ? View.INVISIBLE : View.GONE);
    }

    /**
     * 显示上一级菜单
     *
     * @return
     */
    private boolean showBeforeMenuView() {
        if (menuStack.size() > 1) {
            View topView = menuStack.peek();
            if (topView.getVisibility() != View.GONE) {
                startAnimationOut(topView);
                menuStack.pop();
            }
        } else {
            return false;
        }
        View topView = menuStack.peek();
        startAnimationIn(topView);
        return true;
    }

    /**
     * 进行操作前的准备
     *
     * @param state          当前准备进入的操作状态
     * @param imageViewState ImageView要进入的状态
     * @param hideHighlight  是否隐藏裁剪框
     */
    private void prepare(int state, int imageViewState, boolean hideHighlight) {
        resetToOriginal();
        mEditImage.mSaving = false;
        if (null != mReverseAnim) {
            mReverseAnim.cancel();
            mReverseAnim = null;
        }

        if (hideHighlight) {
            mImageView.hideHighlightView();
        }
        mState = state;
        mImageView.setState(imageViewState);
        mImageView.invalidate();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                showBeforeMenuView();
                return true;

        }

        return super.onKeyDown(keyCode, event);
    }
}
