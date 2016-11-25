package fr.ac_versailles.dane.xiaexpress;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

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

    private Boolean showPopup = false;

    private ImageView background = null;
    private LinearLayout playDetail = null;
    private RelativeLayout zoomDetail = null;
    Bitmap fullSizeBackground = null;


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

        playDetail = (LinearLayout) findViewById(R.id.playDetail);
        playDetail.setVisibility(View.INVISIBLE);

        zoomDetail = (RelativeLayout) findViewById(R.id.zoomDetail);
        zoomDetail.setVisibility(View.INVISIBLE);

        //fullSizeBackground = BitmapFactory.decodeFile(imagesDirectory + fileTitle + ".jpg");
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
        float locationY = event.getY();

        switch (maskedAction) {

            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN: {

                if (!showPopup) {
                    // Look if we try to move a detail
                    for (Map.Entry<Integer, xiaDetail> entry : details.entrySet()) {
                        Integer detailTag = entry.getKey();
                        xiaDetail detailPoints = entry.getValue();

                        Boolean touchIn = Util.pointInPolygon(detailPoints.points, locationX, locationY);
                        if (touchIn) {
                            showDetail(detailTag);
                            break;
                        }
                    }
                }
                else {
                    if (locationX < playDetail.getLeft() || locationX > playDetail.getRight() ||
                            locationY < playDetail.getTop() || locationY > playDetail.getBottom()) {
                        // Touch out the popup, close it !
                        showDetail(0);
                    }
                }
                break;
            }
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL: {
                break;
            }
        }


        return true;
    }

    private void loadBackground(String imagePath) {
        background = (ImageView) findViewById(R.id.image);

        float availableWidth = metrics.widthPixels;
        float availableHeight = metrics.heightPixels;

        final BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);
        fullSizeBackground = bitmap;

        float scaleX = availableWidth / bitmap.getWidth();
        float scaleY = availableHeight / bitmap.getHeight();
        scale = Math.min(scaleX, scaleY);

        xMin = (scaleX == scale) ? 0 : (availableWidth - bitmap.getWidth()*scale) / 2;
        yMin = (scaleY == scale) ? 0 : (availableHeight - bitmap.getHeight()*scale) / 2;

        bitmap = null;

        options.inSampleSize = Util.calculateInSampleSize(options, Math.round(availableWidth) - 1, Math.round(availableHeight) - 1);
        options.inJustDecodeBounds = false;
        Bitmap image = BitmapFactory.decodeFile(imagePath, options);

        background.setImageBitmap(image);
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
                details.get(detailTag).constraint = detailAttr.getNamedItem("constraint").getTextContent();

                // Add points to detail
                String[] pointsArray = path.split(" ");
                Integer pointIndex = 0;
                for (String aPointsArray : pointsArray) {
                    String[] coords = aPointsArray.split(";");
                    Float x = Float.parseFloat(coords[0]) * scale + xMin - cornerWidth / 2;
                    Float y = Float.parseFloat(coords[1]) * scale + yMin - cornerHeight / 2;
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

    private void showDetail(Integer tag) {
        if (showPopup) {
            playDetail.setVisibility(View.INVISIBLE);
            zoomDetail.setVisibility(View.INVISIBLE);
            background.setVisibility(View.VISIBLE);
            detailsArea.setVisibility(View.VISIBLE);
        }
        else {
            Boolean zoom = true;
            String detailTitle = "";
            String detailDescription = "";

            // Load detail infos from xml
            NodeList xmlDetails = xml.getElementsByTagName("detail");
            for (int i = 0; i < xmlDetails.getLength(); i++) {
                Node detail = xmlDetails.item(i);
                NamedNodeMap detailAttr = detail.getAttributes();
                Integer thisTag = Integer.valueOf(detailAttr.getNamedItem("tag").getTextContent());
                if (thisTag.equals(tag)) { // we got it !
                    zoom = detailAttr.getNamedItem("zoom").getTextContent().equals("true");
                    detailTitle = detailAttr.getNamedItem("title").getTextContent();
                    detailDescription = detail.getTextContent();
                    break;
                }
            }

            playDetail.setVisibility(View.VISIBLE);
            Integer width = metrics.widthPixels * 8 / 10;
            Integer left = metrics.widthPixels * 1 / 10;
            Integer height = metrics.heightPixels * 8 / 10;
            Integer top = metrics.heightPixels * 1 / 10;

            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(width, height);
            lp.setMargins(left, top, left, top);
            playDetail.setLayoutParams(lp);

            TextView title = (TextView) findViewById(R.id.detailTitle);
            title.setText(detailTitle);

            TextView desc = (TextView) findViewById(R.id.detalDescription);
            desc.setMovementMethod(new ScrollingMovementMethod());
            desc.setText(detailDescription);

            ImageView detailThumb = (ImageView) findViewById(R.id.detailThumb);

            Rect frame = details.get(tag).bezierFrame();
            dbg.pt("showDetail", "fullSizeBackground", fullSizeBackground.getWidth() + " x " + fullSizeBackground.getHeight());
            dbg.pt("showDetail", "metrics", metrics.widthPixels + " x " + metrics.heightPixels);
            int xOri = Math.max(0, Math.round(frame.left - xMin / scale));
            int yOri = Math.max(0, Math.round(frame.top - yMin / scale));
            int wOri = (frame.width() + xOri > fullSizeBackground.getWidth()) ? fullSizeBackground.getWidth() - xOri : frame.width();
            int hOri = (frame.height() + yOri > fullSizeBackground.getHeight()) ? fullSizeBackground.getHeight() - yOri : frame.height();
            dbg.pt("shoDetail", "Ori", xOri + ", " + yOri + ", " + wOri + ", " + hOri);
            Bitmap original = Bitmap.createBitmap(fullSizeBackground, xOri, yOri, wOri, hOri);
            /*ImageView newShape = getShape(tag);
            Bitmap mask = getMask(newShape);
            Bitmap result = Bitmap.createBitmap(original.getWidth(), original.getHeight(), Bitmap.Config.ARGB_8888);

            Canvas mCanvas = new Canvas(result);
            Paint paint = new Paint();
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
            mCanvas.drawBitmap(original, 0, 0, null);
            mCanvas.drawBitmap(mask, newShape.getX(), newShape.getY(), paint);
            paint.setXfermode(null);
            detailThumb.setImageBitmap(result);*/
            detailThumb.setImageBitmap(original);

            dbg.pt("showDetail", "tag", tag);

            playDetail.setVisibility(View.VISIBLE);
            background.setVisibility(View.INVISIBLE);
            detailsArea.setVisibility(View.INVISIBLE);
            zoomDetail.setVisibility(View.INVISIBLE);



        }
        showPopup = !showPopup;
    }

    private Bitmap getMask(ImageView im) {
        im.setDrawingCacheEnabled(true);
        im.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        im.layout(0, 0, im.getMeasuredWidth(), im.getMeasuredHeight());
        im.buildDrawingCache(true);
        Bitmap m = Bitmap.createBitmap(im.getDrawingCache());
        im.setDrawingCacheEnabled(false);

        return m;
    }

    private ImageView getShape(Integer tag) {
        Boolean drawEllipse = details.get(tag).constraint.equals(Constants.constraintEllipse);

        ImageView shapeView = new ImageView(this);
        ShapeDrawable shape = new ShapeDrawable();
        GradientDrawable drawable = new GradientDrawable();
        Rect frame = details.get(tag).bezierFrame();
        Map<Integer, ImageView> points = details.get(tag).points;

        if (drawEllipse) {
            int width = Math.abs(Math.round(points.get(1).getX() - points.get(3).getX()));
            int height = Math.abs(Math.round(points.get(0).getY() - points.get(2).getY()));
            float x = Math.min(points.get(1).getX(), points.get(3).getX()) - frame.left;
            float y = Math.min(points.get(0).getY(), points.get(2).getY()) - frame.top;

            drawable.setShape(GradientDrawable.OVAL);
            drawable.setSize(width, height);
            shapeView.setX(x - cornerWidth);
            shapeView.setY(y - cornerHeight);

            shapeView.setBackground(drawable);

        }
        else {
            Path p = new Path();
            p.reset();

            ImageView endPoint = new ImageView(this);
            SortedSet<Integer> keys = new TreeSet<>(points.keySet());
            for (Integer key : keys) {
                ImageView point = points.get(key);

                float x = point.getX() + cornerWidth / 2 - frame.left;
                float y = point.getY() + cornerHeight / 2 - frame.top;

                if (key != 0) {
                    p.lineTo(x, y);
                } else {
                    p.moveTo(x, y);
                    endPoint = point;
                }
            }
            p.lineTo(endPoint.getX() + cornerWidth / 2 - frame.left, endPoint.getY() + cornerHeight / 2 - frame.top);
            shape = new ShapeDrawable(new PathShape(p, frame.width(), frame.height()));
            shape.setIntrinsicWidth(frame.width());
            shape.setIntrinsicHeight(frame.height());
            shapeView.setBackground(shape);
        }

        shape.getPaint().setStyle(Paint.Style.FILL);
        shape.getPaint().setColor(Constants.white);
        drawable.setColor(Constants.white);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            shapeView.setZ(2);
        }
        shapeView.setVisibility(View.VISIBLE);
        shapeView.setTag(100);

        return shapeView;

        //return details.get(tag).createShape(this, true, Constants.white, cornerWidth, cornerHeight, metrics, 0, drawEllipse, false);

    }
}
