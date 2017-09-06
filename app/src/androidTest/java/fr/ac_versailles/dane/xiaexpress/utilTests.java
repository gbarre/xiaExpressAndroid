package fr.ac_versailles.dane.xiaexpress;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.DisplayMetrics;

import org.junit.Test;
import org.junit.runner.RunWith;

import static fr.ac_versailles.dane.xiaexpress.Util.mod;
import static fr.ac_versailles.dane.xiaexpress.Util.pointInPolygon;
import static junit.framework.Assert.assertEquals;

/**
 * utilTests.java
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
public class utilTests {

    private final DisplayMetrics metrics = new DisplayMetrics();
    private final Context ctx = InstrumentationRegistry.getTargetContext();
    private final xiaDetail singlePointDetail = new xiaDetail(1, 1, 0, 20, 20, metrics, ctx);
    private final xiaDetail multiplePointsDetail = new xiaDetail(1, 1, 0, 20, 20, metrics, ctx);

    public utilTests() {
        // Single point detail
        singlePointDetail.createPoint(10, 10, 0, ctx);

        // Multiple points detail
        multiplePointsDetail.createPoint(10, 10, 0, ctx);
        multiplePointsDetail.createPoint(200, 150, 1, ctx);
        multiplePointsDetail.createPoint(120, 150, 2, ctx);
    }

    @Test
    public void modTest() throws Exception {
        int output = mod(5, 3);
        int expectedOutput = 2;
        assertEquals(output, expectedOutput);
    }

    @Test
    public void testUtilPointInPolygon() throws Exception {
        // point in
        Boolean outputIn = pointInPolygon(multiplePointsDetail.points, 130, 140);
        Boolean expectedOutputIn= true;
        assertEquals(outputIn, expectedOutputIn);

        // point out
        Boolean outputOut = pointInPolygon(multiplePointsDetail.points, 0, 0);
        Boolean expectedOutputOut = false;
        assertEquals(outputOut, expectedOutputOut);
    }
}
