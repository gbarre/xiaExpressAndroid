package fr.ac_versailles.dane.xiaexpress;

import org.json.JSONObject;

/**
 * TextConverter.java
 * XiaExpress
 * <p>
 * Created by guillaume on 18/05/2017.
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 * @author : guillaume.barre@ac-versailles.fr
 */

public class TextConverter {
    private float videoWidth = 480;
    private float videoHeight = 270;

    TextConverter(float w, float h) {
        videoWidth = w;
        videoHeight = h;
    }

    private JSONObject getJson(String url) {
        JSONObject json = new JSONObject();

        return json;
    }
}
