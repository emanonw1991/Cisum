package com.vivo.emanon.cisum;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/10/9.
 */

public class OnlineMusicUtil {

    private static final String TAG = "OnlineMusicUtil";

    public static void scanOnlineMusic() {
        String onlineMusicListRequest = Constants.SERVER_HOST + "/web/res/audiolists.json";
        HttpUtil.sendOkHttpRequest(onlineMusicListRequest, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String jsonData = response.body().string();
                parseJSONWithJSONObject(AppCache.getOnlineMusicList(), jsonData);
                Log.d("size", "" + AppCache.getOnlineMusicList().size());
            }
        });
    }

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
