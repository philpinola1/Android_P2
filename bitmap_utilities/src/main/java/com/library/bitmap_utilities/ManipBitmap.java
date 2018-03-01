package com.library.bitmap_utilities;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.nio.IntBuffer;

/**
 * @author Keith
 *         Purely static helper functions
 *         WARNING very little error checking is done here
 *         Lots of this was copied and cleaned from
 *         http://stackoverflow.com/questions/9826273/photo-image-to-sketch-algorithm
 *         http://stackoverflow.com/questions/17841787/invert-colors-of-drawable-android
 *         trying to make a sketch effect similar to
 *         http://www.createblog.com/paintshop-pro-tutorials/14018-sketch-effect/
 */
public class ManipBitmap {
    private static final String DEBUG_TAG = "ManipBitmap";
    private static final int LEFT = 0;
    private static final int TOP = 0;

    /**
     * The caller references ManipBitmap using <tt>ManipBitmap.func()</tt>,
     * and so on. Thus, the caller should be prevented from constructing objects of
     * this class, by declaring this private constructor.
     */
    private ManipBitmap() {
        //this prevents even the native class from
        //calling this ctor as well :
        throw new AssertionError();
    }

    /**
     * Turn bitmap to grays (strip all color)
     *
     * @param bmpOriginal, non null
     * @return COPY of bitmap that is now in shades of gray
     */
    protected static Bitmap toGrayscale(Bitmap bmpOriginal) {
        if (bmpOriginal == null)
            throw new NullPointerException();

        final int CONVERTTOGRAYSCALE = 0;

        int height = bmpOriginal.getHeight();
        int width = bmpOriginal.getWidth();

        //create a new bitmap
        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();

        cm.setSaturation(CONVERTTOGRAYSCALE);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, LEFT, TOP, paint);
        return bmpGrayscale;
    }

    /**
     * Color range is (#000000=black to #ffffff=white)
     * Based on percentage, this function will choose a
     * threshold color. All colors above will be changed to
     * white, all colors below will be black
     * It will use this algorithm to determine pixel color
     * when creating a new bitmap to return from bmpOrig
     *
     * @param bmpOrig    original ffull color image
     * @param percentage used to determine where color threshold is
     * @return brand new black and white only bitmap
     */
    protected static Bitmap thresholdBmp(final Bitmap bmpOrig, final int percentage) {
        // lets save a copy of the grayscale image for later
        Bitmap bmpGrayScale = ManipBitmap.toGrayscale(bmpOrig);

        // now invert colors
        Bitmap bmpInvert = ManipBitmap.invert(bmpGrayScale);

        // apply fast blur
        Bitmap myBlurredBitmap = BlurBuilder.fastblur(bmpInvert);

        // color dodge blend
        Bitmap bmpThreshold = ManipBitmap.colorDodgeBlend(myBlurredBitmap,
                bmpGrayScale);

        doThreshold(bmpThreshold, percentage);

        return bmpThreshold;
    }

    private static String hexVal(int int_value) {
        String hex_value = int_value < 0 ? "-"
                + Integer.toHexString(-int_value) : Integer
                .toHexString(int_value);
        return hex_value;
    }

    protected static void doThreshold(Bitmap bmpThreshold, int percentage) {
        //find color value above which pixel is white below which it is Black
        final int RANGE_RESTRICTION = 7;                    //restrict thresholding to the upper 7th of color scale
        //otherwise thresholding often leaves all white pixels
        final int INCREMENT = 167772 / RANGE_RESTRICTION;     //number that divides color range evenly
        //if haven't processed Bitmap then do nothing
        if (bmpThreshold == null) {
            Log.e(DEBUG_TAG, "In doThreshold, bmpFast is null ");
            return;
        }

        //lets adjust this image
        int WHITE_BLACK_LINE = (Color.WHITE - (percentage * INCREMENT));
        Log.i(DEBUG_TAG, "WHITE_BLACK_LINE = " + hexVal(WHITE_BLACK_LINE));
        ManipBitmap.threshold(bmpThreshold, WHITE_BLACK_LINE);
    }
    //

    /**
     * make and return a copy of the drawables bitmap
     *
     * @param drawable non null
     * @return COPY of bitmap
     */
    protected static Bitmap copyBitmap(final Drawable drawable) {
        if (drawable == null)
            throw new NullPointerException();

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    /**
     * new bitmap is a negative of original
     *
     * @param bmp non null
     * @return COPY of bitmap
     */
    protected static Bitmap invert(final Bitmap bmp) {
        float[] colorMatrix_Negative =
                {-1.0f, 0, 0, 0, 255, // red
                        0, -1.0f, 0, 0, 255, // green
                        0, 0, -1.0f, 0, 255, // blue
                        0, 0, 0, 1.0f, 0 // alpha
                };

        if (bmp == null)
            throw new NullPointerException();

        final int height = bmp.getHeight();
        final int width = bmp.getWidth();

        final Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bmpGrayscale);
        final Paint paint = new Paint();
        ColorFilter colorFilter = new ColorMatrixColorFilter(colorMatrix_Negative);

        paint.setColorFilter(colorFilter);
        canvas.drawBitmap(bmp, 0, 0, paint);
        return bmpGrayscale;
    }

    /**
     * Classic color dodge algorithm (see wikipedia)
     *
     * @param in1
     * @param in2
     * @return int
     */
    protected static int colordodge(int in1, int in2) {
        float image = (float) in2;
        float mask = (float) in1;

        //classic color dodge algorithm
        return ((int) ((image == 255) ? image : Math.min(255, (((long) mask << 8) / (255 - image)))));
    }

    /**
     * Blends 2 bitmaps to one and adds the color dodge blend mode to it.
     *
     * @param source non null
     * @param layer  non null
     * @return Bitmap
     */
    protected static Bitmap colorDodgeBlend(Bitmap source, Bitmap layer) {
        if (source == null || layer == null)
            throw new NullPointerException();

        Bitmap base = source.copy(Config.ARGB_8888, true);
        Bitmap blend = layer.copy(Config.ARGB_8888, true);

        IntBuffer buffBase = IntBuffer.allocate(base.getWidth() * base.getHeight());
        base.copyPixelsToBuffer(buffBase);
        buffBase.rewind();

        IntBuffer buffBlend = IntBuffer.allocate(blend.getWidth() * blend.getHeight());
        blend.copyPixelsToBuffer(buffBlend);
        buffBlend.rewind();

        IntBuffer buffOut = IntBuffer.allocate(base.getWidth() * base.getHeight());
        buffOut.rewind();

        while (buffOut.position() < buffOut.limit()) {
            int filterInt = buffBlend.get();
            int srcInt = buffBase.get();

            int redValueFilter = Color.red(filterInt);
            int greenValueFilter = Color.green(filterInt);
            int blueValueFilter = Color.blue(filterInt);

            int redValueSrc = Color.red(srcInt);
            int greenValueSrc = Color.green(srcInt);
            int blueValueSrc = Color.blue(srcInt);

            int redValueFinal = colordodge(redValueFilter, redValueSrc);
            int greenValueFinal = colordodge(greenValueFilter, greenValueSrc);
            int blueValueFinal = colordodge(blueValueFilter, blueValueSrc);

            int pixel = Color.argb(255, redValueFinal, greenValueFinal, blueValueFinal);

            buffOut.put(pixel);
        }
        buffOut.rewind();

        base.copyPixelsFromBuffer(buffOut);
        blend.recycle();    //legacy for Android 2.3 where bitmaps were created on the heap, you had to release them yourself
        blend = null;

        return base;
    }

    /**
     * Takes the source bitmap and modifies it to have only White, Black or Dark gray pixels
     * You can extend this to include more colors
     *
     * @param source non null
     * Note that this function modifies the bitmap that it was sent!
     */
    private static int Lower1 = 0xFFEEEEEE;    //very close to White
//    private static int Lower2 = 0xFFDDDDDD;

    private static int Color1 = Color.WHITE;    //0xFFFFFFFF
    //    private static int Color2 = Color.DKGRAY;
    private static int Color3 = Color.BLACK;    //0xFF000000

    protected static void threshold(Bitmap source) {
        threshold(source, Lower1, Color1, Color3);
    }

    protected static void threshold(Bitmap source, int lower1) {
        threshold(source, lower1, Color1, Color3);
    }


    protected static void threshold(Bitmap source, int lower1, int color1, int color3) {
        if (source == null)
            throw new NullPointerException();

        Color1 = color1;
        Color3 = color3;

        int w = source.getWidth();
        int h = source.getHeight();

        int length = w * h;
        int[] array = new int[length];

        source.getPixels(array, 0, w, 0, 0, w, h);

        for (int i = 0; i < length; i++) {
            // If the bitmap is in ARGB_8888 format
            if (array[i] >= lower1)
                array[i] = color1;
            else
                array[i] = color3;
        }
        source.setPixels(array, 0, w, 0, 0, w, h);
    }

    /**
     * merge the color bmpFastColor with bmpThresholded, pixel by pixel
     * if bmpThresholded pixel is white then use color in  bmpFastColor
     * if bmpThresholded pixel is black then color is black
     *
     * @param bmpFastColor   edited
     * @param bmpThresholded
     */
    protected static void merge(Bitmap bmpFastColor, final Bitmap bmpThresholded) {
        if (bmpFastColor == null || bmpThresholded == null)
            throw new NullPointerException();

        int w = bmpFastColor.getWidth();
        int h = bmpFastColor.getHeight();

        //loop through pixels and make a hard determination
        //if white leave it, otherwise turn it black
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {

                int colorBW = bmpThresholded.getPixel(x, y);

                //if the black and white is white then dont change its color
                if (colorBW != Color1)
                    bmpFastColor.setPixel(x, y, colorBW);
            }
        }
    }

    private static final double VALUE_TOP = 1;

    /**
     * hue, saturarion, value intervals size are for reduce colors on Bitmap
     * saturation, value percents are for increment or decrement [0..100..)
     * This is one that I have not really examined...sorry
     *
     * @param realBitmap
     * @param dodgeBlendBitmap
     * @param hueIntervalSize
     * @param saturationIntervalSize
     * @param valueIntervalSize
     * @param saturationPercent
     * @param valuePercent
     * @return
     */
    protected static Bitmap getCartoonizedBitmap(Bitmap realBitmap, Bitmap dodgeBlendBitmap, int hueIntervalSize, int saturationIntervalSize, int valueIntervalSize, int saturationPercent, int valuePercent) {
        return ManipBitmap.getCartoonizedBitmap(realBitmap, dodgeBlendBitmap, hueIntervalSize, saturationIntervalSize, valueIntervalSize, saturationPercent, valuePercent, VALUE_TOP);
    }

    protected static Bitmap getCartoonizedBitmap(Bitmap realBitmap, Bitmap dodgeBlendBitmap, int hueIntervalSize, int saturationIntervalSize, int valueIntervalSize, int saturationPercent, int valuePercent, double valueTop) {
        if (realBitmap == null || dodgeBlendBitmap == null)
            throw new NullPointerException();

        Bitmap base = BlurBuilder.fastblur(realBitmap).copy(Config.ARGB_8888, true);
        Bitmap dodge = dodgeBlendBitmap.copy(Config.ARGB_8888, false);
        try {
            int realColor;
            int color;
            float top = (float) VALUE_TOP; //Between 0.0f .. 1.0f I use 0.87f
            IntBuffer templatePixels = IntBuffer.allocate(dodge.getWidth()
                    * dodge.getHeight());
            IntBuffer scaledPixels = IntBuffer.allocate(base.getWidth()
                    * base.getHeight());
            IntBuffer buffOut = IntBuffer.allocate(base.getWidth()
                    * base.getHeight());

            base.copyPixelsToBuffer(scaledPixels);
            dodge.copyPixelsToBuffer(templatePixels);

            templatePixels.rewind();
            scaledPixels.rewind();
            buffOut.rewind();

            while (buffOut.position() < buffOut.limit()) {
                color = (templatePixels.get());
                realColor = scaledPixels.get();

                float[] realHSV = new float[3];
                Color.colorToHSV(realColor, realHSV);

                realHSV[0] = getRoundedValue(realHSV[0], hueIntervalSize);

                realHSV[2] = (getRoundedValue(realHSV[2] * 100,
                        valueIntervalSize) / 100) * (valuePercent / 100);
                realHSV[2] = realHSV[2] < 1.0 ? realHSV[2] : 1.0f;

                realHSV[1] = realHSV[1] * (saturationPercent / 100);
                realHSV[1] = realHSV[1] < 1.0 ? realHSV[1] : 1.0f;

                float[] HSV = new float[3];
                Color.colorToHSV(color, HSV);

                boolean putBlackPixel = HSV[2] <= top;

                realColor = Color.HSVToColor(realHSV);

                if (putBlackPixel) {
                    buffOut.put(color);
                } else {
                    buffOut.put(realColor);
                }
            }// END WHILE
            dodge.recycle();
            buffOut.rewind();
            base.copyPixelsFromBuffer(buffOut);

        } catch (Exception e) {
            // TODO: handle exception
        }

        return base;
    }

    /**
     * This is one that I have not really examined...sorry
     *
     * @param value
     * @param intervalSize
     * @return
     */
    private static float getRoundedValue(float value, int intervalSize) {
        float result = Math.round(value);
        int mod = ((int) result) % intervalSize;
        result += mod < (intervalSize / 2) ? -mod : intervalSize - mod;
        return result;
    }

    /**
     * Creates a new Bitmap from src
     * and changes the color saturation of its pixels
     *
     * @param src
     * @param settingSat
     * @return
     */
    protected static Bitmap colorBmp(final Bitmap src, final float settingSat) {

        int w = src.getWidth();
        int h = src.getHeight();

        Bitmap bitmapResult = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvasResult = new Canvas(bitmapResult);
        Paint paint = new Paint();
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(settingSat);
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(colorMatrix);
        paint.setColorFilter(filter);
        canvasResult.drawBitmap(src, 0, 0, paint);

        return bitmapResult;
    }

}

