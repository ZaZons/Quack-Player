package com.quackplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class PlaylistActivity extends AppCompatActivity implements SelectFileListener {
    RecyclerView filesInPlaylist;
    TextView text;
    static FileAdapter adapter;
    static List<FileObject> filesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        String playlistName = (String) getIntent().getSerializableExtra("PlaylistName");
        setSupportActionBar(findViewById(R.id.playlistToolbar));
        getSupportActionBar().setTitle(playlistName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        List<FileObject> playlistObjects = (ArrayList<FileObject>) getIntent().getSerializableExtra("FilesList");
        Log.d("playlistsAdapter", "finalPlaylistObjects: " + playlistObjects);

        filesInPlaylist = findViewById(R.id.files);
        text = findViewById(R.id.infoPlaylist);

        if(playlistObjects != null) {
            adapter = new FileAdapter(this, playlistObjects, playlistName);

            filesInPlaylist.setAdapter(adapter);
            filesInPlaylist.setHasFixedSize(false);
            filesInPlaylist.setLayoutManager(new LinearLayoutManager(this));
            NewMainActivity.setFileAdapter(adapter);
        } else {
            text.setVisibility(View.VISIBLE);
            text.setText("You don't have files in this playlist, go to Home and add some");
        }
    }

    @Override
    public void onSelected(List<FileObject> filesList, int position, FileAdapter adapter) {
        PlaylistActivity.filesList = filesList;
        PlaylistActivity.adapter = adapter;

        OnSelected.onSelected(filesList, position, adapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}






