package com.vivo.emanon.cisum.Utils;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.MediaStore;

import com.vivo.emanon.cisum.Constant.Constants;
import com.vivo.emanon.cisum.Model.Music;

import java.util.List;

/**
 * 本地音乐工具类
 * Created by emanon on 2017/10/9.
 */

public class LocalMusicUtil {

    private static final String SELECTION = MediaStore.Audio.AudioColumns.SIZE + " >= ? AND " + MediaStore.Audio.AudioColumns.DURATION + " >= ?";

    /**
     * 扫描歌曲
     */
    public static void scanLocalMusic(Context context, List<Music> musicList) {
        if (musicList.size() != 0) {
            musicList.clear();
        }

        long filterSize = 1000000;
        long filterTime = 60000;

        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null,
                SELECTION,
                new String[]{
                        String.valueOf(filterSize),
                        String.valueOf(filterTime)
                },
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        if (cursor == null) {
            return;
        }

        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
            String title = cursor.getString((cursor.getColumnIndex(MediaStore.Audio.AudioColumns.TITLE)));
            String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ARTIST));
            String album = cursor.getString((cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM)));
            int duration = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
            String musicPath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA));
            int albumId = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM_ID));

            Music music = new Music();
            music.setId(id);
            music.setTitle(title);
            music.setArtist(artist);
            music.setAlbum(album);
            music.setMusicPath(musicPath);
            music.setDuration(duration);
            String lyricPath = musicPath.substring(0, musicPath.lastIndexOf(".")) + ".lrc";
            music.setLyricPath(lyricPath);
            music.setType(Constants.LOCAL);
            music.setAlbumId(albumId);
            musicList.add(music);
        }
        cursor.close();
    }

    /**
     * 获取本地音乐的封面
     * @param albumId 音乐的AlbumID
     * @return 音乐封面的URI
     */
    static Uri getMediaStoreAlbumCoverUri(long albumId) {
        Uri artworkUri = Uri.parse("content://media/external/audio/albumart");
        return ContentUris.withAppendedId(artworkUri, albumId);
    }
}
