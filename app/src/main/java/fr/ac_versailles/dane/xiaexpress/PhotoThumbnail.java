package fr.ac_versailles.dane.xiaexpress;

import android.graphics.Bitmap;

/**
 * Created by guillaume on 29/09/2016.
 */
public class PhotoThumbnail {
    private Bitmap image;
    private String title;

    public PhotoThumbnail(Bitmap image, String title) {
        super();
        this.image = image;
        this.title = title;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

}
