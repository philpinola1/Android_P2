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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;


public class MainActivity extends AppCompatActivity  {
    private ImageView imgView;
    private Toolbar myToolbar;
    private Button cameraButton;
    private int TAKE_PICTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        myToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(myToolbar);

        imgView = (ImageView)findViewById(R.id.backgroudImage);
        Drawable myDrawable = getResources().getDrawable(R.drawable.gutters);
        imgView.setImageDrawable(myDrawable);

        cameraButton = (Button)findViewById(R.id.camera_button);
    }


    public void captureImage(View v) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, path);

        startActivityForResult(intent, TAKE_PICTURE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TAKE_PICTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
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
                //call the function (or intent?) associated with the eyeball
                Log.d("share_clicked", "Menu Item: Share selected");
                break;
            case R.id.action_eyeball:
                //call the function (or intent?) associated with the eyeball
                Log.d("eyeball", "Menu Item: Eyeball selected");
                break;
            case R.id.action_pencil:
                //call the function (or intent?) associated with the eyeball
                Log.d("eyeball", "Menu Item: Pencil selected");
                break;
            case R.id.action_back:
                //call the function (or intent?) associated with the eyeball
                Log.d("eyeball", "Menu Item: Back selected");
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

