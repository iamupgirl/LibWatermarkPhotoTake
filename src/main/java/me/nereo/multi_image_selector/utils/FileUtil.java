package me.nereo.multi_image_selector.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * 文件帮助类
 */
public class FileUtil {
    public static final int BUFSIZE = 256;
    public static final int COUNT = 320;
    private static final String TAG = "FileUtils";
    private static final long SIZE_KB = 1024;
    private static final long SIZE_MB = 1048576;
    private static final long SIZE_GB = 1073741824;
    private static final int ONE_M = 1024;
    private static final int NOT_FLIE_CODE = -2;
    public static File updateDir = null;
    public static File updateFile = null;

    /**
     * 在SD卡上面创建文件
     *
     * @param filePath 文件路径
     * @return 文件
     * @throws IOException 异常
     */
    public static File createSDFile(String filePath) throws IOException {
        File file = new File(filePath);
        file.createNewFile();
        return file;
    }



    /**
     * 在SD卡上面创建目录
     *
     * @param dirName 目录名称
     * @return 文件
     */
    public static File createSDDir(String dirName) {
        File dir = new File(dirName);
        dir.mkdir();
        return dir;
    }

    /**
     * 判断指定的文件是否存在
     *
     * @param filePath 文件路径
     * @return 是否存在
     */
    public static boolean isFileExist(String filePath) {
        File file = new File(filePath);
        return file.exists();
    }

    /**
     * 准备文件夹，文件夹若不存在，则创建
     *
     * @param filePath 文件路径
     */
    public static void prepareFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    /**
     * 删除指定的文件或目录
     *
     * @param file 文件
     */
    public static void delete(File file) {
        if (file == null || !file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            deleteDirRecursive(file);
        } else {
            file.delete();
        }
    }

    /**
     * 递归删除目录
     *
     * @param dir 文件路径
     */
    public static void deleteDirRecursive(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory()) {
            return;
        }
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        for (File f : files) {
            if (f.isFile()) {
                f.delete();
            } else {
                deleteDirRecursive(f);
            }
        }
        dir.delete();
    }

    /**
     * 取得文件大小
     *
     * @param f 文件
     * @return long 大小
     */
    public long getFileSizes(File f) {
        long s = 0;
        try {
            if (f.exists()) {
                FileInputStream fis = null;
                fis = new FileInputStream(f);
                s = fis.available();
            } else {
                f.createNewFile();
            }
        } catch (Exception e) {
            Log.w(TAG, e);
        }
        return s;
    }

    /**
     * 递归取得文件夹大小
     *
     * @param filedir 文件
     * @return 大小
     */
    public static long getFileSize(File filedir) {
        long size = 0;
        if (null == filedir) {
            return size;
        }
        File[] files = filedir.listFiles();
        if (null == files) {
            return 0;
        }
        try {
            for (File f : files) {
                if (f.isDirectory()) {
                    size += getFileSize(f);
                } else {
                    FileInputStream fis = new FileInputStream(f);
                    size += fis.available();
                    fis.close();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e + "");
        }
        return size;

    }

    /**
     * 转换文件大小
     *
     * @param fileS 大小
     * @return 转换后的文件大小
     */
    public static String formatFileSize(long fileS) {
        DecimalFormat df = new DecimalFormat("#.0");
        String fileSizeString = "";
        if (fileS == 0) {
            fileSizeString = "0" + "KB";
        } else if (fileS < SIZE_KB) {
            fileSizeString = df.format((double) fileS) + "KB";
        } else if (fileS < SIZE_MB) {
            fileSizeString = df.format((double) fileS / SIZE_KB) + "KB";
        } else if (fileS < SIZE_GB) {
            fileSizeString = df.format((double) fileS / SIZE_MB) + "M";
        } else {
            fileSizeString = df.format((double) fileS / SIZE_GB) + "G";
        }
        return fileSizeString;
    }

    /**
     * 将文件写入SD卡
     *
     * @param path     路径
     * @param fileName 文件名称
     * @param input    输入流
     * @return 文件
     */
    public static File writeToSDCard(Context context, String path, String fileName, InputStream input) {
        File file = null;
        OutputStream output = null;
        try {
            createSDDir(path);
            file = createSDFile(path + fileName);
            output = new FileOutputStream(file);

            byte[] buffer = new byte[BUFSIZE];
            int readedLength = -1;
            while ((readedLength = input.read(buffer)) != -1) {
                output.write(buffer, 0, readedLength);
            }
            output.flush();

        } catch (Exception e) {
            Log.e(TAG, e + "");
        } finally {
            try {
                output.close();
            } catch (IOException e) {
                Log.e(TAG, e + "");
            }
        }

        return file;
    }

    /**
     * 判断SD卡是否已经准备好
     *
     * @return 是否有SDCARD
     */
    public static boolean isSDCardReady() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * 文件流拷贝到文件
     *
     * @param in      输入流
     * @param outFile 输出文件
     * @return 操作状态
     */
    public static int copyStreamToFile(Context context, InputStream in, String outFile) {
        if (isFileExist(outFile)) {
            // 文件已经存在；
            return NOT_FLIE_CODE;
        }
        try {
            OutputStream fosto = new FileOutputStream(outFile);
            byte[] bt = new byte[ONE_M];
            int c;
            while ((c = in.read(bt)) > 0) {
                fosto.write(bt, 0, c);
            }
            in.close();
            fosto.close();
            return 0;

        } catch (Exception e) {
            Log.e(TAG, e + "");
            return -1;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                Log.e(TAG, e + "");
            }
        }
    }

    /**
     * 得到sdcard路径
     *
     * @return String
     */
    public static String getExtPath() {
        String path = "";
        if (isSDCardReady()) {
            path = Environment.getExternalStorageDirectory().getPath();
        }
        return path;
    }

    /**
     * 得到应用程序路径目录
     *
     * @param mActivity mActivity
     * @return String
     */
    public static String getPackagePath(Activity mActivity) {
        return mActivity.getFilesDir().toString();
    }

    // 写数据
    public static void writeFile(OutputStream os, String writestr) throws IOException {
        try {

            FileOutputStream fout = (FileOutputStream) os;

            byte[] bytes = writestr.getBytes();

            fout.write(bytes);

            fout.close();
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
    }

    // 读数据
    public static String readFile(InputStream in) throws IOException {
        String res = "";
        try {
            FileInputStream fin = (FileInputStream) in;
            int length = fin.available();
            byte[] buffer = new byte[length];
            fin.read(buffer);
//            res = EncodingUtils.getString(buffer, "UTF-8");
            fin.close();
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
        return res;
    }

    /**
     * 保存Bitmap到sdcard
     *
     * @param b
     */
    public static void saveBitmap(Bitmap b, File file) {
        String path = String.valueOf(file);
        Log.d("xueli---------" + TAG, path);
        try {
            FileOutputStream fout = new FileOutputStream(path);
            BufferedOutputStream bos = new BufferedOutputStream(fout);
            b.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
            Log.i(TAG, "saveBitmap成功");
        } catch (Exception e) {
            Log.i(TAG, "saveBitmap:失败");
            e.printStackTrace();
        }
    }

    public static void saveImageToGallery(Context context, Bitmap bmp) {
        // 首先保存图片
        File appDir = new File(Environment.getExternalStorageDirectory(), "/DCIM/");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 其次把文件插入到系统图库
        try {
            MediaStore.Images.Media.insertImage(context.getContentResolver(),
                    file.getAbsolutePath(), fileName, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // 最后通知图库更新
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.fromFile(new File(file.getPath()))));
    }

    public static String getSDCachePath() {
        String dir ="";
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            dir = Environment.getExternalStorageDirectory().toString();
        } else {
            dir = Environment.getDownloadCacheDirectory().toString();
        }
        return dir;

    }
    /**
     * 创建文件目录
     */
    public static String createTempDir() {
        String dir;

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            dir = Environment.getExternalStorageDirectory().toString();
        } else {
            dir = Environment.getDownloadCacheDirectory().toString();
        }
        return dir;
    }
    /**
     * 从assets目录下拷贝整个文件夹，不管是文件夹还是文件都能拷贝
     *
     * @param context 上下文
     * @param inPath  文件目录，要拷贝的目录
     * @param outPath 目标文件夹位置如：/sdcrad/mydir
     */
    public static void copyAssetsFilesToSD(Context context, String inPath, String outPath) {
        Log.d(TAG, "copyFiles() inPath:" + inPath + ", outPath:" + outPath);
        FileOutputStream fos = null;
        InputStream is = null;
        String[] fileNames = null;
        try {// 获得Assets一共有几多文件
            fileNames = context.getAssets().list(inPath);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        if (fileNames.length > 0) {//如果是目录
            File fileOutDir = new File(outPath);
            if (!fileOutDir.exists()) { // 如果文件路径不存在
                if (!fileOutDir.mkdirs()) { // 创建文件夹
                    Log.e(TAG, "mkdirs() FAIL:" + fileOutDir.getAbsolutePath());
                }
            }
            for (String fileName : fileNames) { //递归调用复制文件夹
                String inDir = inPath;
                String outDir = outPath + File.separator;
                if (!inPath.equals("")) { //空目录特殊处理下
                    inDir = inDir + File.separator;
                }
                copyAssetsFilesToSD(context, inDir + fileName, outDir + fileName);
            }

        } else {//如果是文件
            try {
                File fileOut = new File(outPath);
                if (fileOut.exists()) {

                }
                fos = new FileOutputStream(fileOut);
                is = context.getAssets().open(inPath);
                byte[] buffer = new byte[1024];
                int byteCount = 0;
                while ((byteCount = is.read(buffer)) > 0) {
                    fos.write(buffer, 0, byteCount);
                }
                fos.flush();//刷新缓冲区

            } catch (Exception e) {
                e.printStackTrace();

            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * 下载文件
     * url 网络下载api
     * out 下载到该文件
     *
     * @return
     */
    public static long downLoadZip(String url, String out) {
        URLConnection connection = null;
        int bytesCopied = 0;
        try {
            URL mUrl = new URL(url);
            String fileName = new File(mUrl.getFile()).getName();
            File mFile = new File(out, fileName);
            connection = mUrl.openConnection();
            int length = connection.getContentLength();
            if (mFile.exists() && length == mFile.length()) {
//                L.d(TAG, "file "+mFile.getName()+" already exits!!");
                return 0l;
            }
            ProgressReportingOutputStream mOutputStream = new ProgressReportingOutputStream(mFile);
            bytesCopied = copy(connection.getInputStream(), mOutputStream);
            if (bytesCopied != length && length != -1) {
                //L.e(TAG, "Download incomplete bytesCopied="+bytesCopied+", length"+length);
            }
            mOutputStream.close();
        } catch (Exception e) {

            e.printStackTrace();
        }
        return bytesCopied;
    }

    /**
     * 解压压缩文件
     *
     * @param in
     * @param out
     * @return
     */
    public static long unZip(String in, String out) {
        long extractedSize = 0L;
        Enumeration<ZipEntry> entries;
        ZipFile zip = null;
        try {
            File mInput = new File(in);
            File mOutput = new File(out);
            if (!mOutput.exists()) {
                if (!mOutput.mkdirs()) {
                    Log.e(TAG, "Failed to make directories:" + mOutput.getAbsolutePath());
                }
            }
            zip = new ZipFile(mInput);
            entries = (Enumeration<ZipEntry>) zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory()) {
                    continue;
                }
                File destination = new File(mOutput, entry.getName());
                if (!destination.getParentFile().exists()) {
                    // Log.e(TAG, "make="+destination.getParentFile().getAbsolutePath());
                    destination.getParentFile().mkdirs();
                }
                ProgressReportingOutputStream outStream = new ProgressReportingOutputStream(destination);
                extractedSize += copy(zip.getInputStream(entry), outStream);
                outStream.close();
            }
        } catch (ZipException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                zip.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return extractedSize;
    }

    /**
     * 复制文件
     *
     * @param input
     * @param output
     * @return
     */
    public static int copy(InputStream input, OutputStream output) {
        byte[] buffer = new byte[1024 * 8];
        BufferedInputStream in = new BufferedInputStream(input, 1024 * 8);
        BufferedOutputStream out = new BufferedOutputStream(output, 1024 * 8);
        int count = 0, n = 0;
        try {
            while ((n = in.read(buffer, 0, 1024 * 8)) != -1) {
                out.write(buffer, 0, n);
                count += n;
            }
            out.flush();
        } catch (IOException e) {

            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e) {

                e.printStackTrace();
            }
            try {
                in.close();
            } catch (IOException e) {

                e.printStackTrace();
            }
        }
        return count;
    }

    /**
     *
     */
    public static class ProgressReportingOutputStream extends FileOutputStream {
        public ProgressReportingOutputStream(File file)
                throws FileNotFoundException {
            super(file);
        }

        @Override
        public void write(byte[] buffer, int byteOffset, int byteCount)
                throws IOException {
            super.write(buffer, byteOffset, byteCount);
        }

    }

    //读SD中的文件
    public static String readFileSdcardFile(String fileName) throws IOException {
        String res = "";
        try {
            FileInputStream fin = new FileInputStream(fileName);

            int length = fin.available();

            byte[] buffer = new byte[length];
            fin.read(buffer);

//            res = EncodingUtils.getString(buffer, "UTF-8");

            fin.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }
    /**
     * 复制整个文件夹内容
     * @param oldPath String 原文件路径 如：c:/fqf
     * @param newPath String 复制后路径 如：f:/fqf/ff
     */
    public static void copyFolder(String oldPath, String newPath) {

        try {
            (new File(newPath)).mkdirs(); //如果文件夹不存在 则建立新文件夹
            File a=new File(oldPath);
            String[] file=a.list();
            File temp=null;
            for (int i = 0; i < file.length; i++) {
                if(oldPath.endsWith(File.separator)){
                    temp=new File(oldPath+file[i]);
                }
                else{
                    temp=new File(oldPath+ File.separator+file[i]);
                }

                if(temp.isFile()){
                    FileInputStream input = new FileInputStream(temp);
                    FileOutputStream output = new FileOutputStream(newPath + "/" +
                            (temp.getName()).toString());
                    byte[] b = new byte[1024 * 5];
                    int len;
                    while ( (len = input.read(b)) != -1) {
                        output.write(b, 0, len);
                    }
                    output.flush();
                    output.close();
                    input.close();
                }
                if(temp.isDirectory()){//如果是子文件夹
                    copyFolder(oldPath+"/"+file[i],newPath+"/"+file[i]);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();

        }

    }
}
