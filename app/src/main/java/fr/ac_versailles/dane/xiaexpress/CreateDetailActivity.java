package fr.ac_versailles.dane.xiaexpress;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by guillaume on 29/09/2016.
 */
public class CreateDetailActivity extends AppCompatActivity {

    private static String documentsDirectory = Environment.getExternalStorageDirectory().getPath()+"/XiaExpress/";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_detail);

        Log.d("createDetail", "onCreate launched");

        String title = getIntent().getStringExtra("title");

        TextView titleTextView = (TextView) findViewById(R.id.title);
        titleTextView.setText(title);

        ImageView imageView = (ImageView) findViewById(R.id.image);
        String imagePath = documentsDirectory + title;
        Log.v("imagePath", imagePath);
        final BitmapFactory.Options options = new BitmapFactory.Options();
        //options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);
        imageView.setImageBitmap(bitmap);
    }

}
