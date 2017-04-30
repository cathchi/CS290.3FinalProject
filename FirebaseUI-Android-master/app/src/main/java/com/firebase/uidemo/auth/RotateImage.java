package com.firebase.uidemo.auth;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

public class RotateImage extends BitmapTransformation {
    private float rotate = 0f;
    String id;

    public RotateImage(Context context, float rotate, String id) {
        super(context);
        this.rotate = rotate;
        this.id = id;
    }

    @Override
    protected Bitmap transform(BitmapPool pool, Bitmap toTransform,
                               int outWidth, int outHeight) {
        return rotateBitmap(toTransform, rotate);
    }

    @Override
    public String getId() {
        return "com.example.helpers.MyTransformation."+id;
    }

    public static Bitmap rotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }
}
