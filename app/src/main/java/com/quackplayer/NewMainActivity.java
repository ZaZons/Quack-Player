package com.quackplayer;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.ui.StyledPlayerControlView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.cardview.widget.CardView;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.quackplayer.databinding.ActivityNewMainBinding;

import java.util.List;

public class NewMainActivity extends AppCompatActivity implements SelectFileListener {

    AppBarConfiguration mAppBarConfiguration;
    ActivityNewMainBinding binding;

    static Context context;

    //Player e listener
    static ExoPlayer player;
    static Player.Listener playerListener;

    //Lista e adapter
    static List<FileObject> filesList;
    static FileAdapter fileAdapter;

    //Objeto que está a ser tocado pelo player
    static FileObject currentPlayingObject;

    //Botões
    CardView loopBtn;
    CardView skipPreviousBtn;
    CardView playPauseBtn;
    CardView skipNextBtn;
    CardView shuffleBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();

        binding = ActivityNewMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarNewMain.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_home, R.id.nav_playlists)
                .setOpenableLayout(drawer)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_new_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        //Obter o player e a view
        StyledPlayerControlView playerView = binding.appBarNewMain.playerView;
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);

        //Configurar os botões de controlo
        controls();

        //Configurar o listener
        listener();
    }

    //Controlos do player
    void controls() {
        //Obter os botões
        loopBtn = binding.appBarNewMain.loopBtn;
        skipPreviousBtn = binding.appBarNewMain.previousBtn;
        playPauseBtn = binding.appBarNewMain.playPauseBtn;
        skipNextBtn = binding.appBarNewMain.nextBtn;
        shuffleBtn = binding.appBarNewMain.shuffleBtn;

        //Botão de loop
        loopBtn.setOnClickListener(v -> {
            int repeatMode = player.getRepeatMode();

            switch(repeatMode) {
                case Player.REPEAT_MODE_OFF:
                    player.setRepeatMode(Player.REPEAT_MODE_ALL);
                    break;

                case Player.REPEAT_MODE_ONE:
                    player.setRepeatMode(Player.REPEAT_MODE_OFF);
                    break;

                case Player.REPEAT_MODE_ALL:
                    player.setRepeatMode(Player.REPEAT_MODE_ONE);
                    break;
            }
        });

        //Botão de retrocesso
        skipPreviousBtn.setOnClickListener(v -> {
            long millisecondsToGoBack = 1500;
            if(player.getCurrentPosition() >= millisecondsToGoBack || !player.hasPreviousMediaItem()) {
                player.seekTo(0);
            } else {
                if(player.hasPreviousMediaItem())
                    player.seekToPreviousMediaItem();
            }
        });

        //Botão de pausar/retomar a reprodução
        playPauseBtn.setOnClickListener(v -> {
            if(player.isPlaying())
                player.pause();
            else
                player.play();
        });

        //Botão de avanço
        skipNextBtn.setOnClickListener(v -> {
            if(player.hasNextMediaItem())
                player.seekToNextMediaItem();
        });

        //Botão de shuffle
        shuffleBtn.setOnClickListener(v ->
                player.setShuffleModeEnabled(!player.getShuffleModeEnabled())
        );
    }

    //Listener de eventos do player
    void listener() {
        //Obter as cores
        TypedValue typedValue = new TypedValue();
        TypedArray a = obtainStyledAttributes(typedValue.data, new int[] { android.R.attr.colorPrimary, R.attr.colorSecondary });
        int colorPrimary = a.getColor(0, 0);
        int colorSecondary = a.getColor(1, 0);
        a.recycle();

        //Configurar o listener
        playerListener = new Player.Listener() {
            //Atualização da UI quando o MediaItem é alterado
            @Override
            public void onMediaItemTransition(MediaItem newMediaItem, @com.google.android.exoplayer2.Player.MediaItemTransitionReason int reason) {
                if(currentPlayingObject != null) {
                    currentPlayingObject.setPlaying(false);
                }

                //Procurar pelo ficheiro que está a ser tocado na lista
                for(FileObject fileObject : filesList) {
                    if(fileObject.getMediaItem() == newMediaItem) {
                        currentPlayingObject = fileObject;
                    }
                }

                //Atualizar a UI
                if(currentPlayingObject != null) {
                    currentPlayingObject.setPlaying(true);
                    TextView nowPlaying = binding.appBarNewMain.nowPlaying;
                    nowPlaying.setText(currentPlayingObject.getTitle());
                }

                fileAdapter.notifyDataSetChanged();

                if(!player.isPlaying())
                    player.play();
            }

            //Atualizar a imagem do botão de pausar e retomar a reprodução
            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                ImageView playPauseImg = binding.appBarNewMain.playPauseImg;

                if(isPlaying)
                    playPauseImg.setImageResource(R.drawable.ic_pause);
                else
                    playPauseImg.setImageResource(R.drawable.ic_play_arrow);
            }

            //Atualizar o botão de loop
            @Override
            public void onRepeatModeChanged(int repeatMode) {
                CardView repeatOneIndicator = binding.appBarNewMain.repeatOneIndicator;

                switch(repeatMode) {
                    //Loop off, botão de loop com cor primária
                    case Player.REPEAT_MODE_OFF:
                        repeatOneIndicator.setVisibility(View.INVISIBLE);
                        loopBtn.setCardBackgroundColor(colorPrimary);
                        break;

                    //Single loop, botão de loop com cor secundária e indicador
                    case Player.REPEAT_MODE_ONE:
                        repeatOneIndicator.setVisibility(View.VISIBLE);
                        loopBtn.setCardBackgroundColor(colorSecondary);
                        break;

                    //Loop all, botão de loop com cor secundária
                    case Player.REPEAT_MODE_ALL:
                        repeatOneIndicator.setVisibility(View.INVISIBLE);
                        loopBtn.setCardBackgroundColor(colorSecondary);
                        break;
                }
            }

            //Atualizar o botão de queue aleatória
            @Override
            public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
                if(shuffleModeEnabled)
                    shuffleBtn.setCardBackgroundColor(colorSecondary);
                else
                    shuffleBtn.setCardBackgroundColor(colorPrimary);
            }
        };

        player.addListener(playerListener);
    }

    //Notificação
    static MediaSessionCompat mediaSession = null;
    static void notification() {
        //Apenas executar caso a aplicação ainda não tenha criado a notificação
        if(mediaSession == null) {
            //Criar canal e gestor de notificações
            CharSequence name = "Playback";
            String channelId = "playback_channel";
            String description = "Playback notification";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            channel.setDescription(description);

            NotificationManager notificationChannelManager = context.getSystemService(NotificationManager.class);
            notificationChannelManager.createNotificationChannel(channel);

            //Criar MediaSession e conectar ao player
            mediaSession = new MediaSessionCompat(context, "media_session");
            MediaSessionConnector mediaSessionConnector = new MediaSessionConnector(mediaSession);
            mediaSessionConnector.setPlayer(player);
            mediaSession.setActive(true);

            //Iniciar o serviço de funcionamento em segundo plano
            Intent intent = new Intent(context, PlaybackService.class);
            context.startForegroundService(intent);
        }
    }

    @Override
    public void onSelected(int position, List<FileObject> filesList, FileAdapter fileAdapter) {
        //Redirecionar o evento para o ficheiro "OnSelected"
        OnSelected.onSelected(position, filesList, fileAdapter);
    }

    //Função que devolve o player
    public static ExoPlayer getPlayer() {
        return player;
    }

    //Função que devolve o ficheiro a ser tocado atualmente
    public static FileObject getCurrentPlayingObject() {
        return currentPlayingObject;
    }

    //Função que define o fileAdapter
    public static void setFileAdapter(FileAdapter fileAdapter) {
        NewMainActivity.fileAdapter = fileAdapter;
    }

    //Função que define a lista de ficheiros
    public static void setFilesList(List<FileObject> filesList) {
        NewMainActivity.filesList = filesList;
    }

    //Função para matar todos os processos da aplicação quando fechada
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent intent = new Intent(context, PlaybackService.class);
        stopService(intent);
        PlaybackService.isOn = false;
        player = null;
        finish();
    }

    //Função gerada automaticamente para gerir o sistema de navegação
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_new_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}


















