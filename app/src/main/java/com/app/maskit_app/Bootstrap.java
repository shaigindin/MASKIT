package com.app.maskit_app;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

public class Bootstrap extends Application {

    public Uri SavedUri;
    public String temp;
    Bitmap originalImage;
    // to backup image with filter applied
    Bitmap filteredImage;
    Bitmap filteredImage1;
    Bitmap blurredImage;
    // the final image after applying
    // brightness, saturation, contrast
    Bitmap finalImage;
    boolean loadimage;
    String currPhotoPath;

    String LoadLandscapePath ;
    Integer currentFilterNo;

    List<Triplet<Rect, Integer, Canvas>> filters;
    List<TupleFace<Rect, Float>> _faces;

    @Override
    public void onCreate() {
        super.onCreate();
        LoadLandscapePath = "";
        filters = new ArrayList<>();
        _faces = new ArrayList<>();
        loadimage = false;
    };
}
