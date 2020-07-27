package webapipackage;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public abstract class ApiGeneral {
    public static final String baseUrl = System.getenv("STORAGE_API_BASE") + "/";
    public static final String apiKey = System.getenv("FACE_API_SUBSCRIPTION_KEY");
    //Sas key for accessing uploaded blob images, the url will be concatenated with that key to be access'ble then it will be passed out to the face api
    public static final String sasKey = System.getenv("SAS_KEY");
    //Full storage connection string
    public static final String storageConnectionString = System.getenv("STORAGE_CONNECTION_STRING");


}
