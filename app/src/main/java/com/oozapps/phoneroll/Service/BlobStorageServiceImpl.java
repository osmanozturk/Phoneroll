package com.oozapps.phoneroll.Service;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.UUID;

import webapipackage.ApiGeneral;

public class BlobStorageServiceImpl implements BlobStorageService {
    @Override
    public String storeBitmapWithRandomName(Bitmap bitmap, StorageServiceCallback storageServiceCallback) {
        String fileUrl = null;
        String name = UUID.randomUUID().toString() + ".jpg";
        try {
            CloudStorageAccount account = CloudStorageAccount.parse(ApiGeneral.storageConnectionString);
            CloudBlobClient serviceClient = account.createCloudBlobClient();
            CloudBlobContainer container = serviceClient.getContainerReference(detectionContainerName);
            container.createIfNotExists();

            CloudBlockBlob blob = container.getBlockBlobReference(name);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            byte[] raw = outputStream.toByteArray();
            long len = raw.length;
            ByteArrayInputStream inputStream =
                    new ByteArrayInputStream(raw);
            blob.upload(inputStream, len);
            fileUrl = blob.getUri().toString() + ApiGeneral.sasKey;
            storageServiceCallback.uploadFinished(fileUrl, name);
        } catch (StorageException storageException) {
            System.out.print("StorageException encountered: ");
            System.out.println(storageException.getMessage());
            System.exit(-1);
        } catch (Exception e) {
            System.out.print("Exception encountered: ");
            System.out.println(e.getMessage());
            System.exit(-1);
        }
        return fileUrl;
    }

    @Override
    public boolean deleteRecognisedFile(String name) throws InvalidObjectException {
        try {
            CloudStorageAccount account = CloudStorageAccount.parse(ApiGeneral.storageConnectionString);
            CloudBlobClient serviceClient = account.createCloudBlobClient();
            CloudBlobContainer container = serviceClient.getContainerReference(detectionContainerName);
            if (!container.exists()) {
                throw new InvalidObjectException("Container Doesn't Exist");
            } else {
                CloudBlockBlob blob = container.getBlockBlobReference(name);
                blob.deleteIfExists();
            }

        } catch (StorageException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public Bitmap getRoomBitmap(Context context, StorageServiceCallback storageServiceCallback, String imageName) throws InvalidObjectException {
        Bitmap roomBmp = null;

        try {
            CloudStorageAccount account = CloudStorageAccount.parse(ApiGeneral.storageConnectionString);
            CloudBlobClient serviceClient = account.createCloudBlobClient();
            CloudBlobContainer container = serviceClient.getContainerReference(roomContainerName);
            if (!container.exists()) {
                throw new InvalidObjectException("Container Doesn't Exist");
            } else {
                CloudBlockBlob blob = container.getBlockBlobReference(imageName + ".jpg");
                File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                File image = File.createTempFile(
                        imageName,  /* prefix */
                        ".jpg",         /* suffix */
                        storageDir      /* directory */
                );


                blob.downloadToFile(image.getAbsolutePath());
                roomBmp = BitmapFactory.decodeFile(image.getAbsolutePath());
                storageServiceCallback.downloadFinished(imageName, roomBmp);
            }

        } catch (StorageException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return roomBmp;
    }

}
