package com.vivo.emanon.cisum;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/10/9.
 */

public class Download {
    public static void downloadMusic(String url, String title, String artist) {
        try {
            String fileName = artist + " - " +title + url.substring(url.lastIndexOf("."));
            Uri uri = Uri.parse(url);
            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setTitle(artist + " - " +title);
            request.setDescription("正在下载...");
            request.setDestinationInExternalPublicDir(Constants.DOWNLOAD_DESTINATION, fileName);
            request.setMimeType(MimeTypeMap.getFileExtensionFromUrl(url));
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE |
                    DownloadManager.Request.NETWORK_WIFI);
            DownloadManager downloadManager = (DownloadManager) CisumApp.sContext
                    .getSystemService(Context.DOWNLOAD_SERVICE);
            downloadManager.enqueue(request);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            Toast.makeText(CisumApp.sContext, "下载失败", Toast.LENGTH_SHORT).show();
        }
    }

    public static void downloadLyric(String url, String title, String artist) {
        String fileName = artist + " - " +title + ".lrc";
        final File lyric = new File(Constants.MUSIC_PATH + "/" + fileName);
        if (lyric.exists()) {
            return;
        } else {
            try {
                lyric.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        HttpUtil.sendOkHttpRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                InputStream inputStream = null;
                byte[] buffer = new byte[1024];
                int len = 0;
                FileOutputStream fileOutputStream = null;
                try {
                    inputStream = response.body().byteStream();
                    fileOutputStream = new FileOutputStream(lyric);
                    while ((len = inputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, len);
                    }
                    fileOutputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                       if (inputStream != null) {
                           inputStream.close();
                       }
                       if (fileOutputStream != null) {
                           fileOutputStream.close();
                       }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public static String downloadCover(String url, String title, String artist) {
        String fileName = artist + " - " +title + ".png";
        String coverPath = Constants.MUSIC_PATH + "/" + fileName;
        final File cover = new File(coverPath);
        if (cover.exists()) {
            return coverPath;
        } else {
            try {
                cover.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        HttpUtil.sendOkHttpRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                InputStream inputStream = null;
                byte[] buffer = new byte[1024];
                int len = 0;
                FileOutputStream fileOutputStream = null;
                try {
                    inputStream = response.body().byteStream();
                    fileOutputStream = new FileOutputStream(cover);
                    while ((len = inputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, len);
                    }
                    fileOutputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        if (fileOutputStream != null) {
                            fileOutputStream.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        return coverPath;
    }
}
