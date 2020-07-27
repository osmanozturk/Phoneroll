package com.oozapps.phoneroll.Activity;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.oozapps.phoneroll.R;
import com.oozapps.phoneroll.Service.BlobStorageServiceImpl;
import com.oozapps.phoneroll.Service.StorageServiceCallback;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.AKAZE;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.Features2d;
import org.opencv.imgproc.Imgproc;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidObjectException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class MatchActivity extends Activity {
    static {

        if (OpenCVLoader.initDebug()) {
            Log.i(TAG, "OpenCV initialize success");
            //System.loadLibrary("gnustl_shared");
            //System.loadLibrary("nonfree");
        } else {
            Log.i(TAG, "OpenCV initialize failed");
        }
    }

    private final int PICK_IMAGE_FIRST = 1;
    private final int PICK_IMAGE_SECOND = 2;
    private final int CAMERA_REQUEST = 34313;
    private final int MY_CAMERA_PERMISSION_CODE = 333;
    private final BlobStorageServiceImpl blobStorageService = new BlobStorageServiceImpl();
    private Button browseButton;
    private Button confirmButton;
    private Button cameraButton;
    private TextView resultTextView = null;
    private Bitmap firstBitmap = null;
    private Bitmap secondBitmap = null;
    private ImageView resView = null;
    private String currentPhotoPath;
    private Uri photoURI;
    private boolean matchingOperationDone = false;
    private boolean photosAreMatching = false;
    private Bitmap photoToSearch = null;
    private Bitmap roomBitmap = null;
    private final StorageServiceCallback storageServiceCallback = new StorageServiceCallback() {
        @Override
        public void uploadFinished(String url, String blobName) {

        }

        @Override
        public void downloadFinished(String name, Bitmap bitmap) {
            roomBitmap = bitmap;
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_FIRST && resultCode == RESULT_OK &&
                data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                photoToSearch = MediaStore.Images.Media.getBitmap(
                        getContentResolver(), uri);
                while (roomBitmap == null) ;
                performFeatureMatch();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

//        if (requestCode == PICK_IMAGE_SECOND && resultCode == RESULT_OK &&
//                data != null && data.getData() != null) {
//            Uri uri = data.getData();
//            try {
//                secondBitmap = MediaStore.Images.Media.getBitmap(
//                        getContentResolver(), uri);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }


        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            //Bundle extras = data.getExtras();
            try {
                photoToSearch = MediaStore.Images.Media.getBitmap(
                        getContentResolver(), photoURI);

//                Thread networkThread = new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        blobStorageService.getRoomBitmap(this, storageServiceCallback, )
//                    }
//                });
//                networkThread.start();

                while (roomBitmap == null) ;
                performFeatureMatch();

            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_matcher);
        browseButton = findViewById(R.id.browseButton);
        confirmButton = findViewById(R.id.confirmButton);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (matchingOperationDone) {
                    if (photosAreMatching) {
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("result", photosAreMatching);
                        setResult(Activity.RESULT_OK, returnIntent);
                        finish();
                    } else
                        finish();
                }
            }
        });
        cameraButton = findViewById(R.id.cameraButton);
        resView = findViewById(R.id.resView);
        resultTextView = findViewById(R.id.matchrestext);
        Intent intent = getIntent();
        browseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentFirst = new Intent(Intent.ACTION_GET_CONTENT);
                intentFirst.setType("image/*");

                startActivityForResult(Intent.createChooser(
                        intentFirst, "Select Picture"), PICK_IMAGE_FIRST);

//                Intent intentSecond = new Intent(Intent.ACTION_GET_CONTENT);
//                intentSecond.setType("image/*");
//                startActivityForResult(Intent.createChooser(
//                        intentSecond, "Select Picture"), PICK_IMAGE_SECOND);
            }
        });

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (MatchActivity.this.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
                    } else {
                        dispatchTakePictureIntent();
                    }
                } else {
                    dispatchTakePictureIntent();
                }
            }
        });


        Thread networkThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    blobStorageService.getRoomBitmap(MatchActivity.this, storageServiceCallback, intent.getStringExtra("roomName"));
                } catch (InvalidObjectException e) {
                    e.printStackTrace();
                }
            }
        });
        networkThread.start();

    }

    private Uri getResourceUriFromId(int id) {
        Resources resources = getResources();
        return (new Uri.Builder())
                .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                .authority(resources.getResourcePackageName(id))
                .appendPath(resources.getResourceTypeName(id))
                .appendPath(resources.getResourceEntryName(id))
                .build();
    }

    private Bitmap getBitmapFromUri(Uri uri) {
        InputStream stream = null;

        try {
            stream = getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
        bmpFactoryOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;

        return BitmapFactory.decodeStream(stream, null, bmpFactoryOptions);
    }


    public void performFeatureMatch() {
        Bitmap bmp2 = roomBitmap;
        Bitmap bmp1 = photoToSearch;

        //Bitmap bmp1 = getBitmapFromUri(getResourceUriFromId(R.drawable.graf1));
        //Bitmap bmp2 = getBitmapFromUri(getResourceUriFromId(R.drawable.graf3));
        //Bitmap bmp1 = getBitmapFromUri(getResourceUriFromId(R.drawable.asmal));
        //Bitmap bmp2 = getBitmapFromUri(getResourceUriFromId(R.drawable.bsmal));
        Mat scaledimg1 = new Mat();
        Mat scaledimg2 = new Mat();
        Mat inimg1 = new Mat();
        Mat inimg2 = new Mat();
        Mat img1 = new Mat();
        Mat img2 = new Mat();
        Utils.bitmapToMat(bmp1, inimg1);
        Utils.bitmapToMat(bmp2, inimg2);
        double sizer1 = (double) inimg1.cols() / inimg1.rows();
        double sizer2 = (double) inimg2.cols() / inimg2.rows();
        Size scaleSize = new Size(800, 800 / sizer1);
        Imgproc.resize(inimg1, scaledimg1, scaleSize, 0, 0, Imgproc.INTER_AREA);
        scaleSize = new Size(800, 800 / sizer2);
        Imgproc.resize(inimg2, scaledimg2, scaleSize, 0, 0, Imgproc.INTER_AREA);
        //img1.release();
        //img2.release();
        Imgproc.cvtColor(scaledimg1, img1, Imgproc.COLOR_RGBA2GRAY);
        Imgproc.cvtColor(scaledimg2, img2, Imgproc.COLOR_RGBA2GRAY);
        //img1 = scaledimg1;
        //img2 = scaledimg2;
        //inimg1.release();
        //inimg2.release();
        if (img1.empty() || img2.empty()) {
            System.err.println("Cannot read images!");
            System.exit(0);
        }

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder;
        Document document;
        Mat homography = new Mat(3, 3, CvType.CV_64F);
        double[] homographyData = new double[(int) (homography.total() * homography.channels())];
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            document = documentBuilder.parse(getResources().openRawResource(R.raw.h1to3p));
            String homographyStr = document.getElementsByTagName("data").item(0).getTextContent();
            String[] splited = homographyStr.split("\\s+");
            int idx = 0;
            for (String s : splited) {
                if (!s.isEmpty()) {
                    homographyData[idx] = Double.parseDouble(s);
                    idx++;
                }
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            System.exit(0);
        } catch (SAXException e) {
            e.printStackTrace();
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
        homography.put(0, 0, homographyData);


        //! [AKAZE]
        AKAZE extractor = AKAZE.create();
        //ORB extractor = ORB.create();

        MatOfKeyPoint kpts1 = new MatOfKeyPoint(), kpts2 = new MatOfKeyPoint();
        Mat desc1 = new Mat(), desc2 = new Mat();
        extractor.detectAndCompute(img1, new Mat(), kpts1, desc1);
        extractor.detectAndCompute(img2, new Mat(), kpts2, desc2);
        //! [AKAZE]

        //! [2-nn matching]
        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE);
        List<MatOfDMatch> knnMatches = new ArrayList<>();
        matcher.knnMatch(desc1, desc2, knnMatches, 2);
        //! [2-nn matching]

        //! [ratio test filtering]
        float ratioThreshold = 0.8f; // Nearest neighbor matching ratio
        List<KeyPoint> listOfMatched1 = new ArrayList<>();
        List<KeyPoint> listOfMatched2 = new ArrayList<>();
        List<KeyPoint> listOfKeypoints1 = kpts1.toList();
        List<KeyPoint> listOfKeypoints2 = kpts2.toList();
        for (int i = 0; i < knnMatches.size(); i++) {
            DMatch[] matches = knnMatches.get(i).toArray();
            float dist1 = matches[0].distance;
            float dist2 = matches[1].distance;
            if (dist1 < ratioThreshold * dist2) {
                listOfMatched1.add(listOfKeypoints1.get(matches[0].queryIdx));
                listOfMatched2.add(listOfKeypoints2.get(matches[0].trainIdx));
            }
        }
        //! [ratio test filtering]


        //=====================Finding Homography===================
        List<KeyPoint> listOfInliers1 = new ArrayList<>();
        List<KeyPoint> listOfInliers2 = new ArrayList<>();
        List<DMatch> listOfGoodMatches = new ArrayList<>();

        if (listOfMatched1.size() >= 10) { //enough matches to calculate homography

            ArrayList<Point> firstMatchedPointList = new ArrayList<>();
            ArrayList<Point> secondMatchedPointList = new ArrayList<>();

            MatOfPoint2f firstMatofPoint = new MatOfPoint2f();
            MatOfPoint2f secondMatofPoint = new MatOfPoint2f();

            for (int i = 0; i < listOfMatched1.size(); i++) {
                firstMatchedPointList.add(listOfMatched1.get(i).pt);
            }
            for (int i = 0; i < listOfMatched2.size(); i++) {
                secondMatchedPointList.add(listOfMatched2.get(i).pt);
            }

            firstMatofPoint.fromList(firstMatchedPointList);
            secondMatofPoint.fromList(secondMatchedPointList);

            homography = Calib3d.findHomography(firstMatofPoint, secondMatofPoint, Calib3d.RANSAC);
            //    Log.i(TAG, "runakaze: homographydump\n" + homography.dump());

            //! [homography check]
            double inlierThreshold = 2.5; // Distance threshold to identify inliers with homography check
            listOfInliers1 = new ArrayList<>();
            listOfInliers2 = new ArrayList<>();
            listOfGoodMatches = new ArrayList<>();
            for (int i = 0; i < listOfMatched1.size(); i++) {
                Mat col = new Mat(3, 1, CvType.CV_64F);
                double[] colData = new double[(int) (col.total() * col.channels())];
                colData[0] = listOfMatched1.get(i).pt.x;
                colData[1] = listOfMatched1.get(i).pt.y;
                colData[2] = 1.0;
                col.put(0, 0, colData);

                Mat colRes = new Mat();
                Core.gemm(homography, col, 1.0, new Mat(), 0.0, colRes);
                colRes.get(0, 0, colData);
                Core.multiply(colRes, new Scalar(1.0 / colData[2]), col);
                col.get(0, 0, colData);

                double dist = Math.sqrt(Math.pow(colData[0] - listOfMatched2.get(i).pt.x, 2) +
                        Math.pow(colData[1] - listOfMatched2.get(i).pt.y, 2));

                if (dist < inlierThreshold) {
                    listOfGoodMatches.add(new DMatch(listOfInliers1.size(), listOfInliers2.size(), 0));
                    listOfInliers1.add(listOfMatched1.get(i));
                    listOfInliers2.add(listOfMatched2.get(i));
                }
            }
            //! [homography check]

        }


        //! [draw final matches]
        Mat res = new Mat();
        MatOfKeyPoint inliers1 = new MatOfKeyPoint(listOfInliers1.toArray(new KeyPoint[listOfInliers1.size()]));
        MatOfKeyPoint inliers2 = new MatOfKeyPoint(listOfInliers2.toArray(new KeyPoint[listOfInliers2.size()]));
        MatOfDMatch goodMatches = new MatOfDMatch(listOfGoodMatches.toArray(new DMatch[listOfGoodMatches.size()]));
        Features2d.drawMatches(img1, inliers1, img2, inliers2, goodMatches, res);
        //Imgcodecs.imwrite("akaze_result.png", res);
        double inlierRatio = 0.0;
        if (listOfMatched1.size() > 0)
            inlierRatio = listOfInliers1.size() / (double) listOfMatched1.size();
        if (inlierRatio >= 0.50) {
            photosAreMatching = true;
            String msg = String.format("Pictures are matching %.2f%%", inlierRatio * 100);
            resultTextView.setText(msg);
        } else {
            photosAreMatching = false;
            resultTextView.setText("Couldn't find a match");
        }
        Log.i(TAG, "A-KAZE Matching Results");
        Log.i(TAG, "*******************************");
        Log.i(TAG, "# Keypoints 1:                        \t" + listOfKeypoints1.size());
        Log.i(TAG, "# Keypoints 2:                        \t" + listOfKeypoints2.size());
        Log.i(TAG, "# Matches:                            \t" + listOfMatched1.size());
        Log.i(TAG, "# Inliers:                            \t" + listOfInliers1.size());
        Log.i(TAG, "# Inliers Ratio:                      \t" + inlierRatio);


        Bitmap outputBitmap = Bitmap.createBitmap(res.cols(), res.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(res, outputBitmap);
        resView.setImageBitmap(outputBitmap);
        matchingOperationDone = true;

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
