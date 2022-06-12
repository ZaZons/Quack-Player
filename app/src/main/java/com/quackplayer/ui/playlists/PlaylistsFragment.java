package com.quackplayer.ui.playlists;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.quackplayer.PlaylistsWork;
import com.quackplayer.databinding.FragmentPlaylistsBinding;

import java.io.File;

public class PlaylistsFragment extends Fragment {

    Context context;

    FragmentPlaylistsBinding binding;

    RecyclerView playlistsRecyclerView;
    RelativeLayout createPlaylist;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = this.getContext();
        File rootDir = new File(context.getFilesDir(), "Playlists");

        binding = FragmentPlaylistsBinding .inflate(inflater, container, false);
        View root = binding.getRoot();

        createPlaylist = binding.createPlaylist;
        playlistsRecyclerView = binding.playlistsRecyclerView;

        createPlaylist.setOnClickListener(v -> {
            PlaylistsWork.create(context, null);
        });

        PlaylistsWork.check(playlistsRecyclerView, context, null);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}




