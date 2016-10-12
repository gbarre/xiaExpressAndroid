package fr.ac_versailles.dane.xiaexpress;

import android.nfc.Tag;

import java.io.File;

import static fr.ac_versailles.dane.xiaexpress.dbg.*;

/**
 *  Util.java
 *  MainActivity
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

class Util {

    static void createDirectory(String directory) {
        String TAG = Thread.currentThread().getStackTrace()[2].getClassName()+"."+Thread.currentThread().getStackTrace()[2].getMethodName();
        File cDirectory = new File(directory);
        dbg.enable = true;
        boolean success;
        if (!cDirectory.exists()) {
            success = cDirectory.mkdir();
            if (success) {
                // Do something on success
                pt(TAG, directory + " created");
            } else {
                // Do something else on failure
                pt(TAG, directory + " not created");
            }
        }
        else {
            pt(TAG, directory + " already exist");
        }
    }
}
