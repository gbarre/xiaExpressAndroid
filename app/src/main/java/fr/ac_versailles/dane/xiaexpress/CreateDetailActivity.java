package fr.ac_versailles.dane.xiaexpress;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static fr.ac_versailles.dane.xiaexpress.Util.*;
import static fr.ac_versailles.dane.xiaexpress.dbg.*;

/**
 *  CreateDetailActivity.java
 *  xia-android
 *
 *  Created by guillaume on 29/09/2016.
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

public class CreateDetailActivity extends AppCompatActivity {

    private String rootDirectory;
    private String imagesDirectory;
    private String xmlDirectory;
    //private String cacheDirectory;

    private Integer index = 0;
    private Document xml;
    private String fileName = "";
    private String filePath = "";
    private String fileTitle = "";
    private Point location = new Point(0, 0);
    private float movingPoint = -1; // Id of point
    private Point movingCoords = new Point(0, 0);
    private Boolean landscape = false;

    private Map<Integer, xiaDetail> details = new HashMap<>();
    private Integer currentDetailTag = 0;
    private Integer detailToSegue = 0;
    private Boolean createDetail = false;
    private Point beginTouchLocation = new Point(0, 0);
    private float editDetail = -1;
    private Boolean moveDetail = false;
    private Map<Integer, ImageView> virtPoints = new HashMap<>();
    private Integer[] polygonPointsOrder = new Integer[9999];

    //private ImageView imgView;// = new ImageView(this); // UIImageView(frame: CGRect(x: 0, y: 0, width: 0, height: 0))
    //private img = UIImage()
    private float toolbarHeight = 0;
    private RelativeLayout detailsArea;
    private float scale = 1;
    private float xMin = 0;
    private float yMin = 0;

    //private menu: UIAlertController!
    private int btnTag = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_detail);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbarHeight = myToolbar.getHeight();

        setBtnsIcons();

        detailsArea = (RelativeLayout) findViewById(R.id.detailsArea);

        String TAG = Thread.currentThread().getStackTrace()[2].getClassName()+"."+Thread.currentThread().getStackTrace()[2].getMethodName();

        rootDirectory = String.valueOf(getExternalFilesDir(null)) + File.separator;
        imagesDirectory = Constants.getImagesFrom(rootDirectory);
        xmlDirectory = Constants.getXMLFrom(rootDirectory);
        //cacheDirectory = Constants.getCacheFrom(rootDirectory);

        fileName = getIntent().getStringExtra("title");
        fileTitle = fileName.replace(".jpg", "");
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus){
        // This is done after onCreate
        loadBackground(imagesDirectory + fileName);
        xml = getXMLFromPath(xmlDirectory + fileTitle + ".xml");
        loadDetails(xml);
        cleaningDetails(); // remove details with 1 or 2 points
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        String TAG = Thread.currentThread().getStackTrace()[2].getClassName()+"."+Thread.currentThread().getStackTrace()[2].getMethodName();

        // get pointer index from the event object
        int pointerIndex = event.getActionIndex();

        // get pointer ID
        int pointerId = event.getPointerId(pointerIndex);

        // get masked (not specific to a pointer) action
        int maskedAction = event.getActionMasked();
        float eventX = event.getX();
        float eventY = event.getY() - toolbarHeight;

        switch (maskedAction) {

            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN: {
                // TODO use data
                pt(TAG, "onTouch " + String.valueOf(pointerIndex));
                pt(TAG, "coords" + String.valueOf(eventX + ";" + eventY));
                /*float dist = distance(eventX, eventY, point.getX(), point.getY());
                pt(TAG, "dist" + String.valueOf(dist));
                if ( dist < 50) {
                    pt(TAG, "touch point");
                    movePoint = true;
                    point.setX(eventX);
                    point.setY(eventY);
                }
                else { // add image
                    movePoint = false;
                }*/

                break;
            }
            case MotionEvent.ACTION_MOVE: { // a pointer was moved
                // TODO use data
                pt(TAG, "onTouch action move");
                /*if (movePoint) {
                    point.setX(eventX);
                    point.setY(eventY);
                }*/
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL: {
                // TODO use data
                pt(TAG, "onTouch action up");
                /*if (movePoint) {
                    movePoint = false;
                }
                else {
                    ImageView newPoint = new ImageView(this);
                    newPoint.setImageResource((R.drawable.corner_x1));
                    newPoint.setX(500);
                    newPoint.setY(200);
                    RelativeLayout detailsArea = (RelativeLayout) findViewById(R.id.detailsArea);
                    detailsArea.addView(newPoint);

                    /*RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(500, 200);
                    lp.addRule(RelativeLayout.CENTER_IN_PARENT); // A position in layout.
                    newPoint.setLayoutParams(lp);
// imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    imageView.setImageResource(R.drawable.photo);
                    RelativeLayout layout = (RelativeLayout) findViewById(R.id.layout);
                    layout.addView(imageView);*/

                    pt(TAG, "Add point here");
//                }
                break;
            }
        }


        return true;
    }



    private void addDetail() {
        createDetail = true;
        setBtnsIcons();
    }

    private void cleaningDetails() {
        String TAG = Thread.currentThread().getStackTrace()[2].getClassName()+"."+Thread.currentThread().getStackTrace()[2].getMethodName();
        for(Map.Entry<Integer, xiaDetail> entry : details.entrySet()) {
            Integer detailTag = entry.getKey();
            xiaDetail detail = entry.getValue();
            if ( detailTag != 0 && detail.points.size() < 3 ) {
                // TODO performFullDetailRemove(detailTag);
            }
        }
    }

    private float distance(float xA, float yA, float xB, float yB) {
        return (float) Math.sqrt((xA-xB)*(xA-xB)+(yA-yB)*(yA-yB));
    }

    private void loadBackground(String imagePath) {
        String TAG = Thread.currentThread().getStackTrace()[2].getClassName()+"."+Thread.currentThread().getStackTrace()[2].getMethodName();

        ImageView imageView = (ImageView) findViewById(R.id.image);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float availableWidth = metrics.widthPixels;
        float availableHeight = metrics.heightPixels - toolbarHeight * metrics.scaledDensity;

        //final BitmapFactory.Options options = new BitmapFactory.Options();
        //options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);

        float scaleX = availableWidth / bitmap.getWidth();
        float scaleY = availableHeight / bitmap.getHeight();
        scale = Math.min(scaleX, scaleY);

        xMin = (scaleX == scale) ? 0 : (availableWidth - bitmap.getWidth()*scale) / 2;
        yMin = (scaleY == scale) ? 0 : (availableHeight - bitmap.getHeight()*scale) / 2;

        imageView.setImageBitmap(bitmap);
    }

    private void loadDetails(Document xml) {
        String TAG = Thread.currentThread().getStackTrace()[2].getClassName()+"."+Thread.currentThread().getStackTrace()[2].getMethodName();

        NodeList xmlDetails = xml.getElementsByTagName("detail");
        Bitmap corner = BitmapFactory.decodeResource(getResources(), R.drawable.corner);
        float cornerWidth = corner.getWidth();
        float cornerHeight = corner.getHeight();
        for (int i = 0; i < xmlDetails.getLength(); i++) {
            pt(TAG, "Detail " + i);
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
                // Add points to detail
                String[] pointsArray = path.split(" ");
                if (pointsArray.length > 2) {
                    Integer pointIndex = 0;
                    for (String aPointsArray : pointsArray) {
                        String[] coords = aPointsArray.split(";");
                        if (coords.length == 2) {
                            Float x = Float.parseFloat(coords[0]) * scale + xMin - cornerWidth / 2;
                            Float y = Float.parseFloat(coords[1]) * scale + yMin - cornerHeight / 2;
                            ImageView newPoint = details.get(detailTag).createPoint(x, y, R.drawable.corner, pointIndex, this);
                            // TODO check for zPosition & hidden
                            detailsArea.addView(newPoint);
                        }
                    }
                    String constraint = detailAttr.getNamedItem("constraint").getTextContent();
                    if (constraint.equals("")) {
                        details.get(detailTag).constraint = Constants.constraintPolygon;
                    }
                    else {
                        details.get(detailTag).constraint = (!constraint.equals(Constants.constraintPolygon) && pointsArray.length != 4) ? Constants.constraintPolygon : constraint;
                    }
                    Boolean drawEllipse = (details.get(detailTag).constraint.equals(Constants.constraintEllipse));
                    details.get(detailTag).locked = (detailAttr.getNamedItem("locked").getTextContent().equals("true"));
                    // TODO buildShape(true, color: noEditColor, tag: detailTag, points: details["\(detailTag)"]!.points, parentView: imgView, ellipse: drawEllipse, locked: details["\(detailTag)"]!.locked)
                    // TODO attainable points
                }
            }


            String description = detail.getTextContent();


            pt(TAG, "Path : " + path);
            pt(TAG, "Description : " + description);

        }
    }

    private void setBtnsIcons() {

        // Buttons
        Button btCollection = (Button) findViewById(R.id.collection);
        final ImageButton btAddDetail = (ImageButton) findViewById(R.id.addDetail);
        ImageButton btUndo = (ImageButton) findViewById(R.id.undo);
        ImageButton btPlay = (ImageButton) findViewById(R.id.play);
        Button btOK = (Button) findViewById(R.id.ok);
        ImageButton btExport = (ImageButton) findViewById(R.id.export);
        ImageButton btEdit = (ImageButton) findViewById(R.id.edit);
        ImageButton btTrash = (ImageButton) findViewById(R.id.trash);

        if (createDetail) {
            btAddDetail.setVisibility(View.GONE);
            btPlay.setVisibility(View.GONE);

            btUndo.setVisibility(View.VISIBLE);
            btOK.setVisibility(View.VISIBLE);
            btTrash.setVisibility(View.VISIBLE);
        }
        else {
            btAddDetail.setVisibility(View.VISIBLE);
            btPlay.setVisibility(View.VISIBLE);

            btUndo.setVisibility(View.GONE);
            btOK.setVisibility(View.GONE);
            btTrash.setVisibility(View.GONE);
        }

        btCollection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btAddDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addDetail();
            }
        });

        btOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopCreation();
            }
        });





    }

    private void stopCreation() {
        createDetail = false;
        setBtnsIcons();
    }

    public void showPopUp() {

        AlertDialog.Builder helpBuilder = new AlertDialog.Builder(this);
        helpBuilder.setTitle("Pop Up");
        helpBuilder.setMessage("This is a Simple Pop Up");
        helpBuilder.setPositiveButton("Positive",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing but close the dialog

                    }
                });

        helpBuilder.setNegativeButton("Negative", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing
            }
        });

        helpBuilder.setNeutralButton("Neutral", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing
            }
        });

        // Remember, create doesn't show the dialog
        AlertDialog helpDialog = helpBuilder.create();
        helpDialog.show();

    }

}
