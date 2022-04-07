package com.quackplayer;

import android.net.Uri;

public class FileObject {
    private boolean isPlaying;
    private final String title, artist, duration;
    private final Uri fileUri;

    public FileObject(boolean isPlaying, String title, String artist, String duration, Uri fileUri) {
        this.isPlaying = isPlaying;
        this.title = title;
        this.artist = artist;
        this.duration = duration;
        this.fileUri = fileUri;
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

    public Uri getFileUri() {
        return fileUri;
    }
}




