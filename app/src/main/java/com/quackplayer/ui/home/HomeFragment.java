package com.quackplayer.ui.home;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.quackplayer.FileAdapter;
import com.quackplayer.FileObject;
import com.quackplayer.R;
import com.quackplayer.NewMainActivity;
import com.quackplayer.databinding.FragmentHomeBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {
    Context context;

    FragmentHomeBinding binding;

    static List<FileObject> filesList = new ArrayList<>();

    FileAdapter fileAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = this.getContext();

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setHasOptionsMenu(true);

        requestPermissions();
        return root;
    }

    void start() {
        if(fileAdapter == null)
            findFiles();
        else
            populateRecyclerView();
    }

    void findFiles() {
        try {
            //Usar a MediaStore para encontrar os ficheiros
            ContentResolver contentResolver = context.getContentResolver();

            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            String selection = MediaStore.Audio.Media.IS_MUSIC +  " != 0";
            String sortOrder = MediaStore.Audio.Media.DATE_ADDED + " DESC";

            Cursor cursor = contentResolver.query(uri, null, selection, null, sortOrder);

            if(cursor == null) {
                Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
            } else if (!cursor.moveToNext()) {
                Toast.makeText(context, "No files found", Toast.LENGTH_SHORT).show();
            } else {
                int id = 0;
                cursor.moveToFirst();
                do {
                    long cursorId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));

                    //Get propriedades dos ficheiros para adicionar a lista
                    String getFileName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                    String getArtistName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                    String getDuration = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DURATION));

                    Uri getFileUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, cursorId);

                    //Construir um objeto de cada ficheiro com as propriedades adquiridas provisoriamente
                    final FileObject file = new FileObject(id, getFileName, getArtistName, generateTime(getDuration), getFileUri);

                    //Adicionar o objeto a lista
                    filesList.add(file);
                    id++;
                } while(cursor.moveToNext());

                populateRecyclerView();

                cursor.close();
            }
        } catch (Exception e) {
            Log.d("FindFiles", "Error: " + e.getMessage());
        }
    }

    String generateTime(String duration) {
        long time = Long.parseLong(duration);
        long seconds = time / 1000;
        long minutes = seconds / 60;

        seconds = seconds % 60;

        String stringedTime = "";

        if(minutes > 60) {
            long hours = minutes / 60;
            minutes = minutes % 60;

            String stringedHours = String.format(Locale.ENGLISH, "%02d", hours);
            stringedTime = stringedHours + ":";
        }

        String stringedSeconds = String.format(Locale.ENGLISH, "%02d", seconds);
        String stringedMinutes = String.format(Locale.ENGLISH, "%02d", minutes);
        stringedTime += stringedMinutes + ":" + stringedSeconds;

        return stringedTime;
    }

    void populateRecyclerView() {
        RecyclerView mainRecyclerView = binding.mainRecyclerView;

        if(fileAdapter == null) {
            fileAdapter = new FileAdapter(context, filesList, null);
            FileAdapter.setOriginalFilesList(filesList);
        }

        mainRecyclerView.setAdapter(fileAdapter);
        mainRecyclerView.setHasFixedSize(false);
        mainRecyclerView.setLayoutManager(new LinearLayoutManager(context));

        NewMainActivity.setFileAdapter(fileAdapter);

        Toast.makeText(context, fileAdapter.getItemCount() + " files found", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.new_main, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);

        SearchView searchView = (SearchView) searchItem.getActionView();

        if(!filesList.isEmpty()) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    List<FileObject> filteredList = new ArrayList<>();

                    for(FileObject item : filesList) {
                        String title = item.getTitle().toUpperCase();
                        String author = item.getArtist().toUpperCase();
                        if(title.contains(newText.toUpperCase()) ||
                                author.contains(newText.toUpperCase())) {
                            filteredList.add(item);
                        }
                    }

                    fileAdapter.filter(filteredList);
                    return false;
                }
            });
        }
    }

    void requestPermissions() {
        Dexter.withContext(context)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        start();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}






