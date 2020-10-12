package com.app.maskit_app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;

import java.util.Objects;

import utils.BitmapUtils;

public class Filter {

    private static final int DEFAULT_ICON_HEIGHT = 100;
    private static final int DEFAULT_ICON_WIDTH = 100;
    public static final int NORMAL = 0;
    public static final int BLUR = 1;
    public static final int SMILE = 2;
    public static final int SAD = 3;
    public static final int COVID = 4;
    public static final int COVID_LEFT = 29;
    public static final int COVID_MIDDLE = 30;
    public static final int COVID_RIGHT = 31;
    public static final int FaceWithCovidMask = 5;
    // Marvel's Filters
    public static final int MarvelCaptainAmerica = 6;
    public static final int MarvelIronMan = 7;
    public static final int MarvelThanos = 8;
    public static final int MarvelGroot = 9;
    public static final int MarvelThor = 10;
    public static final int MarvelWolverine = 11;
    public static final int MarvelHulk =  12;
    public static final int MarvelDeadpool =  13;
    public static final int MarvelBeast =  14;
    public static final int MarvelCyclops =  15;
    // Animals Filters
    public static final int AngryDog =  16;
    public static final int Chicken =  17;
    public static final int Cow =  18;
    public static final int PugDog =  19;
    public static final int Whale =  20;
    public static final int Fox =  21;
    public static final int Owl =  22;
    public static final int Panda =  23;
    public static final int Sloth =  24;
    public static final int PixelatedCat =  25;
    public static final int Racoon =  26;
    public static final int RedPanda =  27;
    public static final int Reindeer =  28;
    public static final int BlackCube =  32;

    String path;
    String description;
    Integer filter;

    Filter(String PicturePath, String description, Integer filter)
    {
        this.path = PicturePath;
        this.description = description;
        this.filter = filter;
    }

    /**
     *
     * @param currentFilter - current filter (emoji)
     * @param bounds - bound of the rectangle of the face detection
     * @return - emoji in the right size and resolution to put on rectangle around face
     */
    public static Bitmap getFilterIconResized(Integer currentFilter, Rect bounds, Context context) {
        switch (currentFilter){
            case (SMILE):
                Bitmap emojiSmileyFace = BitmapUtils.getBitmapFromAssets(context,
                        "emojiSmileyFace.png", 84,84)
                        .copy(Bitmap.Config.ARGB_8888, true);
                // get emoji rescaled to the size of the rectangle around the face
                Bitmap toReturn =  BitmapUtils
                        .getResizedBitmap(emojiSmileyFace,
                                bounds.width(),
                                bounds.height());
                emojiSmileyFace.recycle();
                return toReturn;
            case (SAD):
                return BitmapUtils
                    .getResizedBitmap(Objects.requireNonNull(BitmapUtils.getBitmapFromAssets(context,
                            "emojiSadFace.png", 512,
                            512)),
                            bounds.width(),
                            bounds.height());
            case(COVID_LEFT):
                return BitmapUtils
                        .getResizedBitmap(Objects.requireNonNull(BitmapUtils.getBitmapFromAssets(context,
                                "maskLeft.png", 1200,
                                1200)),
                                bounds.width(),
                                bounds.height()*2-(int)Math.floor((float)bounds.height()/2) + 10);
            case(COVID_MIDDLE):
                return BitmapUtils
                        .getResizedBitmap(Objects.requireNonNull(BitmapUtils.getBitmapFromAssets(context,
                                "maskMiddle.png", 1200,
                                1200)),
                                bounds.width(),
                                bounds.height()*2-(int)Math.floor((float)bounds.height()/2) - (int)Math.floor((float)bounds.height()/5));
            case (COVID_RIGHT):
                return BitmapUtils
                        .getResizedBitmap(Objects.requireNonNull(BitmapUtils.getBitmapFromAssets(context,
                                "maskRight1.png", 1200,
                                1200)),
                                bounds.width(),
                                bounds.height()*2-(int)Math.floor((float)bounds.height()/2) + 10);
            case (FaceWithCovidMask):
                return BitmapUtils
                    .getResizedBitmap(Objects.requireNonNull(BitmapUtils.getBitmapFromAssets(context,
                            "emojiFaceWithCoronaMask.png", DEFAULT_ICON_WIDTH,
                            DEFAULT_ICON_WIDTH)),
                            bounds.width(),
                            bounds.height());
            case (MarvelCaptainAmerica):
                // get emoji rescaled to the size of the rectangle around the face
                return BitmapUtils
                        .getResizedBitmap(Objects.requireNonNull(BitmapUtils.getBitmapFromAssets(context,
                                "Marvel_Icons/Marvel_Captain_America.png", 85,
                                DEFAULT_ICON_HEIGHT)),
                                bounds.width(),
                                bounds.height());
            case (MarvelIronMan):
                return BitmapUtils
                        .getResizedBitmap(Objects.requireNonNull(BitmapUtils.getBitmapFromAssets(context,
                                "Marvel_Icons/Marvel_Iron_Man.png", DEFAULT_ICON_WIDTH,
                                85)),
                                bounds.width(),
                                bounds.height());
            case (MarvelThanos):
                return BitmapUtils
                        .getResizedBitmap(Objects.requireNonNull(BitmapUtils.getBitmapFromAssets(context,
                                "Marvel_Icons/Marvel_Thanos.png", 77,
                                DEFAULT_ICON_HEIGHT)),
                                bounds.width(),
                                bounds.height());
            case (MarvelGroot):
                return BitmapUtils
                        .getResizedBitmap(Objects.requireNonNull(BitmapUtils.getBitmapFromAssets(context,
                                "Marvel_Icons/Marvel_Groot.png", DEFAULT_ICON_WIDTH,
                                97)),
                                bounds.width(),
                                bounds.height());
            case (MarvelThor):
                return BitmapUtils
                        .getResizedBitmap(Objects.requireNonNull(BitmapUtils.getBitmapFromAssets(context,
                                "Marvel_Icons/Marvel_Thor.png", DEFAULT_ICON_WIDTH,
                                DEFAULT_ICON_HEIGHT)),
                                bounds.width(),
                                bounds.height());
            case (MarvelWolverine):
                return BitmapUtils
                        .getResizedBitmap(Objects.requireNonNull(BitmapUtils.getBitmapFromAssets(context,
                                "Marvel_Icons/Marvel_Wolverine.png", 82,
                                DEFAULT_ICON_HEIGHT)),
                                bounds.width(),
                                bounds.height());
            case (MarvelHulk):
                return BitmapUtils
                        .getResizedBitmap(Objects.requireNonNull(BitmapUtils.getBitmapFromAssets(context,
                                "Marvel_Icons/Marvel_Hulk.png", 81,
                                DEFAULT_ICON_HEIGHT)),
                                bounds.width(),
                                bounds.height());
            case (MarvelDeadpool):
                return BitmapUtils
                        .getResizedBitmap(Objects.requireNonNull(BitmapUtils.getBitmapFromAssets(context,
                                "Marvel_Icons/Marvel_Deadpool.png", 89,
                                DEFAULT_ICON_HEIGHT)),
                                bounds.width(),
                                bounds.height());
            case (MarvelBeast):
                return BitmapUtils
                        .getResizedBitmap(Objects.requireNonNull(BitmapUtils.getBitmapFromAssets(context,
                                "Marvel_Icons/Marvel_Beast.png", DEFAULT_ICON_WIDTH,
                                DEFAULT_ICON_HEIGHT)),
                                bounds.width(),
                                bounds.height());
            case (MarvelCyclops):
                return BitmapUtils
                        .getResizedBitmap(Objects.requireNonNull(BitmapUtils.getBitmapFromAssets(context,
                                "Marvel_Icons/Marvel_Cyclops.png", 79,
                                DEFAULT_ICON_HEIGHT)),
                                bounds.width(),
                                bounds.height());
            case (AngryDog):
                return BitmapUtils
                        .getResizedBitmap(Objects.requireNonNull(BitmapUtils.getBitmapFromAssets(context,
                                "Animals_Icon/Angry_Dog.png", DEFAULT_ICON_WIDTH,
                                DEFAULT_ICON_HEIGHT)),
                                bounds.width(),
                                bounds.height());
            case (Chicken):
                return BitmapUtils
                        .getResizedBitmap(Objects.requireNonNull(BitmapUtils.getBitmapFromAssets(context,
                                "Animals_Icon/Chicken.png", DEFAULT_ICON_WIDTH,
                                DEFAULT_ICON_HEIGHT)),
                                bounds.width(),
                                bounds.height());
            case (Cow):
                return BitmapUtils
                        .getResizedBitmap(Objects.requireNonNull(BitmapUtils.getBitmapFromAssets(context,
                                "Animals_Icon/Cow.png", 86,
                                90)),
                                bounds.width(),
                                bounds.height());
            case (Fox):
                return BitmapUtils
                        .getResizedBitmap(Objects.requireNonNull(BitmapUtils.getBitmapFromAssets(context,
                                "Animals_Icon/Fox.png", 88,
                                89)),
                                bounds.width(),
                                bounds.height());
            case (Owl):
                return BitmapUtils
                        .getResizedBitmap(Objects.requireNonNull(BitmapUtils.getBitmapFromAssets(context,
                                "Animals_Icon/Owl.png", 71,
                                DEFAULT_ICON_HEIGHT)),
                                bounds.width(),
                                bounds.height());
            case (Panda):
                return BitmapUtils
                        .getResizedBitmap(Objects.requireNonNull(BitmapUtils.getBitmapFromAssets(context,
                                "Animals_Icon/Panda.png", 90,
                                88)),
                                bounds.width(),
                                bounds.height());
            case (PixelatedCat):
                return BitmapUtils
                        .getResizedBitmap(Objects.requireNonNull(BitmapUtils.getBitmapFromAssets(context,
                                "Animals_Icon/Pixelated_Cat.png", DEFAULT_ICON_WIDTH,
                                DEFAULT_ICON_HEIGHT)),
                                bounds.width(),
                                bounds.height());
            case (PugDog):
                return BitmapUtils
                        .getResizedBitmap(Objects.requireNonNull(BitmapUtils.getBitmapFromAssets(context,
                                "Animals_Icon/Pug_Dog.png", DEFAULT_ICON_WIDTH,
                                89)),
                                bounds.width(),
                                bounds.height());
            case (Racoon):
                return BitmapUtils
                        .getResizedBitmap(Objects.requireNonNull(BitmapUtils.getBitmapFromAssets(context,
                                "Animals_Icon/Racoon.png", DEFAULT_ICON_WIDTH,
                                DEFAULT_ICON_HEIGHT)),
                                bounds.width(),
                                bounds.height());
            case (RedPanda):
                return BitmapUtils
                        .getResizedBitmap(Objects.requireNonNull(BitmapUtils.getBitmapFromAssets(context,
                                "Animals_Icon/Red_Panda.png", DEFAULT_ICON_WIDTH,
                                89)),
                                bounds.width(),
                                bounds.height());
            case (Reindeer):
                return BitmapUtils
                        .getResizedBitmap(Objects.requireNonNull(BitmapUtils.getBitmapFromAssets(context,
                                "Animals_Icon/Reindeer.png", DEFAULT_ICON_WIDTH,
                                DEFAULT_ICON_HEIGHT)),
                                bounds.width(),
                                bounds.height());
            case (Sloth):
                return BitmapUtils
                        .getResizedBitmap(Objects.requireNonNull(BitmapUtils.getBitmapFromAssets(context,
                                "Animals_Icon/Sloth.png", DEFAULT_ICON_WIDTH,
                                DEFAULT_ICON_HEIGHT)),
                                bounds.width(),
                                bounds.height());
            case (Whale):
                return BitmapUtils
                        .getResizedBitmap(Objects.requireNonNull(BitmapUtils.getBitmapFromAssets(context,
                                "Animals_Icon/Whale.png", DEFAULT_ICON_WIDTH,
                                DEFAULT_ICON_HEIGHT)),
                                bounds.width(),
                                bounds.height());
            default:
                return null;
        }

    }
}
