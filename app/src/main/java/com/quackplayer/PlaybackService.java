package com.quackplayer;

import static androidx.core.app.NotificationCompat.PRIORITY_HIGH;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.session.MediaSession;
import android.os.IBinder;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.media.session.MediaButtonReceiver;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;

public class PlaybackService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        DescriptionAdapter descriptionAdapter = new DescriptionAdapter();

        PlayerNotificationManager playerNotificationManager =
                new PlayerNotificationManager.Builder(getApplicationContext(), 1, "playback_channel")
                        .setMediaDescriptionAdapter(descriptionAdapter)
                        .setNextActionIconResourceId(R.drawable.ic_skip_next)
                        .setPauseActionIconResourceId(R.drawable.ic_pause)
                        .setPreviousActionIconResourceId(R.drawable.ic_skip_previous)
                        .setPlayActionIconResourceId(R.drawable.ic_play_arrow)
                        .setSmallIconResourceId(R.mipmap.ic_launcher)
                        .setNotificationListener(new PlayerNotificationManager.NotificationListener() {
                            @Override
                            public void onNotificationPosted(int notificationId, Notification notification, boolean ongoing) {
                                PlayerNotificationManager.NotificationListener.super.onNotificationPosted(notificationId, notification, ongoing);
                                if(!ongoing) {
                                    stopForeground(true);
                                } else {
                                    startForeground(startId, notification);
                                }
                            }

                            @Override
                            public void onNotificationCancelled(int notificationId, boolean dismissedByUser) {
                                PlayerNotificationManager.NotificationListener.super.onNotificationCancelled(notificationId, dismissedByUser);
                                stopSelf();
                            }
                        })
                        .build();

        playerNotificationManager.setUseNextActionInCompactView(true);
        playerNotificationManager.setUsePreviousActionInCompactView(true);
        playerNotificationManager.setUseFastForwardAction(false);
        playerNotificationManager.setUseRewindAction(false);
        playerNotificationManager.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        playerNotificationManager.setColor(MainActivity.getColorPrimary());
        playerNotificationManager.setColorized(false);
        playerNotificationManager.setPriority(PRIORITY_HIGH);
        playerNotificationManager.setPlayer(MainActivity.getPlayer());

        return START_NOT_STICKY;
    }
}
