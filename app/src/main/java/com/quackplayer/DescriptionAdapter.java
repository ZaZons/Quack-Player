package com.quackplayer;

import android.app.PendingIntent;
import android.graphics.Bitmap;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;

public class DescriptionAdapter implements PlayerNotificationManager.MediaDescriptionAdapter {

    FileObject getFileObject(MediaItem currentMediaItem) {
        FileObject currentPlayingObject = null;
        for(FileObject m : MainActivity.getList()) {
            if(m.getMediaItem() == currentMediaItem)
                currentPlayingObject = m;
        }
        return currentPlayingObject;
    }

    @Override
    public CharSequence getCurrentContentTitle(Player player) {
        MediaItem currentMediaItem = player.getCurrentMediaItem();
        return getFileObject(currentMediaItem).getTitle();
    }

    @Nullable
    @Override
    public PendingIntent createCurrentContentIntent(Player player) {
        return null;
    }

    @Nullable
    @Override
    public CharSequence getCurrentContentText(Player player) {
        MediaItem currentMediaItem = player.getCurrentMediaItem();
        return getFileObject(currentMediaItem).getArtist();
    }

    @Nullable
    @Override
    public Bitmap getCurrentLargeIcon(Player player, PlayerNotificationManager.BitmapCallback callback) {
//        int window = player.getCurrentMediaItemIndex();
//        Bitmap largeIcon = getLargeIcon(window);
//        if (largeIcon == null && getLargeIconUri(window) != null) {
//            // load bitmap async
//            loadBitmap(getLargeIconUri(window), callback);
//            return getPlaceholderBitmap();
//        }
        return null;
    }
}
