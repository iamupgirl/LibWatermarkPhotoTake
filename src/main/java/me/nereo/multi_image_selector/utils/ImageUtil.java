package me.nereo.multi_image_selector.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.zip.GZIPOutputStream;

import me.nereo.multi_image_selector.R;

/**
 * Description the class 用户头像上传工具类
 */
public class ImageUtil {

    public static final int BUFFER_SIZE = 1024;
    public static final int CONNECT_TIMEOUT = 10000;
    public static final String TAG = "ImageUtil";

    /**
     * 判断是否有SD卡
     *
     * @return true为有SDcard，false则表示没有
     */
    public static boolean hasSdcard() {
        boolean hasCard = false;
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            hasCard = true;
        }
        return hasCard;
    }

    /**
     * 使用系统当前日期加以调整作为照片的名称
     *
     * @return String 文件拍照名字
     * @throws
     */
    public static String getPhotoFileName() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("'IMG'_yyyyMMdd_HHmmss", Locale.CHINA);
        return dateFormat.format(date) + ".jpg";
    }

    /**
     * 检查是否有相机
     *
     * @return boolean true为有相机，false则表示没有
     */
    public static boolean hasCamera(Context contenxt) {
        boolean hasCamera = false;
        PackageManager pm = contenxt.getPackageManager();
        if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            hasCamera = true;
        }
        return hasCamera;
    }

    /**
     * 压缩图片成gz文件
     *
     * @param srcFile  需要被压缩的文件
     * @param destFile 压缩后的文件
     */
    public static void doCompressFile(Context context, File srcFile, File destFile) {
        try {
            if (null != destFile && destFile.exists()) {
                destFile.delete();
            }
            GZIPOutputStream out = null;
            try {
                out = new GZIPOutputStream(new FileOutputStream(destFile));
            } catch (FileNotFoundException e) {
                Log.d(TAG, e.toString());
            }
            FileInputStream in = null;
            try {
                in = new FileInputStream(srcFile);
                byte[] buf = new byte[BUFFER_SIZE];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            } catch (FileNotFoundException e) {
                Log.d(TAG, e.toString());
            } finally {
                in.close();
                out.finish();
                out.close();
            }
        } catch (IOException e) {
            Log.e(TAG, e + "");
        }
    }

    /**
     * 处理图片
     *
     * @param bm        所要转换的bitmap
     * @param newWidth  新的宽
     * @param newHeight 新的高
     * @return 指定宽高的bitmap
     */
    public static Bitmap zoomImg(Bitmap bm, int newWidth, int newHeight) {
        // 获得图片的宽高
        int width = bm.getWidth();
        int height = bm.getHeight();
        // 计算缩放比例
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片
        Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
        return newbm;
    }

    /**
     * @param bm        原图
     * @param newWidth  希望缩放后得到的宽
     * @param newHeight 希望缩放后得到的高
     * @return 缩放截取上部分的位图。
     */
    public static Bitmap zoomAndClipImg(Bitmap bm, int degree, int newWidth, int newHeight) {
        Bitmap result = bm;
        // 得到新的图片
        Bitmap scaleBitmap;
        try {
            scaleBitmap = ImageUtil.zoomImg(bm, newWidth, newHeight);
        } catch (Exception e) {
            return null;
        }
        try {
            // 定义矩阵对象
            Matrix matrix = new Matrix();
            // 参数为负则向左旋转，参数为正则向右旋转
            if (degree == ExifInterface.ORIENTATION_NORMAL) {
                matrix.postRotate(-90);
            } else if (degree == ExifInterface.ORIENTATION_ROTATE_270) {
                matrix.postRotate(180);
            } else if (degree == ExifInterface.ORIENTATION_ROTATE_180) {
                matrix.postRotate(90);
            }
            result = Bitmap.createBitmap(scaleBitmap, 0, 0, newWidth, newHeight, matrix, true);
            scaleBitmap.recycle();
        } catch (Exception e) {
            return null;
        }
        return result;
    }

    /**
     * 存储裁剪后的图片到本地
     *
     * @param bfile    照片的字节数组
     * @param filePath 照片的存储路径
     * @param fileName 照片的名字
     * @return File 文件对象
     */
    public static File getFile(Context context, byte[] bfile, String filePath, String fileName) {
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        File file = null;
        try {
            File dir = new File(filePath);
            if (!dir.exists() && dir.isDirectory()) {// 判断文件目录是否存在
                dir.mkdirs();
            }
            file = new File(filePath + "/" + fileName);
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(bfile);
        } catch (Exception e) {
            Log.e(TAG, e + "");
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e1) {
                    Log.e(TAG, e1 + "");
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {
                    Log.e(TAG, e1 + "");
                }
            }
        }
        return file;
    }

    /**
     * Uri转bitmap
     *
     * @param uri 图像的uri对象
     * @return Bitmap 对象
     * @throws
     */
    public static Bitmap getBitmapFromUri(Context context, Uri uri) {
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
            return bitmap;
        } catch (Exception e) {
            Log.e(TAG, e + "");
        }
        return bitmap;
    }

    /**
     * @param context 上下文
     * @param url     网络图片文件路径
     * @param @return
     * @return File
     * @throws
     * @Method: getLoadPicFileFromUrl
     * @Description: 通过url生产图片文件路径
     */
    public static File getLoadPicFileFromUrl(Context context, String url) {
        if (url != null) {
            int lastSpit = url.lastIndexOf("?");
            int firstSpit = url.lastIndexOf("/");
            String fileName = null;
            if (lastSpit >= 0 && firstSpit >= 0) {
                fileName = url.substring(firstSpit, lastSpit);
            }
            if (fileName == null) {
                return null;
            }
            File cacheDir = null;
            if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
                cacheDir = new File(android.os.Environment.getExternalStorageDirectory(), getConfigString(
                        context, "image_dir"));
            } else {
                cacheDir = context.getCacheDir();
            }
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            return new File(cacheDir, fileName);
        }
        return null;
    }

    /**
     * 读取manifest.xml中application标签下的配置项，如果不存在，则返回空字符串
     *
     * @param key 键名
     * @return 返回字符串
     */
    public static String getConfigString(Context context, String key) {
        String val = "";
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(),
                    PackageManager.GET_META_DATA);
            val = appInfo.metaData.getString(key);
            if (val == null) {
                Log.e(TAG, "please set config value for " + key + " in manifest.xml first");
            }
        } catch (Exception e) {
            Log.w(TAG, e);
        }
        return val;
    }

    /**
     * 从drawable中读取图片
     *
     * @param context
     * @param drawableId
     * @return
     */
    public static BitmapDrawable getResourceBitmap(Context context, int drawableId) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        InputStream is = context.getResources().openRawResource(drawableId);
        Bitmap bm = BitmapFactory.decodeStream(is, null, opt);
        BitmapDrawable bd = new BitmapDrawable(context.getResources(), bm);
        return bd;
    }

    public static Bitmap rotaingImageView(int angle, Bitmap bitmap) {
        // 旋转图片 动作
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        // 创建新的图片
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return resizedBitmap;
    }

    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            Log.e(TAG, e + "");
        }
        return degree;
    }

    public static void setBitmap(Context context, String url, ImageView imageView) {
        Picasso.with(context).load(url).centerCrop().into(imageView);
    }


    public static void setBitmap(Context context, Uri uri, ImageView imageView) {
        Picasso.with(context).load(uri).centerCrop().into(imageView);
    }


    public static void setBitmap(Context context, Integer resid, ImageView imageView) {
        Picasso.with(context).load(resid).centerCrop().into(imageView);
    }

    public static void setBitmap(Context context, File file, ImageView imageView) {
        Picasso.with(context).load(file).centerCrop().into(imageView);
    }

//    public static void setBitmap(Context context, Byte[] bytes, ImageView imageView) {
//        Picasso.with(context).load(bytes).centerCrop().into(imageView);
//    }


    /**
     * 设置水印图片在左上角
     *
     * @param context
     * @param src
     * @param watermark
     * @param paddingLeft
     * @param paddingTop
     * @return
     */
    public static Bitmap createWaterMaskLeftTop(
            Context context, Bitmap src, Bitmap watermark,
            int paddingLeft, int paddingTop) {
        return createWaterMaskBitmap(src, watermark,
                dp2px(context, paddingLeft), dp2px(context, paddingTop));
    }

    private static Bitmap createWaterMaskBitmap(Bitmap src, Bitmap watermark,
                                                int paddingLeft, int paddingTop) {
        if (src == null) {
            return null;
        }
        int width = src.getWidth();
        int height = src.getHeight();
        //创建一个bitmap
        Bitmap newb = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);// 创建一个新的和SRC长度宽度一样的位图
        //将该图片作为画布
        Canvas canvas = new Canvas(newb);
        //在画布 0，0坐标上开始绘制原始图片
        canvas.drawBitmap(src, 0, 0, null);
        //在画布上绘制水印图片
        canvas.drawBitmap(watermark, paddingLeft, paddingTop, null);
        // 保存
        canvas.save(Canvas.ALL_SAVE_FLAG);
        // 存储
        canvas.restore();
        return newb;
    }

    /**
     * 设置水印图片在右下角
     *
     * @param context
     * @param src
     * @param watermark
     * @param paddingRight
     * @param paddingBottom
     * @return
     */
    public static Bitmap createWaterMaskRightBottom(
            Context context, Bitmap src, Bitmap watermark,
            int paddingRight, int paddingBottom) {
        return createWaterMaskBitmap(src, watermark,
                src.getWidth() - watermark.getWidth() - dp2px(context, paddingRight),
                src.getHeight() - watermark.getHeight() - dp2px(context, paddingBottom));
    }

    /**
     * 设置水印图片到右上角
     *
     * @param context
     * @param src
     * @param watermark
     * @param paddingRight
     * @param paddingTop
     * @return
     */
    public static Bitmap createWaterMaskRightTop(
            Context context, Bitmap src, Bitmap watermark,
            int paddingRight, int paddingTop) {
        return createWaterMaskBitmap(src, watermark,
                src.getWidth() - watermark.getWidth() - dp2px(context, paddingRight),
                dp2px(context, paddingTop));
    }

    /**
     * 设置水印图片到左下角
     *
     * @param context
     * @param src
     * @param watermark
     * @param paddingLeft
     * @param paddingBottom
     * @return
     */
    public static Bitmap createWaterMaskLeftBottom(
            Context context, Bitmap src, Bitmap watermark,
            int paddingLeft, int paddingBottom) {
        return createWaterMaskBitmap(src, watermark, dp2px(context, paddingLeft),
                src.getHeight() - watermark.getHeight() - dp2px(context, paddingBottom));
    }

    /**
     * 设置水印图片到中间
     *
     * @param src
     * @param watermark
     * @return
     */
    public static Bitmap createWaterMaskCenter(Bitmap src, Bitmap watermark) {
        return createWaterMaskBitmap(src, watermark,
                (src.getWidth() - watermark.getWidth()) / 2,
                (src.getHeight() - watermark.getHeight()) / 2);
    }

    /**
     * 给图片添加文字到左上角
     *
     * @param context
     * @param bitmap
     * @param text
     * @return
     */
    public static Bitmap drawTextToLeftTop(Context context, Bitmap bitmap, String text,
                                           int size, int color, int paddingLeft, int paddingTop) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        paint.setTextSize(dp2px(context, size));
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        return drawTextToBitmap(context, bitmap, text, paint, bounds,
                dp2px(context, paddingLeft),
                dp2px(context, paddingTop) + bounds.height());
    }

    /**
     * 绘制文字到右下角
     *
     * @param context
     * @param bitmap
     * @param text
     * @param size
     * @param color
     * @param paddingRight
     * @param paddingBottom
     * @return
     */
    public static Bitmap drawTextToRightBottom(Context context, Bitmap bitmap, String text,
                                               int size, int color, int paddingRight, int paddingBottom) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        paint.setTextSize(dp2px(context, size));
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        return drawTextToBitmap(context, bitmap, text, paint, bounds,
                bitmap.getWidth() - bounds.width() - dp2px(context, paddingRight),
                bitmap.getHeight() - dp2px(context, paddingBottom));
    }

    /**
     * 绘制文字到右上方
     *
     * @param context
     * @param bitmap
     * @param text
     * @param size
     * @param color
     * @param paddingRight
     * @param paddingTop
     * @return
     */
    public static Bitmap drawTextToRightTop(Context context, Bitmap bitmap, String text,
                                            int size, int color, int paddingRight, int paddingTop) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        paint.setTextSize(dp2px(context, size));
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        return drawTextToBitmap(context, bitmap, text, paint, bounds,
                bitmap.getWidth() - bounds.width() - dp2px(context, paddingRight),
                dp2px(context, paddingTop) + bounds.height());
    }

    /**
     * 绘制文字到左下方
     *
     * @param context
     * @param bitmap
     * @param text
     * @param size
     * @param color
     * @param paddingLeft
     * @param paddingBottom
     * @return
     */
    public static Bitmap drawTextToLeftBottom(Context context, Bitmap bitmap, String text,
                                              int size, int color, int paddingLeft, int paddingBottom) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(color);
        paint.setTextSize(dp2px(context, size));
        paint.setFakeBoldText(true);
        paint.setShadowLayer(1.6f, 1.5f, 1.3f, Color.BLACK/*context.getResources().getColor(R.color.mis_pink)*/);

        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        return drawTextToBitmap(context, bitmap, text, paint, bounds,
                dp2px(context, paddingLeft),
                bitmap.getHeight() - dp2px(context, paddingBottom));
    }

    /**
     * 添加位置时间信息的水印在特定位置
     * @param context
     * @param bitmap 原图片
     * @param time 要绘制的时间信息
     * @param locationData 要绘制的地理位置信息
     * @param imagePath 保存图片的路径
     */
    public static void addLocation(Context context, int degree, Bitmap bitmap, String time,  String locationData, String imagePath) {

        bitmap = ImageUtil.zoomAndClipImg(bitmap, degree, ScreenUtils.getScreenWidth(context),
                ScreenUtils.getScreenHeight(context) - dp2px(context, 84));

        Bitmap textBitmap = ImageUtil.drawTextToLeftBottom(context, bitmap, time,
                16, context.getResources().getColor(R.color.mis_pink), 30, 60);
        if (!TextUtils.isEmpty(locationData)) {
            textBitmap = ImageUtil.drawTextToLeftBottom(context, textBitmap, locationData,
                    16, context.getResources().getColor(R.color.mis_pink), 30, 30);
        }
        Bitmap watermarkBitmap = ImageUtil.createWaterMaskCenter(bitmap, textBitmap);

        saveBitmapFile(imagePath, watermarkBitmap);
    }

    /**
     * 保存图片
     * @param imagePath
     * @param bitmap
     */
    public static void saveBitmapFile(String imagePath, Bitmap bitmap) {
        File file = new File(imagePath);//将要保存图片的路径
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 绘制文字到中间
     *
     * @param context
     * @param bitmap
     * @param text
     * @param size
     * @param color
     * @return
     */
    public static Bitmap drawTextToCenter(Context context, Bitmap bitmap, String text,
                                          int size, int color) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        paint.setTextSize(dp2px(context, size));
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        return drawTextToBitmap(context, bitmap, text, paint, bounds,
                (bitmap.getWidth() - bounds.width()) / 2,
                (bitmap.getHeight() + bounds.height()) / 2);
    }

    //图片上绘制文字
    private static Bitmap drawTextToBitmap(Context context, Bitmap bitmap, String text,
                                           Paint paint, Rect bounds, int paddingLeft, int paddingTop) {
        android.graphics.Bitmap.Config bitmapConfig = bitmap.getConfig();

        paint.setDither(true); // 获取跟清晰的图像采样
        paint.setFilterBitmap(true);// 过滤一些
        if (bitmapConfig == null) {
            bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
        }
        bitmap = bitmap.copy(bitmapConfig, true);
        Canvas canvas = new Canvas(bitmap);

        canvas.drawText(text, paddingLeft, paddingTop, paint);
        return bitmap;
    }

    /**
     * 缩放图片
     *
     * @param src
     * @param w
     * @param h
     * @return
     */
    public static Bitmap scaleWithWH(Bitmap src, double w, double h) {
        if (w == 0 || h == 0 || src == null) {
            return src;
        } else {
            // 记录src的宽高
            int width = src.getWidth();
            int height = src.getHeight();
            // 创建一个matrix容器
            Matrix matrix = new Matrix();
            // 计算缩放比例
            float scaleWidth = (float) (w / width);
            float scaleHeight = (float) (h / height);
            // 开始缩放
            matrix.postScale(scaleWidth, scaleHeight);
            // 创建缩放后的图片
            return Bitmap.createBitmap(src, 0, 0, width, height, matrix, true);
        }
    }

    /**
     * dip转pix
     *
     * @param context
     * @param dp
     * @return
     */
    public static int dp2px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
}
