package fr.ac_versailles.dane.xiaexpress;

import android.app.Application;

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

    private static Boolean enableDebug = false;

    public static Boolean getEnableDebug() {
        return enableDebug;
    }

    public void setEnableDebug(Boolean enable) {
        enableDebug = enable;
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
