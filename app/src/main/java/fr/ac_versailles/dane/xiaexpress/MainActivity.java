package fr.ac_versailles.dane.xiaexpress;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    private GridView gridView;
    private GridViewAdapter gridAdapter;
    private static final int SELECT_PICTURE = 1;

    private String selectedImagePath;
    private String[] arrayNames = new String[50];

    private String documentsDirectory;
    private String cacheDirectory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        documentsDirectory = String.valueOf(getExternalFilesDir(null)) + File.separator;
        cacheDirectory = documentsDirectory + ".cache" + File.separator;
        File cDirectory = new File(cacheDirectory);
        boolean success = true;
        if (!cDirectory.exists()) {
            success = cDirectory.mkdir();
            if (success) {
                // Do something on success
                Log.v("Success", ".cache created");
            } else {
                // Do something else on failure
                Log.v("Success", ".cache not created");
            }
        }
        else {
            Log.v("Success", ".cache exist");
        }


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
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Log.v("result", data.toString());
                // Convert uri to path
                Uri selectedImageUri = data.getData();
                Log.v("selectedImageUri", selectedImageUri.toString());
                // Copy file to documentsDirectory
                long now = System.currentTimeMillis();

                try {
                    InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                    copy(inputStream, new File(documentsDirectory + now + ".jpg"));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.v("onresult", "try to decode");
                Bitmap bitmap = decodeSampledBitmapFromFile(documentsDirectory + now + ".jpg", 150, 150);
                String imgName = new File(documentsDirectory + now + ".jpg").getName();
                gridAdapter.add(new PhotoThumbnail(bitmap, imgName));
            }
        }
    }

    /**
     * Prepare some dummy data for gridview
     */
    private ArrayList<PhotoThumbnail> getData() {
        final ArrayList<PhotoThumbnail> imageItems = new ArrayList<>();
        File dir = new File(documentsDirectory);
        File[] imgs = dir.listFiles();
        if (imgs != null && imgs.length > 1) {
            for (int i = 1; i < imgs.length; i++) {
                String imgName = imgs[i].getName();
                Log.v("imgs["+i+"]", imgName);
                if (!imgName.equals(new String(".cache"))) {
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
        }
        else {
            Log.v("noImage", "try to load plus200");
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.plus200);
            imageItems.add(new PhotoThumbnail(bitmap, "New resource..."));
        }
        return imageItems;
    }

    /**
     * Copy file from path src to path dst
     */
    public static void copy(InputStream in, File dst) throws IOException {
        Log.v("copy", "copy launched");
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
        } catch (FileNotFoundException e) {
            e.printStackTrace();
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
