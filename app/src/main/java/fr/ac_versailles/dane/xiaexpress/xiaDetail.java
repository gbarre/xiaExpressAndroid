package fr.ac_versailles.dane.xiaexpress;

import android.content.Context;
import android.widget.ImageView;

import java.util.HashMap;
import java.util.Map;

/**
 * .java
 * XiaExpress
 * <p>
 * Created by guillaume on 24/10/2016.
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

class xiaDetail {

    public Map<Integer, ImageView> points = new HashMap<>();
    public Integer tag = 0;
    public double scale = 1.0;
    public String constraint = "";
    public Boolean locked = false;

    public xiaDetail(Integer tag, double scale) {
        this.tag = tag;
        points.clear();
        this.scale = scale;
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
