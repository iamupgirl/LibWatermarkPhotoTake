package me.nereo.multi_image_selector;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

import me.nereo.multi_image_selector.ifs.CameraInterface;
import me.nereo.multi_image_selector.utils.ImageUtil;
import me.nereo.multi_image_selector.utils.ScreenUtils;
import me.nereo.multi_image_selector.view.MySurfaceView;
import me.nereo.multi_image_selector.view.WaterMarkView;

/**
 * 水印拍照页面
 * Created by sherry on 2017/9/27.
 */
public class WatermarkActivity extends Activity implements CameraInterface.CamOpenOverCallback, CameraInterface.OnSavePhotoListener {

    private static final String TAG = "WatermarkActivity";

    public static final String OUTPUT_PATH = "output_path";
    public static final String LOCATION_DATA = "location_data";

    private String mFilePath, mLocationData;
    private float mPreviewRate = -1f;

    private MySurfaceView vSurfaceView;
    private ImageView vIvTakePhoto, vIvCancel, vIvExchange;
    private WaterMarkView vWaterMark;

    private OnPermissionListener onPermissionListener; // 权限监听
    private boolean mHadTake = false;
    private boolean mHadSave = false;

    private OrientationEventListener mOrientListener;
    private int mScreenExifOrientation;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mis_watermark);
        setSurfaceView();
        mFilePath = getIntent().getStringExtra(WatermarkActivity.OUTPUT_PATH);
        mLocationData = getIntent().getStringExtra(WatermarkActivity.LOCATION_DATA);
        onPermissionRequests(Manifest.permission.CAMERA, new OnPermissionListener() {
            @Override
            public void onClick(boolean bln) {

                if (bln) {
                    Log.d("Sherry", "进入权限11");
                    Thread openThread = new Thread() {
                        @Override
                        public void run() {
                            Log.d("Sherry", "doOpenCamera");
                            CameraInterface.getInstance(WatermarkActivity.this).doOpenCamera(WatermarkActivity.this);
                        }
                    };
                    openThread.start();
                } else {
                    Toast.makeText(WatermarkActivity.this, "拍照或无法正常使用", Toast.LENGTH_SHORT).show();
                }
            }
        });
        setView();
        mOrientListener = new OrientationEventListener(this) {
            @Override
            public void onOrientationChanged(int orientation) {
                // i的范围是0～359
                // 屏幕左边在顶部的时候 i = 90;
                // 屏幕顶部在底部的时候 i = 180;
                // 屏幕右边在底部的时候 i = 270;
                // 正常情况默认i = 0;
                if (45 <= orientation && orientation < 135) {
                    if (mScreenExifOrientation != ExifInterface.ORIENTATION_ROTATE_180) {
                        mScreenExifOrientation = ExifInterface.ORIENTATION_ROTATE_180;
                        setSelfVisible(mScreenExifOrientation);
                    }
                } else if (135 <= orientation && orientation < 225) {
                    if (mScreenExifOrientation != ExifInterface.ORIENTATION_ROTATE_270) {
                        mScreenExifOrientation = ExifInterface.ORIENTATION_ROTATE_270;
                        setSelfVisible(mScreenExifOrientation);
                    }
                } else if (225 <= orientation && orientation < 315) {
                    if (mScreenExifOrientation != ExifInterface.ORIENTATION_NORMAL) {
                        mScreenExifOrientation = ExifInterface.ORIENTATION_NORMAL;
                        setSelfVisible(mScreenExifOrientation);
                    }
                } else {
                    if (mScreenExifOrientation != ExifInterface.ORIENTATION_ROTATE_90) {
                        mScreenExifOrientation = ExifInterface.ORIENTATION_ROTATE_90;
                        setSelfVisible(mScreenExifOrientation);
                    }
                }
            }
        };
        mOrientListener.enable();
    }

    /**
     * @param mScreenExifOrientation
     */
    private void setSelfVisible(int mScreenExifOrientation) {
        switch (mScreenExifOrientation) {
            case 1: // 向左
                vWaterMark.setDegree(90);
                break;
            case 3: // 向右
                vWaterMark.setDegree(-90);
                break;
            case 6:
                vWaterMark.setDegree(0);
                break;
            case 8: // 倒置
                vWaterMark.setDegree(180);
                break;
        }
        vWaterMark.invalidate();
    }

    private void setSurfaceView() {
        vSurfaceView = (MySurfaceView) findViewById(R.id.take_photo_surfaceView);
        ViewGroup.LayoutParams params = vSurfaceView.getLayoutParams();
        Point p = ScreenUtils.getScreenMetrics(this);
        params.width = p.x;
        params.height = p.y;
        mPreviewRate = ScreenUtils.getScreenRate(this); //默认全屏的比例预览
        vSurfaceView.setLayoutParams(params);

        vSurfaceView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!mHadTake && !mHadSave) {
                    focusOnTouch((int) event.getX(), (int) event.getY());
                    vSurfaceView.setFocusXY(event.getX(), event.getY());
                    vSurfaceView.invalidate();
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            vSurfaceView.clear();
                            vSurfaceView.invalidate();
                        }
                    };
                    mHandler.postDelayed(runnable, 2000);
                }
                return false;
            }
        });
    }

    /**
     * 在点击处聚焦
     * @param x
     * @param y
     */
    private void focusOnTouch(int x, int y) {
        Rect rect = new Rect(x - 100, y - 100, x + 100, y + 100);
        int left = rect.left * 2000 / vSurfaceView.getWidth() - 1000;
        int top = rect.top * 2000 / vSurfaceView.getHeight() - 1000;
        int right = rect.right * 2000 / vSurfaceView.getWidth() - 1000;
        int bottom = rect.bottom * 2000 / vSurfaceView.getHeight() - 1000;
        // 如果超出了(-1000,1000)到(1000, 1000)的范围，则会导致相机崩溃
        left = left < -1000 ? -1000 : left;
        top = top < -1000 ? -1000 : top;
        right = right > 1000 ? 1000 : right;
        bottom = bottom > 1000 ? 1000 : bottom;
        CameraInterface.getInstance(this).focusOnRect(new Rect(left, top, right, bottom));
    }

    private void setView() {
        vWaterMark = (WaterMarkView) findViewById(R.id.rl_time_location);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        String str = formatter.format(curDate);
        vWaterMark.setTvTime(str);
        vWaterMark.setTvLocation(mLocationData);

        vIvTakePhoto = (ImageView) findViewById(R.id.iv_take_photo);
        vIvTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mHadTake) {
                    mHadTake = true;
                    takePhoto();
                } else {
                    WatermarkActivity.this.setResult(RESULT_OK);
                    WatermarkActivity.this.finish();
                }
            }
        });
        vIvCancel = (ImageView) findViewById(R.id.iv_take_photo_close);
        vIvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WatermarkActivity.this.setResult(RESULT_CANCELED);
                WatermarkActivity.this.finish();
            }
        });
        vIvExchange = (ImageView) findViewById(R.id.iv_take_photo_exchange);
        vIvExchange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mHadTake && !mHadSave) {
                    Thread openThread = new Thread() {
                        @Override
                        public void run() {
                            Log.d("Sherry", "exchangeDirection");
                            CameraInterface.getInstance(WatermarkActivity.this).change(WatermarkActivity.this);
                        }
                    };
                    openThread.start();
                }
            }
        });
    }

    private void takePhoto() {
        if (ImageUtil.hasSdcard()) {
            try {
                CameraInterface.getInstance(WatermarkActivity.this).doTakePicture(WatermarkActivity.this);
                mOrientListener.disable();
            } catch (Exception e) {
                e.printStackTrace();
                mHadTake = false;
            }
        } else {
            Toast.makeText(this, "没找到SD卡", Toast.LENGTH_LONG).show();
            mHadTake = false;
        }
    }

    public void onPermissionRequests(String permission, OnPermissionListener listener) {
        onPermissionListener = listener;
        Log.d("WatermarkActivity", "0");
        if (ContextCompat.checkSelfPermission(this,
                permission)
                != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            Log.d("WatermarkActivity", "1");
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_CONTACTS)) {
                //权限已有
                onPermissionListener.onClick(true);
            } else {
                //没有权限，申请一下
                ActivityCompat.requestPermissions(this,
                        new String[]{permission},
                        1);
            }
        } else {
            onPermissionListener.onClick(true);
            Log.d("WatermarkActivity", "2" + ContextCompat.checkSelfPermission(this,
                    permission));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //权限通过
                if (onPermissionListener != null) {
                    onPermissionListener.onClick(true);
                }
            } else {
                //权限拒绝
                if (onPermissionListener != null) {
                    onPermissionListener.onClick(false);
                }
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void cameraHasOpened() {
        // TODO Auto-generated method stub
        SurfaceHolder holder = vSurfaceView.getSurfaceHolder();
        try {
            CameraInterface.getInstance(WatermarkActivity.this).doStartPreview(holder, mPreviewRate, ScreenUtils.getScreenWidth(this), ScreenUtils.getScreenHeight(this));
        } catch (Exception e) {

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 解决三星S6等部分机型的黑屏问题
                try {
                    SurfaceHolder holder = vSurfaceView.getSurfaceHolder();
                    while (!holder.getSurface().isValid()) {
                        Thread.sleep(50);
                    }
                    Log.d("Sherry", "doStartPreview");
                    CameraInterface.getInstance(WatermarkActivity.this).doStartPreview(holder, mPreviewRate, ScreenUtils.getScreenWidth(WatermarkActivity.this), ScreenUtils.getScreenHeight(WatermarkActivity.this));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        CameraInterface.getInstance(WatermarkActivity.this).doStopCamera();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CameraInterface.getInstance(WatermarkActivity.this).doDestroyedCamera();
        mOrientListener.disable();
    }

    @Override
    public void savePhoto(Bitmap b, String currentTime) {

        mHadTake = true;
        mHadSave = true;

        vIvTakePhoto.setImageResource(R.drawable.icon_take_ok);

        ImageUtil.addLocation(this, mScreenExifOrientation, b, currentTime, mLocationData, mFilePath);

        Log.d("sherry", "保存图片成功，退出拍照页面~");
    }

}