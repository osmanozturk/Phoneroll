package com.oozapps.phoneroll.Activity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.FaceServiceRestClient;
import com.oozapps.phoneroll.R;
import com.oozapps.phoneroll.Service.BlobStorageServiceImpl;
import com.oozapps.phoneroll.Service.EncapsulatedApiService;
import com.oozapps.phoneroll.Service.ServiceCallback;
import com.oozapps.phoneroll.Service.StorageServiceCallback;

import java.io.File;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.content.ContentValues.TAG;

public class MainActivity extends Activity {
    private final int CAMERA_REQUEST = 34313;
    private final int MY_CAMERA_PERMISSION_CODE = 333;


    private final String apiEndpoint = System.getenv("FACE_API_ENDPOINT") + "/face/v1.0";
    // Add your Face subscription key to your environment variables.
    private final String subscriptionKey = System.getenv("FACE_API_SUBSCRIPTION_KEY");


    private final FaceServiceClient faceServiceClient =
            new FaceServiceRestClient(apiEndpoint, subscriptionKey);
    private final BlobStorageServiceImpl blobStorageService = new BlobStorageServiceImpl();
    private final int PICK_IMAGE = 1;
    private String uniqueDeviceId = null;
    private ProgressDialog detectionProgressDialog;
    private EncapsulatedApiService encapsulatedApiService = new EncapsulatedApiService();
    private final ServiceCallback identifyChainCallback = new ServiceCallback() {
        @Override
        public void getFaceIdComplete(String faceId, String blobName) {
            if (faceId == null) {
                Log.e(TAG, "getFaceIdComplete: Callback Null FaceId");
            } else {
                encapsulatedApiService.identifyWithFaceId(faceId, this);
                Thread networkThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            blobStorageService.deleteRecognisedFile(blobName);
                        } catch (InvalidObjectException e) {
                            e.printStackTrace();
                        }
                    }
                });
                networkThread.start();

            }
        }

        @Override
        public void identifyComplete(String personId) {
            if (personId == null) {
                Log.e(TAG, "identifyComplete: Callback Null PersonId");
                Toast.makeText(MainActivity.this, "No Match Found", Toast.LENGTH_LONG).show();
            } else {
                encapsulatedApiService.getPersonWithPersonId("celebrities", personId, this);
            }
        }

        @Override
        public void getPersonComplete(String name, String userData) {
            if (name == null) {
                Log.e(TAG, "getPersonComplete: Callback Null Name");
            } else {
                Log.d(TAG, "getPersonComplete: Name " + name);
                String message;
                Intent accessIntent = null;
                if (uniqueDeviceId.equals(userData)) {
                    message = name + " is Authenticated\n Welcome";
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                    accessIntent = new Intent(MainActivity.this, GrantedActivity.class);


                } else {
                    message = name + "\nDevice mismatch, Access Denied";
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                    accessIntent = new Intent(MainActivity.this, DeniedActivity.class);
                    Log.d(TAG, "getPersonComplete: DevicID" + uniqueDeviceId);


                }

                accessIntent.putExtra("extraTextInfo", message);
                accessIntent.putExtra("uniqueDeviceId", uniqueDeviceId);
                MainActivity.this.startActivity(accessIntent);

            }

        }

        @Override
        public void createPersonComplete(String personId) {

        }
    };

    private final StorageServiceCallback storageServiceCallback = new StorageServiceCallback() {
        @Override
        public void uploadFinished(String url, String blobName) {
            encapsulatedApiService.getFaceId(url, blobName, identifyChainCallback);
        }

        @Override
        public void downloadFinished(String name, Bitmap bitmap) {

        }
    };
    private String currentPhotoPath;
    private Uri photoURI;
    private ImageView faceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button browseButton = findViewById(R.id.button1);
        Button cameraButton = findViewById(R.id.button2);
        faceView = findViewById(R.id.imageView1);
        browseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(
                        intent, "Select Picture"), PICK_IMAGE);
            }
        });
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (MainActivity.this.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
                    } else {
                        dispatchTakePictureIntent();
                    }
                } else {
                    dispatchTakePictureIntent();
                }
            }
        });
        detectionProgressDialog = new ProgressDialog(this);
        uniqueDeviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK &&
                data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                        getContentResolver(), uri);
                faceView.setImageBitmap(bitmap);


                Thread networkThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        blobStorageService.storeBitmapWithRandomName(bitmap, storageServiceCallback);
                    }
                });
                networkThread.start();


            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            //Bundle extras = data.getExtras();
            try {
                Bitmap image = MediaStore.Images.Media.getBitmap(
                        getContentResolver(), photoURI);
                faceView.setImageBitmap(image);
                Thread networkThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        blobStorageService.storeBitmapWithRandomName(image, storageServiceCallback);
                    }
                });
                networkThread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }

    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this,
                        "com.oozapps.phoneroll.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST);
            }
        }
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

}