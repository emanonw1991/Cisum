package com.vivo.emanon.cisum.utils;

import android.util.Log;

import androidx.annotation.NonNull;

import com.vivo.emanon.cisum.app.AppCache;
import com.vivo.emanon.cisum.constant.Constants;
import com.vivo.emanon.cisum.model.Music;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 在线音乐工具类
 * Created by emanon on 2017/10/9.
 */

public class OnlineMusicUtil {

    /**
     * 扫描在线音乐
     */
    public static void scanOnlineMusic() {
        String onlineMusicListRequest = Constants.SERVER_HOST + "/web/res/audiolists.json";
        HttpUtil.sendOkHttpRequest(onlineMusicListRequest, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException, NullPointerException {
                String jsonData = Objects.requireNonNull(response.body()).string();
                parseJSONWithJSONObject(AppCache.getOnlineMusicList(), jsonData);
                Log.d("size", "" + AppCache.getOnlineMusicList().size());
            }
        });
    }

    /**
     * 处理在线音乐Json文件的函数
     *
     * @param musicList 在线音乐列表
     * @param jsonData  在线音乐Json文件
     */
    private static void parseJSONWithJSONObject(List<Music> musicList, String jsonData) {
        try {
            JSONArray jsonArray = new JSONArray(jsonData);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Music music = new Music();
                music.setId(jsonObject.getInt("id"));
                String title = jsonObject.getString("title");
                music.setTitle(title);
                String artist = jsonObject.getString("artist");
                music.setArtist(artist);
                music.setMusicPath(jsonObject.getString("musicPath"));
                music.setLyricPath(jsonObject.getString("lyricPath"));
                music.setAlbum(jsonObject.getString("album"));
                music.setDuration(jsonObject.getInt("duration"));
                music.setType(jsonObject.getString("type"));
                music.setAlbumId(jsonObject.getInt("albumId"));
                String coverPath = jsonObject.getString("coverPath");
                String coverPathLocal = Download.downloadCover(coverPath, title, artist);
                music.setCoverPath(coverPathLocal);
                musicList.add(music);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
