package com.oozapps.phoneroll.Service;

import android.graphics.Bitmap;

public interface StorageServiceCallback {
    void uploadFinished(String url, String blobName);

    void downloadFinished(String name, Bitmap bitmap);
}
