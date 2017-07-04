package fr.ac_versailles.dane.xiaexpress;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import android.widget.TextView;

import com.mvc.imagepicker.ImagePicker;

import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;

/**
 *  MainActivity.java
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


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    static final int PICK_CONTACT_REQUEST = 2;
    //rprivate static final int SELECT_PICTURE = 1;
    private final ArrayList<String> arrayNames = new ArrayList<>();
    String tmpFilePath;
    private GridViewAdapter gridAdapter;
    private GridView gridView;
    private String rootDirectory;
    private String imagesDirectory;
    private String xmlDirectory;
    private String cacheDirectory;
    private int nbThumb = 0;
    // menu elements
    private Boolean isEditing = false;
    private ArrayList<Integer> selectedItems = new ArrayList<>();
    private ImageButton btnTrash, btnExport, btnEdit, btnDuplicate, btnAdd;
    private Button btnEditMode;
    private ListPopupWindow exportPopupWindow;
    private String[] exportsType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // full screen, no title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        // Get menu buttons
        btnTrash = (ImageButton) findViewById(R.id.trash);
        btnExport = (ImageButton) findViewById(R.id.export);
        btnEdit = (ImageButton) findViewById(R.id.edit);
        btnDuplicate = (ImageButton) findViewById(R.id.duplicate);
        btnEditMode = (Button) findViewById(R.id.editMode);
        btnAdd = (ImageButton) findViewById(R.id.add);

        rootDirectory = String.valueOf(getExternalFilesDir(null)) + File.separator;
        imagesDirectory = Constants.getImagesFrom(rootDirectory);
        xmlDirectory = Constants.getXMLFrom(rootDirectory);
        cacheDirectory = Constants.getCacheFrom(rootDirectory);

        Util.createDirectory(imagesDirectory);
        Util.createDirectory(xmlDirectory);
        Util.createDirectory(cacheDirectory);

        setGridView();
        ImagePicker.setMinQuality(400, 400);

        btnAdd.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                ImagePicker.pickImage(MainActivity.this, getResources().getString(R.string.select_picture));
            }
        });

        // left Menu listeners
        btnTrash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = (selectedItems.size() == 1) ? getResources().getString(R.string.delete_file) : String.format(getResources().getString(R.string.delete_n_files), selectedItems.size());
                // Alert
                AlertDialog.Builder controller = new AlertDialog.Builder(MainActivity.this);
                controller.setTitle(title);
                controller.setPositiveButton(getResources().getString(R.string.YES), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Collections.sort(selectedItems, Collections.<Integer>reverseOrder());
                        for (int item : selectedItems) {
                            deleteFiles(item);
                        }
                        selectedItems = new ArrayList<>();
                        endEdit();
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
        });

        exportsType = new String[]{getResources().getString(R.string.xia_tablet), getResources().getString(R.string.inkscape)};
        // Build the export menu
        exportPopupWindow = new ListPopupWindow(this);
        exportPopupWindow.setAdapter(new ArrayAdapter(MainActivity.this, R.layout.list_item, exportsType));
        exportPopupWindow.setAnchorView(btnExport);
        exportPopupWindow.setWidth(185);
        exportPopupWindow.setVerticalOffset(10);
        exportPopupWindow.setModal(true);
        exportPopupWindow.setOnItemClickListener(this);

        btnExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exportPopupWindow.show();
            }
        });

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fileTitle = arrayNames.get(selectedItems.get(0));
                Intent intent = new Intent(MainActivity.this, Metas.class);
                intent.putExtra("fileTitle", fileTitle);
                startActivity(intent);
            }
        });

        btnDuplicate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Alert
                AlertDialog.Builder controller = new AlertDialog.Builder(MainActivity.this);
                controller.setTitle(getResources().getString(R.string.DUPLICATE_FILE));
                controller.setPositiveButton(getResources().getString(R.string.YES), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        long now = System.currentTimeMillis();
                        String fileToDuplicate = arrayNames.get(selectedItems.get(0));
                        try {
                            InputStream in = new FileInputStream(new File(imagesDirectory + fileToDuplicate + ".jpg"));
                            Util.copy(in, new File(imagesDirectory + now + ".jpg"));
                            in = new FileInputStream(new File(xmlDirectory + fileToDuplicate + ".xml"));
                            Util.copy(in, new File(xmlDirectory + now + ".xml"));
                            in = new FileInputStream(new File(cacheDirectory + fileToDuplicate + ".jpg"));
                            Util.copy(in, new File(cacheDirectory + now + ".jpg"));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        endEdit();
                        arrayNames.add(arrayNames.size(), String.valueOf(now));
                        Bitmap bitmap = decodeSampledBitmapFromFile(imagesDirectory + now + ".jpg", 150, 150);
                        gridAdapter.add(new PhotoThumbnail(bitmap, String.valueOf(now)));
                        gridAdapter.notifyDataSetChanged();
                        gridView.setAdapter(gridAdapter);
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
        });

        btnEditMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isEditing = !isEditing;
                LinearLayout leftMenu = (LinearLayout) findViewById(R.id.leftMenu);
                LinearLayout menu = (LinearLayout) findViewById(R.id.mainMenu);
                if (isEditing) {
                    leftMenu.setVisibility(View.VISIBLE);
                    menu.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.orange));
                } else {
                    leftMenu.setVisibility(View.INVISIBLE);
                    menu.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.blue));
                    selectedItems = new ArrayList<>();
                    gridView.invalidateViews();
                    gridAdapter.notifyDataSetChanged();
                    gridView.setAdapter(gridAdapter);
                }
                buildLeftNavbarItems(selectedItems.size());
            }
        });

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (exportPopupWindow.isShowing()) {
            exportPopupWindow.dismiss();
            exportResource(position);
        }

    }

    // Store the image
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode != PICK_CONTACT_REQUEST) {
                gridAdapter.setEmpty(nbThumb == 0);
                if (gridAdapter.getEmpty()) {
                    gridAdapter.deleteItem(0);
                    gridAdapter.notifyDataSetChanged();
                }
                Bitmap bmp = ImagePicker.getImageFromResult(this, requestCode, resultCode, data);
                // Copy file to documentsDirectory
                long now = System.currentTimeMillis();

                try {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    bmp.compress(Bitmap.CompressFormat.JPEG, 85, bos);
                    byte[] bitmapdata = bos.toByteArray();
                    ByteArrayInputStream bs = new ByteArrayInputStream(bitmapdata);
                    Util.copy(bs, new File(imagesDirectory + now + ".jpg"));
                    nbThumb = nbThumb + 1;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Bitmap bitmap = decodeSampledBitmapFromFile(imagesDirectory + now + ".jpg", 150, 150);
                String imgName = new File(imagesDirectory + now + ".jpg").getName();
                gridAdapter.add(new PhotoThumbnail(bitmap, imgName));
                int position = arrayNames.size();
                arrayNames.add(position, imgName.replace(".jpg", ""));

                // Create xml file
                Util.createXiaXML(xmlDirectory + now + ".xml");

                gridAdapter.notifyDataSetChanged();
                gridView.setAdapter(gridAdapter);
                gridAdapter.setEmpty(false);
            }
        }
        if (requestCode == PICK_CONTACT_REQUEST) {
            // Make sure the request was successful
            File file = new File(tmpFilePath);
            file.delete();
        }
    }

    private void buildLeftNavbarItems(int items) {
        int color = (isEditing) ? ContextCompat.getColor(MainActivity.this, R.color.orange) : ContextCompat.getColor(MainActivity.this, R.color.blue);
        btnTrash.setBackgroundColor(color);
        btnExport.setBackgroundColor(color);
        btnEdit.setBackgroundColor(color);
        btnDuplicate.setBackgroundColor(color);
        btnAdd.setBackgroundColor(color);
        btnEditMode.setBackgroundColor(color);
        TextView title = (TextView) findViewById(R.id.title);
        title.setBackgroundColor(color);
        if (items == 1) {
            btnTrash.setVisibility(View.VISIBLE);
            btnExport.setVisibility(View.VISIBLE);
            btnEdit.setVisibility(View.VISIBLE);
            btnDuplicate.setVisibility(View.VISIBLE);
        } else if (items > 1) {
            btnTrash.setVisibility(View.VISIBLE);
            btnExport.setVisibility(View.INVISIBLE);
            btnEdit.setVisibility(View.INVISIBLE);
            btnDuplicate.setVisibility(View.INVISIBLE);
        } else {
            btnTrash.setVisibility(View.INVISIBLE);
            btnExport.setVisibility(View.INVISIBLE);
            btnEdit.setVisibility(View.INVISIBLE);
            btnDuplicate.setVisibility(View.INVISIBLE);
        }
    }

    private Bitmap decodeSampledBitmapFromFile(String path, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize
        options.inSampleSize = Util.calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        // Copy file to cacheDirectory
        String imgName = path.substring(path.lastIndexOf("/")+1);
        File cachedImage = new File(cacheDirectory+imgName);
        try {
            cachedImage.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Bitmap bitmap = BitmapFactory.decodeFile(path,options);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, bos);
        byte[] bitmapData = bos.toByteArray();

        try {
            FileOutputStream fos = new FileOutputStream(cachedImage);
            fos.write(bitmapData);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return BitmapFactory.decodeFile(String.valueOf(cachedImage));
    }

    private void deleteFiles(int index) {
        String fileName = arrayNames.get(index);
        File file = new File(cacheDirectory + fileName + ".jpg");
        file.delete();
        file = new File(imagesDirectory + fileName + ".jpg");
        file.delete();
        file = new File(xmlDirectory + fileName + ".xml");
        file.delete();
        arrayNames.remove(index);
        gridAdapter.deleteItem(index);
        gridAdapter.notifyDataSetChanged();
        gridView.setAdapter(gridAdapter);
    }

    private void endEdit() {
        isEditing = false;
        buildLeftNavbarItems(0);
        if (gridAdapter.getSize() == 0) {
            gridAdapter.setEmpty(true);
            nbThumb = 0;
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.plus200);
            gridAdapter.add(new PhotoThumbnail(bitmap, getResources().getString(R.string.new_resource)));
        }
        gridAdapter.notifyDataSetChanged();
        gridView.setAdapter(gridAdapter);
        LinearLayout menu = (LinearLayout) findViewById(R.id.mainMenu);
        menu.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.blue));

    }

    private void exportResource(int type) {
        String exportXMLString = "";
        String fileName = arrayNames.get(selectedItems.get(0));
        Document xml = Util.getXMLFromPath(xmlDirectory + fileName + ".xml");
        String tmpTitle = (Util.getNodeValue(xml, "xia/title").equals("")) ? fileName : Util.getNodeValue(xml, "xia/title");
        tmpTitle = Util.cleanInput(tmpTitle);
        Export export = new Export(xml, imagesDirectory + fileName + ".jpg");
        switch (type) {
            case 0: // Xia Tablet
                exportXMLString = export.xiaTablet();
                tmpFilePath = cacheDirectory + tmpTitle + ".xml";
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
            startActivityForResult(Intent.createChooser(shareIntent, getResources().getString(R.string.export)), PICK_CONTACT_REQUEST);
        }
    }

    private ArrayList<PhotoThumbnail> getData() {
        final ArrayList<PhotoThumbnail> imageItems = new ArrayList<>();
        File dir = new File(imagesDirectory);
        File[] imgs = dir.listFiles();

        if (imgs != null && imgs.length > 0) {
            nbThumb = imgs.length;
            for (int i = 0; i < imgs.length; i++) {
                String imgName = imgs[i].getName().replace(".jpg", "");
                arrayNames.add(i, imgName);

                Bitmap bitmap;
                // Check if image is in cacheDirectory
                if (new File(cacheDirectory + imgName).exists()) {
                    bitmap = BitmapFactory.decodeFile(cacheDirectory + imgName);
                } else {
                    bitmap = decodeSampledBitmapFromFile(imgs[i].toString(), 150, 150);
                }

                imageItems.add(new PhotoThumbnail(bitmap, imgName));
            }
        } else {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.plus200);
            imageItems.add(new PhotoThumbnail(bitmap, getResources().getString(R.string.new_resource)));
        }
        return imageItems;
    }

    private void setGridView() {
        // Load the collection in grid view
        gridView = (GridView) findViewById(R.id.gridView);
        gridAdapter = new GridViewAdapter(this, R.layout.grid_item_layout, getData(), xmlDirectory);
        gridView.setAdapter(gridAdapter);
        gridAdapter.setEmpty(nbThumb == 0);

        // Add listener on collection
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                PhotoThumbnail item = (PhotoThumbnail) parent.getItemAtPosition(position);
                View element = parent.getChildAt(position);

                if (isEditing) {
                    if (selectedItems.contains(position)) {
                        for (int i = 0; i < selectedItems.size(); i++) {
                            if (selectedItems.get(i) == position) {
                                selectedItems.remove(i);
                                element.setBackgroundColor(Color.TRANSPARENT);
                                break;
                            }
                        }
                    } else {
                        selectedItems.add(position);
                        element.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.orange));
                    }
                    Collections.sort(selectedItems, Collections.<Integer>reverseOrder());
                    buildLeftNavbarItems(selectedItems.size());
                } else { //Create intent
                    if (nbThumb > 0) {
                        Document xml = Util.getXMLFromPath(xmlDirectory + item.getFilename().replace(".jpg", "") + ".xml");
                        Boolean readOnly = Util.getNodeValue(xml, "xia/readonly").equals("true");
                        Intent intent;
                        if (readOnly) {
                            intent = new Intent(MainActivity.this, PlayXia.class);
                        } else {
                            intent = new Intent(MainActivity.this, CreateDetailActivity.class);
                        }
                        intent.putExtra("filename", item.getFilename());

                        //Start details activity
                        startActivity(intent);
                    } else {
                        ImagePicker.pickImage(MainActivity.this, getResources().getString(R.string.select_picture));
                    }
                    selectedItems = new ArrayList<>();
                }
            }
        });
    }

}
