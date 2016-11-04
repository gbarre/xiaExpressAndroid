package fr.ac_versailles.dane.xiaexpress;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import static fr.ac_versailles.dane.xiaexpress.Util.*;
import static fr.ac_versailles.dane.xiaexpress.dbg.*;

/**
 *  MainActivity.java
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


public class MainActivity extends AppCompatActivity {
    private GridView gridView;
    private GridViewAdapter gridAdapter;
    private static final int SELECT_PICTURE = 1;

    private String[] arrayNames = new String[50];

    private String rootDirectory;
    private String imagesDirectory;
    private String xmlDirectory;
    private String cacheDirectory;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // full screen, no title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        String TAG = Thread.currentThread().getStackTrace()[2].getClassName()+"."+Thread.currentThread().getStackTrace()[2].getMethodName();

        rootDirectory = String.valueOf(getExternalFilesDir(null)) + File.separator;
        imagesDirectory = Constants.getImagesFrom(rootDirectory);
        xmlDirectory = Constants.getXMLFrom(rootDirectory);
        cacheDirectory = Constants.getCacheFrom(rootDirectory);

        createDirectory(imagesDirectory);
        createDirectory(xmlDirectory);
        createDirectory(cacheDirectory);

        // Load the collection in grid view
        gridView = (GridView) findViewById(R.id.gridView);
        gridAdapter = new GridViewAdapter(this, R.layout.grid_item_layout, getData());
        gridView.setAdapter(gridAdapter);

        // Add listener on collection
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                PhotoThumbnail item = (PhotoThumbnail) parent.getItemAtPosition(position);

                //Create intent
                Intent intent = new Intent(MainActivity.this, CreateDetailActivity.class);
                intent.putExtra("title", item.getTitle());

                //Start details activity
                startActivity(intent);


            }
        });

        // Button to load images
        Button buttonLoadImage = (Button) findViewById(R.id.buttonLoadPicture);

        // Listener on button
        buttonLoadImage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,
                        "Select Picture"), SELECT_PICTURE);
            }
        });

    }



    // Store the image
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        String TAG = Thread.currentThread().getStackTrace()[2].getClassName()+"."+Thread.currentThread().getStackTrace()[2].getMethodName();
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                // Convert uri to path
                Uri selectedImageUri = data.getData();
                // Copy file to documentsDirectory
                long now = System.currentTimeMillis();

                try {
                    InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                    copy(inputStream, new File(imagesDirectory + now + ".jpg"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Bitmap bitmap = decodeSampledBitmapFromFile(imagesDirectory + now + ".jpg", 150, 150);
                String imgName = new File(imagesDirectory + now + ".jpg").getName();
                gridAdapter.add(new PhotoThumbnail(bitmap, imgName));

                // Create xml file
                String xmlString = "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\"?>\n" +
                        "<xia>\n" +
                        "\t<title></title>\n" +
                        "\t<description></description>\n" +
                        "\t<creator></creator>\n" +
                        "\t<rights></rights>\n" +
                        "\t<license></license>\n" +
                        "\t<date></date>\n" +
                        "\t<publisher></publisher>\n" +
                        "\t<identifier></identifier>\n" +
                        "\t<source></source>\n" +
                        "\t<relation></relation>\n" +
                        "\t<language></language>\n" +
                        "\t<keywords></keywords>\n" +
                        "\t<coverage></coverage>\n" +
                        "\t<contributors></contributors>\n" +
                        "\t<readonly code=\"1234\">false</readonly>\n" +
                        "\t<image description=\"\" title=\"\" desctription=\"\" />\n" +
                        "\t<details show=\"true\">\n" +
                        "\t</details>\n" +
                        "</xia>";
                try {
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(xmlDirectory + now + ".xml"));
                    outputStreamWriter.write(xmlString);
                    outputStreamWriter.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Prepare some dummy data for gridview
     */
    private ArrayList<PhotoThumbnail> getData() {
        String TAG = Thread.currentThread().getStackTrace()[2].getClassName()+"."+Thread.currentThread().getStackTrace()[2].getMethodName();
        final ArrayList<PhotoThumbnail> imageItems = new ArrayList<>();
        File dir = new File(imagesDirectory);
        File[] imgs = dir.listFiles();

        if (imgs != null && imgs.length > 0) {
            for (int i = 0; i < imgs.length; i++) {
                String imgName = imgs[i].getName();
                arrayNames[i] = imgName;

                Bitmap bitmap;
                // Check if image is in cacheDirectory
                if (new File(cacheDirectory + imgName).exists()) {
                    bitmap = BitmapFactory.decodeFile(cacheDirectory + imgName);
                } else {
                    bitmap = decodeSampledBitmapFromFile(imgs[i].toString(), 150, 150);
                }

                imageItems.add(new PhotoThumbnail(bitmap, imgName));
            }
        }
        else {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.plus200);
            imageItems.add(new PhotoThumbnail(bitmap, "New resource..."));
        }
        return imageItems;
    }

    /**
     * Copy file from path src to path dst
     */
    public static void copy(InputStream in, File dst) throws IOException {
        String TAG = Thread.currentThread().getStackTrace()[2].getClassName()+"."+Thread.currentThread().getStackTrace()[2].getMethodName();
        //InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    private Bitmap decodeSampledBitmapFromFile(String path, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        // Copy file to cacheDirectory
        String imgName = path.substring(path.lastIndexOf("/")+1);
        File cachedImage = new File(cacheDirectory+imgName);
        try {
            cachedImage.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Bitmap bitmap = BitmapFactory.decodeFile(path,options);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, bos);
        byte[] bitmapData = bos.toByteArray();

        try {
            FileOutputStream fos = new FileOutputStream(cachedImage);
            fos.write(bitmapData);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return BitmapFactory.decodeFile(String.valueOf(cachedImage));
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

}
