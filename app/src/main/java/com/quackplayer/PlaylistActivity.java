package com.quackplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class PlaylistActivity extends AppCompatActivity implements SelectFileListener {

    static FileAdapter adapter;

    RecyclerView playlistFilesRecyclerView;
    TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        String playlistName = (String) getIntent().getSerializableExtra("PlaylistName");

        //Personalizar ActionBar
        setSupportActionBar(findViewById(R.id.playlistToolbar));
        getSupportActionBar().setTitle(playlistName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        playlistFilesRecyclerView = findViewById(R.id.playlistFilesRecyclerView);
        text = findViewById(R.id.infoPlaylist);

        List<FileObject> playlistObjects = (ArrayList<FileObject>) getIntent().getSerializableExtra("FilesList");

        //Se a playlist tiver ficheiros então popular o RecyclerView
        if(playlistObjects != null) {
            adapter = new FileAdapter(this, playlistObjects, playlistName);

            playlistFilesRecyclerView.setAdapter(adapter);
            playlistFilesRecyclerView.setHasFixedSize(false);
            playlistFilesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            NewMainActivity.setFileAdapter(adapter);
        } else {
            text.setVisibility(View.VISIBLE);
            text.setText("You don't have files in this playlist, go to Home and add some");
        }
    }

    @Override
    public void onSelected(List<FileObject> filesList, int position, FileAdapter adapter) {
        OnSelected.onSelected(filesList, position, adapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}