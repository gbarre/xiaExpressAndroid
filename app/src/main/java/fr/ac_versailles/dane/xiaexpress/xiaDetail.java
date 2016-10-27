package fr.ac_versailles.dane.xiaexpress;

import android.content.Context;
import android.widget.ImageView;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * xiaDetail.java
 * XiaExpress
 *
 * Created by guillaume on 24/10/2016.
 *
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

class xiaDetail {

    public Map<Integer, ImageView> points = new HashMap<>();
    public Integer tag = 0;
    public float scale = 1;
    public String constraint = "";
    public Boolean locked = false;

    public xiaDetail(Integer tag, float scale) {
        this.tag = tag;
        points.clear();
        this.scale = scale;
    }

    public String createPath(float xMin, float ymin){
        if (points.size() < 2) {
            return "0;0";
        }
        else {

            String path = "";
            SortedSet<Integer> keys = new TreeSet<>(points.keySet());
            for (Integer key : keys) {
                ImageView point = points.get(key);
                float x = (point.getX() - xMin) / scale;
                float y = (point.getY() - ymin) / scale;
                path = path + x + ";" + y + " ";
            }

            return path.trim(); // return X1.xxx;Y1.yyy X2.xxx;Y2.yyy X3.xxx;Y3.yyy ...
        }
    }

    public ImageView createPoint(float x, float y, int ResId, Integer index, Context ctx) {
        ImageView image = new ImageView(ctx);
        image.setImageResource(ResId);
        image.setX(x);
        image.setY(y);
        image.setTag(this.tag);
        points.put(index, image);
        return image;
    }

}
