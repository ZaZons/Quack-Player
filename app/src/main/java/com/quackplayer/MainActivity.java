package com.quackplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
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

    final List<FileObject> filesList = new ArrayList<>();
    FileAdapter fileAdapter;

    static ExoPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Pedir permissões
        requestPermissions();
    }

    void start() {
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
                cursor.moveToFirst();
                do {
                    long cursorId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));

                    //Get propriedades dos ficheiros para adicionar a lista
                    String getFileName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                    String getArtistName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                    String getDuration = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DURATION));

                    Uri getFileUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, cursorId);

                    //Construir um objeto de cada ficheiro com as propriedades adquiridas provisoriamente
                    final FileObject file = new FileObject(false, getFileName, getArtistName, generateTime(getDuration), getFileUri);

                    //Adicionar o objeto a lista
                    filesList.add(file);
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
        for(int i = position + 1; i < fileAdapter.getItemCount() + 1; i++) {
            if(i == fileAdapter.getItemCount())
                i = 0;

            if(i == position)
                break;

            MediaItem nextMediaItem = filesList.get(i).getMediaItem();
            player.addMediaItem(nextMediaItem);
        }

        firstItem.setPlaying(true);
        fileAdapter.notifyDataSetChanged();

        player.prepare();
        player.play();
    }

    void requestPermissions() {
        Dexter.withContext(this)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        Toast.makeText(getApplicationContext(), "Permission granted", Toast.LENGTH_SHORT).show();
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