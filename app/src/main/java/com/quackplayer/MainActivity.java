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

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    RecyclerView mainRecyclerView;

    final List<FilesList> filesList = new ArrayList<>();
    FileAdapter fileAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //inicializar e configurar o mainRecylerViewer
        mainRecyclerView = findViewById(R.id.mainRecyclerView);
        mainRecyclerView.setHasFixedSize(false);
        mainRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        //pedir permissoes
        requestPermissions();
    }

    void findFiles() {
        try {
            //usar a media store para encontrar os ficheiros
            ContentResolver contentResolver = getApplicationContext().getContentResolver();

            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            String selection = MediaStore.Audio.Media.IS_MUSIC +  " != 0";
            String sortOrder = MediaStore.Audio.Media.DATE_ADDED + " DESC";

            Cursor cursor = contentResolver.query(uri, null, selection, null, sortOrder);

            if(cursor == null) {
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
            } else if (!cursor.moveToNext()) {
                Toast.makeText(this, "No music found", Toast.LENGTH_SHORT).show();
            } else {
                cursor.moveToFirst();
                do {
                    long cursorId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));

                    //get propriedades das musicas para adicionar a lista
                    String getArtistName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                    String getDuration = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DURATION));
                    String getFileName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));

                    Uri getFileUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, cursorId);

                    //construir um objeto de cada ficheiro com as propriedades adquiridas provisoriamente
                    final FilesList file = new FilesList(false, getFileName, getArtistName, generateTime(getDuration), getFileUri);

                    //adicionar o objeto a lista
                    filesList.add(file);
                } while(cursor.moveToNext());

                //criar e adicionar o adaptador ao recyclerView
                fileAdapter = new FileAdapter(filesList, MainActivity.this);
                mainRecyclerView.setAdapter(fileAdapter);

                Toast.makeText(this, fileAdapter.getItemCount() + " Songs found", Toast.LENGTH_SHORT).show();

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

        Log.d("f:", stringedTime);
        return stringedTime;
    }

    void requestPermissions() {
        Dexter.withContext(this)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        Toast.makeText(getApplicationContext(), "Permission granted", Toast.LENGTH_SHORT).show();
                        findFiles();
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