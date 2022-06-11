package com.quackplayer;

import android.net.Uri;

import com.google.android.exoplayer2.MediaItem;

import java.io.Serializable;

public class FileObject implements Serializable {

    int id;
    boolean isPlaying;
    String title, artist, duration;
    String uri;
    MediaItem mediaItem;

    public FileObject(int id, String title, String artist, String duration, Uri uri) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.duration = duration;
        this.uri = uri.toString();
        isPlaying = false;
    }

    public int getId() {
        return id;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getDuration() {
        return duration;
    }

    public MediaItem getMediaItem() {
        return mediaItem;
    }

    public void createMediaItem() {
        mediaItem = MediaItem.fromUri(uri);
    }

    public void removeMediaItem() {
        mediaItem = null;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }
}