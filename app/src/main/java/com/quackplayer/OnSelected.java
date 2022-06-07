package com.quackplayer;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;

import java.util.List;

public class OnSelected {
    public static void onSelected(int position, List<FileObject> filesList, FileAdapter fileAdapter) {
        NewMainActivity.setFilesList(filesList);

        ExoPlayer player = NewMainActivity.getPlayer();
        //Get o mediaItem do objeto selecionado
        FileObject firstItem = filesList.get(position);
        firstItem.createMediaItem();

        MediaItem mediaItem = firstItem.getMediaItem();

        //Parar o player e limpar a queue
        player.stop();
        player.clearMediaItems();

        //Adicionar à queue o item selecionado e os restantes depois
        //Quando chegar ao fim da lista vai para o primeiro item e adiciona
        //os items seguintes até voltar ao primeiro adicionado
        player.addMediaItem(mediaItem);
        for(int i = position + 1; i < filesList.size() + 1; i++) {
            if(i == filesList.size())
                i = 0;

            if(i == position)
                break;

            filesList.get(i).createMediaItem();
            MediaItem nextMediaItem = filesList.get(i).getMediaItem();
            player.addMediaItem(nextMediaItem);
        }

//        NewMainActivity.setCurrentPlayingObject(firstItem);
        firstItem.setPlaying(true);
        fileAdapter.notifyDataSetChanged();

        //Autorizar o player a tocar em segundo plano
        NewMainActivity.notification();

        player.prepare();
        player.play();
    }
}

