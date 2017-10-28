package fr.ac_versailles.dane.xiaexpress;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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

    static final int EXPORT_REQUEST = 2;
    private final float precisionDist = 30;
    private final Map<Integer, xiaDetail> details = new HashMap<>();
    private Map<Integer, ImageView> virtPoints = new HashMap<>();
    private ImageView imgTopBarBkgd;
    private float cornerWidth = 0;
    private float cornerHeight = 0;
    private ListPopupWindow listPopupWindow;
    private String[] detailsType;
    private ListPopupWindow exportPopupWindow;
    private String[] exportsType;
    private String imagesDirectory;
    private String xmlDirectory;
    private String cacheDirectory;
    private Integer index = 0;
    private Document xml;
    private String fileName = "";
    private String tmpFilePath;
    private String fileTitle = "";
    private float movingPoint = -1; // Id of point
    private float movingCoordsX = 0;
    private float movingCoordsY = 0;
    private Integer currentDetailTag = 0;
    private Boolean createDetail = false;
    private float editDetail = -1;
    private Boolean moveDetail = false;
    private ArrayList<Integer> polygonPointsOrder;
    private Boolean detailsLoaded = false;
    private float toolbarHeight = 0;
    private RelativeLayout detailsArea;
    private float scale = 1;
    private float xMin = 0;
    private float yMin = 0;
    private DisplayMetrics metrics;
    private ProgressBar mProgressBar;
    private GestureDetector gestureDetector;

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
        exportsType = new String[]{getResources().getString(R.string.xia_tablet), getResources().getString(R.string.inkscape)};

        imgTopBarBkgd = findViewById(R.id.imgTopBarBkgd);
        imgTopBarBkgd.setBackgroundColor(Color.TRANSPARENT);
        setBtnsIcons();

        String rootDirectory = String.valueOf(getExternalFilesDir(null)) + File.separator;
        imagesDirectory = Constants.getImagesFrom(rootDirectory);
        xmlDirectory = Constants.getXMLFrom(rootDirectory);
        cacheDirectory = Constants.getCacheFrom(rootDirectory);

        fileName = getIntent().getStringExtra("filename");
        fileTitle = fileName.replace(Constants.JPG_EXTENSION, "");

        gestureDetector = new GestureDetector(this, new GestureListener());
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus){
        // Always lok for xml on focus changed
        this.xml = Util.getXMLFromPath(xmlDirectory + fileTitle + Constants.XML_EXTENSION);
        if (hasFocus) {
            TextView menuTitle = findViewById(R.id.menuTitle);
            menuTitle.setText(Util.getNodeValue(xml, "xia/title"));
            menuTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    detailInfos();
                }
            });
            if (!detailsLoaded) {
                // This is done after onCreate
                Toolbar myToolbar = findViewById(R.id.toolbar);
                toolbarHeight = myToolbar.getBottom();
                // Get device infos
                metrics = getResources().getDisplayMetrics();
                // Get corner size
                Bitmap corner = BitmapFactory.decodeResource(getResources(), R.drawable.corner);
                cornerWidth = corner.getWidth();
                cornerHeight = corner.getHeight();
                // Load elements from res
                detailsArea = findViewById(R.id.detailsArea);
                mProgressBar = findViewById(R.id.progressBar);
                ImageView background = findViewById(R.id.image);
                // AsyncTask loading => background resizing + details
                loadResource loading = new loadResource(background);
                loading.execute();

                cleaningDetails(); // remove details with 1 or 2 points
            }
            else {
                // Just update currentDetail locked attribute in details
                NodeList xmlDetails = this.xml.getElementsByTagName("detail");
                for (int i = 0; i < xmlDetails.getLength(); i++) {
                    Node detail = xmlDetails.item(i);
                    NamedNodeMap detailAttr = detail.getAttributes();
                    Node tag = detailAttr.getNamedItem("tag");
                    Integer detailTag = Integer.valueOf(tag.getTextContent());
                    if (detailTag.equals(currentDetailTag)) {
                        details.get(detailTag).locked = (detailAttr.getNamedItem("locked").getTextContent().equals("true"));
                        break;
                    }
                }
                changeDetailColor(currentDetailTag);
                setBtnsIcons();
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // get masked (not specific to a pointer) action
        int maskedAction = event.getActionMasked();
        float locationX = event.getX();
        float locationY = event.getY() - toolbarHeight;
        float touchedVirtPoint = touchesVirtPoint(locationX, locationY);

        switch (maskedAction) {

            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN: {
                if (createDetail) {
                    details.get(currentDetailTag).constraint = Constants.constraintPolygon;
                    Integer detailPoints = details.get(currentDetailTag).points.size();
                    Boolean addPoint = false;

                    if (detailPoints != 0) { // Points exists
                        // Are we in the polygon ?
                        if (detailPoints > 2) {
                            if (Util.pointInPolygon(details.get(currentDetailTag).points, locationX, locationY)) {
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
                    if ((addPoint || detailPoints == 0) && !moveDetail) {
                        if (detailPoints == 0) {
                            polygonPointsOrder = new ArrayList<>();
                        }
                        movingPoint = details.get(currentDetailTag).points.size();

                        // Add new point
                        ImageView newPoint = details.get(currentDetailTag).createPoint(locationX, locationY, Math.round(movingPoint), this);
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
                        ImageView newShape = details.get(currentDetailTag).createShape(true, Constants.red, false);
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

                        Boolean touchIn = Util.pointInPolygon(detailPoints.points, locationX, locationY);
                        if (touchIn) {
                            touchedTag = detailTag;
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
                    if (touchedVirtPoint != -1 && !details.get(currentDetailTag).locked) {
                        moveDetail = false;
                        int nbPoints = details.get(currentDetailTag).points.size();
                        movingPoint = touchedVirtPoint + 1;
                        // Change indexes of next points
                        int i = nbPoints - 1;
                        while (i > touchedVirtPoint) {
                            details.get(currentDetailTag).points.put(i + 1, details.get(currentDetailTag).points.get(i));
                            i = i - 1;
                        }

                        // Add new point
                        ImageView newPoint = details.get(currentDetailTag).createPoint(locationX, locationY, Math.round(movingPoint), this);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            newPoint.setZ(1);
                        }

                        // Remove old polygon
                        for (int j = 0; j < detailsArea.getChildCount(); j++) {
                            View child = detailsArea.getChildAt(j);
                            Integer childTag = (Integer) child.getTag();
                            if (childTag.equals(currentDetailTag + 100)) {
                                detailsArea.removeView(child);
                            }
                        }

                        // Draw the shape
                        ImageView newShape = details.get(currentDetailTag).createShape(true, Constants.red, false);
                        detailsArea.addView(newShape);
                        detailsArea.addView(newPoint);
                    }
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: { // a pointer was moved
                if (currentDetailTag != 0 && !details.get(currentDetailTag).locked) {
                    if (movingPoint != -1) {
                        movingPoint = Math.round(movingPoint);
                        float plocX = details.get(currentDetailTag).points.get(Math.round(movingPoint)).getX();
                        float plocY = details.get(currentDetailTag).points.get(Math.round(movingPoint)).getY();

                        float dist = distance(locationX, locationY, plocX, plocY);
                        if (dist < 200) {
                            ImageView toMove = details.get(currentDetailTag).points.get(Math.round(movingPoint));
                            int previousPoint = Math.round(Util.mod(Math.round(movingPoint + 3), 4));
                            int nextPoint = Math.round(Util.mod(Math.round(movingPoint + 1), 4));
                            int oppositePoint = Math.round(Util.mod(Math.round(movingPoint + 2), 4));

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
                            } else if (details.get(currentDetailTag).constraint.equals(Constants.constraintEllipse)) {
                                if (movingPoint % 2 == 0) {
                                    float middleHeight = (details.get(currentDetailTag).points.get(oppositePoint).getY() - locationY) / 2 + locationY;
                                    toMove.setX(plocX);
                                    toMove.setY(locationY);
                                    details.get(currentDetailTag).points.get(Math.round(movingPoint)).setX(plocX);
                                    details.get(currentDetailTag).points.get(Math.round(movingPoint)).setY(details.get(currentDetailTag).points.get(Math.round(movingPoint)).getY());
                                    details.get(currentDetailTag).points.get(previousPoint).setX(details.get(currentDetailTag).points.get(previousPoint).getX());
                                    details.get(currentDetailTag).points.get(previousPoint).setY(middleHeight);
                                    details.get(currentDetailTag).points.get(nextPoint).setX(details.get(currentDetailTag).points.get(nextPoint).getX());
                                    details.get(currentDetailTag).points.get(nextPoint).setY(middleHeight);
                                } else {
                                    float middleWidth = (details.get(currentDetailTag).points.get(oppositePoint).getX() - locationX) / 2 + locationX;
                                    toMove.setX(locationX);
                                    toMove.setY(plocY);
                                    details.get(currentDetailTag).points.get(Math.round(movingPoint)).setX(details.get(currentDetailTag).points.get(Math.round(movingPoint)).getX());
                                    details.get(currentDetailTag).points.get(Math.round(movingPoint)).setY(plocY);
                                    details.get(currentDetailTag).points.get(previousPoint).setX(middleWidth);
                                    details.get(currentDetailTag).points.get(previousPoint).setY(details.get(currentDetailTag).points.get(previousPoint).getY());
                                    details.get(currentDetailTag).points.get(nextPoint).setX(middleWidth);
                                    details.get(currentDetailTag).points.get(nextPoint).setY(details.get(currentDetailTag).points.get(nextPoint).getY());
                                }
                            } else {
                                toMove.setX(locationX);
                                toMove.setY(locationY);
                                details.get(currentDetailTag).points.put(Math.round(movingPoint), toMove);
                            }
                        }
                    }

                    if ((createDetail && moveDetail) || (editDetail != -1)) {
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

                    if (details.get(currentDetailTag).points.size() > 2) {
                        // rebuild points & shape
                        for (int j = 0; j < detailsArea.getChildCount(); j++) {
                            View child = detailsArea.getChildAt(j);
                            Integer childTag = (Integer) child.getTag();
                            if (childTag.equals(currentDetailTag + 100)) { // remove shape
                                detailsArea.removeView(child);
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && childTag.equals(currentDetailTag)) { // points to top
                                child.setZ(1);
                            }
                        }

                        Boolean drawEllipse = (details.get(currentDetailTag).constraint.equals(Constants.constraintEllipse));
                        ImageView newShape = details.get(currentDetailTag).createShape(true, Constants.red, drawEllipse);
                        detailsArea.addView(newShape);
                    }
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
                        ImageView newShape = details.get(currentDetailTag).createShape(true, Constants.red, drawEllipse);
                        detailsArea.addView(newShape);

                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) { // show points
                            for (Integer i = 0; i < detailsArea.getChildCount(); i++) {
                                View child = detailsArea.getChildAt(i);
                                Integer childTag = (Integer) child.getTag();
                                if (childTag.equals(currentDetailTag)) {
                                    child.setVisibility(View.VISIBLE);
                                }
                            }
                        }

                        // virtpoints
                        if (details.get(currentDetailTag).constraint.equals(Constants.constraintPolygon) && !details.get(currentDetailTag).locked && !createDetail) {
                            virtPoints = details.get(currentDetailTag).makeVirtPoints();
                            for (Map.Entry<Integer, ImageView> entry : virtPoints.entrySet()) {
                                ImageView point = entry.getValue();
                                detailsArea.addView(point);
                            }
                        }

                        // Save the detail in xml
                        NodeList xmlDetails = this.xml.getElementsByTagName("detail");
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
                        Util.writeXML(this.xml, xmlDirectory + fileTitle + Constants.XML_EXTENSION);
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

        return gestureDetector.onTouchEvent(event);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (listPopupWindow.isShowing()) {
            listPopupWindow.dismiss();
            addDetail(position);
        } else if (exportPopupWindow.isShowing()) {
            exportPopupWindow.dismiss();
            exportResource(position);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == EXPORT_REQUEST) {
            // Make sure the request was successful
            if (tmpFilePath != null) {
                Util.removeFile(tmpFilePath);
            }
        }
    }

    private void addDetail(int type) {
        createDetail = true;
        setBtnsIcons();

        // Prepare new detail
        for (int i = 100; i < 200; i++) {
            if (details.get(i) == null) {
                currentDetailTag = i;
                break;
            }
        }

        // Remove old views
        int iMax = detailsArea.getChildCount();
        for (int i = 0; i < iMax; i++) {
            View child = detailsArea.getChildAt(i);
            Integer childTag = (child != null) ? (Integer) child.getTag() : 9999;
            if (childTag.equals(currentDetailTag)) {
                detailsArea.removeView(child);
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

        Node xmlDetails = this.xml.getElementsByTagName("details").item(0);
        Element xmlNewDetail = this.xml.createElement("detail");
        for(Map.Entry<String, String> entry : attributes.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            xmlNewDetail.setAttribute(key, value);
        }
        xmlDetails.appendChild(xmlNewDetail);

        // Create new detail
        xiaDetail newDetail = new xiaDetail(currentDetailTag, scale, toolbarHeight, cornerWidth, cornerHeight, metrics, this);
        details.put(currentDetailTag, newDetail);

        changeDetailColor(currentDetailTag);

        switch (type) {
            case 0: // rectangle
                details.get(currentDetailTag).constraint = Constants.constraintRectangle;

                // Build the rectangle
                ImageView newPointR0 = details.get(currentDetailTag).createPoint(100, 30, 0, CreateDetailActivity.this);
                ImageView newPointR1 = details.get(currentDetailTag).createPoint(300, 30, 1, CreateDetailActivity.this);
                ImageView newPointR2 = details.get(currentDetailTag).createPoint(300, 150, 2, CreateDetailActivity.this);
                ImageView newPointR3 = details.get(currentDetailTag).createPoint(100, 150, 3, CreateDetailActivity.this);
                ImageView newShapeR = details.get(currentDetailTag).createShape(true, Constants.red, false);

                detailsArea.addView(newShapeR);
                detailsArea.addView(newPointR0);
                detailsArea.addView(newPointR1);
                detailsArea.addView(newPointR2);
                detailsArea.addView(newPointR3);
                stopCreation();

                break;
            case 1: // ellipse
                details.get(currentDetailTag).constraint = Constants.constraintEllipse;

                // Build the "ellipse"
                ImageView newPointE0 = details.get(currentDetailTag).createPoint(300, 50, 0, CreateDetailActivity.this);
                ImageView newPointE1 = details.get(currentDetailTag).createPoint(400, 110, 1, CreateDetailActivity.this);
                ImageView newPointE2 = details.get(currentDetailTag).createPoint(300, 170, 2, CreateDetailActivity.this);
                ImageView newPointE3 = details.get(currentDetailTag).createPoint(200, 110, 3, CreateDetailActivity.this);

                ImageView newShapeE = details.get(currentDetailTag).createShape(true, Constants.red, true);

                detailsArea.addView(newShapeE);
                detailsArea.addView(newPointE0);
                detailsArea.addView(newPointE1);
                detailsArea.addView(newPointE2);
                detailsArea.addView(newPointE3);
                stopCreation();

                break;
            case 2: // polygon
                details.get(currentDetailTag).constraint = Constants.constraintPolygon;
                createDetail = true;
                setBtnsIcons();
                imgTopBarBkgd.setBackgroundColor(Constants.red);

                break;
        }

        // Save the detail in xml
        NodeList xmlR = this.xml.getElementsByTagName("detail");
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

        Util.writeXML(this.xml, xmlDirectory + fileTitle + Constants.XML_EXTENSION);

    }

    private void changeDetailColor(Integer tag) {
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
                    if (thisDetailTag.equals(tag) && !detail.locked) {
                        child.setVisibility(View.VISIBLE);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            child.setZ(1);
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
                    ImageView detailShape = detail.createShape(true, Constants.red, drawEllipse);
                    detailsArea.addView(detailShape);
                }
                else {
                    int color = (detail.locked) ? Constants.orange : Constants.green;
                    ImageView detailShape = detail.createShape(true, color, drawEllipse);
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

        if (createDetail && details.get(tag).constraint.equals(Constants.constraintPolygon)) {
            imgTopBarBkgd.setBackgroundColor(Constants.red);
        }
        else {
            imgTopBarBkgd.setBackgroundColor(Color.TRANSPARENT);
        }
        cleanOldViews(299);
    }

    private void cleaningDetails() {
        for(Map.Entry<Integer, xiaDetail> entry : details.entrySet()) {
            Integer detailTag = entry.getKey();
            xiaDetail detail = entry.getValue();
            if ( detailTag != 0 && detail.points.size() < 3 ) {
                performFullDetailRemove(detailTag, false);
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

    private void deleteDetail() {
        final Integer detailTag = currentDetailTag;
        if ( detailTag != 0 ) {
            // Alert
            AlertDialog.Builder controller = new AlertDialog.Builder(this);
            controller.setTitle(R.string.warning);
            controller.setMessage(R.string.delete_detail);
            controller.setPositiveButton(getResources().getString(R.string.YES), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    createDetail = false;
                    changeDetailColor(-1);
                    imgTopBarBkgd.setBackgroundColor(Color.TRANSPARENT);
                    performFullDetailRemove(detailTag, true);
                    setBtnsIcons();
                }
            });
            controller.setNegativeButton(getResources().getString(R.string.NO), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Do nothing
                }
            });

            // Show the alert controller
            AlertDialog alertController = controller.create();
            alertController.show();
        }
    }

    private void detailInfos() {
        moveDetail = false;
        movingPoint = -1;
        Integer tmpDetailTag = currentDetailTag;
        stopCreation();
        currentDetailTag = tmpDetailTag;

        if (currentDetailTag.equals(0)) {
            //Create intent
            Intent intent = new Intent(CreateDetailActivity.this, Metas.class);
            intent.putExtra("fileTitle", fileTitle);

            //Start details activity
            startActivity(intent);
        }
        else {
            //Create intent
            Intent intent = new Intent(CreateDetailActivity.this, DetailInfos.class);
            intent.putExtra("fileTitle", fileTitle);
            intent.putExtra("tag", currentDetailTag.toString());

            //Start details activity
            startActivity(intent);
        }
    }

    private float distance(float xA, float yA, float xB, float yB) {
        return (float) Math.sqrt((xA-xB)*(xA-xB)+(yA-yB)*(yA-yB));
    }

    private void exportResource(int type) {
        String exportXMLString = "";
        String tmpTitle = Util.cleanInput(Util.getNodeValue(xml, "xia/title"));
        tmpTitle = (tmpTitle.equals("")) ? fileName : tmpTitle;
        Export export = new Export(xml, imagesDirectory + fileName + Constants.JPG_EXTENSION);
        switch (type) {
            case 0: // Xia Tablet
                exportXMLString = export.xiaTablet();
                tmpFilePath = cacheDirectory + tmpTitle + Constants.XML_EXTENSION;
                break;
            case 1: // Inkscape SVG
                exportXMLString = export.inkscape();
                tmpFilePath = cacheDirectory + tmpTitle + ".svg";
                break;
        }
        if (!exportXMLString.equals("")) {
            // write string to temp file
            Util.string2File(exportXMLString, tmpFilePath);

            // Open share Intent
            Intent shareIntent = Util.share(tmpFilePath, tmpTitle);
            startActivityForResult(Intent.createChooser(shareIntent, getResources().getString(R.string.export)), EXPORT_REQUEST);
        }
    }

    private void goForward() {
        //Create intent
        Intent intent = new Intent(CreateDetailActivity.this, PlayXia.class);
        intent.putExtra("filename", fileTitle);
        intent.putExtra("toolbarHeight", String.valueOf(toolbarHeight));

        //Start details activity
        startActivity(intent);
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
                xiaDetail newDetail = new xiaDetail(detailTag, scale, toolbarHeight, cornerWidth, cornerHeight, metrics, this);
                details.put(detailTag, newDetail);
                details.get(detailTag).path = path;
                // Add points to detail
                String[] pointsArray = path.split(" ");
                if (pointsArray.length > 2) {
                    details.get(detailTag).locked = (detailAttr.getNamedItem("locked").getTextContent().equals("true"));
                    Integer pointIndex = 0;
                    for (String aPointsArray : pointsArray) {
                        String[] coords = aPointsArray.split(";");
                        if (coords.length == 2) {
                            Float x = Float.parseFloat(coords[0]) * scale + xMin - cornerWidth / 2;
                            Float y = Float.parseFloat(coords[1]) * scale + yMin - cornerHeight / 2;
                            ImageView newPoint = details.get(detailTag).createPoint(x, y, pointIndex, this);
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

                    ImageView newShape = details.get(detailTag).createShape(true, (details.get(detailTag).locked) ? Constants.orange : Constants.green, drawEllipse);
                    detailsArea.addView(newShape);

                    // TODO attainable points
                }
            }
        }
        detailsLoaded = true;
    }

    private void performFullDetailRemove(int tag, Boolean force) {
        if (details.get(tag).points.size() < 3 || force) {
            // remove point & polygon
            int iMax = detailsArea.getChildCount();
            for (int i = 0; i < iMax; i++) {
                View child = detailsArea.getChildAt(i);
                if (child != null) {
                    Integer childTag = (Integer) child.getTag();
                    if (childTag.equals(tag) || childTag.equals(tag + 100)) {
                        child.setVisibility(View.INVISIBLE);
                        detailsArea.removeView(child);
                    }
                }
            }

            // remove detail object
            details.remove(tag);

            // remove detail in xml
            Node dets = this.xml.getElementsByTagName("details").item(0);
            NodeList xmlDetails = this.xml.getElementsByTagName("detail");
            for (int i = 0; i < xmlDetails.getLength(); i++) {
                Node detail = xmlDetails.item(i);
                NamedNodeMap detailAttr = detail.getAttributes();
                Node t = detailAttr.getNamedItem("tag");
                int detailTag = Integer.valueOf(t.getTextContent());
                if (detailTag == tag) {
                    dets.removeChild(detail);
                }
            }
            Util.writeXML(this.xml, xmlDirectory + fileTitle + Constants.XML_EXTENSION);
            currentDetailTag = 0;
        }
    }

    private void polygonUndo() {
        Integer detailTag = currentDetailTag;
        if (details.get(detailTag).points.size() > 3) {
            // remove last point
            Integer lastPoint = polygonPointsOrder.size() - 1;
            detailsArea.removeView(details.get(detailTag).points.get(lastPoint));
            details.get(detailTag).points.remove(lastPoint);

            // Update polygonPointsOrder indexes
            for (Integer id : polygonPointsOrder) {
                if (id > (lastPoint - 1) && id != polygonPointsOrder.size()) {
                    Integer newValue = polygonPointsOrder.get(id) - 1;
                    polygonPointsOrder.set(id, newValue);
                }
            }

            // Update points index
            Integer iMax = polygonPointsOrder.size() - 1;
            for (Integer i = lastPoint; i < iMax; i++){
                details.get(detailTag).points.put(i, details.get(detailTag).points.get(i + 1));
            }

            // remove last point
            polygonPointsOrder.remove(polygonPointsOrder.size() - 1);

            // Remove old polygon
            for (int j = 0; j < detailsArea.getChildCount(); j++) {
                View child = detailsArea.getChildAt(j);
                Integer childTag = (Integer) child.getTag();
                if (childTag.equals(currentDetailTag + 100)) {
                    detailsArea.removeView(child);
                }
            }
            ImageView newShape = details.get(currentDetailTag).createShape(true, Constants.red, false);
            detailsArea.addView(newShape);
        }
        setBtnsIcons();
    }

    private void setBtnsIcons() {

        // Buttons
        Button btCollection = findViewById(R.id.collection);
        ImageButton btAddDetail = findViewById(R.id.addDetail);
        ImageButton btUndo = findViewById(R.id.undo);
        ImageButton btPlay = findViewById(R.id.play);
        Button btOK = findViewById(R.id.ok);
        ImageButton btExport = findViewById(R.id.export);
        ImageButton btEdit = findViewById(R.id.edit);
        ImageButton btTrash = findViewById(R.id.trash);
        ImageButton btDebug = findViewById(R.id.debug);

        // Build the addDetail menu
        listPopupWindow = new ListPopupWindow(this);
        listPopupWindow.setAdapter(new ArrayAdapter(CreateDetailActivity.this, R.layout.list_item, detailsType));
        listPopupWindow.setAnchorView(btAddDetail);
        listPopupWindow.setWidth(185);
        listPopupWindow.setVerticalOffset(10);
        listPopupWindow.setModal(true);
        listPopupWindow.setOnItemClickListener(this);

        // Build the export menu
        exportPopupWindow = new ListPopupWindow(this);
        exportPopupWindow.setAdapter(new ArrayAdapter(CreateDetailActivity.this, R.layout.list_item, exportsType));
        exportPopupWindow.setAnchorView(btExport);
        exportPopupWindow.setWidth(185);
        exportPopupWindow.setVerticalOffset(10);
        exportPopupWindow.setModal(true);
        exportPopupWindow.setOnItemClickListener(this);


        if (createDetail) {
            btAddDetail.setVisibility(View.GONE);
            btPlay.setVisibility(View.GONE);

            btOK.setVisibility(View.VISIBLE);

        }
        else {
            btAddDetail.setVisibility(View.VISIBLE);
            btPlay.setVisibility(View.VISIBLE);

            btOK.setVisibility(View.GONE);
        }

        if (currentDetailTag != 0 &&
                createDetail &&
                details.get(currentDetailTag).constraint.equals(Constants.constraintPolygon) &&
                details.get(currentDetailTag).points.size() > 3) {
            btUndo.setVisibility(View.VISIBLE);
        }
        else {
            btUndo.setVisibility(View.GONE);
        }

        if (currentDetailTag != 0 && !details.get(currentDetailTag).locked) {
            btTrash.setVisibility(View.VISIBLE);
        }else {
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

        btUndo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                polygonUndo();
            }
        });

        btPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goForward();
            }
        });

        btOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopCreation();
            }
        });

        btExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exportPopupWindow.show();
            }
        });

        btEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detailInfos();
            }
        });

        btTrash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteDetail();
            }
        });

        btDebug.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = "Infos à transmettre au développeur :\n";
                text = text + " - Modèle : " + Build.MANUFACTURER + " - " + Build.MODEL + "\n";
                text = text + " - Version de l'API : " + android.os.Build.VERSION.SDK_INT + "\n";
                text = text + " - Résolution utilisée : " + metrics.widthPixels + " x " + metrics.heightPixels + "\n";
                text = text + " - Densité de pixel : " + metrics.densityDpi + "\n";

                PackageManager manager = CreateDetailActivity.this.getPackageManager();
                PackageInfo info;
                try {
                    info = manager.getPackageInfo(CreateDetailActivity.this.getPackageName(), 0);
                    text = text + " - PackageName : " + info.packageName + "\n";
                    text = text + " - VersionCode : " + info.versionCode + "\n";
                    text = text + " - VersionName : " + info.versionName + "\n";
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

                AlertDialog.Builder controller = new AlertDialog.Builder(CreateDetailActivity.this);
                controller.setTitle("DEBUG");
                controller.setMessage(text);
                final String finalText = text;
                controller.setPositiveButton("Copier...", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("infos", finalText);
                        clipboard.setPrimaryClip(clip);
                    }
                });
                controller.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing
                    }
                });

                // Show the alert controller
                AlertDialog alertController = controller.create();
                alertController.show();
            }
        });
    }

    private void stopCreation() {
        createDetail = false;
        Integer tempTag = currentDetailTag;
        if (currentDetailTag != 0) {
            if (details.get(currentDetailTag).constraint.equals(Constants.constraintPolygon)) {
                currentDetailTag = 0;
                changeDetailColor(-1);
            }
            performFullDetailRemove(tempTag, false); // avoid polygons with 1 ou 2 points
            imgTopBarBkgd.setBackgroundColor(Color.TRANSPARENT);
            setBtnsIcons();
        }
    }

    private float touchesVirtPoint(float locationX, float locationY) {
        float touched = -1;
        for(Map.Entry<Integer, ImageView> entry : virtPoints.entrySet()) {
            Integer id = entry.getKey();
            ImageView point = entry.getValue();

            float dist = distance(locationX, locationY, point.getX()+cornerWidth/2, point.getY()+cornerHeight/2);
            if (dist < precisionDist) {
                touched = id;
                break;
            }
        }

        return touched;
    }

    private class loadResource extends AsyncTask<Void, Void, Bitmap> {
        private final ImageView image;

        loadResource(ImageView im) {
            image = im;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE);

        }

        @Override
        protected Bitmap doInBackground(Void... arg0) {
            // Load background
            String imagePath = imagesDirectory + fileTitle + Constants.JPG_EXTENSION;
            float availableWidth = metrics.widthPixels;
            float availableHeight = metrics.heightPixels - toolbarHeight;

            final BitmapFactory.Options options = new BitmapFactory.Options();
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);

            float scaleX = availableWidth / bitmap.getWidth();
            float scaleY = availableHeight / bitmap.getHeight();
            scale = Math.min(scaleX, scaleY);

            xMin = (scaleX == scale) ? 0 : (availableWidth - bitmap.getWidth() * scale) / 2;
            yMin = (scaleY == scale) ? 0 : (availableHeight - bitmap.getHeight() * scale) / 2;

            options.inSampleSize = Util.calculateInSampleSize(options, Math.round(availableWidth) - 1, Math.round(availableHeight) - 1);
            options.inJustDecodeBounds = false;

            return BitmapFactory.decodeFile(imagePath, options);
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            mProgressBar.setVisibility(View.INVISIBLE);
            image.setImageBitmap(result);
            loadDetails(xml);
        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            detailInfos();
            return true;
        }
    }
}