package com.app.maskit_app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import pl.droidsonroids.gif.GifImageView;
import utils.BitmapUtils;
import utils.ExifUtil;

import com.elconfidencial.bubbleshowcase.*;

public class MainActivity extends AppCompatActivity {

    public static final String SWITCH_BTN_INSTRUCTION_KEY = "SWITCH_BTN_INSTRUCTION_KEY";
    private static final String PREFS_NAME = "PREF";
    private static String IMAGE_NAME = "MaskitLogo.png";
    Menu topMenu;
    public static String filePath = "";
    private Toolbar toolbar;
    public static final int SELECT_GALLERY_IMAGE = 101;
    private final int _requestCodeCamera = 99; // intent code when finished take picture from camera

    public static final int EDIT_FINAL_IMAGE = 0;
    public static final int EDIT_FILTERED_IMAGE = 1;

    // change the UI when its the first time app been used
    boolean firstTime = false;

    // represent whether the switch button to show instruction upon launch in on or off
    boolean showInstruction;

    RecyclerView recyclerView;

    SubsamplingScaleImageView imagePreview;
    FilterAdapter adapter;
    Integer currentFilter;
    Bootstrap strap;

    // loading icon (gif)
    GifImageView loadingView;

    boolean flag=false;

    Boolean landscape = false;

    FaceDetector detector;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // so when keyboard appear (when searching for filter for example) the layout won't change
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        // imageView of the loading gif
        loadingView = (GifImageView) findViewById(R.id.loading_gif_view);

        setContentView(R.layout.activity_main);

        strap = (Bootstrap) getApplicationContext();
        imagePreview = (SubsamplingScaleImageView) findViewById(R.id.photo_preview);

        // Restore preferences
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        showInstruction = settings.getBoolean(SWITCH_BTN_INSTRUCTION_KEY, true);

        if (savedInstanceState != null){
            flag=true;
            filePath = savedInstanceState.getString("imagePath");
        }
        else{
            firstTime = true;
            hideForStartScreen();
        }

        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // In landscape

            landscape = true;
            fullScreenSettings();
            if (strap.LoadLandscapePath.equals("")) {
                final Bitmap bit = BitmapUtils.getBitmapFromAssets(this, IMAGE_NAME, 300, 300);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        imagePreview.setImage(ImageSource.bitmap(bit.copy(Bitmap.Config.ARGB_8888, true)));
                    }
                }, 500);
            }
            else{
                Uri uri = strap.SavedUri;
                try {
                    final Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            imagePreview.setImage(ImageSource.bitmap(bitmap.copy(Bitmap.Config.ARGB_8888,true)));
                        }
                    }, 500);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        } else {
            // In portrait
            createBar();
            loadRecycler();
            loadImage(flag);
            imagePreview.setZoomEnabled(false);
            if (strap.currentFilterNo != null){
                currentFilter = strap.currentFilterNo;
            }
            else {
                currentFilter = Filter.NORMAL;
            }
            if(!firstTime) {
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startFaceDetection();
                    }
                }, 1000);
            }
            adapter.setOnTaskClickCallback(new OnFilterClickListener() {
                @Override
                public void onTaskClicked(Filter filter) {
                    currentFilter = filter.filter;
                }
            });
        }

        if (savedInstanceState == null){
            hideForStartScreen();
        }

        if(showInstruction && firstTime) {
            new BubbleShowCaseBuilder(this) //Activity instance
                    .title("Welcome To MASKiT app\n" +
                            "1) Load image from gallery \\ Take photo\n" +
                            "2) Select the desired filter\n" +
                            "3) Click one of the faces in the red squares\n" +
                            "4) Switch to Landscape mode to see the altered image before saving")
                    .targetView(imagePreview)
                    .titleTextSize(15)//View to point out
                    .show(); //Display the ShowCase
        }

        SwitchMaterial switchBtnInstruction = (SwitchMaterial) findViewById(R.id.switchInstruction);
        // show the instruction button state according to previous user interaction
        switchBtnInstruction.setChecked(showInstruction);
        switchBtnInstruction.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
//                    Snackbar snackbar = Snackbar
//                            .make(imagePreview, "On", Snackbar.LENGTH_LONG);
//                    snackbar.show();
                    showInstruction = true;
                } else {
                    // The toggle is disabled
//                    Snackbar snackbar = Snackbar
//                            .make(imagePreview, "Off", Snackbar.LENGTH_LONG);
//                    snackbar.show();
                    showInstruction = false;
                }
            }
        });


    }



    private void startFaceDetection() {

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        if (strap.filters.size() > 0 && strap._faces.size() > 0){
            editPicture(EDIT_FILTERED_IMAGE);
            setListener();
        }
        else {
            setFaces();
        }
    }


    private void setFaces() {
        loadingView = (GifImageView) findViewById(R.id.loading_gif_view);
        loadingView.setVisibility(View.VISIBLE);
        FaceDetectorOptions highAccuracyOpts =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
                        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
                        .build();
        detector = FaceDetection.getClient();
        imagePreview.buildDrawingCache();
        Bitmap bmap = imagePreview.getDrawingCache().copy(Bitmap.Config.ARGB_8888,true);
        strap.filteredImage1 = bmap.copy(Bitmap.Config.ARGB_8888,true);
        strap.filteredImage = bmap.copy(Bitmap.Config.ARGB_8888,true);
        final InputImage image = InputImage.fromBitmap(bmap, 0);
        Task<List<Face>> result =
                detector.process(image)
                        .addOnSuccessListener(
                                new OnSuccessListener<List<Face>>() {
                                    @SuppressLint("ClickableViewAccessibility")
                                    @Override
                                    public void onSuccess(final List<Face> faces) {
                                        for (Face face : faces){
                                            Rect rect = face.getBoundingBox();
                                            strap._faces.add(new TupleFace<Rect, Float>(rect, face.getHeadEulerAngleY()));
                                        }
                                        drawRectsAroundFaces();
                                        loadingView.setVisibility(View.GONE);
                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        // ...
                                        loadingView.setVisibility(View.GONE);
                                    }
                                });

    }

    private void  drawRectsAroundFaces(){
        // Initialize a new Paint instance to draw the Rectangle
        Paint paint = getRedRect();
        Canvas canvas = new Canvas(strap.filteredImage);

        for (TupleFace<Rect, Float> _face : strap._faces) {
            Rect bounds = _face.getLocation();
            canvas.drawRect(bounds, paint);
        }
        imagePreview.setImage(ImageSource.bitmap(strap.filteredImage));
        strap.filteredImage = strap.filteredImage.copy(Bitmap.Config.ARGB_8888, true);

        setListener();
    }

    private Paint getRedRect() {
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.RED);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(10);
        return paint;
    }

    @SuppressLint("ClickableViewAccessibility")
    private synchronized void setListener(){
        imagePreview.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // keeping the normal behavior (so we can zoom the image) of click listener
                v.onTouchEvent(event);
                // custom behavior, we click on head adding the right filter
                if(strap._faces != null && strap._faces.size() != 0){
                    Canvas canvas = new Canvas(strap.filteredImage);
                    if (event.getAction() == MotionEvent.ACTION_DOWN){
                        int x = (int) (event.getX());
                        int y = (int) (event.getY());
                        Bitmap blurredImage = BlurBuilder.blur(MainActivity.this, strap.filteredImage1).copy(Bitmap.Config.ARGB_8888, true);
                        for (TupleFace<Rect, Float> _face : strap._faces){
                            Rect bounds = _face.getLocation();
                            Float faceAngle = _face.getFaceAngle();
                            if(bounds.contains(x, y)){
                                Triplet<Rect, Integer, Canvas> current = null;
                                if (containInFilter(bounds)){
                                    current = getFilter(bounds);
                                    if(currentFilter == Filter.BLUR && current.second == Filter.BLUR){
                                        break;
                                    }
                                    strap.filters.remove(current);
                                }
                                // if pushed face with no filter yet
                                else{
                                    if (currentFilter == Filter.NORMAL){
                                        return true;
                                    }
                                }
                                // calling normal filter first, to remove any filter that was applied before
                                pasteOnImage(bounds, canvas, strap.filteredImage1.copy(
                                        Bitmap.Config.ARGB_8888, true));
                                switch (currentFilter){
                                    case(Filter.NORMAL):
                                        // already did it.
                                        break;
                                    case (Filter.BLUR):
                                        // blur filter
                                        pasteOnImage(bounds, canvas, blurredImage);
                                        strap.filters.add(new Triplet<Rect, Integer, Canvas>(bounds,
                                                currentFilter, canvas));
                                        break;
                                    case (Filter.BlackCube):
                                        Paint paint = new Paint();
                                        paint.setStyle(Paint.Style.FILL);
                                        paint.setColor(Color.BLACK);
                                        paint.setAntiAlias(true);
                                        paint.setStrokeWidth(10);
                                        canvas.drawRect(bounds, paint);
                                        strap.filters.add(new Triplet<Rect, Integer, Canvas>(bounds,
                                                currentFilter, canvas));
                                        break;
                                    case (Filter.COVID):
                                        String pathOfMaskAccordingToAngle = "maskRight.png";
                                        int tmp;
                                        int width = 1200;
                                        int height = 1200;
                                        if(faceAngle < -12){
                                            // left
                                            tmp = Filter.COVID_LEFT;
                                            pathOfMaskAccordingToAngle = "maskLeft.png";
                                        }
                                        else if(faceAngle > -12 && faceAngle < 12){
                                            // middle
                                            tmp = Filter.COVID_MIDDLE;
                                            width = 984;
                                            pathOfMaskAccordingToAngle = "maskMiddle.png";
                                        }
                                        else{
                                            // right
                                            tmp = Filter.COVID_RIGHT;
                                            pathOfMaskAccordingToAngle = "maskRight1.png";
                                        }
                                        Bitmap BitmapRescaledEmoji =  BitmapUtils
                                                .getResizedBitmap(Objects.requireNonNull(BitmapUtils.
                                                                getBitmapFromAssets(MainActivity.this,
                                                                        pathOfMaskAccordingToAngle,
                                                                        width,
                                                                        height)),
                                                        bounds.width(),
                                                        bounds.height()*2-
                                                                (int)Math.floor((float)bounds.height()/2)
                                                                - (int)Math.floor((float)bounds.height()/5));
                                        pasteEmojiOnFace(bounds, canvas, BitmapRescaledEmoji);
                                        strap.filters.add(new Triplet<Rect, Integer, Canvas>(bounds,
                                                tmp, canvas));
                                        break;

                                    default:
                                        // get emoji rescaled to the size of the rectangle around the face
                                        Bitmap rescaledBitmapEmoji = Filter.getFilterIconResized(
                                                currentFilter, bounds, MainActivity.this);
                                        pasteEmojiOnFace(bounds, canvas, rescaledBitmapEmoji);
                                        strap.filters.add(new Triplet<Rect, Integer, Canvas>(bounds,
                                                currentFilter, canvas));
                                        break;

                                }
                                imagePreview.setImage(ImageSource.bitmap(strap.filteredImage
                                        .copy(Bitmap.Config.ARGB_8888, true)));
                            }
                        }
                    }
                }
                return true;
            }
        });
    }


    /**
     * @param bounds - rectangle around face
     * @param canvas - canvas of all whole image
     * @param rescaledEmoji -  emoji in the right size to put on the rectangle around face (bounds rect)
     */
    private void pasteEmojiOnFace(Rect bounds, Canvas canvas, Bitmap rescaledEmoji) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        // in order to cut from the first arg in drawBitmap and paste on canvas
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
        Rect src = new Rect(0, 0, bounds.width(), bounds.height());
        canvas.drawBitmap(rescaledEmoji, src, bounds, paint);
        rescaledEmoji.recycle();
        canvas.save();
    }


    private Triplet getFilter(Rect bounds) {
        for (Triplet<Rect, Integer, Canvas> p : strap.filters){
            if (p.first.equals(bounds)){
                return p;
            }
        }
        return null;
    }

    private boolean containInFilter(Rect bounds) {
        for (Triplet<Rect, Integer, Canvas> p : strap.filters){
            if (p.first.equals(bounds)){
                return true;
            }
        }
        return false;
    }

    private void loadRecycler() {
        adapter = new FilterAdapter();
        adapter.setFilters();
        adapter.setCurrentFilter(strap.currentFilterNo);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        recyclerView.setAdapter(adapter);
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
    }

    private void fullScreenSettings(){
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        imagePreview.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
        imagePreview.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
//        imagePreview.setAdjustViewBounds(false);
        imagePreview.setBackgroundColor(Color.BLACK);

        // make bottom app bar disappear in landscape mode
        findViewById(R.id.coordinatorLayout).setVisibility(View.GONE);

    }

    // load the default image from assets on app launch
    private void loadImage(boolean flag) {
        if (flag && !filePath.equals("")){
            strap.originalImage = BitmapFactory.decodeFile(filePath);
            strap.originalImage = ExifUtil.rotateBitmap(filePath, strap.originalImage);
        }
        else{
            strap.originalImage = BitmapUtils.getBitmapFromAssets(this, IMAGE_NAME, 300, 300);
        }
        strap.filteredImage = strap.originalImage.copy(Bitmap.Config.ARGB_8888, true);
        strap.finalImage = strap.originalImage.copy(Bitmap.Config.ARGB_8888, true);
        imagePreview.setImage(ImageSource.bitmap(strap.originalImage));

    }

    private void createBar(){
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        topMenu = menu;
        getMenuInflater().inflate(R.menu.top_menu, menu);
        if(firstTime){
            menu.findItem(R.id.action_search).setVisible(false);
            menu.findItem(R.id.share_btn).setVisible(false);
            menu.findItem(R.id.action_save).setVisible(false);
            firstTime = false;
        }
        // Search icon (search through fillers by name)
        final MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) item.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // every time the user typed a character we show the filters that correspond to the
                // query so far therefore we do nothing in this function, and everything in the next function
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                ArrayList<Filter> filteredByQueryArr= (ArrayList<Filter>) adapter.getAllFiltersArr();
                if(!newText.isEmpty()){
                    ArrayList<Filter> filteredByQueryArray = new ArrayList<>();
                    adapter.clearFilters();
                    for(Filter item : filteredByQueryArr){
                        if(item.description.toLowerCase().contains(newText.toLowerCase())){
                            filteredByQueryArray.add(item);
                        }
                    }
                    adapter.setFilter(filteredByQueryArray);
                }
                else{
                    adapter.clearFilters();
                    adapter.setFilter(filteredByQueryArr);
                }
                adapter.notifyDataSetChanged();

                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("imagePath", filePath);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // saving instruction button state (the switch button of whether to show instruction upon launch)
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(SWITCH_BTN_INSTRUCTION_KEY, showInstruction);

        // Commit the edits!
        editor.apply();
    }

    public void onDestroy() {
        super.onDestroy();
        if (!strap.loadimage){
            return;
        }
        else {
            if (!landscape){
                saveOutputImage("temp");
                strap.currentFilterNo = adapter.getCurrentFilter();
            }
            if(detector != null){
                detector.close();
                detector = null;
            }
            // clearing everything
            if (strap.originalImage != null){
                strap.originalImage.recycle();
                strap.originalImage = null;
            }
            if (strap.filteredImage != null) {
                strap.filteredImage.recycle();
                strap.filteredImage = null;
            }

            if( strap.blurredImage != null) {
                strap.blurredImage.recycle();
                strap.blurredImage = null;
            }
            if (strap.finalImage != null) {
                strap.finalImage.recycle();
                strap.finalImage = null;
            }
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_open) {
            openImageFromGallery();
            return true;
        }

        if (id == R.id.action_save) {
            saveOutputImage("MASKiT");
            return true;
        }

        if (id == R.id.about){
            startActivity(new Intent(MainActivity.this, pop.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveOutputImage(String Name) {
        editPicture(EDIT_FINAL_IMAGE);
        strap.finalImage = BitmapUtils.CropBitmapTransparency(strap.finalImage);
        saveImageToGallery(Name);
    }

    private void editPicture(int indicator) {
        imagePreview.buildDrawingCache();
        Bitmap bit = imagePreview.getDrawingCache().copy(Bitmap.Config.ARGB_8888,true);
        Canvas canvas = new Canvas(bit);
        Bitmap blurredImage = BlurBuilder.blur(MainActivity.this,
                bit.copy(Bitmap.Config.ARGB_8888, true));
        if (indicator == EDIT_FILTERED_IMAGE){
            Paint paint = getRedRect();
            for (TupleFace<Rect, Float> _face : strap._faces){
                canvas.drawRect(_face.getLocation(), paint);
            }
        }
        for (Triplet<Rect, Integer, Canvas> p : strap.filters){
            switch (p.second){
                case(Filter.BLUR):
                    pasteOnImage(p.first , canvas, blurredImage);
                    break;
                case(Filter.BlackCube):
                    Paint paint = new Paint();
                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(Color.BLACK);
                    paint.setAntiAlias(true);
                    paint.setStrokeWidth(10);
                    canvas.drawRect(p.first, paint);
                    break;
                default:
                    Bitmap rescaledEmoji = Filter.getFilterIconResized(p.second, p.first, MainActivity.this);
                    pasteEmojiOnFace(p.first, canvas, rescaledEmoji);
                    break;

            }
        }
        if (indicator == EDIT_FINAL_IMAGE){
            strap.finalImage = bit.copy(Bitmap.Config.ARGB_8888,true);

        }
        else{
            Log.d("bla", "bla");
            strap.filteredImage = bit.copy(Bitmap.Config.ARGB_8888,true);
            imagePreview.setImage(ImageSource.bitmap(strap.filteredImage.copy(Bitmap.Config.ARGB_8888,true)));
        }
    }

    /**
     * replace bounds area from image into canvas
     * @param bounds location to replace in the image (canvas)
     * @param imageToPasteTo - canvas of the image we want to paste into
     * @param imageToPasteFrom - image to cut from the bounds area and paste on bounds area on canvas
     */
    private void pasteOnImage(Rect bounds, Canvas imageToPasteTo, Bitmap imageToPasteFrom){
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        // in order to cut from the first arg in drawBitmap and paste on canvas
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
        imageToPasteTo.drawBitmap(imageToPasteFrom, bounds, bounds, paint);
        imageToPasteTo.save();
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }


    private void openImageFromGallery() {
        strap.loadimage = true;
        Dexter.withActivity(this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            Intent intent = new Intent(Intent.ACTION_PICK);
                            intent.setType("image/*");
                            startActivityForResult(intent, SELECT_GALLERY_IMAGE);
                        } else {
                            Snackbar snackbar = Snackbar
                                    .make(imagePreview, "Need this permission in order to take image from gallery!", Snackbar.LENGTH_LONG);
                            // change background color and text color to fit the new design
                            snackbar.setBackgroundTint(ContextCompat.getColor(
                                    MainActivity.this,
                                    R.color.dark_blue_gray));
                            snackbar.setTextColor(ContextCompat.getColor(
                                    MainActivity.this,
                                    R.color.white));
                            snackbar.show();
                        }

                    }
                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions,
                                                                   PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    /*
     * saves image to camera gallery
     * */
    private void saveImageToGallery(final String folderName) {
        Dexter.withActivity(this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            final Uri u = SaveImageKt.saveImage(strap.finalImage, MainActivity.this, folderName);
                            strap.SavedUri = u;
                            final String path = ImageFilePath.getPath(MainActivity.this, u);
                            strap.LoadLandscapePath = path;
                            if (!TextUtils.isEmpty(path)) {
                                Snackbar snackbar = Snackbar
                                        .make(imagePreview, "Image saved to gallery!",
                                                Snackbar.LENGTH_LONG)
                                        .setAction("OPEN", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                openImage(path);
                                            }
                                        });
                                // change background color and text color to fit the new design
                                snackbar.setBackgroundTint(ContextCompat.getColor(
                                        MainActivity.this,
                                        R.color.dark_blue_gray));
                                snackbar.setTextColor(ContextCompat.getColor(
                                        MainActivity.this,
                                        R.color.white));
                                snackbar.setActionTextColor(getResources().getColor(R.color.white));

                                snackbar.show();
                            } else {
                                Log.d("BAD", "BAD");
                                Snackbar snackbar = Snackbar
                                        .make(imagePreview, "Unable to save image!", Snackbar.LENGTH_LONG);

                                // change background color and text color to fit the new design
                                snackbar.setBackgroundTint(ContextCompat.getColor(
                                        MainActivity.this,
                                        R.color.dark_blue_gray));
                                snackbar.setTextColor(ContextCompat.getColor(
                                        MainActivity.this,
                                        R.color.white));

                                snackbar.show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Permissions are not granted!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions,
                                                                   PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();

    }

    // opening image in default image viewer app
    private void openImage(String path) {
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(path)), "image/*");
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap bitmap = null;
        if (!((resultCode == RESULT_OK && requestCode == SELECT_GALLERY_IMAGE) ||
                (requestCode == _requestCodeCamera && resultCode == RESULT_OK))){
            return;
        }
        if (resultCode == RESULT_OK && requestCode == SELECT_GALLERY_IMAGE) {
            bitmap = BitmapUtils.getBitmapFromGallery(this, data.getData(), 800, 800);
        }
        // took picture from camera
        else if (requestCode == _requestCodeCamera && resultCode == RESULT_OK){
            filePath = strap.currPhotoPath ;
            bitmap = BitmapFactory.decodeFile(strap.currPhotoPath );
            bitmap = ExifUtil.rotateBitmap(strap.currPhotoPath , bitmap);
            Log.d("camera", "good " + filePath);
            if (bitmap == null){
                Log.d("is null", "yep yep");
            }

        }
        else {
            Log.d("bad", "bad");
            return;
        }
        // clear  memory
        if(strap.originalImage != null)
            strap.originalImage.recycle();
        if(strap.filteredImage != null)
            strap.filteredImage.recycle();
        if(strap.finalImage != null)
            strap.finalImage.recycle();


        strap.filters.clear();
        strap._faces.clear();


        strap.originalImage = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        strap.filteredImage = strap.originalImage.copy(Bitmap.Config.ARGB_8888, true);
        strap.finalImage = strap.originalImage.copy(Bitmap.Config.ARGB_8888, true);

        imagePreview.setImage(ImageSource.bitmap(strap.originalImage.copy(Bitmap.Config.ARGB_8888, true)));
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            saveImageToGallery("temp");
            recreate();
        }
        else if(!firstTime){
            startFaceDetection();
            // show all of the UI option as the user upload his own image
            finishedStartScreen();
        }

        bitmap.recycle();
    }
//
//    private void deleteTempFolder(String path)  {
//        String folderPath =  path.substring(0, path.lastIndexOf("/"));
//
//
//        Log.d("wow", path);
//        Log.d("wow1", folderPath);
//        folderPath = folderPath.substring(0, folderPath.lastIndexOf("/")+1) + "temp/";
//        Log.d("wow2", folderPath);
//        File dir = new File(folderPath);
//        if (dir.isDirectory()) {
//            try {
//                FileUtils.deleteDirectory(dir);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }

    private void onClickCamera(){
        boolean _permission = checkCameraPermission(MainActivity.this);
        if(_permission) {
            strap.loadimage = true;
            String fileName = "imageToAlter";
            String fileExtension = ".jpg";
            File storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            try {
                File imageFile = File.createTempFile(fileName, fileExtension, storageDirectory);
                strap.currPhotoPath = imageFile.getAbsolutePath();
                strap.LoadLandscapePath = strap.currPhotoPath;
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                Uri imageUri = FileProvider.getUriForFile(MainActivity.this,
                        "com.app.maskit_app.fileprovider", imageFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, _requestCodeCamera);
            } catch (IOException e) {
                Log.e("ErrorTakePhotoBtn", "problem at create a temp file");
                e.printStackTrace();
            }
        }


    }

    private boolean checkCameraPermission(Context context) {
        if (ContextCompat.checkSelfPermission( context, Manifest.permission.CAMERA ) !=
                PackageManager.PERMISSION_GRANTED ){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 3);
        }
        if (ContextCompat.checkSelfPermission( context, Manifest.permission.READ_EXTERNAL_STORAGE )
                != PackageManager.PERMISSION_GRANTED ){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 3);
        }
        if (ContextCompat.checkSelfPermission( context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED ){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 3);
        }
        if (ContextCompat.checkSelfPermission( context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission( context, Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission( context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED){
            return true;
        }
        else{
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for(int i=0; i < grantResults.length; i++){
            if(permissions[i].equals(Manifest.permission.CAMERA) && grantResults[i] != PackageManager.PERMISSION_GRANTED ||
                    permissions[i].equals(Manifest.permission.READ_EXTERNAL_STORAGE) &&
                            grantResults[i] != PackageManager.PERMISSION_GRANTED ||
                    permissions[i].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) &&
                            grantResults[i] != PackageManager.PERMISSION_GRANTED

            ){
                Snackbar snackbar = Snackbar
                        .make(imagePreview, "Cant take image from camera without permission!", Snackbar.LENGTH_LONG);
                // change background color and text color to fit the new design
                snackbar.setBackgroundTint(ContextCompat.getColor(
                        MainActivity.this,
                        R.color.dark_blue_gray));
                snackbar.setTextColor(ContextCompat.getColor(
                        MainActivity.this,
                        R.color.white));
                snackbar.show();
                return;
            }
            onClickCamera();
        }
    }

    /** Taking picture directly from camera */
    public void onClickListenerTakePhotoBtn(MenuItem item) {
        onClickCamera();
    }

    public void onClickShareBtn(MenuItem item) throws IOException {
//        // in order to get Uri of a file outside app storage(there is a better solution, though need to compare performance)
//        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
//        StrictMode.setVmPolicy(builder.build());
        // saving the edit image (without the black bars around)
        editPicture(EDIT_FINAL_IMAGE);
        strap.finalImage = BitmapUtils.CropBitmapTransparency(strap.finalImage);
        Bitmap imageToShare = strap.finalImage.copy(Bitmap.Config.ARGB_8888,true);
        String fileExtension = ".png";
        File storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(getResources().getString(R.string.app_name), fileExtension, storageDirectory);

        Intent shareIntent;
        try {
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            // PNG is lossless, therefor there is no mean to the quality parameter here.
            imageToShare.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();

            shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/*");
            shareIntent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(
                    MainActivity.this,
                    "com.app.maskit_app.fileprovider",
                    imageFile));

            shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(Intent.createChooser(shareIntent, "share image"));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public void onClickGalleryBtn(MenuItem item){
        openImageFromGallery();
    }

    public void onClickSaveBtn(MenuItem item){
        saveOutputImage("MASKiT");
    }

    public void onClickListenerTakePhotoBtn(View view) {
        onClickCamera();
    }

    // show only part of the UI that fit the start screen
    private void hideForStartScreen(){
        // bottom menu
//        findViewById(R.id.share_btn_bottom).setVisibility(View.INVISIBLE);
//        findViewById(R.id.action_save_bottom).setVisibility(View.INVISIBLE);
        findViewById(R.id.recyclerView).setVisibility(View.INVISIBLE);
        findViewById(R.id.switchInstruction).setVisibility(View.VISIBLE);
    }

    // show all of the UI after the user inputted an image
    private void finishedStartScreen(){
//        findViewById(R.id.share_btn_bottom).setVisibility(View.VISIBLE);
//        findViewById(R.id.action_save_bottom).setVisibility(View.VISIBLE);
        findViewById(R.id.switchInstruction).setVisibility(View.GONE);

        // filters (reclyeview)
        findViewById(R.id.recyclerView).setVisibility(View.VISIBLE);

        // showing all top menu options
        topMenu.findItem(R.id.action_search).setVisible(true);
        topMenu.findItem(R.id.share_btn).setVisible(true);
        topMenu.findItem(R.id.action_save).setVisible(true);

        // adding hint text in filter search bar
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) topMenu.findItem(R.id.action_search).getActionView();

        // Assumes current activity is the searchable activity
        SearchableInfo searchableInfo = searchManager.getSearchableInfo(getComponentName());
        searchView.setSearchableInfo(searchableInfo);

        // Override the hint with whatever you like
        searchView.setQueryHint(getResources().getString(R.string.search_hint));

    }

}
