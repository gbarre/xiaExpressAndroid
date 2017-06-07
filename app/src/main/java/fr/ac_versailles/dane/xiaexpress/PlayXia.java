package fr.ac_versailles.dane.xiaexpress;

import android.animation.Animator;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewManager;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.skyfishjy.library.RippleBackground;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import static fr.ac_versailles.dane.xiaexpress.Constants.xmlElementsDict;

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

    private final Map<Integer, xiaDetail> details = new HashMap<>();
    private final int transitionDuration = 500; // in milliseconds
    private float cornerWidth = 0;
    private float cornerHeight = 0;
    private Bitmap fullSizeBackground = null;
    private Document xml;
    private String fileTitle = "";
    private String imagesDirectory;
    private DisplayMetrics metrics;
    private float scale = 1;
    private float xMin = 0;
    private float yMin = 0;
    private RelativeLayout detailsArea;
    private Boolean detailsLoaded = false;
    private Boolean showDetails = true;
    private Boolean showPopup = false;
    private Boolean showMetasPopup = false;
    private Boolean showZoom = false;
    private Boolean enableZoom = true;
    private ImageView background = null;
    private LinearLayout playDetail = null;
    private RelativeLayout playMetas = null;
    private RelativeLayout zoomDetail = null;
    private RelativeLayout movingArea = null;
    private ImageView detailThumb = null;
    private ProgressBar mProgressBar;
    private RippleBackground rippleBackground;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // full screen, no title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_play_xia);

        String rootDirectory = String.valueOf(getExternalFilesDir(null)) + File.separator;
        imagesDirectory = Constants.getImagesFrom(rootDirectory);
        String xmlDirectory = Constants.getXMLFrom(rootDirectory);
        Constants.buildXMLElements(this);

        background = (ImageView) findViewById(R.id.image);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        fileTitle = getIntent().getStringExtra("fileTitle");
        xml = Util.getXMLFromPath(xmlDirectory + fileTitle + ".xml");

        showDetails = (Util.getNodeAttribute(xml, "details", "show").equals("true"));

        playDetail = (LinearLayout) findViewById(R.id.playDetail);
        playDetail.setVisibility(View.INVISIBLE);

        playMetas = (RelativeLayout) findViewById(R.id.playMetas);
        playMetas.setVisibility(View.INVISIBLE);

        zoomDetail = (RelativeLayout) findViewById(R.id.zoomDetail);
        zoomDetail.setVisibility(View.INVISIBLE);

        detailThumb = (ImageView) findViewById(R.id.detailThumb);
        detailThumb.setVisibility(View.INVISIBLE);

        rippleBackground = (RippleBackground) findViewById(R.id.content);
        movingArea = (RelativeLayout) findViewById(R.id.movingArea);

        fullSizeBackground = BitmapFactory.decodeFile(imagesDirectory + fileTitle + ".jpg");

        ImageButton showImgInfos = (ImageButton) findViewById(R.id.showImgInfos);
        showImgInfos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDetail(0);
            }
        });

        ImageButton showMetas = (ImageButton) findViewById(R.id.showMetas);
        showMetas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMetas();
            }
        });

    }

    private void showMetas() {
        // Prepare the metas "popup"
        int width = metrics.widthPixels * 8 / 10;
        int left = metrics.widthPixels / 10;
        int height = metrics.heightPixels * 8 / 10;
        int top = metrics.heightPixels / 10;

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(width, height);
        lp.setMargins(left, top, left, top);
        playMetas.setLayoutParams(lp);

        // Get metadatas from xml
        for (Map.Entry<String, String> entry : xmlElementsDict.entrySet()) {
            String key = entry.getKey();
            setText(key);
        }

        // Animate the popup
        TranslateAnimation translatePopup = new TranslateAnimation(0, 0, metrics.heightPixels, 0);
        translatePopup.setDuration(transitionDuration);
        translatePopup.setFillEnabled(true);
        translatePopup.setFillBefore(true);
        translatePopup.setFillAfter(true);
        playMetas.setAnimation(translatePopup);

        // After animations, the moving thumb come back, we need to remove it (and other things)
        finishTransition endTransition = new finishTransition(null, background, detailsArea, zoomDetail);
        endTransition.execute();
        showMetasPopup = true;
        showPopup = true;

        ImageButton closeButton = (ImageButton) findViewById(R.id.close2);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDetail(0);
            }
        });
    }

    private void setText(String element) {
        String id = "document" + element.substring(0, 1).toUpperCase() + element.substring(1).toLowerCase();
        String text = Util.getNodeValue(xml, "xia/" + element);
        // no html !
        text = text.replaceAll("<", "&lt;");
        text = text.replaceAll(">", "&gt;");
        if (element.equals("description")) {
            WebView webV = (WebView) findViewById(R.id.documentDescription);
            new TextConverter(text, webV, 0, 0, this).execute();
        } else {
            TextView docElement = (TextView) findViewById(getResources().getIdentifier(id, "id", getPackageName()));
            if (!element.equals("title") && !element.equals("creator")) {
                text = "<b>" + xmlElementsDict.get(element) + ": </b>" + text;
            }
            docElement.setText(Util.fromHtml(text));
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus && !detailsLoaded) {
            // This is done after onCreate
            metrics = getResources().getDisplayMetrics();
            Bitmap corner = BitmapFactory.decodeResource(getResources(), R.drawable.corner);
            cornerWidth = corner.getWidth();
            cornerHeight = corner.getHeight();

            detailsArea = (RelativeLayout) findViewById(R.id.detailsArea);
            // AsyncTask loading => background resizing + details
            loadResource loading = new loadResource(background);
            loading.execute();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // get masked (not specific to a pointer) action
        int maskedAction = event.getActionMasked();
        float locationX = event.getX();
        float locationY = event.getY();

        switch (maskedAction) {

            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN: {

                if (showZoom) {
                    zoomDetail(!showZoom, detailThumb);
                } else if (!showPopup) {
                    // Look if touch a detail
                    for (Map.Entry<Integer, xiaDetail> entry : details.entrySet()) {
                        final Integer detailTag = entry.getKey();
                        xiaDetail detailPoints = entry.getValue();

                        Boolean touchIn = Util.pointInPolygon(detailPoints.points, locationX, locationY);
                        if (touchIn) {
                            rippleBackground.setX(locationX - metrics.widthPixels / 2);
                            rippleBackground.setY(locationY - metrics.heightPixels / 2);
                            rippleBackground.startRippleAnimation();
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    showDetail(detailTag);
                                    rippleBackground.stopRippleAnimation();
                                }
                            }, transitionDuration);
                            break;
                        }
                    }
                } else {
                    if (locationX < playDetail.getLeft() || locationX > playDetail.getRight() ||
                            locationY < playDetail.getTop() || locationY > playDetail.getBottom()) {
                        // Touch out the popup, close it !
                        showDetail(0);
                    } else if (locationX > playDetail.getLeft() && locationX < (playDetail.getLeft() + detailThumb.getWidth()) &&
                            locationY > playDetail.getTop() && locationY < (playDetail.getTop() + detailThumb.getHeight())
                            && enableZoom) {
                        zoomDetail(!showZoom, detailThumb);
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
                    if (!child.getTag().equals("20")) {
                        if ((Integer) child.getTag() == detailTag || (Integer) child.getTag() == detailTag + 100) {
                            detailsArea.removeView(child);
                        }
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

                ImageView newShape = details.get(detailTag).createShape(this, false, Constants.blue, cornerWidth, cornerHeight, metrics, 0, drawEllipse, details.get(detailTag).locked);
                detailsArea.addView(newShape);
                int visibility = (showDetails) ? View.VISIBLE : View.INVISIBLE;
                newShape.setVisibility(visibility);
            }
        }
        detailsLoaded = true;
    }

    private void showDetail(final Integer tag) {
        if (showPopup) { // Hide the popup
            // Animate the popup
            TranslateAnimation translatePopup = new TranslateAnimation(0, 0, 0, metrics.heightPixels);
            translatePopup.setDuration(transitionDuration);
            if (!showMetasPopup) {
                playDetail.setAnimation(translatePopup);
                playDetail.setVisibility(View.INVISIBLE);
            } else {
                playMetas.setAnimation(translatePopup);
                playMetas.setVisibility(View.INVISIBLE);
                showMetasPopup = false;
            }

            zoomDetail.setVisibility(View.INVISIBLE);
            //background.setVisibility(View.VISIBLE);
            background.setAlpha((float) 1.0);
            detailsArea.setVisibility(View.VISIBLE);
            //detailThumb.setVisibility(View.INVISIBLE);
        } else { // Show the popup
            //Boolean zoom = true;
            String detailTitle = "";
            String detailDescription = "";
            Boolean drawEllipse = false;

            // Prepare the detail "popup"
            int width = metrics.widthPixels * 8 / 10;
            int left = metrics.widthPixels / 10;
            int height = metrics.heightPixels * 8 / 10;
            int top = metrics.heightPixels / 10;

            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(width, height);
            lp.setMargins(left, top, left, top);
            playDetail.setLayoutParams(lp);

            if (tag != 0) { // show a detail
                // Load detail infos from xml
                NodeList xmlDetails = xml.getElementsByTagName("detail");
                for (int i = 0; i < xmlDetails.getLength(); i++) {
                    Node detail = xmlDetails.item(i);
                    NamedNodeMap detailAttr = detail.getAttributes();
                    Integer thisTag = Integer.valueOf(detailAttr.getNamedItem("tag").getTextContent());
                    if (thisTag.equals(tag)) { // we got it !
                        drawEllipse = details.get(tag).constraint.equals(Constants.constraintEllipse);
                        enableZoom = detailAttr.getNamedItem("zoom").getTextContent().equals("true");
                        detailTitle = detailAttr.getNamedItem("title").getTextContent();
                        detailDescription = detail.getTextContent();
                        break;
                    }
                }
            } else { // show image infos
                NodeList images = xml.getElementsByTagName("image");
                for (int i = 0; i < images.getLength(); i++) {
                    Node image = images.item(i);
                    NamedNodeMap imageAttr = image.getAttributes();
                    detailTitle = imageAttr.getNamedItem("title").getTextContent();
                    detailDescription = imageAttr.getNamedItem("description").getTextContent();
                    enableZoom = false;
                }
            }

            // Put detail title
            TextView title = (TextView) findViewById(R.id.detailTitle);
            title.setText(detailTitle);

            // Put detail description (with scrolling)
            WebView desc = (WebView) findViewById(R.id.detalDescription);
            desc.setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);
            desc.loadData(detailDescription, "text/html; charset=UTF-8", null);

            // look for oembed links
            new TextConverter(detailDescription, desc, 0, 0, this).execute();

            int xOri = 0;
            int yOri = 0;
            int wOri = fullSizeBackground.getWidth();
            int hOri = fullSizeBackground.getHeight();
            Bitmap result;

            if (tag != 0) {
                Rect frame = details.get(tag).bezierFrame();

                // Extract part of image into the frame
                xOri = Math.max(0, Math.round(frame.left - xMin / scale));
                yOri = Math.max(0, Math.round(frame.top - yMin / scale));
                wOri = (frame.width() + xOri + cornerWidth / 2 > fullSizeBackground.getWidth()) ? fullSizeBackground.getWidth() - xOri : frame.width();
                hOri = (frame.height() + yOri + cornerHeight / 2 > fullSizeBackground.getHeight()) ? fullSizeBackground.getHeight() - yOri : frame.height();

                Bitmap bitmap = Bitmap.createBitmap(fullSizeBackground, xOri, yOri, wOri, hOri);

                // Prepare the mask
                ImageView newShapeMask = getShape(tag, bitmap);
                newShapeMask.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                Bitmap mask = getMask(newShapeMask);
                result = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);

                // Put mask & original in canvas
                Canvas mCanvas = new Canvas(result);
                Paint paint = new Paint();
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
                mCanvas.drawBitmap(bitmap, 0, 0, null);
                mCanvas.drawBitmap(mask, 0, 0, paint);
                paint.setXfermode(null);
            } else {
                result = fullSizeBackground;
            }

            // The thumbnail is ready !
            detailThumb.setImageBitmap(result);

            // Create the moving thumb
            ImageView movingThumb = new ImageView(this);
            movingThumb.setImageBitmap(result);
            movingArea.addView(movingThumb);
            movingThumb.getLayoutParams().width = (int) (wOri * scale);
            movingThumb.getLayoutParams().height = (int) (hOri * scale);
            float detailX = xOri * scale + xMin + cornerWidth / 2;
            float detailY = yOri * scale + yMin + cornerHeight / 2;
            movingThumb.setX(detailX);
            movingThumb.setY(detailY);

            // Prepare the scaling animation
            float targetScaleX = detailThumb.getWidth() / (wOri * scale);
            float targetScaleY = detailThumb.getHeight() / (hOri * scale);
            float targetScale = Math.min(targetScaleX, targetScaleY);
            ScaleAnimation scaling = new ScaleAnimation(
                    1, targetScale,
                    1, targetScale,
                    detailX, detailY
            );
            scaling.setDuration(transitionDuration);

            // Prepare the translate animation
            float targetX = left + detailThumb.getX();
            float targetY = top + detailThumb.getY();

            // Delta to center the movingThumb in the detailThumb area
            float deltaX = (wOri > hOri) ? 0 : (detailThumb.getWidth() - wOri * scale * targetScale) / 2;
            float deltaY = (hOri > wOri) ? 0 : (detailThumb.getHeight() - hOri * scale * targetScale) / 2;

            targetX = targetX + deltaX;
            targetY = targetY + deltaY;

            TranslateAnimation translateThumb = new TranslateAnimation(0, targetX - detailX, 0, targetY - detailY);
            translateThumb.setDuration(transitionDuration);

            // Combine animations
            AnimationSet animation = new AnimationSet(true);
            animation.addAnimation(scaling);
            animation.addAnimation(translateThumb);
            animation.setFillEnabled(true);
            animation.setFillAfter(true);
            animation.setFillBefore(false);
            movingThumb.setAnimation(animation); // This will launch animations automatically

            // Animate the popup
            TranslateAnimation translatePopup = new TranslateAnimation(0, 0, metrics.heightPixels, 0);
            translatePopup.setDuration(transitionDuration);
            translatePopup.setFillEnabled(true);
            translatePopup.setFillBefore(true);
            translatePopup.setFillAfter(true);
            playDetail.setAnimation(translatePopup);

            // After animations, the moving thumb come back, we need to remove it (and other things)
            finishTransition endTransition = new finishTransition(movingThumb, background, detailsArea, zoomDetail);
            endTransition.execute();

            // get the center for the clipping circle
            int cx = detailThumb.getWidth() / 2;
            int cy = detailThumb.getHeight() / 2;

            // get the final radius for the clipping circle
            float finalRadius = (float) Math.hypot(cx, cy);

            // create the animator for this view (the start radius is zero)
            detailThumb.setVisibility(View.VISIBLE);
            Animator anim;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                anim = ViewAnimationUtils.createCircularReveal(detailThumb, cx, cy, 0, finalRadius);
                // start the animation
                anim.start();
            }

            ImageButton closeButton = (ImageButton) findViewById(R.id.close);
            closeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showDetail(tag);
                }
            });
        }
        showPopup = !showPopup;
    }

    private void zoomDetail(Boolean show, ImageView im) {
        if (show) {
            ImageView detail_zoom = (ImageView) findViewById(R.id.detail_zoom);
            detail_zoom.setImageDrawable(im.getDrawable());
            playDetail.setVisibility(View.INVISIBLE);
            zoomDetail.setVisibility(View.VISIBLE);
        } else {
            playDetail.setVisibility(View.VISIBLE);
            zoomDetail.setVisibility(View.INVISIBLE);
        }
        showZoom = !showZoom;
    }

    private Bitmap getMask(ImageView im) {
        // This "convert" the im ImageView, from cache, into a Bitmap
        im.setDrawingCacheEnabled(true);
        im.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        im.layout(0, 0, im.getMeasuredWidth(), im.getMeasuredHeight());
        im.buildDrawingCache(true);
        Bitmap m = Bitmap.createBitmap(im.getWidth(), im.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(m);
        im.draw(c);
        im.setDrawingCacheEnabled(false);

        return m;
    }

    private ImageView getShape(Integer tag, Bitmap bitmap) {
        // Extract the shape from the detail path
        // Similar to the xiaDetail.createShape method
        Boolean drawEllipse = details.get(tag).constraint.equals(Constants.constraintEllipse);

        ImageView shapeView = new ImageView(this);
        ShapeDrawable shape = new ShapeDrawable();
        GradientDrawable drawable = new GradientDrawable();
        Rect frame = details.get(tag).bezierFrame();
        Map<Integer, ImageView> points = details.get(tag).points;

        if (drawEllipse) {
            int width = Math.abs(Math.round((points.get(1).getX() - points.get(3).getX()) / scale)) + 2;
            int height = Math.abs(Math.round((points.get(0).getY() - points.get(2).getY()) / scale)) + 2;
            float x = Math.min(points.get(1).getX(), points.get(3).getX()) / scale - frame.left - 1;
            float y = Math.min(points.get(0).getY(), points.get(2).getY()) / scale - frame.top - 1;

            drawable.setShape(GradientDrawable.OVAL);
            drawable.setSize(width, height);
            shapeView.setX(x);
            shapeView.setY(y);

            shapeView.setBackground(drawable);

        } else {
            Path p = new Path();
            p.reset();

            ImageView endPoint = new ImageView(this);
            SortedSet<Integer> keys = new TreeSet<>(points.keySet());
            for (Integer key : keys) {
                ImageView point = points.get(key);

                float x = point.getX() / scale + cornerWidth / 2 - frame.left;
                float y = point.getY() / scale + cornerHeight / 2 - frame.top;

                if (key != 0) {
                    p.lineTo(x, y);
                } else {
                    p.moveTo(x, y);
                    endPoint = point;
                }
            }
            p.lineTo(endPoint.getX() / scale + cornerWidth / 2 - frame.left, endPoint.getY() / scale + cornerHeight / 2 - frame.top);
            float stdWidth = bitmap.getWidth();
            float stdHeight = bitmap.getHeight();

            shape = new ShapeDrawable(new PathShape(p, stdWidth, stdHeight));
            shape.setIntrinsicWidth(Math.round(stdWidth));
            shape.setIntrinsicHeight(Math.round(stdHeight));
            shapeView.setBackground(shape);
        }

        shape.getPaint().setStyle(Paint.Style.FILL);
        shape.getPaint().setColor(Constants.white);
        drawable.setColor(Constants.white);
        shapeView.setVisibility(View.VISIBLE);

        return shapeView;
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
            String imagePath = imagesDirectory + fileTitle + ".jpg";
            float availableWidth = metrics.widthPixels;
            float availableHeight = metrics.heightPixels;

            final BitmapFactory.Options options = new BitmapFactory.Options();
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);
            //fullSizeBackground = bitmap;

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

    private class finishTransition extends AsyncTask<Void, Void, Void> {
        private final ImageView mThumb;
        private final ImageView bkg;
        private final RelativeLayout dArea;
        private final RelativeLayout zoomD;

        finishTransition(ImageView t, ImageView b, RelativeLayout d, RelativeLayout z) {
            mThumb = t;
            bkg = b;
            dArea = d;
            zoomD = z;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Thread.sleep(transitionDuration);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (mThumb != null) {
                ((ViewManager) mThumb.getParent()).removeView(mThumb);
            }
            bkg.setAlpha((float) 0.4);
            dArea.setVisibility(View.INVISIBLE);
            zoomD.setVisibility(View.INVISIBLE);
        }
    }
}
