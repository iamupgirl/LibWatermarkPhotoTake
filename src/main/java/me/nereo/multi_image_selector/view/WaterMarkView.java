package me.nereo.multi_image_selector.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import me.nereo.multi_image_selector.R;
import me.nereo.multi_image_selector.utils.ImageUtil;
import me.nereo.multi_image_selector.utils.ScreenUtils;

/**
 * 跟随屏幕旋转的水印View
 * Created by shanxs on 2018/4/2.
 */
public class WaterMarkView extends View {

    private Context mContext;
    private String mTime;
    private String mLocation;
    private Paint mPaint;
    private int mDegree;
    private float mPaintTextSize = 16;

    public WaterMarkView(Context context) {
        super(context);
        setWillNotDraw(false);
    }

    public WaterMarkView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
        this.mContext = context;
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(mContext.getResources().getColor(R.color.mis_pink));
        mPaint.setTextSize(ImageUtil.dp2px(mContext, mPaintTextSize));
        mPaint.setFakeBoldText(true);
        mPaint.setShadowLayer(1.6f, 1.5f, 1.3f, Color.BLACK);
    }

    public void setTvTime(String time) {
        this.mTime = time;
    }

    public void setTvLocation(String location) {
        this.mLocation = location;
    }

    public void setDegree(int degree) {
        this.mDegree = degree;
    }

    public void setTextSize(int size) {
        this.mPaintTextSize = size;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = ScreenUtils.getScreenWidth(mContext);
        int height = ScreenUtils.getScreenHeight(mContext);
        Log.i("sherry", "width=" + width + "height=" + height);
        switch (mDegree) {
            case 0:
                canvas.rotate(0);
                canvas.drawText(mTime, 60, height - mPaintTextSize / 16 * 120, mPaint);
                canvas.drawText(mLocation, 60, height - mPaintTextSize / 16 * 60, mPaint);
                break;
            case -90:
                canvas.rotate(-90);
                canvas.translate(-height, 0);
                canvas.drawText(mTime, 120, width - mPaintTextSize / 16 * 120, mPaint);
                canvas.drawText(mLocation, 120, width - mPaintTextSize / 16 * 60, mPaint);
                break;
            case 90:
                canvas.rotate(90);
                canvas.translate(0, -width);
                canvas.drawText(mTime, 120, width - mPaintTextSize / 16 * 120, mPaint);
                canvas.drawText(mLocation, 120, width - mPaintTextSize / 16 * 60, mPaint);
                break;
            case 180:
                canvas.rotate(180, width / 2, height / 2);
                canvas.drawText(mTime, 60, height - mPaintTextSize / 16 * 120, mPaint);
                canvas.drawText(mLocation, 60, height - mPaintTextSize / 16 * 60, mPaint);
                break;
        }
    }
}
