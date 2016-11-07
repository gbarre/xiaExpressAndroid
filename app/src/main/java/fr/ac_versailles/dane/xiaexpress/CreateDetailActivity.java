package fr.ac_versailles.dane.xiaexpress;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListPopupWindow;
import android.widget.RelativeLayout;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static fr.ac_versailles.dane.xiaexpress.Util.*;
import static fr.ac_versailles.dane.xiaexpress.dbg.pt;

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

public class CreateDetailActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private String imagesDirectory;
    private String xmlDirectory;
    //private String cacheDirectory;

    private Integer index = 0;
    private Document xml;
    private String fileName = "";
    private String filePath = "";
    private String fileTitle = "";
    private float movingPoint = -1; // Id of point
    private float movingCoordsX = 0;
    private float movingCoordsY = 0;
    private Boolean landscape = false;
    private float precisionDist = 20;

    private Map<Integer, xiaDetail> details = new HashMap<>();
    private Integer currentDetailTag = 0;
    private Integer detailToSegue = 0;
    private Boolean createDetail = false;
    //private Point beginTouchLocation = new Point(0, 0); // old bad idea
    private float editDetail = -1;
    private Boolean moveDetail = false;
    private Map<Integer, ImageView> virtPoints = new HashMap<>();
    private ArrayList<Integer> polygonPointsOrder;

    //private ImageView imgView;// = new ImageView(this); // UIImageView(frame: CGRect(x: 0, y: 0, width: 0, height: 0))
    //private img = UIImage()
    private float toolbarHeight = 0;
    private RelativeLayout detailsArea;
    private float scale = 1;
    private float xMin = 0;
    private float yMin = 0;
    private DisplayMetrics metrics;
    float cornerWidth = 0;
    float cornerHeight = 0;

    //private menu: UIAlertController!
    ListPopupWindow listPopupWindow;
    String[] detailsType;
    private int btnTag = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // full screen, no title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_create_detail);

        String rectangle = getResources().getString(R.string.rectangle);
        String ellipse = getResources().getString(R.string.ellipse);
        String polygon = getResources().getString(R.string.polygon);
        detailsType = new String[]{rectangle, ellipse, polygon};

        setBtnsIcons();


        String TAG = Thread.currentThread().getStackTrace()[2].getClassName()+"."+Thread.currentThread().getStackTrace()[2].getMethodName();

        String rootDirectory = String.valueOf(getExternalFilesDir(null)) + File.separator;
        imagesDirectory = Constants.getImagesFrom(rootDirectory);
        xmlDirectory = Constants.getXMLFrom(rootDirectory);
        //cacheDirectory = Constants.getCacheFrom(rootDirectory);

        fileName = getIntent().getStringExtra("title");
        fileTitle = fileName.replace(".jpg", "");
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus){
        // This is done after onCreate
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbarHeight = myToolbar.getBottom();
        metrics = getResources().getDisplayMetrics();
        Bitmap corner = BitmapFactory.decodeResource(getResources(), R.drawable.corner);
        cornerWidth = corner.getWidth();
        cornerHeight = corner.getHeight();

        loadBackground(imagesDirectory + fileName);
        detailsArea = (RelativeLayout) findViewById(R.id.detailsArea);

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
        float locationX = event.getX();
        float locationY = event.getY() - toolbarHeight;
        // TODO let touchedVirtPoint = touchesVirtPoint(location)
        float touchedVirtPoint = -1;

        switch (maskedAction) {

            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN: {
                if (createDetail) {
                    Integer detailPoints = details.get(currentDetailTag).points.size();
                    Boolean addPoint = false;

                    if ( detailPoints != 0 && touchedVirtPoint == -1) { // Points exists
                        // Are we in the polygon ?
                        if (detailPoints > 2) {
                            if (pointInPolygon(details.get(currentDetailTag).points, locationX, locationY)) {
                                // beginTouchLocation = location old bad idea
                                movingCoordsX = locationX;
                                movingCoordsY = locationY;
                                moveDetail = true;
                                movingPoint = -1;
                            }
                            else {
                                addPoint = true;
                            }
                        }

                        for(Map.Entry<Integer, ImageView> entry : details.get(currentDetailTag).points.entrySet()) {
                            Integer id = entry.getKey();
                            ImageView point = entry.getValue();

                            float dist = distance(locationX, locationY, point.getX(), point.getY());
                            if ( dist < precisionDist ) { // We are close to an exiting point, move it
                                point.setX(locationX);
                                point.setY(locationY);
                                details.get(currentDetailTag).points.put(id, point);
                                movingPoint = id;
                                moveDetail = false;
                                addPoint = false;
                                break;
                            }
                            else {
                                addPoint = true;
                            }
                        }
                    }
                    if (touchedVirtPoint != -1) {
                        moveDetail = false;
                        addPoint = false;
                    }
                    if ( (addPoint || detailPoints == 0 || touchedVirtPoint != -1) && !moveDetail )  {
                        if (detailPoints == 0) {
                            polygonPointsOrder = new ArrayList<Integer>();
                        }
                        Integer nbPoints = details.get(currentDetailTag).points.size();
                        movingPoint = (touchedVirtPoint == -1) ? nbPoints : (touchedVirtPoint + 1);
                        if (touchedVirtPoint != -1) {
                            // Change indexes of next points
                            Integer i = nbPoints;
                            while (i > touchedVirtPoint - 1) {
                                details.get(currentDetailTag).points.put(i+1, details.get(currentDetailTag).points.get(i));
                                i = i - 1;
                                if (i > touchedVirtPoint) {
                                    polygonPointsOrder.set(i, polygonPointsOrder.get(i) + 1);
                                }
                            }
                        }

                        // Add new point
                        ImageView newPoint = details.get(currentDetailTag).createPoint(locationX, locationY, R.drawable.corner, Math.round(movingPoint), this);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            newPoint.setZ(1);
                        }
                        polygonPointsOrder.add(Math.round(movingPoint));

                        for (int i = 0; i < detailsArea.getChildCount(); i++) {
                            View child = detailsArea.getChildAt(i);
                            Integer childTag = (Integer) child.getTag();
                            if (childTag.equals(currentDetailTag + 100)) {
                                detailsArea.removeView(child);
                            }
                        }
                        ImageView newShape = details.get(currentDetailTag).createShape(this,true, Color.RED, cornerWidth, cornerHeight, metrics, toolbarHeight, false, details.get(currentDetailTag).locked);
                        detailsArea.addView(newShape);
                        detailsArea.addView(newPoint);
                    }
                }
                else {
                    int touchedTag;

                    // Look if we try to move a detail
                    for(Map.Entry<Integer, xiaDetail> entry : details.entrySet()) {
                        Integer detailTag = entry.getKey();
                        xiaDetail detailPoints = entry.getValue();

                        Boolean touchIn = pointInPolygon(detailPoints.points, locationX, locationY);
                        if (touchIn) {
                            touchedTag = detailTag;
                            //beginTouchLocation = location; // old bad idea
                            editDetail = touchedTag;
                            currentDetailTag = touchedTag;
                            movingCoordsX = locationX;
                            movingCoordsY = locationY;
                            moveDetail = !detailPoints.locked;
                            changeDetailColor(Math.round(editDetail));
                            break;
                        }
                    }

                    // Should we move an existing point ?
                    if (currentDetailTag != 0 && !details.get(currentDetailTag).locked) {
                        movingPoint = -1;
                        for (Map.Entry<Integer, ImageView> entry : details.get(currentDetailTag).points.entrySet()) {
                            Integer id = entry.getKey();
                            ImageView point = entry.getValue();

                            float dist = distance(locationX, locationY, point.getX(), point.getY());
                            if (dist < precisionDist) { // We are close to an exiting point, move it
                                if (details.get(currentDetailTag).constraint.equals(Constants.constraintPolygon)) { // avoid that the touch point is moving without constraint
                                    point.setX(locationX);
                                    point.setY(locationY);
                                }
                                else {
                                    point.setX(point.getX());
                                    point.setY(point.getY());
                                }

                                details.get(currentDetailTag).points.put(id, point);
                                movingPoint = id;
                                moveDetail = false;
                                break;
                            } else { // No point here, just move the detail
                                moveDetail = !details.get(currentDetailTag).locked;
                            }
                        }
                    }

                    // Should we add a virtual point ?
                    /* TODO add virtpoint LATER
                    if touchedVirtPoint != -1 {
                        moveDetail = false
                        let nbPoints = (details["\(currentDetailTag)"]?.points.count)!
                                movingPoint = touchedVirtPoint + 1
                        // Change indexes of next points
                        var i = nbPoints
                        while i > touchedVirtPoint-1 {
                            details["\(currentDetailTag)"]?.points[i+1] = details["\(currentDetailTag)"]?.points[i]
                            i = i - 1
                        }

                        // Add new point
                        let newPoint = details["\(currentDetailTag)"]?.createPoint(location, imageName: "corner", index: movingPoint)
                        newPoint?.layer.zPosition = 1
                        imgView.addSubview(newPoint!)

                        // Remove old polygon
                        for subview in imgView.subviews {
                            if subview.tag == (currentDetailTag + 100) {
                                subview.removeFromSuperview()
                            }
                        }
                        buildShape(true, color: editColor, tag: currentDetailTag, points: details["\(currentDetailTag)"]!.points, parentView: imgView, locked: details["\(currentDetailTag)"]!.locked)
                    }*/
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: { // a pointer was moved
                if ( movingPoint != -1 && currentDetailTag != 0 && !details.get(currentDetailTag).locked ) {
                    movingPoint = Math.round(movingPoint);
                    float plocX = details.get(currentDetailTag).points.get(Math.round(movingPoint)).getX();
                    float plocY = details.get(currentDetailTag).points.get(Math.round(movingPoint)).getY();

                    float dist = distance(locationX, locationY, plocX, plocY);
                    if ( dist < 200 ) {
                        ImageView toMove = details.get(currentDetailTag).points.get(Math.round(movingPoint));
                        int previousPoint = Math.round(mod(Math.round(movingPoint + 3), 4));
                        int nextPoint = Math.round(mod(Math.round(movingPoint + 1), 4));
                        int oppositePoint = Math.round(mod(Math.round(movingPoint + 2), 4));

                        // Are there any constraint ?
                        if (details.get(currentDetailTag).constraint.equals(Constants.constraintRectangle)) {
                            if (movingPoint % 2 == 0) {
                                details.get(currentDetailTag).points.get(previousPoint).setX(locationX);
                                details.get(currentDetailTag).points.get(previousPoint).setY(details.get(currentDetailTag).points.get(previousPoint).getY());

                                details.get(currentDetailTag).points.get(nextPoint).setX(details.get(currentDetailTag).points.get(nextPoint).getX());
                                details.get(currentDetailTag).points.get(nextPoint).setY(locationY);
                            } else {
                                details.get(currentDetailTag).points.get(previousPoint).setX(details.get(currentDetailTag).points.get(previousPoint).getX());
                                details.get(currentDetailTag).points.get(previousPoint).setY(locationY);

                                details.get(currentDetailTag).points.get(nextPoint).setX(locationX);
                                details.get(currentDetailTag).points.get(nextPoint).setY(details.get(currentDetailTag).points.get(nextPoint).getY());
                            }
                            toMove.setX(locationX);
                            toMove.setY(locationY);
                            details.get(currentDetailTag).points.put(Math.round(movingPoint), toMove);
                        }
                        else if (details.get(currentDetailTag).constraint.equals(Constants.constraintEllipse)) {
                            if (movingPoint % 2 == 0) {
                                float middleHeight = (details.get(currentDetailTag).points.get(oppositePoint).getY() - locationY)/2 + locationY;
                                toMove.setX(plocX);
                                toMove.setY(locationY);
                                details.get(currentDetailTag).points.get(Math.round(movingPoint)).setX(plocX);
                                details.get(currentDetailTag).points.get(Math.round(movingPoint)).setY(details.get(currentDetailTag).points.get(Math.round(movingPoint)).getY());
                                details.get(currentDetailTag).points.get(previousPoint).setX(details.get(currentDetailTag).points.get(previousPoint).getX());
                                details.get(currentDetailTag).points.get(previousPoint).setY(middleHeight);
                                details.get(currentDetailTag).points.get(nextPoint).setX(details.get(currentDetailTag).points.get(nextPoint).getX());
                                details.get(currentDetailTag).points.get(nextPoint).setY(middleHeight);
                            } else {
                                float middleWidth = (details.get(currentDetailTag).points.get(oppositePoint).getX() - locationX)/2 + locationX;
                                toMove.setX(locationX);
                                toMove.setY(plocY);
                                details.get(currentDetailTag).points.get(Math.round(movingPoint)).setX(details.get(currentDetailTag).points.get(Math.round(movingPoint)).getX());
                                details.get(currentDetailTag).points.get(Math.round(movingPoint)).setY(plocY);
                                details.get(currentDetailTag).points.get(previousPoint).setX(middleWidth);
                                details.get(currentDetailTag).points.get(previousPoint).setY(details.get(currentDetailTag).points.get(previousPoint).getY());
                                details.get(currentDetailTag).points.get(nextPoint).setX(middleWidth);
                                details.get(currentDetailTag).points.get(nextPoint).setY(details.get(currentDetailTag).points.get(nextPoint).getY());
                            }
                        }
                        else {
                            toMove.setX(locationX);
                            toMove.setY(locationY);
                            details.get(currentDetailTag).points.put(Math.round(movingPoint), toMove);
                        }
                    }
                }

                if ( (createDetail && moveDetail) || (editDetail != -1) ) {
                    if (moveDetail) {
                        movingPoint = -1;
                        float deltaX = locationX - movingCoordsX;
                        float deltaY = locationY - movingCoordsY;
                        for (int j = 0; j < detailsArea.getChildCount(); j++) {
                            View child = detailsArea.getChildAt(j);
                            Integer childTag = (Integer) child.getTag();
                            if (childTag.equals(currentDetailTag) || childTag.equals(currentDetailTag + 100)) {
                                float destX = child.getX() + deltaX;
                                float destY = child.getY() + deltaY;
                                child.setX(destX);
                                child.setY(destY);
                            }
                        }
                        movingCoordsX = locationX;
                        movingCoordsY = locationY;
                    }
                }

                if (currentDetailTag != 0 && details.get(currentDetailTag).points.size() > 2) {
                    // rebuild points & shape
                    for (int j = 0; j < detailsArea.getChildCount(); j++) {
                        View child = detailsArea.getChildAt(j);
                        Integer childTag = (Integer) child.getTag();
                        if (childTag.equals(currentDetailTag + 100)) { // remove shape
                            detailsArea.removeView(child);
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && childTag.equals(currentDetailTag)) { // points to top
                            child.setTranslationZ(1);
                        }
                    }

                    Boolean drawEllipse = (details.get(currentDetailTag).constraint.equals(Constants.constraintEllipse));
                    ImageView newShape = details.get(currentDetailTag).createShape(this,true, Color.RED, cornerWidth, cornerHeight, metrics, toolbarHeight, drawEllipse, details.get(currentDetailTag).locked);
                    detailsArea.addView(newShape);
                }
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL: {
                if (currentDetailTag > 99) {
                    if (details.get(currentDetailTag).points.size() > 2) {
                        // rebuild points & shape
                        for (int j = 0; j < detailsArea.getChildCount(); j++) {
                            View child = detailsArea.getChildAt(j);
                            Integer childTag = (Integer) child.getTag();
                            if (childTag.equals(currentDetailTag + 100)) { // remove shape
                                detailsArea.removeView(child);
                            }
                        }

                        Boolean drawEllipse = (details.get(currentDetailTag).constraint.equals(Constants.constraintEllipse));
                        ImageView newShape = details.get(currentDetailTag).createShape(this,true, Color.RED, cornerWidth, cornerHeight, metrics, toolbarHeight, drawEllipse, details.get(currentDetailTag).locked);
                        detailsArea.addView(newShape);

                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) { // redraw points
                            String[] pointsArray = details.get(currentDetailTag).path.split(" ");
                            if (pointsArray.length > 2) {
                                Integer pointIndex = 0;
                                for (String aPointsArray : pointsArray) {
                                    String[] coords = aPointsArray.split(";");
                                    if (coords.length == 2) {
                                        Float x = Float.parseFloat(coords[0]) * scale + xMin - cornerWidth / 2;
                                        Float y = Float.parseFloat(coords[1]) * scale + yMin - cornerHeight / 2;
                                        ImageView newPoint = details.get(currentDetailTag).createPoint(x, y, R.drawable.corner, pointIndex, this);
                                        newPoint.setVisibility(View.VISIBLE);
                                        detailsArea.addView(newPoint);
                                        pointIndex = pointIndex + 1;
                                    }
                                }
                            }
                        }

                        // TODO virtpoints

                    /*let locked = details["\(currentDetailTag)"]!.locked
                    if (details["\(currentDetailTag)"]?.constraint == constraintPolygon && !locked) {
                        virtPoints = details["\(currentDetailTag)"]!.makeVirtPoints()
                        for virtPoint in virtPoints {
                            imgView.addSubview(virtPoint.1)
                        }
                    }*/

                        // Save the detail in xml
                        NodeList xmlDetails = xml.getElementsByTagName("detail");
                        for (int i = 0; i < xmlDetails.getLength(); i++) {
                            Node detail = xmlDetails.item(i);
                            NamedNodeMap detailAttr = detail.getAttributes();
                            Node tag = detailAttr.getNamedItem("tag");
                            int detailTag = Integer.valueOf(tag.getTextContent());
                            if (detailTag == currentDetailTag) {
                                Node detailPath = detailAttr.getNamedItem("path");
                                detailPath.setTextContent(details.get(currentDetailTag).createPath(xMin - cornerWidth/2, yMin - cornerHeight/2));
                                Node detailConstraint = detailAttr.getNamedItem("constraint");
                                detailConstraint.setTextContent(details.get(currentDetailTag).constraint);
                            }
                        }
                        writeXML(xml, xmlDirectory + fileTitle + ".xml");
                    }
                }

                if (createDetail) {
                    moveDetail = false;
                }
                else {
                    if (editDetail == -1 && movingPoint == -1) {
                        changeDetailColor(-1);
                        currentDetailTag = 0;
                        moveDetail = false;
                    }
                    else {
                        editDetail = -1;
                    }
                }

                setBtnsIcons();
                break;
            }
        }


        return true;
    }



    private void addDetail(int type) throws InterruptedException {
        createDetail = true;
        setBtnsIcons();

        // Prepare new detail
        for (int i = 100; i < 200; i++) {
            if (details.get(i) == null) {
                currentDetailTag = i;
                break;
            }
        }
        // TODO send alert if i >= 200

        // prepare new xml detail
        Map<String, String> attributes = new HashMap<>();
        attributes.put("tag", currentDetailTag.toString());
        attributes.put("zoom", "true");
        attributes.put("title", "");
        attributes.put("path", "0;0");
        attributes.put("locked", "false");
        attributes.put("constraint", "polygon"); // by default

        Node xmlDetails = xml.getElementsByTagName("details").item(0);
        Element xmlNewDetail = xml.createElement("detail");
        for(Map.Entry<String, String> entry : attributes.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            xmlNewDetail.setAttribute(key, value);
        }
        xmlDetails.appendChild(xmlNewDetail);

        // Create new detail
        xiaDetail newDetail = new xiaDetail(currentDetailTag, scale);
        details.put(currentDetailTag, newDetail);

        changeDetailColor(currentDetailTag);

        switch (type) {
            case 0: // rectangle
                details.get(currentDetailTag).constraint = Constants.constraintRectangle;

                // Build the rectangle
                details.get(currentDetailTag).createPoint(100, 30, R.drawable.corner, 0, CreateDetailActivity.this);
                details.get(currentDetailTag).createPoint(300, 30, R.drawable.corner, 1, CreateDetailActivity.this);
                details.get(currentDetailTag).createPoint(300, 150, R.drawable.corner, 2, CreateDetailActivity.this);
                details.get(currentDetailTag).createPoint(100, 150, R.drawable.corner, 3, CreateDetailActivity.this);

                //details.get(currentDetailTag).createShape(CreateDetailActivity.this, true, Color.RED, cornerWidth, cornerHeight, metrics, toolbarHeight, false, false);

                stopCreation();

                // Save the detail in xml
                NodeList xmlR = xml.getElementsByTagName("detail");
                for (int i = 0; i < xmlR.getLength(); i++) {
                    Node d = xmlR.item(i);
                    NamedNodeMap dAttr = d.getAttributes();
                    Node t = dAttr.getNamedItem("tag");
                    int dTag = Integer.valueOf(t.getTextContent());
                    if (dTag == currentDetailTag) {
                        Node dPath = dAttr.getNamedItem("path");
                        dPath.setTextContent(details.get(currentDetailTag).createPath(xMin - cornerWidth/2, yMin - cornerHeight/2));
                        Node dConstraint = dAttr.getNamedItem("constraint");
                        dConstraint.setTextContent(details.get(currentDetailTag).constraint);
                        break;
                    }
                }

                writeXML(xml, xmlDirectory + fileTitle + ".xml");

                break;
            case 1: // ellipse
                details.get(currentDetailTag).constraint = Constants.constraintEllipse;

                // Build the "ellipse"
                details.get(currentDetailTag).createPoint(300, 50, R.drawable.corner, 0, CreateDetailActivity.this);
                details.get(currentDetailTag).createPoint(400, 110, R.drawable.corner, 1, CreateDetailActivity.this);
                details.get(currentDetailTag).createPoint(300, 170, R.drawable.corner, 2, CreateDetailActivity.this);
                details.get(currentDetailTag).createPoint(200, 110, R.drawable.corner, 3, CreateDetailActivity.this);

                //details.get(currentDetailTag).createShape(CreateDetailActivity.this, true, Color.RED, cornerWidth, cornerHeight, metrics, toolbarHeight, true, false);

                stopCreation();

                // Save the detail in xml
                NodeList xmlE = xml.getElementsByTagName("detail");
                for (int i = 0; i < xmlE.getLength(); i++) {
                    Node d = xmlE.item(i);
                    NamedNodeMap dAttr = d.getAttributes();
                    Node t = dAttr.getNamedItem("tag");
                    int dTag = Integer.valueOf(t.getTextContent());
                    if (dTag == currentDetailTag) {
                        Node dPath = dAttr.getNamedItem("path");
                        dPath.setTextContent(details.get(currentDetailTag).createPath(xMin - cornerWidth/2, yMin - cornerHeight/2));
                        Node dConstraint = dAttr.getNamedItem("constraint");
                        dConstraint.setTextContent(details.get(currentDetailTag).constraint);
                        break;
                    }
                }

                writeXML(xml, xmlDirectory + fileTitle + ".xml");

                break;
            case 2: // polygon
                details.get(currentDetailTag).constraint = Constants.constraintPolygon;
                createDetail = true;
                setBtnsIcons();

                /* TODO Disable other gesture
                if let recognizers = self.view.gestureRecognizers {
                for recognizer in recognizers {
                    self.view.removeGestureRecognizer(recognizer)
                }
                }*/
                break;
        }
    }

    private void changeDetailColor(Integer tag) {
        pt("changeDetailColor", "for tag", tag);
        for(Map.Entry<Integer, xiaDetail> entry : details.entrySet()) {
            Integer thisDetailTag = entry.getKey();
            xiaDetail detail = entry.getValue();
            // Remove and rebuild the shape to avoid the overlay on alpha channel
            for (Integer i = 0; i < detailsArea.getChildCount(); i++) {
                View child = detailsArea.getChildAt(i);
                Integer childTag = (Integer) child.getTag();
                if (childTag.equals(thisDetailTag + 100)) { // polygon
                    child.setTag(thisDetailTag + 300);
                    child.setVisibility(View.GONE);
                }
                if (childTag.equals(thisDetailTag)) { // points
                    if (thisDetailTag.equals(tag)) {
                        child.setVisibility(View.VISIBLE);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            child.setTranslationZ(1);
                        }
                    }
                    else {
                        child.setVisibility(View.GONE);
                    }
                }
            }

            if (detail.points.size() > 2) {
                Boolean drawEllipse = (detail.constraint.equals(Constants.constraintEllipse));
                if (thisDetailTag.equals(tag)) {
                    ImageView detailShape = detail.createShape(this, true, Color.RED, cornerWidth, cornerHeight, metrics, toolbarHeight, drawEllipse, detail.locked);
                    detailsArea.addView(detailShape);
                }
                else {
                    ImageView detailShape = detail.createShape(this, true, Color.GREEN, cornerWidth, cornerHeight, metrics, toolbarHeight, drawEllipse, detail.locked);
                    detailsArea.addView(detailShape);
                }
            }
            else { // only 1 or 2 points, remove them
                for (int i = 0; i < detailsArea.getChildCount(); i++) {
                    View child = detailsArea.getChildAt(i);
                    if (child.getTag() == thisDetailTag) {
                        detailsArea.removeView(child);
                    }
                }
            }
        }

        /* TODO toolbar color for polygon creation
        if createDetail && details["\(tag)"]?.constraint == constraintPolygon {
            imgTopBarBkgd.backgroundColor = editColor
        }
        else {
            imgTopBarBkgd.backgroundColor = blueColor
        }*/
        //cleanOldViews(299);
    }

    private void cleaningDetails() {
        for(Map.Entry<Integer, xiaDetail> entry : details.entrySet()) {
            Integer detailTag = entry.getKey();
            xiaDetail detail = entry.getValue();
            if ( detailTag != 0 && detail.points.size() < 3 ) {
                // TODO performFullDetailRemove(detailTag);
            }
        }
    }

    private void cleanOldViews(int minTag) {
        // Remove old (hidden) subviews
        for (int i = 0; i < detailsArea.getChildCount(); i++) {
            View child = detailsArea.getChildAt(i);
            if ((Integer) child.getTag() > minTag) {
                detailsArea.removeView(child);
            }
        }
    }

    private float distance(float xA, float yA, float xB, float yB) {
        return (float) Math.sqrt((xA-xB)*(xA-xB)+(yA-yB)*(yA-yB));
    }

    private void loadBackground(String imagePath) {
        ImageView imageView = (ImageView) findViewById(R.id.image);

        float availableWidth = metrics.widthPixels;
        float availableHeight = metrics.heightPixels - toolbarHeight;

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
                            newPoint.setVisibility(View.INVISIBLE);
                            detailsArea.addView(newPoint);
                            pointIndex = pointIndex + 1;
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

                    ImageView newShape = details.get(detailTag).createShape(this,true, Color.GREEN, cornerWidth, cornerHeight, metrics, toolbarHeight, drawEllipse, details.get(detailTag).locked);
                    detailsArea.addView(newShape);

                    // TODO attainable points
                }
            }
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

        // Build the addDetail menu
        listPopupWindow = new ListPopupWindow(this);
        listPopupWindow.setAdapter(new ArrayAdapter(CreateDetailActivity.this, R.layout.list_item, detailsType));
        listPopupWindow.setAnchorView(btAddDetail);
        listPopupWindow.setWidth(185);
        //listPopupWindow.setHeight(500);
        listPopupWindow.setVerticalOffset(10);
        listPopupWindow.setModal(true);
        listPopupWindow.setOnItemClickListener(this);


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
                listPopupWindow.show();
            }
        });

        btOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopCreation();
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        listPopupWindow.dismiss();
        try {
            addDetail(position);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void stopCreation() {
        createDetail = false;
        // TODO performFullDetailRemove(currentDetailTag)
        if (details.get(currentDetailTag).constraint.equals(Constants.constraintPolygon)) {
            currentDetailTag = 0;
            changeDetailColor(-1);
        }
        //imgTopBarBkgd.backgroundColor = blueColor
        setBtnsIcons();

        /* TODO Add double tap gesture
        let dSelector : Selector = #selector(ViewCreateDetails.detailInfos)
        let doubleTapGesture = UITapGestureRecognizer(target: self, action: dSelector)
        doubleTapGesture.numberOfTapsRequired = 2
        view.addGestureRecognizer(doubleTapGesture)
        */
    }

    public void showPopUp() {

        AlertDialog.Builder helpBuilder = new AlertDialog.Builder(this);
        helpBuilder.setTitle("Pop Up");
        helpBuilder.setMessage("This is a Simple Pop Up");
        helpBuilder.setPositiveButton("Rectangle", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing but close the dialog
             //   addDetail(0);
            }
        });
        helpBuilder.setNegativeButton("Ellipse", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing
               // addDetail(1);
            }
        });

        helpBuilder.setNeutralButton("Polygon", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing
               // addDetail(2);
            }
        });

        // Remember, create doesn't show the dialog
        AlertDialog helpDialog = helpBuilder.create();
        helpDialog.show();

    }
}
