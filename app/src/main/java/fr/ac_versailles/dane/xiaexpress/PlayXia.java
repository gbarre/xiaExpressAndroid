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
import android.text.method.ScrollingMovementMethod;
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
import android.widget.FrameLayout;
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

    float cornerWidth = 0;
    float cornerHeight = 0;
    Bitmap fullSizeBackground = null;
    private Document xml;
    private String fileTitle = "";
    private String imagesDirectory;
    private String xmlDirectory = "";
    private String cacheDirectory;
    private DisplayMetrics metrics;
    private float scale = 1;
    private float xMin = 0;
    private float yMin = 0;
    private Map<Integer, xiaDetail> details = new HashMap<>();
    private RelativeLayout detailsArea;
    private Boolean detailsLoaded = false;
    private Boolean showPopup = false;
    private Boolean showZoom = false;
    private Boolean enableZoom = true;
    private ImageView background = null;
    private LinearLayout playDetail = null;
    private RelativeLayout zoomDetail = null;
    private ImageView detailThumb = null;
    private ProgressBar mProgressBar;
    private RippleBackground rippleBackground;
    private int transitionDuration = 500; // in milliseconds

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
        cacheDirectory = Constants.getCacheFrom(rootDirectory);

        background = (ImageView) findViewById(R.id.image);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        fileTitle = getIntent().getStringExtra("fileTitle");
        xml = Util.getXMLFromPath(xmlDirectory + fileTitle + ".xml");

        playDetail = (LinearLayout) findViewById(R.id.playDetail);
        playDetail.setVisibility(View.INVISIBLE);

        zoomDetail = (RelativeLayout) findViewById(R.id.zoomDetail);
        zoomDetail.setVisibility(View.INVISIBLE);

        detailThumb = (ImageView) findViewById(R.id.detailThumb);
        detailThumb.setVisibility(View.INVISIBLE);

        rippleBackground = (RippleBackground) findViewById(R.id.content);

        fullSizeBackground = BitmapFactory.decodeFile(imagesDirectory + fileTitle + ".jpg");
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
        final String TAG = Thread.currentThread().getStackTrace()[2].getClassName() + "." + Thread.currentThread().getStackTrace()[2].getMethodName();
        // get pointer index from the event object
        //int pointerIndex = event.getActionIndex();

        // get pointer ID
        //int pointerId = event.getPointerId(pointerIndex);

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

                ImageView newShape = details.get(detailTag).createShape(this, false, Constants.blue, cornerWidth, cornerHeight, metrics, 0, drawEllipse, details.get(detailTag).locked);
                detailsArea.addView(newShape);
            }
        }
        detailsLoaded = true;
    }

    private void showDetail(Integer tag) {
        String TAG = Thread.currentThread().getStackTrace()[2].getClassName() + "." + Thread.currentThread().getStackTrace()[2].getMethodName();
        if (showPopup) {
            playDetail.setVisibility(View.INVISIBLE);
            zoomDetail.setVisibility(View.INVISIBLE);
            background.setVisibility(View.VISIBLE);
            detailsArea.setVisibility(View.VISIBLE);
            detailThumb.setVisibility(View.INVISIBLE);
        } else {
            //Boolean zoom = true;
            String detailTitle = "";
            String detailDescription = "";
            Boolean drawEllipse = false;

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

            // Prepare the detail "popup"
            int width = metrics.widthPixels * 8 / 10;
            int left = metrics.widthPixels * 1 / 10;
            int height = metrics.heightPixels * 8 / 10;
            int top = metrics.heightPixels * 1 / 10;

            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(width, height);
            lp.setMargins(left, top, left, top);
            playDetail.setLayoutParams(lp);

            // Put detail title
            TextView title = (TextView) findViewById(R.id.detailTitle);
            title.setText(detailTitle);

            // Put detail description
            TextView desc = (TextView) findViewById(R.id.detalDescription);
            desc.setMovementMethod(new ScrollingMovementMethod());
            desc.setText(detailDescription);

            Rect frame = details.get(tag).bezierFrame();

            // Extract part of image into the frame
            int xOri = Math.max(0, Math.round(frame.left - xMin / scale));
            int yOri = Math.max(0, Math.round(frame.top - yMin / scale));
            int wOri = (frame.width() + xOri > fullSizeBackground.getWidth()) ? fullSizeBackground.getWidth() - xOri : frame.width();
            int hOri = (frame.height() + yOri > fullSizeBackground.getHeight()) ? fullSizeBackground.getHeight() - yOri : frame.height();

            if (!drawEllipse) {
                wOri = (int) (wOri + cornerWidth / 2);
                hOri = (int) (hOri + cornerHeight / 2);
            }
            Bitmap bitmap = Bitmap.createBitmap(fullSizeBackground, xOri, yOri, wOri, hOri);

            // Prepare the mask
            ImageView newShapeMask = getShape(tag, bitmap);
            newShapeMask.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            Bitmap mask = getMask(newShapeMask);
            Bitmap result = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);

            // Put mask & original in canvas
            Canvas mCanvas = new Canvas(result);
            Paint paint = new Paint();
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
            mCanvas.drawBitmap(bitmap, 0, 0, null);
            mCanvas.drawBitmap(mask, 0, 0, paint);
            paint.setXfermode(null);

            // The thumbnail is ready !
            detailThumb.setImageBitmap(result);

            // Create the moving thumb
            ImageView movingThumb = new ImageView(this);
            movingThumb.setImageBitmap(result);
            detailsArea.addView(movingThumb);
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
            float targetX = left + detailThumb.getX() + cornerWidth / 2;
            float targetY = top + detailThumb.getY() + cornerHeight / 2;

            // Delta to center the movingThumb in the detailThumb area
            float deltaX = (wOri > hOri) ? 0 : (detailThumb.getWidth() - wOri * scale * targetScale) / 2;
            float deltaY = (hOri > wOri) ? 0 : (detailThumb.getHeight() - hOri * scale * targetScale) / 2;

            targetX = targetX + deltaX;
            targetY = targetY + deltaY;

            TranslateAnimation translate = new TranslateAnimation(0, targetX - detailX, 0, targetY - detailY);
            translate.setDuration(transitionDuration);

            // Combine animations
            AnimationSet animation = new AnimationSet(true);
            animation.addAnimation(scaling);
            animation.addAnimation(translate);
            animation.setFillEnabled(true);
            animation.setFillAfter(true);
            animation.setFillBefore(false);
            movingThumb.setAnimation(animation); // This will launch animations automatically

            // After animations, the moving thumb come back, we need to remove it (and other things)
            finishTransition endTransition = new finishTransition(movingThumb, playDetail, background, detailsArea, zoomDetail);
            endTransition.execute();

            // get the center for the clipping circle
            int cx = detailThumb.getWidth() / 2;
            int cy = detailThumb.getHeight() / 2;

            // get the final radius for the clipping circle
            float finalRadius = (float) Math.hypot(cx, cy);

            // create the animator for this view (the start radius is zero)
            detailThumb.setVisibility(View.VISIBLE);
            Animator anim =
                    null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                anim = ViewAnimationUtils.createCircularReveal(detailThumb, cx, cy, 0, finalRadius);
                // make the view visible and start the animation

                anim.start();
            }
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
        private ImageView image;

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
        private ImageView mThumb;
        private LinearLayout popup;
        private ImageView bkg;
        private RelativeLayout dArea;
        private RelativeLayout zoomD;

        finishTransition(ImageView t, LinearLayout p, ImageView b, RelativeLayout d, RelativeLayout z) {
            mThumb = t;
            popup = p;
            bkg = b;
            dArea = d;
            zoomD = z;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

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
            //mThumb.setVisibility(View.GONE);
            ((ViewManager) mThumb.getParent()).removeView(mThumb);
            popup.setVisibility(View.VISIBLE);
            bkg.setVisibility(View.INVISIBLE);
            dArea.setVisibility(View.INVISIBLE);
            zoomD.setVisibility(View.INVISIBLE);
        }
    }
}
