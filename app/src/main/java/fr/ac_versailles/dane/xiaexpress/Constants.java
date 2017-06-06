package fr.ac_versailles.dane.xiaexpress;

import android.app.Application;
import android.content.Context;
import android.graphics.Color;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

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

    public static final String constraintRectangle = "rectangle";
    public static final String constraintEllipse = "ellipse";
    public static final String constraintPolygon = "polygon";

    public static final Map<String, String> xmlElementsDict = new HashMap<>();

    public static final int blue = Color.argb(255, 0, 153, 204);
    public static final int red = Color.argb(255, 255, 0, 0);
    public static final int green = Color.argb(255, 0, 255, 0);
    public static final int white = Color.argb(255, 255, 255, 355);
    public static int darkBlue = Color.argb(255, 0, 102, 153);
    public static int orange = Color.argb(255, 255, 131, 0);
    public static int black = Color.argb(255, 0, 0, 0);

    public static void buildXMLElements(Context ctx) {
        //xmlElementsDict.put("license", ctx.getString(R.string.license));
        xmlElementsDict.put("title", ctx.getString(R.string.title));
        //xmlElementsDict.put("date", ctx.getString(R.string.date));
        xmlElementsDict.put("creator", ctx.getString(R.string.creator));
        xmlElementsDict.put("rights", ctx.getString(R.string.rights));
        xmlElementsDict.put("publisher", ctx.getString(R.string.publisher));
        xmlElementsDict.put("identifier", ctx.getString(R.string.identifier));
        xmlElementsDict.put("source", ctx.getString(R.string.source));
        xmlElementsDict.put("relation", ctx.getString(R.string.relation));
        xmlElementsDict.put("language", ctx.getString(R.string.languages));
        xmlElementsDict.put("keywords", ctx.getString(R.string.keywords));
        xmlElementsDict.put("coverage", ctx.getString(R.string.coverage));
        xmlElementsDict.put("contributors", ctx.getString(R.string.contributors));
        xmlElementsDict.put("description", ctx.getString(R.string.description));
    }


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
