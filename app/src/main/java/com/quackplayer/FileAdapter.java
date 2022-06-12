package com.quackplayer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import java.util.ArrayList;
import java.util.List;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.FileViewHolder> {

    Context context;

    SelectFileListener selectFileListener;

    //Lista de ficheiros atual
    List<FileObject> filesList;
    //Lista de ficheiros original, com todos os ficheiros encontrados
    static List<FileObject> originalFilesList;

    //Caso o ficheiro esteja numa playlist, então esta variável tem o nome dessa playlist
    String playlistName;

    public FileAdapter(Context context, List<FileObject> filesList, String playlistName) {
        this.context = context;
        this.selectFileListener = ((SelectFileListener)context);
        this.filesList = filesList;
        this.playlistName = playlistName;
    }

    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FileViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.files_adapter_layout, null));
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        //Definir o objeto e construir a célula
        FileObject selectedFile = filesList.get(position);

        holder.title.setText(selectedFile.getTitle());
        holder.artist.setText(selectedFile.getArtist());
        holder.duration.setText(selectedFile.getDuration());

        //Se o objeto for o que está a ser tocado então alterar a propriedade "isPlaying"
        FileObject currentPlayingObject = NewMainActivity.getCurrentPlayingObject();
        if(currentPlayingObject != null) {
            if(currentPlayingObject.getId() == selectedFile.getId()) {
                selectedFile.setPlaying(true);
                holder.rootLayout.setBackgroundResource(R.drawable.background_pink);
            } else {
                selectedFile.setPlaying(false);
                holder.rootLayout.setBackgroundResource(R.drawable.background_blue);
            }
        }

        //Definir o uso do evento "onSelected", depende se o ficheiro está ou não numa playlist
        if(playlistName == null)
            holder.rootLayout.setOnClickListener(v ->
                selectFileListener.onSelected(originalFilesList, selectedFile.getId(), this)
            );
        else
            holder.rootLayout.setOnClickListener(v ->
                selectFileListener.onSelected(filesList, position, this)
            );

        //Opções
        holder.options.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), holder.options);

            //Definir as opções, depende se o ficheiro está ou não numa playlist
            if(playlistName != null)
                popup.inflate(R.menu.file_options_playlist);
            else
                popup.inflate(R.menu.file_options_home);

            popup.setOnMenuItemClickListener(menuItem -> {
                switch(menuItem.getItemId()) {
                    //Abrir a atividade "AddToPlaylist" para adicionar o ficheiro a uma playlist
                    case R.id.addToPlaylist:
                        Intent intent = new Intent(context, AddToPlaylistActivity.class);
                        FileObject newFile = selectedFile;
                        newFile.removeMediaItem();
                        newFile.setPlaying(false);
                        intent.putExtra("ToAdd", newFile);

                        context.startActivity(intent);
                        break;

                    //Remover o objeto da playlist e atualizá-la
                    case R.id.removeFromPlaylist:
                        try {
                            Gson gson = new GsonBuilder().setPrettyPrinting().create();
                            File rootDir = new File(context.getFilesDir(), "Playlists");
                            File playlistFile = new File(rootDir, playlistName + ".json");

                            FileReader reader = new FileReader(playlistFile);
                            BufferedReader br = new BufferedReader(reader);

                            ArrayList<FileObject> playlistObjects = gson.fromJson(br,
                                    new TypeToken<ArrayList<FileObject>>(){}.getType());

                            FileObject objectToRemove = null;
                            for(FileObject file : playlistObjects) {
                                if(file.getId() == selectedFile.getId()) {
                                    objectToRemove = file;
                                    break;
                                }
                            }
                            playlistObjects.remove(objectToRemove);

                            FileWriter writer = new FileWriter(playlistFile);
                            String jsonObject = gson.toJson(playlistObjects,
                                    new TypeToken<ArrayList<FileObject>>(){}.getType());

                            writer.write(jsonObject);
                            writer.close();

                            filesList = playlistObjects;
                            notifyDataSetChanged();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                }

                return false;
            });
            popup.show();
        });
    }

    @Override
    public int getItemCount() {
        return filesList.size();
    }

    static class FileViewHolder extends RecyclerView.ViewHolder {

        //Variáveis que correspondem à célula
        RelativeLayout rootLayout;
        TextView title;
        TextView artist;
        TextView duration;
        ImageView options;

        public FileViewHolder(@NonNull View itemView) {
            super(itemView);

            rootLayout = itemView.findViewById(R.id.rootLayout);
            title = itemView.findViewById(R.id.title);
            artist = itemView.findViewById(R.id.artist);
            duration = itemView.findViewById(R.id.duration);
            options = itemView.findViewById(R.id.options);
        }
    }

    //Função para o sistema de pesquisa
    public void filter(List<FileObject> filteredList) {
        filesList = filteredList;
        notifyDataSetChanged();
    }

    //Função para definir a lista de ficheiros original
    public static void setOriginalFilesList(List<FileObject> originalFilesList) {
        FileAdapter.originalFilesList = originalFilesList;
    }
}