package fr.ac_versailles.dane.xiaexpress;

import android.content.Context;
import android.graphics.Color;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.DisplayMetrics;
import android.widget.ImageView;

import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;

/**
 * xiaDetailTest.java
 * XiaExpress
 *
 * Created by guillaume on 02/11/2016.
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

@RunWith(AndroidJUnit4.class)
public class xiaDetailTests {

    private final Context ctx = InstrumentationRegistry.getTargetContext();
    private final DisplayMetrics metrics = new DisplayMetrics();
    private final xiaDetail singlePointDetail = new xiaDetail(1, 1, 0, 20, 20, metrics, ctx);
    private final xiaDetail multiplePointsDetail = new xiaDetail(1, 1, 0, 20, 20, metrics, ctx);
    private final xiaDetail ellipsePointsDetail = new xiaDetail(1, 1, 0, 20, 20, metrics, ctx);

    public xiaDetailTests() {
        // Single point detail
        singlePointDetail.createPoint(10, 10, 0, ctx);

        // Multiple points detail
        multiplePointsDetail.createPoint(10, 10, 0, ctx);
        multiplePointsDetail.createPoint(200, 150, 1, ctx);
        multiplePointsDetail.createPoint(120, 150, 2, ctx);

        // Ellipse points detail
        ellipsePointsDetail.createPoint(200, 200, 0, ctx);
        ellipsePointsDetail.createPoint(350, 400, 1, ctx);
        ellipsePointsDetail.createPoint(200, 600, 2, ctx);
        ellipsePointsDetail.createPoint(50, 400, 3, ctx);

    }

    @Test
    public void testXiaDetailCreatePath() throws Exception {
        // Single point
        String output = singlePointDetail.createPath(0, 0);

        String expectedOutput = "0;0";
        assertEquals(output, expectedOutput);

        // Multiple points
        String output3pts = multiplePointsDetail.createPath(0, 0);
        String expectedOutput3pts = "10.0;10.0 200.0;150.0 120.0;150.0";
        assertEquals(output3pts, expectedOutput3pts);
    }

    @Test
    public void testXiaDetailCreatePoint() throws Exception {
        ImageView output = singlePointDetail.points.get(0);

        ImageView expectedOutput = new ImageView(ctx);
        expectedOutput.setImageResource(R.drawable.corner);
        expectedOutput.setX(10);
        expectedOutput.setY(10);
        expectedOutput.setTag(1);

        assertEquals(output.getTag(), expectedOutput.getTag());
        assertEquals(output.getX(), expectedOutput.getX());
        assertEquals(output.getY(), expectedOutput.getY());
        assertEquals(output.getBackground(), expectedOutput.getBackground());
    }

    @Test
    public void testXiaDetailCreateShapeEllipse() throws Exception {
        ImageView outputEllipse = ellipsePointsDetail.createShape(true, Color.GREEN, true);

        ImageView expectedEllipse = new ImageView(ctx);
        expectedEllipse.setX(60);
        expectedEllipse.setY(210);
        expectedEllipse.setTag(101);

        assertEquals(outputEllipse.getX(), expectedEllipse.getX());
        assertEquals(outputEllipse.getY(), expectedEllipse.getY());
        assertEquals(outputEllipse.getTag(), expectedEllipse.getTag());
    }

    @Test
    public void testXiaDetailCreateShapePolygon() throws Exception {
        ImageView outputEllipse = multiplePointsDetail.createShape(true, Color.GREEN, false);

        ImageView expectedPolygon = new ImageView(ctx);
        expectedPolygon.setX(0);
        expectedPolygon.setY(0);
        expectedPolygon.setTag(101);

        assertEquals(outputEllipse.getX(), expectedPolygon.getX());
        assertEquals(outputEllipse.getY(), expectedPolygon.getY());
        assertEquals(outputEllipse.getTag(), expectedPolygon.getTag());
    }

}
