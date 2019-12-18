package me.nereo.multi_image_selector.ifs;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.SurfaceHolder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import me.nereo.multi_image_selector.utils.CameraParaUtil;
import me.nereo.multi_image_selector.utils.ImageUtil;

/**
 * Created by sherry on 2016/9/26.
 */
public class CameraInterface {

    private final String TAG = "CameraInterface";
    private Camera mCamera;
    private Camera.Parameters mParams;
    private int mCameraCount;
    private static CameraInterface mCameraInterface;

    private static CameraInterface.OnSavePhotoListener mSaveListener;
    private int mCameraPos = 1;

    public interface CamOpenOverCallback {
        void cameraHasOpened();
    }

    public interface OnSavePhotoListener {
        void savePhoto(Bitmap bitmap, String currentTime);
    }

    private CameraInterface() {
    }

    public static synchronized CameraInterface getInstance(OnSavePhotoListener listener) {
        mSaveListener = listener;
        if (mCameraInterface == null) {
            mCameraInterface = new CameraInterface();
        }
        return mCameraInterface;
    }


    /**
     * 打开Camera前置摄像头
     *
     * @param callback
     */
    public void doOpenCamera(CamOpenOverCallback callback) {
        Log.i(TAG, "Camera open....");
        mCamera = null;
        try {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            mCameraCount = Camera.getNumberOfCameras(); // get cameras number
            for (int camIdx = 0; camIdx < mCameraCount; camIdx++) {
                Camera.getCameraInfo(camIdx, cameraInfo); // get camerainfo
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    // 代表摄像头的方位，目前有定义值两个分别为CAMERA_FACING_FRONT前置和CAMERA_FACING_BACK后置
                    mCamera = Camera.open(camIdx);
                    mCameraPos = 1;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "摄像头未正常打开" + e);
        }

        Log.i(TAG, "Camera open over....");
        callback.cameraHasOpened();
    }

    /**
     * 开启预览
     *
     * @param holder
     * @param previewRate
     */
    public void doStartPreview(SurfaceHolder holder, float previewRate, int width, int height) throws Exception {
        Log.i(TAG, "doStartPreview...");
        if (mCamera != null) {
            try {
                // 解决自定义相机预览时屏幕旋转90度的问题
                mCamera.setDisplayOrientation(90);
                mParams = mCamera.getParameters();
                if (isSupportZoom()) {
                    mParams.setZoom(1);
                }
                mParams.setPictureFormat(PixelFormat.JPEG);//设置拍照后存储的图片格式
                if (mCameraPos == 0) {
                    mParams.set("orientation", "portrait");
                    mParams.set("rotation", 180);
                }
                // 设置PreviewSize和PictureSize
                Size previewSize = CameraParaUtil.getInstance().getPropPreviewSize(
                        mParams.getSupportedPreviewSizes(), previewRate, width, height);
                Log.i(TAG, "width =" + previewSize.height + ", height =" + previewSize.width);
                Size pictureSize = CameraParaUtil.getInstance().getPropPictureSize(
                        mParams.getSupportedPictureSizes(), previewSize.width, previewSize.height);
                mParams.setPreviewSize(previewSize.width, previewSize.height);
                mParams.setPictureSize(pictureSize.width, pictureSize.height);
                CameraParaUtil.getInstance().printSupportFocusMode(mParams);
                List<String> focusModes = mParams.getSupportedFocusModes();
                if (focusModes.contains("continuous-video")) {
                    mParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                }
                mCamera.setParameters(mParams);
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();//开启预览

                mParams = mCamera.getParameters(); //重新get一次
                Log.i(TAG, "最终设置1:PreviewSize--With = " + mParams.getPreviewSize().width
                        + "Height = " + mParams.getPreviewSize().height);
                Log.i(TAG, "最终设置2:PictureSize--With = " + mParams.getPictureSize().width
                        + "Height = " + mParams.getPictureSize().height);
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("sherry", e + "预览失败，请重试");
            }
        }
    }

    public int getPictureWidth() throws Exception {
        return mParams.getPictureSize().width;
    }

    public int getPictureHeight() throws Exception {
        return mParams.getPictureSize().height;
    }

    /**
     * 停止预览，不需要释放Camera
     */
    public void doStopCamera() {
        try {
            if (null != mCamera) {

                mCamera.setPreviewCallback(null);
                mCamera.stopPreview();
            }
        } catch (Exception e) {
        }
    }

    /**
     * 停止预览，释放Camera
     */
    public void doDestroyedCamera() {
        try {
            if (null != mCamera) {
                mCamera.setPreviewCallback(null);
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }
        } catch (Exception e) {
        }
    }

    /**
     * 是否支持变焦
     *
     * @return isSupport
     */
    public boolean isSupportZoom() {
        boolean isSupport = true;
        try {
            if (mCamera.getParameters().isSmoothZoomSupported()) {
                isSupport = false;
            }
        } catch (Exception e) {
            return true;
        }
        return isSupport;
    }

    /**
     * 拍照
     */
    public void doTakePicture(final Context context) throws Exception {
        mCamera.takePicture(null , null, mJpegPictureCallback);
//        // 根据系统音量调节快门声音
//        // 获取当前系统音量
//        final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
//        int curVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
//        if (curVolume == 0) { // 0代表静音或者震动
//            final Handler soundHandler = new Handler();
//            Timer t = new Timer();
//            t.schedule(new TimerTask() {
//                @Override
//                public void run() {
//                    soundHandler.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            audioManager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
//                        }
//                    });
//
//                }
//            }, 1000);
//        }
    }

    /*为了实现拍照的快门声音及拍照保存照片需要下面三个回调变量*/
    Camera.ShutterCallback mShutterCallback = new Camera.ShutterCallback() {
        //快门按下的回调，在这里我们可以设置类似播放“咔嚓”声之类的操作。默认的就是咔嚓。
        public void onShutter() {
            // TODO Auto-generated method stub
            Log.i(TAG, "myShutterCallback:onShutter...");
        }
    };

    Camera.PictureCallback mRawCallback = new Camera.PictureCallback() {
        // 拍摄的未压缩原数据的回调,可以为null
        public void onPictureTaken(byte[] data, Camera camera) {
            // TODO Auto-generated method stub
            Log.i(TAG, "myRawCallback:onPictureTaken...");

        }
    };

    Camera.PictureCallback mJpegPictureCallback = new Camera.PictureCallback() {
        //对jpeg图像数据的回调,最重要的一个回调
        public void onPictureTaken(byte[] data, Camera camera) {
            // TODO Auto-generated method stub
            Log.i(TAG, "myJpegCallback:onPictureTaken...");
            Bitmap b = null;
            if (null != data) {
                b = BitmapFactory.decodeByteArray(data, 0, data.length);//data是字节数据，将其解析成位图
                mCamera.stopPreview();
            }
            //保存图片到sdcard
            if (null != b) {
                Bitmap hBitmap = ImageUtil.rotaingImageView(90, b);
                if (b != null) {
                    b.recycle();
                    b = null;
                }
                if (mSaveListener != null) {
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date curDate = new Date(System.currentTimeMillis());//获取当前时间
                    String str = formatter.format(curDate);

                    mSaveListener.savePhoto(hBitmap, str);
                } else {
                    Log.d("sherry", "mSaveListener为null");
                }
            }
        }
    };

    Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
        }
    };

    public void focusOnRect(Rect rect) {
        if (mCamera != null) {
            Camera.Parameters parameters = mCamera.getParameters(); // 先获取当前相机的参数配置对象
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO); // 设置聚焦模式
            Log.d(TAG, "parameters.getMaxNumFocusAreas() : " + parameters.getMaxNumFocusAreas());
            if (parameters.getMaxNumFocusAreas() > 0) {
                List<Camera.Area> focusAreas = new ArrayList<Camera.Area>();
                focusAreas.add(new Camera.Area(rect, 1000));
                parameters.setFocusAreas(focusAreas);
            }
            mCamera.cancelAutoFocus(); // 先要取消掉进程中所有的聚焦功能
            mCamera.setParameters(parameters); // 一定要记得把相应参数设置给相机
            mCamera.autoFocus(autoFocusCallback);
        }
    }

    public void change(CamOpenOverCallback callback) {
        //切换前后摄像头
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        int cameraCount = Camera.getNumberOfCameras();//得到摄像头的个数

        for (int i = 0; i < cameraCount; i++) {
            Camera.getCameraInfo(i, cameraInfo);//得到每一个摄像头的信息
            if (mCameraPos == 1) {
                //现在是后置，变更为前置
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {//代表摄像头的方位，CAMERA_FACING_FRONT前置      CAMERA_FACING_BACK后置
                    closeCamera(i);
                    mCameraPos = 0;
                    break;
                }
            } else {
                //现在是前置， 变更为后置
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {//代表摄像头的方位，CAMERA_FACING_FRONT前置      CAMERA_FACING_BACK后置
                    closeCamera(i);
                    mCameraPos = 1;
                    break;
                }
            }
        }
        Log.i(TAG, "Camera open over....");
        callback.cameraHasOpened();
    }

    private void closeCamera(int i) {
        mCamera.stopPreview();//停掉原来摄像头的预览
        mCamera.release();//释放资源
        mCamera = null;//取消原来摄像头
        mCamera = Camera.open(i);//打开当前选中的摄像头
    }

}
