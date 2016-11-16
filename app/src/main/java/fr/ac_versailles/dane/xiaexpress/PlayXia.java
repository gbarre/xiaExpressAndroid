package fr.ac_versailles.dane.xiaexpress;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * PlayXia.java
 * XiaExpress
 *
 * Created by guillaume on 16/11/2016.
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

public class PlayXia extends AppCompatActivity {

    private Document xml;
    private String fileTitle = "";
    private String imagesDirectory;
    private String xmlDirectory = "";

    private DisplayMetrics metrics;
    private float scale = 1;
    private float xMin = 0;
    private float yMin = 0;
    float cornerWidth = 0;
    float cornerHeight = 0;

    private Map<Integer, xiaDetail> details = new HashMap<>();
    private RelativeLayout detailsArea;
    private Boolean detailsLoaded = false;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // full screen, no title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_play_xia);

        String rootDirectory = String.valueOf(getExternalFilesDir(null)) + File.separator;
        imagesDirectory = Constants.getImagesFrom(rootDirectory);
        xmlDirectory = Constants.getXMLFrom(rootDirectory);

        fileTitle = getIntent().getStringExtra("fileTitle");
        xml = Util.getXMLFromPath(xmlDirectory + fileTitle + ".xml");
        dbg.pt("PlayXia", "xml", Util.nodeToString(xml.getDocumentElement()));
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus){
        if (hasFocus && !detailsLoaded) {
            // This is done after onCreate
            metrics = getResources().getDisplayMetrics();
            Bitmap corner = BitmapFactory.decodeResource(getResources(), R.drawable.corner);
            cornerWidth = corner.getWidth();
            cornerHeight = corner.getHeight();

            loadBackground(imagesDirectory + fileTitle + ".jpg");
            detailsArea = (RelativeLayout) findViewById(R.id.detailsArea);

            loadDetails(this.xml);
        }
    }

    private void loadBackground(String imagePath) {
        ImageView imageView = (ImageView) findViewById(R.id.image);

        float availableWidth = metrics.widthPixels;
        float availableHeight = metrics.heightPixels;

        final BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);

        float scaleX = availableWidth / bitmap.getWidth();
        float scaleY = availableHeight / bitmap.getHeight();
        scale = Math.min(scaleX, scaleY);

        xMin = (scaleX == scale) ? 0 : (availableWidth - bitmap.getWidth()*scale) / 2;
        yMin = (scaleY == scale) ? 0 : (availableHeight - bitmap.getHeight()*scale) / 2;

        dbg.pt("PlayXia", "xMin", xMin);
        dbg.pt("PlayXia", "yMin", yMin);


        bitmap = null;

        options.inSampleSize = Util.calculateInSampleSize(options, Math.round(availableWidth) - 1, Math.round(availableHeight) - 1);
        options.inJustDecodeBounds = false;
        Bitmap image = BitmapFactory.decodeFile(imagePath, options);

        imageView.setImageBitmap(image);
    }

    private void loadDetails(Document xml) {
        NodeList xmlDetails = xml.getElementsByTagName("detail");
        for (int i = 0; i < xmlDetails.getLength(); i++) {
            Node detail = xmlDetails.item(i);
            NamedNodeMap detailAttr = detail.getAttributes();
            Node pathNode = detailAttr.getNamedItem("path");
            String path = pathNode.getTextContent();
            if (!"".equals(path)) { // we have a path, try to draw it...
                // Get detail tag
                Node tag = detailAttr.getNamedItem("tag");
                int detailTag = Integer.valueOf(tag.getTextContent());
                // clean all subview with this tag
                for (int j = 0; j < detailsArea.getChildCount(); j++) {
                    View child = detailsArea.getChildAt(j);
                    if ((Integer) child.getTag() == detailTag || (Integer) child.getTag() == detailTag + 100) {
                        detailsArea.removeView(child);
                    }
                }

                xiaDetail newDetail = new xiaDetail(detailTag, scale);
                details.put(detailTag, newDetail);
                details.get(detailTag).path = path;
                String constraint = detailAttr.getNamedItem("constraint").getTextContent();
                details.get(detailTag).constraint = constraint;

                // Add points to detail
                String[] pointsArray = path.split(" ");
                Integer pointIndex = 0;
                for (String aPointsArray : pointsArray) {
                    String[] coords = aPointsArray.split(";");
                    Float x = Float.parseFloat(coords[0]) * scale + xMin - cornerWidth / 2;
                    Float y = Float.parseFloat(coords[1]) * scale + yMin - cornerHeight / 2;
                    dbg.pt("PlayXia", "coords " + pointIndex, Float.parseFloat(coords[0]) + ";" + Float.parseFloat(coords[1]));
                    dbg.pt("PlayXia", "point " + pointIndex, x + ";" + y);
                    ImageView newPoint = details.get(detailTag).createPoint(x, y, R.drawable.corner, pointIndex, this);
                    newPoint.setVisibility(View.INVISIBLE);
                    detailsArea.addView(newPoint);
                    pointIndex = pointIndex + 1;

                }
                Boolean drawEllipse = (details.get(detailTag).constraint.equals(Constants.constraintEllipse));

                ImageView newShape = details.get(detailTag).createShape(this,false, Constants.blue, cornerWidth, cornerHeight, metrics, 0, drawEllipse, details.get(detailTag).locked);
                detailsArea.addView(newShape);

            }
        }
        detailsLoaded = true;
    }
}
