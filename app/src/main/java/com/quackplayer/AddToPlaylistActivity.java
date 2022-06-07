package com.quackplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class AddToPlaylistActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    static TextView text;
    ImageButton add;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_to_playlist);

        setSupportActionBar(findViewById(R.id.addToPlaylistToolBar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        add = findViewById(R.id.createPlaylistButton);
        recyclerView = findViewById(R.id.playlists);
        text = findViewById(R.id.infoAddToPlaylist);

        FileObject objectToAdd = (FileObject) getIntent().getSerializableExtra("ToAdd");
        PlaylistsWork.check(recyclerView, this, objectToAdd);

        add.setOnClickListener(v -> {
            PlaylistsWork.create(this, objectToAdd);
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    public static void setText(String value) {
        text.setVisibility(View.VISIBLE);
        text.setText(value);
    }

    public static void setTextNotVisible() {
        text.setVisibility(View.GONE);
    }
}









