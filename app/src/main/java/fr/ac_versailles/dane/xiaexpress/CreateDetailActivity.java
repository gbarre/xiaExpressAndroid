package fr.ac_versailles.dane.xiaexpress;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
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

    private Toolbar myToolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_detail);
        myToolbar = (Toolbar) findViewById(R.id.myToolbar);
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
        int blueColor = ContextCompat.getColor(this, R.color.blue);
        Button bt = new Button(this);
        bt.setText("new button");
        bt.setBackgroundColor(blueColor);
        bt.setLayoutParams(new Toolbar.LayoutParams(Toolbar.LayoutParams.WRAP_CONTENT));
        myToolbar.addView(bt);
    }

}
