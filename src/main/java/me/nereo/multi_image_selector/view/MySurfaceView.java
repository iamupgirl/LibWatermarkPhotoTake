package me.nereo.multi_image_selector.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import me.nereo.multi_image_selector.R;
import me.nereo.multi_image_selector.utils.ScreenUtils;

/**
 * Created by sherry on 2016/9/26.
 */

public class MySurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "MySurfaceView";
    private final Context mContext;
    private Paint mPaint;
    private SurfaceHolder mSurfaceHolder;
    private float focusX, focusY;
    private boolean mClearCanvas = true;

    public MySurfaceView(Context context) {
        super(context);
        setWillNotDraw(false);
        mContext = context;
    }

    public MySurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
        mContext = context;
        mSurfaceHolder = getHolder();
        mSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);//translucent半透明 transparent透明
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceHolder.addCallback(this);
        focusX = ScreenUtils.getScreenWidth(context) / 2;
        focusY = ScreenUtils.getScreenHeight(context) / 2;
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(getResources().getColor(R.color.mis_white));
        mPaint.setStrokeWidth(2);
        mPaint.setAntiAlias(true);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i(TAG, "surfaceCreated...");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        Log.i(TAG, "surfaceChanged...");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i(TAG, "surfaceDestroyed...");
    }

    public SurfaceHolder getSurfaceHolder() {
        return mSurfaceHolder;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mClearCanvas) {
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        } else {
            Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.icon_focus);
            canvas.drawBitmap(bitmap, focusX - bitmap.getWidth() / 2, focusY - bitmap.getHeight() / 2, mPaint);
        }
    }

    public void setFocusXY(float x, float y) {
        this.mClearCanvas = false;
        this.focusX = x;
        this.focusY = y;
    }

    public void clear() {
        this.mClearCanvas = true;
    }
}
