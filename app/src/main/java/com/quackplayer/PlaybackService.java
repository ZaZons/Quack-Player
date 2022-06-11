package com.quackplayer;

import static androidx.core.app.NotificationCompat.PRIORITY_HIGH;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.exoplayer2.ui.PlayerNotificationManager;

public class PlaybackService extends Service {
    //Variável para a notificação ser apagada
    static boolean isOn;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        isOn = true;
        NotificationAdapter notificationAdapter = new NotificationAdapter();

        //Criar e personalizar o gestor de notificações
        PlayerNotificationManager playerNotificationManager =
                new PlayerNotificationManager.Builder(getApplicationContext(), 1, "playback_channel")
                        .setMediaDescriptionAdapter(notificationAdapter)
                        .setNextActionIconResourceId(R.drawable.ic_skip_next)
                        .setPauseActionIconResourceId(R.drawable.ic_pause)
                        .setPreviousActionIconResourceId(R.drawable.ic_skip_previous)
                        .setPlayActionIconResourceId(R.drawable.ic_play_arrow)
                        .setSmallIconResourceId(R.mipmap.ic_launcher)
                        .setNotificationListener(new PlayerNotificationManager.NotificationListener() {
                            @Override
                            public void onNotificationPosted(int notificationId, Notification notification, boolean ongoing) {
                                PlayerNotificationManager.NotificationListener.super.onNotificationPosted(notificationId, notification, ongoing);
                                //Se o player estiver ativo então a notificação deve ser atualizada, se não então o serviço fecha
                                if(ongoing) {
                                    startForeground(startId, notification);
                                } else if(!isOn){
                                    stopService(intent);
                                }
                            }

                            @Override
                            public void onNotificationCancelled(int notificationId, boolean dismissedByUser) {
                                PlayerNotificationManager.NotificationListener.super.onNotificationCancelled(notificationId, dismissedByUser);
                                stopSelf();
                            }
                        })
                        .build();

        //Personalizar as ações do gestor de notificações
        playerNotificationManager.setUseNextActionInCompactView(true);
        playerNotificationManager.setUsePreviousActionInCompactView(true);
        playerNotificationManager.setUseFastForwardAction(false);
        playerNotificationManager.setUseRewindAction(false);
        playerNotificationManager.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        playerNotificationManager.setColorized(false);
        playerNotificationManager.setPriority(PRIORITY_HIGH);
        playerNotificationManager.setPlayer(NewMainActivity.getPlayer());

        return START_NOT_STICKY;
    }

    public static void setIsOn(boolean isOn) {
        PlaybackService.isOn = isOn;
    }
}
