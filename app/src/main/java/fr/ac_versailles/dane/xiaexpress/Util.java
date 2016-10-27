package fr.ac_versailles.dane.xiaexpress;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import static fr.ac_versailles.dane.xiaexpress.dbg.*;

/**
 *  Util.java
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

class Util {

    static void buildShape(Context context, Boolean fill, int color, Integer tag, Map<Integer, ImageView> points, RelativeLayout parentView, Boolean ellipse, Boolean locked) {
        Integer shapeArg = 0;
        Integer shapeTag = tag + 100;
        if (fill) {
            shapeArg = (ellipse) ? 3 : 1;
        }
        else {
            shapeArg = (ellipse) ? 2 : 0;
        }

        float xMin = Float.MAX_VALUE;
        float xMax = 0;
        float yMin = Float.MAX_VALUE;
        float yMax = 0;
        // Get dimensions of the shape
        for (int i = 0; i < parentView.getChildCount(); i++) {
            View subview = parentView.getChildAt(i);
            if (subview.getTag() == tag) {
                float xMinSubview = subview.getX();
                float yMinSubview = subview.getY();
                float xMaxSubview = xMinSubview + 10;
                float yMaxSubview = yMinSubview + 10;
                if ( xMinSubview < xMin ) {
                    xMin = xMinSubview;
                }
                if ( yMinSubview < yMin ) {
                    yMin = yMinSubview;
                }
                if ( xMaxSubview > xMax ) {
                    xMax = xMaxSubview;
                }
                if ( yMaxSubview > yMax ) {
                    yMax = yMaxSubview;
                }
            }
        }
        Rect shapeFrame = new Rect(Math.round(xMin), Math.round(yMin), Math.round(xMax), Math.round(yMax));

        // Build the shape
        View myView = new ShapeView(context, shapeFrame, shapeArg, points, color);

        /*myView.backgroundColor = UIColor(white: 0, alpha: 0)
        myView.tag = shapeTag
        parentView.addSubview(myView)

        // Shape is locked ?
        if locked {
            let lock = UIImage(named: "lock")
            let lockView = UIImageView(image: lock!)
            lockView.center = CGPoint(x: shapeFrame.midX, y: shapeFrame.midY)
            lockView.tag = shapeTag
            lockView.layer.zPosition = 105
            lockView.alpha = 0.5
            parentView.addSubview(lockView)
        }*/
    }

    static void createDirectory(String directory) {
        String TAG = Thread.currentThread().getStackTrace()[2].getClassName()+"."+Thread.currentThread().getStackTrace()[2].getMethodName();
        File cDirectory = new File(directory);
        boolean success;
        if (!cDirectory.exists()) {
            success = cDirectory.mkdir();
            if (success) {
                // Do something on success
                pt(TAG, directory, "created");
            } else {
                // Do something else on failure
                pt(TAG, directory, "not created");
            }
        }
        else {
            pt(TAG, directory, "already exist");
        }
    }

    static Document getXMLFromPath(String filepath) {
        File xmlFile = new File(filepath);
        Document xml = null;
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            xml = docBuilder.parse(xmlFile);
        } catch (SAXException | ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return xml;
    }

    static int mod(int x, int y)
    {
        int result = x % y;
        return result < 0? result + y : result;
    }

    static Boolean pointInPolygon(Map<Integer, ImageView> points, float touchPointX, float touchPointY) {
        String TAG = Thread.currentThread().getStackTrace()[2].getClassName()+"."+Thread.currentThread().getStackTrace()[2].getMethodName();
        // translate from C : http://alienryderflex.com/polygon/
        int polyCorners = points.size();
        int j = polyCorners - 1;
        Boolean oddNodes = false;
        //pt(TAG, "touchPoint", "(" + touchPointX + ";" + touchPointY + ")");
        for (int i = 0; i < polyCorners; i++) {
            //pt(TAG, "point " + i, points.get(i).getX() + ";" + points.get(i).getY());
            if ( (points.get(i).getY() < touchPointY && points.get(j).getY() >= touchPointY
                    || points.get(j).getY() < touchPointY && points.get(i).getY() >= touchPointY)
            && (points.get(i).getX() <= touchPointX || points.get(j).getX() <= touchPointX) ) {
                if ( points.get(i).getX() + (touchPointY - points.get(i).getY()) / (points.get(j).getY() - points.get(i).getY()) * (points.get(j).getX() - points.get(i).getX()) < touchPointX ) {
                    oddNodes = !oddNodes;
                }
            }
            j=i;
        }

        return oddNodes;
    }

    static String readFromFile(InputStream inputStream) {
        String TAG = Thread.currentThread().getStackTrace()[2].getClassName()+"."+Thread.currentThread().getStackTrace()[2].getMethodName();
        String ret = "";

        try {

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ret;
    }

    static void writeXML(Document xml, String filepath) {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(xml);
            StreamResult result = new StreamResult(new File(filepath));
            transformer.transform(source, result);

        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }
}