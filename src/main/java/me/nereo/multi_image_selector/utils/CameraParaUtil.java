package me.nereo.multi_image_selector.utils;

import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.Log;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by xueli on 2016/9/26.
 */
public class CameraParaUtil {

    private static final String TAG = "xueli";
    private CameraSizeComparator sizeComparator = new CameraSizeComparator();
    private static CameraParaUtil myCamPara = null;

    private CameraParaUtil() {

    }

    public static CameraParaUtil getInstance() {
        if (myCamPara == null) {
            myCamPara = new CameraParaUtil();
            return myCamPara;
        } else {
            return myCamPara;
        }
    }

    public Size getPropPreviewSize(List<Size> list, float th, int minWidth, int screenHeight) {
        Collections.sort(list, sizeComparator);
        Log.i(TAG, "PreviewSize : minWidth = " + minWidth);
        int i = 0;
        for (Size s : list) {
            Log.i(TAG, "PreviewSize : width = " + s.width + "height = " + s.height);
            if ((s.height == minWidth) && s.width >= screenHeight) {
                Log.i(TAG, "PreviewSize : w = " + s.width + "h = " + s.height);
                break;
            }
            i++;
        }
        if (i == list.size()) {
            i = list.size() - 1;//如果没找到，就选最大的size
        }
        return list.get(i);
    }

    public Size getPropPictureSize(List<Size> list, int minWidth, int minHeight) {
        Collections.sort(list, sizeComparator);

        int i = 0;
        for (Size s : list) {
            Log.i(TAG, "PreviewSize : width = " + s.width + "height = " + s.height);
            if (s.height == minHeight && s.width == minWidth) {
                Log.i(TAG, "PreviewSize : w = " + s.width + "h = " + s.height);
                break;
            }
            i++;
        }
        if (i == list.size()) {
            i = list.size() - 1;//如果没找到，就选最大的size
        }
        return list.get(i);
    }

    public class CameraSizeComparator implements Comparator<Size> {
        public int compare(Size lhs, Size rhs) {
            // TODO Auto-generated method stub  
            if (lhs.width == rhs.width) {
                return 0;
            } else if (lhs.width > rhs.width) {
                return 1;
            } else {
                return -1;
            }
        }

    }

    /**
     * 打印支持的聚焦模式
     *
     * @param params
     */
    public void printSupportFocusMode(Camera.Parameters params) {
        List<String> focusModes = params.getSupportedFocusModes();
        for (String mode : focusModes) {
            Log.i(TAG, "focusModes--" + mode);
        }
    }

}  