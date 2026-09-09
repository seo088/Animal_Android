package com.animalloo.util;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Photo Picker로 선택한 이미지를 앱 캐시 디렉터리에 복사합니다.
 */
public final class ImageFileHelper {

    private static final String LOST_REPORT_DIR = "lost_reports";

    private ImageFileHelper() {
    }

    public static File copyUriToCache(Context context, Uri sourceUri) throws IOException {
        File cacheDir = new File(context.getCacheDir(), LOST_REPORT_DIR);
        if (!cacheDir.exists() && !cacheDir.mkdirs()) {
            throw new IOException("캐시 디렉터리를 생성할 수 없습니다.");
        }

        String fileName = "lost_" + System.currentTimeMillis() + ".jpg";
        File destination = new File(cacheDir, fileName);

        InputStream inputStream = context.getContentResolver().openInputStream(sourceUri);
        if (inputStream == null) {
            throw new IOException("이미지를 읽을 수 없습니다.");
        }

        try (InputStream in = inputStream; OutputStream out = new FileOutputStream(destination)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        }

        return destination;
    }

    public static void deleteCachedFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return;
        }
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
    }

    public static int clearLostReportCache(Context context) {
        File cacheDir = new File(context.getCacheDir(), LOST_REPORT_DIR);
        if (!cacheDir.exists()) {
            return 0;
        }

        File[] files = cacheDir.listFiles();
        if (files == null) {
            return 0;
        }

        int deletedCount = 0;
        for (File file : files) {
            if (file.isFile() && file.delete()) {
                deletedCount++;
            }
        }
        return deletedCount;
    }

    public static long getLostReportCacheSize(Context context) {
        File cacheDir = new File(context.getCacheDir(), LOST_REPORT_DIR);
        if (!cacheDir.exists()) {
            return 0L;
        }

        File[] files = cacheDir.listFiles();
        if (files == null) {
            return 0L;
        }

        long total = 0L;
        for (File file : files) {
            if (file.isFile()) {
                total += file.length();
            }
        }
        return total;
    }
}
