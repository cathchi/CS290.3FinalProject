package com.firebase.uidemo.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import java.io.IOException;

import static android.content.Context.WINDOW_SERVICE;
import static io.fabric.sdk.android.Fabric.TAG;

/**
 * Created by JordanBurton on 4/19/17.
 */

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private Context mContext;
    public static final String ROTATION = "ROTATION";
    private String TAG = "CameraPreview";

    public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;
        mContext = context;
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.setDisplayOrientation(90);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null){
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        Camera.Parameters parameters = mCamera.getParameters();
        Display display = ((WindowManager)getContext().getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        if(display.getRotation() == Surface.ROTATION_0)
        {
            prefsEditor.remove(ROTATION);
            parameters.setPreviewSize(h, w);
            mCamera.setDisplayOrientation(90);
            prefsEditor.putInt(ROTATION, 270);
        }

        if(display.getRotation() == Surface.ROTATION_90)
        {
            prefsEditor.remove(ROTATION);
            parameters.setPreviewSize(w, h);
            mCamera.setDisplayOrientation(0);
            Log.d(TAG, "90 degree rotation");
            prefsEditor.putInt(ROTATION, 0);
        }

        if(display.getRotation() == Surface.ROTATION_180)
        {
            parameters.setPreviewSize(h, w);
            prefsEditor.remove(ROTATION);
        }

        if(display.getRotation() == Surface.ROTATION_270)
        {
            prefsEditor.remove(ROTATION);
            parameters.setPreviewSize(w, h);
            mCamera.setDisplayOrientation(180);
            Log.d(TAG, "270 degree rotation");
            prefsEditor.putInt(ROTATION, 180);
        }

        //mCamera.setParameters(parameters);
        prefsEditor.commit();
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

        } catch (Exception e){
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }
}
