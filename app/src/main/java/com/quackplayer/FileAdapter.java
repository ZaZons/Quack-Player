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

    private List<FileObject> filesList;
    private final SelectFileListener selectFileListener;
    private Context context;
    private String playlistName;
    static List<FileObject> originalFilesList;

    public FileAdapter(List<FileObject> filesList, Context context, String playlistName) {
        this.filesList = filesList;
        this.selectFileListener = ((SelectFileListener)context);
        this.context = context;
        this.playlistName = playlistName;
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FileViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.files_adapter_layout, null));
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        FileObject selectedFile = filesList.get(position);

        holder.title.setText(selectedFile.getTitle());
        holder.artist.setText(selectedFile.getArtist());
        holder.duration.setText(selectedFile.getDuration());

        FileObject currentPlayingObject = NewMainActivity.getCurrentPlayingObject();
        if(currentPlayingObject != null) {
            if(currentPlayingObject.getId() == selectedFile.getId()) {
                selectedFile.setPlaying(true);
            } else {
                selectedFile.setPlaying(false);
            }
        }

        if(selectedFile.isPlaying())
            holder.rootLayout.setBackgroundResource(R.drawable.background_pink);
        else
            holder.rootLayout.setBackgroundResource(R.drawable.background_blue);

        if(playlistName == null)
            holder.rootLayout.setOnClickListener(v -> selectFileListener.onSelected(selectedFile.getId(), originalFilesList, this));
        else
            holder.rootLayout.setOnClickListener(v -> selectFileListener.onSelected(position, filesList, this));

        holder.options.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), holder.options);

            if(playlistName != null)
                popup.inflate(R.menu.file_options_playlist);
            else
                popup.inflate(R.menu.file_options_home);

            popup.setOnMenuItemClickListener(menuItem -> {
                switch(menuItem.getItemId()) {
                    case R.id.addToPlaylist:
                        Intent intent = new Intent(context, AddToPlaylistActivity.class);
                        FileObject newFile = selectedFile;
                        newFile.removeMediaItem();
                        newFile.setPlaying(false);
                        intent.putExtra("ToAdd", newFile);

                        context.startActivity(intent);
                        break;
                    case R.id.removeFromPlaylist:
                        try {
                            Gson gson = new GsonBuilder().setPrettyPrinting().create();
                            File rootDir = new File(context.getFilesDir(), "Playlists");
                            File playlistFile = new File(rootDir, playlistName + ".json");

                            FileReader reader = new FileReader(playlistFile);
                            BufferedReader br = new BufferedReader(reader);

                            ArrayList<FileObject> playlistObjects = gson.fromJson(br, new TypeToken<ArrayList<FileObject>>(){}.getType());
                            FileObject objectToRemove = null;
                            for(FileObject file : playlistObjects) {
                                if(file.getId() == selectedFile.getId()) {
                                    objectToRemove = file;
                                    break;
                                }
                            }
                            playlistObjects.remove(objectToRemove);

                            FileWriter writer = new FileWriter(playlistFile);
                            String jsonObject = gson.toJson(playlistObjects, new TypeToken<ArrayList<FileObject>>(){}.getType());

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

        private final RelativeLayout rootLayout;
        private final TextView title;
        private final TextView artist;
        private final TextView duration;
        private final ImageView options;

        public FileViewHolder(@NonNull View itemView) {
            super(itemView);

            rootLayout = itemView.findViewById(R.id.rootLayout);
            title = itemView.findViewById(R.id.title);
            artist = itemView.findViewById(R.id.artist);
            duration = itemView.findViewById(R.id.duration);
            options = itemView.findViewById(R.id.options);
        }
    }

    public void filter(List<FileObject> filteredList) {
        filesList = filteredList;
        notifyDataSetChanged();
    }

    public static void setOriginalFilesList(List<FileObject> originalFilesList) {
        FileAdapter.originalFilesList = originalFilesList;
    }
}


























