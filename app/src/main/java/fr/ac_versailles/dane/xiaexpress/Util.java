package fr.ac_versailles.dane.xiaexpress;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.text.Html;
import android.text.Spanned;
import android.widget.ImageView;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.StringWriter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
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

    static String cleanInput(String strIn) {
        String out = strIn;
        out = out.replace(" ", "_");
        Pattern regex = Pattern.compile("[^\\w\\.\\-\\_]");
        Matcher results = regex.matcher(strIn);
        while (results.find()) {
            String result = results.group();
            out = out.replace(result, "");
        }
        if (out.length() > 45) {
            return out.substring(0, 45);
        } else return out;
    }

    static void copy(InputStream in, File dst) throws IOException {
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
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

    static void createXiaXML(String filePath) {
        String xmlString = "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\"?>\n" +
                "<xia>\n" +
                "\t<title></title>\n" +
                "\t<description></description>\n" +
                "\t<creator></creator>\n" +
                "\t<rights></rights>\n" +
                "\t<license></license>\n" +
                "\t<date></date>\n" +
                "\t<publisher></publisher>\n" +
                "\t<identifier></identifier>\n" +
                "\t<source></source>\n" +
                "\t<relation></relation>\n" +
                "\t<language></language>\n" +
                "\t<keywords></keywords>\n" +
                "\t<coverage></coverage>\n" +
                "\t<contributors></contributors>\n" +
                "\t<readonly code=\"1234\">false</readonly>\n" +
                "\t<image description=\"\" title=\"\"/>\n" +
                "\t<details show=\"true\">\n" +
                "\t</details>\n" +
                "</xia>";
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(filePath));
            outputStreamWriter.write(xmlString);
            outputStreamWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void removeFile(String filepath) {
        File file = new File(filepath);
        if (!file.delete()) {
            dbg.pt("removeFile", "error", filepath);
        }
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

    static String getNodeAttribute(Document xml, String node, String attribute) {
        String attributeValue = "";
        NodeList nodes = xml.getElementsByTagName(node);
        Node firstNode = nodes.item(0);
        NamedNodeMap nodeAttr = firstNode.getAttributes();
        Node attr = nodeAttr.getNamedItem(attribute);
        attributeValue = attr.getTextContent();
        return attributeValue;
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

    static boolean isOnline(Context c) {
        ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = null;
        if (cm != null) {
            netInfo = cm.getActiveNetworkInfo();
        }
        return netInfo != null && netInfo.isConnectedOrConnecting();
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
            if ((points.get(i).getY() < touchPointY && points.get(j).getY() >= touchPointY
                    || points.get(j).getY() < touchPointY && points.get(i).getY() >= touchPointY)
                    && (points.get(i).getX() <= touchPointX || points.get(j).getX() <= touchPointX)) {
                if (points.get(i).getX() + (touchPointY - points.get(i).getY()) / (points.get(j).getY() - points.get(i).getY()) * (points.get(j).getX() - points.get(i).getX()) < touchPointX) {
                    oddNodes = !oddNodes;
                }
            }
            j = i;
        }

        return oddNodes;
    }

    static Document setNodeAttribute(Document xml, String node, String attribute, String value) {
        NodeList nodes = xml.getElementsByTagName(node);
        Node firstNode = nodes.item(0);
        NamedNodeMap nodeAttr = firstNode.getAttributes();
        Node attr = nodeAttr.getNamedItem(attribute);
        attr.setTextContent(value);
        return xml;
    }

    static Document setNodeValue(Document xml, String node, String value) {
        NodeList nodes = xml.getElementsByTagName(node);
        Node firstNode = nodes.item(0);
        firstNode.setTextContent(value);
        return xml;
    }

    static Intent share(String filePath, String fileTitle) {
        File fileIn = new File(filePath);
        Uri uri = Uri.fromFile(fileIn);
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, fileTitle);
        shareIntent.setType("text/plain");
        return shareIntent;
    }

    static void string2File(String inStr, String filepath) {
        try {
            FileOutputStream outputStream = new FileOutputStream(filepath);
            PrintStream printStream = new PrintStream(outputStream);
            printStream.println(inStr);
            printStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
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

    static String xml2String(Document xml) {

        try {
            StringWriter sw = new StringWriter();
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer;
            transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

            transformer.transform(new DOMSource(xml), new StreamResult(sw));
            return sw.toString();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return "";
    }
}