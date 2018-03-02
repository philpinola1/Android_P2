package com.example.solution_color;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.library.bitmap_utilities.BitMap_Helpers;

import java.io.File;


public class MainActivity extends AppCompatActivity  {
    private ImageView imgView;
    private Toolbar myToolbar;
    private ImageButton cameraButton;
    private Bitmap imageBitmap;
    private DisplayMetrics metrics;
    private File path;
    private int screenWidth, screenHeight;

    private int TAKE_PICTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        myToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(myToolbar);
        //myToolbar.getBackground().setAlpha(5);

        imgView = (ImageView)findViewById(R.id.backgroudImage);
        Drawable myDrawable = getResources().getDrawable(R.drawable.gutters);
        imgView.setImageDrawable(myDrawable);

        cameraButton = (ImageButton)findViewById(R.id.camera_button);


        metrics = this.getResources().getDisplayMetrics();
        screenHeight = metrics.heightPixels;
        screenWidth = metrics.widthPixels;

    }

    public void captureImage(View v) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);



        intent.putExtra(MediaStore.EXTRA_OUTPUT, path);

        startActivityForResult(intent, TAKE_PICTURE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TAKE_PICTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            imgView.setImageBitmap(imageBitmap);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                Log.d("seetings clicked","Menu Item: Settings selected");
                Intent myIntent = new Intent(this, SettingsActivity.class);
                startActivity(myIntent);
                break;
            case R.id.action_share:
                //call the function (or intent?)
                Log.d("share_clicked", "Menu Item: Share selected");
                break;
            case R.id.action_eyeball:
                //call the function (or intent?)
                Log.d("eyeball", "Menu Item: Eyeball selected");
                break;
            case R.id.action_pencil:
                //call the function (or intent?)
                Log.d("pencil", "Menu Item: Pencil selected");
                BitMap_Helpers.thresholdBmp(imageBitmap, 50);
                Bitmap cp = Camera_Helpers.loadAndScaleImage(path.getAbsolutePath(), screenHeight, screenWidth);
                
                break;
            case R.id.action_back:
                //call the function (or intent?)
                Log.d("back", "Menu Item: Back selected");
                imgView.setImageResource(R.drawable.gutters);
                imgView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imgView.setScaleType(ImageView.ScaleType.FIT_XY);
                break;
            default:
                break;
        }
        return true;
    }

    public void onCameraClick() {

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivity(cameraIntent);
    }

}

