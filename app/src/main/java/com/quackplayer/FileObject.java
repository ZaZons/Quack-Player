package com.quackplayer;

import android.net.Uri;

import com.google.android.exoplayer2.MediaItem;

public class FileObject {
    private final int id;
    private boolean isPlaying;
    private final String title, artist, duration;
    private final MediaItem mediaItem;

    public FileObject(int id, boolean isPlaying, String title, String artist, String duration, Uri fileUri) {
        this.id = id;
        this.isPlaying = isPlaying;
        this.title = title;
        this.artist = artist;
        this.duration = duration;
        this.mediaItem = MediaItem.fromUri(fileUri);
    }

    public int getId() {return id;}

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

    public MediaItem getMediaItem() {return mediaItem;}

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }
}




