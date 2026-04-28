package com.game.dream.utils;

import android.content.Context;
import android.os.Environment;
import java.io.File;

public class StorageHelper {

    private Context context;

    public StorageHelper(Context context) {
        this.context = context;
    }

    /**
     * 获取应用专属外部存储目录
     */
    public File getAppExternalDir() {
        return context.getExternalFilesDir(null);
    }

    /**
     * 获取应用专属图片目录
     */
    public File getAppPicturesDir() {
        return context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
    }

    /**
     * 获取应用专属下载目录
     */
    public File getAppDownloadsDir() {
        return context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
    }

    /**
     * 获取公共下载目录（需要权限）
     */
    public File getPublicDownloadsDir() {
        return Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS);
    }

    /**
     * 检查外部存储是否可用
     */
    public boolean isExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /**
     * 创建文件
     */
    public File createFile(String fileName, String subDir) {
        File dir;
        if (subDir != null && !subDir.isEmpty()) {
            dir = new File(getAppExternalDir(), subDir);
        } else {
            dir = getAppExternalDir();
        }

        if (!dir.exists()) {
            dir.mkdirs();
        }

        return new File(dir, fileName);
    }
}
