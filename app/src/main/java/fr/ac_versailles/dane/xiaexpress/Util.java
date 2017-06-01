package fr.ac_versailles.dane.xiaexpress;

import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.Html;
import android.text.Spanned;
import android.widget.ImageView;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import static fr.ac_versailles.dane.xiaexpress.dbg.pt;

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

class Util extends Activity {

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

    static String getNodeValue(Document xml, String node) {
        String value = "";
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        try {
            XPathExpression expr = xpath.compile(node);
            value = expr.evaluate(xml);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        return value;
    }

    static Document getXMLFromPath(String filepath) {
        File xmlFile = new File(filepath);
        Document xml = null;
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            xml = docBuilder.parse(xmlFile);
        } catch (SAXException | ParserConfigurationException | IOException e) {
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
        // translate from C : http://alienryderflex.com/polygon/
        int polyCorners = points.size();
        int j = polyCorners - 1;
        Boolean oddNodes = false;
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

    static void writeXML(Document xml, String filepath) {
        //pt("writeXML", "xml", nodeToString(xml.getDocumentElement()));
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

    static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

    // http://stackoverflow.com/questions/37904739/html-fromhtml-deprecated-in-android-n
    @SuppressWarnings("deprecation")
    static Spanned fromHtml(String html) {
        Spanned result;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            result = Html.fromHtml(html);
        }
        return result;
    }

    static boolean isOnline(Context c) {
        ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}