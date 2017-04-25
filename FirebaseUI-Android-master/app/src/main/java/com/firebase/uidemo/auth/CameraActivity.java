package com.firebase.uidemo.auth;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import android.Manifest;
import android.widget.Toast;

import com.firebase.uidemo.R;
import com.firebase.uidemo.storage.ImageActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import butterknife.OnClick;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

/**
 * Created by JordanBurton on 4/19/17.
 */

public class CameraActivity extends AppCompatActivity  {
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    //private static Uri mFileUri;
    //private static final String TAG = "CameraActivity";

    private Camera mCamera;
    private CameraPreview mPreview;
    private String TAG = "CameraActivity";
    private Uri mFileUri;
    private StorageReference mImageRef;
    private DatabaseReference mRef;
    Button captureButton;
    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null){
                Log.d(TAG, "Error creating media file, check storage permissions: ");
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
        }
    };

    public void createDialog(){
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
        View mRestoreView = getLayoutInflater().inflate(R.layout.popup_image_option, null);
        Button confirmation = (Button) mRestoreView.findViewById(R.id.confirm);
        Button retake = (Button) mRestoreView.findViewById(R.id.retake_pic);

        mBuilder.setView(mRestoreView);
        final AlertDialog pickOption = mBuilder.create();
        pickOption.setCanceledOnTouchOutside(false);

        pickOption.show();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        mRef = FirebaseDatabase.getInstance().getReference();
        captureButton = (Button) findViewById(R.id.button_capture);
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA);
        if(permissionCheck != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        }
        else{
            mCamera = getCameraInstance();

            // Create our Preview view and set it as the content of our activity.
            mPreview = new CameraPreview(this, mCamera);
            FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
            preview.addView(mPreview);
        }


        // Create an instance of Camera

    }


    //@OnClick(R.id.button_capture)
    public void capturePicture(View v){
        Log.d("CameraActivity", "Taking picture");
        mCamera.takePicture(null, null, mPicture);
        captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // get an image from the camera
                        return;
                    }
                }
        );
        //getOutputMediaFile(MEDIA_TYPE_IMAGE);
        createDialog();
        Log.d("CameraActivity", "Finished taking picture");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    mCamera = getCameraInstance();

                    // Create our Preview view and set it as the content of our activity.
                    mPreview = new CameraPreview(this, mCamera);
                    FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
                    preview.addView(mPreview);

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    captureButton.setOnClickListener(new View.OnClickListener(){

                        @Override
                        public void onClick(View v) {

                        }
                    });

                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    /** A safe way to get an instance of the Camera object. */
    private void releaseCameraAndPreview() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    public Camera getCameraInstance(){
        Camera c = null;
        try {
            releaseCameraAndPreview();
            c = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
            Log.d("CameraActivity", "CAMERA IS NULL PLS FIX");
            e.printStackTrace();
        }
        return c; // returns null if camera is unavailable
    }

    @Override
    protected void onPause() {

        // release the camera immediately on pause event
        super.onPause();
        releaseCamera();

        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.removeView(mPreview);
    }

    @Override
    public void onResume(){
        super.onResume();

        if (mCamera == null)
        {
            mCamera = getCameraInstance();
            // three new lines, creating a new CameraPreview, then adding it to the FrameLayout
            mPreview = new CameraPreview(this, mCamera);
            FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
            preview.addView(mPreview);
        }
    }

    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }



    /** Create a file Uri for saving an image or video */
    private Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    private File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        mFileUri = null;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
            mFileUri = Uri.fromFile(mediaFile);
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }



    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState){
        setContentView(R.layout.activity_camera);
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA);
        if(permissionCheck != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        }
        else{
            mCamera = getCameraInstance();

            // Create our Preview view and set it as the content of our activity.
            mPreview = new CameraPreview(this, mCamera);
            FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
            preview.addView(mPreview);
        }
    }

    @Override
    public void onDestroy(){
        releaseCamera();
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        releaseCamera();
        super.onSaveInstanceState(savedInstanceState);
    }

    public void reload(View view) {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    public void uploadPhoto(View view) {
        String uuid = UUID.randomUUID().toString();
        mImageRef = FirebaseStorage.getInstance().getReference(uuid);
        mImageRef.putFile(mFileUri).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //Gson gson = new Gson();
                //Log.d(TAG, "" + taskSnapshot.getDownloadUrl());
                Uri mImage = taskSnapshot.getDownloadUrl();
                //Log.d(TAG, "This is the json: " + json );
                //Log.d(TAG, "From the json: " + gson.fromJson(json, String.class));
                DatabaseReference mUser = mRef.child("users");
                String mUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                mUser.child(mUid).child("image").setValue(mImage.toString());
            }
        });
        //finish();
    }
}