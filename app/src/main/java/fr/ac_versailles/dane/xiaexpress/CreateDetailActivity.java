package fr.ac_versailles.dane.xiaexpress;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.File;

import static fr.ac_versailles.dane.xiaexpress.Util.*;
import static fr.ac_versailles.dane.xiaexpress.dbg.*;

/**
 *  CreateDetailActivity.java
 *  xia-android
 *
 *  Created by guillaume on 29/09/2016.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 *  @author : guillaume.barre@ac-versailles.fr
 */

public class CreateDetailActivity extends AppCompatActivity {

    private String rootDirectory;
    private String imagesDirectory;
    private String xmlDirectory;
    private String cacheDirectory;

    private Boolean createDetail = false; // true while polygon creation

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_detail);
        setBtnsIcons();

        String TAG = Thread.currentThread().getStackTrace()[2].getClassName()+"."+Thread.currentThread().getStackTrace()[2].getMethodName();
        pt(TAG, "onCreate launched");

        rootDirectory = String.valueOf(getExternalFilesDir(null)) + File.separator;
        imagesDirectory = Constants.getImagesFrom(rootDirectory);
        xmlDirectory = Constants.getXMLFrom(rootDirectory);
        cacheDirectory = Constants.getCacheFrom(rootDirectory);

        String title = getIntent().getStringExtra("title");

        ImageView imageView = (ImageView) findViewById(R.id.image);
        String imagePath = imagesDirectory + title;
        pt(TAG, imagePath);
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);
        imageView.setImageBitmap(bitmap);
    }

    private void setBtnsIcons() {

        // Buttons
        Button btCollection = (Button) findViewById(R.id.collection);
        final ImageButton btAddDetail = (ImageButton) findViewById(R.id.addDetail);
        ImageButton btUndo = (ImageButton) findViewById(R.id.undo);
        ImageButton btPlay = (ImageButton) findViewById(R.id.play);
        Button btOK = (Button) findViewById(R.id.ok);
        ImageButton btExport = (ImageButton) findViewById(R.id.export);
        ImageButton btEdit = (ImageButton) findViewById(R.id.edit);
        ImageButton btTrash = (ImageButton) findViewById(R.id.trash);

        if (createDetail) {
            btAddDetail.setVisibility(View.GONE);
            btPlay.setVisibility(View.GONE);

            btUndo.setVisibility(View.VISIBLE);
            btOK.setVisibility(View.VISIBLE);
            btTrash.setVisibility(View.VISIBLE);
        }
        else {
            btAddDetail.setVisibility(View.VISIBLE);
            btPlay.setVisibility(View.VISIBLE);

            btUndo.setVisibility(View.GONE);
            btOK.setVisibility(View.GONE);
            btTrash.setVisibility(View.GONE);
        }

        btCollection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btAddDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addDetail();
            }
        });

        btOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopCreation();
            }
        });





    }

    public void addDetail() {
        createDetail = true;
        setBtnsIcons();
    }

    public void stopCreation() {
        createDetail = false;
        setBtnsIcons();
    }

    public void showPopUp() {

        AlertDialog.Builder helpBuilder = new AlertDialog.Builder(this);
        helpBuilder.setTitle("Pop Up");
        helpBuilder.setMessage("This is a Simple Pop Up");
        helpBuilder.setPositiveButton("Positive",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing but close the dialog

                    }
                });

        helpBuilder.setNegativeButton("Negative", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing
            }
        });

        helpBuilder.setNeutralButton("Neutral", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing
            }
        });

        // Remember, create doesn't show the dialog
        AlertDialog helpDialog = helpBuilder.create();
        helpDialog.show();

    }

}
