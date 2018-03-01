package com.example.solution_color;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Perkins on 2/1/2015.
 *
 * Purely static helper functions
 * Camera load and save
 */
public class Camera_Helpers {
	static  final String DEBUG_TAG ="Camera_Helpers";
    private static final float ROTATE_90_DEGREES = 90;
    private static final int FIRST_PIX_X = 0;
    private static final int FIRST_PIX_Y = 0;
	private static final String TAG = "Camera_Helpers";

	/**
	   The caller references the Camera_Helpers using <tt>Camera_Helpers.func()</tt>,
	   and so on. Thus, the caller should be prevented from constructing objects of 
	   this class, by declaring this private constructor. 
	  */
	private Camera_Helpers(){
		//this prevents even the native class from 
	    //calling this ctor as well :
	    throw new AssertionError();
	}

    /**
     * loads photo from originalImagePath<br>
     * downscales to fit screen imageview<br>
     * see the following for loading and scaling drawables<br>
     * see http://developer.android.com/training/displaying-bitmaps/index.html
     * html<br>
     * @param originalImagePath  like storage/emulated/0/pictures/origfile.png
     * @param viewheight size of your view
     * @param viewwidth
     * @return  a complete bitmap or null if not there
     */
	static public Bitmap loadAndScaleImage(String originalImagePath, int viewheight, int viewwidth) {
		
		if (originalImagePath.isEmpty() || viewheight == 0 || viewwidth == 0)
			throw new IllegalArgumentException();
		
		Log.d(DEBUG_TAG, "In Process Image");
		Log.d(DEBUG_TAG, "Width="+ viewwidth + " Height="+viewheight);

		// First decode with inJustDecodeBounds=true to check dimensions of bitmap
		BitmapFactory.Options options = new BitmapFactory.Options();

        //just get the output from decodefile No Bitmap yet
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(originalImagePath, options);

		int height = viewheight;
		int width = viewwidth;
		boolean flipImage = false;

		// handle if its in landscape
		if (options.outWidth > options.outHeight) {
			height = viewwidth;
			width = viewheight;

            //flip it later
			flipImage = true;
		}

		// lets see if we need to scale it

		// calculate the sample size that fits current image
		options.inSampleSize = calculateInSampleSize(options, width, height);
		Log.d(DEBUG_TAG,
				"options.inSampleSize ="
						+ Integer.toString(options.inSampleSize));

        //now get the bitmap now that we know params
		options.inJustDecodeBounds = false;
		Bitmap bmp = BitmapFactory.decodeFile(originalImagePath, options);

		if (flipImage) {
			Matrix matrix = new Matrix();
			matrix.postRotate(ROTATE_90_DEGREES);
			bmp = Bitmap.createBitmap(bmp, FIRST_PIX_X, FIRST_PIX_Y, bmp.getWidth(),
					bmp.getHeight(), matrix, true);
		}

        //return complete scaled rotated bitmap
		return bmp;
	}
     
	/**
	 * Calculates the size of the image
	 * @param options
	 * @param reqWidth
	 * @param reqHeight
	 * @return
	 */
	private static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {

		if (options ==null || reqWidth == 0 || reqHeight == 0)
			throw new IllegalArgumentException();
	
		// Raw height and width of image
		final int height    = options.outHeight;
		final int width     = options.outWidth;
		int inSampleSize    = 1;

		if (height > reqHeight || width > reqWidth) {

			// Calculate ratios of height and width to requested height and
			// width
			final int heightRatio = Math.round((float) height
					/ (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);

			// Choose the smallest ratio as inSampleSize value, this will
			// guarantee
			// a final image with both dimensions larger than or equal to the
			// requested height and width.
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}
		return inSampleSize;
	}

    /**
     * @param bmp                - bitmap to be saved to sdcard
     * @param processedImagePath - path and name where you want it to go
     * @return
     */
    public static boolean saveProcessedImage(Bitmap bmp, String processedImagePath) {
        OutputStream outStream = null;
        final int QUALITY_FACTOR = 100;

        File file = new File(processedImagePath);

        try {
            outStream = new FileOutputStream(file);
            try {
                bmp.compress(Bitmap.CompressFormat.PNG, QUALITY_FACTOR, outStream);
                outStream.flush();
            } finally {
                outStream.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

	public static boolean  delSavedImage(String imagePath){
		boolean bRet = false;
		try{
			File file = new File(imagePath);
			bRet =  file.delete();
		}catch(NullPointerException e) {
			Log.d(TAG, "delSavedImage: Please pass in a path string");
		}
		return bRet;
	}
}
