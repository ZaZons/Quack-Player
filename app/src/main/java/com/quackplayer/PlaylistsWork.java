package com.quackplayer;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.text.InputFilter;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class PlaylistsWork {
    static RecyclerView playlistsRecyclerView;

    public static void create(Context context, FileObject objectToAdd) {
        final String[] playlistName = new String[1];
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Playlist name");

        EditText name = new EditText(context);
        name.setInputType(InputType.TYPE_CLASS_TEXT);
        name.setSingleLine();
        name.setFilters(new InputFilter[] { new InputFilter.LengthFilter(200) });

        FrameLayout container = new FrameLayout(context);
        FrameLayout.LayoutParams params = new  FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = convertPixelsToDp(150, context);
        params.rightMargin = convertPixelsToDp(150, context);
        name.setLayoutParams(params);
        container.addView(name);

        builder.setView(container);
        builder.setPositiveButton("Create", (dialog, which) -> {
            playlistName[0] = name.getText().toString();

            File playlistsDir = new File(context.getFilesDir(), "Playlists");
            if (!playlistsDir.exists()) {
                playlistsDir.mkdirs();
            }

            File playlistFile = new File(playlistsDir, playlistName[0] + ".json");
            try {
                if(!playlistFile.exists()) {
                    playlistFile.createNewFile();

                    Toast.makeText(context, "Playlist \"" + playlistName[0] + "\" created", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Playlist \"" + playlistName[0] + "\" already exists", Toast.LENGTH_SHORT).show();
                }
                check(null, context, objectToAdd);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    public static void check(RecyclerView playlistsRecyclerView, Context context, FileObject objectToAdd) {
        if(playlistsRecyclerView != null) {
            PlaylistsWork.playlistsRecyclerView = playlistsRecyclerView;
        } else {
            playlistsRecyclerView = PlaylistsWork.playlistsRecyclerView;
        }

        File playlistsDir = new File(context.getFilesDir(), "Playlists");
        if (!playlistsDir.exists()) {
            playlistsDir.mkdirs();
        }

        ArrayList<Path> playlists = new ArrayList<>();
        try(DirectoryStream<Path> dirStream = Files.newDirectoryStream(playlistsDir.getAbsoluteFile().toPath())) {
            for(Path file : dirStream) {
                playlists.add(file);
            }

            if(objectToAdd != null) {
                if(playlists.isEmpty()) {
                    AddToPlaylistActivity.setText("You don't have any playlists created");
                } else {
                    AddToPlaylistActivity.setTextNotVisible();
                }
            }

            PlaylistsAdapter playlistsAdapter = new PlaylistsAdapter(context, playlists, objectToAdd);

            playlistsRecyclerView.setAdapter(playlistsAdapter);
            playlistsRecyclerView.setHasFixedSize(false);
            playlistsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static int convertPixelsToDp(float px, Context context) {
        Resources res = context.getResources();
        DisplayMetrics metrics = res.getDisplayMetrics();
        return (int) (px / (metrics.densityDpi / 160f));
    }
}

















