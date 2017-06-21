package fr.ac_versailles.dane.xiaexpress;

import android.graphics.Bitmap;

/**
 *
 *  PhotoThumbnail.java
 *  XiaExpress
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

class PhotoThumbnail {
    private Bitmap image;
    private String filename;

    public PhotoThumbnail(Bitmap image, String filename) {
        super();
        this.image = image;
        this.filename = filename;
    }

    public Bitmap getImage() {
        return image;
    }

    public String getFilename() {
        return filename;
    }
}
