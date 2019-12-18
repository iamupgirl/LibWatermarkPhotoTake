package me.nereo.multi_image_selector.utils;

import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

/**
 *  屏幕工具
 * Created by nereo on 15/11/19.
 * Updated by nereo on 2016/1/19.
 */
public class ScreenUtils {



    public static Point getScreenSize(Context context){
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point out = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            display.getSize(out);
        }else{
            int width = display.getWidth();
            int height = display.getHeight();
            out.set(width, height);
        }
        return out;
    }

    /**
     * 获取分辨率
     *
     * @param context
     */
    public static int getScreenWidth(Context context) {
        int reWidth = 0;
        try {
            DisplayMetrics metrics = new DisplayMetrics();
            WindowManager window = (WindowManager) context.getSystemService("window");
            window.getDefaultDisplay().getMetrics(metrics);
            int width = metrics.widthPixels;
            int height = metrics.heightPixels;
            int temp;
            if (width > height) {
                temp = width;
                width = height;
                height = temp;
            }
            reWidth = width;
        } catch (Exception e2) {
            Log.e("sherry", e2 + "");
        }
        return reWidth;
    }

    /**
     * 获取分辨率
     *
     * @param context
     */
    public static int getScreenHeight(Context context) {
        int reHeight = 0;
        try {
            DisplayMetrics metrics = new DisplayMetrics();
            WindowManager window = (WindowManager) context.getSystemService("window");
            window.getDefaultDisplay().getMetrics(metrics);
            int width = metrics.widthPixels;
            int height = metrics.heightPixels;
            int temp;
            if (width > height) {
                temp = width;
                width = height;
                height = temp;
            }
            reHeight = height;
        } catch (Exception e2) {
            Log.e("sherry", e2 + "");
        }
        return reHeight;
    }

    /**
     * 获取屏幕宽度和高度，单位为px
     * @param context
     * @return
     */
    public static Point getScreenMetrics(Context context){
        DisplayMetrics dm =context.getResources().getDisplayMetrics();
        int w_screen = dm.widthPixels;
        int h_screen = dm.heightPixels;
        return new Point(w_screen, h_screen);

    }

    /**
     * 获取屏幕长宽比
     * @param context
     * @return
     */
    public static float getScreenRate(Context context){
        Point P = getScreenMetrics(context);
        float H = P.y;
        float W = P.x;
        return (H/W);
    }
}
