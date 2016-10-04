package fr.ac_versailles.dane.xiaexpress;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;


public class MainActivity extends AppCompatActivity {
    private GridView gridView;
    private GridViewAdapter gridAdapter;
    private static final int SELECT_PICTURE = 1;

    private static String documentsDirectory = Environment.getExternalStorageDirectory().getPath()+"/XiaExpress/";

    private String selectedImagePath;
    private String[] arrayNames = new String[50];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //String documentsDirectory = Environment.getExternalStorageDirectory().getPath()+"/XiaExpress/";
        File dir = new File(documentsDirectory);
        dir.mkdirs();

        // Load the collection in grid view
        gridView = (GridView) findViewById(R.id.gridView);
        gridAdapter = new GridViewAdapter(this, R.layout.grid_item_layout, getData());
        gridView.setAdapter(gridAdapter);

        Log.v("arrayNames", Arrays.toString(arrayNames));

        // Add listener on collection
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                PhotoThumbnail item = (PhotoThumbnail) parent.getItemAtPosition(position);

                Log.v("item", item.getTitle());

                //Create intent
                Intent intent = new Intent(MainActivity.this, CreateDetailActivity.class);
                intent.putExtra("title", item.getTitle());
                //intent.putExtra("image", item.getImage());

                //Start details activity
                Log.v("Click", item.getTitle());
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
                // Convert uri to path
                Uri selectedImageUri = data.getData();
                selectedImagePath = getPath(selectedImageUri);

                // Copy file to documentsDirectory
                long now = System.currentTimeMillis();
                try {
                    copy(new File(selectedImagePath), new File(documentsDirectory + now + ".jpg"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //Bitmap bitmap = BitmapFactory.decodeFile(documentsDirectory + now + ".jpg");
                Bitmap bitmap = decodeSampledBitmapFromFile(documentsDirectory + now + ".jpg", 150, 150);
                String imgName = new File(documentsDirectory + now + ".jpg").getName();
                gridAdapter.add(new PhotoThumbnail(bitmap, imgName));
            }
        }
    }

    /**
     * helper to retrieve the path of an image URI
     */
    public String getPath(Uri uri) {
        // just some safety built in
        if( uri == null ) {
            // TODO perform some logging or show user feedback
            return null;
        }
        // try to retrieve the image from the media store first
        // this will only work for images selected from gallery
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uri,
                projection, null, null, null);
        if( cursor != null ){
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        // this is our fallback here
        return uri.getPath();
    }

    /**
     * Prepare some dummy data for gridview
     */
    private ArrayList<PhotoThumbnail> getData() {
        final ArrayList<PhotoThumbnail> imageItems = new ArrayList<>();
        File dir = new File(documentsDirectory);
        File[] imgs = dir.listFiles();
        for (int i = 0; i < imgs.length; i++) {
            //Bitmap bitmap = BitmapFactory.decodeFile(imgs[i].toString());
            Bitmap bitmap = decodeSampledBitmapFromFile(imgs[i].toString(), 150, 150);
            String imgName = imgs[i].getName();
            imageItems.add(new PhotoThumbnail(bitmap, imgName));
            arrayNames[i] = imgName;
        }
        return imageItems;
    }

    /**
     * Copy file from path src to path dst
     */
    public void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
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


    private static Bitmap decodeSampledBitmapFromFile(String path,int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        //options.inDither = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path,options);
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
