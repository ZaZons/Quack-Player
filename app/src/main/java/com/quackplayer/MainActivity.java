package com.quackplayer;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import com.google.android.exoplayer2.ui.StyledPlayerControlView;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SelectFileListener {

    RecyclerView mainRecyclerView;

    static final List<FileObject> filesList = new ArrayList<>();
    FileAdapter fileAdapter;

    static ExoPlayer player;

    CardView skipPreviousBtn;
    CardView playPauseBtn;
    CardView skipNextBtn;

    ImageView playPauseImg;

    static FileObject currentPlayingObject;

    CardView loopBtn;
    CardView repeatOneIndicator;
    CardView shuffleBtn;

    static int colorPrimary;
    int colorSecondary;
    int backgroundColor;

    MediaSessionCompat mediaSession;
    PlayerNotificationManager playerNotificationManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_QuackPlayer);
        setContentView(R.layout.activity_main);

        //Pedir permissões
        requestPermissions();
    }

    void start() {
        skipPreviousBtn = findViewById(R.id.previousBtn);
        playPauseBtn = findViewById(R.id.playPauseBtn);
        skipNextBtn = findViewById(R.id.nextBtn);

        playPauseImg = findViewById(R.id.playPauseImg);

        loopBtn = findViewById(R.id.loopBtn);
        repeatOneIndicator = findViewById(R.id.repeatOneIndicator);
        shuffleBtn = findViewById(R.id.shuffleBtn);

        //Obter as cores
//        int attrs[] = {android.R.attr.colorPrimary, R.attr.colorSecondary};
//        TypedArray colors = obtainStyledAttributes(R.style.Theme_QuackPlayer, attrs);
//        colorPrimary = colors.getColor(0, 0);
//        colorSecondary = colors.getColor(1, 0);
        TypedValue value = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.colorPrimary, value, true);
        colorPrimary = value.data;
        getTheme().resolveAttribute(android.R.attr.colorSecondary, value, true);
        colorSecondary = value.data;

        StyledPlayerControlView playerView = findViewById(R.id.playerView);

        //Inicializar e configurar o mainRecylerView
        mainRecyclerView = findViewById(R.id.mainRecyclerView);
        mainRecyclerView.setHasFixedSize(false);
        mainRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        findFiles();

        //Inicializar o player
        //Associar à view
        //Preparar
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);

        //Configurar os controlos da aplicação
        controls();

        //Configurar o listener do player
        listener();

        //Ativar ActionBar custom
        customActionBar();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.top_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.actionSearch);
        MenuItem searchItemBlack = menu.findItem(R.id.actionSearchBlack);
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_NO) {
            searchItem.setVisible(false);
            searchItemBlack.setVisible(true);
            searchItem = searchItemBlack;
        }

        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                List<FileObject> filteredList = new ArrayList<>();

                for(FileObject item : filesList) {
                    if(item.getTitle().toLowerCase().contains(newText.toLowerCase())) {
                        filteredList.add(item);
                    }
                }
                fileAdapter.filter(filteredList);
                return false;
            }
        });
        return true;
    }

    void customActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(R.layout.custom_action_bar);
        }
    }

    void controls() {
        //Reproduzir o MediaItem anterior
        skipPreviousBtn.setOnClickListener(v -> {
            long millisecondsToGoBack = 1500;
            if(player.getCurrentPosition() >= millisecondsToGoBack) {
                player.seekTo(0);
            } else {
                if(player.hasPreviousMediaItem())
                    player.seekToPreviousMediaItem();
            }
        });

        //Pausar e retomar a reprodução da queue
        playPauseBtn.setOnClickListener(v -> {
            if(player.isPlaying())
                player.pause();
            else
                player.play();
        });

        //Reproduzir o próximo MediaItem
        skipNextBtn.setOnClickListener(v -> {
            if(player.hasNextMediaItem())
                player.seekToNextMediaItem();
        });

        //Alternar entre os diferentes tipos de loop (um MediaItem, queue ou nenhum)
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

        //Ativar e desativar o shuffle
        shuffleBtn.setOnClickListener(v ->
            player.setShuffleModeEnabled(!player.getShuffleModeEnabled())
        );
    }

    void listener() {
        player.addListener(new ExoPlayer.Listener() {
            //Atualização da UI quando o MediaItem é alterado
            @Override
            public void onMediaItemTransition(MediaItem newMediaItem, @com.google.android.exoplayer2.Player.MediaItemTransitionReason int reason) {
                if(currentPlayingObject != null)
                    currentPlayingObject.setPlaying(false);

                for(FileObject fileObject : filesList) {
                    if(fileObject.getMediaItem() == newMediaItem)
                        currentPlayingObject = fileObject;
                }

                if(currentPlayingObject != null) {
                    currentPlayingObject.setPlaying(true);
                    fileAdapter.notifyDataSetChanged();
                }

                if(!player.isPlaying())
                    player.play();
            }

            //Atualizar a imagem do botão de pausar e retomar a reprodução
            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                if(isPlaying)
                    playPauseImg.setImageResource(R.drawable.ic_pause);
                else
                    playPauseImg.setImageResource(R.drawable.ic_play_arrow);
            }

            //Atualizar o botão de repetição
            @Override
            public void onRepeatModeChanged(int repeatMode) {
                int visibility = repeatOneIndicator.getVisibility();
                ColorStateList cardColor = loopBtn.getCardBackgroundColor();

                switch(repeatMode) {
                    case Player.REPEAT_MODE_OFF:
                        repeatOneIndicator.setVisibility(View.INVISIBLE);
                        loopBtn.setCardBackgroundColor(colorPrimary);
                        break;

                    case Player.REPEAT_MODE_ONE:
                        repeatOneIndicator.setVisibility(View.VISIBLE);
                        loopBtn.setCardBackgroundColor(colorSecondary);
                        break;

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
        });
    }

    void notification() {
        CharSequence name = "Playback";
        String channelId = "playback_channel";
        String description = "Playback notification";
        int importance = NotificationManager.IMPORTANCE_HIGH;

        NotificationChannel channel = new NotificationChannel(channelId, name, importance);
        channel.setDescription(description);

        NotificationManager notificationChannelManager = getSystemService(NotificationManager.class);
        notificationChannelManager.createNotificationChannel(channel);

        mediaSession = new MediaSessionCompat(this, "media_session");
        MediaSessionConnector mediaSessionConnector = new MediaSessionConnector(mediaSession);
        mediaSessionConnector.setPlayer(player);
        mediaSession.setActive(true);

        Context context = getApplicationContext();
        Intent intent = new Intent(this, PlaybackService.class);
        context.startForegroundService(intent);
    }

    void findFiles() {
        try {
            //Usar a MediaStore para encontrar os ficheiros
            ContentResolver contentResolver = getApplicationContext().getContentResolver();

            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            String selection = MediaStore.Audio.Media.IS_MUSIC +  " != 0";
            String sortOrder = MediaStore.Audio.Media.DATE_ADDED + " DESC";

            Cursor cursor = contentResolver.query(uri, null, selection, null, sortOrder);

            if(cursor == null) {
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
            } else if (!cursor.moveToNext()) {
                Toast.makeText(this, "No files found", Toast.LENGTH_SHORT).show();
            } else {
                int id = 0;
                cursor.moveToFirst();
                do {
                    long cursorId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));

                    //Get propriedades dos ficheiros para adicionar a lista
                    String getFileName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                    String getArtistName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                    String getDuration = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DURATION));

                    Uri getFileUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, cursorId);

                    //Construir um objeto de cada ficheiro com as propriedades adquiridas provisoriamente
                    final FileObject file = new FileObject(id, false, getFileName, getArtistName, generateTime(getDuration), getFileUri);

                    //Adicionar o objeto a lista
                    filesList.add(file);
                    id++;
                } while(cursor.moveToNext());

                //Criar e adicionar o adaptador ao mainRecyclerView
                fileAdapter = new FileAdapter(filesList, MainActivity.this);
                mainRecyclerView.setAdapter(fileAdapter);

                Toast.makeText(this, fileAdapter.getItemCount() + " files found", Toast.LENGTH_SHORT).show();

                cursor.close();
            }
        } catch (Exception e) {
            Log.d("FindFiles", "Error: " + e.getMessage());
        }
    }

    String generateTime(String duration) {
        long time = Long.parseLong(duration);
        long seconds = time / 1000;
        long minutes = seconds / 60;

        seconds = seconds % 60;

        String stringedTime = "";

        if(minutes > 60) {
            long hours = minutes / 60;
            minutes = minutes % 60;

            String stringedHours = String.format(Locale.ENGLISH, "%02d", hours);
            stringedTime = stringedHours + ":";
        }

        String stringedSeconds = String.format(Locale.ENGLISH, "%02d", seconds);
        String stringedMinutes = String.format(Locale.ENGLISH, "%02d", minutes);
        stringedTime += stringedMinutes + ":" + stringedSeconds;

        return stringedTime;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        player = null;
    }

    @Override
    public void onSelected(int position) {
        //Get o mediaItem do objeto selecionado
        FileObject firstItem = filesList.get(position);
        MediaItem mediaItem = firstItem.getMediaItem();

        //Se o mediaItem q selecionar ja estiver a ser tocado no player
        //então a função retorna
        if(player.getCurrentMediaItem() == mediaItem) {
            player.seekTo(0);
            return;
        }

        //Parar o player e limpar a queue
        player.stop();
        player.clearMediaItems();

        //Adicionar à queue o item selecionado e os restantes depois
        //Quando chegar ao fim da lista vai para o primeiro item e adiciona
        //os items seguintes até voltar ao primeiro adicionado
        player.addMediaItem(mediaItem);
        for(int i = position + 1; i < filesList.size() + 1; i++) {
            if(i == filesList.size())
                i = 0;

            if(i == position)
                break;

            MediaItem nextMediaItem = filesList.get(i).getMediaItem();
            player.addMediaItem(nextMediaItem);
        }

        firstItem.setPlaying(true);
        fileAdapter.notifyDataSetChanged();


        //Autorizar o player a tocar em segundo plano
        notification();

        player.prepare();
        player.play();
    }

    public static FileObject getCurrentPlayingObject() {
        return currentPlayingObject;
    }

    public static ExoPlayer getPlayer() {
        return player;
    }

    void requestPermissions() {
        Dexter.withContext(this)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        start();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        Toast.makeText(getApplicationContext(), "Permission denied", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }
}

























