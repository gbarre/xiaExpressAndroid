package fr.ac_versailles.dane.xiaexpress;

import android.app.Application;
import android.graphics.Color;

import java.io.File;

/**
 *  dbg.java
 *  xia-android
 *
 *  Created by guillaume on 12/10/2016.
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

public class Constants extends Application {

    public static String constraintRectangle = "rectangle";
    public static String constraintEllipse = "ellipse";
    public static String constraintPolygon = "polygon";

    public static int blue = Color.argb(255, 0, 153, 204);
    public static int darkBlue = Color.argb(255, 0, 102, 153);
    public static int orange = Color.argb(255, 255, 131, 0);
    public static int red = Color.argb(255, 255, 0, 0);
    public static int green = Color.argb(255, 0, 255, 0);
    public static int white = Color.argb(255, 255, 255, 355);
    public static int black = Color.argb(255, 0, 0, 0);

    public static Boolean getEnableDebug() {
        return true;
    }

    public static String getImagesFrom(String rootDirectory) {
        return rootDirectory + "images" + File.separator;
    }

    public static String getXMLFrom(String rootDirectory) {
        return rootDirectory + "xml" + File.separator;
    }

    public static String getCacheFrom(String rootDirectory) {
        return rootDirectory + "cache" + File.separator;
    }

}
