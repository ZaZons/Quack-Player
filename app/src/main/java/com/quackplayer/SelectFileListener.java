package com.quackplayer;

import java.util.List;

public interface SelectFileListener {
    void onSelected(int position, List<FileObject> fileList, FileAdapter fileAdapter);
}
