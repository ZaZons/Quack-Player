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

    static TextView text;

    //RecyclerView com a lista de playlists
    RecyclerView recyclerView;
    //Botão de criar playlist
    ImageButton add;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_to_playlist);

        //Personalizar ActionBar
        setSupportActionBar(findViewById(R.id.addToPlaylistToolBar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        text = findViewById(R.id.infoAddToPlaylist);
        recyclerView = findViewById(R.id.playlists);
        add = findViewById(R.id.createPlaylistButton);

        //Objeto para adicionar à playlist
        FileObject objectToAdd = (FileObject) getIntent().getSerializableExtra("ToAdd");
        PlaylistsWork.check(recyclerView, this, objectToAdd);

        //Botão de criar playlist
        add.setOnClickListener(v -> {
            PlaylistsWork.create(this, objectToAdd);
        });
    }

    //Botão de retroceder
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    //Definir texto de informação
    public static void setText(String value) {
        text.setVisibility(View.VISIBLE);
        text.setText(value);
    }

    //Definir texto de informação como invisível
    public static void setTextNotVisible() {
        text.setVisibility(View.GONE);
    }
}