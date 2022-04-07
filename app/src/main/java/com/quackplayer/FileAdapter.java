package com.quackplayer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.FileViewHolder> {

    private final List<FileObject> fileObject;
    private final SelectFileListener selectFileListener;

    public FileAdapter(List<FileObject> fileObject, Context context) {
        this.fileObject = fileObject;
        this.selectFileListener = ((SelectFileListener)context);
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FileViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.files_adapter_layout, null));
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        FileObject selectedFile = fileObject.get(position);

        holder.title.setText(selectedFile.getTitle());
        holder.artist.setText(selectedFile.getArtist());
        holder.duration.setText(selectedFile.getDuration());

        if(selectedFile.isPlaying())
            holder.rootLayout.setBackgroundResource(R.drawable.background_pink);
        else
            holder.rootLayout.setBackgroundResource(R.drawable.background_blue);

        holder.rootLayout.setOnClickListener(v -> selectFileListener.onSelected(position));
    }

    @Override
    public int getItemCount() {
        return fileObject.size();
    }

    static class FileViewHolder extends RecyclerView.ViewHolder {

        private final RelativeLayout rootLayout;
        private final TextView title;
        private final TextView artist;
        private final TextView duration;

        public FileViewHolder(@NonNull View itemView) {
            super(itemView);

            rootLayout = itemView.findViewById(R.id.rootLayout);
            title = itemView.findViewById(R.id.title);
            artist = itemView.findViewById(R.id.artist);
            duration = itemView.findViewById(R.id.duration);
        }
    }
}
