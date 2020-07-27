package com.oozapps.phoneroll.Service;

public interface ServiceCallback {
    void getFaceIdComplete(String faceId, String blobName);

    void identifyComplete(String personId);

    void getPersonComplete(String name, String userData);

    void createPersonComplete(String personId);

}
