package com.quackplayer;

import android.app.PendingIntent;
import android.graphics.Bitmap;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;

public class NotificationAdapter implements PlayerNotificationManager.MediaDescriptionAdapter {

    @Override
    public CharSequence getCurrentContentTitle(Player player) {
        //Nome do ficheiro a ser tocado pelo player
        return NewMainActivity.getCurrentPlayingObject().getTitle();
    }

    @Nullable
    @Override
    public PendingIntent createCurrentContentIntent(Player player) {
        return null;
    }

    @Nullable
    @Override
    public CharSequence getCurrentContentText(Player player) {
        //Autor do ficheiro a ser tocado pelo player
        return NewMainActivity.getCurrentPlayingObject().getArtist();
    }

    @Nullable
    @Override
    public Bitmap getCurrentLargeIcon(Player player, PlayerNotificationManager.BitmapCallback callback) {
        return null;
    }
}
