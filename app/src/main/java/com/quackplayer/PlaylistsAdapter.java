package com.quackplayer;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class PlaylistsAdapter extends RecyclerView.Adapter<PlaylistsAdapter.PlaylistsViewHolder> {

    Context context;

    //Lista de playlists
    List<Path> playlistsList;

    //Objeto a adicionar, se existir um
    FileObject objectToAdd;

    //Número de playlists em que o objeto a adicionar está
    static int objectInXPlaylists;

    public PlaylistsAdapter(Context context, List<Path> playlistsList, FileObject objectToAdd) {
        this.playlistsList = playlistsList;
        this.context = context;
        this.objectToAdd = objectToAdd;
        objectInXPlaylists = 0;
    }

    @NonNull
    @Override
    public PlaylistsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PlaylistsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.playlists_adapter_layout, null));
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistsViewHolder holder, int position) {
        //Definir o objeto e construir a célula
        Path playlist = playlistsList.get(position);

        String name = playlist.getFileName().toString();
        final String finalName = name.substring(0, name.length() - 5);

        holder.name.setText(finalName);

        try {
            //Obter ficheiro da playlist
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            File rootDir = new File(context.getFilesDir(), "Playlists");
            File playlistFile = new File(rootDir, finalName + ".json");

            holder.options.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(v.getContext(), holder.options);
                popup.inflate(R.menu.playlist_options);

                //Eliminar playlist
                popup.setOnMenuItemClickListener(item -> {
                    playlistFile.delete();
                    PlaylistsWork.check(null, context, objectToAdd);
                    return false;
                });
                popup.show();
            });

            //Ler o ficheiro da playlist e obter a lista de ficheiros
            FileReader reader = new FileReader(playlistFile);
            BufferedReader br = new BufferedReader(reader);

            ArrayList<FileObject> playlistObjects = gson.fromJson(br, new TypeToken<ArrayList<FileObject>>(){}.getType());

            //Se existir um objeto para adicionar à playlist então o objeto é adicionado à lista e o ficheiro é atualizado
            //Se não então carregar no ficheiro começa a reprodução
            if(objectToAdd != null) {
                if(playlistObjects != null) {
                    for(FileObject object : playlistObjects) {
                        if(object.getId() == objectToAdd.getId()) {
                            holder.rootLayout.setVisibility(View.GONE);
                            objectInXPlaylists++;
                        }
                    }
                    if(objectInXPlaylists >= playlistsList.size()) {
                        AddToPlaylistActivity.setText("You added this file to all your playlists");
                        objectInXPlaylists = 1;
                    } else {
                        AddToPlaylistActivity.setTextNotVisible();
                    }
                } else {
                    playlistObjects = new ArrayList<>();
                }

                ArrayList<FileObject> finalPlaylistObjects = playlistObjects;
                holder.rootLayout.setOnClickListener(v -> {
                    try {
                        FileWriter writer = new FileWriter(playlistFile);

                        finalPlaylistObjects.add(objectToAdd);
                        String jsonObject = gson.toJson(finalPlaylistObjects, new TypeToken<ArrayList<FileObject>>(){}.getType());

                        writer.write(jsonObject);
                        writer.close();

                        Toast.makeText(context, "\"" + objectToAdd.getTitle() + "\" added to \"" + finalName +
                                "\" playlist", Toast.LENGTH_SHORT).show();
                        notifyDataSetChanged();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } else {
                holder.rootLayout.setOnClickListener(v -> {
                    try {
                        FileReader reader1 = new FileReader(playlistFile);
                        BufferedReader br1 = new BufferedReader(reader1);

                        ArrayList<FileObject> finalPlaylistObjects = gson.fromJson(br1, new TypeToken<ArrayList<FileObject>>(){}.getType());
                        Intent intent = new Intent(context, PlaylistActivity.class);
                        intent.putExtra("FilesList", finalPlaylistObjects);
                        intent.putExtra("PlaylistName", finalName);
                        context.startActivity(intent);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return playlistsList.size();
    }

    static class PlaylistsViewHolder extends RecyclerView.ViewHolder {

        //Variáveis que correspondem à célula
        private final RelativeLayout rootLayout;
        private final TextView name;
        private final ImageView options;

        public PlaylistsViewHolder(@NonNull View itemView) {
            super(itemView);

            rootLayout = itemView.findViewById(R.id.rootLayout);
            name = itemView.findViewById(R.id.name);
            options = itemView.findViewById(R.id.options);
        }
    }
}
























