package com.oozapps.phoneroll.Service;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.InvalidObjectException;

public interface BlobStorageService {
    final static String detectionContainerName = "detection";
    final static String roomContainerName = "rooms";

    String storeBitmapWithRandomName(Bitmap bitmap, StorageServiceCallback storageServiceCallback);

    boolean deleteRecognisedFile(String name) throws InvalidObjectException;

    Bitmap getRoomBitmap(Context context, StorageServiceCallback storageServiceCallback, String imageName) throws InvalidObjectException;

}
