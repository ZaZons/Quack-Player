package com.quackplayer;

import java.util.List;

public interface SelectFileListener {
    //Evento que recebe a lista de ficheiros, a posição do primeiro ficheiro e o adapter
    void onSelected(List<FileObject> filesList, int position, FileAdapter fileAdapter);
}