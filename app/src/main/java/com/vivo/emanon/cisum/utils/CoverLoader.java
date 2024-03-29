package com.vivo.emanon.cisum.utils;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.collection.LruCache;

import com.vivo.emanon.cisum.app.CisumApp;
import com.vivo.emanon.cisum.constant.Constants;
import com.vivo.emanon.cisum.model.Music;
import com.vivo.emanon.cisum.R;

import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * 封面加载类
 * Created by emanon on 2017/10/9.
 */

public class CoverLoader {
    private static final String KEY_NULL = "null";

    //缓存
    private final LruCache<String, Bitmap> mCoverCache;

    private enum Type {
        THUMBNAIL(""),
        BLUR("#BLUR"),
        ROUND("#ROUND");

        private final String value;

        Type(String value) {
            this.value = value;
        }
    }

    public static CoverLoader getInstance() {
        return SingletonHolder.instance;
    }

    private static class SingletonHolder {
        private static final CoverLoader instance = new CoverLoader();
    }

    private CoverLoader() {
        // 获取当前进程的可用内存（单位KB）
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        // 缓存大小为当前进程可用内存的1/8
        int cacheSize = maxMemory / 8;
        mCoverCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(@NonNull String key, @NonNull Bitmap bitmap) {
                return bitmap.getAllocationByteCount() / 1024;
            }
        };
    }

    public Bitmap loadThumbnail(Music music) {
        return loadCover(music, Type.THUMBNAIL);
    }

    public Bitmap loadBlur(Music music) {
        return loadCover(music, Type.BLUR);
    }

    public Bitmap loadRound(Music music) {
        return loadCover(music, Type.ROUND);
    }

    private Bitmap loadCover(Music music, Type type) {
        Bitmap bitmap;
        String key = getKey(music, type);
        if (TextUtils.isEmpty(key)) {
            bitmap = mCoverCache.get(KEY_NULL.concat(type.value));
            if (bitmap != null) {
                return bitmap;
            }

            bitmap = getDefaultCover(type);
            mCoverCache.put(KEY_NULL.concat(type.value), bitmap);
            return bitmap;
        }

        bitmap = mCoverCache.get(key);
        if (bitmap != null) {
            return bitmap;
        }

        bitmap = loadCoverByType(music, type);
        if (bitmap != null) {
            mCoverCache.put(key, bitmap);
            return bitmap;
        }

        return loadCover(null, type);
    }

    private String getKey(Music music, Type type) {
        if (music == null) {
            return null;
        }

        if (music.getType().equals(Constants.LOCAL) && music.getAlbumId() > 0) {
            return String.valueOf(music.getAlbumId()).concat(type.value);
        } else if (music.getType().equals(Constants.ONLINE) && !TextUtils.isEmpty(music.
                getCoverPath())) {
            return music.getCoverPath().concat(type.value);
        } else {
            return null;
        }
    }

    private Bitmap getDefaultCover(Type type) {
        switch (type) {
            case BLUR:
                return BitmapFactory.decodeResource(CisumApp.sContext.getResources(),
                        R.drawable.play_page_default_bg);
            case ROUND:
                Bitmap bitmap = BitmapFactory.decodeResource(CisumApp.sContext.getResources(),
                        R.drawable.play_page_default_cover);
                bitmap = ImageUtil.resizeImage(bitmap, ScreenUtil.getScreenWidth() / 2,
                        ScreenUtil.getScreenWidth() / 2);
                return bitmap;
            default:
                return BitmapFactory.decodeResource(CisumApp.sContext.getResources(), R.drawable.play_page_default_cover);
        }
    }

    private Bitmap loadCoverByType(Music music, Type type) {
        Bitmap bitmap;
        if (music.getType().equals(Constants.LOCAL)) {
            bitmap = loadCoverFromMediaStore(music.getAlbumId());
        } else {
            bitmap = loadCoverFromFile(music.getCoverPath());
        }

        switch (type) {
            case BLUR:
                return ImageUtil.blur(bitmap);
            case ROUND:
                bitmap = ImageUtil.resizeImage(bitmap, ScreenUtil.getScreenWidth() / 2,
                        ScreenUtil.getScreenWidth() / 2);
                return ImageUtil.createCircleImage(bitmap);
            default:
                return bitmap;
        }
    }

    /**
     * 从媒体库加载封面
     * 本地音乐
     */
    private Bitmap loadCoverFromMediaStore(long albumId) {
        ContentResolver resolver = CisumApp.sContext.getContentResolver();
        Uri uri = LocalMusicUtil.getMediaStoreAlbumCoverUri(albumId);
        InputStream is;
        try {
            is = resolver.openInputStream(uri);
        } catch (FileNotFoundException ignored) {
            return null;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        return BitmapFactory.decodeStream(is, null, options);
    }

    /**
     * 从下载的图片加载封面<br>
     * 网络音乐
     */
    private Bitmap loadCoverFromFile(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        return BitmapFactory.decodeFile(path, options);
    }
}
